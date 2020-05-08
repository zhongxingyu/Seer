 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.plugins.resolver.test;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.eclipse.swordfish.api.registry.EndpointDocumentProvider;
 import org.eclipse.swordfish.api.registry.ServiceDescription;
 
 /**
  *
  */
 public class EndpointDocumentProviderStub implements EndpointDocumentProvider {
 
 	ServiceDescriptionStub description;
 
 	public EndpointDocumentProviderStub(ServiceDescriptionStub description) {
 		this.description = description;
 	}
 
 	public Collection<ServiceDescription<?>> getServiceProviderDescriptions(QName portType) {
 		List<ServiceDescription<?>> result = new ArrayList<ServiceDescription<?>>();
 		result.add(description);
 		return result;
 	}
 
    public int getPriority() {
        // TODO Auto-generated method stub
        return 0;
    }

 }
