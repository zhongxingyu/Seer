 /*
  * Copyright 2011 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework.
  *
  *   The vaadin-framework Infrastructure is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.vaadinframework.fragment;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.vaadinframework.annotation.EmbeddedComponent;
 import pt.ist.vaadinframework.annotation.EmbeddedComponentUtils;
 import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
 
 /**
  * @author Sérgio Silva (sergio.silva@ist.utl.pt)
  * 
  */
 public class FragmentQuery implements Serializable {
     String path;
 
     Map<String, String> params;
 
     private void setParams(String query) {
 	final String[] args = query.split("&");
 	for (String param : args) {
 	    final String[] split = param.split("=");
 	    if (split.length == 2) {
 		String name = split[0];
 		String value = split[1];
 		put(name, value);
 	    } else {
 		throw new InvalidFragmentException(query);
 	    }
 	}
     }
 
     private void setPath(final String path) {
 	this.path = path;
     }
 
     public FragmentQuery() {
 	// params = new HashMap<String, String>();
     }
 
     private void put(String key, String value) {
 	if (params == null) {
 	    params = new HashMap<String, String>();
 	}
 	params.put(key, value);
     }
 
     public FragmentQuery(Class<? extends EmbeddedComponentContainer> clazz, String... values) {
 	this();
 	final EmbeddedComponent annotation = EmbeddedComponentUtils.getAnnotation(clazz);
 	final String[] path = annotation.path();
 	final String[] args = annotation.args();
 
 	if ((args.length < values.length) || path.length != 1) {
 	    throw new InvalidFragmentException("args don't match");
 	}
 
 	this.path = path[0];
 
 	for (int i = 0; i < values.length; i++) {
	    put(args[i], values[i]);
 	}
     }
 
     public FragmentQuery(String fragment) {
 	this();
 	if (fragment == null || fragment.isEmpty() || !fragment.startsWith("#")) {
 	    path = null;
 	    params = null;
 	    throw new InvalidFragmentException();
 	}
 
 	if (!fragment.contains("?")) {
 	    path = fragment.substring(1);
 	    params = null;
 	    return;
 	}
 
 	final Pattern compile = Pattern.compile("^\\#(.*)\\?(.*)?$");
 	final Matcher matcher = compile.matcher(fragment);
 	if (matcher.matches() && matcher.groupCount() == 2) {
 	    setPath(matcher.group(1));
 	    setParams(matcher.group(2));
 	}
     }
 
     public String getPath() {
 	return path;
     }
 
     public Map<String, String> getParams() {
 	return params;
     }
 
     public String getQueryString() {
 	String queryString = String.format("%s", path);
 	final ArrayList<String> args = new ArrayList<String>();
 	if (params != null) {
 	    for (Entry<String, String> entry : params.entrySet()) {
 		args.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
 	    }
 	}
 	return args.isEmpty() ? queryString : queryString + "?" + StringUtils.join(args, "&");
     }
 }
