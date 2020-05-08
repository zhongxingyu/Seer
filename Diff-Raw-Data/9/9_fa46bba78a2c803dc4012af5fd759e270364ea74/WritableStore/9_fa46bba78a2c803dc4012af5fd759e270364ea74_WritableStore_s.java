 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl;
 
 import java.io.*;
 import java.util.*;
 
 public abstract class WritableStore extends Store {
 	
 	private Map<String, Writer<?>> openWriters = new HashMap<String,Writer<?>>();
 	
 	@SuppressWarnings("serial")
 	public class AlreadyExistsException extends Exception {
 		private String trace_name; 
 		public AlreadyExistsException(String traceName){ trace_name = traceName;}
 		@Override
 		public String toString(){
 			return "Error! A trace named '"+trace_name+"' already exists.";
 		}
 	}
 	
 	public WritableStore() throws IOException {
 		super();
 	}
 	
 	public abstract void deleteFile(String name) throws IOException;
 	public abstract void deleteTrace(String name) throws IOException;
 	public abstract OutputStream getOutputStream (String name) throws IOException;
 	
 	public void putFile(File file, String name) throws IOException{
 		copy ( new FileInputStream(file), getOutputStream(name) );
 	}
 	
 	public void copy(InputStream ins, OutputStream outs) throws IOException{
 		BufferedOutputStream out = new BufferedOutputStream(outs);
 		BufferedInputStream in = new BufferedInputStream(ins);
 		int c;
 		while ( (c=in.read()) != -1 ){
 			out.write(c);
 		}
 		in.close();
 		out.close();
 	}
 	
 	void notifyClose(String name) throws IOException {
 		openWriters.remove(name);
		refresh();
 	}
 	
 	void notifyOpen(String name, Writer<?> writer) throws IOException {
 		openWriters.put(name, writer);
 	}
 	
 	boolean isAlreadyWriting(String name){
 		return openWriters.containsKey(name);
 	}
 	
 	@Override
 	public void close() throws IOException {
 		super.close();
 		for ( Writer<?> writer : openWriters.values() ){
 			writer.close();
 		}
 	}
 	
 	public static WritableStore open(File file) throws IOException {
 		buildTypeClassMap();
 		if ( file.exists() ){
 			if ( file.isDirectory() )
 				return new DirectoryStore(file);
 			return new JarDirectoryStore(file);
 		} else {
 			if ( file.getName().endsWith(".jar") )
 				return new JarDirectoryStore(file);
 			return new DirectoryStore(file);
 		}
 	}
 	
 	public void copyTrace(Store store, Trace<?> trace ) throws IOException {
 		String[] files = new String[]{
 				infoFile(trace.name()), 
 				(trace.isStateful())? snapshotsFile(trace.name()) : null,
 				traceFile(trace.name())};
 		for ( String file : files ){
 			if ( file != null ){
 				InputStream in = store.getInputStream(file);
 				OutputStream out = getOutputStream(file);
 				copy(in,out);
 			}
 		}
 	}
 	
	abstract void refresh() throws IOException;
	
 	public Trace<?> newTrace(String name, String type, boolean force) throws AlreadyExistsException, LoadTraceException {
 		if ( traces.containsKey(name) ){
 			if ( ! force )
 				throw new AlreadyExistsException(name);
 			return traces.get(name);
 		}
 		Trace<?> trace = buildTrace(name, new PersistentMap(), type);
 		return trace;
 	}
 	
 }
