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
 package org.sonar.plugins.technicaldebt;
 
 import org.sonar.api.Properties;
 import org.sonar.api.Property;
 import org.sonar.api.Plugin;
 import org.sonar.api.Extension;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Properties({
   @Property(key = TechnicalDebtPlugin.TD_DAILY_RATE, defaultValue = TechnicalDebtPlugin.TD_DAILY_RATE_DEFAULT, name = "Daily rate of a developer (in $)", description = ""),
  @Property(key = TechnicalDebtPlugin.TD_MAX_COMPLEXITY, defaultValue = TechnicalDebtPlugin.TD_MAX_COMPLEXITY_DEFAULT, name = "Maximum complexity above which component should be broken", description = ""),
   @Property(key = TechnicalDebtPlugin.TD_COST_COMP_CLASS, defaultValue = TechnicalDebtPlugin.TD_COST_COMP_CLASS_DEFAULT, name = "Average time to split a class that has a too high complexity (in hours)", description = ""),
   @Property(key = TechnicalDebtPlugin.TD_COST_COMP_METHOD, defaultValue = TechnicalDebtPlugin.TD_COST_COMP_METHOD_DEFAULT, name = "Average time to split a method that has a too high complexity (in hours)", description = ""),
   @Property(key = TechnicalDebtPlugin.TD_COST_DUPLI_BLOCK, defaultValue = TechnicalDebtPlugin.TD_COST_DUPLI_BLOCK_DEFAULT, name = "Average time to fix one block duplication block (in hours)", description = ""),
   @Property(key = TechnicalDebtPlugin.TD_COST_VIOLATION, defaultValue = TechnicalDebtPlugin.TD_COST_VIOLATION_DEFAULT, name = "Average time to fix a coding violation (in hours)", description = ""),
   @Property(key = TechnicalDebtPlugin.TD_COST_UNCOVERED_COMPLEXITY, defaultValue = TechnicalDebtPlugin.TD_COST_UNCOVERED_COMPLEXITY_DEFAULT, name = "Average time to cover complexity of one (in hours)", description = ""),
   @Property(key = TechnicalDebtPlugin.TD_COST_UNDOCUMENTED_API, defaultValue = TechnicalDebtPlugin.TD_COST_UNDOCUMENTED_API_DEFAULT, name = "Average time to document 1 API (in hours)", description = ""),
  @Property(key = TechnicalDebtPlugin.TD_COST_CYCLE, defaultValue = TechnicalDebtPlugin.TD_COST_CYCLE_DEFAULT, name = "Average time to cut a dependency between two files (in hours)", description = "")
 })
 
 /** {@inheritDoc} */
 public final class TechnicalDebtPlugin implements Plugin {
   public static final String TD_DAILY_RATE = "technicaldebt.daily.rate";
   public static final String TD_DAILY_RATE_DEFAULT = "500";
 
   public static final String TD_COST_COMP_CLASS = "technicaldebt.split.class";
   public static final String TD_COST_COMP_CLASS_DEFAULT = "8";
 
   public static final String TD_COST_COMP_METHOD = "technicaldebt.split.meth";
   public static final String TD_COST_COMP_METHOD_DEFAULT = "0.5";
 
   public static final String TD_COST_DUPLI_BLOCK = "technicaldebt.dupli.blocks";
   public static final String TD_COST_DUPLI_BLOCK_DEFAULT = "2";
 
   public static final String TD_COST_VIOLATION = "technicaldebt.violation";
   public static final String TD_COST_VIOLATION_DEFAULT = "0.1";
 
   public static final String TD_COST_UNCOVERED_COMPLEXITY = "technicaldebt.uncovered.complexity";
   public static final String TD_COST_UNCOVERED_COMPLEXITY_DEFAULT = "0.2";
 
   public static final String TD_COST_UNDOCUMENTED_API = "technicaldebt.undocumented.api";
   public static final String TD_COST_UNDOCUMENTED_API_DEFAULT = "0.2";
 
   public static final String TD_COST_CYCLE = "technicaldebt.cut.cycle";
   public static final String TD_COST_CYCLE_DEFAULT = "4";
 
   public static final String TD_MAX_COMPLEXITY = "technicaldebt.complexity.max";
   public static final String TD_MAX_COMPLEXITY_DEFAULT = "CLASS=60;METHOD=8";
 
 
   /**
    * {@inheritDoc}
    */
   public String getDescription() {
     return "Calculate the technical debt.";
   }
 
   /**
    * {@inheritDoc}
    */
   public List<Class<? extends Extension>> getExtensions() {
     List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();
     list.add(TechnicalDebtMetrics.class);
     list.add(TechnicalDebtDecorator.class);
     list.add(TechnicalDebtWidget.class);
     list.add(ComplexityDebtSensor.class);
     list.add(ComplexityDebtDecorator.class);
     return list;
   }
 
   /**
    * {@inheritDoc}
    */
   public String getKey() {
     return "technical-debt";
   }
 
   /**
    * {@inheritDoc}
    */
   public String getName() {
     return "Technical Debt";
   }
 }
