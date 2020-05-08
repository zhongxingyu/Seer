 /*
  * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
  * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
  * triple-shield Mayo logo are trademarks and service marks of MFMER.
  *
  * Except as contained in the copyright notice above, or as used to identify 
  * MFMER as the author of this software, the trade names, trademarks, service
  * marks, or product names of the copyright holder shall not be used in
  * advertising, promotion or otherwise in connection with this software without
  * prior written authorization of the copyright holder.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.mayo.cts2.framework.plugin.service.bioportal.transform;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xerces.util.XMLChar;
 
 import edu.mayo.cts2.framework.core.url.UrlConstructor;
 import edu.mayo.cts2.framework.model.core.CodeSystemReference;
 import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
 import edu.mayo.cts2.framework.model.core.DescriptionInCodeSystem;
 import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
 import edu.mayo.cts2.framework.model.core.ScopedEntityName;
 import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
 import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
 import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;
 
 /**
  * The Class AbstractTransform.
  *
  * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
  */
 public class AbstractTransform {
 	
 	protected Log log = LogFactory.getLog(getClass());
 	
 	protected static final String PREDICATE_URI_PREFIX = "http://purl.bioontology.org/predicate/";
 	
 	@Resource
 	private UrlConstructor urlConstructor;
 
 	@Resource
 	private BioportalRestService bioportalRestService;
 	
 	@Resource
 	private IdentityConverter identityConverter;
 
 	/**
 	 * Builds the scoped entity name.
 	 *
 	 * @param name the name
 	 * @param codeSystemName the code system name
 	 * @return the scoped entity name
 	 */
 	protected ScopedEntityName buildScopedEntityName(String name, String codeSystemName){
 		ScopedEntityName scopedName = new ScopedEntityName();
 
 		String[] namePartsColon = StringUtils.split(name, ':');
 		String[] namePartsHash = StringUtils.split(name, '#');
 
 		String[] nameParts;
 		if(namePartsColon.length > namePartsHash.length){
 			nameParts = namePartsColon;
 		} else {
 			nameParts = namePartsHash;
 		}
 
 		if(nameParts.length == 1){
 			scopedName.setName(nameParts[0]);
 			scopedName.setNamespace(codeSystemName);
 		} else {
 			boolean isNamespaceValidNCName = 
 					XMLChar.isValidNCName(nameParts[0]);
 			if(isNamespaceValidNCName){
 				scopedName.setNamespace(nameParts[0]);
 			} else {
 				scopedName.setNamespace(codeSystemName);
 			}
 			scopedName.setName(nameParts[1]);	
 		}
 
 		return this.sanitizeNcNameNamespace(scopedName);
 	}
 
 	private ScopedEntityName sanitizeNcNameNamespace(ScopedEntityName scopedName) {
 		if(! XMLChar.isValidNCName(scopedName.getNamespace())){
 			scopedName.setNamespace("ns" + Integer.toString(
 				scopedName.getNamespace().hashCode()));
 		}
 		
 		return scopedName;
 	}
 
 	/**
 	 * Builds the code system reference.
 	 *
 	 * @param codeSystemName the code system name
 	 * @return the code system reference
 	 */
 	protected CodeSystemReference buildCodeSystemReference(String codeSystemName){
 		CodeSystemReference codeSystemReference = new CodeSystemReference();
 		String codeSystemPath = this.getUrlConstructor().createCodeSystemUrl(codeSystemName);
 
 		codeSystemReference.setContent(codeSystemName);
 		codeSystemReference.setHref(codeSystemPath);
		try{
 		codeSystemReference.setUri(this.getIdentityConverter().getCodeSystemAbout(codeSystemName, BioportalConstants.DEFAULT_ONTOLOGY_ABOUT));
		} catch (Exception e){
			log.warn("Exception Getting CodeSystem URI: " + e.getMessage());
		}
 		
 		return codeSystemReference;
 	}
 	
 	/**
 	 * Builds the code system version reference.
 	 *
 	 * @param codeSystemName the code system name
 	 * @param codeSystemVersionName the code system version name
 	 * @return the code system version reference
 	 */
 	protected CodeSystemVersionReference buildCodeSystemVersionReference(String codeSystemName, String codeSystemVersionName){
 		CodeSystemVersionReference ref = new CodeSystemVersionReference();
 		
 		ref.setCodeSystem(this.buildCodeSystemReference(codeSystemName));
 		
 		NameAndMeaningReference version = new NameAndMeaningReference();
 		version.setContent(codeSystemVersionName);
 		String versionString= this.getIdentityConverter().codeSystemVersionNameToVersion(codeSystemVersionName);
 		version.setHref(this.getUrlConstructor().createCodeSystemVersionUrl(codeSystemName, versionString));
 			
 		ref.setVersion(version);
 		
 		return ref;
 	}
 	
 	/**
 	 * Gets the bioportal rest service.
 	 *
 	 * @return the bioportal rest service
 	 */
 	public BioportalRestService getBioportalRestService() {
 		return bioportalRestService;
 	}
 
 
 	/**
 	 * Sets the bioportal rest service.
 	 *
 	 * @param bioportalRestService the new bioportal rest service
 	 */
 	public void setBioportalRestService(BioportalRestService bioportalRestService) {
 		this.bioportalRestService = bioportalRestService;
 	}
 
 	/**
 	 * Sets the identity converter.
 	 *
 	 * @param identityConverter the new identity converter
 	 */
 	public void setIdentityConverter(IdentityConverter identityConverter) {
 		this.identityConverter = identityConverter;
 	}
 
 
 	/**
 	 * Gets the identity converter.
 	 *
 	 * @return the identity converter
 	 */
 	public IdentityConverter getIdentityConverter() {
 		return identityConverter;
 	}
 
 
 	/**
 	 * Gets the url constructor.
 	 *
 	 * @return the url constructor
 	 */
 	public UrlConstructor getUrlConstructor() {
 		return urlConstructor;
 	}
 
 
 	/**
 	 * Sets the url constructor.
 	 *
 	 * @param urlConstructor the new url constructor
 	 */
 	public void setUrlConstructor(UrlConstructor urlConstructor) {
 		this.urlConstructor = urlConstructor;
 	}
 	
 	/**
 	 * Creates the known entity description.
 	 *
 	 * @param codeSystemName the code system name
 	 * @param codeSystemVersionName the code system version name
 	 * @param label the label
 	 * @return the description in code system
 	 */
 	public DescriptionInCodeSystem createKnownEntityDescription(
 			String codeSystemName, 
 			String codeSystemVersionName,
 			String label){
 		DescriptionInCodeSystem description = new DescriptionInCodeSystem();
 		description.setDesignation(label);
 		
 		CodeSystemVersionReference versionRef = 
 			this.buildCodeSystemVersionReference(codeSystemName, codeSystemVersionName);
 
 		description.setDescribingCodeSystemVersion(versionRef);
 		
 		return description;
 	}
 }
