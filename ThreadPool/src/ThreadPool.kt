interface ThreadPool {
    
    fun execute(task: Task)

    fun shutdown(force: Boolean = false)

    fun pause()

    fun resume()
}