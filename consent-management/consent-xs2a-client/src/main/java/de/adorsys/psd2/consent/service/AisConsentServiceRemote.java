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

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

// TODO discuss error handling (e.g. 400 HttpCode response) https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/498
@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentServiceRemote implements AisConsentServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Override
    public Optional<String> createConsent(CreateAisConsentRequest request) {
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), request, CreateAisConsentResponse.class).getBody();
        return Optional.ofNullable(createAisConsentResponse.getConsentId());
    }

    @Override
    public Optional<ConsentStatus> getConsentStatusById(String consentId) {
        AisConsentStatusResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), AisConsentStatusResponse.class, consentId).getBody();
        return Optional.ofNullable(response.getConsentStatus());
    }

    @Override
    public boolean updateConsentStatusById(String consentId, ConsentStatus status) {
        try {
            consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, status);
            return true;
        } catch (CmsRestException cmsRestException) {
            log.warn("Cannot update consent status, the consent is already deleted or not found");
        }
        return false;
    }

    @Override
    public Optional<AisAccountConsent> getAisAccountConsentById(String consentId) {
        AisAccountConsent accountConsent = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), AisAccountConsent.class, consentId).getBody();
        return Optional.ofNullable(accountConsent);
    }

    @Override
    public Optional<AisAccountConsent> getInitialAisAccountConsentById(String consentId) {
        AisAccountConsent accountConsent = consentRestTemplate.getForEntity(remoteAisConsentUrls.getInitialAisConsentById(), AisAccountConsent.class, consentId).getBody();
        return Optional.ofNullable(accountConsent);
    }

    @Override
    public boolean findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        consentRestTemplate.delete(remoteAisConsentUrls.findAndTerminateOldConsentsByNewConsentId(), newConsentId);
        return true;
    }

    @Override
    public void checkConsentAndSaveActionLog(AisConsentActionRequest request) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), request, Void.class);
    }

    @Override
    public Optional<String> updateAspspAccountAccess(String consentId, AisAccountAccessInfo request) {
        CreateAisConsentResponse response = consentRestTemplate.exchange(remoteAisConsentUrls.updateAisAccountAccess(), HttpMethod.PUT,
            new HttpEntity<>(request), CreateAisConsentResponse.class, consentId).getBody();
        return Optional.ofNullable(response.getConsentId());
    }

    @Override
    public Optional<String> createAuthorization(String consentId, AisConsentAuthorizationRequest request) {
        CreateAisConsentAuthorizationResponse response = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsentAuthorization(),
            request, CreateAisConsentAuthorizationResponse.class, consentId).getBody();

        return Optional.ofNullable(response)
                   .map(CreateAisConsentAuthorizationResponse::getAuthorizationId);
    }

    @Override
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentAuthorizationById(), AisConsentAuthorizationResponse.class, consentId, authorizationId)
                                       .getBody());
    }

    @Override
    public boolean updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentAuthorization(), request, authorizationId);
        return true;
    }

    @Override
    public Optional<List<PsuIdData>> getPsuDataByConsentId(String consentId) {
        return Optional.ofNullable(consentRestTemplate.exchange(remoteAisConsentUrls.getPsuDataByConsentId(),
                                                                HttpMethod.GET,
                                                                null,
                                                                new ParameterizedTypeReference<List<PsuIdData>>() {
                                                                },
                                                                consentId)
                                       .getBody());
    }

    @Override
    public Optional<List<String>> getAuthorisationsByConsentId(String encryptedConsentId) {
        try {
            ResponseEntity<List<String>> request = consentRestTemplate.exchange(
                remoteAisConsentUrls.getAuthorisationSubResources(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                }, encryptedConsentId);
            return Optional.ofNullable(request.getBody());
        } catch (CmsRestException cmsRestException) {
            log.warn("No authorisation found by consentId {}", encryptedConsentId);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String encryptedConsentId, String authorisationId) {
        try {
            ResponseEntity<ScaStatus> request = consentRestTemplate.getForEntity(
                remoteAisConsentUrls.getAuthorisationScaStatus(), ScaStatus.class, encryptedConsentId, authorisationId);
            return Optional.ofNullable(request.getBody());
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't get authorisation SCA Status by consentId {} and authorisationId {}");
        }
        return Optional.empty();
    }

    @Override
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.isAuthenticationMethodDecoupled(), Boolean.class, authorisationId, authenticationMethodId)
                   .getBody();
    }

    @Override
    public boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        try {
            ResponseEntity<Void> responseEntity = consentRestTemplate.exchange(remoteAisConsentUrls.saveAuthenticationMethods(), HttpMethod.POST, new HttpEntity<>(methods), Void.class, authorisationId);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                return true;
            }
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't save authentication methods {} by authorisationId {}", methods, authorisationId);
        }

        return false;
    }

    @Override
    public boolean updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return consentRestTemplate.exchange(remoteAisConsentUrls.updateScaApproach(), HttpMethod.PUT, null, Boolean.class, authorisationId, scaApproach)
                   .getBody();
    }

    @Override
    public boolean updateMultilevelScaRequired(String encryptedConsentId, boolean multilevelScaRequired) {
        return consentRestTemplate.exchange(remoteAisConsentUrls.updateMultilevelScaRequired(),
            HttpMethod.PUT, null, Boolean.class, encryptedConsentId, multilevelScaRequired)
                   .getBody();
    }
}
