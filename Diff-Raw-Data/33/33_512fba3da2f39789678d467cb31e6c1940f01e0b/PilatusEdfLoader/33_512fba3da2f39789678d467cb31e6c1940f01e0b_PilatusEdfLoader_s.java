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
 
 import gda.analysis.io.ScanFileHolderException;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.gda.monitor.IMonitor;
 
 /**
  * This class should be used to load ESRF datafiles created by the Pilatus detector system
  * into the ScanFileHolder object. This has not been tested on general ESRF datafiles.
  * <p>
  * <b>Note</b>: the header data from this loader is left as strings
  */
 public class PilatusEdfLoader extends AbstractFileLoader implements IMetaLoader {
 
 	private String fileName;
 	private Map<String, String> textMetadata = new HashMap<String, String>();
 	
 	/**
 	 * @param fileName
 	 */
 	public PilatusEdfLoader(String fileName) {
 		this.fileName = fileName;
 	}
 
 	@Override
 	public DataHolder loadFile() throws ScanFileHolderException {
 		return loadFile(null);
 	}
 	
 	@Override
 	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
		
		IntegerDataset integerData = null;
		FloatDataset floatData = null;
 		final DataHolder output = new DataHolder();
 		File f = null;
 		FileInputStream fi = null;
 		try {
 
 			f = new File(fileName);
 			fi = new FileInputStream(f);
 
 			BufferedReader br = new BufferedReader(new FileReader(f));
 			String line = br.readLine();
 			
 			// If the first line is not a { then we fail this loader.
 			if (!line.trim().startsWith("{")) throw new ScanFileHolderException("EDF File should start with {"); 
 			
 			if (line.contains("{")) {
 				
 				// Read the meta data
 				int index = readMetaData(br, line.length()+1, mon);
 				
 				// Now read the data
 				int height = Integer.parseInt(textMetadata.get("Dim_1"));
 				int width = Integer.parseInt(textMetadata.get("Dim_2"));
 				String dataType = textMetadata.get("DataType");
 				if (dataType.equals("Float")) {
					floatData = new FloatDataset(width, height);
					Utils.readFloat(fi, floatData, index);
 					
 				} else {
					integerData = new IntegerDataset(width, height);
 					if ("UnsignedShort".equals(textMetadata.get("DataType"))) {
 						if ("LowByteFirst".equals(textMetadata.get("ByteOrder")))
							Utils.readLeShort(fi, integerData, index);
 						else
							Utils.readBeShort(fi, integerData, index);
 					} else {
 						if ("LowByteFirst".equals(textMetadata.get("ByteOrder")))
							Utils.readLeInt(fi, integerData, index);
 						else
							Utils.readBeInt(fi, integerData, index);
 					}
 				}
 				data.setName(DEF_IMAGE_NAME);
 			}
 		} catch (Exception e) {
 			throw new ScanFileHolderException("File failed to load " + fileName, e);
 		} finally {
 			if (fi != null) {
 				try {
 					fi.close();
 				} catch (IOException ex) {
 					// do nothing
 				}
 				fi = null;
 			}
 		}
		if (integerData != null) {
			output.addDataset("ESRF Data Format", integerData);
		} else if (floatData != null) {
			output.addDataset("ESRF Data Format", floatData);			
 		}
 		return output;
 	}
 
 	@Override
 	public void loadMetaData(final IMonitor mon) throws Exception {
 
 		final BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
 		try {
 			final String line       = br.readLine();
 			readMetaData(br, line.length()+1, mon);
 		} finally {
 			br.close();
 		}
 	}
 	
 	@Override
 	public IMetaData getMetaData() {
 		return new MetaDataAdapter() {
 			
 			@Override
 			public String getMetaValue(String key) {
 				return textMetadata.get(key);	
 			}
 			
 			@Override
 			public Collection<String> getMetaNames() throws Exception{
 				return textMetadata.keySet();
 			}
 			
 			@Override
 			public Map<String,int[]> getDataShapes() {
 				int height = Integer.parseInt(textMetadata.get("Dim_1"));
 				int width = Integer.parseInt(textMetadata.get("Dim_2"));
                 final Map<String,int[]> ret = new HashMap<String,int[]>(1);
                 ret.put("ESRF Pilatus Data", new int[]{width,height});
                 return ret;
 			}
 			
 			@Override
 			public Collection<String> getDataNames() {
 				return Collections.unmodifiableCollection(Arrays.asList(new String[]{"ESRF Pilatus Data"}));
 			}
 		};
 	}
 	
 	private int readMetaData(final BufferedReader br, int index, final IMonitor mon) throws Exception {
 		
 		textMetadata.clear();
 		while (true) {
 			
 			if (mon!=null) mon.worked(1);
 			if (mon!=null&&mon.isCancelled()) throw new ScanFileHolderException("Loader cancelled during reading!");
 			
 			String line = br.readLine();
 			index += line.length()+1;
 			if (line.contains("}")) {
 				break;
 			}
 			String[] keyvalue = line.split("=");
 				
 			if (keyvalue.length == 1) {
 				textMetadata.put(keyvalue[0].trim(), "");
 			} else {		
 				int len = (keyvalue[1].endsWith(";")) ? keyvalue[1].length()-1 : keyvalue[1].length();
 				String value = keyvalue[1].substring(0, len);
 				textMetadata.put(keyvalue[0].trim(), value.trim());
 			}
 		}
 		
 		return index;
 	}
 }
