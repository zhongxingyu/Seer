 package nz.co.searchwellington.model;
 
 public class PublisherContentCount {
 
 	private Website publisher;
 	private int count;
 	
 	public PublisherContentCount(Website publisher, int count) {		
 		this.publisher = publisher;
 		this.count = count;
 	}
 	
 	public Website getPublisher() {
 		return publisher;
 	}
 	
 	public void setPublisher(Website publisher) {
 		this.publisher = publisher;
 	}
 	
	public long getCount() {
 		return count;
 	}
 	
 	public void setCount(int count) {
 		this.count = count;
 	}
 	
 }
