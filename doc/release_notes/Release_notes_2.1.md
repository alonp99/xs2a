# Release notes v.2.1

## Event table extended with new searchable columns
From now on XS2A Event object (`de.adorsys.psd2.xs2a.core.event.Event`) contains the following fields:
 * PSU Data
 * TPP authorisation number
 * X-Request-Id

Also corresponding columns (`psu_id`, `psu_id_type`, `psu_corporate_id`, `psu_corporate_id_type`, `tpp_authorisation_number`, `x_request_id`) were added to the `event` table in the CMS database.
Also pay attention that property `requestId` in Event payload object was deleted as duplicated field.

## Added authorisation type to response for getting PSU data authorisations

Now these endpoints: `/v1/payment/{payment-id}/authorisation/psus` and `/v1/ais/consent/{consent-id}/authorisation/psus` have enriched
responses with new field added - `authorisationType`. The value can be `CREATED` or `CANCELLED` by now.