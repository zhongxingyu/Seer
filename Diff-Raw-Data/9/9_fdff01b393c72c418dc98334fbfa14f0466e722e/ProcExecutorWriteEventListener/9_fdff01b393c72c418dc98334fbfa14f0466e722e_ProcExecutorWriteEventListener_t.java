 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package beans;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.cache.Cache;
 import server.WriteEventListener;
 
 import java.util.concurrent.Callable;
 
 /**
  * 
  * A process executer output stream event listener.
  * This event listener writes all output from the process into the Play Cash. 
  * 
  * @author adaml
  *
  */
 public class ProcExecutorWriteEventListener implements WriteEventListener {
 
 	private String serverNodeId;
 
     private static Logger logger = LoggerFactory.getLogger( ProcExecutorWriteEventListener.class );
 	
 	private StringBuilder sb = null;
 	
 	private String keyFormat = "output-%s";
 
 	public ProcExecutorWriteEventListener(String serverNodeId) {
 		this.serverNodeId = serverNodeId;
 	}
 	
 	public ProcExecutorWriteEventListener() {
 		
 	}
 
     private String getKey(){
         return String.format( keyFormat, serverNodeId );
     }
 
     @Override
     public void init()
     {
         sb = new StringBuilder();
         logger.info( "initializing" );
         try {
             sb = Cache.getOrElse( getKey(), new Callable<StringBuilder>() {
                 @Override
                 public StringBuilder call() throws Exception
                 {
                    logger.info( "initializing a new string builder" );
                    return new StringBuilder();
                 }
            }, 0 );
         } catch ( Exception e ) {
             logger.error( "unable to getOrElse string builder", e );
             sb = new StringBuilder();
             Cache.set( getKey(), sb );
         }
     }
 
     @Override
 	public void writeEvent(int b) {
 		sb.append(Character.toChars(b));
 	}
 	
 	public void setKeyFormat( String keyFormat ){
 		this.keyFormat = keyFormat;
 	}
 
 	@Override
 	public void setKey(String serverNodeId) {
 		this.serverNodeId = serverNodeId;
 	}
 
 }
