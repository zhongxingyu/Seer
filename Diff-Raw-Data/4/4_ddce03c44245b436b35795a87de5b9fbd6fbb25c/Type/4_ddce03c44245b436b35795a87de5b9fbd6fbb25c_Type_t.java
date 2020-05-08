 package org.codehaus.xfire.aegis.type;
 
 import java.util.Set;
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.wsdl.SchemaType;
 import org.jdom.Element;
 
 /**
  * Type
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public abstract class Type
     implements SchemaType
 {
     private QName schemaType;
     private TypeMapping typeMapping;
     private Class typeClass;
     private boolean abstrct = true;
     private boolean nillable = false;
     
     public Type()
     {
     }
     
     public abstract Object readObject( MessageReader reader, MessageContext context ) 
     	throws XFireFault;
     
     public abstract void writeObject( Object object, MessageWriter writer, MessageContext context ) 
     	throws XFireFault;
     
     public void writeSchema(Element root)
     {
     }
     
 	/**
 	 * @return Returns the typeMapping.
 	 */
 	public TypeMapping getTypeMapping()
 	{
 		return typeMapping;
 	}
     
 	/**
 	 * @param typeMapping The typeMapping to set.
 	 */
 	public void setTypeMapping( TypeMapping typeMapping )
 	{
 		this.typeMapping = typeMapping;
 	}
     
 	/**
 	 * @return Returns the typeClass.
 	 */
 	public Class getTypeClass()
 	{
 		return typeClass;
 	}
     
 	/**
 	 * @param typeClass The typeClass to set.
 	 */
 	public void setTypeClass( Class typeClass )
 	{
 		this.typeClass = typeClass;
 	}
 
     /**
      * @return True if a complex type schema must be written.
      */
     public boolean isComplex()
     {
         return false;
     }
 
     public boolean isAbstract()
     {
         return abstrct;
     }
 
     public void setAbstract(boolean abstrct)
     {
         this.abstrct = abstrct;
     }
     
     public boolean isNillable()
     {
         return nillable;
     }
     
     public void setNillable(boolean nillable)
     {
         this.nillable = nillable;
     }
 
     /**
      * Return a set of Type dependencies.  Returns null if this type
      * has no dependencies.
      * 
     * @return Set of <code>Type</code> dependencies
      */
     public Set getDependencies()
     {
         return null;
     }
     
     /**
      * @see java.lang.Object#equals(java.lang.Object)
      */
     public boolean equals(Object obj)
     {
         if (obj == this)
             return true;
 
         if ( obj instanceof Type )
         {
             Type type = (Type) obj;
 
             if ( type.getSchemaType().equals( getSchemaType() )
                  &&
                  type.getTypeClass().equals( getTypeClass() ) )
             {
                 return true;
             }
         }
         
         return false;
     }
     
     public int hashCode()
     {
         int hashcode = 0;
         
         if (getTypeClass() != null)
         {
             hashcode ^= getTypeClass().hashCode();
         }
         
         if (getSchemaType() != null)
         {
             hashcode ^= getSchemaType().hashCode();
         }
         
         return hashcode;
     }
     
     /**
      * @return Get the schema type.
      */
     public QName getSchemaType()
     {
         return schemaType;
     }
     
     /**
      * @param name The qName to set.
      */
     public void setSchemaType(QName name)
     {
         schemaType = name;
     }
 
     public boolean isWriteOuter()
     {
         return true;
     }
 }
