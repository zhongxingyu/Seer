 /**
  * 
  */
 package controllers.classgroups;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import models.EMessages;
 import models.classgroups.ClassGroupContainer;
 import models.classgroups.ClassGroupContainer.PupilRecordTriplet;
 import models.dbentities.ClassGroup;
 import models.dbentities.UserModel;
 
 import com.avaje.ebean.Ebean;
 
 import controllers.util.DateFormatter;
 import controllers.util.GenderParser;
 
 /**
  * @author Jens N. Rammant
  *	A helper class for the reading and writing of classgroups.
  *	Works together with {@link controllers.util.XLSXImporter}
  * TODO tests
  */
 public class ClassGroupIO {
 	
 	private static List<String> classHeader;
 	private static List<String> pupilHeader;
 	
 	static{
 		classHeader = new ArrayList<String>();
 		classHeader.add("#");
 		classHeader.add(EMessages.get("class.name"));
 		classHeader.add(EMessages.get("class.expdate"));
 		classHeader.add(EMessages.get("class.level"));
 		classHeader.add(EMessages.get("class.school"));
 		
 		pupilHeader = new ArrayList<String>();
 		pupilHeader.add("#");
 		pupilHeader.add(EMessages.get("classes.pupil.form.id"));
 		pupilHeader.add(EMessages.get("classes.pupil.form.name"));
 		pupilHeader.add(EMessages.get("classes.pupil.form.birthdate"));
		pupilHeader.add(EMessages.get("classes.pupil.form.gender"));
		pupilHeader.add(EMessages.get("classes.pupil.form.preflanguage"));
 		pupilHeader.add(EMessages.get("classes.pupil.form.password"));
 		pupilHeader.add(EMessages.get("classes.pupil.form.email"));
 		
 	}
 
 	/**
 	 * Turns a classgroup and its contents into a format writeable by XLSXImporter
 	 * @param classID id of the class
 	 * @return the classgroup and its contents in a format writeable by XLSXImporter, or null if db failed
 	 */
 	public static List<List<String>> fullClassgroupToList(int classID){
 		try{
 			List<List<String>> res = new ArrayList<List<String>>();
 			res.add(classHeader);
 			ClassGroup cg = Ebean.find(ClassGroup.class,classID);
 			if(cg==null)return null;
 			res.add(classgroupToList(cg));
 			
 			res.add(pupilHeader);
 			List<UserModel> pupils = cg.getPupils(ClassGroup.PupilSet.ALL);
 			for(UserModel um : pupils){
 				res.add(userModelToList(um));
 			}
 			
 			return res;
 		}catch(PersistenceException pe){
 			return null;
 		}
 	}
 	
 	/**
 	 * Converts a list (from XLSX Importer) to a saveable format
 	 * @param list list to be converted
 	 * @param existingClassId an existing class to which the pupils have to be added.
 	 * @return ClassGroup & pupils, null if something goes wrong with the db
 	 */
 	public static ClassGroupContainer listToClassGroup(List<List<String>> list, int existingClassId){
 		try{
 			//Try to add the existing class to the container
 			ClassGroupContainer res = new ClassGroupContainer();
 			ClassGroup cg = Ebean.find(ClassGroup.class,existingClassId);
 			String message = cg!=null?"":EMessages.get("classes.import.classnotexist");
 			res.setClassGroup(cg, cg!=null, message, false);
 			//Iterate over all the records
 			for(int i=0;i<list.size();i++){
 				List<String> record = list.get(i);
 				if(record.isEmpty())continue;
 				//CLASS records are not read, but an error is added
 				if("CLASS".equalsIgnoreCase(record.get(0))){
 					res.appendCGMessage(EMessages.get("classes.import.classrecordwhileaddingtoexisting"));
 				}
 				else if("PUPIL".equalsIgnoreCase(record.get(0))){
 					//Parse the pupil record
 					UserModel parsed = parseToUserModel(record);
 					PupilRecordTriplet prt = parsedUserModelToTriplet(parsed);
 					if(parsed.id!=null&&!parsed.id.isEmpty()){
 						//If id is mentioned, 
 						//add to the existing Pupil list (even if it doesn't exist, but isValid
 						//is false then, so it won't be saved.
 						res.addExistingPupil(prt);
 					}
 					//If no id is mentioned, add to the new pupil list
 					else {
 						res.addNewPupil(prt);
 					}
 				}
 			}
 			//Check some other constraints
 			res.validate();
 			return res;
 		//If something goes wrong, return null	
 		}catch(PersistenceException pe){
 			return null;
 		}
 	}
 	
 	/**
 	 * Converts a list (from XLSX Importer) to a saveable format
 	 * @param list list to be converted
 	 * @return ClassGroups & pupils
 	 */
 	public static List<ClassGroupContainer> listToClassGroup(List<List<String>> list){
 		List<ClassGroupContainer> res = new ArrayList<ClassGroupContainer>();
 		ClassGroupContainer current = null;
 		//Iterate over all lines
 		for(int i=0;i<list.size();i++){
 			List<String> record = list.get(i);
 			//If record is empty, go to next record
 			if(record.isEmpty())continue;
 			//If record is a class
 			if("CLASS".equalsIgnoreCase(record.get(0))){
 				//If there is already a classgroupcontainer, add it to the list
 				if(current!=null)res.add(current);
 				current=new ClassGroupContainer();
 				current.setClassGroup(parseToClassModel(record), true, "", true);
 			//If record is a pupil
 			}else if("PUPIL".equalsIgnoreCase(record.get(0))){
 				if(current==null)current=new ClassGroupContainer();
 				UserModel um = parseToUserModel(record);
 				PupilRecordTriplet prt = parsedUserModelToTriplet(um);
 				//determine whether it's an existing pupil or a new pupil
 				if(um.id==null||um.id.isEmpty()){
 					current.addNewPupil(prt);
 				}else{
 					current.addExistingPupil(prt);
 				}
 			}
 		}
 		if(current!=null)res.add(current);
 		//Check some other constraints
 		for(ClassGroupContainer cgp : res){
 			cgp.validate();
 		}
 		return res;
 	}
 	
 	
 	/**
 	 * 
 	 * @param cg ClassGroup
 	 * @return the contents of the ClassGroup object in a format that can be added to the list
 	 */
 	private static List<String> classgroupToList(ClassGroup cg){
 		ArrayList<String> res = new ArrayList<String>();
 		res.add("CLASS");
 		res.add(cg.name);
 		res.add(DateFormatter.formatDate(cg.expdate));
 		res.add(cg.level);
 		res.add(Integer.toString(cg.schoolid));
 		return res;
 	}
 	
 	/**
 	 * 
 	 * @param um UserModel
 	 * @return the contents of the UserModel object in a format that can be added to the list
 	 */
 	private static List<String> userModelToList(UserModel um){
 		ArrayList<String> res = new ArrayList<String>();
 		res.add("PUPIL");
 		res.add(um.id);
 		res.add(um.name);
 		res.add(DateFormatter.formatDate(um.birthdate));
 		res.add(um.gender.toString());
 		res.add(um.preflanguage);
 		res.add("");
 		res.add(um.email);		
 		return res;
 	}
 	
 	/**
 	 * 
 	 * @param toParse List to parse into UserModel
 	 * @return a UserModel filled with the data in the list
 	 */
 	private static UserModel parseToUserModel(List<String> toParse){
 		UserModel res = new UserModel();
 		try{
 			res.id=toParse.get(1).trim();
 			res.name=toParse.get(2).trim();
 			res.birthdate = DateFormatter.parseString(toParse.get(3).trim());
 			res.gender=GenderParser.parseString(toParse.get(4).trim());
 			res.preflanguage=toParse.get(5).trim();
 			res.password=toParse.get(6).trim();
 			String email = toParse.get(7).trim();
 			if(email!=null)email=email.trim();
 			res.email=email.isEmpty()?null:email;
 		}catch(Exception e){}
 		return res;
 	}
 	
 	/**
 	 * 
 	 * @param toParse List to be parsed into ClassGroup
 	 * @return a ClassGroup filled with the data in the list
 	 */
 	private static ClassGroup parseToClassModel(List<String> toParse){
 		ClassGroup res = new ClassGroup();
 		try{
 			res.name=toParse.get(1);
 			res.expdate=DateFormatter.parseString(toParse.get(2));
 			res.level=toParse.get(3);
 			try{
 				res.schoolid = (int)Double.parseDouble(toParse.get(4));
 			}catch(NumberFormatException nfe){
 				res.schoolid = -1;
 			}
 		}catch(IndexOutOfBoundsException e){}
 		return res;
 	}
 	
 	/**
 	 * 
 	 * @param parsed UserModel to put in the record
 	 * @return a PupilRecordTriplet that fits the parsed data
 	 */
 	private static PupilRecordTriplet parsedUserModelToTriplet(UserModel parsed){
 		PupilRecordTriplet prt = new PupilRecordTriplet();
 		prt.user=parsed;
 		prt.message="";
 		prt.isValid=true;
 		//If an id is mentioned, try to add the existing userdata
 		if(parsed.id!=null&&!parsed.id.isEmpty()){
 			UserModel existing = Ebean.find(UserModel.class, parsed.id);
 			//If the userdata doesn't exist, add error message and show the parsed record
 			if(existing==null){
 				prt.isValid=false;
 				prt.message=EMessages.get("classes.import.usernotexist");
 			}
 			//Else add the existing userdata
 			else{
 				prt.user=existing;
 			}
 		}
 		return prt;
 	}
 	
 }
