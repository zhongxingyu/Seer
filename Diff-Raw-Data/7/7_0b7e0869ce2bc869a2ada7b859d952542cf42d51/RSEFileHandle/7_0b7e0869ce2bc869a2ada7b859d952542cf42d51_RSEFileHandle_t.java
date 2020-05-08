 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Andrei Sobolev)
  *******************************************************************************/
 package org.eclipse.dltk.core.internal.rse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.internal.environment.EFSFileHandle;
 import org.eclipse.dltk.core.internal.rse.perfomance.RSEPerfomanceStatistics;
 import org.eclipse.rse.internal.efs.RSEFileSystem;
 
 public class RSEFileHandle extends EFSFileHandle {
 
 	private final class CountStream extends InputStream {
 		private InputStream stream;
 
 		public CountStream(InputStream stream) {
 			this.stream = stream;
 		}
 
 		public int read() throws IOException {
 			int read = stream.read();
 			if (read != -1) {
 				RSEPerfomanceStatistics
 						.inc(RSEPerfomanceStatistics.TOTAL_BYTES_RECEIVED);
 			}
 			return read;
 		}
 
 		public int available() throws IOException {
 			return this.stream.available();
 		}
 
 		public void close() throws IOException {
 			this.stream.close();
 		}
 
 		public synchronized void mark(int readlimit) {
 			this.stream.mark(readlimit);
 		}
 
 		public boolean markSupported() {
 			return this.stream.markSupported();
 		}
 
 		public int read(byte[] b, int off, int len) throws IOException {
 			return this.stream.read(b, off, len);
 		}
 
 		public int read(byte[] b) throws IOException {
 			return this.stream.read(b);
 		}
 
 		public synchronized void reset() throws IOException {
 			this.stream.reset();
 		}
 
 		public long skip(long n) throws IOException {
 			return this.stream.skip(n);
 		}
 	}
 
 	public RSEFileHandle(IEnvironment env, URI locationURI) {
 		super(env, RSEFileSystem.getInstance().getStore(locationURI));
 	}
 
 	public InputStream openInputStream(IProgressMonitor monitor)
 			throws IOException {
 		if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 			return new CountStream(super.openInputStream(monitor));
 		}
 		return super.openInputStream(monitor);
 	}
 }
