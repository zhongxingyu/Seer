 package com.projectmaxwell.model.validation;
 
 import com.projectmaxwell.model.RecruitInfo;
 import com.projectmaxwell.model.User;
 import com.projectmaxwell.exception.InvalidParameterException;
 import com.projectmaxwell.exception.MissingUserFieldException;
 import com.projectmaxwell.exception.NoValidatorExistsException;
import com.projectmaxwell.phiauth.service.SupportedHttpMethods;
 
 public class ModelValidator {
 
 	//Use this to disambiguate between Posts and Puts
 	public void validateUserObjectByMethod(User input, User existing, SupportedHttpMethods method){
 		//TODO: Switch code to actually use this as entry point, and then make other methods private
 		switch(method){
 		case POST:
 			validateUserCreation(input);
 			break;
 		case PUT:
 			validateUserUpdate(input, existing);
 			break;
 		default:
 			throw new NoValidatorExistsException(String.valueOf(Math.random()),
 					"There are currently no validation steps defined for a user on method '" + method + "'.");
 		}
 	}
 	
 	public void validateUserCreation(User user){
 		if(user.getUserId() != null){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"userId '" + user.getUserId() + "' is invalid.  " +
 					"userId must be null during user creation.");
 		}
 		validateUser(user);
 	}
 	
 	public void validateUser(User user) throws InvalidParameterException {
 		if(user.getUserId() != null){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"userId '" + user.getUserId() + "' is invalid.  " +
 					"userId must be null during user creation.");
 		}
 		if(user.getFirstName() == null || user.getFirstName().length() < 1){
 			throw new MissingUserFieldException(String.valueOf(Math.random()),
 			"Required field 'firstName' is null or empty");
 		}
 		if(user.getLastName() == null || user.getLastName().length() < 1){
 			throw new MissingUserFieldException(String.valueOf(Math.random()),
 			"Required field 'lastName' is null or empty");
 		}
 		if(user.getPin() != null && user.getPin() < 0){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					user.getPin() + " is not a valid value for pin.");
 		}
 		if(user.getYearGraduated() != null && user.getYearGraduated() != 0 && user.getYearGraduated() < 1906){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					user.getYearGraduated() + " is not a valid value for yearGraduated.");
 		}
 		if(user.getYearInitiated() != null && user.getYearInitiated() != 0 && user.getYearInitiated() < 1906){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					user.getYearInitiated() + " is not a valid value for yearInitiated.");
 		}
 		if(user.getPhoneNumber() != null && user.getPhoneNumber().length() != 12){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Length of a phone number must be 12.  Pattern is 'xxx-yyy-zzzz'");
 		}
 		if(user.getUserTypeId() != 5 && user.getRecruitInfo() != null){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"The recruit info object may only be set for recruit users.");
 		}
 		
 		switch(user.getUserTypeId()){
 		//For 
 		case 1:
 			validateAssociateUserCreation(user);
 		case 2:
 			validateUndergradUserCreation(user);
 			break;
 		case 3:
 			validateAlumniUserCreation(user);
 			break;
 		case 5:
 			validateRecruitUserCreation(user);
 			break;
 		default:
 			throw new InvalidParameterException(String.valueOf(Math.random()), "User Type must be valid value.");
 		}
 	}
 
 	private void validateAssociateUserCreation(User user) {
 		//DO associate specific stuff here
 		if(user.getPin() != null && user.getPin() > 0){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Associates may not have PIN numbers.  " +
 					"If this is an initiate or alumnus account, please use the appropriate user type.");
 		}
 		if(user.getYearInitiated() != null && user.getYearInitiated() > 0){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Associates may not have a value for yearInitiated.  " +
 					"If this is an alumnus or initiate account, please use the appropriate user type.");
 		}
 		validateUndergradUserCreation(user);		
 	}
 
 	private void validateRecruitUserCreation(User user) {
 		if(user.getAssociateClassId() != null){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Recruits may not be assigned to associate classes.  " +
 					"If this is an associate account, please use the appropriate user type.");
 		}
 		if(user.getPin() != null){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Recruits may not have pin numbers.  " +
 					"If this is an initiate account, please use the appropriate user type.");
 		}
 		if(user.getYearInitiated() != null){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Recruits may not have a value for yearInitiated.  " +
 					"If this is an alumnus or initiate account, please use the appropriate user type.");
 		}
 		if(user.getYearGraduated() != null){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Recruits may not have a value for yearGraduated.  " +
 					"If this is an alumnus account, please use the appropriate user type.");
 		}
 		if(user.getChapterId() > 0){
 			throw new InvalidParameterException(String.valueOf(Math.random()), 
 					"Recruits may not have a value for chapter.  " +
 					"If this is an alumnus account, please use the appropriate user type.");	
 		}
 	}
 
 	private void validateAlumniUserCreation(User user) {
 		//No validation specific to alumni?
 		//throw new InvalidParameterException(String.valueOf(Math.random()), "Creation of alumni is not yet supported.");
 	}
 
 	public void validateUndergradUserCreation(User user){
 		if(user.getAssociateClassId() == null || user.getAssociateClassId() < 1){
 			throw new MissingUserFieldException(String.valueOf(Math.random()),
 			"Required field 'associateClassId' did not map to a valid associate class");	
 		}
 /*		if(user.getChapterId() > 0){
 			throw new InvalidParameterException(String.valueOf(Math.random()), 
 					"Undergrads may not have a value for chapter.  " +
 					"If this is an alumnus account, please use the appropriate user type.");	
 		}*/
 		if(user.getYearGraduated() != null && user.getYearGraduated() != 0){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Undergrads may not have a value for yearGraduated.  " +
 					"If this is an alumnus account, please use the appropriate user type.");
 		}
 		if(user.getRecruitInfo() != null){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"Current members may not have a value for recruit info.  " +
 					"If this is a recruit account, please use the appropriate user type.");
 		}
 	}
 	
 	/**
 	 * This method actually allows for comparison of old and new fields
 	 * However this isn't being used presently, any field is modifiable
 	 * @param newUserData
 	 * @param oldUserData
 	 */
 	public void validateUserUpdate(User newUserData, User oldUserData){
 		if(newUserData.getUserId() == null || newUserData.getUserId() < 1){
 			throw new MissingUserFieldException(String.valueOf(Math.random()),
 					"Required field 'userId' is null or empty");
 		}
 		if(newUserData.getUserId() != oldUserData.getUserId()){
 			throw new InvalidParameterException(String.valueOf(Math.random()),
 					"userId '" + newUserData.getUserId() + "' is invalid.  " +
 					"userId in request body must match userId in URI.");
 		}
 		if(newUserData.getFirstName() == null || newUserData.getFirstName().length() < 1){
 			throw new MissingUserFieldException(String.valueOf(Math.random()),
 			"Required field 'firstName' is null or empty");
 		}
 	}
 
 	public void validateRecruitInfo(RecruitInfo recruitInfo) {
 		if(recruitInfo.getRecruitEngagementLevelId() == null || recruitInfo.getRecruitEngagementLevelId() < 1){
 			throw new MissingUserFieldException(String.valueOf(Math.random()),
 					"Required field 'recruitInfo.recruitEngagementLevelId' is nullor invalid");
 		}
 		if(recruitInfo.getRecruitSourceId() == null || recruitInfo.getRecruitSourceId() < 1){
 			throw new MissingUserFieldException(String.valueOf(Math.random()),
 					"Required field 'recruitInfo.recruitSourceId' is nullor invalid");
 		}
 	}
 
 }
