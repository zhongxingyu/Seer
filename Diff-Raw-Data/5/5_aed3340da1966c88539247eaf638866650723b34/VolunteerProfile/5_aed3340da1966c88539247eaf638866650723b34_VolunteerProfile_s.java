 package com.sheepdog.mashmesh.models;
 
 import com.google.appengine.api.datastore.GeoPt;
 import com.google.appengine.api.search.*;
 import com.google.appengine.api.users.User;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Unindexed;
 import com.sheepdog.mashmesh.geo.GeoUtils;
 import com.sheepdog.mashmesh.util.ApplicationContants;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import javax.persistence.Embedded;
 import javax.persistence.Id;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 @Entity
 public class VolunteerProfile {
     private static final String INDEX_NAME = "volunteer-locations";
     private static final double DEFAULT_MAXIMUM_DISTANCE_MILES = 25;
     private static final int ESTIMATED_MILES_PER_HOUR = 40;
 
     private static class AppointmentTime {
         private long startTime;
         private long endTime;
     }
 
     @Id private String userId;
     @Unindexed private String documentId;
     @Unindexed private GeoPt location;
     @Unindexed private double maximumDistanceMiles = DEFAULT_MAXIMUM_DISTANCE_MILES;
     @Embedded private List<AppointmentTime> appointmentTimes = new ArrayList<AppointmentTime>();
 
     private double getAppointmentPaddingSeconds() {
         return ESTIMATED_MILES_PER_HOUR * maximumDistanceMiles * 60 * 60;
     }
 
     public void addAppointmentTime(DateTime departureTime, DateTime arrivalTime) {
         Duration commuteDuration = new Duration(departureTime, arrivalTime);
         DateTime startTime = departureTime;
         DateTime endTime = arrivalTime.plus(commuteDuration);
 
         AppointmentTime appointmentTime = new AppointmentTime();
         appointmentTime.startTime = startTime.getMillis();
         appointmentTime.endTime = endTime.getMillis();
 
         appointmentTimes.add(appointmentTime);
     }
 
     public boolean isTimeslotOccupied(DateTime dateTime) {
         long dateTimeMillis = dateTime.getMillis();
         int appointmentPaddingSeconds = (int) getAppointmentPaddingSeconds();
 
         for (AppointmentTime appointmentTime : appointmentTimes) {
             long startTimeMillis = appointmentTime.startTime - appointmentPaddingSeconds;
             long endTimeMills = appointmentTime.endTime + appointmentPaddingSeconds;
 
             if (startTimeMillis < dateTimeMillis && endTimeMills > dateTimeMillis) {
                 return true;
             }
         }
 
         return false;
     }
 
     public UserProfile getUserProfile() {
         Key<UserProfile> userProfileKey = Key.create(UserProfile.class, getUserId());
         return OfyService.ofy().find(userProfileKey);
     }
 
     public String getUserId() {
         return userId;
     }
 
     public void setUserId(String userId) {
         this.userId = userId;
     }
 
     public GeoPt getLocation() {
         return location;
     }
 
     public void setLocation(GeoPt location) {
         this.location = location;
     }
 
     public double getMaximumDistanceMiles() {
         return maximumDistanceMiles;
     }
 
     public void setMaximumDistanceMiles(double maximumDistanceMiles) {
         this.maximumDistanceMiles = maximumDistanceMiles;
     }
 
     public static Index getIndex() {
         IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_NAME).build();
         return SearchServiceFactory.getSearchService().getIndex(indexSpec);
     }
 
     public static int clearIndex() {
         Index index = getIndex();
         int documentCount = 0;
 
         while (true) {
             Query query = Query.newBuilder()
                 .setOptions(QueryOptions.newBuilder().setLimit(1000).build())
                 .build("");
             Collection<ScoredDocument> documents = index.search(query).getResults();
 
             documentCount += documents.size();
 
             if (documents.size() == 0) {
                 break;
             }
 
             List<String> ids = new ArrayList<String>();
 
             for (ScoredDocument document : documents) {
                 ids.add(document.getId());
             }
 
             index.delete(ids);
         }
 
         return documentCount;
     }
 
     public Document makeDocument(UserProfile userProfile) {
         GeoPoint location = GeoUtils.convertToGeoPoint(userProfile.getLocation());
        double maximumDistanceKilometers = maximumDistanceMiles * ApplicationContants.KILOMETERS_PER_MILE;
         Document.Builder documentBuilder = Document.newBuilder()
             .addField(Field.newBuilder().setName("userId").setText(getUserId()))
            .addField(Field.newBuilder().setName("maximumDistance").setNumber(maximumDistanceKilometers))
             .addField(Field.newBuilder().setName("location").setGeoPoint(location));
 
         if (documentId != null) {
             documentBuilder.setId(documentId);
         }
 
         return documentBuilder.build();
     }
 
     public void updateDocument(UserProfile userProfile) {
         Index index = getIndex();
         Document document = makeDocument(userProfile);
         PutResponse response = index.put(document);
         documentId = response.getIds().get(0);
         // TODO: PutExceptions and transient errors.
     }
 
     public Key<VolunteerProfile> getKey() {
         return Key.create(VolunteerProfile.class, getUserId());
     }
 
     public static VolunteerProfile getOrCreate(UserProfile userProfile) {
         Key<VolunteerProfile> volunteerProfileKey= Key.create(VolunteerProfile.class, userProfile.getUserId());
         VolunteerProfile volunteerProfile = OfyService.ofy().find(volunteerProfileKey);
 
         if (volunteerProfile == null) {
             volunteerProfile = new VolunteerProfile();
             volunteerProfile.setUserId(userProfile.getUserId());
         }
 
         return volunteerProfile;
     }
 
     // TODO: Break out volunteer query logic
     private static Query getEligibleVolunteerQuery(GeoPt patientLocation, GeoPt appointmentLocation) {
         String sortString = String.format("distance(location, %s)", GeoUtils.formatGeoPt(patientLocation));
         String queryString = String.format("distance(location, %s) < maximumDistance",
                 GeoUtils.formatGeoPt(patientLocation));
 
         SortOptions sortOptions = SortOptions.newBuilder()
                 .addSortExpression(SortExpression.newBuilder()
                         .setExpression(sortString)
                         .setDefaultValueNumeric(20000)
                         .setDirection(SortExpression.SortDirection.ASCENDING))
                 .build();
         QueryOptions queryOptions = QueryOptions.newBuilder()
                 .setLimit(1000)
                 .setSortOptions(sortOptions)
                 .build();
         return Query.newBuilder().setOptions(queryOptions).build("");//queryString);
     }
 
     private static Collection<VolunteerProfile> getVolunteerProfilesFromDocuments(
             Collection<? extends Document> documents) {
         List<Key<VolunteerProfile>> volunteerProfileKeys = new ArrayList<Key<VolunteerProfile>>();
 
         for (Document document : documents) {
             String userId = document.getOnlyField("userId").getText();
             Key<VolunteerProfile> volunteerProfileKey = Key.create(VolunteerProfile.class, userId);
             volunteerProfileKeys.add(volunteerProfileKey);
         }
 
         return OfyService.ofy().get(volunteerProfileKeys).values();
     }
 
     private static Collection<VolunteerProfile> filterAvailableVolunteers(
             Collection<VolunteerProfile> volunteerProfiles, DateTime appointmentTime) {
         List<VolunteerProfile> availableVolunteerProfiles = new ArrayList<VolunteerProfile>();
 
         for (VolunteerProfile volunteerProfile : volunteerProfiles) {
             if (!volunteerProfile.isTimeslotOccupied(appointmentTime)) {
                 availableVolunteerProfiles.add(volunteerProfile);
             }
         }
 
         return availableVolunteerProfiles;
     }
 
     private static Collection<VolunteerProfile> filterWillingVolunteers(
             Collection<VolunteerProfile> volunteerProfiles, GeoPt patientGeoPt, GeoPt appointmentGeoPt) {
         List<VolunteerProfile> willingVolunteerProfiles = new ArrayList<VolunteerProfile>();
         double patientToAppointmentDistance = GeoUtils.distance(patientGeoPt, appointmentGeoPt);
 
         for (VolunteerProfile volunteerProfile : volunteerProfiles) {
             double volunteerToPatientDistance = GeoUtils.distance(volunteerProfile.getLocation(), patientGeoPt);
             double maximumDistanceMiles = volunteerProfile.getMaximumDistanceMiles();
             if (volunteerToPatientDistance + patientToAppointmentDistance < maximumDistanceMiles) {
                 willingVolunteerProfiles.add(volunteerProfile);
             }
         }
 
         return willingVolunteerProfiles;
     }
 
     private static VolunteerProfile getClosestVolunteer(Collection<VolunteerProfile> volunteerProfiles) {
         // TODO: Improve accuracy with the distance matrix API.
         if (volunteerProfiles.size() == 0) {
             return null;
         } else {
             return volunteerProfiles.iterator().next();
         }
     }
 
     public static VolunteerProfile getEligibleVolunteer(GeoPt patientLocation, GeoPt appointmentLocation,
                                                         DateTime appointmentTime) {
         Query query = getEligibleVolunteerQuery(patientLocation, appointmentLocation);
         Collection<? extends Document> documents = getIndex().search(query).getResults();
         Collection<VolunteerProfile> volunteerProfiles = getVolunteerProfilesFromDocuments(documents);
         Collection<VolunteerProfile> eligibleVolunteerProfiles = filterAvailableVolunteers(
                 volunteerProfiles, appointmentTime);
         Collection<VolunteerProfile> willingVolunteerProfiles = filterWillingVolunteers(
                 eligibleVolunteerProfiles, patientLocation, appointmentLocation);
         VolunteerProfile closestVolunteer = getClosestVolunteer(willingVolunteerProfiles);
         // TODO: Raise an exception if no volunteer is found.
         return closestVolunteer;
     }
 }
