 /**
  * 
  */
 package de.xwic.cube.impl;
 
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
import sun.rmi.runtime.Log;
 import de.xwic.cube.DimensionBehavior;
 import de.xwic.cube.ICell;
 import de.xwic.cube.ICellListener;
 import de.xwic.cube.ICellProvider;
 import de.xwic.cube.ICube;
 import de.xwic.cube.ICubeListener;
 import de.xwic.cube.IDimension;
 import de.xwic.cube.IDimensionElement;
 import de.xwic.cube.IDimensionResolver;
 import de.xwic.cube.IKeyProvider;
 import de.xwic.cube.IMeasure;
 import de.xwic.cube.IMeasureFunctionQuerySupport;
 import de.xwic.cube.IQuery;
 import de.xwic.cube.Key;
 import de.xwic.cube.StorageException;
 import de.xwic.cube.IDataPool.CubeType;
 import de.xwic.cube.event.CellAggregatedEvent;
 import de.xwic.cube.event.CellValueChangedEvent;
 
 /**
  * @author Florian Lippisch
  */
 public class Cube extends Identifyable implements ICube, Externalizable {
 
 	private static final long serialVersionUID = -970760385292258831L;
 	protected DataPool dataPool;
 	protected Map<String, IDimension> dimensionMap = new LinkedHashMap<String, IDimension>();
 	protected Map<String, IMeasure> measureMap = new LinkedHashMap<String, IMeasure>();
 	protected ICellStore data;
 
 	protected List<ICubeListener> cubeListeners = new ArrayList<ICubeListener>();
 	protected DimensionBehavior[] dimensionBehavior = null;
 
 	protected boolean allowSplash = true;
 
 	protected IDimensionResolver dimensionResolver = new DefaultDimensionResolver(this);
 	protected IKeyProvider keyProvider = new DefaultKeyProvider();
 	protected ICellProvider cellProvider = new DefaultCellProvider();
 
 	protected boolean serializeData = false;
 
 	// Commons log
 	private transient Log log;
 	{
 		log = LogFactory.getLog(getClass());
 	}
 
 	/**
 	 * INTERNAL: This constructor is used by the serialization mechanism.
 	 */
 	public Cube() {
 		super(null);
 	}
 
 	/**
 	 * @param dataPool
 	 * @param key
 	 * @param measures
 	 * @param dimensions
 	 */
 	public Cube(DataPool dataPool, String key, IDimension[] dimensions, IMeasure[] measures) {
 		super(key);
 		this.dataPool = dataPool;
 
 		if (dimensions == null) {
 			throw new NullPointerException("dimensions must not be null");
 		}
 		if (measures == null) {
 			throw new NullPointerException("measures must not be null");
 		}
 
 		int capacity = 1;
 		for (IDimension dimension : dimensions) {
 			if (dimensionMap.containsKey(dimension.getKey())) {
 				throw new IllegalArgumentException("The list of dimensions contains a duplicate entry: "
 						+ dimension.getKey());
 			}
 			dimensionMap.put(dimension.getKey(), dimension);
 			capacity = capacity * (dimension.totalSize() + 1);
 		}
 
 		for (IMeasure measure : measures) {
 			if (measureMap.containsKey(measure.getKey())) {
 				throw new IllegalArgumentException("The list of measures contains a duplicate entry: "
 						+ measure.getKey());
 			}
 			measureMap.put(measure.getKey(), measure);
 		}
 
 		dimensionBehavior = new DimensionBehavior[dimensions.length];
 		for (int i = 0; i < dimensionBehavior.length; i++) {
 			dimensionBehavior[i] = DimensionBehavior.DEFAULT;
 		}
 
 		data = createCellStore();
 
 	}
 
 	/**
 	 * @return
 	 */
 	protected ICellStore createCellStore() {
 		return new MapCellStore(dimensionMap.size(), 500);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getDimensions()
 	 */
 	public Collection<IDimension> getDimensions() {
 		return Collections.unmodifiableCollection(dimensionMap.values());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getMeasures()
 	 */
 	public Collection<IMeasure> getMeasures() {
 		return Collections.unmodifiableCollection(measureMap.values());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#remove()
 	 */
 	public void remove() {
 		dataPool.removeCube(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#close()
 	 */
 	@Override
 	public void close() throws StorageException {
 		// Nothing to do.
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getCell(de.xwic.cube.Key)
 	 */
 	public ICell getCell(Key key) {
 		return getCell(key, false);
 	}
 
 	/**
 	 * Returns the cell for specified key. Non existing cell is created when
 	 * createNew flag is set.
 	 * 
 	 * @param key
 	 * @param createNew
 	 * @return
 	 */
 	public ICell getCell(Key key, boolean createNew) {
 		ICell cell = data.get(key);
 		if (cell == null && createNew) {
 			cell = createNewCell(key, measureMap.size());
 			Key newKey = key.clone();
 			data.put(newKey, cell);
 			// check serialization mode
 			/*
 			 * if (!serializeData && key.getClass() != Key.class) {
 			 * serializeData = true; }
 			 */
 		}
 		return cell;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#setCellValue(de.xwic.cube.Key,
 	 * de.xwic.cube.impl.Measure, double)
 	 */
 	public int setCellValue(Key key, IMeasure measure, double value) {
 
 		if (!allowSplash && !key.isLeaf()) {
 			// splash not implemented yet.
 			throw new IllegalArgumentException("The key must contain only leafs.");
 		}
 
 		if (measure.isFunction()) {
 			throw new IllegalArgumentException(
 					"The specified measure is a function. Functional measures can not hold data.");
 		}
 
 		return splashAndWriteValue(0, key, getMeasureIndex(measure), value);
 
 	}
 
 	/**
 	 * Add a value to the existing value in the specified cell.
 	 * 
 	 * @param key
 	 * @param measure
 	 * @param value
 	 * @return
 	 */
 	public int addCellValue(Key key, IMeasure measure, double value) {
 
 		if (!allowSplash && !key.isLeaf()) {
 			// splash not supported
 			StringBuilder nonLeafs = new StringBuilder();
 			for (IDimensionElement elm : key.getDimensionElements()) {
 				if (!elm.isLeaf()) {
 					if (nonLeafs.length() > 0) {
 						nonLeafs.append(", ");
 					}
 					nonLeafs.append(elm.getID());
 				}
 			}
 			throw new IllegalArgumentException("The key must contain only leafs (check " + nonLeafs + " of " + key
 					+ ")");
 		}
 
 		int measureIndex = getMeasureIndex(measure);
 		ICell cell = getCell(key, true);
 
 		Double oldValue = cell.getValue(measureIndex);
 		double newValue = (oldValue != null ? value + oldValue.doubleValue() : value);
 
 		return splashAndWriteValue(0, key, measureIndex, newValue);
 
 	}
 
 	/**
 	 * Returns true if the key is either a leaf or if the non-leaf elements have
 	 * the NO_SPLASH behavior set.
 	 * 
 	 * @param key
 	 * @return
 	 */
 	protected boolean isLeafLikeKey(Key key) {
 		for (int i = 0, max = dimensionMap.size(); i < max; i++) {
 			IDimensionElement elm = key.getDimensionElement(i);
 			DimensionBehavior behavior = dimensionBehavior[i];
 			if (!elm.isLeaf() && !behavior.isFlagged(DimensionBehavior.FLAG_NO_SPLASH)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @param key
 	 * @param measure
 	 * @param value
 	 */
 	protected int splashAndWriteValue(int idx, Key key, int measureIndex, Double value) {
 
 		int cellsModified = 0;
 
 		if (isLeafLikeKey(key)) {
 			ICell cell = getCell(key, value != null);
 
 			if (cell == null) { // can only happens when the value is null too
 				// simply exit, because the data is null anyway
 				return 0;
 			}
 
 			Double oldValue = cell.getValue(measureIndex);
 			double newValue = value != null ? value.doubleValue() : 0.0d;
 			double diff = (oldValue != null ? newValue - oldValue.doubleValue() : newValue);
 
 			// System.out.println("Start ApplyChange to: " + key);
 			cellsModified += applyValueChange(0, key, measureIndex, diff);
 
 		} else {
 			IDimensionElement elmCurr = key.getDimensionElement(idx);
 			if (!elmCurr.isLeaf() && !dimensionBehavior[idx].isFlagged(DimensionBehavior.FLAG_NO_SPLASH)) {
 				Key subKey = key.clone();
 				// splash and iterate over children
 				if (value != null) {
 					double total = elmCurr.getElementsTotalWeight();
 					double atom = total != 0 ? value / total : 0;
 					double rest = value;
 					for (Iterator<IDimensionElement> it = elmCurr.getDimensionElements().iterator(); it.hasNext();) {
 						IDimensionElement de = it.next();
 						double elmValue = it.hasNext() ? de.getWeight() * atom : rest;
 						rest -= elmValue;
 						subKey.setDimensionElement(idx, de);
 						cellsModified += splashAndWriteValue(idx, subKey, measureIndex, elmValue);
 					}
 				} else {
 					for (Iterator<IDimensionElement> it = elmCurr.getDimensionElements().iterator(); it.hasNext();) {
 						IDimensionElement de = it.next();
 						subKey.setDimensionElement(idx, de);
 						cellsModified += splashAndWriteValue(idx, subKey, measureIndex, null);
 					}
 				}
 			} else {
 				cellsModified += splashAndWriteValue(idx + 1, key, measureIndex, value);
 			}
 		}
 
 		// remove empty cells
 		if (value == null) {
 			removeEmptyCells(key, measureIndex);
 		}
 
 		return cellsModified;
 	}
 
 	/**
 	 * Remove empty non-leaf cells during clear(...)
 	 * 
 	 * @param key
 	 * @param measureIndex
 	 */
 	protected void removeEmptyCells(Key key, int measureIndex) {
 		ICell cell = getCell(key, false);
 		if (cell != null) {
 			cell.setValue(measureIndex, null);
 			if (cell.isEmpty()) {
 				data.remove(key);
 			}
 		}
 	}
 
 	/**
 	 * @param i
 	 * @param key
 	 * @param measure
 	 * @param diff
 	 */
 	protected int applyValueChange(int idx, Key key, int measureIndex, double diff) {
 
 		int cellsModified = 0;
 		if (idx == dimensionMap.size()) {
 			ICell cell = getCell(key, true);
 			Double oldValue = cell.getValue(measureIndex);
 			cell.setValue(measureIndex, oldValue != null ? oldValue.doubleValue() + diff : diff);
 			cellsModified = 1;
 			// invoke CellValueChangedListener
 			onCellValueChanged(new CellValueChangedEvent(this, key, cell, measureIndex, diff));
 			// System.out.println("Changed Cell " + key + " from " + oldValue +
 			// " to " + cell.getValue(measure));
 		} else {
 			Key myCursor = key.clone();
 			IDimensionElement element = key.getDimensionElement(idx);
 			do {
 				cellsModified += applyValueChange(idx + 1, myCursor, measureIndex, diff);
 				if (dimensionBehavior[idx].isFlagged((DimensionBehavior.FLAG_NO_AGGREGATION))) {
 					break; // do not "aggregate" this dimension..
 				} else {
 					if (element != null) {
 						element = element.getParent();
 						myCursor.setDimensionElement(idx, element);
 					}
 				}
 			} while (element != null);
 		}
 		return cellsModified;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getCellValue(de.xwic.cube.Key,
 	 * de.xwic.cube.IMeasure)
 	 */
 	public Double getCellValue(Key key, IMeasure measure) {
 		ICell cell = getCell(key);
 		if (measure.isFunction()) {
 			return measure.getFunction().computeValue(this, key, cell, measure);
 		}
 		if (cell != null) {
 			return cell.getValue(getMeasureIndex(measure));
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getCellValue(de.xwic.cube.IQuery,
 	 * de.xwic.cube.IMeasure)
 	 */
 	public Double getCellValue(IQuery query, IMeasure measure) {
 
 		if (measure.isFunction() && measure.getFunction() instanceof IMeasureFunctionQuerySupport) {
 			return ((IMeasureFunctionQuerySupport) measure.getFunction()).computeValue(this, query);
 		} else {
 			boolean isNull = true;
 			double total = 0.0d;
 			for (Key key : query.createKeys()) {
 				Double value = getCellValue(key, measure);
 				if (value != null) {
 					total += value.doubleValue();
 					isNull = false;
 				}
 			}
 			return isNull ? null : new Double(total);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#reset()
 	 */
 	public void reset() {
 		clear();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#clear()
 	 */
 	public void clear() {
 		data.clear();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#clear(de.xwic.cube.IMeasure)
 	 */
 	public void clear(IMeasure measure) {
 		int mIdx = getMeasureIndex(measure);
 		for (Iterator<Key> it = data.getKeyIterator(); it.hasNext();) {
 			Key key = it.next();
 			ICell cell = data.get(key);
 			cell.setValue(mIdx, null);
 			if (cell.isEmpty()) {
 				it.remove();
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#clear(de.xwic.cube.IMeasure, de.xwic.cube.Key)
 	 */
 	public void clear(IMeasure measure, Key key) {
 
 		// start removing the value
 		int measureIndex = getMeasureIndex(measure);
 		splashAndWriteValue(0, key, measureIndex, null);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#clear(de.xwic.cube.Key)
 	 */
 	public void clear(Key key) {
 		// TODO This implementation is simple, but needs to run for each
 		// measure. Could get optimized...
 		for (IMeasure measure : measureMap.values()) {
 			clear(measure, key);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#beginMassUpdate()
 	 */
 	public void beginMassUpdate() {
 		// Default Cube implementation does nothing.
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#writeFinished()
 	 */
 	public void massUpdateFinished() {
 		// Default Cube implementation does nothing.
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#createKey()
 	 */
 	public Key createKey() {
 		IDimensionElement[] elements = new IDimensionElement[dimensionMap.size()];
 		// prefill with the dimension elements
 		int idx = 0;
 		for (IDimension dim : dimensionMap.values()) {
 			elements[idx++] = dim;
 		}
 		return createNewKey(elements);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#createKey(de.xwic.cube.IDimensionElement[])
 	 */
 	public Key createKey(IDimensionElement... elements) {
 		Key key = createKey();
 		for (IDimensionElement elm : elements) {
 			int idx = getDimensionIndex(elm.getDimension());
 			key.setDimensionElement(idx, elm);
 		}
 		return key;
 	}
 
 	/**
 	 * Creates a new Key instance by invoking
 	 * keyProvider.createNewKey(elements).
 	 * 
 	 * @param elements
 	 *            might be null (during deserialization)
 	 * @return
 	 */
 	protected Key createNewKey(IDimensionElement[] elements) {
 		return keyProvider.createNewKey(elements);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#createKey(de.xwic.cube.Key)
 	 */
 	public Key createKey(Key foreignKey) {
 		Key newKey = createKey();
 
 		for (IDimensionElement elm : foreignKey.getDimensionElements()) {
 			try {
 				int idx = getDimensionIndex(elm.getDimension());
 				newKey.setDimensionElement(idx, elm);
 			} catch (IllegalArgumentException iae) {
 				// do nothing -> the dimension does not exist and is skipped.
 			}
 		}
 
 		return newKey;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#createKey(java.lang.String)
 	 */
 	public Key createKey(String key) {
 
 		IDimensionElement[] elements = new IDimensionElement[dimensionMap.size()];
 		// prefill with the dimension elements
 		int idx = 0;
 		for (IDimension dim : dimensionMap.values()) {
 			elements[idx++] = dim;
 		}
 
 		idx = 0;
 		int pos = 0;
 		int start;
 		while ((start = key.indexOf('[', pos)) != -1) {
 			int end = key.indexOf(']', start);
 			if (end == -1) {
 				throw new IllegalArgumentException("Missing ending ]");
 			}
 			String part = key.substring(start + 1, end);
 			int idxDimSep = part.indexOf(':');
 			int idxDimPos;
 			String elmKeys;
 			IDimension dimension;
 			// does the key contain a dimension key?
 			if (idxDimSep == -1) { // no key given -> use index
 				dimension = elements[idx].getDimension();
 				idxDimPos = idx;
 				elmKeys = part;
 			} else { // found key -> look for dimension
 				String dimKey = part.substring(0, idxDimSep);
 				elmKeys = part.substring(idxDimSep + 1);
 				dimension = dimensionMap.get(dimKey);
 				if (dimension == null) {
 					throw new IllegalArgumentException("The specified dimension '" + dimKey
 							+ "' does not exist in this cube.");
 				}
 
 				idxDimPos = -1;
 				for (int i = 0; i < elements.length; i++) {
 					if (elements[i].getDimension().equals(dimension)) {
 						idxDimPos = i;
 						break;
 					}
 				}
 				if (idxDimPos == -1) {
 					throw new IllegalArgumentException("The specified dimension '" + dimKey
 							+ "' exists but is no longer provided in the map!.");
 				}
 			}
 			if (!"*".equals(elmKeys)) { // specific key given.
 				int idxPathSep;
 				int idxPathStart = 0;
 				IDimensionElement dimElm = dimension;
 				do {
 					idxPathSep = elmKeys.indexOf('/', idxPathStart);
 					String elmKey;
 					if (idxPathSep == -1) {
 						elmKey = elmKeys.substring(idxPathStart);
 					} else {
 						elmKey = elmKeys.substring(idxPathStart, idxPathSep);
 					}
 					dimElm = dimElm.getDimensionElement(elmKey);
 					idxPathStart = idxPathSep + 1;
 				} while (idxPathSep != -1);
 				elements[idxDimPos] = dimElm;
 			}
 			pos = end;
 			idx++;
 		}
 		return createKey(elements);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#createQuery()
 	 */
 	public IQuery createQuery() {
 		return new Query(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#createQuery(java.lang.String)
 	 */
 	public IQuery createQuery(String query) {
 
 		return new Query(this, query);
 	}
 
 	public IQuery createQuery(Key key) {
 		Query query = new Query(this);
 		for (IDimensionElement el : key.getDimensionElements()) {
 			query.selectDimensionElements(el);
 		}
 		return query;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see de.xwic.cube.ICube#createQuery(de.xwic.cube.IQuery)
 	 */
 	public IQuery createQuery(IQuery query) {
 		IQuery result = new Query(this);
 		
 		Set<IDimension> selectedDims = query.getSelectedDimensions();
 		Collection<IDimension> cubeDims = this.getDimensions();
 		for (Iterator<IDimension> iterator = selectedDims.iterator(); iterator.hasNext();) {
 			IDimension dimension = (IDimension) iterator.next();
 			if(cubeDims.contains(dimension)){
 				Set<IDimensionElement> selElems  = query.getSelectedDimensionElements(dimension);
 				IDimensionElement[] arrayElem = selElems.toArray(new IDimensionElement[selElems.size()]);
 				result.selectDimensionElements(arrayElem);
 			}
 			
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getCellValue(java.lang.String,
 	 * de.xwic.cube.IMeasure)
 	 */
 	public Double getCellValue(String keyString, IMeasure measure) {
 		return getCellValue(createKey(keyString), measure);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getDimensionIndex(de.xwic.cube.IDimension)
 	 */
 	public int getDimensionIndex(IDimension dimVert) {
 		int idx = 0;
 		for (IDimension dim : dimensionMap.values()) {
 			if (dim.equals(dimVert)) {
 				return idx;
 			}
 			idx++;
 		}
 		throw new IllegalArgumentException("The specified dimension " + dimVert.toString() + " is not used in cube "
 				+ this + " .");
 	}
 
 	/**
 	 * Returns the measure index.
 	 * 
 	 * @param measure
 	 * @return
 	 */
 	public int getMeasureIndex(IMeasure measure) {
 		int idx = 0;
 		// TODO why not use a helper HashMap for that, is this loop faster?
 		for (IMeasure m : measureMap.values()) {
 			if (m.equals(measure)) {
 				return idx;
 			}
 			idx++;
 		}
 		throw new IllegalArgumentException("The specified " + measure + " is not used in this cube.");
 	}
 
 	/**
 	 * @return the dataPool
 	 */
 	public DataPool getDataPool() {
 		return dataPool;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#size()
 	 */
 	public int getSize() {
 		return data.size();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#forEachCell(de.xwic.cube.ICellListener)
 	 */
 	public void forEachCell(ICellListener listener) {
 		for (Iterator<Key> it = data.getKeyIterator(); it.hasNext();) {
 			Key key = it.next();
 			if (!listener.onCell(key, data.get(key))) {
 				return;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
 	 */
 	@SuppressWarnings("unchecked")
 	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
 
 		int version = in.readInt();
 		if (version > 6) {
 			throw new IOException("Can not deserialize cube -> data file version is " + version
 					+ ", but expected between 1..5");
 		}
 		key = (String) in.readObject();
 		title = (String) in.readObject();
 		allowSplash = in.readBoolean();
 		dataPool = (DataPool) in.readObject();
 		dimensionMap = (Map<String, IDimension>) in.readObject();
 		measureMap = (Map<String, IMeasure>) in.readObject();
 
 		log.debug("Restoring Cube '" + key + "' ...");
 
 		dimensionBehavior = new DimensionBehavior[dimensionMap.size()];
 		for (int i = 0; i < dimensionBehavior.length; i++) {
 			dimensionBehavior[i] = DimensionBehavior.DEFAULT;
 		}
 
 		if (version > 1) {
 			cubeListeners = (List<ICubeListener>) in.readObject();
 		}
 
 		if (version > 2) {
 			dimensionResolver = (IDimensionResolver) in.readObject();
 
 			if (version > 3) {
 				keyProvider = (IKeyProvider) in.readObject();
 				cellProvider = (ICellProvider) in.readObject();
 			}
 
 			if (version > 4) {
 				for (int i = 0; i < dimensionBehavior.length; i++) {
 					dimensionBehavior[i] = (DimensionBehavior) in.readObject();
 				}
 			}
 
 			serializeData = in.readBoolean();
 		}
 
 		// read data
 		if (!serializeData) {
 			// optimized data read
 			data = createCellStore();
 			data.restore(in, keyProvider);
 		} else {
 			// customer Key implementation
 			data = (ICellStore) in.readObject();
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
 	 */
 	public void writeExternal(ObjectOutput out) throws IOException {
 
 		// serialize -> write the cube data.
 		int version = 6;
 		out.writeInt(version); // version number
 		out.writeObject(key);
 		out.writeObject(title);
 		out.writeBoolean(allowSplash);
 		out.writeObject(dataPool);
 		out.writeObject(dimensionMap);
 		out.writeObject(measureMap);
 		out.writeObject(cubeListeners);
 		out.writeObject(dimensionResolver);
 		out.writeObject(keyProvider);
 		out.writeObject(cellProvider);
 
 		for (int i = 0; i < dimensionBehavior.length; i++) {
 			out.writeObject(dimensionBehavior[i]);
 		}
 
 		// data serialization mode
 		out.writeBoolean(serializeData);
 
 		// write data...
 		if (!serializeData) {
 			// default, optimized data serialization
 			data.serialize(out);
 		} else {
 			// customer Key implementation used, serialize data
 			out.writeObject(data);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#printCacheProfile(java.io.PrintStream)
 	 */
 	public void printCacheProfile(PrintStream out) {
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getCubeListeners()
 	 */
 	public List<ICubeListener> getCubeListeners() {
 		return cubeListeners;
 	}
 
 	/**
 	 * Invoke all cell value changed listeners
 	 * 
 	 * @param event
 	 */
 	protected void onCellValueChanged(CellValueChangedEvent event) {
 		if (cubeListeners.size() == 0) {
 			return;
 		}
 		for (ICubeListener listener : cubeListeners) {
 			listener.onCellValueChanged(event);
 		}
 	}
 
 	/**
 	 * Invoke all cell aggregated listeners
 	 * 
 	 * @param event
 	 */
 	protected void onCellAggregated(CellAggregatedEvent event) {
 		if (cubeListeners.size() == 0) {
 			return;
 		}
 
 		for (ICubeListener listener : cubeListeners) {
 			listener.onCellAggregated(event);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.impl.Identifyable#toString()
 	 */
 	@Override
 	public String toString() {
 		return getKey() + " (Size:" + data.size() + ")";
 	}
 
 	public void replace(ICube oldCube) {
 		if (this.dataPool != oldCube.getDataPool()) {
 			throw new IllegalArgumentException("Cubes do not share the same DataPool");
 		}
 		// remove new cube
 		dataPool.removeCube(this);
 		// set target cubeKey
 		this.setKey(oldCube.getKey());
 		// replace cube in dataPool
 		dataPool.replaceCube(oldCube, this);
 		// clear oldCube, TODO make old cube invalid and throw exception when it
 		// is used
 		oldCube.clear();
 	}
 
 	/**
 	 * @return the dimensionResolver
 	 */
 	public IDimensionResolver getDimensionResolver() {
 		return dimensionResolver;
 	}
 
 	/**
 	 * @param dimensionResolver
 	 *            the dimensionResolver to set
 	 */
 	public void setDimensionResolver(IDimensionResolver dimensionResolver) {
 		this.dimensionResolver = dimensionResolver;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#isAllowSplash()
 	 */
 	public boolean isAllowSplash() {
 		return allowSplash;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#setAllowSplash(boolean)
 	 */
 	public void setAllowSplash(boolean allowSplash) {
 		this.allowSplash = allowSplash;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getCubeType()
 	 */
 	public CubeType getCubeType() {
 		return CubeType.DEFAULT;
 	}
 
 	/**
 	 * Returns true if Map<Key, Cell> data is serialized or just the content
 	 * optimized.
 	 * 
 	 * @return the serializeData
 	 */
 	public boolean isSerializeData() {
 		return serializeData;
 	}
 
 	/**
 	 * When set to true Map<Key, Cell> data is serialized otherwise just the
 	 * content optimized.
 	 * 
 	 * @param serializeData
 	 *            the serializeData to set
 	 */
 	public void setSerializeData(boolean serializeData) {
 		this.serializeData = serializeData;
 	}
 
 	/**
 	 * @return the keyProvider
 	 */
 	public IKeyProvider getKeyProvider() {
 		return keyProvider;
 	}
 
 	/**
 	 * @param keyProvider
 	 *            the keyProvider to set
 	 */
 	public void setKeyProvider(IKeyProvider keyProvider) {
 		this.keyProvider = keyProvider;
 	}
 
 	protected ICell createNewCell(Key key, int measureSize) {
 		return cellProvider.createCell(key, measureSize);
 	}
 
 	/**
 	 * @return the cellProvider
 	 */
 	public ICellProvider getCellProvider() {
 		return cellProvider;
 	}
 
 	/**
 	 * @param cellProvider
 	 *            the cellProvider to set
 	 */
 	public void setCellProvider(ICellProvider cellProvider) {
 		this.cellProvider = cellProvider;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#setDimensionBehavior(de.xwic.cube.IDimension,
 	 * de.xwic.cube.DimensionBehavior)
 	 */
 	@Override
 	public void setDimensionBehavior(IDimension dimension, DimensionBehavior behavior) {
 		int idx = getDimensionIndex(dimension);
 		dimensionBehavior[idx] = behavior;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.xwic.cube.ICube#getDimensionBehaivior(de.xwic.cube.IDimension)
 	 */
 	@Override
 	public DimensionBehavior getDimensionBehaivior(IDimension dimension) {
 		int idx = getDimensionIndex(dimension);
 		return dimensionBehavior[idx];
 	}
 
 }
