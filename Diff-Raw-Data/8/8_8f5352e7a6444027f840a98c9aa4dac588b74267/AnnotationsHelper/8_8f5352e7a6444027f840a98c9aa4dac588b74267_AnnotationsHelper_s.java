 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.commons;
 
 import java.util.List;
 
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.Element;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationInfo;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationTag;
 import org.ebayopensource.turmeric.tools.annoparser.utils.Utils;
 
 /**
  * The Class AnnotationsHelper.
  *
  * @author sdaripelli
  */
 public class AnnotationsHelper {
 	
 	/**
 	 * Gets the annotation tag.
 	 *
 	 * @param annInfo the ann info
 	 * @param tagName the tag name
 	 * @return the annotation tag
 	 */
 	public static List<ParsedAnnotationTag> getAnnotationTag(ParsedAnnotationInfo annInfo, String tagName){
 		List<ParsedAnnotationTag> callInfo=null;
 		if(annInfo !=null && annInfo.getValue()!=null && 
 				annInfo.getValue().get(tagName)!=null && annInfo.getValue().get(tagName).size()>0){
 			callInfo=annInfo.getValue().get(tagName);
 		}
 		return callInfo;
 	}
 	
 	/**
 	 * Gets the first annotation value.
 	 *
 	 * @param element the element
 	 * @param tagname the tagname
 	 * @return the first annotation value
 	 */
 	public static  String getFirstAnnotationValue(Element element,String tagname){
 		if(element!=null){
 			List<ParsedAnnotationTag> tag=getAnnotationTag(element.getAnnotationInfo(), tagname);
 			if(tag!=null && tag.size()>0){
 				return tag.get(0).getTagValue();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the first annotation value.
 	 *
 	 * @param anno the anno
 	 * @param tagname the tagname
 	 * @return the first annotation value
 	 */
 	public static  String getFirstAnnotationValue(ParsedAnnotationInfo anno,String tagname){
 		if(anno!=null){
 			List<ParsedAnnotationTag> tag=getAnnotationTag(anno, tagname);
 			if(tag!=null && tag.size()>0){
 				return tag.get(0).getTagValue();
 			}
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * Gets the first call info tag value.
 	 *
 	 * @param callInfo the call info
 	 * @param tagName the tag name
 	 * @return the first call info tag value
 	 */
 	public static String getFirstCallInfoTagValue(ParsedAnnotationTag callInfo,String tagName){
 		String ret=null;
 		if(callInfo!=null&& callInfo.getChildren()!=null && callInfo.getChildren().get(tagName)!=null){ 
 				if(callInfo.getChildren().get(tagName).size()>0){
 					ret=callInfo.getChildren().get(tagName).get(0).getTagValue();
 				}
 		}
 		return ret;
 	}
 	
 	/**
 	 * Gets the call info children.
 	 *
 	 * @param callInfo the call info
 	 * @param tagName the tag name
 	 * @return the call info children
 	 */
 	public static List<ParsedAnnotationTag> getCallInfoChildren(ParsedAnnotationTag callInfo,String tagName){
 		List<ParsedAnnotationTag> retList=null;
 		if(callInfo!=null && callInfo.getChildren()!=null){
 			retList=callInfo.getChildren().get(tagName);
 		}
 		return retList;
 	}
 	
 	
 		
 	/**
 	 * Gets the call info.
 	 *
 	 * @param comp the comp
 	 * @param operation the operation
 	 * @param isInput the is input
 	 * @return the call info
 	 */
 	public static List<ParsedAnnotationTag> getCallInfo(ParsedAnnotationInfo annInfo){
 		return getAnnotationTag(annInfo,"CallInfo");
 		
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Gets the actual occurance.
 	 *
 	 * @param comp the comp
 	 * @param isInput the is input
 	 * @param operationName the operation name
 	 * @return the actual occurance
 	 */
 	public static String getActualOccurance(ParsedAnnotationTag callInfo,
 			boolean isInput, String operationName) {
 		String occurrence=null;
 		if(callInfo!=null && callInfo.getChildren()!=null){
 			if(isInput && callInfo.getChildren().get("RequiredInput")!=null && callInfo.getChildren().get("RequiredInput").size()>0){
 				ParsedAnnotationTag reqInp=callInfo.getChildren().get("RequiredInput").get(0);
 				String reqInputValue=reqInp.getTagValue().trim();
 				if("Yes".equalsIgnoreCase(reqInputValue)){					
 					occurrence= "Required";
 				}else if("No".equalsIgnoreCase(reqInputValue)){
 					occurrence= "Optional";
 				}else if("Conditionally".equalsIgnoreCase(reqInputValue)){
 					occurrence= "Conditional";
 				}
 				
 			}else if(!isInput && callInfo.getChildren().get("Returned")!=null && callInfo.getChildren().get("Returned").size()>0){
 				ParsedAnnotationTag ret=callInfo.getChildren().get("Returned").get(0);
 				String retValue=ret.getTagValue().trim();
 				if("Always".equalsIgnoreCase(retValue)){
 					occurrence= "Always";
 				}else if("Conditionally".equalsIgnoreCase(retValue)){
 					occurrence= "Conditionally";
 				}
 			}
 		}
 		return occurrence;
 	}
 	
 	/**
 	 * Gets the single anno child val.
 	 *
 	 * @param tag the tag
 	 * @param tagName the tag name
 	 * @return the single anno child val
 	 */
 	public static String getSingleAnnoChildVal(ParsedAnnotationTag tag,String tagName){
 		String retVal=null;
 		if(tag!=null && tag.getChildren()!=null && tag.getChildren().get(tagName)!=null
 				&& tag.getChildren().get(tagName).size()>0){
 			ParsedAnnotationTag val=tag.getChildren().get(tagName).get(0);
 			if(val!=null){
 				retVal=val.getTagValue();
 			}
 		}
 		return retVal;
 	}
 	
 	/**
 	 * Process seelink.
 	 *
 	 * @param seeLink the see link
 	 * @return the string
 	 */
 	public static String processSeelink(ParsedAnnotationTag seeLink){
 		String subtitle=getSingleAnnoChildVal(seeLink, "SubTitle");
 		String result = "";
 		if (subtitle != null) {
 			result = "\"" + subtitle + "\" section of ";
 		}
 		String url=getSingleAnnoChildVal(seeLink, "URL");
 		String title=getSingleAnnoChildVal(seeLink, "Title");
 		result += "<a href=\"" + url + "\">" + title + "</a>";
 		String seeFor=getSingleAnnoChildVal(seeLink, "For");
 		if (seeFor != null) {
 			result += " for " + seeFor;
 		}
 
 		return result;
 	}
 	
 	
 	public static StringBuffer processDeprication(ParsedAnnotationInfo annInfo){
 		String deprVersion=getFirstAnnotationValue(annInfo, "DeprecationVersion");
 		String deprDetails=getFirstAnnotationValue(annInfo, "DeprecationDetails");
 		String useInstead=getFirstAnnotationValue(annInfo, "UseInstead");
 		
 		StringBuffer result = null;
 		if (!Utils.isEmpty(deprVersion)) {
 			result=new StringBuffer();
 			result.append("Deprecated Version: " + deprVersion+Constants.HTML_BR);
			result.append("Deprecated Details: " + deprDetails+Constants.HTML_BR);
			result.append("Use Instead: " + useInstead+Constants.HTML_BR);
 		}
 		return result;
 	}
 
 }
