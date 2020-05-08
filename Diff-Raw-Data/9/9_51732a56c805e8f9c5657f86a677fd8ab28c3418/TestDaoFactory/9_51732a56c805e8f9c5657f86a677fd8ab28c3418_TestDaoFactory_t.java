 package g53.exceedvote.test;
 
 import java.util.ArrayList;
 
 import g53.exceedvote.domain.*;
 import g53.exceedvote.persistence.*;
 
 /**
  * @author 	Wasupol Tungsakulthong 5310547304 
  * @Version 2012.November.15
  */
 
 public class TestDaoFactory {
 
 	private ArrayList<Voter> voters;
 	private ArrayList<Project> project;
 	private ArrayList<Question> questions;
 	private VoterDao dao;
 	public void testDaoFactory() {
 		dao = DaoFactory.getInstance().getVoterDao();
 		dao.LoadDriver();
 		dao.connect();
 		voters = dao.getAllVoter();
 		project = dao.getProject();
 		questions = dao.getQuestion();
 	}
 
 	public void testVoter() {
		Voter harry = new Voter(0, "harry", "potter",1);
 		if (dao.canInsertVoter(harry.getId(), harry.getName())) {
 			dao.insertVoter(harry);
 		}
 		System.out.println(dao.getVoter(harry.getName(), harry.getPassword()).getName());
 		System.out.print(" -- All Users in application -- \n");
 		for (Voter v : voters) {
 			System.out.printf("%s %s \n",v.getId(),v.getName());
 		}
 	}
 
 	public void testProject() {
 		System.out.print(" -- All Projects in application -- \n");
 		for (Project p : project) {
 			System.out.printf("%s %s %s \n",p.getID(),p.getProjectName(),p.getTeamName());
 		}
 		
 	}
 
 	public void testQuestion() {
 		System.out.print(" -- All Voting Topics in application -- \n");
 		for (Question q : questions) {
 			System.out.printf("%s %s \n",q.getQuestionID(),q.getQuestion());
 		}
 	}
 
 	public static void main(String[] args) {
 		TestDaoFactory test = new TestDaoFactory();
 		test.testDaoFactory();
 		test.testVoter();
 		test.testProject();
 		test.testQuestion();
 	}
 
 }
