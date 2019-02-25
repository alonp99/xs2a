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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;


/**
 * @deprecated since 1.8. Will be removed in 1.10
 * TODO create new version of this class https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/365
 */
@Deprecated
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountReferenceValidationService {
    private final AspspProfileServiceWrapper profileService;

    public ResponseObject validateAccountReferences(Set<AccountReference> references) {
        List<SupportedAccountReferenceField> supportedFields = profileService.getSupportedAccountReferenceFields();

        boolean isInvalidReferenceSet = references.stream()
                                            .map(ar -> isValidAccountReference(ar, supportedFields))
                                            .anyMatch(Predicate.isEqual(false));

        return isInvalidReferenceSet
                   ? ResponseObject.builder()
                         .fail(AIS_400, of(FORMAT_ERROR))
                         .build()
                   : ResponseObject.builder().build();
    }

    private boolean isValidAccountReference(AccountReference reference, List<SupportedAccountReferenceField> supportedFields) {
        Map<SupportedAccountReferenceField, Boolean> validatedFieldsMap = supportedFields.stream()
                                                                              .map(fld -> Pair.of(fld, fld.isValid(reference)))
                                                                              .filter(p -> p.getValue().isPresent())
                                                                              .collect(Collectors.toMap(Pair::getKey, p -> p.getValue().get()));
        return areValidAllFields(validatedFieldsMap, reference);
    }

    private boolean areValidAllFields(Map<SupportedAccountReferenceField, Boolean> validatedFieldsMap, AccountReference reference) {

        List<SupportedAccountReferenceField> validFields = getFilteredFields(validatedFieldsMap, true);
        List<SupportedAccountReferenceField> invalidFields = getFilteredFields(validatedFieldsMap, false);

        boolean areValid = !validFields.isEmpty() && invalidFields.isEmpty();

        if (!areValid) {
            invalidFields.forEach(err -> log.warn("Field {} is not valid in reference: {}", err, reference));
        }

        return areValid;
    }

    private List<SupportedAccountReferenceField> getFilteredFields(Map<SupportedAccountReferenceField, Boolean> validatedFieldsMap, boolean isValidFilter) {
        return validatedFieldsMap.entrySet().stream()
                   .filter(etr -> etr.getValue() == isValidFilter)
                   .map(Map.Entry::getKey)
                   .collect(Collectors.toList());
    }
}
