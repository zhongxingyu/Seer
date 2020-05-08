 package generator.lang.plsql;
 
 import generator.TemplateLoader;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import logger.GLogger;
 import model.*;
 import controls.Register;
 
 public class OracleGenerator implements generator.Generator {	
 	private static String name = "ORACLE (PLSQL)";
 	
 	private Register mc = Register.getInstance();
 	
 	public String generateBR(BusinessRule br) {		
 		BusinessRuleType brt = mc.findBusinessRuleType(br.getBrt_fk());
 		
 		String s = null;
 		switch(brt.getCode()) {
 			case "ACMP":
 				s = generateACMP(br, brt);
 				break;
 			case "ARNG":
 				s = generateARNG(br, brt);
 				break;
 			case "TCMP":
 				s = generateTCMP(br, brt);
 				break;
 			case "ICMP":
 				s = generateICMP(br, brt);
 				break;
 			case "ALIS":
 				s = generateALIS(br, brt);
 				break;
 			default :
 			case "TOTH":
 			case "EOTH":
 			case "AOTH":
 				s = generateOther(br, brt);
 				break;
 		}
 		return s;
 	}
 	
 	private String generateACMP(BusinessRule br, BusinessRuleType brt) {
 		TemplateLoader tl = new TemplateLoader(new File("templates/plsql/acmp.tpl"));
 		
 		Operator o = mc.findOperator(br.getBr_id());
 		tl.bindParam("operator", o.getOperator());
 		
 		TableColumn tbc = mc.findTableColumn(o.getTc1_fk());
 		tl.bindParam("first_value", tbc.getName());
 		
 		Value v = mc.findValues(o.getCv_fk()).get(0);
 		tl.bindParam("second_value", v.getValue());
 		
 		Trigger tg = mc.findTrigger(br.getTg_fk());
 		switch(tg.getEvent()) {
 			default :
 			case "UI" :
 			case "IU" :
 				tl.bindParam("trigger_event", "UPDATE OR INSERT");
 				break;
 			case "U" :
 				tl.bindParam("trigger_event", "UPDATE");
 				break;
 			case "I" :
 				tl.bindParam("trigger_event", "INSERT");
 				break;
 		}
 		
 		APP_Error err = mc.findAppError(br.getErr_fk());
 		tl.bindParam("error_code", err.getCode());
 		tl.bindParam("error_message", err.getErr_comment());
 		
 		// For future multi-table support.
 		// no time for that now.
 		ArrayList<Table> tb_list = mc.findTriggerTable(tg.getTg_id());
 		Table tb = tb_list.get(0);
 		tl.bindParam("trigger_table", tb.getName());
 		
 
 		tl.bindParam("trigger_name", "APP_" + tb.getName().toUpperCase() + "_" + brt.getCode().toUpperCase() + "_" + br.getBr_id());
 		
 		return tl.getString();
 	}
 	
 	private String generateARNG(BusinessRule br, BusinessRuleType brt) {
 		TemplateLoader tl = new TemplateLoader(new File("templates/plsql/arng.tpl"));
 		
 		Operator o = mc.findOperator(br.getBr_id());
 		tl.bindParam("operator", o.getOperator());
 
 		if(mc.findValues(o.getCv_fk()).size() < 2) {
 			GLogger.log(Level.SEVERE, "Define business rule has failed and did not add all values on the business rule:");
 			GLogger.log(Level.SEVERE, br.getName());
 			return "";
 		}
 		
 		TableColumn tbc = mc.findTableColumn(o.getTc1_fk());
 		tl.bindParam("first_value", tbc.getName());
 		
 		Value v = mc.findValues(o.getCv_fk()).get(0);
 		Value v2 = mc.findValues(o.getCv_fk()).get(1);
 		
 		if(Integer.parseInt(v.getValue()) < Integer.parseInt(v2.getValue())){		
 			tl.bindParam("second_value", v.getValue());
 			tl.bindParam("third_value", v2.getValue());
 		} else {
 			tl.bindParam("second_value", v2.getValue());
 			tl.bindParam("third_value", v.getValue());
 		}
 		
 		Trigger tg = mc.findTrigger(br.getTg_fk());
 		switch(tg.getEvent()) {
 			default :
 			case "UI" :
 			case "IU" :
 				tl.bindParam("trigger_event", "UPDATE OR INSERT");
 				break;
 			case "U" :
 				tl.bindParam("trigger_event", "UPDATE");
 				break;
 			case "I" :
 				tl.bindParam("trigger_event", "INSERT");
 				break;
 		}
 		
 		APP_Error err = mc.findAppError(br.getErr_fk());
 		tl.bindParam("error_code", err.getCode());
 		tl.bindParam("error_message", err.getErr_comment());
 		
 		// For future multi-table support.
 		// no time for that now.
 		ArrayList<Table> tb_list = mc.findTriggerTable(tg.getTg_id());
 		Table tb = tb_list.get(0);
 		tl.bindParam("trigger_table", tb.getName());
 		
 
 		tl.bindParam("trigger_name", "APP_" + tb.getName().toUpperCase() + "_" + brt.getCode().toUpperCase() + "_" + br.getBr_id());
 		
 		return tl.getString();
 	}
 	
 	private String generateALIS(BusinessRule br, BusinessRuleType brt) {
 		TemplateLoader tl = new TemplateLoader(new File("templates/plsql/alis.tpl"));
 		
 		Operator o = mc.findOperator(br.getBr_id());
 		tl.bindParam("operator", o.getOperator());
 		
 		TableColumn tbc = mc.findTableColumn(o.getTc1_fk());
 		tl.bindParam("first_value", tbc.getName());
 		
 		ArrayList<Value> vl = mc.findValues(o.getCv_fk());
 		String s = "";
 		for(int i = 0; i<vl.size(); i++) {
 			if(i!=0) {s+= ", ";}
 			s += "'" + vl.get(i).getValue() + "'";
 		}
 		tl.bindParam("second_value", s);
 		
 		Trigger tg = mc.findTrigger(br.getTg_fk());
 		switch(tg.getEvent()) {
 			default :
 			case "UI" :
 			case "IU" :
 				tl.bindParam("trigger_event", "UPDATE OR INSERT");
 				break;
 			case "U" :
 				tl.bindParam("trigger_event", "UPDATE");
 				break;
 			case "I" :
 				tl.bindParam("trigger_event", "INSERT");
 				break;
 		}
 		
 		APP_Error err = mc.findAppError(br.getErr_fk());
 		tl.bindParam("error_code", err.getCode());
 		tl.bindParam("error_message", err.getErr_comment());
 		
 		// For future multi-table support.
 		// no time for that now.
 		ArrayList<Table> tb_list = mc.findTriggerTable(tg.getTg_id());
 		Table tb = tb_list.get(0);
 		tl.bindParam("trigger_table", tb.getName());
 		
 
 		tl.bindParam("trigger_name", "APP_" + tb.getName().toUpperCase() + "_" + brt.getCode().toUpperCase() + "_" + br.getBr_id());
 		
 		return tl.getString();
 	}
 
 	private String generateICMP(BusinessRule br, BusinessRuleType brt) {
 		TemplateLoader tl = new TemplateLoader(new File("templates/plsql/icmp.tpl"));
 		
 		Operator o = mc.findOperator(br.getBr_id());
 		tl.bindParam("operator", o.getOperator());
 		
 		TableColumn tbc1 = mc.findTableColumn(o.getTc1_fk());
 		Table tb1 = mc.findTable(tbc1.getTb_fk());
		tl.bindParam("first_value", tb1.getName() + "." + tbc1.getName());
 
 		TableColumn tbc2 = mc.findTableColumn(o.getTc2_fk());
 		Table tb2 = mc.findTable(tbc2.getTb_fk());
		tl.bindParam("second_value", tb2.getName() + "." + tbc2.getName());
 		
 		Trigger tg = mc.findTrigger(br.getTg_fk());
 		switch(tg.getEvent()) {
 			default :
 			case "UI" :
 			case "IU" :
 				tl.bindParam("trigger_event", "UPDATE OR INSERT");
 				break;
 			case "U" :
 				tl.bindParam("trigger_event", "UPDATE");
 				break;
 			case "I" :
 				tl.bindParam("trigger_event", "INSERT");
 				break;
 		}
 		
 		APP_Error err = mc.findAppError(br.getErr_fk());
 		tl.bindParam("error_code", err.getCode());
 		tl.bindParam("error_message", err.getErr_comment());
 		
 		// For future multi-table support.
 		// no time for that now.
 		ArrayList<Table> tb_list = mc.findTriggerTable(tg.getTg_id());
 		Table tb = tb_list.get(0);
 		tl.bindParam("trigger_table", tb.getName());
 		
 
 		tl.bindParam("trigger_name", "APP_" + tb.getName().toUpperCase() + "_" + brt.getCode().toUpperCase() + "_" + br.getBr_id());
 		
 		return tl.getString();
 	}
 
 
 	private String generateTCMP(BusinessRule br, BusinessRuleType brt) {
 		TemplateLoader tl = new TemplateLoader(new File("templates/plsql/tcmp.tpl"));
 		
 		Operator o = mc.findOperator(br.getBr_id());
 		tl.bindParam("operator", o.getOperator());
 		
 		TableColumn tbc = mc.findTableColumn(o.getTc1_fk());
 		tl.bindParam("first_value", tbc.getName());
 
 		TableColumn tbc2 = mc.findTableColumn(o.getTc2_fk());
 		tl.bindParam("second_value", tbc2.getName());
 		
 		Trigger tg = mc.findTrigger(br.getTg_fk());
 		switch(tg.getEvent()) {
 			default :
 			case "UI" :
 			case "IU" :
 				tl.bindParam("trigger_event", "UPDATE OR INSERT");
 				break;
 			case "U" :
 				tl.bindParam("trigger_event", "UPDATE");
 				break;
 			case "I" :
 				tl.bindParam("trigger_event", "INSERT");
 				break;
 		}
 		
 		APP_Error err = mc.findAppError(br.getErr_fk());
 		tl.bindParam("error_code", err.getCode());
 		tl.bindParam("error_message", err.getErr_comment());
 		
 		// For future multi-table support.
 		// no time for that now.
 		ArrayList<Table> tb_list = mc.findTriggerTable(tg.getTg_id());
 		Table tb = tb_list.get(0);
 		tl.bindParam("trigger_table", tb.getName());
 		
 
 		tl.bindParam("trigger_name", "APP_" + tb.getName().toUpperCase() + "_" + brt.getCode().toUpperCase() + "_" + br.getBr_id());
 		
 		return tl.getString();
 	}
 
 
 
 	private String generateOther(BusinessRule br, BusinessRuleType brt) {
 		TemplateLoader tl = new TemplateLoader(new File("templates/plsql/other.tpl"));
 		
 		Operator o = mc.findOperator(br.getBr_id());
 		tl.bindParam("operator", o.getOperator());
 		
 		CustomCode cc = mc.findCustomCode(o.getOp_id());
 		tl.bindParam("custom_code", cc.getCustomcode());
 		
 		Trigger tg = mc.findTrigger(br.getTg_fk());
 		switch(tg.getEvent()) {
 			default :
 			case "UI" :
 			case "IU" :
 				tl.bindParam("trigger_event", "UPDATE OR INSERT");
 				break;
 			case "U" :
 				tl.bindParam("trigger_event", "UPDATE");
 				break;
 			case "I" :
 				tl.bindParam("trigger_event", "INSERT");
 				break;
 		}
 		
 		APP_Error err = mc.findAppError(br.getErr_fk());
 		tl.bindParam("error_code", err.getCode());
 		tl.bindParam("error_message", err.getErr_comment());
 		
 		// For future multi-table support.
 		// no time for that now.
 		ArrayList<Table> tb_list = mc.findTriggerTable(tg.getTg_id());
 		Table tb = tb_list.get(0);
 		tl.bindParam("trigger_table", tb.getName());
 		
 
 		tl.bindParam("trigger_name", "APP_" + tb.getName().toUpperCase() + "_" + brt.getCode().toUpperCase() + "_" + br.getBr_id());
 		
 		return tl.getString();
 	}
 
 	public String getName() {
 		return name;
 	}
 }
