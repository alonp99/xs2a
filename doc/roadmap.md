## Roadmap

### Versions in progress

### version 2.7 (Planned date 07.06.2019)
- Payment Cancellation Request update according to Errata for Specification v.1.3
- Handle XML application type in Read Transaction request 
- Bugfix: Separate delta report in profile to entryReferenceFrom and deltaList 
- Bugfix: Сreate consent returns empty list of scaMethods 
- Bugfix: CreditorAddress object is provided to SPI even if it was not present in the request
- Bugfix: Accept Authentication Type from ASPSP
- Delete "accounts" field in PiisConsentEntity, deprecated methods and "piis_consent_acc_reference" table (0058 script) in v 2.7 
- Delete createAuthorization in AisConsentAuthorisationServiceBase in v.2.7 

### version 3.5 (Planned date 07.06.2019)
- AspspConsentData refactoring
- Remove deprecated AspspConsentData updates in v.3.5 

## Further development

Starting 15th of March 2019 XS2A Team is going to provide development within two branches:

### Stable branch 2.x

Stable branch will contain bugfixing and possibly necessary changes to support mandatory endpoints defined by Berlin Group NextGenPSD2 Spec 1.3
Stable branch 2.x will be supported at least till 01.09.2019

### version 2.8 (Planned date 21.06.2019)
- Bugfix: Periodic payment can be created with invalid "dayOfExecution" tag 
- Bugfix: Consent-related endpoints return incorrect HTTP status code on providing unknown consent ID
- Bugfix: Get consent status request do not return mandated lastActionDate attribute in response body
- Bugfix: Get Cancellation Authorisation Sub-Resources Request returns wrong JSON format
- Bugfix: Get status of non-existing AIS consent returns wrong response
- Delete unused field in AisAccountConsent in v.2.8 

### version 2.9 (Planned date 05.07.2019)
- Bugfix: Incorrect response for Start authorisation request without psuId in header (Embedded\Decoupled Explicit)
- Bugfix: Incorrect response for Update PSU data for payment initiation request without psuId in header (Decoupled Implicit/Explicit) 
- Bugfix: Wrong response body for Start Payment Authorisation request Redirect Explicit approach
- Bugfix: Consents without successful authorisation should expire with status Rejected
- Bugfix: aspspAccountId no longer available for SPI
- Remove deprecated method in SpiPaymentInitiationResponse in v. 2.9 
- delete extra constructor in SpiInitiateAisConsentResponse in v 2.9 

### version 2.10 (Planned date 19.07.2019)
- Bugfix: Wrong Error code in payment initiation respond for not supported xml product types 
- Bugfix: Check incoming requests to have required information
- Bugfix: Retrieve payment data by redirect-id with correct endpoint
- Bugfix: Incorrect response for Start authorisation request without psuId in header (Redirect Explicit)
- Bugfix: Populating PSU_DATA table with excessive data
- Bugfix: Provide correct PSU Data to the SPI in SpiContextData

### version 2.11 (Planned date 02.08.2019)
- Bugfix: scaStatus link not available in response for Update PSU data for payment initiation (Decoupled Implicit) 
- Bugfix: Bad request when TPP enters an unknown user in the AIS consent embedded approach
- Bugfix: Wrong response for provision of an invalid TAN or password 
- Bugfix: PIIS should validate IBAN 
- Bugfix: SpiAccountConsent shouldn't return real ID (PK from DB)

### Upcoming features 2.x/3.x (Priorities may be changed)
- Bugfix: Get account response is empty for Consent on Account List of Available Accounts 
- Payment Authorisations and Payment Cancellation Authorisations should be separated from AIS Consent Authorisations
- Provide creation date and time in SPIrequest
- add the request execution duration to the log
- Extend logging with technical activities 
- Optional fields in JSON structure are commented in yaml

### Development branch 3.x

Development branch is oriented on implementation of new features and optional endpoints.
No backward compatibility with 2.x is guaranteed.


### version 3.6 (Planned date 21.06.2019)
- Execute payment without sca in OAuth approach 

### version 3.7 (Planned date 05.07.2019)
- Redirect timeout shall not be the same value as authorisation timeout  
- Multilevel SCA for Payment Initiation in Redirect approach
- Multilevel SCA for Establish Consent in Redirect approach 

### version 3.8 (Planned date 19.07.2019)
- Restructure profile by services 
- Move AuthenticationObject to xs2a-core 
- Move PaymentAuthorisationType to the xs2a-core 
- Support delta access for transaction list 

### Upcoming features 3.x (Priorities may be changed)
- Refactor CMS: return ResponseObject instead of Strings, Enums, Booleans etc.
- Support of download link 
- Redesign of error handlers on SPI level 
- Optional SCA for Access to all Accounts for all PSD2 defined AIS – Global Consent 
- Go through code and aggregate all messages sent to PSU to message bundle  
- Support of relative links 
- Validation of authorisation sub-resources  
- Component for scheduled batch processing 
- Support Get Transaction Status Response with xml format 
- Support Get Payment request for xml 
- Support of multicurrency accounts in AIS requests 
- Remove PSU data from CMS by request from ASPSP (for example due to Data protection (GDPR))
- Support sessions: Combination of AIS and PIS services 
- Add a new optional header TPP-Rejection-NoFunds-Preferred 
- Requirements on TPP URIs  
- handling for standard pain types 
- Update enum MessageErrorCode.java 
- Add instance_id for export PIIS consent 
- Extend CMS to store sca method and TAN for Redirect approach 
- Add to events rejected requests 
- Extract events to separate module in CMS 
- Refactoring of payment saving Part 2
- Refactor field validators (especially IBAN) to perform validation in Spring Component, not in static context 
- Recoverability 
- Change the logic of SpiResponseStatus to MessageErrorCode mapping after the discussion with PO 
- Implement CommonPaymentSpi interface in connector 
- Support all 3 formats of ISODateTime 
- Add service to delete consents and payments after period of time 
- Support OAuth sca for PIS
- Support OAuth sca for Payment cancellation
- Support OAuth sca for AIS 

###### Support of Signing Basket

- Implement Establish Signing Basket request
- Implement Get Signing Basket request
- Get Signing Basket Status Request
- Implement Get Authorisation Sub-resources for Signing Baskets
- Implement Get SCA Status request for Signing Baskets
- Implement Cancellation of Signing Baskets
- Support Signing Basket in Embedded approach with multilevel sca
- Support Signing Basket in Decoupled approach with multilevel sca
- Support Signing Basket in Redirect approach with multilevel sca


###### Support of FundsConfirmation Consent:

- Establish FundsConfirmationConsent
- Get FundsConfirmationConsent Status + object
- Revoke FundsConfirmationConsent
- FundsConfirmationConsent in Redirect approach with multilevel sca
- FundsConfirmationConsent in Embedded approach with multilevel sca
- FundsConfirmationConsent in Decoupled approach with multilevel sca
- Get Authorisation Sub-resource request for FundsConfirmationConsent
- Get Sca Status request for FundsConfirmationConsent
- Create interface in cms-aspsp-api to get FundsConfirmationConsent


###### Support of Card Accounts:

- Implement Read Card Account List request
- Implement Read Card Account Details request
- Implement Read Card Account Balance request
- Implement Read Card Account Transaction List request
