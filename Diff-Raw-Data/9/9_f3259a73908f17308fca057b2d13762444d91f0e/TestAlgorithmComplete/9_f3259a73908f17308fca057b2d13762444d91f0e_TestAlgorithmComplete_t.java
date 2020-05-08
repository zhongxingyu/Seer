 package algorithm;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.UUID;
 
 import org.apache.commons.math3.distribution.NormalDistribution;
 
 import crowdtrust.AnnotationType;
 import crowdtrust.BinarySubTask;
 import crowdtrust.InputType;
 import crowdtrust.MediaType;
 import crowdtrust.MultiValueSubTask;
 import crowdtrust.SubTask;
 
 import db.LoginDb;
 import db.RegisterDb;
 import db.SubTaskDb;
 import db.TaskDb;
 
 import junit.framework.TestCase;
 
 public class TestAlgorithmComplete extends TestCase {
 	protected static boolean binary     = false;
 	protected static boolean multi      = true;
 	protected static boolean continuous = false;
 	
 	protected static boolean labs       = false;
 	
 	protected static int numCrowd       = 20;
 	protected static int numRequestors  = 1;
 	
 	protected static AnnotatorModel[] annotators;
 	protected static AnnotatorModel   requestor;
 	
 	protected static Random rand;
 	
 	protected static double binaryBots     = 0.9;
 	protected static double multiBots      = 0.9;
 	protected static double continuousBots = 0.9;
 	
 	protected static int binaryTasks = 0 ;  
 	protected static int multiTasks  = 1 ;  
 	protected static int continTasks = 0 ;  
 	protected static int totalTasks  = 1 ;
 	protected static int subTasksPerTask = 10;
 	
 	protected static float binaryAccuracy     = (float) 0.7;
 	protected static float multiAccuracy      = (float) 0.7;
 	protected static float continuousAccuracy = (float) 0.7;
 	
 	protected static int multiLabels = 5;
 	
 	protected static int allowedToAnswer = 10;
 	
 	protected static LinkedList<AnnotatorSubTaskAnswer> answers;
 	
 	public TestAlgorithmComplete(String name){
 		super(name);
 	}
 	
 	public void testAlgorithmNew(){
 		if((binary || multi || continuous) && labs){	           //if there are any tests to be run
 			annotators = new AnnotatorModel[numCrowd];             //container for the people
 			rand       = new Random();						       //Random number generator
 			answers    = new LinkedList<AnnotatorSubTaskAnswer>(); //Answers to give the crowd
 			for(int i = 0; i < numCrowd; i++){			           //Set up appropriate behaviours
 				String namepass = getRandomName();
 				annotators[i] = new AnnotatorModel(namepass, namepass);	//initalise the annotator with said name/pass
 				if(binary){
 					if(rand.nextDouble() > binaryBots){								//This annotator is a binary bot
 						annotators[i].setUpBinary(500, 500, 1000, 1000);	
 					}else{
 					//	NormalDistribution dist = new NormalDistribution(900, 10);  //Kosher
 						annotators[i].setUpBinary(900, 900, 1000, 1000);	
 					}
 				}
 				if(multi){
 					if(rand.nextDouble() > multiBots){								//This annotator is a multi bot
 						annotators[i].setUpMulti(200, 1000);
 					}else{
 						annotators[i].setUpMulti(800, 1000);						//Kosher
 					}
 				}
 				if(continuous){
 					if(rand.nextDouble() > continuousBots){							//This annotator is a continuous botg
 						annotators[i].setUpContinuous(0.1);
 					}else{
 						annotators[i].setUpContinuous(0.8);							//Kosher
 					}
 				}
 				
 				//Add the created user to the db and set their details
 				RegisterDb.addUser("test@test.com", annotators[i].getUsername(), annotators[i].getPassword(), true);
 				annotators[i].setId(LoginDb.checkUserDetails(annotators[i].getUsername(), annotators[i].getPassword()));
 			}
 			
 			//Create the man who's asking the questions
 			requestor = new AnnotatorModel("gio", "gio");
 			RegisterDb.addUser("testClient@test.com", "gio", "gio", false);
 			requestor.setId(LoginDb.checkUserDetails("gio", "gio"));
 			
 			//Add the binary tasks and subtasks
 			if(binary){
 				for(int i = 0; i < binaryTasks; i++){	//make x binary tasks
 					String name = getRandomName();
 					List<String> testQs = new LinkedList<String>();
 					testQs.add("test q1");	//constructor got binary tasks
 					testQs.add("test q2");
 					TaskDb.addTask(requestor.getBee().getId(),name, "This is a test?", binaryAccuracy,
 							       MediaType.IMAGE, AnnotationType.BINARY, InputType.RADIO, 
 							       allowedToAnswer , getDate(), testQs, new LinkedList<String>(), 
 							       new LinkedList<String>(), 0);
 					int id = TaskDb.getTaskId(name);	
 					for(int j = 0; j < subTasksPerTask; j++){	//create subtasks for this task
 						String sname = getRandomName();
 						SubTaskDb.addSubtask(sname, id);
 						int sid = SubTaskDb.getSubTaskId(sname);
 						BinarySubTask bst = new BinarySubTask(sid,(double) binaryAccuracy,0, numCrowd);
 						AnnotatorSubTaskAnswer asta = new AnnotatorSubTaskAnswer(bst.getId(), bst, new BinaryTestData(rand.nextInt(2)));
 						answers.add(asta);
 					}
 				}
 			}
 			//Add the multi tasks and subtasks
 			if(multi){
 				for(int i = 0; i < multiTasks; i++){
 					String name = getRandomName();
 					List<String> testQs = new LinkedList<String>();
 					testQs.add("test q1");	//constructor got binary tasks
 					testQs.add("test q2");
 					testQs.add("test q3");
 					testQs.add("test q4");
 					testQs.add("test q5");
					LinkedList<String> mins = new LinkedList<String>();
					mins.add("1");
					LinkedList<String> maxs = new LinkedList<String>();
					maxs.add("5");
					double step = 0;
 					TaskDb.addTask(requestor.getBee().getId(),name, "This is a test?", multiAccuracy,
 							       MediaType.IMAGE, AnnotationType.MULTIVALUED, InputType.RADIO, 
							       allowedToAnswer , getDate(), testQs, mins, maxs, step);
 					int id = TaskDb.getTaskId(name);	
 					for(int j = 0; j < subTasksPerTask; j++){
 						String sname = getRandomName();
 						SubTaskDb.addSubtask(sname, id);
 						int sid = SubTaskDb.getSubTaskId(sname);
 						MultiValueSubTask mst = new MultiValueSubTask(sid, multiAccuracy, multiLabels ,allowedToAnswer);
 						AnnotatorSubTaskAnswer asta = new AnnotatorSubTaskAnswer(mst.getId(), mst,
 																				 new MultiTestData((rand.nextInt(multiLabels) + 1), multiLabels));
 						answers.add(asta);
 					}			
 				}
 			}
 			//Add the continuous tasks and subtaks
 			if(continuous){
 				for(int i = 0; i < continTasks; i++){
 					for(int j = 0; j < subTasksPerTask; j++){
 
 					}			
 				}
 			}
 			
 			//Give all the annotators the answ			TaskDb.gers
 			for(int i = 0; i < numCrowd; i++){
 				annotators[i].setTasks(answers);
 			}
 			
 			//Lets start answering some questions
 			
 			for(int i = 0; i < totalTasks; i++){				//Loop through all the tasks
 				for(int j = 0; j < subTasksPerTask; j++){		//Loop through all subtasks of said task
 					LinkedList<Integer> picked = new LinkedList<Integer>();
 					for(int k = 0; k < allowedToAnswer; k++){	
 						int randIndex = rand.nextInt(numCrowd);
 						while(picked.contains(randIndex)){
 							randIndex = rand.nextInt(numCrowd);
 							System.out.println("Picking randIndex " + randIndex);
 						}
 					    SubTask t = SubTaskDb.getSequentialSubTask((i + 1), annotators[randIndex].bee.getId());
 						if(t == null){
 							break;
 						}else{
 							annotators[randIndex].answerTask(t);
 						}
 					}
 				}
 			}
 			
 		
 		}    //bool ifs
 	}		 //methods
 	
 	public String getRandomName(){
 		String uuid = UUID.randomUUID().toString();
 		uuid = uuid.replace("-", "");			
 		uuid = uuid.substring(0, 12);
 		return uuid;
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
 }	         //class
