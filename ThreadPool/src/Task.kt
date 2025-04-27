import java.time.Duration
import java.time.Instant

data class Task(
    val duration: Duration,
    val creationTime: Instant,
    val execute: () -> Unit
)
