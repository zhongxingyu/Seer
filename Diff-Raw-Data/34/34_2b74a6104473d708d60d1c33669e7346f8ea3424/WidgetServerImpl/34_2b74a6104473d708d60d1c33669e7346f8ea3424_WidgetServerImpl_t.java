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
 
 import static server.Config.WIDGET_STOP_TIMEOUT;
 
 import java.io.*;
 import java.util.LinkedList;
 import java.util.List;
 
 import beans.config.Conf;
 import controllers.WidgetAdmin;
 
 import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import play.cache.Cache;
 import play.i18n.Messages;
 import play.mvc.Controller;
 
 import models.ServerNode;
 import models.Widget;
 import models.Widget.Status;
 import models.WidgetInstance;
 import server.*;
 import server.exceptions.ServerException;
 import utils.Utils;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 /**
  * This class provides ability to deploy/undeploy new widget by apiKey.
  * Before that the user must create an account by WidgetAdmin and register a new widget.
  * 
  * @author Igor Goldenberg
  * @see ServerPoolImpl
  * @see WidgetAdmin
  */
 public class WidgetServerImpl implements WidgetServer
 {
    private static Logger logger = LoggerFactory.getLogger( WidgetServerImpl.class );
     @Inject
     private ServerPool serverPool;
 
     @Inject
     private Conf conf;
 
     @Inject
     private DeployManager deployManager;
 
     private List<String> filterOutputLines = new LinkedList<String>(  );
     private List<String> filterOutputStrings = new LinkedList<String>(  );
 
     @PostConstruct
     public void init(){
         Utils.addAllTrimmed( filterOutputLines,  StringUtils.split( conf.cloudify.removeOutputLines, "|" ));
         Utils.addAllTrimmed( filterOutputStrings,  StringUtils.split( conf.cloudify.removeOutputString, "|" ));
     }
 	/**
 	 * Deploy a widget instance.
 	 * @param apiKey 
 	 */
 	public WidgetInstance deploy( String apiKey )
 	{
 		// don't allow for 30 seconds to start the widget again
 		Long timeLeft = (Long)Cache.get(Controller.request().remoteAddress());
 		if ( timeLeft != null )
         {
 			throw new ServerException( Messages.get( "please.wait.x.sec", (timeLeft - System.currentTimeMillis()) / 1000) );
         }
 
 		Widget widget = Widget.getWidget( apiKey );
 		if ( !widget.isEnabled() )
         {
 			throw new ServerException( Messages.get( "widget.disabled.by.administrator" ) );
         }
 			
 		File unzippedDir = Utils.downloadAndUnzip( widget.getRecipeURL(), apiKey );
 
 
         File recipeDir = unzippedDir;
         if ( widget.getRecipeRootPath() != null  ){
             recipeDir = new File( unzippedDir, widget.getRecipeRootPath() );
         }
        logger.info("Deploying an instance for recipe at : [{}] ", recipeDir );
 
 		ServerNode server = serverPool.get();
		if ( server == null ){
 			throw new ServerException(Messages.get("no.available.servers"));
        }
 		
 		widget.countLaunch();
 		
 		String instanceId = deployManager.fork(server, recipeDir).getId();
 		
 		return widget.addWidgetInstance( instanceId, server.getPublicIP() );
 	}
 	
 	
 	public void undeploy( String instanceId )
 	{
 		// keep the user for 30 seconds by IP, to avoid immediate widget start after stop
 		Cache.set(Controller.request().remoteAddress(), new Long(System.currentTimeMillis() + WIDGET_STOP_TIMEOUT*1000), WIDGET_STOP_TIMEOUT ); 
 		
 		serverPool.destroy(instanceId);
 	}
 
 	
 	public Status getWidgetStatus( String instanceId )
 	{
 		ProcExecutor pe = deployManager.getExecutor(instanceId);
 		if ( pe == null )
 			return new Status(Status.STATE_STOPPED, Messages.get( "server.was.terminated" ) );
 		
 		List<String> output = Utils.formatOutput(pe.getOutput(), pe.getPrivateServerIP() + "]", filterOutputLines, filterOutputStrings );
 		Status wstatus = new Status(Status.STATE_RUNNING, output, pe.getElapsedTimeMin());
 		
 		return wstatus;
 	}
 
     public void setServerPool(ServerPool serverPool) {
         this.serverPool = serverPool;
     }
 
     public void setDeployManager(DeployManager deployManager) {
         this.deployManager = deployManager;
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 }
