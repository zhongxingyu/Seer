 package cz.fi.muni.pa165.calorycounter.frontend;
 
 import static cz.fi.muni.pa165.calorycounter.frontend.BaseActionBean.escapeHTML;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.ActivityRecordDto;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import cz.fi.muni.pa165.calorycounter.serviceapi.ActivityRecordService;
 import cz.fi.muni.pa165.calorycounter.serviceapi.ActivityService;
 import cz.fi.muni.pa165.calorycounter.serviceapi.UserService;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.ActivityDto;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.AuthUserDto;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.WeightCategory;
 import java.util.List;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 
 /**
  * Stripes ActionBean for handling record operations.
  *
  * @author Martin Bryndza
  */
 @UrlBinding("/myrecord/{$event}/{record.activityRecordId}")
 public class RecordActionBean extends BaseActionBean {
 
     final static Logger log = LoggerFactory.getLogger(RecordActionBean.class);
     @SpringBean
     protected ActivityRecordService activityRecordService;
     @SpringBean
     protected ActivityService activityService;
     @SpringBean
     protected UserService userService;
     @ValidateNestedProperties(value = {
         @Validate(on = {"createRecord", "save"}, field = "activityName", required = true),
         @Validate(on = {"createRecord", "save"}, field = "duration", required = true, minvalue = 1),
         @Validate(on = {"createRecord", "save"}, field = "activityDate", required = true)
     })
     private ActivityRecordDto record;
     private List<ActivityDto> activities;
     private boolean isEdit = false;
 
     public AuthUserDto getUser() {
         return getSessionUser();
     }
 
     public boolean isIsEdit() {
         return isEdit;
     }
 
     public void setIsEdit(boolean isEdit) {
         this.isEdit = isEdit;
     }
 
     public List<ActivityDto> getActivities() {
         return activities;
     }
 
     public void setActivities(List<ActivityDto> activities) {
         this.activities = activities;
     }
 
     public ActivityRecordDto getRecord() {
         return record;
     }
 
     public void setRecord(ActivityRecordDto record) {
         this.record = record;
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"def", "createRecord"})
     public void setUp() {
         activities = activityService.getAll(getSessionUser().getWeightCategory());
     }
 
     @DefaultHandler
     public Resolution def() {
         log.debug("def()");
         isEdit = false;
         return new ForwardResolution("/record/create.jsp");
     }
 
     @HandlesEvent("createRecord")
     public Resolution createRecord() {
         log.debug("createRecord() record={}", record);
         isEdit = false;
         record.setCaloriesBurnt(getCaloriesBurnt(activities, record.getActivityName(), record.getWeightCategory(), record.getDuration()));
         Long createdId = activityRecordService.create(record);
         getContext().getMessages().add(new LocalizableMessage("record.create.message", escapeHTML(record.getActivityName().toString()), escapeHTML(String.valueOf(record.getDuration())), escapeHTML(String.valueOf(record.getCaloriesBurnt()))));
         log.debug("Created activity record with id " + createdId + ". <a href=\"/records/edit.jsp/" + createdId + "\">edit");
         return new RedirectResolution("/myrecord");
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"edit", "save", "delete"})
     public void loadRecordFromDatabase() {
         String id = getContext().getRequest().getParameter("record.activityRecordId");
         if (id == null) {
             return;
         }
         record = activityRecordService.get(Long.parseLong(id));
         activities = activityService.getAll(record.getWeightCategory());
     }
 
     public Resolution edit() {
         log.debug("edit() record={}", record);
         isEdit = true;
         return new ForwardResolution("/record/edit.jsp");
     }
 
     @HandlesEvent("save")
     public Resolution save() {
         log.debug("save() record={}", record);
         record.setCaloriesBurnt(getCaloriesBurnt(activities, record.getActivityName(), record.getWeightCategory(), record.getDuration()));
         activityRecordService.update(record);
         return new RedirectResolution("/records");
     }
 
     public Resolution delete() {
         log.debug("delete() record={}", record);
         isEdit = false;
         return new ForwardResolution("/record/delete.jsp");
     }
 
     public Resolution confirmDelete() {
         log.debug("confirmDelete() record={}", record);
         activityRecordService.remove(record.getActivityRecordId());
         return new RedirectResolution("/records");
     }
 
    public Resolution cancelCreate() {
        log.debug("cancelCreate()");
        return new RedirectResolution("/record/create.jsp");
    }

     public Resolution cancel() {
         log.debug("cancel()");
         isEdit = false;
         return new RedirectResolution("/records");
     }
 
     private int getCaloriesBurnt(List<ActivityDto> activities, String activityName, WeightCategory weightCategory, int duration) {
         ActivityDto activity = null;
         for (ActivityDto act : activities) {
             if (act.getActivityName().equals(activityName)) {
                 activity = act;
                 break;
             }
         }
         return (duration * (activity.getCaloriesAmount(weightCategory)) / 60);
     }
 }
