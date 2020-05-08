 package au.com.dius.resilience.model;
 
 public final class IncidentFactory {
 
  public static final Incident createIncident(String name, long dateCreated, String note) {
    Incident incident = new Incident(name, dateCreated, note);
 
     return incident;
   }
 }
