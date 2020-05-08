 package com.bibounde.vprotovisdemo.linechart;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.bibounde.vprotovis.LineChartComponent;
 import com.bibounde.vprotovis.chart.line.DefaultLineTooltipFormatter;
 import com.bibounde.vprotovis.chart.line.InterpolationMode;
 import com.bibounde.vprotovis.chart.line.LineTooltipFormatter;
 import com.bibounde.vprotovis.chart.line.Serie;
 import com.bibounde.vprotovis.common.AxisLabelFormatter;
 import com.bibounde.vprotovis.common.Point;
 import com.bibounde.vprotovisdemo.Page;
 import com.bibounde.vprotovisdemo.dialog.CodeDialog;
 import com.bibounde.vprotovisdemo.util.RandomUtil;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.VerticalSplitPanel;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Window.Notification;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class LineChartPage implements Page {
 
     public static final String FQN = "LineChart";
     
     private static final String TAB_DIMENSIONS = "Dimensions";
     private static final String TAB_AXIS = "Grid";
     private static final String TAB_DATA = "Data";
     private static final String TAB_MISC = "Misc";
     
     private VerticalSplitPanel content;
     private ChartPanel chartPanel;
     private DataPanel dataPanel;
     private AxisPanel axisPanel;
     private DimensionPanel dimensionPanel;
     private MiscPanel miscPanel;
     
     private TabSheet tabSheet;
     private Map<String, Object> sourceCodeMap = new HashMap<String, Object>();
     
     public LineChartPage() {
         this.initLayout();
         this.initListener();
         this.renderChart(false);
     }
     
     private void initLayout() {
         this.content = new VerticalSplitPanel();
         this.content.setSplitPosition(40);
         
         this.tabSheet = new TabSheet();
         tabSheet.setSizeFull();
         this.content.addComponent(tabSheet);
         
         this.dimensionPanel = new DimensionPanel();
         tabSheet.addTab(this.dimensionPanel.getComponent(), TAB_DIMENSIONS, new ThemeResource("wrench.png"));
         
         this.dataPanel = new DataPanel();
         tabSheet.addTab(this.dataPanel.getComponent(), TAB_DATA, new ThemeResource("table.png"));
         
         this.axisPanel = new AxisPanel();
         tabSheet.addTab(this.axisPanel.getComponent(), TAB_AXIS, new ThemeResource("shape_align_middle.png"));
         
         this.miscPanel = new MiscPanel();
         tabSheet.addTab(this.miscPanel.getComponent(), TAB_MISC, new ThemeResource("palette.png"));
         
         this.chartPanel = new ChartPanel();
         this.content.addComponent(this.chartPanel.getComponent());
     }
     
     private void initListener() {
         this.chartPanel.getRenderButton().addListener(new ClickListener() {
             
             public void buttonClick(ClickEvent event) {
                 renderChart(true);
             }
         });
         
         this.chartPanel.getSourceButton().addListener(new ClickListener() {
             
             public void buttonClick(ClickEvent event) {
                 try {
                     Configuration configuration = new Configuration();
                     configuration.setClassForTemplateLoading(getClass(), "/templates/");
                     Template tpl = configuration.getTemplate("LineChartComponentCode.ftl");
                     StringWriter sWriter = new StringWriter();
                     
                     tpl.process(sourceCodeMap, sWriter);
                     CodeDialog codeDialog = new CodeDialog(sWriter.toString());
                     content.getWindow().addWindow(codeDialog);
                     codeDialog.center();
                     
                 } catch (IOException e) {
                     content.getWindow().showNotification("Configuration error", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                 } catch (TemplateException e) {
                     content.getWindow().showNotification("Template error", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                 }
             }
         });
     }
     
     private void renderChart(boolean validate) {
         
         if (validate) {
             if (!this.dimensionPanel.validate()) {
                 this.content.getWindow().showNotification("Unable to render chart", "Dimension values are invalid.", Notification.TYPE_ERROR_MESSAGE);
                 this.tabSheet.setSelectedTab(this.dimensionPanel.getComponent());
                 return;
             } else if (!this.axisPanel.validate()) {
                 this.content.getWindow().showNotification("Unable to render chart", "Axis values are invalid.", Notification.TYPE_ERROR_MESSAGE);
                 this.tabSheet.setSelectedTab(this.axisPanel.getComponent());
                 return;
             } else if (!this.miscPanel.validate()) {
                 this.content.getWindow().showNotification("Unable to render chart", "Misc values are invalid.", Notification.TYPE_ERROR_MESSAGE);
                 this.tabSheet.setSelectedTab(this.miscPanel.getComponent());
                 return;
             }
         }
         
         List<Serie> series = this.dataPanel.getSeries();
         
         LineChartComponent chart = this.chartPanel.getChart();
         this.sourceCodeMap.clear();
         
         chart.clearSeries();
         for (Serie serie : series) {
             chart.addSerie(serie.getName(), serie.getValues());
         }
         this.sourceCodeMap.put("series", series);
         
         chart.setChartWidth(this.dimensionPanel.getChartWidth());
         chart.setChartHeight(this.dimensionPanel.getChartHeight());
         
         this.sourceCodeMap.put("chartWidth", this.dimensionPanel.getChartWidth());
         this.sourceCodeMap.put("chartHeight", this.dimensionPanel.getChartHeight());
         
         Double marginLeft = this.dimensionPanel.getMarginLeft();
         if (marginLeft != null) {
             chart.setMarginLeft(marginLeft);
             this.sourceCodeMap.put("marginLeft", marginLeft);
         } else {
             chart.setMarginLeft(10d);
         }
         
         Double marginRight = this.dimensionPanel.getMarginRight();
         if (marginRight != null) {
             chart.setMarginRight(marginRight);
             this.sourceCodeMap.put("marginRight", marginRight);
         } else {
             chart.setMarginRight(10d);
         }
         
         Double marginTop = this.dimensionPanel.getMarginTop();
         if (marginTop != null) {
             chart.setMarginTop(marginTop);
             this.sourceCodeMap.put("marginTop", marginTop);
         } else {
             chart.setMarginTop(10d);
         }
         
         Double marginBottom = this.dimensionPanel.getMarginBottom();
         if (marginBottom != null) {
             chart.setMarginBottom(marginBottom);
             this.sourceCodeMap.put("marginBottom", marginBottom);
         } else {
             chart.setMarginBottom(10d);
         }
         
         Integer lineWidth = this.dimensionPanel.getLineWidth();
         if (lineWidth != null) {
             chart.setLineWidth(lineWidth);
             this.sourceCodeMap.put("lineWidth", lineWidth);
         } else {
             chart.setLineWidth(1);
         }
         
         chart.setXAxisVisible(this.axisPanel.isXAxisEnabled());
         this.sourceCodeMap.put("xAxisVisible", this.axisPanel.isXAxisEnabled());
         
         chart.setXAxisLabelVisible(this.axisPanel.isXAxisLabelEnabled());
         this.sourceCodeMap.put("xAxisLabelVisible", this.axisPanel.isXAxisLabelEnabled());
         
         chart.setXAxisLabelStep(this.axisPanel.getXAxisLabelStep());
         this.sourceCodeMap.put("xAxisLabelStep", this.axisPanel.getXAxisLabelStep());
         
         chart.setXAxisGridVisible(this.axisPanel.isXAxisGridEnabled());
         this.sourceCodeMap.put("xAxisGridVisible", this.axisPanel.isXAxisGridEnabled());
 
         if (this.axisPanel.isXAxisCustomFormatter()) {
             chart.setXAxisLabelFormatter(new AxisLabelFormatter() {
                 public String format(double labelValue) {
                     return String.valueOf(labelValue) + "j.";
                 }
             });
         } else {
             chart.setXAxisLabelFormatter(null);
         }
         this.sourceCodeMap.put("xAxisCustomFormatter", this.axisPanel.isXAxisCustomFormatter());
         
         chart.setYAxisVisible(this.axisPanel.isYAxisEnabled());
         this.sourceCodeMap.put("yAxisVisible", this.axisPanel.isYAxisEnabled());
         
         chart.setYAxisLabelVisible(this.axisPanel.isYAxisLabelEnabled());
         this.sourceCodeMap.put("yAxisLabelVisible", this.axisPanel.isYAxisLabelEnabled());
         
         chart.setYAxisLabelStep(this.axisPanel.getYAxisLabelStep());
         this.sourceCodeMap.put("yAxisLabelStep", this.axisPanel.getYAxisLabelStep());
         
         chart.setYAxisGridVisible(this.axisPanel.isYAxisGridEnabled());
         this.sourceCodeMap.put("yAxisGridVisible", this.axisPanel.isYAxisGridEnabled());
 
         if (this.axisPanel.isYAxisCustomFormatter()) {
             chart.setYAxisLabelFormatter(new AxisLabelFormatter() {
                 public String format(double labelValue) {
                     return String.valueOf(labelValue) + "\u20AC";
                 }
             });
         } else {
             chart.setYAxisLabelFormatter(null);
         }
         this.sourceCodeMap.put("yAxisCustomFormatter", this.axisPanel.isYAxisCustomFormatter());
         
         if (this.miscPanel.isRandomColorSelected()) {
             String[] colors = RandomUtil.nextColors();
             chart.setColors(colors);
             this.sourceCodeMap.put("randomColors", colors);
         } else {
             chart.setColors(null);
         }
         this.sourceCodeMap.put("randomColorsSelected", this.miscPanel.isRandomColorSelected());
 
         if (this.miscPanel.isLegendEnabled()) {
             chart.setLegendVisible(true);
             chart.setLegendAreaWidth(this.miscPanel.getLegendAreaWidth());
             this.sourceCodeMap.put("legendAreaWidth", this.miscPanel.getLegendAreaWidth());
         } else {
             chart.setLegendVisible(false);
         }
         this.sourceCodeMap.put("legendVisible", this.miscPanel.isLegendEnabled());
         
         chart.setInterpolationMode(this.miscPanel.getInterpolationMode());
         if (this.miscPanel.getInterpolationMode() != InterpolationMode.LINEAR) {
             this.sourceCodeMap.put("interpolation", this.miscPanel.getInterpolationMode().name());
         }
         
         chart.setTooltipEnabled(this.miscPanel.isTooltipEnabled());
         if (this.miscPanel.isTooltipEnabled()) {
             if (this.miscPanel.isTooltipCustomEnabled()) {
                 chart.setTooltipFormatter(new LineTooltipFormatter() {
                     
                     public String getTooltipHTML(String serieName, Point value) {
                         StringBuilder tooltipHTML = new StringBuilder();
                         tooltipHTML.append("<table border=0 cellpadding=2 ><tr><td valign=top>").append("<img src=\"");
 
                         String img = "/VAADIN/themes/vprotovisdemo/thumb_up.png";
                         if (value.getY() < 0) {
                             img = "/VAADIN/themes/vprotovisdemo/thumb_down.png";
                         }
                         tooltipHTML.append(img);
                         tooltipHTML.append("\"></td><td>");
                         tooltipHTML.append("<b><i>").append(serieName).append("</i></b><br/>");
                         tooltipHTML.append("\u0024").append(": ").append(value.getY()).append(" \u20AC").append("<br/>");
                         tooltipHTML.append("t").append(": ").append(value.getX()).append(" ms");
                         tooltipHTML.append("</td><tr></table>");
 
                         return tooltipHTML.toString();
                     }
                 });
             } else {
                 chart.setTooltipFormatter(new DefaultLineTooltipFormatter());
             }
            this.sourceCodeMap.put("tooltipCustomEnabled", this.miscPanel.isTooltipCustomEnabled());
         }
        this.sourceCodeMap.put("tooltipEnabled", this.miscPanel.isTooltipEnabled());
         
         
         chart.requestRepaint();
     }
     
 
     public Component getComponent() {
         return this.content;
     }
 
     public boolean validate() {
         return false;
     }
 
 }
