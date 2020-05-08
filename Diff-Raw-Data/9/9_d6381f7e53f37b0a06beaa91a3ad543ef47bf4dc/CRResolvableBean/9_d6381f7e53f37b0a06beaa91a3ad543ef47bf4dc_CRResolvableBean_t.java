 package com.gentics.cr;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import com.gentics.api.lib.exception.UnknownPropertyException;
 import com.gentics.api.lib.resolving.PropertyResolver;
 import com.gentics.api.lib.resolving.Resolvable;
 
 
 /**
  * Rosolveable Proxy Class. As Resolvsables are not serializable this class gets
  * a resolvable and a list of attributes and stores these for further usage as
  * serializable bean.
  * 
  * This class also provides various methods to access attributes
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class CRResolvableBean implements Serializable, Resolvable{
 
 		
 	private static final long serialVersionUID = -8743515908056719834L;
 
 	private Collection<CRResolvableBean> childRepository;
 
 	private Hashtable<String,Object> attrMap;
 
 	private String contentid;
 	
 	private String obj_id;
 
 	private String obj_type;
 
 	private String mother_id;
 
 	private String mother_type;
 
 	private Resolvable resolvable;
 	
 		
 	/**
 	 * Populate the child elements with the given collection of CRResolvableBeans
 	 * @param childRep
 	 */
 	public void fillChildRepository(Collection<CRResolvableBean> childRep)
 	{
 		this.childRepository.addAll(childRep);
 	}
 	
 	/**
 	 * Get the Child elements.
 	 * @return collection of child elements.
 	 */
 	public Collection<CRResolvableBean> getChildRepository()
 	{
 		return(this.childRepository);
 	}
 	
 	/**
 	 * Set the child elements to the given collection of CRResolvableBeans
 	 * @param children
 	 */
 	public void setChildRepository(Collection<CRResolvableBean> children)
 	{
 		this.childRepository=children;
 	}
 	
 	/**
 	 * Create new instance of CRResolvableBean
 	 */
 	public CRResolvableBean()
 	{
 		this.contentid="10001";
 	}
 	
 	/**
 	 * Create new instance of CRResolvableBean.
 	 * This will generate an empty CRResolvableBean with only the contentid set.
 	 * @param contentid
 	 */
 	public CRResolvableBean(String contentid)
 	{
 		this.contentid=contentid;
 	}
 	
 	/**
 	 * Create new instance of CRResolvableBean
 	 * @param resolvable
 	 * 			Sets the given resolvable as basis for the CRResolvableBean
 	 * 			If obj_type of resolvable is 10008 it sets the attribute array to { "binarycontent", "mimetype" }, otherwise to { "binarycontent", "mimetype" }
 	 * 			If you want to be more specific about the attribute array, use public CRResolvableBean(Resolvable resolvable, String[] attributeNames) instead
 	 */
 	public CRResolvableBean(Resolvable resolvable) {
 		//TODO This is ugly => make more beautiful
 		if ("10008".equals(resolvable.get("obj_type").toString())) {
 			init(resolvable, new String[] { "binarycontent", "mimetype" });
 		} else {
 			init(resolvable, new String[] { "binarycontent", "mimetype" });
 		}
 	}
 
 	/**
 	 * make a CRResolvableBean out of a Resolvable
 	 * @param resolvable The Resolvable to be converted to a CRResolveableBean
 	 * @param attributeNames The attributenames as an array of strings that should be fetched from the Resolveable
 	 */
 	public CRResolvableBean(Resolvable resolvable, String[] attributeNames) {
 		init(resolvable, attributeNames);
 	}
 
 	/**
 	 * Initialize the CRResolvableBean with the Resolvable and populate elements / sets the Resolvable as member
 	 * @param resolvable
 	 * @param attributeNames
 	 */
 	private void init(Resolvable resolvable, String[] attributeNames) {
 		if (resolvable != null) {
 			this.resolvable = resolvable;
 			this.childRepository = new Vector<CRResolvableBean>();
 			this.contentid = (String) resolvable.get("contentid");
 			
 			if(resolvable.get("obj_id")!=null)
 				this.obj_id = ((Integer) resolvable.get("obj_id")).toString();
 			if(resolvable.get("obj_type")!=null)
 				this.obj_type = ((Integer) resolvable.get("obj_type")).toString();
 			
 			
 			if(resolvable.get("mother_obj_id")!=null)
 				this.mother_id = ((Integer) resolvable.get("mother_obj_id")).toString();
 			if(resolvable.get("mother_obj_type")!=null)
 				this.mother_type = ((Integer) resolvable.get("mother_obj_type")).toString();
 
 			this.attrMap = new Hashtable<String,Object>();
 			if (attributeNames != null) {
 				
 				ArrayList<String> attributeList = new ArrayList<String>(Arrays.asList(attributeNames));
 				if(attributeList.contains("binarycontenturl"))
 				{
 					this.attrMap.put("binarycontenturl","ccr_bin?contentid="+this.contentid);
 					attributeList.remove("binarycontenturl");
 					attributeNames=attributeList.toArray(attributeNames);
 				}
 				
 				for (int i = 0; i < attributeNames.length; i++) {
 					//we have to inspect returned attribute for containing not serializable objects (Resolvables) and convert them into CRResolvableBeans
 					try {
						Object o = inspectResolvableAttribute(PropertyResolver.resolve(resolvable, attributeNames[i]));
						if(o!=null)
							this.attrMap.put(attributeNames[i], o);
 					} catch (UnknownPropertyException e) {
						Object o = inspectResolvableAttribute(resolvable.get(attributeNames[i]));
						if(o!=null)
							this.attrMap.put(attributeNames[i], o);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Helper Method to inspect Attributes given from PropertyResolver or Resolvables theirself for containing not serializable Resolvables
 	 * @param resolvableAttribute The attribute should be inspected
 	 * @return the cleaned up attribute. All Resolvables are converted to CRResolvableBeans. The attribute should be serializable afterwards.
 	 */
 	@SuppressWarnings("unchecked")
 	private Object inspectResolvableAttribute(Object resolvableAttribute){
 		if(resolvableAttribute instanceof Collection){
 			//in Collections we must inspect all elements. We assume it is a parameterized Collection
 			//and therefore we quit if the first Object in the Collection is not a Resolvable
 			ArrayList<CRResolvableBean> newAttributeObject = new ArrayList<CRResolvableBean>();
 			for(Iterator<Object> it = ((Collection<Object>) resolvableAttribute).iterator(); it.hasNext(); ){
 				Object object = it.next();
 				if(object instanceof Resolvable){
 					newAttributeObject.add(new CRResolvableBean((Resolvable) object, new String[] {}));
 				}
 				else{
 					return resolvableAttribute;
 				}
 			}
 			return newAttributeObject;
 		}
 		else if(resolvableAttribute instanceof Resolvable){
 			return new CRResolvableBean((Resolvable) resolvableAttribute, new String[] {});
 		}
 		else
 			return resolvableAttribute;
 	}
 
 	/**
 	 * Gets the fetched attributes as Map.
 	 * @return attribute map
 	 */
 	public Map<String,Object> getAttrMap() {
 		return attrMap;
 	}
 	
 	/**
 	 * Sets the attributes of the CRResolvableBean to the given map of attributes.
 	 * @param attr
 	 * 		Checks if attr is instance of Hashtable. If true, it sets attr as the new attribute map. If false, a new Hashtable with the given map as basis is being generated.
 	 */
 	public void setAttrMap(Map<String,Object> attr)
 	{
 		if(attr instanceof Hashtable)
 		{
 			this.attrMap = (Hashtable<String,Object>)attr;
 		}
 		else
 		{
 			this.attrMap=new Hashtable<String,Object>(attr);
 		}
 	}
 
 	/**
 	 * Gets the contentid of the CRResolvableBean
 	 * @return contentid
 	 */
 	public String getContentid() {
 		return contentid;
 	}
 	
 	/**
 	 * Sets the contentid of the CRResolvableBean
 	 * @param id - contentid
 	 */
 	public void setContentid(String id)
 	{
 		this.contentid=id;
 	}
 
 	/**
 	 * Gets the mother contentid of the CRResolvableBean
 	 * @return motherid
 	 */
 	public String getMother_id() {
 		return mother_id;
 	}
 	
 	/**
 	 * Seths the mother contentid of the CRResolvableBean
 	 * @param id
 	 */
 	public void setMother_id(String id)
 	{
 		this.mother_id=id;
 	}
 
 	/**
 	 * Gets the type of the mother object
 	 * @return mothertype
 	 */
 	public String getMother_type() {
 		return mother_type;
 	}
 	
 	/**
 	 * Sets the type of the mother object
 	 * @param type
 	 */
 	public void setMother_type(String type)
 	{
 		this.mother_type=type;
 	}
 
 	/**
 	 * Gets the id of the object
 	 * @return objectid
 	 */
 	public String getObj_id() {
 		return obj_id;
 	}
 	
 	/**
 	 * Sets the id of the object
 	 * @param id
 	 */
 	public void setObj_id(String id)
 	{
 		this.obj_id=id;
 	}
 
 	/**
 	 * Gets the type of the Object
 	 * @return objecttype
 	 */
 	public String getObj_type() {
 		return obj_type;
 	}
 	
 	/**
 	 * Sets the type of the object
 	 * @param type
 	 */
 	public void setObj_type(String type)
 	{
 		this.obj_type=type;
 	}
 
 	/**
 	 * Returns true if this CRResolvableBean holds binary content
 	 * @return boolean
 	 */
 	public boolean isBinary() {
 		//TODO this is ugly => make more beautiful
 		if(this.attrMap.containsKey("binarycontent") && this.attrMap.get("binarycontent")!=null)
 		{
 			return(true);
 		}
 		else if ("10008".equals(this.getObj_type())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Gets the mimetype of the CRResolvableBean
 	 * @return
 	 */
 	public String getMimetype() {
 		return (String) this.attrMap.get("mimetype");
 	}
 	
 	/**
 	 * Returns the content attribute as string
 	 * @return content
 	 */
 	public String getContent() {
 		Object o = this.get("content");
 		try
 		{
 			return (String) o;
 		}
 		catch(ClassCastException ex)
 		{
 			//If type is not String then assume that byte[] would do the trick
 			//Not very clean
 			return new String((byte[]) o);
 		}
 	}
 	
 	/**
 	 * Gets the Content as String using the given encoding
 	 * @param encoding
 	 * 			Has to be a supported charset
 	 * 			US-ASCII  	Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
 	 *			ISO-8859-1   	ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
 	 *			UTF-8 	Eight-bit UCS Transformation Format
 	 *			UTF-16BE 	Sixteen-bit UCS Transformation Format, big-endian byte order
 	 *			UTF-16LE 	Sixteen-bit UCS Transformation Format, little-endian byte order
 	 *			UTF-16 	Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
 	 * @return content
 	 */
 	public String getContent(String encoding) {
 		Object bValue = this.get("content");
 		String value="";
 		if(bValue!=null && bValue.getClass()==String.class)
 		{
 			value=(String)bValue;
 		}
 		else
 		{
 			try {
 				value = new String(getBytes(bValue),encoding);
 				
 			}
 			catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return(value);
 	}
 	
 
 	/**
 	 * Gets the binary content if it is set otherwise returns null
 	 * @return binary content as array of bytes
 	 */
 	public byte[] getBinaryContent() {
 		Object o = this.get("binarycontent");
 		if(o instanceof String)
 		{
 			return ((String)o).getBytes();
 		}
 		else
 		{
 			return (byte[]) o;
 		}
 	}
 	
 	/**
 	 * Gets the binary content as InputStream if it is set otherwise returns null
 	 * @return
 	 */
 	public InputStream getBinaryContentAsStream()
 	{
 		byte[] buf = getBinaryContent();
 		InputStream os=null;
 		if(buf!=null)
 		{
 			os = new ByteArrayInputStream(getBinaryContent());
 		}
 		return(os);
 	}
 
 	/**
 	 * Gets the value of the requested attribute
 	 * 		Will first try to fetch the attribute from the Beans attribute array.
 	 * 		If attribute can not be fetched and a base resolvable is set, then it tries to fetch the attribute over the resolvable
 	 * @param attribute requested attribute name
 	 * @return value of attribute or null if value is not set
 	 */
 	public Object get(String attribute) {
 		if("contentid".equalsIgnoreCase(attribute)){
 			return this.getContentid();
 		}
 		else if(this.attrMap!=null && this.attrMap.containsKey(attribute)){
 			return this.attrMap.get(attribute);
 		}
 		else if(this.resolvable!=null){
 			//if we are returning an attribute from an resolvable we must inspect it for containing not serializable Objects
 			return inspectResolvableAttribute(this.resolvable.get(attribute));
 		}
 		else
 			return(null);
 	}
 
 	/**
 	 * Sets the value of the requested attribute
 	 * @param attribute - requested attribute name
 	 * @param obj - value of attribute
 	 */
 	public void set(String attribute, Object obj) {
 		if("contentid".equals(attribute)){
 			this.setContentid((String) obj);
 		}
 		else
 		{
 			if(this.attrMap==null)this.attrMap=new Hashtable<String,Object>();
 			this.attrMap.put(attribute, obj);
 		}
 	}
 	
 	/**
 	 * Converts an Object to an array of bytes
 	 * @param Object to convert
 	 * @return byte[] - converted object
 	 */
 	private byte[] getBytes(Object obj) throws java.io.IOException{
 	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	      ObjectOutputStream oos = new ObjectOutputStream(bos);
 	      oos.writeObject(obj);
 	      oos.flush();
 	      oos.close();
 	      bos.close();
 	      byte [] data = bos.toByteArray();
 	      return data;
 	  }
 	
 	/**
 	 * CRResolvableBean is always able to resolve properties
 	 * @return true
 	 */
 	public boolean canResolve() {
 		
 		return true;
 	}
 	
 	/**
 	 * Gets the value of the requested attribute.
 	 * 		Alias for get(String key)
 	 * @param key requested attribute name
 	 * @return value of attribute
 	 */
 	public Object getProperty(String key) {
 		return get(key);
 	}
 	
 	/**
 	 * A String representation of this CRResolvableBean instance
 	 * @return String contentid
 	 */
 	public String toString()
 	{
 		return this.getContentid();
 		
 	}
 }
