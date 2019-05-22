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

import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.AccountReferenceMapper;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CmsAspspPiisServiceInternalTest {
    private static final long CONSENT_INTERNAL_ID = 1;
    private static final String CONSENT_EXTERNAL_ID = "5bcf664f-68ce-498d-9a93-fe0cce32f6b6";
    private static final String CONSENT_EXTERNAL_ID_WRONG = "efe6d8bd-c6bc-4866-81a3-87ac755ffa4b";
    private static final String PSU_ID = "PSU-ID-1";
    private static final String PSU_ID_WRONG = "PSU-ID-2";
    private static final LocalDate EXPIRE_DATE = LocalDate.now().plusDays(100);
    private static final int FREQUENCY_PER_DAY = 4;
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final OffsetDateTime CREATION_TIMESTAMP = OffsetDateTime.of(2019, 2, 4, 12, 0, 0, 0, ZoneOffset.UTC);
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String TPP_AUTHORITY_ID = "authority id";
    private static final String CARD_NUMBER = "1234567891234";
    private static final LocalDate CARD_EXPIRY_DATE = LocalDate.now().plusDays(1);
    private static final String CARD_INFORMATION = "MyMerchant Loyalty Card";
    private static final String REGISTRATION_INFORMATION = "Your contract Number 1234 with MyMerchant is completed with the registration with your bank.";
    private static TppInfo tppInfo;


    @Mock
    private PiisConsentRepository piisConsentRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AccountReferenceMapper accountReferenceMapper;
    @Mock
    private PiisConsentMapper piisConsentMapper;
    @Mock
    private PiisConsentEntitySpecification piisConsentEntitySpecification;
    @InjectMocks
    private CmsAspspPiisServiceInternal cmsAspspPiisServiceInternal;

    @Before
    public void setUp() {
        tppInfo = buildTppInfo();
        when(psuDataMapper.mapToPsuData(buildPsuIdData())).thenReturn(buildPsuData());
        when(piisConsentMapper.mapToPiisConsent(buildPiisConsentEntity())).thenReturn(buildPiisConsent());
        when(piisConsentRepository.save(any(PiisConsentEntity.class))).thenReturn(buildPiisConsentEntity());
        when(tppInfoMapper.mapToTppInfoEntity(tppInfo)).thenReturn(buildTppInfoEntity());
        when(accountReferenceMapper.mapToAccountReferenceEntity(buildAccountReference())).thenReturn(buildAccountReferenceEntity());
    }

    @Test
    public void createConsent_success() {
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(buildConsent());

        // Given
        PsuIdData psuIdData = buildPsuIdData();
        ArgumentCaptor<PiisConsentEntity> argumentCaptor = ArgumentCaptor.forClass(PiisConsentEntity.class);
        LocalDate validUntil = LocalDate.now().plusDays(1);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, buildAccountReference(), validUntil);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isTrue();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(StringUtils.isNotBlank(actual.get())).isTrue();
        verify(piisConsentRepository).save(argumentCaptor.capture());

        PiisConsentEntity piisConsent = argumentCaptor.getValue();
        assertThat(StringUtils.isNotBlank(piisConsent.getExternalId())).isTrue();
        Assert.assertEquals(buildTppInfoEntity(), piisConsent.getTppInfo());
        Assert.assertEquals(buildAccountReferenceEntity(), piisConsent.getAccount());
        Assert.assertEquals(validUntil, piisConsent.getExpireDate());
        Assert.assertEquals(request.getAllowedFrequencyPerDay(), piisConsent.getAllowedFrequencyPerDay());
        Assert.assertEquals(request.getCardNumber(), piisConsent.getCardNumber());
        Assert.assertEquals(request.getCardExpiryDate(), piisConsent.getCardExpiryDate());
        Assert.assertEquals(request.getCardInformation(), piisConsent.getCardInformation());
        Assert.assertEquals(request.getRegistrationInformation(), piisConsent.getRegistrationInformation());
    }

    @Test
    public void createConsentClosePreviousPiisConsents_success() {
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(buildConsent());

        // Given
        PsuIdData psuIdData = buildPsuIdData();
        AccountReference accountReference = buildAccountReference();

        when(piisConsentEntitySpecification.byPsuIdDataAndTppInfoAndAccountReference(psuIdData, tppInfo, accountReference))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        List<PiisConsentEntity> piisConsentEntities = Arrays.asList(buildPiisConsentEntity(), buildPiisConsentEntity());
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class))).thenReturn(piisConsentEntities);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PiisConsentEntity>> argumentCaptor = ArgumentCaptor.forClass((Class) List.class);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, accountReference, LocalDate.now().plusDays(1));

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        verify(piisConsentRepository).save(argumentCaptor.capture());

        List<PiisConsentEntity> previousPiisConsent = argumentCaptor.getValue();
        assertEquals(piisConsentEntities.size(), previousPiisConsent.size());

        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndTppInfoAndAccountReference(psuIdData, tppInfo, accountReference);

        Set<ConsentStatus> consentStatuses = previousPiisConsent.stream()
                                                 .map(PiisConsentEntity::getConsentStatus)
                                                 .collect(Collectors.toSet());

        assertEquals(consentStatuses.size(), 1);
        assertTrue(consentStatuses.contains(ConsentStatus.REVOKED_BY_PSU));

    }

    @Test
    public void createConsent_withExpireDateToday_success() {
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(buildConsent());

        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, buildAccountReference(), LocalDate.now());

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isTrue();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(StringUtils.isNotBlank(actual.get())).isTrue();
    }

    @Test
    public void createConsent_withTppInfo_success() {
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(buildConsent());

        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, buildAccountReference(), EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isTrue();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(StringUtils.isNotBlank(actual.get())).isTrue();
    }

    @Test
    public void createConsent_savingFailed_shouldFail() {
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(buildConsent(null));

        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, buildAccountReference(), EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void createConsent_withEmptyPsuIdDate_shouldFail() {
        // Given
        PsuIdData emptyPsuIdData = new PsuIdData(null, null, null, null);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, null, EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(emptyPsuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void createConsent_withEmptyTppInfo_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(new TppInfo(), buildAccountReference(), EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void createConsent_withNullTppInfo_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(null, buildAccountReference(), EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void createConsent_withNullAccounts_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, null, EXPIRE_DATE);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void createConsent_withInvalidExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        LocalDate yesterdayDate = LocalDate.now().minusDays(1);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, null, yesterdayDate);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void createConsent_withNullExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, buildAccountReference(), null);

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void createConsent_withCardExpireDate_shouldFail() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        LocalDate yesterdayDate = LocalDate.now().minusDays(1);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(tppInfo, null, yesterdayDate);
        request.setCardExpiryDate(LocalDate.now().minusDays(1));

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, request);

        // Then
        assertThat(actual.isPresent()).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    @Test
    public void getConsentsForPsu_Success() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID);
        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildPiisConsentEntity()));
        PiisConsent expected = buildPiisConsent();

        // When
        List<PiisConsent> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.get(0)).isEqualTo(expected);
        verify(piisConsentEntitySpecification, times(1))
            .byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentsForPsu_Failure_WrongPsuId() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID_WRONG);

        // When
        List<PiisConsent> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual.isEmpty()).isTrue();
        verify(piisConsentEntitySpecification, times(1))
            .byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void terminateConsent_Success() {
        // Given
        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(piisConsentRepository.findOne(any(Specification.class))).thenReturn(buildPiisConsentEntity());
        ArgumentCaptor<PiisConsentEntity> argumentCaptor = ArgumentCaptor.forClass(PiisConsentEntity.class);

        // When
        boolean actual = cmsAspspPiisServiceInternal.terminateConsent(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual).isTrue();
        verify(piisConsentRepository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getConsentStatus()).isEqualTo(ConsentStatus.TERMINATED_BY_ASPSP);
        verify(piisConsentEntitySpecification, times(1))
            .byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void terminateConsent_Failure_WrongConsentId() {
        // Given

        // When
        boolean actual = cmsAspspPiisServiceInternal.terminateConsent(CONSENT_EXTERNAL_ID_WRONG, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertThat(actual).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
        verify(piisConsentEntitySpecification, times(1))
            .byConsentIdAndInstanceId(CONSENT_EXTERNAL_ID_WRONG, DEFAULT_SERVICE_INSTANCE_ID);
    }

    private PiisConsentEntity buildConsent() {
        return buildConsent(CONSENT_INTERNAL_ID);
    }

    private PiisConsentEntity buildConsent(Long id) {
        PiisConsentEntity piisConsent = new PiisConsentEntity();
        piisConsent.setId(id);
        piisConsent.setExternalId(CONSENT_EXTERNAL_ID);
        piisConsent.setRequestDateTime(OffsetDateTime.now());
        piisConsent.setPsuData(buildPsuData());
        piisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        return piisConsent;
    }

    private PsuData buildPsuData() {
        return new PsuData(PSU_ID, null, null, null);
    }

    private PsuIdData buildPsuIdData() {
        return buildPsuIdData(PSU_ID);
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null);
    }

    private AccountReference buildAccountReference() {
        return new AccountReference("aspspAccountId", "resourceId",
                                    "DE89370400440532013000",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);
    }

    private AccountReferenceEntity buildAccountReferenceEntity() {
        AccountReference accountReference = buildAccountReference();
        AccountReferenceEntity accountReferenceEntity = new AccountReferenceEntity();
        accountReferenceEntity.setAspspAccountId(accountReference.getAspspAccountId());
        accountReferenceEntity.setIban(accountReference.getIban());
        return accountReferenceEntity;
    }

    private List<PiisConsentEntity> buildPiisConsentEntityList() {
        return Collections.singletonList(buildPiisConsentEntity());
    }

    private PiisConsentEntity buildPiisConsentEntity() {
        PiisConsentEntity consentEntity = new PiisConsentEntity();
        consentEntity.setExternalId(CONSENT_EXTERNAL_ID);
        consentEntity.setCreationTimestamp(CREATION_TIMESTAMP);
        return consentEntity;
    }

    private PiisConsent buildPiisConsent() {
        PiisConsent consent = new PiisConsent();
        consent.setId(CONSENT_EXTERNAL_ID);
        consent.setCreationTimestamp(CREATION_TIMESTAMP);
        return consent;
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        return tppInfo;
    }

    private TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfo = new TppInfoEntity();
        tppInfo.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        return tppInfo;
    }

    private CreatePiisConsentRequest buildCreatePiisConsentRequest(TppInfo tppInfo, AccountReference account, LocalDate validUntil) {
        CreatePiisConsentRequest request = new CreatePiisConsentRequest();
        request.setTppInfo(tppInfo);
        request.setAccount(account);
        request.setValidUntil(validUntil);
        request.setAllowedFrequencyPerDay(FREQUENCY_PER_DAY);
        request.setCardNumber(CARD_NUMBER);
        request.setCardExpiryDate(CARD_EXPIRY_DATE);
        request.setCardInformation(CARD_INFORMATION);
        request.setRegistrationInformation(REGISTRATION_INFORMATION);
        return request;
    }
}
