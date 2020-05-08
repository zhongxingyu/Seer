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
 
 package gda.device.scannable.scannablegroup;
 
 import static gda.device.scannable.PositionConvertorFunctions.toObjectArray;
 import static gda.device.scannable.PositionConvertorFunctions.toParticularContainer;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.continuouscontroller.ContinuousMoveController;
 import gda.device.continuouscontroller.TrajectoryMoveController;
 import gda.device.scannable.ContinuouslyScannableViaController;
 import gda.device.scannable.PositionConvertorFunctions;
 import gda.device.scannable.ScannableMotionBase;
 import gda.device.scannable.ScannableMotionUnitsBase;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.python.core.Py;
 import org.python.core.PyException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Extend as ScannableMotionBase. Provides a {@link #completeInstantiation()} method which adds a dictionary of
  * ScannableFields to an instance. Each ScannableField allows one of the instances fields to be interacted with like it
  * itself is a scannable. Fields are accessible from Jython as attributes, or using the
  * {@link #getFieldScannable(String)}.
  * <p>
  * When moving a ScannableField (via either a pos or scan command), the ScnnableField calls the parent to perform the
  * actual task. The ScannableField's asynchronousMoveto command will call the parent with a list of None values except
  * for the field it represents which will be passed the desired position value.
  * <p>
  * The asynchronousMoveTo method in class that inherits from this base class then must handle these Nones. In some cases
  * the method may actually be able to move the underlying system associated with one field individually from others. However by
  * default it substitute's the None values with the actual current position of parent's scannables associated fields.
  * <p>
  * TODO: THIS IS NOT YET IMPLEMENTED ScannableMotionBaseWithMemory() extends this class and provides a solution
  * useful for some scenarios: it keeps track of the last position moved to, and replaces the Nones in an
  * asynchronousMoveTo request with these values. There are a number of dangers associated with this which are addressed
  * in that class's documentation, but it provides a way to move one axis within a group of non-orthogonal axis while
  * keeping the others still.
  */
 public class ScannableMotionWithScannableFieldsBase extends ScannableMotionBase implements ICoordinatedParentScannable,  ContinuouslyScannableViaController {
 
 	public class ScannableField extends ScannableMotionUnitsBase implements ICoordinatedChildScannable, ContinuouslyScannableViaController {
 
 		private final boolean isInputField;
 
 		private final int index;
 
 		private ScannableMotionWithScannableFieldsBase parent;
 
 		/**
 		 * @param fieldName
 		 * @param parent
 		 * @param isInputField
 		 * @param index
 		 */
 		public ScannableField(String fieldName, ScannableMotionWithScannableFieldsBase parent, boolean isInputField, int index) {
 			this.parent = parent;
 			this.isInputField = isInputField;
 			this.index = index;
 
 			setName(fieldName);
 			if (isInputField) {
 				setInputNames(new String[] { fieldName });
 				setExtraNames(new String[] {});
 			} else {
 				setInputNames(new String[] {});
 				setExtraNames(new String[] { fieldName });
 			}
 		}
 
 		@Override
 		public String[] getOutputFormat() {
 			return Arrays.copyOfRange(parent.getOutputFormat(), index, index + 1);
 		}
 
 		@Override
 		public void rawAsynchronousMoveTo(Object internalPosition) throws DeviceException {
 			if (!isInputField) {
 				throw new DeviceException(String.format(
 						"The ScannableField %s.%s represents an output. It therefore could not be moved to %s .",
 						parent.getName(), getName(), internalPosition.toString()));
 			}
 			if (parent.isTargeting()) {
 				parent.setChildTarget(this, internalPosition);
 			} else {
 				parent.asynchronousMoveFieldTo(index, internalPosition);
 			}
 		}
 
 		@Override
 		public Object rawGetPosition() throws DeviceException {
 			return parent.getFieldPosition(index);
 		}
 
 //		/**
 //		 * Should not be reachable. A call to this scannable's getPosition should map directly to a call on the parent
 //		 * Scannables' getfieldPosition.
 //		 */
 //		@Override
 //		public Object rawGetPosition() throws DeviceException {
 //			throw new RuntimeException("DottedAccessPseudoDevice is improperly implemented.");
 //		}
 
 		@Override
 		public boolean isBusy() throws DeviceException {
 			return parent.isBusy();
 		}
 
 		@Override
 		public void atCommandFailure() throws DeviceException {
 			parent.atCommandFailure();
 		}
 
 		@Override
 		public void atLevelMoveStart() throws DeviceException {
 			parent.addChildToMove(this);
 		}
 
 		@Override
 		public ICoordinatedParent getParent() {
 			return parent;
 		}
 
 		@Override
 		public boolean isInputField() {
 			return isInputField;
 		}
 
 		@Override
 		public void setParent(ICoordinatedParent parent) {
 			this.parent = (ScannableMotionWithScannableFieldsBase) parent;
 		}
 
 		@Override
 		public void atScanStart() throws DeviceException {
 			if (!parent.awaitingScanEnd) {
 				parent.atScanStart();
 				parent.awaitingScanEnd = true;
 			}
 		}
 
 		@Override
 		public void atScanEnd() throws DeviceException {
 			if (parent.awaitingScanEnd) {
 				parent.atScanEnd();
 				parent.awaitingScanEnd = false;
 			}
 		}
 
 		@Override
 		public void atScanLineStart() throws DeviceException {
 			if (!parent.awaitingScanLineEnd) {
 				parent.atScanLineStart();
 				parent.awaitingScanLineEnd = true;
 			}
 		}
 
 		@Override
 		public void atScanLineEnd() throws DeviceException {
 			if (parent.awaitingScanLineEnd) {
 				parent.atScanLineEnd();
 				parent.awaitingScanLineEnd = false;
 			}
 		}
 
 		@Override
 		public void atPointStart() throws DeviceException {
 			if (!parent.awaitingPointEnd) {
 				parent.atPointStart();
 				parent.awaitingPointEnd = true;
 			}
 		}
 
 		@Override
 		public void atPointEnd() throws DeviceException {
 			if (parent.awaitingPointEnd) {
 				parent.atPointEnd();
 				parent.awaitingPointEnd = false;
 			}
 		}
 
 		@Override
 		public void close() throws DeviceException {
 			parent.close();
 		}
 
 		@Override
 		public int getLevel() {
 			return parent.getLevel();
 		}
 
 		@Override
 		public int getNumberTries() {
 			return parent.getNumberTries();
 		}
 
 		@Override
 		public int getProtectionLevel() throws DeviceException {
 			return parent.getProtectionLevel();
 		}
 
 		@Override
 		public Double[] getTolerances() throws DeviceException {
 			return new Double[] { parent.getTolerances()[index] };
 		}
 
 		@Override
 		public Double[] getOffset() {
 			Double[] parentOffsetArray = parent.getOffset();
 			if (parentOffsetArray == null) {
 				return null;
 			}
 			return (parentOffsetArray[index]==null) ? null : new Double[] { parentOffsetArray[index] };
 		}
 		
 		@Override
 		public Double[] getScalingFactor() {
 			Double[] parentScalingFactor = parent.getScalingFactor();
 			if (parentScalingFactor == null) {
 				return null;
 			}
 			return (parentScalingFactor[index]==null) ? null : new Double[] { parentScalingFactor[index] };
 		}
 		
 		
 		@Override
 		public void setLevel(int level) {
 			parent.setLevel(level);
 		}
 
 		@Override
 		public void setTolerance(Double tolerance) throws DeviceException {
 			if (!isInputField) {
 				throw new DeviceException(String.format("Could not set tolerance on scannable %s to %s. This scannable"
 						+ " represents a read-only field on %s.", getName(), tolerance, parent.getName()));
 			}
 			Double[] newTolerance = parent.getTolerances();
 			if (newTolerance==null) {
 				newTolerance = new Double[parent.getInputNames().length];
 				Arrays.fill(newTolerance, 0.);
 			}
 			newTolerance[index] = tolerance;
 			parent.setTolerances(newTolerance);
 		}
 
 		
 		
 		@Override
 		public void setTolerances(Double[] tolerance) throws DeviceException {
 			if (!(tolerance.length == 1)) {
 				throw new DeviceException(String.format("Could not set tolerance on scannable %s to %s. This scannable"
 						+ "has only one field.", getName(), Arrays.toString(tolerance)));
 			}
 			setTolerance(tolerance[0]);
 		}
 
 		@Override
 		public void setOffset(Double... offsetArray) {
 			if (!(offsetArray.length == 1)) {
 				throw new IllegalArgumentException(String.format("Could not scannable%s's offset to %s. This scannable"
 						+ "has only one field.", getName(), Arrays.toString(offsetArray)));
 			}
 			Double[] parentOffsetArray = parent.getOffset();
 			if (parentOffsetArray == null) {
 				parentOffsetArray = new Double[parent.getInputNames().length + parent.getExtraNames().length];
 			}
 			parentOffsetArray[index] = offsetArray[0];
 			parent.setOffset(parentOffsetArray);
 		}
 		
 		@Override
 		public void setScalingFactor(Double... scaleArray) {
 			if (!(scaleArray.length == 1)) {
 				throw new IllegalArgumentException(String.format("Could not scannable%s's scaling factor to %s. This scannable"
 						+ "has only one field.", getName(), Arrays.toString(scaleArray)));
 			}
 			Double[] parentScaleArray = parent.getScalingFactor();
 			if (parentScaleArray == null) {
 				parentScaleArray = new Double[parent.getInputNames().length + parent.getExtraNames().length];
 			}
 			parentScaleArray[index] = scaleArray[0];
 			parent.setScalingFactor(parentScaleArray);
 		}
 		
 		@Override
 		@Deprecated
 		public void atEnd() throws DeviceException {
 			throw new RuntimeException("Call to deprecated method atEnd() on " + getName()
 					+ ". Use atScanEnd() instead");
 		}
 
 		@Override
 		@Deprecated
 		public void atStart() throws DeviceException {
 			throw new RuntimeException("Call to deprecated method atStart() on " + getName()
 					+ ". Use atScanStart() instead");
 		}
 
 		@Override
 		public Double[] getLowerGdaLimits() {
 			Double[] limit = parent.getLowerGdaLimits();
 			return limit == null ? null : new Double[] { limit[index]};
 		}
 
 		@Override
 		public Double[] getUpperGdaLimits() {
 			Double[] limit = parent.getUpperGdaLimits();
 			return limit == null ? null : new Double[] { limit[index] };
 		}
 
 		@Override
 		public void setLowerGdaLimits(Double lowerLim) throws Exception {
 			Double[] limit = parent.getLowerGdaLimits();
 			if (limit == null){
 				limit = new Double[parent.getInputNames().length];
 			}
 			limit[index] = lowerLim;
 			parent.setLowerGdaLimits(limit);
 		}
 
 		@Override
 		public void setUpperGdaLimits(Double upperLim) throws Exception {
 			Double[] limit = parent.getUpperGdaLimits();
 			if (limit == null){
 				limit = new Double[parent.getInputNames().length];
 			}
 			limit[index] = upperLim;
 			parent.setUpperGdaLimits(limit);
 		}
 
 		@Override
 		public void setLowerGdaLimits(Double[] lowerLim) throws Exception {
 			if (!(lowerLim.length == 1)) {
 				throw new DeviceException(String.format(
 						"Could not setLowerGdaLmits() on scannable %s to %s. This scannable has only one field.",
 						getName(), Arrays.toString(lowerLim)));
 			}
 			setLowerGdaLimits(lowerLim[0]);
 		}
 
 		@Override
 		public void setUpperGdaLimits(Double[] upperLim) throws Exception {
 			if (!(upperLim.length == 1)) {
 				throw new DeviceException(String.format(
 						"Could not setUpperGdaLmits() on scannable %s to %s. This scannable has only one field.",
 						getName(), Arrays.toString(upperLim)));
 			}
 			setUpperGdaLimits(upperLim[0]);
 		}
 
 		@Override
 		public void setOutputFormat(String[] formats) {
 			if (!(formats.length == 1)) {
 				throw new RuntimeException(String.format(
 						"Could not setOutputFormat() on scannable %s to %s. This scannable has only one field.",
 						getName(), Arrays.toString(formats)));
 			}
 			String[] newFormats = parent.getOutputFormat();
 			newFormats[index] = formats[0];
 			parent.setOutputFormat(newFormats);
 		}
 
 		@Override
 		public void setNumberTries(int numberTries) {
 			parent.setNumberTries(numberTries);
 		}
 
 		@Override
 		public void setProtectionLevel(int permissionLevel) throws DeviceException {
 			parent.setProtectionLevel(permissionLevel);
 		}
 
 		@Override
 		public void stop() throws DeviceException {
 			parent.stop();
 		}
 
 		@Override
 		public ContinuousMoveController getContinuousMoveController() {
 			return parent.getContinuousMoveController();
 		}
 
 		@Override
 		public boolean isOperatingContinously() {
 			return parent.isOperatingContinously();
 		}
 
 		@Override
 		public void setOperatingContinuously(boolean b) {
 			parent.setOperatingContinuously(b);
 		}
 
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(ScannableMotionWithScannableFieldsBase.class);
 
 	List<ScannableField> children;
 
 	Map<String, ScannableField> scannableFieldByName;
 
 	Map<Integer, Object> targetMap;
 
 	CoordinatedParentScannableComponent coordinatedScannableComponent;
 
 	private boolean awaitingScanEnd = false;
 
 	private boolean awaitingScanLineEnd = false;
 
 	private boolean awaitingPointEnd = false;
 
 	private boolean operatingContinuousely;
 	
 	private boolean autoCompletePartialMoveToTargets = false;
 
 	private TrajectoryMoveController controller;
 
 	private Object[] positionAtScanStart = null; // null indicates not operating in a scan
 
 	public void setPositionAtScanStart(Object[] positionAtScanStart) {
 		this.positionAtScanStart = positionAtScanStart;
 	}
 
 	private boolean usePositionAtScanStartWhenCompletingPartialMoves = true;
 
 	public boolean isUsePositionAtScanStartWhenCompletingPartialMoves() {
 		return usePositionAtScanStartWhenCompletingPartialMoves;
 	}
 
 	public void setUsePositionAtScanStartWhenCompletingPartialMoves(boolean usePositionAtScanStartWhenCompletingPartialMoves) {
 		this.usePositionAtScanStartWhenCompletingPartialMoves = usePositionAtScanStartWhenCompletingPartialMoves;
 	}
 
 	public boolean isAutoCompletePartialMoveToTargets() {
 		return autoCompletePartialMoveToTargets;
 	}
 
 	public void setAutoCompletePartialMoveToTargets(boolean autoCompletePartialMoveToTargets) {
 		this.autoCompletePartialMoveToTargets = autoCompletePartialMoveToTargets;
 	}
 
 	/**
 	 * Sets input names and creates scannable for each field;
 	 */
 	@Override
 	public void setInputNames(String[] names) {
 		super.setInputNames(names);
 		configureFieldScannables();
 	}
 
 	/**
 	 * Sets extra names and creates scannable for each field;
 	 */
 	@Override
 	public void setExtraNames(String[] names) {
 		super.setExtraNames(names);
 		configureFieldScannables();
 	}
 
 	private void configureFieldScannables() {
 		createScannableForEachField();
 		coordinatedScannableComponent = new CoordinatedParentScannableComponent(this);
 		List<ICoordinatedChildScannable> coordinatedFieldList = new ArrayList<ICoordinatedChildScannable>();
 		for (Scannable field : children) {
 			coordinatedFieldList.add((ICoordinatedChildScannable) field);
 		}
 		coordinatedScannableComponent.setMembers(coordinatedFieldList);
 	}
 
 	void resetState() {
 		awaitingScanEnd = false;
 		awaitingScanLineEnd = false;
 		awaitingPointEnd = false;
 	}
 
 	@Override
 	public void atScanStart() throws DeviceException {
 		if (usePositionAtScanStartWhenCompletingPartialMoves) {
 			positionAtScanStart = toObjectArray(getPosition());
 		}
 		logger.info(getName() + ": base position for partial position moves = " + Arrays.toString(positionAtScanStart));
 		super.atScanStart();
 	}
 
 	@Override
 	public void atCommandFailure() throws DeviceException {
 		positionAtScanStart = null;
 		coordinatedScannableComponent.resetState();
 		resetState();
 	}
 	
 	
 	@Override
 	public void atScanEnd() throws DeviceException {
 		positionAtScanStart = null;
 		super.atScanEnd();
 	}
 	
 	/**
 	 * Returns the position sampled at the start of the scan, or null if operating in a scan.
 	 * @return position if in scan or null
 	 */
 	public Object[] getPositionAtScanStart() {
 		return positionAtScanStart;
 	}
 	
 	/**
 	 * Calls asynchronousMovoTo on the DottedAccessScannable. Fills all fields but index with nulls. May be overridden
 	 * to improve performance.
 	 * 
 	 * @param index
 	 * @param position
 	 * @throws DeviceException
 	 */
 	public void asynchronousMoveFieldTo(int index, Object position) throws DeviceException {
 		ArrayList<Object> target = new ArrayList<Object>();
 		// Fill with nulls to start
 		for (ICoordinatedChildScannable member : children) {
 			if (member.isInputField()) {
 				target.add(null);
 			}
 		}
 		try {
 			target.set(index, position);
 		} catch (IndexOutOfBoundsException e) {
 			throw new DeviceException("", e);
 		}
 		asynchronousMoveTo(target.toArray());
 	}
 
 	@Override
 	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
 		if (!autoCompletePartialMoveToTargets) {
 			super.asynchronousMoveTo(externalPosition);
 		} else {
 			Object[] externalArray = toObjectArray(externalPosition);
 			if (Arrays.asList(externalArray).contains(null)) {
 				Object[] completedArray = completePartialMoveTarget(externalArray);
 				Object completedPosition = toParticularContainer(completedArray, externalPosition);
 				super.asynchronousMoveTo(completedPosition);
 			} else {
 				super.asynchronousMoveTo(externalPosition);
 			}
 		}
 	}
 	
 	protected Object[] completePartialMoveTarget(Object[] externalTarget) throws DeviceException {
 		Object[] originalExternalTarget = Arrays.copyOf(externalTarget, externalTarget.length);
 		Object[] basePosition = (getPositionAtScanStart() != null) ? getPositionAtScanStart() : toObjectArray(getPosition());
 		for (int i = 0; i < externalTarget.length; i++) {
 			if (externalTarget[i] == null) {
 				externalTarget[i] = PositionConvertorFunctions.toDouble(basePosition[i]);
 			}
 		}
 		// update the now badly named PositionAtScanStart
 		if (positionAtScanStart != null) {
 			for (int i = 0; i < originalExternalTarget.length; i++) {
 				if (originalExternalTarget[i] != null) {
 					positionAtScanStart[i] = PositionConvertorFunctions.toDouble(originalExternalTarget[i]);
 				}
 			}
 		}
 
 		return externalTarget;
 		
 	}
 
 	/**
 	 * Populates scannableFields and scannableFieldByName.
 	 */
 	protected void createScannableForEachField() {
 		children = new ArrayList<ScannableField>();
 		scannableFieldByName = new HashMap<String, ScannableField>();
 		ScannableField field;
 		String name;
 		for (int i = 0; i < getInputNames().length; i++) {
 			name = getInputNames()[i];
 			field = new ScannableField(name, this, true, i);
 			children.add(field);
 			scannableFieldByName.put(name, field);
 		}
 		for (int i = 0; i < getExtraNames().length; i++) {
 			int fieldIndex = i + getInputNames().length;
 			name = getExtraNames()[i];
 			field = new ScannableField(getExtraNames()[i], this, false, fieldIndex);
 			children.add(field);
 			scannableFieldByName.put(getExtraNames()[i], field);
 		}
 	}
 
 
 	/**
 	 * Returns the ScannableField with the given name. As PseudoDevice does not extend PyObject, it is an 'old style'
 	 * class and the more appropriate __getattribute__ method won't get called. The problem with __getattr__ is that
 	 * defined methods (such as Scannable.a()) will block it. Then again, maybe this is not a problem!
 	 * 
 	 * @param name
 	 */
 	public ScannableField __getattr__(String name) {
 		ScannableField field = getFieldScannable(name);
 		if (field == null) {
 			throw new PyException(Py.AttributeError, "");
 		}
 		return field;
 	}
 
 	/**
 	 * Returns the ScannableField with the given name.
 	 * 
 	 * @param name
 	 * @return the Scannablefield, or null if it could not be found.
 	 */
 	public ScannableField getFieldScannable(String name) {
 		return scannableFieldByName.get(name);
 	}
 
 	/**
 	 * Override this if there is a more efficient way for your Scannable to get the value of a field than than by
 	 * calling getPosition and picking out the ith object.
 	 * 
 	 * @param index
 	 * @return position of ith field
 	 * @throws DeviceException
 	 */
 	public Object getFieldPosition(int index) throws DeviceException {
 		Object wholePosition = getPosition();
 		Object[] wholePositionArray = PositionConvertorFunctions.toObjectArray(wholePosition);
 		if ((wholePositionArray.length == 1) & (index != 0)) {
 			throw new IllegalArgumentException("The scannables position has only one element. Index must be zero not: "
 					+ index);
 		}
 		return wholePositionArray[index];
 	}
 
 	@Override
 	public void addChildToMove(ICoordinatedChildScannable element) {
 		coordinatedScannableComponent.addChildToMove(element);
 	}
 
 	@Override
 	public boolean isTargeting() {
 		return coordinatedScannableComponent.isTargeting();
 	}
 
 	@Override
 	public void setChildTarget(ICoordinatedChildScannable element, Object position) throws DeviceException {
 		coordinatedScannableComponent.setChildTarget(element, position);
 	}
 
 	public void setMembers(List<ICoordinatedChildScannable> members) {
 		coordinatedScannableComponent.setMembers(members);
 	}
 
 	/// ContinuouslyScannableViaController ///
 
 	@Override
 	public void setOperatingContinuously(boolean b) {
 		operatingContinuousely = b;
 	}
 
 	@Override
 	public boolean isOperatingContinously() {
 		return operatingContinuousely;
 	}
 
 	@Override
 	public ContinuousMoveController getContinuousMoveController() {
 		return controller;
 	}
 	
 	public void setContinuousMoveController(TrajectoryMoveController controller) {
 		this.controller = controller;
 	}
 
 //	@Override
 //	public void asynchronousMoveTo(Object position) throws DeviceException {
 //		if (isOperatingContinously()) {
 //			controller.addPoint(PositionConvertorFunctions.toDoubleArray(
 //					externalToInternal(gda.device.scannable.ScannableUtils.objectToArray(position))));
 //		} else {
 //			super.asynchronousMoveTo(position);
 //		}
 //	}
 	
 //	@Override
 //	public Object getPosition() throws DeviceException {
 //		if (isOperatingContinously()) {
 //			return internalToExternal(controller.getLastPointAdded());
 //		}
 //		return super.getPosition();
 //	}
 	
 	@Override
 	public boolean isBusy() throws DeviceException {
 		if (isOperatingContinously()) {
 			return controller.isMoving();
 		}
 		return super.isBusy();
 	}	
 	
 	@Override
 	public void waitWhileBusy() throws DeviceException, InterruptedException {
 		if (isOperatingContinously()) {
 			controller.waitWhileMoving();
 		} else {
 			super.waitWhileBusy();
 		}
 	}
 	
 	@Override
 	public void stop() throws DeviceException {
 		positionAtScanStart = null;
 		super.stop();
 		if (isOperatingContinously()) {
 			try {
 				controller.stopAndReset();
 			} catch (InterruptedException e) {
 				throw new DeviceException("InterruptedException while stopping and resetting " + controller.getName());
 			}
 		}
 	}
 
 	@Override
 	public Object getPositionWhileMovingContinuousely(ICoordinatedScannableGroupChildScannable childScannable) throws DeviceException {
 		int index = children.indexOf(childScannable);
 		return toObjectArray(getPosition())[index];
 	}
 }
