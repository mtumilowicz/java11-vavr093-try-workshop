import io.vavr.control.Try;

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
        return Try.failure(new NullPointerException());
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

class BackupRepository {
    static Try<String> findById(int id) {
        if (id == 3) {
            return Try.failure(new BackupRepositoryConnectionProblem());
        }
        return Try.of(() -> "from cache");
    }
}

class DatabaseUserCannotBeFound extends Exception {

}

class BackupRepositoryConnectionProblem extends Exception {

}

class DatabaseConnectionProblem extends Exception {

}
