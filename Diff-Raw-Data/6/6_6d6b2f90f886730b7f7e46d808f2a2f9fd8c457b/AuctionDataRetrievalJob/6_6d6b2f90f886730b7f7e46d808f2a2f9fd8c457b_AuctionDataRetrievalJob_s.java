 package au.wow.auctionhouse.retriever;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.quartz.Job;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 import au.wow.auctionhouse.comms.AuctionHouseComms;
 import au.wow.auctionhouse.dao.AuctionHouseDAO;
 import au.wow.auctionhouse.dao.AuctionHouseDAOImpl;
 import au.wow.auctionhouse.model.AuctionHouseSnapshot;
 import au.wow.auctionhouse.model.AuctionHouseSnapshotDetails;
 
 public class AuctionDataRetrievalJob implements Job {
 	
 	/**
 	 * This job will check if a new set of auction data is available for retrieval. If it is then, a file will be saved
 	 * with the data in it, otherwise nothing will happen. 
 	 * 
 	 * Can be run for any realm/region, and is to be managed from a Quartz job. 
 	 */
 	public void execute(JobExecutionContext context) throws JobExecutionException {
 		
 		try {
 			JobDataMap data = context.getMergedJobDataMap();
 			String realm = getNonEmptyStringFromJobMap(data, "REALM");
 			String region = getNonEmptyStringFromJobMap(data, "REGION");
 			String path = getNonEmptyStringFromJobMap(data, "FILEPATH");
 			
 			// construct a realm specific path
 			path = String.format("%s\\%s\\%s", path, region, realm);
 			
 			log("Checking auction data updates for realm '" + realm + "' on region '" + region + "'");
 			
 			AuctionHouseSnapshot snapshot = null;
 			Date today = new Date();
 			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
 			path += "\\" + df.format(today);
 			
 			AuctionHouseComms ahComms = new AuctionHouseComms();
 			//ahComms.setProxyHost("192.168.105.2");
 			//ahComms.setProxyPort(3128);
 			AuctionHouseSnapshotDetails snapshotDetails = ahComms.getAuctionHouseSnapshotDetails(region, realm);
 			AuctionHouseDAO dao = new AuctionHouseDAOImpl();
 			boolean created = false;
 			
 			if (!dao.isLatestSnapshot(path, snapshotDetails)) {
				snapshot = ahComms.getAuctionHouseSnapshot(snapshotDetails);
				created = dao.saveAuctionHouseSnapshotToFile(path, snapshotDetails, snapshot);
 			}
 			
 			if (created) {
 				log("Saved auction file: " + path + "\\" + snapshotDetails.getLastModified());
 			} else {
 				log("Snapshot " + path + "\\" + snapshotDetails.getLastModified() + " already exists.");
 			}		
 			
 		} catch (Throwable e) {
 			e.printStackTrace();
 			throw new JobExecutionException(e.getCause());
 		}
 	}
 	
 	public String getNonEmptyStringFromJobMap(JobDataMap data, String key) {
 		String result = data.getString(key);
 		if (result == null || "".equals(result)) {
 			throw new NullPointerException("data for the key of " + key + " was not found on the job data");
 		}
 		return result;
 	}
 	
 	private void log(String value) {
 		System.out.print(new Date());
 		System.out.println(" - " + value);
 	}
 }
