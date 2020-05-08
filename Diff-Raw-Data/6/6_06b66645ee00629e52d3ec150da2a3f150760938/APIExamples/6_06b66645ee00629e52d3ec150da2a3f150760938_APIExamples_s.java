 
 /*Intelligent InSites Web Service API Examples
 
 This simple console application demonstrates many of the features of the
 Intelligent InSites Web Services API. Simply un-comment each line to try them out.
 This class implements the BasicClient class to perform HTTP requests.*/
 
 package com.intelligentinsites.codesamples;
 
 import com.intelligentinsites.codesamples.BasicClient;
import java.io.*;
 
 public class APIExamples {
 	static BasicClient client;
     public static void main(String[] args) {
     	client = new BasicClient("www.insites-dev.intelligentinsites.com", "username", "password");
     	
         //// limit
    	//System.out.println(client.get("/api/2.0/rest/equipment.xml"));			//Get 100 equipment resources
    	System.out.println(client.get("/api/2.0/rest/equipment.xml?limit=10"));	//Get 3 equipment resources
     	//System.out.println(client.get("/api/2.0/rest/equipment.xml?limit=-1"));	//Get all equipment resources
         
         //// limit + first-result
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?limit=5&first-result=0"));	//Get the first 5 equipment resources
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?limit=5&first-result=5"));	//Get the next 5 equipment resources
         
         //// select
         //System.out.println(client.get("/api/2.0/rest/staff/BxhL.xml"));								//Get the staff resource with an id of 'BxhL'
         //System.out.println(client.get("/api/2.0/rest/staff/BxhL.xml?select=name"));					//Get only the name field of the staff with an id of 'BxhL'
         //System.out.println(client.get("/api/2.0/rest/staff/BxhL.xml?select=name,current-location"));	//Get the name and location of the staff with an id of 'BxhL'
         
         //// expand
         //System.out.println(client.get("/api/2.0/rest/logins.xml?expand=staff")); 								//Get login resources with the 'staff' field expanded.
         //System.out.println(client.get("/api/2.0/rest/logins.xml?expand=staff.primary-location"));				//Get logins with the associated staff's primary location expanded.
 		//System.out.println(client.get("/api/2.0/rest/equipment.xml?expand=sensors,service-status"));			//Get equipment with multiple fields expanded
 		//System.out.println(client.get("/api/2.0/rest/equipment.xml?expand=sensors&expand=service-status"));
 		//System.out.println(client.get("/api/2.0/rest/equipment.xml?expand=*"));								//Wildcard used to get equipment with all fields expanded
 		//System.out.println(client.get("/api/2.0/rest/equipment.xml?expand=sensors.*"));						//Get equipment with all fields within the 'sensors' field expanded
         
         ////sort
         //System.out.println(client.get("/api/2.0/rest/staff.xml?sort=name"));								//Sort all staff by name in ascending order, then get the first 100.
         //System.out.println(client.get("/api/2.0/rest/staff.xml?sort=name+asc"));							//Sort all staff by name in ascending order, then get the first 100.
         //System.out.println(client.get("/api/2.0/rest/staff.xml?sort=name+desc"));							//Sort all staff by name in descending order, then get the first 100.
         //System.out.println(client.get("/api/2.0/rest/staff.xml?sort=to-lower(name)+asc"));				//Sort all staff by name, ignoring case
         //System.out.println(client.get("/api/2.0/rest/staff.xml?sort=current-location.name&sort=name"));	//Sort all staff by location name, then by name
     	
     	////filter
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?filter=manufacturer~'Ekahau'"));																		//Get equipment where the manufacturer field is 'Ekahau'
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?filter=manufacturer+eq+'Ekahau'"));
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?filter=manufacturer~'Ekahau'+and+current-location.name~'BioMed'"));									//Get equipment where manufacturer = 'Ekahau' and current location name = 'BioMed'
         //System.out.println(client.get("/api/2.0/rest/patient-visits.xml?filter=status.name+eq+'In+Prep'+and+(type.name+eq+'ER+Patient'+or+type.name+eq+'OR+Patient')"));	//Get patient-visits with a status of 'In Prep', and whose type is either 'ER Patient', or 'OR Patient'
         //System.out.println(client.get("/api/2.0/rest/entities.xml?filter=sensors.total-count+gt+0"));																		//Get entities with attached sensors
         //System.out.println(client.get("/api/2.0/rest/sensors.xml?filter=entity-attached-to.element-type+eq+'equipment'")); 												//Get sensors attached to any equipment
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?filter=this+matches+'pump'")); 																		//Get equipment matching the search term 'pump'
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?filter=name+matches+'IV.*'")); 																		//Get equipment where 'name' matches the regex 'IV.*'
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?filter=name+matches+'/iv.*/i'")); 																		//Case insensitive match by surrounding a regex with '/.../i'
         //System.out.println(client.get("/api/2.0/rest/equipment.xml?filter=current-location+in+'Bxdc'")); 																	//Get equipment within the location 'Bxdc' or any descendant locations within the location hierarchy
         //System.out.println(client.get("/api/2.0/rest/locations.xml?filter=this+in+'BxhM'")); 																				//Get the location 'BxhM' and its descendant locations
     	
     	////field methods
     	//System.out.println(client.get("/api/2.0/rest/equipment.xml?select=sensors.sort(provider+asc)"));										//Sort each equipment's sensors by provider.
     	//System.out.println(client.get("/api/2.0/rest/equipment.xml?select=sensors.get(0)"));													//Get equipment and select only the first sensor from each.
     	//System.out.println(client.get("/api/2.0/rest/equipment.xml?select=sensors.sort(provider+asc).get(0..2)"));							//Sort each equipment's sensors by provider, then select the first 3 sensors from each.
     	//System.out.println(client.get("/api/2.0/rest/equipment.xml?filter=sensors.filter(not-reporting+eq+'false').total-count+gt+0"));		//Get equipment with at least one sensor that is not reporting
     	//System.out.println(client.get("/api/2.0/rest/locations.xml?filter=parent-hierarchy.filter(name+eq+'Campus+1').total-count+gt+0"));	//Get descendant locations of 'Campus 1'.
     	
     	////Creating, Deleting, and Modifying Data
     	//System.out.println(client.post("/api/2.0/rest/equipment.xml?name=Wheelchair7&short-name=wc7&type=Bxc6x&status=Bxc&service-status=Bxc"));									//Create a new equipment resource named 'Wheelchair 1'
     	//System.out.println(client.post("/api/2.0/rest/equipment/Bxj.xml?model=X4000"));																							//Update an existing equipment resource by changing the model to 'X4000'
         //System.out.println(client.post("/api/2.0/rest/sensors/Bxrx/button-press.xml?button=2"));													                                //Inform InSites that button 2 was pressed on sensor 'Bxrx'
         //System.out.println(client.post("/api/2.0/rest/sensors/by-key/ekahau-rtls/00:18:8e:20:44:a7/move.xml?new-location=BxdL"));													//Inform InSites that the sensor with key/value (ekahau-rtls/00:18:8e:20:44:a7) has moved to location 'BxdL'
         //System.out.println(client.post("/api/2.0/rest/locations/Bxc.xml?attributes.insites.assigned-patient=Bxzkw"));																//Assign a value to the custom attribute assigned-patient
         //System.out.println(client.post("/api/2.0/rest/locations/Bxc.xml?attributes.insites.assigned-patient="));																	//Clear the value of the custom attribute assigned-patient
         //System.out.println(client.post("/api/2.0/rest/locations/BxjL.xml?attributes.insites.required-equipment-types=Bxc7L&attributes.insites.required-equipment-types=Bxc7R"));	//Assign multiple values to a custom attribute collection
     }
 }
