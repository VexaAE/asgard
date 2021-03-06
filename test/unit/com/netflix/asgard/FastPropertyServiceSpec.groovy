package com.netflix.asgard

import spock.lang.Specification

class FastPropertyServiceSpec extends Specification {

    final String baseUrl = 'http://platformservice.us-east-1.company.net:7001/platformservice/REST/v1/props/property/'
    final String encodedInvalidId = 'test%20invalid%20%2B!%25/,%5B%5D:%5C%5E\$%7C*()'

    UserContext userContext = new UserContext(region: Region.US_EAST_1, internalAutomation: true)
    ConfigService configService = Mock(ConfigService) {
        getRegionalPlatformServiceServer(_) >> 'platformservice.us-east-1.company.net'
        getPlatformServicePort() >> '7001'
        isOnline() >> true
    }
    RestClientService restClientService = Mock(RestClientService)
    Caches caches = new Caches(new MockCachedMapBuilder([
            (EntityType.fastProperty): Mock(CachedMap),
    ]))
    TaskService taskService = new TaskService() {
        def runTask(UserContext userContext, String name, Closure work, Link link = null, Task existingTask = null) {
            work(new Task())
        }
    }
    FastPropertyService service = new FastPropertyService(configService: configService,
            restClientService: restClientService, caches: caches, taskService: taskService)

    def 'should get Fast Property'() {
        when:
        service.get(userContext, 'test invalid +!%/,[]:\\^$|*()')

        then:
        1 * restClientService.getAsXml("${baseUrl}getPropertyById?id=${encodedInvalidId}")
    }

    def 'should delete Fast Property'() {
        restClientService.getAsXml(_) >>> [['key': 'test'], null]

        when:
        service.deleteFastProperty(userContext, 'test invalid +!%/,[]:\\^$|*()', 'cmccoy', 'us-west-1')

        then:
        1 * restClientService.getAsXml("${baseUrl}removePropertyById?id=${encodedInvalidId}\
&source=asgard&updatedBy=cmccoy&cmcTicket=")
    }
}
