 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.qi;
 
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multiset;
 import org.apache.commons.configuration.Configuration;
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.MeasureUtils;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.rules.RulePriority;
 import org.sonar.api.rules.Violation;
 import org.sonar.api.utils.KeyValueFormat;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * An abstract class that should be implemented to add a violation QI axis
  */
 public abstract class AbstractViolationsDecorator extends AbstractDecorator {
 
   /**
    * Creates an AbstractViolationsDecorator
    *
    * @param configuration     the config
    * @param metric            the metric that should be used for decoration
    * @param axisWeight        the axis weight key
    * @param defaultAxisWeight the axis weight default value
    */
   public AbstractViolationsDecorator(Configuration configuration, Metric metric,
                                      String axisWeight, String defaultAxisWeight) {
     super(configuration, metric, axisWeight, defaultAxisWeight);
   }
 
   /**
    * @return the key to retrieve the weights by rule priority
    */
   public abstract String getConfigurationKey();
 
   /**
    * @return the key to retrieve the defaults weights by rule priority
    */
   public abstract String getDefaultConfigurationKey();
 
   /**
    * @return the metric the weighted violations should be stored under
    */
   public abstract Metric getWeightedViolationMetricKey();
 
   /**
    * @return the plugin key for which filter the violations
    */
   public abstract String getPluginKey();
 
   @Override
   public List<Metric> dependsUpon() {
     return Lists.newArrayList(CoreMetrics.VIOLATIONS);
   }
 
   /**
    * Standard implementation of the decorate method for violations axes
    *
    * @param resource the resource
    * @param context  the context
    */
   public void decorate(Resource resource, DecoratorContext context) {
     Multiset<RulePriority> violations = countViolationsBySeverity(context);
     Map<RulePriority, Integer> weights = getWeightsByPriority();
 
     double weightedViolations = getWeightedViolations(weights, violations, context);
     saveMeasure(context, weightedViolations / getValidLines(context));
     saveWeightedViolations(context, weightedViolations);
   }
 
   /**
    * Counts the number of violation by priority
    *
    * @param context the context
    * @return a multiset of priority count
    */
   protected Multiset<RulePriority> countViolationsBySeverity(DecoratorContext context) {
     List<Violation> violations = context.getViolations();
     Multiset<RulePriority> violationsBySeverity = HashMultiset.create();
 
     for (Violation violation : violations) {
       if (violation.getRule().getPluginName().equals(getPluginKey())) {
         violationsBySeverity.add(violation.getSeverity());
       }
     }
     return violationsBySeverity;
   }
 
   /**
    * Calculates the weighted violations
    *
    * @param weights    the weights to be used
    * @param violations the violations
    * @param context    the context
    * @return the crossed sum at the level + the sum of children
    */
   protected double getWeightedViolations(Map<RulePriority, Integer> weights, Multiset<RulePriority> violations, DecoratorContext context) {
     double weightedViolations = 0.0;
    for (Map.Entry<RulePriority, Integer> entry : weights.entrySet()) {
      weightedViolations += entry.getValue() * violations.count(entry.getKey());
     }
     for (DecoratorContext childContext : context.getChildren()) {
       weightedViolations += MeasureUtils.getValue(childContext.getMeasure(getWeightedViolationMetricKey()), 0.0);
     }
     return weightedViolations;
   }
 
   /**
    * @return the weights by priority
    */
   protected Map<RulePriority, Integer> getWeightsByPriority() {
     String property = configuration.getString(getConfigurationKey(), getDefaultConfigurationKey());
 
     return KeyValueFormat.parse(property, new KeyValueFormat.RulePriorityNumbersPairTransformer());
   }
 
   /**
    * Used to save the weighted violations
    *
    * @param context the context
    * @param value   the value
    */
   protected void saveWeightedViolations(DecoratorContext context, double value) {
     if (Utils.shouldSaveMeasure(context.getResource())) {
       context.saveMeasure(getWeightedViolationMetricKey(), value);
     }
   }
 }
