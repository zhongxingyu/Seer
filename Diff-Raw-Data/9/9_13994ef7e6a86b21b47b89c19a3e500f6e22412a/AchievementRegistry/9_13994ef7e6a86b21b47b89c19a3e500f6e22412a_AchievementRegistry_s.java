 package dashboard.registry;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.ServletContext;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 import dashboard.error.NoSuchAchievementException;
 import dashboard.model.Course;
 import dashboard.model.Student;
 import dashboard.model.StudyMoment;
 import dashboard.model.achievement.Achievement;
 import dashboard.model.achievement.ComboAchievement;
 import dashboard.util.CalendarToDateConverter;
 
 public class AchievementRegistry {
 
 	private static ArrayList<Achievement> achievementList;
 	private static boolean started;
 	
 	static{
 		achievementList = new ArrayList<Achievement>();
 	}
 	
 	/**
 	 * fills the achievement list with all possible achievements		
 	 * @throws IOException 
 	 * @throws JDOMException 
 	 */
 	private static void fillAchievementList(ServletContext context) throws JDOMException, IOException{
 		achievementList.addAll(getTimeStudiedAchievements());
 		InputStream is = context.getResourceAsStream("/WEB-INF/achievements.xml");
 		Document doc = new SAXBuilder().build(is);
 		Element root = doc.getRootElement();
 		for(Element ae: root.getChildren("achievement")){
 			achievementList.add(getAchievementFromElement(ae));
 		}
 	}
 	
 	private static Achievement getAchievementFromElement(Element ae){
 		try{
 			return getByID(ae.getChildText("id"));
 		} catch(NoSuchAchievementException e){
 			String id = ae.getChildText("id");
 			String name = ae.getChildText("name");
 			String desc = ae.getChildText("desc");
 			Course course = CourseRegistry.getCourseByID(ae.getChildText("Course"));
 			String icon = ae.getChildText("icon");
 			boolean visible = ae.getChildText("visible").equals("true");
 			Achievement achievement = new Achievement(id, name, desc, course, icon, visible);
 			if(ae.getChildText("combo").equals("true")){
 				int needed = Integer.valueOf(ae.getChildText("needed"));
 				ArrayList<Achievement> cAchievementList = new ArrayList<Achievement>();
 				for(Element c: ae.getChildren("subachievement")){
 					try{
 						cAchievementList.add(getByID(c.getText()));
 					} catch (NoSuchAchievementException e1){
 						e1.printStackTrace();
 					}
 				}
 				return new ComboAchievement(id,name,desc,course,icon,visible,cAchievementList,needed);
 			}
 			if(ae.getChildText("needTime")!= null && ae.getChildText("needTime").equals("true"))
 				achievement.addTimeRequirement(Long.valueOf(ae.getChildText("time")));
 			if(ae.getChildText("needNumber")!= null && ae.getChildText("needNumber").equals("true"))
 				achievement.addNumberRequirement(Integer.valueOf(ae.getChildText("number")));
 			if(ae.getChildText("needPeriod")!= null && ae.getChildText("needPeriod").equals("true")){
 				Date start = readDate(ae, "start");
 				Date end = readDate(ae, "end");
 				achievement.addPeriodRequirement(start, end);
 			}
 			if(ae.getChildText("needExpiration")!= null && ae.getChildText("needExpiration").equals("true"))
 				achievement.addExpirationRequirement(readDate(ae, "end"));
 			if(ae.getChildText("needRepeating")!= null && ae.getChildText("needRepeating").equals("true"))
 				achievement.addRepeatingRequirement(ae.getChildText("reccuring"));
 			if(ae.getChildText("needLocations")!= null && ae.getChildText("needLocations").equals("true"))
 				achievement.addLocationRequirement(Integer.valueOf(ae.getChildText("numberOfLocations")));
 			if(ae.getChildText("needPages")!= null && ae.getChildText("needPages").equals("true"))
 				achievement.addPagesRequirement(Integer.valueOf(ae.getChildText("numberOfPages")));
 			if(ae.getChildText("needExercices")!= null && ae.getChildText("needExercices").equals("true"))
 				achievement.addExercicesRequirement(Integer.valueOf(ae.getChildText("numberOfExercices")));
 			if(ae.getChildText("needFriends")!= null && ae.getChildText("needFriends").equals("true"))
				achievement.addExercicesRequirement(Integer.valueOf(ae.getChildText("numberOfFriends")));
 			return achievement;
 		}
 	}
 	
 	private static Date readDate(Element ae, String prefix){
 		Element top = ae.getChild(prefix);
 		return CalendarToDateConverter.getDate(Integer.parseInt(top.getChildText("year")),
 				Integer.parseInt(top.getChildText("month")),Integer.parseInt(top.getChildText("day")),
 				Integer.parseInt(top.getChildText("hour")),Integer.parseInt(top.getChildText("minute")),
 				Integer.parseInt(top.getChildText("second")));
 	}
 	
 	private static int getRepeatingTime(String s){
 		if(s.equals("daily")){
 			return Calendar.DAY_OF_YEAR;
 		} else if(s.equals("weekly")){
 			return Calendar.WEEK_OF_YEAR;
 		} else if(s.equals("monthly")){
 			return Calendar.MONTH;
 		} else if(s.equals("yearly")){
 			return Calendar.YEAR;
 		} else {
 			return 0;
 		}
 	}
 
 	private static ArrayList<Achievement> getTimeStudiedAchievements(){
 		ArrayList<Achievement> timeStudiedList = new ArrayList<Achievement>();
 		Iterator<Course> it = CourseRegistry.getAllCourses().iterator();
 		while(it.hasNext()){
 			Course course = it.next();
 			Achievement ts1 = new Achievement("TIME_STUDIED_" + course.name() + "_1", course.getName() + " noob", "Studeer " + 2 * course.getCredit() + " minuten voor " + course.getName(), course, "noob.png", true);
 			ts1.addTimeRequirement(120 * course.getCredit());
 			Achievement ts2 = new Achievement("TIME_STUDIED_" + course.name() + "_2", course.getName() + " novice", "Studeer " + 10 * course.getCredit() + " minuten voor " + course.getName(), course, "novice.png",true);
 			ts2.addTimeRequirement(600 * course.getCredit());
 			Achievement ts3 = new Achievement("TIME_STUDIED_" + course.name() + "_3", course.getName() + " apprentice", "Studeer " + 1 * course.getCredit() + " uur voor " + course.getName(), course, "apprentice.png", true);
 			ts3.addTimeRequirement(3600 * course.getCredit());
 			Achievement ts4 = new Achievement("TIME_STUDIED_" + course.name() + "_4", course.getName() + " expert", "Studeer " + 4 * course.getCredit() + " uur voor " + course.getName(), course, "expert.png", true);
 			ts4.addTimeRequirement(14400 * course.getCredit());
 			Achievement ts5 = new Achievement("TIME_STUDIED_" + course.name() + "_5", course.getName() + " pro", "Studeer " + 10 * course.getCredit() + " uur voor " + course.getName(), course, "pro.png", true);
 			ts5.addTimeRequirement(36000 * course.getCredit());
 			Achievement ts6 = new Achievement("TIME_STUDIED_" + course.name() + "_6", course.getName() + " zombie", "Studeer " + 35 * course.getCredit() + " uur voor " + course.getName(), course, "zombie.png", true);
 			ts6.addTimeRequirement(126000 * course.getCredit());
 			timeStudiedList.addAll(Arrays.asList(ts1,ts2,ts3,ts4,ts5,ts6));
 		}
 		return timeStudiedList;
 	}
 	
 	/**
 	 * returns a hashmap containing a course key corresponding to an ArrayList containing all achievements bound to that course applicable to the student
 	 * @param student
 	 * @return
 	 */
 	public static HashMap<Course,ArrayList<Achievement>> getAchievements(Student student){
 		HashMap<Course,ArrayList<Achievement>> achievementMap = new HashMap<Course,ArrayList<Achievement>>();
 		ArrayList<Achievement> noCourseAchievementList = new ArrayList<Achievement>();
 		for(Achievement achievement: achievementList){
 			if(achievement.getCourse()==null){
 				noCourseAchievementList.add(achievement);
 			} else if(student.getCourseList().contains(achievement.getCourse())){
 				if(achievementMap.containsKey((achievement).getCourse())){
 					achievementMap.get(achievement.getCourse()).add(achievement);
 				} else {
 					ArrayList<Achievement> newAchievementList = new ArrayList<Achievement>();
 					newAchievementList.add(achievement);
 					achievementMap.put(achievement.getCourse(),newAchievementList);
 				}
 			}
 		}
 		achievementMap.put(null,noCourseAchievementList);
 		return achievementMap;
 	}
 	
 	public static ArrayList<Achievement> getCompletedAchievements(Student student){
 		ArrayList<Achievement> completedAchievementList = new ArrayList<Achievement>();
 		for(Achievement achievement: achievementList){
 			if(achievement.getProgress(student)>=1){
 				completedAchievementList.add(achievement);
 			}
 		}
 		return completedAchievementList;
 	}
 	
 	public static Achievement getByID(String id) throws NoSuchAchievementException{
 		Iterator<Achievement> it = achievementList.iterator();
 		while(it.hasNext()){
 			Achievement achievement = it.next();
 			if(achievement.getId().equals(id)){
 				return achievement;
 			}
 		}
 		throw new NoSuchAchievementException();
 	}
 
 	public static void init(ServletContext servletContext) {
 		started = true;
 		try {
 			fillAchievementList(servletContext);
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static boolean started(){
 		return started;
 	}
 	
 	public static ArrayList<Achievement> getChangedAchievements(Student student, Student oldStudent){
 		ArrayList<Achievement> changedAchievements = new ArrayList<Achievement>();
 		for(Achievement achievement: achievementList){
 			if(student.getCourseList().contains(achievement.getCourse()) || achievement.getCourse() == null){
 				float old = achievement.getProgress(oldStudent);
 				float nieuw = achievement.getProgress(student);
 				if(achievement.getProgress(oldStudent) != achievement.getProgress(student)){
 					changedAchievements.add(achievement);
 				}
 			}
 		}
 		return changedAchievements;
 	}
 }
