 package org.bh.platform;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.controller.Controller;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DistributionMap;
 import org.bh.data.types.IntervalValue;
 import org.bh.gui.View;
 import org.bh.gui.chart.IBHAddValue;
 import org.bh.platform.i18n.ITranslator;
 
 /**
  * In this class a stackbarchart is created to compare the results of the
  * scenarios. There should be a differentiation between deterministic and
  * stochastic determined results.
  * 
  * @author Norman Weisenburger, Patrick Heinz
  * @version 1.0, 22.01.2010
  * 
  */
 public class DashBoardController extends Controller{
 	
 	
     public static enum ChartKeys{
     	/**
     	 * DashBoard_StackedBarChart_ShareholderValue
     	 */
     	DB_SBC_SV;
     
     	@Override
     	public String toString() {
     		return getClass().getName() + "." + super.toString();
     	}
     }
     
     public static enum Keys{
     	/**
     	 * Number of scenarios
     	 */
     	NO_OF_SCENARIOS,
     	/**
     	 * Number of scenario descriptions
     	 */
     	NO_OF_SCENARIOS_DESCR,  	
     	/**
     	 * Shareholder value range description
     	 */
     	SV_RANGE_DESCR,
     	/**
     	 * Shareholder value range
     	 */
     	SV_RANGE;
     
     	@Override
     	public String toString() {
     		return getClass().getName() + "." + super.toString();
     	}
     }
 
 	Map<DTOScenario, Map<?,?>> result;
     
 	private int valueAtRisk = 95;
 	
 	/**
 	 * The constructor for the dashboard controller.
 	 * 
 	 * @param view
 	 */
     public DashBoardController(View view){
     	super(view);
     }
     
 	/**
 	 * Creates a stackedbarchart to compare shareholder values.
 	 * 
 	 * @param result Map of the calculation.
 	 */
     @SuppressWarnings("unchecked")
 	public void setResult(Map<DTOScenario, Map<?,?>> result) {
 		ITranslator translator = Services.getTranslator();
 		
 		DTOScenario s;
 		DistributionMap d;
 		Map<String, Calculable[]> r;
 		Calculable sv;
 		IntervalValue i;
 		
 		IBHAddValue stackedBarChart = view.getBHchartComponents().get(ChartKeys.DB_SBC_SV.toString());
 	
 		for(Entry<DTOScenario, Map<?, ?>> e : result.entrySet()){
 			e.getKey().isValid(true);
 			s = e.getKey();
 			if(s.isDeterministic()) {
 				r = (Map<String, Calculable[]>) e.getValue();
 				sv = r.get(IShareholderValueCalculator.Result.SHAREHOLDER_VALUE.toString())[0];
 				if(sv instanceof IntervalValue) {
 					i = (IntervalValue) sv;
					stackedBarChart.addValue(i.getMin(), translator.translate(ChartKeys.DB_SBC_SV) + " " + translator.translate("min"),
                                                s.get(DTOScenario.Key.NAME).toString() + " (" + translator.translate("interval") + ")");
					stackedBarChart.addValue(i.getMax() - i.getMin(), translator.translate(ChartKeys.DB_SBC_SV) + " " + translator.translate("max"),
                                                s.get(DTOScenario.Key.NAME).toString() + " (" + translator.translate("interval") + ")");
 				}else { // instance of DoubleValue || IntegerValue
 					stackedBarChart.addValue(sv.parse(),translator.translate(ChartKeys.DB_SBC_SV), s.get(DTOScenario.Key.NAME).toString() + " (" + translator.translate("deterministic") + ")");
 				}
 			}else { //stochastic scenario
 				d = (DistributionMap) e.getValue();
 				sv = d.valueAtRisk(valueAtRisk);
 				i = (IntervalValue) sv;
 				stackedBarChart.addValue(i.getMin(),translator.translate(ChartKeys.DB_SBC_SV), s.get(DTOScenario.Key.NAME).toString() + " (" + translator.translate("stochastic") + ")");
 				stackedBarChart.addValue(i.getMax() - i.getMin(),translator.translate(ChartKeys.DB_SBC_SV), s.get(DTOScenario.Key.NAME).toString() + " (" + translator.translate("stochastic") + ")");
 			}
 		}
     }
 }
 
