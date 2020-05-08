 package com.amee.admin.restlet.environment;
 
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.LocaleConstants;
import com.amee.domain.LocaleService;
import com.amee.domain.ValueDefinition;
import com.amee.domain.data.ItemDefinition;
import com.amee.restlet.environment.DefinitionBrowser;
import com.amee.restlet.utils.APIFault;
import com.amee.service.data.DataConstants;
import com.amee.service.definition.DefinitionService;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
 @Component
 @Scope("prototype")
 public class ItemDefinitionResource extends AdminResource {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private LocaleService localeService;
 
     @Autowired
     private DefinitionService definitionService;
 
     @Autowired
     private DefinitionBrowser definitionBrowser;
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
         definitionBrowser.setItemDefinitionUid(request.getAttributes().get("itemDefinitionUid").toString());
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() && (definitionBrowser.getItemDefinitionUid() != null);
     }
 
     @Override
     public List<IAMEEEntityReference> getEntities() {
         List<IAMEEEntityReference> entities = new ArrayList<IAMEEEntityReference>();
         entities.add(getRootDataCategory());
        entities.add(definitionBrowser.getItemDefinition());
         return entities;
     }
 
     @Override
     public String getTemplatePath() {
         return DataConstants.VIEW_ITEM_DEFINITION;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         List<ValueDefinition> valueDefinitions = definitionService.getValueDefinitions();
         Map<String, Object> values = super.getTemplateValues();
         values.put("browser", definitionBrowser);
         values.put("itemDefinition", definitionBrowser.getItemDefinition());
         values.put("valueDefinitions", valueDefinitions.isEmpty() ? null : valueDefinitions);
         values.put("availableLocales", LocaleConstants.AVAILABLE_LOCALES.keySet());
         return values;
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         obj.put("itemDefinition", definitionBrowser.getItemDefinition().getJSONObject());
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         Element element = document.createElement("ItemDefinitionResource");
         element.appendChild(definitionBrowser.getItemDefinition().getElement(document));
         return element;
     }
 
     @Override
     public void doStore(Representation entity) {
 
         log.debug("doStore()");
 
         ItemDefinition itemDefinition = definitionBrowser.getItemDefinition();
         Form form = getForm();
 
         // Parse any submitted locale names
         for (String name : form.getNames()) {
             if (name.startsWith("name_")) {
                 // Get locale and locale name to handle.
                 String locale = name.substring(name.indexOf("_") + 1);
                 // Validate - Must have an available locale.
                 if (!LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                     badRequest(APIFault.INVALID_PARAMETERS);
                     return;
                 }
                 // Remove or Update/Create?
                 if (form.getNames().contains("remove_name_" + locale)) {
                     // Remove.
                     localeService.clearLocaleName(itemDefinition, locale);
                     itemDefinition.onModify();
                 } else {
                     // Update or create.
                     String localeNameStr = form.getFirstValue(name);
                     // Validate - Must have a locale name value.
                     if (StringUtils.isBlank(localeNameStr)) {
                         badRequest(APIFault.INVALID_PARAMETERS);
                         return;
                     }
                     // Do the update or create.
                     localeService.setLocaleName(itemDefinition, locale, localeNameStr);
                     itemDefinition.onModify();
                 }
             }
         }
 
         Set<String> names = form.getNames();
         if (names.contains("name")) {
             itemDefinition.setName(form.getFirstValue("name"));
         }
         if (names.contains("drillDown")) {
             itemDefinition.setDrillDown(form.getFirstValue("drillDown"));
         }
 
         success();
     }
 
     @Override
     public void doRemove() {
         log.debug("doRemove");
         definitionService.remove(definitionBrowser.getItemDefinition());
         success();
     }
 }
