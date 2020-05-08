 /*
  * Copyright: (c) 2004-2012 Mayo Foundation for Medical Education and 
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
 package edu.mayo.cts2.framework.plugin.service.exist.profile.valuesetdefinition;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import javax.annotation.Resource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 import edu.mayo.cts2.framework.model.command.Page;
 import edu.mayo.cts2.framework.model.command.ResolvedFilter;
 import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
 import edu.mayo.cts2.framework.model.core.ComponentReference;
 import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
 import edu.mayo.cts2.framework.model.core.PredicateReference;
 import edu.mayo.cts2.framework.model.core.SortCriteria;
 import edu.mayo.cts2.framework.model.core.URIAndEntityName;
 import edu.mayo.cts2.framework.model.directory.DirectoryResult;
 import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
 import edu.mayo.cts2.framework.model.service.core.NameOrURI;
 import edu.mayo.cts2.framework.model.service.core.Query;
 import edu.mayo.cts2.framework.model.util.ModelUtils;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectoryEntry;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.AbstractExistService;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.resolvedvalueset.ExistResolvedValueSetQueryService;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.resolvedvalueset.ExistResolvedValueSetResolutionService;
 import edu.mayo.cts2.framework.service.command.restriction.ResolvedValueSetQueryServiceRestrictions;
 import edu.mayo.cts2.framework.service.profile.resolvedvalueset.ResolvedValueSetQuery;
 import edu.mayo.cts2.framework.service.profile.resolvedvalueset.name.ResolvedValueSetReadId;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResult;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
 import edu.mayo.cts2.framework.util.spring.AggregateService;
 
 @Component
 @AggregateService
 public class ExistValueSetDefinitionResolutionService 
 	extends AbstractExistService 
 	implements ValueSetDefinitionResolutionService {
 
 	@Autowired(required = false) 
 	@Qualifier("valueSetDefinitionResolutionServiceImpl")
 	private ValueSetDefinitionResolutionService valueSetResolutionImpl;
 	
 	@Resource
 	private ExistResolvedValueSetQueryService existResolvedValueSetQueryService;
 	
 	@Resource
 	private ExistResolvedValueSetResolutionService existResolvedValueSetResolutionService;
 
 	@Override
 	public Set<PredicateReference> getKnownProperties() {
 		if (valueSetResolutionImpl != null)
 		{
 			return valueSetResolutionImpl.getKnownProperties();
 		}
 		return null;
 	}
 
 	@Override
 	public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms() {
 		if (valueSetResolutionImpl != null)
 		{
 			return valueSetResolutionImpl.getSupportedMatchAlgorithms();
 		}
 		return null;
 	}
 
 	@Override
 	public Set<? extends ComponentReference> getSupportedSearchReferences() {
 		if (valueSetResolutionImpl != null)
 		{
 			return valueSetResolutionImpl.getSupportedSearchReferences();
 		}
 		return null;
 	}
 
 	@Override
 	public Set<? extends ComponentReference> getSupportedSortReferences() {
 		if (valueSetResolutionImpl != null)
 		{
 			return valueSetResolutionImpl.getSupportedSortReferences();
 		}
 		return null;
 	}
 
 	/*
 	 * If the valueSetResolutionImpl is not provided - this falls through to the old implementation 
 	 * that doesn't really resolve anything... it looks to see if there is one (and only one)
 	 * ResolvedValueSet for the definition. If so, return that.
 	 * 
 	 * TODO: decide if the old code should just be removed - assume valueSetResolutionImpl will be deployed with this?
 	 * Or otherwise retire this T ODO?
 	 * 
 	 * (non-Javadoc)
 	 * @see edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService#resolveDefinition(edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId, java.util.Set, edu.mayo.cts2.framework.model.service.core.NameOrURI, edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery, edu.mayo.cts2.framework.model.core.SortCriteria, edu.mayo.cts2.framework.model.command.ResolvedReadContext, edu.mayo.cts2.framework.model.command.Page)
 	 */
 	@Override
 	public ResolvedValueSetResult<URIAndEntityName> resolveDefinition(
             final ValueSetDefinitionReadId id,
             Set<NameOrURI> codeSystemVersions,
             NameOrURI tag,
             SortCriteria sort,
             ResolvedReadContext context,
             Page page) {
 		
 		if (valueSetResolutionImpl != null)
 		{
			return valueSetResolutionImpl.resolveDefinition(id, codeSystemVersions, tag, sort, context, page);
 		}
 		
 		ResolvedValueSetQuery resolvedValueSetQuery = new ResolvedValueSetQuery(){
 
 			@Override
 			public Set<ResolvedFilter> getFilterComponent() {
 				return null;
 			}
 
 			@Override
 			public Query getQuery() {
 				return null;
 			}
 
 			@Override
 			public ResolvedValueSetQueryServiceRestrictions getResolvedValueSetQueryServiceRestrictions() {
 				ResolvedValueSetQueryServiceRestrictions restrictions = new ResolvedValueSetQueryServiceRestrictions();
 				restrictions.setValueSets(new HashSet<NameOrURI>(Arrays.asList(id.getValueSet())));
 				
 				return restrictions;
 			}
 			
 		};
 		
 		DirectoryResult<ResolvedValueSetDirectoryEntry> summaries = 
 			existResolvedValueSetQueryService.getResourceSummaries(resolvedValueSetQuery, sort, page);
 		
 		if(summaries != null && summaries.getEntries() != null && summaries.getEntries().size() == 1){
 			ResolvedValueSetDirectoryEntry summary = summaries.getEntries().get(0);
 			
 			ResolvedValueSetReadId identifier = 
 				new ResolvedValueSetReadId(
 					summary.getResourceName(),
 					id.getValueSet(),
 					ModelUtils.nameOrUriFromName(id.getName()));
 			
 			return this.existResolvedValueSetResolutionService.
 						getResolution(
 								identifier, 
 								null, 
 								page);
 		} else {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	@Override
 	public ResolvedValueSet resolveDefinitionAsCompleteSet(
 			ValueSetDefinitionReadId arg0, 
 			Set<NameOrURI> arg1, 
 			NameOrURI arg2,
 			ResolvedReadContext arg3) {
 		if (valueSetResolutionImpl != null)
 		{
 			return valueSetResolutionImpl.resolveDefinitionAsCompleteSet(arg0, arg1, arg2, arg3);
 		}
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ResolvedValueSetResult<EntityDirectoryEntry> resolveDefinitionAsEntityDirectory(
 			ValueSetDefinitionReadId arg0, 
 			Set<NameOrURI> arg1, 
 			NameOrURI arg2,
 			ResolvedValueSetResolutionEntityQuery arg3, 
 			SortCriteria arg4,
 			ResolvedReadContext arg5, Page arg6) {
 		if (valueSetResolutionImpl != null)
 		{
 			return valueSetResolutionImpl.resolveDefinitionAsEntityDirectory(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
 		}
 		throw new UnsupportedOperationException();
 	}
 
 }
