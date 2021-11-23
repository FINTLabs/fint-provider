# FINT provider

[![Build Status](https://jenkins.fintlabs.no/buildStatus/icon?job=FINTLabs/fint-provider/master)](https://jenkins.fintlabs.no/job/FINTLabs/fint-provider/master)
[![Coverage Status](https://coveralls.io/repos/github/FINTLabs/fint-provider/badge.svg?branch=master)](https://coveralls.io/github/FINTLabs/fint-provider?branch=master)
[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg)](http://opensource.org/licenses/MIT)

## Configuration

| Key | Description | Default value |
|-----|---------------|-------------|
| fint.provider.assets.endpoint | Admin endpoint for retrieving active asset IDs. | _none_ |
| fint.provider.ttl-status | How long (in minutes) the provider will wait for an initial status from the adapter (that the event was received and understood) | 2 |
| fint.provider.ttl-response |  How long (in minutes) the provider will wait for a response with the data from the adapter | 15 |
| fint.provider.max-number-of-emitters | The max number of emitters one orgId can have | 50 |
| fint.provider.event-state.hazelcast | Use Hazelcast Map for EventState | true |
| fint.provider.event-state.list-name | The name of the list used to store event states stored in redisson | current-corrids |
| fint.provider.swagger-https | Force https in the swagger api-docs request | true (disabled in the test profile) |
| fint.provider.test-mode | Runs the provider in test-mode, enabling a test consumer and adapter. | false |
| fint.provider.sse.heartbeat.enabled | Send (empty) heartbeat SSE events at a configured interval. | false |
| fint.provider.sse.heartbeat.interval | Heartbeat interval (milliseconds) | 15000 |

## Log payload

To log the event payload add the configuration to `application.properties` or as an environment variable.

```
logging.level.no.fint.provider.events.status.StatusController: TRACE
logging.level.no.fint.provider.events.response.ResponseController: TRACE
```

## Integration test

To enable integration test enable the `integration` spring profile.



