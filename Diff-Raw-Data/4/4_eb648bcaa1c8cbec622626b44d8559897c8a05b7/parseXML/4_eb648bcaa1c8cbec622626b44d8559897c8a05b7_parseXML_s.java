 /**
  * Loads and saves from and to XML files.
  * 
  * @author Brett M. Story
  * @date 13 October, 2013
  */
 
 package io;
 
 import objects.*;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import objects.MyCourse;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class parseXML {
 	
 	private static MyCourse course;
         
     private static MyCourse loadCourseInfo(File file) {   
     	
     	course = new MyCourse(null);
     	
     	try {
             // Initial setup of document parser         
             DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
             Document doc = docBuilder.parse(file);
             
             // normalize text representation
             doc.getDocumentElement().normalize();
             
             /* ------------------------------
              * Section for courseName 
              * -------------------------------*/
             NodeList courseNameList = doc.getElementsByTagName("courseName");
             Node courseNameNode = courseNameList.item(0);
             
             //Note: you must do .getFirstChild().getNodeValue() to return what's inside the tags
             course.setName(courseNameNode.getFirstChild().getNodeValue());
         
             /* ------------------------------
              * Section for courseID/#/Section
              * -------------------------------*/
             NodeList courseIDList = doc.getElementsByTagName("courseID");
             Node courseIDNode = courseIDList.item(0);
             
             course.setCourseID(courseIDNode.getFirstChild().getNodeValue());
             
             NodeList courseNumberList = doc.getElementsByTagName("courseNumber");
             Node courseNumberNode = courseNumberList.item(0);
             
             course.setCourseNumber(Integer.parseInt(courseNumberNode.getFirstChild().getNodeValue()));
             
             NodeList sectionList = doc.getElementsByTagName("section");
             Node sectionNode = sectionList.item(0);
             
             course.setSection(sectionNode.getFirstChild().getNodeValue());
             
             /* ------------------------------
              * Section for building/roomNumber
              * -------------------------------*/
             
             NodeList buildingList = doc.getElementsByTagName("building");
             Node buildingNode = buildingList.item(0);
             
             course.setBuilding(buildingNode.getFirstChild().getNodeValue());
             
             NodeList roomIDList = doc.getElementsByTagName("roomID");
             Node roomIDNode = roomIDList.item(0);
             
             course.setRoomID( roomIDNode.getFirstChild().getNodeValue());
             
             /* ------------------------------
              * Section for meetingTime
              * -------------------------------*/
             
             NodeList meetingTimeList = doc.getElementsByTagName("meetingTime");
             Node meetingTimeNode = meetingTimeList.item(0);
             
             course.setMeetingTime(meetingTimeNode.getFirstChild().getNodeValue());
             
         //Beyond this point are all catches
         }catch (SAXParseException err)
         {
             System.out.println("** Parsing error" + ", line " +
                     err.getLineNumber() + ", uri " + err.getSystemId());
             System.out.println(" " + err.getMessage());
             
         }catch (SAXException e) {
             // TODO Auto-generated catch block
             Exception x = e.getException();
             ((x == null) ? e: x).printStackTrace();
             
         }catch (Throwable t) {
             // TODO Auto-generated catch block
             t.printStackTrace();
         }
         return course;
     }
     
     private static void loadStudentInfo(File file) {
         
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             SAXParser saxParser = factory.newSAXParser();
             
             DefaultHandler handler = new DefaultHandler() {
             	
             	String firstName = null;
             	String lastName = null;
             	String psuedoName = null;
             	
                 boolean isFirstName = false;
                 boolean isLastName = false;
                 boolean isPsuedoName = false;
                 boolean isGhostName = false;
 
     
                 // A SAX callback method which finds the start of an XML element
                 public void startElement (String uri, String localName, String qName,
                     Attributes attributes) throws SAXException {
                 	
                     if (qName.equalsIgnoreCase("firstName")) {
                         isFirstName = true;
                     }
                     if (qName.equalsIgnoreCase("lastName")) {
                         isLastName = true;
                     }
                     if (qName.equalsIgnoreCase("psuedoName")) {
                         isPsuedoName = true;
                     }
                     if (qName.equalsIgnoreCase("ghostName")) {
                     	isGhostName = true;
                     }
                 }
                 
                 // A SAX callback method which finds the end of an XML element
                 public void endElement(String uri, String localName, 
                     String qName) throws SAXException {
                 	
                 	// adds a student to course when it reaches </student>
                 	if (qName.equalsIgnoreCase("student")) {
                 		course.addStudent(firstName, lastName, psuedoName);
                 	}
                 }
                 
                 // A SAX callback method which contains all the characters in an element
                 public void characters(char ch[], int start, int length) 
                     throws SAXException {
                 	
                     //If the element is <name>
                     if (isFirstName) {
                         firstName = new String(ch, start, length);
                         isFirstName = false; // must declare name false for next search
                     }
                     //If the element is <name>
                     if (isLastName) {
                         lastName = new String(ch, start, length);
                         isLastName = false; // must declare name false for next search
                     }
                     //If the element is <psuedoName>
                     if (isPsuedoName) {
                         psuedoName = new String(ch, start, length);
                         isPsuedoName = false;   
                     }
                     if (isGhostName) {
                     	course.addGhostStudent(new String(ch, start, length));
                     	isGhostName = false;
                     }
                 }
                 
             };
             
             saxParser.parse(file, handler);
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private static void loadAssignmentInfo(File file) {
         
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             SAXParser saxParser = factory.newSAXParser();
             
             DefaultHandler handler = new DefaultHandler() 
             {
             	String assignmentName;
             	int assignmentWorth;
             	
             	String currentCategoryName;
             	int currentCategoryIndex = 0;
             	
             	boolean isCategoryName = false;
                 boolean isAssignmentName = false;
                 boolean isWorth = false;
                 boolean isGrade = false;
                 
                 int assignIndex = 0;
                 int studentIndex = 0;
                 
                 int numberOfStudents = course.getTotalStudents();
                 
                 // A SAX callback method which finds the start of an XML element
                 public void startElement (String uri, String localName, String qName,
                     Attributes attributes) throws SAXException {
                     if (qName.equalsIgnoreCase("categoryName")) {
                         isCategoryName = true;
                     }
                     if (qName.equalsIgnoreCase("assignmentName")) {
                         isAssignmentName = true;
                     }    
                     if (qName.equalsIgnoreCase("worth")) {
                         isWorth = true;
                     }               
                     if (qName.equalsIgnoreCase("grade")) {
                         isGrade = true;
                     }
                 }
                 
                 // A SAX callback method which finds the end of an XML element
                 public void endElement(String uri, String localName, 
                     String qName) throws SAXException {
                     if (qName.equalsIgnoreCase("assignment")) {
                         course.getAssignmentCategory(currentCategoryIndex).addAssignment(assignmentName, assignmentWorth);
                         studentIndex = 0;
                         assignIndex ++;
                     }
                 }
                 
                 // A SAX callback method which contains all the characters in an element
                 public void characters(char ch[], int start, int length) 
                     throws SAXException {
                 	if (isCategoryName) {
                 		currentCategoryName = new String(ch, start, length);
                 		course.addAssignmentCategory(currentCategoryName);
                 		currentCategoryIndex = course.getAssignmentCategoryIndex(currentCategoryName);
                 		isCategoryName = false;
                 	}
                     if (isAssignmentName) {
                         assignmentName = new String(ch, start, length);
                         isAssignmentName = false;
                         course.getGradeBook().addAssignmentColumn(numberOfStudents);
                     }
                     if (isWorth) {
                     	assignmentWorth = Integer.parseInt(new String(ch, start, length));
                     	isWorth = false;
                     }
                    if (isGrade) {
                 	   course.getGradeBook().setGrade(assignIndex, studentIndex, Double.parseDouble(new String(ch, start, length)));
                 	   isGrade = false;
                 	   studentIndex ++;
                    }
                 }
             };
             
             saxParser.parse(file, handler);
             
         } catch (Exception e) {
             e.printStackTrace();
         }
         
     }
     
     public static MyCourse loadXML(File file) {
     	
     	course = loadCourseInfo(file);
     	
     	loadStudentInfo(file);
     	
     	loadAssignmentInfo(file);
     	
     	return course;
     }
     
     public static void saveXML(MyCourse tmpCourse, File file) {
     	Writer writer = null;
 
     	try {
     		if (file.exists())
     			file.delete();
     		
     		file.createNewFile();
     		
     	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
     	    
     	    //Add header of XML file
     	    writer.write("<class id=\""+file.toString()+"\">\n");
     	    
     	    //Add general course information to file
     	    writer.write("\t<courseName>" + course.getName() + "</courseName>\n");
     	    writer.write("\t<courseID>" + course.getCourseID() + "</courseID>\n");
     	    writer.write("\t<courseNumber>" + course.getCourseNumber() + "</courseNumber>\n");
     	    writer.write("\t<section>" + course.getSection() + "</section>\n");
     	    writer.write("\t<building>" + course.getBuilding() + "</building>\n");
     	    writer.write("\t<roomID>" + course.getRoomID() + "</roomID>\n");
     	    writer.write("\t<meetingTime>" + course.getMeetingTime() + "</meetingTime>\n\n");
     	    
     	    //Add student information
             for (int i = 0; i < course.getNumberOfStudents(); i++) {
                 writer.write("\t<student>\n");
                 writer.write("\t\t<firstName>" + course.getStudent(i).getFirstName() + "</firstName>\n");
                 writer.write("\t\t<lastName>" + course.getStudent(i).getLastName() + "</lastName>\n");
                 writer.write("\t\t<psuedoName>" + course.getStudent(i).getPseudoName() + "</psuedoName>\n");
                 writer.write("\t</student>\n");
             }
             
         	writer.write("\t<!-- Ghost Students -->\n");
             
             //Add ghost students
             for (int i = 0; i <course.getNumberOfGhostStudents(); i++) {
                 writer.write("\t<ghostStudent>\n");
                 writer.write("\t\t<ghostName>" + course.getGhostStudent(i).getPseudoName() + "</ghostName>\n");
                 writer.write("\t</ghostStudent>\n");
             }
     	    
             //Add assignment information
             
             int assignmentIndex = 0;
             
             for (int i = 0; i < course.getNumberOfAssignmentCategories(); i++) {
             	writer.write("\t<category>\n");
             	writer.write("\t\t<categoryName>" + course.getAssignmentCategory(i).getName() + "</categoryName>\n");
             	
             	for (int j = 0; j < course.getAssignmentCategory(i).getNumberOfAssignments(); j++) {
                 	writer.write("\t\t<assignment>\n");
                 	writer.write("\t\t\t<assignmentName>" + course.getAssignmentCategory(i).getAssignment(j).getName() + "</assignmentName>\n");
                 	writer.write("\t\t\t<worth>" + course.getAssignmentCategory(i).getAssignment(j).getWorth() + "</worth>\n");
                 	
                 	for (int k = 0; k < course.getNumberOfStudents(); k++) {
                 		writer.write("\t\t\t<grade id=\"" + course.getStudent(k).getFullName() + "\">" + course.getGradeBook().getGrade(assignmentIndex, k) + "</grade>\n");
                 	}
                 	
                 	writer.write("\t\t\t<!-- Ghost Students -->\n");
 
                 	for (int k = 0; k < course.getNumberOfGhostStudents(); k++) {
                 		int currentIndex = k + course.getNumberOfStudents();
                 		writer.write("\t\t\t<grade id=\"" + course.getGhostStudent(k).getPseudoName() + "\">" +
                 						course.getGradeBook().getGrade(assignmentIndex, currentIndex) + "</grade>\n");
                 	}
                 	
                 	writer.write("\t\t</assignment>\n");
                 	
                 	assignmentIndex ++;
             	}
             	
             	writer.write("\t</category>\n");
             }
             
             writer.write("</class>");
     	    
     	} catch (IOException ex){
     	  // report
     	} finally {
     	   try {writer.close();} catch (Exception ex) {}
     	}
     }
     
     private static void printCourse() {
         System.out.println("Course Name: " + course.getName());
         System.out.println("Course ID: " + course.getCourseID() + course.getCourseNumber() + "-" + course.getSection());
         System.out.println("Room: " + course.getBuilding() + " " + course.getRoomID());
         System.out.println("Meeting Time: " + course.getMeetingTime());
         
         for (int i = 0; i < course.getNumberOfStudents(); i++) {
             System.out.println("NAME: " + course.getStudent(i).getFullName());
             System.out.println("PSUEDONAME: " + course.getStudent(i).getPseudoName());
         }
         
         for (int i = 0; i < course.getNumberOfGhostStudents(); i++) {
         	System.out.println("GHOST STUDENT: " + course.getGhostStudent(i).getPseudoName());
         }
         
         
         
         for (int i = 0; i < course.getNumberOfAssignmentCategories(); i++) {
         	System.out.println("CATEGORY: " + course.getAssignmentCategory(i).getName());
         	
         	for (int j = 0; j < course.getAssignmentCategory(i).getNumberOfAssignments(); j++) {
         		System.out.println("ASSIGNMENT: " + course.getAssignmentCategory(i).getAssignment(j).getName());
         		System.out.println("WORTH: " + course.getAssignmentCategory(i).getAssignment(j).getWorth());
         	}
         	
         	for (int j = 0; j < course.getNumberOfStudents(); j++) {
         		System.out.println(course.getStudent(j).getFullName() + "'s GRADE: " + course.getGradeBook().getGrade(i, j));
         	}
         	
         	for (int j = 0; j < course.getNumberOfGhostStudents(); j++) {
         		System.out.println(course.getGhostStudent(j).getPseudoName() + "'s GRADE: " + course.getGradeBook().getGrade(i, j));
         	}
         }
     }
     
     public static void main(String argv[]) {
     	course = loadXML(new File("structure.xml"));
     	printCourse();
     	saveXML(course, new File("output.xml"));
    }
 }
