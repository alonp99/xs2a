# SPI Developer Guide

## Purpose and Scope

## Choosing the deployment layout

### Microservices Deployment

### Embedded Deployment

### Embedding XS2A Library

### Embedding CMS Library

### Embedding Profile library

## Setting up ASPSP Profile options

### Using debug interface

## Implementing SPI-API

### General requirements

#### SpiResponse

#### Work with ASPSP-Consent-Data object

### Implementation of AisConsentSpi

#### Providing account resources to consent 

#### SCA Approach REDIRECT

#### SCA Approach DECOUPLED

#### SCA Approach EMBEDDED

### Implementation of AccountSpi

### Implementation of PaymentSpi(s)
     
**SPI** means Service Provider Interface. that is an API intended to be implemented or extended by a third party. 

We distinguished between following Interfaces: 

**SinglePaymentSpi**:  Interface to be used for the single payment Implementation. The following Methods have to be implemented: **initiatePayment** (aims to initiate a Payment), **getPaymentById** (aims to read the pay,ent by id )and 
**getPaymentStatusById** (aims to read the payment status by id). 
    
   * Response by the methods "initiate Payment" will returns a positive or negative payment initiation response as a part of SpiResponse and will contains the following: 
      * **contextData**: holder of call's context data (e.g. about **PsuData** and **TppInfo**): 
        
        * **PsuData** contains data about PSU known in scope of the request: 
   
     | Attribute         |  Type   |      Condition    | Description       |
     | :---              |  :---:  |          :---:    |          :---     |
     | PsuId             | String  |      Conditional  | Client ID of the PSU in the ASPSP client interface.|
     | psuIdType         | String  |      Conditional  | Type of the PsuId, needed in scenarios where PSUs have several PsuIds as access possibility|
     | psuCorporateId    | String  |      Conditional  | Identification of a Corporate in the Online Channels. Might be mandated in the ASPSP’s documentation. Only used in a corporate context.|
     | psuCorporateIdType| String  |      Conditional  | This is describing the type of the identification needed by the ASPSP to identify the psuCorporateId|

   * **tppInfo** contains information about the Tpp's certificate:
        - "Registration number": example = "1234_registrationNumber",
        - "Tpp name": example = "Tpp company",
        - "National competent authority": example = "Bafin",
        - "Redirect URI": URI of TPP, where the transaction flow shall be redirected to after a Redirect. Mandated for the **Redirect SCA Approach** (including OAuth2 SCA approach), specially when TPP-Redirect-preferred equals "true". It is recommended to always use this header field. 
        - "Nok redirect URI": if this URI is contained, the TPP is asking to redirect the transaction flow to this address instead of the TPP-Redirect-URI in case of a negative result of the redirect ScaMethod. This might be ignored by the ASPSP
        
        * **payment**: payment, that extends SpiPayment (Single Payment)

        * **initialAspspConsentData** Encrypted data to be stored in the consent management system




   * Response by the methods "**getPaymentById**" will returns payment as a part of SpiResponse and will contains the following data: 

     **contextData**, **payment** (Single Payment), And 

     **aspspConsentData**: This is used as a container of some binary data to be used on SPI level. Spi developers may save here necessary information, that will be stored and encrypted in consent. This shall not use without consentId!
                         Encrypted data that may be stored in the consent management system in the consent linked to a request.They may be null if consent does not contain such data, or request is not done from a workflow with a consent. 
                         
                         
   * Response by the methods "getPaymentStatusById" will contains the following: **contextData**, **payment** and **aspspConsentData**. This method will return a response object, which contains the transaction status



**PeriodicPaymentSpi**: Interface to be used for periodic payment SPI implementation

  The following Methods have to be implemented: **initiatePayment**  **getPaymentById** and **getPaymentStatusById**. 
    
  * Response by the methods "initiate Payment" will returns a positive or negative payment initiation response as a part of SpiResponse and will contains the following: 
      * **contextData**: holder of call's context data (e.g. about **PsuData** and **TppInfo** )
   
      * **payment**: Periodic Payment

      * **initialAspspConsentData** Encrypted data to be stored in the consent management system


  * Response by the methods "getPaymentById" will returns payment as a part of SpiResponse and will contains the following data: 

      **contextData**, 
    
      **payment** (Periodic Payment)

      **aspspConsentData**: This is used as a container of some binary data to be used on SPI level. Spi developers may save here necessary information, that will be stored and encrypted in consent. This shall not use without consentId!
                         Encrypted data that may be stored in the consent management system in the consent linked to a request.They may be null if consent does not contain such data, or request is not done from a workflow with a consent. 
                         
                         
  * Response by the methods "getPaymentStatusById" will contains the following: **contextData**, **payment** and **aspspConsentData**. This method will return a response object, which contains the transaction status


