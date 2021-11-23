package no.fint.provider.events.status

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.event.model.Event
import no.fint.event.model.HeaderConstants
import no.fint.provider.events.exceptions.UnknownEventException
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.test.web.servlet.MockMvc

class StatusControllerSpec extends MockMvcSpecification {
    private StatusController controller
    private StatusService statusService
    private MockMvc mockMvc

    private Event event
    private String eventJson

    void setup() {
        event = new Event()
        eventJson = new ObjectMapper().writeValueAsString(event)

        statusService = Mock(StatusService)
        controller = new StatusController(statusService: statusService)
        mockMvc = standaloneSetup(controller)
    }

    def "POST status"() {
        when:
        def response = mockMvc.perform(
                post('/status')
                        .header(HeaderConstants.ORG_ID, 'rogfk.no')
                        .header(HeaderConstants.CLIENT, 'spock')
                        .content(eventJson)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))

        then:
        1 * statusService.updateEventState(_ as Event)
        response.andExpect(status().isOk())
    }

    def "Return http status bad request when invalid event from adapter"() {
        when:
        def response = mockMvc.perform(
                post('/status')
                        .header(HeaderConstants.ORG_ID, 'rogfk.no')
                        .header(HeaderConstants.CLIENT, 'spock')
                        .content(eventJson)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))

        then:
        1 * statusService.updateEventState(_ as Event) >> { throw new HttpMessageConversionException('test exception') }
        response.andExpect(status().isBadRequest())
    }

    def "Return http status gone when unknown event from adapter"() {
        when:
        def response = mockMvc.perform(
                post('/status')
                        .header(HeaderConstants.ORG_ID, 'rogfk.no')
                        .header(HeaderConstants.CLIENT, 'spock')
                        .content(eventJson)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))

        then:
        1 * statusService.updateEventState(_ as Event) >> { throw new UnknownEventException() }
        response.andExpect(status().isGone())
    }
}
