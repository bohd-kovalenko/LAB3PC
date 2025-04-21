import java.time.Duration
import java.util.*

class TaskFactory {

    private companion object {
        const val LOWER_BOUND = 5L
        const val UPPER_BOUND = 21L
    }

    private val random = Random()

    fun createTask(): Task {
        val duration = Duration.ofSeconds(random.nextLong(LOWER_BOUND, UPPER_BOUND))
        return Task(duration) {
            Thread.sleep(duration)
            println("Task finished! Duration: $duration")
        }
    }
}