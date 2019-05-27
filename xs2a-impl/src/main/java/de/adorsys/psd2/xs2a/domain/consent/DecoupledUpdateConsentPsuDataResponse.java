package de.adorsys.psd2.xs2a.domain.consent;

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

/**
 * UpdateConsentPsuDataResponse extension to be used ONLY when switching from Embedded to Decoupled approach during SCA method selection
 */
public class DecoupledUpdateConsentPsuDataResponse extends UpdateConsentPsuDataResponse {
    public DecoupledUpdateConsentPsuDataResponse(ScaStatus scaStatus, String consentId, String authorisationId) {
        super(scaStatus, consentId, authorisationId);
    }

    /**
     * Returns <code>null</code> instead of chosenScaMethod when switching from Embedded to Decoupled approach during
     * SCA method selection as this value should not be provided in the response body according to the specification
     *
     * @return <code>null</code>
     */
    @Override
    public Xs2aAuthenticationObject getChosenScaMethodForPsd2Response() {
        return null;
    }
}
