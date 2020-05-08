 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.hubiquitus.hapi.structures;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @cond internal
  * @version 0.3
  *  JabberID contain the different part of the jid and some method to use it 
  *  A JabberID should look like : my_user@domain/resource
  */
 
 public class JabberID {
 	
 	
 	private String username = "";
 	private String domain = "";
 	private String resource = "";
 	
 	/**
 	 * constructor
 	 * @param jid - jabber id (ie : my_user@domain.com/resource)
 	 * @throws Exception - throw exception if invalid jid format
 	 */
 	public JabberID(String jid) throws Exception {
 		this.setJID(jid);
 	}
 	
 	/**
 	 * @return bare jid (ie : my_user@domain)
 	 */
 	public String getBareJID() {
 		return this.username + "@" + this.domain;
 	}
 	
 	/**
 	 * @return full jid (ie : my_user@domain/resource)
 	 */
 	public String getFullJID() {
 		if(resource != "")
 			return this.username + "@" + this.domain + "/" + this.resource;
 		else
 			return getBareJID();
 	}
 
 	/**
 	 * Set jid (either full or bare)
 	 * @param jid - jid (ie : my_user@domain or my_user@domain/resource)
 	 * @throws Exception - throw exception if invalid jid format
 	 */
 	public void setJID(String jid) throws Exception {
 		if (jid != null) {
			Pattern pattern = Pattern.compile("^(?:([^@/<>'\"]+)@)([^@/<>'\"]+)(?:/([^/<>'\"]*))?$");
 			Matcher matcher = pattern.matcher(jid);
			if (matcher.matches() && matcher.find()) {
 				setUsername(matcher.group(1));
 				setDomain(matcher.group(2));
				
 				if (matcher.groupCount() >= 3) {
 					setResource(matcher.group(3));
 				}
 			} else {
 				throw new Exception();
 			}
 		} else {
 			throw new Exception();
 		}
 	}
 	
 	/* Getter & setter */
 	
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		if(username != null)
 			this.username = username;
 		else 
 			this.username = "";
 	}
 
 	public String getDomain() {
 		return domain;
 	}
 
 	
 	public void setDomain(String domain) {
 		if(domain != null)
 			this.domain = domain;
 		else 
 			this.domain = "";
 	}
 
 	public String getResource() {
 		return this.resource;
 	}
 
 	public void setResource(String resource) {
 		if(resource != null)
 			this.resource = resource;
 		else 
 			this.resource = "";
 	}
 
 	@Override
 	public String toString() {
 		return "JabberID [username=" + username + ", domain=" + domain
 				+ ", ressources=" + resource + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
 		result = prime * result
 				+ ((resource == null) ? 0 : resource.hashCode());
 		result = prime * result
 				+ ((username == null) ? 0 : username.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		JabberID other = (JabberID) obj;
 		if (domain == null) {
 			if (other.domain != null)
 				return false;
 		} else if (!domain.equals(other.domain))
 			return false;
 		if (resource == null) {
 			if (other.resource != null)
 				return false;
 		} else if (!resource.equals(other.resource))
 			return false;
 		if (username == null) {
 			if (other.username != null)
 				return false;
 		} else if (!username.equals(other.username))
 			return false;
 		return true;
 	}
 	
 }
 
 /**
  * @endcond
  */
