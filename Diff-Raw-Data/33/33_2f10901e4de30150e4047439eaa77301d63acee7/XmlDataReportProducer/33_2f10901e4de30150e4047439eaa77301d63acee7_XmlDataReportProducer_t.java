 import java.io.*;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRExporter;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.data.JRXmlDataSource;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.export.JRXlsExporter;
 
 
 public class XmlDataReportProducer {
 
   public static byte[] compile(String jrxml, String xmldata, String select, String type) {
    byte[] report = null;
	ByteArrayInputStream bs_jrxml = null;
	ByteArrayInputStream bs_xml = null;
	try {
      bs_jrxml = new ByteArrayInputStream(jrxml.getBytes());
      bs_xml = new ByteArrayInputStream(xmldata.getBytes());
 
       JasperReport jr = JasperCompileManager.compileReport(bs_jrxml);
       JRXmlDataSource ds = new JRXmlDataSource(bs_xml, select);
       JasperPrint jp = JasperFillManager.fillReport(jr, null, ds);
 	  if (type.equalsIgnoreCase("pdf"))
		report = pdf(jp);
 	  else if (type.equalsIgnoreCase("xls"))
		report = xls(jp);
	  bs_jrxml.close();
	  bs_xml.close();
    }catch (Exception e) {
       e.printStackTrace();
       return null;
     }
	return report;
   }
 
  private static byte[] xls(JasperPrint jasperprint) throws JRException, IOException {
   	JRExporter exporter = new JRXlsExporter(); 
 	ByteArrayOutputStream xls = new ByteArrayOutputStream(); 
 	exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperprint);
 	exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, xls);
 	exporter.exportReport();
	byte[] report = xls.toByteArray();
	xls.close();
	return report;
   }
 
   private static byte[] pdf(JasperPrint jasperprint) throws JRException {
   	return JasperExportManager.exportReportToPdf(jasperprint);
   }
 
 }
 
