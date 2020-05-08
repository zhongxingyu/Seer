 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 
 package org.jasig.schedassist.web.owner.relationships;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.jasig.schedassist.ICalendarAccountDao;
 import org.jasig.schedassist.model.ICalendarAccount;
 import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 /**
  * Form backing object for {@link CreateAdhocRelationshipFormController}.
  *  
  * @author Nicholas Blair, nblair@doit.wisc.edu
  * @version $Id: ModifyAdhocRelationshipFormBackingObjectValidator.java 2521 2010-09-10 16:04:57Z npblair $
  */
 public class ModifyAdhocRelationshipFormBackingObjectValidator implements Validator {
 
 	private static final String VALID_USERNAME_REGEX = "[\\w\\d\\.\\-\\_@]+";
 	private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile(VALID_USERNAME_REGEX);
 
 	private ICalendarAccountDao calendarAccountDao;
 	/**
 	 * @param calendarAccountDao
 	 */
 	public ModifyAdhocRelationshipFormBackingObjectValidator(
 			ICalendarAccountDao calendarAccountDao) {
 		this.calendarAccountDao = calendarAccountDao;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
 	 */
 	public boolean supports(Class<?> clazz) {
 		return ModifyAdhocRelationshipFormBackingObject.class.equals(clazz);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
 	 */
 	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "visitorUsername", "visitorUsername", "No person matching your input was found.");
		ValidationUtils.rejectIfEmpty(errors, "relationship", "relationship.empty", "You must specify a description for your relationship.");

 		ModifyAdhocRelationshipFormBackingObject fbo = (ModifyAdhocRelationshipFormBackingObject) target;
 		Matcher m = VALID_USERNAME_PATTERN.matcher(fbo.getVisitorUsername());
 		if(StringUtils.isBlank(fbo.getVisitorUsername())) {
			errors.rejectValue("visitorUsername", "visitorUsername", "You must specify a NetID (field was empty).");
 		} else if(!m.matches()) {
			errors.rejectValue("visitorUsername", "visitorUsername", "Invalid NetID");
 		} else {
 			ICalendarAccount account = calendarAccountDao.getCalendarAccount(fbo.getVisitorUsername());
 			if(null == account) {
				errors.rejectValue("visitorUsername", "visitor.notfound", "Person not found or not eligible for WiscCal.");
 			}
 		}
 		if(StringUtils.isBlank(fbo.getRelationship())) {
 			errors.rejectValue("relationship", "relationship.empty", "You must specify a description for your relationship.");
 		} else if (fbo.getRelationship().length() > 64) {
 			errors.rejectValue("relationship", "relationship.toolong", "Relationship description is too long, please shorten to less than 64 characters.");
 		}
 	}
 
 }
