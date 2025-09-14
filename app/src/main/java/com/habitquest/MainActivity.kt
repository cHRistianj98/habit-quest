package com.habitquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HabitQuestApp() }
    }
}

@Composable
fun HabitQuestApp() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        HomeScreen()
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
    val id: String, val title: String, val progress: Float, // 0..1
    val milestones: List<MilestoneUi>
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var goals by remember { mutableStateOf(listOf(sampleGoal())) }


    Scaffold(topBar = { TopAppBar(title = { Text("HabitQuest") }) }) { padding ->
        LazyColumn(contentPadding = padding) {
            items(goals) { g -> GoalCard(g) }
            item {
                Spacer(Modifier.height(8.dp))
                ElevatedButton(
                    onClick = { goals = goals + sampleGoal(copyId = true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text("Dodaj cel (demo)") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}


@Composable
fun GoalCard(goal: GoalUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* Navigation to details here later */ }) {
        Column(Modifier.padding(16.dp)) {
            Text(goal.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { goal.progress })
            Spacer(Modifier.height(8.dp))
            Text("Następny krok: ${goal.milestones.firstOrNull { it.state != MilestoneState.REACHED }?.title ?: "Wszystko zaliczone!"}")
            Spacer(Modifier.height(12.dp))
            MilestoneTimeline(goal.milestones)
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
    Surface(
        tonalElevation = if (locked) 0.dp else 2.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (locked) Icons.Filled.Lock else Icons.Filled.EmojiEvents,
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.title, fontWeight = FontWeight.SemiBold)
                Text(m.subtitle, style = MaterialTheme.typography.bodySmall)
            }
            val actionEnabled = !locked
            TextButton(enabled = actionEnabled, onClick = { /* otwórz listę nagród */ }) {
                Text(if (actionEnabled) "Nagrody" else "Zablokowane")
            }
        }
    }
}

private fun sampleGoal(copyId: Boolean = false): GoalUi {
    val ms = listOf(
        MilestoneUi("m1", 1, "5 × 3 km", "Odblokuj koszulki (B/Ś/P)", MilestoneState.REACHED),
        MilestoneUi("m2", 2, "5 × 5 km", "Odblokuj spodenki (B/Ś/P)", MilestoneState.ACTIVE),
        MilestoneUi("m3", 3, "3 × 10 km", "Pas na bidon (B/Ś/P)", MilestoneState.LOCKED),
        MilestoneUi("m4", 4, "1 × półmaraton", "Buty treningowe (B/Ś/P)", MilestoneState.LOCKED),
        MilestoneUi("m5", 5, "4 tyg. planu", "Zegarek/pas HR (B/Ś/P)", MilestoneState.LOCKED),
        MilestoneUi("m6", 6, "Maraton", "Super badge + zestaw", MilestoneState.LOCKED),
    )
    val progress = ms.count { it.state == MilestoneState.REACHED }.toFloat() / ms.size
    return GoalUi(
        id = if (copyId) java.util.UUID.randomUUID().toString() else "g1",
        title = "Maraton — plan bazowy",
        progress = progress,
        milestones = ms
    )
}
