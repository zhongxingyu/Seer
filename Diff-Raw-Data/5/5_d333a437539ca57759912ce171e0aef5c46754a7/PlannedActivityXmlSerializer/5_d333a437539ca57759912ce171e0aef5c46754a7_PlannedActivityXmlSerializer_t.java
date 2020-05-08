 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.tools.StringTools;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
 import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Element;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class PlannedActivityXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
     private ActivityXmlSerializer activityXmlSerializer;
     private ActivityReferenceXmlSerializer activityReferenceXmlSerializer;
     private PlannedActivityLabelXmlSerializer plannedActivityLabelXmlSerializer = new PlannedActivityLabelXmlSerializer();
     public static final String PLANNED_ACTIVITY = "planned-activity";
 
     public static final String POPULATION = "population";
     private static final String DETAILS = "details";
     private static final String DAY = "day";
     private static final String CONDITION = "condition";
     private static final String WEIGHT = "weight";
 
     protected PlanTreeNode<?> nodeInstance() {
         return new PlannedActivity();
     }
 
     protected String elementName() {
         return PLANNED_ACTIVITY;
     }
 
     protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
         return null;
     }
 
     protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
         ((PlannedActivity) node).setDetails(element.attributeValue(DETAILS));
         ((PlannedActivity) node).setDay(new Integer(element.attributeValue(DAY)));
         ((PlannedActivity) node).setCondition(element.attributeValue(CONDITION));
         if (element.attributeValue(WEIGHT) != null) {
             ((PlannedActivity) node).setWeight(new Integer(element.attributeValue(WEIGHT)));
         }
 
         String populationAbbreviation = element.attributeValue(POPULATION);
         if (populationAbbreviation != null) {
             Population population = new Population();
             population.setAbbreviation(populationAbbreviation);
             ((PlannedActivity) node).setPopulation(population);
         }
 
         if(XsdElement.PLANNED_ACTIVITY_LABEL.xmlName()!=null) {
         Iterator iterator = element.elementIterator(XsdElement.PLANNED_ACTIVITY_LABEL.xmlName());
             if (iterator != null) {
                 while(iterator.hasNext())  {
                     PlannedActivityLabel label = plannedActivityLabelXmlSerializer.readElement((Element) iterator.next());
                     ((PlannedActivity) node).addPlannedActivityLabel(label);
 
                 }
             }
         }
 
         Activity activity = activityXmlSerializer.readElement(element.element(XsdElement.ACTIVITY.xmlName()));
         ((PlannedActivity) node).setActivity(activity);
     }
 
     protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
         element.addAttribute(DETAILS, ((PlannedActivity) node).getDetails());
         element.addAttribute(DAY, ((PlannedActivity) node).getDay().toString());
         element.addAttribute(CONDITION, ((PlannedActivity) node).getCondition());
         if( ((PlannedActivity) node).getWeight() != null) {
             element.addAttribute(WEIGHT, ((PlannedActivity) node).getWeight().toString());
         }    
         if(!(((PlannedActivity) node).getPlannedActivityLabels().isEmpty())) {
             for(PlannedActivityLabel label:((PlannedActivity) node).getPlannedActivityLabels()) {
                 Element eLabel = plannedActivityLabelXmlSerializer.createElement(label);
                 element.add(eLabel);
             }
         }
 
         Population population = ((PlannedActivity) node).getPopulation();
         if (population != null) {
             element.addAttribute(POPULATION, ((PlannedActivity) node).getPopulation().getAbbreviation());
         }
 
         Element eActivity = activityReferenceXmlSerializer.createElement(((PlannedActivity) node).getActivity());
         element.add(eActivity);
     }
 
     public void setActivityXmlSerializer(ActivityXmlSerializer activityXmlSerializer) {
         this.activityXmlSerializer = activityXmlSerializer;
     }
 
     public void setPlannedActivityLabelXmlSerializer(PlannedActivityLabelXmlSerializer plannedActivityLabelXmlSerializer) {
         this.plannedActivityLabelXmlSerializer = plannedActivityLabelXmlSerializer;
     }
 
     public void setActivityReferenceXmlSerializer(ActivityReferenceXmlSerializer activityReferenceXmlSerializer) {
         this.activityReferenceXmlSerializer = activityReferenceXmlSerializer;
     }
 
     @Override
     public String validateElement(MutableDomainObject planTreeNode, Element element) {
 
         StringBuffer errorMessageStringBuffer = new StringBuffer(super.validateElement(planTreeNode, element));
         PlannedActivity plannedActivity = (PlannedActivity) planTreeNode;
 
         if (element.element(XsdElement.ACTIVITY.xmlName()) != null) {
             if (!activityXmlSerializer.validateElement(plannedActivity.getActivity(), element.element(XsdElement.ACTIVITY.xmlName()))) {
                errorMessageStringBuffer.append(String.format("activities are different for " + planTreeNode.getClass().getSimpleName()
                         + ". expected:%s. \n", plannedActivity.getActivity()));
             }
         } else {
             if (!activityReferenceXmlSerializer.validateElement(plannedActivity.getActivity(), element.element(XsdElement.ACTIVITY_REFERENCE.xmlName()))) {
                errorMessageStringBuffer.append(String.format("activity references are different for " + planTreeNode.getClass().getSimpleName()
                         + ". expected:%s. \n", plannedActivity.getActivity()));
             }
         }
 
         for(PlannedActivityLabel label: plannedActivity.getPlannedActivityLabels()) {
            if (!plannedActivityLabelXmlSerializer.validateElement(label, element.element(XsdElement.PLANNED_ACTIVITY_LABEL.xmlName()))) {
             errorMessageStringBuffer.append(String.format("labels are different for " + planTreeNode.getClass().getSimpleName()
                     + ". expected:%s. \n",label));
             }
         }
 
         if (!StringUtils.equals(plannedActivity.getDetails(), element.attributeValue(DETAILS))) {
             errorMessageStringBuffer.append(String.format("details  are different for " + planTreeNode.getClass().getSimpleName()
                     + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getDetails(),
                     element.attributeValue(DETAILS)));
 
         } else if (!StringUtils.equals(StringTools.valueOf(plannedActivity.getDay()), element.attributeValue(DAY))) {
             errorMessageStringBuffer.append(String.format("days  are different for " + planTreeNode.getClass().getSimpleName()
                     + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getDay(),
                     element.attributeValue(DAY)));
 
         } else if (!StringUtils.equals(plannedActivity.getCondition(), element.attributeValue(CONDITION))) {
             errorMessageStringBuffer.append(String.format("conditions  are different for " + planTreeNode.getClass().getSimpleName()
                     + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getCondition(),
                     element.attributeValue(CONDITION)));
 
         } else if (!StringUtils.equals(StringTools.valueOf(plannedActivity.getWeight()), element.attributeValue(WEIGHT))) {
             errorMessageStringBuffer.append(String.format("weights  are different for " + planTreeNode.getClass().getSimpleName()
                     + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getWeight(),
                     element.attributeValue(WEIGHT)));
 
         }
 
         if (plannedActivity.getPopulation() != null && element.attributeValue(POPULATION) != null) {
             Population population = plannedActivity.getPopulation();
             if (!StringUtils.equals(population.getAbbreviation(), element.attributeValue(POPULATION))) {
                 errorMessageStringBuffer.append(String.format("populations  are different for " + planTreeNode.getClass().getSimpleName()
                         + ". expected:%s , found (in imported document) :%s \n", population.getAbbreviation(),
                         element.attributeValue(POPULATION)));
 
 
             }
 
         } else if ((plannedActivity.getPopulation() == null && element.attributeValue(POPULATION) != null) || (plannedActivity.getPopulation() != null && element.attributeValue(POPULATION) == null)) {
             errorMessageStringBuffer.append(String.format("populations  are different for " + planTreeNode.getClass().getSimpleName()
                     + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getPopulation(),
                     element.attributeValue(POPULATION)));
 
         }
         return errorMessageStringBuffer.toString();
     }
 
     public PlannedActivity getPlannedActivityWithMatchingGridId(List<PlannedActivity> plannedActivities, Element element) {
         for (PlannedActivity plannedActivity : plannedActivities) {
             if (StringUtils.equals(plannedActivity.getGridId(), element.attributeValue(ID))) {
 
                 return plannedActivity;
 
             }
 
         }
 
         return null;
 
     }
 }
