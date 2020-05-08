 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.portlet.impl.SportletResponse;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.StringTokenizer;
 import java.util.List;
 import java.util.ArrayList;
 
 public class PortletGridLayout extends BaseLayoutManager {
 
     private int numColumns;
     private int[] colSizes;
     private String columnString;
 
     public PortletGridLayout() {}
 
     public String getClassName() {
         return PortletGridLayout.class.getName();
     }
 
     public void setColumns(String columnString) {
         this.columnString = columnString;
     }
 
     public String getColumns() {
         return columnString;
     }
 
     public List init(List list) {
         list = super.init(list);
         if (columnString != null) {
             StringTokenizer st = new StringTokenizer(columnString, ",");
             numColumns = st.countTokens();
             colSizes = new int[numColumns];
             int i = 0;
             while (st.hasMoreTokens()) {
                 String col = st.nextToken();
                 colSizes[i] = Integer.parseInt(col);
                 i++;
             }
         } else {
             numColumns = 1;
             colSizes = new int[1];
             colSizes[0] = 100;
         }
         return list;
     }
 
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         SportletResponse res = event.getSportletResponse();
         PrintWriter out = res.getWriter();
 
         if (insets == null) insets = new PortletInsets();
 
         //int j = 0, k = 0;
 
         //out.println("row: "+rows+" columns "+cols);
 
         int numComponents = components.size();
         PortletComponent p = null;
 
         int portletsPerColumns = numComponents/numColumns;
         int portletCount = 0;
 
         System.out.println(" ================ portletspercolumn: "+portletsPerColumns);
         System.out.println(" ================ numcolumns: "+numColumns);
         System.out.println(" ================ numComponents: "+numComponents);
 
         // cycle through to find a max window
         for (int i=0;i<numComponents;i++) {
            p = (PortletComponent)components.get(portletCount);
             if (p.getWidth().equals("100%")) {
                i=numComponents;
             }
         }
         // ok this one is maximized show only this window
         if (p.getWidth().equals("100%")) {
             // make another table around this, just for the padding
             out.println("<table border=\"0\" width=\"100%\" cellpadding=\"2\" cellspacing=\"0\"> ");
             out.println("<tr><td>");
             p.doRender(event);
             out.println("</td></tr></table>");
 
         } else {
             //out.println("<table width=\"" + gwidth + "%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\" bgcolor=\"#FFFFFF\">");
             out.println("<table border=\"0\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"> <!-- overall gridlayout table -->");
 
             out.println("<tr> <!-- overall one row -->");
             for (int i=0;i<numColumns;i++) {
                 // new column
                 out.println("<td width=\""+colSizes[i]+"%\" valign=\"top\"> <!-- this is a row -->");
                 // construct a table inside this column
                 out.println("<table border=\"0\" width=\"100%\" cellpadding=\"2\" cellspacing=\"0\"> <!-- this is table inside row ("+i+")-->");
                 // now render the portlets in this column
                 //out.println("<tr>");
                 for (int j=1;j<=portletsPerColumns;j++) {
                     out.println("<tr><td>");
                     p = (PortletComponent)components.get(portletCount);
                     if (p.isVisible()) {
                         p.doRender(event);
                     }
                     out.println("</td></tr>");
                     portletCount++;
 
                     // if we have some (1) portlet left because of odd number of
                     // portlets to display just render the last ones in that column here
                     if ((portletCount<numComponents) && (i==numColumns-1) && (j==portletsPerColumns)) {
                         j--;
                     }
                 }
                 // close this row again
                 out.println("</table> <!-- end table inside row -->");
                 out.println("</td>");
             }
             out.println("</tr> <!-- end overall one row -->");
             out.println("</table> <!-- end overall gridlayout table -->");
         }
     }
 
 }
 
