 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.authentication;
 
 import java.util.Vector;
 import org.acegisecurity.providers.anonymous.AnonymousProcessingFilter;
 import org.acegisecurity.userdetails.memory.UserAttribute;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.WikiGroup;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class allows anonymous users to be provided default roles from the
  * JAMWiki database.
  */
 public class JAMWikiAnonymousProcessingFilter extends AnonymousProcessingFilter {
 
 	/** Standard logger. */
 	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAnonymousProcessingFilter.class.getName());
 
 	/**
 	 * Set roles for non-logged-in user.
 	 */
 	public void afterPropertiesSet() throws Exception {
 		super.afterPropertiesSet();
 		UserAttribute user = this.getUserAttribute();
 		if (user == null) {
 			logger.warning("No user attribute available in JAMWikiAnonymousProcessingFilter.  Please verify the Acegi configuration settings.");
 		}
		// FIXE - by default user is given ROLE_ANONYMOUS.  that gets used in some
		// JSP pages.  needs to be cleaned up.
 		Role[] groupRoles = new Role[0];
 		try {
 			groupRoles = WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_ANONYMOUS);
 		} catch (Exception e) {
 			// FIXME - without default roles bad things happen, so should this throw the
 			// error to the calling method?
 			logger.severe("Unable to retrieve default roles for " + WikiGroup.GROUP_ANONYMOUS, e);
 		}
 		for (int i=0; i < groupRoles.length; i++) {
 			user.addAuthority(groupRoles[i]);
 		}
 	}
 }
