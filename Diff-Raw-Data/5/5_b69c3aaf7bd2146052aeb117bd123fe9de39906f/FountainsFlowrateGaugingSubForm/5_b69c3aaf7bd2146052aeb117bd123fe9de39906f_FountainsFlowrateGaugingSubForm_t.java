 package es.icarto.gvsig.sixhiara.forms;
 
 import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
 
 @SuppressWarnings("serial")
 public class FountainsFlowrateGaugingSubForm extends AbstractSubForm {
 
     public static final String TABLENAME = "quantidade_agua";
    public static String[] colNames = { "COD_FONTE", "DATA", "HORA",
 	    "QUAN_AGUA", "Q_EXTRAER" };
    public static String[] colAlias = { "Cod Fonte", "Data", "Hora",
 	    "Quantidade agua", "Caudal extrado" };
 
     @Override
     protected void fillSpecificValues() {
 
     }
 
     @Override
     protected String getBasicName() {
 	return TABLENAME;
     }
 
 }
