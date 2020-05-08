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
 import org.apache.struts2.interceptor.validation.SkipValidation;
import org.jrecruiter.common.GoogleMapsUtils;
 import org.jrecruiter.common.Constants.CommongKeyIds;
 import org.jrecruiter.common.Constants.JobStatus;
 import org.jrecruiter.common.Constants.OfferedBy;
 import org.jrecruiter.model.Industry;
import org.jrecruiter.model.Job;
 import org.jrecruiter.model.Region;
 import org.jrecruiter.web.JobForm;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.opensymphony.xwork2.ModelDriven;
 import com.opensymphony.xwork2.Preparable;
 import com.opensymphony.xwork2.validator.annotations.EmailValidator;
 import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
 import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
 import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
 import com.opensymphony.xwork2.validator.annotations.UrlValidator;
 import com.opensymphony.xwork2.validator.annotations.Validations;
 import com.opensymphony.xwork2.validator.annotations.ValidatorType;
 
 /**
  * Add a job posting.
  *
  * @author Gunnar Hillert
  * @version $Id$
  *
  */
 @Result(name="success", location="index", type="redirectAction")
 public class AddJobAction extends JobBaseAction implements Preparable, ModelDriven<JobForm> {
 
     /** serialVersionUID. */
     private static final long serialVersionUID = 4614516114027504626L;
 
     /**
      * Logger Declaration.
      */
     private final static Logger LOGGER = LoggerFactory.getLogger(AddJobAction.class);
 
     /**
      * Initialize the form.
      */
     @Override
     @SkipValidation
     public String execute() {

        if (this.model.getJob() == null) {
            this.model.setJob(new Job());
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
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "model.job.industry.id",   message = "You must select an industry."),
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "model.job.offeredBy",     message = "Please select whether you are the hiring company or a recruiter."),
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "model.job.region.id",     message = "Please select a region."),
                     @RequiredFieldValidator(type = ValidatorType.SIMPLE, fieldName = "model.job.usesMap",       message = "Do you want to use Google Maps?")
                     },
             requiredStrings = {
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "model.job.jobTitle",      message = "You must enter a job title."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "model.job.businessEmail", message = "You must enter an Email Address."),
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "model.job.description",   message = "You must enter a job description.")
                     },
             emails =
                     { @EmailValidator(type = ValidatorType.SIMPLE,            fieldName = "model.job.businessEmail",      message = "You must enter a valid email.")},
             urls =
                     { @UrlValidator(type = ValidatorType.SIMPLE,              fieldName = "model.job.website",            message = "You must enter a valid url.")},
             stringLengthFields =
                     {@StringLengthFieldValidator(type = ValidatorType.SIMPLE, trim = true, maxLength = "50", fieldName = "model.job.jobTitle", message = "The job title must be shorter than ${maxLength} characters.")}
             )
 
     public String save() {
 
         LOGGER.debug("Adding Job...");
 
         final Region region = jobService.getRegion(this.model.getJob().getRegion().getId());
         final Industry industry = jobService.getIndustry(this.model.getJob().getIndustry().getId());
 
         this.model.getJob().setRegion(region);
         this.model.getJob().setIndustry(industry);
 
         this.model.getJob().setStatus(JobStatus.ACTIVE);
         this.model.getJob().setUser(super.getLoggedInUser());
         this.model.getJob().setRegistrationDate(new Date());
         this.model.getJob().setUpdateDate(new Date());
 
         jobService.addJobAndSendToMailingList(this.model.getJob());
 
         super.addActionMessage(getText("class.AddJobAction.job_add_success"));
 
         return SUCCESS;
     }
 
     public void validateSave() {
 
         if (OfferedBy.UNDEFINED.equals(this.model.getJob().getOfferedBy())) {
             super.addFieldError("model.job.offeredBy", "Please select who is offering the job.");
         }
 
         if (CommongKeyIds.UNDEFINED.getId().equals(this.model.getJob().getIndustry().getId())) {
             super.addFieldError("model.job.industry.id", "Please specify an industry.");
         } else if (CommongKeyIds.OTHER.getId().equals(this.model.getJob().getIndustry().getId())) {
             if (StringUtils.isBlank(this.model.getJob().getIndustryOther())) {
                 super.addFieldError("model.job.industryOther", "Please specify an industry.");
             }
         }
 
         if (CommongKeyIds.UNDEFINED.getId().equals(this.model.getJob().getRegion().getId())) {
             super.addFieldError("model.job.region.id", "Please specify a region.");
         } else if (CommongKeyIds.OTHER.getId().equals(this.model.getJob().getRegion().getId())) {
             if (StringUtils.isBlank(this.model.getJob().getRegionOther())) {
                 super.addFieldError("model.job.regionOther", "Please specify a region.");
             }
         }
 
         if (this.model.getJob().getUsesMap()) {
             if (this.model.getJob().getLongitude() == null) {
                 super.addFieldError("model.job.longitude", "Please specify a longitude.");
             }
             if (this.model.getJob().getLatitude() == null) {
                 super.addFieldError("model.job.latitude", "Please specify a latitude.");
             }
             if (this.model.getJob().getZoomLevel() == null) {
                 super.addFieldError("model.job.zoomLevel", "Please specify a zoom level.");
             }
         }
 
     }
 }
