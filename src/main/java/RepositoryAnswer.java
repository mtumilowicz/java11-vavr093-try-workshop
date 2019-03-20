import io.vavr.control.Try;

/**
 * Created by mtumilowicz on 2019-03-13.
 */
class RepositoryAnswer {
    static Try<String> findById(int id) {
        return CacheRepository.findById(id).orElse(() -> DatabaseRepository.findById(id));
    }
}
