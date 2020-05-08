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
 package org.eclipse.riena.communication.core.attachment;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * internal class used for serializing attachments. this class is only used for
  * hessian webservice types.
  * 
  * @author Christian Campo
  */
 public class ByteArrayDataSource implements IDataSource {
 
 	private byte[] internalBuffer;
 	private String name;
 	private InputStream input;
 
 	/**
 	 * Default constructor. <br>
 	 * called by deserialization
 	 */
 	public ByteArrayDataSource() {
 		super();
 	}
 
 	/**
 	 * copies the source into an byte array
 	 * 
 	 * @param source
 	 */
 	public ByteArrayDataSource(IDataSource source) {
 		super();
 		if (source == null) {
 			internalBuffer = new byte[0];
 		} else {
 			name = source.getName();
 			try {
 				input = source.getInputStream();
 			} catch (IOException e) {
 				throw new RuntimeException("IOException when transporting attachment ", e); //$NON-NLS-1$
 			}
 		}
 	}
 
 	/**
 	 * @see javax.IDataSource.DataSource#getInputStream()
 	 */
 	public InputStream getInputStream() throws IOException {
 		// return new ByteArrayInputStream( internalBuffer );
 		if (input == null) {
 			return null;
 		}
		if (input.markSupported()) {
			input.reset();
		}
 		return input;
 	}
 
 	/**
 	 * @see javax.IDataSource.DataSource#getName()
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @see javax.IDataSource.DataSource#getOutputStream()
 	 */
 	public OutputStream getOutputStream() throws IOException {
 		throw new IOException("not supported"); //$NON-NLS-1$
 	}
 
 	public void checkValid() {
 		return;
 	}
 }
