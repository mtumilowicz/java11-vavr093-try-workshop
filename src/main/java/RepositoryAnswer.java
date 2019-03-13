import io.vavr.control.Try;

/**
 * Created by mtumilowicz on 2019-03-13.
 */
class RepositoryAnswer {
    static Try<String> findById(int id) {
        return DatabaseRepository.findById(id).orElse(() -> BackupRepository.findById(id));
    }
}
