 package org.imirsel.nema.contentrepository.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.imirsel.nema.model.FileDataType;
 import org.imirsel.nema.model.GroupDataType;
 import org.imirsel.nema.model.OsDataType;
 
 
 /**Returns various resource typess
  * 
  * @author kumaramit01
  * @since 0.3.0
  */
 public class ResourceTypeServiceImpl implements ResourceTypeService {
 	
	List<FileDataType> fileDataTypeList  = new ArrayList<FileDataType>();
	List<OsDataType> supportedOsList  = new ArrayList<OsDataType>();
	List<GroupDataType> groupList = new ArrayList<GroupDataType>();
 	
 	public ResourceTypeServiceImpl(){
 		fileDataTypeList.add( new FileDataType("Chord Interval Text File",
 				"org.imirsel.nema.analytics.evaluation.chord.ChordIntervalTextFile"));
 		fileDataTypeList.add( new FileDataType("Chord Short Hand Text File",
 		"org.imirsel.nema.analytics.evaluation.chord.ChordShortHandTextFile"));
 		fileDataTypeList.add( new FileDataType("Chord Number Text File",
 		"org.imirsel.nema.analytics.evaluation.chord.ChordNumberTextFile"));
 		fileDataTypeList.add( new FileDataType("Classification Text File",
 		"org.imirsel.nema.analytics.evaluation.classifiction.ClassificationTextFile"));
 		fileDataTypeList.add( new FileDataType("Melody Text File",
 		"org.imirsel.nema.analytics.evaluation.melody.MelodyTextFile"));
 		fileDataTypeList.add( new FileDataType("Key Text File",
 		"org.imirsel.nema.analytics.evaluation.key.KeyTextFile"));
 		
 		supportedOsList.add(new OsDataType("Unix","Unix Like"));
 		supportedOsList.add(new OsDataType("Windows","Windows Like"));
 		
 		groupList.add(new GroupDataType("imirsel","imirsel"));
 		
 	}
 	
 	public List<FileDataType> getSupportedFileDataTypes(){
 		return fileDataTypeList;
 	}
 	public List<OsDataType> getSupportedOperatingSystems(){
 		return this.supportedOsList;
 	}
 	public List<GroupDataType> getSupportedGroups(){
 		return this.groupList;
 	}
 	
 	
 	/**
 	 * 
 	 * @param value
 	 * @return OsDataType -returns the OsDataType
 	 */
 	public OsDataType getOsDataType(String value){
 		if(value.equalsIgnoreCase("Unix Like")){
 			OsDataType os = new OsDataType("Unix","Unix Like");
 			return os;
 		}else if(value.equals("Windows Like")){
 			OsDataType os = new OsDataType("Unix","Unix Like");
 			return os;
 		}else{
 			throw new IllegalArgumentException("error invalid Os: " + value);
 		}
 		
 	}
 
 }
