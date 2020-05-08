 package files;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import recomb.recListMaker;
 import simulator.sim;
 
 import coalescent.ArgHandler;
 import coalescent.CoalescentMain;
 public class fileReader {
 	ArgHandler fileSet;
 	String[] args;
 	boolean seeded = false;
 	private boolean debug = true;
 	public fileReader(ArgHandler aFileSet ){
 		fileSet = aFileSet;
 		
 	}
 	public void paramFileProcess(){
 		try {
 			BufferedReader stream = getFileToRead(fileSet.getParamFile().toString());
 			String line;
 			while((line = stream.readLine())!=null){//reading everything line by line
 				char[] charLine = line.toCharArray();
 					for(int i =0;i<charLine.length;i++){
 						if(charLine[i]=='#' && i == 0){//comment
 							i = charLine.length;
 							line = "";
 							//break;
 						}
 						else if(charLine[i]=='#' && i!=0){//comment at a point in line kill comment 
 							line = line.substring(0, i);
 							//processParamBuffer(line);
 							i = charLine.length;
 							
 						}
 					
 					}
 				if(!(line.length()==0)){
 					processParamBuffer(line);
 				}
 			}
 		stream.close();
 		if(!seeded){
 			System.out.println(String.format("coalescent seed: %d\n", -1 * cosiRand.randomNum.seedRNG()));
 		}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Error paramFileProcess" + e.getMessage() );
 		}
 	}
 	private void processParamBuffer(String line) {//gonna have to make some objects
 		//process the param file
 		if(line.contains("length")){//length has to be set before recomb file
 			System.out.println(line);
			CoalescentMain.simulator.simSetLength(Integer.parseInt(cleanString(line)[1]));
			//also need gc_set_length
 		}
 		else if (line.contains("recomb_file")){
 			System.out.println(line);
 			processRecombFile(line);
 		}
 		else if (line.contains("mutation_rate")){
 			System.out.println(line);
 			args = cleanString(line);
 			double mu =  Double.parseDouble(args[1]);
			CoalescentMain.simulator.setTheta(mu);
 			// set mutation rate in sim
 		}
 		else if (line.contains("infinite_sites")){
 			System.out.println(line);
 			args = cleanString(line);
 			String answer = args[1];
 			if(answer.equalsIgnoreCase("yes")){
 				//set infinite sites....
 			}
 		}
 		else if (line.contains("gene_conversion_rate")){
 			System.out.println(line);
 			args = cleanString(line);
 			double gcr = Double.parseDouble(args[1]);
 			CoalescentMain.geneConversion.setGCRate(gcr);
 		}
 		else if (line.contains("number_mutation_sites")){
 			System.out.println(line);
 			args = cleanString(line);
 			int numMut = Integer.parseInt(args[1]);
 			CoalescentMain.simulator.setNumMut(numMut);
 			
 		}
 		else if (line.contains("pop_label")){
 			System.out.println(line);
 			
 		}
 		else if (line.contains("pop_size")){
 			System.out.println(line);
 			args = cleanString(line);
 			int popName = Integer.parseInt(args[1]);
 			int popSize = Integer.parseInt(args[2]);
 			if(CoalescentMain.dem.setPopSizeByName(0, popName, popSize)!=1)
 			System.out.println("parameter file pop Specified does not exist ERROR" + line);
 		}
 		else if (line.contains("sample_size")){
 			//System.out.println(line);
 			args = cleanString(line);
 			int popName = Integer.parseInt(args[1]);
 			int sampleSize = Integer.parseInt(args[2]);
 			CoalescentMain.dem.populateByName(popName, sampleSize, 0);
 			if(fileSet.getSegFile()!= null){
 				String out = String.format("A %d %d\n", popName,sampleSize);
 				//write out to file
 			}
 		}
 		else if (line.contains("pop_define")){
 			System.out.println(line);
 			args = cleanString(line);
 			int popName = Integer.parseInt(args[1]);
 			char[] label = args[2].toCharArray();
 			//String 
 			CoalescentMain.dem.createPop(popName, label, 0);
 		}
 		else if (line.contains("pop_event")){
 			System.out.println(line);
 			CoalescentMain.histFactory.historicalProcessPopEvent(line);
 			
 		}
 		else if (line.contains("random_seed")){
 			System.out.println(line);
 			args = cleanString(line);
 			if(Double.parseDouble(args[1])>0){
 				long rseed = -1*Long.parseLong(args[1]);
 				cosiRand.randomNum.setRngSeed(rseed);
 				
 			}
 		}
 		else {
 			String err = line + "is not a valid parameter \n" +
 					"this parameter will be skipped";
 			System.out.println(err);
 		}
 	}
 	private void processRecombFile(String line) {
 		try{
 		BufferedReader aStream;
 		String fileType = "recomb_file";
 		//line.replace(" ", "");//maybe bad for windows machines with spaces in filename
 		line = line.trim();
 		line = line.substring(fileType.length());
 		aStream = getFileToRead(line.trim());
 			while((line = aStream.readLine() )!=null){
 				String[] result = line.split("\\s+");
 				int start = Integer.parseInt(result[0]);
 				double rate = Double.parseDouble(result[1]);
 				CoalescentMain.recomb.addRecombSiteLL(start, rate);
 			}
 		aStream.close();
 		CoalescentMain.recomb.recomb_calc_r();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	private BufferedReader getFileToRead(String aPath){
 	 BufferedReader bf;
 	try {
 		bf = new BufferedReader(new FileReader(aPath));
 		return bf;
 	} 	catch (FileNotFoundException e) {
 		e.printStackTrace();
 		return null;
 		}
 	}
 	
 	private String[] cleanString(String aLine){
 		String[] result = aLine.split("\\s+");
 		return result;
 	}
 	
 }
 
