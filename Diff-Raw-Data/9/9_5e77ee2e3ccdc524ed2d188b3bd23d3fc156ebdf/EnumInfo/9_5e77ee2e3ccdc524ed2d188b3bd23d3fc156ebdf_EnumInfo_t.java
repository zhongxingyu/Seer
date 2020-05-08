 /*
  * JBoss, the OpenSource J2EE webOS
  *
  * Distributable under LGPL license.
  * See terms of license at gnu.org.
  */
 package org.jboss.reflect;
 
 import java.util.HashMap;
 
 /**
  * comment
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  */
 public class EnumInfo extends ClassInfo
 {
    protected EnumConstantInfo[] enumConstants;
    protected HashMap constants = new HashMap();
 
    public EnumInfo(String name, int modifiers, AnnotationValue[] annotations, EnumConstantInfo[] enumConstants)
    {
       super(name, modifiers, null, null, null, null, null, annotations);
       this.enumConstants = enumConstants;
       for (int i = 0; i < enumConstants.length; i++)
       {
         constants.put(enumConstants[i].getName(), enumConstants[i]);
       }
    }
 
    public EnumConstantInfo[] getEnumConstants()
    {
       return enumConstants;
    }
 
    public EnumConstantInfo getEnumConstant(String name)
    {
       return (EnumConstantInfo)constants.get(name);
    }
 
    
 
 }
