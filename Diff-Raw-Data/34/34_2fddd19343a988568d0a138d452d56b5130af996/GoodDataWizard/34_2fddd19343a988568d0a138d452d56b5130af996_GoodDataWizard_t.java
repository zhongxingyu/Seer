 package org.snaplogic.snap.gooddata;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.snaplogic.cc.Capabilities;
 import org.snaplogic.cc.Capability;
 import org.snaplogic.cc.ComponentContainer;
 import org.snaplogic.cc.InputView;
 import org.snaplogic.cc.OutputView;
 import org.snaplogic.cc.prop.DictProp;
 import org.snaplogic.cc.prop.ListProp;
import org.snaplogic.cc.prop.ListPropErr;
 import org.snaplogic.cc.prop.SimpleProp;
 import org.snaplogic.cc.prop.SimpleProp.SimplePropType;
 import org.snaplogic.common.ComponentResourceErr;
 import org.snaplogic.common.Field;
 import org.snaplogic.common.SnapHttpLib;
 import org.snaplogic.common.Field.SnapFieldType;
 import org.snaplogic.common.exceptions.SnapComponentException;
 import org.snaplogic.snapi.ExtendedSnapi;
 import org.snaplogic.snapi.Keys;
 import org.snaplogic.snapi.PropertyConstraint;
 import org.snaplogic.snapi.ResDef;
 import org.snaplogic.snapi.Snapi;
 import org.snaplogic.snapi.PropertyConstraint.Type;
