 /**
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  **/
 package org.jboss.demo.widgets.server;
 
 import net.sf.json.JSONArray;
 import org.jboss.demo.widgets.client.shared.Capital;
 import org.jboss.demo.widgets.client.shared.CapitalsSelected;
 import org.jboss.demo.widgets.client.shared.Client;
 import org.jboss.demo.widgets.client.shared.Server;
 import org.richfaces.cdi.push.Push;
 
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import java.util.List;
 
 /**
  * @author <a href="http://community.jboss.org/people/bleathem">Brian Leathem</a>
  */
 public class CapitalsObserver {
     @Inject
     CapitalsBean capitalsBean;
 
     @Inject @Push(topic = "capitalsSelected")
     private Event<String> jsfEvent;
 
     public void observeCapitalSelectionClientEvent(@Observes @Client CapitalsSelected capitalsSelected) {
         capitalsBean.setSelectedCapitals(capitalsSelected.getSelectedCapitals());
     }
 
     public void observeCapitalSelectionServerEvent(@Observes @Server CapitalsSelected capitalsSelected) {
        if (FacesContext.getCurrentInstance() != null) {
            jsfEvent.fire("capitalsSelected");
        }
         broadcastRest(capitalsSelected.getSelectedCapitals());
     }
 
     public void broadcastRest(final List<Capital> selectedCapitals) {
         AtmosphereHandler.lookupBroadcaster().broadcast(JSONArray.fromObject(selectedCapitals).toString());
     }
 }
