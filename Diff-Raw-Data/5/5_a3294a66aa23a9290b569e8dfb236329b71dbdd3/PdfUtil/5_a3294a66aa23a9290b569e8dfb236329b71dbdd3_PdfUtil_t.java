 
 package eionet.meta.exports.pdf;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import com.eteks.awt.PJAToolkit;
 import com.lowagie.text.Chunk;
 import com.lowagie.text.Element;
 import com.lowagie.text.Font;
 import com.lowagie.text.FontFactory;
 import com.lowagie.text.Phrase;
 import com.lowagie.text.Rectangle;
 import com.lowagie.text.pdf.PdfPCell;
 import com.lowagie.text.pdf.PdfPTable;
 
 import eionet.meta.DElemAttribute;
 import eionet.meta.DataElement;
 import eionet.meta.DsTable;
 import eionet.meta.FixedValue;
 import eionet.util.UnicodeEscapes;
 import eionet.util.Util;
 
 public class PdfUtil {
 
     private static final float  MAX_IMG_WIDTH  = 520;
     private static final float  MAX_IMG_HEIGHT = 600;
     private static final int    MAX_VALUE_LEN  = 1500;
 
     public static PdfPTable foreignKeys(Vector fks) throws Exception {
 
         int colCount = 4;
 
         // set up the table
         PdfPTable table = new PdfPTable(colCount);
         table.setHorizontalAlignment(Element.ALIGN_LEFT);
         int[] headerwidths = {20, 20, 20 , 40}; // percentage
         table.setWidthPercentage(100); // percentage
 
         // start adding rows and cells
         int rowCount = 0;
 
         // add caption row
         PdfPCell cell = new PdfPCell(new Phrase("Foreign keys",
                 Fonts.get(Fonts.TBL_CAPTION)));
 
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setColspan(colCount);
         cell.setBorder(Rectangle.NO_BORDER);
 
         table.addCell(cell);
 
         // add header row
         Vector headers = new Vector();
         headers.add("Element");
         headers.add("Table");
         headers.add("Cardinality");
         headers.add("Definition");
         for (int i = 0; i < headers.size(); i++) {
             String header = (String) headers.get(i);
             cell = new PdfPCell(new Phrase(header, Fonts.get(Fonts.TBL_HEADER)));
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setBorder(Rectangle.NO_BORDER);
             cell.setGrayFill(0.4f);
             table.addCell(cell);
         }
 
         // add the fk rows
         Vector fields = new Vector();
         fields.add("elm_name");
         fields.add("tbl_name");
         fields.add("cardin");
         fields.add("definition");
         for (int i = 0; fks != null && i < fks.size(); i++) {
 
             Hashtable fkRel = (Hashtable) fks.get(i);
 
             for (int t = 0; t < fields.size(); t++) {
 
                 String value = (String) fkRel.get(fields.get(t));
                 Phrase phr = process(value, Fonts.get(Fonts.CELL_VALUE));
                 cell = new PdfPCell(phr);
                 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 cell.setPaddingLeft(5);
                 cell.setBorder(Rectangle.NO_BORDER);
 
                 if (i % 2 == 1) {
                     cell.setGrayFill(0.9f);
                 }
 
                 table.addCell(cell);
             }
         }
 
         if (table.size() > 0) {
             return table;
         } else {
             return null;
         }
     }
 
     public static PdfPTable simpleAttributesTable(Vector attrs)
     throws Exception {
         return simpleAttributesTable(attrs, null);
     }
 
     public static PdfPTable simpleAttributesTable(Vector attrs, Vector map)
     throws Exception {
 
         if (attrs == null || attrs.size() == 0) {
             return null;
         }
 
         Vector show = map == null ? new Vector() : (Vector) map.clone();
         // set up the table
         PdfPTable table = new PdfPTable(2);
         table.setHorizontalAlignment(Element.ALIGN_LEFT);
         int[] headerwidths = {25, 75}; // percentage
         table.setWidths(headerwidths);
         table.setWidthPercentage(100); // percentage
 
         // start adding rows and cells
         int rowCount = 0;
         PdfPCell nameCell = null;
         PdfPCell valueCell = null;
         String methodology = null;
 
         for (int i = 0; attrs != null && i < attrs.size(); i++) {
 
             String name = null;
             String dispName = null;
             String value = null;
             //Vector values = null;
 
             Object o = attrs.get(i);
             if (o.getClass().getName().endsWith("Hashtable")) {
                 name = (String) ((Hashtable) o).get("name");
                 dispName = name;
                 value = (String) ((Hashtable) o).get("value");
             } else if (o.getClass().getName().endsWith("DElemAttribute")) {
                 name = ((DElemAttribute) o).getShortName();
                 dispName = ((DElemAttribute) o).getName();
                 value = ((DElemAttribute) o).getValue();
 
                 // JH201103 - skip image attributes, will be treated later
                 String dispType = ((DElemAttribute) o).getDisplayType();
                 if (dispType != null && dispType.equals("image")) {
                     continue;
                 }
             }
 
             if (Util.isEmpty(value) || Util.isEmpty(name)) {
                 continue;
             }
             int pos = map == null ? show.size() : show.indexOf(name);
             if (pos < 0) {
                 continue;
             }
 
             /*if (show != null && !show.contains(name)) continue;
             if (name.equalsIgnoreCase("methodology")) {
                 methodology= value;
                 continue;
             }*/
 
             //int c;
             Vector values = new Vector();
             StringTokenizer st = new StringTokenizer(value, "\n");
             while (st.hasMoreTokens()) {
                 values.add(st.nextToken());
             }
 
             Hashtable hash = new Hashtable();
             hash.put("name", dispName);
             hash.put("values", values);
 
             if (map != null) {
                 show.remove(pos);
             }
             show.add(pos, hash);
         }
 
         /*for (c = 0; c < (value.length() / MAX_VALUE_LEN); c++)
                 values.add(value.substring(c*MAX_VALUE_LEN, (c + 1)*MAX_VALUE_LEN));
             if (value.length() > c*MAX_VALUE_LEN)
                 values.add(value.substring(c*MAX_VALUE_LEN));*/
 
         //if (values == null || values.size() == 0) continue;
         //for (int j = 0; j < values.size(); j++) {
         //String _name = j == 0 ? name : "";
         //nameCell = new PdfPCell(new Phrase(_name, Fonts.get(Fonts.ATTR_TITLE)));
         //valueCell = new PdfPCell(processLinks((String) values.get(j), Fonts.get(Fonts.CELL_VALUE)));
 
         for (int i = 0; show != null && i < show.size(); i++) {
 
             Object o = show.get(i);
             if (!o.getClass().getName().endsWith("Hashtable")) {
                 continue;
             }
             Hashtable hash = (Hashtable) o;
             String dispName = (String) hash.get("name");
             Vector values   = (Vector) hash.get("values");
 
             for (int j = 0; j < values.size(); j++) {
 
                 dispName = j > 0 ? "" : dispName;
                 String value = PdfUtil.rmvCR((String) values.get(j));
 
                 nameCell = new PdfPCell(new Phrase(dispName,
                         Fonts.get(Fonts.ATTR_TITLE)));
                 /*BaseFont bf = BaseFont.createFont("/home/www/webs/datadict/public/WEB-INF/classes/arial.ttf",
                                 BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                 Font font = new Font(bf, 10);*/
                 valueCell = new PdfPCell(process(value,
                         Fonts.get(Fonts.CELL_VALUE)));
                 valueCell.setLeading(9 * 1.2f, 0);
 
                 nameCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                 nameCell.setPaddingRight(5);
                 nameCell.setBorder(Rectangle.NO_BORDER);
                 valueCell.setBorder(Rectangle.NO_BORDER);
 
                 if (rowCount % 2 != 1) {
                     nameCell.setGrayFill(0.9f);
                     valueCell.setGrayFill(0.9f);
                 }
 
                 table.addCell(nameCell);
                 table.addCell(valueCell);
             }
 
             rowCount++;
         }
 
         if (table.size() > 0) {
             return table;
         } else {
             return null;
         }
     }
 
     public static PdfPTable complexAttributeTable(DElemAttribute attr)
     throws Exception {
 
         // get the attribute fields
         Vector fields = attr.getFields();
         if (fields == null || fields.size() == 0) {
             return null;
         }
 
         int fieldCount = fields.size();
 
         // set up the table
         PdfPTable table = new PdfPTable(fieldCount);
         table.setHorizontalAlignment(Element.ALIGN_LEFT);
 
         // set the column widths
         float[] headerwidths = new float[fieldCount];
         for (int i = 0; i < fieldCount; i++) {
             headerwidths[i] = 100 / fieldCount; // percentage
         }
 
         table.setWidths(headerwidths);
         table.setWidthPercentage(100); // percentage
 
         // start adding rows and cells
         int rowCount = 0;
 
         // add caption row
         PdfPCell cell =
             new PdfPCell(new Phrase(attr.getName() == null ? attr.getShortName() : attr.getName(),
                         Fonts.get(Fonts.TBL_CAPTION)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setColspan(fieldCount);
         cell.setBorder(Rectangle.NO_BORDER);
 
         table.addCell(cell);
 
         // add header row
         for (int i = 0; i < fields.size(); i++) {
 
             Hashtable field = (Hashtable) fields.get(i);
             String fieldName = (String) field.get("name");
 
             cell = new PdfPCell(new Phrase(fieldName, Fonts.get(Fonts.TBL_HEADER)));
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setBorder(Rectangle.NO_BORDER);
             cell.setGrayFill(0.4f);
 
             table.addCell(cell);
         }
 
         // add the attribute value rows
         Vector valueRows = attr.getRows();
         for (int i = 0; valueRows != null && i < valueRows.size(); i++) {
 
             Hashtable rowHash = (Hashtable) valueRows.get(i);
 
             for (int t = 0; t < fieldCount; t++) {
                 Hashtable fieldHash = (Hashtable) fields.get(t);
                 String fieldID = (String) fieldHash.get("id");
                 String fieldValue = fieldID == null ? " " : (String) rowHash.get(fieldID);
 
                 if (fieldValue == null) {
                     fieldValue = " ";
                 }
 
                 Phrase phr = process(fieldValue, Fonts.get(Fonts.CELL_VALUE));
                 cell = new PdfPCell(phr);
                 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 cell.setPaddingLeft(5);
                 cell.setBorder(Rectangle.NO_BORDER);
 
                 if (i % 2 == 1) {
                     cell.setGrayFill(0.9f);
                 }
 
                 table.addCell(cell);
             }
         }
 
         if (table.size() > 0) {
             return table;
         } else {
             return null;
         }
     }
 
     public static PdfPTable tableElements(Vector tblElems, Vector captions)
     throws Exception {
         return tableElements(tblElems, captions, null);
     }
 
     public static PdfPTable tableElements(Vector tblElems,
             Vector captions,
             Sectioning sect)
     throws Exception {
 
         if (tblElems == null || tblElems.size() == 0) {
             return null;
         }
 
         // set up the PDF table
         int colCount = sect == null ? 3 : 5;
         PdfPTable table = new PdfPTable(colCount);
         table.setHorizontalAlignment(Element.ALIGN_LEFT);
 
         // set the column widths
         float[] headerwidths1 = {7, 17, 26, 23, 17}; // percentage
         float[] headerwidths2 = {25, 45, 30}; // percentage
         if (sect != null) {
             table.setWidths(headerwidths1);
         } else {
             table.setWidths(headerwidths2);
         }
         table.setWidthPercentage(100); // percentage
 
         // start adding rows and cells
         int rowCount = 0;
         PdfPCell cell = null;
 
         // add caption rows
         for (int i = 0; captions != null && i < captions.size(); i++) {
 
             String caption = (String) captions.get(i);
 
             cell = new PdfPCell(new Phrase(caption, Fonts.get(Fonts.TBL_CAPTION)));
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setColspan(colCount);
             cell.setBorder(Rectangle.NO_BORDER);
 
             table.addCell(cell);
         }
 
         // add header row
         // elem number
         if (sect != null) {
             cell = new PdfPCell(new Phrase(" ", Fonts.get(Fonts.TBL_HEADER)));
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setBorder(Rectangle.NO_BORDER);
             cell.setGrayFill(0.4f);
 
             table.addCell(cell);
         }
 
         // element name
         cell = new PdfPCell(new Phrase("Column name", Fonts.get(Fonts.TBL_HEADER)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setBorder(Rectangle.NO_BORDER);
         cell.setGrayFill(0.4f);
 
         table.addCell(cell);
 
         // definition
         cell = new PdfPCell(new Phrase("Column definition", Fonts.get(Fonts.TBL_HEADER)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setBorder(Rectangle.NO_BORDER);
         cell.setGrayFill(0.4f);
 
         table.addCell(cell);
 
         // methodology
         if (sect != null) {
             cell = new PdfPCell(new Phrase("Methodology", Fonts.get(Fonts.TBL_HEADER)));
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setBorder(Rectangle.NO_BORDER);
             cell.setGrayFill(0.4f);
 
             table.addCell(cell);
         }
 
         // data specs
         cell = new PdfPCell(new Phrase("Data specifications", Fonts.get(Fonts.TBL_HEADER)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setBorder(Rectangle.NO_BORDER);
         cell.setGrayFill(0.4f);
 
         table.addCell(cell);
 
         boolean wasFK = false;
 
         // add value rows
 
         for (int i = 0; i < tblElems.size(); i++) {
 
             DataElement elem = (DataElement) tblElems.get(i);
             String elemType = elem.getType();
 
             String nr = "";
             // sectioning
             if (sect != null) {
                 nr = sect == null ? null : sect.level(" ", 3, false);
                 nr = nr == null ? " " : nr;
 
                 cell = new PdfPCell(new Phrase(nr, Fonts.get(Fonts.CELL_VALUE)));
                 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 cell.setPaddingLeft(5);
 
                 if (i == tblElems.size() - 1) {
                     cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
                 } else {
                     cell.setBorder(Rectangle.LEFT);
                 }
 
                 if (i % 2 == 1) {
                     cell.setGrayFill(0.9f);
                 }
 
                 table.addCell(cell);
             }
 
             // add short name+full name+public or internal+foreign key+GIS
             String s = elem.getAttributeValueByShortName("Name");
             String name = Util.isEmpty(s) ? elem.getShortName() : s;
            String identifier = elem.getIdentifier();
             Vector fks = elem.getFKRelations();
 
             String pori = elem.getAttributeValueByShortName("PublicOrInternal");
             if (pori != null && pori.equalsIgnoreCase("undefined")) {
                 pori = null;
             }
 
             Phrase phr = new Phrase();
             phr.add(process(name + "\n", Fonts.get(Fonts.CELL_VALUE_BOLD)));
            phr.add(process("(" + identifier + ")\n",
                     Fonts.get(Fonts.CELL_VALUE)));
             if (!Util.isEmpty(pori)) {
                 phr.add(new Chunk("\n" + pori,
                         FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
             }
             if (fks != null && fks.size() > 0) {
                 phr.add(new Chunk("\nForeign key",
                         FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
             }
             if (elem.getGIS() != null) {
                 phr.add(new Chunk("\n" + elem.getGIS(),
                         FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
             }
 
             cell = new PdfPCell(phr);
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
 
             if (i == tblElems.size() - 1) {
                 cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
             } else {
                 cell.setBorder(Rectangle.LEFT);
             }
 
             if (i % 2 == 1) {
                 cell.setGrayFill(0.9f);
             }
 
             table.addCell(cell);
 
             // add definition
             String defin = elem.getAttributeValueByShortName("Definition");
             defin = defin == null ? " " : defin;
 
             phr = process(defin, Fonts.get(Fonts.CELL_VALUE));
             cell = new PdfPCell(phr);
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
 
             if (i == tblElems.size() - 1) {
                 cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
             } else {
                 cell.setBorder(Rectangle.LEFT);
             }
 
             if (i % 2 == 1) {
                 cell.setGrayFill(0.9f);
             }
 
             table.addCell(cell);
 
             // add methodology
             if (sect != null) {
                 String method = elem.getAttributeValueByShortName("Methodology");
                 method = method == null ? "" : method;
                 if (elem.hasImages()) {
                     if (method.length() > 0) {
                         method = method + "\n";
                     }
                     method = method + "Illustrations(s): see section " + sect.getRefIllustrations();
                 }
 
                 phr = process(method, Fonts.get(Fonts.CELL_VALUE));
                 cell = new PdfPCell(phr);
                 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 cell.setPaddingLeft(5);
 
                 if (i == tblElems.size() - 1) {
                     cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
                 } else {
                     cell.setBorder(Rectangle.LEFT);
                 }
 
                 if (i % 2 == 1) {
                     cell.setGrayFill(0.9f);
                 }
 
                 table.addCell(cell);
             }
 
             // add data specs
             StringBuffer dataspecs = new StringBuffer();
             String datatype = elem.getAttributeValueByShortName("Datatype");
             if (datatype != null) {
                 dataspecs.append(datatype);
             }
 
 
             if (elemType.equals("CH1")) {
                 Vector fxvs = elem.getFixedValues();
                 if (fxvs == null || fxvs.size() == 0) {
                     if (dataspecs.length() > 0) {
                         dataspecs.append(" codelist");
                     } else {
                         dataspecs.append("Codelist");
                     }
                 } else {
                     if (dataspecs.length() > 0) {
                         dataspecs.append(" codelist:\nsee ");
                     } else {
                         dataspecs.append("Codelist:\nsee ");
                     }
 
                     if (sect == null) {
                         dataspecs.append("below");
                     } else {
                         dataspecs.append("section ").
                         append(sect.getRefCodelists());
                     }
                 }
             } else if (elemType.equals("CH2")) {
                 if (dataspecs.length() > 0) {
                     dataspecs.insert(0, "Datatype: ");
 
                     String[][] ss = {{"MinSize", "Minimum size: "},
                             {"MaxSize", "Maximum size: "},
                             {"MinInclusiveValue", "Minimum inclusive value: "},
                             {"MinExclusiveValue", "Minimum exclusive value: "},
                             {"MaxInclusiveValue", "Maximum inclusive value: "},
                             {"MaxExclusiveValue", "Maximum exclusive value: "},
                             {"DecimalPrecision", "Decimal precision: "},
                             {"Unit", "Unit: "}};
                     for (int k = 0; k < ss.length; k++) {
                         String value =
                             elem.getAttributeValueByShortName(ss[k][0]);
                         if (!Util.isEmpty(value)) {
                             dataspecs.append("\n").
                             append(ss[k][1]).append(value);
                         }
                     }
                 } else {
                     dataspecs = new StringBuffer(" ");
                 }
             }
 
             cell = new PdfPCell(process(dataspecs.toString(),
                     Fonts.get(Fonts.CELL_VALUE)));
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
 
             if (i == tblElems.size() - 1) {
                 cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
             } else {
                 cell.setBorder(Rectangle.LEFT);
             }
 
             if (i % 2 == 1) {
                 cell.setGrayFill(0.9f);
             }
 
             table.addCell(cell);
         }
 
         if (wasFK) {
             String txt = " indicates that the element "
                     + "is participating in a foreign key relationship!\n"
                     + "Look for more in the element factsheet or dataset guideline.";
 
             Phrase phr =  new Phrase();
             Chunk cnk = new Chunk("(FK)", Fonts.get(Fonts.FK_INDICATOR));
             phr.add(cnk);
             cnk = new Chunk(txt, Fonts.get(Fonts.CELL_VALUE));
             phr.add(cnk);
 
             cell = new PdfPCell(phr);
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setColspan(colCount);
             cell.setBorder(Rectangle.NO_BORDER);
 
             table.addCell(cell);
         }
 
         if (table.size() > 0) {
             table.setHeaderRows(1);
             return table;
         } else {
             return null;
         }
     }
 
     public static PdfPTable tableElements(Vector tblElems)
     throws Exception {
 
         return tableElements(tblElems, null);
     }
 
     public static PdfPTable codelist(Vector fxvs) throws Exception {
 
 
         if (fxvs == null || fxvs.size() == 0) {
             return null;
         }
 
         // set up the PDF table
         PdfPTable table = new PdfPTable(3);
         table.setHorizontalAlignment(Element.ALIGN_LEFT);
 
         // set the column widths
         float[] headerwidths = {16, 42, 42}; // percentage
         table.setWidths(headerwidths);
         table.setWidthPercentage(100); // percentage
 
         // add header row
 
         // value
         PdfPCell cell =
             new PdfPCell(new Phrase("Value", Fonts.get(Fonts.TBL_HEADER)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setBorder(Rectangle.NO_BORDER);
         cell.setGrayFill(0.4f);
 
         table.addCell(cell);
 
         // definition
         cell =
             new PdfPCell(new Phrase("Definition", Fonts.get(Fonts.TBL_HEADER)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setBorder(Rectangle.NO_BORDER);
         cell.setGrayFill(0.4f);
 
         table.addCell(cell);
 
         // short description
         cell =
             new PdfPCell(new Phrase("Short Description", Fonts.get(Fonts.TBL_HEADER)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setBorder(Rectangle.NO_BORDER);
         cell.setGrayFill(0.4f);
 
         table.addCell(cell);
 
         for (int i = 0; i < fxvs.size(); i++) {
             FixedValue fxv = (FixedValue) fxvs.get(i);
             String val = fxv.getValue();
             if (Util.isEmpty(val)) {
                 continue;
             }
 
             String def = fxv.getDefinition();
             def = def == null ? "" : def;
 
             String desc = fxv.getShortDesc();
             desc = desc == null ? "" : desc;
 
             // value cell
             Phrase phr = process(val, Fonts.get(Fonts.CELL_VALUE));
             cell = new PdfPCell(phr);
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
 
             if (i == fxvs.size() - 1) {
                 cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
             } else {
                 cell.setBorder(Rectangle.LEFT);
             }
 
             if (i % 2 == 1) {
                 cell.setGrayFill(0.9f);
             }
 
             table.addCell(cell);
 
             // definition cell
             phr = process(def, Fonts.get(Fonts.CELL_VALUE));
             cell = new PdfPCell(phr);
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
 
             if (i == fxvs.size() - 1) {
                 cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
             } else {
                 cell.setBorder(Rectangle.LEFT);
             }
 
             if (i % 2 == 1) {
                 cell.setGrayFill(0.9f);
             }
 
             table.addCell(cell);
 
             // short description cell
             phr = process(desc, Fonts.get(Fonts.CELL_VALUE));
             cell = new PdfPCell(phr);
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
 
             if (i == fxvs.size() - 1) {
                 cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
             } else {
                 cell.setBorder(Rectangle.LEFT);
             }
 
             if (i % 2 == 1) {
                 cell.setGrayFill(0.9f);
             }
 
             table.addCell(cell);
         }
 
         if (table.size() > 0) {
             table.setHeaderRows(1);
             return table;
         } else {
             return null;
         }
     }
 
     public static PdfPTable vsTable(String filePath, String vsTitle)
     throws Exception {
 
         com.lowagie.text.Image vsImage = vsImage(filePath);
         if (vsImage == null) {
             return null;
         }
 
         PdfPTable table = new PdfPTable(1);
         int[] headerwidths = {100}; // percentage
         table.setWidths(headerwidths);
         table.setWidthPercentage(100); // percentage
 
         PdfPCell cell = null;
 
         if (!Util.isEmpty(vsTitle)) {
             cell = new PdfPCell(new Phrase(vsTitle, Fonts.get(Fonts.HEADING_0)));
             cell.setPaddingRight(0);
             cell.setPaddingLeft(0);
             cell.setPaddingTop(0);
             cell.setPaddingBottom(0);
             cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
             table.addCell(cell);
 
             cell = new PdfPCell(new Phrase(" ", Fonts.get(Fonts.HEADING_0)));
             cell.setPaddingRight(0);
             cell.setPaddingLeft(0);
             cell.setPaddingTop(0);
             cell.setPaddingBottom(0);
             cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
             table.addCell(cell);
         }
 
         cell = new PdfPCell(vsImage, true);
         cell.setPaddingRight(0);
         cell.setPaddingLeft(0);
         cell.setPaddingTop(0);
         cell.setPaddingBottom(0);
         cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
         table.addCell(cell);
 
         if (table.size() > 0) {
             return table;
         } else {
             return null;
         }
     }
 
     public static PdfPTable fixedValuesTable(Vector fxValues, boolean levelled)
     throws Exception {
 
         if (fxValues == null || fxValues.size() == 0) {
             return null;
         }
 
         // set up the PDF table
         PdfPTable table = new PdfPTable(2);
         table.setHorizontalAlignment(Element.ALIGN_LEFT);
 
         // set the column widths
         float[] headerwidths = {30, 70}; // percentage
         table.setWidths(headerwidths);
         table.setWidthPercentage(100); // percentage
 
         // start adding rows and cells
 
         // add header row
 
         // value
         PdfPCell cell = new PdfPCell(new Phrase("Value", Fonts.get(Fonts.TBL_HEADER)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setBorder(Rectangle.NO_BORDER);
         cell.setGrayFill(0.4f);
 
         table.addCell(cell);
 
         // definition
         cell = new PdfPCell(new Phrase("Definition", Fonts.get(Fonts.TBL_HEADER)));
         cell.setHorizontalAlignment(Element.ALIGN_LEFT);
         cell.setPaddingLeft(5);
         cell.setBorder(Rectangle.NO_BORDER);
         cell.setGrayFill(0.4f);
 
         table.addCell(cell);
 
         // add value rows
         for (int i = 0; i < fxValues.size(); i++) {
 
             FixedValue fxv = (FixedValue) fxValues.get(i);
 
             // value
             Phrase phr = process(fxv.getValue(), Fonts.get(Fonts.CELL_VALUE));
             cell = new PdfPCell(phr);
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setBorder(Rectangle.NO_BORDER);
 
             if (i % 2 == 1) {
                 cell.setGrayFill(0.9f);
             }
 
             table.addCell(cell);
 
             // definition
             String definition = fxv.getDefinition();
             if (Util.isEmpty(definition)) {
                 definition = " ";
             }
             phr = process(definition, Fonts.get(Fonts.CELL_VALUE));
             cell = new PdfPCell(phr);
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setBorder(Rectangle.NO_BORDER);
 
             if (i % 2 == 1) {
                 cell.setGrayFill(0.9f);
             }
 
             table.addCell(cell);
         }
 
         if (table.size() > 0) {
             return table;
         } else {
             return null;
         }
     }
 
     public static PdfPTable tablesList(Vector tables) throws Exception {
         return tablesList(tables, null);
     }
 
     public static PdfPTable tablesList(Vector tables, Vector headers)
     throws Exception {
 
         // get the attribute fields
         if (tables == null || tables.size() == 0) {
             return null;
         }
 
         if (headers == null) {
             headers = new Vector();
             Hashtable hash = new Hashtable();
             hash.put("attr", "Name");
             hash.put("title", "Name");
             hash.put("width", "30");
             headers.add(hash);
             hash = new Hashtable();
             hash.put("attr", "Definition");
             hash.put("title", "Definition");
             hash.put("width", "35");
             headers.add(hash);
             hash = new Hashtable();
             hash.put("attr", "ShortDescription");
             hash.put("title", "Short description");
             hash.put("width", "35");
             headers.add(hash);
         }
 
         // set up the table
         PdfPTable table = new PdfPTable(headers.size());
         table.setHorizontalAlignment(Element.ALIGN_LEFT);
 
         // set the column widths
         float[] headerwidths = new float[headers.size()];
         for (int i = 0; i < headers.size(); i++) {
             Hashtable hash = (Hashtable) headers.get(i);
             String _width = (String) hash.get("width");
             headerwidths[i] = Float.parseFloat(_width);
         }
         table.setWidths(headerwidths);
         table.setWidthPercentage(100); // percentage
 
         // add header row
         PdfPCell cell = null;
         for (int i = 0; i < headers.size(); i++) {
 
             Hashtable hash = (Hashtable) headers.get(i);
             String title = (String) hash.get("title");
 
             cell = new PdfPCell(new Phrase(title, Fonts.get(Fonts.TBL_HEADER)));
             cell.setHorizontalAlignment(Element.ALIGN_LEFT);
             cell.setPaddingLeft(5);
             cell.setBorder(Rectangle.NO_BORDER);
             cell.setGrayFill(0.4f);
             table.addCell(cell);
         }
 
         // add the the rest of rows
         for (int i = 0; tables != null && i < tables.size(); i++) {
 
             DsTable dsTable = (DsTable) tables.get(i);
 
             for (int j = 0; j < headers.size(); j++) {
 
                 Hashtable hash = (Hashtable) headers.get(j);
                 String attr = (String) hash.get("attr");
 
                 String val = dsTable.getAttributeValueByShortName(attr);
                 val = Util.isEmpty(val) ? "" : val;
                 Phrase phr = process(val, Fonts.get(Fonts.CELL_VALUE));
                 cell = new PdfPCell(phr);
                 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 cell.setPaddingLeft(5);
                 cell.setBorder(Rectangle.NO_BORDER);
 
                 if (i % 2 == 1) {
                     cell.setGrayFill(0.9f);
                 }
 
                 table.addCell(cell);
             }
         }
 
         if (table.size() > 0) {
             table.setHeaderRows(1);
             return table;
         } else {
             return null;
         }
     }
 
     //public static PdfPTable imgAttributes(Vector attrs, String imgPath)
     public static Vector imgAttributes(Vector attrs, String imgPath)
     throws Exception {
 
         if (imgPath == null || attrs == null || attrs.size() == 0) {
             return null;
         }
 
         // set up the table
         PdfPTable table = new PdfPTable(1);
         table.setHorizontalAlignment(Element.ALIGN_LEFT);
         table.setWidthPercentage(100); // percentage
 
         Vector imgVector = new Vector();
 
         for (int i = 0; attrs != null && i < attrs.size(); i++) {
 
             Object o = attrs.get(i);
             if (o.getClass().getName().endsWith("Hashtable")) {
                 continue;
             }
 
             DElemAttribute attr = (DElemAttribute) o;
             String dispType = attr.getDisplayType();
             if (dispType == null || !dispType.equals("image")) {
                 continue;
             }
 
             String name = attr.getShortName();
             Vector values = attr.getValues();
 
             //PdfPCell cell = null;
             for (int j = 0; values != null && j < values.size(); j++) {
 
                 String value = (String) values.get(j);
                 /*String nrName = name + " #" + String.valueOf(j + 1);
                 nrName = "";
 
                 // add row for name
                 cell = new PdfPCell(
                             new Phrase(nrName, Fonts.get(Fonts.TBL_CAPTION)));
                 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 cell.setPaddingLeft(5);
                 cell.setBorder(Rectangle.NO_BORDER);
                 table.addCell(cell);*/
 
                 // add image
                 String filePath = imgPath + value;
                 com.lowagie.text.Image vsImage = vsImage(filePath);
                 if (vsImage != null) {
                     imgVector.add(vsImage);
                 }
 
                 /*cell = new PdfPCell(vsImage);
                 cell.setPaddingRight(0);
                 cell.setPaddingLeft(0);
                 cell.setPaddingTop(0);
                 cell.setPaddingBottom(0);
                 cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                 table.addCell(cell);
 
                 // add line
                 cell = new PdfPCell(
                             new Phrase(" ", Fonts.get(Fonts.CELL_VALUE)));
                 cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                 cell.setPaddingLeft(5);
                 cell.setBorder(Rectangle.NO_BORDER);
                 table.addCell(cell);*/
 
             }
         }
 
         //if (table.size() > 0)
         //return table;
         if (imgVector.size() > 0) {
             return imgVector;
         } else {
             return null;
         }
     }
 
     public static com.lowagie.text.Image vsImage(String filePath)
     throws Exception {
 
         if (Util.isEmpty(filePath)) {
             return null;
         }
 
         // we're using PJA's toolkit, because iText cannot handle some
         // of the GIFs and Java needs X11 on Linux
 
         // get old properties
         String propToolkit = System.getProperty("awt.toolkit");
         String propGraphics = System.getProperty("java.awt.graphicsenv");
         String propFonts = System.getProperty("java.awt.fonts");
 
         // set new properties
         System.setProperty("awt.toolkit", "com.eteks.awt.PJAToolkit");
         System.setProperty("java.awt.graphicsenv", "com.eteks.java2d.PJAGraphicsEnvironment");
         System.setProperty("java.awt.fonts", System.getProperty("user.dir"));
 
         try {
             PJAToolkit kit = new PJAToolkit();
 
             // create java.awt.Image
             java.awt.Image jImg = kit.createImage(filePath);
             if (jImg == null) {
                 return null;
             }
 
             // of the java.awt.Image, create com.lowagie.text.Image
             com.lowagie.text.Image vsImage =
                 com.lowagie.text.Image.getInstance(jImg, null);
             vsImage.setAlignment(com.lowagie.text.Image.LEFT);
 
             float width = vsImage.getScaledWidth();
             float height = vsImage.getScaledHeight();
 
             if (width > MAX_IMG_WIDTH) {
                 vsImage.scaleAbsoluteWidth(MAX_IMG_WIDTH);
             }
             if (height > MAX_IMG_HEIGHT) {
                 vsImage.scaleAbsoluteHeight(MAX_IMG_HEIGHT);
             }
 
             // reset old properties
             if (propToolkit != null) {
                 System.setProperty("awt.toolkit", propToolkit);
             }
             if (propGraphics != null) {
                 System.setProperty("java.awt.graphicsenv", propGraphics);
             }
             if (propFonts != null) {
                 System.setProperty("java.awt.fonts", propFonts);
             }
 
             return vsImage;
         } catch (Exception e) {
 
             // reset old properties
             if (propToolkit != null) {
                 System.setProperty("awt.toolkit", propToolkit);
             }
             if (propGraphics != null) {
                 System.setProperty("java.awt.graphicsenv", propGraphics);
             }
             if (propFonts != null) {
                 System.setProperty("java.awt.fonts", propFonts);
             }
 
             throw e;
         }
     }
 
     public static Phrase processLinks(String value, Font font) throws Exception {
 
         Phrase phr = new Phrase();
 
         StringTokenizer st = new StringTokenizer(value, " \t\n\r\f", true);
         while (st.hasMoreTokens()) {
             String s = st.nextToken();
             if (!isLink(s)) {
                 phr.add(new Chunk(s, font));
             } else {
                 Chunk ch = new Chunk(s, Fonts.get(Fonts.ANCHOR));
                 ch.setAnchor(s);
 
                 phr.add(ch);
             }
         }
 
         return phr;
     }
 
     private static boolean isLink(String s) {
         try {
             URL url = new URL(s);
         } catch (MalformedURLException e) {
             return false;
         }
 
         return true;
     }
 
     public static String rmvCR(String s) {
 
         if (s == null) {
             return s;
         }
 
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             if (c != '\r') {
                 buf.append(c);
             }
         }
 
         return buf.toString();
     }
 
     private static Phrase process(String value, Font font) throws Exception {
 
         if (value == null) {
             return new Phrase();
         }
 
         String s = processUnicode(value);
         return processLinks(s, font);
     }
 
     public static String processUnicode(String s) {
 
         if (s == null) {
             return null;
         }
 
         StringBuffer buf = new StringBuffer();
         UnicodeEscapes unicodeEscapes = new UnicodeEscapes();
         for (int i = 0; i < s.length(); i++) {
 
             char c = s.charAt(i);
             String tk = s.substring(i, i + 1);
 
             if (c == '&') {
                 int j = s.indexOf(";", i);
                 if (j > i) {
                     char cc = s.charAt(i + 1);
                     int decimal = -1;
                     if (cc == '#') {
                         // handle Unicode escape
                         String sDecimal = s.substring(i + 2, j);
                         try {
                             decimal = Integer.parseInt(sDecimal);
                         } catch (Exception e) {
                         }
                     } else {
                         // handle entity
                         String ent = s.substring(i + 1, j);
                         decimal = unicodeEscapes.getDecimal(ent);
                     }
 
                     if (decimal >= 0) {
                         // if decimal was found
                         c = (char) decimal;
                         i = j;
                     }
                 }
             }
 
             buf.append(c);
         }
 
         //if (s.startsWith("&#1081")) return "\u1081\u1094";
 
         return buf.toString();
     }
 
     public static void main(String[] args) {
     }
 }
