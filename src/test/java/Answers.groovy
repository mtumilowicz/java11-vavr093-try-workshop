import io.vavr.collection.List
import io.vavr.control.Try
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate

/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Answers extends Specification {

    def "create successful try with value 1"() {
        given:
        def successful = Try.success(1)

        expect:
        successful.success
        successful.get() == 1
    }

    def "create failure try with cause IllegalStateException with message: 'wrong status' "() {
        given:
        def successful = Try.failure(new IllegalStateException("wrong status"))

        expect:
        successful.failure
        successful.cause.class == IllegalStateException
        successful.cause.message == "wrong status"
    }

    def "wrap div (4 / 2) with try and verify success and output"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        def tried = Try.of({ div.apply(4, 2) }) // wrap here

        then:
        tried.success
        tried.get() == 2
    }

    def "wrap div (4 / 0) with try and verify failure and cause"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        def tried = Try.of({ div.apply(4, 0) })

        then:
        tried.failure
        tried.cause.class == ArithmeticException
        tried.cause.message == "Division by zero"
    }

    def "wrap parseInt with try, and invoke in on 1 and a, then verify success and failure"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }

        when:
        def parsed = Try.of({ parse.apply("1") })
        def notParsed = Try.of({ parse.apply("a") })

        then:
        parsed.success
        parsed.get() == 1
        notParsed.failure
        notParsed.cause.class == NumberFormatException
        notParsed.cause.message == 'For input string: "a"'
    }

    def "sum all values of try sequence or return the first failure"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def parsed1 = Try.of({ parse.apply("1") })
        def parsed2 = Try.of({ parse.apply("2") })
        def parsed3 = Try.of({ parse.apply("3") })
        def parsed4 = Try.of({ parse.apply("4") })
        def failure = Try.of({ parse.apply("a") })

        when:
        def sum = Try.sequence(List.of(parsed1, parsed2, parsed3, parsed4))
                .map({ it.sum() })
        def withFailure = Try.sequence(List.of(parsed1, parsed2, parsed3, parsed4, failure))
                .map({ it.sum() })

        then:
        sum.get() == 10
        withFailure.failure
        withFailure.cause.class == NumberFormatException
        withFailure.cause.message == 'For input string: "a"'
    }

    def "square parsed number, or do nothing"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def parsed = Try.of({ parse.apply("2") })
        def notParsed = Try.of({ parse.apply("a") })

        when:
        def squared = parsed.map({ it * it })
        def fail = notParsed.map({ it * it })

        then:
        squared.success
        squared.get() == 4
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }

    def "if success increment counter, otherwise do nothing"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def parsed = Try.of({ parse.apply("2") })
        def notParsed = Try.of({ parse.apply("a") })
        def successCounter = new AtomicInteger()

        when:
        def squared = parsed.andThen({ successCounter.incrementAndGet() })
        def fail = notParsed.andThen({ successCounter.incrementAndGet() })

        then:
        squared.success
        squared.get() == 2
        successCounter.get() == 1
        and:
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }

    def "map value with a partial function; if not defined -> empty"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def zero = Try.of({ parse.apply("0") })
        def two = Try.of({ parse.apply("2") })

        when:
        def dived = zero.collect(new Functions().div())
        def summed = two.collect(new Functions().add())

        then:
        summed.success
        summed.get() == 7
        dived.failure
        dived.cause.class == NoSuchElementException
        dived.cause.message == "Predicate does not hold for 0"
    }

    def "if value > 2 do nothing, otherwise failure"() {
        given:
        Predicate<Integer> moreThanTwo = { it > 2 }
        def three = Try.of({ 3 })
        def two = Try.of({ 2 })

        when:
        def filteredThree = three.filter(moreThanTwo)
        def filteredTwo = two.filter(moreThanTwo)
        
        then:
        filteredThree.success
        filteredThree.get() == 3
        filteredTwo.failure
        filteredTwo.cause.class == NoSuchElementException
        filteredTwo.cause.message == "Predicate does not hold for 2"
    }
}