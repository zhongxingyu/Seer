 import com.cete.dynamicpdf.*;
 import com.cete.dynamicpdf.pageelements.*;
 
 import java.io.FileNotFoundException;
 import java.math.BigDecimal;
 import java.sql.*;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Enumeration;
 import java.util.Vector;
 
 public class ROInvoice {
 
 
     Connection connection;
 
     public ROInvoice() throws Exception {
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
         ROInvoice invoice = null;
         com.cete.dynamicpdf.Document.addLicense(licKey);
         try {
             invoice = new ROInvoice();
         } catch (Exception e) {
             System.out.println("error: " + e.toString());
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         // Add Invoices to the document
         if (invoice != null) {
             invoice.drawInvoice(objDocument, orderid);
         }
 
         // Outputs the Invoices to the file.
         objDocument.draw("ROInvoice.pdf");
         try {
             if (!invoice.connection.isClosed())
                 invoice.connection.close();
         } catch (SQLException e) {
             System.out.print("Error: " + e.toString());
 
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
         boolean overflow = false;
         private String overFlowText;
 
         private Template template = new Template();
         private boolean pageTemplateImagesSet = false;
         private RgbColor objBGColor = new WebColor("#E0E0FF");
         private WebColor objTotalBGColor = new WebColor("#FFC0C0");
         private WebColor objBorderColor = new WebColor("#000000");
         private WebColor objThankYouText = new WebColor("#000080");
         private CeteDAO ceteDAO = null;
         //private ProductDAO productDAO = null;
 
         public MyInvoice() {
             //LayoutGrid grid; //Default is decimal
             //grid = new LayoutGrid();
             //  template.getElements().add(grid);
             // Top part of Invoice
 
             template.getElements().add(new Label("Heritage Manufacturing, Inc.", 0, 20, 540, 18, Font.getHelveticaBold(), 14, TextAlign.CENTER));
             template.getElements().add(new Label("4600 NW 135th Street", 0, 36, 540, 18, Font.getHelvetica(), 12, TextAlign.CENTER));
             template.getElements().add(new Label("Opa Locka, FL  33054", 0, 50, 540, 18, Font.getHelvetica(), 12, TextAlign.CENTER));
             template.getElements().add(new Label("Tel: 305.685.5966", 0, 64, 540, 18, Font.getHelvetica(), 12, TextAlign.CENTER));
             template.getElements().add(new Label("Fax: 305.687.6721", 0, 78, 540, 18, Font.getHelvetica(), 12, TextAlign.CENTER));
 
             template.getElements().add(new Label(" ", 0, 0, 540, 18, Font.getHelveticaBold(), 18, TextAlign.RIGHT));
 
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
             objGroup.add(new Label("Serial #:", 366, 39, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Model:", 366, 53, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Date:", 366, 67, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Ship Via:", 366, 81, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Payment:", 366, 95, 85, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
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
                 objGroup.add(new Rectangle(0, 306 + i * 36, 540, 18, objBGColor, objBGColor));
             }
             objGroup.add(new Rectangle(450, 250, 90, 20, 0.5f));
             objGroup.add(new Rectangle(450, 702, 90, 18, objTotalBGColor, objTotalBGColor));
             objGroup.add(new Rectangle(0, 270, 540, 450, 0.5f));
 
             objGroup.add(new Line(0, 288, 540, 288, 0.5f));
             objGroup.add(new Line(0, 630, 540, 630, 0.5f));
 
             objGroup.add(new Line(360, 630, 360, 720, 0.5f));
             objGroup.add(new Line(450, 270, 450, 720, 0.5f));
             objGroup.add(new Line(450, 702, 540, 702, 0.5f));
 
             //objGroup.add(new Label("Quantity", 0, 272, 60, 12, Font.getHelveticaBold(), 12, TextAlign.CENTER));
             objGroup.add(new Label("Description of Work", 0, 272, 360, 12, Font.getHelveticaBold(), 12, TextAlign.CENTER));
             // objGroup.add(new Label("Price", 360, 272, 90, 12, Font.getHelveticaBold(), 12, TextAlign.CENTER));
             objGroup.add(new Label("Price", 450, 272, 90, 12, Font.getHelveticaBold(), 12, TextAlign.CENTER));
 
             objGroup.add(new Label("Sub Total", 364, 632, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Discount", 364, 650, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Sales Tax", 364, 668, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Freight", 364, 686, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             objGroup.add(new Label("Total", 364, 704, 82, 12, Font.getHelveticaBold(), 12, TextAlign.RIGHT));
             return objGroup;
         }
 
         private void setPageTemplateImage() {
             // Adds the image to page template if it is not already added
             if (!pageTemplateImagesSet) {
                 try {
                     // template.getElements().add(new Image(getServletContext().getRealPath("images/logo_mg.gif"), 0, 0, 0.85f));
                     template.getElements().add(new Image("/usr/local/apache-tomcat/webapps/HMI/WEB-INF/images/logo_mg.gif", 0, 0, 0.85f));
                    //  template.getElements().add(new Image("images/logo_mg.gif", 0, 0, 0.85f));
                 } catch (FileNotFoundException ex) {
                     ex.printStackTrace(System.err);
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
 
             while (e2.hasMoreElements()) {
                 // Break if at the bottom of the page
 
                 if (yOffset >= 594) {
                     invoiceFinished = false;
                     break;
                 }
                 if (overflow) {
                     overflow = false;
                     drawLineItem(new ProductDAO(overFlowText, new BigDecimal(0)), objPage, orderid);
                 } else {
                     drawLineItem((ProductDAO) e2.nextElement(), objPage, orderid);
                 }
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
             page.getElements().add(new Label(ceteDAO.getSerialNumber(), 460, 39, 100, 12, Font.getHelvetica(), 12));
             page.getElements().add(new Label(ceteDAO.getModelNumber(), 460, 53, 100, 12, Font.getHelvetica(), 12));
             Date date = ceteDAO.getShip_Date();
             if (date != null) {
                 page.getElements().add(new Label(dateFormat.format(date), 460, 67, 100, 12, Font.getHelvetica(), 12));
             }
             page.getElements().add(new Label(ceteDAO.getShipMethod(), 460, 81, 100, 24, Font.getHelvetica(), 12));
             page.getElements().add(new Label(ceteDAO.getPayMethod(), 460, 95, 100, 24, Font.getHelvetica(), 12));
 
         }
 
         private void drawBillTo(CeteDAO ceteDAO, Page page) {
             String billToAddress = "";
 
             billToAddress += ceteDAO.getBillToName() + "\n" +
                     ceteDAO.getBillToAddress1() + "\n";
             if (ceteDAO.getBillToAddress2() != null) {
                 billToAddress += ceteDAO.getBillToAddress2() + "\n";
             }
             billToAddress += ceteDAO.getBillToCSZ();
 
 
             page.getElements().add(new TextArea(billToAddress, 3, 139, 194, 70, Font.getHelvetica(), 12));
         }
 
         private void drawShipTo(CeteDAO ceteDAO, Page page) {
 
             // Adds ship to address
             String shipToAddress = ceteDAO.getBillToName() + "\n" +
                     ceteDAO.getBillToAddress1() + "\n";
             if (ceteDAO.getBillToAddress2() != null) {
                 shipToAddress += ceteDAO.getBillToAddress2() + "\n";
                 shipToAddress += ceteDAO.getBillToCSZ() + "\n";
             } else {
                 shipToAddress += ceteDAO.getBillToCSZ() + "\n\n";
             }
 
             shipToAddress += ceteDAO.getPhone();
 
 
             page.getElements().add(new TextArea(shipToAddress, 343, 139, 194, 72, Font.getHelvetica(), 12));
 
         }
 
         private void drawComments(CeteDAO ceteDAO, Page page) {
             // add notes
             String comments = ceteDAO.getComplaint();
             page.getElements().add(new TextArea(comments, 10, 220, 300, 49, Font.getHelvetica(), 10));
 
             page.getElements().add(new TextArea(ceteDAO.getNotes(), 4, 632, 350, 90, Font.getHelveticaBold(), 10, objThankYouText));
             if (ceteDAO.getDoctype().equals("Order")) {
                 page.getElements().add(new Label("Invoice", 400, 0, 140, 24, Font.getHelveticaBold(), 24, TextAlign.LEFT));
             } else {
                 page.getElements().add(new Label("Estimate", 400, 0, 140, 24, Font.getHelveticaBold(), 24, TextAlign.LEFT));
             }
         }
 
         private void drawLineItem(ProductDAO productDAO, Page page, String orderid) throws SQLException {
 
             BigDecimal unitPrice;
             unitPrice = new BigDecimal(0);
             unitPrice = productDAO.get_product_price();
 
             TextArea ta = new TextArea(productDAO.get_product_description(), 14, 3 + yOffset, 436, 12, Font.getHelvetica(), 12);
             page.getElements().add(ta);
 
             unitPrice = unitPrice.setScale(2, BigDecimal.ROUND_HALF_EVEN);
 
             DecimalFormat df = new DecimalFormat("#,##0.00");
             if (unitPrice.compareTo(new BigDecimal(0.0)) != 0) {
                 page.getElements().add(new Label(df.format(unitPrice.doubleValue()), 454, 3 + yOffset, 82, 12,
                         Font.getHelvetica(), 12, TextAlign.RIGHT));
             }
             yOffset += 18;
             // keep printing description for this item until all done
             while (((ta = ta.getOverflowTextArea(14, 3 + yOffset, 436, 12)) != null) && (yOffset <= 594)) {
                 page.getElements().add(ta);
                 yOffset += 18;
             }
 
             if (ta != null) {
                 overFlowText = ta.getText();
                 overflow = true;
                 yOffset -= 18;
                 System.out.println("=>" + overFlowText + "<");
             }
 
         }
 
 
         private void drawTotals(CeteDAO ceteDAO, Page page) {
             // Add totals to the bottom of the Invoice
             subTotal = ceteDAO.getsubTotal();
             DecimalFormat df = new DecimalFormat("#,##0.00");
 
             page.getElements().add(new Label(df.format(subTotal.doubleValue()), 454, 631, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
             page.getElements().add(new Label("-" + df.format(ceteDAO.getDiscount().doubleValue()), 454, 649, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
             page.getElements().add(new Label(df.format(ceteDAO.getTax().doubleValue()), 454, 667, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
             page.getElements().add(new Label(df.format(ceteDAO.getFreight().doubleValue()), 454, 685, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
             BigDecimal GT = subTotal.subtract(ceteDAO.getDiscount());
             GT = GT.add(ceteDAO.getTax()).add(ceteDAO.getFreight());
             page.getElements().add(new Label(df.format(GT.doubleValue()), 454, 703, 82, 12, Font.getHelvetica(), 12,
                     TextAlign.RIGHT));
         }
 
         private Vector getInvoiceData(Connection connection, String orderid) {
             Vector v1 = null;
             try {
 
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT orderid," +
                                 "shipvia," +
                                 "serial_number," +
                                 "model_number," +
                                 "NVL(complaint,' ') COMPLAINT," +
                                 "NVL(notes,'No Additional  Notes') NOTES," +
                                 "pay_method," +
                                 "NVL(DISCOUNT,0) DISCOUNT," +
                                 "NVL(freight,0) FREIGHT," +
                                 "NVL(tax,0) TAX ," +
                                 "alt_name," +
                                 "alt_address1," +
                                 "alt_address2," +
                                 "alt_city||','||alt_state||' '||alt_zip CSZ," +
                                "NVL(alt_phone,'0000000000') alt_phone ,SYSDATE,subtotal,doctype " +
                                 "from rlorders o, rlcustomers c " +
                                 "WHERE orderid = ? " +
                                 "AND o.customerid = c.id");
 
                 ps.setString(1, orderid);     // Phone Order
 
                 ResultSet rs = ps.executeQuery();
                 v1 = new Vector(1, 1);
                 while (rs.next()) {
                     CeteDAO ceteDAO = new CeteDAO(
                             rs.getInt("orderid"),
                             rs.getString(2),
                             rs.getString(3),
                             rs.getString(4),
                             rs.getString(5),
                             rs.getString(6),
                             rs.getString(7),
 
                             rs.getBigDecimal("discount"),
                             rs.getBigDecimal("freight"),
                             rs.getBigDecimal("tax"),
 
                             rs.getString(11),
                             rs.getString(12),
                             rs.getString(13),
                             rs.getString(14),
                             rs.getString(15),
                             rs.getDate(16), rs.getBigDecimal("subtotal"), rs.getString("doctype"));
 
                     v1.add(ceteDAO);
 
                 }
             } catch (SQLException ex1) {
                 ex1.printStackTrace(System.err);
             }
             return v1;
         }
 
         private Vector getLineItems(Connection connection, String orderid) {
             Vector v1 = null;
             try {
 
                 PreparedStatement ps = connection.prepareStatement(
                         "select description, price  from rldetails where orderid = ?");
 
                 ps.setString(1, orderid);
 
                 ResultSet rs = ps.executeQuery();
                 v1 = new Vector(1, 1);
                 while (rs.next()) {
                     ProductDAO productDAO = new ProductDAO(rs.getString(1), rs.getBigDecimal(2));
                     v1.add(productDAO);
 
                 }
             } catch (SQLException ex1) {
                 ex1.printStackTrace(System.err);
             }
             return v1;
         }
     }
 
 
     class ProductDAO {
 
         private String _product_description;
         private BigDecimal _product_price;
 
 
         public ProductDAO(String product_description, BigDecimal product_price) {
 
 
             this._product_description = product_description;
             this._product_price = product_price;
 
 
         }
 
 
         public String get_product_description() {
             return _product_description;
         }
 
         public BigDecimal get_product_price() {
             return _product_price;
         }
 
     }
 
     class CeteDAO {
 
         private int orderID;
         private String shipMethod;
         private String serialNumber;
         private String modelNumber;
         private String complaint;
         private String notes;
         private String payMethod;
         private BigDecimal subtotal;
         private BigDecimal discount;
         private BigDecimal tax;
         private BigDecimal freight;
 
         private String billName;
         private String billAddress1;
         private String billAddress2;
         private String billCSZ;
         private String phone;
         private Date ship_Date;
         private String doctype;
 
 
         // private BigDecimal grandTotal;
 
 
         public CeteDAO(int orderID, String shipMethod, String serialNumber,
                        String modelNumber, String complaint, String notes,
                        String payMethod, BigDecimal discount, BigDecimal freight,
                        BigDecimal tax, String billName, String billAddress1,
                        String billAddress2, String billCSZ, String phone, Date ship_Date, BigDecimal subtotal, String doctype) {
 
 
             this.orderID = orderID;
             this.shipMethod = shipMethod;
             this.serialNumber = serialNumber;
             this.modelNumber = modelNumber;
             this.complaint = complaint;
             this.notes = notes;
             this.payMethod = payMethod;
 
             this.discount = discount;
             this.tax = tax;
             this.freight = freight;
             this.subtotal = subtotal;
 
             this.billName = billName;
             this.billAddress1 = billAddress1;
             this.billAddress2 = billAddress2;
             this.billCSZ = billCSZ;
             this.phone = phone;
             this.ship_Date = ship_Date;
             this.doctype = doctype;
 
 
         }
 
 
         public int getOrderID() {
             return orderID;
         }
 
         public String getShipMethod() {
 
             return shipMethod;
 
         }
 
         public String getSerialNumber() {
             return serialNumber;
 
         }
 
         private String getModelNumber() {
             return modelNumber;
         }
 
         public String getComplaint() {
             return complaint;
         }
 
         public String getNotes() {
             return notes;
         }
 
         public String getPayMethod() {
             return payMethod;
         }
 
         public BigDecimal getsubTotal() {
             return subtotal;
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
 
 
         public String getPhone() {
             return "(" + phone.substring(0, 3) + ") " + phone.subSequence(3, 6) + "-" + phone.substring(6, 10);
         }
 
         public Date getShip_Date() {
             return ship_Date;
         }
 
         public String getDoctype() {
             return doctype;
         }
 
     }
 
     public Connection DbConn(String key) throws Exception {
         return ConnectionFactory.getConnection(key);
     }
 
 
 }
