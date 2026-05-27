package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val thumbnail: String, // "code", "science", "math", "design", "general", or custom URI
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val title: String,
    val description: String,
    val youtubeUrl: String,
    val videoDuration: Int = 15, // in minutes
    val isCompleted: Boolean = false,
    val notes: String = "",
    val isBookmarked: Boolean = false,
    val savedTimestamps: String = "", // Comma/newline separated like "00:45|Introduction,03:15|Key Rule"
    val orderIndex: Int = 0,
    val chapterName: String = "Chapter 1"
) : Serializable

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val dailyStreak: Int = 0,
    val lastActiveDate: String = "", // "YYYY-MM-DD"
    val totalStudyTimeMinutes: Int = 0,
    val preferredStudyHours: Int = 2,
    val preferredStudyTime: String = "18:00", // "HH:MM"
    val reminderEnabled: Boolean = true,
    val pomodoroTimeMinutes: Int = 25,
    val notesOfflineSaved: Int = 0
) : Serializable

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val lessonId: Long,
    val lessonTitle: String,
    val courseTitle: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val scheduledTime: String = "" // "18:00"
) : Serializable

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String, // Unique key e.g. "first_course", "stretch_7", etc.
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0
) : Serializable
