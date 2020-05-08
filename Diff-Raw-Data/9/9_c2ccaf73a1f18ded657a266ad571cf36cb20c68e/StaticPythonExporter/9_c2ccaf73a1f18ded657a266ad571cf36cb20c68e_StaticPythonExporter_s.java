 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pt.webdetails.cdb.exporters;
 
 import com.github.mustachejava.Mustache;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /**
  *
  * @author pdpi
  */
 public class StaticPythonExporter extends AbstractExporter {
 
  final private static String TEMPLATE_PATH = "StaticPython.mustache";
   public StaticPythonExporter() {
     this.fileExportExtension = "zip";
     this.templateFile = "StaticPython.mustache";
   }
 
   @Override
   public void binaryExport(String group, String id, String url, OutputStream out) throws IOException {
     String src = getSource(group, id, url);
     ZipOutputStream zos = new ZipOutputStream(out);
     zos.putNextEntry(new ZipEntry(id + ".py"));
     zos.write(src.getBytes("utf-8"));
     zos.putNextEntry(new ZipEntry(id + ".csv"));
     zos.write(ExporterEngine.exportCda(group, id, "csv").getBytes("utf-8"));
     zos.close();
   }
 
 }