**BulkPaymentSpi**: Interface to be used for bulk payment SPI implementation

   The following Methods have to be implemented: **initiatePayment**  **getPaymentById** and **getPaymentStatusById**.

   * Response by the methods "initiate Payment" will returns a positive or negative payment initiation response as a part of SpiResponse and will contains the following: 
      * **contextData**: holder of call's context data (e.g. about **PsuData** and **TppInfo** )
   
      * **payment**: Bulk Payment

      * **initialAspspConsentData** Encrypted data to be stored in the consent management system

   * Response by the methods "getPaymentById" will returns payment as a part of SpiResponse and will contains the following data: 

      **contextData**, 
    
      **payment** (Bulk Payment)

      **aspspConsentData**: This is used as a container of some binary data to be used on SPI level. Spi developers may save here necessary information, that will be stored and encrypted in consent. This shall not use without consentId!
                         Encrypted data that may be stored in the consent management system in the consent linked to a request.They may be null if consent does not contain such data, or request is not done from a workflow with a consent. 
                         
                         
   * Response by the methods "getPaymentStatusById" will contains the following: **contextData**, **payment** and **aspspConsentData**. This method will return a response object, which contains the transaction status
   
   
**PaymentAuthorisationSpi**: Interface to be used while implementing payment authorisation flow on SPI level. This Interface will be implemented by extending the **AuthorisationSPi** 

   The following methods have to be implemented: **authorisePsu**, **requestAvailableScaMethods**, **requestAuthorisationCode**.
    
   * **authorisePsu**: This Method authorises psu and returns current (success or failure) authorisation status. have to be use only with embedded SCA Approach. It contains following Data:
      * **contextdata**
      * **psuLoginData**: ASPSP identifier(s) of the psu, provided by TPP within this request
      * **password**: Psu's password
      * **businessObject**: payment object
      * **aspspConsentData**
        
   * **requestAvailableScaMethods**: This returns a list of SCA methods for the psu by its login and to be use only with the embedded Approach. It contains following Data:
    
      * **contextdata**, **businessObject** and **aspspConsentData**
        
       
   * **requestAuthorisationCode**: This performs SCA depending on selected SCA method. To be use only with embedded Approach.  This method return a positive or negative response as a part
   of SpiResponse. If the authentication method is unknow, then  empty SpiAuthorizationCoderesult should be returned. It contains following Data:
   
      * **contextdata**, **businessObject**, **aspspConsentData** and **authenticationMethodId** (Id of a chosen sca method)
        
        
    
   In case of **Decoupled SCA Approach**, the method **startScaDecoupled** have to be implement: This method notifies a decoupled app about starting SCA. AuthorisationId is provided
   to allow the app to access CMS.  It returns a response object, containing a message from ASPSP to PSU, giving him instrctions regarding decoupled SCA starting. It contains the following data: 
   
   * **contextdata**, **businessObject**, **aspspConsentData**, **authenticationMethodId** (for a decoupled SCA method within embedded approach)) and **authorisationId** (that is a unique identifier of authorisation process)
   
   
**PaymentCancellationSpi**: Interface to be used to cancel a payment
The following methods have to be implemented:

   * **initiatePaymentCancellation**: This method will return the payment cancellation response with information about transaction status and whether authorisation of the request is required. 
    It contains the following data: 
        * **contextdata**, **Payment** (payment to be cancelled) and **aspspConsentData**
      

   * **cancelPaymentWithoutSca**: to be use by cancelling payment without performing SCA. This method returns a positive or negative response as part of spiRestponse. 
   It contains the following data:
      * **contextdata**, **Payment** (payment to be cancelled) and **aspspConsentData**
    
   * **verifyScaAuthorisationAndCancelPayment**: This method sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful validation cancels payment at ASPSP. it also returnd 
   a positive or negative response as part of spiResponse. It contains the following data: 
   * **contextdata**, **Payment** (payment to be cancelled), **aspspConsentData** and **spiScaConfirmation** (payment cancellation confirmation information)

                         
