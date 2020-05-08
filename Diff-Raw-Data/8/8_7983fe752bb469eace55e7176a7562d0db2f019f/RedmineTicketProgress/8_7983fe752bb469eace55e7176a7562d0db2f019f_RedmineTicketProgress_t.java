 /*******************************************************************************
  * Copyright (c) 2004, 2007 Mylyn project committers and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mylyn project committers
  *******************************************************************************/
 /*******************************************************************************
  * Copyright (c) 2008 Sven Krzyzak
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Sven Krzyzak - adapted Trac implementation for Redmine
  *******************************************************************************/
 package org.svenk.redmine.core.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class RedmineTicketProgress extends RedmineTicketAttribute {
 
 	private static List<RedmineTicketProgress> availableValues;
 	
 	private RedmineTicketProgress(int value) {
 		super(value + " %", value);
 	}
 
 	private static final long serialVersionUID = 1L;
 
 	public static List<RedmineTicketProgress> availableValues() {
 		if (availableValues==null) {
 			availableValues = new ArrayList<RedmineTicketProgress>(10);
 			for(int i=0; i<=10; i++) {
 				availableValues.add(new RedmineTicketProgress(i*10));
 			}
 			availableValues = Collections.unmodifiableList(availableValues);
 		}
 		return availableValues;
 	}
 	
 	public static String getDefaultValue() {
		return availableValues().get(0).getValue() + "";
 	}
 	
 }
