 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2010, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
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
  */
 
 package org.jboss.arquillian.container.appengine.remote_1_5;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.container.test.api.OperateOnDeployment;
 import org.jboss.arquillian.container.test.api.RunAsClient;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 
 /**
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 @RunWith(Arquillian.class)
 @RunAsClient
 public class AppEngineRemoteClientTestCase
 {
    /**
     * Deployment for the test
     *
     * @return web archive
     */
    @Deployment(name = "default")
    public static WebArchive getTestArchive()
    {
       return ShrinkWrap.create(WebArchive.class, "simple.war")
             .addClass(TestServlet.class)
             .setWebXML("gae-web.xml")
             .addAsWebInfResource("appengine-web.xml")
             .addAsWebInfResource("logging.properties");
    }
 
    @Test
    @OperateOnDeployment("default")
    public void shouldBeAbleToInvokeServletInDeployedWebApp() throws Exception
    {
      String body = readAllAndClose(new URL("http://arquillian-gae.appspot.com/test").openStream());
 
       Assert.assertEquals(
             "Verify that the servlet was deployed and returns expected result",
             TestServlet.MESSAGE,
             body);
    }
 
    private String readAllAndClose(InputStream is) throws Exception
    {
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       try
       {
          int read;
          while ((read = is.read()) != -1)
          {
             out.write(read);
          }
       }
       finally
       {
          try
          {
             is.close();
          }
          catch (Exception ignored)
          {
          }
       }
       return out.toString();
    }
 }
