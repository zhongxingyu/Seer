 package no.niths.common;
 
 public final class SecurityConstants {
 
     //Roles
     public static final String R_ANONYMOUS = "ROLE_ANONYMOUS";
     public static final String R_STUDENT = "ROLE_STUDENT";
     public static final String R_ADMIN = "ROLE_ADMIN";
     public static final String R_COMMITTEE_LEADER = "ROLE_COMMITEE_LEADER";
     public static final String R_FADDER_LEADER = "ROLE_FADDER_LEADER";
     public static final String R_SR = "ROLE_SR";
     public static final String R_DEVELOPER = "ROLE_DEVELOPER";
     
     //Authorizes annotations
     public static final String ONLY_SR = "hasRole('ROLE_SR')";
     public static final String ONLY_ADMIN = "hasRole('ROLE_ADMIN')";
     public static final String ADMIN_AND_SR = "hasAnyRole('ROLE_ADMIN', 'ROLE_SR')";
     public static final String ADMIN_AND_SR_AND_STUDENT = "hasAnyRole('ROLE_ADMIN', 'ROLE_SR', 'ROLE_STUDENT')";
 }
