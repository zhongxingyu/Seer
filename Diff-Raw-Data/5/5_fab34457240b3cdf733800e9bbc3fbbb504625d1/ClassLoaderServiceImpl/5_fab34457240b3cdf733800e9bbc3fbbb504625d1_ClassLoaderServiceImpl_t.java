 /*-
  * Copyright 2014 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.osgi;
 import org.eclipse.ui.services.AbstractServiceFactory;
 import org.eclipse.ui.services.IServiceLocator;
 
 import py4j.ClassLoaderService;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.io.NumPyFileLoader;
 
 import com.thoughtworks.xstream.core.util.CompositeClassLoader;
 
 /**
  * Implementation of ClassLoaderService which allows access to split packages
  */
 public class ClassLoaderServiceImpl extends AbstractServiceFactory implements ClassLoaderService {
 
 	private CompositeClassLoader loader;
 
 	public ClassLoaderServiceImpl() {
 		loader = new CompositeClassLoader();
 		loader.add(AbstractDataset.class.getClassLoader()); // analysis.dataset
		loader.add(NumPyFileLoader.class.getClassLoader()); // analysis
 		loader.add(Slice.class.getClassLoader());           // analysis.api
 	}
 
 	@Override
 	public ClassLoader getClassLoader() {
 		return loader;
 	}
 
 	@Override
 	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
         if (serviceInterface == ClassLoaderService.class) {
         	return new ClassLoaderServiceImpl();
         }
 		return null;
 	}
 }
