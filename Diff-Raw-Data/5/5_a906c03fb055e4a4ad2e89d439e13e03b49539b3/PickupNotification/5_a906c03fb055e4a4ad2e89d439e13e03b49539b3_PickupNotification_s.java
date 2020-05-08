 package com.sheepdog.mashmesh;
 
 
 import com.sheepdog.mashmesh.models.UserProfile;
 import com.sheepdog.mashmesh.util.ApplicationConfiguration;
 import com.sheepdog.mashmesh.util.EmailUtils;
 import com.sheepdog.mashmesh.util.VelocityUtils;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.context.Context;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import javax.mail.MessagingException;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 // TODO: The structure of this class doesn't really make any sense at all...
 public class PickupNotification {
     // TODO: Pull out these constants
     private static final int MAXIMUM_URL_LENGTH = 2048;
     private static final String STATIC_MAPS_ENDPOINT_URL = "https://maps.googleapis.com/maps/api/staticmap";
     private static final String DYNAMIC_MAPS_URL = "https://maps.google.com/maps";
 
     public static final String VOLUNTEER_NOTIFICATION_TEMPLATE_PATH = "notifications/volunteerDirections.vm";
     public static final String PATIENT_NOTIFICATION_TEMPLATE_PATH = "notifications/patientPickup.vm";
     private static final String FAILURE_NOTIFICATION_TEMPLATE_PATH = "notifications/failure.vm";
 
     // TODO: Would we need to inject these?
     private static final DateTimeFormatter dateFormatter = DateTimeFormat.fullDate();
     private static final DateTimeFormatter timeFormatter = DateTimeFormat.shortTime();
 
     private final UserProfile patientProfile;
     private final UserProfile volunteerUserProfile;
     private final Itinerary itinerary;
     private final String appointmentAddress;
 
     public PickupNotification(UserProfile patientProfile, UserProfile volunteerUserProfile,
                               Itinerary itinerary, String appointmentAddress) {
         this.patientProfile = patientProfile;
         this.volunteerUserProfile = volunteerUserProfile;
         this.itinerary = itinerary;
         this.appointmentAddress = appointmentAddress;
     }
 
     public String getDepartureLatLng() {
         return itinerary.getStartLatLng();
     }
 
     public DateTime getDepartureTime() {
         return itinerary.getDepartureTime();
     }
 
     public String getPickupLatLng() {
         return itinerary.getStartLeg().getEndLatLng();
     }
 
     public DateTime getPickupTime() {
         return itinerary.getStartLeg().getArrivalTime();
     }
 
     public String getArrivalLatLng() {
         return itinerary.getEndLatLng();
     }
 
     public DateTime getArrivalTime() {
         return itinerary.getArrivalTime();
     }
 
     private URI createStaticMapUri(boolean renderOverviewPolyline) throws URISyntaxException {
         URIBuilder uriBuilder = new URIBuilder(STATIC_MAPS_ENDPOINT_URL);
 
         uriBuilder.addParameter("sensor", "false");
         uriBuilder.addParameter("key", ApplicationConfiguration.getMapsApiKey());
         uriBuilder.addParameter("size", "600x400");
         uriBuilder.addParameter("markers", "color:green|label:A|" + getDepartureLatLng());
         uriBuilder.addParameter("markers", "color:blue|label:B|" + getPickupLatLng());
         uriBuilder.addParameter("markers", "color:blue|label:C|" + getArrivalLatLng());
         uriBuilder.addParameter("language", "en_US");
         uriBuilder.addParameter("maptype", "roadmap");
 
         if (renderOverviewPolyline) {
             uriBuilder.addParameter("path", "weight:4|color:blue|enc:" + itinerary.getOverviewPolyline());
         }
 
         return uriBuilder.build();
     }
 
     private URI createDynamicMapUri() throws URISyntaxException {
         URIBuilder uriBuilder = new URIBuilder(DYNAMIC_MAPS_URL);
         uriBuilder.addParameter("saddr", volunteerUserProfile.getAddress());
         uriBuilder.addParameter("daddr", patientProfile.getAddress() + " to:" + appointmentAddress);
         return uriBuilder.build();
     }
 
     private static String formatDateTime(DateTimeFormatter formatter, DateTime dateTime) throws IOException {
         StringWriter writer = new StringWriter();
         formatter.printTo(writer, dateTime);
         return writer.toString();
     }
 
    private static String formatDate(DateTime dateTime) throws IOException {
         return formatDateTime(timeFormatter, dateTime);
     }
 
    private static String formatTime(DateTime dateTime) throws IOException {
         return formatDateTime(dateFormatter, dateTime);
     }
 
     private static String getAppointmentSummary(String appointmentAddress, DateTime dateTime) throws IOException {
         return String.format("%s on %s at %s",
                 appointmentAddress, formatDate(dateTime), formatTime(dateTime));
     }
 
     private static String renderTemplateToString(String templatePath, Context context) {
         Template template = VelocityUtils.getInstance().getTemplate(templatePath);
         StringWriter writer = new StringWriter();
         template.merge(context, writer);
         return writer.toString();
     }
 
     public String renderTemplate(String templatePath) throws URISyntaxException, IOException {
         URI staticMapUri = createStaticMapUri(true);
         URI dynamicMapUri = createDynamicMapUri();
 
         if (staticMapUri.toString().length() > MAXIMUM_URL_LENGTH) {
             // If the polyline is too long, we need to eliminate it in order for the map to render
             staticMapUri = createStaticMapUri(false);
         }
 
         Context context = new VelocityContext();
         context.put("volunteerUserProfile", volunteerUserProfile);
         context.put("patientProfile", patientProfile);
         context.put("appointmentDate", formatDate(getArrivalTime()));
         context.put("appointmentTime", formatTime(getArrivalTime()));
         context.put("appointmentAddress", appointmentAddress);
         context.put("departureTime", formatTime(getDepartureTime()));
         context.put("arrivalTime", formatTime(getArrivalTime()));
         context.put("pickupTime", formatTime(getPickupTime()));
         context.put("staticMapUrl", staticMapUri.toString());
         context.put("dynamicMapUrl", dynamicMapUri.toString());
         context.put("directionLegs", itinerary.getLegs());
 
         return renderTemplateToString(templatePath, context);
     }
 
     public void send() throws IOException, URISyntaxException, MessagingException {
         String subject = "Appointment Pickup: " + getAppointmentSummary(appointmentAddress, getArrivalTime());
         String volunteerNotification = renderTemplate(VOLUNTEER_NOTIFICATION_TEMPLATE_PATH);
         EmailUtils.sendEmail(volunteerUserProfile.getEmail(), subject, volunteerNotification);
 
         String patientNotification = renderTemplate(PATIENT_NOTIFICATION_TEMPLATE_PATH);
         EmailUtils.sendEmail(patientProfile.getEmail(), subject, patientNotification);
     }
 
     public static String renderFailureNotification(UserProfile patientProfile, String appointmentAddress,
                                                    DateTime arrivalTime) throws IOException {
         Context context = new VelocityContext();
         context.put("patientProfile", patientProfile);
         context.put("appointmentAddress", appointmentAddress);
         context.put("appointmentDate", formatDate(arrivalTime));
         context.put("appointmentTime", formatTime(arrivalTime));
         return renderTemplateToString(FAILURE_NOTIFICATION_TEMPLATE_PATH, context);
     }
 
     public static void sendFailureNotification(UserProfile patientProfile, String appointmentAddress,
                                                DateTime arrivalTime) throws IOException, MessagingException {
         String subject = "No Pickup Available: " + getAppointmentSummary(appointmentAddress, arrivalTime);
         String patientNotification = renderFailureNotification(patientProfile, appointmentAddress, arrivalTime);
         EmailUtils.sendEmail(patientProfile.getEmail(), subject, patientNotification,
                 ApplicationConfiguration.getFailureEmailAddress());
     }
 }
