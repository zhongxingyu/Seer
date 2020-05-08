 package org.bh.plugin.fte;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DoubleValue;
 
 /**
  * This class provides the functionality to calculate an enterprise value with
  * the Flow to Equity method. The formula for the enterprise value is </br>
  * UW[t] = (UW[t+1] + FTE[t+1]) / (EKrFTE[t+1] + 1) </br> with:</br> <li>FTE =
  * FCF + TaxShield - Z + DA</li> <li>Z = Interest payment on debt and DA = Debt
  * amortization</li> <li>UW[T] = FTE[T] / EKrFTE[T]</li>
  * 
  * @author Sebastian
  * @author Norman
  * @version 1.0, 25.12.2009
  * @version 1.1, 06.01.2010 added Logger
  */
 public class FTECalculator implements IShareholderValueCalculator {
 	private static final Logger LOG = Logger.getLogger(FTECalculator.class);
 
 	private static final String UNIQUE_ID = "fte";
 	private static final String GUI_KEY = "fte";
 
 	public enum Result {
 		/**
 		 * The advantage for the present value when having debts.
 		 * </br><b>Array</b> but only first value [0] interesting
 		 */
 		PRESENT_VALUE_TAX_SHIELD,
 		/**
 		 * FK * FKr </br><b>Array</b>
 		 */
 		FLOW_TO_EQUITY_INTEREST,
 		/**
 		 * Advantage for FTE when having debts </br><b>Array</b>
 		 */
 		FLOW_TO_EQUITY_TAX_SHIELD,
 		/**
 		 * FCF + FTE_Tax_Shield - FTE_Interest </br><b>Array</b>
 		 */
 		FLOW_TO_EQUITY,
 		/**
 		 * FK[t+1] - FK[t] </br><b>Array</b>
 		 */
 		DEBT_AMORTISATION,
 		/**
 		 * Variable equity return rate </br><b>Array</b>
 		 */
 		EQUITY_RETURN_RATE_FTE;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	@Override
 	public Map<String, Calculable[]> calculate(DTOScenario scenario,
 			boolean verboseLogging) {
 
 		if (verboseLogging)
 			LOG.info("----- FTE procedure started -----");
 
 		Calculable[] fcf = new Calculable[scenario.getChildrenSize() +1];
 		Calculable[] fk = new Calculable[scenario.getChildrenSize() +1];
 		int i = 0;
 		if (verboseLogging)
 			LOG.debug("Input Values: FCF ; Liablities(FK):");
 		for (DTOPeriod period : scenario.getChildren()) {
 			if (i > 0)
 				fcf[i] = period.getFCF();
 			fk[i] = period.getLiabilities();
 			if(i == fcf.length - 2){
 				fcf[i + 1] = period.getFCF();
 				fk[i + 1] = period.getLiabilities();
 			}
 			if (verboseLogging && LOG.isDebugEnabled()) {
 				LOG.debug("\t" + period.get(DTOPeriod.Key.NAME) + ": " + fcf[i]
 						+ " ; " + fk[i]);
 			}
 			i++;
 		}
 		// Get all needed input parameters for the calculation
 		int T = fcf.length - 1;
 
 		// Instantiate the result arrays
 		Calculable[] uw = new Calculable[fcf.length];
 		Calculable ekr = scenario.getCalculable(DTOScenario.Key.REK);
 		Calculable fkr = scenario.getCalculable(DTOScenario.Key.RFK);
 		Calculable s = scenario.getTax();
 
 		// Instantiate result Arrays
 		Calculable[] FTEInterest = new Calculable[fcf.length]; // Interest
 		// payment on
 		// debt
 		// (Zinszahlung
 		// auf FK)
 		Calculable[] FTETaxShield = new Calculable[fcf.length]; // Tax benefit
 		// from debt
 		// (Steuervorteil
 		// aus
 		// Verschuldung)
 		Calculable[] FTE = new Calculable[fcf.length]; // Flow to equity (EK
 		// Zugang)
 		Calculable[] FTEDebtAmort = new Calculable[fcf.length]; // Debt
 		// amortization
 		// (Differenz
 		// FK[t] zu
 		// FK[t-1])
 		Calculable[] PresentValueTaxShield = new Calculable[fcf.length];
 		Calculable[] EKrFTE = new Calculable[fcf.length];
 
 		// Instantiate helper array
 		Calculable[] uwLastCalc = new Calculable[fcf.length];
 
 		// ### Step 1
 		// Calculation of FTE
 		// FTE = FCF + TaxShield - Z + DA
 		// with Z = Interest payment on debt and DA = Debt amortization
 		FTEInterest[T] = fkr.mul(fk[T]);
 		FTETaxShield[T] = s.mul(fkr).mul(fk[T]);
 		FTE[T] = fcf[T].add(FTETaxShield[T]).sub(FTEInterest[T]);
 		if (verboseLogging && LOG.isDebugEnabled()) {
 			LOG.debug("FTEInterest[T] (FKR * FK[T]): " + FTEInterest[T]);
 			LOG.debug("FTETaxShield[T](s * FK[T]): " + FTETaxShield[T]);
 			LOG.debug("FTE[T]: (fcf[T] + FTETaxShield[T] - FTEInterest[T]): "
					+ FTE[T]);
 		}
 		for (int t = T - 1; t >= 1; t--) {
 			FTEDebtAmort[t] = fk[t].sub(fk[t - 1]);
 			FTEInterest[t] = fkr.mul(fk[t - 1]);
 			FTETaxShield[t] = s.mul(fkr).mul(fk[t - 1]);
 			FTE[t] = fcf[t].add(FTETaxShield[t]).sub(FTEInterest[t]).add(
 					FTEDebtAmort[t]);
 			if (verboseLogging && LOG.isDebugEnabled()) {
 				LOG.debug("FTEDebtAmort[" + t + "] (FK[t] - FK[t-1]): "
 						+ FTEDebtAmort[t]);
 				LOG.debug("FTEInterest[" + t + "] (FKR * FK[t-1]): "
 						+ FTEInterest[t]);
 				LOG.debug("FTETaxShield[" + t + "] ( s * FKR * FK[t-1]): "
 						+ FTETaxShield[t]);
 				LOG
 						.debug("FTE["
 								+ t
 								+ "] (FCF[t] + FTETaxShield[t] - FTEInterest[t] + FTEDebtAmort[t]): "
 								+ FTE[t]);
 			}
 		}
 
 		// ### Step 2
 		// Calculation of the present value tax shield
 		PresentValueTaxShield = calcPresentValueTaxShield(s, fkr, fk,
 				PresentValueTaxShield, verboseLogging);
 
 		// ### Step 3
 		// Calculation of enterprise values
 
 		// Load dynamic equity rate of return with initial equity rate of
 		// return.
 		// One value is needed to start
 		for (int j = 0; j < EKrFTE.length; j++) {
 			EKrFTE[j] = ekr;
 			if (verboseLogging)
 				LOG.debug("EKrFTE[j]: " + EKrFTE[j]);
 		}
 		// Calculation of UW and rEKv with FTE
 		if (verboseLogging)
 			LOG.debug("Calculation of UW and rEKv with FTE");
 		boolean isIdentical = false;
 		int iterations = 0;
 		do {
 			for (int k = 0; k < uw.length; k++) {
 				uwLastCalc[k] = uw[k];
 			}
 			// Calculation of the enterprise value
 			// UW[T] = FTE[T] / EKrFTE[T]
			
 			uw[T] = FTE[T].div(EKrFTE[T]);
			if (verboseLogging && LOG.isDebugEnabled()) {
				LOG.debug("uw[T](FTE[T] / EKrFTE[T]): " + FTE[T] + "/" + EKrFTE[T] + " = " + uw[T]  );
			}
			
 			for (int t = T - 1; t >= 0; t--) {
 				// UW[t] = (UW[t+1] + FTE[t+1]) / (EKrFTE[t+1] + 1)
 				uw[t] = (uw[t + 1].add(FTE[t + 1])).div(EKrFTE[t + 1]
 						.add(new DoubleValue(1)));
 				if (verboseLogging && LOG.isDebugEnabled()) {
 					LOG.debug("UW[" + t
 							+ "] (UW[t+1] + FTE[t+1]) / (EKrFTE[t+1] + 1): "
 							+ uw[t]);
 				}
 			}
 
 			EKrFTE = calcVariableEquityReturnRate(s, fkr, fk, ekr, uw, EKrFTE,
 					PresentValueTaxShield, verboseLogging);
 
 			// Check if former and current values are the same
 			isIdentical = false;
 			if (uwLastCalc[0] == null || checkAbs(uw[0], uwLastCalc[0])) {
 				isIdentical = false;
 			} else {
 				isIdentical = true;
 			}
 			if (verboseLogging)
 				LOG.debug("No. of iterations: " + iterations);
 			iterations++;
 			if (iterations > 25000) {
 				isIdentical = true;
 			}
 		} while (!isIdentical);
 
 		Map<String, Calculable[]> result = new HashMap<String, Calculable[]>();
 		result.put(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE
 				.toString(), uw);
 		result.put(IShareholderValueCalculator.Result.DEBT.toString(), fk);
 		result.put(
 				IShareholderValueCalculator.Result.FREE_CASH_FLOW.toString(),
 				fcf);
 
 		result.put(Result.PRESENT_VALUE_TAX_SHIELD.toString(),
 				PresentValueTaxShield);
 		result.put(Result.FLOW_TO_EQUITY_INTEREST.toString(), FTEInterest);
 		result.put(Result.FLOW_TO_EQUITY_TAX_SHIELD.toString(), FTETaxShield);
 		result.put(Result.FLOW_TO_EQUITY.toString(), FTE);
 		result.put(Result.DEBT_AMORTISATION.toString(), FTEDebtAmort);
 		result.put(Result.EQUITY_RETURN_RATE_FTE.toString(), EKrFTE);
 
 		if (verboseLogging)
 			LOG.info("----- FTE procedure finished -----");
 
 		return result;
 	}
 
 	@Override
 	public String getUniqueId() {
 		return UNIQUE_ID;
 	}
 
 	@Override
 	public String getGuiKey() {
 		return GUI_KEY;
 	}
 
 	// Calculate PresentValueTaxShield for endless period
 	// PresentValueTaxShield[T] = (s * FKr * FK[T]) / FKr
 	private Calculable calcPresentValueTaxShieldEndless(Calculable s,
 			Calculable FKr, Calculable FK, boolean verboseLogging) {
 		if (verboseLogging && LOG.isDebugEnabled()) {
 			LOG.debug("PVTSEndless (s * FKr * FK[T]) / FKr): "
 					+ s.mul(FKr).mul(FK).div(FKr));
 		}
 		return s.mul(FKr).mul(FK).div(FKr);
 	}
 
 	// Calculate PresentValueTaxShield for finite period
 	// PresentValueTaxShield[t] = (PresentValueTaxShield[t + 1] + (s * FKr *
 	// FK[t])) / (FKr + 1)
 	private Calculable[] calcPresentValueTaxShieldFinite(Calculable s,
 			Calculable FKr, Calculable[] FK, Calculable[] PVTS, boolean verboseLogging) {
 		for (int i = PVTS.length - 2; i >= 0; i--) {
 			PVTS[i] = (PVTS[i + 1].add(s.mul(FKr).mul(FK[i + 1 - 1]))).div(FKr
 					.add(new DoubleValue(1)));
 			if (verboseLogging && LOG.isDebugEnabled()) {
 				LOG
 						.debug("PVTS["
 								+ i
 								+ "] (PresentValueTaxShield[t + 1] + (s * FKr * FK[t])) / (FKr + 1): "
 								+ PVTS[i]);
 			}
 		}
 		return PVTS;
 	}
 
 	/**
 	 * Calculates the value of the indebted enterprise. Used in FCF and FTE
 	 * method.
 	 * 
 	 * @param s
 	 *            Taxes
 	 * @param FKr
 	 *            Debt rate of return
 	 * @param FK
 	 *            Debt
 	 * @param PVTS
 	 *            self reference
 	 * @return The value of the indebted enterprise for each year
 	 */
 	private Calculable[] calcPresentValueTaxShield(Calculable s,
 			Calculable FKr, Calculable[] FK, Calculable[] PVTS, boolean verboseLogging) {
 		PVTS[PVTS.length - 1] = calcPresentValueTaxShieldEndless(s, FKr,
 				FK[FK.length - 1], verboseLogging);
 		return calcPresentValueTaxShieldFinite(s, FKr, FK, PVTS, verboseLogging);
 	}
 
 	/**
 	 * Calculates the variable equity return rate for the endless period. Used
 	 * in FCF and FTE method.</br> EKrFCF[T] = EKr + ((EKr - FKr) * (1-s) *
 	 * (FK[T] / UW[T]))
 	 * 
 	 * @param s
 	 *            Taxes
 	 * @param FKr
 	 *            Debt rate of return
 	 * @param FK
 	 *            Debt
 	 * @param EKr
 	 *            Equity rate of return
 	 * @param UW
 	 *            Enterprise value
 	 * @return Variable equity return rate for the endless period
 	 */
 	private Calculable calcVariableEquityReturnRateEndless(Calculable s,
 			Calculable FKr, Calculable FK, Calculable EKr, Calculable UW, boolean verboseLogging) {
 		Calculable result = EKr.add(((EKr.sub(FKr)).mul(s.mul(new DoubleValue(-1)).add(
 				new DoubleValue(1))).mul(FK.div(UW))));
 		if (verboseLogging && LOG.isDebugEnabled())
 			LOG.debug("EKrFCFEndless: " + result);
 		return result;
 	}
 
 	/**
 	 * Calculates the variable equity return rate for the finite periods. Used
 	 * in FCF and FTE method.</br> EKrFCF[t] = EKr + ((EKr - FKr) * ((FK[t -1] -
 	 * PVTS[t-1]) / UW[t - 1]))
 	 * 
 	 * @param s
 	 *            Taxes
 	 * @param FKr
 	 *            Debt rate of return
 	 * @param FK
 	 *            Debt
 	 * @param EKr
 	 *            Equity rate of return
 	 * @param UW
 	 *            Enterprise value
 	 * @param EKrV
 	 *            self reference
 	 * @param PresentValueTaxShield
 	 *            Value of the indebted enterprise
 	 * @return Variable equity return rate for the finite periods
 	 */
 	private Calculable[] calcVariableEquityReturnRateFinite(Calculable s,
 			Calculable FKr, Calculable[] FK, Calculable EKr, Calculable UW[],
 			Calculable[] EKrV, Calculable[] PresentValueTaxShield, boolean verboseLogging) {
 		for (int t = 1; t < UW.length - 1; t++) {
 			EKrV[t] = EKr.add(((EKr.sub(FKr)).mul((FK[t - 1]
 					.sub(PresentValueTaxShield[t - 1])).div(UW[t - 1]))));
 			if (verboseLogging && LOG.isDebugEnabled()) {
 				LOG
 						.debug("EKrV["
 								+ t
 								+ "] (EKr + ((EKr - FKr) * ((FK[t -1] - PVTS[t-1]) / UW[t - 1])) :"
 								+ EKrV[t]);
 			}
 		}
 		return EKrV;
 	}
 
 	/**
 	 * Calculates the variable equity return rate. Used in FCF and FTE method.
 	 * 
 	 * @param s
 	 *            Taxes
 	 * @param FKr
 	 *            Debt rate of return
 	 * @param FK
 	 *            Debt
 	 * @param EKr
 	 *            Equity rate of return
 	 * @param UW
 	 *            Enterprise value
 	 * @param EKrV
 	 *            self reference
 	 * @param PresentValueTaxShield
 	 *            Value of the indebted enterprise
 	 * @return Variable equity return rate
 	 */
 	private Calculable[] calcVariableEquityReturnRate(Calculable s,
 			Calculable FKr, Calculable[] FK, Calculable EKr, Calculable UW[],
 			Calculable[] EKrV, Calculable[] PresentValueTaxShield, boolean verboseLogging) {
 		EKrV[EKrV.length - 1] = calcVariableEquityReturnRateEndless(s, FKr,
 				FK[EKrV.length - 1], EKr, UW[EKrV.length - 1], verboseLogging);
 		return calcVariableEquityReturnRateFinite(s, FKr, FK, EKr, UW, EKrV,
 				PresentValueTaxShield, verboseLogging);
 	}
 
 	/**
 	 * This method checks whether the input parameters differ from each other
 	 * more than 0.0000000001
 	 * 
 	 * @param firstValue
 	 * @param secondValue
 	 * @return <b>true</b> if input parameters differ more<br>
 	 *         <b>false</b> if input parameters differ less
 	 */
 	private boolean checkAbs(Calculable firstValue, Calculable secondValue) {
 		// if(firstValue. sub(secondValue).abs().greaterThan(new
 		// DoubleValue(0.0000000001))){
 		// return true;
 		// }else{
 		// return false;
 		// }
 		return !firstValue.diffToLess(secondValue, 0.0000000001);
 	}
 }
