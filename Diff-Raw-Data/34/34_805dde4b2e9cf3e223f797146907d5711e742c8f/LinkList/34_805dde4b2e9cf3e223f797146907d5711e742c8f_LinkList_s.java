 package org.linkstorage.bean;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.linkstorage.model.Link;
 
 @XmlRootElement(name="links")
 public class LinkList {
 
 	private int count;
 	private List<Link> links;
 
 	public LinkList() {}
 
 	public LinkList(List<Link> links) {
 		this.links = links;
 		this.count = links.size();
 	}
 
 	public int getCount() {
 		return count;
 	}
 
 	public void setCount(int count) {
 		this.count = count;
 	}
 
 	@XmlElement(name="link")
 	public List<Link> getLinks() {
 		return links;
 	}
}
