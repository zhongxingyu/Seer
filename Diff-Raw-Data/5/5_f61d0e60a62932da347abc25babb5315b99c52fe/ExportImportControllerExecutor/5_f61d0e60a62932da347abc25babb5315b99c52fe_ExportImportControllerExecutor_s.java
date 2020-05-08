 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model.importexport;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 
 /**
  * Generic export/import controller whose main responsibility it is to actually
  * execute an {@link IExportImportController}.
  * 
  * @author emueller
  */
 public class ExportImportControllerExecutor {
 
 	private File file;
 	private IProgressMonitor monitor;
 	private IOException importExportError;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param file
 	 *            the file that should be exported/imported
 	 * @param monitor
 	 *            a ProgressMonitor to inform about the progress of the export/import process
 	 */
 	public ExportImportControllerExecutor(File file, IProgressMonitor monitor) {
 		this.file = file;
 		this.monitor = monitor;
 	}
 
 	/**
 	 * Executes the given {@link IExportImportController}.
 	 * 
 	 * @param controller
 	 *            the controller to be executed
 	 * @throws IOException
 	 *             in case an error occurs during export/import
 	 */
 	public void execute(final IExportImportController controller) throws IOException {
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
				monitor.beginTask(controller.getLabel(), 100);
 				// TODO: let export controllers set worked state
 				monitor.worked(10);
 				try {
 					controller.execute(file, monitor);
 				} catch (IOException e) {
 					importExportError = e;
 				}
 				// / TODO
 				monitor.worked(30);
 				monitor.worked(60);
 				monitor.done();
 			}
 		}.run(false);
 
 		if (importExportError != null) {
 			throw importExportError;
 		}
 	}
 }
