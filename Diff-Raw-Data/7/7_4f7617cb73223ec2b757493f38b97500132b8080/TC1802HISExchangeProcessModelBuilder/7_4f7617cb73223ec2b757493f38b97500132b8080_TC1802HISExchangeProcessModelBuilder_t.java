 package org.eclipse.rmf.reqif10.tests.uc003.tc18xx;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.rmf.reqif10.AttributeDefinitionEnumeration;
import org.eclipse.rmf.reqif10.AttributeValueEnumeration;
 import org.eclipse.rmf.reqif10.AttributeValueString;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 
 @SuppressWarnings("nls")
 public class TC1802HISExchangeProcessModelBuilder {
 
 	final static String LAST_CHANGE_STRING_2 = "2012-04-09T01:51:37.112+02:00";
 	private ReqIF reqIF;
 
 	public TC1802HISExchangeProcessModelBuilder(ReqIF reqIF) throws Exception {
 		this.reqIF = reqIF;
 		modifyReqIF();
 	}
 
 	private void modifyReqIF() throws Exception {
 		// No action for 01 needed
 		// 02 does not exist any more
 		modifySpecObject03();
 		// No action for 04 needed
 		// No action for 05 needed
 		// No action for 06 needed
 		// No action for 07 needed
 		// No action for 08 needed
 		// No action for 09 needed
 	}
 
 	private void modifySpecObject03() throws Exception {
 		SpecObject specObject = reqIF.getCoreContent().getSpecObjects().get(1);
 		AttributeValueString value = (AttributeValueString) ReqIF10Util.getAttributeValueForLabel(specObject, "A1");
 		value.setTheValue("O3.A1 once");
 		specObject.setLastChange(toDate(LAST_CHANGE_STRING_2));

		AttributeDefinitionEnumeration enumeration = (AttributeDefinitionEnumeration) specObject.getType().getSpecAttributes().get(3);
		AttributeValueEnumeration valueEnum = (AttributeValueEnumeration) ReqIF10Util.getAttributeValueForLabel(specObject, "E1");
		valueEnum.getValues().set(0, enumeration.getType().getSpecifiedValues().get(1));
 	}
 
 	public ReqIF getReqIF() {
 		return reqIF;
 	}
 
 	public static XMLGregorianCalendar toDate(String date) throws DatatypeConfigurationException {
 		XMLGregorianCalendar xmlGregoriaCalendar = (XMLGregorianCalendar) EcoreUtil.createFromString(XMLTypePackage.eINSTANCE.getDateTime(), date);
 		return xmlGregoriaCalendar;
 	}
 
 }
