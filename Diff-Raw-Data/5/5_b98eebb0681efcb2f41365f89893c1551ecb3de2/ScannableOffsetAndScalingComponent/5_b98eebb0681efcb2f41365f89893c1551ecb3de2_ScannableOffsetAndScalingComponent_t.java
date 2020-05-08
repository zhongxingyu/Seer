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
 
 package gda.device.scannable.component;
 
 import gda.device.Scannable;
 import gda.device.scannable.PositionConvertorFunctions;
 
 /**
  * All setters, getters and messages work with external positions.
  */
 public class ScannableOffsetAndScalingComponent implements PositionConvertor {
 
 	/**
 	 * Array of offsets (one for each input name). Null if no limits set. Any value within array may be null if that
 	 * input has no corresponding offset.
 	 */
 	private Double[] offsetArray = null;
 
 	private Double[] scaleArray = null;
 
 	private Scannable hostScannable;
 
 	public boolean hasOffsetOrScaleBeenSet() {
 		return ((offsetArray != null) | (scaleArray != null));
 	}
 
 	/**
 	 * e.g. 'Scannable' or 'Epics' (For messages only)
 	 */
 	protected String type = "";
 
 	public ScannableOffsetAndScalingComponent() {
 		type = "Scannable";
 	}
 
 	/**
 	 * 
 	 */
 	final private void checkPositionLength(Object[] positionArray) {
 		if ((positionArray.length != getHostScannable().getInputNames().length)
 				&& (positionArray.length != (getHostScannable().getInputNames().length + getHostScannable()
 						.getExtraNames().length))) {
 			throw new IllegalArgumentException(String.format(
 					"Expected position of length %d or %d but got position of length %d", getHostScannable()
 							.getInputNames().length, getHostScannable().getInputNames().length
 							+ getHostScannable().getExtraNames().length, positionArray.length));
 		}
 	}
 
 	public Double[] getOffset() {
 		return offsetArray;
 	}
 
 	public Double[] getScalingFactor() {
 		return scaleArray;
 	}
 
 	public void setOffset(Double[] internalOffset) {
 		if (internalOffset != null) {
 			checkPositionLength(internalOffset);
 		}
 		this.offsetArray = internalOffset;
 	}
 
 	public void setScalingFactor(Double[] scale) {
 		if (scale != null) {
 			checkPositionLength(scale);
 		}
 		this.scaleArray = scale;
 	}
 
 	public void setHostScannable(Scannable hostScannable) {
 		this.hostScannable = hostScannable;
 	}
 
 	public Scannable getHostScannable() {
 		return hostScannable;
 	}
 
 	/**
 	 * Removes offsets and then scale factors. Only converts an element of externalPosition from Object to Double if
 	 * there is an offset or scale to be applied to that element.
 	 */
 	@Override
 	public Object externalTowardInternal(Object externalPosition) {
 		// Units will have been removed if they existed on externalPosition
 
 		if (externalPosition == null) {
 			return null;
 		}
 
 		if (!hasOffsetOrScaleBeenSet()) {
 			return externalPosition;
 		}
 
 		// Create interimObjectArray and modify in place
 		Object[] externalObjectArray = PositionConvertorFunctions.toObjectArray(externalPosition);
 		Object[] internalObjectArray = externalObjectArray.clone();
 
 		for (int i = 0; i < externalObjectArray.length; i++) {
 			if (offsetArray != null) {
				if ((offsetArray[i] != null) && (externalObjectArray[i] != null)) {
 					internalObjectArray[i] = PositionConvertorFunctions.toDouble(externalObjectArray[i])
 							- offsetArray[i];
 				}
 			}
			if ((scaleArray != null)  && (internalObjectArray[i] != null)) {
 				if (scaleArray[i] != null) {
 					internalObjectArray[i] = PositionConvertorFunctions.toDouble(internalObjectArray[i])
 							/ scaleArray[i];
 				}
 			}
 		}
 
 		return PositionConvertorFunctions.toParticularContainer(internalObjectArray, externalPosition);
 
 	}
 
 	/**
 	 * Applies scale and then offset. Only converts an element of internalPosition from Object to Double if there is an
 	 * offset or scale to be remove from that element.
 	 */
 	@Override
 	public Object internalTowardExternal(Object internalPosition) {
 		// Units will not exist in an internalPosition (by definition)
 
 		if (internalPosition == null) {
 			return null;
 		}
 
 		if (!hasOffsetOrScaleBeenSet()) {
 			return internalPosition;
 		}
 
 		// Create interimObjectArray and modify in place
 		Object[] internalObjectArray = PositionConvertorFunctions.toObjectArray(internalPosition);
 		Object[] externalObjectArray = internalObjectArray.clone();
 		if (scaleArray != null) {
 			for (int i = 0; i < scaleArray.length; i++) {
 				if (scaleArray[i] != null) {
 					externalObjectArray[i] = PositionConvertorFunctions.toDouble(internalObjectArray[i])
 							* scaleArray[i];
 				}
 			}
 		}
 		if (offsetArray != null) {
 			for (int i = 0; i < offsetArray.length; i++) {
 
 				if (offsetArray[i] != null && externalObjectArray[i] != null) {
 					externalObjectArray[i] = PositionConvertorFunctions.toDouble(externalObjectArray[i])
 							+ offsetArray[i];
 				}
 			}
 		}
 
 		return PositionConvertorFunctions.toParticularContainer(externalObjectArray, internalPosition);
 
 	}
 
 }
