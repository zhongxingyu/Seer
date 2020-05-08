 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.bh.plugin.resultAnalysis;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.bh.controller.OutputController;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.IntervalValue;
 import org.bh.gui.View;
 import org.bh.gui.chart.IBHAddValue;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 
 /**
  *
  * @author Marco Hammel
  * @author Sebastian Scharfenberger
  */
 public class BHResultController extends OutputController {
 
     protected static Logger log = Logger.getLogger(BHResultController.class);
    private ITranslator translator;
 
     public static enum ChartKeys {
 
         APV_WF_SV,
         APV_BC_CS,
         FCF_WF_SV,
         FCF_BC_CS,
         FCF_BC_FCF,
         FCF_BC_RR,
         FTE_BC_SV,
         FTE_BC_CS,
         FTE_BC_FTE;
 
         @Override
         public String toString() {
             return getClass().getName() + "." + super.toString();
         }
     }
 
     public BHResultController(View view, Map<String, Calculable[]> result, DTOScenario scenario) {
         super(view, result, scenario);
        this.translator = Services.getTranslator();
     }
 
     @Override
     public void setResult(Map<String, Calculable[]> result, DTOScenario scenario) {
 
         super.setResult(result, scenario);
 
         if (scenario.isIntervalArithmetic()) {
             log.debug("generate charts for intervall input");
 
             if (scenario.getDCFMethod().getUniqueId().equals("apv")) {
                 IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.APV_WF_SV.toString());
                 comp.addValue(((IntervalValue) result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[0]).getMin(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF") + " " + translator.translate("min"));
                 comp.addValue(((IntervalValue) result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[0]).getMax(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF") + " " + translator.translate("max"));
                 comp.addValue(((IntervalValue) result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[0]).getMin(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD") + " " + translator.translate("min"));
                 comp.addValue(((IntervalValue) result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[0]).getMax(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD") + " " + translator.translate("max"));
                 comp.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0]).getMin() * -1, translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("min"));
                 comp.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0]).getMax() * -1, translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("max"));
                 comp.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0]).getMin(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE") + " " + translator.translate("min"));
                 comp.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0]).getMax(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE") + " " + translator.translate("max"));
 
                 IBHAddValue comp2 = super.view.getBHchartComponents().get(ChartKeys.APV_BC_CS.toString());
                 int length = result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF").length;
                 for (int i = 0; i < length; i++) {
                     String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
                     comp2.addValue(((IntervalValue) result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[i]).getMin(), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF") + " " + translator.translate("min"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[i]).getMax(), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF") + " " + translator.translate("max"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i]).getMin(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("min"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i]).getMax(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("max"), name);
                 }
 
             } else if (scenario.getDCFMethod().getUniqueId().equals("fcf")) {
 
                 IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FCF_WF_SV.toString());
                 comp.addValue(((IntervalValue) result.get("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")[0]).getMin(), translator.translate(ChartKeys.FCF_WF_SV), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL") + " " + translator.translate("min"));
                 comp.addValue(((IntervalValue) result.get("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")[0]).getMax(), translator.translate(ChartKeys.FCF_WF_SV), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL") + " " + translator.translate("max"));
                 comp.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0]).getMin() * -1, translator.translate(ChartKeys.FCF_WF_SV.toString()), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("min"));
                 comp.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0]).getMax() * -1, translator.translate(ChartKeys.FCF_WF_SV.toString()), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("max"));
                 comp.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0]).getMin(), translator.translate(ChartKeys.FCF_WF_SV.toString()), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE") + " " + translator.translate("min"));
                 comp.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0]).getMax(), translator.translate(ChartKeys.FCF_WF_SV.toString()), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE") + " " + translator.translate("max"));
 
                 IBHAddValue comp2 = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_CS.toString());
                 for (int i = 0; i < result.get("org.bh.plugin.fcf.FCFCalculator$Result.PRESENT_VALUE_TAX_SHIELD").length; i++) {
                     String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
                     comp2.addValue(((IntervalValue) result.get("org.bh.plugin.fcf.FCFCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[i]).getMin(), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.PRESENT_VALUE_TAX_SHIELD") + " " + translator.translate("min"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.plugin.fcf.FCFCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[i]).getMax(), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.PRESENT_VALUE_TAX_SHIELD") + " " + translator.translate("max"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i]).getMin(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("min"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i]).getMin(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("max"), name);
                 }
 
                 IBHAddValue comp3 = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_FCF.toString());
                 if (result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0] != null) {
                     String name = scenario.getChildren().get(0).get(DTOPeriod.Key.NAME).toString();
                     comp3.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0]).getMin(), translator.translate(ChartKeys.FCF_BC_FCF.toString()) + " " + translator.translate("min"), name);
                     comp3.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0]).getMax(), translator.translate(ChartKeys.FCF_BC_FCF.toString()) + " " + translator.translate("max"), name);
                 }
                 for (int i = 1; i < result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW").length; i++) {
                     String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
                     comp3.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[i]).getMin(), translator.translate(ChartKeys.FCF_BC_FCF.toString()) + " " + translator.translate("min"), name);
                     comp3.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[i]).getMax(), translator.translate(ChartKeys.FCF_BC_FCF.toString()) + " " + translator.translate("max"), name);
 
                 }
 
                 IBHAddValue comp4 = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_RR.toString());
                 for (int i = 0; i < result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF").length; i++) {
                     String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
                     comp4.addValue(((IntervalValue) result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF")[i]).getMin(), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF") + " " + translator.translate("min"), name);
                     comp4.addValue(((IntervalValue) result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF")[i]).getMax(), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF") + " " + translator.translate("max"), name);
                     comp4.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE")[0]).getMin(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE") + " " + translator.translate("min"), name);
                     comp4.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE")[0]).getMax(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE") + " " + translator.translate("max"), name);
                 }
 
             } else if (scenario.getDCFMethod().getUniqueId().equals("fte")) {
 
                 IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_SV.toString());
                 //Not necessary ct Pohl
                 //comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].parse(), translator.translate(ChartKeys.FTE_BC_SV.toString()), translator.translate(ChartKeys.FTE_BC_SV.toString()));
 
                 IBHAddValue comp2 = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_CS.toString());
                 for (int i = 0; i < result.get("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD").length; i++) {
                     String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
                     comp2.addValue(((IntervalValue) result.get("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD")[i]).getMin(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD") + " " + translator.translate("min"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD")[i]).getMax(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD") + " " + translator.translate("max"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i]).getMin(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("min"), name);
                     comp2.addValue(((IntervalValue) result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i]).getMax(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("max"), name);
                 }
 
                 IBHAddValue comp3 = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_FTE.toString());
                 for (int i = 1; i < result.get("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY").length; i++) {
                     String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
                     comp3.addValue(((IntervalValue) result.get("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY")[i]).getMin(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY") + " " + translator.translate("min"), name);
                     comp3.addValue(((IntervalValue) result.get("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY")[i]).getMax(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY") + " " + translator.translate("max"), name);
                 }
 
             } else if (scenario.getDCFMethod().getUniqueId().equals("allDCF")) {
                 //TODO UI controll for all supported dcf UIs in plugin
             } else {
             }
         }
         log.debug("generate charts for non intervall deterministic input");
         if (scenario.getDCFMethod().getUniqueId().equals("apv")) {
             this.fillAPVcharts(result, scenario);
         } else if (scenario.getDCFMethod().getUniqueId().equals("fcf")) {
             this.fillFCFcharts(result, scenario);
         } else if (scenario.getDCFMethod().getUniqueId().equals("fte")) {
             this.fillFTEcharts(result, scenario);
         } else if (scenario.getDCFMethod().getUniqueId().equals("allDCF")) {
             this.fillAPVcharts(result, scenario);
             this.fillFCFcharts(result, scenario);
             this.fillFTEcharts(result, scenario);
         } else {
         }
     }
 
     private void fillAPVcharts(Map<String, Calculable[]> result, DTOScenario scenario) {
         IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.APV_WF_SV.toString());
         comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[0].parse(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF"));
         comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[0].parse(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD"));
         comp.addValue((result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0].parse().doubleValue() * -1), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"));
         comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].parse(), translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE"));
 
         IBHAddValue comp2 = super.view.getBHchartComponents().get(ChartKeys.APV_BC_CS.toString());
         int length = result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF").length;
         for (int i = 0; i < length; i++) {
             String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
             comp2.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[i].parse(), translator.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF"), name);
             comp2.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i].parse(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"), name);
         }
     }
 
     private void fillFCFcharts(Map<String, Calculable[]> result, DTOScenario scenario) {
         IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FCF_WF_SV.toString());
         comp.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")[0].parse(), translator.translate(ChartKeys.FCF_WF_SV), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL"));
         comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0].parse().doubleValue() * -1, translator.translate(ChartKeys.FCF_WF_SV.toString()), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"));
         comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].parse(), translator.translate(ChartKeys.FCF_WF_SV.toString()), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE"));
 
         IBHAddValue comp2 = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_CS.toString());
         for (int i = 0; i < result.get("org.bh.plugin.fcf.FCFCalculator$Result.PRESENT_VALUE_TAX_SHIELD").length; i++) {
             String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
             comp2.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[i].parse(), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.PRESENT_VALUE_TAX_SHIELD"), name);
             comp2.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i].parse(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"), name);
         }
 
         IBHAddValue comp3 = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_FCF.toString());
         if (result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0] != null) {
             String name = scenario.getChildren().get(0).get(DTOPeriod.Key.NAME).toString();
             comp3.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0].parse(), translator.translate(ChartKeys.FCF_BC_FCF.toString()), name);
         }
         for (int i = 1; i < result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW").length; i++) {
             String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
             comp3.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[i].parse(), translator.translate(ChartKeys.FCF_BC_FCF.toString()), name);
         }
 
         IBHAddValue comp4 = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_RR.toString());
         for (int i = 0; i < result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF").length; i++) {
             String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
             comp4.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF")[i].parse(), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF"), name);
             comp4.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE")[0].parse(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE"), name);
         }
     }
 
     private void fillFTEcharts(Map<String, Calculable[]> result, DTOScenario scenario) {
         //IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_SV.toString());
         //Not necessary ct Pohl
         //comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].parse(), translator.translate(ChartKeys.FTE_BC_SV.toString()), translator.translate(ChartKeys.FTE_BC_SV.toString()));
 
         IBHAddValue comp2 = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_CS.toString());
         for (int i = 0; i < result.get("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD").length; i++) {
             String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
             comp2.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD")[i].parse(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD"), name);
             comp2.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i].parse(), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"), name);
         }
 
         IBHAddValue comp3 = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_FTE.toString());
         for (int i = 1; i < result.get("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY").length; i++) {
             String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
             comp3.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY")[i].parse(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY"), name);
         }
     }
 
     public static Map<String, Calculable> getFormulaMap(DTOScenario scenario, Map<String, Calculable[]> result) {
         log.debug("generate map for formular parser");
         HashMap<String, Calculable> formulaMap = new HashMap<String, Calculable>();
         	int t = 0;
         	int T = scenario.getChildrenSize() - 1;
         	
         	//General
         	putFormulaValue(formulaMap, "FCFT", result, "org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW", T);
         	putFormulaValue(formulaMap, "FCFt+1", result, "org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW", t+1);
             putFormulaValue(formulaMap, "s", result, "org.bh.calculation.IShareholderValueCalculator$Result.TAXES", 0);
             putFormulaValue(formulaMap, "rFK", result, "org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE", 0);
             putFormulaValue(formulaMap, "ruEK", result, "org.bh.calculation.IShareholderValueCalculator$Result.EQUITY_RETURN_RATE", 0);
             putFormulaValue(formulaMap, "FKT", result, "org.bh.calculation.IShareholderValueCalculator$Result.DEBT", T);
             putFormulaValue(formulaMap, "FKt", result, "org.bh.calculation.IShareholderValueCalculator$Result.DEBT", t);
         
             //APV
             putFormulaValue(formulaMap, "UWAPV,T", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", T);
             putFormulaValue(formulaMap, "UWAPV,t", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);
             putFormulaValue(formulaMap, "Vu,t+1", result, "org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF", t+1);
             putFormulaValue(formulaMap, "Vs,t+1", result, "org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD", t+1);
 
             //FCF
             putFormulaValue(formulaMap, "UWFCF,T", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", T);        
             putFormulaValue(formulaMap, "UWFCF,t", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);        
             putFormulaValue(formulaMap, "GKt+1", result, "org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL", t+1);
             putFormulaValue(formulaMap, "rvTEK", result, "org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF", T);
             putFormulaValue(formulaMap, "rvt+1EK", result, "org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF", t+1);
             
             //FTE
             putFormulaValue(formulaMap, "UWFTE,T", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", T);
             putFormulaValue(formulaMap, "UWFTE,t", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);
             putFormulaValue(formulaMap, "UWFTE,t+1", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t+1);
             putFormulaValue(formulaMap, "FTET", result, "org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY", T);
             putFormulaValue(formulaMap, "FTEt+1", result, "org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY", t+1);
             putFormulaValue(formulaMap, "rvTEK", result, "org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE", T);
             putFormulaValue(formulaMap, "rvt+1EK", result, "org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE", t+1);
 
         return formulaMap;
     }
     
     protected static void putFormulaValue(Map<String, Calculable> formulaMap,
     		String formulaKey, Map<String, Calculable[]> resultMap, String resultKey, int resultIndex) {
     	Calculable[] result = resultMap.get(resultKey);
     	if (result == null || resultIndex >= result.length)
     		return;
     	
     	formulaMap.put(formulaKey, result[resultIndex]);
     }
 		//TODO Marcos changes , sry forgot to commit first
 //    public Map<String, Calculable> getFormulaMap(DTOScenario scenario) {
 //        HashMap<String, Calculable> formulaMap = new HashMap<String, Calculable>();
 //
 //        if (scenario.getDCFMethod().getUniqueId().equals("apv")) {
 //            formulaMap.put("FCFT", result.get("org.bh.calculation.IShareholderValueCalculator$ Result.FREE_CASH_FLOW")[1]);
 //            formulaMap.put("ruEK", result.get("org.bh.plugin.apv.APVCalculator$Result. PRESENT_VALUE_FCF")[1]);
 //            formulaMap.put("srFK", result.get("org.bh.plugin.apv.APVCalculator$Result. PRESENT_VALUE_TAX_SHIELD")[1]);
 //            formulaMap.put("FKT", result.get("org.bh.calculation.IShareholderValueCalculator$ Result.EQUITY_RETURN_RATE")[0]);
 //            formulaMap.put("rFK", result.get("org.bh.calculation.IShareholderValueCalculator$ Result.TAXES[0]")[1]);
 //            formulaMap.put("FKT2", result.get("org.bh.calculation.IShareholderValueCalculator$ Result.DEBT [t]")[1]);
 //            return formulaMap;
 //        } else if (scenario.getDCFMethod().getUniqueId().equals("fcf")) {
 //            formulaMap.put("GKt", result.get("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")[1]);
 //            formulaMap.put("FCFT", result.get("org.bh.calculation.IShareholderValueCalculator$ Result.FREE_CASH_FLOW[t+1]")[1]);
 //            formulaMap.put("FKT", result.get("org.bh.calculation.IShareholderValueCalculator$ Result.DEBT [t]")[1]);
 //            formulaMap.put("s", result.get("org.bh.calculation.IShareholderValueCalculator$ Result.TAXES[0]")[1]);
 //            formulaMap.put("rFK", result.get("org.bh.calculation.IShareholderValueCalculator$ Result.DEBT_RETURN_RATE [0]")[1]);
 //            formulaMap.put("rvEK", result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF [0]")[1]);
 //            return formulaMap;
 //        } else if (scenario.getDCFMethod().getUniqueId().equals("fte")) {
 //            formulaMap.put("FTEt", result.get("org.bh.plugin.fte.FTECalculator$Result. FLOW_TO_EQUITY [t+1]")[1]);
 //            formulaMap.put("UWt", result.get("org.bh.calculation.IShareholderValueCalculator$ Result. SHAREHOLDER_VALUE")[0]);
 //            formulaMap.put("rvEK", result.get("org.bh.plugin.fte.FTECalculator$Result. EQUITY_RETURN_RATE_FTE")[0]);//[0]
 //            return formulaMap;
 //        } else {
 //            return null;
 //        }
 //    }
 }
