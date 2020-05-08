 package org.pillarone.riskanalytics.domain.assets;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateTime;
 import org.pillarone.riskanalytics.core.components.Component;
 import org.pillarone.riskanalytics.core.components.PeriodStore;
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope;
 import org.pillarone.riskanalytics.domain.assets.output.BondAccounting;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author cyril (dot) neyme (at) kpmg (dot) fr
  */
 public class AssetEngine extends Component {
     static Log LOG = LogFactory.getLog(AssetEngine.class);
     private SimulationScope simulationScope;
     private PeriodStore periodStore;
     private static final String FIXED_INTEREST_RATE_BONDS = "fixedInterestRateBonds";
     private static final String TERM_STRUCTURE = "term structure";
 
     private PacketList<CashParameters> inCashParameters = new PacketList<CashParameters>(CashParameters.class);
     private PacketList<BondParameters> inBondParameters = new PacketList<BondParameters>(BondParameters.class);
     private PacketList<YieldModellingChoices> inYieldModellingChoices = new PacketList<YieldModellingChoices>(YieldModellingChoices.class);
     private PacketList<FeesParameters> inFeesParameters = new PacketList<FeesParameters>(FeesParameters.class);
     //allocators have to be set as well as other assets.
 
     private PacketList<BondAccounting> outBondAccounting = new PacketList<BondAccounting>(BondAccounting.class);
 
     private List<FixedInterestRateBond> fixedInterestRateBonds;
     private double capitalGain;
     private BondAccounting bondAccounting;
     private FeesParameters feesParameters;
     private ITermStructure termStructure;
 
 
     //todo(cne) ATTN: the following calculates for each period notwithstanding for example the time since one bond has expired, or if the maturity date is within the period, or at the calculation date
 
     protected void doCalculation() {
 
         bondAccounting = new BondAccounting();
         manageCash();
         valuateBonds();
 
         outBondAccounting.add(bondAccounting);
 
     }
 
 
     private void valuateBonds() {
 
         //pick up the term structure
         termStructure = (ITermStructure) periodStore.getFirstPeriod(TERM_STRUCTURE);
         if (termStructure == null) {
             YieldModellingChoices packet = inYieldModellingChoices.get(0);
             if (packet.getClass() == CIRYieldModellingChoices.class) {
                 termStructure = new YieldCurveCIRStrategy(packet);
             } else if (packet.getClass() == ConstantYieldModellingChoices.class) {
                 termStructure = new ConstantYieldCurveStrategy(((ConstantYieldModellingChoices) packet).getRate());
             }
             periodStore.put(TERM_STRUCTURE, termStructure);
         }
 
 
         // initialize received bonds of current period and add them to the list
 
         if (periodStore.exists(FIXED_INTEREST_RATE_BONDS)) {
             fixedInterestRateBonds = (List<FixedInterestRateBond>) periodStore.getCloned(FIXED_INTEREST_RATE_BONDS, PeriodStore.LAST_PERIOD);
         } else {
             fixedInterestRateBonds = new ArrayList<FixedInterestRateBond>();
         }
         for (BondParameters bond : inBondParameters) {
             FixedInterestRateBond newBond = new FixedInterestRateBond(bond);
             LOG.info(getCurrentIteration() + ", " + getCurrentPeriodStartDate() + ", " + bond.bondType);
 
             fixedInterestRateBonds.add(newBond);
         }
         periodStore.put(FIXED_INTEREST_RATE_BONDS, fixedInterestRateBonds, 0);
 
         feesParameters = inFeesParameters.get(0);
 
         // calculate figures
         double marketValue = 0d;
 
         int sizeOfFixedInterestRateBonds = fixedInterestRateBonds.size();
 
         //the market value calculation shall not add all the bonds in fixedInterestRateBonds because this list contains also the same bonds from previous periods.
         //todo(cne) the following may not be appropriate when adding new bonds in future periods (thus when considering asset allocation)
 
         for (int i = 0; i < inBondParameters.size(); i++) {
             marketValue += fixedInterestRateBonds.get(sizeOfFixedInterestRateBonds - i - 1).getMarketValue(getBeginOfFirstPeriodDate(),
                     getCurrentPeriodStartDate(), termStructure);
         }
         bondAccounting.setMarketValue(marketValue);
 
         /* for (FixedInterestRateBond bond : fixedInterestRateBonds) {
             marketValue += bond.getMarketValue(simulationContext.getBeginOfFirstPeriodDate(), simulationContext.getCurrentPeriodStartDate(), termStructure);
         }
         bondAccounting.setMarketValue(marketValue);*/
 
         for (FixedInterestRateBond bond : fixedInterestRateBonds) {
             //get the capital gain, used notably for cash management
             capitalGain += bond.getCapitalGain(getCurrentPeriodStartDate(), termStructure);
 
             //calculate bookValue of this bond portfolio
             bondAccounting.addBookValue(bond.getBookValue() * bond.quantity);
 
             //compute portfolio duration as the weighted average duration of all the bonds in the portfolio weighted by their dollar values (i.e. market values)
             bondAccounting.addDuration(bond.getDurationDiscrete(getCurrentPeriodStartDate(),
                     termStructure) * bond.getMarketValue(getCurrentPeriodStartDate(), termStructure) / marketValue);
 
             //calculate cash from maturities by adding the face values of the expired bonds.
             if (bond.isExpired(getCurrentPeriodStartDate())) {
                 bondAccounting.addCashFromMaturities(bond.getFaceValue() * bond.quantity);
             }
 
             //get the transaction costs + the deposit fees, as a percentage respectively of cash invested and market v alue. The rate is set by user
             bondAccounting.addExpenses(feesParameters.getBondTransactionCostsRate() * bondAccounting.getCashInvested() +
                     feesParameters.getBondDepositFeesPercentageOfMarketValue() * bondAccounting.getMarketValue());
         }
 
         //the following functions shall get former market values, i.e. results from former calculations.
         int i = 0;
         while (i < fixedInterestRateBonds.size() - inBondParameters.size()) {
 
             bondAccounting.addValueAdjustments(fixedInterestRateBonds.get(i + inBondParameters.size()).getMarketValue(getBeginOfFirstPeriodDate(),
                     getNextPeriodStartDate(), termStructure) - fixedInterestRateBonds.get(i).getMarketValue(getBeginOfFirstPeriodDate(),
                     getCurrentPeriodStartDate(), termStructure));
 
             bondAccounting.addMarkedToMarketReturn((fixedInterestRateBonds.get(i + inBondParameters.size()).getMarketValue(getBeginOfFirstPeriodDate(),
                     getNextPeriodStartDate(), termStructure) - fixedInterestRateBonds.get(i).getMarketValue(getBeginOfFirstPeriodDate(),
                     getCurrentPeriodStartDate(), termStructure) -
                     bondAccounting.getCashInvested() + bondAccounting.getCashIncome() - bondAccounting.getExpenses()) / marketValue);
             //fixedInterestRateBonds.get(i).getMarketValue(simulationContext.getBeginOfFirstPeriodDate(), simulationContext.getCurrentPeriodStartDate(), termStructure));
 
             bondAccounting.addBookReturn((fixedInterestRateBonds.get(i + inBondParameters.size()).getBookValue() - fixedInterestRateBonds.get(i).getBookValue()) / marketValue);
             //fixedInterestRateBonds.get(i).getMarketValue(simulationContext.getCurrentPeriodStartDate(), termStructure));
 
             i++;
         }
 
 
     }
 
 
     private void manageCash() {
         CashParameters cashPacket = inCashParameters.get(0);
 
         double availableCash = cashPacket.getInitialCash() - cashPacket.getMinimalCashLevel() + capitalGain;
 
         //todo(cne) only bonds are available, the cash model is therefore simple, other assets have to be implemented to get a relevant allocation
 
         if (availableCash > cashPacket.getMaximalCashLevel()) {
             //set new initial, minimal, maximal cash levels, return purchase order
             for (FixedInterestRateBond bond : fixedInterestRateBonds) {
                 bondAccounting.addCashInvested(bond.invest(availableCash));
                 availableCash -= bond.invest(availableCash);
             }
         } else if (availableCash < cashPacket.getMinimalCashLevel()) {
             double neededCash = cashPacket.getMinimalCashLevel() - availableCash;
             for (FixedInterestRateBond bond : fixedInterestRateBonds) {
                 if (neededCash > 0) {
                     bondAccounting.addCashFromSales(bond.sell(neededCash));
                     neededCash -= bond.sell(neededCash);
                 }
             }
         }
         capitalGain = 0d;
     }
 
     public PacketList<BondParameters> getInBondParameters() {
         return inBondParameters;
     }
 
     public void setInBondParameters(PacketList<BondParameters> inBondParameters) {
         this.inBondParameters = inBondParameters;
     }
 
     public PeriodStore getPeriodStore() {
         return periodStore;
     }
 
     public void setPeriodStore(PeriodStore periodStore) {
         this.periodStore = periodStore;
     }
 
    public PacketList<YieldModellingChoices> getInModellingChoices() {
         return inYieldModellingChoices;
     }
 
    public void setInModellingChoices(PacketList<YieldModellingChoices> inYieldModellingChoices) {
         this.inYieldModellingChoices = inYieldModellingChoices;
     }
 
     public PacketList<BondAccounting> getOutBondAccounting() {
         return outBondAccounting;
     }
 
     public void setOutBondAccounting(PacketList<BondAccounting> outBondAccounting) {
         this.outBondAccounting = outBondAccounting;
     }
 
     public PacketList<CashParameters> getInCashParameters() {
         return inCashParameters;
     }
 
     public void setInCashParameters(PacketList<CashParameters> inCashParameters) {
         this.inCashParameters = inCashParameters;
     }
 
     public PacketList<FeesParameters> getInFeesParameters() {
         return inFeesParameters;
     }
 
     public void setInFeesParameters(PacketList<FeesParameters> inFeesParameters) {
         this.inFeesParameters = inFeesParameters;
     }
 
     public SimulationScope getSimulationScope() {
         return simulationScope;
     }
 
     public void setSimulationScope(SimulationScope simulationScope) {
         this.simulationScope = simulationScope;
     }
 
     private DateTime getCurrentPeriodStartDate() {
         return simulationScope.getCurrentPeriodStartDate();
     }
 
     private int getCurrentIteration() {
         return simulationScope.getCurrentIteration();
     }
 
     private DateTime getBeginOfFirstPeriodDate() {
         return simulationScope.getBeginOfFirstPeriodDate();
     }
 
     private DateTime getNextPeriodStartDate() {
         return simulationScope.getNextPeriodStartDate();
     }
 
 
 }
