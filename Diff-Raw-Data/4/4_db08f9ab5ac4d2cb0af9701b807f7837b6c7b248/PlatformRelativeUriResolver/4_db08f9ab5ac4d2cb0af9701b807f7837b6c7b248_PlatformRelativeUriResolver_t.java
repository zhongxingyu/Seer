 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.core.scoping;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.scoping.impl.ImportUriResolver;
 
 /**
  * This URI resolver tries to resolve against the <code>URI</code> of the object's resource.
  * If this is a platform resource <code>URI</code> then it determines an absolute path and resolves against that.
  * 
  * @author Henrik Rentz-Reichert
  *
  */
 public class PlatformRelativeUriResolver extends ImportUriResolver {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.xtext.scoping.impl.ImportUriResolver#resolve(org.eclipse.emf.ecore.EObject)
 	 */
 	@Override
 	public String resolve(EObject object) {
 		String resolve = super.resolve(object);
 		if (resolve!=null && object.eResource()!=null && object.eResource().getURI()!=null) {
 			URI uri = URI.createURI(resolve);
 			if (uri.isRelative()) {
 				URI base = object.eResource().getURI().trimSegments(1);
 				if (base.isPlatformResource()) {
 					IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(base.toPlatformString(true)));
 					// URI.resolve expects a trailing separator for some reason...
					String abs = folder.getRawLocationURI().toString();
 					base = URI.createURI(abs);
 				}
				base = base.appendSegment("");
 				uri = uri.resolve(base);
 				resolve = uri.toString();
 			}
 		}
 		return resolve;
 	}
 }
