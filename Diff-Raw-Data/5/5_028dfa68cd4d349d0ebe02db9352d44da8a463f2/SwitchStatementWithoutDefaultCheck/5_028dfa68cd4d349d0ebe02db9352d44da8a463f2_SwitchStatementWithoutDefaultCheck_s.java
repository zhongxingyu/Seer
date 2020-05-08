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
 
 import org.sonar.check.BelongsToProfile;
 import org.sonar.check.IsoCategory;
 import org.sonar.check.Priority;
 import org.sonar.check.Rule;
 
 import com.sonar.c.api.CKeyword;
 import com.sonar.sslr.api.AstNode;
 import com.sonarsource.c.plugin.CCheck;
 
@Rule(key = "C.SwitchStatementWithoutDefault", name = "Avoid swith statement without a \"default\" clause.",
     isoCategory = IsoCategory.Reliability, priority = Priority.MAJOR,
     description = "<p>It's usually a good idea to introduce a default case in every switch statement. "
         + "Even if the developer is sure that all currently possible cases are covered, this should be expressed in the default branch. "
         + "This way the code is protected aginst later changes, e.g. introduction of new types.</p>")
 @BelongsToProfile(title = CChecksConstants.SONAR_C_WAY_PROFILE_KEY, priority = Priority.MAJOR)
 public class SwitchStatementWithoutDefaultCheck extends CCheck {
 
   @Override
   public void init() {
     subscribeTo(getCGrammar().switchStatement);
   }
 
   public void visitNode(AstNode node) {
     if ( !node.hasChildren(CKeyword.DEFAULT)) {
      log("Avoid swith statement without a \"default\" clause.", node);
     }
   }
 
 }
