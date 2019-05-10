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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.ConsentApi;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.ConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.header.ConsentHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked") // This class implements autogenerated interface without proper return values generated
@Slf4j
@RestController
@AllArgsConstructor
@Api(value = "v1", description = "Provides access to the account information", tags = {"Account Information Service (AIS)"})
public class ConsentController implements ConsentApi {
    private final ConsentService consentService;
    private final ResponseMapper responseMapper;
    private final ConsentModelMapper consentModelMapper;
    private final AuthorisationMapper authorisationMapper;
    private final TppRedirectUriMapper tppRedirectUriMapper;
    private final ResponseErrorMapper responseErrorMapper;
    private final ConsentHeadersBuilder consentHeadersBuilder;

    @Override
    public ResponseEntity createConsent(UUID xRequestID, Consents body, String digest, String signature,
                                        byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID,
                                        String psUCorporateIDType, Boolean tpPRedirectPreferred, String tpPRedirectURI,
                                        String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred,
                                        String tpPNotificationURI, String tpPNotificationContentPreferred, String psUIPAddress,
                                        String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                        String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID,
                                        String psUGeoLocation) {

        CreateConsentReq createConsent = consentModelMapper.mapToCreateConsentReq(body);

        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        TppRedirectUri tppRedirectUri = tppRedirectUriMapper.mapToTppRedirectUri(tpPRedirectURI, tpPNokRedirectURI);
        ResponseObject<CreateConsentResponse> createResponse =
            consentService.createAccountConsentsWithResponse(createConsent, psuData, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred), tppRedirectUri);

        if (createResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(createResponse.getError(),
                                                             consentHeadersBuilder.buildErrorCreateConsentHeaders());
        }

        CreateConsentResponse createConsentResponse = createResponse.getBody();
        ResponseHeaders headers = consentHeadersBuilder.buildCreateConsentHeaders(createConsentResponse.getAuthorizationId(),
                                                                                  createConsentResponse.getLinks().getSelf());

        return responseMapper.created(createResponse, consentModelMapper::mapToConsentsResponse201, headers);
    }

    @Override
    public ResponseEntity getConsentStatus(String consentId, UUID xRequestID, String digest, String signature,
                                           byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                           String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                           String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                           UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject<ConsentStatusResponse> accountConsentsStatusByIdResponse = consentService.getAccountConsentsStatusById(consentId);
        return accountConsentsStatusByIdResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(accountConsentsStatusByIdResponse.getError())
                   : responseMapper.ok(accountConsentsStatusByIdResponse, consentModelMapper::mapToConsentStatusResponse200);
    }

    @Override
    public ResponseEntity startConsentAuthorisation(UUID xRequestID, String consentId, Object body,
                                                    String digest, String signature, byte[] tpPSignatureCertificate,
                                                    String PSU_ID, String psUIDType, String psUCorporateID,
                                                    String psUCorporateIDType, Boolean tpPRedirectPreferred,
                                                    String tpPRedirectURI, String tpPNokRedirectURI,
                                                    String tpPNotificationURI, String tpPNotificationContentPreferred,
                                                    String psUIPAddress, String psUIPPort, String psUAccept,
                                                    String psUAcceptCharset, String psUAcceptEncoding,
                                                    String psUAcceptLanguage, String psUUserAgent,
                                                    String psUHttpMethod, UUID psUDeviceID,
                                                    String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);
        String password = authorisationMapper.mapToPasswordFromBody((Map) body);

        ResponseObject<AuthorisationResponse> createResponse = consentService.createAisAuthorisation(psuData, consentId, password);

        if (createResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(createResponse.getError(),
                                                             consentHeadersBuilder.buildErrorStartConsentAuthorisationHeaders());
        }

        AuthorisationResponse authorisationResponse = createResponse.getBody();
        ResponseHeaders responseHeaders = consentHeadersBuilder.buildStartConsentAuthorisationHeaders(authorisationResponse.getAuthorisationId());

        return responseMapper.created(ResponseObject.builder()
                                          .body(authorisationMapper.mapToAisCreateOrUpdateAuthorisationResponse(createResponse))
                                          .build(),
                                      responseHeaders);
    }

    @Override
    public ResponseEntity updateConsentsPsuData(UUID xRequestID, String consentId, String authorisationId, Object body, String digest,
                                                String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType,
                                                String psUCorporateID, String psUCorporateIDType, String psUIPAddress, String psUIPPort,
                                                String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage,
                                                String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType);

        return updateAisAuthorisation(psuData, authorisationId, consentId, body);
    }

    private ResponseEntity updateAisAuthorisation(PsuIdData psuData, String authorisationId, String consentId, Object body) {
        UpdateConsentPsuDataReq updatePsuDataRequest = consentModelMapper.mapToUpdatePsuData(psuData, consentId, authorisationId, (Map) body);
        ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuDataResponse = consentService.updateConsentPsuData(updatePsuDataRequest);

        if (updateConsentPsuDataResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(updateConsentPsuDataResponse.getError(),
                                                             consentHeadersBuilder.buildErrorUpdateConsentsPsuDataHeaders(authorisationId));
        }

        ResponseHeaders responseHeaders = consentHeadersBuilder.buildUpdateConsentsPsuDataHeaders(authorisationId);

        return responseMapper.ok(updateConsentPsuDataResponse, authorisationMapper::mapToAisUpdatePsuAuthenticationResponse, responseHeaders);
    }

    @Override
    public ResponseEntity getConsentScaStatus(String consentId, String authorisationId, UUID xRequestID, String digest,
                                              String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                              String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                              String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID,
                                              String psUGeoLocation) {

        ResponseObject<ScaStatus> consentAuthorisationScaStatusResponse = consentService.getConsentAuthorisationScaStatus(consentId, authorisationId);
        return consentAuthorisationScaStatusResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(consentAuthorisationScaStatusResponse.getError())
                   : responseMapper.ok(consentAuthorisationScaStatusResponse, authorisationMapper::mapToScaStatusResponse);
    }

    @Override
    public ResponseEntity getConsentAuthorisation(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate,
                                                  String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                  String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID,
                                                  String psUGeoLocation) {
        ResponseObject<Xs2aAuthorisationSubResources> consentInitiationAuthorisationsResponse = consentService.getConsentInitiationAuthorisations(consentId);
        return consentInitiationAuthorisationsResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(consentInitiationAuthorisationsResponse.getError())
                   : responseMapper.ok(consentInitiationAuthorisationsResponse, authorisationMapper::mapToAuthorisations);
    }

    @Override
    public ResponseEntity getConsentInformation(String consentId, UUID xRequestID, String digest, String signature,
                                                byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                                UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<AccountConsent> accountConsentByIdResponse = consentService.getAccountConsentById(consentId);
        return accountConsentByIdResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(accountConsentByIdResponse.getError())
                   : responseMapper.ok(accountConsentByIdResponse, consentModelMapper::mapToConsentInformationResponse200Json);
    }

    @Override
    public ResponseEntity deleteConsent(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate,
                                        String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                        String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject<Void> response = consentService.deleteAccountConsentsById(consentId);
        return response.hasError()
                   ? responseErrorMapper.generateErrorResponse(response.getError())
                   : responseMapper.delete(response);
    }
}
