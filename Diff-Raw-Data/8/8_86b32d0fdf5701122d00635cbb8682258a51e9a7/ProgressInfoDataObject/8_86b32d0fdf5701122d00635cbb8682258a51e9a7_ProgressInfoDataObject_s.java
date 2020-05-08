 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.uiprocess;
 
 /**
  * a data object for process state
  */
 public class ProgressInfoDataObject implements Comparable<ProgressInfoDataObject> {
 	private static final String EMPTY = "";//$NON-NLS-1$
 	private String processName = EMPTY;
 	private int value;
 	private int maxValue;
 	private int key;
 	private ProcessState processState;
 
 	/**
 	 * @param maxValue
 	 * @param processName
 	 * @param value
 	 */
 	public ProgressInfoDataObject(int key, int maxValue, int value, String processName, ProcessState pState) {
 		this.maxValue = maxValue;
 		this.processName = processName;
 		this.value = value;
 		this.key = key;
 		this.processState = pState;
 	}
 
 	/**
 	 * @return the processName
 	 */
 	public String getProcessName() {
 		return processName;
 	}
 
 	/**
 	 * @return the value
 	 */
 	public int getValue() {
 		return value;
 	}
 
 	/**
 	 * @return the maxValue
 	 */
 	public int getMaxValue() {
 		return maxValue;
 	}
 
 	/**
 	 * @return the processState
 	 */
 	public ProcessState getProcessState() {
 		return processState;
 	}
 
 	public Integer getKey() {
 		return Integer.valueOf(key);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
		if (obj == null || !ProgressInfoDataObject.class.isAssignableFrom(obj.getClass())) {
 			return false;
 		}
		ProgressInfoDataObject other = ProgressInfoDataObject.class.cast(obj);
 		return key == other.key;
 	}
 
 	@Override
 	public int hashCode() {
 		return key;
 	}
 
 	public int compareTo(ProgressInfoDataObject other) {
 		if (equals(other) && getProcessState().equals(other.getProcessState()) && getValue() == other.getValue()) {
 			return 0;
 		}//TODO
 		return -1;
 
 	}
 
 }
