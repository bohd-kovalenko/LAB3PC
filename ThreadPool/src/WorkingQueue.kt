import java.time.Duration
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class WorkingQueue<T>(
    internal val queue: Queue<T>,
    internal val lock: Lock = ReentrantLock(),
    internal val condition: Condition = lock.newCondition(),
    internal var sumLoad: Duration = Duration.ZERO,
    var state: ThreadPoolState = ThreadPoolState.ACTIVE,
    internal var id: String = UUID.randomUUID().toString()
) {
    val averageLoad: MutableList<Int> = mutableListOf()

    fun pushTask(task: T) {
        if (state == ThreadPoolState.ACTIVE) {
            averageLoad.add(queue.size)
            queue.offer(task)
            lock.withLock {
                condition.signalAll()
            }
        }
    }
}
