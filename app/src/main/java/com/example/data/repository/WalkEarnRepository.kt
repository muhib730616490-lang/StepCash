package com.example.data.repository

import com.example.data.database.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class WalkEarnRepository(private val db: WalkEarnDatabase) {
    private val userDao = db.userDao()
    private val stepDao = db.stepDao()
    private val transactionDao = db.transactionDao()
    private val taskDao = db.taskDao()
    private val shopDao = db.shopDao()

    val loggedInUser: Flow<User?> = userDao.getLoggedInUserFlow()
    val allUsers: Flow<List<User>> = userDao.getAllUsersFlow()
    val allStepRecords: Flow<List<StepRecord>> = stepDao.getAllStepRecords()
    val allTransactions: Flow<List<TransactionRecord>> = transactionDao.getAllTransactions()
    val allTasks: Flow<List<TaskRecord>> = taskDao.getAllTasksFlow()
    val allShopItems: Flow<List<ShopItem>> = shopDao.getAllShopItemsFlow()

    // Conversions
    // 1000 Steps = 1 Point
    // 10 Points = $0.10 USD (or 100 Points = $1.00 USD)
    private val stepsPerPoint = 1000
    private val pointValueUsd = 0.01 // Each point is worth $0.01

    suspend fun getLoggedInUserDirect(): User? = userDao.getLoggedInUser()

    suspend fun registerOrLoginGoogleUser(id: String, name: String, email: String, photoUrl: String): User {
        userDao.logoutAll() // Logout existing sessions
        var existing = userDao.getUserById(id)
        if (existing == null) {
            existing = User(
                id = id,
                name = name,
                email = email,
                photoUrl = photoUrl,
                steps = 0,
                distance = 0f,
                points = 100, // starting gift points
                balance = 1.0, // $1.00 starting gift
                level = 1,
                streak = 1,
                isAdmin = email.contains("admin", ignoreCase = true) || email == "admin@walkearn.com",
                isLoggedIn = true
            )
            userDao.insertUser(existing)
            
            // Generate standard initial tasks and shop items if empty
            prepopulateInitialData()
        } else {
            val updated = existing.copy(isLoggedIn = true)
            userDao.insertUser(updated)
        }
        return userDao.getUserById(id)!!
    }

    suspend fun prepopulateInitialData() {
        // Prep tasks
        val tasks = listOf(
            TaskRecord(titleEn = "Daily 5,000 steps challenge", titleAr = "تحدي الـ 5,000 خطوة اليومي", points = 50, type = "DAILY", targetSteps = 5000),
            TaskRecord(titleEn = "A Marathoner - Walk 10,000 steps", titleAr = "العداء المحترف - امشِ 10,000 خطوة", points = 120, type = "CHALLENGE", targetSteps = 10000),
            TaskRecord(titleEn = "Invite 3 new friends", titleAr = "دعوة 3 أصدقاء جدد", points = 150, type = "REFERRAL", targetSteps = 3),
            TaskRecord(titleEn = "Complete 3 days streak of walking", titleAr = "أكمل سلسلة مشي لمدة 3 أيام", points = 100, type = "CHALLENGE", targetSteps = 3)
        )
        tasks.forEach { taskDao.insertTask(it) }

        // Prep shop
        val shopItems = listOf(
            ShopItem(titleEn = "Super Golden Shoes (2x Points multiplier)", titleAr = "الحذاء الذهبي الخارق (مضاعف نقاط 2x)", descriptionEn = "Walk and earn double the points on all steps forever.", descriptionAr = "امشِ واكسب ضعف النقاط على جميع خطواتك للأبد.", pricePoints = 800, isVip = false, iconName = "flash_on"),
            ShopItem(titleEn = "WalkEarn VIP Membership Badge", titleAr = "عضوية WalkEarn الراقية VIP", descriptionEn = "Unlocks exclusive premium giveaways, faster withdraw processing and support.", descriptionAr = "تفتح ميزات حصرية وهدايا، وتزيد سرعة معالجة السحوبات والدعم.", pricePoints = 2000, isVip = true, iconName = "star"),
            ShopItem(titleEn = "Energy Booster Level up", titleAr = "ترقية معزز الطاقة للمستوى الأعلى", descriptionEn = "Increases daily walking points limit from 200 to 500 points.", descriptionAr = "يزيد الحد الأقصى للنقاط اليومية من 200 إلى 500 نقطة.", pricePoints = 500, isVip = false, iconName = "speed")
        )
        shopItems.forEach { shopDao.insertShopItem(it) }
    }

    suspend fun updateSteps(stepsIncrement: Int, isVipActive: Boolean) {
        val user = userDao.getLoggedInUser() ?: return
        
        val newSteps = user.steps + stepsIncrement
        val calculatedDistance = newSteps * 0.00075f // approx 0.75m per step in km
        
        // Calculate new points to earn
        val stepDiffPoints = stepsIncrement / stepsPerPoint
        var pointsEarned = if (stepDiffPoints > 0) stepDiffPoints else 0
        
        // Adjust points for multipliers/VIP if applicable
        if (isVipActive) {
            pointsEarned *= 2
        }

        val finalPoints = user.points + pointsEarned
        val finalBalance = user.balance + (pointsEarned * pointValueUsd)
        
        // Calculate dynamic level (every 10,000 cumulative steps increases level)
        val finalLevel = (newSteps / 10000) + 1

        val updatedUser = user.copy(
            steps = newSteps,
            distance = calculatedDistance,
            points = finalPoints,
            balance = finalBalance,
            level = if (finalLevel > user.level) finalLevel else user.level
        )
        userDao.updateUser(updatedUser)

        // Save to daily history
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val existingRecord = stepDao.getStepRecordByDate(currentDate)
        if (existingRecord != null) {
            stepDao.insertStepRecord(
                existingRecord.copy(
                    steps = existingRecord.steps + stepsIncrement,
                    distance = existingRecord.distance + (stepsIncrement * 0.00075f),
                    pointsEarned = existingRecord.pointsEarned + pointsEarned,
                    calories = existingRecord.calories + (stepsIncrement * 0.04f) // ~0.04 kcal per step
                )
            )
        } else {
            stepDao.insertStepRecord(
                StepRecord(
                    date = currentDate,
                    steps = stepsIncrement,
                    distance = stepsIncrement * 0.00075f,
                    pointsEarned = pointsEarned,
                    calories = stepsIncrement * 0.04f
                )
            )
        }

        // Update Daily/Challenge progress
        updateTasksProgress(newSteps)
    }

    private suspend fun updateTasksProgress(cumulativeSteps: Int) {
        // Query current tasks from DB and check progress
        val dbTasks = db.taskDao().getAllTasksFlow()
        // Simple iteration inside ViewModel/Repo to adjust progress
    }

    suspend fun saveUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun logout() {
        userDao.logoutAll()
    }

    suspend fun processTransaction(type: String, amountUsd: Double, points: Int, destAddress: String, method: String): Boolean {
        val user = userDao.getLoggedInUser() ?: return false
        
        if (type == "WITHDRAWAL") {
            if (user.points < points || user.balance < amountUsd) {
                return false
            }
            // Subtract points and balance
            val updatedUser = user.copy(
                points = user.points - points,
                balance = user.balance - amountUsd
            )
            userDao.updateUser(updatedUser)
            
            // Log transaction
            transactionDao.insertTransaction(
                TransactionRecord(
                    userId = user.id,
                    type = "WITHDRAWAL",
                    amount = amountUsd,
                    points = points,
                    status = "PENDING",
                    destinationAddress = destAddress,
                    paymentMethod = method
                )
            )
            return true
        } else if (type == "DEPOSIT") {
            // Add points and balance
            val updatedUser = user.copy(
                points = user.points + points,
                balance = user.balance + amountUsd
            )
            userDao.updateUser(updatedUser)

            // Log transaction
            transactionDao.insertTransaction(
                TransactionRecord(
                    userId = user.id,
                    type = "DEPOSIT",
                    amount = amountUsd,
                    points = points,
                    status = "COMPLETED",
                    destinationAddress = destAddress,
                    paymentMethod = method
                )
            )
            return true
        }
        return false
    }

    // Admin commands
    suspend fun adminModifyUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun adminDeleteUser(id: String) {
        userDao.deleteUser(id)
    }

    suspend fun adminAddTask(task: TaskRecord) {
        taskDao.insertTask(task)
    }

    suspend fun adminDeleteTask(id: Long) {
        taskDao.deleteTask(id)
    }

    suspend fun adminUpdateTransaction(transaction: TransactionRecord) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun insertTask(task: TaskRecord) {
        taskDao.insertTask(task)
    }

    suspend fun insertShopItem(item: ShopItem) {
        shopDao.insertShopItem(item)
    }

    suspend fun buyShopItem(item: ShopItem): Boolean {
        val user = userDao.getLoggedInUser() ?: return false
        if (user.points >= item.pricePoints) {
            val updatedUser = user.copy(
                points = user.points - item.pricePoints
            )
            userDao.updateUser(updatedUser)
            shopDao.updateShopItem(item.copy(purchased = true))
            
            // Record a transaction
            transactionDao.insertTransaction(
                TransactionRecord(
                    userId = user.id,
                    type = "SHOP_PURCHASE",
                    amount = item.pricePoints * pointValueUsd,
                    points = item.pricePoints,
                    status = "COMPLETED",
                    destinationAddress = if (item.isVip) "VIP Badge" else item.titleEn,
                    paymentMethod = "Points Balance"
                )
            )
            return true
        }
        return false
    }
}
