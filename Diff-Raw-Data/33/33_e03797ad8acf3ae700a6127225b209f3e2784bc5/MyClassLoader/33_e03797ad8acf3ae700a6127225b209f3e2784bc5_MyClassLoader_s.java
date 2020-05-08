 /*
  * Copyright 2013 Charles University in Prague
  * Copyright 2013 Vojtech Horky
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package cz.cuni.mff.d3s.spl.tests.probes.instrument;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import org.junit.Ignore;
 
 @Ignore
 public class MyClassLoader extends URLClassLoader {
 	private boolean actionAlreadyLoaded = false;
 	
 	public MyClassLoader(URL[] urls, ClassLoader parent) {
 		super(urls, parent);
 	}
 
 	@Override
 	public Class<?> loadClass(String name) throws ClassNotFoundException {
 		if (actionAlreadyLoaded) {
 			return super.loadClass(name);
 		}
 		if (!name.equals("cz.cuni.mff.d3s.spl.tests.probes.instrument.Action")) {
 			return super.loadClass(name);
 		}
 		InputStream stream = this.getResourceAsStream(name.replace('.', '/')
 				+ ".class");
 		byte[] data;
 		try {
 			data = streamToBytes(stream);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return super.loadClass(name);
 		}
 		actionAlreadyLoaded = true;
 		return defineClass(name, data, 0, data.length);
 	}
 
 	@Override
 	public String toString() {
 		return String.format("MyClassLoader[%s]", super.toString());
 	}
 
 	private static byte[] streamToBytes(InputStream input) throws IOException {
 		ByteArrayOutputStream output = new ByteArrayOutputStream();
 		while (true) {
 			byte[] buffer = new byte[1024];
 			int bytesActuallyRead = input.read(buffer);
 			if (bytesActuallyRead == -1) {
 				break;
 			}
 			output.write(buffer, 0, bytesActuallyRead);
 		}
 		return output.toByteArray();
 	}
 }
