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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service("status-payments")
@RequiredArgsConstructor
public class ReadSinglePaymentStatusService implements ReadPaymentStatusService {
    private final PisAspspDataService pisAspspDataService;
    private final SpiPaymentFactory spiPaymentFactory;
    private final SinglePaymentSpi singlePaymentSpi;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    @Override
    public ReadPaymentStatusResponse readPaymentStatus(List<PisPayment> pisPayments, String paymentProduct, SpiContextData spiContextData, @NotNull String encryptedPaymentId) {
        Optional<SpiSinglePayment> spiSinglePaymentOptional = spiPaymentFactory.createSpiSinglePayment(pisPayments.get(0), paymentProduct);

        if (!spiSinglePaymentOptional.isPresent()) {
            return new ReadPaymentStatusResponse(
                ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
                    .messages(Collections.singletonList("Payment not found"))
                    .build()
            );
        }

        SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(encryptedPaymentId);

        SpiResponse<TransactionStatus> spiResponse = singlePaymentSpi.getPaymentStatusById(spiContextData, spiSinglePaymentOptional.get(), aspspConsentDataProvider);
        // TODO remove aspspConsentData from SPI Response in version 3.4 or later https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/786
        if (spiResponse.getAspspConsentData() != null) {
            aspspConsentDataProvider.updateAspspConsentData(spiResponse.getAspspConsentData().getAspspConsentData());
        }

        if (spiResponse.hasError()) {
            return new ReadPaymentStatusResponse(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS));
        }

        return new ReadPaymentStatusResponse(spiResponse.getPayload());
    }
}
