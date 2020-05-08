 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.designtime.el;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jst.jsf.common.internal.types.TypeConstants;
 import org.eclipse.jst.jsf.context.symbol.IBoundedTypeDescriptor;
 import org.eclipse.jst.jsf.context.symbol.IObjectSymbol;
 import org.eclipse.jst.jsf.context.symbol.ISymbol;
 import org.eclipse.jst.jsf.context.symbol.ITypeDescriptor;
 
 /**
  * A design time proxy for the runtime PropertyResolver.  This is used to
  * resolve all but the first element of a var.prop.prop2 type of sub-expression in
  * a JSF EL expression. @see DefaultDTVariableResolver for how to resolve 'var' at
  * designtime
  *
  * Clients may implement
  * 
  * @author cbateman
  */
 public class DefaultDTPropertyResolver extends AbstractDTPropertyResolver
 {
     /**
      * Returns a symbol encapsulating the property on base with the name
      * properyId
      * 
      * @param base
      * @param propertyId
      * @return the symbol for the named propertyId or null if not found
      */
     public ISymbol getProperty(ISymbol base, Object propertyId)
     {
         ITypeDescriptor typeDesc = null;
 
         Object[] factoredProperties = new Object[] {propertyId};
 
         // check for expected interface types per JSP.2.3.4
         if (base instanceof IObjectSymbol)
         {
             final IObjectSymbol objSymbol = (IObjectSymbol) base;
             typeDesc = objSymbol.getTypeDescriptor();
 
             // although not stated explicitly stated by the spec, in practice (based on Sun RI),
             // a list cannot have non-numeric indexed properties
             // note: due to remove(Object) having different return types
             // an object can't be both a List and a Map!  So we can consider
             // a List instanceof out of order
             if (objSymbol.supportsCoercion(TypeConstants.TYPE_LIST))
             {
                 typeDesc = null;
             }
             // per JSP.2.3.4, if instance of map (unconstrained in our terminology)
             else if (objSymbol.supportsCoercion(TypeConstants.TYPE_MAP))
             {
                 typeDesc = objSymbol.coerce(TypeConstants.TYPE_MAP);
 
                 // handle string keys into maps that contain dots.  Because type descriptor
                 // handle dotted property ids (i.e. 'x.y.z') as separate properties with
                 // intermediate parts, we need to handle this specially.
                 if (propertyId instanceof String && ((String)propertyId).indexOf('.')>-1)
                 {
                     factoredProperties = factorKey(propertyId);
                 }
             }
 
             // check unconstrained type
             if (typeDesc instanceof IBoundedTypeDescriptor)
             {
                 // TODO: propertyId may need to change when supporting
                 // template types
                 if (((IBoundedTypeDescriptor)typeDesc).isUnboundedForType(TypeConstants.TYPE_JAVAOBJECT))
                 {
                     // the most we know is that it could be an Object
                     return ((IBoundedTypeDescriptor)typeDesc).getUnboundedProperty(propertyId, TypeConstants.TYPE_JAVAOBJECT);
                 }
             }
         }
 
         int i = 0;
         ISymbol  matchedSymbol;
 
         do
         {
             matchedSymbol = null; // always reset so if the for completes without setting, the
                                   // while ends
             SEARCH_SEGMENT: for (final Iterator it = getIterator(typeDesc); it.hasNext();)
             {
                 final ISymbol element = (ISymbol) it.next();
 
                 if (element.getName().equals(factoredProperties[i])
                         && element instanceof IObjectSymbol)
                 {
                     matchedSymbol = element;
                     typeDesc = ((IObjectSymbol)matchedSymbol).getTypeDescriptor();
                     break SEARCH_SEGMENT;
                 }
             }
         } while(++i < factoredProperties.length && matchedSymbol != null);
 
         // may be null if none matched
         return matchedSymbol;
     }
     
     /**
      * @param base
      * @return all properties of base
      */
     public ISymbol[] getAllProperties(ISymbol base)
     {
         // if nothing found, return an empty array
         List  symbolsList =  Collections.EMPTY_LIST;
         
         if (base instanceof IObjectSymbol)
         {
             ITypeDescriptor typeDesc = null;
             
             // per JSP.2.3.4, if instance of map (unconstrained in our terminology)
             if (((IObjectSymbol)base).supportsCoercion(TypeConstants.TYPE_MAP))
             {
                 typeDesc = 
                     ((IObjectSymbol)base).coerce(TypeConstants.TYPE_MAP);
             }
             // Lists have no properties, even if they are also beans
             else if (((IObjectSymbol)base).supportsCoercion(TypeConstants.TYPE_LIST))
             {
                 typeDesc = null;
             }
             else
             {
                 typeDesc = ((IObjectSymbol)base).getTypeDescriptor();
             
             }
             
             if (typeDesc != null)
             {
                 symbolsList =  typeDesc.getProperties();
             }
         }
 
         return (ISymbol[]) symbolsList.toArray(ISymbol.EMPTY_SYMBOL_ARRAY);
     }
 
     public ISymbol getProperty(ISymbol base, int offset) 
     {
         ITypeDescriptor typeDesc = null;
 
         if (offset < 0)
         {
         	// should never be called with offset < 0
         	throw new AssertionError("offsets must be >=0 to be valid");
         }
         
         // check for expected interface types per JSP.2.3.4
         if (base instanceof IObjectSymbol)
         {
 
         	final IObjectSymbol objSymbol = (IObjectSymbol) base;
             typeDesc = objSymbol.getTypeDescriptor();
 
            // per JSP.2.3.4, if instance of map (unconstrained in our terminology)
             if (typeDesc.isArray())
             {
                 ISymbol arrayElement = typeDesc.getArrayElement();
                 // reset the name
                 arrayElement.setName(base.getName()+"["+offset+"]");
                 return arrayElement;
             }
             
             // per JSP.2.3.4, if instance of list (unbounded in our terminology)
             if (objSymbol.supportsCoercion(TypeConstants.TYPE_LIST))
             {
                 typeDesc = objSymbol.coerce(TypeConstants.TYPE_LIST);
             }
 
             // check unconstrained type
             if (typeDesc instanceof IBoundedTypeDescriptor)
             {
                 // TODO: propertyId may need to change when supporting
                 // template types
                 if (((IBoundedTypeDescriptor)typeDesc).isUnboundedForType(TypeConstants.TYPE_JAVAOBJECT))
                 {
                     // the most we know is that it could be an Object
                    return ((IBoundedTypeDescriptor)typeDesc).getUnboundedProperty(new Integer(offset), TypeConstants.TYPE_JAVAOBJECT);
                 }
             }
         }
         
         return null;
     }
     
     private Iterator getIterator(ITypeDescriptor typeDesc)
     {
         if (typeDesc != null)
         {
             return typeDesc.getProperties().iterator();
         }
         return Collections.EMPTY_LIST.iterator();
     }
     
     /**
      * Takes a key expression and factors it down to into all property segments it contains.
      * Property segments occur mainly when String keys contain '.' characters, indicating that
      * more one than property actually must be traversed to evaluate the whole expr.
      * @param key
      * @return an array containing all property segments of the key.  If the key contains only
      * one property, then this is returned a single element in the array
      */
     private Object[] factorKey(Object key)
     {
         if (key instanceof String)
         {
             List  segments = new ArrayList();
             
             String stringKey = (String) key;
             int nextPos = -1;
             
             while ((nextPos = stringKey.indexOf('.')) > -1)
             {
                 segments.add(stringKey.substring(0, nextPos));
                 stringKey = stringKey.substring(nextPos+1);
             }
             
             if (stringKey != null && stringKey.length() > 0)
             {
                 segments.add(stringKey);
             }
             
             return segments.toArray();
         }
 
         return new Object[] {key};
     }
 }
