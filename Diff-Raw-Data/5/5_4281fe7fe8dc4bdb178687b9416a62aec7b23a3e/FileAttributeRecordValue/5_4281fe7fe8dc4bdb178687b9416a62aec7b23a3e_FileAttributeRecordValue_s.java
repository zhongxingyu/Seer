 
 package edu.common.dynamicextensions.domain;
 
 import java.io.Serializable;
 
 import edu.common.dynamicextensions.domaininterface.FileAttributeRecordValueInterface;
 
 /**
  * This Class represents the a single value for multi select attribute.
  * 
  * @author Rahul Ner 
  * @hibernate.class  table="DE_FILE_ATTR_RECORD_VALUES"
  */
 public class FileAttributeRecordValue extends DynamicExtensionBaseDomainObject
 		implements
 			Serializable,
 			FileAttributeRecordValueInterface
 {
 
 	/**
 	 * content of the file.
 	 */
 	private byte[] fileContent;
 
 	/**
 	 * this is the name of the file 
 	 */
 	private String fileName;
 
 	/**
 	 * file content e.g. MIME text etc.
 	 */
 	private String contentType;
 
 	/**
 	 * This method returns the unique identifier of the AbstractMetadata.
 	 * @hibernate.id name="id" column="IDENTIFIER" type="long"
 	 * length="30" unsaved-value="null" generator-class="native"
 	 * @hibernate.generator-param name="sequence" value="DE_FILE_ATTR_REC_VALUES_SEQ"
 	 * @return the identifier of the AbstractMetadata.
 	 */
 	public Long getId()
 	{
 		return id;
 	}
 
 	/**
 	 * @return Returns the contentType.
 	 * @hibernate.property name="contentType" type="string" column="CONTENT_TYPE"
 	 */
 	public String getContentType()
 	{
 		return contentType;
 	}
 
 	/**
 	 * @param contentType The contentType to set.
 	 */
 	public void setContentType(String contentType)
 	{
 		this.contentType = contentType;
 	}
 
 	/**
 	 * @return Returns the fileContent.
 	 * @hibernate.property name="fileContent" type="edu.common.dynamicextensions.util.BinaryBlobType" column="FILE_CONTENT"
 	 */
 	public byte[] getFileContent()
 	{
 		return fileContent;
 	}
 
 	/**
 	 * @param fileContent The fileContent to set.
 	 */
 	public void setFileContent(byte[] fileContent)
 	{
 		this.fileContent = fileContent;
 	}
 
 	/**
 	 * @return Returns the fileName.
 	 * @hibernate.property name="fileName" type="string" column="FILE_NAME" 
 	 */
 	public String getFileName()
 	{
 		return fileName;
 	}
 
 	/**
 	 * @param fileName The fileName to set.
 	 */
 	public void setFileName(String fileName)
 	{
 		this.fileName = fileName;
 	}
 
 	/**
 	 * This method copies the values from one file record to another
 	 * @param fileRecordValue
 	 */
 	public void copyValues(FileAttributeRecordValue fileRecordValue)
 	{
 		this.contentType = fileRecordValue.getContentType();
 		this.fileName = fileRecordValue.getFileName();
 		this.fileContent = fileRecordValue.getFileContent();
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString()
 	{
 		return this.fileName;
 	}
 }
