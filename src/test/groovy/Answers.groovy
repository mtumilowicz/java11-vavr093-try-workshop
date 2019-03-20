import io.vavr.Function1
import io.vavr.PartialFunction
import io.vavr.collection.List
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import java.nio.file.NoSuchFileException
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Answers extends Specification {

    def "create successful Try with value 1, verify success and value"() {
        given:
        Try<Integer> success = Try.success(1)

        expect:
        success.success
        success.get() == 1
    }

    def "create failed Try with cause IllegalStateException with message: 'wrong status', verify failure, cause and message"() {
        given:
        Try<Integer> fail = Try.failure(new IllegalStateException('wrong status'))

        expect:
        fail.failure
        fail.cause.class == IllegalStateException
        fail.cause.message == 'wrong status'
    }

    def "convert Try to Option"() {
        given:
        Try<Integer> success = Try.of({ 1 })
        Try<Integer> fail = Try.failure(new IllegalStateException())

        when:
        Option<Integer> successOption = success.toOption()
        Option<Integer> failOption = fail.toOption()

        then:
        successOption == Option.some(1)
        failOption == Option.none()
    }

    def "wrap div (4 / 2) with try and verify success and output"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        Try<Integer> dived = Try.of({ div.apply(4, 2) }) // wrap here

        then:
        dived.success
        dived.get() == 2
    }

    def "wrap div (4 / 0) with try and verify failure and cause"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        Try<Integer> fail = Try.of({ div.apply(4, 0) })

        then:
        fail.failure
        fail.cause.class == ArithmeticException
        fail.cause.message == 'Division by zero'
    }

    def "wrap parseInt with try, and invoke it on 1 and a, then verify success and failure"() {
        given:
        Function<String, Integer> parseInt = { Integer.parseInt(it) }

        when:
        Try<Integer> parsed = Try.of({ parseInt.apply('1') })
        Try<Integer> notParsed = Try.of({ parseInt.apply('a') })

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
        Try<Integer> parsed1 = Try.of({ parse.apply('1') })
        Try<Integer> parsed2 = Try.of({ parse.apply('2') })
        Try<Integer> parsed3 = Try.of({ parse.apply('3') })
        Try<Integer> parsed4 = Try.of({ parse.apply('4') })
        Try<Integer> failure = Try.of({ parse.apply('a') })

        when:
        Try<Number> sum = Try.sequence(List.of(parsed1, parsed2, parsed3, parsed4))
                .map({ it.sum() })
        Try<Number> fail = Try.sequence(List.of(parsed1, parsed2, parsed3, parsed4, failure))
                .map({ it.sum() })

        then:
        sum.success
        sum.get() == 10
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }

    def "square parsed number, or do nothing"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        Try<Integer> parsed = Try.of({ parse.apply('2') })
        Try<Integer> notParsed = Try.of({ parse.apply('a') })

        when:
        Try<Integer> squared = parsed.map({ it * it })
        Try<Integer> fail = notParsed.map({ it * it })

        then:
        squared.success
        squared.get() == 4
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }

    def "if success - increment counter, otherwise do nothing"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        Try<Integer> parsed = Try.of({ parse.apply('2') })
        Try<Integer> notParsed = Try.of({ parse.apply('a') })
        def successCounter = 0

        when:
        Try<Integer> squared = parsed.andThen({ successCounter++ })
        Try<Integer> fail = notParsed.andThen({ successCounter++ })

        then:
        squared.success
        squared.get() == 2
        successCounter == 1
        and:
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }

    def "map value with a partial function; if not defined -> NoSuchElementException"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        Try<Integer> zero = Try.of({ parse.apply('0') })
        Try<Integer> two = Try.of({ parse.apply('2') })

        and:
        PartialFunction<Integer, Integer> div = Function1.of({ 5 / it })
                .partial({ it != 0 })
        PartialFunction<Integer, Integer> add = Function1.of({ 5 + it })
                .partial({ true })

        when:
        Try<Integer> dived = zero.collect(div)
        Try<Integer> summed = two.collect(add)

        then:
        summed.success
        summed.get() == 7
        and:
        dived.failure
        dived.cause.class == NoSuchElementException
        dived.cause.message == 'Predicate does not hold for 0'
    }

    def "if value > 2 do nothing, otherwise failure"() {
        given:
        Predicate<Integer> moreThanTwo = { it > 2 }
        Try<Integer> three = Try.of({ 3 })
        Try<Integer> two = Try.of({ 2 })

        when:
        Try<Integer> filteredThree = three.filter(moreThanTwo)
        Try<Integer> filteredTwo = two.filter(moreThanTwo)

        then:
        filteredThree.success
        filteredThree.get() == 3
        and:
        filteredTwo.failure
        filteredTwo.cause.class == NoSuchElementException
        filteredTwo.cause.message == 'Predicate does not hold for 2'
    }

    def "if person.isAdult do nothing, otherwise failure with customized error - NotAnAdultException"() {
        given:
        def adult = new Person(20)
        def kid = new Person(10)
        Try<Person> adultTry = Try.of({ adult })
        Try<Person> kidTry = Try.of({ kid })

        when:
        Try<Person> filteredAdult = adultTry.filter(Person.isAdult(), { new NotAnAdultException() } as Supplier)
        Try<Person> filteredKid = kidTry.filter(Person.isAdult(), { new NotAnAdultException() } as Supplier)

        then:
        filteredAdult.success
        filteredAdult.get() == adult
        and:
        filteredKid.failure
        filteredKid.cause.class == NotAnAdultException
    }

    def "on failure increment failure counter, on success increment success counter"() {
        given:
        def failureCounter = 0
        def successCounter = 0
        def existingId = 1
        def databaseConnectionProblem = 2

        when:
        DatabaseRepository.findById(existingId)
                .onSuccess({ successCounter++ })
        DatabaseRepository.findById(databaseConnectionProblem)
                .onFailure({ failureCounter++ })

        then:
        successCounter == 1
        failureCounter == 1
    }

    def "try to find in database then try to find in backup"() {
        given:
        def fromDatabaseId = 1
        def fromCacheId = 2
        def backupConnectionProblemId = 3

        when:
        Try<String> fromDatabase = RepositoryAnswer.findById(fromDatabaseId)
        Try<String> fromCache = RepositoryAnswer.findById(fromCacheId)
        Try<String> backupConnectionProblem = RepositoryAnswer.findById(backupConnectionProblemId)

        then:
        fromDatabase.success
        fromDatabase.get() == 'from database'
        and:
        fromCache.success
        fromCache.get() == 'from cache'
        and:
        backupConnectionProblem.failure
        backupConnectionProblem.cause.class == BackupRepositoryConnectionProblem
    }

    def "if database connection error, recover with default response"() {
        given:
        def defaultResponse = 'default response'
        def databaseConnectionError = 2
        def realId = 1

        when:
        Try<String> byIdSuccess = DatabaseRepository.findById(realId)
                .recover(DatabaseConnectionProblem.class, { defaultResponse } as Function)
        Try<String> byIdRecovered = DatabaseRepository.findById(databaseConnectionError)
                .recover(DatabaseConnectionProblem.class, { defaultResponse } as Function)

        then:
        byIdSuccess.success
        byIdSuccess.get() == 'from database'
        and:
        byIdRecovered.success
        byIdRecovered.get() == defaultResponse
    }

    def "vavr try with resources: success"() {
        when:
        Try<String> concat = TWRAnswer.usingVavr('src/test/resources/lines.txt')

        then:
        concat.success
        concat.get() == '1,2,3'
    }

    def "vavr try with resources: failure - file does not exists"() {
        when:
        Try<String> concat = TWRAnswer.usingVavr('NonExistingFile.txt')

        then:
        concat.failure
        concat.cause.class == NoSuchFileException
        concat.cause.message == 'NonExistingFile.txt'
    }
}