 package com.atlassian.sal.bamboo.license;
 
 import org.apache.log4j.Logger;
 import com.atlassian.sal.api.license.LicenseHandler;
 import com.atlassian.bamboo.license.BambooLicenseManager;
 
 public class BambooLicenseHandler implements LicenseHandler
 {
     private static final Logger log = Logger.getLogger(BambooLicenseHandler.class);
     // ------------------------------------------------------------------------------------------------------- Constants
     // ------------------------------------------------------------------------------------------------- Type Properties
     // ---------------------------------------------------------------------------------------------------- Dependencies
    BambooLicenseManager bambooLicenseManager;
     // ---------------------------------------------------------------------------------------------------- Constructors
 
     public BambooLicenseHandler(BambooLicenseManager bambooLicenseManager)
     {
         this.bambooLicenseManager = bambooLicenseManager;
     }
     // ----------------------------------------------------------------------------------------------- Interface Methods
     // -------------------------------------------------------------------------------------------------- Action Methods
     // -------------------------------------------------------------------------------------------------- Public Methods
 
    public void setLicense(String s)
     {
        boolean licenseValid = bambooLicenseManager.authenticateLicense(s);
         if (licenseValid)
         {
            bambooLicenseManager.setLicense(s);
         }
         else
         {
            log.error("Did not set the Bamboo license, license Invalid.");
             throw new IllegalArgumentException("Did not set the Bamboo license, license Invalid.");
         }
     }
     // ------------------------------------------------------------------------------------------------- Helper Methods
     // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
 }
