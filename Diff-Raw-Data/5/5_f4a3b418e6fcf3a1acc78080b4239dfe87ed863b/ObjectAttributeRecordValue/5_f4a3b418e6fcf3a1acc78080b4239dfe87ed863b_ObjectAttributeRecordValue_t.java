 
 package edu.common.dynamicextensions.domain;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import edu.common.dynamicextensions.domaininterface.ObjectAttributeRecordValueInterface;
 
 /**
  * This Class represents the a single value for multi select attribute.
  * 
  * @author Rahul Ner 
  * @author Sujay Narkar
  * 
  * @hibernate.class  table="DE_OBJECT_ATTR_RECORD_VALUES"
  */
 public class ObjectAttributeRecordValue extends DynamicExtensionBaseDomainObject
 		implements
 			Serializable,
 			ObjectAttributeRecordValueInterface
 {
 
	/**
	 * added default serial version UID
	 */
	private static final long serialVersionUID = 1L;

 	private Object object;
 
 	/**
 	 * this is the name of the file 
 	 */
 	private String className;
 
 	/**
 	 * This method returns the unique identifier of the AbstractMetadata.
 	 * @hibernate.id name="id" column="IDENTIFIER" type="long"
 	 * length="30" unsaved-value="null" generator-class="native"
 	 * @hibernate.generator-param name="sequence" value="DE_OBJECT_ATTR_REC_VALUES_SEQ"
 	 */
 	public Long getId()
 	{
 		return id;
 	}
 
 	/**
 	 * @hibernate.property name="className" type="string" column="CLASS_NAME"
 
 	 * @return Returns the className.
 	 */
 	public String getClassName()
 	{
 		return className;
 	}
 
 	/**
 	 * @param className The className to set.
 	 */
 	public void setClassName(String className)
 	{
 		this.className = className;
 	}
 
 	/**
 	 * @param objectContent The objectContent to set.
 	 * @throws ClassNotFoundException 
 	 * @throws IOException 
 	 */
 	private void setObjectContent(byte[] objectContent) throws IOException, ClassNotFoundException
 	{
 		ByteArrayInputStream byteStream = new ByteArrayInputStream(objectContent);
 		ObjectInputStream ojectInputStream = new ObjectInputStream(byteStream);
 		object = ojectInputStream.readObject();
 	}
 
 	/**
 	 * @hibernate.property name="objectContent" type="edu.common.dynamicextensions.util.BinaryBlobType" column="OBJECT_CONTENT"
 	 * @return Returns the objectContent.
 	 * @throws IOException 
 	 */
 	private byte[] getObjectContent() throws IOException
 	{
 		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 		ObjectOutputStream ojectOutputStream = new ObjectOutputStream(byteStream);
 		ojectOutputStream.writeObject(object);
 		return byteStream.toByteArray();
 	}
 
 	/**
 	 * This method copies the values from one file record to another
 	 * @param fileRecordValue
 	 */
 	public void copyValues(ObjectAttributeRecordValue objectRecordValue)
 	{
 		this.className = objectRecordValue.getClassName();
 		this.object = objectRecordValue.getObject();
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString()
 	{
 		return this.className;
 	}
 
 	public Object getObject()
 	{
 		return object;
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.ObjectAttributeRecordValueInterface#setObject(java.lang.Object)
 	 */
 	public void setObject(Object value) throws IOException
 	{
 		this.object = value;
 	}
 }
