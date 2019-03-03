import io.vavr.control.Try
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate

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
        def tried = Try.of({ -1 }) // wrap here

        then:
        false // verify success here
        false // verify output here
    }

    def "wrap div (4 / 0) with try and verify failure and cause"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        def tried = Try.of({ -1 }) // wrap here

        then:
        false // verify failure here
        false // verify failure class here
        false // verify failure message here
    }

    def "wrap parseInt with try, and invoke in on 1 and a, then verify success and failure"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }

        when:
        def parsed = Try.of({ -1 })
        def notParsed = Try.of({ -1 })

        then:
        false // verify success here
        false // verify output here
        false // verify failure here
        false // verify failure class here
        false // verify failure message here
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
        def sum = Try.of({ -1 }) // sum parsed1,...,parsed4
        def withFailure = Try.of({ -1 }) // sum parsed1,...,parsed4,failure

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
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def parsed = Try.of({ parse.apply("2") })
        def notParsed = Try.of({ parse.apply("a") })
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

    def "map value with a partial function; if not defined -> empty"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def zero = Try.of({ parse.apply("0") })
        def two = Try.of({ parse.apply("2") })

        when:
        def dived = zero // map here using new Functions().div()
        def summed = two // map here using new Functions().add()

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
        def filteredThree = three // filter here using moreThanTwo
        def filteredTwo = two // filter here using moreThanTwo

        then:
        filteredThree.success
        filteredThree.get() == 3
        filteredTwo.failure
        filteredTwo.cause.class == NoSuchElementException
        filteredTwo.cause.message == "Predicate does not hold for 2"
    }

    def "if person.isAdult do nothing, otherwise failure with customized error - NotAnAdultException"() {
        given:
        def adult = Try.of({ new Person(20) })
        def kid = Try.of({ new Person(10) })

        when:
        def filteredAdult = adult  // filter here using NotAnAdultException
        def filteredKid = kid  // filter here using NotAnAdultException

        then:
        filteredAdult.success
        filteredKid.failure
        filteredKid.cause.class == NotAnAdultException
        !filteredKid.cause.message
    }

    def "on failure increment failure counter, on success increment success counter"() {
        given:
        def failureCounter = new AtomicInteger()
        def successCounter = new AtomicInteger()
        def existingId = 1
        def databaseConnectionProblem = 2

        when:
        Repository.findById(existingId) // handle side effect here
        Repository.findById(databaseConnectionProblem) // handle side effect here

        then:
        successCounter.get() == 1
        failureCounter.get() == 1
    }

    def "find by id, otherwise try to find by name, otherwise failure"() {
        given:
        def realId = 1
        def realName = "Michal"
        and:
        def fakeId = 2
        def fakeName = "not-found"

        when:
        def foundById = Repository.findById(realId) // handle case here
        def foundByName = Repository.findById(fakeId) // handle case here
        def notFound = Repository.findById(fakeId) // handle case here

        then:
        Try.success("found-by-id") == foundById
        Try.success("found-by-name") == foundByName
        notFound.cause.class == UserCannotBeFound
    }
}