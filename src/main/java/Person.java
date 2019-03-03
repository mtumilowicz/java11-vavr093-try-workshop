import java.util.function.Predicate;

/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Person {
    final int age;

    Person(int age) {
        this.age = age;
    }

    static Predicate<Person> isAdult() {
        return person -> person.age >= 18;
    }
}

class NotAnAdultException extends Exception {

}
