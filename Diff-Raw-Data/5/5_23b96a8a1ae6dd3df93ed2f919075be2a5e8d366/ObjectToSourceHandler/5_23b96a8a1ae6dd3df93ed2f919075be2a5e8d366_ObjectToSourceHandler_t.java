 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.trickl.crawler.handle;
 
 import com.trickl.crawler.api.Task;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.StringReader;
 import javax.xml.transform.Source;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 import org.apache.droids.exception.DroidsException;
 import org.w3c.dom.Node;
 
 public class ObjectToSourceHandler<T extends Task> implements TaskResultHandler<T, Object>
 {
    private TaskResultHandler<T, Source> outputHandler;
 
    public ObjectToSourceHandler()
    {
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public void handle(T task, Object object) throws DroidsException, IOException
    {
       if (task == null || object == null) throw new NullPointerException();
       Source source = null;
       if (object instanceof Node) {
          source = new DOMSource((Node) object);
       }
       else if (object instanceof InputStream) {
          source = new StreamSource((InputStream) source);
       }
       else if (object instanceof String) {
         
         source = new StreamSource(new StringReader((String) object));
       }
       else {
          throw new DroidsException("ObjectToSourceHandler cannot handle type:" + source.getClass().getCanonicalName());
       }
 
       if (outputHandler != null)
       {  
          outputHandler.handle(task, source);         
       }
    }
 
    public void setOutputHandler(TaskResultHandler<T, Source> outputHandler)
    {
      this.outputHandler = outputHandler;
    }
 }
