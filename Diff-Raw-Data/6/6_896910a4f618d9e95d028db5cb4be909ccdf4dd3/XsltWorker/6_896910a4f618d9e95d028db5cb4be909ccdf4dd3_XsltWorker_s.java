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
 package com.trickl.crawler.robot.xslt;
 
 import com.trickl.crawler.api.Parser;
 import com.trickl.crawler.api.Task;
 import com.trickl.crawler.api.Worker;
 import java.io.IOException;
 import java.net.URI;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.droids.api.ManagedContentEntity;
 import org.apache.droids.api.Parse;
 import org.apache.droids.api.Protocol;
 import org.apache.droids.exception.DroidsException;
 
 import org.w3c.dom.Document;
 
 public class XsltWorker<T extends Task> implements Worker<T> {
 
    public final static Logger logger = Logger.getLogger(XsltWorker.class.getCanonicalName());
    private final XsltDroid<T> droid;
 
    public XsltWorker(XsltDroid<T> droid) {
       this.droid = droid;
    }
 
    @Override
    public void execute(T task) throws DroidsException, IOException {
       final String userAgent = this.getClass().getCanonicalName();
       logger.log(Level.FINE, "Starting {0}", userAgent);
       URI uri = task.getURI();
       final Protocol protocol = task.getProtocol();
             
       if (droid.getForceAllow() || protocol.isAllowed(uri)) {
          logger.log(Level.INFO, "Loading {0}", uri);
          
          ManagedContentEntity entity = protocol.load(uri);
          try {
             String contentType = entity.getMimeType();
             logger.log(Level.FINE, "Content type {0}", contentType);
             if (contentType == null) {
                logger.info("Missing content type... can't parse...");
             } else {
                Parser parser = droid.getParserFactory().getParser(contentType);
                if (parser != null) {
                   Parse parse = parser.parse(entity, task);
                   droid.getLinkExtractor().handle(task, (Document) parse.getData());
                   droid.getXslTransformHandler().handle(task, (Document) parse.getData());                  
                }
             }
         } finally {
             entity.finish();
          }
       } else {
          logger.info("Stopping processing since"
                  + " bots are not allowed for this url.");
       }
    }
 }
 
