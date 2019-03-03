import io.vavr.control.Try
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BinaryOperator
import java.util.function.Function

/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Workshop extends Specification {

    def "create successful try with value 1"() {
        given:
        def successful = false // create here

        expect:
        successful.success
        successful.get() == 1
    }

    def "create failure try with cause IllegalStateException with message: 'wrong status' "() {
        given:
        def successful = false // create here

        expect:
        successful.failure
        successful.cause.class == IllegalStateException
        successful.cause.message == "wrong status"
    }

    def "wrap div (4 / 2) with try and verify success and output"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        def tried = Try.of({ -> -1 }) // wrap here

        then:
        false // verify success here
        false // verify output here
    }

    def "wrap div (4 / 0) with try and verify failure and cause"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        def tried = Try.of({ -> -1 }) // wrap here

        then:
        false // verify failure here
        false // verify failure class here
        false // verify failure message here
    }

    def "wrap parseInt with try, and invoke in on 1 and a, then verify success and failure"() {
        given:
        Function<String, Integer> parse = { i -> Integer.parseInt(i) }

        when:
        def parsed = Try.of({ -> -1 })
        def notParsed = Try.of({ -> -1 })

        then:
        false // verify success here
        false // verify output here
        false // verify failure here
        false // verify failure class here
        false // verify failure message here
    }

    def "sum all values of try sequence or return the first failure"() {
        given:
        Function<String, Integer> parse = { i -> Integer.parseInt(i) }
        def parsed1 = Try.of({ -> parse.apply("1") })
        def parsed2 = Try.of({ -> parse.apply("2") })
        def parsed3 = Try.of({ -> parse.apply("3") })
        def parsed4 = Try.of({ -> parse.apply("4") })
        def failure = Try.of({ -> parse.apply("a") })

        when:
        def sum = Try.of({ -> -1 }) // sum parsed1,...,parsed4
        def withFailure = Try.of({ -> -1 }) // sum parsed1,...,parsed4,failure

        then:
        sum.get() == 10
        withFailure.failure
        withFailure.cause.class == NumberFormatException
        withFailure.cause.message == 'For input string: "a"'
    }

    def "square parsed number, or do nothing"() {
        given:
        Function<String, Integer> parse = { i -> Integer.parseInt(i) }
        def parsed = Try.of({ -> parse.apply("2") })
        def notParsed = Try.of({ -> parse.apply("a") })

        when:
        def squared = parsed // square
        def fail = notParsed // square

        then:
        squared.success
        squared.get() == 4
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }

    def "if success increment counter, otherwise do nothing"() {
        given:
        Function<String, Integer> parse = { i -> Integer.parseInt(i) }
        def parsed = Try.of({ -> parse.apply("2") })
        def notParsed = Try.of({ -> parse.apply("a") })
        def successCounter = new AtomicInteger()

        when:
        def squared = parsed // increment here
        def fail = notParsed // increment here

        then:
        squared.success
        squared.get() == 2
        successCounter.get() == 1
        and:
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }
}