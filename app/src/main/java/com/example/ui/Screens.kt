package com.example.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduTrackApp(viewModel: StudyViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedCourseId by viewModel.selectedCourseId.collectAsState()
    val activeLessonId by viewModel.activeLessonId.collectAsState()

    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isCreatingCourse by viewModel.isCreatingCourse.collectAsState()
    val isAddingLesson by viewModel.isAddingLesson.collectAsState()

    val courses by viewModel.courses.collectAsState()
    val bookmarkedLessons by viewModel.bookmarkedLessons.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val todayTasks by viewModel.todayTasks.collectAsState()
    val recommendMsg by viewModel.scheduleRecommendationMessage.collectAsState()

    val activeCourse = courses.find { it.id == selectedCourseId }

    Scaffold(
        topBar = {
            if (selectedCourseId == null) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "EduTrack",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFFE6E1E5)
                            )
                            Text(
                                text = "Good morning, Learner",
                                fontSize = 12.sp,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    },
                    actions = {
                        // Quick Search Circle Button
                        IconButton(
                            onClick = { viewModel.currentTab.value = "courses" },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF2B2930), shape = CircleShape)
                                .border(1.dp, Color(0xFF49454F), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Active profile badge
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .background(Color(0xFFD0BCFF), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF141218)
                    )
                )
            }
        },
        bottomBar = {
            if (selectedCourseId == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        Triple("home", "Home", Icons.Default.Home),
                        Triple("courses", "Courses", Icons.Default.LibraryBooks),
                        Triple("schedule", "Schedule", Icons.Default.CalendarMonth),
                        Triple("focus", "Focus Room", Icons.Default.Timer),
                        Triple("dashboard", "Dashboard", Icons.Default.Leaderboard)
                    )

                    tabs.forEach { (tabId, label, icon) ->
                        NavigationBarItem(
                            selected = currentTab == tabId,
                            onClick = { viewModel.currentTab.value = tabId },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedCourseId == null && currentTab == "courses") {
                ExtendedFloatingActionButton(
                    text = { Text("Create Course", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { viewModel.isCreatingCourse.value = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedCourseId != null && activeCourse != null) {
                CourseDetailAndPlayerScreen(
                    course = activeCourse,
                    viewModel = viewModel,
                    onBack = {
                        viewModel.selectedCourseId.value = null
                        viewModel.activeLessonId.value = null
                    }
                )
            } else {
                when (currentTab) {
                    "home" -> HomeScreen(viewModel, todayTasks, userStats, recommendMsg)
                    "courses" -> CoursesScreen(viewModel, courses, isSearchActive, searchQuery)
                    "schedule" -> ScheduleScreen(viewModel, userStats)
                    "focus" -> FocusScreen(viewModel, userStats)
                    "dashboard" -> DashboardScreen(viewModel, userStats, achievements, courses)
                }
            }

            // Create Course Dialog Flow
            if (isCreatingCourse) {
                CreateCourseDialog(
                    onDismiss = { viewModel.isCreatingCourse.value = false },
                    onConfirm = { title, desc, tag ->
                        viewModel.createCustomCourse(title, desc, tag)
                        viewModel.isCreatingCourse.value = false
                    }
                )
            }
        }
    }
}

// ---------------- HOME SCREEN ----------------

@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    todayTasks: List<DailyTask>,
    userStats: UserStats?,
    recommendMsg: String?
) {
    val context = LocalContext.current
    var completedCount = todayTasks.count { it.isCompleted }
    var totalTasks = todayTasks.size
    val currentLessons by viewModel.currentLessons.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats Chips Row from Elegant Dark design
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Streak Chip
            QuickStatChip(
                icon = Icons.Default.LocalFireDepartment,
                value = "${userStats?.dailyStreak ?: 0} Day Streak",
                iconColor = Color(0xFFD0BCFF)
            )
            // Focused hours chip
            val hours = ((userStats?.totalStudyTimeMinutes ?: 0) / 60.0)
            val hoursText = if (hours > 0) "%.1fh".format(hours) else "${userStats?.totalStudyTimeMinutes ?: 0}m"
            QuickStatChip(
                icon = Icons.Default.Timer,
                value = "$hoursText Today",
                iconColor = Color(0xFFD0BCFF)
            )
            // Level stats chip
            val unlockedCount = viewModel.achievements.collectAsState().value.count { it.isUnlocked }
            QuickStatChip(
                icon = Icons.Default.Stars,
                value = "Level ${unlockedCount + 1}",
                iconColor = Color(0xFFD0BCFF)
            )
        }

        // Continue Watching Header Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CONTINUE WATCHING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCAC4D0),
                letterSpacing = 1.5.sp
            )
            Text(
                text = "See All",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD0BCFF),
                modifier = Modifier.clickable {
                    viewModel.currentTab.value = "courses"
                }
            )
        }

        // Continue Watching Interactive Module
        val continueTask = todayTasks.firstOrNull { !it.isCompleted } ?: todayTasks.firstOrNull()
        if (continueTask != null) {
            val courseTasks = todayTasks.filter { it.courseTitle == continueTask.courseTitle }
            val courseProgress = if (courseTasks.isNotEmpty()) {
                (courseTasks.count { it.isCompleted } * 100) / courseTasks.size
            } else {
                35
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { viewModel.launchLessonPlayer(continueTask.lessonId) },
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF381E72), Color(0xFF21005D))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = continueTask.courseTitle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Resume Lesson",
                            tint = Color(0xFFD0BCFF).copy(alpha = 0.9f),
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1B1F))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = continueTask.lessonTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$courseProgress% Complete",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCAC4D0)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { courseProgress.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = Color(0xFFD0BCFF),
                            trackColor = Color(0xFF49454F)
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { viewModel.currentTab.value = "courses" },
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF381E72), Color(0xFF21005D))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Computer Science 101",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Get Started",
                            tint = Color(0xFFD0BCFF).copy(alpha = 0.8f),
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1B1F))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Start Your Learning Journey!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5)
                            )
                            Text(
                                text = "0% Complete",
                                fontSize = 11.sp,
                                color = Color(0xFFCAC4D0)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = Color(0xFFD0BCFF),
                            trackColor = Color(0xFF49454F)
                        )
                    }
                }
            }
        }

        // Streak adjustment banner if recommended
        AnimatedVisibility(
            visible = recommendMsg != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            recommendMsg?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Smart Scheduler",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Smart Shift Applied",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { viewModel.scheduleRecommendationMessage.value = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Banner", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Daily Study Goal Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Goals Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (totalTasks == 0) "No tasks generated yet" else "Today's Task Progress",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$completedCount/$totalTasks Completed",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful custom linear progress
                val progressFraction = if (totalTasks > 0) completedCount.toFloat() / totalTasks else 0f
                val animProgress by animateFloatAsState(targetValue = progressFraction, label = "Progress")

                LinearProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "${userStats?.totalStudyTimeMinutes ?: 0} mins focused",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "${userStats?.preferredStudyHours ?: 2} hrs Daily Goal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Today's Study Schedule Checklist Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.currentTab.value = "schedule" }) {
                    Text("Optimize", fontWeight = FontWeight.Bold)
                }
            }

            if (todayTasks.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No study schedule generated for today",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Enroll in a course or set study parameters to auto-construct your timetable.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                viewModel.runScheduleGeneration()
                            }
                        ) {
                            Text("Generate Schedule Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                todayTasks.forEach { task ->
                    DailyTaskRow(task = task, onCheckedChange = {
                        viewModel.toggleLessonCompletedById(task.lessonId)
                    }, onClick = {
                        viewModel.launchLessonPlayer(task.lessonId)
                    })
                }
            }
        }

        // Pomodoro Card shortcut
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.currentTab.value = "focus" },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Need a Quick Focus Burst?",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Utilize our Duolingo-styled Pomodoro Timer to keep mental burnout at bay.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun DailyTaskRow(
    task: DailyTask,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF2B2930) else Color(0xFF1C1B1F)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF49454F)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Left vertical Accent highlight line
            if (task.isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(Color(0xFFD0BCFF))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(Color(0xFF49454F))
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time box on the left
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val timeStr = if (task.scheduledTime.isNotEmpty()) task.scheduledTime else "09:30"
                    val parts = timeStr.split(":")
                    val hours = parts.getOrNull(0)?.toIntOrNull() ?: 9
                    val period = if (hours < 12) "AM" else "PM"
                    val formattedHour = if (hours == 0) "12" else if (hours > 12) "${hours - 12}" else "$hours"
                    val formattedMinute = parts.getOrNull(1) ?: "30"
                    
                    Text(
                        text = period,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCAC4D0)
                    )
                    Text(
                        text = "$formattedHour:$formattedMinute",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE6E1E5)
                    )
                }

                // Middle info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.lessonTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFE6E1E5),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "YouTube • ${task.courseTitle}",
                        fontSize = 11.sp,
                        color = Color(0xFFCAC4D0)
                    )
                }

                // Smooth checked/unchecked indicator action
                IconButton(
                    onClick = { onCheckedChange(!task.isCompleted) },
                    modifier = Modifier.size(32.dp)
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(2.dp, Color(0xFF49454F), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatChip(
    icon: ImageVector,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .background(Color(0xFF2B2930), shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF49454F), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE6E1E5)
        )
    }
}

