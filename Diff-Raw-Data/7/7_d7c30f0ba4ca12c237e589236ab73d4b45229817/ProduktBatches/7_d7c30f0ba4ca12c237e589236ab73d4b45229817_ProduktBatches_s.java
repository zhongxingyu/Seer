 package web_interface;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.DecimalFormat;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.text.pdf.draw.DottedLineSeparator;
 import com.itextpdf.text.pdf.draw.LineSeparator;
 
 import businessLogic_layer.IFunctionality;
 import dao_interfaces.DALException;
 import dto.ProduktBatchDTO;
 import dto.ProduktBatchKompDTO;
 import dto.RaavareBatchDTO;
 import dto.RaavareDTO;
 import dto.ReceptDTO;
 import dto.ReceptKompDTO;
 
 public class ProduktBatches {
 	public void handleAdminiProduktBatch(HttpServletRequest request, HttpServletResponse response, IFunctionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/produktbatch/adminiproduktbatch.jsp").forward(request, response);
 	}
 	public void handleCreateProduktBatch(ServletRequest request, ServletResponse response, IFunctionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int receptId;
 		String receptNavn;
 		List<ReceptDTO> receptList = funktionalitetsLaget.getReceptDAO().getReceptList();
 		String html = "<select name='receptid'>";
 		for (int i=0; i < receptList.size(); i++) {
 			receptId = receptList.get(i).getReceptId();
 			receptNavn = receptList.get(i).getReceptNavn();
 			html += "<option value='"+receptId+"'>"+receptId+" - "+receptNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("receptList", html);
 		request.getRequestDispatcher("/WEB-INF/admin/produktbatch/createproduktbatch.jsp").forward(request, response);
 	}
 	public void handleShowProduktBatch(HttpServletRequest request, HttpServletResponse response, IFunctionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		String html = "<table border=1>";
 		html += "<tr><td>Produktbatch ID</td><td>Recept ID</td><td>Dato</td><td>Status</td><td>Udskriv</td></tr>";
 		int produktbatchid, receptid, status;
 		String dato;
 		List<ProduktBatchDTO> produktbatch = funktionalitetsLaget.getProduktBatchDAO().getProduktBatchList(); 
 		for (int i=0; i < produktbatch.size(); i++) {
 			produktbatchid = produktbatch.get(i).getPbId();
 			receptid = produktbatch.get(i).getReceptId();
 			dato = produktbatch.get(i).getStartDato();
 			status = produktbatch.get(i).getStatus();
 			html += "<tr><td>"+produktbatchid+"</td><td>"+receptid+"</td><td>"+dato+"</td><td>"+statusToString(status)+"</td><td><form action='' method='POST'><input type='hidden' name='pbId' value='"+produktbatchid+"'><input type='submit' value='Udskriv' name='udskriv'></form></td></tr>";
 		}
 		html +="</table>";
 		request.setAttribute("list", html);
 		request.getRequestDispatcher("/WEB-INF/admin/produktbatch/showproduktbatch.jsp").forward(request, response);
 	}
 	public void handlePrintProduktBatch(HttpServletRequest request, HttpServletResponse response, IFunctionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int produktBatchId, receptId, oprId = 0, raavareId, status, raavareBatchId = 0;
 		String udskrevet, receptNavn, raavareNavn, startDato, slutDato;
 		double tara = 0, netto = 0, taraSum = 0, nettoSum = 0;
 		produktBatchId = Integer.parseInt(request.getParameter("pbId"));
 		ProduktBatchDTO pbDTO = funktionalitetsLaget.getProduktBatchDAO().getProduktBatch(produktBatchId);
 		receptId = pbDTO.getReceptId();
 		ReceptDTO receptDTO = funktionalitetsLaget.getReceptDAO().getRecept(receptId);
 		receptNavn = receptDTO.getReceptNavn();
 		List<ReceptKompDTO> receptKompList = funktionalitetsLaget.getReceptKompDAO().getReceptKompList(receptId);
 		status = pbDTO.getStatus();
 		startDato = pbDTO.getStartDato();
 		slutDato = pbDTO.getSlutDato();
 		
 		Calendar d = Calendar.getInstance();
 		DecimalFormat df = new DecimalFormat("00");
 		udskrevet = d.get(Calendar.YEAR) + "-"
 		+ df.format(d.get(Calendar.MONTH) + 1) + "-"
 		+ df.format(d.get(Calendar.DATE)) + " "
 		+ df.format(d.get(Calendar.HOUR_OF_DAY)) + ":"
 		+ df.format(d.get(Calendar.MINUTE)) + ":"
 		+ df.format(d.get(Calendar.SECOND));
 		try {
 			// step 1
 	        Document document = new Document();
 	        // step 2
 	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
 	        PdfWriter.getInstance(document, baos);
 	        // step 3
 	        document.open();
 	        // Line seperator
 	        Chunk SEPERATOR = new Chunk(
 	                new LineSeparator(0.5f, 95, BaseColor.BLUE, Element.ALIGN_CENTER, 3.5f));
 	        Chunk DOTTED = new Chunk(
 	                new DottedLineSeparator());
 	        // step 4
 	        document.add(new Paragraph("Udskrevet "+udskrevet));
 	        document.add(new Paragraph("Produkt Batch nr. "+produktBatchId));
 	        document.add(new Paragraph("Recept nr. "+receptId));
 	        document.add(new Paragraph("Recept navn: "+receptNavn));
 	        document.add(SEPERATOR);
 	        for (ReceptKompDTO receptKomp: receptKompList) {
 	        	RaavareDTO raavare = funktionalitetsLaget.getRaavareDAO().getRaavare(receptKomp.getRaavareId());
 	        	raavareId = receptKomp.getRaavareId();
 	        	raavareNavn = raavare.getRaavareNavn();
 	        	List<ProduktBatchKompDTO> produktBatchKompList = funktionalitetsLaget.getProduktBatchKompDAO().getProduktBatchKompList(produktBatchId);
 		        document.add(new Paragraph("Råvare nr. "+raavareId));
 		        document.add(new Paragraph("Råvare navn: "+raavareNavn));
 		        document.add(DOTTED);
 		        PdfPTable table = new PdfPTable(6);
 		        table.getDefaultCell().setBorder(0);
 		        table.addCell("Nominel Netto");
 		        table.addCell("Tolerance");
 		        table.addCell("Tara");
 		        table.addCell("Netto");
 		        table.addCell("Batch");
 		        table.addCell("Operatør");
 		        for (ProduktBatchKompDTO produktBatchKomp: produktBatchKompList) {
 		        	int rbId = produktBatchKomp.getRbId();
 		        	RaavareBatchDTO tjekRaavareBatch = funktionalitetsLaget.getRaavareBatchDAO().getRaavareBatch(rbId);
 		        	if (raavareId == tjekRaavareBatch.getRaavareId()) {
 		        		tara = produktBatchKomp.getTara();
 		        		taraSum += tara;
 		        		netto = produktBatchKomp.getNetto();
 		        		nettoSum += netto;
 		        		raavareBatchId = tjekRaavareBatch.getRbId();
 		        		oprId = produktBatchKomp.getOprId();
 				        table.addCell(""+receptKomp.getNomNetto());
				        table.addCell(""+receptKomp.getTolerance());
 		        		table.addCell(""+tara);
 		        		table.addCell(""+netto);
 		        		table.addCell(""+raavareBatchId);
 		        		table.addCell(""+oprId);
 		        	}
 		        }
		        if (tara == 0) {
 		        	table.addCell("");
 		        	table.addCell("");
 		        	table.addCell("");
 		        	table.addCell("");
 		        }
 		        document.add(table);
 		        document.add(SEPERATOR);
 	        }
 	        document.add(new Paragraph("Sum Tara: "+((taraSum != 0)?taraSum:"")));
 	        document.add(new Paragraph("Sum Netto: "+((nettoSum != 0)?nettoSum:"")));
 	        document.add(new Paragraph("\n\n"));
 	        document.add(new Paragraph("Produktion Status: "+statusToString(status)));
 	        document.add(new Paragraph("Produktion Startet: "+startDato));
 	        document.add(new Paragraph("Produktion Slut: "+slutDato));
 	       
 	        // step 5
 	        document.close();
 	        // slut
 	        response.setHeader("Expires", "0");
 	        response.setHeader("Cache-Control",
 	            "must-revalidate, post-check=0, pre-check=0");
 	        response.setHeader("Pragma", "public");
 	        response.setHeader("Content-Disposition", "attachment; filename=\"ProduktBatch"+produktBatchId+".pdf\"");
 	        // setting the content type
 	        response.setContentType("application/pdf");
 	        // the contentlength
 	        response.setContentLength(baos.size());
 	        // write ByteArrayOutputStream to the ServletOutputStream
 	        OutputStream os = response.getOutputStream();
 	        baos.writeTo(os);
 	        os.flush();
 	        os.close();
 		}
 		catch(DocumentException e) {
 			e.printStackTrace();
 			System.out.println("fanget");
 		}
 	}
 	private String statusToString(int id) {
 		String status;
 		switch(id) {
 		case 0:
 			status = "Startet";
 			break;
 		case 1:
 			status = "Under produktion";
 			break;
 		case 2:
 			status = "Afsluttet";
 			break;
 		default:
 			status = "Invalid status";
 			break;
 		}
 		return status;
 	}
 	
 	public void handleCreateProduktBatchSubmit(HttpServletRequest request, HttpServletResponse response, IFunctionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int produktbatchid = 0, receptid, status;
 		String errorMsg = "", slutDato = "",startDato;
 		try {
 			produktbatchid = Integer.parseInt(request.getParameter("produktbatchid"));
 		}
 		catch (NumberFormatException e) {
 			errorMsg = "Fejl i produktbatchid, muligvis specialtegn! ";
 		}
 		receptid = Integer.parseInt(request.getParameter("receptid"));
 		status = 0;
 		Calendar d = Calendar.getInstance();
 		DecimalFormat df = new DecimalFormat("00");
 		startDato = d.get(Calendar.YEAR) + "-"
 		+ df.format(d.get(Calendar.MONTH) + 1) + "-"
 		+ df.format(d.get(Calendar.DATE)) + " "
 		+ df.format(d.get(Calendar.HOUR_OF_DAY)) + ":"
 		+ df.format(d.get(Calendar.MINUTE)) + ":"
 		+ df.format(d.get(Calendar.SECOND));
 		if (produktbatchid != 0 && funktionalitetsLaget.testPbId(produktbatchid))
 			errorMsg = "RåvareBatch ID findes i forvejen!";
 		if (errorMsg.length() == 0) {
 			funktionalitetsLaget.getProduktBatchDAO().createProduktBatch(new ProduktBatchDTO(produktbatchid,receptid,startDato, slutDato, status));
 			request.setAttribute("succes", "Produktbatch oprettet!");
 		}
 		else {
 			request.setAttribute("fail", errorMsg);
 		}
 		
 		int receptId;
 		String receptNavn;
 		List<ReceptDTO> receptList = funktionalitetsLaget.getReceptDAO().getReceptList();
 		String html = "<select name='receptid'>";
 		for (int i=0; i < receptList.size(); i++) {
 			receptId = receptList.get(i).getReceptId();
 			receptNavn = receptList.get(i).getReceptNavn();
 			html += "<option value='"+receptId+"'>"+receptId+" - "+receptNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("receptList", html);
 		request.getRequestDispatcher("/WEB-INF/admin/produktbatch/createproduktbatch.jsp").forward(request, response);
 	}
 	
 }
