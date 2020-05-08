 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 
 /**
  * Default implementation of the {@link IParameterDescriptor} interface.
  */
 public class ParameterDescriptor implements IParameterDescriptor {
 
 	private String name;
 	private String shortDescription;
 	private String displayName;
 	private Type type;
 	private Object parent;
 	private Method readMethod;
 	private Method writeMethod;
 
 	/**
 	 * Creates a new ParameterDescriptor object with a given name and type,
 	 * associated with a given parent object, and with a given display name
 	 * and short description.
 	 * @param name the name of this parameter. The name must follow the JavaBean naming
 	 * conventions.
 	 * @param parent the object where this parameter is instantiated (or null for container-only).
 	 * @param displayName the localizable name of this parameter.
 	 * @param shortDescription a short localizable description of this parameter.
 	 */
 	public ParameterDescriptor (String name,
 		Object parent,
 		String displayName,
 		String shortDescription)
 	{
 		this.name = name;
 		this.parent = parent;
 		this.displayName = displayName;
 		this.shortDescription = shortDescription;
 		
 		// Case of UI-only parts: no parent to update
 		if ( parent == null ) return;
 			
 		try {
 			BeanInfo info;
 			info = Introspector.getBeanInfo(parent.getClass());
 			for ( PropertyDescriptor pd : info.getPropertyDescriptors() ) {
 				if ( pd.getName().equals(name) ) {
 					readMethod = pd.getReadMethod();
 					writeMethod = pd.getWriteMethod();
 				}
 			}
 		}
 		catch ( IntrospectionException e ) {
 			throw new RuntimeException(String.format(
 				"Introspection error when creating descriptor for '%s'", name), e);
 		}
 		
 		if ( readMethod == null ) {
 			throw new NullPointerException(String.format(
 				"The readMethod for '%s' is null.", name));
 		}
 		// Get the type of the parameter from the return type
 		this.type = readMethod.getGenericReturnType();
 	}
 	
 	public String getName () {
 		return name;
 	}
 
 	public String getDisplayName() {
 		return displayName;
 	}
 
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 	}
 
 	public String getShortDescription() {
 		return shortDescription;
 	}
 	
 	public void setShortDescription (String shortDescription) {
 		this.shortDescription = shortDescription;
 	}
 	
 	public Type getType () {
 		return type;
 	}
 	
 	public Method getReadMethod () {
 		return readMethod;
 	}
 	
 	public Method getWriteMethod () {
 		return writeMethod;
 	}
 	
 	public Object getParent () {
 		return parent;
 	}
 
 }
