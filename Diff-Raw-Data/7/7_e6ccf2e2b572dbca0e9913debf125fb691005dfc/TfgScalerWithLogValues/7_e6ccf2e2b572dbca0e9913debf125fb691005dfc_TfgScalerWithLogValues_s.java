 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
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
 
 package gda.device.detector.countertimer;
 
 import gda.device.DeviceException;
 
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * A version of TfgScaler for Spectroscopy Ionchambers which assumes it has output channels (time),I0,It,Iref.
  * <p>
  * It has optional additional channels ln(I0/It) and ln(I0/Iref) It also reads the dark current at the scan start.
  */
 public class TfgScalerWithLogValues extends TfgScalerWithDarkCurrent {
 
 	public static final String LNI0IT_LABEL = "lnI0It";
 	public static final String LNITIREF_LABEL = "lnItIref";
 
 	private boolean outputLogValues = false; // add ln(I0/It) and ln(I0/Iref) to the output
 
 	public TfgScalerWithLogValues() {
 		super();
 	}
 
 	/**
 	 * @return Returns the outputLogValues.
 	 */
 	public boolean isOutputLogValues() {
 		return outputLogValues;
 	}
 
 	/**
 	 * When set to true ln(I0/It) and ln(I0/Iref) will be added to the output columns
 	 * 
 	 * @param outputLogValues
 	 *            The outputLogValues to set.
 	 */
 
 	public void setOutputLogValues(boolean outputLogValues) {
 		this.outputLogValues = outputLogValues;
 
 		// adjust the extraNmaes and outputFormat arrays
 		if (!configured) {
 			return;
 		}
 		if (outputLogValues) {
 			if (!ArrayUtils.contains(extraNames, LNI0IT_LABEL)) {
 				extraNames = (String[]) ArrayUtils.add(extraNames, LNI0IT_LABEL);
 				outputFormat = (String[]) ArrayUtils.add(outputFormat, "%.4g");
 			}
 			if (!ArrayUtils.contains(extraNames, LNITIREF_LABEL)) {
 				extraNames = (String[]) ArrayUtils.add(extraNames, LNITIREF_LABEL);
 				outputFormat = (String[]) ArrayUtils.add(outputFormat, "%.4g");
 			}
 		} else {
 			if (ArrayUtils.contains(extraNames, LNI0IT_LABEL)) {
 				int index = ArrayUtils.indexOf(extraNames, LNI0IT_LABEL);
 				extraNames = (String[]) ArrayUtils.remove(extraNames, index);
				outputFormat = (String[]) ArrayUtils.remove(outputFormat, index);
 			}
 			if (ArrayUtils.contains(extraNames, LNITIREF_LABEL)) {
 				int index = ArrayUtils.indexOf(extraNames, LNITIREF_LABEL);
 				extraNames = (String[]) ArrayUtils.remove(extraNames, index);
				outputFormat = (String[]) ArrayUtils.remove(outputFormat, index);
 			}
 
 		}
 	}
 
 	@Override
 	public int getTotalChans() throws DeviceException {
 		int cols = scaler.getDimension()[0];
 		if (numChannelsToRead != null) {
 			cols = numChannelsToRead;
 		}
 		if (timeChannelRequired) {
 			cols++;
 		}
 		if (outputLogValues) {
 			cols += 2;
 		}
 		if (isTFGv2()) {
 			cols--;
 		}
 		return cols;
 	}
 
 	@Override
 	public double[] readout() throws DeviceException {
 		double[] output = super.readout();
 		
 		return performCorrections(output);
 	}
 
 	protected double[] performCorrections(double[] output) throws DeviceException {
 		if (getDarkCurrent() != null) {
 			output = adjustForDarkCurrent(output, getCollectionTime());
 		}
 
 		if (outputLogValues) {
 			output = appendLogValues(output);
 		}
 		return output;
 	}
 	
 	@Override
 	public double[] readFrame(int frame) throws DeviceException {
 		// For legacy XAFS scans the time channel is in column 1
 		double[] output = super.readFrame(frame);
 		return performCorrections(output);
 	}
 
 	protected double[] appendLogValues(double[] output) {
 		Double[] logs = new Double[2];
 		// find which col is which I0, It and Iref
 		Double[] values = getI0ItIRef(output);
 
 		// NOTE Assumes that the order of the data (time, I0, It, Iref...)
 		// for dark current does not change.
 		logs[0] = Math.log(values[0] / values[1]);
 		logs[1] = Math.log(values[1] / values[2]);
 
 		// always return a numerical value
 		if (logs[0].isInfinite() || logs[0].isNaN()) {
 			logs[0] = 0.0;
 		}
 		if (logs[1].isInfinite() || logs[1].isNaN()) {
 			logs[1] = 0.0;
 		}
 
 		// append to output array
 		output = correctCounts(output, values);
 		output = ArrayUtils.add(output, logs[0]);
 		output = ArrayUtils.add(output, logs[1]);
 		return output;
 	}
 
 	/*
 	 * This should only be called when outputLogValues is set to true.
 	 */
 	private Double[] getI0ItIRef(double[] data) {
 		if (timeChannelRequired) {
 			return new Double[] { data[1], data[2], data[3] };
 		}
 		return new Double[] { data[0], data[1], data[2] };
 	}
 
 	private double[] correctCounts(final double[] output, final Double[] counts) {
 		final int outIndex = (timeChannelRequired) ? 1 : 0;
 		for (int i = 0; i < counts.length; i++) {
 			output[i + outIndex] = counts[i];
 		}
 		return output;
 	}
 
 }
