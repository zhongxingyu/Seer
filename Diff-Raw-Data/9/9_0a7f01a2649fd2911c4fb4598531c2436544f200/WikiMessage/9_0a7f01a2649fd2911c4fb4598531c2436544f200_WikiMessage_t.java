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
 package org.jamwiki;
 
 import java.util.List;
 import org.jamwiki.utils.WikiLogger;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * This class is a utility class useful for storing messages key and object
  * values that can later be displayed using the jstl fmt:message tag.
  */
 public class WikiMessage {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiMessage.class.getName());
 	private final String key;
 	private String[] params = null;
 
 	/**
 	 * Create a new message that is mapped to the specified ApplicationResources
 	 * key value.
 	 *
 	 * @param key The ApplicationResources key that corresponds to the message
 	 *  to display.
 	 */
 	public WikiMessage(String key) {
 		this.key = key;
 	}
 
 	/**
 	 * Create a new message that is mapped to the specified ApplicationResources
 	 * key value using a single parameter.
 	 *
 	 * @param key The ApplicationResources key that corresponds to the message
 	 *  to display.
 	 * @param param1 The parameter that corresponds to the {0} param in the
 	 *  specified message key value.  Note that this parameter is automatically
 	 *  HTML escaped to prevent erorrs in display.
 	 */
 	public WikiMessage(String key, String param1) {
 		this.key = key;
 		this.params = new String[1];
 		params[0] = this.escapeHtml(param1);
 	}
 
 	/**
 	 * Create a new message that is mapped to the specified ApplicationResources
 	 * key value using two parameters.
 	 *
 	 * @param key The ApplicationResources key that corresponds to the message
 	 *  to display.
 	 * @param param1 The parameter that corresponds to the {0} param in the
 	 *  specified message key value.  Note that this parameter is automatically
 	 *  HTML escaped to prevent erorrs in display.
 	 * @param param2 The parameter that corresponds to the {1} param in the
 	 *  specified message key value.  Note that this parameter is automatically
 	 *  HTML escaped to prevent erorrs in display.
 	 */
 	public WikiMessage(String key, String param1, String param2) {
 		this.key = key;
 		this.params = new String[2];
 		params[0] = this.escapeHtml(param1);
 		params[1] = this.escapeHtml(param2);
 	}
 
 	/**
 	 * Create a new message that is mapped to the specified ApplicationResources
 	 * key value using an array of parameters.
 	 *
 	 * @param key The ApplicationResources key that corresponds to the message
 	 *  to display.
 	 * @param params An array of parameters that correspond to the {0}, {1}, etc
 	 *  params in the specified message key value.  Note that these parameters are
 	 *  automatically HTML escaped to prevent erorrs in display.
 	 */
 	public WikiMessage(String key, String[] params) {
 		this.key = key;
 		if (params != null) {
 			this.params = new String[params.length];
 			for (int i = 0; i < params.length; i++) {
 				this.params[i] = this.escapeHtml(params[i]);
 			}
 		}
 	}
 
 	/**
 	 * Create a new message that is mapped to the specified ApplicationResources
 	 * key value using an list of parameters.
 	 *
 	 * @param key The ApplicationResources key that corresponds to the message
 	 *  to display.
 	 * @param params An list of parameters that correspond to the {0}, {1}, etc
 	 *  params in the specified message key value.  Note that these parameters are
 	 *  automatically HTML escaped to prevent erorrs in display.
 	 */
 	public WikiMessage(String key, List<String> paramList) {
 		this.key = key;
 		if (paramList != null && !paramList.isEmpty()) {
 			this.params = new String[paramList.size()];
 			int i = 0;
 			for (String param : paramList) {
 				this.params[i++] = this.escapeHtml(param);
 			}
 		}
 	}
 
 	/**
 	 * Return the ApplicationResources message key associated with this message.
 	 *
 	 * @return The ApplicationResources message key associated with this message.
 	 */
 	public String getKey() {
 		return this.key;
 	}
 
 	/**
 	 * Return the array of parameter objects associated with this message.
 	 *
 	 * @return The array of parameter objects associated with this message.
 	 */
 	public String[] getParams() {
 		return this.params;
 	}
 
 	/**
	 * Return the number of params assigned to this WikiMessage.
	 *
	 * @return The number of params assigned to this WikiMessage.
	 */
	public int getParamsLength() {
		return ((this.params == null) ? 0 : this.params.length);
	}

	/**
 	 * This set method allows message parameters to be set without being escaped.
 	 * Note that this can be a gaping security hole as it opens the site up to
 	 * cross-site scripting attacks.  USE THIS METHOD ONLY IF YOU KNOW WHAT YOU ARE
 	 * DOING!
 	 *
 	 * @param params The array of parameter objects to associate with this message.
 	 */
 	public void setParamsWithoutEscaping(String[] params) {
 		this.params = params;
 	}
 	
 	/**
 	 * Escape HTML.  StringEscapeUtils.escapeHtml should be used for this functionality,
 	 * but the current version escapes unicode characters as well as HTML entities
 	 * which breaks some wiki functionality.
 	 */
 	private String escapeHtml(String param) {
 		// this could be optimized should performance become an issue
 		param = StringUtils.replace(param, "&", "&amp;");
 		param = StringUtils.replace(param, "<", "&lt;");
 		param = StringUtils.replace(param, ">", "&gt;");
 		param = StringUtils.replace(param, "\"", "&quot;");
 		return param;
 	}
 }
