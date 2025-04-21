import java.time.Duration
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class WorkingQueue<T>(
    internal val queue: Queue<T>,
    internal val lock: Lock = ReentrantLock(),
    internal val condition: Condition = lock.newCondition(),
    internal var sumLoad: Duration = Duration.ZERO,
    internal var state: ThreadPoolState = ThreadPoolState.ACTIVE
) {

    fun pushTask(task: T) {
        if (state == ThreadPoolState.ACTIVE) {
        queue.offer(task)
            lock.withLock {
                condition.signalAll()
            }
        }
    }
}