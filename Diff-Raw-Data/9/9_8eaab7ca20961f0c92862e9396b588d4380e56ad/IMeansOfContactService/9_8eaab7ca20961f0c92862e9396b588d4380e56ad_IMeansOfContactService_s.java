 package fr.cg95.cvq.service.request;
 
 import java.util.List;
 
 import fr.cg95.cvq.business.request.MeansOfContact;
 import fr.cg95.cvq.business.request.MeansOfContactEnum;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 
 /**
  * IMeansOfContactService provides methods that :
  * <br /> - fetch and configure Means of Contact
  * <br /> - manage Requester's notifications strategy
  *
  * @author rdj@zenexity.fr
  */
 public interface IMeansOfContactService {
 
     MeansOfContact getMeansOfContactByType(MeansOfContactEnum type);
 
     /**
      * Get all available Means Of Contact
      */
     List<MeansOfContact> getAvailableMeansOfContact();
 
     /**
      * Get all enabled Means Of Contact, in local authority
      */
     List<MeansOfContact> getEnabledMeansOfContact();
 
     /**
      * Get all enabled Means of Contact, for a particular adult.
      *
      * a means of contact is enabled for an adult if :
      * <ul>
      *   <li>it's enabled in local authority</li>
      *   <li>and adult has filled matching field of the Means of Contact</li>
      * </ul>
      * Example: An adult might be notified by email, if Means of Contact Email is enabled
      * and if he has provided his email (when creating home folder) .
      */
     List<MeansOfContact> getAdultEnabledMeansOfContact(Adult adult);
 
     /**
      * Same as getAdultEnabledMeansOfContact for the current ecitizen
      */
     List<MeansOfContact> getCurrentEcitizenEnabledMeansOfContact();
 
     /**
      * Enalbe a Means of Contact for a local authority
      */
     void enableMeansOfContact(MeansOfContact meansOfContact);
 
     /**
      * Disable a Means of Contact for a local authority
      * @throws CvqModelException
      * <br><br>
      * Expected business error code is :
      * <dl>
     *   <dt>unique_meansofcontact_enabled</dt>
      *     <dd>MeansOfContact can't be disabled. It is the unique enabled</dd>
      * <dl>
      */
     void disableMeansOfContact(MeansOfContact meansOfContact)
         throws CvqModelException;
 
     /**
      * Test if the given MeansOfContact supports attachment
      */
     boolean supportAttachment(MeansOfContact moc);
 
     /**
      * Notify by email.
      */
     public void notifyByEmail(String from, String to, String subject,
         String body, byte[] data, String attachmentName)
         throws CvqException;
 
     /**
      * Notify by Sms
      * <ul>
      *   <li>Throw "sms_service.not.enabled" error code if smsService is not enable</li>
      *   <li>Throw smsService error code if error in smsService</li>
      * </ul>
      */
     void notifyBySms(String to, String body)
         throws CvqException;
 
     MeansOfContact getById(Long id) throws CvqObjectNotFoundException;
 }
