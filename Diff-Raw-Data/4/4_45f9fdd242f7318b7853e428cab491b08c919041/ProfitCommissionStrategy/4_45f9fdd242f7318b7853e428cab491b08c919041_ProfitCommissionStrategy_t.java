 package org.pillarone.riskanalytics.domain.pc.reinsurance.commissions;
 
 import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author ben.ginsberg (at) intuitive-collaboration (dot) com
  */
 public class ProfitCommissionStrategy implements ICommissionStrategy {
 
     private double profitCommissionRatio = 0d;
     private double commissionRatio = 0d; // for "prior" fixed commission
     private double costRatio = 0d;
     private boolean lossCarriedForwardEnabled = true;
     private double initialLossCarriedForward = 0d;
     /**
      * not a parameter but updated during calculateCommission() to avoid side effect for the parameter variable
      */
     private double lossCarriedForward = 0d;
 
     public IParameterObjectClassifier getType() {
         return CommissionStrategyType.PROFITCOMMISSION;
     }
 
     public Map getParameters() {
         Map<String, Object> map = new HashMap<String, Object>(4);
         map.put("profitCommissionRatio", profitCommissionRatio);
         map.put("commissionRatio", commissionRatio);
         map.put("costRatio", costRatio);
         map.put("lossCarriedForwardEnabled", lossCarriedForwardEnabled);
         map.put("initialLossCarriedForward", initialLossCarriedForward);
         return map;
     }
 
     public void calculateCommission(List<Claim> claims, List<UnderwritingInfo> underwritingInfos, boolean isFirstPeriod, boolean isAdditive) {
         if (lossCarriedForwardEnabled && isFirstPeriod) {
             lossCarriedForward = initialLossCarriedForward;
         }
         double incurredClaims = 0d;
         for (Claim claim: claims) {
             incurredClaims += claim.getUltimate();
         }
         double totalPremiumWritten = 0d;
         for (UnderwritingInfo underwritingInfo : underwritingInfos) {
             totalPremiumWritten += underwritingInfo.getPremiumWritten();
         }
         double fixedCommission = commissionRatio * totalPremiumWritten; // calculate 'prior' fixed commission
         double nextLossCarriedForward = totalPremiumWritten * (1d - costRatio) - fixedCommission - incurredClaims;
        double commissionableProfit = Math.max(0d, nextLossCarriedForward + lossCarriedForward);
         double totalCommission =  fixedCommission + profitCommissionRatio * commissionableProfit;
 
         if (isAdditive) {
             for (UnderwritingInfo underwritingInfo : underwritingInfos) {
                 underwritingInfo.setCommission(-underwritingInfo.getPremiumWritten() * totalCommission / totalPremiumWritten +
                                                underwritingInfo.getCommission());
             }
         }
         else {
             for (UnderwritingInfo underwritingInfo : underwritingInfos) {
                 underwritingInfo.setCommission(-underwritingInfo.getPremiumWritten() * totalCommission / totalPremiumWritten);
             }
         }
 
        // todo(jwa): something is inconsistent here
         lossCarriedForward = lossCarriedForwardEnabled ? Math.min(0d, nextLossCarriedForward) : 0d;
     }
 }
