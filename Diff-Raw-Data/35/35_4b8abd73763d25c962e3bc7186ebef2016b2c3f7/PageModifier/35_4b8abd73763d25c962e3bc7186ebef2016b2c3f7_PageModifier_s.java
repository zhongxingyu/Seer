 /*
  * Copyright (c) 2007, Lan Boon Ping. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.ui.component.modifiers;
 
import java.util.List;
import java.io.PrintWriter;
 import java.io.IOException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.qi4j.api.annotation.Modifies;
 import org.qi4j.api.annotation.Uses;
import org.qi4j.ui.RenderFailedException;
 import org.qi4j.ui.InitFailedException;
 import org.qi4j.ui.association.HasComponents;
 import org.qi4j.ui.component.Component;
 import org.qi4j.ui.component.ComponentLifecycle;
 
 public final class PageModifier implements ComponentLifecycle
 {
     @Modifies private ComponentLifecycle next;
     @Uses private HasComponents hasComponents;
 
     public void init() throws InitFailedException
     {
         next.init();
     }
 
     public void dispose()
     {
         next.dispose();
     }
 
     public void render( HttpServletRequest request, HttpServletResponse response ) throws RenderFailedException
     {
         try
         {
             PrintWriter printWriter = response.getWriter();
             printWriter.write( "<html><head></head><body>" );
 
            List<Component> components = hasComponents.getComponents();
             for( Component component : components )
             {
                 component.render( request, response );
             }
 
             printWriter.write( "</body></html>" );
             next.render( request, response );
         }
         catch( IOException e )
         {
             throw new RenderFailedException( "Fail to render page.", e );
         }
     }
 }
