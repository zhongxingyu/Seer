 package n3phele.service.model;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import n3phele.service.model.core.Collection;
 import n3phele.service.model.core.Entity;
 
 @XmlRootElement(name="AccountDataCollection")
 @XmlType(name="Collection", propOrder={"total", "elements"})
 public class AccountDataCollection extends Entity {
 	private long total;
 	private List<AccountData> elements;
 	/**
 	 * 
 	 */
 	public AccountDataCollection() {
 		super();
 	}
 
 	/**
 	 * @param name
 	 * @param uri
 	 * @param elements
 	 */
 	public AccountDataCollection(List<AccountData> e) {
 		elements = e;
 		total = e.size();
 	}
 
 	/**
 	 * @return the total
 	 */
 	public long getTotal() {
 		return total;
 	}
 
 	/**
 	 * @param total the total to set
 	 */
 	public void setTotal(long total) {
 		this.total = total;
 	}
 
 	/**
 	 * @return the elements
 	 */
 	public List<AccountData> getElements() {
 		return elements;
 	}
 
 	/**
 	 * @param elements the elements to set
 	 */
 	public void setElements(List<AccountData> elements) {
 		this.elements = elements;
 	}
 
 
 }
