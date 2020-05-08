 package org.geworkbench.util.network;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.GeneOntologyUtil;
 import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
 import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
 
 /**
  * Created by IntelliJ IDEA.
  * User: xiaoqing
  * Date: Jan 5, 2007
  * Time: 12:31:48 PM
  * To change this template use File | Settings | File Templates.
  */
 
 /**
  * It is used to save all celllualr Network information related to a specific
  * marker.
  * 
  */
 public class CellularNetWorkElementInformation implements java.io.Serializable {
 
 	private static final long serialVersionUID = -4163326138016520667L;
 	private HashMap<String, Integer> interactionNumMap = new HashMap<String, Integer>();
 
 	private static Set<String> allInteractionTypes = new HashSet<String>();
 
 	private DSGeneMarker dSGeneMarker;
 	private String goInfoStr;
 	private String geneType;
 	private InteractionDetail[] interactionDetails;
 	private double threshold;
 	private static double smallestIncrement;
 	private static Double defaultSmallestIncrement = 0.01;
 	private static int binNumber;
 	private boolean isDirty;
 
 	public CellularNetWorkElementInformation(
 			HashMap<String, Integer> interactionNumMap,
 			DSGeneMarker dSGeneMarker, String goInfoStr, String geneType) {
 		this.interactionNumMap = interactionNumMap;
 		this.dSGeneMarker = dSGeneMarker;
 		this.goInfoStr = goInfoStr;
 		this.geneType = geneType;
 
 	}
 
 	public CellularNetWorkElementInformation(DSGeneMarker dSGeneMarker) {
 		this.dSGeneMarker = dSGeneMarker;
 		smallestIncrement = defaultSmallestIncrement;
 		isDirty = true;
 		goInfoStr = "";
 		Set<GOTerm> set = getAllGOTerms(dSGeneMarker);
 
 		if (set != null && set.size() > 0) {
 			for (GOTerm goTerm : set) {
 				goInfoStr += goTerm.getName() + "; ";
 			}
 		}
 		geneType = GeneOntologyUtil.checkMarkerFunctions(
 				dSGeneMarker);
 
 		reset();
 
 	}
 	
 	private static Set<GOTerm> getAllGOTerms(DSGeneMarker dsGeneMarker) {
 		GeneOntologyTree tree = GeneOntologyTree.getInstance();
         String geneId = dsGeneMarker.getLabel();
         String[] goTerms = AnnotationParser.getInfo(geneId, AnnotationParser.GOTERM);
         if (goTerms != null) {
             Set<GOTerm> set = new HashSet<GOTerm>();
             for (String goTerm : goTerms) {
                 String goIdStr = goTerm.split("/")[0].trim();
                 if (!goIdStr.equalsIgnoreCase("---")) {
                     int goId = new Integer(goIdStr);
                     if(tree.getTerm(goId)!=null)
                     set.add(tree.getTerm(goId));
                 }
             }
             return set;
         }
 
         return null;
 	}
 	
     public TreeMap<String, Set<GOTerm>> getAllAncestorGoTerms(String catagory) {
 		GeneOntologyTree tree = GeneOntologyTree.getInstance();
         String geneId = dSGeneMarker.getLabel();
         String[] goTerms = AnnotationParser.getInfo(geneId, catagory);
 
         TreeMap<String, Set<GOTerm>> treeMap = new TreeMap<String, Set<GOTerm>>();
         if (goTerms != null) {
 
             for (String goTerm : goTerms) {
                 String goIdStr = goTerm.split("/")[0].trim();
                 try {
                     if (!goIdStr.equalsIgnoreCase("---")) {
                         Integer goId = new Integer(goIdStr);
                         if (goId != null) {
                             treeMap.put(goTerm, tree.getAncestors(goId));
                         }
                     }
                 } catch (NumberFormatException ne) {
                     ne.printStackTrace();
                 }
 
             }
         }
         return treeMap;
     }
 
 	/**
 	 * Remove all previous retrieved information.
 	 */
 	public void reset() {
 
 		for (String interactionType : allInteractionTypes) {
 			interactionNumMap.put(interactionType, 0);
 		}
 		binNumber = (int) (1 / smallestIncrement) + 1;
 
 	}
 
 	public ArrayList<InteractionDetail> getSelectedInteractions(
 			List<String> interactionIncludedList) {
 		ArrayList<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();
 		if (interactionDetails != null && interactionDetails.length > 0) {
 			for (int i = 0; i < interactionDetails.length; i++) {
 				InteractionDetail interactionDetail = interactionDetails[i];
 				if (interactionDetail != null
 						&& interactionDetail.getConfidence() >= threshold) {
 					if (interactionIncludedList.contains(interactionDetail
 							.getInteractionType())) {
 						arrayList.add(interactionDetail);
 					}
 
 				}
 			}
 		}
 		return arrayList;
 	}
 
 	public ArrayList<InteractionDetail> getSelectedInteractions(String interactionType) {
 		ArrayList<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();
 		if (interactionDetails != null && interactionDetails.length > 0) {
 			for (int i = 0; i < interactionDetails.length; i++) {
 				InteractionDetail interactionDetail = interactionDetails[i];
 				if (interactionDetail != null
 						&& interactionDetail.getConfidence() >= threshold) {
 					if (interactionType.equals(interactionDetail.getInteractionType())) {
 						arrayList.add(interactionDetail);
 					}
 
 				}
 			}
 		}
 		return arrayList;
 	}
 	
 	public static double getSmallestIncrement() {
 		if (smallestIncrement <= 0 )
 			smallestIncrement = defaultSmallestIncrement;
 		return smallestIncrement;
 	}
 
 	public void setSmallestIncrement(double smallestIncrementNumber) {
 		smallestIncrement = smallestIncrementNumber;
 	}
 
	public int[] getDistribution() {
 		int[] distribution = new int[binNumber];
 		for (int i = 0; i < binNumber; i++)
 			distribution[i] = 0;
 		if (interactionDetails == null || interactionDetails.length <= 0)
 			return distribution;
 		for (InteractionDetail interactionDetail : interactionDetails) {
			if (interactionDetail != null) {
 				int confidence = (int) (interactionDetail.getConfidence() * 100);
 				if (confidence < distribution.length && confidence >= 0) {
 					for (int i = 0; i <= confidence; i++)
 						distribution[i]++;
 
 				}
 
 			}
 		}
 		return distribution;
 	}
 
 	public static int getBinNumber() {
 		if (binNumber <= 0)
 			binNumber = (int) (1 / defaultSmallestIncrement) + 1;
 		return binNumber;
 	}
 
 	public boolean isDirty() {
 		return isDirty;
 	}
 
 	public static void setAllInteractionTypes(
 			List<String> allInteractionTypeList) {
 		allInteractionTypes.clear();
 		allInteractionTypes.addAll(allInteractionTypeList);
 
 	}
 
 	public void setDirty(boolean dirty) {
 		isDirty = dirty;
 	}
 
 	public static void setBinNumber(int binNumber) {
 		CellularNetWorkElementInformation.binNumber = binNumber;
 	}
 
 	public CellularNetWorkElementInformation(
 			HashMap<String, Integer> interactionNumMap,
 			DSGeneMarker dSGeneMarker) {
 		this.interactionNumMap = interactionNumMap;
 		this.dSGeneMarker = dSGeneMarker;
 	}
 
 	public Integer getInteractionNum(String interactionType) {
 		if (interactionNumMap.containsKey(interactionType))
 			return interactionNumMap.get(interactionType);
 		else
 			return null;
 	}
 
 	public HashMap<String, Integer> getInteractionNumMap() {
 		return this.interactionNumMap;
 	}
 
 	public double getThreshold() {
 		return threshold;
 	}
 
 	public void setThreshold(double _threshold) {
 		if (threshold != _threshold) {
 			threshold = _threshold;
 
 		}
 		update();
 	}
 
 	public InteractionDetail[] getInteractionDetails() {
 		return interactionDetails;
 	}
 
 	/**
 	 * Associate the gene marker with the details.
 	 * 
 	 * @param arrayList
 	 */
 	public void setInteractionDetails(List<InteractionDetail> arrayList) {
 
 		if (arrayList != null && arrayList.size() > 0) {
 			interactionDetails = new InteractionDetail[arrayList.size()];
 			this.interactionDetails = arrayList.toArray(interactionDetails);
 		} else {
 			interactionDetails = null;
 			reset();
 		}
 
 		if (interactionDetails != null) {
 			update();
 		}
 
 	}
 
 	/**
 	 * Update the number of interaction based on the new threshold or new
 	 * InteractionDetails.
 	 */
 	private void update() {
 
 		reset();
 
 		if (interactionDetails == null || interactionDetails.length == 0) {
 			return;
 		}
 
 		for (InteractionDetail interactionDetail : interactionDetails) {
 			if (interactionDetail != null) {
 				int confidence = (int) (interactionDetail.getConfidence() * 100);
 				String interactionType = interactionDetail.getInteractionType();
 				if (confidence >= 100 * threshold) {
 					if (this.interactionNumMap.containsKey(interactionType)) {
 						int num = interactionNumMap.get(interactionType) + 1;
 						interactionNumMap.put(interactionType, num);
 
 					} else {
 						interactionNumMap.put(interactionType, 1);
 
 					}
 
 				}
 			}
 		}
 
 	}
 
 	public int[] getInteractionDistribution(String interactionType) {
 		int[] interactionDistribution = new int[binNumber];
 		for (int i = 0; i < binNumber; i++)
 			interactionDistribution[i] = 0;
 		if (interactionDetails == null || interactionDetails.length <= 0)
 			return interactionDistribution;
 
 		for (InteractionDetail interactionDetail : interactionDetails) {
 			int confidence = (int) (interactionDetail.getConfidence() * 100);
 			if (confidence < interactionDistribution.length && confidence >= 0) {
 
 				if (interactionDetail.getInteractionType().equals(
 						interactionType)) {
 					for (int i = 0; i <= confidence; i++)
 						interactionDistribution[i]++;
 
 				}
 			}
 
 		}
 
 		return interactionDistribution;
 	}
 
 	public void setInteractionNum(String interactionType, int interactionNum) {
 		this.interactionNumMap.put(interactionType, interactionNum);
 	}
 
 	public DSGeneMarker getdSGeneMarker() {
 		return dSGeneMarker;
 	}
 
 	public void setdSGeneMarker(DSGeneMarker dSGeneMarker) {
 		this.dSGeneMarker = dSGeneMarker;
 	}
 
 	public String getGoInfoStr() {
 		return goInfoStr;
 	}
 
 	public void setGoInfoStr(String goInfoStr) {
 		this.goInfoStr = goInfoStr;
 	}
 
 	public String getGeneType() {
 		return geneType;
 	}
 
 	public void setGeneType(String geneType) {
 		this.geneType = geneType;
 	}
 
 	public boolean equals(Object obj) {
 		if (obj instanceof CellularNetWorkElementInformation) {
 			return dSGeneMarker.getGeneName().equals(
 					((CellularNetWorkElementInformation) obj).getdSGeneMarker()
 							.getGeneName())
 					&& dSGeneMarker.getLabel().equals(
 							((CellularNetWorkElementInformation) obj)
 									.getdSGeneMarker().getLabel());
 		} else {
 			return false;
 		}
 	}
 }
