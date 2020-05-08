 package org.dawnsci.persistence.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.vecmath.Matrix3d;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5Datatype;
 
 import org.dawb.common.services.IPersistentFile;
 import org.dawb.common.util.eclipse.BundleUtils;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.HierarchicalDataFileUtils;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.Nexus;
 import org.dawb.hdf5.nexus.NexusUtils;
 import org.dawnsci.io.h5.H5LazyDataset;
 import org.dawnsci.io.h5.H5Utils;
 import org.dawnsci.persistence.Activator;
 import org.dawnsci.persistence.function.FunctionBean;
 import org.dawnsci.persistence.function.FunctionBeanConverter;
 import org.dawnsci.persistence.roi.ROIBean;
 import org.dawnsci.persistence.roi.ROIBeanConverter;
 import org.dawnsci.persistence.util.PersistenceUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionMetaReader;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /**
  * Implementation of IPersistentFile<br>
  * 
  * This class is internal and not supposed to be used out of this bundle.
  * 
  * @author wqk87977
  *
  */
 class PersistentFileImpl implements IPersistentFile{
 	private static final Logger logger = LoggerFactory.getLogger(PersistentFileImpl.class);
 	private IHierarchicalDataFile file;
 	private String filePath;
 	private final String ENTRY          = "/entry";
 	private final String DATA_ENTRY     = "/entry/data";
 	private final String HISTORY_ENTRY  = "/entry/history";
 	private final String MASK_ENTRY     = "/entry/mask";
 	private final String ROI_ENTRY      = "/entry/region";
 	private final String FUNCTION_ENTRY = "/entry/function";
 	private final String DIFFRACTIONMETADATA_ENTRY = "/entry/diffraction_metadata";
 
 	/**
 	 * Version of the API
 	 */
 	private final String VERSION_FILE = "/resource/persistence-version.txt";
 	/**
 	 * Site where the API is used
 	 */
 	private final String SITE_FILE = "/resource/persistence-site.txt";
 
 	/**
 	 * For save
 	 * @param file
 	 */
 	public PersistentFileImpl(IHierarchicalDataFile file) throws Exception{
 		this.file = file;
 		this.filePath = file.getPath();
 		// set the site and version
 		String sitePath = "", versionPath = "";
 		if(Activator.getContext() == null){
 			sitePath = System.getProperty("user.dir")+SITE_FILE;
 			versionPath = System.getProperty("user.dir")+VERSION_FILE;
 		} else {
 			sitePath = BundleUtils.getBundleLocation(Activator.getContext().getBundle()).getAbsolutePath()+SITE_FILE;
 			versionPath = BundleUtils.getBundleLocation(Activator.getContext().getBundle()).getAbsolutePath()+VERSION_FILE;
 		}
 		setSite(PersistenceUtils.readFile(sitePath));
 		setVersion(PersistenceUtils.readFile(versionPath));
 	}
 
 	/**
 	 * For read
 	 * @param filePath
 	 */
 	public PersistentFileImpl(String filePath) {
 		this.filePath = filePath;
 		try {
 			this.file = HierarchicalDataFactory.getReader(filePath);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setMasks(Map<String, ? extends IDataset> masks) throws Exception {
 		writeH5Mask(masks);
 	}
 
 	@Override
 	public void addMask(String name, IDataset mask, IMonitor mon) throws Exception{
 
 		AbstractDataset id = DatasetUtils.cast((BooleanDataset)mask, AbstractDataset.INT8);
 		//check if parent group exists
 		Group parent = (Group)file.getData(MASK_ENTRY);
 		if(parent == null) parent = createParentEntry(MASK_ENTRY);
 		final Datatype datatype = H5Utils.getDatatype(id);
 		long[] shape = H5Utils.getLong(id.getShape());
 		final Dataset dataset = file.replaceDataset(name, datatype, shape, id.getBuffer(), parent);
 		file.setNexusAttribute(dataset, Nexus.SDS);
 	}
 
 	@Override
 	public void setData(IDataset data) throws Exception {
 		writeH5Data(data, null, null);
 	}
 	
 	@Override
 	public void setHistory(IDataset... sets) throws Exception {
 		
 		if(file == null) file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = createParentEntry(HISTORY_ENTRY);
 
 		int index = 0;
 		for (IDataset data : sets) {
 			index++;
 			if(data != null){
 
 				String dataName = !data.getName().equals("") ? data.getName() : "history"+index;
 				final Datatype      datatype = H5Utils.getDatatype(data);
 				long[] shape = H5Utils.getLong(data.getShape());
 
 				final Dataset dataset = file.replaceDataset(dataName,  datatype, shape, ((AbstractDataset)data).getBuffer(), parent);
 				file.setNexusAttribute(dataset, Nexus.SDS);
 			}
 		}
 	}
 
 	@Override
 	public void setAxes(List<? extends IDataset> axes) throws Exception{
 		writeH5Data(null, axes.get(0), axes.get(1));
 	}
 
 	@Override
 	public void setROIs(Map<String, IROI> rois) throws Exception {
 		writeH5Rois(rois);
 	}
 
 	@Override
 	public void addROI(String name, IROI roiBase) throws Exception {
 
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = createParentEntry(ROI_ENTRY);
 
 		// JSON serialisation
 		GsonBuilder builder = new GsonBuilder();
 		//TODO: serialiser to be worked on...
 		//builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
 		Gson gson = builder.create();
 
 		writeRoi(file, parent, name, roiBase, gson);
 	}
 
 	@Override
 	public void setRegionAttribute(String regionName, String attributeName, String attributeValue) throws Exception {
 		if ("JSON".equals(attributeName)) throw new Exception("Cannot override the JSON attribute!");
 		final HObject node = file.getData(ROI_ENTRY+"/"+regionName);
 		file.setAttribute(node, attributeName, attributeValue);
 	}
 
 	@Override
 	public String getRegionAttribute(String regionName, String attributeName) throws Exception{
 		return file.getAttributeValue(ROI_ENTRY+"/"+regionName+"@"+attributeName);
 	}
 
 	/**
 	 * Used to set the version of the API
 	 * @param version
 	 * @throws Exception
 	 */
 	private void setVersion(String version) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		//check if parent group exists
 		Group parent = (Group)file.getData(ENTRY);
 		if(parent == null) parent = createParentEntry(ENTRY);
 		file.setAttribute(parent, "Version", version);
 	}
 
 	@Override
 	public void setSite(String site) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		//check if parent group exists
 		Group parent = (Group)file.getData(ENTRY);
 		if(parent == null) parent = createParentEntry(ENTRY);
 		file.setAttribute(parent, "Site", site);
 	}
 
 	@Override
 	public ILazyDataset getData(String dataName, IMonitor mon) throws Exception{
 		ILazyDataset data = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		dataName = !dataName.equals("") ? dataName : "data";
 		data = readH5Data(dh, dataName, DATA_ENTRY);
 
 		return data;
 	}
 
 	/**
 	 * Method to set datasets which persist history
 	 * 
 	 * @param data
 	 * @throws Exception 
 	 */
 	@Override
 	public Map<String, ILazyDataset> getHistory(IMonitor mon) throws Exception {
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		Map<String, ILazyDataset> sets = new HashMap<String, ILazyDataset>(dh.size());
         for (String name : dh.getNames()) {
 			if (name.startsWith(HISTORY_ENTRY)) {
 				sets.put(name, dh.getLazyDataset(name));
 			}
 		}
         return sets;
 	}
 
 
 	@Override
 	public List<ILazyDataset> getAxes(String xAxisName, String yAxisName, IMonitor mon) throws Exception {
 		List<ILazyDataset> axes = new ArrayList<ILazyDataset>();
 		ILazyDataset xaxis = null, yaxis = null;
 
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		xAxisName = !xAxisName.equals("") ? xAxisName : "X Axis";
 		xaxis = readH5Data(dh, xAxisName, DATA_ENTRY);
 		if(xaxis != null)
 			axes.add(xaxis);
 		yAxisName = !yAxisName.equals("") ? yAxisName : "Y Axis";
 		yaxis = readH5Data(dh, yAxisName, DATA_ENTRY);
 		if(yaxis != null)
 			axes.add(yaxis);
 
 		return axes;
 	}
 
 	@Override
 	public BooleanDataset getMask(String maskName, IMonitor mon) throws Exception {
 		ShortDataset sdata = (ShortDataset)LoaderFactory.getDataSet(filePath, MASK_ENTRY+"/"+maskName, mon);
 		BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(sdata, AbstractDataset.BOOL);
 		return bd;
 	}
 
 	@Override
 	public Map<String, IDataset> getMasks(IMonitor mon) throws Exception {
 		Map<String, IDataset> masks = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		masks = readH5Masks(dh, mon);
 		return masks;
 	}
 
 	@Override
 	public IROI getROI(String roiName) throws Exception {
 		IROI roi = null;
 		roi = readH5ROI(roiName);
 
 		return roi;
 	}
 
 	@Override
 	public Map<String, IROI> getROIs(IMonitor mon) throws Exception {
 		Map<String, IROI> rois = null;
 		rois = readH5ROIs(mon);
 
 		return rois;
 	}
 
 	@Override
 	public List<String> getDataNames(IMonitor mon) throws Exception{
 		List<String> names = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		names = getNames(dh, DATA_ENTRY);
 
 		return names;
 	}
 
 	@Override
 	public List<String> getMaskNames(IMonitor mon) throws Exception{
 		List<String> names = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		names = getNames(dh, MASK_ENTRY);
 		return names;
 	}
 
 	@Override
 	public List<String> getROINames(IMonitor mon) throws Exception{
 		List<String> names = null;
 
 		IHierarchicalDataFile file = null;
 		try {
 			file      = HierarchicalDataFactory.getReader(getFilePath());
 			Group grp = (Group)file.getData(ROI_ENTRY);
 			if (grp==null) throw new Exception("Reading Exception: " +ROI_ENTRY+ " entry does not exist in the file " + filePath);
 
 			List<HObject> children =  grp.getMemberList();
 			if (names==null) names = new ArrayList<String>(children.size());
 			for (HObject hObject : children) {
 				names.add(hObject.getName());
 			}
         } finally {
         	if (file!=null) file.close();
         }
 		return names;
 	}
 
 	@Override
 	public void setDiffractionMetadata(IDiffractionMetadata metadata) throws Exception {
 		writeH5DiffractionMetadata(metadata);		
 	}
 
 	@Override
 	public IDiffractionMetadata getDiffractionMetadata(IMonitor mon) throws Exception {
 		//Reverse of the setMetadata.  Would be nice in the future to be able to use the
 		//LoaderFactory but work needs to be done on loading specific metadata from nexus
 		//files first
 
 		return readH5DiffractionMetadata(mon);	
 	}
 
 	@Override
 	public String getVersion() throws Exception {
 		return file.getAttributeValue(ENTRY+"@Version");
 	}
 
 	@Override
 	public String getSite() throws Exception {
 		return file.getAttributeValue(ENTRY+"@Site");
 	}
 	
 	@Override
 	public boolean containsData() {
 		return isEntry(DATA_ENTRY,null);
 	}
 
 	@Override
 	public boolean containsMask() {
 		return isEntry(MASK_ENTRY,null);
 	}
 
 	@Override
 	public boolean containsRegion() {
 		return isEntry(ROI_ENTRY,null);
 	}
 
 	@Override
 	public boolean containsDiffractionMetadata() {
 		return isEntry(DIFFRACTIONMETADATA_ENTRY,null);
 	}
 
 	@Override
 	public boolean containsFunction() {
 		return isEntry(FUNCTION_ENTRY,null);
 	}
 
 	private boolean isEntry(String entryPath, IMonitor mon)  {
 		HObject hOb = null;
 		try {
 			hOb = file.getData(entryPath);
 			return hOb != null;
 		} catch (Exception e) {
 			logger.debug("Error while reading file: "+ e);
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Method to write image data (and axis) to an HDF5 file given a specific path entry to save the data.
 	 * 
 	 * @param data
 	 * @param xAxisData
 	 * @param yAxisData
 	 * @throws Exception
 	 */
 	private void writeH5Data(final IDataset data, 
 							final IDataset xAxisData, 
 							final IDataset yAxisData) throws Exception {
 
 		if(file == null)
 			file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = createParentEntry(DATA_ENTRY);
 
 		if(data != null){
 
 			String dataName = !data.getName().equals("") ? data.getName() : "data";
 			final Datatype      datatype = H5Utils.getDatatype(data);
 			long[] shape = H5Utils.getLong(data.getShape());
 
 			final Dataset dataset = file.replaceDataset(dataName,  datatype, shape, ((AbstractDataset)data).getBuffer(), parent);
 			file.setNexusAttribute(dataset, Nexus.SDS);
 		}
 		if(xAxisData != null){
 			String xAxisName = !xAxisData.getName().equals("") ? xAxisData.getName() : "X Axis";
 			final Datatype      xDatatype = H5Utils.getDatatype(xAxisData);
			long[] shape = H5Utils.getLong(xAxisData.getShape());
 
 			final Dataset xDataset = file.replaceDataset(xAxisName,  xDatatype, shape, ((AbstractDataset)xAxisData).getBuffer(), parent);
 			file.setNexusAttribute(xDataset, Nexus.SDS);
 		}
 
 		if(yAxisData != null){
 			String yAxisName = !yAxisData.getName().equals("") ? yAxisData.getName() : "Y Axis";
 			final Datatype      yDatatype = H5Utils.getDatatype(yAxisData);
			long[] shape = H5Utils.getLong(yAxisData.getShape());
 
 			final Dataset yDataset = file.replaceDataset(yAxisName,  yDatatype, shape,((AbstractDataset)yAxisData).getBuffer(), parent);
 			file.setNexusAttribute(yDataset, Nexus.SDS);
 		}
 	}
 
 
 	/**
 	 * Method to write mask data to an HDF5 file given a specific path entry to save the data.
 	 * 
 	 * @param masks
 	 * @throws Exception
 	 */
 	private void writeH5Mask(final Map<String, ? extends IDataset> masks) throws Exception {
 
 		if(file == null)
 			file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = createParentEntry(MASK_ENTRY);
 
 		if (masks != null) {
 			Set<String> names = masks.keySet();
 
 			Iterator<String> it = names.iterator();
 			while(it.hasNext()){
 				String name = it.next();
 				BooleanDataset bd = (BooleanDataset)masks.get(name);
 				AbstractDataset id = DatasetUtils.cast(bd, AbstractDataset.INT8);
 				final Datatype datatype = H5Utils.getDatatype(id);
 				final long[] shape = new long[id.getShape().length];
 				for (int i = 0; i < shape.length; i++)
 					shape[i] = id.getShape()[i];
 
 				final Dataset dataset = file.replaceDataset(name, datatype, shape, id.getBuffer(), parent);
 				file.setNexusAttribute(dataset, Nexus.SDS);
 			}
 		}
 	}
 
 	/**
 	 * Method to write rois data to an HDF5 file given a specific path entry to save the data.<br>
 	 * The rois are serialised using GSON and are saved as JSON format in the HDF5 file.
 	 * 
 	 * @param rois
 	 * @throws Exception
 	 */
 	private void writeH5Rois(final Map<String, IROI> rois) throws Exception {
 
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = createParentEntry(ROI_ENTRY);
 
 		if (rois != null) {
 			// JSON serialisation
 			GsonBuilder builder = new GsonBuilder();
 			//TODO: serialiser to be worked on...
 			//builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
 			Gson gson = builder.create();
 
 			Iterator<String> it = rois.keySet().iterator();
 			while(it.hasNext()){
 				String name = it.next();
 				IROI roi = rois.get(name);
 				writeRoi(file, parent, name, roi, gson);
 			}
 
 		}
 	}
 
 	private HObject writeRoi(IHierarchicalDataFile file, 
 			Group   parent,
 			String  name,
 			IROI    roi,
 			Gson    gson) throws Exception {
 
 		ROIBean roibean = ROIBeanConverter.IROIToROIBean(name, roi);
 
 		long[] dims = {1};
 
 		String json = gson.toJson(roibean);
 
 		// we create the dataset
 		Dataset dat = file.replaceDataset(name, new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE), dims, new int[]{0}, parent);
 		// we set the JSON attribute
 		file.setAttribute(dat, "JSON", json);
 
 		return dat;
 	}
 
 	private Group createParentEntry(String fullEntry) throws Exception {
 		return HierarchicalDataFileUtils.createParentEntry(file, fullEntry, Nexus.DATA);
 	}
 
 	/**
 	 * Method to read image data (axes, masks, image) from an HDF5 file and returns an ILazyDataset
 
 	 * @return ILazyDataset
 	 * @throws Exception 
 	 */
 	private ILazyDataset readH5Data(DataHolder dh, String dataName, String dataEntry) throws Exception{
 		ILazyDataset ld = dh.getLazyDataset(dataEntry+"/"+dataName);
 		if(ld == null) throw new Exception("Reading Exception: " +dataEntry+ " entry does not exist in the file " + filePath);
 		return ld;
 	}
 
 	/**
 	 * Method to read mask data from an HDF5 file
 	 * @return BooleanDataset
 	 * @throws Exception 
 	 */
 	private BooleanDataset readH5Mask(DataHolder dh, String maskName) throws Exception{
 		ILazyDataset ld = dh.getLazyDataset(MASK_ENTRY+"/"+maskName);
 		if (ld instanceof H5LazyDataset) {
 			return (BooleanDataset)DatasetUtils.cast(((H5LazyDataset)ld).getCompleteData(null), AbstractDataset.BOOL);
 		} else {
 			return (BooleanDataset)DatasetUtils.cast(dh.getDataset(MASK_ENTRY+"/"+maskName), AbstractDataset.BOOL);
 		}
 	}
 
 	/**
 	 * Method to read mask data from an HDF5 file
 	 * @return Map<String, BooleanDataset>
 	 * @throws Exception 
 	 */
 	private Map<String, IDataset> readH5Masks(DataHolder dh, IMonitor mon) throws Exception{
 		Map<String, IDataset> masks = new HashMap<String, IDataset>();
 		List<String> names = getMaskNames(mon);
 
 		Iterator<String> it = names.iterator();
 		while (it.hasNext()) {
 			String name = (String) it.next();
 
 			ShortDataset sdata = (ShortDataset)LoaderFactory.getDataSet(filePath, MASK_ENTRY+"/"+name, mon);
 			BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(sdata, AbstractDataset.BOOL);
 			masks.put(name, bd);
 		}
 		return masks;
 	}
 
 	/**
 	 * Method to read roi data from an HDF5 file
 	 * 
 	 * @return ROIBase
 	 * @throws Exception 
 	 */
 	private IROI readH5ROI(String roiName) throws Exception{
 
 		GsonBuilder builder = new GsonBuilder();
 		//TODO: deserialiser to be worked on...
 		//builder.registerTypeAdapter(ROIBean.class, new ROIDeserializer());
 		Gson gson = builder.create();
 
 		String json = file.getAttributeValue(ROI_ENTRY+"/"+roiName+"@JSON");
 		if(json == null) throw new Exception("Reading Exception: " +ROI_ENTRY+ " entry does not exist in the file " + filePath);
 		// JSON deserialization
 		json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
 		ROIBean roibean = ROIBeanConverter.getROIBeanfromJSON(gson, json);
 
 		//convert the bean to roibase
 		IROI roi = ROIBeanConverter.ROIBeanToROIBase(roibean);
 
 		return roi;
 	}
 
 	/**
 	 * Method to read roi data from an HDF5 file
 	 * @return Map<String, ROIBase>
 	 * @throws Exception 
 	 */
 	private Map<String, IROI> readH5ROIs(IMonitor mon) throws Exception{
 		Map<String, IROI> rois = new HashMap<String, IROI>();
 		if(file == null)
 			file = HierarchicalDataFactory.getReader(filePath);
 		// JSON deserialization
 		GsonBuilder builder = new GsonBuilder();
 		//TODO: deserialiser to be worked on...
 		//builder.registerTypeAdapter(ROIBean.class, new ROIDeserializer());
 		Gson gson = builder.create();
 
 		List<String> names = getROINames(mon);
 		
 		if (names== null) return null;
 		
 		Iterator<String> it = names.iterator();
 		while (it.hasNext()) {
 			String name = (String) it.next();
 			String json = file.getAttributeValue(ROI_ENTRY+"/"+name+"@JSON");
 			json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
 			ROIBean roibean = ROIBeanConverter.getROIBeanfromJSON(gson, json);
 
 			//convert the bean to roibase
 			IROI roi = ROIBeanConverter.ROIBeanToROIBase(roibean);
 			rois.put(name, roi);
 		}
 
 		return rois;
 	}
 
 	/**
 	 * Method to retrieve all names in dataEntry
 	 * @param dataEntry
 	 * @param mon
 	 * @return List<String>
 	 * @throws Exception
 	 */
 	private List<String> getNames(DataHolder dh, String dataEntry) throws Exception{
 		List<String> nameslist = new ArrayList<String>();
 		String[] names = dh.getNames();
 		for (int i = 0; i < names.length; i++) {
 			if(names[i].startsWith(dataEntry)){
 				nameslist.add(names[i].substring(dataEntry.length()+1));
 			}
 		}
 		if (nameslist.isEmpty()) throw new Exception("Reading Exception: " +dataEntry+ " entry does not exist in the file " + filePath);
 
 		return nameslist;
 	}
 
 	@Override
 	public void close() {
 		try {
 			if (file != null) {
 				file.close();
 			}
 		} catch (Exception e) {
 			logger.debug("Cannot close " + filePath, e);
 		}
 	}
 
 	public IHierarchicalDataFile getFile(){
 		return file;
 	}
 
 	public String getFilePath(){
 		return filePath;
 	}
 
 	@Override
 	public boolean isRegionSupported(IROI roi) {
 		return ROIBeanConverter.isROISupported(roi);
 	}
 
 	@Override
 	public void setFunctions(Map<String, IFunction> functions) throws Exception {
 		writeH5Functions(functions);
 	}
 
 	@Override
 	public void addFunction(String name, IFunction function) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = createParentEntry(FUNCTION_ENTRY);
 
 		// JSON serialisation
 		GsonBuilder builder = new GsonBuilder();
 		Gson gson = builder.create();
 
 		writeFunction(file, parent, name, function, gson);
 
 	}
 
 	@Override
 	public AFunction getFunction(String functionName) throws Exception {
 		AFunction function = null;
 		function = readH5Function(functionName);
 		return function;
 	}
 
 	@Override
 	public Map<String, IFunction> getFunctions(IMonitor mon) throws Exception {
 		Map<String, IFunction> functions = null;
 		functions = readH5Functions(mon);
 		return functions;
 	}
 
 	@Override
 	public List<String> getFunctionNames(IMonitor mon) throws Exception {
 		List<String> names = null;
 
 		IHierarchicalDataFile file = null;
 		try {
 			file      = HierarchicalDataFactory.getReader(getFilePath());
 			Group grp = (Group)file.getData(FUNCTION_ENTRY);
 			if (grp==null) throw new Exception("Reading Exception: " +FUNCTION_ENTRY+ " entry does not exist in the file " + filePath);
 
 			List<HObject> children =  grp.getMemberList();
 			if (names==null) names = new ArrayList<String>(children.size());
 			for (HObject hObject : children) {
 				names.add(hObject.getName());
 			}
         } finally {
         	if (file!=null) file.close();
         }
 		return names;
 	}
 
 	/**
 	 * Method to write function data to an HDF5 file given a specific path entry to save the data.<br>
 	 * The rois are serialised using GSON and are saved as JSON format in the HDF5 file.
 	 * 
 	 * @param functions
 	 * @throws Exception
 	 */
 	private void writeH5Functions(final Map<String, IFunction> functions) throws Exception {
 
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = createParentEntry(FUNCTION_ENTRY);
 
 		if (functions != null) {
 			// JSON serialisation
 			GsonBuilder builder = new GsonBuilder();
 			Gson gson = builder.create();
 
 			Iterator<String> it = functions.keySet().iterator();
 			while(it.hasNext()){
 				String name = it.next();
 				IFunction roi = functions.get(name);
 				writeFunction(file, parent, name, roi, gson);
 			}
 		}
 	}
 
 	private HObject writeFunction(IHierarchicalDataFile file, Group parent,
 			String name, IFunction function, Gson gson) throws Exception {
 
 		FunctionBean fBean = FunctionBeanConverter.AFunctionToFunctionBean(name, (AFunction)function);
 
 		long[] dims = {1};
 
 		String json = gson.toJson(fBean);
 
 		// we create the dataset
 		Dataset dat = file.replaceDataset(name, new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE), dims, new int[]{0}, parent);
 		// we set the JSON attribute
 		file.setAttribute(dat, "JSON", json);
 
 		return dat;
 	}
 
 	/**
 	 * Method to read function data from an HDF5 file
 	 * 
 	 * @return AFunction
 	 * @throws Exception 
 	 */
 	private AFunction readH5Function(String functionName) throws Exception{
 
 		String json = file.getAttributeValue(FUNCTION_ENTRY+"/"+functionName);
 		if(json == null) throw new Exception("Reading Exception: " +FUNCTION_ENTRY+ " entry does not exist in the file " + filePath);
 		// JSON deserialization
 		GsonBuilder builder = new GsonBuilder();
 		Gson gson = builder.create();
 		FunctionBean fBean = FunctionBeanConverter.getFunctionBeanfromJSON(gson, json);
 		//convert the bean to AFunction
 		AFunction function = FunctionBeanConverter.FunctionBeanToAFunction(fBean);
 
 		return function;
 	}
 
 	/**
 	 * Method to read function data from an HDF5 file
 	 * @return Map<String, AFunction>
 	 * @throws Exception 
 	 */
 	private Map<String, IFunction> readH5Functions(IMonitor mon) throws Exception{
 		Map<String, IFunction> functions = new HashMap<String, IFunction>();
 		if(file == null)
 			file = HierarchicalDataFactory.getReader(filePath);
 		// JSON deserialization
 		GsonBuilder builder = new GsonBuilder();
 		Gson gson = builder.create();
 
 		List<String> names = getFunctionNames(mon);
 
 		Iterator<String> it = names.iterator();
 		while (it.hasNext()) {
 			String name = (String) it.next();
 			String json = file.getAttributeValue(FUNCTION_ENTRY+"/"+name+"@JSON");
 			json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
 			FunctionBean fBean = FunctionBeanConverter.getFunctionBeanfromJSON(gson, json);
 
 			//convert the bean to AFunction
 			AFunction function = FunctionBeanConverter.FunctionBeanToAFunction(fBean);
 			functions.put(name, function);
 		}
 
 		return functions;
 	}
 
 	private void writeH5DiffractionMetadata(IDiffractionMetadata metadata) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = HierarchicalDataFileUtils.createParentEntry(file, DIFFRACTIONMETADATA_ENTRY,Nexus.DETECT);
 
 		DetectorProperties detprop = metadata.getDetector2DProperties();
 
 		H5Datatype intType = new H5Datatype(Datatype.CLASS_INTEGER, 32/8, Datatype.NATIVE, Datatype.NATIVE);
 		H5Datatype doubleType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);
 
 		final Dataset nXPix = file.replaceDataset("x_pixel_number", intType, new long[] {1}, new int[]{detprop.getPx()}, parent);
 		file.setAttribute(nXPix,NexusUtils.UNIT, "pixels");
 		final Dataset nYPix = file.replaceDataset("y_pixel_number", intType, new long[] {1}, new int[]{detprop.getPy()}, parent);
 		file.setAttribute(nYPix,NexusUtils.UNIT , "pixels");
 
 		final Dataset sXPix = file.replaceDataset("x_pixel_size", doubleType, new long[] {1}, new double[]{detprop.getHPxSize()}, parent);
 		file.setAttribute(sXPix, NexusUtils.UNIT, "mm");
 		final Dataset sYPix = file.replaceDataset("y_pixel_size", doubleType, new long[] {1}, new double[]{detprop.getVPxSize()}, parent);
 		file.setAttribute(sYPix, NexusUtils.UNIT, "mm");
 
 		double[] beamVector = new double[3];
 		detprop.getBeamVector().get(beamVector);
 		file.replaceDataset("beam_vector", doubleType, new long[] {3}, beamVector, parent);
 		
 		double[] beamCentre = detprop.getBeamCentreCoords();
 		
 		double dist = detprop.getBeamCentreDistance();
 		
 		final Dataset centre = file.replaceDataset("beam_centre", doubleType, new long[] {2}, beamCentre, parent);
 		file.setAttribute(centre,NexusUtils.UNIT, "pixels");
 		final Dataset distance = file.replaceDataset("distance", doubleType, new long[] {1}, new double[] {dist}, parent);
 		file.setAttribute(distance, NexusUtils.UNIT, "mm");
 		
 		Matrix3d or = detprop.getOrientation();
 		double[] orientation = new double[] {or.m00 ,or.m01, or.m02,
 				or.m10, or.m11, or.m12,
 				or.m20, or.m21, or.m22};
 
 		file.replaceDataset("detector_orientation", doubleType, new long[] {9}, orientation, parent);
 
 		DiffractionCrystalEnvironment crysenv = metadata.getDiffractionCrystalEnvironment();
 
 		final Dataset energy = file.replaceDataset("energy", doubleType, new long[] {1}, new double[]{crysenv.getWavelength()}, parent);
 		file.setAttribute(energy, NexusUtils.UNIT, "Angstrom");
 
 		final Dataset count = file.replaceDataset("count_time", doubleType, new long[] {1}, new double[]{crysenv.getExposureTime()}, parent);
 		file.setAttribute(count, NexusUtils.UNIT, "s");
 
 		final Dataset phi_start = file.createDataset("phi_start", doubleType, new long[] {1}, new double[]{crysenv.getPhiStart()}, parent);
 		file.setAttribute(phi_start, NexusUtils.UNIT, "degrees");
 
 		final Dataset phi_range = file.replaceDataset("phi_range", doubleType, new long[] {1}, new double[]{crysenv.getPhiRange()}, parent);
 		file.setAttribute(phi_range, NexusUtils.UNIT, "degrees");
 	}
 
 	private IDiffractionMetadata readH5DiffractionMetadata(IMonitor mon) throws Exception {
 		
 		NexusDiffractionMetaReader nexusDiffReader = new NexusDiffractionMetaReader(filePath);
 		
 		return nexusDiffReader.getDiffractionMetadataFromNexus(null);
 	}
 }
