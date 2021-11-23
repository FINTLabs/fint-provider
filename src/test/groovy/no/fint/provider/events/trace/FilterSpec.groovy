package no.fint.provider.events.trace

import spock.lang.Specification

class FilterSpec extends Specification {
    def "Filter matches"() {
        given:
        def filter = new Filter()
        filter.add('viken.no')
        filter.add('fintlabs.no')
        filter.add('innlandetfylke.no')

        when:
        def applies = filter.applies('fintlabs.no')
        def contains = filter.contains('fintlabs.no')

        then:
        applies && contains
    }

    def "Filter doesnt match"() {
        given:
        def filter = new Filter()
        filter.add('viken.no')
        filter.add('fintlabs.no')
        filter.add('innlandetfylke.no')

        when:
        def applies = filter.applies('oslo.kommune.no')
        def contains = filter.contains('oslo.kommune.no')

        then:
        !applies && !contains
    }

    def 'Emtpy filter matches none'() {
        given:
        def filter = new Filter()

        when:
        def applies = filter.applies('fintlabs.no')
        def contains = filter.contains('fintlabs.no')

        then:
        !applies && !contains
    }
}
