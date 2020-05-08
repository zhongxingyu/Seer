 /**
  * 
  */
 package pl.psnc.dl.wf4ever.portal.myexpimport.model;
 
 import javax.xml.bind.annotation.XmlElement;
 
 import org.apache.wicket.util.crypt.Base64;
 
 /**
  * @author Piotr Hołubowicz
  *
  */
 public abstract class SimpleResource
 	extends BaseResource
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6545380482268434131L;
 
 	private String content;
 
 	private String contentType;
 
 
 	/**
 	 * @return the content base64-encoded
 	 */
 	public String getContent()
 	{
 		return content;
 	}
 
 
 	/**
 	 * @param content the content to set, base64-encoded
 	 */
 	public void setContent(String content)
 	{
 		this.content = content;
 	}
 
 
 	public byte[] getContentDecoded()
 	{
 		return Base64.decodeBase64(content);
 	}
 
 
 	/**
 	 * @return the contentType
 	 */
 	@XmlElement(name = "content-type")
 	public String getContentType()
 	{
 		return contentType;
 	}
 
 
 	/**
 	 * @param contentType the contentType to set
 	 */
 	public void setContentType(String contentType)
 	{
 		this.contentType = contentType;
 	}
 
 
 	public abstract String getFilename();
 
 }
