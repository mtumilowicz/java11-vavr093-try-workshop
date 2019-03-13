import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
class LockExecutorAnswer {
    ReadWriteLock lock;

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