 /*
  * Copyright 2011 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import java.io.Serializable;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public interface ILazyLoader extends Serializable {
 
 	/**
 	 * 
 	 * @return true if file is readable
 	 */
 	public boolean isFileReadable();
 
 	/**
 	 * @param mon
 	 * @param shape
 	 * @param start
 	 * @param stop
 	 * @param step
 	 * @return a slice of a dataset
	 * @throws Exception
 	 */
 	public IDataset getDataset(IMonitor mon, int[] shape, int[] start, int[] stop, int[] step) throws Exception;
 }
