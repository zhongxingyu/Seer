 package statistic;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.lang.acl.ACLMessage;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Vector;
 
 import jxl.write.WriteException;
 
 public class StatisticDispatcher extends Agent{
 
 	private static final long serialVersionUID = 1L;
 	
 	private String fileLocation = "statistic.csv";	// TODO: default value must be at another place
 	private Vector<StatisticPackage> packages = new Vector<StatisticPackage>() ;
 
 	@Override
 	protected void setup(){
 		fileLocation = (String)getArguments()[0];
 		addBehaviour(new StatisticDispatcherBehaviour());
 		confirmation((AID)getArguments()[1]);
 	}
 
 	private void confirmation(AID systemStarter) {
 		ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
 		confirm.addReceiver(systemStarter);
 		send(confirm);
 	}
 
 	void addPackage(StatisticPackage pack) {
 		packages.add(pack);
 	}
 
 	void exportToFile() {
 		try {
 			// TODO
 			File file = createFile();
 		//	System.out.println("Statistic " + file.getAbsolutePath());		#lao
 			writeStatistic(file);
			packages.clear();				
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void writeStatistic(File file) {
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(file,true));
 			for (StatisticPackage pack : packages){
 				pack.writeToFile(bw);
 			}
 			bw.flush();
 			bw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (WriteException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	private File createFile() throws IOException {
 		File file = new File(fileLocation);
 		if (!file.exists()) {
 			file.createNewFile();
 		}
 		return file;
 	}
 }
