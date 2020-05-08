 package CourseList;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import java.io.File;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import USC.Course;
 
 public class CourseList {
 	public static void main (String[] args) {
 
 		Scanner in = new Scanner(System.in); 
 
 		String url = "";
 
 		/**
 		 * For DepartmentList Parsing. 
 		 */
 		/*
 		File departmentList = new File("DepartmentList.txt");
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(departmentList));
 
 			while (br.ready()) {
 				url = br.readLine();
 
 				parseDepartment(url);
 			}
 
 			br.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		*/
 		
 		/**
 		 * For individual course testing. 
 		 */
 		
 		while (!url.equals("~")) {
 			System.out.println("Enter Department URL to parse, enter '~' to stop:  ");
 			url = in.nextLine();
 
 			parseDepartment(url);
 		}
 	}
 	
 	
 	public static void parseDepartment(String url) {
 		
 		Document doc = null;
 		
 		//IF NO INTERNET
		File input = new File("tmp/CTPR.html");
 		
 		System.out.println("Opening...");
 		
 		try {
 			//doc = Jsoup.connect(url).get();
 			//IF NO INTERNET
 			doc = Jsoup.parse(input, "UTF-8", "");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println("Successfully Opened...");
 		
 		System.out.println("Parsing...");
 		
 		Elements courses = doc.select("p");
 		
 		
 		ArrayList<Course> deptList = new ArrayList<Course>();
 		ArrayList<String> errorList = new ArrayList<String>();
 		
 		
 		for (Element src : courses) {
 
 			//Course data
 			String dept = "";
 			String number = "";
 			String name = "";
 			String description = "";
 			
 			double units = 0;
 			double maxUnits = 0;
 			double overall = 0;
 			
 			Boolean spring = false;
 			Boolean summer = false;
 			Boolean fall = false;
 			
 			//For ab, abc, abcd, abz, abcz, abcdz etc courses;
 			Boolean ab = false;
 			Boolean abc = false;
 			Boolean abcd = false;
 			Boolean az = false;
 			Boolean abz = false;
 			Boolean abcz = false;
 			Boolean abcdz = false;
 			
 			String numberA = "";
 			String descriptionA = "";
 			double unitsA = 0;
 			double maxUnitsA = 0;
 			Boolean springA = false;
 			Boolean summerA = false;
 			Boolean fallA = false;
 			
 			String numberB = "";
 			String descriptionB = "";
 			double unitsB = 0;
 			double maxUnitsB = 0;
 			Boolean springB = false;
 			Boolean summerB = false;
 			Boolean fallB = false;
 			
 			String numberC = "";
 			String descriptionC = "";
 			double unitsC = 0;
 			double maxUnitsC = 0;
 			Boolean springC = false;
 			Boolean summerC = false;
 			Boolean fallC = false;
 			
 			String numberD = "";
 			String descriptionD = "";
 			double unitsD = 0;
 			double maxUnitsD = 0;
 			Boolean springD = false;
 			Boolean summerD = false;
 			Boolean fallD = false;
 			
 			String numberZ = "";
 			String descriptionZ = "";
 			double unitsZ = 0;
 			double maxUnitsZ = 0;
 			Boolean springZ = false;
 			Boolean summerZ = false;
 			Boolean fallZ = false;
 			
 			
 			//Splitting the course details. 
 			ArrayList<String> split = new ArrayList<String>();
 			System.out.println(src.text());
 			
 			String c = src.text();
 			
 			String[] weirdSplit = c.split("");		//for weird character
 			
 			for (String s : weirdSplit) {
 				String[] spaceSplit = s.split(" ");
 				for (String sp : spaceSplit) {
 					split.add(sp);
 				}
 			}
 			//Finish splitting course details. 
 			
 			//Parsing flags:
 			Boolean courseCodeFinished = false;
 			Boolean nameFinished = false;
 			Boolean unitFinished = false;
 			Boolean desZ = false, desD = false, desC = false, desB = false, desA = false;
 			
 			try {
 				//TODO PARSING GOES HERE
 				for (int i = 0; i < split.size(); i++) {
 					if (i == 0 && courseCodeFinished == false) {
 						if (split.get(0).toUpperCase().equals(split.get(0)) && !split.get(0).equals("")) {
 							//Department Code (example CSCI)
 							dept = split.get(0);
 							//Finished parsing department code
 							
 							//Course number
 							i++; //(i == 1)
 							
 							//For notes (not a course)
 							if (isDigit(split.get(1).charAt(0)) == false) {
 								break;
 							}
 							
 							number = split.get(1);
 
 							if (number.contains("abcdz") == true) {
 								abcdz = true;
 							} else if (number.contains("abcd") == true) {
 								abcd = true;
 							} else if (number.contains("abcz") == true) {
 								abcz = true;
 							} else if (number.contains("abc") == true) {
 								abc = true;
 							} else if (number.contains("abz") == true) {
 								abz = true;
 							} else if (number.contains("ab") == true) {
 								ab = true;
 							} else if (number.contains("az") == true) {
 								az = true;
 							}
 
 							for (int j = 0; j < number.length(); j++) {
 								if (number.charAt(j) == 'a') {
 									numberA = numberA + number.charAt(j);
 								} else if (number.charAt(j) == 'b') {
 									numberB = numberB + number.charAt(j);
 								} else if (number.charAt(j) == 'c') {
 									numberC = numberC + number.charAt(j);
 								} else if (number.charAt(j) == 'd') {
 									numberD = numberD + number.charAt(j);
 								} else if (number.charAt(j) == 'z') {
 									numberZ = numberZ + number.charAt(j);
 								}else {
 									numberA = numberA + number.charAt(j);
 									numberB = numberB + number.charAt(j);
 									numberC = numberC + number.charAt(j);
 									numberD = numberD + number.charAt(j);
 									numberZ = numberZ + number.charAt(j);
 								}
 							}
 							//Finish parsing course number
 							
 							courseCodeFinished = true;
 							
 							continue;
 						} else {
 							//Not a course
 							break;
 						}
 					}
 					//End bracket for parsing department and number
 
 					//Parse Name
 					if (nameFinished == false) {
 						if (split.get(i).contains("") || split.get(i).contains("")) {	// Example: Master's	//replace with '
 							if (name != "") {
 								name = name + " ";
 							}
 							for (int j = 0; j < split.get(i).length(); j++) {
 								if (split.get(i).charAt(j) == '') {
 									name = name + "'";
 								} else if (split.get(i).charAt(j) == '') {
 									name = name + "-";
 								} else {
 									name = name + split.get(i).charAt(j);
 								}
 							}
 
 						} else {
 							if (name == "") {
 								name = split.get(i);
 							} else {
 								name = name + " " + split.get(i);
 							}
 						}
 						
 						
 						if (split.get(i+1).equals("(a:") == true ) {
 							nameFinished = true;
 						} else if (split.get(i+1).charAt(0) == '('
 									&& isDigit(split.get(i+1).charAt(1)) == true) {
 							nameFinished = true;
 						}
 						
 						continue;
 					}
 					//End of parsing course name
 					
 					//Parse Units
 					//multi-semester course, format (a: #, ...; b: #, ...; ......)
 					if (unitFinished == false && split.get(i).equals("(a:")) {
 						//i++; //move to the unit number;
 						if(split.get(i+1).contains(".")) {
 							unitsA = Double.parseDouble("."+split.get(i+1).charAt(1));
 						} else {
 							unitsA = Double.parseDouble(""+split.get(i+1).charAt(0));
 						}
 						maxUnitsA = unitsA;
 						
 						if (split.get(i+2).contains("Sp") == true) {
 							springA = true;
 						} else if (split.get(i+2).contains("Sm") == true) {
 							summerA = true;
 						} else if (split.get(i+2).contains("Fa") == true) {
 							fallA = true;
 						}
 						
 						i++;
 						while (split.get(i).contains(")") == false) {
 							if (split.get(i).equals("b:")) {
 								if(split.get(i+1).contains(".")) {
 									unitsB = Double.parseDouble("."+split.get(i+1).charAt(1));
 								} else {
 									unitsB = Double.parseDouble(""+split.get(i+1).charAt(0));
 								}
 								maxUnitsB = unitsB;
 								
 								if (split.get(i+2).contains("Sp") == true) {
 									springB = true;
 								} else if (split.get(i+2).contains("Sm") == true) {
 									summerB = true;
 								} else if (split.get(i+2).contains("Fa") == true) {
 									fallB = true;
 								}
 							} else if (split.get(i).equals("c:")) {
 								if(split.get(i+1).contains(".")) {
 									unitsC = Double.parseDouble("."+split.get(i+1).charAt(1));
 								} else {
 									unitsC = Double.parseDouble(""+split.get(i+1).charAt(0));
 								}
 								maxUnitsC = unitsC;
 								
 								if (split.get(i+2).contains("Sp") == true) {
 									springC = true;
 								} else if (split.get(i+2).contains("Sm") == true) {
 									summerC = true;
 								} else if (split.get(i+2).contains("Fa") == true) {
 									fallC = true;
 								}
 							} else if (split.get(i).equals("d:")) {
 								if(split.get(i+1).contains(".")) {
 									unitsD = Double.parseDouble("."+split.get(i+1).charAt(1));
 								} else {
 									unitsD = Double.parseDouble(""+split.get(i+1).charAt(0));
 								}
 								maxUnitsD = unitsD;
 								
 								if (split.get(i+2).contains("Sp") == true) {
 									springD = true;
 								} else if (split.get(i+2).contains("Sm") == true) {
 									summerD = true;
 								} else if (split.get(i+2).contains("Fa") == true) {
 									fallD = true;
 								}
 							} else if (split.get(i).equals("z:")) {
 								if(split.get(i+1).contains(".")) {
 									unitsZ = Double.parseDouble("."+split.get(i+1).charAt(1));
 								} else {
 									unitsZ = Double.parseDouble(""+split.get(i+1).charAt(0));
 								}
 								maxUnitsZ = unitsZ;
 								
 								if (split.get(i+2).contains("Sp") == true) {
 									springZ = true;
 								} else if (split.get(i+2).contains("Sm") == true) {
 									summerZ = true;
 								} else if (split.get(i+2).contains("Fa") == true) {
 									fallZ = true;
 								}
 							}
 							
 							i++;
 						}
 						
 						unitFinished = true;
 						continue;
 					}
 					
 					//Multi-semester course, format (#-#-#-#-#) or (#-#-#-#-#,...)
 					if (unitFinished == false && split.get(i).contains("-") && (abcdz || abcd || abcz || abc || abz || ab || az) ) {
 						if (split.get(i).length() == 11 && abcdz == true ) {
 							unitsA = Double.parseDouble(""+split.get(i).charAt(1));
 							unitsB = Double.parseDouble(""+split.get(i).charAt(3));
 							unitsC = Double.parseDouble(""+split.get(i).charAt(5));
 							unitsD = Double.parseDouble(""+split.get(i).charAt(7));
 							unitsZ = Double.parseDouble(""+split.get(i).charAt(9));
 							
 						} else if (split.get(i).length() == 9 && abcd == true ) {
 							unitsA = Double.parseDouble(""+split.get(i).charAt(1));
 							unitsB = Double.parseDouble(""+split.get(i).charAt(3));
 							unitsC = Double.parseDouble(""+split.get(i).charAt(5));
 							unitsD = Double.parseDouble(""+split.get(i).charAt(7));
 							
 						} else if (split.get(i).length() == 9 && abcz == true ) {
 							unitsA = Double.parseDouble(""+split.get(i).charAt(1));
 							unitsB = Double.parseDouble(""+split.get(i).charAt(3));
 							unitsC = Double.parseDouble(""+split.get(i).charAt(5));
 							unitsZ = Double.parseDouble(""+split.get(i).charAt(7));
 							
 						} else if (split.get(i).length() == 7 && abc == true ) {
 							unitsA = Double.parseDouble(""+split.get(i).charAt(1));
 							unitsB = Double.parseDouble(""+split.get(i).charAt(3));
 							unitsC = Double.parseDouble(""+split.get(i).charAt(5));
 							
 						} else if (split.get(i).length() == 7 && abz == true ) {
 							unitsA = Double.parseDouble(""+split.get(i).charAt(1));
 							unitsB = Double.parseDouble(""+split.get(i).charAt(3));
 							unitsZ = Double.parseDouble(""+split.get(i).charAt(5));
 							
 						} else if (split.get(i).length() == 5 && ab == true ) {
 							unitsA = Double.parseDouble(""+split.get(i).charAt(1));
 							unitsB = Double.parseDouble(""+split.get(i).charAt(3));
 							
 						} else if (split.get(i).length() == 5 && az == true ) {
 							unitsA = Double.parseDouble(""+split.get(i).charAt(1));
 							unitsZ = Double.parseDouble(""+split.get(i).charAt(3));
 							
 						}
 
 						maxUnitsA = unitsA;
 						maxUnitsB = unitsB;
 						maxUnitsC = unitsC;
 						maxUnitsD = unitsD;
 						maxUnitsZ = unitsZ;
 						
 						if (split.get(i).charAt(split.get(i).length()-1) != ')') {
 							i++;	//Move to semesterOffered.
 							
 							if (split.get(i).contains("Sp") == true) {
 								spring = true;
 								springA = true;
 								springB = true;
 								springC = true;
 								springD = true;
 								springZ = true;
 							}
 							if (split.get(i).contains("Sm") == true) {
 								summer = true;
 								summerA = true;
 								summerB = true;
 								summerC = true;
 								summerD = true;
 								summerZ = true;
 							} 
 							if (split.get(i).contains("Fa") == true) {
 								fall = true;
 								fallA = true;
 								fallB = true;
 								fallC = true;
 								fallD = true;
 								fallZ = true;
 							}
 						}
 						
 						unitFinished = true;
 						continue;
 					}
 					
 					//multi-semester course format (#) 
 					if (unitFinished == false && (abcdz || abcd || abcz || abc || abz || ab || az)) {
 						units = Double.parseDouble(""+split.get(i).charAt(1));
 						unitsA = units;
 						unitsB = units;
 						unitsC = units;
 						unitsD = units;
 						unitsZ = units;
 						maxUnits = units;
 						maxUnitsA = units;
 						maxUnitsB = units;
 						maxUnitsC = units;
 						maxUnitsD = units;
 						maxUnitsZ = units;
 						
 						if (split.get(i).charAt(split.get(i).length()-1) != ')') {
 							i++;	//Move to semesterOffered.
 							
 							if (split.get(i).contains("Sp") == true) {
 								spring = true;
 								springA = true;
 								springB = true;
 								springC = true;
 								springD = true;
 								springZ = true;
 							}
 							if (split.get(i).contains("Sm") == true) {
 								summer = true;
 								summerA = true;
 								summerB = true;
 								summerC = true;
 								summerD = true;
 								summerZ = true;
 							} 
 							if (split.get(i).contains("Fa") == true) {
 								fall = true;
 								fallA = true;
 								fallB = true;
 								fallC = true;
 								fallD = true;
 								fallZ = true;
 							}
 						}
 						
 						unitFinished = true;
 						continue;
 					}
 					
 					//Single Semester course, format (#-#) or (#-#,...)
 					if (unitFinished == false && split.get(i).contains("-") && !(abcdz || abcd || abcz || abc || abz || ab || az) ) {
 
 						if (split.get(i).charAt(1) == '.') {
 							units = Double.parseDouble("."+split.get(i).charAt(2));
 							if (split.get(i).length() == 6) {
 								maxUnits = Double.parseDouble(""+split.get(i).charAt(4));
 							} else if (split.get(i).length() == 7) {
 								maxUnits = Double.parseDouble(""+split.get(i).charAt(4)+split.get(i).charAt(5));
 							}
 						} else {
 							units = Double.parseDouble(""+split.get(i).charAt(1));
 							if (split.get(i).length() == 5) {
 								maxUnits = Double.parseDouble(""+split.get(i).charAt(3));
 							} else if (split.get(i).length() == 6) {
 								maxUnits = Double.parseDouble(""+split.get(i).charAt(3)+split.get(i).charAt(4));
 							}
 						}
 						
 						if (split.get(i).charAt(split.get(i).length()-1) != ')') {
 							i++;	//Move to semester offered
 							
 							if (split.get(i).equals("max")) {
 								i++;	//set overall max
 								if (split.get(i).length() == 2) {
 									overall = Double.parseDouble(""+split.get(i).charAt(0));
 								} else if (split.get(i).length() == 3) {
 									overall = Double.parseDouble(""+split.get(i).charAt(0)+split.get(i).charAt(1));
 								}
 								
 								if (split.get(i).charAt(split.get(i).length()-1) != ')') {
 									i++; //Move to semester offered. 
 								}
 							}
 							
 							if (split.get(i).contains("Sp") == true) {
 								spring = true;
 							} 
 							if (split.get(i).contains("Sm") == true) {
 								summer = true;
 							}
 							if (split.get(i).contains("Fa") == true) {
 								fall = true;
 							}
 						}
 						
 						unitFinished = true;
 						continue;
 					}
 					
 					//Single semester course, format (#,....) or (#) or (# or #,...) or (#, #, ...)
 					if (unitFinished == false && split.get(i).contains("-") == false && !(abcdz || abcd || abcz || abc || abz || ab || az) ) {
 						
 						units = Double.parseDouble(""+split.get(i).charAt(1));
 						maxUnits = units;
 						
 						if (split.get(i).charAt(split.get(i).length()-1) != ')') {
 							i++; 
 							while (true) {
 								if (split.get(i).charAt(split.get(i).length()-1) == ')') {
 									break;
 								}
 								
 								if (split.get(i).length() == 1) {		//example CSCI 562 "2 years"
 									break;
 								}
 								
 								if (isDigit(split.get(i).charAt(0)) == true) {
 									if (split.get(i).length() == 2) {			//example 3,
 										maxUnits = Double.parseDouble(""+split.get(i).charAt(0));
 									} else if (split.get(i).length() == 3) {	//example 12,
 										maxUnits = Double.parseDouble(""+split.get(i).charAt(0)+split.get(i).charAt(1));
 									} else if (split.get(i).length() == 4) {	//Example 1.5,
 										maxUnits = Double.parseDouble(""+split.get(i).charAt(0)+"."+split.get(i).charAt(2));	
 									}
 
 									if (split.get(i).charAt(split.get(i).length()-1) != ')') {
 										i++; //not end of units. 
 									}
 									continue;
 								}
 
 								if (split.get(i).equals("or")) {
 									i++;	//get the maxUnit
 									if (split.get(i).length() == 2) {
 										maxUnits = Double.parseDouble(""+split.get(i).charAt(0));
 									} else if (split.get(i).length() == 3) {
 										maxUnits = Double.parseDouble(""+split.get(i).charAt(0)+split.get(i).charAt(1));
 									}
 
 									if (split.get(i).charAt(split.get(i).length()-1) != ')') {
 										i++; //not end of units. 
 									}
 									continue;
 								}
 
 								if (split.get(i).equals("max")) {
 									i++;
 									if (split.get(i).length() == 2) {
 										overall = Double.parseDouble(""+split.get(i).charAt(0));
 									} else if (split.get(i).length() == 3) {
 										overall = Double.parseDouble(""+split.get(i).charAt(0)+split.get(i).charAt(1));
 									}
 
 									if (split.get(i).charAt(split.get(i).length()-1) != ')') {
 										i++; //Move to semester offered. 
 									}
 									break;
 								} 
 							}
 							
 							if (split.get(i).contains("Sp") == true) {
 								spring = true;
 							} 
 							if (split.get(i).contains("Sm") == true) {
 								summer = true;
 							}
 							if (split.get(i).contains("Fa") == true) {
 								fall = true;
 							}
 
 						}
 						
 						unitFinished = true;
 						continue;
 					}
 					//End of parsing units. 
 					
 					if (description == "") {
 						description = split.get(i);
 					} else {
 						description = description + " " + split.get(i);
 					}
 					
 					if (split.get(i).equals("z:")) {
 						desZ = true;
 					} else if (split.get(i).equals("d:")) {
 						desD = true;
 					} else if (split.get(i).equals("c:")) {
 						desC = true;
 					} else if (split.get(i).equals("b:")) {
 						desB = true;
 					} else if (split.get(i).equals("a:")) {
 						desA = true;
 					}
 					
 					if (desZ == true) {
 						if (descriptionZ == "") {
 							descriptionZ = split.get(i);
 						} else {
 							descriptionZ = descriptionZ + " " + split.get(i);
 						}
 					} else if (desD == true) {
 						if (descriptionD == "") {
 							descriptionD = split.get(i);
 						} else {
 							descriptionD = descriptionD + " " + split.get(i);
 						}
 					} else if (desC == true) {
 						if (descriptionC == "") {
 							descriptionC = split.get(i);
 						} else {
 							descriptionC = descriptionC + " " + split.get(i);
 						}
 					} else if (desB == true) {
 						if (descriptionB == "") {
 							descriptionB = split.get(i);
 						} else {
 							descriptionB = descriptionB + " " + split.get(i);
 						}
 					} else if (desA == true) {
 						if (descriptionA == "") {
 							descriptionA = split.get(i);
 						} else {
 							descriptionA = descriptionA + " " + split.get(i);
 						}
 					}
 				}
 				//Finish parsing course
 				
 				if (descriptionA == "") { descriptionA = description; }
 				if (descriptionB == "") { descriptionB = description; }
 				if (descriptionC == "") { descriptionC = description; }
 				if (descriptionD == "") { descriptionD = description; }
 				if (descriptionZ == "") { descriptionZ = description; }
 				
 				
 				if (overall == 0) {
 					overall = maxUnits;
 				}
 				
 				if (dept == "" || number == "") {
 					continue;
 				}
 				
 				if (units == 0 && unitsA == 0 && unitsB == 0 && unitsC == 0 && unitsD == 0 && unitsZ == 0) {
 					System.err.println(c);
 					throw (new Exception());
 				}
 				
 				if (maxUnits == 0 && maxUnitsA == 0 && maxUnitsB == 0 && maxUnitsC == 0 && maxUnitsD == 0 && maxUnitsZ == 0) {
 					System.err.println(c);
 					throw new Exception();
 				}
 				
 				if (abcdz == true) {
 					System.out.println("Department: " + dept);
 					System.out.println("NumberA: " + numberA);
 					System.out.println("NumberB: " + numberB);
 					System.out.println("NumberC: " + numberC);
 					System.out.println("NumberD: " + numberD);
 					System.out.println("NumberZ: " + numberZ);
 					System.out.println("Name: " + name);
 					System.out.println("UnitsA: " + unitsA);
 					System.out.println("UnitsB: " + unitsB);
 					System.out.println("UnitsC: " + unitsC);
 					System.out.println("UnitsD: " + unitsD);
 					System.out.println("UnitsZ: " + unitsZ);
 					System.out.println("Max UnitsA: " + maxUnitsA);
 					System.out.println("Max UnitsB: " + maxUnitsB);
 					System.out.println("Max UnitsC: " + maxUnitsC);
 					System.out.println("Max UnitsD: " + maxUnitsD);
 					System.out.println("Max UnitsZ: " + maxUnitsZ);
 					System.out.println("DescriptionA: " + descriptionA);
 					System.out.println("DescriptionB: " + descriptionB);
 					System.out.println("DescriptionC: " + descriptionC);
 					System.out.println("DescriptionD: " + descriptionD);
 					System.out.println("DescriptionZ: " + descriptionZ);
 					System.out.println("Offered in SpringA: " + springA);
 					System.out.println("Offered in SummerA: " + summerA);
 					System.out.println("Offered in FallA: " + fallA);
 					System.out.println("Offered in SpringB: " + springB);
 					System.out.println("Offered in SummerB: " + summerB);
 					System.out.println("Offered in FallB: " + fallB);
 					System.out.println("Offered in SpringC: " + springC);
 					System.out.println("Offered in SummerC: " + summerC);
 					System.out.println("Offered in FallC: " + fallC);
 					System.out.println("Offered in SpringD: " + springD);
 					System.out.println("Offered in SummerD: " + summerD);
 					System.out.println("Offered in FallD: " + fallD);
 					System.out.println("Offered in SpringZ: " + springZ);
 					System.out.println("Offered in SummerZ: " + summerZ);
 					System.out.println("Offered in FallZ: " + fallZ);
 				
 					deptList.add(new Course(dept, numberA, name, unitsA, maxUnitsA, maxUnitsA, descriptionA, springA, summerA, fallA));
 					deptList.add(new Course(dept, numberB, name, unitsB, maxUnitsB, maxUnitsB, descriptionB, springB, summerB, fallB));
 					deptList.add(new Course(dept, numberC, name, unitsC, maxUnitsC, maxUnitsC, descriptionC, springC, summerC, fallC));
 					deptList.add(new Course(dept, numberD, name, unitsD, maxUnitsD, maxUnitsD, descriptionD, springD, summerD, fallD));
 					deptList.add(new Course(dept, numberZ, name, unitsZ, maxUnitsZ, maxUnitsZ, descriptionZ, springZ, summerZ, fallZ));
 				} else if (abcd == true) {
 					System.out.println("Department: " + dept);
 					System.out.println("NumberA: " + numberA);
 					System.out.println("NumberB: " + numberB);
 					System.out.println("NumberC: " + numberC);
 					System.out.println("NumberD: " + numberD);
 					System.out.println("Name: " + name);
 					System.out.println("UnitsA: " + unitsA);
 					System.out.println("UnitsB: " + unitsB);
 					System.out.println("UnitsC: " + unitsC);
 					System.out.println("UnitsD: " + unitsD);
 					System.out.println("Max UnitsA: " + maxUnitsA);
 					System.out.println("Max UnitsB: " + maxUnitsB);
 					System.out.println("Max UnitsC: " + maxUnitsC);
 					System.out.println("Max UnitsD: " + maxUnitsD);
 					System.out.println("DescriptionA: " + descriptionA);
 					System.out.println("DescriptionB: " + descriptionB);
 					System.out.println("DescriptionC: " + descriptionC);
 					System.out.println("DescriptionD: " + descriptionD);
 					System.out.println("Offered in SpringA: " + springA);
 					System.out.println("Offered in SummerA: " + summerA);
 					System.out.println("Offered in FallA: " + fallA);
 					System.out.println("Offered in SpringB: " + springB);
 					System.out.println("Offered in SummerB: " + summerB);
 					System.out.println("Offered in FallB: " + fallB);
 					System.out.println("Offered in SpringC: " + springC);
 					System.out.println("Offered in SummerC: " + summerC);
 					System.out.println("Offered in FallC: " + fallC);
 					System.out.println("Offered in SpringD: " + springD);
 					System.out.println("Offered in SummerD: " + summerD);
 					System.out.println("Offered in FallD: " + fallD);
 					
 					deptList.add(new Course(dept, numberA, name, unitsA, maxUnitsA, maxUnitsA, descriptionA, springA, summerA, fallA));
 					deptList.add(new Course(dept, numberB, name, unitsB, maxUnitsB, maxUnitsB, descriptionB, springB, summerB, fallB));
 					deptList.add(new Course(dept, numberC, name, unitsC, maxUnitsC, maxUnitsC, descriptionC, springC, summerC, fallC));
 					deptList.add(new Course(dept, numberD, name, unitsD, maxUnitsD, maxUnitsD, descriptionD, springD, summerD, fallD));
 				} else if (abcz == true) {
 					System.out.println("Department: " + dept);
 					System.out.println("NumberA: " + numberA);
 					System.out.println("NumberB: " + numberB);
 					System.out.println("NumberC: " + numberC);
 					System.out.println("NumberZ: " + numberZ);
 					System.out.println("Name: " + name);
 					System.out.println("UnitsA: " + unitsA);
 					System.out.println("UnitsB: " + unitsB);
 					System.out.println("UnitsC: " + unitsC);
 					System.out.println("UnitsZ: " + unitsZ);
 					System.out.println("Max UnitsA: " + maxUnitsA);
 					System.out.println("Max UnitsB: " + maxUnitsB);
 					System.out.println("Max UnitsC: " + maxUnitsC);
 					System.out.println("Max UnitsZ: " + maxUnitsZ);
 					System.out.println("DescriptionA: " + descriptionA);
 					System.out.println("DescriptionB: " + descriptionB);
 					System.out.println("DescriptionC: " + descriptionC);
 					System.out.println("DescriptionZ: " + descriptionZ);
 					System.out.println("Offered in SpringA: " + springA);
 					System.out.println("Offered in SummerA: " + summerA);
 					System.out.println("Offered in FallA: " + fallA);
 					System.out.println("Offered in SpringB: " + springB);
 					System.out.println("Offered in SummerB: " + summerB);
 					System.out.println("Offered in FallB: " + fallB);
 					System.out.println("Offered in SpringC: " + springC);
 					System.out.println("Offered in SummerC: " + summerC);
 					System.out.println("Offered in FallC: " + fallC);
 					System.out.println("Offered in SpringZ: " + springZ);
 					System.out.println("Offered in SummerZ: " + summerZ);
 					System.out.println("Offered in FallZ: " + fallZ);
 					
 					deptList.add(new Course(dept, numberA, name, unitsA, maxUnitsA, maxUnitsA, descriptionA, springA, summerA, fallA));
 					deptList.add(new Course(dept, numberB, name, unitsB, maxUnitsB, maxUnitsB, descriptionB, springB, summerB, fallB));
 					deptList.add(new Course(dept, numberC, name, unitsC, maxUnitsC, maxUnitsC, descriptionC, springC, summerC, fallC));
 					deptList.add(new Course(dept, numberZ, name, unitsZ, maxUnitsZ, maxUnitsZ, descriptionZ, springZ, summerZ, fallZ));
 				} else if (abc == true) {
 					System.out.println("Department: " + dept);
 					System.out.println("NumberA: " + numberA);
 					System.out.println("NumberB: " + numberB);
 					System.out.println("NumberC: " + numberC);
 					System.out.println("Name: " + name);
 					System.out.println("UnitsA: " + unitsA);
 					System.out.println("UnitsB: " + unitsB);
 					System.out.println("UnitsC: " + unitsC);
 					System.out.println("Max UnitsA: " + maxUnitsA);
 					System.out.println("Max UnitsB: " + maxUnitsB);
 					System.out.println("Max UnitsC: " + maxUnitsC);
 					System.out.println("DescriptionA: " + descriptionA);
 					System.out.println("DescriptionB: " + descriptionB);
 					System.out.println("DescriptionC: " + descriptionC);
 					System.out.println("Offered in SpringA: " + springA);
 					System.out.println("Offered in SummerA: " + summerA);
 					System.out.println("Offered in FallA: " + fallA);
 					System.out.println("Offered in SpringB: " + springB);
 					System.out.println("Offered in SummerB: " + summerB);
 					System.out.println("Offered in FallB: " + fallB);
 					System.out.println("Offered in SpringC: " + springC);
 					System.out.println("Offered in SummerC: " + summerC);
 					System.out.println("Offered in FallC: " + fallC);
 					
 					deptList.add(new Course(dept, numberA, name, unitsA, maxUnitsA, maxUnitsA, descriptionA, springA, summerA, fallA));
 					deptList.add(new Course(dept, numberB, name, unitsB, maxUnitsB, maxUnitsB, descriptionB, springB, summerB, fallB));
 					deptList.add(new Course(dept, numberC, name, unitsC, maxUnitsC, maxUnitsC, descriptionC, springC, summerC, fallC));
 				} else if (abz == true) {
 					System.out.println("Department: " + dept);
 					System.out.println("NumberA: " + numberA);
 					System.out.println("NumberB: " + numberB);
 					System.out.println("NumberZ: " + numberZ);
 					System.out.println("Name: " + name);
 					System.out.println("UnitsA: " + unitsA);
 					System.out.println("UnitsB: " + unitsB);
 					System.out.println("UnitsZ: " + unitsZ);
 					System.out.println("Max UnitsA: " + maxUnitsA);
 					System.out.println("Max UnitsB: " + maxUnitsB);
 					System.out.println("Max UnitsZ: " + maxUnitsZ);
 					System.out.println("DescriptionA: " + descriptionA);
 					System.out.println("DescriptionB: " + descriptionB);
 					System.out.println("DescriptionZ: " + descriptionZ);
 					System.out.println("Offered in SpringA: " + springA);
 					System.out.println("Offered in SummerA: " + summerA);
 					System.out.println("Offered in FallA: " + fallA);
 					System.out.println("Offered in SpringB: " + springB);
 					System.out.println("Offered in SummerB: " + summerB);
 					System.out.println("Offered in FallB: " + fallB);
 					System.out.println("Offered in SpringZ: " + springZ);
 					System.out.println("Offered in SummerZ: " + summerZ);
 					System.out.println("Offered in FallZ: " + fallZ);
 					
 					deptList.add(new Course(dept, numberA, name, unitsA, maxUnitsA, maxUnitsA, descriptionA, springA, summerA, fallA));
 					deptList.add(new Course(dept, numberB, name, unitsB, maxUnitsB, maxUnitsB, descriptionB, springB, summerB, fallB));
 					deptList.add(new Course(dept, numberZ, name, unitsZ, maxUnitsZ, maxUnitsZ, descriptionZ, springZ, summerZ, fallZ));
 				} else if (ab == true ) {
 					System.out.println("Department: " + dept);
 					System.out.println("NumberA: " + numberA);
 					System.out.println("NumberB: " + numberB);
 					System.out.println("Name: " + name);
 					System.out.println("UnitsA: " + unitsA);
 					System.out.println("UnitsB: " + unitsB);
 					System.out.println("Max UnitsA: " + maxUnitsA);
 					System.out.println("Max UnitsB: " + maxUnitsB);
 					System.out.println("DescriptionA: " + descriptionA);
 					System.out.println("DescriptionB: " + descriptionB);
 					System.out.println("Offered in SpringA: " + springA);
 					System.out.println("Offered in SummerA: " + summerA);
 					System.out.println("Offered in FallA: " + fallA);
 					System.out.println("Offered in SpringB: " + springB);
 					System.out.println("Offered in SummerB: " + summerB);
 					System.out.println("Offered in FallB: " + fallB);
 
 					deptList.add(new Course(dept, numberA, name, unitsA, maxUnitsA, maxUnitsA, descriptionA, springA, summerA, fallA));
 					deptList.add(new Course(dept, numberB, name, unitsB, maxUnitsB, maxUnitsB, descriptionB, springB, summerB, fallB));
 				
 				} else if (az == true ) {
 					System.out.println("Department: " + dept);
 					System.out.println("NumberA: " + numberA);
 					System.out.println("NumberZ: " + numberZ);
 					System.out.println("Name: " + name);
 					System.out.println("UnitsA: " + unitsA);
 					System.out.println("UnitsZ: " + unitsZ);
 					System.out.println("Max UnitsA: " + maxUnitsA);
 					System.out.println("Max UnitsZ: " + maxUnitsZ);
 					System.out.println("DescriptionA: " + descriptionA);
 					System.out.println("DescriptionZ: " + descriptionZ);
 					System.out.println("Offered in SpringA: " + springA);
 					System.out.println("Offered in SummerA: " + summerA);
 					System.out.println("Offered in FallA: " + fallA);
 					System.out.println("Offered in SpringZ: " + springZ);
 					System.out.println("Offered in SummerZ: " + summerZ);
 					System.out.println("Offered in FallZ: " + fallZ);
 
 					deptList.add(new Course(dept, numberA, name, unitsA, maxUnitsA, maxUnitsA, descriptionA, springA, summerA, fallA));
 					deptList.add(new Course(dept, numberZ, name, unitsZ, maxUnitsZ, maxUnitsZ, descriptionZ, springZ, summerZ, fallZ));
 				
 				} else {
 					System.out.println("Department: " + dept);
 					System.out.println("Number: " + number);
 					System.out.println("Name: " + name);
 					System.out.println("Units: " + units);
 					System.out.println("Max Units: " + maxUnits);
 					System.out.println("Overall Max: " + overall);
 					System.out.println("Description: " + description);
 					System.out.println("Offered in Spring: " + spring);
 					System.out.println("Offered in Summer: " + summer);
 					System.out.println("Offered in Fall: " + fall);
 
 					deptList.add(new Course(dept, number, name, units, maxUnits, overall, description, spring, summer, fall));
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				
 				errorList.add(c);
 			}
 		}
 		
 		
 		//outputting data to file. 
 		try {
 			PrintStream out = new PrintStream(new FileOutputStream("output/" + deptList.get(0).getDept() + ".txt"));
 			
 			for (Course c : deptList) {
 				out.println(c.getCode() + "|" + c.getName() + "|" + c.getUnits() + "|" + c.getMaxUnits());
 			}
 			
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		if (errorList.size() > 0) {
 			try {
 				PrintStream out = new PrintStream(new FileOutputStream("output/error_" + deptList.get(0).getDept() + ".txt"));
 				
 				for (String s : errorList) {
 					out.println(s);
 					out.println();
 				}
 				
 				out.close();
 				
 				System.out.println("Error(s) outputted to error_" + deptList.get(0).getDept() + ".txt");
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("Finished parsing... File outputted to " + deptList.get(0).getDept() + ".txt");
 		//Finished outputting to file. 
 	}
 	
 	public static boolean isDigit(char c) {
 		return ((c == '0') 
 				|| (c == '1') 
 				|| (c == '2') 
 				|| (c == '3') 
 				|| (c == '4') 
 				|| (c == '5') 
 				|| (c == '6') 
 				|| (c == '7') 
 				|| (c == '8') 
 				|| (c == '9')
 				|| (c == '.'));		//For the decimal. 
 	}
 	
 }
