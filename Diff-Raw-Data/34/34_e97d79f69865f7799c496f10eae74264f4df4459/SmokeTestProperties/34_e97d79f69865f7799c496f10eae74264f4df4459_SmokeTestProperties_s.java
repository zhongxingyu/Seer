 package org.openmrs.module.mirebalais.smoke.helper;
 
 public class SmokeTestProperties {
 
     public String getWebAppUrl() {
        return envOrDefault("WEBAPP_URL", "http://localhost:8080/openmrs_1.9");
     }
 
     public String getDatabaseUrl() {
        return envOrDefault("DATABASE_URL", "jdbc:mysql://localhost:3306/openmrs_1_9");
     }
 
     public String getDatabaseUsername() {
        return envOrDefault("DATABASE_USERNAME", "openmrs_1_9_user");
     }
 
     public String getDatabasePassword() {
        return envOrDefault("DATABASE_PASSWORD", "c65Ln9e^.di1");
     }
 
     public String getDatabaseDriverClass() {
         return "com.mysql.jdbc.Driver";
     }
 
     private String envOrDefault(String environmentVariable, String defaultValue) {
         return System.getenv(environmentVariable) != null ? System.getenv(environmentVariable) : defaultValue;
     }
 }
