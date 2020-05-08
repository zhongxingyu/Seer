 package org.goodreturn.borrowers.portlet;
 
 import java.util.List;
 
 import org.goodreturn.model.Story;
 import org.goodreturn.service.StoryLocalServiceUtil;
 import org.goodreturn.service.persistence.TempBlPK;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.util.Validator;
 
 /**
  * Validator class which validates the Story object in numerous situations.
  * 
  * @author Kyle Pink
  */
 public class StoryValidator {
 
 	/**
 	 * Validates the story object.
 	 * 
 	 * @param story - Story object to be validated.
 	 * @param errors - List which allows errors to be collected and returned.
 	 * @return boolean to indicate if the story is valid.
 	 */
 	public static boolean validateStory(Story story, List<String> errors) {
 		//TODO add error entries for each of the errors.
 		boolean storyValid = true;
 		//null check
 		if (story == null) {
 			errors.add("story-null-error");
 			return false;
 		}
 
 		//Valid story types.
 		if (!(story.getStory_Type().equals("initial") || story.getStory_Type().equals("final"))) {
 			errors.add("story-type-error");
 			storyValid = false;
 		}
		
		//Validates story text not being 1000(max).
		if (story.getStory_Text().length() > 999) {
			errors.add("story-text-error");
			storyValid = false;
		}
 
 		//non null borrower_loan_Id
 		if (story.getAbacus_Borrower_Loan_Id() <= 0) {
 			errors.add("story-loan-id-error");
 			storyValid = false;
 		}
 
 		//URL
 		//Only validates for URL if videoUrl exists.
 		if (!story.getVideo_Url().equals("")) {
 			//If pointless white space remove it.
 			if (story.getVideo_Url().trim().equals("")) {
 				story.setVideo_Url("");
 				
 			} else if (!Validator.isUrl(story.getVideo_Url())) {
 				errors.add("story-vid-url-error");
 				storyValid = false;
 			}
 		}
 		
 		return storyValid;
 	}
 }
