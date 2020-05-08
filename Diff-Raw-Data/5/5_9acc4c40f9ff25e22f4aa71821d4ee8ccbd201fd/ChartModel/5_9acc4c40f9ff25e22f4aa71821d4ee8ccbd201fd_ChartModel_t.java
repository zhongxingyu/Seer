 package org.pentaho.chart.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.pentaho.chart.model.Theme.ChartTheme;
 
 public class ChartModel implements Serializable {
   public static final int CHART_ENGINE_JFREE = 0;
   public static final int CHART_ENGINE_OPENFLASH = 1;
   
   public static final String DEFAULT_FAMILY = "serif";
   public static final int DEFAULT_SIZE = 14;
   
   StyledText title = new StyledText();
   ArrayList<String> subtitles = new ArrayList<String>();
   ChartLegend legend = new ChartLegend();
   String backgroundImageLocation;
   Texture backgroundTexture;
   Gradient backgroundGradient;
   Plot plot;
   int chartEngine = CHART_ENGINE_OPENFLASH;
   ChartTheme theme;
   CssStyle style = new CssStyle();
   
   /**
    * Get the user friendly name of the currently implemented engine
    * @param chartEngine value of ChartModel.CHART_ENGINE_JFREE or ChartModel.CHART_ENGINE_OPENFLASH
    * @return The friendly name of the implemented engine
    */
  public static String getChartEngineFriendlyNameFromId(int chartEngine){
     String chartEngineFriendlyName = null;
     
     switch(chartEngine){
       case CHART_ENGINE_JFREE:{
         chartEngineFriendlyName = "JFreeChart";  //$NON-NLS-1$
       }break;
       case CHART_ENGINE_OPENFLASH:{
         chartEngineFriendlyName = "OpenFlashChart";  //$NON-NLS-1$
       }break;
     }
     
     return chartEngineFriendlyName;
   }
 
   /**
    * If the friendly name is one of "JFreeChart" or "OpenFlashChart" then the engine
    * is set accordingly
    * @param chartEngineFriendlyName
    * @return -1 if chartEngineFriendlyName ws invalid
    */
  public static int getChartEngineIdFromFriendlyName(String chartEngineFriendlyName){
     int chartEngine = -1;
     
     if(chartEngineFriendlyName != null){
       if(chartEngineFriendlyName.equalsIgnoreCase("JFreeChart")){  //$NON-NLS-1$
         chartEngine = CHART_ENGINE_JFREE;
       } else if (chartEngineFriendlyName.equalsIgnoreCase("OpenFlashChart")){  //$NON-NLS-1$
         chartEngine = CHART_ENGINE_OPENFLASH;
       }
     }
     return chartEngine;
   }
   
   public int getChartEngine() {
     return chartEngine;
   }
 
   public void setChartEngine(int chartEngine) {
     this.chartEngine = chartEngine;
   }
 
   public ChartTheme getTheme() {
     return theme;
   }
 
   public void setTheme(ChartTheme theme) {
     this.theme = theme;
   }
 
   public List<String> getSubtitles() {
     return subtitles;
   }
   
   public void setSubtitles(List<String> subtitles) {
     subtitles.clear();
     if (subtitles != null) {
       subtitles.addAll(subtitles);
     }
   }
   
   public String getSubtitle() {
     return subtitles.size() > 0 ? subtitles.get(0) : null;
   }
 
   public void setSubtitle(String title) {
     subtitles.clear();
     if (title != null) {
       subtitles.add(title);
     }
   }
 
   public ChartLegend getLegend() {
     return legend;
   }
 
   public void setLegend(ChartLegend chartLegend) {
     this.legend = chartLegend;
   }
 
   public Object getBackground() {
     Object background = null;
     if (style.getBackgroundColor() != null) {
       background = style.getBackgroundColor();
     } else if (backgroundGradient != null) {
       background = backgroundGradient;
     } else if (backgroundImageLocation != null) {
       background = backgroundImageLocation;
     } else if (backgroundTexture != null) {
       background = backgroundTexture;
     }
     return background;
   }
 
   public void setBackground(Integer backgroundColor) {
     style.setBackgroundColor(backgroundColor);
     if (backgroundColor != null) {
       backgroundGradient = null;
       backgroundImageLocation = null;
       backgroundTexture = null;
     }
   }
 
   public void setBackground(String backgroundImageLocation) {
     this.backgroundImageLocation = backgroundImageLocation;
     if (backgroundImageLocation != null) {
       backgroundGradient = null;
       setBackground((Integer)null);
       backgroundTexture = null;
     }
   }
   
   public void setBackground(Gradient backgroundGradient) {
     this.backgroundGradient = backgroundGradient;
     if (backgroundGradient != null) {
       setBackground((Integer)null);
       backgroundImageLocation = null;
       backgroundTexture = null;
     }
   }
   
   public void setBackground(Texture backgroundTexture) {
     this.backgroundTexture = backgroundTexture;
     if (backgroundTexture != null) {
       backgroundGradient = null;
       setBackground((Integer)null);
       backgroundImageLocation = null;
     }
   }
   
   public Plot getPlot() {
     return plot;
   }
 
   public void setPlot(Plot plot) {
     this.plot = plot;
   }
 
   public StyledText getTitle() {
     return title;
   }
 
   public void setTitle(StyledText title) {
     this.title = title;
   }
 
   public Integer getBorderColor() {
     return style.getBorderColor();
   }
 
   public boolean getBorderVisible() {
     return style.getBorderVisible();
   }
 
   public Integer getBorderWidth() {
     return style.getBorderWidth();
   }
 
   public void setBorderColor(Integer color) {
     style.setBorderColor(color);
   }
 
   public void setBorderVisible(boolean visible) {
     style.setBorderVisible(visible);
   }
 
   public void setBorderWidth(Integer width) {
     style.setBorderWidth(width);
   }
 
   public CssStyle getStyle() {
     return style;
   }
 
 }
