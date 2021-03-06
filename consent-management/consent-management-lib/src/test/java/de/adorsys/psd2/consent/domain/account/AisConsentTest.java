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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentTest {
    private static final PsuData PSU_DATA = new PsuData("psu", null, null, null);
    private static final TppInfoEntity TPP_INFO = new TppInfoEntity();

    @Test
    public void isWrongConsentData_shouldReturnTrue_emptyPsuDataList() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.emptyList(), TPP_INFO, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    public void isWrongConsentData_shouldReturnTrue_tppInfoNull() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), null, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    public void isWrongConsentData_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertFalse(actual);
    }

    @Test
    public void isNonReccuringAlreadyUsed_shouldReturnFalse_recurringConsent() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO, true);

        // When
        boolean actual = aisConsent.isNonReccuringAlreadyUsed();

        // Then
        assertFalse(actual);
    }

    @Test
    public void isNonReccuringAlreadyUsed_shouldReturnFalse_nonRecurringWithoutOldUsages() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);
        aisConsent.setUsages(Collections.singletonList(buildAisConsentUsage(LocalDate.now())));

        // When
        boolean actual = aisConsent.isNonReccuringAlreadyUsed();

        // Then
        assertFalse(actual);
    }

    @Test
    public void isNonReccuringAlreadyUsed_shouldReturnTrue_nonRecurringWithOldUsages() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);
        aisConsent.setUsages(Arrays.asList(buildAisConsentUsage(LocalDate.now()),
                                           buildAisConsentUsage(LocalDate.now().minusDays(1))));

        // When
        boolean actual = aisConsent.isNonReccuringAlreadyUsed();

        // Then
        assertTrue(actual);
    }

    private AisConsent buildAisConsent(List<PsuData> psuDataList, TppInfoEntity tppInfoEntity, boolean recurringIndicator) {
        AisConsent consent = new AisConsent();
        consent.setPsuDataList(psuDataList);
        consent.setTppInfo(tppInfoEntity);
        consent.setRecurringIndicator(recurringIndicator);
        return consent;
    }

    private AisConsentUsage buildAisConsentUsage(LocalDate usageDate) {
        AisConsentUsage usage = new AisConsentUsage();
        usage.setUsageDate(usageDate);
        return usage;
    }
}
