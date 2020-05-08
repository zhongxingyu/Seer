 
 package edu.osu.picola.beans;
 
 import edu.osu.picola.dao.AssignmentDAO;
 import edu.osu.picola.dao.QuestionDAO;
 import edu.osu.picola.dao.UserDAO;
 import edu.osu.picola.dataobjects.Assignment;
 import edu.osu.picola.dataobjects.Question;
 import edu.osu.picola.dataobjects.User;
 import java.io.Serializable;
 import java.util.Date;
 import java.sql.Timestamp;
 import java.util.Arrays;
 import java.util.List;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ActionListener;
 
 @ManagedBean(name="createAssignmentBean")
 @SessionScoped
 public class CreateAssignmentBean implements Serializable, ActionListener{
     private Assignment selected;
     private String assignment_descr;
     private Date indivdual_start_date;
     private Date indivdual_end_date;
     private Date BP_start_date;
     private Date BP_end_date;
     private Date MP_start_date;
     private Date MP_end_date;
     private int assignment_number;
     private int assignment_id;
     private String assignment_name;
 
     
     
     private String initquestion;
     private String answer_to_question_explanation;
     private String multiple_choice_answer;
     private String option_a;
     private String option_b;
     private String option_c;
     private String option_d;
     private String option_e;
     
     private String bpQuestion;
     private String bpQuestionDesc;
     private String mpQuestion;
     private String mpQuestionDesc;
 
     public String getAssignment_name() {
         return assignment_name;
     }
 
     public void setAssignment_name(String assignment_name) {
         this.assignment_name = assignment_name;
     }
     
     public String getAssignment_descr() {
         return assignment_descr;
     }
 
     public void setAssignment_descr(String assignment_descr) {
         this.assignment_descr = assignment_descr;
     }
 
     public Date getIndivdual_start_date() {
         return indivdual_start_date;
     }
 
     public void setIndivdual_start_date(Date indivdual_start_date) {
         this.indivdual_start_date = indivdual_start_date;
     }
 
     public Date getIndivdual_end_date() {
         return indivdual_end_date;
     }
 
     public void setIndivdual_end_date(Date indivdual_end_date) {
         this.indivdual_end_date = indivdual_end_date;
     }
 
     public Date getBP_start_date() {
         return BP_start_date;
     }
 
     public void setBP_start_date(Date BP_start_date) {
         this.BP_start_date = BP_start_date;
     }
 
     public Date getBP_end_date() {
         return BP_end_date;
     }
 
     public void setBP_end_date(Date BP_end_date) {
         this.BP_end_date = BP_end_date;
     }
 
     public Date getMP_start_date() {
         return MP_start_date;
     }
 
     public void setMP_start_date(Date MP_start_date) {
         this.MP_start_date = MP_start_date;
     }
 
     public Date getMP_end_date() {
         return MP_end_date;
     }
 
     public void setMP_end_date(Date MP_end_date) {
         this.MP_end_date = MP_end_date;
     }
 
     public int getAssignment_number() {
         return assignment_number;
     }
 
     public void setAssignment_number(int assignment_number) {
         this.assignment_number = assignment_number;
     }
 
     public int getAssignment_id() {
         return assignment_id;
     }
 
     public void setAssignment_id(int assignment_id) {
         this.assignment_id = assignment_id;
     }
 
     public String getInitquestion() {
         return initquestion;
     }
 
     public void setInitquestion(String initquestion) {
         this.initquestion = initquestion;
     }
 
     public String getAnswer_to_question_explanation() {
         return answer_to_question_explanation;
     }
 
     public void setAnswer_to_question_explanation(String answer_to_question_explanation) {
         this.answer_to_question_explanation = answer_to_question_explanation;
     }
 
     public String getMultiple_choice_answer() {
         return multiple_choice_answer;
     }
 
     public void setMultiple_choice_answer(String multiple_choice_answer) {
         this.multiple_choice_answer = multiple_choice_answer;
     }
 
     public String getOption_a() {
         return option_a;
     }
 
     public void setOption_a(String option_a) {
         this.option_a = option_a;
     }
 
     public String getOption_b() {
         return option_b;
     }
 
     public void setOption_b(String option_b) {
         this.option_b = option_b;
     }
 
     public String getOption_c() {
         return option_c;
     }
 
     public void setOption_c(String option_c) {
         this.option_c = option_c;
     }
 
     public String getOption_d() {
         return option_d;
     }
 
     public void setOption_d(String option_d) {
         this.option_d = option_d;
     }
 
     public String getOption_e() {
         return option_e;
     }
 
     public void setOption_e(String option_e) {
         this.option_e = option_e;
     }
 
     public String getBpQuestion() {
         return bpQuestion;
     }
 
     public void setBpQuestion(String bpQuestion) {
         this.bpQuestion = bpQuestion;
     }
 
     public String getBpQuestionDesc() {
         return bpQuestionDesc;
     }
 
     public void setBpQuestionDesc(String bpQuestionDesc) {
         this.bpQuestionDesc = bpQuestionDesc;
     }
 
     public String getMpQuestion() {
         return mpQuestion;
     }
 
     public void setMpQuestion(String mpQuestion) {
         this.mpQuestion = mpQuestion;
     }
 
     public String getMpQuestionDesc() {
         return mpQuestionDesc;
     }
 
     public void setMpQuestionDesc(String mpQuestionDesc) {
         this.mpQuestionDesc = mpQuestionDesc;
     }
     
     public void clearForm(ActionEvent event){
         System.out.println("CreateAssignmentBean: Clearing Form!");
         this.assignment_descr = "";
         this.indivdual_start_date = new Date(0);
         this.indivdual_end_date = new Date(0);
         this.BP_start_date = new Date(0);
         this.BP_end_date = new Date(0);
         this.MP_start_date = new Date(0);
         this.MP_end_date = new Date(0);
         this.assignment_number = 0;
         this.assignment_id = 0;
         
         this.initquestion = "";
         this.answer_to_question_explanation = "";
         this.multiple_choice_answer = "";
         this.option_a = "";
         this.option_b = "";
         this.option_c = "";
         this.option_d = "";
         this.option_e = "";
         
         this.bpQuestion = "";
         this.bpQuestionDesc = "";
         this.mpQuestion = "";
         this.mpQuestionDesc = "";
     }
 
     @Override
     public void processAction(ActionEvent event) throws AbortProcessingException {
         System.out.println("CreateAssignmentBean: Submitting Assignment!");
         /* convert string to timestamp */
         Timestamp is = new Timestamp (indivdual_start_date.getTime());
         Timestamp ie = new Timestamp (indivdual_end_date.getTime());
         Timestamp bs = new Timestamp (BP_start_date.getTime());
         Timestamp be = new Timestamp (BP_end_date.getTime());
         Timestamp ms = new Timestamp (MP_start_date.getTime());
         Timestamp me = new Timestamp (MP_end_date.getTime());
         
         int user_id = LoginBean.user.getUser_id();
         int course_id = AssignmentMenuBean.selectedCourse.getCourse_id();
         
         System.out.println("-------Information for inserting-----");
         System.out.println("user: (" + LoginBean.user + ") with id: " + user_id);
         System.out.println("course: (" + AssignmentMenuBean.selectedCourse + ") with number: " + course_id);
         System.out.println("Initial TimeStamp: " + is);
         System.out.println();
         
         /* insert assignment */
         Assignment assignment = new Assignment(assignment_descr, is, ie, bs,
                 be, ms, me, user_id, course_id, assignment_number,assignment_name);
         
         Question init = new Question(this.initquestion, this.answer_to_question_explanation, this.multiple_choice_answer,
 			Arrays.asList(new String[]{this.option_a, this.option_b, this.option_c, this.option_d, this.option_e}));
         Question mp = new Question(this.mpQuestion, this.mpQuestionDesc, true, false);
         Question bp = new Question(this.bpQuestion, this.bpQuestionDesc, false, true);
         
         AssignmentDAO.insertAssignment(assignment);
         QuestionDAO.insertQuestion(init);
         init.setQuestion_id(QuestionDAO.getQuestionByQuestion(init.getQuestion()));
         QuestionDAO.insertQuestion(bp);
         bp.setQuestion_id(QuestionDAO.getQuestionByQuestion(bp.getQuestion()));
         QuestionDAO.insertQuestion(mp);
         mp.setQuestion_id(QuestionDAO.getQuestionByQuestion(mp.getQuestion()));
         
         assignment = AssignmentDAO.getAssignmentByDateAndIds(user_id, course_id,assignment.getAssignment_number());
 
        QuestionDAO.insertQuestionToAssignment(assignment.getAssignment_id(), user_id);
         
         
         /* assign assignment to each student in the course */
         List<User> roster = UserDAO.getCourseRoster(course_id);
         for (int i = 0; i < roster.size(); i++) {
             System.out.println("User being added: " + roster.get(i));
             AssignmentDAO.assignToStudent(roster.get(i).getUser_id(),
                     assignment.getAssignment_id());
         }
         
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"SUCCESS!", "Assignment Created"));  
     }
 }
