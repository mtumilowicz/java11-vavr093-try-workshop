import io.vavr.Function1
import io.vavr.PartialFunction
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import java.nio.file.NoSuchFileException
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate

/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Workshop extends Specification {

    def "create successful Try with value 1, verify success and value"() {
        given:
        Try<Integer> success = false // create here, hint: success

        expect:
        false // verify success here, hint: isSuccess
        false // verify value here, hint: get
    }

    def "create failed Try with cause IllegalStateException with message: 'wrong status', verify failure, cause and message"() {
        given:
        Try<Integer> fail = false // create here, hint: failure

        expect:
        false // verify failure here, hint: isFailure
        false // verify cause class here, hint: getCause, class
        false // verify cause message here, hint: getCause, getMessage
    }

    def "conversion: option -> try"() {
        given:
        Option<Integer> emptyOption = Option.none()
        Option<Integer> notEmptyOption = Option.some(1)

        when:
        Try<Integer> failTry = emptyOption // convert here, hint: toTry()
        Try<Integer> successTry = notEmptyOption // convert here, hint: toTry()

        then:
        failTry.failure
        failTry.cause.class == NoSuchElementException

        and:
        successTry.success
        successTry.get() == 1
    }

    def "convert Try to Option"() {
        given:
        Try<Integer> success = Try.of({ 1 })
        Try<Integer> failure = Try.failure(new IllegalStateException())

        when:
        Option<Integer> successOption = success // convert here, hint: toOption()
        Option<Integer> failureOption = failure // convert here, hint: toOption()

        then:
        successOption == Option.some(1)
        failureOption.empty
    }

    def "wrap div (4 / 2) with try and verify success and value"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        Try<Integer> success = Try.of({ -1 }) // wrap here

        then:
        false // verify success here, hint: isSuccess
        false // verify value here, hint: get
    }

    def "wrap div (4 / 0) with try and verify failure and cause"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b }

        when:
        Try<Integer> fail = Try.of({ -1 }) // wrap here

        then:
        false // verify failure here, hint: isFailure()
        false // verify failure class here, hint: getCause(), .class
    }

    def "wrap parseInt with try, and invoke it on 1 and 'a', then verify success and failure"() {
        given:
        Function<String, Integer> parseInt = { Integer.parseInt(it) }

        when:
        Try<Integer> parsed = Try.of({ -1 }) // wrap here, hint: parse.apply('1')
        Try<Integer> notParsed = Try.of({ -1 }) // wrap here, hint: parse.apply('a')

        then:
        false // verify success here
        false // verify failure here, hint: isFailure()
        false // verify failure class here, hint: getCause(), .class
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
        Try<Integer> sum = Try.of({ -1 }) // sum here parsed1,...,parsed4; hint: sequence, map
        Try<Integer> fail = Try.of({ -1 }) // sum here parsed1,...,parsed4, failure, hint: sequence, map

        then:
        sum == Try.of({ 10 })
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }

    def "parse number, if success - square it, otherwise do nothing"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def number = '2'
        def letter = 'a'
        Try<Integer> parsed = Try.of({ -1 }) // try to parse number here
        Try<Integer> notParsed = Try.of({ -1 }) // try to parse letter here

        when:
        Try<Integer> squared = parsed // square here, hint: map
        Try<Integer> fail = notParsed // square here, hint: map

        then:
        squared.success
        squared.get() == 4
        and:
        fail.failure
        fail.cause.class == NumberFormatException
        fail.cause.message == 'For input string: "a"'
    }

    def "try to parse a number, if success - increment counter, otherwise do nothing"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def number = '2'
        def letter = 'a'
        def successCounter = 0

        when:
        Try<Integer> squared = parsed // try to parse number and increment here, hint: andThen
        Try<Integer> fail = notParsed // try to parse letter and increment here, hint: andThen

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
        Try<Integer> dived = zero // map here using div, hint: collect
        Try<Integer> summed = two // map here using add, hint: collect

        then:
        summed.success
        summed.get() == 7
        and:
        dived.failure
        dived.cause.class == NoSuchElementException
        dived.cause.message == 'Predicate does not hold for 0'
    }

    def "if value > 2 do nothing, otherwise failure with NoSuchElementException"() {
        given:
        Predicate<Integer> moreThanTwo = { it > 2 }
        Try<Integer> three = Try.of({ 3 })
        Try<Integer> two = Try.of({ 2 })

        when:
        Try<Integer> filteredThree = three // filter here using moreThanTwo, hint: filter
        Try<Integer> filteredTwo = two // filter here using moreThanTwo, hint: filter

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
        def adult = Person.builder().age(20).build()
        def kid = Person.builder().age(10).build()
        Try<Person> adultTry = Try.of({ adult })
        Try<Person> kidTry = Try.of({ kid })

        when:
        Try<Person> filteredAdult = adultTry  // filter here using NotAnAdultException, hint: filter
        Try<Person> filteredKid = kidTry  // filter here using NotAnAdultException, hint: filter

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
        DatabaseRepository.findById(existingId) // handle side effect here, hint: onSuccess
        DatabaseRepository.findById(databaseConnectionProblem) // handle side effect here, hint: onFailure

        then:
        successCounter == 1
        failureCounter == 1
    }

    def "try to find in cache, then try to find in database"() {
        given:
        def fromDatabaseId = 1
        def fromCacheId = 2
        def backupConnectionProblemId = 3

        when:
        Try<String> fromDatabase = RepositoryFacade.findById(fromDatabaseId)
        Try<String> fromCache = RepositoryFacade.findById(fromCacheId)
        Try<String> backupConnectionProblem = RepositoryFacade.findById(backupConnectionProblemId)

        then:
        fromDatabase.success
        fromDatabase.get() == 'from database'
        and:
        fromCache.success
        fromCache.get() == 'from cache'
        and:
        backupConnectionProblem.failure
        backupConnectionProblem.cause.class == CacheUserCannotBeFound
    }

    def "if database connection error, recover with default response"() {
        given:
        def defaultResponse = 'default response'
        def databaseConnectionError = 2
        def realId = 1

        when:
        Try<String> byIdSuccess = DatabaseRepository.findById(realId) // recover here with defaultResponse, hint: recover
        Try<String> byIdRecovered = DatabaseRepository.findById(databaseConnectionError) // recover here with defaultResponse, hint: recover

        then:
        byIdSuccess.success
        byIdSuccess.get() == 'found-by-id'
        and:
        byIdRecovered.success
        byIdRecovered.get() == defaultResponse
    }

    def "vavr try with resources: success"() {
        when:
        Try<String> concat = TWR.usingVavr('src/test/resources/lines.txt')

        then:
        concat.success
        concat.get() == '1,2,3'
    }

    def "vavr try with resources: failure - file does not exists"() {
        when:
        Try<String> concat = TWR.usingVavr('NonExistingFile.txt')

        then:
        concat.failure
        concat.cause.class == NoSuchFileException
        concat.cause.message == 'NonExistingFile.txt'
    }
}