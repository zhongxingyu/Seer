 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the @author tags. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.mbui.client.cui;
 
 import com.google.gwt.user.client.ui.Widget;
 import org.jboss.mbui.client.aui.aim.InteractionUnit;
 
 /**
  * @author Harald Pehl
  * @date 10/25/2012
  */
 public abstract class Reification
 {
     protected final InteractionUnit interactionUnit;
 
     protected Reification(final InteractionUnit interactionUnit)
     {
         assert interactionUnit != null : "InteractionUnit must not be null";
         this.interactionUnit = interactionUnit;
     }
 
     public abstract Widget create(Context context);
 
     public abstract Widget update(Context context);
 }
