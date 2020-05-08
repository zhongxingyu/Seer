 package es.udc.cartolab.gvsig.fonsagua.forms.comunidades;
 
 import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
 
 @SuppressWarnings("serial")
 public class EntrevistadoresForm extends AbstractSubForm {

     public static final String NAME = "entrevistadores";
    public static String[] colNames = { "nombre", "cargo", "instit", "telefono" };
    public static String[] colAlias = { "Nombre", "Cargo", "Institucin",
	    "Telfono Institucin" };
 
     @Override
     protected void fillSpecificValues() {
     }
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
 }
