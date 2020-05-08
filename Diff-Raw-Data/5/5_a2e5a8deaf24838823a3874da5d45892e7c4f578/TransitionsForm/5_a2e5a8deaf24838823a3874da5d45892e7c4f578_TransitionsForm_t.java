 package org.vamdc.portal.session.queryBuilder.forms;
 
 import java.util.ArrayList;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.QueryData;
 import org.vamdc.portal.session.queryBuilder.fields.AbstractField;
 import org.vamdc.portal.session.queryBuilder.fields.RangeField;
 import org.vamdc.portal.session.queryBuilder.fields.UnitConvRangeField;
 import org.vamdc.portal.session.queryBuilder.unitConv.EnergyUnitConverter;
 
 public class TransitionsForm extends AbstractForm implements Form{
 
 	public String getTitle() { return "Transitions"; }
 	public Integer getOrder() { return Order.Transitions; }
 	public String getView() { return "/xhtml/query/forms/standardForm.xhtml"; }
 	
 	public TransitionsForm(QueryData queryData){
 		super(queryData);
 		fields = new ArrayList<AbstractField>();
 		fields.add(new RangeField(Restrictable.RadTransWavelength,"Wavelength"));
 		AbstractField field = new UnitConvRangeField(Restrictable.StateEnergy, "Upper state energy", new EnergyUnitConverter());
		field.setPrefix("upper.");
 		fields.add(field);
 		//fields.add(new RangeField("upper",Restrictable.StateEnergy,"Upper state energy"));
 		field = new UnitConvRangeField(Restrictable.StateEnergy, "Lower state energy", new EnergyUnitConverter());
		field.setPrefix("lower.");
 		fields.add(field);
 		//fields.add(new RangeField("lower",Restrictable.StateEnergy,"Lower state energy"));
 		fields.add(new RangeField(Restrictable.RadTransProbabilityA,"Probability, A"));
 	}
 
 
 	
 
 }
