 /**
  *   Context
  *   Copyright(c) 2011 Sergio Gabriel Teves
  * 
  *   This file is part of UrlResolver.
  *
  *   UrlResolver is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   UrlResolver is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with UrlResolver. If not, see <http://www.gnu.org/licenses/>.
  */
 package ar.sgt.resolver.listener;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author gabriel
  *
  */
 public interface Context {
 
	public static final String METHOD_POST = "post";
	public static final String METHOD_GET = "get";
 
 	public static final String WEB_APPLICATION_CONTEXT = "web-application-context";
 	
 	public HttpServletRequest getRequest();
 	
 	public HttpServletResponse getResponse();
 	
 }
