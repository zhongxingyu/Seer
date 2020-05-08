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
 
 import beans.api.ProcessStreamHandler;
 import models.ServerNode;
 
 import org.apache.commons.exec.ExecuteWatchdog;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.modules.spring.Spring;
 import server.ApplicationContext;
 import server.ProcExecutor;
 import beans.api.ExecutorFactory;
 import server.WriteEventListener;
 
 /**
  * A factory class for generating different process executors.
  * 
  * @author adaml
  *
  */
 public class ExecutorFactoryImpl implements ExecutorFactory {
 	
 	private static Logger logger = LoggerFactory.getLogger( ExecutorFactoryImpl.class );
 
    ServerNode serverNode = new ServerNode();

     public WriteEventListener getExecutorWriteEventListener( String key ){
         WriteEventListener writeEventListener = (WriteEventListener) Spring.getBean("executorWriteEventListener");
        writeEventListener.setKey( serverNode.getId().toString() );
         writeEventListener.init();
         return writeEventListener;
     }
 
     public ProcessStreamHandler getProcessStreamHandler( String key ){
         ProcessStreamHandler streamHandler = (ProcessStreamHandler) Spring.getBean("processStreamHandler");
         streamHandler.setWriteEventListener( getExecutorWriteEventListener( key ) );
         return streamHandler;
     }
 
 	@Override
 	public ProcExecutor getBootstrapExecutor( ServerNode serverNode ) {
 
 		logger.info("Creating bootstrap executor.");
         String key = getKey(serverNode);
 
         ProcessStreamHandler streamHandler = getProcessStreamHandler(key);
 		ExecuteWatchdog watchdog = new ExecuteWatchdog(ApplicationContext.get().conf().cloudify.bootstrapCloudWatchDogProcessTimeoutMillis);
 
         ProcExecutor executor = (ProcExecutor) Spring.getBean( "bootstrapExecutor" );
 		executor.setExitValue(0);
 		executor.setWatchdog(watchdog);
 		executor.setStreamHandler(streamHandler);
 		executor.setId(key);
 
         return executor ;
 	}
 
 	@Override
 	public ProcExecutor getDeployExecutor( ServerNode server ) {
 	
 		logger.info("Creating deploy executor.");
         String key = getKey(server);
 
         ProcessStreamHandler streamHandler = getProcessStreamHandler(key);
 		ExecuteWatchdog watchdog = new ExecuteWatchdog( ApplicationContext.get().conf().cloudify.bootstrapCloudWatchDogProcessTimeoutMillis );
 
         ProcExecutor executor = (ProcExecutor) Spring.getBean( "deployExecutor" );
 		executor.setExitValue(1);
 		executor.setWatchdog(watchdog);
 		executor.setStreamHandler(streamHandler);
 		executor.setId(key);
 
         return  executor;
 	}
 
     private String getKey(ServerNode server) {
         return server.getId().toString();
     }
 
 
 }
