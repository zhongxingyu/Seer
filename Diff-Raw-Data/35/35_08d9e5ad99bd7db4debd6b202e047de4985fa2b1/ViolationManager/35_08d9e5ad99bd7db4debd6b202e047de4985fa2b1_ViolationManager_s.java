 /*******************************************************************************
  * Copyright (c) 2009, 2010 Dejan Spasic
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpmd.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import org.eclipse.ui.XMLMemento;
 import org.phpsrc.eclipse.pti.tools.phpmd.PhpmdLog;
 import org.phpsrc.eclipse.pti.tools.phpmd.PhpmdPlugin;
 
 public class ViolationManager {
 	private static ViolationManager manager;
 	private Collection<IViolation> violations;
 
 	private List<IViolationManagerListener> listeners = new ArrayList<IViolationManagerListener>();
 
 	private ViolationManager() {
 		violations = new HashSet<IViolation>();
 	}
 
 	public static ViolationManager getManager() {
 		if (null == manager) {
 			manager = new ViolationManager();
 		}
 		return manager;
 	}
 
 	public IViolation[] getViolations() {
 		if (null == violations) {
 			loadViolations();
 		}
 		return violations.toArray(new IViolation[violations.size()]);
 	}
 
 	private void loadViolations() {
 		violations = new HashSet<IViolation>(20);
 		FileReader reader = null;
 		try {
 			reader = new FileReader(getViolationFile());
			loadViolations(XMLMemento.createReadRoot(reader));
 		} catch (FileNotFoundException e) {
 			// no violation exists yet
 		} catch (Exception e) {
 			PhpmdLog.logError(e);
 		} finally {
 			try {
 				if (null != reader)
 					reader.close();
 			} catch (IOException e) {
 				PhpmdLog.logError(e);
 			}
 		}
 	}
 
 	private File getViolationFile() {
 		return PhpmdPlugin.getDefault().getStateLocation().append("phpmd_violations.xml").toFile();
 	}
 
	private void loadViolations(XMLMemento memento) {
		// IMemento[] children = memento.getChildren(type);
	}

 	public void saveViolations() {
 	}
 
 	private void fireViolationsChanged(IViolation[] violationsAdded, IViolation[] violationsRemoved) {
 		ViolationManagerEvent event = new ViolationManagerEvent(this, violationsAdded, violationsRemoved);
 
		for (IViolationManagerListener listener : listeners) {
 			listener.violationsChanged(event);
		}
 	}
 
 	public void addViolation(IViolation newViolation) {
 		if (null == newViolation)
 			return;
 		addViolation(new IViolation[] { newViolation });
 	}
 
 	public void addViolation(IViolation[] newViolations) {
 		if (null == newViolations)
 			return;
 
 		Collection<IViolation> items = new HashSet<IViolation>(newViolations.length);
 
 		for (IViolation currentViolation : newViolations) {
 			if (null != currentViolation && violations.add(currentViolation)) {
 				items.add(currentViolation);
 			}
 		}
 
 		if (0 < items.size()) {
 			IViolation[] added = items.toArray(new IViolation[items.size()]);
 			fireViolationsChanged(added, IViolation.NONE);
 		}
 	}
 
 	public void removeViolation(IViolation oldViolation) {
 		if (null == oldViolation)
 			return;
 		removeViolation(new IViolation[] { oldViolation });
 	}
 
 	public void removeViolation(IViolation[] oldViolations) {
 		if (null == oldViolations)
 			return;
 
 		Collection<IViolation> items = new HashSet<IViolation>(oldViolations.length);
 
 		for (IViolation currentViolation : oldViolations) {
 			if (null != currentViolation && violations.remove(currentViolation)) {
 				items.add(currentViolation);
 			}
 		}
 
 		if (0 < items.size()) {
 			IViolation[] removed = items.toArray(new IViolation[items.size()]);
 			fireViolationsChanged(IViolation.NONE, removed);
 		}
 	}
 
 	public void removeAllViolations() {
 		if (0 < violations.size()) {
 			IViolation[] removed = violations.toArray(new IViolation[violations.size()]);
 			violations.clear();
 			fireViolationsChanged(IViolation.NONE, removed);
 		}
 	}
 
 	public void addViolationManagerListener(IViolationManagerListener listener) {
 		if (!listeners.contains(listener))
 			listeners.add(listener);
 	}
 
 	public void removeViolationManagerListener(IViolationManagerListener listener) {
 		listeners.remove(listener);
 	}
 }
