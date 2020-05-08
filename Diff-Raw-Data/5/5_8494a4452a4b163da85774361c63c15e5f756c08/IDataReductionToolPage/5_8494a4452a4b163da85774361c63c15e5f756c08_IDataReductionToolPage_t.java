 package org.dawb.common.ui.plot.tools;
 
 import java.util.List;
 
 import ncsa.hdf.object.Group;
 
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawnsci.plotting.api.tool.IToolPage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.io.h5.H5Utils;
 
 /**
  * Interface used  to define this tool as a data reduction tool. 
  * 
  * Data Reduction tools generally reduce data from nD to (n-1)D,
  * for instance radial integration of an image (2d) to 1d. They can also be applied
  * n+1 data dimensions using the @see DataReductionWizard
  * 
  * @author fcp94556
  *
  */
 public interface IDataReductionToolPage extends IToolPage {
 
 	/**
 	 * Export the tool results to an hdf5 file under the passed in group.
 	 * 
 	 * This method is used to run the tool multiple times on different slices of the data.
 	 * 
 	 * This method will not be called on the UI thread in most instances.
 	 * 
 	 * @param bean
 	 */
 	public DataReductionInfo export(DataReductionSlice bean) throws Exception;
 
 	/**
 	 * Bean to contain data for slice of tool.
 	 * @author fcp94556
 	 *
 	 */
 	public final class DataReductionSlice {
 		
 		/**
 		 * The file we are writing to.
 		 */
 		private IHierarchicalDataFile file;
 		/**
 		 * The Group which the user chose.
 		 */
 		private Group                 parent;
 		/**
 		 * The actual sliced data to operate on.
 		 */
 		private AbstractDataset       data;
 		
 		/**
		 * May be null, 0 = x, 1 = y. Y may be omitted, in which case use indexes
 		 */
 		private List<AbstractDataset> axes;
 		/**
		 * May be null, data which the tool may need for persistence.
 		 */
 		private Object                userData;
 		private IProgressMonitor      monitor;
 		
 		public DataReductionSlice(IHierarchicalDataFile hf,
 				                  Group group,
 				                  AbstractDataset set, 
 				                  Object ud, 
 				                  IProgressMonitor mon) {
 			this.file    = hf;
 			this.parent  = group;
 			this.data    = set;
 			this.userData= ud;
 			this.monitor = mon;
 		}
 		public IHierarchicalDataFile getFile() {
 			return file;
 		}
 		public void setFile(IHierarchicalDataFile hf) {
 			this.file = hf;
 		}
 		public Group getParent() {
 			return parent;
 		}
 		public void setParent(Group parent) {
 			this.parent = parent;
 		}
 		public AbstractDataset getData() {
 			return data;
 		}
 		public void setData(AbstractDataset set) {
 			this.data = set;
 		}
 
 		public void appendData(AbstractDataset more) throws Exception {
 			H5Utils.appendDataset(file, parent, more);
 		}
 
 		public Object getUserData() {
 			return userData;
 		}
 		public void setUserData(Object userData) {
 			this.userData = userData;
 		}
 		public IProgressMonitor getMonitor() {
 			return monitor;
 		}
 		public void setMonitor(IProgressMonitor monitor) {
 			this.monitor = monitor;
 		}
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((axes == null) ? 0 : axes.hashCode());
 			result = prime * result + ((data == null) ? 0 : data.hashCode());
 			result = prime * result + ((file == null) ? 0 : file.hashCode());
 			result = prime * result
 					+ ((monitor == null) ? 0 : monitor.hashCode());
 			result = prime * result
 					+ ((parent == null) ? 0 : parent.hashCode());
 			result = prime * result
 					+ ((userData == null) ? 0 : userData.hashCode());
 			return result;
 		}
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			DataReductionSlice other = (DataReductionSlice) obj;
 			if (axes == null) {
 				if (other.axes != null)
 					return false;
 			} else if (!axes.equals(other.axes))
 				return false;
 			if (data == null) {
 				if (other.data != null)
 					return false;
 			} else if (!data.equals(other.data))
 				return false;
 			if (file == null) {
 				if (other.file != null)
 					return false;
 			} else if (!file.equals(other.file))
 				return false;
 			if (monitor == null) {
 				if (other.monitor != null)
 					return false;
 			} else if (!monitor.equals(other.monitor))
 				return false;
 			if (parent == null) {
 				if (other.parent != null)
 					return false;
 			} else if (!parent.equals(other.parent))
 				return false;
 			if (userData == null) {
 				if (other.userData != null)
 					return false;
 			} else if (!userData.equals(other.userData))
 				return false;
 			return true;
 		}
 		public List<AbstractDataset> getAxes() {
 			return axes;
 		}
 		public void setAxes(List<AbstractDataset> axes) {
 			this.axes = axes;
 		}
 
 		
 	}
 	/**
 	 * TODO May add a method here to define extra wizard pages if a tool requires it.
 	 * public IWizardPage getToolExportWizardPage(...) {
 	 */
 	
 	public final class DataReductionInfo {
 		private IStatus status;
 		/**
 		 * used to provide data between calls of the tool to maintain
 		 * state. For instance used in the peak fitting tool to provide
 		 * peaks with state.
 		 */
 		private Object  userData;
 		
 		public DataReductionInfo(IStatus status) {
 			this(status, null);
 		}
 		/**
 		 * 
 		 * @param status
 		 * @param userData may be null.
 		 */
 		public DataReductionInfo(IStatus status, Object userData) {
 			this.status   = status;
 			this.userData = userData;
 		}
 		
 		public IStatus getStatus() {
 			return status;
 		}
 		public void setStatus(IStatus status) {
 			this.status = status;
 		}
 		public Object getUserData() {
 			return userData;
 		}
 		public void setUserData(Object userData) {
 			this.userData = userData;
 		}
 		
 	}
 }
