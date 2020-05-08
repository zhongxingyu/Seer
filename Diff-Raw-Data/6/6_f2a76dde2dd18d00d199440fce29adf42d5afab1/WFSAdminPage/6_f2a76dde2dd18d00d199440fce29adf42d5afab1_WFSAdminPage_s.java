 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.wfs.web;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.geoserver.web.services.BaseServiceAdminPage;
 import org.geoserver.wfs.GMLInfo;
 import org.geoserver.wfs.WFSInfo;
 import org.geoserver.wfs.GMLInfo.SrsNameStyle;
 
 @SuppressWarnings("serial")
 public class WFSAdminPage extends BaseServiceAdminPage<WFSInfo> {
 
     protected Class<WFSInfo> getServiceClass() {
         return WFSInfo.class;
     }
     
     protected void build(final IModel info, Form form) {
         //max features
         form.add( new TextField( "maxFeatures" ) );
         
         //service level
         RadioGroup sl = new RadioGroup( "serviceLevel" );
         form.add( sl );
         sl.add( new Radio( "basic", new Model( WFSInfo.ServiceLevel.BASIC ) ) );
         sl.add( new Radio( "transactional", new Model( WFSInfo.ServiceLevel.TRANSACTIONAL  ) ) );
         sl.add( new Radio( "complete", new Model( WFSInfo.ServiceLevel.COMPLETE ) ) );
         
         IModel gml2Model = new LoadableDetachableModel(){
             public Object load(){
                 return ((WFSInfo)info.getObject()).getGML().get(WFSInfo.Version.V_10);
             }
         };
 
         IModel gml3Model = new LoadableDetachableModel(){
             public Object load(){
                 return ((WFSInfo)info.getObject()).getGML().get(WFSInfo.Version.V_11);
             }
         };
 
         form.add(new GMLPanel("gml2", gml2Model));
         form.add(new GMLPanel("gml3", gml3Model));
     }
     
     static class GMLPanel extends Panel {
 
         public GMLPanel(String id, IModel gmlModel) { 
             super(id, new CompoundPropertyModel(gmlModel));
             
            //feature bounding
            CheckBox bounding = new CheckBox("featureBounding");
            add(bounding);
            
             //srsNameStyle
             List<GMLInfo.SrsNameStyle> choices = 
                 Arrays.asList(SrsNameStyle.values());
             DropDownChoice srsNameStyle = new DropDownChoice("srsNameStyle", choices);
             add(srsNameStyle);
         }
         
     }
 
     protected String getServiceName(){
        return "WFS";
     }
 }
