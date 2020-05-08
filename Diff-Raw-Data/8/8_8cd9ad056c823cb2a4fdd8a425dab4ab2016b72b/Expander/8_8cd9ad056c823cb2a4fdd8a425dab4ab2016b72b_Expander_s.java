 package net.sf.laja.parser.cdd.statetemplate;
 
 import java.util.*;
 
 public class Expander {
     public boolean cyclic = false;
     public String cyclicMessage;
     public Imports importsResult;
     public List<Attribute> attributesResult;
     public List<Attribute> expandedAttributesResult;
     private List<StateTemplate> stateTemplates = new ArrayList<StateTemplate>();
     private Map<String, StateTemplate> stateTemplateMap;
 
     private StateTemplateErrors errors;
 
     public void add(StateTemplate stateTemplate) {
         stateTemplates.add(stateTemplate);
     }
 
     /**
      * Adds expanded attributes and imports from expanded attributes, to all state templates.
      *
      * @param errors if any cyclic dependency is encountered a message is added to 'errors'.
      */
     public void expand(StateTemplateErrors errors) {
         this.errors = errors;
         Map<String, ExpansionResult> stateClassMap = calculateExpansion();
 
         if (cyclic) {
             errors.addMessage(cyclicMessage);
             return;
         }
         // Update state templates with the expanded attributes and imports.
         for (StateTemplate stateTemplate : stateTemplates) {
             ExpansionResult expandedResult = stateClassMap.get(stateTemplate.stateClass);
             stateTemplate.setAttributes(expandedResult.attributes);
             stateTemplate.setExpandedAttributes(expandedResult.expandedAttributes);
             for (Importstatement importstatement : expandedResult.imports) {
                 stateTemplate.imports.addImportIfNotExists(importstatement);
             }
         }
     }
 
     /**
      * Goes through all state templates and calculates new expanded attributes and
      * possible needed imports for every state template.
      *
      * @return state templates with attributes and imports.
      */
     public Map<String, ExpansionResult> calculateExpansion() {
         Map<String, ExpansionResult> stateClassMap = new LinkedHashMap<String, ExpansionResult>();
         
         for (StateTemplate stateTemplate : stateTemplates) {
             importsResult = new Imports();
             attributesResult = new ArrayList<Attribute>();
             expandedAttributesResult = new ArrayList<Attribute>();
             List<String> expandedAttributes = new ArrayList<String>();
 
             expand(stateTemplate.stateClass, false, expandedAttributes);
             stateClassMap.put(stateTemplate.stateClass, new ExpansionResult(attributesResult, expandedAttributesResult, importsResult));
         }
         calculateAllExpandedTypes();
 
         return stateClassMap;
     }
 
     private void calculateAllExpandedTypes() {
         stateTemplateMap = new HashMap<String,StateTemplate>();
         for (StateTemplate stateTemplate : stateTemplates) {
             stateTemplateMap.put(stateTemplate.classname, stateTemplate);
         }
         for (StateTemplate stateTemplate : stateTemplates) {
             for (ExpandedType expandedType : stateTemplate.expandedTypes) {
                 addExpandedType(expandedType, stateTemplate.allExpandedTypes, stateTemplate.allImports);
             }
         }
     }
 
     private void addExpandedType(ExpandedType expandedType, Set<ExpandedType> allExpandedTypes, Imports allImports) {
         if (!allExpandedTypes.contains(expandedType)) {
             allExpandedTypes.add(expandedType);
             StateTemplate stateTemplate = stateTemplateMap.get(expandedType.type);
             allImports.addImports(stateTemplate.imports);
 
             for (ExpandedType expType : stateTemplate.expandedTypes) {
                 addExpandedType(expType, allExpandedTypes, allImports);
             }
         }
     }
 
     private void expand(String type, boolean isExpanded, List<String> expandedAttributes) {
         if (cyclic) {
             return;
         }
        expandedAttributes.add(type);
         StateTemplate stateTemplate = findStateTemplate(type);
 
         if (stateTemplate != null) {
             for (Attribute attribute : stateTemplate.attributes) {
                 if (attribute.isExpand) {
                     if (isCyclic(type, attribute, expandedAttributes)) {
                         return;
                     }
                     addExpandedImports(stateTemplate, attribute);
                     if (!isExpanded) {
                         expandedAttributesResult.add(attribute);
                     }
                     expand(attribute.type, true, expandedAttributes);
                 } else {
                     if (!attributesResult.contains(attribute)) {
                         addAttributeToResult(attribute, isExpanded);
                     } else {
                         Attribute existingAttribute = attributesResult.get(attributesResult.indexOf(attribute));
                         if (existingAttribute.isOptional && attribute.isMandatory) {
                             // Replace the attribute with the one that is mandatory.
                             attributesResult.remove(attribute);
                             addAttributeToResult(attribute, isExpanded);
                         }
                     }
                 }
             }
         }
     }
 
     private boolean isCyclic(String type, Attribute attribute, List<String> expandedAttributes) {
         String expandedAttribute = type + "." + attribute.variable;
 
         if (expandedAttributes.contains(expandedAttribute)) {
             cyclicMessage = "Cyclic references: ";
             String separator = "";
             for (String expandingType : expandedAttributes) {
                 cyclicMessage += separator + expandingType;
                 separator = " > ";
             }
             cyclicMessage += separator + type;
             cyclic = true;
             return true;
         }
         expandedAttributes.add(expandedAttribute);
         return false;
     }
 
     private void addAttributeToResult(Attribute attribute, boolean isExpanded) {
         if (isExpanded) {
             Attribute copy = attribute.copyAllAttributesExceptIdAndKeyPlusReplaceIdWithExclude();
             copy.cleanCommentFromIdAndKey();
             attributesResult.add(copy);
         } else {
             attributesResult.add(attribute);
         }
     }
 
     private void addExpandedImports(StateTemplate stateTemplate, Attribute attribute) {
         StateTemplate expandedTemplate = findStateTemplate(attribute.type);
         if (expandedTemplate != null) {
             for (Importstatement importstatement : expandedTemplate.imports) {
                 importsResult.addImportIfNotExists(importstatement);
             }
             String type = attribute.isState ? attribute.cleanedStateType : attribute.type;
             stateTemplate.expandedTypes.add(new ExpandedType(attribute.variable, type, expandedTemplate.packagename + "." + type));
         }
     }
 
     private StateTemplate findStateTemplate(String type) {
         for (StateTemplate stateTemplate : stateTemplates) {
             if (type.equals(stateTemplate.stateClass)) {
                 return stateTemplate;
             }
         }
         return null;
     }
     
     public class ExpansionResult {
         public final List<Attribute> attributes;
         public final List<Attribute> expandedAttributes;
         public final Imports imports;
 
         public ExpansionResult(List<Attribute> attributes, List<Attribute> expandedAttributes, Imports imports) {
             this.attributes = attributes;
             this.expandedAttributes = expandedAttributes;
 
             this.imports = imports;
         }
 
         @Override
         public String toString() {
             return "ExpansionResult{" +
                     "attributes=" + attributes +
                     "expandedAttributes=" + expandedAttributes +
                     ", imports=" + imports +
                     '}';
         }
     }
 }
