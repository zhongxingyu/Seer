 /*
  *  Copyright 2011 Yannick LOTH.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.googlecode.wicketelements.components.menu;
 
 import com.googlecode.wicketelements.library.behavior.AttributeModifierFactory;
 import org.apache.wicket.Page;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.StringResourceModel;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Yannick LOTH
  */
 public class BreadcrumbPanel extends Panel {
 
     private static final long serialVersionUID = 1L;
     private final PageTreeModel pageTreeModel;
     private ListView<Class<? extends Page>> lv;
 
     public BreadcrumbPanel(final String id) {
         super(id);
         pageTreeModel = new PageTreeModel();
     }
 
     private List<Class<? extends Page>> buildBreadcrumbPageList() {
         final List<Class<? extends Page>> list = new ArrayList<Class<? extends Page>>();
         final Class<? extends Page> pageClass = getPage().getClass();
         final PageTree modelObject = pageTreeModel.getObject();
         final PageTree currentPageTree = modelObject.getPageTree(pageClass);
         if (currentPageTree != null) {
             PageTree currPageTree = currentPageTree;
             while (!currPageTree.isRoot()) {
                 if (currPageTree.getPageClass() != null) {
                     list.add(currPageTree.getPageClass());
                 }
                 currPageTree = currPageTree.getParent();
             }
             if (currPageTree.getPageClass() != null) {
                 list.add(currPageTree.getPageClass());
             }
         } else {
             list.add(getPage().getClass());
         }
         Collections.reverse(list);
         return list;
     }
 
     @Override
     protected void onBeforeRender() {
         super.onBeforeRender();
         if (lv == null) {
             lv = new ListView<Class<? extends Page>>("breadcrumbs",
                     buildBreadcrumbPageList()) {
 
                 private static final long serialVersionUID = 1L;
 
                 @Override
                 protected void populateItem(
                         final ListItem<Class<? extends Page>> item) {
                     final Class<? extends Page> pageClass = item
                             .getModelObject();
                     {
                         final Link<Class<Page>> link = new BookmarkablePageLink<Class<Page>>(
                                 "breadcrumbLink", pageClass) {
 
                             private static final long serialVersionUID = 1L;
 
                             @Override
                             protected void onBeforeRender() {
                                 super.onBeforeRender();
                                 if (pageClass.equals(getPage().getClass())) {
                                     onPageLink(this);
                                     onCurrentPageLink(this);
                                 } else {
                                     onPageLink(this);
                                 }
                             }
                         };
                         {
                             final Label languageLabel = new Label(
                                     "breadcrumbLabel", new LoadableDetachableModel<String>() {
                                 @Override
                                 protected String load() {
                                    return new StringResourceModel(pageClass.getCanonicalName(), BreadcrumbPanel.this.getPage(), null, (Object) null).getString();
                                 }
                             });
                             languageLabel.setRenderBodyOnly(true);
                             link.add(languageLabel);
                         }
                         link.add(AttributeModifierFactory.newAttributeAppenderForTitle(new LoadableDetachableModel<String>() {
                             @Override
                             protected String load() {
                                return new StringResourceModel(pageClass.getCanonicalName(), BreadcrumbPanel.this.getPage(), null, (Object) null).getString();
                             }
                         }));
                         item.add(link);
                     }
                     item.setOutputMarkupId(true);
                 }
             };
             lv.setRenderBodyOnly(true);
             add(lv);
         }
 
         lv.setList(buildBreadcrumbPageList());
     }
 
     /**
      * This method is executed when the link to the current locale is added to
      * the page. It may be useful, for example, to disable the link, as the
      * locale is already selected, or to add some attribute to the tag.
      *
      * @param pageLink The link for the current page.
      */
     protected void onCurrentPageLink(final Link<Class<Page>> pageLink) {
     }
 
     /**
      * This method is executed when the link to a locale is added to the page.
      *
      * @param pageLink The link for the specific page.
      */
     protected void onPageLink(final Link<Class<Page>> pageLink) {
     }
 }
