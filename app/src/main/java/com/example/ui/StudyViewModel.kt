package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val studyDao = AppDatabase.getDatabase(application).studyDao()
    private val repository = StudyRepository(studyDao)

    // UI Navigation & Detail State
    val currentTab = MutableStateFlow("home") // "home", "courses", "schedule", "focus", "dashboard"
    val selectedCourseId = MutableStateFlow<Long?>(null)
    val activeLessonId = MutableStateFlow<Long?>(null)

    // Interactive States
    val isSearchActive = MutableStateFlow(false)
    val searchQuery = MutableStateFlow("")
    val isCreatingCourse = MutableStateFlow(false)
    val isAddingLesson = MutableStateFlow(false)
    val scheduleRecommendationMessage = MutableStateFlow<String?>(null)

    // Data streams from Repository
    val courses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedLessons: StateFlow<List<Lesson>> = repository.bookmarkedLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<Achievement>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<DailyTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pomodoro interactive timer state
    val pomodoroTimeLeft = MutableStateFlow(25 * 60) // in seconds
    val pomodoroRunning = MutableStateFlow(false)
    val pomodoroMode = MutableStateFlow("Focus") // "Focus", "Break"
    val pomodoroSessionsCompleted = MutableStateFlow(0)
    private var pomodoroJob: Job? = null

    // Date computation
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String
        get() = dateFormatter.format(Date())

    // Active screen stream for lessons
    private val _currentLessons = MutableStateFlow<List<Lesson>>(emptyList())
    val currentLessons: StateFlow<List<Lesson>> = _currentLessons.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultData()
            checkAndSyncDailyStreak()
            updateTaskStreamForDate()
            generateDailyScheduleIfNeeded()
        }

        // Observe selected Course ID to update active lessons view
        viewModelScope.launch {
            selectedCourseId.collect { courseId ->
                if (courseId != null) {
                    repository.getLessonsForCourse(courseId).collect {
                        _currentLessons.value = it
                    }
                } else {
                    _currentLessons.value = emptyList()
                }
            }
        }
    }

    private fun checkAndSyncDailyStreak() {
        viewModelScope.launch {
            val stats = repository.getUserStatsDirect() ?: return@launch
            val today = todayDateString
            val yesterdayCal = Calendar.getInstance()
            yesterdayCal.add(Calendar.DATE, -1)
            val yesterday = dateFormatter.format(yesterdayCal.time)

            if (stats.lastActiveDate == today) {
                // Already synced for today, do nothing
                return@launch
            }

            val newStreak = when (stats.lastActiveDate) {
                yesterday -> stats.dailyStreak + 1
                "" -> 1
                else -> {
                    // Missed days! Implement Smart Timetable Adjustment
                    handleMissedDaysAdjustment(stats.lastActiveDate)
                    1 // Streak resets or starting fresh
                }
            }

            repository.updateUserStats(
                stats.copy(
                    dailyStreak = newStreak,
                    lastActiveDate = today
                )
            )

            // Evaluate streak achievements
            if (newStreak >= 5) {
                unlockAchievement("streak_5")
            }
        }
    }

    private suspend fun handleMissedDaysAdjustment(lastActive: String) {
        // Smart scheduler: carry forward unfinished schedule items from previous dates
        val today = todayDateString
        val unfinishedTasks = repository.allTasks.first().filter {
            it.dateString == lastActive && !it.isCompleted
        }

        if (unfinishedTasks.isNotEmpty()) {
            scheduleRecommendationMessage.value = "Welcome back! 📚 We adjusted your schedule and moved ${unfinishedTasks.size} historical lessons into today's timetable so you don't miss a beat!"
            // Re-schedule them for today
            for (task in unfinishedTasks) {
                repository.insertDailyTask(
                    DailyTask(
                        dateString = today,
                        lessonId = task.lessonId,
                        lessonTitle = task.lessonTitle,
                        courseTitle = task.courseTitle,
                        durationMinutes = task.durationMinutes,
                        isCompleted = false,
                        scheduledTime = task.scheduledTime
                    )
                )
            }
        } else {
            scheduleRecommendationMessage.value = "Great work restarting your study routine today! Let's conquer our goals."
        }
    }

    // Dynamic schedule list for active date
    private val _todayTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val todayTasks: StateFlow<List<DailyTask>> = _todayTasks.asStateFlow()

    fun updateTaskStreamForDate() {
        viewModelScope.launch {
            repository.getTasksForDateFlow(todayDateString).collect {
                _todayTasks.value = it
            }
        }
    }

    fun generateDailyScheduleIfNeeded() {
        viewModelScope.launch {
            val today = todayDateString
            val existingTasks = repository.getTasksForDate(today)
            if (existingTasks.isEmpty()) {
                generateDailySchedule()
            }
        }
    }

    suspend fun generateDailySchedule() {
        val today = todayDateString
        // Retrieve current stats to see preferred study count
        val stats = repository.getUserStatsDirect() ?: return
        val totalAllowedMinutes = stats.preferredStudyHours * 60
        val startTime = stats.preferredStudyTime

        // Get all lessons from all courses
        val allEnrolledCourses = repository.allCourses.first()
        val nextLessonsToStudy = mutableListOf<Triple<Lesson, String, Int>>() // Lesson, CourseTitle, Duration

        for (course in allEnrolledCourses) {
            val courseLessons = repository.getLessonsForCourseList(course.id)
            val incomplete = courseLessons.filter { !it.isCompleted }
            for (lesson in incomplete) {
                nextLessonsToStudy.add(Triple(lesson, course.title, lesson.videoDuration))
            }
        }

        // Generate timetable limit based on user allowed hours
        var currentAllocatedMinutes = 0
        var taskHour = startTime.split(":").firstOrNull()?.toInt() ?: 18
        var taskMinute = startTime.split(":").getOrNull(1)?.toInt() ?: 0

        repository.deleteTasksForDate(today)

        for (triple in nextLessonsToStudy) {
            val lesson = triple.first
            val courseTitle = triple.second
            val duration = triple.third

            if (currentAllocatedMinutes + duration > totalAllowedMinutes) {
                break // Schedule is full
            }

            // Pad times
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", taskHour, taskMinute)
            repository.insertDailyTask(
                DailyTask(
                    dateString = today,
                    lessonId = lesson.id,
                    lessonTitle = lesson.title,
                    courseTitle = courseTitle,
                    durationMinutes = duration,
                    isCompleted = false,
                    scheduledTime = formattedTime
                )
            )

            currentAllocatedMinutes += duration
            // Advance study time
            taskMinute += duration
            if (taskMinute >= 60) {
                taskHour += taskMinute / 60
                taskMinute %= 60
            }
            if (taskHour >= 24) {
                taskHour %= 24
            }
        }
    }

    // Pomodoro Core Control
    fun startPomodoro() {
        if (pomodoroRunning.value) return
        pomodoroRunning.value = true
        pomodoroJob = viewModelScope.launch {
            while (pomodoroTimeLeft.value > 0) {
                delay(1000)
                pomodoroTimeLeft.value -= 1
            }
            onPomodoroFinished()
        }
    }

    fun pausePomodoro() {
        pomodoroRunning.value = false
        pomodoroJob?.cancel()
    }

    fun resetPomodoro() {
        pausePomodoro()
        val stats = userStats.value
        pomodoroTimeLeft.value = (stats?.pomodoroTimeMinutes ?: 25) * 60
        pomodoroMode.value = "Focus"
    }

    private suspend fun onPomodoroFinished() {
        pomodoroRunning.value = false
        val currentMode = pomodoroMode.value
        if (currentMode == "Focus") {
            pomodoroSessionsCompleted.value += 1
            val focusMinutes = userStats.value?.pomodoroTimeMinutes ?: 25
            incrementStudyTime(focusMinutes)
            unlockAchievement("pomo_session")

            // Switch to break
            pomodoroMode.value = "Break"
            pomodoroTimeLeft.value = 5 * 60 // 5 minutes break
        } else {
            // Switch back to focus
            pomodoroMode.value = "Focus"
            val focusMinutes = userStats.value?.pomodoroTimeMinutes ?: 25
            pomodoroTimeLeft.value = focusMinutes * 60
        }
    }

    // Helper functions
    private suspend fun incrementStudyTime(minutes: Int) {
        val stats = repository.getUserStatsDirect() ?: return
        val updatedTime = stats.totalStudyTimeMinutes + minutes
        repository.updateUserStats(stats.copy(totalStudyTimeMinutes = updatedTime))

        if (updatedTime >= 300) { // 5 hours
            unlockAchievement("hours_5")
        }
    }

    fun createCustomCourse(title: String, desc: String, thumbnail: String) {
        viewModelScope.launch {
            val course = Course(title = title, description = desc, thumbnail = thumbnail)
            repository.insertCourse(course)
            unlockAchievement("first_course")
        }
    }

    fun addLessonToCourse(courseId: Long, title: String, desc: String, youtubeUrl: String, duration: Int, chapterName: String = "Chapter 1") {
        viewModelScope.launch {
            val count = _currentLessons.value.size
            val lesson = Lesson(
                courseId = courseId,
                title = title,
                description = desc,
                youtubeUrl = youtubeUrl,
                videoDuration = duration,
                orderIndex = count,
                chapterName = if (chapterName.isBlank()) "Chapter 1" else chapterName.trim()
            )
            repository.insertLesson(lesson)
        }
    }

    fun toggleLessonCompleted(lesson: Lesson) {
        viewModelScope.launch {
            val currentlyCompleted = lesson.isCompleted
            val updated = lesson.copy(isCompleted = !currentlyCompleted)
            repository.updateLesson(updated)

            // Trigger corresponding daily task completion if scheduled for today
            val tasks = _todayTasks.value
            val matchingTask = tasks.find { it.lessonId == lesson.id }
            if (matchingTask != null) {
                repository.updateDailyTask(matchingTask.copy(isCompleted = !currentlyCompleted))
            }

            // Track stats
            if (!currentlyCompleted) {
                incrementStudyTime(lesson.videoDuration)
            } else {
                // Decrement if untoggled
                val stats = repository.getUserStatsDirect() ?: return@launch
                val updatedTime = (stats.totalStudyTimeMinutes - lesson.videoDuration).coerceAtLeast(0)
                repository.updateUserStats(stats.copy(totalStudyTimeMinutes = updatedTime))
            }
        }
    }

    fun updateLessonNotes(lessonId: Long, newNotes: String) {
        viewModelScope.launch {
            val lesson = repository.getLessonById(lessonId) ?: return@launch
            repository.updateLesson(lesson.copy(notes = newNotes))
            unlockAchievement("first_note")
        }
    }

    fun toggleLessonBookmark(lesson: Lesson) {
        viewModelScope.launch {
            repository.updateLesson(lesson.copy(isBookmarked = !lesson.isBookmarked))
        }
    }

    fun addNoteTimestamp(lessonId: Long, timestampString: String, labelString: String) {
        viewModelScope.launch {
            val lesson = repository.getLessonById(lessonId) ?: return@launch
            val existing = lesson.savedTimestamps
            val newItem = "$timestampString|$labelString"
            val updatedString = if (existing.isEmpty()) newItem else "$existing,$newItem"
            repository.updateLesson(lesson.copy(savedTimestamps = updatedString))
            unlockAchievement("first_note") // Triggers node badge
        }
    }

    fun deleteLessonTimestamp(lessonId: Long, index: Int) {
        viewModelScope.launch {
            val lesson = repository.getLessonById(lessonId) ?: return@launch
            val list = lesson.savedTimestamps.split(",").toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
                repository.updateLesson(lesson.copy(savedTimestamps = list.joinToString(",")))
            }
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            if (selectedCourseId.value == course.id) {
                selectedCourseId.value = null
            }
            if (activeLessonId.value != null) {
                val activeLesson = repository.getLessonById(activeLessonId.value!!)
                if (activeLesson?.courseId == course.id) {
                    activeLessonId.value = null
                }
            }
        }
    }

    fun updatePreferredSchedule(hours: Int, time: String) {
        viewModelScope.launch {
            val stats = repository.getUserStatsDirect() ?: return@launch
            repository.updateUserStats(
                stats.copy(
                    preferredStudyHours = hours,
                    preferredStudyTime = time
                )
            )
            // Re-generate schedule dynamically
            generateDailySchedule()
        }
    }

    fun unlockAchievement(id: String) {
        viewModelScope.launch {
            val currentBadge = repository.allAchievements.first().find { it.id == id }
            if (currentBadge != null && !currentBadge.isUnlocked) {
                repository.updateAchievement(
                    currentBadge.copy(
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun togglePomodoroDuration() {
        val stats = userStats.value ?: return
        val currentDuration = stats.pomodoroTimeMinutes
        val nextDuration = if (currentDuration == 25) 50 else if (currentDuration == 50) 15 else 25
        viewModelScope.launch {
            repository.updateUserStats(stats.copy(pomodoroTimeMinutes = nextDuration))
            if (!pomodoroRunning.value) {
                pomodoroTimeLeft.value = nextDuration * 60
            }
        }
    }

    suspend fun getLessonById(id: Long): Lesson? = repository.getLessonById(id)

    fun toggleLessonCompletedById(lessonId: Long) {
        viewModelScope.launch {
            repository.getLessonById(lessonId)?.let {
                toggleLessonCompleted(it)
            }
        }
    }

    fun launchLessonPlayer(lessonId: Long) {
        viewModelScope.launch {
            repository.getLessonById(lessonId)?.let {
                selectedCourseId.value = it.courseId
                activeLessonId.value = it.id
            }
        }
    }

    fun runScheduleGeneration() {
        viewModelScope.launch {
            generateDailySchedule()
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val stats = repository.getUserStatsDirect() ?: return@launch
            repository.updateUserStats(stats.copy(reminderEnabled = enabled))
        }
    }
}
