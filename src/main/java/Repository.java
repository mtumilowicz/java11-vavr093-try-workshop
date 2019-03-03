import io.vavr.control.Try;

/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Repository {
    static Try<String> findById(int id) {
        switch (id) {
            case 2:
                return Try.failure(new DatabaseConnectionProblem());
            case 3:
                return Try.failure(new UserCannotBeFound());
            default:
                return Try.of(() -> "found-by-id");
        }
    }

    static Try<String> findByName(String name) {
        switch (name) {
            case "database":
                return Try.failure(new DatabaseConnectionProblem());
            case "not-found":
                return Try.failure(new UserCannotBeFound());
            default:
                return Try.of(() -> "found-by-name");
        }
    }
}

class UserCannotBeFound extends Exception {

}

class DatabaseConnectionProblem extends Exception {

}
