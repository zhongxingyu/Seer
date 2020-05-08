 package com.solidstategroup.radar.model.filter;
 
 public class ProfessionalUserFilter extends BaseFilter {
     public enum UserField {
         SURNAME("uSurname"),
         FORENAME("uForename"),
         TITLE("uTitle"),
         ROLE("uRole"),
         EMAIL("email"),
        CENTRE("unit.name"),
         REGISTRATION_DATE("uDateJoin"),
         GMC("uGMC");
 
         private String databaseFieldName;
 
         private UserField(String databaseFieldName) {
             this.databaseFieldName = databaseFieldName;
         }
 
         public static UserField getUserField(String fieldName) {
             for (UserField userField : values()) {
                 if (userField.getDatabaseFieldName().equalsIgnoreCase(fieldName)) {
                     return userField;
                 }
             }
 
             return UserField.SURNAME;
         }
 
         public String getDatabaseFieldName() {
             return databaseFieldName;
         }
     }
 
     public ProfessionalUserFilter() {
         super();
         sortField = UserField.SURNAME.getDatabaseFieldName();
     }
 }
