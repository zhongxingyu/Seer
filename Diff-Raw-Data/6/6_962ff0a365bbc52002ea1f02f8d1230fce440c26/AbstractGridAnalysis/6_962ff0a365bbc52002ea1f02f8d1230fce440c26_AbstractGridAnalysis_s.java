 package org.geworkbench.analysis;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
 
 /**
  * Analyses that have a corresponding (ca)Grid component should extend this
  * abstract class to have the grid analyses exposed.
  * 
  * @author keshav
  * @version $Id: AbstractGridAnalysis.java,v 1.6.2.1 2007/11/05 21:08:42 keshav
  *          Exp $
  */
 public abstract class AbstractGridAnalysis extends AbstractAnalysis {
 
 	private Log log = LogFactory.getLog(this.getClass());
 
 	/**
 	 * Analyses extending this class will implement this method, setting the
 	 * name of the service. This name should be equivalent to one of the names
 	 * in the "grid services cache". See {@link GridServiceRunner}.
 	 * 
 	 * @return String
 	 */
 	public abstract String getAnalysisName();
 
 	/**
 	 * 
 	 * @return
 	 */
 	public abstract Class getBisonReturnType();
 
 	/**
 	 * Some grid analyses use microarray set views as inputs, some don't.
 	 * 
 	 * @return
 	 */
 	protected abstract boolean useMicroarraySetView();
 
 	/**
 	 * Some grid analyses use "other" data sets (ie. EdgeList) as inputs, some
 	 * don't.
 	 * 
 	 * @return
 	 */
 	protected abstract boolean useOtherDataSet();
 
 	/**
 	 * This method should be implmented to obtain the user input (which is
 	 * stored as BISON parameters), convert it to caGrid parameters, and return
 	 * a map with caGrid parameter values keyed by parameter names.
 	 * 
 	 * @return
 	 */
 	protected abstract Map<Serializable, Serializable> getBisonParameters();
 
 	/**
 	 * Packs the bison inputs (including parameters) for the grid service.
 	 * Services will take in a {@link DSMicroarraySetView} and/or a
 	 * {@link DSDataSet}.
 	 * 
 	 * @param microarraySetView
 	 * @param otherDataset
 	 * @return {@link List} - A list of service inputs.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Serializable> handleBisonInputs(
 			DSMicroarraySetView microarraySetView, DSDataSet otherDataset) {
 
 		List<Serializable> serviceParameterList = new ArrayList<Serializable>();
 
 		if (useMicroarraySetView()) {
 			serviceParameterList.add(microarraySetView);
 		}
 		if (useOtherDataSet()) {
 			if (otherDataset != null) {
 				serviceParameterList.add(otherDataset);
 			} else {
 				log
 						.error("Was told to use the other dataset but cannot.  This is null.");
 			}
 		}
 
 		serviceParameterList.add((Serializable) getBisonParameters());
 
 		return serviceParameterList;
 	}
 }
