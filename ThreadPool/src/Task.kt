import java.time.Duration

data class Task(
    val duration: Duration,
    val execute: () -> Unit
)