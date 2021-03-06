 /**
  * Maven and Sonar plugin for .Net
  * Copyright (C) 2010 Jose Chillan and Alexandre Victoor
  * mailto: jose.chillan@codehaus.org or alexvictoor@codehaus.org
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugin.dotnet.cpd;
 
 import org.sonar.api.Plugin;
 import org.sonar.api.Extension;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class is the container for all others extensions
  */
 public class DotnetCpdPlugin implements Plugin {
 
 	public static final String	KEY									= "dotnet-cpd";
 
 	public final static String	CPD_MINIMUM_TOKENS_PROPERTY			= "sonar.dotnet.cpd.minimumTokens";
 	public final static String	CPD_MINIMUM_TOKENS_DEFAULT_VALUE	= "100";
 
 	// The key which uniquely identifies your plugin among all others Sonar
 	// plugins
 	public String getKey() {
 
 		return KEY;
 	}
 
 	public String getName() {
 
 		return "Dotnet cpd plugin";
 	}
 
 	public String getDescription() {
 
 		return "Copy/Paste detector for dotnet projects";
 	}
 
 	// This is where you're going to declare all your Sonar extensions
 	public List<Class<? extends Extension>> getExtensions() {
 
 		List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();
 		list.add(CpdSensor.class);
		//list.add(SumDuplicationsDecorator.class);
		//list.add(DuplicationDensityDecorator.class);
		//list.add(CsCpdMapping.class);
 
 		return list;
 	}
 
 	@Override
 	public String toString() {
 
 		return getKey();
 	}
 }
