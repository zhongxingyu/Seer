 package com.hoegernet.wrsvpdf.reporting;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jasperreports.engine.JasperPrint;
 
 import com.hoegernet.wrsvpdf.Configuration;
 import com.hoegernet.wrsvpdf.exceptions.PdfGeneratorException;
 import com.hoegernet.wrsvpdf.types.Staffel;
 
 /**
 * @author Thorsten H�ger
  * 
  *         Projekt: com.hoegernet.wrsvpdf/ Type: ClubReport
  * 
  *         created: 15.06.2009
  * 
  */
 public class RulesReport extends AbstractGenerator {
 	
 	/**
 	 * @param staffel
 	 * @return Report
 	 * @throws PdfGeneratorException
 	 */
 	public static JasperPrint createReport(final Staffel staffel) throws PdfGeneratorException {
 		AbstractGenerator.assertNull(staffel, "Staffel mustn't be null");
 		
 		final Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("title", staffel.getTitle());
 		parameters.put("staffelname", staffel.getName());
 		parameters.put("regeln", staffel.getRegelungen().replaceAll("[ ]{2,}", " "));
 		
 		return AbstractGenerator.print(Configuration.REPORT_RULES, parameters, new Vector<Object[]>(), new String[0], (staffel.getTitle().length() > Configuration.MAX_TITLE_LENGTH));
 	}
 	
 }
