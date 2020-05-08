 /**************************************************************************
  * Copyright (c) 2012 Anya Helene Bagge
  * Copyright (c) 2012 University of Bergen
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version. See http://www.gnu.org/licenses/
  * 
  * 
  * See the file COPYRIGHT for more information.
  * 
  * Contributors:
  * * Anya Helene Bagge
  * 
  *************************************************************************/
 package org.nuthatchery.pica.resources.internal.facts;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.eclipse.jdt.annotation.Nullable;
 import org.nuthatchery.pica.resources.ISerializer;
 import org.nuthatchery.pica.resources.storage.IStorage;
 import org.nuthatchery.pica.resources.storage.IStoreUnit;
 import org.nuthatchery.pica.resources.storage.StoreUnit;
 import org.nuthatchery.pica.util.ISignature;
 
 import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
 
 public class GenericFact<T> extends Fact<T> {
 
 	private final ISerializer<T> io;
 
 
 	public GenericFact(String name, ISerializer<T> io) {
 		super(name);
 		this.io = io;
 	}
 
 
 	public GenericFact(String name, IStorage storage, ISerializer<T> io) {
 		super(name, storage);
 		this.io = io;
 	}
 
 
 	@Override
 	@Nullable
 	protected IStoreUnit<T> loadHelper(IStorage s) throws IOException {
 		return s.get(factName, new GenericStoreUnit<T>(io));
 	}
 
 
 	@Override
 	protected void saveHelper(T val, IStorage s) {
 		GenericStoreUnit<T> unit = new GenericStoreUnit<T>(val, signature, io);
 		s.put(factName, unit);
 	}
 
 
 	static class GenericStoreUnit<T> extends StoreUnit<T> {
 		private final ISerializer<T> io;
 		@Nullable
 		private T val;
 
 
 		public GenericStoreUnit(ISerializer<T> io) {
 			this(null, null, io);
 		}
 
 
 		public GenericStoreUnit(@Nullable T val, @Nullable ISignature signature, ISerializer<T> io) {
 			super(signature);
 			this.val = val;
 			this.io = io;
 		}
 
 
 		@Override
 		@Nullable
 		@SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
 		public byte[] getData() {
 			T v = val;
			if(v != null) {
 				ByteArrayOutputStream stream = new ByteArrayOutputStream();
 				try {
 					io.write(v, stream);
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 				}
 				return stream.toByteArray();
 			}
 			else {
 				return null;
 			}
 		}
 
 
 		@Override
 		@Nullable
 		public T getValue() {
 			return val;
 		}
 
 
 		@Override
 		public void setData(byte[] data) {
 			try {
				val = io.read(new ByteArrayInputStream(data));
 			}
 			catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
