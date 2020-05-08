 package sma.ontology;
 
 import java.io.*;
 import java.util.StringTokenizer;
 
 import sma.gui.UtilsGUI;
 import java.util.*;
 /**
  * <p><B>Title:</b> IA2-SMA</p>
  * <p><b>Description:</b> Practical exercise 2011-12. Recycle swarm.</p>
  * Information about the current game. This object is initialized from a file.
  * <p><b>Copyright:</b> Copyright (c) 2011</p>
  * <p><b>Company:</b> Universitat Rovira i Virgili (<a
  * href="http://www.urv.cat">URV</a>)</p>
 * @author David Isern & Joan Albert Lpez
  * @see sma.CoordinatorAgent
  * @see sma.CentralAgent
  */
 public class InfoGame implements java.io.Serializable {
 
   private AuxInfo info;  //Object sent to the CoordinatorAgent during the initialization
   
   private int turn=0;
   private int gameDuration;
   private long timeout;
  
   static private boolean DEBUG = false;
 
   public InfoGame() {
 	  info=new AuxInfo();
   }
 
   public AuxInfo getInfo() {
 	return info;
   }
 
   public void setInfo(AuxInfo info) {
 	this.info = info;
   }
 
   private void showMessage(String s) {
     if(this.DEBUG)
       System.out.println(s);
   }
   
   public int getTurn() { return this.turn; }
   public void incrTurn() { this.turn++; }
   public int getGameDuration() {return this.gameDuration;}
   public void setGameDuration(int d) {this.gameDuration = d;}
   public long getTimeout() {return this.timeout;}
   public void setTimeout(long n) {this.timeout = n;}	
   public boolean isEndGame() { return (this.turn>=this.getGameDuration()); }
 
   public void writeGameResult(String fileOutput, Cell[][] t) throws IOException, Exception {
     File file= new File(fileOutput);
     String content = "" + this.getGameDuration()+"\n"+this.getTimeout()+"\n";
     UtilsGUI.writeFile(content,file);
     showMessage("File written");
   }
 
 
   public void readGameFile (String file) throws IOException,Exception {
 	FileReader fis = new FileReader(file);
     BufferedReader dis = new BufferedReader(fis);
     int NROWS = 0, NCOLS = 0;
     
 	String dades = dis.readLine(); 
 	StringTokenizer st = new StringTokenizer(dades, " ");
 	NROWS = Integer.parseInt(st.nextToken());
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	NCOLS = Integer.parseInt(st.nextToken());
 	this.info.map = new Cell[NROWS][NCOLS];
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	this.info.setNumFishingPhasesToNegotiate((Integer.parseInt(st.nextToken())));
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	this.info.setNumTotalNegotiations(Integer.parseInt(st.nextToken()));
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	this.info.setNumSeafoodGroups(Integer.parseInt(st.nextToken()));
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	this.info.setNumBoats(Integer.parseInt(st.nextToken()));
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	this.info.setMoneyPorts(Double.parseDouble(st.nextToken()));
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	this.info.setCapacityBoatsPerDeposit(Double.parseDouble(st.nextToken()));
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	this.info.setCapacityPortsPerDeposit(Double.parseDouble(st.nextToken()));
 	dades = dis.readLine(); st = new StringTokenizer(dades, " ");
 	String qtyPorts = st.nextToken();
 	
 	String[] portsSplit = qtyPorts.split(",");
 	Integer counter = 0;
 	
 	for (String qty : portsSplit)
 	{
 		this.info.setPortTypesQuantity(PortType.values()[counter], Integer.parseInt(qty));
 		InfoAgent agent = new InfoAgent(AgentType.Port);
		agent.setAgentType(AgentType.Boat);
 		agent.setPortType(PortType.values()[counter]);
 		agent.setQuantityOfLobster(info.getCapacityPorts());
 		agent.setQuantityOfOctopus(info.getCapacityPorts());
 		agent.setQuantityOfShrimp(info.getCapacityPorts());
 		agent.setQuantityOfTuna(info.getCapacityPorts());
 		agent.setMoneyAvailable(info.getMoneyPorts());
 		this.info.setPortAgent(agent);
 		counter++;
 	}
 	this.info.setNumPorts(counter);
 	
 	List<Cell> initialPositions = new ArrayList<Cell>();
 	
 	for (int row = 0; row < NROWS; row++)
 		for (int col = 0; col < NCOLS; col++)
 		{
 			Cell cell = new Cell(CellType.Sea);
 			cell.setRow(row); cell.setColumn(col);
 			this.info.map[row][col] = cell;
 		}
 		
 	//Now we set randomly cells for the quantity of boats defined between ranges NROWS - NCOLS
 	for (int ctr = 0; ctr < info.getNumBoats(); ctr++)
 	{
 		int rndRow = (int)  (Math.random() * ( (NROWS - 1) - 0 ));
 		int rndCol = (int) ( Math.random() * ( (NCOLS - 1) - 0 ));
 
 		this.info.map[rndRow][rndCol].setType(CellType.Boat);
 		InfoAgent agent = new InfoAgent(AgentType.Boat);
 		agent.setAgentType(AgentType.Boat);
 		agent.setName(AgentType.Boat.toString() + ctr, ctr +"");
 		agent.setQuantityOfLobster(info.getCapacityBoats());
 		agent.setQuantityOfOctopus(info.getCapacityBoats());
 		agent.setQuantityOfShrimp(info.getCapacityBoats());
 		agent.setQuantityOfTuna(info.getCapacityBoats());
 		this.info.map[rndRow][rndCol].addAgent(agent);
 		initialPositions.add(this.info.map[rndRow][rndCol]);
 	}
 	
   }
 }
