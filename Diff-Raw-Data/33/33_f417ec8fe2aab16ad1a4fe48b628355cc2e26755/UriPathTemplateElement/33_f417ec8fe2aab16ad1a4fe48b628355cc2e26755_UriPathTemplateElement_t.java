 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.ui.cnf;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.eclipse.debug.ui.actions.ILaunchable;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsEndpoint;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsResourceMethod;
 import org.jboss.tools.ws.jaxrs.ui.cnf.UriPathTemplateMediaTypeMappingElement.EnumCapabilityType;
 import org.jboss.tools.ws.jaxrs.ui.internal.utils.Logger;
 
public class UriPathTemplateElement implements ITreeContentProvider, ILaunchable {
 
 	private final IJaxrsEndpoint endpoint;
 
 	private final UriPathTemplateCategory uriPathTemplateCategory;
 
 	public UriPathTemplateElement(IJaxrsEndpoint endpoint, UriPathTemplateCategory uriPathTemplateCategory) {
 		super();
 		this.endpoint = endpoint;
 		this.uriPathTemplateCategory = uriPathTemplateCategory;
 	}
 
 	@Override
 	public Object[] getChildren(Object parentElement) {
 		List<Object> elements = new ArrayList<Object>(3);
 		elements.add(new UriPathTemplateMediaTypeMappingElement(endpoint.getConsumedMediaTypes(),
 				EnumCapabilityType.CONSUMES, endpoint.getResourceMethods().getLast().getJavaElement()));
 		elements.add(new UriPathTemplateMediaTypeMappingElement(endpoint.getProducedMediaTypes(),
 				EnumCapabilityType.PRODUCES, endpoint.getResourceMethods().getLast().getJavaElement()));
 		elements.add(new UriPathTemplateMethodMappingElement(endpoint.getResourceMethods()));
 		return elements.toArray();
 	}
 
 	@Override
 	public Object getParent(Object element) {
 		return uriPathTemplateCategory;
 	}
 
 	@Override
 	public boolean hasChildren(Object element) {
 		return (getChildren(element).length > 0);
 	}
 
 	@Override
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		// TODO Auto-generated method stub
 		Logger.trace("Input changed: {} -> {}", oldInput, newInput);
 	}
 
 	public boolean hasErrors() {
 		for (IJaxrsResourceMethod resourceMethod : endpoint.getResourceMethods()) {
 			if (resourceMethod.hasErrors()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/** @return the uriMapping */
 	public IJaxrsEndpoint getEndpoint() {
 		return endpoint;
 	}
 
 	public IJaxrsResourceMethod getLastMethod() {
 		return endpoint.getResourceMethods().getLast();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		UriPathTemplateElement other = (UriPathTemplateElement) obj;
 		if (endpoint == null) {
 			if (other.endpoint != null)
 				return false;
 		} else if (!endpoint.equals(other.endpoint))
 			return false;
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "UriPathTemplateElement [endpoint=" + endpoint + "]";
 	}
 
 }
