 package org.xbrlapi.utilities;
 
 import java.net.URI;
 
 import org.apache.log4j.Logger;
 
 /**
  * Defines a range of constants (namespaces etc) that are used throughout the
  * XBRLAPI implementation
  * 
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class Constants {
 
     public final static String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
     public final static String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
     
     public final static String XMLPrefix = "xml";
     public final static String XLinkPrefix = "xlink";
     public final static String XBRL21Prefix = "xbrli";
     public final static String GenericLinkPrefix = "gen";
     public final static String XBRL21LinkPrefix = "link";
     public final static String GenericLabelPrefix = "genlab";
     public final static String GenericReferencePrefix = "genref";
     public final static String XBRLAPIPrefix = "xbrlapi";
     public final static String CompPrefix = "comp";
     public final static String XBRLAPIEntitiesPrefix = "entity";
     public final static String XBRLAPILanguagesPrefix = "lang";
     public final static String XMLSchemaPrefix = "xsd";
     public final static String XMLSchemaInstancePrefix = "xsi";
 
     public final static String FragmentRootElementName = "fragment";
     public final static String FragmentDataContainerElementName = "data";
 
     protected static Logger logger = Logger.getLogger(Constants.class);
 
     public final static URI XMLNamespace = URI.create("http://www.w3.org/XML/1998/namespace");
     public final static URI XMLNSNamespace = URI.create("http://www.w3.org/2000/xmlns/");
     public final static URI XLinkNamespace = URI.create("http://www.w3.org/1999/xlink");
     public final static URI XBRL21Namespace = URI.create("http://www.xbrl.org/2003/instance");
     public final static URI XBRL21LinkNamespace = URI.create("http://www.xbrl.org/2003/linkbase");
     public final static URI GenericLinkNamespace = URI.create("http://xbrl.org/2008/generic");
     public final static URI GenericLabelNamespace = URI.create("http://xbrl.org/2008/label");
     public final static URI GenericReferenceNamespace = URI.create("http://xbrl.org/2008/reference");
     public final static URI XBRLAPINamespace = URI.create("http://xbrlapi.org/");
     public final static URI CompNamespace = URI.create("http://xbrlapi.org/composite");
     public final static URI XBRLAPIEntitiesNamespace = URI.create("http://xbrlapi.org/entities");
     public final static URI XBRLAPIEquivalentEntitiesArcrole = URI.create("http://xbrlapi.org/arcrole/equivalent-entity");
     public final static URI XBRLAPILanguagesNamespace = URI.create("http://xbrlapi.org/rfc1766/languages");    
     public final static URI XMLSchemaNamespace = URI.create("http://www.w3.org/2001/XMLSchema");
     public final static URI XMLSchemaInstanceNamespace = URI.create("http://www.w3.org/2001/XMLSchema-instance");
     public final static URI LabelArcrole = URI.create("http://www.xbrl.org/2003/arcrole/concept-label");
     public final static URI GenericLabelArcrole = URI.create("http://xbrl.org/arcrole/2008/element-label");
     public final static URI ReferenceArcrole = URI.create("http://www.xbrl.org/2003/arcrole/concept-reference");
     public final static URI GenericReferenceArcrole = URI.create("http://xbrl.org/arcrole/2008/element-reference");
     public final static URI CalculationArcrole = URI.create("http://www.xbrl.org/2003/arcrole/summation-item");
     public final static URI PresentationArcrole = URI.create("http://www.xbrl.org/2003/arcrole/parent-child");
     public final static URI GeneralSpecialArcrole = URI.create("http://www.xbrl.org/2003/arcrole/general-special");
     public final static URI EssenceAliasArcrole = URI.create("http://www.xbrl.org/2003/arcrole/essence-alias");
     public final static URI SimilarTuplesArcrole = URI.create("http://www.xbrl.org/2003/arcrole/similar-tuples");
     public final static URI RequiresElementArcrole = URI.create("http://www.xbrl.org/2003/arcrole/requires-element");
     public final static URI FactFootnoteArcrole = URI.create("http://www.xbrl.org/2003/arcrole/fact-footnote");
     public final static URI StandardLabelRole = URI.create("http://www.xbrl.org/2003/role/label");
     public final static URI TerseLabelRole = URI.create("http://www.xbrl.org/2003/role/terseLabel");
     public final static URI VerboseLabelRole = URI.create("http://www.xbrl.org/2003/role/verboseLabel");
     public final static URI PositiveLabelRole = URI.create("http://www.xbrl.org/2003/role/positiveLabel");
     public final static URI PositiveTerseLabelRole = URI.create("http://www.xbrl.org/2003/role/positiveTerseLabel");
     public final static URI PositiveVerboseLabelRole = URI.create("http://www.xbrl.org/2003/role/positiveVerboseLabel");
     public final static URI NegativeLabelRole = URI.create("http://www.xbrl.org/2003/role/negativeLabel");
     public final static URI NegativeTerseLabelRole = URI.create("http://www.xbrl.org/2003/role/negativeTerseLabel");
     public final static URI NegativeVerboseLabelRole = URI.create("http://www.xbrl.org/2003/role/negativeVerboseLabel");
     public final static URI ZeroLabelRole = URI.create("http://www.xbrl.org/2003/role/zeroLabel");
     public final static URI ZeroTerseLabelRole = URI.create("http://www.xbrl.org/2003/role/zeroTerseLabel");
     public final static URI ZeroVerboseLabelRole = URI.create("http://www.xbrl.org/2003/role/zeroVerboseLabel");
     public final static URI TotalLabelRole = URI.create("http://www.xbrl.org/2003/role/totalLabel");
     public final static URI PeriodStartLabelRole = URI.create("http://www.xbrl.org/2003/role/periodStartLabel");
     public final static URI PeriodEndLabelRole = URI.create("http://www.xbrl.org/2003/role/periodEndLabel");
     public final static URI DocumentationLabelRole = URI.create("http://www.xbrl.org/2003/role/documentationLabel");
     public final static URI DefinitionGuidanceLabelRole = URI.create("http://www.xbrl.org/2003/role/definitionGuidanceLabel");
     public final static URI DisclosureGuidanceLabelRole = URI.create("http://www.xbrl.org/2003/role/disclosureGuidanceLabel");
     public final static URI PresentationGuidanceLabelRole = URI.create("http://www.xbrl.org/2003/role/presentationGuidanceLabel");
     public final static URI MeasurementGuidanceLabelRole = URI.create("http://www.xbrl.org/2003/role/measurementGuidanceLabel");
     public final static URI CommentaryGuidanceLabelRole = URI.create("http://www.xbrl.org/2003/role/commentaryGuidanceLabel");
     public final static URI ExampleGuidanceLabelRole = URI.create("http://www.xbrl.org/2003/role/exampleGuidanceLabel");
     public final static URI StandardReferenceRole = URI.create("http://www.xbrl.org/2003/role/reference");
     public final static URI DefinitionReferenceRole = URI.create("http://www.xbrl.org/2003/role/definitionRef");
     public final static URI DisclosureReferenceRole = URI.create("http://www.xbrl.org/2003/role/disclosureRef");
     public final static URI MandatoryDisclosureReferenceRole = URI.create("http://www.xbrl.org/2003/role/mandatoryDisclosureRef");
     public final static URI RecommendedDisclosureReferenceRole = URI.create("http://www.xbrl.org/2003/role/recommendedDisclosureRef");
     public final static URI UnspecifiedDisclosureReferenceRole = URI.create("http://www.xbrl.org/2003/role/unspecifiedDisclosureGuidanceLabel");
     public final static URI PresentationReferenceRole = URI.create("http://www.xbrl.org/2003/role/presentationRef");
     public final static URI MeasurementReferenceRole = URI.create("http://www.xbrl.org/2003/role/measurementRef");
     public final static URI CommentaryReferenceRole = URI.create("http://www.xbrl.org/2003/role/commentaryRef");
     public final static URI ExampleReferenceRole = URI.create("http://www.xbrl.org/2003/role/exampleRef");
     public final static URI StandardLinkRole = URI.create("http://www.xbrl.org/2003/role/link");
     public final static URI ISO4217 = URI.create("http://www.xbrl.org/2003/iso4217");
     
     public final static URI StandardGenericLabelRole = URI.create("http://www.xbrl.org/2008/role/label");
     public final static URI TerseGenericLabelRole = URI.create("http://www.xbrl.org/2008/role/terseLabel");
     public final static URI VerboseGenericLabelRole = URI.create("http://www.xbrl.org/2008/role/verboseLabel");
     public final static URI DocumentationGenericLabelRole = URI.create("http://www.xbrl.org/2008/role/documentation");
     
     public final static URI StandardGenericReferenceRole = URI.create("http://www.xbrl.org/2008/role/reference");
     public final static URI LinkbaseReferenceArcrole = URI.create("http://www.w3.org/1999/xlink/properties/linkbase");
 
 
 }
