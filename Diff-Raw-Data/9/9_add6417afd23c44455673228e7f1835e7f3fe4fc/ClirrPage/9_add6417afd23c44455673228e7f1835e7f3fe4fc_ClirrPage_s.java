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
 package org.sonar.plugins.clirr;
 
 import org.sonar.api.resources.Resource;
 import org.sonar.api.web.*;
 import org.sonar.plugins.clirr.client.GwtClirrPage;
 
 @NavigationSection(NavigationSection.RESOURCE)
 @UserRole(UserRole.USER)
 @ResourceQualifier({Resource.QUALIFIER_PROJECT,Resource.QUALIFIER_MODULE,Resource.QUALIFIER_PACKAGE})
 public class ClirrPage extends AbstractRubyTemplate implements RubyRailsPage {
 
 
   public String getId() {
     return "clirr";
   }
 
   public String getTitle() {
     return "API Changes";
   }
 
   @Override
   protected String getTemplatePath() {
    return "/Users/SimonBrandhof/projects/sonar-plugins/trunk/clirr/src/main/resources/clirr_page.html.erb";
   }
 }
 
 
