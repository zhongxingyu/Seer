 package org.pillarone.riskanalytics.domain.pc.reinsurance.commissions;
 
 import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter;
 import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory;
 import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;
 import org.pillarone.riskanalytics.core.util.GroovyUtils;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 import org.pillarone.riskanalytics.domain.utils.constraints.DoubleConstraints;
 
 import java.util.*;
 
 
 /**
  * Assigns a commission rate and calculates the commission on ceded premium based on the loss ratio
  * (total losses / total premium).
  *
  * The commission rate is a left-countinuous step-function of the loss ratio, with a finite number of jumps.
  * Each step interval, or "commission band", is realized internally as a key-value pair in a Java Map object.
  * Each map entry's key is the interval's left endpoint, and the map's value is the commission rate that
  * applies for loss ratios in the interval. Because the interval's right endpoint is not stored in the map,
  * we use the following conventions for defining & evaluating the resulting step function:
  * <ol>
  * <li>A first band, from -Infinity, with commission 0, is always added.
  * <li>Intermediate bands must be given in order of increasing lower limit.
  * <li>Each band applies to loss ratios inclusive of the lower limit, but
  * exclusive of the upper limit (which is the next band's lower limit, if any).
  * Otherwise, the last commission is used for all sufficiently large
  * loss ratios (i.e. at or above the last band's lower limit).
  * <li>The last band given effectively has no upper limit.
  * The caller must therefore specify a last band with commission 0 under typical use cases.
  * </ol>
  *
  * @author shartmann (at) munichre (dot) com, ben.ginsberg (at) intuitive-collaboration.com
  */
 public class SlidingCommissionStrategy implements ICommissionStrategy {
 
     public static final String LOSS_RATIO = "Loss Ratio (from)";
     public static final String COMMISSION = "Commission";
     public static final int LOSS_RATIO_COLUMN_INDEX = 0;
     public static final int COMMISSION_COLUMN_INDEX = 1;
 
     private ConstrainedMultiDimensionalParameter commissionBands = new ConstrainedMultiDimensionalParameter(
             GroovyUtils.convertToListOfList(new Object[]{0d, 0d}),
             Arrays.asList(LOSS_RATIO, COMMISSION),
             ConstraintsFactory.getConstraints(DoubleConstraints.IDENTIFIER));
 
     private LinkedHashMap<Double, Double> commissionRates = null;
     List<Double> lowerBandLimits = null;
 
     public IParameterObjectClassifier getType() {
         return CommissionStrategyType.SLIDINGCOMMISSION;
     }
 
     public Map getParameters() {
         Map<String, Object> map = new HashMap<String, Object>(1);
         map.put("commissionBands", commissionBands);
         return map;
     }
 
     public void calculateCommission(List<Claim> claims, List<UnderwritingInfo> underwritingInfos, boolean isFirstPeriod, boolean isAdditive) {
         double totalClaims = 0d;
         double totalPremium = 0d;
         for (Claim claim : claims) {
             totalClaims += claim.getUltimate();
         }
         for (UnderwritingInfo uwInfo : underwritingInfos) {
             totalPremium += uwInfo.getPremiumWritten();
         }
         double totalLossRatio = totalClaims / totalPremium;
         if (commissionRates == null) setCommissionRates(); // lazy initialization
 
         double highestMatchingLowerBound = Double.NEGATIVE_INFINITY;
         for (double entryLossRatio : lowerBandLimits) { // or: use commissionRates.keySet() if lowerBandLimits not desired
             if ((highestMatchingLowerBound < entryLossRatio) && (entryLossRatio <= totalLossRatio)) {
                 highestMatchingLowerBound = entryLossRatio;
             }
         }
         double commission = commissionRates.get(highestMatchingLowerBound);
 
         if (isAdditive) {
             for (UnderwritingInfo uwInfo : underwritingInfos) {
                 uwInfo.setCommission(uwInfo.getCommission() - uwInfo.getPremiumWritten() * commission);
             }
         }
         else {
             for (UnderwritingInfo uwInfo : underwritingInfos) {
                 uwInfo.setCommission(-uwInfo.getPremiumWritten() * commission);
             }
         }
     }
 
     private void setCommissionRates() {
         int numberOfBands = commissionBands.getValueRowCount();
         lowerBandLimits = new LinkedList<Double>();
         commissionRates = new LinkedHashMap<Double, Double>(numberOfBands);
         int columnLossRatio = commissionBands.getColumnIndex(LOSS_RATIO);
         int columnCommission = commissionBands.getColumnIndex(COMMISSION);
         double previousLossRatio = Double.NEGATIVE_INFINITY;
         lowerBandLimits.add(previousLossRatio);
         commissionRates.put(previousLossRatio, 0d);
         for (int row = 1; row <= numberOfBands; row++) {
            double lossRatio = (Double) commissionBands.getValueAt(row, columnLossRatio);
            double commission = (Double) commissionBands.getValueAt(row, columnCommission);
             lowerBandLimits.add(lossRatio);
             commissionRates.put(lossRatio, commission);
             previousLossRatio = lossRatio;
         }
         // sort lowerBandLimits here if necessary (i.e. if above check is removed)
     }
         
 }
