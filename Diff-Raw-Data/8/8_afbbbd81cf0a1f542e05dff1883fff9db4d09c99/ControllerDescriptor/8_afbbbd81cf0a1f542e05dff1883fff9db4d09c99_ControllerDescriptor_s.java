 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.controller;
 
 /**
  * Describes a Controller. It contains information about the controller url/httpMethod, the
  * controller class, this controller name.
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class ControllerDescriptor {
 
 	private String controllerClass;
 	private String controllerName;
 	private String httpMethod;
 	private String url;
 
 	public ControllerDescriptor(String url, String method, String controllerName, String controllerClass) {
 		this.url = url.toLowerCase();
 		this.httpMethod = method.toLowerCase();
 		this.controllerName = (controllerName.length() > 2) ? controllerName.substring(0, 1).toUpperCase() + controllerName.substring(1)
 				: controllerName.toUpperCase();
 		this.controllerClass = controllerClass;
 	}
 
 	/**
 	 * @return the controller's actionId
 	 */
 	public String getControllerName() {
 		return controllerName;
 	}
 
 	/**
 	 * @return the controller's class
 	 */
 	public String getControllerClass() {
 		return this.controllerClass;
 	}
 
 	/**
 	 * @return the controller's httpMethod
 	 */
 	public String getHttpMethod() {
 		return httpMethod;
 	}
 
 	/**
 	 * @return the controller's url
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String.format("class: %s; name: %s; url: %s; http method: %s",
 				this.controllerClass, this.controllerName, this.url, this.httpMethod);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
		return this.url.hashCode() & this.httpMethod.hashCode() & this.controllerClass.hashCode() & this.controllerName.hashCode();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof ControllerDescriptor) {
 			ControllerDescriptor other = (ControllerDescriptor) obj;
 			return this.url.equals(other.url) && this.httpMethod.equals(other.httpMethod) && this.controllerClass.equals(other.controllerClass)
 					&& this.controllerName.equals(other.controllerName);
 		} else {
 			return false;
 		}
 	}
 }
