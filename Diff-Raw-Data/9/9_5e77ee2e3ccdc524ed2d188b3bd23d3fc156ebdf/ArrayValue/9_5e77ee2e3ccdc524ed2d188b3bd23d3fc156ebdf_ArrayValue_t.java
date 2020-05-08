 /*
  * JBoss, the OpenSource J2EE webOS
  *
  * Distributable under LGPL license.
  * See terms of license at gnu.org.
  */
 package org.jboss.reflect;
 
 import java.util.Arrays;
 
 /**
  * Annotation value
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  */
 public class ArrayValue implements Value
 {
    protected TypeInfo type;
    protected Value[] values;
    protected int hash = -1;
 
    public ArrayValue(TypeInfo type, Value[] values)
    {
       this.type = type;
       this.values = values;
       calculateHash();
 
    }
 
    public TypeInfo getType()
    {
       return type;
    }
 
    public Value[] getValues()
    {
       return values;
    }
 
    public boolean equals(Object o)
    {
       if (this == o) return true;
       if (!(o instanceof ArrayValue)) return false;
 
       final ArrayValue arrayValue = (ArrayValue) o;
 
       if (!type.equals(arrayValue.type)) return false;
       if (!Arrays.equals(values, arrayValue.values)) return false;
 
       return true;
    }
 
    public void calculateHash()
    {
      // FIXME java5 hash = Arrays.hashCode(values);
       hash = hash * 29 +  type.hashCode();
    }
 }
