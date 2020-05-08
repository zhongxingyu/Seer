 package de.switajski.priebes.flexibleorders.report.itextpdf;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Component;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.switajski.priebes.flexibleorders.domain.Address;
 import de.switajski.priebes.flexibleorders.domain.Amount;
 import de.switajski.priebes.flexibleorders.domain.ConfirmationReport;
 import de.switajski.priebes.flexibleorders.domain.HandlingEvent;
 import de.switajski.priebes.flexibleorders.domain.helper.AmountCalculator;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.CustomPdfPTableBuilder;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.ParagraphBuilder;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.PdfPTableBuilder;
 
 @Component
 public class OrderConfirmationPdfView extends PriebesIText5PdfView {
 
 	//TODO: make VAT_RATE dependent from order
 	public static final Double VAT_RATE = 0.19d;
 
 	@Override
 	protected void buildPdfDocument(Map<String, Object> model,
 			Document document, PdfWriter writer, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		
 		ConfirmationReport report = (ConfirmationReport) model.get(ConfirmationReport.class.getSimpleName());
 		
		String leftTop = "Auftragsnummer: " + report.getDocumentNumber().toString();
 		String expectedDelivery = "";
 		String customerNumber = "";
		String created = "Auftragsdatum: " + dateFormat.format(report.getCreated());
 		Address adresse = report.getInvoiceAddress();
 		String heading = "Auftragsbesttigung";
 		
 		Amount net = AmountCalculator.calculateNetAmount(report);
 		Amount vat = AmountCalculator.calculateVatAmount(report, OrderConfirmationPdfView.VAT_RATE);
 		Amount gross = net.add(vat);
 
 		// insert address
 		document.add(ParagraphBuilder.createEmptyLine());
 		document.add(ParagraphBuilder.createEmptyLine());
 		document.add(ParagraphBuilder.createEmptyLine());
 		document.add(ParagraphBuilder.createEmptyLine());
 		if (adresse == null) {
 			document.add(ParagraphBuilder.createEmptyLine());
 			document.add(ParagraphBuilder.createEmptyLine());
 			document.add(ParagraphBuilder.createEmptyLine());
 		} else {
 			document.add(new ParagraphBuilder(adresse.getName1())
 			.withIndentationLeft(36f)
 			.withLineSpacing(12f)
 			.addTextLine(adresse.getName2())
 			.addTextLine(adresse.getStreet())
 			.addTextLine(adresse.getPostalCode() + " " + adresse.getCity())
 			.addTextLine(adresse.getCountry().toString())
 			.build());
 		}
 		document.add(ParagraphBuilder.createEmptyLine());
 		document.add(ParagraphBuilder.createEmptyLine());
         
 		
         // insert heading
 		document.add(new ParagraphBuilder(heading)
 		.withFont(FontFactory.getFont(FONT, 12, Font.BOLD))
 		.build());
 		document.add(ParagraphBuilder.createEmptyLine());
 		
 
 		// info table
		CustomPdfPTableBuilder infoTableBuilder = CustomPdfPTableBuilder.createInfoTable(
        		leftTop,
         		created, expectedDelivery, customerNumber);
         PdfPTable infoTable = infoTableBuilder.build();
 		infoTable.setWidthPercentage(100);
 		document.add(infoTable);
         //TODO: if (auftragsbestaetigung.getAusliefDatum==null) insertInfo(document,"Voraussichtliches Auslieferungsdatum:" + auftragsbestaetigung.getGeplAusliefDatum());
         document.add(ParagraphBuilder.createEmptyLine());
 
         
         // insert main table
         document.add(createTable(report));
 
 		
         // insert footer table
 		CustomPdfPTableBuilder footerBuilder = CustomPdfPTableBuilder.createFooterBuilder(
 				net, vat, null, gross, null)
 				.withTotalWidth(PriebesIText5PdfView.WIDTH);
 	    
 	    PdfPTable footer = footerBuilder.build();
 	    
 	    footer.writeSelectedRows(0, -1,
 	    		/*xPos*/ PriebesIText5PdfView.PAGE_MARGIN_LEFT, 
 	    		/*yPos*/ PriebesIText5PdfView.PAGE_MARGIN_BOTTOM + FOOTER_MARGIN_BOTTOM, 
 	    		writer.getDirectContent());
 	}
 
 	private PdfPTable createTable(ConfirmationReport cReport) throws DocumentException{
 		PdfPTableBuilder builder = new PdfPTableBuilder(PdfPTableBuilder.createPropertiesWithSixCols());
 		for (HandlingEvent he: cReport.getEvents()){
 			List<String> list = new ArrayList<String>();
 			// Art.Nr.:
 			list.add(he.getOrderItem().getProduct().getProductNumber().toString());
 			// Artikel
 			list.add(he.getOrderItem().getProduct().getName());
 			// Anzahl
 			list.add(String.valueOf(he.getQuantity()));
 			// EK per Stueck
 			list.add(he.getOrderItem().getNegotiatedPriceNet().toString());
 			// Bestellnr
 			list.add(he.getOrderItem().getOrder().getOrderNumber());
 			// gesamt
 			list.add(he.getOrderItem().getNegotiatedPriceNet().multiply(he.getQuantity()).toString());
 			
 			builder.addBodyRow(list);
 		}
 		
 		return builder.withFooter(false).build();
 	}
 	
 }
