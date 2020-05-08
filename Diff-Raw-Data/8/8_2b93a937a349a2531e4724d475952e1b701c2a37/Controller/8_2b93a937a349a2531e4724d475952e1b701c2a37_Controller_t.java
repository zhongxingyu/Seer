 package melt;
 
 import melt.View.Viewer;
 import java.io.File;
 import java.util.ArrayList;
 
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.Marshaller;
 import melt.Model.*;
 
 /**
  *
  * @author mbaxkmc7
  */
 public class Controller {
 
     //TestBank instance contains all the test in the application
     private TestBank model;
     private StudentBank students;
     private Viewer viewer;
     //Name of the file in which the tests are contained
     private String testFile;
     private String studentFile;
 
     /**
      * The constructor for the controller class
      *
      * @param model The model that contains the tests of the application.
      * @param viewer The viewer of the application - the main UI.
      * @param testFile The name of the xml file that contains the tests.
      */
     public Controller(TestBank model, Viewer viewer, String testFile,String studentFile) {
         model = new TestBank();
         students = new StudentBank();
         this.model = model;
         this.viewer = viewer;
         this.testFile = testFile;
         this.studentFile = studentFile;
         
     }
 
    public StudentBank getStudents() {
        return students;
    }

    public void setStudents(StudentBank students) {
        this.students = students;
    }

     
     public Student addStudent(String studentName,Test selectedTest){
         
         Student student = students.addStudent(studentName, selectedTest);
         this.updateStudentFile();
         return student;
     }
     /**
      * Load the objects from the xml file
      *
      *
      */
     public void loadFromXmlFile() {
 
         try {
 
             JAXBContext jaxbContext = JAXBContext.newInstance(TestBank.class);
             
             Unmarshaller jaxbTestsUnmarshaller = jaxbContext.createUnmarshaller();
             
             File XMLfile = new File(testFile);
             
             //unmarshall the object and store it to the TestBank object
             model = (TestBank) jaxbTestsUnmarshaller.unmarshal(XMLfile);
             
 
         } catch (JAXBException e) {
             System.err.println(e);
         }
     }
 
     /**
      * Update the content of the xml file. This function is called when the
      * contents of the tests are changed, in order to save the changes to the
      * file.
      */
     public void updateXmlFile() {
 
         try {
 
             // create JAXB context and initializing Marshaller
             JAXBContext jaxbTestsContext = JAXBContext.newInstance(TestBank.class);
             
             Marshaller jaxbMarshaller = jaxbTestsContext.createMarshaller();
             
             // for getting nice formatted output
             jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
             
             //specify the location and name of xml file to be created
             File XMLfile = new File(testFile);
             
             // Writing to XML file
             jaxbMarshaller.marshal(model, XMLfile);
             
             // Writing to console
             jaxbMarshaller.marshal(model, System.out);
             
 
         } catch (JAXBException e) {
             // some exception occured
             e.printStackTrace();
         }
 
     }
 
     public void updateStudentFile(){
         try{
             JAXBContext jaxbStudentsContext = JAXBContext.newInstance(StudentBank.class);
             Marshaller studentBankMarshaller = jaxbStudentsContext.createMarshaller();
             studentBankMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
             File XMLfileStudent = new File(studentFile);
             studentBankMarshaller.marshal(students,XMLfileStudent);
             studentBankMarshaller.marshal(students, System.out);
         }
         catch(JAXBException e){
             
         }
     }
     
     public void loadStudentFile(){
         try{
             JAXBContext jaxbContextStudents = JAXBContext.newInstance(StudentBank.class);
             Unmarshaller jaxbStudentsUnmarshaller = jaxbContextStudents.createUnmarshaller();
             File XMLStudentFile = new File(studentFile);
             students = (StudentBank) jaxbStudentsUnmarshaller.unmarshal(XMLStudentFile);
         }catch(JAXBException e){
             
         }
     }
     /**
      * Get a test with a specified id
      *
      * @param id the id of the test
      * @return the desired test. Null if the section with this id does not
      * exist.
      */
     public Test getTest(int id) {
         for (Test test : this.model.getTests()) {
             if (test.getId() == id) {
                 return test;
             }
         }
         return null;
     }
 
     /**
      * Add a new test to the model.
      *
      * @param name name of the test
      * @param creator the creator of the test
      * @param instructions the instructions of the test
      * @param id the id of the new test
      * @return the new test
      */
     public Test addTest(String name, String creator, String instructions) {
         int id;
         if (this.getTestBank().getTests().isEmpty()) {
             id = 1;
         } else {
             int last = this.getTestBank().getTests().size() - 1;
             id = this.getTestBank().getTests().get(last).getId() + 1;
         }
 
         Test newTest = new Test(id, name, creator);
         newTest.setInstructions(instructions);
         this.model.addTest(newTest);
         return newTest;
     }
 
     /**
      * The details of the test are edited with the new ones
      *
      * @param test The test object we want to edit
      * @param newName The new name for the test
      * @param newCreator The new test creator
      * @param newInstructions The new instructions for the test
      * @return
      */
     public Test updateTestDetails(Test test, String newName, String newCreator, String newInstructions) {
 
         test.setName(newName);
         test.setInstructions(newInstructions);
         test.setCreator(newCreator);
         this.updateXmlFile();
         return test;
 
     }
 
     /**
      * Add a new section to an existing test.
      *
      * @param test the test in which the new section will be added
      * @param sectionId the id of the new section
      * @param Time the time limit for the new section
      * @return the new section that is added
      */
     public Section addSection(Test test, double Time, String sectionName, String instructions) {
 
         int newSectionId;
         if (test.getSections().isEmpty()) {
             newSectionId = 1;
         } else {
             int last = test.getSections().size() - 1;
             int id = test.getSections().get(last).getId();
             newSectionId = id + 1;
         }
 
         Section newSection = new Section(newSectionId, Time, sectionName, instructions);
 
         test.addSection(newSection);
         return newSection;
 
     }
 
     /**
      * Update the details of the section.
      *
      * @param section The section we want updated
      * @param newTime The new time limit for the section
      * @param newSectionName The new name of the section
      * @param newInstructions New instructions for the section
      * @return The section that we edited
      */
     public Section updateSectionDetails(Section section, double newTime, String newSectionName, String newInstructions) {
 
         section.setName(newSectionName);
         section.setTime(newTime);
         section.setInstructions(newInstructions);
         this.updateXmlFile();
         return section;
     }
 
     /**
      * Add a new subsection to a section
      *
      * @param section the section in which the new subsection will be added
      * @param subsectionId the id of the new subsection
      * @return the new subsection
      */
     public Subsection addSubsection(Section section, String type, String subsectionName) {
         int subsectionId;
         if (section.getSubsections().isEmpty()) {
             subsectionId = 1;
         } else {
             int last = section.getSubsections().size() - 1;
             int id = section.getSubsections().get(last).getId();
             subsectionId = id + 1;
         }
 
         Subsection newSubsection = new Subsection(type, subsectionName, subsectionId);
 
         //Mcq question = new Mcq(null, null, 1,"",1.0);
         //newSubsection.addQuestion(question);
         section.addSubsection(newSubsection); //checks must be made here
         return newSubsection;
 
     }
 
     /**
      *
      * @param subsection
      * @param newType
      * @param newSubsectionName
      * @return
      */
     public Subsection updateSubsectionDetails(Subsection subsection, String newType, String newSubsectionName) {
 
         subsection.setName(newSubsectionName);
         subsection.setType(newType);
         this.updateXmlFile();
         return subsection;
     }
 
     /**
      *
      * @param subsection
      * @param answers
      * @param correctAnswers
      * @param questionText
      * @param mark
      * @return
      */
     public Question addQuestion(Subsection subsection, ArrayList<String> answers, ArrayList<Integer> correctAnswers, String questionText, double mark) {
         int questionId;
         if (subsection.getQuestions().isEmpty()) {
             questionId = 1;
         } else {
             int last = subsection.getQuestions().size() - 1;
             int id = subsection.getQuestions().get(last).getId();
             questionId = id + 1;
         }
 
         Question question = new Mcq(answers, correctAnswers, questionId, questionText, mark);
         subsection.addQuestion(question);
         return question;
     }
 
     /**
      *
      * @param subsection
      * @param answers
      * @param questionText
      * @param mark
      * @return
      */
     public Question addQuestion(Subsection subsection, ArrayList<FibqBlankAnswers> answers, String questionText, double mark) {
 
         int questionId = subsection.getQuestions().size() + 1;
         Question question = new Fibq(answers, questionId, questionText, mark);
         subsection.addQuestion(question);
         return question;
     }
 
     /**
      *
      * @param subsection
      * @param questionText
      * @param mark
      * @return
      */
     public Question addQuestion(Subsection subsection, String questionText, double mark) {
         int questionID = subsection.getQuestions().size() + 1;
         Question question = new Essay(questionID, questionText, mark);
         subsection.addQuestion(question);
         return question;
     }
 
     /**
      *
      * @param subsection
      * @param questionText
      * @param mark
      * @param wordLimit
      * @return
      */
     public Question addQuestion(Subsection subsection, String questionText, double mark, int wordLimit) {
         int questionID = subsection.getQuestions().size() + 1;
         Question question = new Essay(questionID, questionText, mark, wordLimit);
         subsection.addQuestion(question);
         return question;
     }
 
     /**
      *
      * @param question
      * @param newAnswers
      * @param newCorrectAnswers
      * @param newQuestionText
      * @param newMark
      * @return
      */
     public Question updateQuestionDetails(Question question, ArrayList<String> newAnswers, ArrayList<Integer> newCorrectAnswers, String newQuestionText, double newMark) {
 
         Mcq mcq = (Mcq) question;
         mcq.setQuestionText(newQuestionText);
         mcq.setMark(newMark);
         mcq.setAnswers(newAnswers);
         mcq.setCorrectAnswers(newCorrectAnswers);
         this.updateXmlFile();
         return mcq;
     }
 
     /**
      *
      * @param question
      * @param newAnswers
      * @param newQuestionText
      * @param newMark
      * @return
      */
     public Question updateQuestionDetails(Question question, ArrayList<FibqBlankAnswers> newAnswers, String newQuestionText, double newMark) {
 
         Fibq fibq = (Fibq) question;
         fibq.setQuestionText(newQuestionText);
         fibq.setMark(newMark);
         fibq.setCorrectAnswers(newAnswers);
         this.updateXmlFile();
         return fibq;
     }
 
     /**
      *
      * @param question
      * @param newQuestionText
      * @param newMark
      * @return
      */
     public Question updateQuestionDetails(Question question, String newQuestionText, double newMark) {
 
         Essay essay = (Essay) question;
         essay.setQuestionText(newQuestionText);
         essay.setMark(newMark);
         this.updateXmlFile();
         return essay;
     }
 
     /**
      *
      * @param question
      * @param newQuestionText
      * @param newMark
      * @param wordLimit
      * @return
      */
     public Question updateQuestionDetails(Question question, String newQuestionText, double newMark, int wordLimit) {
 
         Essay essay = (Essay) question;
         essay.setQuestionText(newQuestionText);
         essay.setMark(newMark);
         essay.setWordLimit(wordLimit);
         this.updateXmlFile();
         return essay;
     }
 
     /**
      * Delete a test from the system
      *
      * @param testId the id of the test that we want to delete
      * @return the result of the deletion
      */
     public boolean deleteTest(int testId) {
 
         if (this.model.deleteTest(testId)) {
             this.updateXmlFile();
             return true;
         }
         return false;
     }
 
     /**
      *
      * @param test the test from which the section will be deleted
      * @param sectionId the id of the section that we want to delete
      * @return the result of the deletion
      */
     public boolean deleteSection(Test test, int sectionId) {
 
 
         if (test.deleteSection(sectionId)) {
             this.updateXmlFile();
             return true;
         }
         return false;
 
 
     }
 
     /**
      *
      * @param section the section from which the subsection will be deleted
      * @param subsectionId the id of the subsection we want to delete
      * @return the result of the deletion
      */
     public boolean deleteSubsection(Section section, int subsectionId) {
         if (section.deleteSubsection(subsectionId)) {
             this.updateXmlFile();
             return true;
         }
         return false;
     }
 
     /**
      *
      * @param section the section from which the question will be deleted
      * @param questionID the id of the question that we want to delete
      * @return the result of the deletion
      */
     public boolean deleteQuestion(Subsection subsection, int questionID) {
         if (subsection.deleteQuestion(questionID)) {
             this.updateXmlFile();
             return true;
         }
         return false;
     }
     //delete functions
 
     /**
      * Get the application model.
      *
      * @return the application model
      */
     public TestBank getTestBank() {
         return model;
     }
 }
