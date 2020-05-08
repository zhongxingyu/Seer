 
 public class SharedObject {
 
 	public int getValue(int clientId, long opTime) throws InterruptedException {		
 		startReading();
 		try {
 			Thread.sleep(opTime);
			int service = ++Server.service;
			Server.readerOutput.append(service + "\t\t\t\t" + this.value + "\t\t\t\t" + "R" + clientId + "\t\t\t\t" + numberOfReaders + Common.NL);
 			return this.value;
 		} finally {
 			stopReading();
 		}
 	}
 	
 	public void setValue(int clientId, long opTime) throws InterruptedException {
 		startWriting();
 		try {
 			Thread.sleep(opTime);
			int service = ++Server.service;
 			this.value = clientId;
			Server.writerOutput.append(service + "\t\t\t\t" + this.value + "\t\t\t\t" + "W" + clientId + Common.NL);
 		} finally {
 			stopWriting();
 		}
 	}
 	
 	public synchronized void startReading() throws InterruptedException {
 		numberOfReadersTotal++;
 		while(numberOfWriters!=0) wait();
 		numberOfReaders++;
 	}
 	
 	public synchronized void stopReading() {
 		numberOfReaders--; 
 		numberOfReadersTotal--;
 		if (numberOfReadersTotal==0) notify();
 	}
 	
 	public synchronized void startWriting() throws InterruptedException {
 		numberOfWritersTotal++;
 		while (numberOfReadersTotal+numberOfWriters!=0) wait();
 		numberOfWriters=1;
 	}
 	
 	public synchronized void stopWriting() {
 		numberOfWriters = 0; numberOfWritersTotal--;
 		notifyAll();
 	}
 	
 	public int numberOfReadersTotal;
 	public int numberOfReaders;
 	public int numberOfWritersTotal;
 	public int numberOfWriters;
 	private int value = -1;
 }
