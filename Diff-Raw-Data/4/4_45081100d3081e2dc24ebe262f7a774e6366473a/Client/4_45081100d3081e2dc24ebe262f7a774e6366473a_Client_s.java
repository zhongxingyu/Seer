 package sim.storage.cli;
 
 import sim.Request;
 import sim.Response;
 import sim.storage.manager.RAPoSDAStorageManager;
 
 public class Client {
 
 	private static final int NUM_REQUEST = 10;
 	private static final int REQ_SIZE = 1;
 	private double arrival = 0.0;
 
 	public double run(RAPoSDAStorageManager sm) {
 		double lastResponse = Double.MIN_VALUE;
 		for (int i=0; i < NUM_REQUEST; i++) {
			Request req = new Request(i, REQ_SIZE, arrival++);
 			Response res = sm.write(req);
 			double tempTime = req.getArrvalTime() + res.getResponseTime();
 			lastResponse = lastResponse < tempTime ? tempTime : lastResponse;
 		}
 		return lastResponse;
 	}
 }
