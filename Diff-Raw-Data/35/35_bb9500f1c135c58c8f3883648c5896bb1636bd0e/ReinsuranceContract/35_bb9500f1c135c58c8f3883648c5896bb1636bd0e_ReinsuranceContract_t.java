 package org.pillarone.riskanalytics.domain.pc.reinsurance.contracts;
 
 import org.pillarone.riskanalytics.core.components.Component;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.core.packets.SingleValuePacket;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.claims.ClaimUtilities;
 import org.pillarone.riskanalytics.domain.pc.claims.SortClaimsByFractionOfPeriod;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.ReinsuranceResultWithCommissionPacket;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.commissions.CommissionStrategyType;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.commissions.ICommissionStrategy;
 import org.pillarone.riskanalytics.domain.pc.reserves.cashflow.ClaimDevelopmentPacket;
 import org.pillarone.riskanalytics.domain.pc.reserves.fasttrack.ClaimDevelopmentLeanPacket;
 import org.pillarone.riskanalytics.domain.pc.underwriting.CededUnderwritingInfo;
 import org.pillarone.riskanalytics.domain.pc.underwriting.CededUnderwritingInfoUtilities;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfoUtilities;
 
 import java.util.*;
 
 /**
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class ReinsuranceContract extends Component implements IReinsuranceContractMarker {
 
     protected PacketList<Claim> inClaims = new PacketList<Claim>(Claim.class);
     protected PacketList<UnderwritingInfo> inUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     private PacketList<SingleValuePacket> inInitialReserves = new PacketList<SingleValuePacket>(SingleValuePacket.class);
 
     protected PacketList<Claim> outUncoveredClaims = new PacketList<Claim>(Claim.class);
     protected PacketList<Claim> outCoveredClaims = new PacketList<Claim>(Claim.class);
 
     // todo(sku): remove the following and related lines as soon as PMO-648 is resolved
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanNet = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanGross = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
     private PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanCeded = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
 
     protected PacketList<UnderwritingInfo> outNetAfterCoverUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     protected PacketList<CededUnderwritingInfo> outCoverUnderwritingInfo = new PacketList<CededUnderwritingInfo>(CededUnderwritingInfo.class);
 
     protected PacketList<ReinsuranceResultWithCommissionPacket> outContractFinancials = new PacketList<ReinsuranceResultWithCommissionPacket>(ReinsuranceResultWithCommissionPacket.class);
 
     /**
      * Defines the kind of contract and parametrization
      */
     protected IReinsuranceContractStrategy parmContractStrategy = ReinsuranceContractType.getTrivial();
 
     protected ICommissionStrategy parmCommissionStrategy = CommissionStrategyType.getNoCommission();
     /**
      * Defines the claim and underwriting info the contract will receive.
      * Namely, the net after contracts with lower inuring priority.
      * <p/>
      * Cave: Setting the inuring priority is not trivial. Make sure you have a
      * correct understanding before 'playing around' with it.
      */
     protected int parmInuringPriority = 0;
 
     private double coveredByReinsurer;
 
     public void doCalculation() {
         if (parmContractStrategy == null)
             throw new IllegalStateException("ReinsuranceContract.missingContractStrategy");
 
         parmContractStrategy.initBookkeepingFigures(inClaims, inUnderwritingInfo);
 
         initCoveredByReinsurer();
         Collections.sort(inClaims, SortClaimsByFractionOfPeriod.getInstance());
         if (isSenderWired(outUncoveredClaims)) {
             calculateClaims(inClaims, outCoveredClaims, outUncoveredClaims, this);
         }
         else {
             calculateCededClaims(inClaims, outCoveredClaims, this);
         }
 
         if (isSenderWired(outCoverUnderwritingInfo) || isSenderWired(outNetAfterCoverUnderwritingInfo)) {
             calculateCededUnderwritingInfos(inUnderwritingInfo, outCoverUnderwritingInfo, outCoveredClaims);
         }
 
         parmCommissionStrategy.calculateCommission(outCoveredClaims, outCoverUnderwritingInfo, false, false);
         if (isSenderWired(outNetAfterCoverUnderwritingInfo)) {
             calculateNetUnderwritingInfos(inUnderwritingInfo, outCoverUnderwritingInfo, outNetAfterCoverUnderwritingInfo, outCoveredClaims);
         }
         fillDevelopedClaimsChannels();
         if (isSenderWired(getOutContractFinancials())) {
             ReinsuranceResultWithCommissionPacket result = new ReinsuranceResultWithCommissionPacket();
             CededUnderwritingInfo underwritingInfo = CededUnderwritingInfoUtilities.aggregate(outCoverUnderwritingInfo);
             if (underwritingInfo != null) {
                 result.setCededPremium(-underwritingInfo.getPremium());
                 result.setCededCommission(-underwritingInfo.getCommission());
             }
             result.setCededClaim(ClaimUtilities.aggregateClaims(outCoveredClaims, this).getUltimate());
             if (result.getCededPremium() != 0) {
                 result.setCededLossRatio(result.getCededClaim() / -result.getCededPremium());
             }
             outContractFinancials.add(result);
         }
         parmContractStrategy.resetMemberInstances();
 
         for (UnderwritingInfo outCoverUnderwritingInfoPacket : outCoverUnderwritingInfo) {
             outCoverUnderwritingInfoPacket.setReinsuranceContract(this);
         }
     }
 
     protected void initCoveredByReinsurer() {
         coveredByReinsurer =  parmContractStrategy.covered();
     }
 
     protected void fillDevelopedClaimsChannels() {
         if (inClaims.size() > 0 && inClaims.get(0) instanceof ClaimDevelopmentLeanPacket) {
             for (Claim claim : inClaims) {
                 outClaimsDevelopmentLeanGross.add((ClaimDevelopmentLeanPacket) claim);
             }
         }
         if (outCoveredClaims.size() > 0 && outCoveredClaims.get(0) instanceof ClaimDevelopmentLeanPacket) {
             for (Claim claim : outUncoveredClaims) {
                 outClaimsDevelopmentLeanNet.add((ClaimDevelopmentLeanPacket) claim);
             }
             for (Claim claim : outCoveredClaims) {
                 outClaimsDevelopmentLeanCeded.add((ClaimDevelopmentLeanPacket) claim);
             }
         }
     }
 
     public void calculateClaims(List<Claim> grossClaims, List<Claim> cededClaims, List<Claim> netClaims, Component origin) {
         for (Claim claim : grossClaims) {
             Claim cededClaim = getCoveredClaim(claim, origin).scale(coveredByReinsurer);
             cededClaims.add(cededClaim);
 
             Claim claimNet = claim.getNetClaim(cededClaim);
            adjustAttachedExposureInfo(claim, claimNet);
             setClaimReferences(claimNet, claim, origin);
             netClaims.add(claimNet);
         }
     }
 
     public void calculateCededClaims(List<Claim> grossClaims, List<Claim> cededClaims, Component origin) {
         for (Claim claim : grossClaims) {
             cededClaims.add(getCoveredClaim(claim,origin).scale(coveredByReinsurer));
         }
     }
 
     /**
      * Calculates the ceded claim, with or without claim development information; for aggregate
      * covers with development information, the ceded part is allocated to each single claim.
      * <p/>
      * //todo(bgi): see if we need to refactor, move ceded development allocation into the Strategies and Claim packets
      *
      * @param grossClaim A Claim packet, possibly with development info (ClaimDevelopmentPacket or ClaimDevelopmentLeanPacket).
      * @param origin
      * @return A Claim packet (or ClaimDevelopmentPacket or ClaimDevelopmentLeanPacket), of the same type as grossClaim.
      */
     protected Claim getCoveredClaim(Claim grossClaim, Component origin) {
         Claim claimCeded;
         double coveredLoss = parmContractStrategy.allocateCededClaim(grossClaim);
         boolean hasDevelopment = grossClaim instanceof ClaimDevelopmentPacket ||
                 grossClaim instanceof ClaimDevelopmentLeanPacket;
         // calculate ceded development info (paid/reserved/changeInReserves) as appropriate to the Claim packet type
         if (hasDevelopment && parmContractStrategy instanceof IReinsuranceContractStrategyWithClaimsDevelopment) {
             // ceded development info has a different (allocation) factor from ceded claims
             if (grossClaim instanceof ClaimDevelopmentLeanPacket) {
                 ClaimDevelopmentLeanPacket claim = new ClaimDevelopmentLeanPacket(grossClaim);
                 claim.setPaid(((IReinsuranceContractStrategyWithClaimsDevelopment)
                         parmContractStrategy).allocateCededPaid((ClaimDevelopmentLeanPacket) grossClaim));
 //                claim.setReserved(claim.getUltimate() - claim.getPaid()); // perhaps not necessary since CDLP.getReserved calculates the same difference
                 claim.setReserved(claim.getUltimate() - claim.getPaid()); // perhaps not necessary since CDLP.getReserved calculates the same difference
                 claimCeded = claim;
             }
             else { // (grossClaim instanceof ClaimDevelopmentPacket)
                 ClaimDevelopmentPacket claim = new ClaimDevelopmentPacket(grossClaim);
                 claim.setPaid(((IReinsuranceContractStrategyWithClaimsDevelopment)
                         parmContractStrategy).allocateCededPaid((ClaimDevelopmentPacket) grossClaim));
                 double reserved = claim.getUltimate() - claim.getPaid();
                 claim.setChangeInReserves(reserved - claim.getReserved());
                 claim.setReserved(reserved);
                 claimCeded = claim;
             }
         }
         else {
             claimCeded = grossClaim.copy();
             double claimLoss = grossClaim.getUltimate();
             double cededFraction = claimLoss == 0 ? 1d : coveredLoss / claimLoss;
             claimCeded.scale(cededFraction);//scales ultimate & (for claims with development) paid & (for CDP) reserves & changeInReserves
         }
         // use the same ceded claim interface for all types of Claim packets and contract strategies
         claimCeded.setUltimate(coveredLoss);
        adjustAttachedExposureInfo(grossClaim, claimCeded);
         // set other common attributes
         setClaimReferences(claimCeded, grossClaim, origin);
         return claimCeded;
     }
 
    /** Adjust attached exposure info by the same factor as the ultimate claim value is reduced
     *  https://issuetracking.intuitive-collaboration.com/jira/browse/PMO-1624 */
    private void adjustAttachedExposureInfo(Claim originalClaim, Claim resultingClaim) {
        if (parmContractStrategy instanceof QuotaShareContractStrategy
                || parmContractStrategy instanceof SurplusContractStrategy
                || parmContractStrategy instanceof ReverseSurplusContractStrategy) {
            if (originalClaim.hasExposureInfo()) {
                double coverRatio = resultingClaim.getUltimate() / originalClaim.getUltimate();
                resultingClaim.setExposure(originalClaim.getExposure().copy().scale(coverRatio));
            }
        }
    }

     private void setClaimReferences(Claim claim, Claim grossClaim, Component origin) {
         claim.origin = origin;
         claim.setReinsuranceContract(this);
         if (grossClaim.getOriginalClaim() != null) {
             claim.setOriginalClaim(grossClaim.getOriginalClaim());
         }
         else {
             claim.setOriginalClaim(grossClaim);
         }
     }
 
     /**
      * the origin ceded values along a cascade of contracts should always point to the origin of the cascade
      *
      * @param underwritingInfo
      * @param derivedUnderwritingInfo
      */
     protected void setOriginalUnderwritingInfo(UnderwritingInfo underwritingInfo, UnderwritingInfo derivedUnderwritingInfo) {
         if (underwritingInfo != null && underwritingInfo.getOriginalUnderwritingInfo() != null) {
             derivedUnderwritingInfo.setOriginalUnderwritingInfo(underwritingInfo.getOriginalUnderwritingInfo());
         }
         else {
             derivedUnderwritingInfo.setOriginalUnderwritingInfo(underwritingInfo);
         }
     }
 
     protected void calculateUnderwritingInfos(List<UnderwritingInfo> grossUnderwritingInfos,
                                               List<UnderwritingInfo> cededUnderwritingInfos,
                                               List<UnderwritingInfo> netUnderwritingInfos,
                                               List<Claim> cededClaims) {
         parmContractStrategy.initCededPremiumAllocation(cededClaims, grossUnderwritingInfos);
         for (UnderwritingInfo underwritingInfo : grossUnderwritingInfos) {
             UnderwritingInfo cededUnderwritingInfo = parmContractStrategy.calculateCoverUnderwritingInfo(underwritingInfo, getTotalInitialReserves());
             setOriginalUnderwritingInfo(underwritingInfo, cededUnderwritingInfo);
             cededUnderwritingInfo.setReinsuranceContract(this);
             cededUnderwritingInfos.add(cededUnderwritingInfo);
             UnderwritingInfo netUnderwritingInfo = UnderwritingInfoUtilities.calculateNet(underwritingInfo, cededUnderwritingInfo);
             setOriginalUnderwritingInfo(underwritingInfo, netUnderwritingInfo);
             netUnderwritingInfo.setReinsuranceContract(this);
             netUnderwritingInfos.add(netUnderwritingInfo);
         }
     }
 
     protected void calculateNetUnderwritingInfos(List<UnderwritingInfo> grossUnderwritingInfos,
                                                  List<CededUnderwritingInfo> cededUnderwritingInfos,
                                                  List<UnderwritingInfo> netUnderwritingInfos,
                                                  List<Claim> cededClaims) {
         parmContractStrategy.initCededPremiumAllocation(cededClaims, grossUnderwritingInfos);
         for (int i = 0; i < grossUnderwritingInfos.size(); i++) {
             UnderwritingInfo netUnderwritingInfo = UnderwritingInfoUtilities.calculateNet(grossUnderwritingInfos.get(i), cededUnderwritingInfos.get(i));
             setOriginalUnderwritingInfo(grossUnderwritingInfos.get(i), netUnderwritingInfo);
             netUnderwritingInfo.setReinsuranceContract(this);
             netUnderwritingInfos.add(netUnderwritingInfo);
         }
     }
 
     protected void calculateCededUnderwritingInfos(List<UnderwritingInfo> grossUnderwritingInfos,
                                                    List<CededUnderwritingInfo> cededUnderwritingInfos,
                                                    List<Claim> cededClaims) {
         parmContractStrategy.initCededPremiumAllocation(cededClaims, grossUnderwritingInfos);
         for (UnderwritingInfo underwritingInfo : grossUnderwritingInfos) {
             CededUnderwritingInfo cededUnderwritingInfo = parmContractStrategy.calculateCoverUnderwritingInfo(underwritingInfo, getTotalInitialReserves());
             setOriginalUnderwritingInfo(underwritingInfo, cededUnderwritingInfo);
             cededUnderwritingInfo.setReinsuranceContract(this);
             cededUnderwritingInfos.add(cededUnderwritingInfo.scale(coveredByReinsurer));
         }
     }
 
     private double getTotalInitialReserves() {
         double totalInitialReserves = 0d;
         for (SingleValuePacket initialReserve : inInitialReserves) {
             totalInitialReserves += initialReserve.getValue();
         }
         return totalInitialReserves;
     }
 
     public IReinsuranceContractStrategy getParmContractStrategy() {
         return parmContractStrategy;
     }
 
     public void setParmContractStrategy(IReinsuranceContractStrategy parmContractStrategy) {
         this.parmContractStrategy = parmContractStrategy;
     }
 
     public int getParmInuringPriority() {
         return parmInuringPriority;
     }
 
     public void setParmInuringPriority(int parmInuringPriority) {
         this.parmInuringPriority = parmInuringPriority;
     }
 
     public PacketList<Claim> getInClaims() {
         return inClaims;
     }
 
     public void setInClaims(PacketList<Claim> inClaims) {
         this.inClaims = inClaims;
     }
 
     public PacketList<UnderwritingInfo> getInUnderwritingInfo() {
         return inUnderwritingInfo;
     }
 
     public void setInUnderwritingInfo(PacketList<UnderwritingInfo> inUnderwritingInfo) {
         this.inUnderwritingInfo = inUnderwritingInfo;
     }
 
     public PacketList<Claim> getOutUncoveredClaims() {
         return outUncoveredClaims;
     }
 
     public void setOutUncoveredClaims(PacketList<Claim> outUncoveredClaims) {
         this.outUncoveredClaims = outUncoveredClaims;
     }
 
     public PacketList<Claim> getOutCoveredClaims() {
         return outCoveredClaims;
     }
 
     public void setOutCoveredClaims(PacketList<Claim> outCoveredClaims) {
         this.outCoveredClaims = outCoveredClaims;
     }
 
     public PacketList<UnderwritingInfo> getOutNetAfterCoverUnderwritingInfo() {
         return outNetAfterCoverUnderwritingInfo;
     }
 
     public void setOutNetAfterCoverUnderwritingInfo(PacketList<UnderwritingInfo> outNetAfterCoverUnderwritingInfo) {
         this.outNetAfterCoverUnderwritingInfo = outNetAfterCoverUnderwritingInfo;
     }
 
     public PacketList<CededUnderwritingInfo> getOutCoverUnderwritingInfo() {
         return outCoverUnderwritingInfo;
     }
 
     public void setOutCoverUnderwritingInfo(PacketList<CededUnderwritingInfo> outCoverUnderwritingInfo) {
         this.outCoverUnderwritingInfo = outCoverUnderwritingInfo;
     }
 
     public ICommissionStrategy getParmCommissionStrategy() {
         return parmCommissionStrategy;
     }
 
     public void setParmCommissionStrategy(ICommissionStrategy parmCommissionStrategy) {
         this.parmCommissionStrategy = parmCommissionStrategy;
     }
 
     public PacketList<ReinsuranceResultWithCommissionPacket> getOutContractFinancials() {
         return outContractFinancials;
     }
 
     public void setOutContractFinancials(PacketList<ReinsuranceResultWithCommissionPacket> outContractFinancials) {
         this.outContractFinancials = outContractFinancials;
     }
 
     public PacketList<SingleValuePacket> getInInitialReserves() {
         return inInitialReserves;
     }
 
     public void setInInitialReserves(PacketList<SingleValuePacket> inInitialReserves) {
         this.inInitialReserves = inInitialReserves;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsDevelopmentLeanNet() {
         return outClaimsDevelopmentLeanNet;
     }
 
     public void setOutClaimsDevelopmentLeanNet(PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanNet) {
         this.outClaimsDevelopmentLeanNet = outClaimsDevelopmentLeanNet;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsDevelopmentLeanGross() {
         return outClaimsDevelopmentLeanGross;
     }
 
     public void setOutClaimsDevelopmentLeanGross(PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanGross) {
         this.outClaimsDevelopmentLeanGross = outClaimsDevelopmentLeanGross;
     }
 
     public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsDevelopmentLeanCeded() {
         return outClaimsDevelopmentLeanCeded;
     }
 
     public void setOutClaimsDevelopmentLeanCeded(PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanCeded) {
         this.outClaimsDevelopmentLeanCeded = outClaimsDevelopmentLeanCeded;
     }
 
     public double getCoveredByReinsurer() {
         return coveredByReinsurer;
     }
 
     public void setCoveredByReinsurer(double coveredByReinsurer) {
         this.coveredByReinsurer = coveredByReinsurer;
     }
 }
