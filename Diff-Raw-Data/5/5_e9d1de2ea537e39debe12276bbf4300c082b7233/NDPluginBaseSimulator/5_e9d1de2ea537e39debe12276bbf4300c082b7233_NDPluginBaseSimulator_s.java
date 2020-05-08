 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package gda.device.detector.areadetector.v17.impl;
 
 import gda.device.detector.areadetector.v17.NDPluginBase;
 
 /**
  * Simulation of NDPligin. 
  * NDPluginBaseSimulator pluginBase = new NDPluginBaseSimulator(); 
  * pluginBase.setDims(new * int[]{1000,1000}); 
  * pluginBase.setDatatype(NDPluginBase.DataType.UINT32 );
  */
 public class NDPluginBaseSimulator implements NDPluginBase {
 
 	int[] dims;
 
 	public int[] getDims() {
 		return dims;
 	}
 
 	public void setDims(int[] dims) {
 		this.dims = dims;
 	}
 
 	public short getDatatypeOrd() {
 		return datatype;
 	}
 
 	public void setDatatypeOrd(short ordinal) {
 		datatype = ordinal;
 	}
 
 	public void setDatatype(NDPluginBase.DataType datatype) {
 		this.datatype = (short) datatype.ordinal();
 	}
 
 	@Override
 	public String getPortName_RBV() throws Exception {
 
 		return null;
 	}
 
 	@Override
 	public String getPluginType_RBV() throws Exception {
 
 		return null;
 	}
 
 	@Override
 	public String getNDArrayPort() throws Exception {
 
 		return null;
 	}
 
 	@Override
 	public void setNDArrayPort(String ndarrayport) throws Exception {
 
 	}
 
 	@Override
 	public String getNDArrayPort_RBV() throws Exception {
 
 		return null;
 	}
 
 	@Override
 	public int getNDArrayAddress() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public void setNDArrayAddress(int ndarrayaddress) throws Exception {
 
 	}
 
 	@Override
 	public int getNDArrayAddress_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public boolean isCallbackEnabled() throws Exception {
 
 		return false;
 	}
 
 	@Override
 	public boolean isCallbacksEnabled_RBV() throws Exception {
 
 		return false;
 	}
 
 	@Override
 	public double getMinCallbackTime() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public void setMinCallbackTime(double mincallbacktime) throws Exception {
 
 	}
 
 	@Override
 	public double getMinCallbackTime_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public short getBlockingCallbacks() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public void setBlockingCallbacks(int blockingcallbacks) throws Exception {
 
 	}
 
 	@Override
 	public short getBlockingCallbacks_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public int getArrayCounter() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public void setArrayCounter(int arraycounter) throws Exception {
 
 	}
 
 	@Override
 	public int getArrayCounter_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public double getArrayRate_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public int getDroppedArrays() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public void setDroppedArrays(int droppedarrays) throws Exception {
 
 	}
 
 	@Override
 	public int getDroppedArrays_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public int getNDimensions_RBV() throws Exception {
 		return dims.length;
 	}
 
 	@Override
 	public int getArraySize0_RBV() throws Exception {
		return dims[0];
 	}
 
 	@Override
 	public int getArraySize1_RBV() throws Exception {
		return dims[1];
 	}
 
 	@Override
 	public int getArraySize2_RBV() throws Exception {
 
 		return 0;
 	}
 
 	short datatype = NDPluginBase.UInt8;
 
 	@Override
 	public short getDataType_RBV() throws Exception {
 		return datatype;
 	}
 
 	@Override
 	public short getColorMode_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public short getBayerPattern_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public int getUniqueId_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public double getTimeStamp_RBV() throws Exception {
 
 		return 0;
 	}
 
 	@Override
 	public String getNDAttributesFile() throws Exception {
 
 		return null;
 	}
 
 	@Override
 	public void setNDAttributesFile(String ndattributesfile) throws Exception {
 
 	}
 
 	@Override
 	public void disableCallbacks() throws Exception {
 
 	}
 
 	@Override
 	public void enableCallbacks() throws Exception {
 
 	}
 
 	@Override
 	public void reset() throws Exception {
 
 	}
 
 	@Override
 	public String getInitialArrayPort() {
 
 		return null;
 	}
 
 	@Override
 	public Integer getInitialArrayAddress() {
 
 		return null;
 	}
 
 }
