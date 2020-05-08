 package myPackage;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * @author qyh
  *
  */
 public class MakePair {
 	private static final String MALE_FILE_PATH = "attachments\\male.txt";
 	private static final String FEMALE_FILE_PATH = "attachments\\female.txt";
 	private static final String PLAY_FILE_PATH = "attachments\\players.txt";
 	private static final int NUM_OF_EACH_GENDER = 100;
 	private static ArrayList<Person> male = new ArrayList<Person>(NUM_OF_EACH_GENDER);
 	private static ArrayList<Person> female = new ArrayList<Person>(NUM_OF_EACH_GENDER);
 	private static Person player;
 	private static boolean IsPlayerMale; 
 	static int[] voteBox = new int[101]; //to record the number of voter for each female
 	
 	/**
 	 * Set data using specific file data
 	 * the player's attribution is also read from lineNumber line of file 
 	 * @param lineNumber
 	 */
 	public static void usingFileData(int lineNumber) {
 		readFileToPerson(MALE_FILE_PATH,male);
 		readFileToPerson(FEMALE_FILE_PATH,female);
 		player = readFileToPlayer(PLAY_FILE_PATH, lineNumber);
 	}
 	
 	/**
 	 * Set data using random data
 	 * the player's attribution is gained by input
 	 * @param lineNumber
 	 */
 	public static void usingRandomData(String attribution){
 		String id = "-1,";
 		String gender = attribution.substring(0,2);
 		if(gender.equals("1")){
 			IsPlayerMale = true;
 		}
 		else{
 			IsPlayerMale = false;
 		}
 		player = createPerson(id.concat(attribution.substring(2)));
 		setRandomData();
 	}
 	
 	/**
 	 * Show all the result of the matching,just for testing
 	 * 
 	 */
 	public static void showAllResult(){
 		if(IsPlayerMale){
 			male.add(player);
 		}
 		else{
 			female.add(player);
 		}
 		Person[] onePair;
 		for(int i=0;i<100;i++){
 			onePair = getOnePair();
 			System.out.println(onePair[0]+"----"+onePair[1]);
 			male.remove(onePair[0]);
 			female.remove(onePair[1]);
 		}
 	}
 	
 	/**
 	 * Add player to the matching and get the result
 	 * @return Person ,the player's partner
 	 */
 	public static Person getMatcher(){
 		if(IsPlayerMale){
 			male.add(player);
 		}
 		else{
 			female.add(player);
 		}
 		Person[] result = getOnePair();
 		if(IsPlayerMale){
 			while(result[0].getId()!= player.getId()){
 				male.remove(result[0]);
 				female.remove(result[1]);
 				result = getOnePair();
 			}
 			return result[1];
 		}
 		else{
 			while(result[1].getId()!= player.getId()){
 				male.remove(result[0]);
 				female.remove(result[1]);
 				result = getOnePair();
 			}
 			return result[0];
 		}
 	}
 	
 	/**
 	 * Read data from file of path,and set the data to player of specific line number
 	 * line Number is begin from 1
 	 * @param path
 	 * @param p
 	 */
 	private static Person readFileToPlayer (String path,int lineNumber) {
 		Person player = null;
 		FileReader fr = null ;
 		BufferedReader br = null;
 		try {
 			fr = new FileReader(new File(path));
 			br = new BufferedReader(fr);
 			String line ;
 			String[] val;
 			for(int i=0;i<NUM_OF_EACH_GENDER;i++){
 				if(i == lineNumber-1){
 					line =br.readLine();
 					val = line.split(",");
 					if(val[0].equals("0")){
 						IsPlayerMale = false;
 					}
 					else if(val[0].equals("1")){
 						IsPlayerMale = true;
 					}
 					player = new Person(-1,Integer.parseInt(val[1]),
 							Integer.parseInt(val[2]),Integer.parseInt(val[3]),
 							Integer.parseInt(val[4]),Integer.parseInt(val[5]),
 							Integer.parseInt(val[6]));
 				}
 				else{
 					br.readLine();
 				}
 			}
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		finally{
 			try {
 				br.close();
 				fr.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		return player;
 		
 		
 	}
 	
 	/**
 	 * Read data from file which is in the path,and set the data to ArrayList<Person> p
 	 * @param path
 	 * @param p
 	 */
 	private static void readFileToPerson(String path,ArrayList<Person> p) {
 		FileReader fr = null ;
 		BufferedReader br = null;
 		try {
 			fr = new FileReader(new File(path));
 			br = new BufferedReader(fr);
 			String line ;
 			for(int i=0;i<NUM_OF_EACH_GENDER;i++){
 				line = br.readLine();
 				p.add(createPerson(line));
 			}
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		finally{
 			try {
 				br.close();
 				fr.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		
 		
 	}
 	
 	/**
 	 * Create a Person by analyzing a String of a line
 	 * @param str
 	 * @return Person who set data from str
 	 */
 	private static Person createPerson(String str){
 		String[] val = str.split(",");
 		return (new Person(Integer.parseInt(val[0]),Integer.parseInt(val[1]),
 							Integer.parseInt(val[2]),Integer.parseInt(val[3]),
 							Integer.parseInt(val[4]),Integer.parseInt(val[5]),
 							Integer.parseInt(val[6])));
 	}
 	
 	/**
 	 * Calculate the the score of targetPerson to sourcePerson,
 	 * adding some weight to guaranteer that the play who has 
 	 * bigger total value and smaller id is prior 
 	 * @param targetPerson
 	 * @param sourcePerson
 	 * @return the score of targetPerson to sourcePerson
 	 */
 	private static int calculate(Person targetPerson,Person sourcePerson){
 		return ((sourcePerson.getExpectLooks()*targetPerson.getLooks() 
 				+ sourcePerson.getExpectCharacter()*targetPerson.getCharacter()
 				+ sourcePerson.getExpectWealth()*targetPerson.getWealth())*300*100
 				+ (targetPerson.getLooks()+ targetPerson.getCharacter()
 				+ targetPerson.getWealth())*100 +(NUM_OF_EACH_GENDER-targetPerson.getId()));
 	}
 	
 	/**
 	 * Get one matching pair according to the specific rule
 	 * @return Person[], index 0 is male,index 1 is female
 	 */
 	public static Person[] getOnePair(){
 		//males vote one who has the highest score in exists females
 		//meanwhile female can record the male who has the highest score among voters
 		int voteeIndex,score;
 		int size = female.size();
 		initializeVote(voteBox,0,size);
 		for(Person voter:male){
 			if(voter.getTarget()== null || (!female.contains(voter.getTarget()))){
 				setHighestScoreFemale(voter);
 			}
 			
 			voteeIndex = female.indexOf(voter.getTarget());
 			
 			voteBox[voteeIndex]++;
 			score = calculate(voter, female.get(voteeIndex));
 			if(score>female.get(voteeIndex).getTargetScore()){
 				female.get(voteeIndex).setTarget(voter);
 				female.get(voteeIndex).setTargetScore(score);
 			}
 		}
 		
 		//find the female who is the most popular
 		int index = 0;
 		for(int i=1;i<size;i++){
 			if(compareVote(i,index,voteBox,female)>0){
 				index = i;
 			}
 		}
 		
 		//return the pair
 		return new Person[]{female.get(index).getTarget(),female.get(index)};
 	}
 	
 	/**
 	 * Compare two female's voteBox to find the winner according to the rule that 
 	 * the one who has higher summary of attributions and smaller id will win when 
 	 * having the same votes
 	 * @param index1 
 	 * @param index2
 	 * @param voteBox
 	 * @return
 	 */
 	public static int compareVote(int index1,int index2,int[] voteBox,ArrayList<Person> female){
 		Person f1 = female.get(index1);
 		Person f2 = female.get(index2);
 		if(voteBox[index1]>voteBox[index2]){
 			return 1;
 		}
 		else if(voteBox[index1]<voteBox[index2]){
 			return -1;
 		}
 		else if(f1.getLooks()+f1.getCharacter()+f1.getWealth()
 				>f2.getLooks()+f2.getCharacter()+f2.getWealth()){
 			return 1;
 		}
 		else if(f1.getLooks()+f1.getCharacter()+f1.getWealth()
 				<f2.getLooks()+f2.getCharacter()+f2.getWealth()){
 			return -1;
 		}
 		else if(f1.getId()<f2.getId()){
 			return 1;
 		}
 		else{
 			return -1;
 		}
 	}
 	
 	/**
 	 * For specific male,set a female who is mostly fitted his expectation to his target
 	 * in remaining females 
 	 * @param man
 	 */
 	public static void setHighestScoreFemale(Person man){
 		int score;
 		man.setTarget(null);
 		man.setTargetScore(0);
 		for(int i=0;i<female.size();i++){
 			score = calculate(female.get(i),man);
 			if(score>man.getTargetScore()){
 				man.setTargetScore(score);
 				man.setTarget(female.get(i));
 			}			
 		}
 	}
 	
 	/**
 	 * Use  value to initialize array a whose length is size 
 	 * @param a
 	 * @param value
 	 * @param size
 	 */
 	private static void initializeVote(int[] a,int value,int size){
 		for(int i=0;i<size;i++){
 			a[i] = value;
 		}
 	}
 	
 	/**
 	 * Set random generated data to male and female
 	 */
 	private static void setRandomData(){
 		male = generateRandomData();
 		female = generateRandomData();
 	}
 	
 	/**
 	 * Generate NUM_OF_EACH_GENDER person with random value between [1,98]
 	 * @return ArrayList<Person>
 	 */
 	private static ArrayList<Person> generateRandomData(){
 		ArrayList<Person> persons = new ArrayList<Person>();
 		Random ran = new Random();
 		for(int i=0;i<NUM_OF_EACH_GENDER;i++){
 			persons.add(new Person(i,ran.nextInt(98)+1,ran.nextInt(98)+1,ran.nextInt(98)+1,
					ran.nextInt(98)+1,ran.nextInt(98)+1,ran.nextInt(98)+1));
 		}
 		return persons;
 	}
 	
 	/**
 	 * Clear all the data
 	 */
 	public static void clearData(){
 		female.clear();
 		male.clear();
 		player = null;
 	}
 	
 	
 }
