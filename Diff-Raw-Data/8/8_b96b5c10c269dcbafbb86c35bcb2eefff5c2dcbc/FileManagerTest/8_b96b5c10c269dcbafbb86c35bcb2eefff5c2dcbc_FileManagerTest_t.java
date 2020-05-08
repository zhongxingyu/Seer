 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * mkoegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.eclipse.emf.emfstore.client.test.common.dsl.Add;
 import org.eclipse.emf.emfstore.client.test.common.dsl.Create;
 import org.eclipse.emf.emfstore.client.test.common.util.ProjectUtil;
 import org.eclipse.emf.emfstore.internal.client.model.filetransfer.FileDownloadStatus;
 import org.eclipse.emf.emfstore.internal.client.model.filetransfer.FileDownloadStatus.Status;
 import org.eclipse.emf.emfstore.internal.server.model.FileIdentifier;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
import org.junit.After;
 import org.junit.Test;
 
 /**
  * Tests the file manager.
  * 
  * @author mkoegel
  * 
  */
 public class FileManagerTest extends TransmissionTests {
 
 	private File file;
 
	@After
 	@Override
 	public void after() {
 		if (file != null) {
 			FileUtils.deleteQuietly(file);
 			file = null;
 		}
 		super.after();
 	}
 
 	@Test
 	public void testTransfer() throws ESException, IOException, InterruptedException {
 		file = File.createTempFile("foo", "tmp"); //$NON-NLS-1$//$NON-NLS-2$
 		file.deleteOnExit();
 		final FileIdentifier id = getProjectSpace1().addFile(file);
 		// dummy change, addFile is not recognized as a change
 		Add.toProject(getProjectSpace1().toAPI(), Create.testElement());
 		ProjectUtil.commit(getProjectSpace1().toAPI());
 
 		ProjectUtil.update(getProjectSpace2().toAPI());
 		final FileDownloadStatus status = getProjectSpace2().getFile(id);
 		assertTrue(status != null);
 		// wait for file to be completely transferred
 		while (status.getStatus() != Status.FINISHED) {
 			Thread.sleep(500);
 		}
 		final FileInputStream fileInputStream = new FileInputStream(file);
 		final FileInputStream fileInputStream2 = new FileInputStream(status.getTransferredFile());
 		try {
 			assertEquals(IOUtils.toByteArray(fileInputStream), IOUtils.toByteArray(fileInputStream2));
 		} finally {
 			fileInputStream.close();
 			fileInputStream2.close();
 		}
 	}
 
 	@Test
 	public void testTransferWithBlocking() throws ESException, IOException, InterruptedException {
 		file = File.createTempFile("foo", "tmp"); //$NON-NLS-1$//$NON-NLS-2$
 		file.deleteOnExit();
 		final FileIdentifier id = getProjectSpace1().addFile(file);
 		// dummy change, addFile is not recognized as a change
 		Add.toProject(getProjectSpace1().toAPI(), Create.testElement());
 		ProjectUtil.commit(getProjectSpace1().toAPI());
 
 		ProjectUtil.update(getProjectSpace1().toAPI());
 
 		final FileDownloadStatus status = getProjectSpace2().getFile(id);
 		assertTrue(status != null);
 		status.getTransferredFile(true);
 		final FileInputStream fileInputStream = new FileInputStream(file);
 		final FileInputStream fileInputStream2 = new FileInputStream(status.getTransferredFile(true));
 		try {
 			assertEquals(IOUtils.toByteArray(fileInputStream), IOUtils.toByteArray(fileInputStream2));
 		} finally {
 			fileInputStream.close();
 			fileInputStream2.close();
 		}
 	}
 }
