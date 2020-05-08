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
 
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UserDetailsService;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.jamwiki.WikiBase;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DataAccessResourceFailureException;
 
 /**
  * Loads user data from JAMWiki database.
  *
  * @author Rainer Schmitz
  * @version $Id: $
  * @since 28.11.2006
  */
 public class JAMWikiDaoImpl implements UserDetailsService {
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.acegisecurity.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
 	 */
 	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
 		UserDetails loadedUser;
 		try {
 			loadedUser = WikiBase.getDataHandler().lookupWikiUser(username, null);
 		} catch (Exception e) {
			throw new DataAccessResourceFailureException(e.getMessage(), e);
 		}
 		if (loadedUser == null) {
 			throw new UsernameNotFoundException("User with name '" + username + "' not found in JAMWiki database.");
 		}
 		return loadedUser;
 	}
 
 }
