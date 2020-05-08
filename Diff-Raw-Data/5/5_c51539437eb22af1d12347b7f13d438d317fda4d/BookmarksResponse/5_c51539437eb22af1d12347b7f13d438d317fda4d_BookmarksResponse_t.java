 package fr.epsi.i4.bookmark.web;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.UriBuilder;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement(name = "bookmarks")
 public class BookmarksResponse {
 
 	private final List<Link> links = new ArrayList<>();
 	private final List<Link> navigationLinks = new ArrayList<>();
 	private int startIndex;
 	private int itemCount;
 
 	@XmlElement(name = "link")
 	public List<Link> getLinks() {
 		return links;
 	}
 	
 	@XmlElementWrapper(name="nav")
 	@XmlElement(name = "link")
 	public List<Link> getNavigationLinks() {
 		return navigationLinks;
 	}
 
 	public void addNavigationLink(Link link) {
 		navigationLinks.add(link);
 	}
 
 	public void addBookmarkLink(URI uri) {
 		links.add(new Link(uri, "bookmark"));
 	}
 
 	@XmlAttribute
 	public int getStartIndex() {
 		return startIndex;
 	}
 
 	public void setStartIndex(int startIndex) {
 		this.startIndex = startIndex;
 	}
 
 	@XmlAttribute
 	public int getItemCount() {
 		return itemCount;
 	}
 
 	public void setItemCount(int itemCount) {
 		this.itemCount = itemCount;
 	}
 	
 	public void addNavigationLinks(UriBuilder uriBuilder) {
		UriBuilder navUriBuilder = uriBuilder.clone().replaceQueryParam("startIndex", "{startIndex}");
 		if (this.startIndex > 0) {
 			int previousIndex = Math.max(0, this.startIndex - this.itemCount);
 			this.navigationLinks.add(new Link(navUriBuilder.build(previousIndex),"previousPage"));
 		}
 		if (!this.links.isEmpty() && this.links.size() == this.itemCount) {
 			this.navigationLinks.add(new Link(navUriBuilder.build(this.startIndex + this.itemCount),"nextPage"));
 		}
		this.navigationLinks.add(new Link(uriBuilder.path("latest").replaceQuery("").build(), "latest"));
 	}
 }
