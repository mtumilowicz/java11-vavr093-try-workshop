import io.vavr.control.Try
import spock.lang.Specification

import java.util.function.BinaryOperator
import java.util.function.Function 
/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Answers extends Specification {

    def "wrap div (4 / 2) with try and verify success and output"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b}

        when:
        def tried = Try.of({-> div.apply(4, 2)}) // wrap here

        then:
        tried.success
        tried.get() == 2
    }

    def "wrap div (4 / 0) with try and verify failure and cause"() {
        given:
        BinaryOperator<Integer> div = {a, b -> a / b}

        when:
        def tried = Try.of({-> div.apply(4, 0)})

        then:
        tried.failure
        tried.cause.class == ArithmeticException
        tried.cause.message == "Division by zero"
    }

    def "wrap parseInt with try, and invoke in on 1 and a, then verify success and failure"() {
        given:
        Function<String, Integer> parse = { i -> Integer.parseInt(i)}

        when:
        def parsed = Try.of({ -> parse.apply("1")})
        def notParsed = Try.of({ -> parse.apply("a")})

        then:
        parsed.success
        parsed.get() == 1
        notParsed.failure
        notParsed.cause.class == NumberFormatException
        notParsed.cause.message == 'For input string: "a"'
    }

}