package io.github.pitbull51067.focus_plan_builder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pitbull51067.focus_plan_builder.ui.theme.Focus_Plan_BuilderTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

data class FocusPlan(
    val subject: String,
    val minutes: Int,
    val category: String,
    val breakMinutes: Int
)

// ---------------------------------------------------------------------------
// Pure calculation functions (easy to unit test on their own)
// ---------------------------------------------------------------------------

fun durationCategory(minutes: Int): String = when {
    minutes < 10 -> "Invalid"
    minutes in 10..29 -> "Quick review"
    minutes in 30..60 -> "Focused session"
    else -> "Extended session"
}

fun recommendedBreak(minutes: Int): Int = when {
    minutes in 10..29 -> 5
    minutes in 30..60 -> 10
    else -> 15
}

// ---------------------------------------------------------------------------
// Activity
// ---------------------------------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Focus_Plan_BuilderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FocusPlanRoute(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// State owner ("smart" composable)
// ---------------------------------------------------------------------------

@Composable
fun FocusPlanRoute(modifier: Modifier = Modifier) {
    // Survives process death / rotation.
    var subject by rememberSaveable { mutableStateOf("") }
    var minutesText by rememberSaveable { mutableStateOf("") }

    // The generated plan does NOT need to survive rotation per the spec,
    // so plain `remember` is enough here.
    var plan by remember { mutableStateOf<FocusPlan?>(null) }

    // Never call toInt() directly on user input -- toIntOrNull() can't crash.
    val minutes: Int? = minutesText.toIntOrNull()

    // Derived, not a separately-tracked mutable Boolean.
    val canCreatePlan = subject.isNotBlank() && minutes != null && minutes in 10..180

    FocusPlanScreen(
        subject = subject,
        minutesText = minutesText,
        plan = plan,
        onSubjectChange = { newValue ->
            subject = newValue
            plan = null // editing an input clears any previous result
        },
        onMinutesChange = { newValue ->
            minutesText = newValue
            plan = null
        },
        canCreatePlan = canCreatePlan,
        onCreatePlan = {
            val m = minutesText.toIntOrNull()
            if (subject.isNotBlank() && m != null && m in 10..180) {
                plan = FocusPlan(
                    subject = subject.trim(),
                    minutes = m,
                    category = durationCategory(m),
                    breakMinutes = recommendedBreak(m)
                )
            }
        },
        modifier = modifier
    )
}

// ---------------------------------------------------------------------------
// Stateless screen ("dumb" composable)
// ---------------------------------------------------------------------------

@Composable
fun FocusPlanScreen(
    subject: String,
    minutesText: String,
    plan: FocusPlan?,
    onSubjectChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    canCreatePlan: Boolean,
    onCreatePlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Focus Plan Builder",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter a subject and the number of minutes you have available " +
                "to generate a focused study plan."
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = onSubjectChange,
            label = { Text("Study subject") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = minutesText,
            onValueChange = onMinutesChange,
            label = { Text("Available minutes (10-180)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onCreatePlan,
                enabled = canCreatePlan
            ) {
                Text("Create plan")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (plan != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = plan.subject,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Duration: ${plan.minutes} minutes")
                    Text(text = "Category: ${plan.category}")
                    Text(text = "Recommended break: ${plan.breakMinutes} minutes")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Study ${plan.subject} for ${plan.minutes} minutes, " +
                            "and then take a ${plan.breakMinutes}-minute break."
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Preview (visible in Android Studio's Design pane without running the app)
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun FocusPlanScreenPreview() {
    Focus_Plan_BuilderTheme {
        FocusPlanScreen(
            subject = "Compose State",
            minutesText = "45",
            plan = FocusPlan(
                subject = "Compose State",
                minutes = 45,
                category = durationCategory(45),
                breakMinutes = recommendedBreak(45)
            ),
            onSubjectChange = {},
            onMinutesChange = {},
            canCreatePlan = true,
            onCreatePlan = {}
        )
    }
}
