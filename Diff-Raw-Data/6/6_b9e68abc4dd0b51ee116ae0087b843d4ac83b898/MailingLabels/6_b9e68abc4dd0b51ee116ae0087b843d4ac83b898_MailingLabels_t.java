 import com.cete.dynamicpdf.*;
 import com.cete.dynamicpdf.pageelements.GridType;
 import com.cete.dynamicpdf.pageelements.LayoutGrid;
 import com.cete.dynamicpdf.pageelements.TextArea;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 
 public class MailingLabels extends HttpServlet {
     // Set page dimensions
     int topMargin;
     int bottomMargin;
     float rightMargin;
     float leftMargin;
     // Set the number of labels per page
     int maximumColumns;
     int maximumRows;
     // Set the spacing between the labels
     int horizontalSpace;
     int verticalSpace;
     // These margins are on the labels themsleves
     int labelTopBottomMargin;
     int labelLeftRightMargin;
     LayoutGrid grid = new LayoutGrid(GridType.PRINT);
 
     Document document;
     Page page;
 
     float currentColumn, currentRow, labelWidth, labelHeight;
     String Name;
     String Address1;
     String Address2;
     String CSZ;
     ServletOutputStream sOut;
     Connection connection;
 
     public void init(ServletConfig servletConfig) throws ServletException {
         super.init(servletConfig);
         topMargin = 36;
         bottomMargin = 36;
         rightMargin = 13.5f;
         leftMargin = 13.5f;
         maximumColumns = 3;
         maximumRows = 10;
         horizontalSpace = 9;
         verticalSpace = 0;
         labelTopBottomMargin = 5;
         labelLeftRightMargin = 15;
     }
 
     public void doGet(HttpServletRequest req, HttpServletResponse res)
             throws IOException, ServletException {
 
         try {
             connection = DbConn("HMI");
         } catch (Exception e) {
             System.out.print("error " + e.toString());
         }
         sOut = res.getOutputStream();
 
         // Create a document and set it's properties
         document = new Document();
 
 
             //   template.getElements().add(grid);
 
             // Top part of Invoice
         page = new Page(PageSize.LETTER, PageOrientation.PORTRAIT);
         document.setCreator("MailingLabels.java");
         document.setAuthor("Your Name");
         document.setTitle("Mailing Labels");
 
         // Entrypoint for the labels
         currentRow = 1;
         currentColumn = 1;
 
         ResultSet data = null;
         // Creates a ResultSet for the report
         try {
             String query = "select ALT_NAME,ALT_ADDRESS1,ALT_ADDRESS2,ALT_CITY||',  '||ALT_STATE||' '||ALT_ZIP CSZ " +
                     "from HT_ORDERS where PRINT_LABEL = 1";
 
             PreparedStatement ps = connection.prepareStatement(query);
             //ps.setInt(1, Integer.parseInt(req.getParameter("invoice")));
             data = ps.executeQuery();
         } catch (SQLException ex1) {
             ex1.printStackTrace(System.err);
         }
 
         // Loop over the ResultSet and add each label
         try {
             assert data != null;
             while (data.next()) {
                 Name = safeDBNull(data.getString("alt_name"), data);
                 Address1 = safeDBNull(data.getString("alt_address1"), data);
                 Address2 = safeDBNull(data.getString("alt_address2"), data);
                 CSZ = safeDBNull(data.getString("CSZ"), data);
                 addLabel();
             }
         } catch (SQLException ex2) {
             ex2.printStackTrace(System.err);
         }
 
         if (page.getElements().getCount() > 0) {
             document.getPages().add(page);
         }
         // Outputs the MailingLabels to the current web page
         document.drawToWeb(req, res, sOut, "MailingLabels.pdf");
         try {
             data.close();
             connection.close();
         } catch (SQLException e) {
             System.out.print("error " + e.getSQLState());
             //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         sOut.close();
     }
 
     private float findLabelHeight() {
         return (page.getDimensions().getHeight() - (page.getDimensions().getTopMargin()
                 + page.getDimensions().getBottomMargin()) - ((maximumRows - 1)
                 * verticalSpace)) / maximumRows;
     }
 
     private float findLabelWidth() {
         return (page.getDimensions().getWidth() - (page.getDimensions().getRightMargin()
                 + page.getDimensions().getLeftMargin()) - ((maximumColumns - 1)
                 * horizontalSpace)) / maximumColumns;
     }
 
     private void addLabel() {
         // Add a new page if you are beyond the maximum Rows
 
 
         if (currentRow == maximumRows + 1) {
             document.getPages().add(page);
             currentRow = 1;
         }
         // Determines if the the label belongs in the first row or first column of the page
         if (currentColumn > 1 & currentRow > 1) {
             addToPage();
         } else if (currentColumn > 1 & currentRow == 1) {
             addToFirstRow();
         } else if (currentColumn == 1 & currentRow > 1) {
             addToFirstColumn();
         } else {
             page = new Page(PageSize.LETTER, PageOrientation.PORTRAIT);
             page.getElements().add(grid);
             page.getDimensions().setTopMargin(topMargin);
             page.getDimensions().setBottomMargin(bottomMargin);
             page.getDimensions().setRightMargin(rightMargin);
             page.getDimensions().setLeftMargin(leftMargin);
             labelWidth = findLabelWidth();
             labelHeight = findLabelHeight();
             addToFirstRowColumn();
         }
 
         // Incremment your row if you are beyond the maximum columns
         if (currentColumn == maximumColumns + 1) {
             currentRow = currentRow + 1;
             currentColumn = 1;
         }
     }
 
     // Adds the label on at least row 2 column 2 of the page
     private void addToPage() {
         float x;
         float y;
         y = (currentRow - 1) * (labelHeight + verticalSpace);
         x = (currentColumn - 1) * (labelWidth + horizontalSpace);
         addLabelInfo(x, y);
         currentColumn = currentColumn + 1;
     }
 
     // Adds the label on the first row of labels
     private void addToFirstRow() {
         float x;
         float y;
         y = 0;
         x = (currentColumn - 1) * (labelWidth + horizontalSpace);
         addLabelInfo(x, y);
         currentColumn = currentColumn + 1;
     }
 
     // Adds the label to the First column of labels
     private void addToFirstColumn() {
         float x;
         float y;
         y = (currentRow - 1) * (labelHeight + verticalSpace);
         x = 0;
         addLabelInfo(x, y);
         currentColumn = currentColumn + 1;
     }
 
     // Adds only the first label of every page (row 1 column 1)
     private void addToFirstRowColumn() {
         float x;
         float y;
         y = 0;
         x = 0;
         addLabelInfo(x, y);
         currentColumn = currentColumn + 1;
     }
 
     // This is where you format the look of each label
     private void addLabelInfo1(float x, float y) {
         TextArea txt3;
         TextArea txt4;
         TextArea txt1 = new TextArea(Name, x + labelLeftRightMargin,
                 y + labelTopBottomMargin, labelWidth
                 - (labelLeftRightMargin * 2), 11,
                 Font.getTimesRoman(), 11);
         TextArea txt2 = new TextArea(Address1, x + labelLeftRightMargin,
                 y + labelTopBottomMargin + 12, labelWidth
                 - (labelLeftRightMargin * 2), 11,
                 Font.getTimesRoman(), 11);
         if (Address2.length() > 0) {
             txt3 = new TextArea(Address2, x + labelLeftRightMargin,
                     y + labelTopBottomMargin + 24, labelWidth
                     - (labelLeftRightMargin * 2), 11,
                     Font.getTimesRoman(), 11);
 
             txt4 = new TextArea(CSZ, x + labelLeftRightMargin,
                     y + labelTopBottomMargin + 36, labelWidth
                     - (labelLeftRightMargin * 2), 11,
                     Font.getTimesRoman(), 11);
         } else {
             txt3 = new TextArea(CSZ, x + labelLeftRightMargin,
                     y + labelTopBottomMargin + 24, labelWidth
                     - (labelLeftRightMargin * 2), 11,
                     Font.getTimesRoman(), 11);
 
             txt4 = new TextArea("", x + labelLeftRightMargin,
                     y + labelTopBottomMargin + 36, labelWidth
                     - (labelLeftRightMargin * 2), 11,
                     Font.getTimesRoman(), 11);
         }
         page.getElements().add(txt1);
         page.getElements().add(txt2);
         page.getElements().add(txt3);
         page.getElements().add(txt4);
     }
 
      private void addLabelInfo(float x, float y) {
         TextArea txt3;
         TextArea txt4;
          switch ((int) y) {
          case 360:
               y = 366;
              break;
          case 432:
               y = 438;
              break;
           case 504:
               y = 510;
               break;
           case 576:
               y = 588;
               break;
           case 648:
                  y = 660;
               break;
           }
         TextArea txt1 = new TextArea(Name, x + labelLeftRightMargin,
                 y + labelTopBottomMargin, labelWidth
                 - (labelLeftRightMargin * 2), 11,
                 Font.getTimesRoman(), 11);
         TextArea txt2 = new TextArea(Address1, x + labelLeftRightMargin,
                 y + labelTopBottomMargin + 12, labelWidth
                 - (labelLeftRightMargin * 2), 11,
                 Font.getTimesRoman(), 11);
         if (Address2.length() > 0) {
             txt3 = new TextArea(Address2, x + labelLeftRightMargin,
                     y + labelTopBottomMargin + 24, labelWidth
                     - (labelLeftRightMargin * 2), 11,
                     Font.getTimesRoman(), 11);
 
             txt4 = new TextArea(CSZ, x + labelLeftRightMargin,
                     y + labelTopBottomMargin + 36, labelWidth
                     - (labelLeftRightMargin * 2), 11,
                     Font.getTimesRoman(), 11);
         } else {
             txt3 = new TextArea(CSZ, x + labelLeftRightMargin,
                     y + labelTopBottomMargin + 24, labelWidth
                     - (labelLeftRightMargin * 2), 11,
                     Font.getTimesRoman(), 11);
 
             txt4 = new TextArea("", x + labelLeftRightMargin,
                     y + labelTopBottomMargin + 36, labelWidth
                     - (labelLeftRightMargin * 2), 11,
                     Font.getTimesRoman(), 11);
         }
         page.getElements().add(txt1);
         page.getElements().add(txt2);
         page.getElements().add(txt3);
         page.getElements().add(txt4);
     }
 
     private String safeDBNull(String value, ResultSet data) {
         try {
             if (data.wasNull()) {
                 return "";
             } else {
                 return value;
             }
         } catch (SQLException ex3) {
             ex3.printStackTrace(System.err);
         }
         return "";
     }
 
     public Connection DbConn(String key) throws Exception {
         return ConnectionFactory.getConnection(key);
     }
 
 }
