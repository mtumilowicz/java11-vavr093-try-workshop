import io.vavr.CheckedConsumer
import io.vavr.Function1
import io.vavr.PartialFunction
import io.vavr.collection.List
import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import java.nio.file.NoSuchFileException
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

import static io.vavr.API.$
import static io.vavr.API.Case
import static io.vavr.Predicates.instanceOf

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

    def "conversion: option -> try"() {
        given:
        Option<Integer> emptyOption = Option.none()
        Option<Integer> notEmptyOption = Option.some(1)

        when:
        Try<Integer> failTry = emptyOption.toTry()
        Try<Integer> successTry = notEmptyOption.toTry()

        then:
        failTry.failure
        failTry.cause.class == NoSuchElementException

        and:
        successTry.success
        successTry.get() == 1
    }

    def "conversion: try -> option"() {
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
    
    def "conversion: try -> either"() {
        given:
        IllegalStateException exception = new IllegalStateException()
        Try<Integer> success = Try.of({ 1 })
        Try<Integer> fail = Try.failure(exception)

        when:
        Either<Throwable, Integer> successEither = success.toEither()
        Either<Throwable, Integer> failEither = fail.toEither()

        then:
        successEither == Either.right(1)
        failEither == Either.left(exception)
    }

    def "wrap div (4 / 2) with try and verify success and value"() {
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
    }

    def "wrap parseInt with try, and invoke it on 1 and 'a', then verify success and failure"() {
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

    def "parse number, if success - square it, otherwise do nothing"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def number = '2'
        def letter = 'a'
        Try<Integer> parsed = Try.of({ parse.apply(number) })
        Try<Integer> notParsed = Try.of({ parse.apply(letter) })

        when:
        Try<Integer> squared = parsed.map({ it * it })
        Try<Integer> fail = notParsed.map({ it * it })

        then:
        squared.success
        squared.get() == 4
        and:
        fail.failure
        fail.cause.class == NumberFormatException
    }

    def "try to parse a number, if success - increment counter, otherwise do nothing"() {
        given:
        Function<String, Integer> parse = { Integer.parseInt(it) }
        def number = '2'
        def letter = 'a'
        def successCounter = 0

        when:
        Try<Integer> squared = Try.of({ parse.apply(number) }).andThen({ successCounter++ })
        Try<Integer> fail = Try.of({ parse.apply(letter) }).andThen({ successCounter++ })

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

    def "if value > 2 do nothing, otherwise failure with NoSuchElementException"() {
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
        def adult = Person.builder().age(20).build()
        def kid = Person.builder().age(10).build()
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

    def "try to find in cache, then try to find in database"() {
        given:
        def fromDatabaseId = 4
        def fromCacheId = 20
        def databaseConnectionId = 2

        when:
        Try<String> fromDatabase = RepositoryAnswer.findById(fromDatabaseId)
        Try<String> fromCache = RepositoryAnswer.findById(fromCacheId)
        Try<String> backupConnectionProblem = RepositoryAnswer.findById(databaseConnectionId)

        then:
        fromDatabase.success
        fromDatabase.get() == 'from database'
        and:
        fromCache.success
        fromCache.get() == 'from cache'
        and:
        backupConnectionProblem.failure
        backupConnectionProblem.cause.class == DatabaseConnectionProblem
    }

    def "if database connection error, recover with default response"() {
        given:
        def defaultResponse = 'default response'
        def databaseConnectionError = 2
        def realId = 1

        when:
        Try<String> byIdSuccess = DatabaseRepository.findById(realId)
                .recover(DatabaseConnectionProblem.class, {
            defaultResponse
        } as Function) // recover with other database
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
    
    def "get person from database, and then try to estimate income"() {
        given:
        Function<Person, Integer> estimateIncome = { 
            switch (it.id) {
                case 1:
                    throw new RuntimeException()
                default:
                    return 30
            }
        }
        and:
        def personWithoutIncome = 1
        def personWithIncome = 2

        when:
        Try<Integer> withIncome = PersonRepository.findById(personWithIncome)
                .map({ estimateIncome.apply(it) })
        Try<Integer> withoutIncome = PersonRepository.findById(personWithoutIncome)
                .map({ estimateIncome.apply(it) })
        
        then:
        withIncome.success
        withoutIncome.failure
    }

    def "get person from database, and then try to estimate income with flatMap"() {
        given:
        Function<Person, Try<Integer>> estimateIncome = {
            switch (it.id) {
                case 1:
                    return Try.failure(new RuntimeException())
                default:
                    return Try.success(20)
            }
        }
        and:
        def personWithoutIncome = 1
        def personWithIncome = 2

        when:
        Try<Integer> withIncome = PersonRepository.findById(personWithIncome)
                .flatMap({ estimateIncome.apply(it) })
        Try<Integer> withoutIncome = PersonRepository.findById(personWithoutIncome)
                .flatMap({ estimateIncome.apply(it) })

        then:
        withIncome.success
        withoutIncome.failure
    }

    def "get person from database, change age and then try to save"() {
        given:
        def canBeSavedId = 1
        def userModifiedId = 2
        def connectionProblemId = 3
        def fakeId = 4

        when:
        Try<Person> tried1 = PersonRepository.findById(canBeSavedId)
                .map({ it.withAge(1) })
                .andThenTry({ PersonRepository.save(it) } as CheckedConsumer)
        and:
        Try<Person> tried2 = PersonRepository.findById(userModifiedId)
                .map({ it.withAge(2) })
                .andThenTry({ PersonRepository.save(it) } as CheckedConsumer)
        and:
        Try<Person> tried3 = PersonRepository.findById(connectionProblemId)
                .map({ it.withAge(3) })
                .andThenTry({ PersonRepository.save(it) } as CheckedConsumer)
        and:
        Try<Person> tried4 = PersonRepository.findById(fakeId)
                .map({ it.withAge(4) })
                .andThenTry({ PersonRepository.save(it) } as CheckedConsumer)

        then:
        tried1.success
        and:
        tried2.failure
        tried2.cause.class == PersonModifiedInMeantimeException
        and:
        tried3.failure
        tried3.cause.class == DatabaseConnectionProblem
        and:
        tried4.failure
        tried4.cause.class == EntityNotFoundException
    }

    def "if entity cannot be found in cache try to find in database; if cache is under synchronization with database - return default response"() {
        given:
        def userFromCache = 1
        def databaseConnection = 2
        def userFromDatabase = 4
        def cacheSynchronization = 5

        when:
        Function<Integer, Try<String>> findById = {
            CacheRepository.findById(it)
                    .recover(CacheSynchronization.class, "cache synchronization with database, try again later")
                    .recoverWith(CacheUserCannotBeFound.class, { DatabaseRepository.findById(it.getUserId()) })
                    .recover(DatabaseConnectionProblem.class, "cannot connect to database")
        }

        then:
        findById.apply(userFromCache).get() == 'from cache'
        findById.apply(userFromDatabase).get() == 'from database'
        findById.apply(cacheSynchronization).get() == 'cache synchronization with database, try again later'
        findById.apply(databaseConnection).get() == 'cannot connect to database'
    }

    def "map exceptions"() {
        given:

        expect:
        Try.of({ Integer.parseInt("a") }).mapFailure(
                Case($(instanceOf(NumberFormatException.class)), { new CannotParseInteger(it.message) } as Function)
        ).cause.class == CannotParseInteger // transform to function
    }
    
    class CannotParseInteger extends RuntimeException {
        CannotParseInteger(String message) {
            super(message)
        }
    }
}