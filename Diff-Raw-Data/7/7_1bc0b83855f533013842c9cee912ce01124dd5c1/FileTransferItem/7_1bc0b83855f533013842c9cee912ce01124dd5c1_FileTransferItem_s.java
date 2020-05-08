 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.runtime.services.filetransfer;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
 
 /**
  * FileTransferItem
  */
 public class FileTransferItem extends PropertiesContainer implements IFileTransferItem {
 
 	/**
 	 * Constructor.
 	 */
 	public FileTransferItem() {
 		setProperty(PROPERTY_ENABLED, true);
 		setProperty(PROPERTY_DIRECTION, HOST_TO_TARGET);
 	}
 
 	public FileTransferItem(IPath fromHost, boolean enabled) {
 		this();
 		if (fromHost != null)
 			setProperty(PROPERTY_HOST, fromHost.toPortableString());
 		setProperty(PROPERTY_ENABLED, enabled);
 	}
 
 	public FileTransferItem(IPath fromHost, IPath toTarget) {
 		this();
 		if (fromHost != null)
 			setProperty(PROPERTY_HOST, fromHost.toPortableString());
 		if (toTarget != null)
 			setProperty(PROPERTY_TARGET, toTarget.toPortableString());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IFileTransferItem#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		return getBooleanProperty(PROPERTY_ENABLED);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IFileTransferItem#getHostPath()
 	 */
 	@Override
 	public IPath getHostPath() {
 		return getStringProperty(PROPERTY_HOST) != null ? new Path(getStringProperty(PROPERTY_HOST)) : null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IFileTransferItem#getTargetPath()
 	 */
 	@Override
 	public IPath getTargetPath() {
 		return getStringProperty(PROPERTY_TARGET) != null ? new Path(getStringProperty(PROPERTY_TARGET)) : null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IFileTransferItem#getDirection()
 	 */
 	@Override
 	public int getDirection() {
 		int direction = getIntProperty(PROPERTY_DIRECTION);
 		return direction == TARGET_TO_HOST ? TARGET_TO_HOST : HOST_TO_TARGET;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IFileTransferItem#getOptions()
 	 */
 	@Override
 	public String getOptions() {
 		return getStringProperty(PROPERTY_OPTIONS);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		int hc = getHostPath() != null ? getHostPath().hashCode() : 0;
 		hc = hc << 8 + (getTargetPath() != null ? getTargetPath().hashCode() : 0);
 		hc = hc << 8 + getDirection();
 	    return hc;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		boolean equals = super.equals(obj);
 		if (!equals && obj instanceof IFileTransferItem) {
			return getHostPath() != null ? getHostPath().equals(((IFileTransferItem)obj).getHostPath()) : ((IFileTransferItem)obj).getHostPath() == null
						&& getTargetPath() != null ? getTargetPath().equals(((IFileTransferItem)obj).getTargetPath()) : ((IFileTransferItem)obj).getTargetPath() == null
						&& getDirection() == ((IFileTransferItem)obj).getDirection();
 		}
 		return equals;
 	}
 }
