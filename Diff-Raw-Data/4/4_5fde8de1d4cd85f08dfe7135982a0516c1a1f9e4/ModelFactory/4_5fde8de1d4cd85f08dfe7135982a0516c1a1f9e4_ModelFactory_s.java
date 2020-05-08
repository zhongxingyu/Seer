 package melt.cs.manchester.factory;
 
 import java.util.LinkedList;
 import java.util.List;
 import melt.cs.manchester.logic.Assessment;
 import melt.cs.manchester.logic.Choice;
 import melt.cs.manchester.logic.FillInBlank;
 import melt.cs.manchester.logic.MultipleChoice;
 import melt.cs.manchester.logic.Question;
 import melt.cs.manchester.logic.Section;
 import melt.cs.manchester.logic.SubSection;
 
 /**
  *
  * @author CipherHat
  * @author Ruvin for reviewing and implementing algorithms
  */
 public class ModelFactory {
 
     private List<Assessment> assessmentList = new LinkedList<Assessment>();
 
     public ModelFactory() {
         // TODO Auto-generated constructor stub
     }
 
     //<editor-fold defaultstate="collapsed" desc="Assessment">
     /**
      *
      * @param name cannot be NULL
      * @param instruction can be NULL
      * @param code can be NULL
      * @param timeInterval can be NULL, but the input required is int[3] with
      * {hour:min:secs} format. Hour <= 23 and >= 0, Hour and Minute <=59 and >=0
      * @throws Exception if any condition for the input is not met
      */
     public void createAssessment(String name, String instruction, String code, int[] timeInterval) throws Exception {
         if (name == null) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         Assessment assessment = new Assessment();
         assessmentList.add(assessment);
         setAssessmentName(assessmentList.size(), name);
         setAssessmentInstruction(assessmentList.size(), instruction);
         setAssessmentCode(assessmentList.size(), code);
         setAssessmentTimeInterval(assessmentList.size(), timeInterval);
     }
 
     public void setAssessmentName(int position, String name) throws Exception {
         if (!isAssessmentCreated(position - 1)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         if (name == null) {
             throw new Exception("Cannot set null name to the Assessment");
         }
         assessmentList.get(position - 1).setName(name);
     }
 
     public void setAssessmentInstruction(int position, String instruction) throws Exception {
         if (!isAssessmentCreated(position - 1)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         assessmentList.get(position - 1).setInstruction(instruction);
     }
 
     public void setAssessmentCode(int position, String code) throws Exception {
         if (!isAssessmentCreated(position - 1)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         assessmentList.get(position - 1).setCode(code);
     }
 
     /**
      * @author CipherHat implement the method structure
      * @author Ruvin implement algorithm
      * @param position
      * @param timeInterval
      * @throws Exception
      */
     public void setAssessmentTimeInterval(int position, int[] timeInterval) throws Exception {
         if (!isAssessmentCreated(position - 1)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         if (timeInterval.length == 3) {
             if (isTimeFormatValid(timeInterval) == false) {
                 throw new Exception("Time format is wrong");
             }
         } else {
             throw new Exception("Time format is wrong");
         }
 
         int timeConvertedInSecs = timeInterval[0] * 3600 + timeInterval[1] * 60 + timeInterval[2];
 
         assessmentList.get(position - 1).setTimeInterval(timeConvertedInSecs);
     }
 
     public String getAssessmentName(int position) throws Exception {
         if (!isAssessmentCreated(position - 1)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         return assessmentList.get(position - 1).getName();
     }
 
     public String getAssessmentInstruction(int position) throws Exception {
         if (!isAssessmentCreated(position - 1)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         return assessmentList.get(position - 1).getInstruction();
     }
 
     public String getAssessmentCode(int position) throws Exception {
         if (!isAssessmentCreated(position - 1)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         return assessmentList.get(position - 1).getCode();
     }
 
     /**
      * @author Ruvin
      * @param position
      * @return
      * @throws Exception
      */
     public int[] getAssessmentTimeInterval(int position) throws Exception {
         if (!isAssessmentCreated(position - 1)) {
             throw new Exception("Need to create Assessment first");
         }
 
         int[] time = null;
         int unconvertedSeconds = assessmentList.get(position - 1).getTimeInterval();
         int hour = unconvertedSeconds / 3600;
         unconvertedSeconds %= 3600;
         int minute = unconvertedSeconds / 60;
         unconvertedSeconds %= 60;
         int second = unconvertedSeconds;
         time[0] = hour;
         time[1] = minute;
         time[2] = second;
         return time;
     }
 
     // </editor-fold>
     //<editor-fold defaultstate="collapsed" desc="Checker">
     /**
      * @author Ruvin time[2]!
      * @param time
      * @return
      */
     private boolean isTimeFormatValid(int[] time) {
         if (time[0] < 0 || time[0] > 23 || time[1] < 0 || time[1] > 59 || time[2] < 0 || time[2] > 59) {
             return false;
         }
         return true;
     }
 
     public boolean isAssessmentCreated(int position) {
         if (assessmentList.get(position) == null) {
             return false;
         }
         return true;
     }
 
     public boolean isSectionCreated(int[] positions) {
         if (assessmentList.get(positions[0]).getSectionList().get(positions[1]) == null) {
             return false;
         }
         return true;
     }
 
     public boolean isSubSectionCreated(int[] positions) {
         if (assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]) == null) {
             return false;
         }
         return true;
     }
 
     public boolean isQuestionCreated(int[] positions) {
         if (assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).getQuestionList().get(positions[3]) == null) {
             return false;
         }
         return true;
     }
 
     // </editor-fold>
     //<editor-fold defaultstate="collapsed" desc="Section">
     public void createSection(int[] positions, String name, String instruction, int[] timeLimit) throws Exception {
         if (!isAssessmentCreated(positions[0] - 1)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         Section section = new Section();
         List<Section> sectionList = assessmentList.get(positions[0] - 1).getSectionList();
         sectionList.add(section);
         assessmentList.get(positions[0] - 1).setSectionList(sectionList);
         positions[1] = sectionList.size();
         setSectionName(positions, name);
         setSectionInstruction(positions, instruction);
         // setSectionTimeLimit(positions, timeLimit);
     }
 
     public void setSectionName(int[] positions, String name) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         assessmentList.get(positions[0]).getSectionList().get(positions[1]).setName(name);
     }
 
     public void setSectionInstruction(int[] positions, String instruction) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         assessmentList.get(positions[0]).getSectionList().get(positions[1]).setInstruction(instruction);
     }
 
     /**
      * @author Ruvin
      */
     public void setSectionTimeLimit(int[] positions, int[] timeLimit) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         if (timeLimit.length == 3) {
             if (isTimeFormatValid(timeLimit) == false) {
                 throw new Exception("Time format is wrong");
             }
         } else {
             throw new Exception("Time format is wrong");
         }
 
         int timeConvertedInSecs = timeLimit[0] * 3600 + timeLimit[1] * 60 + timeLimit[2];
 
         assessmentList.get(positions[0]).getSectionList().get(positions[1]).setTimeLimit(timeConvertedInSecs);
     }
 
     /**
      * @author Ruvin
      * @param positions
      * @return
      * @throws Exception
      */
     public String getSectionName(int[] positions) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions)) {
             throw new Exception("Need to create Section first");
         }
         return assessmentList.get(positions[0]).getSectionList().get(positions[1]).getName();
     }
 
     /**
      * @author Ruvin
      * @param positions
      * @return
      * @throws Exception
      */
     public String getSectionInstruction(int[] positions) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions)) {
             throw new Exception("Need to create Section first");
         }
         return assessmentList.get(positions[0]).getSectionList().get(positions[1]).getInstruction();
     }
 
     /**
      * @author Ruvin
      * @return
      * @throws Exception
      */
     public int[] getSectionTimeLimit(int[] positions) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions)) {
             throw new Exception("Need to create Assessment first");
         }
         int[] time = null;
         int unconvertedSeconds = assessmentList.get(positions[0]).getSectionList().get(positions[1]).getTimeLimit();
         int hour = unconvertedSeconds / 3600;
         unconvertedSeconds %= 3600;
         int minute = unconvertedSeconds / 60;
         unconvertedSeconds %= 60;
         int second = unconvertedSeconds;
         time[0] = hour;
         time[1] = minute;
         time[2] = second;
         return time;
     }
 
     // </editor-fold>
     //<editor-fold defaultstate="collapsed" desc="SubSection">
     public void createSubSection(int[] positions, String name, String instruction) throws Exception {
         int[] positionsTemp = new int[2];
         positionsTemp[0] = positions[0] - 1;
         positionsTemp[1] = positions[1] - 1;
         if (!isAssessmentCreated(positionsTemp[0]) || !isSectionCreated(positionsTemp)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         SubSection subSection = new SubSection();
         List<SubSection> subSectionList = assessmentList.get(positionsTemp[0]).getSectionList().get(positionsTemp[1]).getSubSectionList();
         subSectionList.add(subSection);
         positions[2] = subSectionList.size();
         assessmentList.get(positionsTemp[0]).getSectionList().get(positionsTemp[1]).setSubSectionList(subSectionList);
         setSubSectionName(positions, name);
         setSubSectionInstruction(positions, instruction);
     }
 
     public void setSubSectionName(int[] positions, String name) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         positions[2] = positions[2] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions) || !isSubSectionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
 
         assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).setName(name);
     }
 
     public void setSubSectionInstruction(int[] positions, String instruction) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         positions[2] = positions[2] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions) || !isSubSectionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
 
         assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).setInstruction(instruction);
     }
 
     /**
      * @author Ruvin
      * @param positions
      * @return
      * @throws Exception
      */
     public String getSubSectionName(int[] positions) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         positions[2] = positions[2] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions) || !isSubSectionCreated(positions)) {
             throw new Exception("Need to create Subsection first");
         }
         return assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).getName();
     }
 
     /**
      * @author Ruvin
      * @param positions
      * @return
      * @throws Exception
      */
     public String getSubSectionInstruction(int[] positions) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         positions[2] = positions[2] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions) || !isSubSectionCreated(positions)) {
             throw new Exception("Need to create Subsection first");
         }
         return assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).getInstruction();
     }
     // </editor-fold>
     //<editor-fold defaultstate="collapsed" desc="Question">
 
     public void createMultipleChoice(int[] positions, String questionText, int mark) throws Exception {
         int[] positionsTemp = new int[3];
         positionsTemp[0] = positions[0] - 1;
         positionsTemp[1] = positions[1] - 1;
         positionsTemp[2] = positions[2] - 1;
         if (!isAssessmentCreated(positionsTemp[0]) || !isSectionCreated(positionsTemp) || !isSubSectionCreated(positionsTemp)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         Question question = new MultipleChoice();
         List<Question> questionList = assessmentList.get(positionsTemp[0]).getSectionList().get(positionsTemp[1]).getSubSectionList().get(positionsTemp[2]).getQuestionList();
         questionList.add(question);
         positions[3] = questionList.size();
         assessmentList.get(positionsTemp[0]).getSectionList().get(positionsTemp[1]).getSubSectionList().get(positionsTemp[2]).setQuestionList(questionList);
         setQuestionText(positions, questionText);
         setQuestionMark(positions, mark);
     }
 
     public void setQuestionText(int[] positions, String questionText) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         positions[2] = positions[2] - 1;
         positions[3] = positions[3] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions) || !isSubSectionCreated(positions) || !isQuestionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
 
         assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).getQuestionList().get(positions[3]).setQuestionOrInstruction(questionText);
     }
 
     public void setQuestionMark(int[] positions, int mark) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         positions[2] = positions[2] - 1;
         positions[3] = positions[3] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions) || !isSubSectionCreated(positions) || !isQuestionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
 
         assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).getQuestionList().get(positions[3]).setMark(mark);
     }
 
     /**
      * @author Ruvin
      * @param positions
      * @return
      * @throws Exception
      */
     public String getQuestionText(int[] positions) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         positions[2] = positions[2] - 1;
         positions[3] = positions[3] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions) || !isSubSectionCreated(positions) || !isQuestionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
 
         return assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).getQuestionList().get(positions[3]).getQuestionOrInstruction();
     }
 
     /**
      * @author Ruvin
      * @param positions
      * @return
      * @throws Exception
      */
     public int getQuestionMark(int[] positions) throws Exception {
         positions[0] = positions[0] - 1;
         positions[1] = positions[1] - 1;
         positions[2] = positions[2] - 1;
         positions[3] = positions[3] - 1;
         if (!isAssessmentCreated(positions[0]) || !isSectionCreated(positions) || !isSubSectionCreated(positions) || !isQuestionCreated(positions)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
 
         return assessmentList.get(positions[0]).getSectionList().get(positions[1]).getSubSectionList().get(positions[2]).getQuestionList().get(positions[3]).getMark();
     }
 
     public void createFillInBlank(int[] positions, String instructionText, int mark) throws Exception {
         int[] positionsTemp = new int[3];
         positionsTemp[0] = positions[0] - 1;
         positionsTemp[1] = positions[1] - 1;
         positionsTemp[2] = positions[2] - 1;
         if (!isAssessmentCreated(positionsTemp[0]) || !isSectionCreated(positionsTemp) || !isSubSectionCreated(positionsTemp)) {
             throw new Exception("Invalid positions provided or objects are not created yet");
         }
         Question question = new FillInBlank();
         List<Question> questionList = assessmentList.get(positionsTemp[0]).getSectionList().get(positionsTemp[1]).getSubSectionList().get(positionsTemp[2]).getQuestionList();
         questionList.add(question);
         positions[3] = questionList.size();
         assessmentList.get(positionsTemp[0]).getSectionList().get(positionsTemp[1]).getSubSectionList().get(positionsTemp[2]).setQuestionList(questionList);
         setQuestionText(positions, instructionText);
         setQuestionMark(positions, mark);
     }
 
     // </editor-fold>
     public Assessment getAssessmentObject(int position) {
         return assessmentList.get(position);
     }
 
     public Choice createChoice() {
         Choice choice = new Choice();
         return choice;
     }
 }
