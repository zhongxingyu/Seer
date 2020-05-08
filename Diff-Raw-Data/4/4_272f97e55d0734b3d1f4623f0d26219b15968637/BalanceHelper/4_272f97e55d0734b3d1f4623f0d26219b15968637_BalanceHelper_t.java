 /**
  * 
  */
 package de.aidger.utils.reports;
 
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Vector;
 
 import de.aidger.model.Runtime;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.model.reports.BalanceCourse;
 import de.aidger.model.reports.BalanceFilter;
 import de.aidger.view.UI;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 import de.unistuttgart.iste.se.adohive.model.IEmployment;
 import de.unistuttgart.iste.se.adohive.model.IHourlyWage;
 
 /**
  * This class is used to get all the existing semesters and years.
  * 
  * @author aidGer Team
  */
 public class BalanceHelper {
 
     /**
      * Initializes a new BalanceHelper.
      */
     public BalanceHelper() {
     }
 
     /**
      * Filters the given courses using the given filters.
      * 
      * @param courses
      *            The courses to filter.
      * @param filters
      *            The filters to use.
      * @return The filtered courses
      */
     public List<ICourse> filterCourses(List<ICourse> courses,
             BalanceFilter filters) {
         List<ICourse> filteredOnceCourses = new Vector<ICourse>();
         List<ICourse> filteredTwiceCourses = new Vector<ICourse>();
         List<ICourse> filteredTriceCourses = new Vector<ICourse>();
         /*
          * Only use courses, which have the filtered criteria.
          */
         if (!(filters == null)) {
             boolean filterExists = false;
             /*
              * There are existing filters.
              */
             if (!filters.getGroups().isEmpty()) {
                 /*
                  * There are existing group filters.
                  */
                 for (Object group : filters.getGroups()) {
                     for (ICourse course : courses) {
                         if (!filteredOnceCourses.contains(course)
                                 && course.getGroup().equals(group)) {
                             /*
                              * The course is not already in the filtered courses
                              * and meets the group criteria.
                              */
                             filteredOnceCourses.add(course);
                         }
                     }
                 }
                 filterExists = true;
             } else {
                 filteredOnceCourses = courses;
             }
             if (!filters.getLecturers().isEmpty()) {
                 /*
                  * There are existing lecture filters.
                  */
                 for (Object lecturer : filters.getLecturers()) {
                     for (ICourse course : filteredOnceCourses) {
                         if (!filteredTwiceCourses.contains(course)
                                 && course.getLecturer().equals(lecturer)) {
                             /*
                              * The course is not already in the filtered courses
                              * and meets the lecturer criteria.
                              */
                             filteredTwiceCourses.add(course);
                         }
                     }
                 }
                 filterExists = true;
             } else {
                 filteredTwiceCourses = filteredOnceCourses;
             }
             if (!filters.getTargetAudiences().isEmpty()) {
                 /*
                  * There are existing target audience filters.
                  */
                 for (Object lecturer : filters.getTargetAudiences()) {
                     for (ICourse course : filteredTwiceCourses) {
                         if (!filteredTriceCourses.contains(course)
                                 && course.getTargetAudience().equals(lecturer)) {
                             /*
                              * The course is not already in the filtered courses
                              * and meets the target audience criteria.
                              */
                             filteredTriceCourses.add(course);
                         }
                     }
                 }
                 filterExists = true;
             } else {
                 filteredTriceCourses = filteredTwiceCourses;
             }
             if (!filterExists) {
                 filteredTriceCourses = courses;
             }
         } else {
             /*
              * If there are no filters, use the normal courses.
              */
             filteredTriceCourses = courses;
         }
         return filteredTriceCourses;
     }
 
     /**
      * Gets all the years of a semester.
      * 
      * @param year
      *            The semester to check.
      * @return The years of the semester.
      */
     public String[] getYearSemesters(int year) {
         // Lose the first two numbers of the year
         int semester = year % 100;
         /*
          * Contains the year in YYYY form, the previous, current and next
          * semester in that order.
          */
         String[] semesters = new String[4];
         semesters[0] = "" + year;
         switch (semester) {
         /*
          * If the given year is 2001-2008, (year % 100) will give a single
          * number below 9. Therefore, the previous, current and next semester
          * all need a leading 0 added.
          */
         case 0:
             semesters[1] = "WS" + "99" + "00";
             semesters[2] = "SS" + "00";
             semesters[3] = "WS" + "00" + "01";
             break;
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
             semesters[1] = "WS0" + (semester - 1) + "0" + semester;
             semesters[2] = "SS0" + semester;
             semesters[3] = "WS0" + semester + "0" + (semester + 1);
             break;
         /*
          * If the given year is 2009, the previous and current semester will
          * both be a single number and therefore need a leading 0 added. The
          * next semester will be 10 and thus needs no adjustments.
          */
         case 9:
             semesters[1] = "WS0" + (semester - 1) + "0" + semester;
             semesters[2] = "SS0" + semester;
             semesters[3] = "WS0" + semester + (semester + 1);
             break;
         /*
          * If the given year is 2010, the current and next semesters will be 10
          * and 11 and therefore don't need a leading 0. The previous semester
          * will be 9 though.
          */
         case 10:
             semesters[1] = "WS0" + (semester - 1) + semester;
             semesters[2] = "SS" + semester;
             semesters[3] = "WS" + semester + (semester + 1);
             break;
         case 99:
             semesters[1] = "WS" + (semester - 1) + semester;
             semesters[2] = "SS" + semester;
             semesters[3] = "WS" + semester + "00";
             break;
         /*
          * In all other relevant cases (11 and higher), the semesters can be
          * used the way (year % 100) returns them.
          */
         default:
             semesters[1] = "WS" + (semester - 1) + semester;
             semesters[2] = "SS" + semester;
             semesters[3] = "WS" + semester + (semester + 1);
             break;
         }
         return semesters;
     }
 
     /**
      * Calculates the data relevant for the balance into a balance course model.
      * Returns that balance course model.
      * 
      * @param course
      *            The course to be calculated.
      * @return The balance course model.
      */
     public static BalanceCourse getBalanceCourse(ICourse course) {
         BalanceCourse balanceCourse = new BalanceCourse();
         balanceCourse.setTitle(course.getDescription());
         balanceCourse.setPart(course.getPart());
         balanceCourse.setLecturer(course.getLecturer());
         balanceCourse.setTargetAudience(course.getTargetAudience());
         double plannedAWS = 0;
         double basicAWS = course.getNumberOfGroups()
                 * course.getUnqualifiedWorkingHours();
         balanceCourse.setBasicAWS(basicAWS);
         List<IEmployment> employments = null;
         List<IAssistant> assistants = null;
         try {
             employments = (new Employment()).getAll();
             assistants = (new Assistant()).getAll();
         } catch (Exception e) {
             e.printStackTrace();
         }
         for (IEmployment employment : employments) {
             /*
              * Sum up the budget costs of the course by multiplying the hours of
              * the fitting employments.
              */
             if (course.getId() == employment.getCourseId()
                     && (balanceCourse.getBudgetCosts().isEmpty() || !balanceCourse
                         .getBudgetCosts().contains(employment.getFunds()))) {
                 Double budgetCost = 0.0;
                 for (IAssistant assistant : assistants) {
                     if (employment.getAssistantId() == assistant.getId()) {
                         budgetCost = budgetCost
                                 + calculateBudgetCost(employment);
                     }
                 }
                 if (balanceCourse.budgetCostExists(employment.getFunds())) {
                     balanceCourse.addBudgetCostvalue(employment.getFunds(),
                         budgetCost.doubleValue());
                 } else {
                     balanceCourse.addBudgetCost(employment.getFunds(),
                         employment.getCostUnit(), budgetCost.doubleValue());
                 }
                 plannedAWS = plannedAWS + employment.getHourCount();
             }
         }
         balanceCourse.setPlannedAWS(new BigDecimal(plannedAWS).setScale(2,
             BigDecimal.ROUND_HALF_EVEN).doubleValue());
         return balanceCourse;
     }
 
     /**
      * Calculates the budget costs of this employment
      */
     public static double calculateBudgetCost(IEmployment employment) {
         String qualification = employment.getQualification();
         double calculationFactor = 1.0;
         double calculationMethod = Integer.parseInt(Runtime.getInstance()
             .getOption("calc-method"));
         if (calculationMethod == 1) {
             calculationFactor = Double.parseDouble(de.aidger.model.Runtime
                 .getInstance().getOption("pessimistic-factor"));
         } else {
             calculationFactor = Double.parseDouble(de.aidger.model.Runtime
                 .getInstance().getOption("historic-factor"));
         }
         List<IHourlyWage> hourlyWages;
         try {
             hourlyWages = new HourlyWage().getAll();
             for (IHourlyWage hourlyWage : hourlyWages) {
                if (hourlyWage.getMonth().equals(employment.getMonth())
                        && hourlyWage.getYear().equals(employment.getYear())
                         && hourlyWage.getQualification().equals(qualification)) {
                     return hourlyWage.getWage().doubleValue()
                             * calculationFactor * employment.getHourCount();
                 }
             }
         } catch (AdoHiveException e) {
             UI.displayError(e.toString());
         }
         return 0;
     }
 
     /**
      * Calculates the budget costs of this employment as pre-tax.
      */
     public static double calculatePreTaxBudgetCost(IEmployment employment) {
         String qualification = employment.getQualification();
         double calculationFactor = 1.0;
         List<IHourlyWage> hourlyWages;
         try {
             hourlyWages = new HourlyWage().getAll();
             for (IHourlyWage hourlyWage : hourlyWages) {
                 if (hourlyWage.getMonth() == employment.getMonth()
                         && hourlyWage.getYear() == employment.getYear()
                         && hourlyWage.getQualification().equals(qualification)) {
                     return hourlyWage.getWage().doubleValue()
                             * calculationFactor * employment.getHourCount();
                 }
             }
         } catch (AdoHiveException e) {
             UI.displayError(e.toString());
         }
         return 0;
     }
 
     /**
      * Checks all the courses for their semesters and returns all the semesters
      * afterwards.
      * 
      * @return A Vector containing the semesters as Strings.
      */
     public Vector<String> getSemesters() {
         Vector<String> semesters = new Vector<String>();
         List<ICourse> courses = null;
         /*
          * Add an empty semester string as the first entry. Relevant for the
          * combo boxes.
          */
         try {
             courses = (new Course()).getAll();
         } catch (AdoHiveException e) {
             e.printStackTrace();
         }
         for (ICourse course : courses) {
             if (!semesters.contains(course.getSemester())) {
                 semesters.add(course.getSemester());
             }
         }
         return semesters;
     }
 
     /**
      * Checks all the semesters for their years and returns the years as a
      * vector afterwards.
      * 
      * @return The vector of years as ints.
      */
     public Vector<Integer> getYears() {
         Vector<String> semesters = getSemesters();
         if (semesters.size() > 0) {
             /*
              * Only get the years, if there are any valid courses with a
              * semester.
              */
             Vector<Integer> years = new Vector<Integer>();
             /*
              * Check for every semester out of the semester vector, if the year
              * of that semester is already noted and add it if it's not.
              */
             for (Object semester : semesters) {
                 char[] semesterChar = ((String) semester).toCharArray();
                 int year = 0;
                 if (Character.isDigit(semesterChar[0])) {
                     // The semester is in the form YYYY.
                     for (int i = 0; i < semesterChar.length; i++) {
                         year = year
                                 + Character.getNumericValue(semesterChar[i])
                                 * (int) Math.pow(10, 3 - i);
                     }
                 } else {
                     // The semester is in the form SSYY or WSYYYY.
                     int i = 0;
                     while (!Character.isDigit(semesterChar[i])) {
                         i++;
                     }
                     int power = 0;
                     switch (semesterChar.length - i) {
                     case 2:
                         // The semester is in the form SSYY
                         year = 2000;
                         power = 10;
                         for (int j = i; j < semesterChar.length; j++) {
                             year = year
                                     + Character
                                         .getNumericValue(semesterChar[j])
                                     * power;
                             if (power == 10) {
                                 power = 1;
                             } else {
                                 power = 10;
                             }
                         }
                         break;
                     case 4:
                         /*
                          * The semester is in the form WSYYYY. Both semester
                          * years must be checked
                          */
                         for (int l = 0; l < 3; l = l + 2) {
                             year = 2000;
                             power = 10;
                             for (int j = i + l; j < semesterChar.length
                                     - (2 - l); j++) {
                                 year = year
                                         + Character
                                             .getNumericValue(semesterChar[j])
                                         * power;
                                 if (power == 10) {
                                     power = 1;
                                 } else {
                                     power = 10;
                                 }
                             }
                             if (!years.contains(year)) {
                                 years.add(year);
                             }
                         }
                         break;
                     }
                 }
                 if (!years.contains(year)) {
                     years.add(year);
                 }
             }
             Vector<Integer> sortedYears = new Vector<Integer>();
             sortedYears.add(years.get(0));
             for (int i = 1; i < years.size(); i++) {
                 boolean addedYear = false;
                 for (int j = 0; j < sortedYears.size(); j++) {
                     if ((Integer) years.get(i) <= (Integer) sortedYears.get(j)) {
                         sortedYears.add(j, years.get(i));
                         addedYear = true;
                         break;
                     }
                 }
                 if (!addedYear) {
                     sortedYears.add(years.get(i));
                 }
             }
             return sortedYears;
         } else {
             /*
              * There are no valid courses. Return empty vector.
              */
             Vector<Integer> years = new Vector<Integer>();
             return years;
         }
     }
 
     /**
      * Checks if the given semester contains any courses.
      * 
      * @param semester
      *            The semester to check
      * @return true if the semester contains one or more courses.
      */
     public boolean courseExists(String semester, BalanceFilter filters) {
         List<ICourse> courses = null;
         try {
             courses = (new Course()).getAll();
         } catch (AdoHiveException e) {
             UI.displayError(e.toString());
         }
         List<ICourse> filteredCourses = this.filterCourses(courses, filters);
         for (ICourse course : filteredCourses) {
             if (course.getSemester().equals(semester)) {
                 return true;
             }
         }
         return false;
     }
 }
