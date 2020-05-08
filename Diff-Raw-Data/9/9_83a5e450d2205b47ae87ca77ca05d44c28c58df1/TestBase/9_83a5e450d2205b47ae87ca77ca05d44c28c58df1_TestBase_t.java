 package org.jboss.pressgangccms.test;
 
 /**
  * This interface contains some shared constants
  * 
  * @author Matthew Casperson
  */
 public interface TestBase
 {
 
 	// REST entity relationships:
 	// BlobConstant
 	// Category
 	// Image
 	// -- LanguageImage
 	// Project
 	// PropertyTag
 	// Role
 	// StringConstant
 	// Tag
 	// Topic
 	// -- TopicSourceUrl
 	// -- BugzillaBug
 	// TranslatedTopic
 	// -- TranslatedTopicString User
 
 	/** The environment variable that holds the REST password */
 	String RESTPASS = "REST_PASSWORD";
 	/** The environment variable that holds the REST username */
 	String RESTUSER = "REST_USERNAME";
 	/** JSON content type */
 	String JSON_CONTENT_TYPE = "application/json";
 	/** HTTP OK Response Code */
 	int HTTP_OK = 200;
 	/** HTTP Custom Error Response Code */
 	int HTTP_CUSTOM_ERROR = 500;
	/** HTTP Custom Error Response Code */
	int HTTP_BAD_REQUEST = 400;
 	/** This ID will be replaced with a marker, so the number just has to be something that won't be found anywhere other than the ID field */
 	Integer UPDATE_ID = 1234;
 }
