 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber * : Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
  *
  * Copyright 2009: Manuel Bross, Simon Drees, Marco Hammel, Patrick Heinz, Marcel Hockenberger, Marcus Katzor, Edgar Kauz, Anton Kharitonov, Sarah Kuhn, Michael Löckelt, Heiko Metzger, Jacqueline Missikewitz, Marcel Mrose, Steffen Nees, Alexander Roth, Sebastian Scharfenberger, Carsten Scheunemann, Dave Schikora, Alexander Schmalzhaf, Florian Schultze, Klaus Thiele, Patrick Tietze, Robert Vollmer, Norman Weisenburger, Lars Zuckschwerdt
  *
  * Copyright 2008: Camil Bartetzko, Tobias Bierer, Lukas Bretschneider, Johannes Gilbert, Daniel Huser, Christopher Kurschat, Dominik Pfauntsch, Sandra Rath, Daniel Weber
  *
  * This program is free software: you can redistribute it and/or modify it un-der the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT-NESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package org.bh.plugin.wienerProcess;
 
 import java.awt.Component;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 
 import org.apache.log4j.Logger;
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.calculation.IStochasticProcess;
 import org.bh.data.DTO;
 import org.bh.data.DTOKeyPair;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.IPeriodicalValuesDTO;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DistributionMap;
 import org.bh.data.types.DoubleValue;
 import org.bh.data.types.IntegerValue;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHOptionPane;
 import org.bh.gui.swing.comp.BHDescriptionLabel;
 import org.bh.gui.swing.comp.BHTextField;
 import org.bh.platform.Services;
 import org.bh.validation.VRIsDouble;
 import org.bh.validation.VRIsGreaterThan;
 import org.bh.validation.VRIsInteger;
 import org.bh.validation.VRMandatory;
 import org.bh.validation.ValidateStochastic;
 import org.bh.validation.ValidationRule;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 //import org.bh.data.types.StringValue;
 //import org.bh.plugin.directinput.DTODirectInput;
 //import java.util.Iterator;
 /**
  * This class provides the functionality to process the Wiener Process on every
  * value which should be determined stochastically.
  * 
  * @author Sebastian
  * @version 0.1, 04.01.2010
  * 
  */
 public class WienerProcess implements IStochasticProcess {
 	private static final String SLOPE = "slope";
 	private static final String STANDARD_DEVIATION = "standardDeviation";
 	private static final String REPETITIONS = "repetitions";
 	private static final String STEPS_PER_PERIOD = "stepsPerPeriod";
 	private static final String AMOUNT_OF_PERIODS = "amountOfPeriods";
 
 	private static final Logger log = Logger.getLogger(WienerProcess.class);
 
 	private static final String UNIQUE_ID = "wienerProcess";
 	private static final String GUI_KEY = "wienerProcess";
 	private static final int NUM_THREADS = Runtime.getRuntime()
 			.availableProcessors();
 
 	private DTOScenario scenario;
 	private JPanel panel;
 	private HashMap<String, Double> internalMap;
 	private HashMap<String, Integer> map;
 
 	private BHMainFrame bhmf;
 
 	@Override
 	public DistributionMap calculate() {
 		log.info("Wiener Process started");
 
 		DistributionMap result = new DistributionMap(1);
 		DTOPeriod last = scenario.getLastChild();
 		List<DTOKeyPair> stochasticKeys = scenario.getPeriodStochasticKeys();
 		IShareholderValueCalculator dcfMethod = scenario.getDCFMethod();
 
 		boolean isValid = ValidateStochastic.validateWienerProcess(internalMap);
 		int choice = 0;
 
 		if (isValid == false) {
 			choice = BHOptionPane.showConfirmDialog(bhmf, Services
 					.getTranslator().translate("WCalcWPMessage"), Services
 					.getTranslator().translate("WCalcWP"),
 					JOptionPane.YES_NO_OPTION);
 		}
 
 		if (isValid == true || choice == JOptionPane.YES_OPTION) {
 
 			Countdown countdown = new Countdown(map.get(REPETITIONS));
 			int stepsPerPeriod = map.get(STEPS_PER_PERIOD);
 			int amountOfPeriods = map.get(AMOUNT_OF_PERIODS);
 
 			DTO.setThrowEvents(false);
 			DTOScenario[] tempScenarios = new DTOScenario[NUM_THREADS];
 
 			CalculationThread[] threads = new CalculationThread[NUM_THREADS];
 			for (int i = 0; i < NUM_THREADS; i++) {
 				DTOScenario tempScenario = tempScenarios[i] = new DTOScenario(
 						true);
 				tempScenario.put(DTOScenario.Key.REK, scenario
 						.get(DTOScenario.Key.REK));
 				tempScenario.put(DTOScenario.Key.RFK, scenario
 						.get(DTOScenario.Key.RFK));
 				tempScenario.put(DTOScenario.Key.BTAX, scenario
 						.get(DTOScenario.Key.BTAX));
 				tempScenario.put(DTOScenario.Key.CTAX, scenario
 						.get(DTOScenario.Key.CTAX));
 
 				for (int j = 0; j < amountOfPeriods; j++) {
 					tempScenario.addChild((DTOPeriod) last.clone());
 				}
 
 				threads[i] = new CalculationThread(countdown, stepsPerPeriod,
 						stochasticKeys, tempScenario, result, dcfMethod, last);
 				threads[i].setName("Wiener Process " + (i + 1));
 				threads[i].start();
 			}
 
 			for (int i = 0; i < NUM_THREADS; i++) {
 				try {
 					threads[i].join();
 				} catch (InterruptedException e) {
 					log.error("", e);
 				}
 			}
 		}
 
 		DTO.setThrowEvents(true);
 		log.info("Wiener Process finished");
 
 		return result;
 	}
 
 	protected class CalculationThread extends Thread {
 		private Countdown countdown;
 		private int stepsPerPeriod;
 		private List<DTOKeyPair> stochasticKeys;
 		private DTOScenario tempScenario;
 		private DistributionMap result;
 		private IShareholderValueCalculator dcfMethod;
 		private DTOPeriod last;
 		private Random random = new Random();
 
 		public CalculationThread(Countdown countdown, int stepsPerPeriod,
 				List<DTOKeyPair> stochasticKeys, DTOScenario tempScenario,
 				DistributionMap result, IShareholderValueCalculator dcfMethod,
 				DTOPeriod last) {
 			super();
 			this.countdown = countdown;
 			this.stepsPerPeriod = stepsPerPeriod;
 			this.stochasticKeys = stochasticKeys;
 			this.tempScenario = tempScenario;
 			this.result = result;
 			this.dcfMethod = dcfMethod;
 			this.last = last;
 		}
 
 		@Override
 		public void run() {
 			while (countdown.hasRunsLeft()) {
 				DTOPeriod previous = last.getPrevious();
 				for (DTOPeriod period : tempScenario.getChildren()) {
 					for (DTOKeyPair key : stochasticKeys) {
 						IPeriodicalValuesDTO pvdto = period
 								.getPeriodicalValuesDTO(key.getDtoId());
 						IPeriodicalValuesDTO previousDto = previous
 								.getPeriodicalValuesDTO(key.getDtoId());
 						Calculable previousValue = previousDto
 								.getCalculable(key.getKey());
 						pvdto.put(key.getKey(), doOneWienerProcess(
 								previousValue, stepsPerPeriod,
 								internalMap.get(key.getKey()
 										+ STANDARD_DEVIATION), internalMap
 										.get(key.getKey() + SLOPE), random));
 					}
 				}
 
 				Map<String, Calculable[]> dcfResult = dcfMethod.calculate(
 						tempScenario, false);
 
 				result
 						.put(((DoubleValue) dcfResult
 								.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE
 										.toString())[0]).getValue());
 
 			}
 		}
 	}
 
 	protected class Countdown {
 		private int runsLeft;
 
 		public Countdown(int runs) {
 			runsLeft = runs;
 		}
 
 		public synchronized boolean hasRunsLeft() {
 			return (--runsLeft >= 0);
 		}
 	}
 
 	@Override
 	public JPanel calculateParameters() {
 		internalMap = new HashMap<String, Double>();
 		map = new HashMap<String, Integer>();
 		TreeMap<DTOKeyPair, List<Calculable>> toBeDetermined = scenario
 				.getPeriodStochasticKeysAndValues();
 
 		JPanel result = new JPanel();
 
 		String rowDef = "4px,p,4px,p,4px,p,4px,p,4px";
 		String colDef = "4px,right:pref,4px,60px:grow,8px:grow,right:pref,4px,max(35px;pref):grow,4px:grow";
 		FormLayout layout = new FormLayout(colDef, rowDef);
 		result.setLayout(layout);
 		layout.setColumnGroups(new int[][] { { 4, 8 } });
 		CellConstraints cons = new CellConstraints();
 
 		result.add(new BHDescriptionLabel(AMOUNT_OF_PERIODS), cons.xywh(2, 2,
 				1, 1));
 		BHTextField tf = new BHTextField(AMOUNT_OF_PERIODS);
 		ValidationRule[] rules = { VRMandatory.INSTANCE, VRIsInteger.INSTANCE,
 				new VRIsGreaterThan(2, true) };
 		tf.setValidationRules(rules);
 		result.add(tf, cons.xywh(4, 2, 1, 1));
 		map.put(AMOUNT_OF_PERIODS, new Integer(5));
 
 		result.add(new BHDescriptionLabel(STEPS_PER_PERIOD), cons.xywh(2, 4, 1,
 				1));
 		BHTextField tf1 = new BHTextField(STEPS_PER_PERIOD);
 		ValidationRule[] rules1 = { VRMandatory.INSTANCE, VRIsInteger.INSTANCE,
 				VRIsGreaterThan.GTZERO };
 		tf1.setValidationRules(rules1);
 		result.add(tf1, cons.xywh(4, 4, 1, 1));
 		map.put(STEPS_PER_PERIOD, new Integer(1));
 
 		result.add(new BHDescriptionLabel(REPETITIONS), cons.xywh(6, 2, 1, 1));
 		BHTextField tf2 = new BHTextField(REPETITIONS);
 		ValidationRule[] rules2 = { VRMandatory.INSTANCE, VRIsInteger.INSTANCE,
 				VRIsGreaterThan.GTZERO };
 		tf2.setValidationRules(rules2);
 		result.add(tf2, cons.xywh(8, 2, 1, 1));
 		map.put(REPETITIONS, new Integer(1));
 
 		result.add(new JSeparator(), cons.xywh(2, 8, 7, 1));
 
 		for (Entry<DTOKeyPair, List<Calculable>> e : toBeDetermined.entrySet()) {
 			String key = e.getKey().getKey();
 			double standardDeviation = calcStandardDeviation(e.getValue())
 					.toNumber().doubleValue();
 			double slope = calcSlope(e.getValue()).toNumber().doubleValue();
 			internalMap.put(e.getKey().getKey() + SLOPE, slope);
 			internalMap.put(e.getKey().getKey() + STANDARD_DEVIATION,
 					standardDeviation);
 
 			layout.appendRow(RowSpec.decode("p"));
 			layout.appendRow(RowSpec.decode("4px"));
 
 			result.add(new BHDescriptionLabel(key), cons.xywh(2, layout
					.getRowCount() - 1, 8, 1));
 
 			layout.appendRow(RowSpec.decode("p"));
 			layout.appendRow(RowSpec.decode("14px"));
 
 			BHTextField tfslope = new BHTextField(key + SLOPE, Services
 					.numberToString(slope));
 			ValidationRule[] rules_slope = { VRMandatory.INSTANCE,
 					VRIsDouble.INSTANCE };
 			tfslope.setValidationRules(rules_slope);
 
 			BHTextField tfstandardDeviation = new BHTextField(key
 					+ STANDARD_DEVIATION, Services
 					.numberToString(standardDeviation));
 			ValidationRule[] rules_standardDeviation = { VRMandatory.INSTANCE,
 					VRIsDouble.INSTANCE };
 			tfstandardDeviation.setValidationRules(rules_standardDeviation);
 
 			result.add(new BHDescriptionLabel(SLOPE), cons.xywh(2, layout
 					.getRowCount() - 1, 1, 1));
 			result.add(tfslope, cons.xywh(4, layout.getRowCount() - 1, 1, 1));
 			result.add(new BHDescriptionLabel(STANDARD_DEVIATION), cons.xywh(6,
 					layout.getRowCount() - 1, 1, 1));
 			result.add(tfstandardDeviation, cons.xywh(8,
 					layout.getRowCount() - 1, 1, 1));
 
 		}
 		this.panel = result;
 		return result;
 	}
 
 	@Override
 	public void updateParameters() {
 		Component[] components = panel.getComponents();
 		for (int i = 0; i < components.length; i++) {
 			if (components[i] instanceof BHTextField) {
 				BHTextField c = (BHTextField) components[i];
 				String key = c.getKey();
 				String text = c.getText();
 				if (internalMap.containsKey(key))
 					internalMap.put(key, Services.stringToDouble(text));
 				else
 					map.put(key, Services.stringToInt(text));
 			}
 		}
 	}
 
 	private Calculable calcSlope(List<Calculable> inputValues) {
 		// d = 1/n(X0 - X-n)
 		Calculable result = (inputValues.get(inputValues.size() - 1)
 				.sub(inputValues.get(0))).div(new IntegerValue(inputValues
 				.size()));
 		return result;
 	}
 
 	private Calculable calcStandardDeviation(List<Calculable> inputValues) {
 		Calculable d = calcSlope(inputValues);
 		Calculable sum = new DoubleValue(0);
 		for (int i = 0; i < inputValues.size() - 1; i++) {
 			Calculable x = inputValues.get(i + 1).sub(inputValues.get(i))
 					.sub(d).pow(new IntegerValue(2));
 			sum = sum.add(x);
 			// sum = sum.add(inputValues.get(i +
 			// 1).sub(inputValues.get(i)).sub(d));
 		}
 		Calculable result = (sum.div(new IntegerValue(inputValues.size())))
 				.sqrt();
 		return result;
 	}
 
 	private Calculable doOneWienerProcess(Calculable lastValue,
 			int amountOfSteps, double standardDeviation, double slope,
 			Random random) {
 		double deltaT = 1.0 / amountOfSteps;
 		double deltaTsqrt = Math.sqrt(deltaT);
 		Calculable value = lastValue;
 		for (int i = 0; i < amountOfSteps; i++) {
 			// Xt+dT = Xt + d * dT + (standardA * dTsqrt * eps)
 			value = value.add(new DoubleValue(slope * deltaT
 					+ standardDeviation * deltaTsqrt * random.nextGaussian()));
 		}
 		return value;
 	}
 
 	@Override
 	public IStochasticProcess createNewInstance(DTOScenario scenario) {
 		WienerProcess instance = new WienerProcess();
 		instance.scenario = scenario;
 		return instance;
 	}
 
 	@Override
 	public String getUniqueId() {
 		return UNIQUE_ID;
 	}
 
 	@Override
 	public String getGuiKey() {
 		return GUI_KEY;
 	}
 }
