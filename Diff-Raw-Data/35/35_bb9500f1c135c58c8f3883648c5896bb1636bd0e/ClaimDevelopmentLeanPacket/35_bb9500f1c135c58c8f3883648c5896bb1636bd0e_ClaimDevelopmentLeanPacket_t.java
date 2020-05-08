 package org.pillarone.riskanalytics.domain.pc.reserves.fasttrack;
 
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.claims.ClaimPacketFactory;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Arrays;
 
 /**
  * @author shartmann (at) munichre (dot) com
  */
 public class ClaimDevelopmentLeanPacket extends Claim {
 
     private double paid;
     private double reserved; //todo(bgi): eliminate this field (currently, we'd only need to replace it with its getter in toString)?
     private Claim convertedClaim;
 
     private static final String INCURRED = "incurred";
     private static final String PAID = "paid";
     private static final String RESERVED = "reserved";
 
     public ClaimDevelopmentLeanPacket(){
     }
 
     public ClaimDevelopmentLeanPacket(Claim claim){
         super.set(claim);
     }
 
     @Override
     public Claim copy() {
         ClaimDevelopmentLeanPacket copy = ClaimDevelopmentLeanPacketFactory.createPacket();
         copy.set(this);
         return copy;
     }
 
     /**
      *  This function is not secure in the sense that it does not check if the object itself and
      *  the object received as argument are of equal type.
      */
     //todo(bgi): see if this one should perhaps enforce safety
     @Override
     public Claim getNetClaim(Claim cededClaim) {
         ClaimDevelopmentLeanPacket netClaim = (ClaimDevelopmentLeanPacket) copy();
         netClaim.setIncurred(netClaim.getIncurred() - ((ClaimDevelopmentLeanPacket) cededClaim).getIncurred());
         netClaim.paid -= ((ClaimDevelopmentLeanPacket) cededClaim).paid;
        if (hasExposureInfo()) {
            double coverRatio = netClaim.getUltimate() / getUltimate();
            netClaim.setExposure(getExposure().copy().scale(coverRatio));
        }
         return netClaim;
     }
 
     /**
      *  This function is not secure in the sense that it does not check if the object itself and
      *  the object received as argument are of equal type.
      */
     @Override
     public void plus(Claim claim) {
         setIncurred(getIncurred() + ((ClaimDevelopmentLeanPacket) claim).getIncurred());
         paid += ((ClaimDevelopmentLeanPacket) claim).getPaid();
     }
 
     /**
      *  This function is not secure in the sense that it does not check if the object itself and
      *  the object received as argument are of equal type.
      */
     @Override
     public void minus(Claim claim) {
         setIncurred(getIncurred() - ((ClaimDevelopmentLeanPacket) claim).getIncurred());
         paid -= ((ClaimDevelopmentLeanPacket) claim).getPaid();
     }
 
     @Override
     public ClaimDevelopmentLeanPacket scale(double factor) {
         super.scale(factor);
         paid *= factor;
         return this;
     }
 
     /**
      *  Converts the current packet into a Claim packet, setting its value to incurred
      */
     public Claim getClaimPacket() {
         if (convertedClaim == null) {
             convertedClaim = ClaimPacketFactory.createPacket();
             convertedClaim.setOrigin(origin);
             convertedClaim.setOriginalClaim(getOriginalClaim());
             convertedClaim.setEvent(getEvent());
             convertedClaim.setFractionOfPeriod(getFractionOfPeriod());
             convertedClaim.setClaimType(getClaimType());
             convertedClaim.setPeril(getPeril());
             convertedClaim.setLineOfBusiness(getLineOfBusiness());
         }
         convertedClaim.setUltimate(getUltimate());
         return convertedClaim;
     }
 
     @Override
     public void set(Claim claim) {
         super.set(claim);
         setPaid(((ClaimDevelopmentLeanPacket) claim).getPaid());
     }
 
     @Override
     public Map<String, Number> getValuesToSave() throws IllegalAccessException {
         Map<String, Number> valuesToSave = new HashMap<String, Number>(3);
         valuesToSave.put(INCURRED, getUltimate());
         valuesToSave.put(PAID, paid);
         valuesToSave.put(RESERVED, getUltimate() - paid);
         return valuesToSave;
     }
 
     @Override
     public List<String> getFieldNames() {
         return Arrays.asList(INCURRED, PAID, RESERVED);
     }
 
     @Override
     public String toString() {
         String separator = ", ";
         StringBuilder result = new StringBuilder();
         result.append(getIncurred());
         result.append(separator);
         result.append(paid);
         result.append(separator);
         result.append(reserved);
         result.append(separator);
         if (getClaimType() != null) result.append(getClaimType()).append(separator);
         if (origin != null) result.append(origin.getName()).append(separator);
         if (getOriginalClaim() != null) result.append(System.identityHashCode(getOriginalClaim())).append(separator);
         if (getLineOfBusiness() != null) result.append(getLineOfBusiness().getName()).append(separator);
         if (getPeril() != null) result.append(getPeril().getName()).append(separator);
         if (getReinsuranceContract() != null) result.append(getReinsuranceContract().getName()).append(separator);
         return result.toString();
     }
 
     public double getIncurred() {
         return getUltimate();
     }
 
     public void setIncurred(double incurred) {
         setUltimate(incurred);
     }
 
     public double getPaid() {
         return paid;
     }
 
     public void setPaid(double paid) {
         this.paid = paid;
     }
 
     public double getReserved() {
         return getUltimate() - paid;
     }
 
     public void setReserved(double reserved) {
         this.reserved = reserved;
     }
 }
