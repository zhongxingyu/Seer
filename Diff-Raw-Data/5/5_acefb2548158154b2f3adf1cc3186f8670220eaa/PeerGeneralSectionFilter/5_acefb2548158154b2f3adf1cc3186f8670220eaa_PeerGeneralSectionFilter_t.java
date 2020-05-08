 /**
  * PeerGeneralSectionFilter.java
  * Created on Sep 6, 2012
  *
  * Copyright (c) 2012 Wind River Systems, Inc.
  *
  * The right to copy, distribute, modify, or otherwise make use
  * of this software may be licensed only pursuant to the terms
  * of an applicable Wind River license agreement.
  */
 package org.eclipse.tcf.te.tcf.ui.internal.tabbed;
 
 import org.eclipse.jface.viewers.IFilter;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 
 /**
  * Peer model node general section filter implementation.
  */
 public class PeerGeneralSectionFilter implements IFilter {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
 	 */
 	@Override
 	public boolean select(Object element) {
		return element instanceof IPeerModel;
 	}
 
 }
