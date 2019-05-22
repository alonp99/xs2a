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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage.cancellation;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.xs2a.config.factory.PisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisCancellationScaIdentifiedAuthorisationStageTest {
    private final static String DECOUPLED_SERVICE_NAME = PisScaStageAuthorisationFactory.getCancellationServiceName(ScaApproach.EMBEDDED, ScaStatus.RECEIVED);

    @InjectMocks
    private PisCancellationScaIdentifiedAuthorisationStage pisCancellationScaIdentifiedAuthorisationStage;

    @Mock
    private PisScaStageAuthorisationFactory pisScaStageAuthorisationFactory;

    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataRequest request;
    @Mock
    private GetPisAuthorisationResponse response;
    @Mock
    private PisCancellationScaReceivedAuthorisationStage pisCancellationScaReceivedAuthorisationStage;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResponse;

    @Test
    public void apply_Success() {
        when(pisScaStageAuthorisationFactory.getService(DECOUPLED_SERVICE_NAME))
            .thenReturn(pisCancellationScaReceivedAuthorisationStage);

        when(pisCancellationScaReceivedAuthorisationStage.apply(request, response))
            .thenReturn(expectedResponse);

        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaIdentifiedAuthorisationStage.apply(request, response);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(pisScaStageAuthorisationFactory).getService(DECOUPLED_SERVICE_NAME);
        verify(pisCancellationScaReceivedAuthorisationStage).apply(request, response);
    }

}
