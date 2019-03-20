import io.vavr.control.Try;
import lombok.Value;

/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Repository {
    /*
    implement function that will try to:
        1. return result from database (DatabaseRepository)
        1. if not found: return result from backup (BackupRepository)
     */
    static Try<String> findById(int id) {
        return null;
    }
    
    static Try<String> findByIdRecovered(int id) {
        return CacheRepository.findById(id)
                .recover(CacheSynchronization.class, "cache synchronization with database, try again later")
                .recoverWith(CacheUserCannotBeFound.class, value -> DatabaseRepository.findById(value.getUserId()))
                .recover(DatabaseConnectionProblem.class, "cannot connect to database");
    }
}

class CacheRepository {
    static Try<String> findById(int id) {
        if (id == 2) {
            return Try.failure(new CacheUserCannotBeFound(id));
        }
        if (id == 3) {
            return Try.failure(new CacheUserCannotBeFound(id));
        }        
        if (id == 4) {
            return Try.failure(new CacheUserCannotBeFound(id));
        }
        if (id == 5) {
            return Try.failure(new CacheSynchronization());
        }
        return Try.of(() -> "from cache");
    }
}

class DatabaseRepository {
    static Try<String> findById(int id) {
        switch (id) {
            case 2:
                return Try.failure(new DatabaseConnectionProblem());
            case 3:
                return Try.failure(new DatabaseUserCannotBeFound());
            default:
                return Try.of(() -> "from database");
        }
    }

}

class DatabaseUserCannotBeFound extends RuntimeException {

}

@Value
class CacheUserCannotBeFound extends RuntimeException {
    int userId;
}

class DatabaseConnectionProblem extends RuntimeException {

}

class CacheSynchronization extends RuntimeException {
    
}
