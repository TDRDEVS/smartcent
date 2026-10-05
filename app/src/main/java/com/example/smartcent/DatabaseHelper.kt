package com.example.smartcent

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.time.LocalDate

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "budget_tracker.db"
        private const val DATABASE_VERSION = 8
        private const val TABLE_USERS = "users"
        private const val TABLE_CATEGORIES = "categories"
        private const val TABLE_EXPENSES = "expenses"
        private const val TABLE_INCOME = "income"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_CATEGORY_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users table
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT
            )
        """.trimIndent()
        )

        // Categories table
        db.execSQL(
            """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT,
                user_id INTEGER,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
            )
        """.trimIndent()
        )

        // Expenses table
        db.execSQL(
            """
            CREATE TABLE $TABLE_EXPENSES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                start_time TEXT,
                end_time TEXT,
                description TEXT,
                amount REAL,
                category TEXT,
                photo_uri TEXT,
                user_id INTEGER,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
            )
        """.trimIndent()
        )
        // Income table
        db.execSQL(
            """
    CREATE TABLE $TABLE_INCOME (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        amount REAL,
        date TEXT,
        FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
    )
    """.trimIndent()
        )
        // Budgets table
        db.execSQL(
            """
        CREATE TABLE budgets (
            user_id INTEGER PRIMARY KEY,
            goal REAL,
            max_goal REAL,
            FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
        )
    """.trimIndent()
        )
        // Achievements table (for gamification)
        db.execSQL(
            """
        CREATE TABLE achievements (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            feature TEXT,        -- e.g. "expenses", "categories", "income"
            milestone TEXT,      -- e.g. "First Expense", "10 Expenses"
            unlocked INTEGER DEFAULT 0,
            date_unlocked TEXT,
            FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
        )
        """.trimIndent()
        )
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS budgets")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INCOME")
        db.execSQL("DROP TABLE IF EXISTS achievements")
        onCreate(db)
    }
    // USER FUNCTIONS
    fun insertUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values) != -1L
    }
    fun checkUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME=? AND $COLUMN_PASSWORD=?",
            arrayOf(username, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
    fun getUserId(username: String): Int? {
        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT id FROM $TABLE_USERS WHERE $COLUMN_USERNAME=?", arrayOf(username))
        val userId = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        return userId
    }
    //  CATEGORY FUNCTIONS
    fun insertCategory(name: String, userId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, name)
            put("user_id", userId)
        }
        return db.insert(TABLE_CATEGORIES, null, values) != -1L
    }
    fun getCategoriesForUser(userId: Int): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_CATEGORY_NAME FROM $TABLE_CATEGORIES WHERE user_id=?",
            arrayOf(userId.toString())
        )
        val categories = mutableListOf<String>()
        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0))
        }
        cursor.close()
        return categories
    }
    //  EXPENSE FUNCTIONS
    fun insertExpense(
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        amount: Double,
        category: String,
        photoUri: String?,
        userId: Int
    ): Boolean {
        val db = writableDatabase

        // Force date into YYYY-MM-DD
        val parts = date.split("/")
        val normalizedDate = if (parts.size == 3) {
            String.format("%04d-%02d-%02d", parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
        } else {
            date // already in correct format
        }
        val values = ContentValues().apply {
            put("date", normalizedDate)
            put("start_time", startTime)
            put("end_time", endTime)
            put("description", description)
            put("amount", amount)
            put("category", category)
            put("photo_uri", photoUri)
            put("user_id", userId)
        }
        return db.insert(TABLE_EXPENSES, null, values) != -1L
    }
    data class Expense(
        val date: String,
        val startTime: String,
        val endTime: String,
        val description: String,
        val amount: Double,
        val category: String,
        val photoUri: String?
    )
    //each user will have their own unique data hence this function
    fun getExpensesForUser(userId: Int): List<Expense> {
        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT * FROM $TABLE_EXPENSES WHERE user_id=?", arrayOf(userId.toString()))
        val expenses = mutableListOf<Expense>()
        while (cursor.moveToNext()) {
            expenses.add(
                Expense(
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time")),
                    endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    photoUri = cursor.getString(cursor.getColumnIndexOrThrow("photo_uri"))
                )
            )
        }
        cursor.close()
        return expenses
    }
    fun getExpensesByDateRange(userId: Int, startDate: String, endDate: String): List<Expense> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_EXPENSES WHERE user_id=? AND DATE(date) BETWEEN DATE(?) AND DATE(?) ORDER BY DATE(date) ASC",
            arrayOf(userId.toString(), startDate, endDate)
        )
        val expenses = mutableListOf<Expense>()
        while (cursor.moveToNext()) {
            expenses.add(
                Expense(
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time")),
                    endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    photoUri = cursor.getString(cursor.getColumnIndexOrThrow("photo_uri"))
                )
            )
        }
        cursor.close()
        return expenses
    }
    //For the tracking board on the main dashboard

    fun getTotalExpenses(userId: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(amount) FROM expenses WHERE user_id = ?",
            arrayOf(userId.toString())
        )
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }

    data class Budget(val gaol: Double, val maxGoal: Double)

    fun getBudgetGoal(userId: Int): Double {
        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT goal FROM budgets WHERE user_id = ?", arrayOf(userId.toString()))
        var goal = 0.0
        if (cursor.moveToFirst()) {
            goal = cursor.getDouble(0)
        }
        cursor.close()
        return goal
    }
    fun getBudgetMax(userId: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT max_goal FROM budgets WHERE user_id = ?",
            arrayOf(userId.toString())
        )
        var maxGoal = 0.0
        if (cursor.moveToFirst()) {
            maxGoal = cursor.getDouble(0)
        }
        cursor.close()
        return maxGoal
    }
    fun saveBudgetGoal(userId: Int, min: Double, max: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("goal", min)   // store min budget
            put("max_goal", max)
        }
        val result =
            db.insertWithOnConflict("budgets", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return result != -1L
    }
    fun addIncome(userId: Int, amount: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("amount", amount)
            put("date", System.currentTimeMillis().toString())
        }
        val result = db.insert(TABLE_INCOME, null, values)
        return result != -1L
    }

    fun addExpense(userId: Int, amount: Double, category: String) {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put("user_id", userId)
                put("amount", amount)
                put("category", category)
            }
            db.insert("expenses", null, values)
        } finally {
            db.close()
        }
    }
    // --- Get total income for a user ---
    fun getTotalIncome(userId: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(amount) FROM $TABLE_INCOME WHERE user_id=?",
            arrayOf(userId.toString())
        )
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getDouble(0) else 0.0
        cursor.close()
        return total
    }

    // Data class for chart entries
    data class CategorySpending(
        val category: String,
        val total: Double
    )

    // Get spending totals per category for a user in a date range
    fun getSpendingByCategory(userId: Int, startDate: String, endDate: String): List<CategorySpending> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT category, SUM(amount) as total
        FROM $TABLE_EXPENSES
        WHERE user_id = ? AND date BETWEEN ? AND ?
        GROUP BY category
        """.trimIndent(),
            arrayOf(userId.toString(), startDate, endDate)
        )

        val results = mutableListOf<CategorySpending>()
        while (cursor.moveToNext()) {
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
            results.add(CategorySpending(category, total))
        }
        cursor.close()
        return results
    }

    fun normalizeOldDates() {
        val db = writableDatabase
        db.execSQL(
            """
        UPDATE $TABLE_EXPENSES
        SET date = substr(date, 7, 4) || '-' || substr(date, 4, 2) || '-' || substr(date, 1, 2)
        WHERE instr(date, '/') > 0
        """
        )
    }

    fun unlockAchievement(userId: Int, feature: String, milestone: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("feature", feature)
            put("milestone", milestone)
            put("unlocked", 1)
            put("date_unlocked", System.currentTimeMillis().toString())
        }
        db.insert("achievements", null, values)
    }

    fun getExpenseCount(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_EXPENSES WHERE user_id=?", arrayOf(userId.toString()))
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    fun getAchievements(userId: Int): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT feature, milestone FROM achievements WHERE user_id=? AND unlocked=1",
            arrayOf(userId.toString())
        )
        val achievements = mutableListOf<Pair<String, String>>()
        while (cursor.moveToNext()) {
            achievements.add(Pair(cursor.getString(0), cursor.getString(1)))
        }
        cursor.close()
        return achievements
    }

    fun getCategoryCount(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES WHERE user_id=?", arrayOf(userId.toString()))
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    fun getIncomeCount(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_INCOME WHERE user_id=?", arrayOf(userId.toString()))
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    fun getGoalCount(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM budgets WHERE user_id=?", arrayOf(userId.toString()))
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    fun getGoalProgress(userId: Int): Double {
        val income = getTotalIncome(userId)
        val expenses = getTotalExpenses(userId)
        val goal = getBudgetGoal(userId) // your min goal
        val maxGoal = getBudgetMax(userId)

        val savings = income - expenses
        return if (maxGoal > 0) (savings / maxGoal) * 100 else 0.0
    }
}
