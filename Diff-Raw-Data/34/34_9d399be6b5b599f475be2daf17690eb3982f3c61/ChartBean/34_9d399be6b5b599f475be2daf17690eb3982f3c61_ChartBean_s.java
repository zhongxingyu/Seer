 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.samples.showcase.example.compat.chart;
 
 import java.io.Serializable;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.event.ActionEvent;
 
 import com.icesoft.faces.component.outputchart.OutputChart;
 
 import org.icefaces.samples.showcase.metadata.annotation.ComponentExample;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResource;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResources;
 import org.icefaces.samples.showcase.metadata.annotation.Menu;
 import org.icefaces.samples.showcase.metadata.annotation.MenuLink;
 import org.icefaces.samples.showcase.metadata.annotation.ResourceType;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 @ComponentExample(
         title = "example.compat.chart.title",
         description = "example.compat.chart.description",
         example = "/resources/examples/compat/chart/chart.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="chart.xhtml",
                     resource = "/resources/examples/compat/"+
                                "chart/chart.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="ChartBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/"+
                                "showcase/example/compat/chart/ChartBean.java")
         }
 )
 @Menu(
 	title = "menu.compat.chart.subMenu.title",
 	menuLinks = {
             @MenuLink(title = "menu.compat.chart.subMenu.main",
                     isDefault = true,
                     exampleBeanName = ChartBean.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.area",
                     exampleBeanName = ChartArea.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.bar",
                     exampleBeanName = ChartBar.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.areaStacked",
                     exampleBeanName = ChartAreaStacked.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.barStacked",
                     exampleBeanName = ChartBarStacked.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.barClustered",
                     exampleBeanName = ChartBarClustered.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.line",
                     exampleBeanName = ChartLine.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.point",
                     exampleBeanName = ChartPoint.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.pie2d",
                     exampleBeanName = ChartPie2d.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.pie3d",
                     exampleBeanName = ChartPie3d.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.custom",
                     exampleBeanName = ChartCustom.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.options",
                     exampleBeanName = ChartOptions.BEAN_NAME),
             @MenuLink(title = "menu.compat.chart.subMenu.dynamic",
                     exampleBeanName = ChartDynamic.BEAN_NAME)            
 })
 @ManagedBean(name= ChartBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class ChartBean extends ComponentExampleImpl<ChartBean> implements Serializable {
 	
 	public static final String BEAN_NAME = "chart";
 	
 	private String clickedStatus = "No chart has been clicked yet.";
 	
 	public ChartBean() {
 		super(ChartBean.class);
 	}
 	
     @PostConstruct
     public void initMetaData() {
         super.initMetaData();
     }
 
 	public String getClickedStatus() { return clickedStatus; }
 	
 	public void setClickedStatus(String clickedStatus) { this.clickedStatus = clickedStatus; }
 	
 	public void chartClicked(ActionEvent event) {
         if (event.getSource() instanceof OutputChart) {
             OutputChart chart = (OutputChart)event.getSource();
             if (chart.getClickedImageMapArea().getXAxisLabel() != null) {
                 StringBuffer sb = new StringBuffer(65);
                 sb.append(chart.getChartTitle());
                sb.append(" clicked with a value of ");
                 sb.append(chart.getClickedImageMapArea().getValue());
                sb.append(" along axis of ");
                 sb.append(chart.getClickedImageMapArea().getXAxisLabel());
                sb.append(" and legend of ");
                sb.append(chart.getClickedImageMapArea().getLengendLabel());
                 sb.append(".");
                 
                 clickedStatus = sb.toString();
             }
 
         }
 	}
 }
