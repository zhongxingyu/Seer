 package gov.nih.nci.ncicb.cadsr.common;
 
 import gov.nih.nci.ncicb.cadsr.common.resource.DataElement;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.io.StringWriter;
 
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.ValidationException;
 
 import javax.xml.transform.*;
 import javax.xml.transform.stream.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 
 
 
 
 import gov.nih.nci.ncicb.cadsr.common.util.logging.Log;
 import gov.nih.nci.ncicb.cadsr.common.util.logging.LogFactory;
 import net.sf.saxon.TransformerFactoryImpl;
 
 public class CDEXMLConverter
 {
 	private static Log log = LogFactory.getLog(CDEXMLConverter.class.getName());
 
 	static private CDEXMLConverter _instance = null;
 	public static final String CDEFormatStyleSheet = "ConvertCDE.xslt";
 	protected Transformer cdeTransformer = null;
 
 	public String convertFormToXML (List<DataElement> listDE) throws MarshalException, ValidationException, TransformerException
 	{
 		StringWriter writer = new StringWriter();
 		try
 		{
 			Marshaller.marshal(listDE, writer);
 		}
 		catch (MarshalException ex)
 		{
 			log.debug("CDEV2 " + listDE);
 			throw ex;
 		} catch (ValidationException ex) {
 			// need exception handling
 			log.debug("CDEV2 " + listDE);
 			throw ex;
 		}
 
 		// Now use our transformer to create V2 format
 		Source xmlInput = new StreamSource(new StringReader(writer.toString()));
 		//Source xmlInput = new StreamSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?><BC4JDataElementTransferObject public-id=\"2183222\" is-published=\"false\"><value-domain xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" CDPublicId=\"0\" public-id=\"0\" is-published=\"false\" xsi:type=\"java:gov.nih.nci.ncicb.cadsr.common.dto.ValueDomainTransferObject\"><vd-idseq>D8307BCB-408D-2325-E034-0003BA12F5E7</vd-idseq></value-domain><long-cDEName>Alpha DVG Blood Pressure, Diastolic</long-cDEName><using-contexts></using-contexts><context-name>DCP</context-name><de-idseq>FAA03445-BB9E-0301-E034-0003BA3F9857</de-idseq><CDEId>2183222</CDEId><data-element-concept xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" CDPublicId=\"0\" public-id=\"0\" is-published=\"false\" xsi:type=\"java:gov.nih.nci.ncicb.cadsr.common.dto.DataElementConceptTransferObject\"><dec-idseq>D536CC36-DCAD-2CF4-E034-0003BA12F5E7</dec-idseq><idseq>D536CC36-DCAD-2CF4-E034-0003BA12F5E7</idseq></data-element-concept><long-name>Alpha DVG Blood Pressure, Diastolic</long-name><context xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject\"><conte-idseq>A932C6E7-82EE-67C2-E034-0003BA12F5E7</conte-idseq></context><version>2.0</version><idseq>FAA03445-BB9E-0301-E034-0003BA3F9857</idseq><registration-status>Qualified</registration-status><preferred-name>ALPHA_DVG_BPD</preferred-name><asl-name>RELEASED</asl-name><preferred-definition>Oracle\"s Discrete Value Group (DVG) for a diastolic blood pressure that may not be obtained.</preferred-definition></BC4JDataElementTransferObject>"));
 
 		ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();
 		Result xmlOutput = new StreamResult(xmlOutputStream);
 		try {
 			cdeTransformer.transform(xmlInput, xmlOutput);
 		} catch (TransformerException e) {
 			log.debug(writer.toString());
 			throw e;
 		}
 
 		String V2XML = xmlOutputStream.toString();
 		System.out.println("X2XML::");
 		return V2XML;
 		//return writer.toString();
 	}
 	
 	public String convertOriginalXMLToXML (String originalXML) throws MarshalException, ValidationException, TransformerException
 	{
 		// Now use our transformer to create V2 format
 		Source xmlInput = new StreamSource(new StringReader(originalXML.toString()));
 		//Source xmlInput = new StreamSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?><BC4JDataElementTransferObject public-id=\"2183222\" is-published=\"false\"><value-domain xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" CDPublicId=\"0\" public-id=\"0\" is-published=\"false\" xsi:type=\"java:gov.nih.nci.ncicb.cadsr.common.dto.ValueDomainTransferObject\"><vd-idseq>D8307BCB-408D-2325-E034-0003BA12F5E7</vd-idseq></value-domain><long-cDEName>Alpha DVG Blood Pressure, Diastolic</long-cDEName><using-contexts></using-contexts><context-name>DCP</context-name><de-idseq>FAA03445-BB9E-0301-E034-0003BA3F9857</de-idseq><CDEId>2183222</CDEId><data-element-concept xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" CDPublicId=\"0\" public-id=\"0\" is-published=\"false\" xsi:type=\"java:gov.nih.nci.ncicb.cadsr.common.dto.DataElementConceptTransferObject\"><dec-idseq>D536CC36-DCAD-2CF4-E034-0003BA12F5E7</dec-idseq><idseq>D536CC36-DCAD-2CF4-E034-0003BA12F5E7</idseq></data-element-concept><long-name>Alpha DVG Blood Pressure, Diastolic</long-name><context xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject\"><conte-idseq>A932C6E7-82EE-67C2-E034-0003BA12F5E7</conte-idseq></context><version>2.0</version><idseq>FAA03445-BB9E-0301-E034-0003BA3F9857</idseq><registration-status>Qualified</registration-status><preferred-name>ALPHA_DVG_BPD</preferred-name><asl-name>RELEASED</asl-name><preferred-definition>Oracle\"s Discrete Value Group (DVG) for a diastolic blood pressure that may not be obtained.</preferred-definition></BC4JDataElementTransferObject>"));
 
 		ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();
 		Result xmlOutput = new StreamResult(xmlOutputStream);
 		try {
 			cdeTransformer.transform(xmlInput, xmlOutput);
 		} catch (TransformerException e) {
 			log.debug(originalXML.toString());
 			throw e;
 		}
 
 		String V2XML = xmlOutputStream.toString();
 		System.out.println("X2XML::");
 		return V2XML;
 		//return writer.toString();
 	}
 
 	protected CDEXMLConverter()
 	{
 		StreamSource xslSource = null;
 		try
 		{
 			InputStream xslStream = this.getClass().getResourceAsStream(CDEFormatStyleSheet);
 			xslSource = new StreamSource(xslStream);
 		}
 		catch(Exception e) {
 			System.out.println("CDEXMLConverter error loading conversion xsl: " + CDEFormatStyleSheet + " exc: "+ e);
 		}
 		try
 		{
 			log.debug("creating transformerV1ToV2");
 			cdeTransformer = net.sf.saxon.TransformerFactoryImpl.newInstance().newTransformer(xslSource);
 		} catch (TransformerException e) {
 			log.debug("transformerV1ToV2 exception: " + e.toString());
 			log.debug("transformerV1ToV2 exception: " + e.getMessage());
 		}
 	}
 
 	static public CDEXMLConverter instance(){
 		if (_instance == null) {
 			_instance = new CDEXMLConverter();
 		}
 		return _instance;
 	}
 
 	public static void main(String args[])
 	{
 		List<DataElement> de = null;
		String originalXML = "<?xml version='1.0' encoding='UTF-8'?><array-list><BC4JDataElementTransferObject xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' public-id='1234' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.bc4j.BC4JDataElementTransferObject'><value-domain CDPublicId='0' public-id='3506068' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ValueDomainTransferObject'><max-length>3</max-length><datatype>CHARACTER</datatype><representation public-id='2421125' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.RepresentationTransferObject'><concept-derivation-rule xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ConceptDerivationRuleTransferObject'><idseq>F37D0428-DA3E-6787-E034-0003BA3F9857</idseq><methods></methods><name>C38147</name><component-concepts display-order='0' is-primary='true' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ComponentConceptTransferObject'><idseq>F37D0428-DA3F-6787-E034-0003BA3F9857</idseq><concept public-id='2204645' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ConceptTransferObject'><definition-source>NCI</definition-source><code>C38147</code><evs-source>NCI_CONCEPT_CODE</evs-source><long-name>Yes or No Response</long-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject'><conte-idseq>D9344734-8CAF-4378-E034-0003BA12F5E7</conte-idseq><name>NCIP</name></context><conte-idseq>D9344734-8CAF-4378-E034-0003BA12F5E7</conte-idseq><version>1.0</version><idseq>F37D0428-DA3C-6787-E034-0003BA3F9857</idseq><latest-version-ind>Yes</latest-version-ind><preferred-name>C38147</preferred-name><asl-name>RELEASED</asl-name><origin>NCI Thesaurus</origin><preferred-definition>A response or indicator that can have a value of either yes or no.</preferred-definition></concept></component-concepts><type>Simple Concept</type><rule></rule><concatenation-char></concatenation-char></concept-derivation-rule><long-name>Ind-2</long-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.BC4JContextTransferObjectCDERest'><version>1.0</version><name>NCIP</name></context><version>1.0</version><preferred-name>C38147</preferred-name></representation><vd-idseq>C37A1BEB-75AE-B6C9-E040-BB89AD434179</vd-idseq><valid-values xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ValidValueTransferObject'><short-meaning-value>No</short-meaning-value><short-meaning>No</short-meaning><value-meaning public-id='3197154' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ValueMeaningTransferObject'><long-name>No</long-name><version>1.0</version><idseq>9D865DE5-55AD-1AC9-E040-BB89AD436B4F</idseq><preferred-definition>The non-affirmative response to a question.</preferred-definition></value-meaning><vp-idseq>C37A1BEB-75C2-B6C9-E040-BB89AD434179</vp-idseq></valid-values><valid-values xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ValidValueTransferObject'><short-meaning-value>Yes</short-meaning-value><short-meaning>Yes</short-meaning><value-meaning public-id='3197153' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ValueMeaningTransferObject'><long-name>Yes</long-name><version>1.0</version><idseq>9D865DE5-558A-1AC9-E040-BB89AD436B4F</idseq><preferred-definition>The affirmative response to a question or activity.</preferred-definition></value-meaning><vp-idseq>C37A1BEB-75CC-B6C9-E040-BB89AD434179</vp-idseq></valid-values><min-length>2</min-length><long-name>Yes No Indicator</long-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject'><conte-idseq>99BA9DC8-2095-4E69-E034-080020C9C0E0</conte-idseq><name>CTEP</name><description>NCI Cancer Therapy Evaluation Program</description></context><conte-idseq>99BA9DC8-2095-4E69-E034-080020C9C0E0</conte-idseq><version>1.0</version><asl-name>RELEASED</asl-name></value-domain><classifications xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.bc4j.BC4JClassificationsTransferObject'><class-scheme-name>CATEGORY</class-scheme-name><class-scheme-long-name>Type of Category</class-scheme-long-name><cs-version>2.0</cs-version><csi-idseq>CC69E6B4-3586-10FF-E034-0003BA12F5E7</csi-idseq><class-scheme-item-version>1.0</class-scheme-item-version><cs-idseq>99BA9DC8-84A3-4E69-E034-080020C9C0E0</cs-idseq><class-scheme-item-type>CATEGORY_TYPE</class-scheme-item-type><class-scheme-definition>Type of Category</class-scheme-definition><de-idseq>D7E08B29-E84C-0B30-E034-0003BA12F5E7</de-idseq><class-scheme-item-id>2811953</class-scheme-item-id><class-scheme-item-name>Disease Description</class-scheme-item-name></classifications><classifications xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.bc4j.BC4JClassificationsTransferObject'><class-scheme-name>All Candidates</class-scheme-name><class-scheme-long-name>All Candidates</class-scheme-long-name><cs-version>1.0</cs-version><csi-idseq>A0F7D34A-88C8-B24E-E040-BB89AD435F91</csi-idseq><class-scheme-item-version>1.0</class-scheme-item-version><cs-idseq>9908CAE4-B4C1-B44B-E040-BB89AD434D5C</cs-idseq><class-scheme-item-type>GROUP_TYPE</class-scheme-item-type><class-scheme-definition>All Candidate CDEs for migration</class-scheme-definition><de-idseq>D7E08B29-E84C-0B30-E034-0003BA12F5E7</de-idseq><class-scheme-item-id>3227970</class-scheme-item-id><class-scheme-item-name>CTEP</class-scheme-item-name></classifications><classifications xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.bc4j.BC4JClassificationsTransferObject'><class-scheme-name>DISEASE</class-scheme-name><class-scheme-long-name>Type of Disease</class-scheme-long-name><cs-version>2.0</cs-version><csi-idseq>BEC03024-E6DC-740C-E034-0003BA12F5E7</csi-idseq><class-scheme-item-version>1.0</class-scheme-item-version><cs-idseq>99BA9DC8-84A1-4E69-E034-080020C9C0E0</cs-idseq><class-scheme-item-type>DISEASE_TYPE</class-scheme-item-type><class-scheme-definition>Type of Disease</class-scheme-definition><de-idseq>D7E08B29-E84C-0B30-E034-0003BA12F5E7</de-idseq><class-scheme-item-id>2811883</class-scheme-item-id><class-scheme-item-name>Sarcoma</class-scheme-item-name></classifications><long-cDEName>Does the patient have bone pain</long-cDEName><using-contexts></using-contexts><vd-idseq>C37A1BEB-75AE-B6C9-E040-BB89AD434179</vd-idseq><context-name>CTEP</context-name><de-idseq>D7E08B29-E84C-0B30-E034-0003BA12F5E7</de-idseq><CDEId>1234</CDEId><data-element-concept CDPublicId='2008551' public-id='2008797' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.BC4JDataElementConceptTransferObjectCDERest'><cd-long-name>Assessments</cd-long-name><CDContextName>CTEP</CDContextName><property public-id='0' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.PropertyTransferObject'><concept-derivation-rule xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ConceptDerivationRuleTransferObject'><idseq>F37D0428-B602-6787-E034-0003BA3F9857</idseq><methods></methods><name>C3303</name><component-concepts display-order='0' is-primary='true' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ComponentConceptTransferObject'><idseq>F37D0428-B603-6787-E034-0003BA3F9857</idseq><concept public-id='2202326' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ConceptTransferObject'><definition-source>NCI</definition-source><code>C3303</code><evs-source>NCI_CONCEPT_CODE</evs-source><long-name>Pain</long-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject'><conte-idseq>D9344734-8CAF-4378-E034-0003BA12F5E7</conte-idseq><name>NCIP</name></context><conte-idseq>D9344734-8CAF-4378-E034-0003BA12F5E7</conte-idseq><version>1.0</version><idseq>F37D0428-B600-6787-E034-0003BA3F9857</idseq><latest-version-ind>Yes</latest-version-ind><preferred-name>C3303</preferred-name><asl-name>RELEASED</asl-name><origin>NCI Thesaurus</origin><preferred-definition>The sensation of discomfort, distress, or agony, resulting from the stimulation of specialized nerve endings.</preferred-definition></concept></component-concepts><type>Simple Concept</type><rule></rule><concatenation-char></concatenation-char></concept-derivation-rule><idseq>AE29A78C-5252-3A05-E034-0003BA12F5E7</idseq></property><object-class public-id='0' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ObjectClassTransferObject'><concept-derivation-rule xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ConceptDerivationRuleTransferObject'><idseq>F37D0428-BBA2-6787-E034-0003BA3F9857</idseq><methods></methods><name>C12366</name><component-concepts display-order='0' is-primary='true' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ComponentConceptTransferObject'><idseq>F37D0428-BBA3-6787-E034-0003BA3F9857</idseq><concept public-id='2202686' is-published='false' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ConceptTransferObject'><definition-source>NCI</definition-source><code>C12366</code><evs-source>NCI_CONCEPT_CODE</evs-source><long-name>Bone</long-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject'><conte-idseq>D9344734-8CAF-4378-E034-0003BA12F5E7</conte-idseq><name>NCIP</name></context><conte-idseq>D9344734-8CAF-4378-E034-0003BA12F5E7</conte-idseq><version>1.0</version><idseq>F37D0428-BBA0-6787-E034-0003BA3F9857</idseq><latest-version-ind>Yes</latest-version-ind><preferred-name>C12366</preferred-name><asl-name>RELEASED</asl-name><origin>NCI Thesaurus</origin><preferred-definition>Connective tissue that forms the skeletal components of the body.</preferred-definition></concept></component-concepts><type>Simple Concept</type><rule></rule><concatenation-char></concatenation-char></concept-derivation-rule><idseq>ACAAECCE-7A25-657B-E034-0003BA0B1A09</idseq></object-class><CDVersion>1.0</CDVersion><dec-idseq>99BA9DC8-4455-4E69-E034-080020C9C0E0</dec-idseq><CDPrefName>ASSESS</CDPrefName><long-name>Bone Pain</long-name><deleted-ind>No</deleted-ind><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.BC4JContextTransferObjectCDERest'><version>2.31</version><name>CTEP</name><description>NCI Cancer Therapy Evaluation Program</description></context><conte-idseq>99BA9DC8-2095-4E69-E034-080020C9C0E0</conte-idseq><version>2.31</version><date-modified>2013-02-25 14:55:38.0</date-modified><latest-version-ind>Yes</latest-version-ind><date-created>2002-02-13 00:00:00.0</date-created><preferred-name>BONE_PAIN</preferred-name><modified-by>SBR</modified-by><created-by>SBR</created-by><asl-name>RELEASED</asl-name><preferred-definition>information related to bone pain.</preferred-definition></data-element-concept><vd-name>Yes No Indicator</vd-name><dec-name>Bone Pain</dec-name><long-name>Bone Pain Ind-2</long-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.BC4JContextTransferObjectCDERest'><version>2.31</version><name>CTEP</name><description>NCI Cancer Therapy Evaluation Program</description></context><version>4.0</version><idseq>D7E08B29-E84C-0B30-E034-0003BA12F5E7</idseq><registration-status>Qualified</registration-status><referece-docs display-order='0' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ReferenceDocumentTransferObject'><doc-name>CRF Text</doc-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject'><conte-idseq>99BA9DC8-2095-4E69-E034-080020C9C0E0</conte-idseq><name>CTEP</name></context><doc-id-seq>D7E08B29-E852-0B30-E034-0003BA12F5E7</doc-id-seq><doc-iDSeq>D7E08B29-E852-0B30-E034-0003BA12F5E7</doc-iDSeq><doc-type>Alternate Question Text</doc-type><doc-text>Bone Pain</doc-text></referece-docs><referece-docs display-order='0' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ReferenceDocumentTransferObject'><doc-name>CRF Text2</doc-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject'><conte-idseq>99BA9DC8-2095-4E69-E034-080020C9C0E0</conte-idseq><name>CTEP</name></context><doc-id-seq>9531491D-EB9A-375D-E040-BB89AD437EC5</doc-id-seq><doc-iDSeq>9531491D-EB9A-375D-E040-BB89AD437EC5</doc-iDSeq><doc-type>Alternate Question Text</doc-type><doc-text>Does the patient have unexplained bone pain?</doc-text></referece-docs><referece-docs display-order='0' xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ReferenceDocumentTransferObject'><doc-name>Does the patient have bone pa</doc-name><context xsi:type='java:gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject'><conte-idseq>99BA9DC8-2095-4E69-E034-080020C9C0E0</conte-idseq><name>CTEP</name></context><doc-id-seq>D7E08B29-E853-0B30-E034-0003BA12F5E7</doc-id-seq><doc-iDSeq>D7E08B29-E853-0B30-E034-0003BA12F5E7</doc-iDSeq><doc-type>Preferred Question Text</doc-type><doc-text>Does the patient have bone pain</doc-text></referece-docs><preferred-name>BONE_PAIN_IND2</preferred-name><asl-name>RELEASED</asl-name><preferred-definition>the yes/no indicator that asks whether the patient is experiencing bone pain.</preferred-definition></BC4JDataElementTransferObject></array-list>";
 		try {
 			//System.out.println(CDEXMLConverter.instance().convertFormToXML(de));
			System.out.println(CDEXMLConverter.instance().convertOriginalXMLToXML(originalXML));
 		} catch (MarshalException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ValidationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
