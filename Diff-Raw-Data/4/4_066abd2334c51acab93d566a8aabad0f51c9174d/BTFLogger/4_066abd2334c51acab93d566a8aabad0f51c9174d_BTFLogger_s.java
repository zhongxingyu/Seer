 package biosim.core.util;
 
 import biosim.core.sim.Simulation;
 import biosim.core.sim.Logger;
 
 import sim.engine.SimState;
 import sim.util.Double2D;
 import sim.util.MutableDouble2D;
 
 import java.io.File;
 //import java.nio.file.Files; //new to Java 7, includes convenience func for making tmp directories
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.BufferedWriter;
 import java.util.ArrayList;
 
 public class BTFLogger implements Logger{
 	//public ArrayList<String> ximage, yimage, timage, id, timestamp;
 	public BufferedWriter ximgout, yimgout, timgout, idout, timeout;
 	public File parentDirectory, tmpDir;
 	public String tmpFilePrefix;
 	public int runCtr = 0;
 	public BTFLogger(){
 		this(new File(System.getProperties().getProperty("user.dir")));
 	}
 	public BTFLogger(File dir){
 		parentDirectory = dir;
 		tmpFilePrefix = "BTFLogger-";
 	}
 
 	public void initFiles() throws IOException {
 		ximgout = new BufferedWriter(new FileWriter(new File(tmpDir, "xpos.btf")));
 		yimgout = new BufferedWriter(new FileWriter(new File(tmpDir, "ypos.btf")));
 		timgout = new BufferedWriter(new FileWriter(new File(tmpDir, "timage.btf")));
 		idout = new BufferedWriter(new FileWriter(new File(tmpDir, "id.btf")));
 		timeout = new BufferedWriter(new FileWriter(new File(tmpDir, "clocktime.btf")));		
 	}
 	public void nullFiles(){
 		ximgout = yimgout = timgout = idout = null;
 	}
 	public void closeFiles() throws IOException{
 		ximgout.close();
 		yimgout.close();
 		timgout.close();
 		idout.close();
 		timeout.close();
 	}
 	
 	public void init(){
 		try{
 			tmpDir = File.createTempFile(tmpFilePrefix,"-run-"+runCtr,parentDirectory);
 			runCtr++;
 			if(!(tmpDir.delete())){
 				throw new IOException("Could not delete "+tmpDir.getAbsolutePath());
 			}
 			if(!(tmpDir.mkdir())){
 				throw new IOException("Could not mkdir "+tmpDir.getAbsolutePath());
 			}
 			System.out.println("[BTFLogger] Starting new logs in "+tmpDir.getAbsolutePath());
 			this.initFiles();
 		} catch(IOException ioe){
 			System.err.println("[BTFLogger] Could not init logs: "+ioe);
 			this.nullFiles();
 		}
 	}
 	public void step(SimState simstate){
 		if(ximgout==null) return;
 		if(simstate instanceof Simulation){
 			Simulation sim = (Simulation)simstate;
 			for(int i=0;i<sim.bodies.size();i++){
 				Double2D loc = sim.field2D.getObjectLocation(sim.bodies.get(i));
 				MutableDouble2D dir = new MutableDouble2D(sim.bodyOrientations.get(i));
 				try{
 					ximgout.write(loc.x+"\n");
 					yimgout.write(loc.y+"\n");
 					timgout.write(dir.angle()+"\n");
					idout.write(sim.bodyIDs.getOrDefault(i,""+i)+"\n");
 					timeout.write((sim.schedule.getSteps()*sim.resolution)+"\n");
 				} catch(IOException e){
 					System.err.println("[BTFLogger] Error writing to log files: "+e);
 				}
 			}
 		}
 	}
 	public void finish(){
 		System.out.println("[BTFLogger] Finishing logs");
 		if(ximgout==null) return;
 		try{
 			this.closeFiles();
 		} catch (IOException ioe){
 			System.err.println("[BTFLogger] Error closing log files: "+ioe);
 		}
 	}
 }
