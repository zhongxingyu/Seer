 /**
  * 
  */
 package pl.psnc.dl.wf4ever.portal.myexpimport.model;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * @author Piotr Hołubowicz
  *
  */
 @XmlRootElement(name = "workflow")
 public class Workflow
 	extends SimpleResource
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3019438116219497825L;
 
 	private String contentUri;
 
 
 	/**
 	 * @return the filename
 	 */
 	@XmlElement
 	public String getFilename()
 	{
 		return contentUri.substring(contentUri.lastIndexOf('/') + 1).trim();
 	}
 
 
 	/**
 	 * @return the contentUri
 	 */
 	@XmlElement(name = "content-uri")
 	public String getContentUri()
 	{
 		return contentUri;
 	}
 
 
 	/**
 	 * @param contentUri the contentUri to set
 	 */
 	public void setContentUri(String contentUri)
 	{
 		this.contentUri = contentUri;
 	}
 
 
 	@Override
 	public String toString()
 	{
 		return String.format("workflow \"%s\"", getFilename());
 	}
 
 }