import org.snaplogic.util.ConvertUtils;
 
 import com.gooddata.connector.CsvConnector;
 import com.gooddata.connector.DateDimensionConnector;
 import com.gooddata.connector.backend.DerbyConnectorBackend;
 import com.gooddata.connector.model.PdmSchema;
 import com.gooddata.exception.GdcLoginException;
 import com.gooddata.exception.GdcRestApiException;
 import com.gooddata.integration.model.Project;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.processor.Command;
 import com.gooddata.processor.ProcessingContext;
 
 /**
  * For information on the usage of this see {@link GoodDataWizard#checkDateFormats} method
  * 
  * @author grisha@snaplogic.com
  */
 class NumericFieldsCannotBeSpecifiedAsDatesException extends Exception {
 	private ComponentResourceErr err;
 
 	NumericFieldsCannotBeSpecifiedAsDatesException(ComponentResourceErr err) {
 		super();
 		this.err = err;
 	}
 
 	ComponentResourceErr getErr() {
 		return err;
 	}
 
 }
 
 /**
  * Wizard component for creating GoodData data flows. This depends on functionality available as of SnapLogic 2.3.0. Be
  * sure to make directives file of the Snap that includes this functionality to start with <tt>MIN_VERSION=2.3</tt>.
  * 
  * @author grisha@snaplogic.com
  * 
  * @since SnapLogic 2.3.0
  */
 public class GoodDataWizard extends AbstractGoodDataComponent {
 
 	public static final String[] LDM_TYPES = new String[] { SourceColumn.LDM_TYPE_ATTRIBUTE,
 			SourceColumn.LDM_TYPE_CONNECTION_POINT, SourceColumn.LDM_TYPE_DATE, SourceColumn.LDM_TYPE_FACT,
 			SourceColumn.LDM_TYPE_IGNORE, SourceColumn.LDM_TYPE_LABEL, SourceColumn.LDM_TYPE_REFERENCE };
 
 	public static final String DEFAULT_LDM_DATE_FORMAT = "yyyy-MM-dd";
 
 	public static final String PROP_OVERWRITE = "overwrite";
 
 	public static final String PROP_GD_URI_PREFIX = "uri_prefix";
 
 	public static String PROP_DLI_DEFINITION = "dli_definition";
 
 	public static String PROP_DATE_FORMAT_DEFINITION = "date_format_definition";
 
 	public static final String DEFAULT_GD_URI_PREFIX = "/gd";
 
 	private static final String CREATE_NEW_CONNECTION = "Create new...";
 
 	private static final String PROP_WIZARD_STEP = "wizard_step";
 
 	private static final String LABEL_KEY_FIELD_NAME = "field_name";
 	private static final String LABEL_KEY_PARENT = "parent";
 	private static final String CREATE_NEW_PROJECT = "Create new...";
 	private static final String PROP_DLI_NAME = "dli_name";
 	public static String PROP_LABEL_DEFINITION = "labels_definition";
 
 	private static final String DLI_SPEC_KEY_FIELD_NAME = "field_name";
 
 	private static final String DLI_SPEC_KEY_LDM_TYPE = "ldm_type";
 
 	private static final String DATE_FORMAT_KEY_FIELD_NAME = "field_name";
 
 	private static final String DATE_FORMAT_KEY_FORMAT = "format";
 
 	private static final String PROP_NEW_PROJECT_NAME = "new_project_name";
 
 	private static final String PROP_NEW_PROJECT_DESCR = "new_project_descr";
 
 	private static final String PROP_GD_COMP_NAME = "component_name";
 
 	private static final Object DEFAULT_GD_COMP_NAME = "GoodDataUpload";
 
 	private static final String PROP_GD_CONN_NAME = "new_connection_name";
 
 	private static final Object DEFAULT_GD_CONN_NAME = "GoodDataConnection";
 
 	private static final String PROP_GD_INPUT = "gd_input";
 
 	private static final String PROP_REFERENCE_DEFINITION = "reference_definition";
 
 	private static final String REFERENCE_KEY_FIELD_NAME = "field_name";
 
 	private static final String REFERENCE_KEY_TARGET = "reference_target";
 
 	private static final Object SAMPLE_REFERENCE = "Dataset:Column";
 
 	private static final String DATE_FORMAT_KEY_DIMENSION = "df_dimension";
 
 	private static final String DATE_FORMAT_CREATE_DD = "Create new...";
 
 	private static final String DATE_FORMAT_CONNECT_DD = "Reuse existing...";
 
 	private static final String DATE_FORMAT_REF_OR_NAME = "df_ref_or_name";
 
 	private static final Object DATE_FORMAT_REF_NAME_SAMPLE = "";
 
 	@Override
 	public String getDescription() {
 		return "GoodData Project Setup";
 	}
 
 	@Override
 	public Capabilities getCapabilities() {
 		return new Capabilities() {
 			private static final long serialVersionUID = 3848994857590856035L;
 
 			{
 				put(Capability.INPUT_VIEW_LOWER_LIMIT, 0);
 				put(Capability.INPUT_VIEW_UPPER_LIMIT, 0);
 				put(Capability.OUTPUT_VIEW_LOWER_LIMIT, 0);
 				put(Capability.OUTPUT_VIEW_UPPER_LIMIT, 0);
 				put(Capability.WIZARD, true);
 			}
 		};
 	}
 
 	@Override
 	public String getLabel() {
 		return "GoodData Wizard (Java)";
 	}
 
 	@Override
 	public String getAPIVersion() {
 		return "1.0";
 	}
 
 	@Override
 	public String getComponentVersion() {
 		return "1.0";
 	}
 
 	// TODO this becomes an actual method in ComponentAPI
 	// but not yet
 	private void hidePropertiesTmp(String... propNames) {
 		for (String propName : propNames) {
 			setPropertyHidden(propName, true);
 		}
 	}
 
 	// TODO this should become part of Snaplogic's ComponentAPI
 	// but after 2.3.0 (where it will be done in a much more efficient way)
 	protected List<String> getResourcesMatchingCategory(List<String> cats) {
 		info("Looking for resources matching %s", GoodDataConnection.CONNECTION_CATEGORIES);
 		Snapi snapi = Snapi.getSnapi();
 		String serverUri = ComponentContainer.getServerUri();
 		List resList = snapi.listResources(serverUri, null, null);
 		Map<String, Object> resMap = (Map) resList.get(0);
 		List uriList = new ArrayList();
 		List candidates = new ArrayList();
 		for (String relUri : resMap.keySet()) {
 			String absUri = SnapHttpLib.concatPaths(serverUri, relUri);
 			Map resource = snapi.readResource(absUri, null);
 			resource = (Map) resource.get(absUri);
 			Map resdefMap = (Map) resource.get(Keys.RESDEF);
 			ResDef resdef = new ResDef(resdefMap);
 			// TODO resdef should have getResourceCategories method!
 			Map catMap = (Map) resdefMap.get(Keys.RESOURCE_CATEGORY);
 			if (catMap == null) {
 				continue;
 			}
 			List<String> catList = (List) catMap.get(Keys.VALUE);
 			if (catList == null) {
 				continue;
 			}
 			for (String cat : catList) {
 				if (cat.equals(GoodDataConnection.GOODDATA_CONNECTION_CATEGORY)) {
 					candidates.add(absUri);
 				}
 			}
 		}
 		return candidates;
 	}
 
 	@Override
 	public void createResourceTemplate() {
 		// TODO until more data is gathered about this usage, we cannot
 		// provide SDK functions that are both usable and not redundant.
 		// So this part is manual - here we're getting a list of properties
 		// used in this step that will be hidden in the next step of the Wizard
 		// This will become clearer once documentation for Wizard is up.
 
 		// First step: do we use existing connection or create a new one?
 		// TODO wizard currently does not support Resource References
 		// when it does, this should be changed for better user experience.
 		// This is not a connection reference here, but we'll reuse the same
 		// property name
 		List<String> connections = getResourcesMatchingCategory(GoodDataConnection.CONNECTION_CATEGORIES);
 		List<String> availableGDConnections = cutServerUrl(connections);
 		availableGDConnections.add(0, CREATE_NEW_CONNECTION);
 		// TODO PropertyConstraint constructor should be lenient enough to take
 		// a Collection
 		PropertyConstraint availableGDConnectionsConstraint = new PropertyConstraint(Type.LOV, availableGDConnections
 				.toArray());
 		SimpleProp connProp = new SimpleProp("GoodData Connection", SimplePropType.SnapString, "GoodData Connection",
 				availableGDConnectionsConstraint, true);
 		setPropertyDef(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF, connProp);
 		String curConValue = getStringPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF);
 		if (curConValue == null) {
 			int idx = 0;
 			if (availableGDConnections.size() > 1) {
 				idx = 1;
 			}
 			setPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF, availableGDConnections.get(idx));
 		}
 
 		setPropertyDef(PROP_GD_URI_PREFIX, new SimpleProp("URI prefix", SimplePropType.SnapString, "URI prefix", true));
 		setPropertyValue(PROP_GD_URI_PREFIX, DEFAULT_GD_URI_PREFIX);
 
 		setPropertyDef(PROP_GD_COMP_NAME, new SimpleProp("Component Name", SimplePropType.SnapString, "Component Name",
 				true));
 		setPropertyValue(PROP_GD_COMP_NAME, DEFAULT_GD_COMP_NAME);
 
 		// TODO we should add another property, "overwrite" to control whether
 		// to overwrite
 		// resources that may already exist.
 		setPropertyDef(PROP_WIZARD_STEP, new SimpleProp(PROP_WIZARD_STEP, SimplePropType.SnapNumber, "",
 				new PropertyConstraint(Type.HIDDEN, true), true));
 		setPropertyValue(PROP_WIZARD_STEP, new BigDecimal(1));
 	}
 
 	private List<String> cutServerUrl(List<String> links) {
 		String serverUri = ComponentContainer.getServerUri();
 		List<String> result = new LinkedList<String>();
 		for (String link : links) {
 			result.add(link.replace(serverUri, ""));
 		}
 		return result;
 	}
 
 	private String enrichWithServerUrl(String link) {
 		String serverUri = ComponentContainer.getServerUri();
 		return serverUri + link;
 	}
 
 	/**
 	 * Step 2 of wizard - gather properties to create a {@link GoodDataConnection} if existing one hasn't been picked
 	 */
 	private void gatherConnectionProps(ComponentResourceErr err) {
 		nextStep();
 
 		setPropertyDef(PROP_GD_CONN_NAME, new SimpleProp("Connection Name", SimplePropType.SnapString,
 				"Connection Name", true));
 		setPropertyValue(PROP_GD_CONN_NAME, DEFAULT_GD_CONN_NAME);
 
 		GoodDataConnection.createGDConnectionResourceTemplate(this);
 	}
 
 	/**
 	 * Simple utility method to increment wizard step and hide currently shown properties
 	 */
 	private void nextStep() {
 		for (String propName : getResdef().listPropertyNames()) {
 			setPropertyHidden(propName, true);
 		}
 		int curStep = ((BigDecimal) getPropertyValue(PROP_WIZARD_STEP)).intValue();
 		curStep++;
 		setPropertyValue(PROP_WIZARD_STEP, new BigDecimal(curStep));
 	}
 
 	/**
 	 * Step 3 of wizard -- get output views of existing resources that can serve as input to the {@link GoodDataPut} to
 	 * be created.
 	 */
 	private void getViewCandidates(ComponentResourceErr err) {
 		nextStep();
 		info("Looking for resources matching %s", GoodDataConnection.CONNECTION_CATEGORIES);
 		Snapi snapi = Snapi.getSnapi();
 		String serverUri = ComponentContainer.getServerUri();
 		List resList = snapi.listResources(serverUri, null, null);
 		Map<String, Object> resMap = (Map) resList.get(0);
 		List uriList = new ArrayList();
 		List<String> candidates = new ArrayList<String>();
 		for (String relUri : resMap.keySet()) {
 			String absUri = SnapHttpLib.concatPaths(serverUri, relUri);
 			Map resource = snapi.readResource(absUri, null);
 			resource = (Map) resource.get(absUri);
 			Map resdefMap = (Map) resource.get(Keys.RESDEF);
 			ResDef resdef = new ResDef(resdefMap);
 			Collection<String> outViews = resdef.listOutputViewNames();
 			if (outViews.isEmpty()) {
 				continue;
 			}
 			for (String outViewName : outViews) {
 				Map<String, Object> outView = resdef.getOutputView(outViewName);
 				if (outView.get(0) == null) {
 					candidates.add(relUri + "::" + outViewName);
 				} else {
 					continue;
 				}
 			}
 		}
 		String[] candArr = (String[]) candidates.toArray(new String[] {});
 		PropertyConstraint gdInputConstraint = new PropertyConstraint(Type.LOV, candArr);
 		setPropertyDef(PROP_GD_INPUT, new SimpleProp("Input to GoodData", SimplePropType.SnapString,
 				"Output view of a resource that serves as input to GoodData", gdInputConstraint, true));
 		if (candidates.isEmpty()) {
 			err.setMessage("No resources found that can serve as input to GoodData.");
 			return;
 		}
 		setPropertyValue(PROP_GD_INPUT, candidates.get(0));
 	}
 
 	/**
 	 * Get the {@link ResDef} and fields from the view picked as the good data source in {@link #PROP_GD_INPUT} property
 	 * 
 	 * @return A {@link Map} such that key <tt>fields</tt> corresponds to <tt>List<List<String>></tt> of fields and
 	 *         {@link Keys#RESDEF} is the {@link ResDef}
 	 */
 	// TODO "fields" should be part of Keys
 	private Map<String, Object> getResDefandFieldsFromSourceView() {
 		Snapi snapi = Snapi.getSnapi();
 		String gdInput = enrichWithServerUrl(getStringPropertyValue(PROP_GD_INPUT));
 		String[] uriAndView = gdInput.split("::");
 		String uri = uriAndView[0];
 		String view = uriAndView[1];
 		ResDef gdInputResdef = getSrcResDef(uri);
 		Map<String, Object> outView = gdInputResdef.getOutputView(view);
 		List<List<String>> fields = (List) outView.get("fields");
 		Map<String, Object> retval = new HashMap<String, Object>();
 		retval.put("fields", fields);
 		retval.put(Keys.RESDEF, gdInputResdef);
 		return retval;
 	}
 
 	/**
 	 * Get the {@link ResDef} specified in {@link #PROP_GD_INPUT} property
 	 */
 	// TODO this should be a part of Java SnAPI...
 	private ResDef getSrcResDef(String uri) {
 		Snapi snapi = Snapi.getSnapi();
 		Map resdefMap = snapi.readResource(uri, null);
 		resdefMap = (Map) resdefMap.get(uri);
 		resdefMap = (Map) resdefMap.get(Keys.RESDEF);
 		ResDef resdef = new ResDef(resdefMap);
 		return resdef;
 	}
 
 	/**
 	 * Step 4 of wizard - show a DLI based on the view picked in previous step. User can edit this of course. This will
 	 * be created as a complex property, and then, in the {@link #execute(Map, Map)} method XML will be written out and
 	 * loaded into GoodData
 	 * 
 	 * @param err
 	 */
 	private void getDLIConfig(ComponentResourceErr err) {
 		nextStep();
 		Map<String, Object> resdefAndFields = getResDefandFieldsFromSourceView();
 		List<List<String>> fields = (List<List<String>>) resdefAndFields.get("fields");
 		SimpleProp stringProp = new SimpleProp("Field", SimplePropType.SnapString, "");
 		DictProp fieldDefProp = new DictProp("Field definition", stringProp, "", 2, 2, true, true);
 		fieldDefProp.put(DLI_SPEC_KEY_FIELD_NAME, new SimpleProp("Field name", SimplePropType.SnapString, "string1"));
 		PropertyConstraint ldmTypeConstraint = new PropertyConstraint(Type.LOV, LDM_TYPES);
 		fieldDefProp.put(DLI_SPEC_KEY_LDM_TYPE, new SimpleProp("LDM Type", SimplePropType.SnapString, "",
 				ldmTypeConstraint, true));
 		ListProp dliSpec = new ListProp("DLI definition", fieldDefProp, "", fields.size(), fields.size(), true);
 		setPropertyDef(PROP_DLI_DEFINITION, dliSpec);
 		List dliSpecValue = new ArrayList();
 		for (List<String> field : fields) {
 			Map fieldSpec = new HashMap();
 			fieldSpec.put(DLI_SPEC_KEY_FIELD_NAME, field.get(0));
 			String snapTypeStr = field.get(1);
 			if (snapTypeStr.equals(SnapFieldType.SnapDateTime.toString())) {
 				fieldSpec.put(DLI_SPEC_KEY_LDM_TYPE, SourceColumn.LDM_TYPE_DATE);
 			} else if (snapTypeStr.equals(SnapFieldType.SnapNumber.toString())) {
 				fieldSpec.put(DLI_SPEC_KEY_LDM_TYPE, SourceColumn.LDM_TYPE_FACT);
 			} else {
 				fieldSpec.put(DLI_SPEC_KEY_LDM_TYPE, SourceColumn.LDM_TYPE_ATTRIBUTE);
 			}
 			dliSpecValue.add(fieldSpec);
 		}
 		setPropertyValue(PROP_DLI_DEFINITION, dliSpecValue);
 		setPropertyDef("dli_name", new SimpleProp("DLI Name", SimpleProp.SimplePropType.SnapString, "DLI Name", true));
 	}
 
 	private void getProjects(ComponentResourceErr err) {
 		nextStep();
 
 		PropertyConstraint projNameLovConstraint = new PropertyConstraint();
 		SimpleProp projNameProp = new SimpleProp("Project name", SimpleProp.SimplePropType.SnapString, "Project name",
 				projNameLovConstraint, true);
 		String gdConnUri = getStringPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF);
 		ArrayList<String> projectArrayList = new ArrayList<String>();
 		projectArrayList.add(0, CREATE_NEW_PROJECT);
 
 		ResDef conResDef;
 		if (CREATE_NEW_CONNECTION.equalsIgnoreCase(gdConnUri)) {
 			ExtendedSnapi snapi = ExtendedSnapi.getExtendedSnapi();
 			String serverUri = ComponentContainer.getServerUri();
 			conResDef = createConnectionObject(snapi, serverUri);
 		} else {
 			conResDef = getResourceObject(enrichWithServerUrl(gdConnUri), null);
 		}
 
 		GdcRESTApiWrapper restApi;
 		try {
 			restApi = GoodDataConnection.login(conResDef, this);
 		} catch (GdcLoginException gdcle) {
 			elog(gdcle);
 			if (err == null) {
 				throw new SnapComponentException(gdcle);
 			}
 			err.getResourceRefErr(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF).setMessage(
 					"Could not validate connection reference (wrong username/password?): %s", new Object[] { gdcle });
 			return;
 		}
 
 		HashMap<String, Project> projectList = GoodDataApiHelper.getProjectList(restApi);
 		Set<String> projectSet = projectList.keySet();
 		projectArrayList.addAll(projectSet);
 
 		projNameLovConstraint.put(PropertyConstraint.Type.LOV, projectArrayList.toArray(new String[0]));
 		setPropertyDef(GoodDataPut.PROP_PROJECT_NAME, projNameProp);
 
 		setPropertyValue(GoodDataPut.PROP_PROJECT_NAME, projectArrayList.get(0));
 	}
 
 	/*
 	 * * To create a wizard screen for field formats that are
 	 * 
 	 * @param fieldsToFormat list of fields that need formats specified as returned by {@link
 	 * #checkDateFormats(ComponentResourceErr)}
 	 */
 	private void getDateFormats(ComponentResourceErr err, List<String> fieldsToFormat) {
 		nextStep();
 		SimpleProp stringProp = new SimpleProp("Field", SimplePropType.SnapString, "");
 		DictProp fieldDefProp = new DictProp("Field definition", stringProp, "", 4, 4, true, true);
 		fieldDefProp.put(DATE_FORMAT_KEY_FIELD_NAME,
 				new SimpleProp(" Field name", SimplePropType.SnapString, "string1"));
 		fieldDefProp.put(DATE_FORMAT_KEY_FORMAT, new SimpleProp(" Format", SimplePropType.SnapString, "", null, true));
 		PropertyConstraint schemaConstrain = new PropertyConstraint(Type.LOV, new String[] { DATE_FORMAT_CREATE_DD,
 				DATE_FORMAT_CONNECT_DD });
 		fieldDefProp.put(DATE_FORMAT_KEY_DIMENSION, new SimpleProp("Create Schema", SimplePropType.SnapString,
 				"Create a new Date Dimension, or connect an existing one?", schemaConstrain, true));
 		fieldDefProp.put(DATE_FORMAT_REF_OR_NAME, new SimpleProp("Name/Reference", SimplePropType.SnapString, "", null,
 				true));
 		ListProp dateFormatSpec = new ListProp("Date formats", fieldDefProp, "", fieldsToFormat.size(), fieldsToFormat
 				.size(), true);
 		setPropertyDef(PROP_DATE_FORMAT_DEFINITION, dateFormatSpec);
 		List formatSpecValue = new ArrayList();
 		for (String field : fieldsToFormat) {
 			Map fieldSpec = new HashMap();
 			fieldSpec.put(DATE_FORMAT_KEY_FIELD_NAME, field);
 			fieldSpec.put(DATE_FORMAT_KEY_FORMAT, DEFAULT_LDM_DATE_FORMAT);
 			fieldSpec.put(DATE_FORMAT_KEY_DIMENSION, DATE_FORMAT_CREATE_DD);
 			fieldSpec.put(DATE_FORMAT_REF_OR_NAME, DATE_FORMAT_REF_NAME_SAMPLE);
 			formatSpecValue.add(fieldSpec);
 		}
 		setPropertyValue(PROP_DATE_FORMAT_DEFINITION, formatSpecValue);
 	}
 
 	private void setEmptyHiddenProperty(String name) {
 		setPropertyDef(name, new SimpleProp(name, SimplePropType.SnapString, "", new PropertyConstraint(Type.HIDDEN,
 				true), false));
 		setPropertyValue(name, null);
 	}
 
 	/**
 	 * This is to be run in step 4. The logic is this: If there are fields that have type
 	 * {@link SnapFieldType#SnapString} and are specified as {@link #LDM_TYPE_DATE} by the user in the previous step,
 	 * this method returns a {@link List} of them to be used in {@link #getDateFormats()} in the same step. If fields
 	 * specified as {@link #LDM_TYPE_DATE} are {@link SnapFieldType#SnapDateTime}, we don't need a user to enter a
 	 * format, we can implicitly specify it as {@link #DEFAULT_LDM_DATE_FORMAT} since we control it. If a field
 	 * specified as <tt>Date</tt> is a {@link SnapFieldType#SnapNumber} then we throw
 	 * {@link NumericFieldsCannotBeSpecifiedAsDatesException} to an error and go no further.
 	 * 
 	 * @throws NumericFieldsCannotBeSpecifiedAsDatesException
 	 *             if any fields of type {@link SnapNumber} are
 	 */
 	private List<String> checkDateFormats(ComponentResourceErr err) {
 		// Get result of the DLI specification
 		List<Map> dliSpecValue = (List<Map>) getListPropertyValue(PROP_DLI_DEFINITION);
 
 		// Not really used, but might be handy
 		Map<String, Object> resdefAndFields = getResDefandFieldsFromSourceView();
 		List<List<String>> fields = (List<List<String>>) resdefAndFields.get("fields");
 
 		List<String> fieldsSpecedAsDate = new ArrayList<String>();
 		// Look for fields speced as LDM_TYPE_DATE
 		for (Map<String, String> fieldSpecs : dliSpecValue) {
 			String specedFieldName = fieldSpecs.get(DLI_SPEC_KEY_FIELD_NAME);
 			String specedLdmType = fieldSpecs.get(DLI_SPEC_KEY_LDM_TYPE);
 			if (!specedLdmType.equals(SourceColumn.LDM_TYPE_DATE)) {
 				continue;
 			}
 			fieldsSpecedAsDate.add(specedFieldName);
 		}
 
 		return fieldsSpecedAsDate;
 	}
	
 	private void confirmationScreen() {
 		nextStep();
 		PropertyConstraint unmodifiable = new PropertyConstraint(Type.UNMODIFIABLE, true);
 		String propNameHack = "You are about to create DLI and model for your GoodData project. Click 'Next' to continue, or 'Back' to review your choices";
 		setPropertyDef(propNameHack, new SimpleProp(propNameHack, SimplePropType.SnapString, "", unmodifiable, false));
 		setPropertyValue(propNameHack, "OK");
 	}
 
 	private void getLabels(ComponentResourceErr err, List<String> fieldsToFormat) {
 		nextStep();
 		SimpleProp stringProp = new SimpleProp("Field", SimpleProp.SimplePropType.SnapString, "");
 		DictProp fieldDefProp = new DictProp("Field definition", stringProp, "", 2, 2, true, true);
 		fieldDefProp.put(DATE_FORMAT_KEY_FIELD_NAME, new SimpleProp("Label", SimpleProp.SimplePropType.SnapString,
 				"string1"));
 		List<String> possibleLabelTargets = getPossibleLabelTargets(err);
 		PropertyConstraint labelTargetConstraint = new PropertyConstraint(PropertyConstraint.Type.LOV,
 				possibleLabelTargets);
 		fieldDefProp.put(LABEL_KEY_PARENT, new SimpleProp("Reference", SimpleProp.SimplePropType.SnapString, "",
 				labelTargetConstraint, true));
 		ListProp labelSpec = new ListProp("Labels", fieldDefProp, "", fieldsToFormat.size(), fieldsToFormat.size(),
 				true);
 		setPropertyDef(PROP_LABEL_DEFINITION, labelSpec);
 		List<Map<String, String>> formatSpecValue = new ArrayList<Map<String, String>>();
 		for (String field : fieldsToFormat) {
 			Map<String, String> fieldSpec = new HashMap<String, String>();
 			fieldSpec.put(LABEL_KEY_FIELD_NAME, field);
 			fieldSpec.put(LABEL_KEY_PARENT, possibleLabelTargets.get(0));
 			formatSpecValue.add(fieldSpec);
 		}
 		setPropertyValue(PROP_LABEL_DEFINITION, formatSpecValue);
 	}
 
 	private List<String> getPossibleLabelTargets(ComponentResourceErr err) {
 		List<String> result = new ArrayList<String>();
 		List<Map> dliSpecValue = getListPropertyValue(PROP_DLI_DEFINITION);
 
 		for (Map fieldSpecs : dliSpecValue) {
 			String specedFieldName = (String) fieldSpecs.get(DATE_FORMAT_KEY_FIELD_NAME);
 			String specedLdmType = (String) fieldSpecs.get(DLI_SPEC_KEY_LDM_TYPE);
 			if (specedLdmType.equals(SourceColumn.LDM_TYPE_FACT)
 					|| specedLdmType.equals(SourceColumn.LDM_TYPE_REFERENCE)
 					|| specedLdmType.equals(SourceColumn.LDM_TYPE_LABEL)
 					|| specedLdmType.equals(SourceColumn.LDM_TYPE_DATE)
 					|| specedLdmType.equals(SourceColumn.LDM_TYPE_IGNORE))
 				continue;
 			result.add(specedFieldName);
 		}
 		return result;
 	}
 
 	private List<String> checkLabels(ComponentResourceErr err) throws NumericFieldsCannotBeSpecifiedAsDatesException {
 		List<String> result = new ArrayList<String>();
 		List<Map> dliSpecValue = getListPropertyValue(PROP_DLI_DEFINITION);
 
 		for (Map<String, String> fieldSpecs : dliSpecValue) {
 			String specedFieldName = (String) fieldSpecs.get("field_name");
 			String specedLdmType = (String) fieldSpecs.get("ldm_type");
 			if (!specedLdmType.equals(SourceColumn.LDM_TYPE_LABEL)) {
 				continue;
 			}
 			result.add(specedFieldName);
 		}
 		return result;
 	}
 
 	@Override
 	public void suggestResourceValues(ComponentResourceErr err) {
 		try {
 			int step = ((BigDecimal) getPropertyValue(PROP_WIZARD_STEP)).intValue();
 			switch (step) {
 			case 1:
 				String gdConnUri = getStringPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF);
 				if (gdConnUri.equals(CREATE_NEW_CONNECTION)) {
 					// Proceed to step 2: Ask for connection properties
 					gatherConnectionProps(err);
 					return;
 				}
 				// Else, fall through
 				nextStep();
 			case 2:
 				getProjects(err);
 				break;
 			case 3:
 				String projectName = getStringPropertyValue(GoodDataPut.PROP_PROJECT_NAME);
 				if (CREATE_NEW_PROJECT.equals(projectName)) {
 					// Need to display request for connection properties
 					gatherProjectProperties(err);
 					break;
 				} else {
 					nextStep();
 				}
 			case 4:
 				getViewCandidates(err);
 				break;
 			case 5:
 				getDLIConfig(err);
 				break;
 			case 6:
 				List<String> stringDateFields = checkDateFormats(err);
 				if (stringDateFields.isEmpty()) {
 					setEmptyHiddenProperty(PROP_DATE_FORMAT_DEFINITION);
 					nextStep();
 				} else {
 					getDateFormats(err, stringDateFields);
 					break;
 				}
 			case 7:
 				try {
 					List<String> stringLabelFields = checkLabels(err);
 					if (stringLabelFields.isEmpty()) {
 						setEmptyHiddenProperty(PROP_LABEL_DEFINITION);
 						nextStep();
 					} else {
 						getLabels(err, stringLabelFields);
 						break;
 					}
 				} catch (NumericFieldsCannotBeSpecifiedAsDatesException nfcbsade) {
 					return;
 				}
 			case 8:
 				List<String> stringReferenceFields = checkReferences(err);
 				if (stringReferenceFields.isEmpty()) {
 					setEmptyHiddenProperty(PROP_REFERENCE_DEFINITION);
 					nextStep();
 				} else {
 					getReferences(err, stringReferenceFields);
 					break;
 				}
 			case 9:
 				confirmationScreen();
 				return;
 			}
 		} catch (Exception e) {
 			err.setMessage("Error occurred: %s", e);
 			elog(e);
 		}
 	}
 
 	private void getReferences(ComponentResourceErr err, List<String> referenceFields) {
 		nextStep();
 		SimpleProp stringProp = new SimpleProp("Field", SimplePropType.SnapString, "");
 		DictProp fieldDefProp = new DictProp("Field definition", stringProp, "", 2, 2, true, true);
 		fieldDefProp.put(REFERENCE_KEY_FIELD_NAME, new SimpleProp("Field name", SimplePropType.SnapString, "string1"));
 		PropertyConstraint ldmTypeConstraint = new PropertyConstraint(Type.LOV, LDM_TYPES);
 		fieldDefProp.put(REFERENCE_KEY_TARGET, new SimpleProp("Target", SimplePropType.SnapString, "", null, true));
 		ListProp dateFormatSpec = new ListProp("References", fieldDefProp, "", referenceFields.size(), referenceFields
 				.size(), true);
 		setPropertyDef(PROP_REFERENCE_DEFINITION, dateFormatSpec);
 		List formatSpecValue = new ArrayList();
 		for (String field : referenceFields) {
 			Map fieldSpec = new HashMap();
 			fieldSpec.put(REFERENCE_KEY_FIELD_NAME, field);
 			fieldSpec.put(REFERENCE_KEY_TARGET, SAMPLE_REFERENCE);
 			formatSpecValue.add(fieldSpec);
 		}
 		setPropertyValue(PROP_REFERENCE_DEFINITION, formatSpecValue);
 	}
 
 	private List<String> checkReferences(ComponentResourceErr err) {
 		List<String> result = new ArrayList<String>();
 		List<Map> dliSpecValue = getListPropertyValue(PROP_DLI_DEFINITION);
 
 		for (Map<String, String> fieldSpecs : dliSpecValue) {
 			String specedFieldName = (String) fieldSpecs.get("field_name");
 			String specedLdmType = (String) fieldSpecs.get("ldm_type");
 			if (!specedLdmType.equals(SourceColumn.LDM_TYPE_REFERENCE)) {
 				continue;
 			}
 			result.add(specedFieldName);
 		}
 		return result;
 	}
 
 	private void gatherProjectProperties(ComponentResourceErr err) {
 		nextStep();
 		setPropertyDef(PROP_NEW_PROJECT_DESCR, new SimpleProp("New Project Description", SimplePropType.SnapString,
 				true));
 		setPropertyDef(PROP_NEW_PROJECT_NAME, new SimpleProp(" New Project Name", SimplePropType.SnapString, true));
 	}
 
 	@Override
 	public void execute(Map<String, InputView> inputViews, Map<String, OutputView> outputViews) {
 		try {
 			ExtendedSnapi snapi = ExtendedSnapi.getExtendedSnapi();
 			String gdUriPrefix = getStringPropertyValue(PROP_GD_URI_PREFIX);
 			String serverUri = ComponentContainer.getServerUri();
 			String gdConnUri = getStringPropertyValue(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF);
 
 			// Either create a new connection or reuse existing
 			ResDef connResDef;
 			if (gdConnUri.equals(CREATE_NEW_CONNECTION)) {
 				String connectionName = getStringPropertyValue(PROP_GD_CONN_NAME);
 				info("Creating new connection: " + connectionName);
 				gdConnUri = gdUriPrefix + "/" + connectionName;
 				connResDef = createConnectionObject(snapi, serverUri);
 				connResDef.save(gdConnUri);
 				info("Connection " + gdConnUri + " created");
 			} else {
 				gdConnUri = enrichWithServerUrl(gdConnUri);
 				connResDef = getResourceObject(gdConnUri, null);
 			}
 
 			// Login to GoodData
 			GdcRESTApiWrapper restApi;
 			try {
 				restApi = GoodDataConnection.login(connResDef, this);
 			} catch (GdcLoginException gdcle) {
 				elog(gdcle);
 				throw new SnapComponentException(gdcle);
 			}
 
 			// Either create a new project, or parse project URL to get it
 			String idNameCompound = getStringPropertyValue(GoodDataPut.PROP_PROJECT_NAME);
 			String projectName;
 			String projectId;
 			if (CREATE_NEW_PROJECT.equals(idNameCompound)) {
 				// Create a new project
 				String newProjectName = getStringPropertyValue(PROP_NEW_PROJECT_NAME);
 				String newProjectDescr = getStringPropertyValue(PROP_NEW_PROJECT_DESCR);
 				info("Creating new project in GoodData: " + newProjectName);
 				projectId = restApi.createProject(newProjectName, newProjectDescr, null);
 				info("Project " + newProjectName + " created, its ID is " + projectId);
 				// We will need this later
 				projectName = newProjectName;
 			} else {
 				// Get Project ID
 				projectId = parseProjectId(idNameCompound);
 				projectName = parseProjectName(idNameCompound);
 			}
 
 			String dliName = getStringPropertyValue(PROP_DLI_NAME);
 
 			List dliColumnTypes = getListPropertyValue(PROP_DLI_DEFINITION);
 			List dateFormatsList = getListPropertyValue(PROP_DATE_FORMAT_DEFINITION);
 			List referenceList = getListPropertyValue(PROP_REFERENCE_DEFINITION);
 			HashMap<String, String> dateFormats = propertySheetToHash(dateFormatsList, DATE_FORMAT_KEY_FIELD_NAME,
 					DATE_FORMAT_KEY_FORMAT);
 			HashMap<String, String> dateDimensions = propertySheetToHash(dateFormatsList, DATE_FORMAT_KEY_FIELD_NAME,
 					DATE_FORMAT_KEY_DIMENSION);
 			HashMap<String, String> ddNames = propertySheetToHash(dateFormatsList, DATE_FORMAT_KEY_FIELD_NAME,
 					DATE_FORMAT_REF_OR_NAME);
 			List labelFields = getListPropertyValue(PROP_LABEL_DEFINITION);
 			HashMap<String, String> labelParents = propertySheetToHash(labelFields, LABEL_KEY_FIELD_NAME,
 					LABEL_KEY_PARENT);
 			HashMap<String, String> references = propertySheetToHash(referenceList, REFERENCE_KEY_FIELD_NAME,
 					REFERENCE_KEY_TARGET);
 
 			// handle DD
 			if (dateDimensions != null) {
 				for (Map.Entry<String, String> dd : dateDimensions.entrySet()) {
 					if (dd.getValue().equalsIgnoreCase(DATE_FORMAT_CREATE_DD)) {
 						// Create new TD
 						String ddName = ddNames.get(dd.getKey());
 						info("Creating date dimension " + ddName);
 						DateDimensionConnector ddConnector = DateDimensionConnector.createConnector();
 						Command c = new Command("LoadDateDimension");
 						Properties prop = new Properties();
 						prop.put("name", ddName);
 						c.setParameters(prop);
 						ProcessingContext ctx = new ProcessingContext();
 						ctx.setProjectId(projectId);
 						ddConnector.processCommand(c, null, ctx);
 						String ddMaql = ddConnector.generateMaql();
 						restApi.executeMAQL(projectId, ddMaql);
 						info("Date dimension " + ddName + " created.");
 					} else if (dd.getValue().equalsIgnoreCase(DATE_FORMAT_CONNECT_DD)) {
 						// nothing to do at the moment
 						// dimensions are connected in schema creation part
 					}
 				}
 			}
 
 			SourceSchema ss = SourceSchema.createSchema(dliName);
 
 			for (Iterator localIterator1 = dliColumnTypes.iterator(); localIterator1.hasNext();) {
 				Object columnType = localIterator1.next();
 
 				HashMap entry = (HashMap) columnType;
 				String fieldName = (String) entry.get(DLI_SPEC_KEY_FIELD_NAME);
 				String ldmType = (String) entry.get(DLI_SPEC_KEY_LDM_TYPE);
 				SourceColumn column = new SourceColumn(fieldName, ldmType, fieldName);
				column.setFolder(dliName);
 
				if (ldmType.equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE)) {
 					// setup date dimension
					if (dateFormats != null && dateFormats.containsKey(fieldName)) {
						// this is a field which was not date originally, set its format string
						column.setFormat((String) dateFormats.get(fieldName));
					} else {
						//set default used by CSV Staging algorithm in GoodDataPut (yyyy-MM-dd)
						column.setFormat(ConvertUtils.DATE_FORMAT_3);
					}
 					column.setSchemaReference(ddNames.get(fieldName));
 				}
				
 				if (labelParents != null && labelParents.containsKey(fieldName)) {
 					column.setReference((String) labelParents.get(fieldName));
 				}
 
 				if (references != null && references.containsKey(fieldName)) {
 					String[] ref = references.get(fieldName).split(":");
 					if (ref.length != 2) {
 						elog(new Throwable("Error in reference format: " + references.get(fieldName)));
 						throw new SnapComponentException("Error in reference format: " + references.get(fieldName));
 					}
 					column.setSchemaReference(ref[0]);
 					column.setReference(ref[1]);
 				}
 
 				ss.addColumn(column);
 			}
 
 			try {
 				// PdmSchema is a representation of SourceSchema in the ConnectorBackend
 				PdmSchema pdm = PdmSchema.createSchema(ss);
 				// TODO Replace the Derby backend with stream backend
 				// Initialize embedded DB backend
 				DerbyConnectorBackend derbyConnectorBackend = DerbyConnectorBackend.create();
 				derbyConnectorBackend.setProjectId(projectId);
 				derbyConnectorBackend.setPdm(pdm);
 				// Setup CSV connector that will be used for reading the data
 				CsvConnector csvConnector = CsvConnector.createConnector(derbyConnectorBackend);
 				csvConnector.setSchema(ss);
 				csvConnector.initialize();
 				// Generate MAQL for Source Schema
 				String maql = csvConnector.generateMaql();
 				// Execute the MAQL (creates DLI)
 				info("Going to create a new DLI " + dliName + " in project " + projectName);
 				restApi.executeMAQL(projectId, maql);
 				info("DLI " + dliName + " created");
 				// We are done here, project is set up in GoodData
 			} catch (IOException e) {
 				elog(e);
 				throw new SnapComponentException(e);
 			}
 
 			info("Parsing output view of the source component...");
 			String gdInput = enrichWithServerUrl(getStringPropertyValue(PROP_GD_INPUT));
 			String[] uriAndView = gdInput.split("::");
 			String uri = uriAndView[0];
 			String outViewName = uriAndView[1];
 
 			Map<String, Object> resdefAndFields = getResDefandFieldsFromSourceView();
 			ResDef srcResDef = (ResDef) resdefAndFields.get(Keys.RESDEF);
 			info("Source component understood");
 
 			String componentName = getStringPropertyValue(PROP_GD_COMP_NAME);
 			String gdPutUri = gdUriPrefix + "/" + componentName;
 			info("Creating new GoodDataPutDelimited component '" + componentName + "'. Its URI will be " + gdPutUri);
 			ResDef putResDef = snapi.createResourceObject(serverUri, GoodDataPutDenormalized.class.getName(), null);
 			putResDef.setResourceRef(AbstractGoodDataComponent.GOODDATA_CONNECTION_REF, gdConnUri);
 			putResDef.setPropertyValue(GoodDataPutDenormalized.PROP_PROJECT_NAME, projectName);
 			putResDef.setPropertyValue(GoodDataPutDenormalized.PROP_PROJECT_ID, projectId);
 			putResDef.setPropertyValue(GoodDataPutDenormalized.PROP_DLI, dliName);
 			try {
 				// since the component will need the Source Schema, we need to serialize it into the string and save it
 				putResDef.setPropertyValue(GoodDataPutDenormalized.PROP_SOURCE_SCHEMA, ss.getConfig());
 			} catch (IOException e) {
 				elog(e);
 				throw new SnapComponentException(e);
 			}
 			info("Component '" + componentName + "' created and its basic properties were set.");
 
 			info("Transferring output view of the source component to component '" + componentName
 					+ "' (as Input View).");
 			// Let's remove possible existing views and insert the right one
 			Collection<String> inViews = putResDef.listInputViewNames();
 			String inViewName = "Input";
 			if (inViews.size() > 0) {
 				inViewName = inViews.iterator().next();
 				putResDef.removeInputView(inViewName);
 			}
 			List<Field> fields = new ArrayList<Field>();
 			List<List<String>> srcFields = (List<List<String>>) resdefAndFields.get("fields");
 			String[][] fieldLinks = new String[srcFields.size()][2];
 			int i = 0;
 			for (List<String> srcField : srcFields) {
 				String fieldName = srcField.get(0);
 				Field f = new Field(fieldName, SnapFieldType.map.get(srcField.get(1)), srcField.get(2));
 				fields.add(f);
 				List<String> link = new ArrayList<String>();
 				fieldLinks[i][0] = fieldName;
 				fieldLinks[i][1] = fieldName;
 				i++;
 			}
 			putResDef.addRecordInputView(inViewName, fields, "", true);
 			info("View duplicated");
 			putResDef.save(gdPutUri);
 			info("Component '" + componentName + "' saved to Snaplogic");
 
 		} catch (GdcRestApiException gde) {
 			elog(gde);
 			throw new SnapComponentException(gde);
 		}
 		// This part is pending the fix of
 		// https://www.snaplogic.org/trac/ticket/2571
 
 		// PipelineResDef pipe =
 		// (PipelineResDef)snapi.createResourceObject(serverUri,
 		// Keys.PIPELINE_COMPONENT_NAME, null);
 		// pipe.add(putResDef, "Target");
 		// pipe.add(srcResDef, "Source");
 		//        
 		// pipe.linkViews("Source", outViewName, "Target", inViewName,
 		// fieldLinks);
 		// pipe.save(gdUriPrefix + "/Pipeline");
 	}
 
 	private String parseProjectId(String idNameCompound) {
 		StringTokenizer t = new StringTokenizer(idNameCompound, " ");
 		String result = null;
 		while (t.hasMoreElements()) {
 			result = t.nextToken();
 		}
 		return result;
 	}
 
 	private String parseProjectName(String idNameCompound) {
 		StringTokenizer t = new StringTokenizer(idNameCompound, " ");
 		String result = "";
 		int tokenCount = t.countTokens();
 		for (int i = 0; i < tokenCount - 2; i++) {
			if (i > 0)
				result += " ";
 			result += t.nextToken();
 		}
 		return result;
 	}
 
 	private ResDef createConnectionObject(ExtendedSnapi snapi, String serverUri) {
 		ResDef connResDef = snapi.createResourceObject(serverUri, GoodDataConnection.class.getName(), null);
 		String[] connProps = { GoodDataConnection.PROP_HOSTNAME, GoodDataConnection.PROP_HOSTNAME_FTP,
 				GoodDataConnection.PROP_PASSWORD, GoodDataConnection.PROP_USERNAME, GoodDataConnection.PROP_PROTOCOL };
 		for (String connProp : connProps) {
 			String val = getStringPropertyValue(connProp);
 			connResDef.setPropertyValue(connProp, val);
 		}
 		return connResDef;
 	}
 
 	private HashMap<String, String> propertySheetToHash(List<Object> list, String key_key, String value_key) {
 		if (list == null)
 			return null;
 		HashMap<String, String> result = new HashMap<String, String>();
 		for (Iterator localIterator = list.iterator(); localIterator.hasNext();) {
 			Object item = localIterator.next();
 			HashMap entry = (HashMap) item;
 			String key = (String) entry.get(key_key);
 			String value = (String) entry.get(value_key);
 			result.put(key, value);
 		}
 
 		return result;
 	}
 }
