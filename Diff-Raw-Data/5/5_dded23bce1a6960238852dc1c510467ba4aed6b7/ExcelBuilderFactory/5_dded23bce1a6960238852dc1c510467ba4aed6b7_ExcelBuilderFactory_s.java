 package jp.co.nttcom.camel.documentbuilder.factory.excel;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import jp.co.nttcom.camel.documentbuilder.factory.BuilderFactory;
 import jp.co.nttcom.camel.documentbuilder.xml.model.Component;
 import jp.co.nttcom.camel.documentbuilder.xml.model.Model;
 import net.sf.jxls.transformer.XLSTransformer;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CreationHelper;
 import org.apache.poi.ss.usermodel.Hyperlink;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 public class ExcelBuilderFactory extends BuilderFactory {
 
     @Override
     protected void doBuild(Model model, File dir) throws IOException {
         List<ViewComponent> viewComponents = convertForView(model);
         OutputStream os = null;
         try {
            Workbook workbook = createworkbook(viewComponents);
             updateIndex(workbook);
             os = new BufferedOutputStream(new FileOutputStream(new File(dir, "component.xls")));
             workbook.write(os);
         } finally {
             if (os != null) {
                 os.close();
             }
         }
     }
 
     private List<ViewComponent> convertForView(Model model) {
         List<ViewComponent> components = new ArrayList<ViewComponent>();
         for (Component component : model.getComponents()) {
             ViewComponent vc = new ViewComponent(component);
             components.add(vc);
         }
         return components;
     }
 
    private Workbook createworkbook(List<ViewComponent> components) throws IOException {
         // 各コンポーネントの情報を表示するシートの名称を設定
         List<String> sheetNames = new ArrayList<String>();
         for (ViewComponent component : components) {
             String sheetName = component.getName();
             sheetNames.add(sheetName);
         }
 
         // コンポーネント一覧
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("comps", components);
 
         InputStream is = null;
         Workbook workbook;
         try {
             is = new BufferedInputStream(
                     ExcelBuilderFactory.class.getResourceAsStream("/template/excel/component.xls"));
             XLSTransformer tf = new XLSTransformer();
             workbook = tf.transformMultipleSheetsList(is, components, sheetNames, "component", map, 3);
         } catch (InvalidFormatException ex) {
             throw new IOException(ex);
         } finally {
             if (is != null) {
                 is.close();
             }
         }
 
         return workbook;
     }
 
     private void updateIndex(Workbook workbook) throws IOException {
         Sheet listSheet = workbook.getSheet("コンポーネント一覧");
         CreationHelper helper = workbook.getCreationHelper();
         for (int i = 4;; i++) {
             Row row = listSheet.getRow(i);
             if (row == null) {
                 break;
             }
             Cell cell = row.getCell(2);
             String cellValue = cell.getStringCellValue();
             Hyperlink link = helper.createHyperlink(Hyperlink.LINK_DOCUMENT);
             link.setAddress(cellValue + "!B3");
             cell.setHyperlink(link);
         }
     }
 }