// ---------------- COURSES SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    viewModel: StudyViewModel,
    courses: List<Course>,
    isSearchActive: Boolean,
    searchQuery: String
) {
    var searchQueryMutable by remember { mutableStateOf(searchQuery) }
    val filteredCourses = courses.filter {
        it.title.contains(searchQueryMutable, ignoreCase = true) ||
                it.description.contains(searchQueryMutable, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Banner
        OutlinedTextField(
            value = searchQueryMutable,
            onValueChange = {
                searchQueryMutable = it
                viewModel.searchQuery.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search your courses...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQueryMutable.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQueryMutable = ""
                        viewModel.searchQuery.value = ""
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (searchQueryMutable.isNotEmpty()) "Search Results" else "My Custom Courses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${filteredCourses.size} Total",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (filteredCourses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "No study courses found",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Tap 'Create Course' to compile your first YouTube-driven syllabus!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.isCreatingCourse.value = true }
                    ) {
                        Text("Add Custom Course", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredCourses) { course ->
                    CourseCard(course = course, onClick = {
                        viewModel.selectedCourseId.value = course.id
                    }, onDelete = {
                        viewModel.deleteCourse(course)
                    })
                }
            }
        }
    }
}

fun getCourseGradient(subject: String): Brush {
    val clean = subject.lowercase().trim()
    if (clean.contains("science") || clean.contains("bio") || clean.contains("chem") || clean.contains("physics")) {
        return Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)))
    }
    if (clean.contains("design") || clean.contains("art") || clean.contains("ui") || clean.contains("craft")) {
        return Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E)))
    }
    if (clean.contains("math") || clean.contains("calc") || clean.contains("stat") || clean.contains("algebra") || clean.contains("percent")) {
        return Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
    }
    if (clean.contains("code") || clean.contains("program") || clean.contains("dev") || clean.contains("tech") || clean.contains("comput")) {
        return Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)))
    }
    // Dynamic color choice using a quick hash index
    val hash = java.lang.Math.abs(clean.hashCode())
    val presets = listOf(
        listOf(Color(0xFFF59E0B), Color(0xFFD97706)), // Amber / Gold
        listOf(Color(0xFFEF4444), Color(0xFFDC2626)), // Red / Crimson
        listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)), // Purple
        listOf(Color(0xFFEC4899), Color(0xFFDB2777)), // Pink
        listOf(Color(0xFF14B8A6), Color(0xFF0D9488)), // Teal
        listOf(Color(0xFF6366F1), Color(0xFF4F46E5)), // Indigo
        listOf(Color(0xFF84CC16), Color(0xFF65A30D))  // Lime
    )
    val colors = presets[hash % presets.size]
    return Brush.linearGradient(colors)
}

