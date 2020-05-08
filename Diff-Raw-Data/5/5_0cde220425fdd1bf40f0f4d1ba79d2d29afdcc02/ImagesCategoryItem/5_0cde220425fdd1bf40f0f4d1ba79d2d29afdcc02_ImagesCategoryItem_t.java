 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.deltacloud.ui.views.cloud;
 
 import java.beans.PropertyChangeEvent;
 import java.text.MessageFormat;
import java.util.Collection;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudImage;
 import org.jboss.tools.deltacloud.core.IImageFilter;
 import org.jboss.tools.deltacloud.core.job.AbstractCloudElementJob;
 import org.jboss.tools.deltacloud.core.job.AbstractCloudElementJob.CLOUDELEMENT;
 import org.jboss.tools.deltacloud.ui.ErrorUtils;
 import org.jboss.tools.deltacloud.ui.views.CVMessages;
 
 /**
  * @author Jeff Johnston
  * @author Andre Dietisheim
  */
 public class ImagesCategoryItem extends CloudElementCategoryItem<DeltaCloudImage> {
 
 	private static final String IMAGE_CATEGORY_NAME = "ImageCategoryName"; //$NON-NLS-1$
 
 	protected ImagesCategoryItem(DeltaCloud model, DeltaCloudViewItem<?> parent, TreeViewer viewer) {
 		super(model, parent, viewer);
 	}
 
 	public String getName() {
 		return CVMessages.getString(IMAGE_CATEGORY_NAME);
 	}
 
 	@Override
 	protected void asyncLoadCloudElements() {
 		setLoadingIndicator();
 		new AbstractCloudElementJob(
 				MessageFormat.format("Get images from cloud {0}", getModel().getName()), getModel(), CLOUDELEMENT.IMAGES) {
 
 			@Override
 			protected IStatus doRun(IProgressMonitor monitor) throws DeltaCloudException {
 				try {
 					DeltaCloudImage[] images = getCloud().getImages();
 					replaceCloudElements(getModel(), images);
 					return Status.OK_STATUS;
 				} catch(DeltaCloudException e) {
 					clearChildren();
 					throw e;
 				}
 			}
 		}.schedule();
 	}
 
 	@Override
 	protected DeltaCloudViewItem<?>[] getElements(DeltaCloudImage[] modelElements, int startIndex, int stopIndex) {
 		DeltaCloudViewItem<?>[] elements = new DeltaCloudViewItem[stopIndex - startIndex];
 		for (int i = startIndex; i < stopIndex; ++i) {
 			elements[i - startIndex] = new ImageItem(modelElements[i], this, getViewer());
 		}
 		return elements;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		DeltaCloud cloud = (DeltaCloud) event.getSource();
 		DeltaCloudImage[] newImages = (DeltaCloudImage[]) event.getNewValue();
 		try {
 			replaceCloudElements(cloud, newImages);
 		} catch (DeltaCloudException e) {
 			// TODO: internationalize strings
 			ErrorUtils.handleError(
 					"Error",
 					MessageFormat.format("Could not display new images from cloud \"{0}\"", cloud.getName()), e,
 					getViewer().getControl().getShell());
 		}
 	}
 
 	@Override
 	protected DeltaCloudImage[] filter(DeltaCloudImage[] images) throws DeltaCloudException {
 		DeltaCloud cloud = getModel();
 		IImageFilter f = cloud.getImageFilter();
		Collection<DeltaCloudImage> filteredImages = f.filter(images);
		return filteredImages.toArray(new DeltaCloudImage[filteredImages.size()]);
 	}
 
 	@Override
 	protected void addPropertyChangeListener(DeltaCloud cloud) {
 		cloud.addPropertyChangeListener(DeltaCloud.PROP_IMAGES, this);
 	}
 }
