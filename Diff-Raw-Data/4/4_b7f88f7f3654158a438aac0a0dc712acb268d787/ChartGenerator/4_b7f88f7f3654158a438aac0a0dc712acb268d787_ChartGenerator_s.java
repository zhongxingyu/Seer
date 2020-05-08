 package es.testingserver.atlassian.helpers;
 
 import com.atlassian.jira.charts.Chart;
 import com.atlassian.jira.charts.jfreechart.ChartHelper;
 import com.atlassian.jira.charts.jfreechart.PieChartGenerator;
 import com.atlassian.jira.component.ComponentAccessor;
 import com.atlassian.jira.util.I18nHelper;
 import com.google.common.collect.Maps;
 import org.jfree.data.general.DefaultPieDataset;
 
 import java.io.IOException;
 import java.util.Map;
 
 public class ChartGenerator {
 
     public static Chart getChart( double totalSp, double burnedSp )
     {
         Chart chart = null;
 
         try
         {
             Map<String, Object> params = Maps.newHashMap();
             DefaultPieDataset dataset = new DefaultPieDataset();
            dataset.setValue( "Total SP", totalSp );
            dataset.setValue( "Burned SP", burnedSp );
             I18nHelper i18nBean = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
             final ChartHelper helper = new PieChartGenerator(dataset, i18nBean).generateChart();
             helper.generate(400, 100);
 
             params.put("chart", helper.getLocation());
             params.put("chartDataset", dataset);
             params.put("imagemap", helper.getImageMap());
             params.put("imagemapName", helper.getImageMapName());
             chart = new Chart(
                     helper.getLocation(),
                     helper.getImageMap(),
                     helper.getImageMapName(),
                     params
             );
         }
         catch ( IOException e )
         {
             e.printStackTrace();
             throw new RuntimeException("Error generating chart", e);
         }
 
         return chart;
     }
 
 }
