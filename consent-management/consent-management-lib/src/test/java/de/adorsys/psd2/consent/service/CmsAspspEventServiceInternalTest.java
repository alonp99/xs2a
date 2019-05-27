/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.event.EventEntity;
import de.adorsys.psd2.consent.repository.EventRepository;
import de.adorsys.psd2.consent.repository.specification.EventEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.EventMapper;
import de.adorsys.psd2.xs2a.core.event.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspEventServiceInternalTest {
    private static final String DEFAULT_BANK_INSTANCE_ID = "UNDEFINED";

    @InjectMocks
    private CmsAspspEventServiceInternal cmsAspspEventServiceInternal;

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private EventEntitySpecification eventEntitySpecification;

    @Test
    public void getEventsForPeriod_Success() {
        OffsetDateTime start = OffsetDateTime.parse("2018-11-01T00:00:00Z");
        OffsetDateTime between = OffsetDateTime.parse("2018-11-10T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2018-12-01T00:00:00Z");

        when(eventMapper.mapToEventList(any()))
            .thenReturn(Collections.singletonList(buildCmsEvent(between)));

        // Given
        Event expected = buildCmsEvent(between);

        // When
        List<Event> events = cmsAspspEventServiceInternal.getEventsForPeriod(start, end, DEFAULT_BANK_INSTANCE_ID);

        // Then
        assertThat(events.isEmpty()).isFalse();
        assertThat(events.get(0)).isEqualTo(expected);
    }

    @Test
    public void getEventsForPeriod_EmptyResponse() {
        OffsetDateTime start = OffsetDateTime.parse("2018-11-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2018-12-01T00:00:00Z");

        when(eventMapper.mapToEventList(any()))
            .thenReturn(Collections.emptyList());

        // When
        List<Event> events = cmsAspspEventServiceInternal.getEventsForPeriod(start, end, DEFAULT_BANK_INSTANCE_ID);

        // Then
        assertThat(events.isEmpty()).isTrue();
    }

    private Event buildCmsEvent(OffsetDateTime timestamp) {
        return Event.builder()
                   .timestamp(timestamp)
                   .build();
    }

    private EventEntity buildEventEntity(OffsetDateTime timestamp) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTimestamp(timestamp);
        return eventEntity;
    }
}
