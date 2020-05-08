 /*
 *	http://www.jrecruiter.org
 *
 *	Disclaimer of Warranty.
 *
 *	Unless required by applicable law or agreed to in writing, Licensor provides
 *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 *	including, without limitation, any warranties or conditions of TITLE,
 *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
 *	solely responsible for determining the appropriateness of using or
 *	redistributing the Work and assume any risks associated with Your exercise of
 *	permissions under this License.
 *
 */
 package org.jrecruiter.web.actions.admin;
 
 import java.util.Date;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.struts2.convention.annotation.Result;
import org.jrecruiter.common.GoogleMapsUtils;
 import org.jrecruiter.common.Constants.CommongKeyIds;
 import org.jrecruiter.model.Industry;
 import org.jrecruiter.model.Job;
 import org.jrecruiter.model.Region;
 import org.jrecruiter.web.WebUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.opensymphony.xwork2.validator.annotations.EmailValidator;
 import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
 import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
 import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
 import com.opensymphony.xwork2.validator.annotations.UrlValidator;
 import com.opensymphony.xwork2.validator.annotations.Validations;
 import com.opensymphony.xwork2.validator.annotations.ValidatorType;
 
 /**
  * Edit a job. Save the changes or delete the job posting altogether.
  *
  * @author Gunnar Hillert
  * @version $Id$
  *
  */
 @Result(name="success", location="show-jobs", type="redirectAction")
 public class EditJobAction extends JobBaseAction {
 
     /** serialVersionUID. */
     private static final long serialVersionUID = 2621352739536377825L;
 
     /**
      * Logger Declaration.
      */
     private final static Logger LOGGER = LoggerFactory.getLogger(EditJobAction.class);
 
     /**
      * Delete the job.
      */
     public String delete() {
 
          final Job jobFromDB = jobService.getJobForId(this.model.getJob().getId());
 
          if (jobFromDB == null) {
              throw new IllegalArgumentException("Job with id " + model.getJob().getId() + " does not exist.");
          }
 
          jobService.deleteJobForId(this.model.getJob().getId()); //FIXME SECURITY
          super.addActionMessage(getText("class.ShowJobsAction.job_delete_success_one"));
          return SUCCESS;
     }
 
     /**
      * Retrieve a job for edting.
      */
     public String execute() {
 
         if (this.id == null) {
             throw new IllegalArgumentException("No job id was provided.");
         }
 
         final Job jobFromDb = jobService.getJobForId(this.id);
 
         if (jobFromDb != null) {
             LOGGER.info("Loaded job with Id: " + jobFromDb.getId());
             this.model.setJob(jobFromDb);
         } else {
             throw new IllegalStateException("No job found for id " + this.id);
         }
 
        if (!this.model.getJob().getUsesMap()) {
            this.model.getJob().setZoomLevel(GoogleMapsUtils.defaultMapZoomLevel);
            this.model.getJob().setLatitude(GoogleMapsUtils.defaultMapLatitude);
            this.model.getJob().setLongitude(GoogleMapsUtils.defaultMapLongitude);
        }

         return INPUT;
     }
 
     /**
      * Save the job.
      */
     @Validations(
             requiredFields = {
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "job.businessEmail", message = "You must enter an Email Address."),
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "job.industry.id", message = "You must select an industry."),
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "job.offeredBy", message = "Please select whether you are the hiring company or a recruiter."),
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "job.region.id", message = "Please select a region."),
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "job.usesMap", message = "Do you want to use Google Maps.")
                     },
             requiredStrings = {
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "job.jobTitle", message = "You must enter a job title."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "job.businessName", message = "You must provide a company name."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "job.description", message = "You must enter a job description.")
                     },
             emails =
                     { @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "job.businessEmail", message = "You must enter a valid email.")},
             urls =
                     { @UrlValidator(type = ValidatorType.SIMPLE, fieldName = "job.website", message = "You must enter a valid url.")},
             stringLengthFields =
                     {@StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50", fieldName = "jobTitle", message = "The job title must be shorter than ${maxLength} characters.")}
             )
     public String save() {
 
         final Job jobFromDB = jobService.getJobForId(model.getJob().getId());
 
         if (jobFromDB == null) {
             throw new IllegalArgumentException("Job with id " + model.getJob().getId() + " does not exist.");
         }
 
         if (this.model.getJob().getIndustry() != null && this.model.getJob().getIndustry().getId() != null) {
             final Industry industry = jobService.getIndustry(jobFromDB.getIndustry().getId());
 
             if (industry == null) {
                 throw new IllegalArgumentException("Industry with id " + this.model.getJob().getIndustry().getId() + " does not exist.");
             } else {
                 jobFromDB.setIndustry(model.getJob().getIndustry());
             }
         }
 
         if (this.model.getJob().getRegion() != null && this.model.getJob().getRegion().getId() != null) {
             final Region region = jobService.getRegion(this.model.getJob().getRegion().getId());
 
             if (region == null) {
                 throw new IllegalArgumentException("Region with id " + this.model.getJob().getRegion().getId() + " does not exist.");
             } else {
                 jobFromDB.setRegion(model.getJob().getRegion());
             }
         }
 
         final String jobDescription = model.getJob().getDescription();
 
         jobFromDB.setBusinessName(model.getJob().getBusinessName());
         jobFromDB.setRegionOther(model.getJob().getRegionOther());
         jobFromDB.setJobTitle(model.getJob().getJobTitle());
         jobFromDB.setSalary(model.getJob().getSalary());
         jobFromDB.setDescription(WebUtil.stripTags(jobDescription, "<b>"));
         jobFromDB.setWebsite(model.getJob().getWebsite());
         jobFromDB.setBusinessAddress1(model.getJob().getBusinessAddress1());
         jobFromDB.setBusinessAddress2(model.getJob().getBusinessAddress2());
         jobFromDB.setBusinessCity(model.getJob().getBusinessCity());
         jobFromDB.setBusinessState(model.getJob().getBusinessState());
         jobFromDB.setBusinessZip(model.getJob().getBusinessZip());
         jobFromDB.setBusinessPhone(model.getJob().getBusinessPhone());
         jobFromDB.setBusinessEmail(model.getJob().getBusinessEmail());
         jobFromDB.setJobRestrictions(model.getJob().getJobRestrictions());
 
         if (model.getJob().getUsesMap()) {
             jobFromDB.setUsesMap(Boolean.TRUE);
             jobFromDB.setLongitude(model.getJob().getLongitude());
             jobFromDB.setLatitude(model.getJob().getLatitude());
             jobFromDB.setZoomLevel(model.getJob().getZoomLevel());
         } else {
             jobFromDB.setUsesMap(Boolean.FALSE);
             jobFromDB.setLongitude(null);
             jobFromDB.setLatitude(null);
             jobFromDB.setZoomLevel(null);
         }
 
         jobFromDB.setUpdateDate(new Date());
         jobService.updateJob(jobFromDB);
 
         super.addActionMessage(getText("class.EditJobAction.job_edit_success", ""));
 
         return SUCCESS;
 
     }
 
     public void validateSave() {
 
         if (CommongKeyIds.OTHER.getId().equals(this.model.getJob().getIndustry().getId())
                 && StringUtils.isEmpty(this.model.getJob().getIndustryOther())) {
 
             super.addFieldError("job.industryOther", "Please enter an industry.");
 
         }
         if (CommongKeyIds.OTHER.getId().equals(this.model.getJob().getRegion().getId())
                 && StringUtils.isEmpty(this.model.getJob().getRegionOther())) {
 
             super.addFieldError("job.regionOther", "Please enter a region.");
 
         }
     }
 }
