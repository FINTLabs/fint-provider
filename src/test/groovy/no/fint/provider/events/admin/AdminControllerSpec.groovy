package no.fint.provider.events.admin


import no.fint.event.model.HeaderConstants
import no.fint.events.FintEvents
import no.fint.provider.events.subscriber.DownstreamSubscriber
import no.fint.test.utils.MockMvcSpecification
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AdminControllerSpec extends MockMvcSpecification {
    private DownstreamSubscriber downstreamSubscriber
    private AdminController controller
    private FintEvents fintEvents
    private AdminService adminService
    private MockMvc mockMvc

    void setup() {
        downstreamSubscriber = Mock(DownstreamSubscriber)
        adminService = Mock(AdminService)
        fintEvents = Mock(FintEvents)
        controller = new AdminController(fintEvents: fintEvents, adminService: adminService, downstreamSubscriber: downstreamSubscriber)
        mockMvc = standaloneSetup(controller)
    }

    def "POST new orgId"() {
        when:
        def response = mockMvc.perform(post('/admin/orgIds/123').header(HeaderConstants.CLIENT, 'spock'))

        then:
        1 * fintEvents.registerDownstreamListener('123', downstreamSubscriber)
        1 * adminService.register('123', 'spock') >> true
        1 * adminService.isRegistered('123') >> false
        response.andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, equalTo('http://localhost/admin/orgIds/123')))
    }

    def "POST new orgId, return no content if orgId is already registered"() {
        when:
        def response = mockMvc.perform(post('/admin/orgIds/123').header(HeaderConstants.CLIENT, 'spock'))

        then:
        1 * adminService.isRegistered('123') >> true
        response.andExpect(status().isNoContent())
    }

    def "GET all registered orgIds"() {
        when:
        def response = mockMvc.perform(get('/admin/orgIds'))

        then:
        1 * adminService.getOrgIds() >> ['123': 123456L, '234': 234567L]
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(2)))
                .andExpect(jsonPath('$[0].orgId').value(equalTo('123')))
                .andExpect(jsonPath('$[0].registered').value(equalTo(123456)))
                .andExpect(jsonPath('$[1].orgId').value(equalTo('234')))
                .andExpect(jsonPath('$[1].registered').value(equalTo(234567)))
    }

    def "GET registered orgId"() {
        when:
        def response = mockMvc.perform(get('/admin/orgIds/123'))

        then:
        1 * adminService.isRegistered('123') >> true
        1 * adminService.getTimestamp('123') >> 123456L
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.orgId').value(equalTo('123')))
                .andExpect(jsonPath('$.registered').value(equalTo(123456)))
    }

    def "GET non existing orgId, return not found"() {
        when:
        def response = mockMvc.perform(get('/admin/orgIds/123'))

        then:
        response.andExpect(status().isNotFound())
    }

    def "Reject registering unknown orgId"() {
        when:
        def response = mockMvc.perform(post('/admin/orgIds/invalid.org').header(HeaderConstants.CLIENT, 'spock'))

        then:
        response.andExpect(status().is4xxClientError())
        1 * adminService.register('invalid.org', 'spock') >> false

        when:
        def response2 = mockMvc.perform(post('/admin/orgIds/valid.org').header(HeaderConstants.CLIENT, 'spock'))

        then:
        response2.andExpect(status().is2xxSuccessful())
        1 * adminService.register('valid.org', 'spock') >> true
    }
}
