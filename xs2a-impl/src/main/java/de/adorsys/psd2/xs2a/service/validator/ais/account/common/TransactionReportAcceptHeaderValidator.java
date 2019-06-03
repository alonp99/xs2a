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

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.ais.AccountResponseType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.REQUESTED_FORMATS_INVALID;

/**
 * Validator to be used for validating if accept header is supported by ASPSP
 * - If accept header is not presented in request validator will not validate header;
 * - If property "supportedTransactionApplicationTypes" was not configured validator will not validate header;
 * - Otherwise header should be presented in supported headers in bank profile
 */
@Component
@RequiredArgsConstructor
public class TransactionReportAcceptHeaderValidator {
    private final AspspProfileService aspspProfileService;

    public ValidationResult validate(String acceptHeader) {
        if (StringUtils.isNotBlank(acceptHeader)) {
            List<String> supportedTransactionApplicationTypes = aspspProfileService.getAspspSettings().getSupportedTransactionApplicationTypes();

            if (!isAtLeastOneAcceptHeaderSupported(supportedTransactionApplicationTypes, acceptHeader)) {
                return ValidationResult.invalid(ErrorType.AIS_406, TppMessageInformation.of(REQUESTED_FORMATS_INVALID));
            }
        }

        return ValidationResult.valid();
    }

    private boolean isAtLeastOneAcceptHeaderSupported(List<String> supportedHeaders, String acceptHeader) {
        return CollectionUtils.isEmpty(supportedHeaders) ||
                   supportedHeaders.stream()
                       .map(AccountResponseType::fromValue)
                       .filter(Objects::nonNull)
                       .anyMatch(tp -> acceptHeader.toLowerCase().contains(tp.getValue()));

    }
}
