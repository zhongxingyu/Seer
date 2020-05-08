 package mx.itesm.mexadl.metrics;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import mx.itesm.mexadl.MexAdlProcessor;
 import mx.itesm.mexadl.util.Util;
 
 import org.apache.velocity.Template;
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.xpath.XPath;
 
 /**
  * The MetricsProcessor class generates an aspect that introduces the
  * implementation of the mx.itesm.mexadl.metrics.MaintainabilityMetrics
  * interface, to specially marked components in an xADL architecture definition.
  * 
  * @author jccastrejon
  * 
  */
 public class MetricsProcessor implements MexAdlProcessor {
 
     /**
      * XPath expression to identify the MexADL quality metrics.
      */
     private static XPath metricsPath;
 
     /**
      * AspectJ template that defines the quality metrics to be measured in the
      * components of a system's architecture.
      */
     private static Template metricsAspectTemplate;
 
     static {
         try {
             MetricsProcessor.metricsAspectTemplate = Util.getVelocityTemplate(MetricsProcessor.class);
             MetricsProcessor.metricsPath = XPath.newInstance("//mexadl:maintainabilityMetrics");
         } catch (Exception e) {
             System.out.println("Error loading MetricsProcessor");
             e.printStackTrace();
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void processDocument(Document document, String xArchFilePath) throws JDOMException, IOException {
         MetricsData metricsData;
         Map<String, Object> metric;
         Map<String, Object> metricSet;
         Map<String, Object> definition;
         Map<String, Object> properties;
         List<Element> metricDefinitions;
         List<Map<String, Object>> metrics;
         List<Map<String, Object>> metricSets;
         List<Map<String, Object>> definitionsList;
 
         metricDefinitions = MetricsProcessor.metricsPath.selectNodes(document);
         if ((metricDefinitions != null) && (!metricDefinitions.isEmpty())) {
             definitionsList = new ArrayList<Map<String, Object>>();
             for (Element metricDefinition : metricDefinitions) {
                 metricsData = this.getMetricsData(document, metricDefinition);
                 definition = new HashMap<String, Object>();
                 definitionsList.add(definition);
 
                 metricSets = new ArrayList<Map<String, Object>>();
                 definition.put("type", metricsData.getType());
                 definition.put("metricSets", metricSets);
                 for (MetricsData metricSetDefinition : metricsData.getMetrics()) {
                     metricSet = new HashMap<String, Object>();
                     metricSets.add(metricSet);
 
                     metrics = new ArrayList<Map<String, Object>>();
                     metricSet.put("name", metricSetDefinition.getType());
                    metricSet.put("type", metricSetDefinition.getType()); // FIXME
                     metricSet.put("metrics", metrics);
                     for (String metricName : metricSetDefinition.getData().keySet()) {
                         metric = new HashMap<String, Object>();
                         metrics.add(metric);
 
                         metric.put("name", metricName);
                         metric.put("value", metricSetDefinition.getData().get(metricName));
                     }
                 }
 
             }
 
             // Create the interactions aspect
             properties = new HashMap<String, Object>();
             properties.put("definitionsList", definitionsList);
             Util.createAspectFile(document, xArchFilePath, MetricsProcessor.metricsAspectTemplate, properties,
                     "metrics");
         }
     }
 
     /**
      * Get the quality metrics associated the the specified element.
      * 
      * @param element
      * @throws JDOMException
      */
     @SuppressWarnings("unchecked")
     private MetricsData getMetricsData(final Document document, final Element element) throws JDOMException {
         MetricsData metricSet;
         MetricsData returnValue;
         List<MetricsData> metricSets;
 
         returnValue = null;
         if (element != null) {
             returnValue = new MetricsData();
             metricSets = new ArrayList<MetricsData>();
             returnValue.setType(Util.getLinkImplementationClass(document, Util.getIdValue(element.getParentElement())));
             returnValue.setMetrics(metricSets);
 
             for (Element child : (List<Element>) element.getChildren()) {
                 if (child.getNamespace() == Util.MEXADL_NAMESPACE) {
                     metricSet = new MetricsData();
                     metricSets.add(metricSet);
 
                    metricSet.setType(Util.getXsiType(child));
                     metricSet.setData(new HashMap<String, String>());
                     for (Attribute attribute : (List<Attribute>) child.getAttributes()) {
                         if (attribute.getNamespace() == Util.MEXADL_NAMESPACE) {
                             metricSet.getData().put(attribute.getName(), attribute.getValue());
                         }
                     }
                 }
             }
         }
 
         return returnValue;
     }
 }
