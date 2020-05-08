 package time;
 
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.apache.commons.lang3.time.DateFormatUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 
 import election.Nodes;
 import election.Variables;
 
 public class TimeOper implements TimeOperInt{
 	Logger log = Logger.getLogger(TimeOper.class);
 	private int amount = 0;
 	private Date time = new Date();
 	
 	public void setAmount(int amount){
 		this.amount = amount;
 		log.debug("Error: "+amount);
 	}
 	public Date getTime(){
 		Date now = new Date();
 		
 		return now;
 	}
 	public String printTime(){
 		return time.toString();
 	}
 	public int getAmount(){
 		return amount;
 	}
 	public Date appendError(){
 		Date now = getTime();
 		DateUtils.addMilliseconds(now, amount);
 		time = now;
 		
 		return now;
 	}
 	public long getRunningTime(){
 		Date time = appendError();
 		long timeLong = time.getTime();
 		return timeLong;
 	}
 	public void getNodesTime(){
 		LinkedList<Nodes> nodesList = Variables.getNodesList();
 		Nodes indexNode = null;
 		Iterator<Nodes> nodesIter = nodesList.iterator();
 		while(nodesIter.hasNext()){
 			indexNode = nodesIter.next();
 			try{
 				Registry reg = LocateRegistry.getRegistry(indexNode.getIpAddr(),
 						indexNode.getRmiPort());
 				TimeOperInt timeOper = (TimeOperInt) reg.lookup("TimeOper");
 				long time = timeOper.getRunningTime();
 				long rtt = indexNode.getRtt();
 				indexNode.setTime(time + (rtt / 2));
 			}catch(RemoteException e){
 				e.printStackTrace();
 			}catch(NotBoundException e){
 				e.printStackTrace();
 			}
 		}
 	}
 	public void computeFix(){
 		BasicConfigurator.configure();
 		LinkedList<Nodes> nodesList = Variables.getNodesList();
 		Nodes indexNode = null;
 		Iterator<Nodes> nodesIter = nodesList.iterator();
 		Long timeSum = 0L;
 		Long timeSumAvg = 0L;
 		Long corTimeSum = 0L;
 		Long corTimeSumAvg = 0L;
 		int counter = 0;
 		
 		//Compute a general average
 		while(nodesIter.hasNext()){
 			timeSum += nodesIter.next().getTime();
 		}
 		timeSumAvg = timeSum / nodesList.size();
 		nodesIter = nodesList.iterator();
 		
		//Compute average but exclude extreme values (5 minutes)
 		while(nodesIter.hasNext()){
 			indexNode = nodesIter.next();
 			log.debug("node time: "+indexNode.getTime());
 			log.debug("timeSumAvg: "+timeSumAvg);
			if(((indexNode.getTime() - timeSumAvg) > 300000) || 
					((timeSumAvg - indexNode.getTime()) > 300000)){
 				continue;
 			}else{
 				corTimeSum += indexNode.getTime();
 				counter++;
 			}
 		}
 		corTimeSumAvg = corTimeSum / counter;
 		
 		//Store fix for every node
 		nodesIter = nodesList.iterator();
 		long fix =0L;
 		while(nodesIter.hasNext()){
 			indexNode= nodesIter.next();
 			fix = corTimeSumAvg - indexNode.getTime();
 			indexNode.setFix( (int) fix);
 		}
 	}
 	/*public String printWrongTime(){
 		Date now = getTime();
 		DateUtils.addMilliseconds(now, amount);
 		String time = printTime(now);
 		
 		return time;
 	}*/
 	public void fixErrorAmount(int fix){
 		setAmount(amount + fix);
 	}
 	public void pushFix(){
 		LinkedList<Nodes> nodesList = Variables.getNodesList();
 		Iterator<Nodes> nodesIt = nodesList.iterator();
 		Nodes indexNode = null;
 		while(nodesIt.hasNext()){
 			indexNode = nodesIt.next();
 			try{
 				Registry reg = LocateRegistry.getRegistry(indexNode.getIpAddr(),
 						indexNode.getRmiPort());
 				TimeOperInt timeOper = (TimeOperInt) reg.lookup("TimeOper");
 				timeOper.fixErrorAmount(indexNode.getFix());
 				reg = null;
 				timeOper = null;
 			}catch(RemoteException e){
 				e.printStackTrace();
 			}catch(NotBoundException e){
 				e.printStackTrace();
 			}
 		}
 	}
 }
