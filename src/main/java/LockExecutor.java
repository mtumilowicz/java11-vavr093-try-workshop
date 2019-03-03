import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

class LockExecutor {
    private final ReadWriteLock lock;

    LockExecutor(ReadWriteLock lock) {
        this.lock = lock;
    }

    <T> T write(Supplier<T> action) {
        lock.writeLock().lock();
        try {
            return action.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    <T> T read(Supplier<T> action) {
        lock.readLock().lock();
        try {
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }
}