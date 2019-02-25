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

package de.adorsys.psd2.xs2a.web.controller;

import com.google.gson.Gson;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.model.InlineResponse200;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.service.AccountReferenceValidationService;
import de.adorsys.psd2.xs2a.service.FundsConfirmationService;
import de.adorsys.psd2.xs2a.service.mapper.FundsConfirmationModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FundsConfirmationControllerTest {
    private static final UUID REQUEST_ID = UUID.fromString("ddd36e05-d67a-4830-93ad-9462f71ae1e6");
    private final String FUNDS_REQ_DATA = "/json/ConfirmationOfFundsTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @InjectMocks
    private FundsConfirmationController fundsConfirmationController;
    @Mock
    private FundsConfirmationService fundsConfirmationService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private FundsConfirmationModelMapper fundsConfirmationModelMapper;
    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Before
    public void setUp() {
        when(fundsConfirmationService.fundsConfirmation(any())).thenReturn(readResponseObject());
        when(referenceValidationService.validateAccountReferences(any())).thenReturn(ResponseObject.builder().build());
    }

    @Test
    public void fundConfirmation() throws IOException {
        when(responseMapper.ok(any(), any())).thenReturn(getInlineResponse());

        //Given
        ConfirmationOfFunds confirmationOfFunds = getConfirmationOfFunds();
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<?> actualResult = fundsConfirmationController.checkAvailabilityOfFunds(confirmationOfFunds, null, null, null, null);
        InlineResponse200 fundsConfirmationResponse = (InlineResponse200) actualResult.getBody();

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(fundsConfirmationResponse.isFundsAvailable()).isEqualTo(true);
    }

    private ResponseObject<FundsConfirmationResponse> readResponseObject() {
        return ResponseObject.<FundsConfirmationResponse>builder()
            .body(new FundsConfirmationResponse(true)).build();
    }

    private ResponseEntity getInlineResponse() {
        return new ResponseEntity<>(new InlineResponse200().fundsAvailable(true), HttpStatus.OK);
    }
    
    private ConfirmationOfFunds getConfirmationOfFunds() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(FUNDS_REQ_DATA, UTF_8), ConfirmationOfFunds.class);
    }
}
