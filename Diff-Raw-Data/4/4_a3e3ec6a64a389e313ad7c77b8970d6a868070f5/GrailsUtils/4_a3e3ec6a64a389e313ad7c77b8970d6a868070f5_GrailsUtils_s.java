 /**
  * 
  */
 package org.andromda.cartridges.grails;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.andromda.metafacades.uml.AttributeFacade;
 import org.andromda.metafacades.uml.ClassifierFacade;
 import org.andromda.metafacades.uml.DependencyFacade;
 import org.andromda.metafacades.uml.Entity;
 import org.andromda.metafacades.uml.EnumerationFacade;
 import org.andromda.metafacades.uml.EventFacade;
 import org.andromda.metafacades.uml.FilteredCollection;
 import org.andromda.metafacades.uml.FrontEndActionState;
 import org.andromda.metafacades.uml.FrontEndEvent;
 import org.andromda.metafacades.uml.FrontEndExceptionHandler;
 import org.andromda.metafacades.uml.FrontEndForward;
 import org.andromda.metafacades.uml.GeneralizableElementFacade;
 import org.andromda.metafacades.uml.ModelElementFacade;
 import org.andromda.metafacades.uml.ValueObject;
 import org.andromda.utils.StringUtilsHelper;
 
 /**
  * @author stephane.manciot@ebiznext.com
  * 
  *         Contains utilities used within the Grails cartridge.
  */
 public class GrailsUtils
 {
 
     /**
      * 
      */
     private static final String DEFAULT_TRIGGER_KEY = "success";
 
     /**
      * Returns an ordered set containing the argument model elements, model
      * elements with a name that is already used by another model element in the
      * argument collection will not be returned. The first operation with a name
      * not already encountered will be returned, the order inferred by the
      * argument's iterator will determine the order of the returned list.
      * 
      * @param modelElements
      *            a collection of model elements, elements that are not model
      *            elements will be ignored
      * @return the argument model elements without, elements with a duplicate
      *         name will only be recorded once
      */
     public List filterUniqueByName(Collection modelElements)
     {
         final Map filteredElements = new LinkedHashMap();
 
         for (final Iterator elementIterator = modelElements.iterator(); elementIterator
             .hasNext();)
         {
             final Object object = elementIterator.next();
             if (object instanceof ModelElementFacade)
             {
                 final ModelElementFacade modelElement = (ModelElementFacade) object;
                 if (!filteredElements.containsKey(modelElement.getName()))
                 {
                     filteredElements.put(modelElement.getName(), modelElement);
                 }
             }
         }
 
         return new ArrayList(filteredElements.values());
     }
 
     /**
      * Retrieves the values object references for this entity. If
      * <code>follow</code> is true, then all value object references (including
      * those that were inherited) will be retrieved.
      */
     public Collection getValueObjectReferences(Entity e, boolean follow)
     {
         final Collection sourceDependencies = new ArrayList(e.getSourceDependencies());
         if (follow)
         {
             for (GeneralizableElementFacade entity = e.getGeneralization(); entity != null; entity =
                 entity.getGeneralization())
             {
                 sourceDependencies.addAll(entity.getSourceDependencies());
             }
         }
         return new FilteredCollection(sourceDependencies)
         {
             public boolean evaluate(Object object)
             {
                 boolean valid = false;
                 Object targetElement = ((DependencyFacade) object).getTargetElement();
                 if (targetElement instanceof ClassifierFacade)
                 {
                     ClassifierFacade element = (ClassifierFacade) targetElement;
                     valid =
                         element.isDataType() || element instanceof ValueObject
                             || element instanceof EnumerationFacade;
                 }
                 return valid;
             }
         };
     }
 
     /**
      * Specifies whether this attribute should use a custom validator
      * 
      * @param attr
      *            - entity attribute
      * @return true if this attribute should respect custom validator, false
      *         otherwise
      */
     public boolean hasCustomValidator(ModelElementFacade attr)
     {
         boolean ret = attr.hasStereotype(GrailsProfile.STEREOTYPE_CUSTOM_VALIDATOR);
         return ret;
     }
 
     /**
      * @param classifier
      *            - classifier
      * @return classifier name
      */
     public String getClassifierName(ClassifierFacade classifier)
     {
        String ret = 
                classifier.getName().substring(0, 1).toLowerCase() + classifier.getName().substring(1);
        return ret;
     }
 
     /**
      * @param classifier
      *            - classifier
      * @param attr
      *            - entity attribute
      * @return custom validator name
      */
     public String getCustomValidatorName(ClassifierFacade classifier, ModelElementFacade attr)
     {
         String ret = null;
         if (hasCustomValidator(attr))
         {
             ret =
                 classifier.getName().substring(0, 1).toLowerCase() + classifier.getName().substring(1);
             ret += attr.getName().substring(0, 1).toUpperCase() + attr.getName().substring(1);
             ret += "Validator";
         }
         return ret;
     }
 
     /**
      * @param classifier
      *            - classifier
      * @param attr
      *            - entity attribute
      * @return default custom validator closure
      */
     public String getDefaultCustomValidatorClosure(ClassifierFacade classifier,
         ModelElementFacade attr)
     {
         String ret = null;
         if (hasCustomValidator(attr))
         {
             ret =
                 "{ value, " + classifier.getName().substring(0, 1).toLowerCase()
                     + classifier.getName().substring(1) + " -> return true }";
         }
         return ret;
     }
 
     /**
      * Specifies whether this attribute's value should respect credit card
      * constraints.
      * 
      * @param attr
      *            - entity attribute
      * @return true if this attribute's value should respect credit card
      *         constraints, false otherwise
      */
     public boolean isCreditCard(AttributeFacade attr)
     {
         return GrailsProfile.TAGGEDVALUEVALUE_CREDIT_CARD.equals(getConstraintType(attr));
     }
 
     /**
      * Specifies whether this attribute's value should respect email address
      * constraints.
      * 
      * @param attr
      *            - entity attribute
      * @return true if this attribute's value should respect email address
      *         constraints, false otherwise
      */
     public boolean isEmail(AttributeFacade attr)
     {
         return GrailsProfile.TAGGEDVALUEVALUE_EMAIL.equals(getConstraintType(attr));
     }
 
     /**
      * Specifies whether this attribute's value should respect url constraints.
      * 
      * @param attr
      *            - entity attribute
      * @return true if this attribute's value should respect url constraints,
      *         false otherwise
      */
     public boolean isUrl(AttributeFacade attr)
     {
         return GrailsProfile.TAGGEDVALUEVALUE_URL.equals(getConstraintType(attr));
     }
 
     /**
      * Specifies whether this attribute's value should respect regular
      * expression constraints.
      * 
      * @param attr
      *            - entity attribute
      * @return true if this attribute's value should respect regular expression
      *         constraints, false otherwise
      */
     public boolean isRegularExpression(AttributeFacade attr)
     {
         return GrailsProfile.TAGGEDVALUEVALUE_REGULAR_EXPRESSION
             .equals(getConstraintType(attr));
     }
 
     /**
      * @param attr
      *            - entity attribute
      * @return matches constraint
      */
     public String getMatchesConstraint(AttributeFacade attr)
     {
         if (isRegularExpression(attr))
         {
             Object taggedValue =
                 attr.findTaggedValue(GrailsProfile.TAGGEDVALUE_GORM_CONSTRAINT_MATCHES);
             return taggedValue != null ? taggedValue.toString() : null;
         }
         return null;
     }
 
     /**
      * @param attr
      *            - entity attribute
      * @return matches constraint
      */
     public String getRangeConstraint(AttributeFacade attr)
     {
         if (isRange(attr))
         {
             Object taggedValue =
                 attr.findTaggedValue(GrailsProfile.TAGGEDVALUE_GORM_CONSTRAINT_RANGE);
             return taggedValue != null ? taggedValue.toString() : null;
         }
         return null;
     }
 
     /**
      * Specifies whether this attribute's value should respect range
      * constraints.
      * 
      * @param attr
      *            - entity attribute
      * @return true if this attribute's value should respect range constraints,
      *         false otherwise
      */
     public boolean isRange(AttributeFacade attr)
     {
         return GrailsProfile.TAGGEDVALUEVALUE_RANGE.equals(getConstraintType(attr));
     }
 
     /**
      * @param useCaseName
      * @return web flow name
      */
     public String getWebFlowName(String useCaseName)
     {
         return normalizeWebFlowStateName(useCaseName) + "Flow";
     }
 
     /**
      * @param useCaseName
      * @return web flow actions handler
      */
     public String getWebFlowActionsHandler(String useCaseName)
     {
         StringTokenizer str =
             new StringTokenizer(StringUtilsHelper.toPhrase(useCaseName).replace(' ', '-'), "-");
         StringBuffer out = new StringBuffer();
         while (str.hasMoreTokens())
         {
             String token = str.nextToken();
             token =
                 token.substring(0, 1).toUpperCase()
                     + ((token.length() > 0) ? token.substring(1) : "");
             out.append(token);
         }
         return out.toString() + "ActionsHandler";
     }
 
     /**
      * @param stateName
      * @return web flow state name
      */
     public String normalizeWebFlowStateName(String stateName)
     {
         return normalizeWebFlowName(stateName);
     }
 
     /**
      * @param name
      * @return web flow state name
      */
     public String normalizeWebFlowName(String name)
     {
         if (name != null)
         {
             StringTokenizer str =
                 new StringTokenizer(StringUtilsHelper.toPhrase(name).replace(' ', '-'), "-");
             StringBuffer out = new StringBuffer();
             boolean first = true;
             while (str.hasMoreTokens())
             {
                 String token = str.nextToken();
                 token =
                     (first ? token.substring(0, 1).toLowerCase() : token.substring(0, 1)
                         .toUpperCase())
                         + ((token.length() > 0) ? token.substring(1) : "");
                 if (first)
                 {
                     first = false;
                 }
                 out.append(token);
             }
             return out.toString();
         }
         return null;
     }
 
     /**
      * @param controllerName
      * @return web flow controller name
      */
     public String normalizeWebFlowControllerName(String controllerName)
     {
         if (controllerName != null)
         {
             if (controllerName.endsWith("Controller"))
             {
                 controllerName = controllerName.substring(0, controllerName.length() - 10);
             }
             return normalizeWebFlowName(controllerName);
         }
         return null;
     }
 
     /**
      * @param actionState
      * @return
      */
     public String getTriggerKey(FrontEndActionState actionState)
     {
         String triggerKey = null;
         FrontEndForward forward = actionState.getForward();
         if (forward != null)
         {
             if (forward.isEnteringView())
             {
                 EventFacade trigger = forward.getDecisionTrigger();
                 if (trigger == null)
                 {
                     trigger = forward.getTrigger();
                 }
                 if (trigger != null)
                 {
                     if (trigger instanceof FrontEndEvent)
                     {
                         triggerKey = StringUtilsHelper.toResourceMessageKey(trigger.getName());
                     }
                 }
             }
             else if (forward.isEnteringActionState())
             {
                 // TODO
             }
         }
         else
         {
             System.err.println("no forward found for " + actionState.getPackageName() + "."
                 + actionState.getName());
         }
         return (triggerKey != null && triggerKey.trim().length() > 0) ? triggerKey
             : DEFAULT_TRIGGER_KEY;
     }
 
     public String getExceptionType(FrontEndExceptionHandler exception)
     {
         final Object value =
             exception.findTaggedValue(GrailsProfile.TAGGEDVALUE_WEB_FLOW_EXCEPTION_TYPE);
         String exceptionType = value == null ? null : value.toString();
         if (exceptionType == null)
         {
             exceptionType = GrailsProfile.TAGGEDVALUE_WEB_FLOW_DEFAULT_EXCEPTION_TYPE;
         }
         return exceptionType;
     }
 
     private String getConstraintType(AttributeFacade attr)
     {
         String ret = null;
         if (attr.hasStereotype(GrailsProfile.STEREOTYPE_CONSTRAINT))
         {
             Object taggedValue =
                 attr.findTaggedValue(GrailsProfile.TAGGEDVALUE_GORM_CONSTRAINT_TYPE);
             if (taggedValue != null)
             {
                 ret = taggedValue.toString();
             }
         }
         return ret;
     }
 }
