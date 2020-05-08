 package org.pillarone.riskanalytics.domain.pc.underwriting;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author jessika.walter (at) intuitive-collaboration (dot) com
  */
 public class CededUnderwritingInfo extends UnderwritingInfo {
 
     private double fixedPremium;
     private double variablePremium;
 
     private double fixedCommission;
     private double variableCommission;
 
     private static final String PREMIUM = "premium";
    private static final String PREMIUM_FIXED = "fixedPremium";
    private static final String PREMIUM_VARIABLE = "variablePremium";
     private static final String COMMISSION = "commission";
    private static final String COMMISSION_FIXED = "fixedCommission";
    private static final String COMMISSION_VARIABLE = "variableCommission";
 
     public CededUnderwritingInfo copy() {
         CededUnderwritingInfo copy = CededUnderwritingInfoPacketFactory.createPacket();
         copy.set(this);
         return copy;
     }
 
     public UnderwritingInfo copyToSuperClass() {
         UnderwritingInfo copy = UnderwritingInfoPacketFactory.createPacket();
         copy.set(this);
         return copy;
     }
 
     public void set(CededUnderwritingInfo underwritingInfo) {
         super.set(underwritingInfo);
         fixedPremium = underwritingInfo.fixedPremium;
         variablePremium = underwritingInfo.variablePremium;
         fixedCommission = underwritingInfo.fixedCommission;
         variableCommission = underwritingInfo.variableCommission;
     }
 
     public Map<String, Number> getValuesToSave() throws IllegalAccessException {
         Map<String, Number> valuesToSave = super.getValuesToSave();
         valuesToSave.put(PREMIUM_FIXED, fixedPremium);
         valuesToSave.put(PREMIUM_VARIABLE, variablePremium);
         valuesToSave.put(COMMISSION_FIXED, fixedCommission);
         valuesToSave.put(COMMISSION_VARIABLE, variableCommission);
         return valuesToSave;
     }
 
     @Override
     public List<String> getFieldNames() {
         return Arrays.asList(PREMIUM, PREMIUM_FIXED, PREMIUM_VARIABLE, COMMISSION, COMMISSION_FIXED, COMMISSION_VARIABLE);
     }
 
     public CededUnderwritingInfo plus(UnderwritingInfo other) {
         super.plus(other);
         if (other instanceof CededUnderwritingInfo) {
             fixedPremium += ((CededUnderwritingInfo) other).fixedPremium;
             variablePremium += ((CededUnderwritingInfo) other).variablePremium;
             fixedCommission += ((CededUnderwritingInfo) other).fixedCommission;
             variableCommission += ((CededUnderwritingInfo) other).variableCommission;
         }
         return this;
     }
 
     public UnderwritingInfo minus(UnderwritingInfo other) {
         super.minus(other);
         if (other instanceof CededUnderwritingInfo) {
             fixedPremium -= ((CededUnderwritingInfo) other).fixedPremium;
             variablePremium -= ((CededUnderwritingInfo) other).variablePremium;
             fixedCommission -= ((CededUnderwritingInfo) other).fixedCommission;
             variableCommission -= ((CededUnderwritingInfo) other).variableCommission;
         }
         return this;
     }
 
     public CededUnderwritingInfo scale(double factor) {
         super.scale(factor);
         fixedPremium *= factor;
         variablePremium *= factor;
         fixedCommission *= factor;
         variableCommission *= factor;
         return this;
     }
 
     public String toString() {
         return "premium: " + String.valueOf(getPremium()) + ", commission: " + getCommission() +
                 ", origin: " + (origin == null ? "null" : origin.getNormalizedName()) +
                 ", original " + (getOriginalUnderwritingInfo() == null ? "null" :
                 getOriginalUnderwritingInfo().origin == null ? "unnamed" :
                         getOriginalUnderwritingInfo().origin.getNormalizedName());
     }
 
     public double getFixedPremium() {
         return fixedPremium;
     }
 
     public void setFixedPremium(double fixedPremium) {
         this.fixedPremium = fixedPremium;
     }
 
     public double getVariablePremium() {
         return variablePremium;
     }
 
     public void setVariablePremium(double variablePremium) {
         this.variablePremium = variablePremium;
     }
 
     public double getFixedCommission() {
         return fixedCommission;
     }
 
     public void setFixedCommission(double fixedCommission) {
         this.fixedCommission = fixedCommission;
     }
 
     public double getVariableCommission() {
         return variableCommission;
     }
 
     public void setVariableCommission(double variableCommission) {
         this.variableCommission = variableCommission;
     }
 }
