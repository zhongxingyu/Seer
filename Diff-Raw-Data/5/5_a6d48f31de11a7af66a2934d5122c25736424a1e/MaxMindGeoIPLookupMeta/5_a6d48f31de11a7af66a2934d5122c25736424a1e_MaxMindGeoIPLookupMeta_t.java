 package plugin.com.maxmind.geoip;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.widgets.Shell;
 import org.pentaho.di.core.CheckResult;
 import org.pentaho.di.core.CheckResultInterface;
 import org.pentaho.di.core.Counter;
 import org.pentaho.di.core.database.DatabaseMeta;
 import org.pentaho.di.core.exception.KettleDatabaseException;
 import org.pentaho.di.core.exception.KettleException;
 import org.pentaho.di.core.exception.KettleXMLException;
 import org.pentaho.di.core.row.RowMetaInterface;
 import org.pentaho.di.core.row.ValueMeta;
 import org.pentaho.di.core.row.ValueMetaInterface;
 import org.pentaho.di.core.variables.VariableSpace;
 import org.pentaho.di.core.xml.XMLHandler;
 import org.pentaho.di.repository.Repository;
 import org.pentaho.di.trans.Trans;
 import org.pentaho.di.trans.TransMeta;
 import org.pentaho.di.trans.step.BaseStepMeta;
 import org.pentaho.di.trans.step.StepDataInterface;
 import org.pentaho.di.trans.step.StepDialogInterface;
 import org.pentaho.di.trans.step.StepInterface;
 import org.pentaho.di.trans.step.StepMeta;
 import org.pentaho.di.trans.step.StepMetaInterface;
 import org.pentaho.di.core.annotations.Step;
 import org.pentaho.di.trans.step.StepCategory;
 import org.w3c.dom.Node;
 
 /*
  * @author Daniel Einspanjer
  * @since  April-16-2008
  */
 
 @Step(name = "MaxMindGeoIPLookup", image = "plugin/com/maxmind/geoip/MGL.png",
 		tooltip = "MaxMindGeoIPLookupMeta.TypeTooltipDesc.MaxMindGeoIPLookup", 
 		description = "MaxMindGeoIPLookupMeta.TypeLongDesc.MaxMindGeoIPLookup", 
 		category = StepCategory.CATEGORY_LOOKUP)
 public class MaxMindGeoIPLookupMeta extends BaseStepMeta implements StepMetaInterface
 {
 	private String ipAddressFieldName;	
 	private String dbLocation;
     private String  fieldName[];
     private String  fieldLookupType[];
     private String  fieldIfNull[];
 
     public String getIpAddressFieldName()
 	{
 		return ipAddressFieldName;
 	}
 
 	public void setIpAddressFieldName(String ipAddressFieldName)
 	{
 		this.ipAddressFieldName = ipAddressFieldName;
 	}
 
 	public String getDbLocation() {
 		return dbLocation;
 	}
 
 	public void setDbLocation(String dbLocation) {
 		this.dbLocation = dbLocation;
 	}
 
 	public String[] getFieldName() {
 		return fieldName;
 	}
 
 	public void setFieldName(String[] fieldName) {
 		this.fieldName = fieldName;
 	}
 
 	public String[] getFieldLookupType() {
 		return fieldLookupType;
 	}
 
 	public void setFieldLookupType(String[] fieldLookupType) {
 		this.fieldLookupType = fieldLookupType;
 	}
 	public String[] getFieldIfNull() {
 		return fieldIfNull;
 	}
 
 	public void setFieldIfNull(String[] fieldIfNull) {
 		this.fieldIfNull = fieldIfNull;
 	}
 
 	public MaxMindGeoIPLookupMeta()
 	{
 		super(); // allocate BaseStepInfo
 	}
 
 	public String getXML()
 	{
         final StringBuilder retval = new StringBuilder(500);
 
         retval.append("   ").append(XMLHandler.addTagValue("ip_address_field_name", ipAddressFieldName)); //$NON-NLS-1$ //$NON-NLS-2$
         retval.append("   ").append(XMLHandler.addTagValue("db_location", dbLocation)); //$NON-NLS-1$ //$NON-NLS-2$
 
         retval.append("    <fields>"); //$NON-NLS-1$
         for (int i = 0; i < fieldName.length; i++)
         {
             retval.append("      <field>"); //$NON-NLS-1$
             retval.append("        ").append(XMLHandler.addTagValue("name", fieldName[i])); //$NON-NLS-1$ //$NON-NLS-2$
             retval.append("        ").append(XMLHandler.addTagValue("lookup_type", fieldLookupType[i])); //$NON-NLS-1$ //$NON-NLS-2$
             retval.append("        ").append(XMLHandler.addTagValue("ifnull", fieldIfNull[i])); //$NON-NLS-1$ //$NON-NLS-2$
             retval.append("      </field>"); //$NON-NLS-1$
         }
         retval.append("    </fields>"); //$NON-NLS-1$
 		return retval.toString();
 	}
 
 	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
 	{
 		//TODO: Turn these into a configurable fields table
 		ValueMetaInterface v = new ValueMeta("country_code", ValueMeta.TYPE_STRING, 2, 0);
 		v.setOrigin(origin);
 		r.addValueMeta(v);
 		
 		v = new ValueMeta("country_name", ValueMeta.TYPE_STRING, 44, 0);
 		v.setOrigin(origin);
 		r.addValueMeta(v);
 		
 		v = new ValueMeta("region_code", ValueMeta.TYPE_STRING, 2, 0);
 		v.setOrigin(origin);
 		r.addValueMeta(v);
 		
 		v = new ValueMeta("region_name", ValueMeta.TYPE_STRING, 44, 0);
 		v.setOrigin(origin);
 		r.addValueMeta(v);
 		
 		v = new ValueMeta("city_name", ValueMeta.TYPE_STRING, 255, 0);
 		v.setOrigin(origin);
 		r.addValueMeta(v);
 		
		v = new ValueMeta("latitude", ValueMeta.TYPE_NUMBER, 10, 4);
 		v.setOrigin(origin);
 		r.addValueMeta(v);
 		
		v = new ValueMeta("longitude", ValueMeta.TYPE_NUMBER, 10, 4);
 		v.setOrigin(origin);
 		r.addValueMeta(v);
 		
 		v = new ValueMeta("timezone", ValueMeta.TYPE_STRING, 255, 0);
 		v.setOrigin(origin);
 		r.addValueMeta(v);
 		
 	}
 
     public void allocate(int nrfields)
     {
         fieldName = new String[nrfields];
         fieldLookupType = new String[nrfields];
         fieldIfNull = new String[nrfields];
     }
 
 	public Object clone()
 	{
 		MaxMindGeoIPLookupMeta retval = (MaxMindGeoIPLookupMeta) super.clone();
         final int nrfields = fieldName.length;
 
         retval.allocate(nrfields);
 
         for (int i=0;i<nrfields;i++)
         {
             retval.fieldName[i] = fieldName[i];
             retval.fieldLookupType[i] = fieldLookupType[i];
             retval.fieldIfNull[i] = fieldIfNull[i];
         }
 		return retval;
 	}
 
 	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
 		throws KettleXMLException
 	{
 		try
 		{
 			setIpAddressFieldName(XMLHandler.getTagValue(stepnode, "ip_address_field_name"));
 			setDbLocation(XMLHandler.getTagValue(stepnode, "db_location"));
 
 			final Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
             final int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
 
             allocate(nrfields);
 
             for (int i=0;i<nrfields;i++)
             {
                 final Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
 
                 fieldName[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
                 fieldLookupType[i]  = XMLHandler.getTagValue(fnode, "lookup_type"); //$NON-NLS-1$
                 fieldIfNull[i] = XMLHandler.getTagValue(fnode, "ifnull"); //$NON-NLS-1$
             }
 		}
 		catch(Exception e)
 		{
 			throw new KettleXMLException("Unable to read step info from XML node", e);
 		}
 	}
 
 	public void setDefault()
 	{
 		ipAddressFieldName = "";
 		dbLocation = "";
 		allocate(0);
 	}
 
 	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String,Counter> counters) throws KettleException
 	{
 		try
 		{
 			setIpAddressFieldName(rep.getStepAttributeString (id_step, 0, "ip_address_field_name"));
 			setDbLocation(rep.getStepAttributeString(id_step, "db_location"));
 
             int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
 
             allocate(nrfields);
 
             for (int i=0;i<nrfields;i++)
             {
                 fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
                 fieldLookupType[i] = rep.getStepAttributeString (id_step, i, "field_lookup_type" ); //$NON-NLS-1$
                 fieldIfNull[i] = rep.getStepAttributeString(id_step, i, "field_ifnull"); //$NON-NLS-1$
             }
 }
 		catch(KettleDatabaseException dbe)
 		{
 			throw new KettleException("error reading step with id_step="+id_step+" from the repository", dbe);
 		}
 		catch(Exception e)
 		{
 			throw new KettleException("Unexpected error reading step with id_step="+id_step+" from the repository", e);
 		}
 	}
 	
 	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
 	{
 		try
 		{
 			rep.saveStepAttribute(id_transformation, id_step, "ip_address_field_name", getIpAddressFieldName());
 			rep.saveStepAttribute(id_transformation, id_step, "db_location", getDbLocation());
 
             for (int i = 0; i < fieldName.length; i++)
             {
                 rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]); //$NON-NLS-1$
                 rep.saveStepAttribute(id_transformation, id_step, i, "field_lookup_type", fieldLookupType[i]); //$NON-NLS-1$
                 rep.saveStepAttribute(id_transformation, id_step, i, "field_ifnull", fieldIfNull[i]); //$NON-NLS-1$
             }
 }
 		catch(KettleDatabaseException dbe)
 		{
 			throw new KettleException("Unable to save step information to the repository, id_step="+id_step, dbe);
 		}
 	}
 
 	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
 	{
 		//TODO: l10n all these messages
 		//TODO: Check for maxmind .dat file presence
 
 		CheckResult cr;
 		if (prev==null || prev.size()==0)
 		{
 			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepMeta);
 			remarks.add(cr);
 		}
 		else
 		{
 			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepMeta);
 			remarks.add(cr);
 		}
 		
 		// See if we have input streams leading to this step!
 		if (input.length>0)
 		{
 			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
 			remarks.add(cr);
 		}
 		else
 		{
 			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
 			remarks.add(cr);
 		}
 		
 		if (prev.indexOfValue(getIpAddressFieldName()) < 0)
 		{
 			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "IP Address field not found.", stepMeta);
 		}
 		else
 		{
 			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "IP Address field found.", stepMeta);
 		}
 		
 		if (getDbLocation() == null || getDbLocation().length() == 0)
 		{
 			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "MaxMind GeoIP DB Location not specified.", stepMeta);
 		}
 		else
 		{
 			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "MaxMind GeoIP DB Location is specified.", stepMeta);
 		}
 	}
 	
 	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name)
 	{
 		return new MaxMindGeoIPLookupDialog(shell, meta, transMeta, name);
 	}
 
 	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
 	{
 		return new MaxMindGeoIPLookup(stepMeta, stepDataInterface, cnr, transMeta, disp);
 	}
 
 	public StepDataInterface getStepData()
 	{
 		return new MaxMindGeoIPLookupData();
 	}
 }
