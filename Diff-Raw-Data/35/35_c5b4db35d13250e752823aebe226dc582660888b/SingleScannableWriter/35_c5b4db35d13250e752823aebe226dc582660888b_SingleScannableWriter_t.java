 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.data.scan.datawriter.scannablewriter;
 
 import gda.data.scan.datawriter.SelfCreatingLink;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.ScannableMotionUnits;
 
 import java.lang.reflect.Array;
 import java.nio.charset.Charset;
 import java.util.Collection;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.nexusformat.NeXusFileInterface;
 import org.nexusformat.NexusException;
 import org.nexusformat.NexusFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A simple example of a scannable with one input or extra name that needs 
  * to record in a specific place
  */
 public class SingleScannableWriter implements ScannableWriter {
 	private static final Logger logger = LoggerFactory.getLogger(SingleScannableWriter.class);
 
 	private String[] paths;
 	private String[] units;
 	private Collection<String> prerequisiteScannableNames;
 	
 	private int levels = 0;
 	
 	@Override
 	public Collection<String> getPrerequisiteScannableNames() {
 		return prerequisiteScannableNames;
 	}
 	
 	/**
 	 * Set the file into the position to write the data
 	 * 
 	 * @param file
 	 * @return name of trailing component 
 	 * @throws NexusException
 	 */
 	protected String enterLocation(NeXusFileInterface file, String path) throws NexusException {
 		levels = 0;
 		StringTokenizer st = new StringTokenizer(path, "/");
 		while(st.hasMoreTokens()) {
 			String[] split = st.nextToken().split(":");
 			String name = split[0];
 			if (split.length == 1) {
 				// no class, write data
 				return name;
 			}
 			String clazz = split[1];
 			try {
 				file.makegroup(name, clazz);
 			} catch (NexusException ne) {
 				// ignore, it might be there already
 			}
 			file.opengroup(name, clazz);
 			levels++;
 		}
 		
 		throw new IllegalArgumentException("configured path is not well formed (suspect it has no trailing component)");
 	}
 	
 	protected void leaveLocation(NeXusFileInterface file) throws NexusException {
 		for (int i = 0; i < levels; i++) {
 			file.closegroup();
 		}
 	}
 	
 	protected int[] makedimfordim(int[] dim) {
 		int[] mdim = new int[dim.length];
 		for (int i = 0; i < mdim.length; i++) {
 			mdim[i] = -1;
 		}
 		return mdim;
 	}
 
 	protected int[] nulldimfordim(int[] dim) {
 		int[] mdim = new int[dim.length];
 		for (int i = 0; i < mdim.length; i++) {
 			mdim[i] = 0;
 		}
 		return mdim;
 	}
 	
 	protected int[] onedimfordim(int[] dim) {
 		int[] mdim = new int[dim.length];
 		for (int i = 0; i < mdim.length; i++) {
 			mdim[i] = 1;
 		}
 		return mdim;
 	}
 	
 	protected int[] minusonedimfordim(int[] dim) {
 		int[] mdim = new int[dim.length];
 		for (int i = 0; i < mdim.length; i++) {
 			mdim[i] = -1;
 		}
 		return mdim;
 	}
 	
 	protected int componentsFor(Scannable s) {
 		int i = s.getInputNames() != null ? s.getInputNames().length : 0;
 		int e = s.getExtraNames() != null ? s.getExtraNames().length : 0;
 		return i + e;
 	}
 	protected String componentNameFor(Scannable s, int i) {
 		return ArrayUtils.addAll(s.getInputNames() != null ? s.getInputNames() : new String[]{}, 
 				s.getExtraNames() != null ? s.getExtraNames() : new String[]{})[i].toString();
 	}
 	
 	@Override
 	public Collection<? extends SelfCreatingLink> makeScannable(NeXusFileInterface file, Scannable s, Object position,
 			int[] dim) throws NexusException {
 		Vector<SelfCreatingLink> sclc = new Vector<SelfCreatingLink>();
 
 		for (int i = 0; i < componentsFor(s); i++) {
 			try {
 				if (paths[i].isEmpty())
 					continue;
 				String componentName = componentNameFor(s,i);
 				String unit = null; 
 				if (s instanceof ScannableMotionUnits)
 					unit = ((ScannableMotionUnits) s).getUserUnits();
 				if (units != null && units.length > i)
 					unit = units[i];
 				sclc.addAll(makeComponent(file, dim, paths[i], s.getName(), componentName, getComponentObject(s, position, i), unit));
 			} catch (Exception e) {
 				logger.error("error converting scannable data", e);
 			}
 		}
 		return sclc;
 	}
 
	protected Object getComponentObject(@SuppressWarnings("unused") Scannable s, Object position, int i) {
 		return getArrayObject(position)[i];
 	}
 	
 	private final Class<?>[] ARRAY_PRIMITIVE_TYPES = { 
 			int[].class, float[].class, double[].class, boolean[].class, 
 			byte[].class, short[].class, long[].class, char[].class };
 
 	private Object[] getArrayObject(Object foo) {
 		if (foo.getClass().isAssignableFrom(Object[].class))
 			return (Object[]) foo;
 		if (foo.getClass().isArray()) {
 			Class<?> valKlass = foo.getClass();
 			Object[] outputArray = null;
 			
 			for(Class<?> arrKlass : ARRAY_PRIMITIVE_TYPES){
 				if(valKlass.isAssignableFrom(arrKlass)){
 					int arrlength = Array.getLength(foo);
 					outputArray = new Object[arrlength];
 					for(int i = 0; i < arrlength; ++i){
 						outputArray[i] = Array.get(foo, i);
 					}
 					break;
 				}
 			}
 			if(outputArray == null) // not primitive type array
 				outputArray = (Object[]) foo;
 			
 			return outputArray;
 		}
 		return new Object[] { foo };
 	}
 
 	protected Collection<SelfCreatingLink> makeComponent(NeXusFileInterface file, int[] dim, String path, String scannableName, String componentName, Object pos, String unit) throws NexusException {
 		Vector<SelfCreatingLink> sclc = new Vector<SelfCreatingLink>();
 
 		String name = enterLocation(file, path);
 		
 		file.makedata(name, NexusFile.NX_FLOAT64, dim.length, minusonedimfordim(dim));
 		file.opendata(name);
 		file.putattr("local_name", String.format("%s.%s", scannableName, componentName).getBytes(), NexusFile.NX_CHAR);
 
 		String axislist = "1";
 		for (int j = 2; j <= dim.length; j++) {
 			axislist = axislist + String.format(",%d", j);
 		}
 		file.putattr("axis", axislist.getBytes(), NexusFile.NX_CHAR);
 		if (unit != null && !unit.isEmpty())
 			file.putattr("units", unit.getBytes(Charset.forName("UTF-8")), NexusFile.NX_CHAR);
 		Object poso = pos; //autoboxing
 		file.putslab(new double[] {(Double) poso}, nulldimfordim(dim), onedimfordim(dim));
 
 		sclc.add(new SelfCreatingLink(file.getdataID()));
 		file.closedata();
 
 		leaveLocation(file);
 		
 		return sclc;
 	}
 
 	@Override
 	public void writeScannable(NeXusFileInterface file, Scannable s, Object position, int[] start) throws NexusException {
 		for (int i = 0; i < componentsFor(s); i++) {
 			if (paths[i].isEmpty())
 				continue;
 			String name = enterLocation(file, paths[i]);
 			
 			file.opendata(name);
 			try {
				Object slab = getComponentSlab(s, position, i);
				file.putslab(slab, start, onedimfordim(start));
 			} catch (Exception e) {
 				logger.error("error converting scannable data", e);
 			}
 			file.closedata();
 	
 			leaveLocation(file);
 		}
 	}
 
	@SuppressWarnings("unused")
	protected Object getComponentSlab(Scannable s, Object position, int i) throws DeviceException {
		Object poso = getComponentObject(s, position, i);
		return new double[] {(Double) poso};
 	}
 	
 	public String[] getPaths() {
 		return paths;
 	}
 
 	public void setPaths(String[] paths) {
 		this.paths = paths;
 	}
 
 	public String[] getUnits() {
 		return units;
 	}
 
 	public void setUnits(String[] units) {
 		this.units = units;
 	}
 
 	public void setPrerequisiteScannableNames(Collection<String> prerequisiteScannableNames) {
 		this.prerequisiteScannableNames = prerequisiteScannableNames;
 	}
 }
