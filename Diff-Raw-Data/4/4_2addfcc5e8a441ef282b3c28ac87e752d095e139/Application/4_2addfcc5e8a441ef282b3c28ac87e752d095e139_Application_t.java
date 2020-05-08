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
 package controllers;
 
 import static utils.RestUtils.OK_STATUS;
 
 import akka.util.Duration;
 import models.ServerNode;
 import models.Widget;
 
 import org.apache.commons.lang.NumberUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.Play;
 import play.Routes;
 import play.cache.Cache;
 import play.i18n.Messages;
 import play.libs.Akka;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import server.ApplicationContext;
 import server.HeaderMessage;
 import server.exceptions.ServerException;
 import beans.events.Events;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.config.ServerConfig;
 import com.avaje.ebean.config.dbplatform.MySqlPlatform;
 import com.avaje.ebeaninternal.api.SpiEbeanServer;
 import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Widget controller with the main functions like start(), stop(), getWidgetStatus().
  * 
  * @author Igor Goldenberg
  */
 public class Application extends Controller
 {
     private static Logger logger = LoggerFactory.getLogger( Application.class );
     // guy - todo - apiKey should be an encoded string that contains the userId and widgetId.
     //              we should be able to decode it, verify user's ownership on the widget and go from there.
 	public static Result start( String apiKey, String hpcsKey, String hpcsSecretKey )
 	{
 		try
 		{
             // guy - todo - get rid of the "exception/catch" flow and use simple return result statements instead.
             // don't allow for 30 seconds to start the widget again
             Long timeLeft = (Long) Cache.get(Controller.request().remoteAddress());
             if (timeLeft != null) {
                 throw new ServerException(Messages.get("please.wait.x.sec", (timeLeft - System.currentTimeMillis()) / 1000));
             }
 
 			logger.info("starting widget with [apiKey, hpcsKey, hpcsSecretKey] = [{},{},{}]", new Object[]{apiKey, hpcsKey, hpcsSecretKey} );
  			Widget widget = Widget.getWidget( apiKey );
             ServerNode serverNode = null;
            	if ( widget == null || !widget.isEnabled()) {
                 	new HeaderMessage().setError( Messages.get("widget.disabled.by.administrator") ).apply( response().getHeaders() );
 	                return badRequest(  );
             }
 			ApplicationContext.get().getEventMonitor().eventFired( new Events.PlayWidget( request().remoteAddress(), widget ));
 			//TODO[adaml]: add proper input validation response
             if ( !StringUtils.isEmpty( hpcsKey ) && !StringUtils.isEmpty( hpcsSecretKey ) ){
                 if ( !isValidInput(hpcsKey, hpcsSecretKey) ) {
                     new HeaderMessage().setError(Messages.get("invalid.hpcs.credentials")).apply(response().getHeaders());
                     return badRequest();
                 }
 
                 serverNode = new ServerNode();
                 serverNode.setUserName( hpcsKey );
                 serverNode.setRemote(true);
                 serverNode.setApiKey( hpcsSecretKey );
                 serverNode.save();
             }else{
                 serverNode = ApplicationContext.get().getServerPool().get(widget.getLifeExpectancy());
                 if (serverNode == null) {
                     ApplicationContext.get().getMailSender().sendPoolIsEmptyMail();
                     throw new ServerException(Messages.get("no.available.servers"));
                 }
             }
 
             // run the "bootstrap" and "deploy" in another thread.
             final ServerNode finalServerNode = serverNode;
             final Widget finalWidget = widget;
             Akka.system().scheduler().scheduleOnce(
                     Duration.create(0, TimeUnit.SECONDS),
                     new Runnable() {
                         @Override
                         public void run() {
                             if (finalServerNode.isRemote()) {
                                 ApplicationContext.get().getServerBootstrapper().bootstrapCloud(finalServerNode);
                             }
                             ApplicationContext.get().getWidgetServer().deploy(finalWidget, finalServerNode);
                         }
                     });
 
             return statusToResult( new Widget.Status().setInstanceId(serverNode.getId().toString()).setRemote(serverNode.isRemote()) );
 		}catch(ServerException ex)
 		{
             return exceptionToStatus( ex );
 		}
 	}
 
 
     private static Result exceptionToStatus( Exception e ){
         Widget.Status status = new Widget.Status();
         status.setState(Widget.Status.State.STOPPED);
         status.setMessage(e.getMessage());
         return statusToResult(status);
     }
     private static Result statusToResult( Widget.Status status ){
         Map<String,Object> result = new HashMap<String, Object>();
         result.put("status", status );
         return ok( Json.toJson( result ));
     }
 
 	private static boolean isValidInput(String hpcsKey, String hpcsSecretKey) {
 		return !StringUtils.isEmpty(hpcsKey) && !StringUtils.isEmpty(hpcsSecretKey)
 				&& hpcsKey.contains(":") && !hpcsKey.startsWith(":") && !hpcsKey.endsWith(":");
 	}
 	
 	public static Result stop( String apiKey, String instanceId )
 	{
 		ServerNode serverNode = ServerNode.find.byId(Long.parseLong(instanceId));
 		if (serverNode.isRemote()) {
 			return notFound();
 		}else {
 			Widget widget = Widget.getWidget( apiKey );
 			if ( widget != null ){
 				ApplicationContext.get().getEventMonitor().eventFired( new Events.StopWidget( request().remoteAddress(), widget ) );
 			}
 			if ( instanceId != null ){
 				ApplicationContext.get().getWidgetServer().undeploy(instanceId);
 			}
 
 			return ok(OK_STATUS).as("application/json");
 		}
 	}
 
 	
 	public static Result getWidgetStatus( String apiKey, String instanceId )
 	{
 		try
 		{
             if (!NumberUtils.isNumber( instanceId )){
                 return badRequest();
             }
             ServerNode serverNode = ServerNode.find.byId( Long.parseLong(instanceId) );

 			Widget.Status wstatus = ApplicationContext.get().getWidgetServer().getWidgetStatus(serverNode);
 			return statusToResult(wstatus);
 		}catch(ServerException ex)
 		{
 			return exceptionToStatus( ex );
 		}
 	}
 
     public static Result generateDDL(){
         if ( Play.isDev() ) {
             EbeanServer defaultServer = Ebean.getServer( "default" );
 
             ServerConfig config = new ServerConfig();
             config.setDebugSql( true );
 
             DdlGenerator ddlGenerator = new DdlGenerator( ( SpiEbeanServer ) defaultServer, new MySqlPlatform(), config );
             String createDdl = ddlGenerator.generateCreateDdl();
             String dropDdl = ddlGenerator.generateDropDdl();
             return ok( createDdl );
         }else{
             return forbidden(  );
         }
     }
 
     public static Result javascriptRoutes()
     {
         response().setContentType( "text/javascript" );
         return ok(
                 Routes.javascriptRouter( "jsRoutes",
 
                         // Routes for Projects
                         routes.javascript.WidgetAdmin.getAllWidgets(),
                         routes.javascript.WidgetAdmin.checkPasswordStrength(),
                         routes.javascript.WidgetAdmin.postChangePassword(),
                         routes.javascript.WidgetAdmin.getPasswordMatch(),
                         routes.javascript.WidgetAdmin.deleteWidget()
 
                 )
         );
 
     }
 }
