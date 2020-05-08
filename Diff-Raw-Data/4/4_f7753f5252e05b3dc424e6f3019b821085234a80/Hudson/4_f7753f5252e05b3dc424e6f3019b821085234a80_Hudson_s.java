 /*******************************************************************************
  *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  ******************************************************************************/
 package net.sourceforge.jcctray.model;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 
 /**
  * An implementation of {@link ICruise} that connects to Hudson server (<a
  * href="http://hudson.dev.java.net/">http://hudson.dev.java.net/</a>)
  * 
  * @author Ketan Padegaonkar
  */
 public class Hudson extends CruiseControlJava implements ICruise {
 
 	public String getName() {
 		return "Hudson";
 	}
 
 	protected String getXmlReportURL(Host host) {
 		return host.getHostName().replaceAll("/*$", "") + "/cc.xml";
 	}
 
 	protected String forceBuildURL(DashBoardProject project) {
 		String hostName = project.getHost().getHostName();
 		URL url = null;
 		try {
 			url = new URL(hostName);
 			int portNum = url.getPort();
 			if (portNum == -1) 
 				if (url.getProtocol().equals("http"))
 					portNum = 80;
 				else if (url.getProtocol().equals("https"))
 					portNum = 443;
 			return url.getProtocol() + "://" + url.getHost() + ":" + portNum + url.getPath().replaceAll("/*$", "")
 					+ "/job/" + project.getName() + "/build?delay=0sec";
 		} catch (MalformedURLException e) {
 			getLog().error("The url was malformed: " + url, e);
 		}
 		return null;
 	}
 }
