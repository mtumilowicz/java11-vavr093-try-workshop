import io.vavr.Function1
import io.vavr.PartialFunction
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import java.nio.file.NoSuchFileException
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

    def "convert Try to Option"() {
        given:
        def success = Try.of({ 1 })
        def failure = Try.failure(new IllegalStateException())
        
        when:
        def successOption = success // convert here, hint: toOption()
        def failureOption = failure // convert here, hint: toOption()
        
        then:
        successOption.defined
        successOption == Option.some(1)
        failureOption.empty
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

    def "wrap parseInt with try, and invoke it on 1 and a, then verify success and failure"() {
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

    def "map value with a partial function; if not defined -> NoSuchElementException"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def zero = Try.of({ parse.apply("0") })
        def two = Try.of({ parse.apply("2") })

        and:
        PartialFunction<Integer, Integer> div = Function1.of({ 5 / it })
                .partial({ it != 0 })
        PartialFunction<Integer, Integer> add = Function1.of({ 5 + it })
                .partial({ true })

        when:
        def dived = zero // map here using div
        def summed = two // map here using add

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
        DatabaseRepository.findById(existingId) // handle side effect here
        DatabaseRepository.findById(databaseConnectionProblem) // handle side effect here

        then:
        successCounter.get() == 1
        failureCounter.get() == 1
    }

    def "try to find in database then try to find in backup"() {
        given:
        def fromDatabaseId = 1
        def fromCacheId = 2
        def backupConnectionProblemId = 3

        when:
        def fromDatabase = Repository.findById(fromDatabaseId)
        def fromCache = Repository.findById(fromCacheId)
        def backupConnectionProblem = Repository.findById(backupConnectionProblemId)

        then:
        fromDatabase == Try.of({ "from database" })
        fromCache == Try.of({ "from cache" })
        backupConnectionProblem.failure
        backupConnectionProblem.cause.class == BackupRepositoryConnectionProblem
    }

    def "if database connection error, recover with default response"() {
        given:
        def defaultResponse = "default response"
        def databaseConnectionError = 2
        def realId = 1

        when:
        def byIdSuccess = DatabaseRepository.findById(realId) // recover here with defaultResponse
        def byIdRecovered = DatabaseRepository.findById(databaseConnectionError) // recover here with defaultResponse

        then:
        byIdSuccess.success
        byIdSuccess.get() == "found-by-id"
        byIdRecovered.success
        byIdRecovered.get() == defaultResponse
    }

    def "vavr try with resources: success"() {
        when:
        def concat = TWR.usingVavr("src/test/resources/lines.txt")

        then:
        concat.success
        concat.get() == "1,2,3"
    }

    def "vavr try with resources: failure - file does not exists"() {
        when:
        def concat = TWR.usingVavr("NonExistingFile.txt")

        then:
        concat.failure
        concat.cause.class == NoSuchFileException
        concat.cause.message == 'NonExistingFile.txt'
    }
}