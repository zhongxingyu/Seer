 package com.reucon.commons.operation.checks;
 
 import com.reucon.commons.operation.EnvironmentCheckResult;
 import com.reucon.commons.operation.OperationalEnvironment;
 import com.reucon.commons.operation.VersionNumber;
 
 /**
  * Checks that the version of the database.
  */
 public class CheckDatabaseVersion extends AbstractEnvironmentCheck
 {
     private final VersionNumber requiredVersion;
 
     public CheckDatabaseVersion(String requiredVersion)
     {
         this.requiredVersion = new VersionNumber(requiredVersion);
     }
 
     @Override
     public EnvironmentCheckResult run(OperationalEnvironment environment)
     {
         return checkVersionIsAtLeast(requiredVersion, determineVersion(environment));
     }
 
     protected VersionNumber determineVersion(OperationalEnvironment environment)
     {
        if (environment.getDatabaseMajorVersion() == null || environment.getDatabaseMinorVersion() == null)
        {
            return null;
        }
         return new VersionNumber(environment.getDatabaseMajorVersion(), environment.getDatabaseMinorVersion());
     }
 }
