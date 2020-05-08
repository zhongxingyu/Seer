 /*
  * Sonar C-Rules Plugin
  * Copyright (C) 2010 SonarSource
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.c.checks;
 
 import java.util.Collection;
 import java.util.List;
 
 import com.google.common.collect.Lists;
 import com.sonarsource.c.plugin.CCheck;
 import com.sonarsource.c.plugin.CCheckRepository;
 
 /**
  * This class is declared in the manifest and used by the C Plugin to find out which checks do exist in this module. 
  * 
  * <br>
  * <b>
  * Note: This mechanism should not be used anymore once it is possible to group plugins classloaders,
  * and this class should then be removed.
  * </b>
  * 
  * @deprecated
  */
 public class CheckRepository implements CCheckRepository {
 
   public Collection<Class<? extends CCheck>> getCheckClasses() {
     List<Class<? extends CCheck>> checks = Lists.newArrayList();
     checks.add(ParsingErrorCheck.class);
     checks.add(FunctionComplexityCheck.class);
     checks.add(EmptyBlockCheck.class);
     checks.add(SwitchStatementWithoutDefaultCheck.class);
     checks.add(NestedIfDepthCheck.class);
     checks.add(BooleanExpressionComplexityCheck.class);
     checks.add(FunctionLocCheck.class);
     checks.add(FileLocCheck.class);
     checks.add(IfStatementWithoutBracesCheck.class);
     checks.add(WhileLoopWithoutBracesCheck.class);
     checks.add(ForLoopWithoutBracesCheck.class);
     checks.add(CollapsibleIfStatementsCheck.class);
     checks.add(ExcessiveParameterListCheck.class);
     checks.add(FunctionNameCheck.class);
     checks.add(FileNameCheck.class);
     return checks;
   }
 }
