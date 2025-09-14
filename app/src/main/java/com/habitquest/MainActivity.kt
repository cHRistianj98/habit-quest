package com.habitquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HabitQuestApp() }
    }
}

@Composable
fun HabitQuestApp() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        AppNav()
    }
}

enum class MilestoneState { LOCKED, ACTIVE, REACHED }

data class MilestoneUi(
    val id: String,
    val index: Int,
    val title: String,
    val subtitle: String,
    val state: MilestoneState
)

data class GoalUi(
    val id: String,
    val title: String,
    val progress: Float, // 0..1
    val milestones: List<MilestoneUi>
)

private fun sampleGoals(): List<GoalUi> = listOf(sampleGoal("g1"))
private fun sampleGoal(id: String = UUID.randomUUID().toString()): GoalUi {
    val ms = listOf(
        MilestoneUi("m1", 1, "5 × 3 km", "Odblokuj koszulki (B/Ś/P)", MilestoneState.REACHED),
        MilestoneUi("m2", 2, "5 × 5 km", "Odblokuj spodenki (B/Ś/P)", MilestoneState.ACTIVE),
        MilestoneUi("m3", 3, "3 × 10 km", "Pas na bidon (B/Ś/P)", MilestoneState.LOCKED),
        MilestoneUi("m4", 4, "1 × półmaraton", "Buty treningowe (B/Ś/P)", MilestoneState.LOCKED),
        MilestoneUi("m5", 5, "4 tyg. planu", "Zegarek/pas HR (B/Ś/P)", MilestoneState.LOCKED),
        MilestoneUi("m6", 6, "Maraton", "Super badge + zestaw", MilestoneState.LOCKED),
    )
    val progress = ms.count { it.state == MilestoneState.REACHED }.toFloat() / ms.size
    return GoalUi(id, "Maraton — plan bazowy", progress, ms)
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val goals = remember { sampleGoals() }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                initialGoals = goals,
                onGoalClick = { id -> nav.navigate("goal/$id") }
            )
        }
        composable(
            route = "goal/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("goalId")
            val goal = goals.find { it.id == id }
            GoalDetailScreen(goal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(initialGoals: List<GoalUi>, onGoalClick: (String) -> Unit) {
    var goals by remember { mutableStateOf(initialGoals) }

    androidx.compose.material3.Scaffold(topBar = { TopAppBar(title = { Text("HabitQuest") }) }) { padding ->
        LazyColumn(contentPadding = padding) {
            items(goals) { g -> GoalCard(g) { onGoalClick(g.id) } }
            item {
                Spacer(Modifier.height(8.dp))
                ElevatedButton(
                    onClick = { goals = goals + sampleGoal() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("Dodaj cel (demo)") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun GoalCard(goal: GoalUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(goal.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { goal.progress })
            Spacer(Modifier.height(8.dp))
            val nextLabel = goal.milestones.firstOrNull { it.state != MilestoneState.REACHED }?.title ?: "Wszystko zaliczone!"
            Text("Następny krok: $nextLabel")
            Spacer(Modifier.height(12.dp))
            MilestoneTimeline(goal.milestones)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(goal: GoalUi?) {
    androidx.compose.material3.Scaffold(topBar = { TopAppBar(title = { Text("Szczegóły celu") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            if (goal == null) {
                Text("Nie znaleziono celu")
            } else {
                Text(goal.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(progress = { goal.progress })
                Spacer(Modifier.height(16.dp))
                MilestoneTimeline(goal.milestones)
            }
        }
    }
}

@Composable
fun MilestoneTimeline(milestones: List<MilestoneUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        milestones.forEach { m -> MilestoneRow(m) }
    }
}

@Composable
fun MilestoneRow(m: MilestoneUi) {
    val locked = m.state == MilestoneState.LOCKED
    Surface(tonalElevation = if (locked) 0.dp else 2.dp, shape = MaterialTheme.shapes.medium) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(if (locked) Icons.Filled.Lock else Icons.Filled.EmojiEvents, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.title, fontWeight = FontWeight.SemiBold)
                Text(m.subtitle, style = MaterialTheme.typography.bodySmall)
            }
            val actionEnabled = !locked
            TextButton(enabled = actionEnabled, onClick = { /* TODO: nagrody */ }) {
                Text(if (actionEnabled) "Nagrody" else "Zablokowane")
            }
        }
    }
}