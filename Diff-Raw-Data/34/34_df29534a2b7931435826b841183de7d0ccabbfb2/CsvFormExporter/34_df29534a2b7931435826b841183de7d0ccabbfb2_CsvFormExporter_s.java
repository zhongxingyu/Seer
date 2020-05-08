 /** */
 package net.frontlinesms.plugins.forms.csv;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import net.frontlinesms.FrontlineSMSConstants;
 import net.frontlinesms.csv.CsvExporter;
 import net.frontlinesms.csv.CsvUtils;
 import net.frontlinesms.csv.Utf8FileWriter;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.repository.ContactDao;
 import net.frontlinesms.plugins.forms.data.domain.Form;
 import net.frontlinesms.plugins.forms.data.domain.FormField;
 import net.frontlinesms.plugins.forms.data.domain.FormResponse;
 import net.frontlinesms.plugins.forms.data.domain.ResponseValue;
 import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 public class CsvFormExporter extends CsvExporter {
 
 	/**
 	 * Exports results of the supplied form to the supplied file.
 	 * 
 	 * FIXME fix CSV exports in this method - should be properly escaped - use {@link CsvUtils}
 	 * 
 	 * @param exportFile File to be written
 	 * @param toExport Form to get the results from.
 	 * @param aggregate TRUE to aggregate values
 	 * @param contactDao Factory to look for the contact
 	 * @throws IOException
 	 */
 	public static void exportForm(File exportFile, Form toExport, ContactDao contactDao, FormResponseDao formResponseDao) throws IOException {
 		LOG.trace("ENTER");
 		LOG.debug("Filename [" + exportFile.getAbsolutePath() + "]");
 		
 		Utf8FileWriter out = null;
 		try {
 			out = new Utf8FileWriter(exportFile);
 			
 			CsvUtils.writeLine(out, CsvFormExporter.getColumnsNameAsStringArray(toExport));
			for (FormResponse formResponse : formResponseDao.getFormResponses(toExport, 0, 0)) {
				CsvUtils.writeLine(out, CsvFormExporter.getResultsAsStringArray(formResponse, contactDao.getFromMsisdn(formResponse.getSubmitter())));
 			}
 		}  finally {
 			if (out != null) out.close();
 			LOG.trace("EXIT"); 
 		}
 	}
 	
 	/**
 	 * @param toExport The form to be exported
 	 * @return An array of Strings representing the text to display as columns
 	 */
 	private static String[] getColumnsNameAsStringArray (Form toExport) {
 		LinkedList<String> columnsName = new LinkedList<String>(); 
 		columnsName.add(InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_CONTACT_NAME));
 		columnsName.add(InternationalisationUtils.getI18NString(FrontlineSMSConstants.COMMON_PHONE_NUMBER)); 
 		
 		for (FormField field : toExport.getFields()) {
 			if (field.getType().hasValue()) {
 				columnsName.add(field.getLabel());
 			}
 		}
 		
 		return columnsName.toArray(new String[0]);
 	}
 	
 	/**
 	 * @param formResponse A form response
 	 * @param contact The contact having submitted this form response
 	 * @return An array of Strings representing the text to display as the form values for this contact
 	 */
 	private static String[] getResultsAsStringArray(FormResponse formResponse, Contact contact) {
 		LinkedList<String> resultsValue = new LinkedList<String>();
 		resultsValue.add(contact.getName());
 		resultsValue.add(contact.getPhoneNumber());
 		
 		for(ResponseValue r : formResponse.getResults()) {
 			resultsValue.add(r.toString());
 		}
 		return resultsValue.toArray(new String[0]);
 	}
 }
