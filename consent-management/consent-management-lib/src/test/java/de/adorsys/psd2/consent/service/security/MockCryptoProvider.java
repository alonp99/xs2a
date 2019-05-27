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

package de.adorsys.psd2.consent.service.security;

import de.adorsys.psd2.consent.service.security.provider.AbstractCryptoProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;


public class MockCryptoProvider extends AbstractCryptoProvider {
    private final boolean alwaysFail;

    public MockCryptoProvider(String externalId, boolean alwaysFail) {
        super(externalId, 256, 32, "PBKDF2WithHmacSHA256");
        this.alwaysFail = alwaysFail;
    }

    @Override
    public Optional<EncryptedData> encryptData(byte[] data, String password) {
        if (alwaysFail) {
            return Optional.empty();
        }

        String encrypted = new String(data) + password;
        return Optional.of(new EncryptedData(encrypted.getBytes()));
    }

    @Override
    public Optional<DecryptedData> decryptData(byte[] data, String password) {
        if (alwaysFail) {
            return Optional.empty();
        }

        String decrypted = StringUtils.removeEnd(new String(data), password);
        return Optional.of(new DecryptedData(decrypted.getBytes()));
    }
}
