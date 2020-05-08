 /*
  * Copyright 2009 Andrew Pietsch 
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you 
  * may not use this file except in compliance with the License. You may 
  * obtain a copy of the License at 
  *      
  *      http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing permissions 
  * and limitations under the License. 
  */
 
 package com.pietschy.gwt.pectin.demo.client.misc;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.*;
 import com.pietschy.gwt.pectin.client.FormModel;
 
 /**
  * Created by IntelliJ IDEA.
  * User: andrew
  * Date: Aug 10, 2009
  * Time: 6:13:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AbstractDemo extends Composite
 {
    private DockPanel dock = new DockPanel();
    private FlowPanel contentPanel = new FlowPanel();
    private FlowPanel blurbPanel = new FlowPanel();
    private FlowPanel footerPanel = new FlowPanel();
 
    public AbstractDemo()
    {
       dock.setWidth("100%");
       dock.add(blurbPanel, DockPanel.NORTH);
       dock.add(contentPanel, DockPanel.CENTER);
       dock.add(footerPanel, DockPanel.SOUTH);
 
       contentPanel.setStylePrimaryName("AbstractDemo-Content");
       blurbPanel.setStylePrimaryName("AbstractDemo-Blurb");
       footerPanel.setStylePrimaryName("AbstractDemo-Footer");
 
       initWidget(dock);
    }
 
    private void showSource(Class<?> clazz)
    {
       String url = "http://code.google.com/p/gwt-pectin/source/browse/trunk/demo/src/main/java/" +
                    clazz.getName().replace('.', '/') + ".java";
       
       Window.open(url, "PectinSource", "");
    }
 
    protected void setMainContent(Widget form)
    {
       contentPanel.clear();
       contentPanel.add(form);
    }
 
 //   protected void addAside(Widget aside)
 //   {
 //      asidePanel.add(aside);
 //   }
 
    protected void addBlurbParagraph(String blurb)
    {
       blurbPanel.add(new HTML("<p>" + blurb + "</p>"));
    }
 
 
    protected void addLinkToModel(FormModel model)
    {
       addLinkToModel(model.getClass());
    }
 
   protected void addLinkToModel(Class<?> clazz)
    {
      addLinkToSource("Show Model Source", clazz);
    }
 
    protected void addLinkToView(Widget view)
    {
       addLinkToView(view.getClass());
    }
 
    protected void addLinkToView(Class<?> clazz)
    {
       addLinkToSource("Show View Source", clazz);
    }
 
    protected void addLinkToSource(String linkName, final Class<?> clazz)
    {
       Anchor link = new Anchor(linkName);
       link.addClickHandler(new ClickHandler()
       {
          public void onClick(ClickEvent event)
          {
             showSource(clazz);
          }
       });
       
       footerPanel.add(link);
    }
 
 }
