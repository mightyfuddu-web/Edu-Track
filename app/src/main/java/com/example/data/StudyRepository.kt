package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

class StudyRepository(private val studyDao: StudyDao) {

    val allCourses: Flow<List<Course>> = studyDao.getAllCourses()
    val bookmarkedLessons: Flow<List<Lesson>> = studyDao.getBookmarkedLessons()
    val userStats: Flow<UserStats?> = studyDao.getUserStatsFlow()
    val allAchievements: Flow<List<Achievement>> = studyDao.getAllAchievements()
    val allTasks: Flow<List<DailyTask>> = studyDao.getAllTasksFlow()

    // Courses & Lessons Actions
    suspend fun getCourseById(id: Long): Course? = studyDao.getCourseById(id)
    suspend fun getLessonsForCourseList(courseId: Long): List<Lesson> = studyDao.getLessonsForCourseList(courseId)
    fun getLessonsForCourse(courseId: Long): Flow<List<Lesson>> = studyDao.getLessonsForCourse(courseId)
    suspend fun getLessonById(id: Long): Lesson? = studyDao.getLessonById(id)

    suspend fun insertCourse(course: Course): Long = studyDao.insertCourse(course)
    suspend fun updateCourse(course: Course) = studyDao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = studyDao.deleteCourse(course)

    suspend fun insertLesson(lesson: Lesson): Long = studyDao.insertLesson(lesson)
    suspend fun updateLesson(lesson: Lesson) = studyDao.updateLesson(lesson)
    suspend fun deleteLesson(lesson: Lesson) = studyDao.deleteLesson(lesson)

    // User Stats Actions
    suspend fun getUserStatsDirect(): UserStats? = studyDao.getUserStats()
    suspend fun insertUserStats(stats: UserStats) = studyDao.insertUserStats(stats)
    suspend fun updateUserStats(stats: UserStats) = studyDao.updateUserStats(stats)

    // Daily Tasks Actions
    fun getTasksForDateFlow(dateString: String): Flow<List<DailyTask>> = studyDao.getTasksForDateFlow(dateString)
    suspend fun getTasksForDate(dateString: String): List<DailyTask> = studyDao.getTasksForDate(dateString)
    suspend fun insertDailyTask(task: DailyTask): Long = studyDao.insertDailyTask(task)
    suspend fun updateDailyTask(task: DailyTask) = studyDao.updateDailyTask(task)
    suspend fun deleteTasksForDate(dateString: String) = studyDao.deleteTasksForDate(dateString)

    // Achievements Actions
    suspend fun updateAchievement(achievement: Achievement) = studyDao.updateAchievement(achievement)
    suspend fun insertAchievements(achievements: List<Achievement>) = studyDao.insertAchievements(achievements)

    // Setup Default Data if database is blank
    suspend fun initializeDefaultData() {
        // Initialize UserStats with a dummy row if not exists
        val currentStats = studyDao.getUserStats()
        if (currentStats == null) {
            studyDao.insertUserStats(
                UserStats(
                    id = 1,
                    dailyStreak = 3, // Start with a friendly 3-day motivating streak!
                    lastActiveDate = "2026-05-26",
                    totalStudyTimeMinutes = 48,
                    preferredStudyHours = 2,
                    preferredStudyTime = "18:00",
                    reminderEnabled = true,
                    pomodoroTimeMinutes = 25
                )
            )
        }

        // Initialize Achievements
        val achievementsList = studyDao.getAllAchievements().first()
        if (achievementsList.isEmpty()) {
            val defaultAchievements = listOf(
                Achievement("first_course", "Knowledge Explorer", "Create or import your first custom course.", "school", true, System.currentTimeMillis()),
                Achievement("first_note", "Active Writer", "Add your first study note to a lesson video.", "edit_note"),
                Achievement("streak_5", "Unstoppable Scholar", "Sustain a 5-day daily study streak.", "local_fire_department"),
                Achievement("hours_5", "Focus Master", "Complete over 5 hours of total study time.", "timer"),
                Achievement("pomo_session", "Pomodoro Pioneer", "Complete a full 25-minute interactive focus timer cycle.", "hourglass_empty")
            )
            studyDao.insertAchievements(defaultAchievements)
        }

        // Clean up pre-seeded default courses if they exist in the DB (keep courses only that the user made)
        val existingCourses = studyDao.getAllCourses().first()
        for (course in existingCourses) {
            val titleLower = course.title.lowercase().trim()
            if (titleLower.contains("science of learning") || titleLower.contains("visual design & aesthetic")) {
                studyDao.deleteCourse(course)
                // Delete its associated lessons too
                val courseLessons = studyDao.getLessonsForCourseList(course.id)
                for (lesson in courseLessons) {
                    studyDao.deleteLesson(lesson)
                }
            }
        }
    }
}
