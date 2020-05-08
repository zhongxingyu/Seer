 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource SA
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
 package org.sonar.plugins.taglist;
 
 
 import org.sonar.api.batch.Decorator;
 import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependsUpon;
 import org.sonar.api.measures.*;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.Java;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.resources.ResourceUtils;
 import org.sonar.api.rules.ActiveRule;
 import org.sonar.api.rules.Rule;
 import org.sonar.api.rules.RulesManager;
 import org.sonar.api.rules.Violation;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class TaglistDistributionDecorator implements Decorator {
 
   private RulesManager rulesManager;
   private RulesProfile rulesProfile;
 
   public TaglistDistributionDecorator(RulesManager rulesManager, RulesProfile rulesProfile) {
     this.rulesManager = rulesManager;
     this.rulesProfile = rulesProfile;
   }
 
  @DependsUpon
   public List<Metric> generatesMetrics() {
     return Arrays.asList(TaglistMetrics.TAGS_DISTRIBUTION);
   }
 
   /**
    * {@inheritDoc}
    */
   public boolean shouldExecuteOnProject(Project project) {
     return project.getLanguage().equals(Java.INSTANCE);
   }
 
   /**
    * {@inheritDoc}
    */
   public void decorate(Resource resource, DecoratorContext context) {
 
     // Calculate distribution on classes, but keep it in memory, not in DB
     if (ResourceUtils.isFile(resource)) {
       context.saveMeasure(computeDistribution(context).build().setPersistenceMode(PersistenceMode.MEMORY));
     } else {
       // Otherwise, aggregate the distribution
       CountDistributionBuilder builder = new CountDistributionBuilder(TaglistMetrics.TAGS_DISTRIBUTION);
 
       // At modules and project levels, simply aggregate distribution of children
       for (Measure childMeasure : context.getChildrenMeasures(TaglistMetrics.TAGS_DISTRIBUTION)) {
         builder.add(childMeasure);
       }
 
       if (!builder.isEmpty()) {
         context.saveMeasure(builder.build());
       }
     }
   }
 
   // This method should disappear after the rule management API gets refactored
   private PropertiesBuilder<String, Integer> computeDistribution(DecoratorContext context) {
     PropertiesBuilder<String, Integer> tagsDistrib = new PropertiesBuilder<String, Integer>(TaglistMetrics.TAGS_DISTRIBUTION);
     for (Rule rule : rulesManager.getPluginRules(TaglistPlugin.KEY)) {
       ActiveRule activeRule = rulesProfile.getActiveRule(TaglistPlugin.KEY, rule.getKey());
       int violationsForTag = 0;
       if (activeRule != null) {
         for (Violation violation : context.getViolations()) {
           if (violation.getRule().equals(activeRule.getRule())) {
             violationsForTag++;
           }
         }
       }
       tagsDistrib.add(rule.getKey(), violationsForTag);
     }
     return tagsDistrib;
   }
 
 
 }
