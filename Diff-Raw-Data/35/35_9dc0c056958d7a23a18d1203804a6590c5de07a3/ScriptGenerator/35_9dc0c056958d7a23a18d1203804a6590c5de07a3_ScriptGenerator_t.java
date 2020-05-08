 package game;
 
 import java.io.File;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Random;
 
 
 /**
  * This class will generate a script based on specifications from the
  * experimenter. For now we just look at min/max spawning intervals.
  * @author tim
  *
  */
 public class ScriptGenerator {
 	
 	private BufferedWriter scriptFile;
 	private Random rand = new Random();
 	public final static String SEP = "\t";
 	public int fishNum = 0;
	public String scriptname;
 	
 	public ScriptGenerator() {
		this(makeScriptFilename());
 	}
 	public ScriptGenerator(String scriptname){
		this.scriptname = scriptname;
	}
	
	private static String makeScriptFilename(){
		return "scripts/scriptv1_"+System.currentTimeMillis();
 	}
 	
 	/**
 	 * the user can specify what to use for the separator for script values
 	 */
 	public String sep = "\t";
 	
 	/**
 	 * this generates N script events where the inter-event interval is
 	 * between min/10 and max/10 seconds and is expressed in milliseconds
 	 * @param N
 	 */
 	public void generate(GameSpec g, int N) {
 		try {
			if (this.scriptname==null) {
				this.scriptname = makeScriptFilename();
			}
			if (this.scriptFile==null){
				try {
					this.scriptFile = new BufferedWriter(new FileWriter(new File(this.scriptname+".txt")));
					this.scriptFile.write("0"+sep+"version"+sep+RunGame.versionNum+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Error Opening Script File");
					e.printStackTrace();
				}
			}
 
 			scriptFile.write(g.toScript());
 
 			int nextInterval = 0;
 			int minFish = g.minFishRelease;
 			int maxFish = g.maxFishRelease;
 			for (int i = 0; i < N; i++) {
 				nextInterval = 1;
 				int r = rand.nextInt((maxFish - minFish) * 100);
 				long m = minFish * 100;
 				long theInterval = m + r;
 				boolean goodFish = rand.nextInt(2) == 1;
 				boolean fromLeft = rand.nextInt(2) == 1;
 				fishNum++;
 				String scriptLine = "" + theInterval + sep
 						+ (goodFish ? "good" : "bad ") + sep
 						+ (fromLeft ? "left " : "right") + sep + fishNum + "\n";
 				System.out.print(scriptLine);
 				scriptFile.write(scriptLine);
 
 			} // close for
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 	
 	public void close(){
 		try {
 			scriptFile.write("0\tgameover\t1\n ");
 			scriptFile.close();
			scriptFile = null;
			scriptname = null;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Problems closing scriptfile:"+e);
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args){
 		GameSpec g = new GameSpec();
 		ScriptGenerator gs = new ScriptGenerator();
 		gs.generate(g,10);
 		gs.close();
 	}
 
 }
