 /*
  * ################################################################
  *
  * ProActive: The Java(TM) library for Parallel, Distributed,
  *            Concurrent computing with Security and Mobility
  *
  * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
  * Contact: proactive@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version
  * 2 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  Initial developer(s):               The ProActive Team
  *                        http://proactive.inria.fr/team_members.htm
  *  Contributor(s):
  *
  * ################################################################
  * $$PROACTIVE_INITIAL_DEV$$
  */
 package functionalTests.component.lookup;
 
 import org.junit.Assert;
 import org.objectweb.fractal.api.Component;
 import org.objectweb.fractal.api.factory.GenericFactory;
 import org.objectweb.fractal.api.type.ComponentType;
 import org.objectweb.fractal.api.type.InterfaceType;
 import org.objectweb.fractal.api.type.TypeFactory;
 import org.objectweb.fractal.util.Fractal;
 import org.objectweb.proactive.core.component.Constants;
 import org.objectweb.proactive.core.component.ContentDescription;
 import org.objectweb.proactive.core.component.ControllerDescription;
 import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.util.ProActiveInet;
 import org.objectweb.proactive.core.util.URIBuilder;
 
 import functionalTests.ComponentTest;
 import functionalTests.component.I1;
 
 
 public class Test extends ComponentTest {
 
     /**
      *
      */
     private ComponentType typeA;
     private Component componentA;
 
     public Test() {
         super("registration and lookup", "registration and lookup");
     }
 
     @org.junit.Test
     public void action() throws Exception {
         Component boot = Fractal.getBootstrapComponent();
         TypeFactory typeFactory = Fractal.getTypeFactory(boot);
         GenericFactory componentFactory = Fractal.getGenericFactory(boot);
         typeA = typeFactory.createFcType(new InterfaceType[] { typeFactory.createFcItfType("i1", I1.class
                 .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) });
         componentA = componentFactory.newFcInstance(typeA, new ControllerDescription("component-a",
             Constants.PRIMITIVE), new ContentDescription(A.class.getName()));
         Fractive.registerByName(componentA, "componentA");
        String url = URIBuilder.buildURIFromProperties(ProActiveInet.getInstance().getHostname(),
                "componentA").toString();
         Component retreived = Fractive.lookup(url);
         Assert.assertEquals(componentA, retreived);
     }
 }
