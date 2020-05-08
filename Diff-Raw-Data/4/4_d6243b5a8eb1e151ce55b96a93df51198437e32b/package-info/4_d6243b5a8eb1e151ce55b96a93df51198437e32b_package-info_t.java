 /*
  * Copyright (C) 2011-2014 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 /**
  * Provides eXo acceptance front-end (using Juzu).
  *
  * @since 2.0
  */
 
 // Application
 
 @Application(
     defaultController = HomeController.class,
     name = "AcceptanceApplication")
 
 // This is a webapp
 @Servlet("/")
 
 // Injection Bindings
 @Bindings(
     {
         @Binding(value = CurrentUser.class, scope = Scope.SESSION),
         @Binding(value = Flash.class, scope = Scope.FLASH),
         @Binding(value = Context.class, scope = Scope.REQUEST)
     })
 
 
 @WebJars(
     {
         @WebJar(value = "angular-ui-bootstrap"),
         @WebJar(value = "angularjs"),
         @WebJar(value = "bootstrap"),
         @WebJar(value = "font-awesome"),
         @WebJar(value = "jquery", version = "2.0.3"),
         @WebJar(value = "lodash", version = "2.4.1"),
         @WebJar(value = "restangular")
     })
 
 // Declare assets
 @Assets(
     {
         @Asset(id = "pace.js", value = "external/pace/0_4_16/pace.min.js"),
         @Asset(id = "pace.css", value = "external/pace/0_4_16/pace.css"),
         @Asset(id = "bootstrap.css", value = "bootstrap/3.0.3/css/bootstrap.min.css"),
         @Asset(id = "font-awesome.css", value = "font-awesome/4.0.3/css/font-awesome.min.css"),
         @Asset(id = "jquery.js", value = "jquery/2.0.3/jquery.min.js"),
        @Asset(id = "angular.js", value = "angularjs/1.2.9/angular.min.js"),
        @Asset(id = "angular-route.js", value = "angularjs/1.2.9/angular-route.min.js"),
         @Asset(id = "lodash.js", value = "lodash/2.4.1/lodash.min.js"),
         @Asset(id = "bootstrap.js", value = "bootstrap/3.0.3/js/bootstrap.min.js",
                depends = {"jquery.js"}),
         @Asset(id = "restangular.js", value = "restangular/1.2.2/restangular.min.js",
                depends = {"angular.js", "angular-route.js", "lodash.js"}),
         @Asset(id = "ui-bootstrap.js", value = "angular-ui-bootstrap/0.9.0/ui-bootstrap-tpls.min.js",
                depends = {"angular.js", "bootstrap.js"}),
         @Asset(value = "acceptance.js",
                depends = {"bootstrap.js"}),
         @Asset(value = "sources.js",
                depends = {"ui-bootstrap.js"}),
         @Asset(value = "acceptance.css",
                depends = {"bootstrap.css", "font-awesome.css"}),
         @Asset(value = "administration/credential/index.js",
                depends = {"restangular.js", "ui-bootstrap.js"}),
         @Asset(value = "administration/vcs/repository.js",
                depends = {"restangular.js", "ui-bootstrap.js"})
     })
 
 // Always use these assets
 @WithAssets({"acceptance.js", "acceptance.css", "pace.js", "pace.css"})
 
 // Custom tags
 @Tags({
           @Tag(name = "adminMenu", path = "adminMenu.gtmpl")
       }) package org.exoplatform.acceptance.ui;
 
 import org.exoplatform.acceptance.ui.controllers.HomeController;
 import org.exoplatform.acceptance.ui.model.Context;
 import org.exoplatform.acceptance.ui.model.CurrentUser;
 import org.exoplatform.acceptance.ui.model.Flash;
 
 import juzu.Application;
 import juzu.Scope;
 import juzu.plugin.asset.Asset;
 import juzu.plugin.asset.Assets;
 import juzu.plugin.asset.WithAssets;
 import juzu.plugin.binding.Binding;
 import juzu.plugin.binding.Bindings;
 import juzu.plugin.servlet.Servlet;
 import juzu.plugin.webjars.WebJar;
 import juzu.plugin.webjars.WebJars;
 import juzu.template.Tag;
 import juzu.template.Tags;
