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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectoryEntry;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetHeader;
 import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
 
 /**
  * The Class CodeSystemVersionTransform.
  *
  * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
  */
 @Component
 public class ResolvedValueSetTransform extends AbstractOntologyTransform {
 
 	/* (non-Javadoc)
 	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#createNewResourceVersion()
 	 */
 	public List<ResolvedValueSetDirectoryEntry> transfrom(String xml) {
 		Document doc = BioportalRestUtils.getDocument(xml);
 
 		List<Node> nodeList = TransformUtils.getNodeListWithPath(doc, "success.data.list.ontologyBean");
 	
 		List<ResolvedValueSetDirectoryEntry> returnList = new ArrayList<ResolvedValueSetDirectoryEntry>();
 		
 		for(Node node : nodeList){
 			ResolvedValueSetDirectoryEntry entry = new ResolvedValueSetDirectoryEntry();
 
 			String ontologyVersionId = TransformUtils.getNamedChildText(node, ONTOLOGY_VERSION_ID);
 			
			ResolvedValueSetHeader header = this.getHeader(node);
 		
 			entry.setResolvedHeader(header);
 			
 			entry.setHref(this.getUrlConstructor().createResolvedValueSetUrl(
 					header.getResolutionOf().getValueSet().getContent(), 
 					header.getResolutionOf().getValueSetDefinition().getContent(), 
 					ontologyVersionId));
 			
 			entry.setResolvedValueSetURI(header.getResolutionOf().getValueSet().getUri() + "/resolution/" + ontologyVersionId);
 			
 			returnList.add(entry);
 		}
 		
 		return returnList;
 	}
 	
 	public ResolvedValueSetHeader getHeader(Node node){
 		String ontologyId = TransformUtils.getNamedChildText(node, ONTOLOGY_ID);
 		String ontologyVersionId = TransformUtils.getNamedChildText(node, ONTOLOGY_VERSION_ID);
 		
 		ResolvedValueSetHeader header = new ResolvedValueSetHeader();
 		header.setResolutionOf(this.getValueSetDefinitionReference(ontologyId, ontologyVersionId));
 		
 		List<Node> resolvedCodeSystemVersions = TransformUtils.getNodeListWithPath(node, "viewOnOntologyVersionId.int");
 		for(Node resolvedCodeSystemVersion : resolvedCodeSystemVersions){
 			String codeSystemVersionOntologyVersionId = 
 					TransformUtils.getNodeText(resolvedCodeSystemVersion);
 				
 			String codeSystemVersionName = 
 					this.getIdentityConverter().ontologyVersionIdToCodeSystemVersionName(codeSystemVersionOntologyVersionId);
 			
 			String codeSystemName = 
 					this.getIdentityConverter().codeSystemVersionNameCodeSystemName(codeSystemVersionName);
 			
 			header.addResolvedUsingCodeSystem(
 					this.buildCodeSystemVersionReference(
 							codeSystemName, 
 							codeSystemVersionName));	
 		}
 
 		return header;
 	}
 
 }
