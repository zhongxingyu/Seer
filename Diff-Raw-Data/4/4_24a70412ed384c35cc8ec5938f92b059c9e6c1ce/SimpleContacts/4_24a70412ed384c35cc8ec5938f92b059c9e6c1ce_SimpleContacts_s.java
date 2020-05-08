 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.model.CannedAccessControlList;
 import com.amazonaws.services.s3.model.DeleteObjectRequest;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.amazonaws.services.simpledb.AmazonSimpleDB;
 import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
 import com.amazonaws.services.simpledb.model.Attribute;
 import com.amazonaws.services.simpledb.model.CreateDomainRequest;
 import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
 import com.amazonaws.services.simpledb.model.Item;
 import com.amazonaws.services.simpledb.model.ListDomainsResult;
 import com.amazonaws.services.simpledb.model.PutAttributesRequest;
 import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
 import com.amazonaws.services.simpledb.model.SelectRequest;
 import com.amazonaws.services.sns.AmazonSNS;
 import com.amazonaws.services.sns.AmazonSNSClient;
 import com.amazonaws.services.sns.model.PublishRequest;
 
 
 public class SimpleContacts {
 	private static final String LINE_SEPARATOR = "------------------";
 	
 	//SimpleDB keys
 	private static final String CONTACT_DOMAIN_TITLE = "cspp51083.samuelh.simplecontacts";
 	private static final String FIRST_KEY = "First";
 	private static final String LAST_KEY = "Last";
 	private static final String PHONE_KEY = "Phone";
 	private static final String EMAIL_KEY = "Email";
 	private static final String STREET_KEY = "Street";
 	private static final String CITY_KEY = "City";
 	private static final String STATE_KEY = "State";
 	private static final String ZIP_KEY = "Zip";
 	private static final String TAG_KEY = "Tag";
 	private static final String BIRTHDAY_KEY = "Birthday";
 	private static final String UPDATE_TOPIC_ARN = "arn:aws:sns:us-east-1:875425895862:51083-updated";
 	
 	private static Scanner scn = new Scanner(System.in);
 	private static AmazonSimpleDB simpleDBClient;
 	private static AmazonSNS snsClient;
 	private static AmazonS3 s3client;
 	private static String selectedContactId;
 	private static Random random = new Random();
 	
 	public static void main(String[] args) {
 		//welcome the user and give them a chance to edit environment variables before continuing
 		System.out.println("Welcome to the Simple Contact Manager");
 		System.out.println(LINE_SEPARATOR);
 		
 		//get a SimpleDBClient
 		simpleDBClient = getSimpleDBClient();
 		
 		//get a Simple Notification Service (SNS) client
 		snsClient = getSNSClient();
 		
 		//get an S3 client
 		s3client = S3ContactManager.getS3Client();
 		
 		//ensure that the MySimpleContacts domain exists for this user
 		ensureDomainExists();
         
 		//formatting
 		System.out.println(LINE_SEPARATOR);
 		
 		while (true) {
 			System.out.println("\nPlease select an option below by entering the corresponding number and pressing enter\n");
 			System.out.println("0 Exit the program");
 			System.out.println("1 List contacts");
 			System.out.println("2 Select contact");
 			System.out.println("3 Retrieve details about selected contact");
 			System.out.println("4 Edit details about selected contact");
 			System.out.println("5 Create new contact");
 			System.out.println("6 Search contacts");
 			System.out.println("7 Delete Contact");
 			
 			//call the operation corresponding to the user's choice
 			handleUserChoice(scn.nextLine());
 		}
 	}
 	
 	/********************************************************************
 	* Get SimpleDB client using the user's credentials
 	*********************************************************************/
 	private static AmazonSimpleDB getSimpleDBClient() {
 		AWSCredentials myCredentials;
 		AmazonSimpleDB simpleDBClient = null;
 		
 		try {
 			//get credentials from default provider chain 
 			myCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
 			simpleDBClient = new AmazonSimpleDBClient(myCredentials); 
 		} catch (Exception ex) {
 			System.out.println("There was a problem reading your credentials.");
 			System.exit(0);
 		}
 		
 		return simpleDBClient;
 	}
 	
 	/********************************************************************
 	* Get SNS client using the user's credentials
 	*********************************************************************/
 	private static AmazonSNS getSNSClient() {
 		AWSCredentials myCredentials;
 		AmazonSNS snsClient = null;
 		
 		try {
 			//get credentials from default provider chain
 			myCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
 			snsClient = new AmazonSNSClient(myCredentials);
 		} catch (Exception ex) {
 			System.out.println("There was a problem reading your credentials.");
 			System.exit(0);
 		}
 		
 		return snsClient;
 	}
 
 	/********************************************************************
 	* Make sure the contacts domain exists for our user on SimpleDB.
 	* If not, create it.
 	*********************************************************************/
 	private static void ensureDomainExists() {
 		//Although createDomain() is idemponent, the documentation warns that it
 		//could take up to 10 seconds to create the domain, so we should only call createDomain() once per user
 		boolean domainExists = false;
 		ListDomainsResult domains = simpleDBClient.listDomains();
 	
 		for (String domainName : domains.getDomainNames()) {
 			if (domainName.equals(CONTACT_DOMAIN_TITLE)) {
 				domainExists = true;
 				break;
 			}
 		}
 		
 		if (!domainExists) {
 			try {
 				simpleDBClient.createDomain(new CreateDomainRequest(CONTACT_DOMAIN_TITLE));
 			} catch (Exception ex) {
 				System.out.println(ex.getMessage());
 			}
 		}
 	}
 
 	/********************************************************************
 	* Handle user's input for contact manipulation options
 	*********************************************************************/
 	private static void handleUserChoice(String userInput) {
 		//initialize choice to an invalid option
 		int choice = -1;
 		
 		try {
 			//get the user's choice from the console
 			choice = Integer.parseInt(userInput);
 		} catch (NumberFormatException ex) {
 			System.out.println(userInput + " is not a valid option. Please enter one of the numbers given");
 			return;
 		}
 		
 		//handle the user's choice
 		switch(choice) {
 		case 0:
 			//terminate the program
 			System.out.println("\nThank you for using Simple Contact Manager. Goodbye.");
 			System.exit(0);
 			break;
 		case 1:
 			//list all contacts
 			listContacts();
 			break;
 		case 2:
 			//select a contact
 			selectContact();
 			break;
 		case 3:
 			//retrieve details about selected contact
 			retrieveContactDetails();
 			break;
 		case 4:
 			//edit details about a selected contact
 			editContactDetails();
 			break;
 		case 5:
 			//create a new contact
 			createNewContact();
 			break;
 		case 6:
 			//search contacts
 			searchContacts();
 			break;
 		case 7:
 			//delete selected contact
 			deleteContact();
 		default:
 			System.out.println(choice + " is not a valid option. Please enter one of the numbers given");
 		}
 	}
 
 	/********************************************************************
 	* List the contacts in the user's contact database
 	*********************************************************************/
 	private static void listContacts() {
         // Select all contacts
         String selectExpression = "select * from `" + CONTACT_DOMAIN_TITLE + "`";
         
         List<Item> allContacts = simpleDBClient.select(new SelectRequest(selectExpression)).getItems();
         
         if (allContacts.size() > 0) {
         	System.out.println("All contacts:\n");
         	displayContacts(allContacts);
         } else {
         	System.out.println("No contacts have been added yet\n");
         }
 	}
 	
 
 	/********************************************************************
 	* Print a collection of contacts
 	*********************************************************************/
 	private static void displayContacts(List<Item> contacts) {
         String first = "",
         		last = "";
         
         for (Item item : contacts) {
             System.out.println("Contact ID: " + item.getName());
             for (Attribute attribute : item.getAttributes()) {
             	if (attribute.getName().equals(FIRST_KEY)) {
             		first = attribute.getValue();
             	} else if (attribute.getName().equals(LAST_KEY)) {
             		last = attribute.getValue();
             	}
             }
             // print the contact's name
         	System.out.println("Name: " + first + " " + last + "\n");
         	
         	// reset first and last
         	first = "";
         	last = "";
         }
 		
 	}
 
 	/********************************************************************
 	* Let the user select a contact
 	*********************************************************************/
 	private static void selectContact() {
 		System.out.println("Please enter the Contact ID of the contact you would like to select:");
 		selectedContactId = scn.nextLine();
 	}
 	
 	/********************************************************************
 	* Retrieve contact info for the selected contact
 	*********************************************************************/
 	private static void retrieveContactDetails() {
 		// check that a contact has been selected
 		if (selectedContactId == null) {
 			System.out.println("Please select a contact first using this option");
 			return;
 		}
 		
 		// build query
         String selectExpression = "select * from `" + CONTACT_DOMAIN_TITLE + "` where itemName() = '" + selectedContactId + "'";
         
         // execute query
         for (Item item : simpleDBClient.select(new SelectRequest(selectExpression)).getItems()) {
             System.out.println("Contact ID: " + item.getName());
             for (Attribute attribute : item.getAttributes()) {
             	System.out.println(attribute.getName() + ": " + attribute.getValue());
             }
             System.out.println();
         }
 	}
 	
 	/********************************************************************
 	* Let the user edit a contact's information
 	*********************************************************************/
 	private static void editContactDetails() {
 		
 		// check that a contact has been selected
 		if (selectedContactId == null) {
 			System.out.println("Please select a contact first using option 2");
 			return;
 		}
 
 		System.out.println("\nEdit/Delete contact information for contact " + selectedContactId + ":\n");
 
 		//collection of all possible attributes. used to let user add values for keys that this contact does not have
 		List<String> unusedAttributes = new ArrayList<String>(Arrays.asList(FIRST_KEY, LAST_KEY, PHONE_KEY, 
 				EMAIL_KEY, STREET_KEY, CITY_KEY, STATE_KEY, ZIP_KEY, TAG_KEY, BIRTHDAY_KEY));
 		
 		//collections of attributes to update/delete
 		Collection<ReplaceableAttribute> updateAttributes = new ArrayList<ReplaceableAttribute>();
 		Collection<Attribute> deleteAttributes = new ArrayList<Attribute>();
 		
 		// build query
         String selectExpression = "select * from `" + CONTACT_DOMAIN_TITLE + "` where itemName() = '" + selectedContactId + "'";
         
         //initialize modification option to an invalid option
         int modifyOption = -1;
         
         //initialize the new value for an attribute and first/last names
         String newValue = ""
         		, first = ""
         		, last = "";
         
         //let user review/edit existing attributes
         System.out.println("Step 1: Review/Edit/Delete existing attributes\n");
         for (Item item : simpleDBClient.select(new SelectRequest(selectExpression)).getItems()) {
             for (Attribute attribute : item.getAttributes()) {
             	//remove this attribute name from the list of unused attributes
             	unusedAttributes.remove(attribute.getName());
             	
             	//prompt user for a modification option (repeat if the user enters an invalid option)
             	while (modifyOption == -1) {
             		//show the attribute
 	            	System.out.println("\n" + attribute.getName() + ": " + attribute.getValue());
 	            	
 	            	//prompt the user for a modification option
 	            	System.out.println("Enter 0 to skip, 1 to edit, or 2 to delete this attribute");
 	            	
 	            	try {
 	            		modifyOption = Integer.valueOf(scn.nextLine());
 	            	} catch (NumberFormatException ex) {
 	            		System.out.println("Invalid entry. Please enter 0, 1, or 2");
 	            	}
             	}
             	
             	//handle the user's choice
         		switch(modifyOption) {
         		case 0:
         			//skip this attribute
         			if (attribute.getName().equals(FIRST_KEY)) first = attribute.getValue();
         			if (attribute.getName().equals(LAST_KEY)) last = attribute.getValue();
         			break;
         		case 1:
         			//modify this attribute
         			System.out.println("Please enter a new value for this attribute:");
         			
         			//provide instructions for keys requiring special formatting
         			if (attribute.getName().equals(TAG_KEY)) { 
         				System.out.println("List Tags surrounded by [ ], eg, [cool][smart][awesome]");
         			} else if (attribute.getName().equals(BIRTHDAY_KEY)) {
         				System.out.println("Birthday must be in YYYY-MM-DD format");
         			}
         			
         			//get the new value
         			newValue = scn.nextLine();
         			
         			if (attribute.getName().equals(FIRST_KEY)) first = newValue;
         			if (attribute.getName().equals(LAST_KEY)) last = newValue;
         			
         			//add the old attribute to attributes needing deletion
         			deleteAttributes.add(attribute);
         			
         			//add the new value to attributes to update
         			updateAttributes.add(new ReplaceableAttribute(attribute.getName(), newValue, true));
 
         			break;
         		case 2:
         			//delete this attribute
         			deleteAttributes.add(attribute);
         			
         			//put this attribute name back in unused attributes if it's not currently there
         			if (!unusedAttributes.contains(attribute.getName())) unusedAttributes.add(attribute.getName());
         			
         			break;
         		default:
         			System.out.println(modifyOption + " is not a valid option. Please enter one of the numbers given");
         		}
             	
         		//reset modifyOption
         		modifyOption = -1;
             }
             System.out.println();
         }
         
         System.out.println("Step 2: Input values for unused attributes (or skip optional attributes)\n");
         for (String attributeName : unusedAttributes) {
         	System.out.println("Press enter to skip or input a value for " + attributeName);
         	newValue = scn.nextLine();
         	
         	//add the new attribute if one was entered
         	if (newValue.length() > 0) {
         		updateAttributes.add(new ReplaceableAttribute(attributeName, newValue, true));
         	} else if (attributeName.equals(FIRST_KEY)) {
         		//ensure that a first name exists
         		while (newValue.length() == 0) {
         			System.out.println("First name is required. Please enter a value for first name:");
         			newValue = scn.nextLine();
         		}
  
         		//add the new first name value
         		first = newValue;
     			updateAttributes.add(new ReplaceableAttribute(FIRST_KEY, newValue, true));
         	} else if (attributeName.equals(LAST_KEY)) {
         		last = " ";
         	} else {
         		//optional attribute, just skip it
         	}
         }
 		
         //perform necessary updates
         if (deleteAttributes.size() > 0 || updateAttributes.size() > 0) {
 	        try {
 	        	System.out.println("Performing updates. Please wait...");
 	        	//delete attributes
 	            if (deleteAttributes.size() > 0) simpleDBClient.deleteAttributes(new DeleteAttributesRequest().withDomainName(CONTACT_DOMAIN_TITLE).withItemName(selectedContactId).withAttributes(deleteAttributes));
 	    		
 	    		//add attributes
 	    		if (updateAttributes.size() > 0) simpleDBClient.putAttributes(new PutAttributesRequest().withDomainName(CONTACT_DOMAIN_TITLE).withItemName(selectedContactId).withAttributes(updateAttributes));
 	    		
 	    		//succesfully applied updates
 	    		System.out.println("Successfully updated contact in SimpleDB.");
 	    		
 	    		//update the contact's s3 object
 	    		updateContactInS3();
 	    		
 	    		// publish sns notification
 	    		String message = "{  \"updateType\" : \"edit\", \"itemId\" : " + "\"" + selectedContactId + "\", \"first\" : " + "\"" + first + "\", \"last\" : \"" + last + "\", \"url\" : \"" + "https://s3.amazonaws.com/" + CONTACT_DOMAIN_TITLE + "/" + first + last + selectedContactId + ".html\"}" ;
 	    		System.out.println(message);
 	    		snsClient.publish(new PublishRequest(UPDATE_TOPIC_ARN, message));
 	    		
 	        } catch (Exception ex) {
 	        	System.out.println("There was a problem performing updates. Please review this contact's details and try again.");
 	        }    
         } else {
         	System.out.println("No changes to be made.");
         }
 	}
 
 	/********************************************************************
 	* Create a new contact in SimpleDB/S3
 	*********************************************************************/
 	private static void createNewContact() {
 		String first = "",
 				last = "",
 				phoneNumber = "",
 				emailAddress = "",
 				streetAddress = "",
 				city = "",
 				state = "",
 				zip = "",
 				tags = "",
 				birthday = "";
 		
 		//lists of phone numbers/labels and email addresses/labels
 		List<String> phoneRecords = new ArrayList<String>(),
 			emailRecords = new ArrayList<String>();
 			
 		//get the contact's first name
 		do {
 			System.out.println("Enter the contact's first name (mandatory):");
 			first = scn.nextLine();
 		} while (first.length() == 0);
 		
 		//get the contact's last name
 		System.out.println("Enter the contact's last name (optional - just press enter to skip):");
 		last = scn.nextLine();
 		
 		//collect phone numbers/labels
 		do {
 			//reset variable
 			phoneNumber = "";
 			
 			//prompt user to input a phone number/label
 			System.out.print("Enter");
 			if (phoneRecords.size() == 0) {
 				System.out.print(" a ");
 			} else {
 				System.out.print(" another ");
 			}
 			System.out.println("phone number for this contact with a label (eg. 773-202-5862, Work)");
 			System.out.println("(phone numbers are optional - just press enter to skip):");
 			
 			//get the input
 			phoneNumber = scn.nextLine();
 			
 			//add this phone record to our list if one was entered
 			if (phoneNumber.length() > 0) phoneRecords.add(phoneNumber);
 			
 		} while (phoneNumber.length() > 0);
 		
 		//collect email addresses/labels
 		do {
 			//reset variables
 			emailAddress = "";
 			
 			//prompt the user to input an email address
 			System.out.print("Enter");
 			if (emailRecords.size() == 0) {
 				System.out.print(" an ");
 			} else {
 				System.out.print(" another ");
 			}
 			System.out.println("email address for this contact with a label (eg. samuelh@henrycorp.com, Work)");
 			System.out.println("(email addresses are optional - just press enter to skip):");
 			
 			//get the input
 			emailAddress = scn.nextLine();
 			
 			//add this email address to our list if one was entered
 			if (emailAddress.length() > 0) emailRecords.add(emailAddress);
 			
 		} while (emailAddress.length() > 0);
 		
 		//get contact's mailing address
 		System.out.println("Enter the street address for this contact (optional - just press enter to skip):");
 		streetAddress = scn.nextLine();
 		
 		//get contact's city
 		System.out.println("Enter the city for this contact (optional - just press enter to skip):");
 		city = scn.nextLine();
 		
 		//get contact's city
 		System.out.println("Enter the state for this contact (optional - just press enter to skip):");
 		state = scn.nextLine();
 		
 		//get contact's zip code
 		System.out.println("Enter the 5 digit zip code for this contact (optional - just press enter to skip):");
 		zip = scn.nextLine();
 		
 		//get contact's birthday
 		System.out.println("Enter the birthday (must be in YYYY-MM-DD format) for this contact (optional - just press enter to skip):");
 		birthday = scn.nextLine();
 
 		System.out.println("Enter tag(s) for this user separated by [ ], eg, [cool][smart][awesome] (optional - just press enter to skip):");
 		tags = scn.nextLine();
 		
 
 		//create the contact's database record
 		int itemId = createContactRecordInSimpleDB(first, last, phoneRecords, emailRecords, streetAddress, city, state, zip, tags, birthday);
 		if (itemId > -1) {
 			//create the contact's S3 page if the record was created correctly
 			try {
 				createContactPageInS3(itemId, first, last, phoneRecords, emailRecords, streetAddress, city, state, zip, tags, birthday);
 			} catch (Exception ex) {
 				System.out.println("There was a problem creating the webpage for this contact.");
 			}
 		}
 	}
 	
 	/********************************************************************
 	* Create a contact's SimpleDB record
 	*********************************************************************/
 	private static int createContactRecordInSimpleDB(String first,
 			String last, List<String> phoneRecords,
 			List<String> emailRecords, String streetAddress, String city,
 			String state, String zip, String tags, String birthday) {
 		
 		//make sure a first name was entered
 		if (first.length() == 0) {
 			System.out.println("There was a problem creating this contact. First name is required. Please try again.");
 			return -1;
 		}
 		
 		//list of attributes to create with
 		Collection<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
 		
 		//add first name attribute
 		attributes.add(new ReplaceableAttribute(FIRST_KEY, first, true));
 		
 		//add last name attribute if one was entered
 		if (last.length() > 0)  attributes.add(new ReplaceableAttribute(LAST_KEY, last, true)) ;
 		
 		//add phone number attributes if entered
 		for (String phoneRecord : phoneRecords) {
 			attributes.add(new ReplaceableAttribute(PHONE_KEY, phoneRecord, true));
 		}
 		
 		//add email address attributes if entered
 		for (String emailRecord : emailRecords) {
 			attributes.add(new ReplaceableAttribute(EMAIL_KEY, emailRecord, true));
 		}
 		
 		//add street address attribute if it was entered
 		if (streetAddress.length() > 0) attributes.add(new ReplaceableAttribute(STREET_KEY, streetAddress, true));
 		
 		//add city attribute if it was entered
 		if (city.length() > 0) attributes.add(new ReplaceableAttribute(CITY_KEY, city, true));
 		
 		//add zip attribute if it was entered
 		if (zip.length() > 0) attributes.add(new ReplaceableAttribute(ZIP_KEY, zip, true));
 		
 		//add state attribute if it was entered
 		if (state.length() > 0) attributes.add(new ReplaceableAttribute(STATE_KEY, state, true));
 		
 		//add tag attributes that were entered
 		if (tags.length() > 0) attributes.add(new ReplaceableAttribute(TAG_KEY, tags, true));
 		
 		//add birthday attribute if it was entered
 		if (birthday.length() > 0) attributes.add(new ReplaceableAttribute(BIRTHDAY_KEY, birthday, true));
 		
 		try {
 			//create the contact with a random id and the input attributes
 			int itemId = random.nextInt(10000);
 			simpleDBClient.putAttributes(new PutAttributesRequest().withItemName(String.valueOf(itemId)).withAttributes(attributes).withDomainName(CONTACT_DOMAIN_TITLE));
 			System.out.println("Successfully created new contact: " + first + " " + last);
 			return itemId;
 		} catch (Exception ex) {
 			System.out.println("There was a problem adding your contact to SimpleDB, please try again.");
 		}
 		
 		return -1;
 	}
 	
 	/********************************************************************
 	* Create a contact's page in S3
 	 * @throws Exception 
 	*********************************************************************/
 	private static void createContactPageInS3(int itemId, String first, String last,
 			List<String> phoneRecords, List<String> emailRecords,
 			String streetAddress, String city, String state, String zip,
 			String tags, String birthday) throws Exception {
 		
 		String htmlTemplateBeginning = "<!DOCTYPE html><html><body><table>";
 		String htmlTemplateEnding = "</body></html>";
 		String htmlHeaderRow = "<tr>";
 		String htmlDetailRow = "<tr>";
 
 		//build the document
 		//add a table cell for the first name
 		if (first.length() > 0) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + FIRST_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + first + "</td>";
 		} else {
 			System.out.println("ERROR: First name is mandatory. Please try again.");
 			return;
 		}
 		
 		//add a table cell for the last name if it was entered
 		if (last.length() > 0) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + LAST_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + last + "</td>";
 		}
 		
 		//add a table cell for each phone number that was entered
 		for (String phoneRecord : phoneRecords) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + PHONE_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + phoneRecord + "</td>";
 		}
 		
 		//add a table cell for each email address that was entered
 		for (String emailRecord : emailRecords) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + EMAIL_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + emailRecord + "</td>";
 		}
 		
 		//add a table cell for the street address if it was entered
 		if (streetAddress.length() > 0) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + STREET_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + streetAddress + "</td>";
 		}
 		
 		//add a table cell for the city if it was entered
 		if (city.length() > 0) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + CITY_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + city + "</td>";
 		}
 		
 		//add a table cell for the state if it was entered
 		if (state.length() > 0) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + STATE_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + state + "</td>";
 		}
 		
 		//add a table cell for the zip if it was entered
 		if (zip.length() > 0) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + ZIP_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + zip + "</td>";
 		}
 		
 		//add a table cell for the tags that were entered
 		if (tags.length() > 0) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + TAG_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + tags + "</td>";
 		}
 		
 		//add a table cell for the birthday if it was entered
 		if (birthday.length() > 0) {
 			htmlHeaderRow = htmlHeaderRow + "<th>" + BIRTHDAY_KEY + "</th>";
 			htmlDetailRow = htmlDetailRow + "<td>" + birthday + "</td>";
 		}
 		
 		//terminate the rows
 		htmlHeaderRow = htmlHeaderRow + "</tr>";
 		htmlDetailRow = htmlDetailRow + "</tr>";
 		
 		String newDocument = htmlTemplateBeginning + htmlHeaderRow + htmlDetailRow + htmlTemplateEnding;
 		
 		String s3bucketName = "cspp51083.samuelh.simplecontacts";
 
 		//concatenate the file name (append a random number (not same as contact id) in case 
 		//multiple contacts are created with the same first name or contact edited many times) so the S3 object won't be overwritten
 		String fileName = first + last + String.valueOf(itemId) + ".html";
 		
 		//create the new HTML file
 		File contactDocument = new File (fileName);
 		FileWriter fw;
 		fw = new FileWriter(contactDocument);
 		fw.write(newDocument);
 		fw.close();
 		
 		//store HTML file with public accessibility in S3
 		s3client.putObject(new PutObjectRequest(s3bucketName, fileName, contactDocument).withCannedAcl(CannedAccessControlList.PublicRead));
 		
 		//publish SNS message
 		String message = "{  \"updateType\" : \"create\", \"itemId\" : " + "\"" + itemId + "\", \"first\" : " + "\"" + first + "\", \"last\" : \"" + last + "\", \"url\" : \"" + "https://s3.amazonaws.com/" + s3bucketName + "/" + first + last + itemId + ".html\"}" ;
 		System.out.println(message);
 		snsClient.publish(new PublishRequest(UPDATE_TOPIC_ARN, message));
 		
 		System.out.println("Succesfully added " + fileName + " to your S3 bucket " + s3bucketName);
 		
 		//delete the local file after storing in S3 so it is not retained
 		contactDocument.delete();
 	}
 	
 	/********************************************************************
 	* Update a contact's web page in S3
 	*********************************************************************/
 	private static void updateContactInS3() {
 		String first = ""; 
 		String last = "";
 		List<String> phoneRecords = new ArrayList<String>(); 
 		List<String> emailRecords = new ArrayList<String>(); 
 		String streetAddress = ""; 
 		String city = ""; 
 		String state = ""; 
 		String zip = "";
 		String tags = ""; 
 		String birthday = "";
 		
 		// check that a contact has been selected
 		if (selectedContactId == null) {
 			System.out.println("Please select a contact first using this option");
 			return;
 		}
 		
 		// build query
         String selectExpression = "select * from `" + CONTACT_DOMAIN_TITLE + "` where itemName() = '" + selectedContactId + "'";
         
         // execute query
         for (Item item : simpleDBClient.select(new SelectRequest(selectExpression)).getItems()) {
             //get the updated attributes
             for (Attribute attribute : item.getAttributes()) {
             	if (attribute.getName().equals(FIRST_KEY)) first = attribute.getValue();
             	if (attribute.getName().equals(LAST_KEY)) last = attribute.getValue();
             	if (attribute.getName().equals(PHONE_KEY)) phoneRecords.add(attribute.getValue());
             	if (attribute.getName().equals(EMAIL_KEY)) emailRecords.add(attribute.getValue());
             	if (attribute.getName().equals(STREET_KEY)) streetAddress = attribute.getValue();
             	if (attribute.getName().equals(CITY_KEY)) city = attribute.getValue();
             	if (attribute.getName().equals(STATE_KEY)) state = attribute.getValue();
             	if (attribute.getName().equals(ZIP_KEY)) zip = attribute.getValue();
             	if (attribute.getName().equals(TAG_KEY)) tags = attribute.getValue();
             	if (attribute.getName().equals(BIRTHDAY_KEY)) birthday = attribute.getValue();
             }
             System.out.println();
         }
 		
         //create the web page for this updated contact in S3
 		try {
 			int itemId = Integer.valueOf(selectedContactId);
 			createContactPageInS3(itemId, first, last, phoneRecords, emailRecords,
 					streetAddress, city, state, zip, tags, birthday);
 		} catch (Exception e) {
 			System.out.println("There was a problem updating S3");
 			System.out.println(e.getMessage());
 		}
 	}
 
 	/********************************************************************
 	* Let the user search for a contact
 	*********************************************************************/
 	private static void searchContacts() {
 		//prompt the user for a search option
 		System.out.println("Enter the number of a search to run");
 		System.out.println("1 - First name starts with");
 		System.out.println("2 - Last name starts with");
 		System.out.println("3 - State equals");
 		System.out.println("4 - Zip equals");
 		System.out.println("5 - Has Tag");
 		System.out.println("6 - Has multiple Tags");
 		System.out.println("7 - Birthday before");
 		System.out.println("8 - Birthday between");
 		System.out.println("9 - Birthday after");
 		
 		//initialize choice to an invalid option
 		int choice = -1;
 		
 		//get the user's input choice
 		try {
 			choice = Integer.valueOf(scn.nextLine());
 		} catch (NumberFormatException ex) {
 			System.out.println(choice + " is not a valid option. Please try again and enter one of the numbers given");
 			return;
 		}
 		
 		// build query
         String baseSelectExpression = "select * from `" + CONTACT_DOMAIN_TITLE + "` where ";
         String userInputWhereClause = "";
         String userInputParameter = "";
         
 		//handle the user's choice
 		switch(choice) {
 		case 1:
 			//search for First name starts with
 			System.out.println("Please enter the character(s) the first name should start with:");
 			userInputParameter = scn.nextLine();
 			userInputWhereClause = " First like '" + userInputParameter + "%'";
 			break;
 		case 2:
 			//search for Last name starts with
 			System.out.println("Please enter the character(s) the last name should start with:");
 			userInputParameter = scn.nextLine();
 			userInputWhereClause = " Last like '" + userInputParameter + "%'";
 			break;
 		case 3:
 			//State equals
 			System.out.println("Please enter the two character state abbreviation (e.g., MD, NY, WA, etc.):");
 			userInputParameter = scn.nextLine();
 			userInputWhereClause = " State = '" + userInputParameter + "'";
 			break;
 		case 4:
 			//Zip equals
 			System.out.println("Please enter the five digit zip code:");
 			userInputParameter = scn.nextLine();
 			userInputWhereClause = " Zip = '" + userInputParameter + "'";
 			break;
 		case 5:
 			//Has Tag
 			System.out.println("Please enter a Tag (just the value, not the brackets):");
 			userInputParameter = scn.nextLine();
 			userInputWhereClause = " Tag like '%[" + userInputParameter + "]%'";
 			break;
 		case 6:
 			//Has multiple Tags
 			String aTag = "";
 			String userInputBeginWhereClause = " Tag like '%[";
 			String userInputEndWhereClause = "]%'";
 			boolean multipleTagInputFlag = false;
 			
 			//let the user input an arbitrary number of tags
 			do {
 				//prompt the user for a tag
 				if (aTag.length() == 0) {
 					System.out.print("Please enter a tag (just the value, not the brackets):");
 				} else {
 					System.out.println("Please enter another tag (or just press enter to finish entering tags):");
 					multipleTagInputFlag = true;
 				}
 				
 				//get the input
 				aTag = scn.nextLine();
 				
 				//if a tag was entered, update the query string
 				if (aTag.length() > 0) {
 					//include an "and" if multiple tags have been entered
 					if (multipleTagInputFlag) {
 						userInputWhereClause = userInputWhereClause + " and ";
 					}
 					
 					//append the input tag to the query
 					userInputWhereClause = userInputWhereClause + userInputBeginWhereClause + aTag + userInputEndWhereClause;
 				}
 				
 			} while(aTag.length() > 0);
 			break;
 		case 7:
 			//Birthday before
 			System.out.println("Please enter the date (in YYYY-MM-DD format) before which to search");
 			userInputParameter = scn.nextLine();
 			userInputWhereClause = " Birthday < '" + userInputParameter + "'";
 			break;
 		case 8:
 			//Birthday between
 			System.out.println("Please enter the date (in YYYY-MM-DD format) after which to search");
 			String firstDate = scn.nextLine();
 			System.out.println("Please enter the date (in YYYY-MM-DD format) before which to search");
 			String secondDate = scn.nextLine();
 			userInputWhereClause = " Birthday > '" + firstDate + "' and Birthday < '" + secondDate + "'";
 			break;
 		case 9:
 			//Birthday after
 			System.out.println("Please enter the date (in YYYY-MM-DD format) after which to search");
 			userInputParameter = scn.nextLine();
 			userInputWhereClause = " Birthday > '" + userInputParameter + "'";
 			break;
 		default:
 			System.out.println(choice + " is not a valid option. Please enter one of the numbers given");
 			return;
 		} 
 		
 		try {
 			//execute the query
 			List<Item> matchingContacts = simpleDBClient.select(new SelectRequest(baseSelectExpression + userInputWhereClause)).getItems();
 			//display any matching contacts
 			if (matchingContacts.size() > 0) {
 				System.out.println("\nResults:\n");
 				displayContacts(matchingContacts);
 			} else {
 				System.out.println("No contacts matched your search.");
 			}
 		} catch (Exception ex) {
 			System.out.println(baseSelectExpression);
 			System.out.println(ex.getMessage());
 		}
 
 	}
 	
 	private static void deleteContact() {
 		// check that a contact has been selected
 		if (selectedContactId == null) {
 			System.out.println("Please select a contact first using option 2");
 			return;
 		}
 		
 		// delete from simpledb
 		simpleDBClient.deleteAttributes(new DeleteAttributesRequest().withDomainName(CONTACT_DOMAIN_TITLE).withItemName(selectedContactId));
 		
 		// delete from s3
         String selectExpression = "select * from `" + CONTACT_DOMAIN_TITLE + "` where itemName() = '" + selectedContactId + "'";
         String first = "", 
         		last = "";
         
         for (Item item : simpleDBClient.select(new SelectRequest(selectExpression)).getItems()) {
             for (Attribute attribute : item.getAttributes()) {
             	if (attribute.getName().equals(FIRST_KEY)) {
             		first = attribute.getValue();
             	} else if (attribute.getName().equals(LAST_KEY)) {
             		last = attribute.getValue();
             	}
             }
         }
 		s3client.deleteObject(new DeleteObjectRequest(CONTACT_DOMAIN_TITLE, first + last + selectedContactId + ".html"));
 		
 		// publish sns notification
		String message = "{ \"updateType\" : \"delete\", \"itemId\" : " + "\"" + selectedContactId + "}";
 		System.out.println(message);
 		snsClient.publish(new PublishRequest(UPDATE_TOPIC_ARN, message));
 	}
 	
 	private static void sendSNSUpdate(String actionType, String first, String last, String url) {
 		//snsClient.publish(new PublishRequest().withMessageStructure(null));
 	}
 }
 
