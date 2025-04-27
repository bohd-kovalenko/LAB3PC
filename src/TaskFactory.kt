import java.time.Duration
import java.time.Instant
import java.util.*

class TaskFactory {

    val averageTimeToComplete: MutableList<Long> = mutableListOf()

    private companion object {
        const val LOWER_BOUND = 5L
        const val UPPER_BOUND = 21L
    }

    private val random = Random()

    fun createTask(): Task {
        val duration = Duration.ofSeconds(random.nextLong(LOWER_BOUND, UPPER_BOUND))
        val creationTime = Instant.now()
        return Task(duration, creationTime) {
            Thread.sleep(duration)
            val timeToComplete = Duration.between(creationTime, Instant.now())
            averageTimeToComplete.add(timeToComplete.toMillis())
            println("Task finished! Duration: $duration, current thread: ${Thread.currentThread().name}, time to complete: $timeToComplete")
        }
    }
}
