import lombok.Value;

import java.util.function.Predicate;

/**
 * Created by mtumilowicz on 2019-03-03.
 */
@Value
class Person {
    int age;

    static Predicate<Person> isAdult() {
        return person -> person.age >= 18;
    }
}

class NotAnAdultException extends Exception {

}
