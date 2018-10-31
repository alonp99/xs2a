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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.embedded;


import com.fasterxml.jackson.core.type.TypeReference;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.util.Context;

import de.adorsys.psd2.model.ScaStatusResponse;
import de.adorsys.psd2.model.UpdatePsuAuthentication;
import de.adorsys.psd2.model.UpdatePsuAuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class GetSCAStatusSuccessful {

    @Autowired
    private Context context;

    @Autowired
    private TestService testService;

    //@Given("^PSU sends the single payment initiation request and receives the paymentId$")
    //See Global Successful Steps

    //@And("^PSU sends the start authorisation request and receives the authorisationId$")
    //See Global Successful Steps

    @And("^PSU prepares successful Get Request using (.*)$")
    public void loadGetRequestData(String dataFileName) throws IOException {
        testService.parseJson("/data-input/pis/embedded/" + dataFileName,  new TypeReference<TestData<HashMap, ScaStatusResponse>>() {
        });

    }
    @When("^PSU sends his Get Request$")
    public void sendGetRequest() {
            testService.sendRestCall(HttpMethod.GET, context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/authorisations/" + context.getAuthorisationId());
    }

    @Then("^PSU checks if the correct SCA status is received$")
    public void checkScaStatus () {
        ResponseEntity<ScaStatusResponse> actualResponse = context.getActualResponse();
        ScaStatusResponse givenResponseBody = (ScaStatusResponse) context.getTestData().getResponse().getBody();

       // assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        // assertThat(actualResponse.getBody().getScaStatus(), equalTo(givenResponseBody.getScaStatus()));

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getScaStatus(), equalTo(givenResponseBody.getScaStatus()));
    }



}