fun getCourseIcon(subject: String): ImageVector {
    val clean = subject.lowercase().trim()
    if (clean.contains("science") || clean.contains("bio") || clean.contains("chem") || clean.contains("physics")) {
        return Icons.Default.Science
    }
    if (clean.contains("design") || clean.contains("art") || clean.contains("ui") || clean.contains("craft")) {
        return Icons.Default.Brush
    }
    if (clean.contains("math") || clean.contains("calc") || clean.contains("stat") || clean.contains("algebra") || clean.contains("percent")) {
        return Icons.Default.Percent
    }
    if (clean.contains("code") || clean.contains("program") || clean.contains("dev") || clean.contains("tech") || clean.contains("comput")) {
        return Icons.Default.Code
    }
    if (clean.contains("book") || clean.contains("read") || clean.contains("history") || clean.contains("english") || clean.contains("write") || clean.contains("novel")) {
        return Icons.Default.MenuBook
    }
    return Icons.Default.School
}

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val brush = getCourseGradient(course.thumbnail)
    val icon = getCourseIcon(course.thumbnail)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column {
            // Elegant gradient cover card (Duolingo visual aspect)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(brush)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.22f), shape = CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.35f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = course.thumbnail.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Delete Course Button mapping
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.15f), shape = CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Course",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Launch Course Syllabus",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ---------------- CREATE COURSE DIALOG ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var subjectText by remember { mutableStateOf("General") }

    val recommendations = listOf(
        "General", "Coding", "Mathematics", "Science", "Design", 
        "Physics", "Chemistry", "History", "Literature", "Finance"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Create Custom Course", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Course Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = subjectText,
                    onValueChange = { subjectText = it },
                    label = { Text("Subject (Custom or Selected)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("Quick Subject Suggestions", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommendations) { rec ->
                        val isSelected = subjectText.lowercase().trim() == rec.lowercase().trim()
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { subjectText = rec }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = rec,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        val finalSubject = if (subjectText.isBlank()) "General" else subjectText.trim()
                        onConfirm(title, desc, finalSubject)
                    }
                },
                enabled = title.isNotEmpty()
            ) {
                Text("Compile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ---------------- SCHEDULE SCREEN ----------------

@Composable
fun ScheduleScreen(viewModel: StudyViewModel, userStats: UserStats?) {
    var hours by remember { mutableFloatStateOf((userStats?.preferredStudyHours ?: 2).toFloat()) }
    var inputTime by remember { mutableStateOf(userStats?.preferredStudyTime ?: "18:00") }
    var isEditing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Study Planner Lab",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "EduTrack's automated scheduling algorithm compiles a balanced daily checklist of lessons from your enrolled list based on your preferred hourly study limit.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f)
        )

        // Preferences Setup Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule Allocation Profile",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = {
                        if (isEditing) {
                            viewModel.updatePreferredSchedule(hours.toInt(), inputTime)
                        }
                        isEditing = !isEditing
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save Preferences" else "Edit Preferences",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                // Max Study Hours Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Daily Studying Limit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${hours.toInt()} Hours / Day",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = hours,
                        onValueChange = { if (isEditing) hours = it },
                        valueRange = 1f..8f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = isEditing
                    )
                }

                // Preferred Study Start Time Text Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Preferred Start Time", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (isEditing) {
                        OutlinedTextField(
                            value = inputTime,
                            onValueChange = { inputTime = it },
                            modifier = Modifier.width(100.dp),
                            placeholder = { Text("HH:MM") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            isError = !inputTime.matches(Regex("\\d{2}:\\d{2}")),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = userStats?.preferredStudyTime ?: "18:00",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Daily Notification Settings Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notify Before Study Block Starts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Recieve motivational prompts on scheduled hours.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Switch(
                checked = userStats?.reminderEnabled ?: true,
                onCheckedChange = { value ->
                    viewModel.setReminderEnabled(value)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Generate schedule button
        Button(
            onClick = {
                scope.launch {
                    viewModel.generateDailySchedule()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Re-Optimize Schedule Now", fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------- ACTION FOCUS SCREEN (POMODORO) ----------------

@Composable
fun FocusScreen(viewModel: StudyViewModel, userStats: UserStats?) {
    val timeLeft by viewModel.pomodoroTimeLeft.collectAsState()
    val isRunning by viewModel.pomodoroRunning.collectAsState()
    val mode by viewModel.pomodoroMode.collectAsState()
    val sessions by viewModel.pomodoroSessionsCompleted.collectAsState()

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val scope = rememberCoroutineScope()

    // Interactive circular animation progress state using canvas
    val targetTotal = (if (mode == "Focus") userStats?.pomodoroTimeMinutes ?: 25 else 5) * 60f
    val sweepFraction = if (targetTotal > 0) timeLeft.toFloat() / targetTotal else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Study Focus Room",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Activate Pomodoro sessions to train cognitive discipline.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        // Circular Timer Widget
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Elegant glowing radial rings representing focus states
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2) - 12f
                val center = Offset(size.width / 2, size.height / 2)
                drawArc(
                    color = if (mode == "Focus") DarkPrimary else DarkSecondary,
                    startAngle = -90f,
                    sweepAngle = 360f * sweepFraction,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = mode.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (mode == "Focus") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Sessions today: $sessions",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Interactive control buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = { viewModel.resetPomodoro() },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
                    .size(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Primary Play / Pause
            Button(
                onClick = {
                    if (isRunning) {
                        viewModel.pausePomodoro()
                    } else {
                        viewModel.startPomodoro()
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .height(64.dp)
                    .width(140.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRunning) "Pause" else "Focus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Target Toggle Button
            IconButton(
                onClick = { viewModel.togglePomodoroDuration() },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
                    .size(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = "Change Duration",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Helper explanation card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Pro Tip: Tap hourglass above to switch intervals",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Choose from rapid 15 min sprint, standard 25 min cycle, or deep 50 min session. Completing focus slots updates your study time count instantly!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ---------------- DASHBOARD & BADGES ----------------

@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    userStats: UserStats?,
    achievements: List<Achievement>,
    courses: List<Course>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Study Analytics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )

        // Metrics Row Grid (Notion styled cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Minutes Focused Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = CircleShape
                                )
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${userStats?.totalStudyTimeMinutes ?: 0}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Mins",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Streak Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MetricStreakColor.copy(alpha = 0.12f),
                                    shape = CircleShape
                                )
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MetricStreakColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${userStats?.dailyStreak ?: 0}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MetricStreakColor
                    )
                    Text(
                        text = "Daily Streak",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // High-Quality Weekly Analytics simulated chart using vectors
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Analytics Time (Minutes)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Bar Graph using Row
                val weeklyLogs = listOf(
                    Pair("Mon", 10),
                    Pair("Tue", 15),
                    Pair("Wed", 45), // Studied extra hours !
                    Pair("Thu", 20),
                    Pair("Fri", 12),
                    Pair("Sat", 30),
                    Pair("Sun", userStats?.totalStudyTimeMinutes?.coerceAtMost(60) ?: 0)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyLogs.forEach { log ->
                        val barHeightFraction = (log.second / 60f).coerceIn(0.1f..1f)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(barHeightFraction)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Text(text = log.first, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Achievements Badges section
        Text(
            text = "My Achievements & Badges",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )

        achievements.forEach { badge ->
            BadgeRow(badge = badge)
        }
    }
}

@Composable
fun BadgeRow(badge: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (badge.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val icon = when (badge.iconName) {
                "school" -> Icons.Default.School
                "edit_note" -> Icons.Default.EditNote
                "local_fire_department" -> Icons.Default.LocalFireDepartment
                "timer" -> Icons.Default.Timeline
                else -> Icons.Default.HourglassEmpty
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = (if (badge.isUnlocked) MaterialTheme.colorScheme.tertiary else Color.Gray)
                            .copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) MaterialTheme.colorScheme.tertiary else Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = badge.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "UNLOCKED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LOCKED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ---------------- COURSE DETAIL AND EMBEDDED PLAYER SCREEN ----------------

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailAndPlayerScreen(
    course: Course,
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val lessons by viewModel.currentLessons.collectAsState()
    val activeLessonId by viewModel.activeLessonId.collectAsState()

    val currentLesson = lessons.find { it.id == activeLessonId } ?: lessons.firstOrNull()

    var activeTab by remember { mutableStateOf("lessons") } // "lessons", "notes", "timestamps"

    var noteText by remember { mutableStateOf("") }
    var timestampStr by remember { mutableStateOf("") }
    var timestampNoteStr by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    var isAddingLessonDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentLesson) {
        if (currentLesson != null) {
            noteText = currentLesson.notes
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // High-Quality Custom Web Video Block
        if (currentLesson != null) {
            val videoId = extractYoutubeVideoId(currentLesson.youtubeUrl)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {

    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.loadsImagesAutomatically = true
    settings.useWideViewPort = true
    settings.loadWithOverviewMode = true
    settings.mediaPlaybackRequiresUserGesture = false
    settings.allowFileAccess = true
    settings.allowContentAccess = true
    settings.mixedContentMode =
        android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

    webViewClient = WebViewClient()
    webChromeClient = WebChromeClient()

    loadUrl(
        "https://www.youtube.com/embed/$videoId"
    )
                        }
                    },
                    update = { webView ->
                        val currentUrl = webView.url ?: ""
                        val targetUrl = "https://www.youtube.com/embed/$videoId?autoplay=1&enablejsapi=1"
                        if (!currentUrl.contains(videoId)) {
                            webView.loadUrl(targetUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Placeholder empty state cover block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(44.dp))
                    Text("No lessons inside course yet", color = Color.LightGray)
                }
            }
        }

        // Toolbar
        TopAppBar(
            title = {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { isAddingLessonDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Lesson", tint = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Segmented bar representing Notion-like tabs
        TabRow(
            selectedTabIndex = when (activeTab) {
                "lessons" -> 0
                "notes" -> 1
                else -> 2
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = activeTab == "lessons", onClick = { activeTab = "lessons" }) {
                Text("Syllabus", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(selected = activeTab == "notes", onClick = { activeTab = "notes" }) {
                Text("Notes Block", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(selected = activeTab == "timestamps", onClick = { activeTab = "timestamps" }) {
                Text("Timestamps", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Tabs Content container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                "lessons" -> {
                    if (lessons.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AddComment, null, modifier = Modifier.size(44.dp), tint = Color.Gray)
                                Text("No lessons yet! Add one using the top '+' bar.", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        val lessonsByChapter = lessons.groupBy { it.chapterName }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            lessonsByChapter.forEach { (chapter, chapterLessons) ->
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp, bottom = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = chapter.uppercase(),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${chapterLessons.size} ${if (chapterLessons.size == 1) "Lesson" else "Lessons"}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Divider(
                                            modifier = Modifier.padding(top = 6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        )
                                    }
                                }

                                items(chapterLessons) { lesson ->
                                    val isPlaying = activeLessonId == lesson.id || (activeLessonId == null && lessons.firstOrNull()?.id == lesson.id)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.activeLessonId.value = lesson.id },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Transparent
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Checkbox(
                                                checked = lesson.isCompleted,
                                                onCheckedChange = { viewModel.toggleLessonCompleted(lesson) }
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = lesson.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (lesson.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(text = "${lesson.videoDuration} mins video", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            }

                                            IconButton(onClick = { viewModel.toggleLessonBookmark(lesson) }) {
                                                Icon(
                                                    imageVector = if (lesson.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = "Bookmark",
                                                    tint = if (lesson.isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "notes" -> {
                    if (currentLesson == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Assign a lesson first", color = Color.Gray)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Offline Study Notebook (Saved)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )

                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 180.dp),
                                placeholder = { Text("Outline concepts, flashcard text, spaced repetition formulas directly under this video...") },
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.updateLessonNotes(currentLesson.id, noteText)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Persist Offline Sticky Notes", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                "timestamps" -> {
                    if (currentLesson == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Assign a lesson first", color = Color.Gray)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Bookmark Video Timestamps",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = timestampStr,
                                    onValueChange = { timestampStr = it },
                                    placeholder = { Text("02:15") },
                                    label = { Text("Time") },
                                    modifier = Modifier.weight(0.35f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = timestampNoteStr,
                                    onValueChange = { timestampNoteStr = it },
                                    placeholder = { Text("Key Formula") },
                                    label = { Text("Bookmark Label") },
                                    modifier = Modifier.weight(0.65f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                            }

                            Button(
                                onClick = {
                                    if (timestampStr.isNotEmpty() && timestampNoteStr.isNotEmpty()) {
                                        viewModel.addNoteTimestamp(currentLesson.id, timestampStr, timestampNoteStr)
                                        timestampStr = ""
                                        timestampNoteStr = ""
                                    }
                                },
                                enabled = timestampStr.isNotEmpty() && timestampNoteStr.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Bookmark, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Bookmark Node", fontWeight = FontWeight.Bold)
                            }

                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                            // Timestamp items list
                            val list = currentLesson.savedTimestamps.split(",").filter { it.contains("|") }
                            if (list.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No timestamp marks logged yet.", color = Color.Gray, fontSize = 11.sp)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(list) { index, item ->
                                        val node = item.split("|")
                                        val time = node.getOrNull(0) ?: "00:00"
                                        val desc = node.getOrNull(1) ?: "Mark"

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = time,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    Text(text = desc, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                }

                                                IconButton(onClick = {
                                                    viewModel.deleteLessonTimestamp(currentLesson.id, index)
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Lesson Dialog Block
    if (isAddingLessonDialog) {
        AddLessonDialog(
            onDismiss = { isAddingLessonDialog = false },
            onConfirm = { title, desc, url, mins, chapter ->
                viewModel.addLessonToCourse(course.id, title, desc, url, mins, chapter)
                isAddingLessonDialog = false
            }
        )
    }
}

@Composable
fun AddLessonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("15") }
    var chapterName by remember { mutableStateOf("Chapter 1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary)
                Text("Append Course Lesson", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Lesson Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Brief Explanation") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("YouTube Link") },
                    placeholder = { Text("https://www.youtube.com/watch?v=...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it },
                    label = { Text("Video Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = chapterName,
                    onValueChange = { chapterName = it },
                    label = { Text("Chapter / Module (e.g. Chapter 1, Basics)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationStr.toIntOrNull() ?: 15
                    val finalChapter = if (chapterName.isBlank()) "Chapter 1" else chapterName.trim()
                    if (title.isNotEmpty() && youtubeUrl.isNotEmpty()) {
                        onConfirm(title, desc, youtubeUrl, duration, finalChapter)
                    }
                },
                enabled = title.isNotEmpty() && youtubeUrl.isNotEmpty()
            ) {
                Text("Insert Lesson", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ---------------- GLOBAL HELPER FUNCTION ----------------

fun extractYoutubeVideoId(url: String): String {
    return try {
        if (url.contains("embed/")) {
            url.split("embed/")[1].split("?")[0]
        } else if (url.contains("v=")) {
            url.split("v=")[1].split("&")[0]
        } else if (url.contains("youtu.be/")) {
            url.split("youtu.be/")[1].split("?")[0]
        } else {
            url
        }
    } catch (e: Exception) {
        ""
    }
}
