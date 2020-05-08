 package webspider.core.crawler;
 
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Spider implements myIWSpider {
 
 	private SpiderImpl spider;
 	@Override
 	public void openUserInterface() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void closeUserInterface() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void startIWSpider(String mySeed) {
 		try {
 			this.spider = new SpiderImpl(new URL(mySeed));
 			this.spider.start();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public boolean isIWRobotSafe(String myUrl) {
 		if (this.spider == null){
			throw new IllegalStateException("Spider hasn't been initialized");
 		}
 		try {
 			return this.spider.isRobotAllowed(new URL(myUrl));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	@Override
 	public void stopIWSpider() {
 		if (this.spider == null){
			throw new IllegalStateException("Spider hasn't been initialized");
 		}
 		this.spider.stop();
 	}
 
 	@Override
 	public void resumeIWSpider() {
 		if (this.spider == null){
			throw new IllegalStateException("Spider hasn't been initialized");
 		}
 		this.spider.start();
 		
 	}
 
 	@Override
 	public void killIWSpider() {
 		if (this.spider == null){
			throw new IllegalStateException("Spider hasn't been initialized");
 		}
 		try {
 			this.spider.stop();
 			this.spider.printToFile();
 			this.spider = null;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String[] getLocalIWUrls() {
 		List<String> stringURLs = new ArrayList<String>();
 		for (URL url : this.spider.getInternalLinksProcessed()){
 			stringURLs.add(url.toString());
 		}
 		return stringURLs.toArray(new String[]{});
 	}
 
 	@Override
 	public String[] getExternalIWURLs() {
 		List<String> stringURLs = new ArrayList<String>();
 		for (URL url : this.spider.getExternalLinksProcessed()){
 			stringURLs.add(url.toString());
 		}
 		return stringURLs.toArray(new String[]{});
 	}
 
 	
 }
