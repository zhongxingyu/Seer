/*******************************************************************************
 * Copyright (c) 2013 WPI-Suite
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 ******************************************************************************/

 package edu.wpi.cs.wpisuitetng.exceptions;
 
 import javax.servlet.http.HttpServletResponse;
 
 public class NotFoundException extends WPISuiteException {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2573827722743846840L;
 
 	public NotFoundException(String message) {
 		super(message);
 	}
 	
 	public NotFoundException() {
 	}
 
 	@Override
 	public int getStatus() {
 		return HttpServletResponse.SC_NOT_FOUND; //404
 	}
 
 }
