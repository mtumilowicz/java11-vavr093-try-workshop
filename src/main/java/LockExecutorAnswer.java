import io.vavr.control.Try;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

public class LockExecutorAnswer {
    private final ReadWriteLock lock;

    LockExecutorAnswer(ReadWriteLock lock) {
        this.lock = lock;
    }

    <T> Try<T> write(Supplier<T> action) {
        lock.writeLock().lock();
        return Try.ofSupplier(action)
                .andFinally(() -> lock.writeLock().unlock());
    }

    <T> Try<T> read(Supplier<T> action) {
        lock.readLock().lock();
        return Try.ofSupplier(action)
                .andFinally(() -> lock.readLock().unlock());
    }
}