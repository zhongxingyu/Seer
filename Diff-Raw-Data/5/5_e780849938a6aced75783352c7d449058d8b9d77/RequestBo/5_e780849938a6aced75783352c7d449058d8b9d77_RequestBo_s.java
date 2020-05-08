 package fr.cg95.cvq.generator.plugins.bo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 import fr.cg95.cvq.generator.common.Condition;
 import fr.cg95.cvq.generator.common.Step;
 
 /**
  * @author rdj@zenexity.fr
  */
 public class RequestBo {
     
     private String name;
     private String acronym;
     private List<Step> steps;
     private Set<Condition> conditions;
     private List<ElementBo> elements;
 
     public RequestBo(String name, String targetNamespace) {
         this.name =  StringUtils.uncapitalize(name);
         this.acronym = StringUtils.substringAfterLast(targetNamespace, "/");
     }
     
     public String getName() {
         return name;
     }
     
     public String getAcronym() {
         return acronym;
     }
 
     public List<Step> getSteps() {
         return steps;
     }
     public void setSteps(List<Step> steps) {
        this.steps = steps;
         
        for (Iterator<Step> it = steps.iterator(); it.hasNext();) {
             if (it.next().getName() == null)
                 it.remove();
         }
     }
     
     public Set<Condition> getConditions() {
         return conditions;
     }
     
     public void setConditions(Set<Condition> conditions) {
         this.conditions = conditions;
     }
     
     public List<ElementBo> getElements() {
         return elements;
     }
     
     public void addElement (ElementBo element) {
         if (elements == null)
             elements = new ArrayList<ElementBo>();
         elements.add(element);
     }
     
     public List<ElementBo> getElementsByStep(Step step, int column) {
         List<ElementBo> stepElements = new ArrayList<ElementBo>();
         for (ElementBo element : elements) {
             if (element.getStep().getName().equals(step.getName())
                     && element.getColumn() == column)
                 stepElements.add(element);
         }
         testAfterAttribute(stepElements);
         return sortByAfterAttribute(stepElements);
     }
     
     private List<ElementBo> sortByAfterAttribute(List<ElementBo> elements) {
         List<ElementBo> sortedElements = new ArrayList<ElementBo>();
         Set<ElementBo> notSortedElements = new HashSet<ElementBo>();
         for (ElementBo element : elements) {
             if (element.getAfter() == null)
                 sortedElements.add(element);
             else 
                 notSortedElements.add(element);
         }
         for (ElementBo notSortedElement : notSortedElements) {
             for (ElementBo sortedElement : sortedElements) {
                 if (notSortedElement.getAfter().equals(sortedElement.getName())) {
                     sortedElements.add(sortedElements.indexOf(sortedElement) + 1  , notSortedElement);
                     break;
                 }
             }
         }
         return sortedElements;
     }
     
     private void testAfterAttribute(List<ElementBo> elements) {
         Map<String, String> name_afterMap = new HashMap<String, String>();
         for (ElementBo element : elements)
             name_afterMap.put(element.getName(), element.getAfter());
         
         Set<String> afters = new HashSet<String>();
         for (String name : name_afterMap.keySet()) {
             String after = name_afterMap.get(name);
             if (after != null) {
                 if (!name_afterMap.keySet().contains(after))
                     throw new RuntimeException("testAfterAttribute() - {" + after +"} not exist");
                 if (after.equals(name))
                     throw new RuntimeException("testAfterAttribute() - self reference {" + name +"} <->{" + after +"}");
                 if (name_afterMap.get(after) != null && name_afterMap.get(after).equals(name))
                     throw new RuntimeException("testAfterAttribute() - cyclic reference : {" + name +"} <->{" + after +"}");
                 if (afters.contains(after))
                     throw new RuntimeException("testAfterAttribute() - not unique reference for {" + after +"}");
                 else
                     afters.add(after);
             }
         }
     }
     
     // i18n
     private Map<String,Map<String,String>> i18nLabels = new HashMap<String, Map<String,String>>();
 
     public Map<String, Map<String,String>> getI18nLabels() {
         return i18nLabels;
     }
     
     public void addI18nLabel(String lang, String descType, String label) {
         Map<String, String> map = new HashMap<String, String>();
         map.put(descType, label);
         i18nLabels.put(lang, map);
     }
     
 }
