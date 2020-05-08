 /****************************************************************************
  *
  * Copyright (c) 2013, Linagora
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  *
  *****************************************************************************/
 
 package controllers;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.ow2.petals.log.api.FlowBuilder;
 import org.ow2.petals.log.api.model.Flow;
 import org.ow2.petals.log.api.model.FlowStep;
 
 import models.Preferences;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import utils.MiscUtils;
 import views.html.*;
 
 /**
  * @author Vincent Zurczak - Linagora
  */
 public class Application extends Controller {
 
 	static Form<Preferences> prefsForm = Form.form( Preferences.class );
 
 
 	public static Result index() {
 		return ok(index.render("Your new application is ready."));
 	}
 
 
 	public static Result about() {
 		return ok( about.render());
 	}
 
 
 	public static Result help() {
 		return ok( help.render());
 	}
 
 
 	public static Result preferences() {
 
 		Map<String,String> locales = new LinkedHashMap<String,String> ();
 		locales.put( "en_US", "English" );
 		locales.put( "fr_FR", "Français" );
 
 		prefsForm = prefsForm.fill( MiscUtils.loadPreferences());
 		return ok( preferences.render( locales, prefsForm ));
 	}
 
 
 	public static Result updatePreferences() {
 
 		final Form<Preferences> filledForm = prefsForm.bindFromRequest();
 		Preferences newPrefs = filledForm.get();
 		MiscUtils.savePreferences( newPrefs );
 
 		return redirect( routes.Application.preferences());
 	}
 
 
 	public static Result flows() throws Exception {
 
 		String dir = MiscUtils.loadPreferences().logRootDirectory;
 		FlowBuilder flowBuilder = new FlowBuilder();
 		flowBuilder.setLogDirectories( dir );
 
 		return ok( flows.render( flowBuilder.findFlowIds()));
 	}
 
 
 	public static Result flow( String flowId ) {
 
 		String dir = MiscUtils.loadPreferences().logRootDirectory;
 		FlowBuilder flowBuilder = new FlowBuilder();
 		flowBuilder.setLogDirectories( dir );
 
 		Flow flowObject = flowBuilder.parseFlow( flowId );
 		return ok( flow.render( flowObject ));
 	}
 
 
 	public static Result step( String flowId, String stepId ) {
 
 		String dir = MiscUtils.loadPreferences().logRootDirectory;
 		FlowBuilder flowBuilder = new FlowBuilder();
 		flowBuilder.setLogDirectories( dir );
 
 		Flow flowObject = flowBuilder.parseFlow( flowId );
		FlowStep stepObject = flowObject.getRoot().findDescendant( stepId );
 
 		return ok( step.render( stepObject, flowId ));
 	}
 }
