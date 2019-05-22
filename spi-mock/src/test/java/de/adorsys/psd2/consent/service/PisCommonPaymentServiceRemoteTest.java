/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.config.PisCommonPaymentRemoteUrls;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentServiceRemoteTest {

    private static final String AUTHORISATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String URL = "http://base.url";

    @InjectMocks
    private PisCommonPaymentServiceRemote service;

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private PisCommonPaymentRemoteUrls remotePisCommonPaymentUrls;

    @Test
    public void getAuthorisationScaApproach_created() {
        when(remotePisCommonPaymentUrls.getAuthorisationScaApproach()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(URL, AuthorisationScaApproachResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)));

        service.getAuthorisationScaApproach(AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        verify(remotePisCommonPaymentUrls, times(1)).getAuthorisationScaApproach();
        verify(consentRestTemplate).getForEntity(eq(URL), eq(AuthorisationScaApproachResponse.class), eq(AUTHORISATION_ID));
    }

    @Test
    public void getAuthorisationScaApproach_cancelled() {
        when(remotePisCommonPaymentUrls.getCancellationAuthorisationScaApproach()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(URL, AuthorisationScaApproachResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)));

        service.getAuthorisationScaApproach(AUTHORISATION_ID, CmsAuthorisationType.CANCELLED);

        verify(remotePisCommonPaymentUrls, times(1)).getCancellationAuthorisationScaApproach();
        verify(consentRestTemplate).getForEntity(eq(URL), eq(AuthorisationScaApproachResponse.class), eq(AUTHORISATION_ID));
    }
}
