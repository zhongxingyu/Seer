 import com.cete.dynamicpdf.*;
 import com.cete.dynamicpdf.pageelements.*;
 import oracle.jdbc.OracleTypes;
 import oracle.jdbc.driver.OracleCallableStatement;
 
 import java.io.FileNotFoundException;
 import java.math.BigDecimal;
 import java.sql.*;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Vector;
 
 /*
     96B458F0-F835-15C5-821B54169493EC5E       POP order
     0E79447A-BDC7-27D7-2BB45180104B9E73  order  with options
     0CEF8F80-C978-3504-4067C5581CE60A27  order with multiple items not options
 
 */
 public class Invoice {
 
 
     Connection connection;
 
     public Invoice() throws Exception {
         try {
 
             connection = DbConn("HMI");
         } catch (ClassNotFoundException ex1) {
             ex1.printStackTrace(System.err);
         } catch (SQLException ex2) {
             ex2.printStackTrace(System.err);
         }
     }
 
     public static void main(String args[]) throws SQLException {
 
         // Create a document and set it's properties
         Document objDocument = new Document();
         objDocument.setCreator("Invoice.java");
         objDocument.setAuthor("Your Name");
         objDocument.setTitle("Invoice");
         String licKey = "GEN40JPSKHHNCBSVuhQrLJN/2/7YXQlfFfJpXwGPI9IHqIK3/PI2AFwYRMSi+tcDTZomQLCeaMxLcYq9cmfIve/GVzp6nIGtJq2g";
         String orderid = args[0];
         Invoice invoice = null;
         com.cete.dynamicpdf.Document.addLicense(licKey);
         try {
             invoice = new Invoice();
         } catch (Exception e) {
             System.out.println(e.toString());  //To change body of catch statement use File | Settings | File Templates.
         }
         // Add Invoices to the document
         if (invoice != null) {
             invoice.drawInvoice(objDocument, orderid);
         }
 
         // Outputs the Invoices to the file.
         objDocument.draw("Invoice.pdf");
         try {
             if (!invoice.connection.isClosed())
                 invoice.connection.close();
         } catch (SQLException e) {
             System.out.println(e.toString());  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public void drawInvoice(Document objDocument, String orderid) throws SQLException {
 
         MyInvoice objInvoice = new MyInvoice();
         //Add the template to the document
         objDocument.setTemplate(objInvoice.getTemplate());
         objInvoice.draw(connection, objDocument, orderid);
 
     }
 
     private class MyInvoice {
 
         private BigDecimal subTotal = new BigDecimal(0.0);
 
         private float yOffset = 0;
         Enumeration e1 = null;
         Enumeration e2 = null;
         private Template template = new Template();
         private boolean pageTemplateImagesSet = false;
         private RgbColor objBGColor = new WebColor("#E0E0FF");
         private WebColor objTotalBGColor = new WebColor("#FFC0C0");
         private WebColor objBorderColor = new WebColor("#000000");
         private WebColor objThankYouText = new WebColor("#000080");
 
         private CeteDAO ceteDAO = null;
         private ProductDAO productDAO = null;
         private ArrayList<Integer> popItems;
 
 
         public MyInvoice() {
             // LayoutGrid grid = new LayoutGrid(); //Default is decimal
             //   template.getElements().add(grid);
             // Top part of Invoice
 
             template.getElements().add(new Label("Heritage Manufacturing, Inc.", 0, 10, 540, 18, Font.getHelveticaBold(), 14, TextAlign.CENTER));
             template.getElements().add(new Label("4600 NW 135th Street", 0, 26, 540, 18, Font.getHelvetica(), 12, TextAlign.CENTER));
             template.getElements().add(new Label("Opa Locka, FL  33054", 0, 40, 540, 18, Font.getHelvetica(), 12, TextAlign.CENTER));
             template.getElements().add(new Label("Tel: 305.685.5966", 0, 54, 540, 18, Font.getHelvetica(), 12, TextAlign.CENTER));
             template.getElements().add(new Label("Fax: 305.687.6721", 0, 68, 540, 18, Font.getHelvetica(), 12, TextAlign.CENTER));
 
             template.getElements().add(new Label("Invoice", 400, 0, 140, 24, Font.getHelveticaBold(), 24, TextAlign.LEFT));
 
             template.getElements().add(new PageNumberingLabel("Page %%SP%% of %%ST%% ",
                     450, 253, 90, 20, Font.getHelveticaBold(), 12, TextAlign.CENTER));
             // Add Invoice Details Template
             template.getElements().add(getDetailsGroup());
 
             // Add BillTo Template
             template.getElements().add(getBillToGroup());
 
             // Add ShipTo Template
             template.getElements().add(getShipToGroup());
 
             // Add Line Item Template
             template.getElements().add(getLineItemGroup());
 
             // Sets the image to the page template
             setPageTemplateImage();
         }
 
 
         public Template getTemplate() {
             return template;
         }
 
 
         private Group getDetailsGroup() {
             // Returns a group containing the details template
             Group objGroup = new Group();
 
             objGroup.add(new Label("Order ID:", 366, 25, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Order Date:", 366, 39, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Placed By:", 366, 53, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Date Printed:", 366, 67, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Shipped Via:", 366, 81, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             return objGroup;
         }
 
         private Group getBillToGroup() {
             // Returns a group containing the bill to template
             Group objGroup = new Group();
 
             objGroup.add(new Rectangle(0, 120, 200, 90, 0.5f));
             objGroup.add(new Rectangle(0, 120, 200, 14, objBorderColor, objTotalBGColor, 0.5f));
             objGroup.add(new Label("Bill To:", 3, 121, 200, 12, Font.getHelveticaBold(), 12));
             return objGroup;
         }
 
         private Group getShipToGroup() {
             // Returns a group containing the ship to template
             Group objGroup = new Group();
 
             objGroup.add(new Rectangle(340, 120, 200, 90, 0.5f));
             objGroup.add(new Rectangle(340, 120, 200, 14, objBorderColor, objTotalBGColor, 0.5f));
             // objGroup.add(new Line(340, 136, 540, 136, 0.5f));
             objGroup.add(new Label("Ship To:", 343, 121, 200, 12, Font.getHelveticaBold(), 12));
             return objGroup;
         }
 
         private Group getLineItemGroup() {
             // Returns a group containing the line items template
             Group objGroup = new Group();
 
             for (int i = 0; i < 9; i++) {
                 objGroup.add(new Rectangle(0, 306 + i * 36, 540, 18, objBGColor,
                         objBGColor));
             }
             objGroup.add(new Rectangle(450, 250, 90, 20, 0.5f));
             objGroup.add(new Rectangle(450, 702, 90, 18, objTotalBGColor, objTotalBGColor));
             objGroup.add(new Rectangle(0, 270, 540, 450, 0.5f));
 
             objGroup.add(new Line(0, 288, 540, 288, 0.5f));
             objGroup.add(new Line(0, 630, 540, 630, 0.5f));
             objGroup.add(new Line(60, 270, 60, 630, 0.5f));
             objGroup.add(new Line(390, 270, 390, 720, 0.5f));
             objGroup.add(new Line(450, 270, 450, 720, 0.5f));
             objGroup.add(new Line(450, 702, 540, 702, 0.5f));
 
             objGroup.add(new Label("Quantity", 0, 272, 60, 12, Font.getHelveticaBold(), 12, TextAlign.CENTER));
             objGroup.add(new Label("Description", 60, 272, 300, 12, Font.getHelveticaBold(), 12, TextAlign.CENTER));
             objGroup.add(new Label("Price", 400, 272, 40, 12, Font.getHelveticaBold(), 12, TextAlign.CENTER));
             objGroup.add(new Label("Extension", 450, 272, 90, 12, Font.getHelveticaBold(), 12, TextAlign.CENTER));
 
             objGroup.add(new Label("Sub Total", 364, 632, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Discount", 364, 650, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Sales Tax", 364, 668, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Freight", 364, 686, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Total", 364, 704, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
 
             String footer = "IF YOU PURCHASED A CYLINDER it is very important that this new cylinder aligns and locks up securely." +
                     "This must be checked before firing your revolver. If you are unsure, have a gunsmith or firearms retailer " +
                     "check it for you or you can send us your revolver along with this new cylinder and $15 for shipping. We will take care of it for you.\n" +
                     "Returns / Exchanges / Refunds\n" +
                     "We appreciate your business and are confident that you'll be pleased with your order. However, " +
                     "if you are unhappy for any reason, please return your order directly to us within 10 days of receipt " +
                     "of the order. A copy of your Invoice receipt along with a brief note stating why you are returning/exchanging " +
                     "the item must be enclosed. All items must be unused and in it's original package. No damaged or altered goods " +
                     "will be accepted. All refunds or exchanges will occur within 30 working days from receipt of goods. Shipping " +
                     "and handling will not be refunded. \n" +
                     "If you need to exchange a Leather Good, on your Invoice receipt, write a quick note stating what size you need. " +
                     "There will be a $5.00 re-shipping charge. We can use your credit card on file or you can send a money order with " +
                     "the goods.\n" +
                     "\n" +
                     "WARNING: Your revolver was manufactured to perform properly with the original parts as designed. It is your duty to " +
                     "make sure that any part you purchase is installed correctly and that replacement parts or original parts are not " +
                     "altered or changed. Your gun is a complex tool with many parts that must relate correctly to each other. Assembling " +
                     "a firearm incorrectly or with altered parts can result in a damaged gun, injury or death. Do not attempt to work on " +
                     "it yourself. This is a small price to pay for your safety.";
             objGroup.add(new TextArea(footer, 4, 632, 350, 90,
                     Font.getHelveticaBold(), 5, objThankYouText));
             return objGroup;
         }
 
         private void setPageTemplateImage() {
             // Adds the image to page template if it is not already added
             if (!pageTemplateImagesSet) {
                 try {
 
                     template.getElements().add(new Image("/usr/local/apache-tomcat/webapps/HMI/WEB-INF/images/logo_mg.gif", 0, 0, 0.85f));
 
                 } catch (FileNotFoundException ex) {
                     try {
                         template.getElements().add(new Image("images/logo_mg.gif", 0, 0, 0.85f));
                         // ex.printStackTrace(System.err);
                     } catch (FileNotFoundException ex2) {
 
                     }
                 }
                 pageTemplateImagesSet = true;
             }
 
         }
 
         public void draw(Connection connection, Document document, String orderid) throws SQLException {
             // Each Invoice should begin a new section
             document.getSections().begin();
             // Gets the Invoice data
             Vector v1 = getInvoiceData(connection, orderid);
             Vector v2 = getLineItems(connection, orderid);
             e1 = v1.elements();
             e2 = v2.elements();
 
             // Adds the invoice if there is data            
             if (e1 != null) {
                 // Draws the invoice data, returns a page object if it is
                 // the last page
                 Page objLastPage = drawInvoiceData(document, orderid);
                 // Draws aditional pages if necessary
                 while (objLastPage == null) {
                     objLastPage = drawInvoiceData(document, orderid);
                 }
                 // Draws the totals to the bottom of the last page of the Invoice
                 drawTotals(ceteDAO, objLastPage);
             }
 
         }
 
         private Page drawInvoiceData(Document document, String orderid) throws SQLException {
             // Tracks if the invoice is finished
             boolean invoiceFinished = true;
             // Tracks the y position on the page
             yOffset = 288;
             if (ceteDAO == null) {
                 ceteDAO = (CeteDAO) e1.nextElement();
             }
 
             // Create a page for the Invoice
             Page objPage = new Page(PageSize.LETTER, PageOrientation.PORTRAIT, 36.0f);
             // Add Details to the Invoice
             drawInvoiceDetails(ceteDAO, objPage);
             // Add bill to address
             drawBillTo(ceteDAO, objPage);
             // Add ship to address
             drawShipTo(ceteDAO, objPage);
             drawComments(ceteDAO, objPage);
             getPOPItems(connection);
             while (e2.hasMoreElements()) {
                 // Break if at the bottom of the page
 
                 if (yOffset >= 594) {
                     invoiceFinished = false;
                     break;
                 }
 
                 drawLineItem((ProductDAO) e2.nextElement(), objPage, orderid);
             }
 
             // Add the page to the document
             document.getPages().add(objPage);
 
             // If Invoice is finished return the page else return null so
             // another page will be added
             if (invoiceFinished) {
                 return objPage;
             } else {
                 objPage.getElements().add(new Label("Continued...", 454, 704, 82,
                         12, Font.getHelvetica(), 12, TextAlign.RIGHT));
                 return null;
             }
         }
 
         private void drawInvoiceDetails(CeteDAO ceteDAO, Page page) {
 
             // Adds Invoice details to the page
             page.getElements().add(new Label(String.valueOf(ceteDAO.getOrderID()), 460, 25, 100, 12, Font.getHelvetica(), 12));
             SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
             page.getElements().add(new Label(dateFormat.format(ceteDAO.getOrderDate()), 460, 39, 100, 12, Font.getHelvetica(), 12));
             page.getElements().add(new Label(String.valueOf(ceteDAO.getClerk()), 460, 53, 100, 12, Font.getHelvetica(), 12));
             Date date = ceteDAO.getPrintDate();
             if (date != null) {
                 page.getElements().add(new Label(dateFormat.format(date), 460, 67, 100, 12, Font.getHelvetica(), 12));
             }
             page.getElements().add(new Label(ceteDAO.getShipMethod(), 460, 81, 100, 24, Font.getHelvetica(), 12));
 
         }
 
         private void drawBillTo(CeteDAO ceteDAO, Page page) {
             String billToAddress = "";
 
             if (!ceteDAO.getCompany().equalsIgnoreCase("Heritage Manufacturing Inc.")) {
 
                 if (ceteDAO.getCompany() != null && ceteDAO.getCompany().length() > 1) {
                     billToAddress = ceteDAO.getCompany() + "\n";
                 }
                 billToAddress += ceteDAO.getBillToName() + "\n" +
                         ceteDAO.getBillToAddress1() + "\n";
                 if (ceteDAO.getBillToAddress2() != null) {
                     billToAddress += ceteDAO.getBillToAddress2() + "\n";
                 }
                 billToAddress += ceteDAO.getBillToCSZ();
 
 
             } else {
                 billToAddress = ceteDAO.getShipToName() + "\n" +
                         ceteDAO.getShipToAddress1() + "\n";
                 if (ceteDAO.getShipToAddress2() != null) {
                     billToAddress += ceteDAO.getShipToAddress2() + "\n";
                 }
                 billToAddress += ceteDAO.getShipToCSZ();
             }
             page.getElements().add(new TextArea(billToAddress, 3, 139, 194, 70, Font.getHelvetica(), 12));
         }
 
         private void drawShipTo(CeteDAO ceteDAO, Page page) {
 
             // Adds ship to address
             String shipToAddress = ceteDAO.getShipToName() + "\n" +
                     ceteDAO.getShipToAddress1() + "\n";
             if (ceteDAO.getShipToAddress2() != null) {
                 shipToAddress += ceteDAO.getShipToAddress2() + "\n";
                 shipToAddress += ceteDAO.getShipToCSZ() + "\n";
             } else {
                 shipToAddress += ceteDAO.getShipToCSZ() + "\n\n";
             }
 
             shipToAddress += ceteDAO.getPhone();
 
 
             page.getElements().add(new TextArea(shipToAddress, 343, 139, 194, 72, Font.getHelvetica(), 12));
 
         }
 
         private void drawComments(CeteDAO ceteDAO, Page page) {
 
             // Adds ship to address
             String comments = ceteDAO.getComments();
 
 
             page.getElements().add(new TextArea(comments, 10, 220, 320, 49, Font.getHelvetica(), 8));
 
         }
 
         private boolean hasLongDescription(int item) {
 
             for (int i = 0; i < popItems.size(); i++) {
                 if (popItems.get(i).equals(item)) {
                     return true;
 
                 }
             }
 
             return false;
         }
 
 
         private void drawLineItem(ProductDAO productDAO, Page page, String orderid) throws SQLException {
 
             BigDecimal quantity;
             BigDecimal unitPrice;
 
             quantity = new BigDecimal(productDAO.get_product_quantity());
             unitPrice = productDAO.get_product_price();
 
             BigDecimal lineTotal = unitPrice.multiply(quantity);
             subTotal = lineTotal.add(subTotal);
 
             TextArea ta = null;
             boolean hasLongDescription = false;
 
             // System.out.println("drawLineItem yOffset is: " + yOffset);
 
             page.getElements().add(new Label(quantity.toString(), 4,
                     3 + yOffset, 52, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
 
 
             if (productDAO.get_page_name() == null) {
                 page.getElements().add(new Label(productDAO.get_product_description(),
                         64, 3 + yOffset, 326, 12, Font.getHelveticaBold(), 12));
             } else {
                 page.getElements().add(new Label(productDAO.get_product_description() + " - " + productDAO.get_page_name(),
                         64, 3 + yOffset, 326, 12, Font.getHelveticaBold(), 12));
             }
 
             unitPrice = unitPrice.setScale(2, BigDecimal.ROUND_HALF_EVEN);
             lineTotal = lineTotal.setScale(2, BigDecimal.ROUND_HALF_EVEN);
 
             DecimalFormat df = new DecimalFormat("#,##0.00");
             page.getElements().add(new Label(df.format(unitPrice.doubleValue()), 364, 3 + yOffset, 82, 12,
                     Font.getHelvetica(), 12, TextAlign.RIGHT));
             page.getElements().add(new Label(df.format(lineTotal.doubleValue()), 454, 3 + yOffset, 82, 12,
                     Font.getHelvetica(), 12, TextAlign.RIGHT));
 
             yOffset += 18;
             ResultSet rs = getItemOptions(connection, orderid, productDAO.get_product_id(), productDAO.get_product_order_item());
             drawLineItem(rs, page, productDAO.get_product_quantity());
 
             if (hasLongDescription(productDAO.get_product_id())) {
                 ta = new TextArea(productDAO.get_short_description(), 64, 3 + yOffset, 326, 12, Font.getHelvetica(), 12);
                 page.getElements().add(ta);
                 hasLongDescription = true;
             }
             yOffset += 18;
             // keep printing description for this item until all done
             try {
                 while (((ta = ta.getOverflowTextArea(64, 3 + yOffset, 326, 12)) != null) && (yOffset <= 594)) {
                     page.getElements().add(ta);
                     yOffset += 18;
                 }
             } catch (Exception e) {
                 // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             if (!hasLongDescription)           // if we did not have a long desc backoff line counter
                 yOffset -= 18;
         }
 
         private void drawLineItem(ResultSet rs, Page page, int qty) throws SQLException {
             // Adds a line item to the invoice
             while (rs.next()) {
                 BigDecimal unitPrice;
                 unitPrice = new BigDecimal(0.0);
 
                 BigDecimal quantity;
                 quantity = new BigDecimal(qty);
 
                 unitPrice = rs.getBigDecimal("PRICE");
                 BigDecimal lineTotal = unitPrice.multiply(quantity);
                 subTotal = lineTotal.add(subTotal);
 
                 page.getElements().add(new Label("", 4, 3 + yOffset, 52, 12, Font.getHelvetica(), 12, TextAlign.RIGHT));
                 page.getElements().add(new Label("    " + rs.getString("NAME") + ": " + rs.getString("ITEM"),
                         64, 3 + yOffset, 326, 12, Font.getHelvetica(), 12));
 
                 unitPrice = unitPrice.setScale(2, BigDecimal.ROUND_HALF_EVEN);
 
                 lineTotal = lineTotal.setScale(2, BigDecimal.ROUND_HALF_EVEN);
                 DecimalFormat df = new DecimalFormat("#,##0.00");
                 if (unitPrice.compareTo(new BigDecimal(0.0)) != 0) {
 
                     page.getElements().add(new Label(df.format(unitPrice.doubleValue()), 364, 3 + yOffset, 82, 12,
                             Font.getHelvetica(), 12, TextAlign.RIGHT));
                     page.getElements().add(new Label(df.format(lineTotal.doubleValue()), 454, 3 + yOffset, 82, 12,
                             Font.getHelvetica(), 12, TextAlign.RIGHT));
                 }
                 yOffset += 18;
 
             }
         }
 
         private void drawTotals(CeteDAO ceteDAO, Page page) {
             // Add totals to the bottom of the Invoice
 
             DecimalFormat df = new DecimalFormat("#,##0.00");
 
             page.getElements().add(new Label(df.format(subTotal.doubleValue()), 454, 631, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
             page.getElements().add(new Label("-" + df.format(ceteDAO.getDiscount().doubleValue()), 454, 649, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
             page.getElements().add(new Label(df.format(ceteDAO.getTax().doubleValue()), 454, 667, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
             page.getElements().add(new Label(df.format(ceteDAO.getFreight().doubleValue()), 454, 685, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
             page.getElements().add(new Label(df.format(ceteDAO.getGrandTotal().doubleValue()), 454, 703, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
         }
 
         private Vector getInvoiceData(Connection connection, String orderid) {
             Vector v1 = null;
             try {
 
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT o.invoice," +
                                 "o.order_date, " +
                                 "o.CLERK," +
                                 "SYSDATE," +
                                 "SHIP_DESCRIPTION," +
 
                                 "NVL(c.COMPANY,' ') BillName," +
                                 "c.FIRST_NAME||' '||c.LAST_NAME BillTo, " +
                                 "c.address1 Address1," +
                                 "c.address2 Address2," +
                                 "c.city||',  '||c.state||' '||c.zip BillToCSZ," +
 
                                 "o.alt_name, " +
                                 "o.alt_address1," +
                                 "o.alt_address2," +
                                 "o.alt_city||', '||o.alt_state||' '||o.alt_zip," +
 
                                 "o.subtotal," +
                                 "o.discount_amount," +
                                 "o.tax_amount," +
                                 "o.ship_total," +
                                 "totalprice ," +
                                 "NVL(comments,' '), " +
                                 "NVL(alt_phone,' ') " +
                                 "FROM ht_orders o, ht_customers c " +
                                 "WHERE o.id = ? " +
                                 "AND o.customerid = c.id");
 
                 ps.setString(1, orderid);     // Phone Order
 
                 ResultSet rs = ps.executeQuery();
                 v1 = new Vector(1, 1);
                 while (rs.next()) {
                     CeteDAO ceteDAO = new CeteDAO(
 
                             rs.getInt(1),                   // invoice
                             rs.getDate(2),                  // order date
                             rs.getString(3),                // clerk
                             rs.getDate(4),                  // date printed
                             rs.getString(5),                 // ship via
                             rs.getString(6),                  // company
                             rs.getString(7),                   // bill to name
                             rs.getString(8),                  // address line 1
                             rs.getString(9),                // address line 2
                             rs.getString(10),               // bill to csz
                             rs.getString(11),               // shipto name
                             rs.getString(12),               // ship to address1
                             rs.getString(13),           // ship to address2
                             rs.getString(14),           // ship to csz
                             rs.getBigDecimal(15),       // sub total
                             rs.getBigDecimal(16),       // discount amount
                             rs.getBigDecimal(17),       // sales tax
                             rs.getBigDecimal(18),       // freight charges
                             rs.getBigDecimal(19),       // grand total
                             rs.getString(20),            // comments
                             rs.getString(21)             //phone
 
 
                     );
 
 
                     v1.add(ceteDAO);
 
                 }
             } catch (SQLException ex1) {
                 ex1.printStackTrace(System.err);
             }
             return v1;
         }
 
         private ResultSet getItemOptions(Connection connection, String orderid, int item, int orderitem) {
             ResultSet rs = null;
             try {
                 CallableStatement stmt = connection.prepareCall("BEGIN GET_ITEM_OPTIONS(?, ?, ?, ?); END;");
                 stmt.setString(1, orderid);    // order number
                 stmt.setInt(2, item);           // item number
                 stmt.setInt(3, orderitem);           // order item number
                 stmt.registerOutParameter(4, OracleTypes.CURSOR); //REF CURSOR
                 stmt.execute();
                 rs = ((OracleCallableStatement) stmt).getCursor(4);
 
 
             } catch (SQLException ex1) {
                 ex1.printStackTrace(System.err);
             }
             return rs;
         }
 
         private ResultSet getPOPItems(Connection connection) {
             ResultSet rs = null;
             try {
                 CallableStatement stmt = connection.prepareCall("BEGIN GET_POP_ITEMS(?); END;");
                 stmt.registerOutParameter(1, OracleTypes.CURSOR); //REF CURSOR
                 stmt.execute();
                 rs = ((OracleCallableStatement) stmt).getCursor(1);
                 popItems = new ArrayList<Integer>(10);
                 while (rs.next()) {
                     popItems.add(rs.getInt(1));
                 }
 
 
             } catch (SQLException ex1) {
                 ex1.printStackTrace(System.err);
             }
             return rs;
         }
 
         private Vector getLineItems(Connection connection, String orderid) {
             Vector v1 = null;
             try {
 
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT op.id, op.productid, op.qty, p.title, op.price,pagename," +
                                 "REGEXP_REPLACE(SHORT_DESCRIPTION,'<p>','\n') SHORT_DESCRIPTION " +
                                 "FROM HT_ORDER_PRODUCTS op, HT_PRODUCTS p " +
                                 "WHERE op.productid = p.id " +
                                 "AND op.orderid = ?");
 
                 ps.setString(1, orderid);
 
                 ResultSet rs = ps.executeQuery();
                 v1 = new Vector<ProductDAO>(1, 1);
                String s = " Belt & Holster, Cyl Pouch & Ammo Pouch";
 
                 while (rs.next()) {
                     if (rs.getInt(2) == 58713) {
                        s = "Embossed" + s;
                     } else if (rs.getInt(2) == 59308) {
                        s = "Mexican DL" + s;
                     } else {
                         s = rs.getString(4);
                     }
                     ProductDAO productDAO = new ProductDAO(rs.getInt(1), rs.getInt(2),
                             rs.getShort(3), s,
                             rs.getBigDecimal(5), rs.getString(6), rs.getString(7));
                     v1.add(productDAO);
 
                 }
             } catch (SQLException ex1) {
                 ex1.printStackTrace(System.err);
             }
             return v1;
         }
     }
 
 
     class ProductDAO {
         private int _product_order_item;
         private int _product_id;
         private short _product_quantity;
         private String _product_description;
         private BigDecimal _product_price;
         private String _page_name;
         private String _short_description;
 
         public ProductDAO(int product_order_item,
                           int product_id,
                           short product_quantity,
                           String product_description,
                           BigDecimal product_price, String page_name, String short_description) {
 
             this._product_order_item = product_order_item;
             this._product_id = product_id;
             this._product_quantity = product_quantity;
             this._product_description = product_description;
             this._product_price = product_price;
             this._page_name = page_name;
             this._short_description = short_description;
 
 
         }
 
         public int get_product_order_item() {
             return _product_order_item;
         }
 
         public int get_product_id() {
             return _product_id;
         }
 
         public short get_product_quantity() {
             return _product_quantity;
         }
 
         public String get_product_description() {
             return _product_description.replace("<br>", " ");
 
         }
 
         public BigDecimal get_product_price() {
             return _product_price;
         }
 
         public String get_page_name() {
             return _page_name;
         }
 
         public String get_short_description() {
             return _short_description.replace("<br>", " ");
         }
     }
 
     class CeteDAO {
 
         private int orderID;
         private Date orderDate;
         private String clerk;
         private Date printDate;
         private String shipMethod;
 
         private String company;
         private String billName;
         private String billAddress1;
         private String billAddress2;
         private String billCSZ;
 
         private String shipToName;
         private String shipToAddress1;
         private String shipToAddress2;
         private String shipToCSZ;
 
         private BigDecimal subTotal;
         private BigDecimal discount;
         private BigDecimal tax;
 
         private BigDecimal freight;
         private BigDecimal grandTotal;
 
         private String comments;
         private String phone;
 
         public CeteDAO(int orderID,
 
                        Date orderDate,
                        String clerk,
                        Date printDate,
                        String shipMethod,
                        String company,
                        String billToName,
                        String billToAddress1,
                        String billToAddress2,
                        String billToCSZ,
 
                        String shipToName,
                        String shipToAddress1,
                        String shipToAddress2,
                        String shipToCSZ,
                        BigDecimal subTotal,
                        BigDecimal discount,
                        BigDecimal tax,
                        BigDecimal freight,
                        BigDecimal grandTotal,
                        String comments,
                        String phone) {
 
 
             this.orderID = orderID;
             this.orderDate = orderDate;
             this.clerk = clerk;
             this.printDate = printDate;
             this.shipMethod = shipMethod;
 
             this.company = company;
             this.billName = billToName;
             this.billAddress1 = billToAddress1;
             this.billAddress2 = billToAddress2;
             this.billCSZ = billToCSZ;
 
             this.shipToName = shipToName;
             this.shipToAddress1 = shipToAddress1;
             this.shipToAddress2 = shipToAddress2;
             this.shipToCSZ = shipToCSZ;
 
             this.subTotal = subTotal;
             this.discount = discount;
             this.tax = tax;
             this.freight = freight;
             this.grandTotal = grandTotal;
 
             this.comments = comments;
             this.phone = phone;
 
             /* System.out.println("SubTot:"+ getSubTotal() + "disc: " + getDiscount());
      System.out.println(" tax:"+ getTax() + " freight:" + getFreight());*/
         }
 
 
         public int getOrderID() {
             return orderID;
         }
 
         public Date getOrderDate() {
             return orderDate;
         }
 
         public String getClerk() {
             String user = "Unknown";
             if (clerk.equalsIgnoreCase("sonia"))
                 user = "PinkPearl";
 
             if (clerk.equalsIgnoreCase("jessie"))
                 user = "Ejector";
 
             if (clerk.equalsIgnoreCase("maria"))
                 user = "QuickDraw";
 
             if (clerk.equalsIgnoreCase("chanty"))
                 user = "Missfire";
             if (clerk.equalsIgnoreCase("web"))
                 user = "web";
 
             return user;
 
 
         }
 
         public Date getPrintDate() {
             return printDate;
         }
 
         public String getShipMethod() {
             if (shipMethod.length() > 16) {
                 return shipMethod.substring(0, 16);
             } else {
                 return shipMethod;
             }
         }
 
         public String getCompany() {
             return company;
         }
 
 
         public String getBillToName() {
             return billName;
         }
 
         public String getBillToAddress1() {
             return billAddress1;
         }
 
         public String getBillToAddress2() {
             return billAddress2;
         }
 
         public String getBillToCSZ() {
             return billCSZ;
         }
 
         public String getShipToName() {
             return shipToName;
         }
 
         public String getShipToAddress1() {
             return shipToAddress1;
         }
 
         public String getShipToAddress2() {
             return shipToAddress2;
         }
 
         public String getShipToCSZ() {
             return shipToCSZ;
         }
 
 
         public BigDecimal getSubTotal() {
             return subTotal;
         }
 
         public BigDecimal getDiscount() {
             return discount;
         }
 
         public BigDecimal getTax() {
             return tax;
         }
 
         public BigDecimal getFreight() {
             return freight;
         }
 
         public BigDecimal getGrandTotal() {
             return grandTotal;
         }
 
         public String getComments() {
             return comments;
         }
 
         public String getPhone() {
             return phone;
         }
 
 
     }
 
     public Connection DbConn(String key) throws Exception {
         return ConnectionFactory.getConnection(key);
     }
 
 
 }
