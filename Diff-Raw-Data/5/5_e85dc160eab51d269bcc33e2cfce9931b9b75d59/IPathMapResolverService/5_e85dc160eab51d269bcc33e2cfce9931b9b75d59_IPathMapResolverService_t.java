 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.core.interfaces;
 
 import org.eclipse.tcf.services.IPathMap;
 import org.eclipse.tcf.te.runtime.services.interfaces.IService;
 
 /**
  * Path map resolver service.
  * <p>
  * Allows to map a host path to a target path and a target path
  * to a host path.
  */
 public interface IPathMapResolverService extends IService {
 
 	/**
 	 * Resolves the given file name by checking if the given path map rule is a match.
 	 * <p>
 	 * The path map rule matches, if the path map rule source attribute is
 	 * equal to the given file name or if the path map rule source attribute
 	 * is a prefix of the given file name.
 	 * <p>
 	 * If the path map rule is a match, the corresponding fraction of the
 	 * given file name is replaced with the path map rule destination attribute.
 	 * <p>
 	 * If the path map rule is not a match, <code>null</code> is returned.
 	 *
 	 * @param rule The path map rule. Must not be <code>null</code>.
 	 * @param fnm The file name. Must not be <code>null</code>.
 	 * @return The mapped path or <code>null</code>.
 	 */
 	public String map(IPathMap.PathMapRule rule, String fnm);
 
 	/**
 	 * Find a matching target path for the given host path.
 	 * <p>
 	 * Walks the configured (object) path map for the given context and search
 	 * for matching path map rules by trying to reverse map the given host path
 	 * with each path map rule.
 	 * <p>
 	 * If a path map rule matches, the search is stopped and the mapped target
 	 * path is returned.
 	 * <p>
 	 * <b>Note:</b> This method must be called from outside the TCF event dispatch thread.
 	 *
 	 * @param context The context. Must not be <code>null</code>.
 	 * @param hostPath The host path. Must not be <code>null</code>
	 * @return The mapped target path or <code>null</code>.
 	 */
 	public String findTargetPath(Object context, String hostPath);
 
 	/**
 	 * Find a matching host path for the given target path.
 	 * <p>
 	 * Walks the configured (object) path map for the given context and search
 	 * for matching path map rules by trying to map the given target path with
 	 * each path map rule.
 	 * <p>
 	 * If a path map rule matches, the host path is validated by checking if a file system
 	 * element exist and the element can be accessed for reading.
 	 * <p>
 	 * <b>Note:</b> This method must be called from outside the TCF event dispatch thread.
 	 *
 	 * @param context The context. Must not be <code>null</code>.
 	 * @param targetPath The target path. Must not be <code>null</code>
	 * @return The mapped host path or <code>null</code>.
 	 */
 	public String findHostPath(Object context, String targetPath);
 }
