 /*
  * Created on Mar 25, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.rembrandt.web.helper;
 
 import gov.nih.nci.caintegrator.util.CaIntegratorConstants;
 import gov.nih.nci.rembrandt.web.struts.form.KMDataSetForm;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * 
  * @author Himanso
  */
 public class KMDataSetHelper {
 
 	public static KMDataSetForm populateReporters(List _reporters,
 			String _plotType, KMDataSetForm _kmForm) {
 		List reporters = new ArrayList();
		
		if (_reporters != null && _plotType != null && _kmForm != null) {
 			reporters = _reporters;
			if (_plotType.equals(CaIntegratorConstants.GENE_EXP_KMPLOT)) {
 				if(_kmForm.getAlgorithm()!= null &&
 						_kmForm.getAlgorithm().equals("unified")){
 					reporters.add(0,CaIntegratorConstants.GRAPH_BLANK);
 				}else{
 					reporters.add(0, CaIntegratorConstants.GRAPH_DEFAULT);
 				}
 			}
 			if (_plotType.equals(CaIntegratorConstants.COPY_NUMBER_KMPLOT)
 					&& reporters.size() > 1) {
 				reporters.add(0, CaIntegratorConstants.GRAPH_BLANK);
 
 			}
 			_kmForm.setReporters(reporters);
 		}
 		return _kmForm;
 	}
 }
