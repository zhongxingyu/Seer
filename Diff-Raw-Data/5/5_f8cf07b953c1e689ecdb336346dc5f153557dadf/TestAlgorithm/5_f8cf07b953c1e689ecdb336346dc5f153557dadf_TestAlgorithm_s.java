 package algorithm;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Random;
 import java.util.UUID;
 
 import crowdtrust.BinaryTask;
 import crowdtrust.Task;
 
 import db.LoginDb;
 import db.RegisterDb;
 import db.TaskDb;
 
 import junit.framework.TestCase;
 
 public class TestAlgorithm extends TestCase {
 	protected static int annotatorNumber = 1000;
 	
 	protected static int totalPos = 1000;	//Annotators when created have 
 	protected static int totalNeg = 1000;   //'Answered' 2000 questions
 	
 	protected AnnotatorModel[] annotators;
 	
 	public TestAlgorithm(String name){
 		super(name);
 	}
 	
 	public void testAlgorithm(){
 		//Lets create some annotators with id's 1 - 1000 and place them in array
 		annotators = new AnnotatorModel[annotatorNumber];
 		for(int i = 0; i < annotatorNumber; i++){
 			String uuid = UUID.randomUUID().toString();
 			uuid = uuid.replace("-", "");
 			uuid = uuid.substring(0, 12);
 			annotators[i] = new AnnotatorModel(uuid, uuid);
 		}
 		
 		//Set up the annotators so they can answer binary question
 		Random rand = new Random();
 		for(int i = 0; i < annotatorNumber; i++){
 			int truePos = rand.nextInt(999) + 1;	
 			int trueNeg = rand.nextInt(999) + 1;
 			annotators[i].setUpBinary(truePos, trueNeg, totalPos, totalNeg);
 		}
 		boolean labs = false;
 		if(labs){
 		//Add them to the Database
 		for(int i = 0; i < 1000; i++){
 			RegisterDb.addUser("test@test.com", annotators[i].getUsername(), annotators[i].getPassword(), true);
 			annotators[i].setId(LoginDb.checkUserDetails(annotators[i].getUsername(), annotators[i].getPassword()));
 		}
 		
 		//Lets make a client
 		RegisterDb.addUser("testClient@test.com", "gio", "gio", false);
 		int accountId = LoginDb.checkUserDetails("gio", "gio");
 		//Lets add a binary task to the database
 		long expirey = getDate();
 		double accuracy = 0.7;
 		assertTrue(TaskDb.addTask(accountId,"BinaryTestTask", "This is a test", accuracy, 1, expirey));
 		}
 	}
 	
 	protected long getDate(){
 		long ret        = 0           ;
 		String str_date = "11-June-15";
 	    DateFormat formatter ; 
 	    Date date ; 
 	    formatter = new SimpleDateFormat("dd-MMM-yy");
 		try {
 			date = (Date)formatter.parse(str_date);
 			ret = date.getTime();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} 
 		return ret;
 	}
 }