The Payment initiation depends heavily on the **Strong Customer Authentication (SCA)** approach implemented by the ASPSP. The Berlin Group describes four approaches to implement this, but we currently done this with 
3 Approaches (REDIRECT, DECOUPLED and EMBEDDED). 
  
#### SCA Approach REDIRECT

-- Prerequisites in case of **consent for payment initiation**

    - PSU initiated a payment by using TPP
    - PSU is authenticated via two factors: for example PsuId and passwort
    - Each Payment initiation needs it own consent!

After the Payment Initiation is created, it has to be authorise from the PSU. In case of redirect approach the authorisation can be explicit or implicit.
    
 * **The explicit Start of the authorisation** process means that the Payment initiation Request is followed by an explicit Request of the TPP to start the authorisation. This is followed by a redirection to the ASPSP SCA authorisation site. 
 A status request might be requested by the TPP after the session is reredirected to the TPP's system 
   
     * In this case the authorisation will be used in case if tppExplicitAuthorisationPreferred = true and signingBasketSupported = true or in case of multilevel SCA
         * **tppExplicitAuthorisationPreferred**: value of tpp'choice of authorisation method
         * **signingBasketSupported**: reads if signing basket is supported on the ASPSP profile. It will return _true_  if ASPSP supports signing basket , _false_ if doesn't
    
 * In case of **implicit Start of the Authorisation process** the ASPSP needed no additional data from TPP. In this case, the redirection of the PSU browser session happens 
 directly after the Payment Initiation Response. In addition an SCA status request can be sent by the TPP to follow the SCA process.
  
    * In this case the authorisation will be used based on tppExplicitAuthorisationPreferred and signingBasketSupported values.
        * Implicit authorisation will be used in all the cases where tppExplicitAuthorisationPreferred or signingBasketSupported not equals true
        * Implicit approach is impossible in case of multilevel SCA
         

* For The Redirect Approach the developer needs to implement the following methods: 

    * **createCommonPaymentAuthorisation**: this will create payment authorisation response and contains:
        * **paymentId**: ASPSP identifier of a payment,
        * **paymentType**: e.g. single payment, periodic payment, bulk payment
        * **psudata**: PsuIdData container of authorisation data about PSU
        
    * **updateCommonPaymentPsuData**: contains **request** that provides transporting data when updating consent psu data. It will return the update payment authorization response
    
    * **getAuthorisationSubResources** with the **paymentId** and returns authorisation sub resources
    
    * **getAuthorisationScaStatus** with **paymentId** (ASPSP identifier of the payment, associated with the authorisation) and 
    **authorisationId** (authorisation identifier). This method will returns SCA status
    
    * **getScaApproachServiceTypeProvider**: to get sca approach used in current service. This will return the ScaApproach **“Redirect”**
    
    
* Redirect Approach for Payment cancellation

    * **createCommonPaymentCancellationAuthorisation**: This will create payment cancellation authorisation with **paymentId**, **paymentType** and **psudata**
    * **getCancellationAuthorisationSubResources**: with the **paymentId**
    * **updateCommonPaymentCancellationPsuData**: updates the cancellation for the payment
    * **getCancellationAuthorisationScaStatus** with **PaymentId** and **CancellationId**
    * **getScaApproachServiceTypeProvider**: to get sca approach used in current service. This will return the ScaApproach **“Redirect”**
    
 
    

#### SCA Approach DECOUPLED

#### SCA Approach EMBEDDED

### Implementation of FundsConfirmationSpi

## Working with CMS

### Using the CMS-PSU-API

#### SCA Approaches REDIRECT and DECOUPLED

#### SCA Approach EMBEDDED

### Using the CMS-ASPSP-API

#### Using the Tpp locking interface

#### Using the Consents/Payments export interface

#### Using the FundsConfirmation Consent interface

## Special modes

### Multi-tenancy support
