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
 
 import org.jamwiki.utils.Encryption;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.security.providers.encoding.PasswordEncoder;
 
 /**
  * Custom implementation of the JAMWiki password encoder.  This class is (hopefully)
  * a temporary one that can eventually be removed in favor of Spring Security's
  * default SHA password encoder.
  */
 public class JAMWikiPasswordEncoder implements PasswordEncoder {
 
 	/**
 	 * Encode a password as specified by the Spring Security PasswordEncoder interface.
 	 *
 	 * @param rawPass the password to encode
 	 * @param salt Ignored by JAMWiki.
 	 * @return encoded password
 	 */
 	public String encodePassword(String rawPass, Object salt) {
		if (StringUtils.isBlank(rawPass)) {
			throw new IllegalArgumentException("Password cannot be empty");
		}
 		return Encryption.encrypt(rawPass);
 	}
 
     /**
 	 * Validate a raw password against an encoded password as specified by the Spring
 	 * Security PasswordEncoder interface.
 	 *
 	 * @param encPass a pre-encoded password
 	 * @param rawPass a raw password to encode and compare against the pre-encoded password
 	 * @param salt Ignored by JAMWiki.
 	 * @return true if the password is valid , false otherwise
 	 */
 	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		if (StringUtils.isBlank(rawPass)) {
			return false;
		}
 		return StringUtils.equals(encPass, Encryption.encrypt(rawPass));
 	}
 }
