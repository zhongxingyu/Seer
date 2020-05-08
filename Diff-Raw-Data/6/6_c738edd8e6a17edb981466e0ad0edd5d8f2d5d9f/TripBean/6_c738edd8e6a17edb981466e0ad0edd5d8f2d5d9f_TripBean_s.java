 package be.kdg.groeph.bean;
 
 import be.kdg.groeph.model.RepeatingTripType;
 import be.kdg.groeph.model.Trip;
 import be.kdg.groeph.model.TripType;
 import be.kdg.groeph.model.TripUser;
 import be.kdg.groeph.service.TripService;
 import be.kdg.groeph.util.Tools;
 import org.apache.log4j.Logger;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 @Component
 @SessionScoped
 @Named
 public class TripBean implements Serializable {
     static Logger logger = Logger.getLogger(TripBean.class);
     private static final String REAPTING = "Repeating";
     private static final String TIMEBOUND = "Timebound";
     private static final String ANYTIME = "Anytime";
     private static final String EDITTRIP = "EDITTRIP";
     private static final String YEARLY = "Yearly";
     private static final String MONTHLY = "Monthly";
     private static final String WEEKLY = "Weekly";
     private static final String DAILY = "Daily";
     @ManagedProperty(value = "#{tripService}")
     @Autowired
     TripService tripService;
     @Qualifier("loginBean")
     @Autowired
     LoginBean loginBean;
 
     @NotEmpty(message = "{title} {notempty}")
     private String title;
     @NotEmpty(message = "{description} {notempty}")
     private String description;
     @NotNull(message = "{startTime} {notempty}")
     private Date startTime;
     @NotNull(message = "{endTime} {notempty}")
     private Date endTime;
     @NotEmpty(message = "{label} {notempty}")
     private String label;
     // private List<Label> labels;
     @NotEmpty(message = "{tripType} {notempty}")
     private String tripType;
     @NotEmpty(message = "repetitionType mag niet leeg zijn!")
     private String repetitionType;
     @NotNull(message = "Number of repetitions is required")
     private Integer numberOfRepetitions;
 
     private boolean isPublic;
 
     @NotEmpty(message = "{title} {notempty}")
     private String newTitle;
     @NotEmpty(message = "{description} {notempty}")
     private String newDescription;
     @NotNull(message = "{startTime} {notempty}")
     private Date newStartTime;
     @NotNull(message = "{endTime} {notempty}")
     private Date newEndTime;
     @NotEmpty(message = "{label} {notempty}")
     private String newLabel;
     @NotEmpty(message = "{tripType} {notempty}")
     private String newTripType;
     @NotEmpty(message = "repetitionType mag niet leeg zijn!")
     private String newRepetitionType;
     @NotNull(message = "Number of repetitions is required")
     private Integer newNumberOfRepetitions;
     private boolean newIsPublic;
 
 
     Trip currentTrip;
     private String filter;
     private boolean isVisible;
     private boolean started;
     private boolean hasCurrentTrip;
     private boolean isInteractive;
     private boolean editableTrip;
     boolean isRepeated;
     boolean notAnytime;
 
 
     public TripBean() {
         isPublic = true;
         editableTrip = false;
         //labels = new ArrayList<Label>();
         filter = "";
         numberOfRepetitions = null;
     }
 
     public String getNewRepetitionType() {
         return newRepetitionType;
     }
 
     public void setNewRepetitionType(String newRepetitionType) {
         this.newRepetitionType = newRepetitionType;
     }
 
     public Integer getNewNumberOfRepetitions() {
         return newNumberOfRepetitions;
     }
 
     public void setNewNumberOfRepetitions(Integer newNumberOfRepetitions) {
         this.newNumberOfRepetitions = newNumberOfRepetitions;
     }
 
     public String getFilter() {
         return filter;
     }
 
     public void setFilter(String filter) {
         this.filter = filter;
     }
 
     public Integer getNumberOfRepetitions() {
         return numberOfRepetitions;
     }
 
     public void setNumberOfRepetitions(Integer numberOfRepetitions) {
         this.numberOfRepetitions = numberOfRepetitions;
     }
 
     public boolean getPublic() {
         return isPublic;
     }
 
     public void setPublic(boolean aPublic) {
         isPublic = aPublic;
     }
 
     public String getRepetitionType() {
         return repetitionType;
     }
 
     public void setRepetitionType(String repetitionType) {
         this.repetitionType = repetitionType;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getTitle() {
         return title;
     }
 
     public boolean isEditableTrip() {
         return editableTrip;
     }
 
     public void setEditableTrip(boolean editableTrip) {
         this.editableTrip = editableTrip;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setStartTime(Date time) {
         this.startTime = time;
     }
 
     public Date getStartTime() {
         return startTime;
     }
 
     public Date getEndTime() {
         return endTime;
     }
 
     public void setEndTime(Date endTime) {
         this.endTime = endTime;
     }
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
    /* public List<Label> getLabels() {
 
         labels = tripService.getLabels(getCurrentTrip());
 
         return labels;
     }
 
     public void setLabels(ArrayList<Label> labels) {
         this.labels = labels;
     }   */
 
     public String getTripType() {
         return tripType;
     }
 
     public void setTripType(String tripType) {
         this.tripType = tripType;
     }
 
     public Trip getCurrentTrip() {
         return currentTrip;
     }
 
     public void setCurrentTrip(Trip currentTrip) {
         this.currentTrip = currentTrip;
     }
 
     public boolean isVisible() {
         return isVisible;
     }
 
     public void setVisible(boolean visible) {
         isVisible = visible;
     }
 
     public boolean isStarted() {
         return started;
     }
 
     public void setStarted(boolean started) {
         this.started = started;
     }
 
     public String getNewTitle() {
         return newTitle;
     }
 
     public void setNewTitle(String newTitle) {
         this.newTitle = newTitle;
     }
 
     public String getNewDescription() {
         return newDescription;
     }
 
     public void setNewDescription(String newDescription) {
         this.newDescription = newDescription;
     }
 
     public Date getNewStartTime() {
         return newStartTime;
     }
 
     public void setNewStartTime(Date newStartTime) {
         this.newStartTime = newStartTime;
     }
 
     public Date getNewEndTime() {
         return newEndTime;
     }
 
     public void setNewEndTime(Date newEndTime) {
         this.newEndTime = newEndTime;
     }
 
     public String getNewLabel() {
         return newLabel;
     }
 
     public void setNewLabel(String newLabel) {
         this.newLabel = newLabel;
     }
 
     public String getNewTripType() {
         return newTripType;
     }
 
     public void setNewTripType(String newTripType) {
         this.newTripType = newTripType;
     }
 
     public boolean isNewIsPublic() {
         return newIsPublic;
     }
 
     public void setNewIsPublic(boolean newIsPublic) {
         this.newIsPublic = newIsPublic;
     }
 
     public boolean isNotAnytime() {
         return notAnytime;
     }
 
     public void setNotAnytime(boolean notAnytime) {
         this.notAnytime = notAnytime;
     }
 
     public boolean isRepeated() {
         return isRepeated;
     }
 
     public void setRepeated(boolean repeated) {
         isRepeated = repeated;
     }
 
     public boolean isHasCurrentTrip() {
         try {
             if (currentTrip == null) {
                 hasCurrentTrip = false;
             } else {
                 hasCurrentTrip = true;
             }
             return hasCurrentTrip;
         } catch (Exception e) {
             logger.error(e.toString());
             return false;
         }
     }
 
     public void setHasCurrentTrip(boolean hasCurrentTrip) {
         this.hasCurrentTrip = hasCurrentTrip;
     }
 
     public String NoCurrentTrip() {
         try {
             setCurrentTrip(null);
             return null;
         } catch (Exception e) {
             logger.info(e.toString());
             return null;
         }
     }
 
     public String setThisAsCurrentTrip(Trip trip) {
         try {
             setCurrentTrip(trip);
             System.out.println(currentTrip.getId());
            return "SETTRIP";
         } catch (Exception e) {
             logger.error(e.toString());
             return Tools.FAILURE;
         }
     }
 
     public String addTrip() {
         try {
             Trip trip;
             setVisible(false);
             TripType type = tripService.getTypeByName(getTripType());
             RepeatingTripType repeatingTripType = tripService.getRepetitionTypeByName(getRepetitionType());
             //public Trip(String title, String description, Date startTime, Date endTime, String label, TripType tripType, boolean isPublic) {
 
             if (numberOfRepetitions != null) {
                 for (int i = 0; i < numberOfRepetitions; i++) {
                     Calendar calStart = Calendar.getInstance();
                     calStart.setTime(getStartTime());
                     Calendar calEnd = Calendar.getInstance();
                     calEnd.setTime(getEndTime());
                     switch (repetitionType) {
                         case "Yearly":
                             calStart.add(Calendar.YEAR, i);
                             calEnd.add(Calendar.YEAR, i);
                             break;
                         case "Monthly":
                             calStart.add(Calendar.MONTH, i);
                             calEnd.add(Calendar.MONTH, i);
                             break;
                         case "Weekly":
                             calStart.add(Calendar.WEEK_OF_YEAR, i);
                             calEnd.add(Calendar.WEEK_OF_YEAR, i);
                             break;
                         case "Daily":
                             calStart.add(Calendar.DAY_OF_MONTH, i);
                             calEnd.add(Calendar.DAY_OF_MONTH, i);
                             break;
                     }
 
                     trip = new Trip(getTitle(), getDescription(), calStart.getTime(), calEnd.getTime(), getLabel(), type, repeatingTripType, getPublic(), isVisible);
                     trip.setStarted(false);
 
                     loginBean.getUser().addTrip(trip);
                     if (tripService.addTrip(trip)) {
                         currentTrip = trip;
                     } else {
                         return Tools.FAILURE;
                     }
                 }
                 logger.info("Trip: " + currentTrip + " has been added");
                 clearFields();
                 return Tools.SUCCESS;
             } else {
 
                 trip = new Trip(getTitle(), getDescription(), getStartTime(), getEndTime(), getLabel(), type, repeatingTripType, getPublic(), isVisible);
                 trip.setStarted(false);
 
                 // Label label = new Label(getLabel());
                 //trip.addLabel(label);
 
                 loginBean.getUser().addTrip(trip);
                 if (tripService.addTrip(trip)) {
                     currentTrip = trip;
                     logger.info("Trip: " + currentTrip + " has been added");
                     clearFields();
                     return Tools.SUCCESS;
                 } else {
                     logger.info("Trip could not be added");
                     return Tools.FAILURE;
                 }
             }
         } catch (Exception e) {
             logger.error(e.toString());
             return Tools.FAILURE;
         }
     }
 
     public void clearFields() {
         try {
             title = null;
             description = null;
             isPublic = true;
             tripType = null;
             startTime = null;
             endTime = null;
             label = null;
             repetitionType = null;
             numberOfRepetitions = null;
             //labels = null;
         } catch (Exception e) {
             logger.error(e.toString());
         }
     }
 
     public List<Trip> getAllPublicTrips() {
         try {
             List<Trip> publictrips = tripService.fetchAllPublicTrips();
             logger.info("All public trips fetched");
             return Tools.filter(publictrips, filter);
         } catch (Exception e) {
             logger.error(e.toString());
             return null;
         }
     }
 
     public List<TripType> getAllTripTypes() {
         try {
             logger.info("All trip types fetched.");
             return tripService.fetchAllTripTypes();
         } catch (Exception e) {
             logger.error(e.toString());
             return null;
         }
     }
 
     public List<RepeatingTripType> getAllRepeatingTripTypes() {
         try {
             logger.info("all repeating trips fetched.");
             return tripService.fetchAllRepeatingTripTypes();
         } catch (Exception e) {
             logger.error(e.toString());
             return null;
         }
     }
 
     public List<Trip> getAllInvitedTrips() {
         try {
             logger.info("All invited trips for user: " + loginBean.getUser().getEmail() + " get.");
             return Tools.filter(tripService.getAllInvitedTripsByUser(loginBean.getUser()), filter);
         } catch (Exception e) {
             logger.error(e.toString());
             return null;
         }
     }
 
     public List<Trip> getAllParticipatedTrips() {
         try {
             logger.info("All participated trips for user: " + loginBean.getUser().getEmail() + " get.");
             return Tools.filter(tripService.getAllParticipatedTripsByUser(loginBean.getUser()), filter);
         } catch (Exception e) {
             logger.error(e.toString());
             return null;
         }
     }
 
     public List<Trip> getAllCreatedTrips() {
         try {
             logger.info("All created trips from user: " + loginBean.getUser().getEmail() + " get.");
             return Tools.filter(tripService.getAllCreatedTripsByUser(loginBean.getUser()), filter);
         } catch (Exception e) {
             logger.error(e.toString());
             return null;
         }
     }
 
     public void confirmParticipation() {
         try {
             TripUser user = loginBean.getUser();
             currentTrip.addConfirmedUser(user);
             user.confirmParticipation(currentTrip);
             tripService.addConfirmedTrip(currentTrip);
             logger.info("Participation for user: " + user.getEmail() + " confirmed for trip: " + currentTrip.getTitle());
         } catch (Exception e) {
             logger.error(e.toString());
         }
     }
 
     public boolean isConfirmInvitation() {
         try {
             if (currentTrip != null) {
                 if (currentTrip.getTripUsers().contains(loginBean.getUser())) {
                     return true;
                 } else {
                     return false;
                 }
             }
             return false;
         } catch (Exception e) {
             logger.error(e);
             return false;
         }
     }
 
     public boolean publishTrip() {
         try {
             currentTrip.setVisible(true);
             if (tripService.updateTrip(currentTrip)) {
                 logger.info("Trip: " + currentTrip.getTitle() + " is published.");
                 return true;
             } else {
                 logger.error("Trip: " + currentTrip.getTitle() + " could not be published.");
                 return false;
             }
         } catch (Exception e) {
             logger.error(e.toString());
             return false;
         }
     }
 
     public boolean startTrip() {
         try {
             currentTrip.setStarted(true);
             if (tripService.updateTrip(currentTrip)) {
                 logger.info("Trip: " + currentTrip.getTitle() + " is started.");
                 return true;
             } else {
                 logger.error("Trip: " + currentTrip.getTitle() + " could not be started.");
                 return false;
             }
         } catch (Exception e) {
             logger.error(e.toString());
             return false;
         }
     }
 
     public boolean stopTrip() {
         try {
             currentTrip.setStarted(false);
             if (tripService.updateTrip(currentTrip)) {
                 logger.info("Trip: " + currentTrip.getTitle() + " is stopped.");
                 return true;
             } else {
                 logger.error("Trip: " + currentTrip.getTitle() + " could not be stopped.");
                 return false;
             }
         } catch (Exception e) {
             logger.error(e.toString());
             return false;
         }
     }
 
     public String updateTrip() {
         try {
             Trip trip = getCurrentTrip();
 
             trip.setDescription(getNewDescription());
             trip.setLabel(getNewLabel());
             //Label label = new Label(getNewLabel());
             //trip.addLabel(label);
             trip.setEndTime(getNewEndTime());
             trip.setStartTime(getNewStartTime());
             trip.setTitle(getNewTitle());
             TripType tripType = tripService.getTypeByName(getNewTripType());
             trip.setTripType(tripType);
             trip.setPublic(isNewIsPublic());
 
             RepeatingTripType repetitionType = tripService.getRepetitionTypeByName(getNewRepetitionType());
             trip.setRepeatingTripType(repetitionType);
 
             if (tripService.updateTrip(trip)) {
                 editableTrip = false;
                 logger.info("Trip: " + currentTrip.getTitle() + " is updated.");
                 return Tools.SUCCESS;
             }
 
             logger.info("Trip: " + currentTrip.getTitle() + " could not be updated.");
             return Tools.FAILURE;
         } catch (Exception e) {
             logger.error(e.toString());
             return Tools.FAILURE;
         }
     }
 
 
     public String editTrip() {
         try {
             newDescription = currentTrip.getDescription();
             newLabel = currentTrip.getLabel();
             //newLabel = getCurrentTrip().getLabels().get(0).getName();
             newTripType = getCurrentTrip().getTripType().getType();
             if (!newTripType.toString().equals("Anytime")) {
                 newEndTime = getCurrentTrip().getEndTime();
                 newStartTime = getCurrentTrip().getStartTime();
             }
             newTitle = getCurrentTrip().getTitle();
             newIsPublic = getCurrentTrip().isPublic();
             if (newTripType.toString().equals("Repeating")) {
                 newRepetitionType = getCurrentTrip().getRepeatingTripType().getRepeatingType();
             }
             editableTrip = true;
             return EDITTRIP;
         } catch (Exception e) {
             logger.error(e);
             return null;
         }
     }
 
 
     public void changeUpdateTripType() {
         try {
             System.out.println(getNewTripType());
             if (getNewTripType().equals(REAPTING)) {
                 isRepeated = true;
             } else {
                 isRepeated = false;
             }
 
             if (getNewTripType().equals(ANYTIME)) {
                 notAnytime = false;
             } else {
                 notAnytime = true;
             }
 
             logger.info("Trip type for trip: " + currentTrip.getTitle() + " has been changed.");
         } catch (Exception e) {
             logger.error(e);
         }
     }
 
     public void changeTripType() {
         try {
             if (getTripType().equals(REAPTING)) {
                 isRepeated = true;
             } else {
                 isRepeated = false;
             }
 
             if (getTripType().equals(ANYTIME)) {
                 notAnytime = false;
             } else {
                 notAnytime = true;
             }
 
             logger.info("Trip type for trip: " + currentTrip.getTitle() + " has been changed.");
         } catch (Exception e) {
             logger.error(e);
         }
     }
 
    /* public void refreshCurrentTrip() {
         setCurrentTrip(tripService.getTripById(currentTrip.getId()));
     } */
 }
