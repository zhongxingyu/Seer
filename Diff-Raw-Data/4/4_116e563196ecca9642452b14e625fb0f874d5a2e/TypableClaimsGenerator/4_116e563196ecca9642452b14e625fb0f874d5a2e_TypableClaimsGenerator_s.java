 package org.pillarone.riskanalytics.domain.pc.generators.claims;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.pillarone.riskanalytics.core.model.Model;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter;
 import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope;
 import org.pillarone.riskanalytics.domain.pc.claims.Claim;
 import org.pillarone.riskanalytics.domain.pc.claims.ClaimPacketFactory;
 import org.pillarone.riskanalytics.domain.pc.claims.IRiskAllocatorStrategy;
 import org.pillarone.riskanalytics.domain.pc.claims.RiskAllocatorType;
 import org.pillarone.riskanalytics.domain.pc.constants.ClaimType;
 import org.pillarone.riskanalytics.domain.pc.constants.FrequencyBase;
 import org.pillarone.riskanalytics.domain.pc.constants.FrequencySeverityClaimType;
 import org.pillarone.riskanalytics.domain.pc.generators.GeneratorCachingComponent;
 import org.pillarone.riskanalytics.domain.pc.generators.copulas.DependenceStream;
 import org.pillarone.riskanalytics.domain.pc.generators.copulas.EventDependenceStream;
 import org.pillarone.riskanalytics.domain.pc.generators.frequency.Frequency;
 import org.pillarone.riskanalytics.domain.pc.generators.severities.Event;
 import org.pillarone.riskanalytics.domain.pc.generators.severities.EventSeverity;
 import org.pillarone.riskanalytics.domain.pc.underwriting.IUnderwritingInfoMarker;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingFilterUtilities;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
 import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingUtilities;
 import org.pillarone.riskanalytics.domain.utils.*;
 import org.pillarone.riskanalytics.domain.utils.randomnumbers.UniformDoubleList;
 import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
 import umontreal.iro.lecuyer.probdist.Distribution;
 import umontreal.iro.lecuyer.probdist.TruncatedDist;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  *
  * Typisierbare Schadengeneratoren in parmClaimsModel
  *
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class TypableClaimsGenerator extends GeneratorCachingComponent implements PerilMarker {
 
     private SimulationScope simulationScope;
 
     /**
      * needs to be connected only if a none absolute base is selected
      */
     private PacketList<UnderwritingInfo> inUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
     /**
      * needs to be connected only if the claims generator was selected as target in a copula
      */
     private PacketList<DependenceStream> inProbabilities = new PacketList<DependenceStream>(DependenceStream.class);
     /**
      * needs to be connected only ...
      */
     private PacketList<EventDependenceStream> inEventSeverities = new PacketList<EventDependenceStream>(EventDependenceStream.class);
 
     private ComboBoxTableMultiDimensionalParameter parmUnderwritingInformation = new ComboBoxTableMultiDimensionalParameter(
         Arrays.asList(""), Arrays.asList("Underwriting Information"), IUnderwritingInfoMarker.class);
 
     // attritional, frequency average attritional, ...
     private IClaimsGeneratorStrategy parmClaimsModel = ClaimsGeneratorType.getStrategy(ClaimsGeneratorType.ATTRITIONAL, new HashMap());
     /**
      * Defines the kind of allocation and parameterization
      */
     private IRiskAllocatorStrategy parmAssociateExposureInfo = RiskAllocatorType.getStrategy(RiskAllocatorType.NONE, new HashMap());
 
     /**
      * claims which source is a covered line
      */
     private PacketList<Claim> outClaims = new PacketList<Claim>(Claim.class);
 
     private PacketList<Frequency> outClaimsNumber = new PacketList<Frequency>(Frequency.class);
 
     /**
      * underwriting info of segments selected with parmUnderwritingInformation
      */
     private PacketList<UnderwritingInfo> outUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
 
     /** used for date generation for single claims */
     private IRandomNumberGenerator dateGenerator = RandomNumberGeneratorFactory.getUniformGenerator();
 
     protected void doCalculation() {
         outUnderwritingInfo.addAll(
             UnderwritingFilterUtilities.filterUnderwritingInfo(
                 inUnderwritingInfo,
                 parmUnderwritingInformation.getValuesAsObjects()));
         List<Double> claimValues = new ArrayList<Double>();
         List<Event> events = new ArrayList<Event>();
         PacketList<Claim> claims = new PacketList<Claim>(Claim.class);
         if (!(parmClaimsModel instanceof NoneClaimsGeneratorStrategy)) {
             double scalingFactor = UnderwritingUtilities.scaleFactor(outUnderwritingInfo, parmClaimsModel.getClaimsSizeBase());
             ClaimType claimType = ClaimType.ATTRITIONAL;
             if (parmClaimsModel instanceof AttritionalClaimsGeneratorStrategy) {
                 claimType = ClaimType.ATTRITIONAL;
                 if (this.isReceiverWired(inProbabilities)) {
                     List<Double> probabilities = filterProbabilities();
                     if (probabilities.size() > 1) {
                        throw new IllegalArgumentException("An attritional claims model accepts at most one probability.\n" +
                            "There is currently more than one correlation component sending a probability for " +
                            "the claims generator " + this.getNormalizedName() + ".");
                     } else {
                         claimValues = calculateClaimsValues(
                             probabilities,
                             parmClaimsModel.getClaimsSizeDistribution(),
                             parmClaimsModel.getClaimsSizeModification());
                     }
                 }
                 if (claimValues == null || claimValues.size() == 0) {
                     claimValues = generateClaimsValues(1,
                         parmClaimsModel.getClaimsSizeDistribution(),
                         parmClaimsModel.getClaimsSizeModification());
                 }
 
             } else if (parmClaimsModel instanceof IFrequencyClaimsGeneratorStrategy) {
                 double frequency = generateFrequency(
                     ((IFrequencyClaimsGeneratorStrategy) parmClaimsModel).getFrequencyDistribution(),
                     ((IFrequencyClaimsGeneratorStrategy) parmClaimsModel).getFrequencyModification(),
                     ((IFrequencyClaimsGeneratorStrategy) parmClaimsModel).getFrequencyBase());
                 if (parmClaimsModel instanceof FrequencyAverageAttritionalClaimsGeneratorStrategy) {
                     claimType = ClaimType.ATTRITIONAL;
 
                     scalingFactor *= frequency;
                     claimValues = generateClaimsValues(1,
                         parmClaimsModel.getClaimsSizeDistribution(),
                         parmClaimsModel.getClaimsSizeModification());
                 } else if (parmClaimsModel instanceof IFrequencySingleClaimsGeneratorStrategy) {
                     if (((IFrequencySingleClaimsGeneratorStrategy) parmClaimsModel).getProduceClaim().equals(FrequencySeverityClaimType.AGGREGATED_EVENT)) {
                         claimType = ClaimType.AGGREGATED_EVENT;
                         events = generateEvents((int) frequency);
                     } else if (((IFrequencySingleClaimsGeneratorStrategy) parmClaimsModel).getProduceClaim().equals(FrequencySeverityClaimType.SINGLE)) {
                         claimType = ClaimType.SINGLE;
                     }
                     claimValues = generateClaimsValues((int) frequency,
                         parmClaimsModel.getClaimsSizeDistribution(),
                         parmClaimsModel.getClaimsSizeModification());
                 }
             } else if (parmClaimsModel instanceof ExternalSeverityClaimsGeneratorStrategy) {
                 if (this.isReceiverWired(inEventSeverities)) {
                     List<EventSeverity> filteredEventSeverities = filterEvents();
                     claimValues = calculateEventClaimsValues(filteredEventSeverities, parmClaimsModel.getClaimsSizeDistribution());
                     events = extractEvents(filteredEventSeverities);
                 } else {
                     throw new IllegalArgumentException("As inProbabilities is not wired, the selected claims model is not supported");
                 }
             } else {
                 throw new NotImplementedException(parmClaimsModel.toString());
             }
             if (events.size() == 0) {
                 if (claimValues.size() == 0) {
                     claimValues.add(0d);
                 }
                 for (Double claimValue : claimValues) {
                     Claim claim = ClaimPacketFactory.createPacket();
                     claim.origin = this;
                     claim.setPeril(this);
                     claim.setClaimType(claimType);
                     claim.setUltimate(claimValue * scalingFactor);
                     setFractionOfPeriod(claimType, claim);
                     claims.add(claim);
                 }
             } else {
                 for (int i = 0; i < claimValues.size(); i++) {
                     Claim claim = ClaimPacketFactory.createPacket();
                     claim.origin = this;
                     claim.setPeril(this);
                     claim.setClaimType(claimType);
                     claim.setUltimate(claimValues.get(i) * scalingFactor);
                     claim.setEvent(events.get(i));
                     claim.setFractionOfPeriod(claim.getEvent().getDate());
                     claims.add(claim);
                 }
             }
         }
         outClaims.addAll(parmAssociateExposureInfo.getAllocatedClaims(claims, outUnderwritingInfo));
         Frequency frequency = new Frequency();
         frequency.setValue(outClaims.size());
         outClaimsNumber.add(frequency);
     }
 
     protected void setFractionOfPeriod(ClaimType claimType, Claim claim) {
         if (parmClaimsModel instanceof IOccurrenceClaimsGeneratorStrategy) {
             IRandomNumberGenerator generator = getCachedGenerator(((IOccurrenceClaimsGeneratorStrategy) parmClaimsModel).getOccurrenceDistribution(), parmClaimsModel.getClaimsSizeModification());
             claim.setFractionOfPeriod((Double) generator.nextValue());
         }
         else {
             if (claimType.equals(ClaimType.ATTRITIONAL)) {
                 claim.setFractionOfPeriod(0.5d);
             }
             else {
                 claim.setFractionOfPeriod((Double) dateGenerator.nextValue());
             }
         }
     }
 
     protected Model getModel() {
         return simulationScope.getModel();
     }
 
     /**
      * Filter the claims generator's inProbabilites: only inProbabilites with corresponding
      * marginals string matching the claims generator's name are used.
      */
     protected List<Double> filterProbabilities() {
         List<Double> probabilities = new ArrayList<Double>();
         for (DependenceStream stream : inProbabilities) {
             // todo(sku): refactor in order to use component references in marginals
             int index = stream.marginals.indexOf(getNormalizedName());
             if (index > -1) {
                 probabilities.add((Double) stream.probabilities.get(index));
             }
         }
         return probabilities;
     }
 
     protected List<Event> extractEvents(List<EventSeverity> eventSeverities) {
         List<Event> events = new ArrayList<Event>();
         for (EventSeverity eventSeverity : eventSeverities) {
             events.add(eventSeverity.event);
         }
         return events;
     }
 
     protected List<EventSeverity> filterEvents() {
         List<EventSeverity> eventSeverities = new ArrayList<EventSeverity>();
         for (EventDependenceStream stream : inEventSeverities) {
             int index = stream.marginals.indexOf(getNormalizedName());
             if (index > -1) {
                 eventSeverities.add(stream.severities.get(index));
             }
         }
         return eventSeverities;
     }
 
     protected double generateFrequency(RandomDistribution distribution, DistributionModified modification, FrequencyBase frequencyBase) {
         double frequency = 0;
         IRandomNumberGenerator generator = getCachedGenerator(distribution, modification);
         // todo(sku): refactor in order to use IExposureBaseStrategy or an equivalent construct
         if (frequencyBase.equals(FrequencyBase.NUMBER_OF_POLICIES)) {
             double scaleFactor = 0;
             for (UnderwritingInfo underwritingInfo : inUnderwritingInfo) {
                 scaleFactor += underwritingInfo.numberOfPolicies;
             }
             frequency = ((Double) generator.nextValue()) * scaleFactor;
         } else {
             frequency = generator.nextValue().intValue();
         }
         return frequency;
     }
 
     protected List<Double> generateClaimsValues(int number, RandomDistribution distribution, DistributionModified modification) {
         if (distribution == null) {
             throw new IllegalStateException("A distribution must be set");
         }
         IRandomNumberGenerator generator = getCachedGenerator(distribution, modification);
         List<Double> claimValues = new ArrayList<Double>(number);
         for (int i = 0; i < number; i++) {
             claimValues.add((Double) generator.nextValue());
         }
         return claimValues;
     }
 
     protected List<Event> generateEvents(int number) {
         List<Double> dates = UniformDoubleList.getDoubles(number, true);
         List<Event> events = new ArrayList<Event>(number);
         for (Double date : dates) {
             Event event = new Event();
             event.setDate(date);
             events.add(event);
         }
         return events;
     }
 
     // todo(sku): refactor once the variate distributions are properly refactored
     protected List<Double> calculateClaimsValues(List<Double> probabilities, RandomDistribution distribution, DistributionModified modification) {
         Distribution dist = distribution.getDistribution();
         if (modification.getType().equals(DistributionModifier.CENSORED) || modification.getType().equals(DistributionModifier.CENSOREDSHIFT)) {
             dist = new CensoredDist(distribution.getDistribution(),
                     (Double) modification.getParameters().get("min"), (Double) modification.getParameters().get("max"));
         }
         else if (modification.getType().equals(DistributionModifier.TRUNCATED) || modification.getType().equals(DistributionModifier.TRUNCATEDSHIFT)) {
             Double leftBoundary = (Double) modification.getParameters().get("min");
             Double rightBoundary = (Double) modification.getParameters().get("max");
             dist = new TruncatedDist((ContinuousDistribution) distribution.getDistribution(), leftBoundary, rightBoundary);
         }
 //        else if (modification.getType().equals(DistributionModifier.LEFTTRUNCATEDRIGHTCENSORED)) {
 //            Double leftBoundary = (Double) modification.getParameters().get("min");
 //            Double rightBoundary = (Double) modification.getParameters().get("max");
 //            dist = new CensoredDist(new TruncatedDist((ContinuousDistribution) distribution.getDistribution(),
 //                                        leftBoundary, (double) Double.POSITIVE_INFINITY),
 //                                    (double) Double.NEGATIVE_INFINITY, rightBoundary);
 //        }
         List<Double> claimValues = new ArrayList<Double>(probabilities.size());
         double shift = modification.getParameters().get("shift") == null ? 0 : (Double) modification.getParameters().get("shift");
         for (Double probability : probabilities) {
             claimValues.add(dist.inverseF(probability) + shift);
         }
         return claimValues;
     }
 
     protected List<Double> calculateEventClaimsValues(List<EventSeverity> eventSeverities, RandomDistribution distribution) {
         List<Double> claimValues = new ArrayList<Double>(eventSeverities.size());
         for (EventSeverity severity : eventSeverities) {
             claimValues.add(distribution.getDistribution().inverseF(severity.value));
         }
         return claimValues;
     }
 
     public PacketList<UnderwritingInfo> getInUnderwritingInfo() {
         return inUnderwritingInfo;
     }
 
     public void setInUnderwritingInfo(PacketList<UnderwritingInfo> inUnderwritingInfo) {
         this.inUnderwritingInfo = inUnderwritingInfo;
     }
 
     public PacketList<DependenceStream> getInProbabilities() {
         return inProbabilities;
     }
 
     public void setInProbabilities(PacketList<DependenceStream> inProbabilities) {
         this.inProbabilities = inProbabilities;
     }
 
     public PacketList<EventDependenceStream> getInEventSeverities() {
         return inEventSeverities;
     }
 
     public void setInEventSeverities(PacketList<EventDependenceStream> inEventSeverities) {
         this.inEventSeverities = inEventSeverities;
     }
 
     public ComboBoxTableMultiDimensionalParameter getParmUnderwritingInformation() {
         return parmUnderwritingInformation;
     }
 
     public void setParmUnderwritingInformation(ComboBoxTableMultiDimensionalParameter parmUnderwritingInformation) {
         this.parmUnderwritingInformation = parmUnderwritingInformation;
     }    
 
     public IClaimsGeneratorStrategy getParmClaimsModel() {
         return parmClaimsModel;
     }
 
     public void setParmClaimsModel(IClaimsGeneratorStrategy parmClaimsModel) {
         this.parmClaimsModel = parmClaimsModel;
     }
 
     public PacketList<Claim> getOutClaims() {
         return outClaims;
     }
 
     public void setOutClaims(PacketList<Claim> outClaims) {
         this.outClaims = outClaims;
     }
 
     public PacketList<UnderwritingInfo> getOutUnderwritingInfo() {
         return outUnderwritingInfo;
     }
 
     public void setOutUnderwritingInfo(PacketList<UnderwritingInfo> outUnderwritingInfo) {
         this.outUnderwritingInfo = outUnderwritingInfo;
     }
 
     public IRiskAllocatorStrategy getParmAssociateExposureInfo() {
         return parmAssociateExposureInfo;
     }
 
     public void setParmAssociateExposureInfo(IRiskAllocatorStrategy parmAssociateExposureInfo) {
         this.parmAssociateExposureInfo = parmAssociateExposureInfo;
     }
 
     public SimulationScope getSimulationScope() {
         return simulationScope;
     }
 
     public void setSimulationScope(SimulationScope simulationScope) {
         this.simulationScope = simulationScope;
     }
 
     public PacketList<Frequency> getOutClaimsNumber() {
         return outClaimsNumber;
     }
 
     public void setOutClaimsNumber(PacketList<Frequency> outClaimsNumber) {
         this.outClaimsNumber = outClaimsNumber;
     }
 }
