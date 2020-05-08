 /**
  * EasySOA Registry
  * Copyright 2012 Open Wide
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact : easysoa-dev@googlegroups.com
  */
 
 package org.easysoa.registry.indicators.rest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import org.apache.log4j.Logger;
 import org.easysoa.registry.SubprojectServiceImpl;
 import org.easysoa.registry.types.Deliverable;
 import org.easysoa.registry.types.DeployedDeliverable;
 import org.easysoa.registry.types.Endpoint;
 import org.easysoa.registry.types.EndpointConsumption;
 import org.easysoa.registry.types.InformationService;
 import org.easysoa.registry.types.ServiceImplementation;
 import org.easysoa.registry.types.SoaNode;
 import org.easysoa.registry.types.SoftwareComponent;
 import org.easysoa.registry.types.TaggingFolder;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 import org.nuxeo.ecm.webengine.model.WebObject;
 import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
 
 /**
  * Indicators
  * 
  * @author mdutoo
  * 
  */
 //XXX Slightly outdated after removal of Service doctype
 @WebObject(type = "EasySOA")
 @Path("easysoa")
 public class IndicatorsController extends ModuleRoot {
 
     private static Logger logger = Logger.getLogger(IndicatorsController.class);
     
     // XXX Categories are currently unused by view
     public static final String CATEGORY_DOCTYPE_COUNTS = "Doctype counts";
     public static final String CATEGORY_DOCTYPE_SPECIFIC = "Doctype-specific indicators";
     public static final String CATEGORY_MISC = "Miscellaneous";
 
     private Map<String, List<IndicatorProvider>> indicatorProviders = new HashMap<String, List<IndicatorProvider>>();
     
     public IndicatorsController() {
         // Document count by type
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(SoaNode.ABSTRACT_DOCTYPE));
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(InformationService.DOCTYPE));
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(SoftwareComponent.DOCTYPE));
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(ServiceImplementation.DOCTYPE));
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(Deliverable.DOCTYPE));
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(DeployedDeliverable.DOCTYPE));
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(Endpoint.DOCTYPE));
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(EndpointConsumption.DOCTYPE));
         addIndicator(CATEGORY_DOCTYPE_COUNTS, new DoctypeCountIndicator(TaggingFolder.DOCTYPE));
         
         // Doctype-specific indicators
         //addIndicator(CATEGORY_DOCTYPE_SPECIFIC, new ServiceStateProvider());
         addIndicator(CATEGORY_DOCTYPE_SPECIFIC, new ServiceImplStateProvider());
         addIndicator(CATEGORY_DOCTYPE_SPECIFIC, new SoftwareComponentIndicatorProvider());
         addIndicator(CATEGORY_DOCTYPE_SPECIFIC, new TagsIndicatorProvider());
         addIndicator(CATEGORY_DOCTYPE_SPECIFIC, new ServiceConsumptionIndicatorProvider());
 
         // Miscellaneous indicators
         addIndicator(CATEGORY_MISC, new PlaceholdersIndicatorProvider());
         
     }
     
     public void addIndicator(String category, IndicatorProvider indicator) {
         if (!indicatorProviders.containsKey(category)) {
             indicatorProviders.put(category, new ArrayList<IndicatorProvider>());
         }
         indicatorProviders.get(category).add(indicator);
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     public Object doGetHTML(/*@DefaultValue(null) */@QueryParam("subprojectId") String subprojectId) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
 
         // using all subprojects or getting default one is let to indicator impls TODO better
         //subprojectId = SubprojectServiceImpl.getSubprojectIdOrCreateDefault(session, subprojectId);
         // TODO default or not ??
         /*String subprojectPathCriteria;
         if (subprojectId == null) {
             subprojectPathCriteria = "";
         } else {
             subprojectPathCriteria = " " + IndicatorProvider.NXQL_PATH_STARTSWITH + session.getDocument(new IdRef(subprojectId)).getPathAsString() + "'";
         }*/
         
        if("".equals(subprojectId)){
            subprojectId = null;
        }
        
         Map<String, Map<String, IndicatorValue>> indicatorsByCategory = computeIndicators(subprojectId);
         
         // Create and return view
         HashMap<String, Integer> nbMap = new HashMap<String, Integer>();
         HashMap<String, Integer> percentMap = new HashMap<String, Integer>();
         for (Map<String, IndicatorValue> indicatorCategory : indicatorsByCategory.values()) {
             for (Entry<String, IndicatorValue> indicator : indicatorCategory.entrySet()) {
                 if (indicator.getValue().getCount() != -1) {
                     nbMap.put(indicator.getKey(), indicator.getValue().getCount());
                 }
                 if (indicator.getValue().getPercentage() != -1) {
                     percentMap.put(indicator.getKey(), indicator.getValue().getPercentage());
                 }
             }
         }
         
         // Set args for the template
         return getView("indicators")
                 .arg("nbMap", nbMap)
                 .arg("percentMap", percentMap)
                 .arg("subprojectId", subprojectId);
     }
     
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Object doGetJSON(/*@DefaultValue(null) */@QueryParam("subproject") String subprojectId) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         
         subprojectId = SubprojectServiceImpl.getSubprojectIdOrCreateDefault(session, subprojectId);
         // TODO default or not ??
         
         return computeIndicators(subprojectId);
     }
     
     private Map<String, Map<String, IndicatorValue>> computeIndicators(String subprojectId) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         
         List<IndicatorProvider> computedProviders = new ArrayList<IndicatorProvider>();
         List<IndicatorProvider> pendingProviders = new ArrayList<IndicatorProvider>();
         Map<String, IndicatorValue> computedIndicators = new HashMap<String, IndicatorValue>();
         Map<String, Map<String, IndicatorValue>> indicatorsByCategory = new HashMap<String, Map<String, IndicatorValue>>();
         int previousComputedProvidersCount = -1;
 
         // Compute indicators in several passes, with respect to dependencies
         while (computedProviders.size() != previousComputedProvidersCount) {
             previousComputedProvidersCount = computedProviders.size();
             
             for (Entry<String, List<IndicatorProvider>> indicatorProviderCategory : indicatorProviders.entrySet()) {
                 // Start or continue indicator category
                 Map<String, IndicatorValue> categoryIndicators = indicatorsByCategory.get(indicatorProviderCategory.getKey());
                 if (categoryIndicators == null) {
                     categoryIndicators = new HashMap<String, IndicatorValue>();
                 }
                 
                 // Browse all providers
                 for (IndicatorProvider indicatorProvider : indicatorProviderCategory.getValue()) {
                     if (!computedProviders.contains(indicatorProvider)) {
                         // Compute indicator only if the dependencies are already computed
                         List<String> requiredIndicators = indicatorProvider.getRequiredIndicators();
                         boolean allRequirementsSatisfied = true;
                         if (requiredIndicators != null) {
                             for (String requiredIndicator : requiredIndicators) {
                                 if (!computedIndicators.containsKey(requiredIndicator)) {
                                     allRequirementsSatisfied = false;
                                     break;
                                 }
                             }
                         }
                         
                         // Actual indicator calculation
                         if (allRequirementsSatisfied) {
                             Map<String, IndicatorValue> indicators = null;
                             try {
                                 indicators = indicatorProvider
                                         .computeIndicators(session, subprojectId, computedIndicators);
                             }
                             catch (Exception e) {
                                 logger.warn("Failed to compute indicator '" + indicatorProvider.toString() + "': " + e.getMessage());
                             }
                             if (indicators != null) {
                                 categoryIndicators.putAll(indicators);
                                 computedIndicators.putAll(indicators);
                             }
                             
                             computedProviders.add(indicatorProvider);
                             pendingProviders.remove(indicatorProvider);
                         }
                         else {
                             pendingProviders.add(indicatorProvider);
                         }
                     }
                 }
                 indicatorsByCategory.put(indicatorProviderCategory.getKey(), categoryIndicators);
             }
         }
         
         // Warn if some indicators have been left pending 
         for (IndicatorProvider pendingProvider : pendingProviders) {
             logger.warn(pendingProvider.getClass().getName()
                     + " provider dependencies could not be satisfied ("
                     + pendingProvider.getRequiredIndicators() + ")");
         }
         
         return indicatorsByCategory;
     }
         
 
 }
