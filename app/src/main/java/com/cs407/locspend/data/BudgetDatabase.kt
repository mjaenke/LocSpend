package com.cs407.locspend.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import com.cs407.locspend.R
import java.util.Date

// User entity with unique username
@Entity (
    indices = [Index(
        value = ["userName"], unique = true
    )]
)
data class User (
    @PrimaryKey(autoGenerate = true) val userId : Int = 0,
    val userName : String = ""
)

// Converter class Date -> Long
class Converters {
    // Converts Timestamp (long) to Date object
    @TypeConverter
    fun fromTimestamp(value: Long) : Date {
        return Date(value)
    }

    // Converts Date object to Timestamp (long)
    @TypeConverter
    fun dateToTimestamp(date: Date) : Long {
        return date.time
    }
}

// Budget Entity with primary key and various fields including nullable fields
@Entity
data class Budget (
    @PrimaryKey(autoGenerate = true) val budgetId : Int = 0,
    val budgetCategory: String,
    var budgetAmount: Double,
    val budgetSpent: Double,

    @ColumnInfo(typeAffinity = ColumnInfo.TEXT) val budgetDetail : String?,
    val budgetPath : String?,
    val lastEdited : Date
)

@Entity (
    primaryKeys = ["userId", "budgetId"],
    foreignKeys = [ForeignKey(
        entity = User::class, // Foreign key referencing User
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Budget::class, // Foreign key referencing Budget
        parentColumns = ["budgetId"],
        childColumns = ["budgetId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class UserBudgetRelation (
    val userId : Int,
    val budgetId : Int
)

data class BudgetSummary (
    val budgetId: Int,
    val budgetCategory : String,
    val budgetAmount : Double,
    val lastEdited : Date
)

// DAO (Data Accessing Object) for interacting with User Entity
@Dao
interface UserDao {
    // Query to to get User by userName
    @Query("SELECT * FROM user WHERE userName = :name")
    suspend fun getByName(name : String): User

    // Query to get User by userId
    @Query("SELECT * FROM user WHERE userId = :id")
    suspend fun getById(id : Int) : User

    // Query to get BudgetSummary from user (ordered by lastEdited)
    @Query(
        """SELECT * FROM User, Budget, UserBudgetRelation
            WHERE User.userId = :id
            AND UserBudgetRelation.userId = User.userId
            And Budget.budgetId = UserBudgetRelation.budgetId
            ORDER BY Budget.lastEdited DESC"""
    )
    suspend fun getUsersWithBudgetListsById(id : Int) : List<BudgetSummary>

    // Same query as above but returns PagingSource for pagination
    @Query(
        """SELECT * FROM User, Budget, UserBudgetRelation
            WHERE User.userId = :id
            AND UserBudgetRelation.userId = User.userId
            AND Budget.budgetId = UserBudgetRelation.budgetId
            ORDER BY Budget.lastEdited DESC
        """
    )
    fun getUsersWithBudgetListsByIdPaged(id : Int): PagingSource<Int, BudgetSummary>

    // Insert New User into DB
    @Insert (entity = User::class)
    suspend fun insert(user : User)
}

@Dao
interface BudgetDao {
    // Query to get Budget by budgetId
    @Query("SELECT * FROM budget WHERE budgetId = :id")
    suspend fun getById(id : Int) : Budget

    // Query to get Budget by category
    @Query("""
        SELECT * FROM Budget 
        WHERE budgetCategory = :category 
        AND budgetId IN (
            SELECT budgetId FROM UserBudgetRelation WHERE userId = :userId
        )
    """)
    suspend fun getByCategory(category : String, userId : Int) : Budget

    // Query to get BudgetId by its rowId
    @Query("SELECT budgetId FROM Budget WHERE rowid = :rowId")
    suspend fun getByRowId(rowId: Long) : Int

    // Insert or Update a Budget
    @Upsert(entity = Budget::class)
    suspend fun upsert(budget : Budget): Long

    // Insert a relation between a user and a budget
    @Insert
    suspend fun insertRelation(userAndBudget: UserBudgetRelation)

    // Insert or update Budget and create a relation to the User if Budget is new
    @Transaction
    suspend fun upsertBudget(budget : Budget, userId: Int) {
        val rowId = upsert(budget)
        if (budget.budgetId == 0) {
            val budgetId = getByRowId(rowId)
            insertRelation(UserBudgetRelation(userId, budgetId))
        }
    }

    // Query to count the number of budgets a user has
    @Query(
        """SELECT COUNT(*) FROM User, Budget, UserBudgetRelation
            WHERE User.userId = :userId
            AND UserBudgetRelation.userId = User.userId
            AND Budget.budgetId = UserBudgetRelation.budgetId"""
    )
    suspend fun userBudgetCount(userId : Int) : Int
}

@Dao
interface DeleteDao {
    // Delete a user by their userId
    @Query("DELETE FROM user WHERE userId = :userId")
    suspend fun deleteUser(userId : Int)

    // Query to get all budget IDs related to a user
    @Query(
        """SELECT Budget.budgetId FROM User, Budget, UserBudgetRelation
            WHERE User.userid = :userId
            AND UserBudgetRelation.userId = User.userId
            AND UserBudgetRelation.budgetId = Budget.budgetId"""
    )
    suspend fun getAllBudgetIdsByUser(userId : Int) : List<Int>

    // Delete budgets by IDs
    @Query("DELETE FROM budget WHERE budgetId IN (:budgetIds)")
    suspend fun deleteBudgets(budgetIds : List<Int>)

    // Delete all users (Clear for testing)
    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()

    // Delete all budgets
    @Query("DELETE FROM budget")
    suspend fun deleteAllBudgets()

    // Transaction to delete user and all their budgets
    @Transaction
    suspend fun delete(userId: Int) {
        deleteBudgets(getAllBudgetIdsByUser(userId))
        deleteUser(userId)
    }
}

@Database(entities = [User::class, Budget::class, UserBudgetRelation::class], version = 3)

@TypeConverters(Converters::class)

abstract class BudgetDatabase : RoomDatabase() {
    // DAOs to access database
    abstract fun userDao() : UserDao
    abstract fun budgetDao() : BudgetDao
    abstract fun deleteDao() : DeleteDao

    companion object {
        @Volatile
        private var INSTANCE: BudgetDatabase? = null
        // Get or create the database instance
        fun getDatabase(context: Context): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    context.getString(R.string.budget_database),
                )
                    .fallbackToDestructiveMigration() // New DB (for development ONLY)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}