package no.fint.provider.events.sse

import no.fint.event.model.HeaderConstants
import no.fint.events.FintEvents
import no.fint.provider.events.ProviderProps
import no.fint.provider.events.admin.AdminService
import no.fint.provider.events.subscriber.DownstreamSubscriber
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

class SseControllerSpec extends MockMvcSpecification {
    private SseController controller
    private DownstreamSubscriber downstreamSubscriber
    private SseService sseService
    private FintEvents fintEvents
    private MockMvc mockMvc
    private AdminService adminService
    private ProviderProps props

    void setup() {
        sseService = Mock()
        fintEvents = Mock()
        adminService = Mock()
        props = Mock()
        downstreamSubscriber = Mock(DownstreamSubscriber)
        controller = new SseController(sseService: sseService, fintEvents: fintEvents, downstreamSubscriber: downstreamSubscriber, adminService: adminService, props: props)
        mockMvc = standaloneSetup(controller)
    }

    def "Subscribe to sse client"() {
        when:
        def response = mockMvc.perform(get('/sse/123')
                .header('x-allowed-asset-ids', 'rogfk.no,bar.rogfk.no')
                .header(HeaderConstants.ORG_ID, 'rogfk.no')
                .header(HeaderConstants.CLIENT, 'client'))

        then:
        1 * adminService.register('rogfk.no', 'client') >> true
        1 * sseService.subscribe('123', 'rogfk.no', 'client')
        1 * fintEvents.registerDownstreamListener('rogfk.no', downstreamSubscriber)
        response.andExpect(status().isOk())
    }

    def "Subscribe to sse client with invalid org should fail"() {
        when:
        def response = mockMvc.perform(get('/sse/123')
                .header('x-allowed-asset-ids', 'rogfk.no,bar.rogfk.no')
                .header(HeaderConstants.ORG_ID, 'rogfk.no')
                .header(HeaderConstants.CLIENT, 'client'))

        then:
        1 * adminService.register('rogfk.no', 'client') >> false
        0 * sseService.subscribe('123', 'rogfk.no', 'client')
        0 * fintEvents.registerDownstreamListener('rogfk.no', downstreamSubscriber)
        response.andExpect(status().is4xxClientError())

    }

    def "Subscribe to sse client with incorrect org should fail"() {
        when:
        def response = mockMvc.perform(get('/sse/123')
                .header('x-allowed-asset-ids', 'test.org')
                .header(HeaderConstants.ORG_ID, 'rogfk.no')
                .header(HeaderConstants.CLIENT, 'client'))

        then:
        0 * adminService.register('rogfk.no', 'client')
        0 * sseService.subscribe('123', 'rogfk.no', 'client')
        0 * fintEvents.registerDownstreamListener('rogfk.no', downstreamSubscriber)
        response.andExpect(status().is4xxClientError())

    }

    def "Get registered sse clients"() {
        given:
        def emitter = new FintSseEmitter('123', 'client', 1000)
        def emitters = new FintSseEmitters(1, null)
        emitters.add(emitter)

        when:
        def response = mockMvc.perform(get('/sse/clients'))

        then:
        1 * sseService.getSseClients() >> ['rogfk.no': emitters]
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$[0].orgId').value(equalTo('rogfk.no')))
                .andExpect(jsonPath('$[0].clients[0].id').value(equalTo('123')))
    }

    def "Delete registered sse clients"() {
        when:
        def response = mockMvc.perform(delete('/sse/clients'))

        then:
        1 * sseService.removeAll()
        response.andExpect(status().isOk())
    }

    def "Auth init"() {
        when:
        def response = mockMvc.perform(get('/sse/auth-init'))

        then:
        response.andExpect(status().isOk())
    }
}
