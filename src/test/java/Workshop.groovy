import io.vavr.control.Try
import spock.lang.Specification

import java.util.function.BinaryOperator
import java.util.function.Function


/**
 * Created by mtumilowicz on 2019-03-03.
 */
class Workshop extends Specification {
    
    def "wrap div (4 / 2) with try and verify success and output"() {
        given:
        BinaryOperator<Integer> div = { a, b -> a / b}

        when:
        def tried = Try.of({-> -1}) // wrap here

        then:
        false // verify success here
        false // verify output here
    }

    def "wrap div (4 / 0) with try and verify failure and cause"() {
        given:
        BinaryOperator<Integer> div = {a, b -> a / b}

        when:
        def tried = Try.of({-> -1}) // wrap here

        then:
        false // verify failure here
        false // verify failure class here
        false // verify failure message here
    }

    def "wrap parseInt with try, and invoke in on 1 and a, then verify success and failure"() {
        given:
        Function<String, Integer> parse = { i -> Integer.parseInt(i)}

        when:
        def parsed = Try.of({ -> -1})
        def notParsed = Try.of({ -> -1})

        then:
        false // verify success here
        false // verify output here
        false // verify failure here
        false // verify failure class here
        false // verify failure message here
    }
}