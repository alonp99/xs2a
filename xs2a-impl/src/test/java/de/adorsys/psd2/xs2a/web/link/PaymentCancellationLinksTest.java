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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentCancellationLinksTest {

    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";
    private static final String CANCEL_AUTH_LINK = "http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations";
    private static final String SELF_LINK = "http://url/v1/payments/sepa-credit-transfers/1111111111111";
    private static final String STATUS_LINK = "http://url/v1/payments/sepa-credit-transfers/1111111111111/status";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final PsuIdData PSU_DATA_EMPTY = new PsuIdData(null, null, null, null);
    private PaymentCancellationLinks links;
    private Links expectedLinks;
    private CancelPaymentResponse response;

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

    @Before
    public void setUp() {
        expectedLinks = new Links();

        response = new CancelPaymentResponse();
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setPaymentId(PAYMENT_ID);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(PaymentType.SINGLE);
        response.setPsuData(PSU_DATA);
        response.setTransactionStatus(TransactionStatus.ACCP);
    }

    @Test
    public void buildCancellationLinks_redirect_implicit() {
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(redirectLinkBuilder.buildPaymentScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID))).thenReturn(REDIRECT_LINK);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setScaRedirect(REDIRECT_LINK);
        expectedLinks.setScaStatus(CANCEL_AUTH_LINK + "/" + AUTHORISATION_ID);

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_redirect_explicit() {
        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisation(CANCEL_AUTH_LINK);

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_embedded_implicit() {
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);
        String authLink = CANCEL_AUTH_LINK + "/" + AUTHORISATION_ID;

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setUpdatePsuAuthentication(authLink);
        expectedLinks.setScaStatus(authLink);

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_embedded_explicit() {
        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisationWithPsuAuthentication(CANCEL_AUTH_LINK);

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_decoupled_implicit() {
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);
        String authLink = CANCEL_AUTH_LINK + "/" + AUTHORISATION_ID;

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setUpdatePsuAuthentication(authLink);
        expectedLinks.setScaStatus(authLink);

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_decoupled_explicit() {
        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisationWithPsuAuthentication(CANCEL_AUTH_LINK);

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_scaOAuth_implicit() {
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.OAUTH);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setScaOAuth("scaOAuth");

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_scaOAuth_explicit() {
        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.OAUTH);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setScaOAuth("scaOAuth");

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_embedded_implicit_psuEmpty() {
        response.setPsuData(PSU_DATA_EMPTY);

        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);
        String authLink = CANCEL_AUTH_LINK + "/" + AUTHORISATION_ID;

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setUpdatePsuIdentification(authLink);
        expectedLinks.setScaStatus(authLink);

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_embedded_explicit_psuEmpty() {
        response.setPsuData(PSU_DATA_EMPTY);

        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisationWithPsuIdentification(CANCEL_AUTH_LINK);

        assertEquals(expectedLinks, links);
    }

    @Test
    public void buildCancellationLinks_status_RJCT() {
        boolean isExplicitMethod = true;
        response.setTransactionStatus(TransactionStatus.RJCT);

        links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, response, isExplicitMethod);

        assertEquals(expectedLinks, links);
    }

    public CancelPaymentResponse getDefaultCancelPaymentResponse() {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setPaymentId(PAYMENT_ID);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(PaymentType.SINGLE);
        return response;
    }
}
