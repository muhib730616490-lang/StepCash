package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.WalkEarnDatabase
import com.example.data.repository.WalkEarnRepository
import com.example.data.model.*
import com.example.sensor.StepTracker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WalkEarnViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WalkEarnDatabase.getDatabase(application)
    private val repository = WalkEarnRepository(database)
    private val stepTracker = StepTracker(application)

    // Language State
    private val _currentLanguage = MutableStateFlow("ar") // Default to Arabic (RTL) as requested
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    // Observable states from Repository
    val activeUser: StateFlow<User?> = repository.loggedInUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stepRecords: StateFlow<List<StepRecord>> = repository.allStepRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionRecord>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskRecord>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shopItems: StateFlow<List<ShopItem>> = repository.allShopItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Anti-Cheat Alert State
    private val _antiCheatAlert = MutableStateFlow<String?>(null)
    val antiCheatAlert: StateFlow<String?> = _antiCheatAlert.asStateFlow()

    // Screen State
    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    init {
        // Start physical step tracking
        stepTracker.startTracking()

        // Collect steps from physical hardware sensors
        viewModelScope.launch {
            stepTracker.stepsFlow.collect { increment ->
                val isVip = isVipActive()
                repository.updateSteps(increment, isVip)
                checkDailyGoalsProgress()
            }
        }

        // Collect anti-cheat signals from sensor hardware
        viewModelScope.launch {
            stepTracker.antiCheatTriggered.collect { code ->
                _antiCheatAlert.value = code
            }
        }

        // Restore language selection if any
        val sharedPrefs = application.getSharedPreferences("walk_earn_prefs", Application.MODE_PRIVATE)
        _currentLanguage.value = sharedPrefs.getString("app_lang", "ar") ?: "ar"

        // Check if there is already a logged-in user
        viewModelScope.launch {
            val user = repository.getLoggedInUserDirect()
            if (user != null) {
                _currentScreen.value = "dashboard"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepTracker.stopTracking()
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        val sharedPrefs = getApplication<Application>().getSharedPreferences("walk_earn_prefs", Application.MODE_PRIVATE)
        sharedPrefs.edit().putString("app_lang", lang).apply()
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    private fun isVipActive(): Boolean {
        val vipItem = shopItems.value.find { it.isVip }
        return vipItem?.purchased == true
    }

    // Google Login Logic
    fun handleGoogleLogin(email: String, name: String, photoUrl: String) {
        viewModelScope.launch {
            val user = repository.registerOrLoginGoogleUser(
                id = "google_" + email.hashCode().toString(),
                name = name,
                email = email,
                photoUrl = photoUrl
            )
            // If they are admin, give admin privileges
            if (email.contains("admin", ignoreCase = true) || email == "admin@walkearn.com") {
                repository.saveUser(user.copy(isAdmin = true))
            }
            _currentScreen.value = "dashboard"
        }
    }

    // Manual Steps Simulation (Useful for Emulator/Physical testing)
    fun simulateSteps(steps: Int, speed: Float) {
        viewModelScope.launch {
            stepTracker.simulateWalkSteps(steps, speed)
        }
    }

    fun dismissAntiCheatAlert() {
        _antiCheatAlert.value = null
    }

    // Daily Goals verification
    private suspend fun checkDailyGoalsProgress() {
        val user = activeUser.value ?: return
        val currentTasks = tasks.value
        currentTasks.forEach { task ->
            if (!task.isCompleted && task.type == "DAILY" && user.steps >= task.targetSteps) {
                // Complete task and reward points
                val updatedTask = task.copy(isCompleted = true, currentProgress = task.targetSteps)
                repository.insertTask(updatedTask)
                
                // Reward user
                val updatedUser = user.copy(
                    points = user.points + task.points,
                    balance = user.balance + (task.points * 0.01)
                )
                repository.saveUser(updatedUser)
            }
        }
    }

    // Transaction requests
    fun requestWithdrawal(amountUsd: Double, destination: String, method: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val points = (amountUsd * 100).toInt() // $1.00 = 100 points
            val success = repository.processTransaction("WITHDRAWAL", amountUsd, points, destination, method)
            if (success) {
                onSuccess()
            } else {
                onError("INSUFFICIENT_FUNDS")
            }
        }
    }

    fun requestDeposit(amountUsd: Double, method: String) {
        viewModelScope.launch {
            val points = (amountUsd * 100).toInt()
            repository.processTransaction("DEPOSIT", amountUsd, points, "Walk Earn Balance", method)
        }
    }

    // Shop purchase
    fun buyUpgrade(item: ShopItem, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val success = repository.buyShopItem(item)
            if (success) {
                onSuccess()
            } else {
                onError("INSUFFICIENT_POINTS")
            }
        }
    }

    // Admin Operations
    fun adminUpdateUserSteps(userId: String, steps: Int) {
        viewModelScope.launch {
            val user = allUsers.value.find { it.id == userId } ?: return@launch
            val updatedDistance = steps * 0.00075f
            val updatedUser = user.copy(steps = steps, distance = updatedDistance)
            repository.adminModifyUser(updatedUser)
        }
    }

    fun adminAddPoints(userId: String, points: Int) {
        viewModelScope.launch {
            val user = allUsers.value.find { it.id == userId } ?: return@launch
            val updatedUser = user.copy(
                points = user.points + points,
                balance = user.balance + (points * 0.01)
            )
            repository.adminModifyUser(updatedUser)
        }
    }

    fun adminDeleteUser(userId: String) {
        viewModelScope.launch {
            repository.adminDeleteUser(userId)
        }
    }

    fun adminAddNewTask(titleEn: String, titleAr: String, points: Int, target: Int, type: String) {
        viewModelScope.launch {
            val task = TaskRecord(
                titleEn = titleEn,
                titleAr = titleAr,
                points = points,
                type = type,
                targetSteps = target
            )
            repository.adminAddTask(task)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentScreen.value = "login"
        }
    }

    // Localized labels helper
    fun getLabel(key: String): String {
        val isAr = _currentLanguage.value == "ar"
        return when (key) {
            "app_title" -> if (isAr) "امشِ واكسب" else "Walk Earn"
            "login_title" -> if (isAr) "ابدأ المشي واكسب الأرباح" else "Start Walking & Earning"
            "login_desc" -> if (isAr) "قم بتسجيل الدخول بأمان باستخدام حساب Google وابدأ رحلتك الصحية والمالية." else "Log in securely using Google Account to start your healthy and financial journey."
            "google_sign_in" -> if (isAr) "تسجيل الدخول باستخدام Google" else "Sign In with Google"
            "select_account" -> if (isAr) "اختر حساب Google" else "Choose a Google Account"
            "dashboard" -> if (isAr) "الرئيسية" else "Dashboard"
            "wallet" -> if (isAr) "المحفظة" else "Wallet"
            "tasks" -> if (isAr) "المهام اليومية" else "Daily Tasks"
            "shop" -> if (isAr) "المتجر" else "Shop"
            "settings" -> if (isAr) "الإعدادات" else "Settings"
            "admin_panel" -> if (isAr) "لوحة التحكم" else "Admin Panel"
            "steps" -> if (isAr) "الخطوات" else "Steps"
            "distance" -> if (isAr) "المسافة" else "Distance"
            "calories" -> if (isAr) "السعرات" else "Calories"
            "points" -> if (isAr) "النقاط" else "Points"
            "balance" -> if (isAr) "الرصيد" else "Balance"
            "level" -> if (isAr) "المستوى" else "Level"
            "streak" -> if (isAr) "السلسلة" else "Streak"
            "withdraw" -> if (isAr) "سحب الأرباح" else "Withdraw"
            "deposit" -> if (isAr) "إيداع" else "Deposit"
            "withdraw_address" -> if (isAr) "عنوان المحفظة أو الحساب" else "Wallet Address or Account"
            "payment_method" -> if (isAr) "طريقة الدفع" else "Payment Method"
            "withdraw_success" -> if (isAr) "تم تقديم طلب السحب بنجاح وهي قيد المراجعة." else "Withdrawal request submitted successfully and is under review."
            "withdraw_error" -> if (isAr) "الرصيد غير كافٍ لإتمام هذه العملية." else "Insufficient balance to complete this operation."
            "days" -> if (isAr) "أيام" else "Days"
            "buy" -> if (isAr) "شراء" else "Purchase"
            "purchased" -> if (isAr) "تم الشراء" else "Purchased"
            "vip_badge" -> if (isAr) "شعار VIP نشط" else "VIP Badge Active"
            "language_select" -> if (isAr) "تغيير اللغة" else "Change Language"
            "logout" -> if (isAr) "تسجيل الخروج" else "Log Out"
            "admin_code" -> if (isAr) "كود دخول الإدارة" else "Admin Access Code"
            "admin_login_btn" -> if (isAr) "دخول الإدارة" else "Admin Access"
            "admin_incorrect" -> if (isAr) "كود الدخول غير صحيح!" else "Incorrect Admin Code!"
            "users_count" -> if (isAr) "عدد الأعضاء" else "Total Members"
            "add_task_btn" -> if (isAr) "إضافة مهمة جديدة" else "Add New Task"
            "anti_cheat_title" -> if (isAr) "تحذير نظام الحماية من الغش!" else "Anti-Cheat Protection Warning!"
            "anti_cheat_speed" -> if (isAr) "تم رصد سرعة خطوات غير طبيعية ومحاولة تلاعب ببيانات الحساسات! تم تجميد كسب النقاط مؤقتاً لحماية النظام." else "Abnormal stepping speed and sensor tampering attempt detected! Point generation is temporarily frozen to protect the network."
            "anti_cheat_simulator" -> if (isAr) "تجاوزت السرعة المحاكاة الحد المسموح به للبشر (15 كم/س)! يرجى تقليل السرعة لتجنب الحظر." else "Simulated speed exceeded human limits (15 km/h)! Please lower the speed to avoid a permanent ban."
            "dismiss" -> if (isAr) "موافق" else "Dismiss"
            "active_tasks" -> if (isAr) "المهام النشطة" else "Active Challenges"
            "invite_friend" -> if (isAr) "دعوة صديق جديد" else "Invite a Friend"
            "invite_code" -> if (isAr) "كود الدعوة الخاص بك" else "Your Referral Code"
            "share_with_friends" -> if (isAr) "شارك مع أصدقائك للحصول على 150 نقطة فوراً!" else "Share with friends to get 150 points instantly!"
            "treadmill_mode" -> if (isAr) "محاكي المشي الداخلي (جهاز الجري)" else "Indoor Walk Simulator (Treadmill)"
            "speed_kmh" -> if (isAr) "السرعة (كم/ساعة)" else "Speed (km/h)"
            "simulate_walk_btn" -> if (isAr) "بدء خطوات المحاكاة" else "Simulate Step Workout"
            "binance_ready" -> if (isAr) "جاهز للربط مع Binance API" else "Ready to sync with Binance API"
            "crypto_ready" -> if (isAr) "البنية جاهزة للربط مع المحافظ الرقمية وسلسلة الكتل" else "Structure prepared for on-chain/crypto sync"
            "db_ready" -> if (isAr) "قاعدة البيانات والأنظمة جاهزة للمزامنة السحابية" else "Databases & services ready for cloud sync"
            "transaction_history" -> if (isAr) "سجل العمليات والتحويلات" else "Transaction & Transfer History"
            "amount_usd" -> if (isAr) "القيمة (بالدولار USD)" else "Amount (USD)"
            "deposit_success" -> if (isAr) "تمت عملية الإيداع بنجاح في محفظتك." else "Deposit processed successfully to your wallet."
            else -> key
        }
    }
}
