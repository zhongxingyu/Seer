 package pl.linet;
 
 import com.lowagie.text.pdf.*;
 import com.lowagie.text.*;
 import com.lowagie.text.pdf.PdfPTable;
 import java.io.*;
 
 import javax.swing.JOptionPane;
 
 //import java.util.*;
 
 /**
  * @author kukems
  * 
  *         TODO To change the template for this generated type comment go to
  *         Window - Preferences - Java - Code Style - Code Templates
  */
 public class Generuj {
 	private String nazwa = "Biuro Pośrednictwa KREDYTOR",
 			nip = "583-187-65-07",
 			adres = "ul. Zakopiańska 28/1",
 			kod = "80-142",
 			miasto = "Gdańsk",
 			miejsc = "Gdańsk",
 			konto = "Multi Bank S.A.                     45-1140-2017-0000-4102-0403-2470";
 	private String[] confArray = { nazwa, nip, adres, kod, miasto, miejsc,
 			konto };
 	private Document document;
 
 	/**
 	 * ustawia dane sprzedającego
 	 * 
 	 */
 	public Generuj() {
 		try {
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
 					"config_fakturka.txt"));
 			String[] txt_array = (String[]) in.readObject();
 			nazwa = txt_array[0];
 			nip = txt_array[1];
 			adres = txt_array[2];
 			kod = txt_array[3];
 			miasto = txt_array[4];
 			konto = txt_array[5];
 			miejsc = txt_array[6];
 		} catch (Exception E) {
 			JOptionPane.showMessageDialog(null,
 					"Wystąpił błąd skonfiguruj najpierw Moje Dane w menu konfiguracja"
 							+ E, "Błąd", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Generuje plik pdf z argumentami jako danymi do faktury
 	 * 
 	 * @param k_nazwa
 	 *            - nazwa kupujacego
 	 * @param k_adres
 	 *            - adres kupujacego
 	 * @param k_nip
 	 *            - nip kupujacego
 	 * @param k_kod
 	 * @param k_miesc
 	 * @param usluga
 	 * @param pkwiu
 	 * @param ilosc
 	 * @param netto
 	 *            - kwota netto
 	 * @param vat
 	 * @param platnosc
 	 * @param termin
 	 * @param numer_f
 	 * @param miesc
 	 * @param brutto
 	 * @param data
 	 * @param slownie
 	 */
 
 	public void generatePdf(String k_nazwa, String k_adres, String k_nip,
 			String k_kod, String k_miesc, String usluga, String pkwiu,
 			String ilosc, float netto, int vat, String platnosc, String termin,
 			String numer_f, String data, String slownie) {
 
 		// przeliczenia podatku
 		float brutto = Przelicz.brutto(netto, vat);
 		// skraca netto
 		// TODO - przeliczanie z brutto
 		netto = (float) Math.round(netto * 100) / 100;
 		float podatek = Przelicz.podatek(netto, vat);
 
 		document = new Document();
 		try {
 			// we create a writer that listens to the document
 			// and directs a PDF-stream to a file
 			PdfWriter
 					.getInstance(document, new FileOutputStream("faktura.pdf"));
 
 			document.open();
 			/* fonty */
 			BaseFont vera;
 			if (System.getProperty("os.name").startsWith("Windows"))
 				vera = BaseFont.createFont("C:\\Windows\\Fonts\\Arial.ttf",
 						BaseFont.CP1250, BaseFont.EMBEDDED);
 			else if (System.getProperty("os.name").equals("Mac OS X")) {
				vera = BaseFont.createFont("/Library/Fonts/Arial.ttf",
 						BaseFont.CP1250, BaseFont.EMBEDDED);
 			} else {
 				vera = BaseFont.createFont(
 						"/usr/share/fonts/corefonts/arial.ttf",
 						BaseFont.CP1250, BaseFont.EMBEDDED);
 			}
 			Font font = new Font(vera, 12);
 
 			PdfPTable table1 = new PdfPTable(2);
 			// ustawienia pierwszej tabeli
 			table1.setWidthPercentage(100);
 			table1.setSpacingBefore(5f);
 			table1.setSpacingAfter(15f);
 
			table1.addCell(new Paragraph("Faktura VAT           ORYGINAŁ/KOPIA",
 					font));
 			table1.addCell(new Phrase("z dnia: " + data));
 			table1.addCell(new Phrase("nr.  " + numer_f));
 			table1.addCell(new Phrase("Miejscowość: " + miejsc, font));
 			document.add(table1);
 
 			/*
 			 * table1 zawiera table i table2 table - tabela z danymi
 			 * wystawiajacego fakture table2 - tabela z danymi odbiorcy faktury
 			 */
 			// tworzenie obiektow tabel
 			PdfPTable table2 = new PdfPTable(4);
 
 			// ustawienia tabeli sprzedawcy/nabywcy
 			table2.setWidthPercentage(100);
 			table2.setHeaderRows(1);
 
 			// tabelki z danymi sprzedawcy/nabywcy
 			PdfPCell cell_kup = new PdfPCell(new Phrase("Dane Nabywcy:"));
 			cell_kup.setColspan(2);
 			PdfPCell cell_sprzed = new PdfPCell(new Phrase("Dane Sprzedawcy:"));
 			cell_sprzed.setColspan(2);
 
 			table2.addCell(cell_sprzed);
 			table2.addCell(cell_kup);
 
 			table2.addCell("Nazwa:");
 			table2.addCell(new Phrase(nazwa, font));
 			table2.addCell("Nazwa:");
 			table2.addCell(new Phrase(k_nazwa, font));
 			table2.addCell("Nip");
 			table2.addCell(new Phrase(nip, font));
 			table2.addCell("Nip");
 			table2.addCell(new Phrase(k_nip, font));
 			table2.addCell("Adres");
 			table2.addCell(new Phrase(adres, font));
 			table2.addCell("Adres");
 			table2.addCell(new Phrase(k_adres, font));
 			table2.addCell(new Phrase("Miejscowość", font));
 			table2.addCell(new Phrase(kod + " " + miejsc, font));
 			table2.addCell(new Phrase("Miejscowość", font));
 			table2.addCell(new Phrase(k_kod + " " + k_miesc, font));
 			document.add(table2);
 			// koniec tabeli sprzedawcy/nabywcy
 
 			// tabela produktu
 			// ustawienia tabelki
 			float[] widths = { 6f, 1f, 1f, 1f, 2f, 2f, 1f, 2f };
 			PdfPTable pr_table = new PdfPTable(widths);
 			pr_table.setSpacingBefore(15f);
 			pr_table.setSpacingAfter(24);
 			pr_table.setWidthPercentage(100);
 
 			// dane tabeli produktu
 			pr_table.addCell(new Phrase("Nazwa Towaru/Usługi", font));
 			pr_table.addCell(new Phrase("PKWiU", font));
 			pr_table.addCell(new Phrase("j.m", font));
 			pr_table.addCell(new Phrase("Ilość", font));
 			pr_table.addCell(new Phrase("Wartość netto", font));
 			pr_table.addCell(new Phrase("Kwota Podatku", font));
 			pr_table.addCell(new Phrase("% VAT", font));
 			pr_table.addCell(new Phrase("Wartość Brutto", font));
 
 			pr_table.addCell(new Phrase(usluga, font));
 			pr_table.addCell(pkwiu);
 			pr_table.addCell("");
 			pr_table.addCell(ilosc);
 			pr_table.addCell(Float.toString(netto));
 			pr_table.addCell(Float.toString(Przelicz.podatek(netto, vat)));
 			pr_table.addCell(Integer.toString(vat));
 			pr_table.addCell(Float.toString(brutto));
 
 			document.add(pr_table);
 			// koniec tabelki produktu
 
 			// tabelka kontener
 			PdfPTable kt_table = new PdfPTable(2);
 			kt_table.setWidthPercentage(100);
 			// tabelka płatności w kontener
 			PdfPTable pl_table = new PdfPTable(2);
 			PdfPCell pl_cell = new PdfPCell(new Phrase("  numer konta:        "
 					+ konto));
 			pl_table.getDefaultCell().setHorizontalAlignment(
 					Element.ALIGN_RIGHT);
 			pl_cell.setColspan(2);
 
 			pl_table.addCell(new Phrase("Sposób Zapłaty: " + platnosc, font));
 			pl_table.addCell(new Phrase("termin płatności: " + termin, font));
 			pl_table.addCell(pl_cell);
 			pl_table.addCell(new Phrase("Do zapłaty: " + brutto + "zł", font));
 			pl_table.addCell(new Phrase("słownie: " + slownie, font));
 			// dodanie tabeli platności do kontenera
 			kt_table.addCell(pl_table);
 
 			// tabelka rozliczenie podatku
 			float[] widths2 = { 0.5f, 1f, 2f, 2f };
 			PdfPTable pd_table = new PdfPTable(widths2);
 
 			// ustawienia tabelki podatku
 			pd_table.setWidthPercentage(100);
 
 			// zawartość tabelki podatku
 			PdfPCell pd_cell = new PdfPCell(new Phrase(
 					"Rozliczenie sprzedaży według stawek podatku", font));
 			pd_cell.setColspan(4);
 
 			pd_table.addCell(pd_cell);
 			if (vat == 23) {
 				pd_table.addCell("23%");
 				pd_table.addCell(Float.toString(netto));
 				pd_table.addCell(Float.toString(podatek));
 				pd_table.addCell(Float.toString(brutto));
 
 				pd_table.addCell("7%");
 				pd_table.addCell("");
 				pd_table.addCell("");
 				pd_table.addCell("");
 			} else if (vat == 7) {
 				pd_table.addCell("23%");
 				pd_table.addCell("");
 				pd_table.addCell("");
 				pd_table.addCell("");
 
 				pd_table.addCell("7%");
 				pd_table.addCell(Float.toString(netto));
 				pd_table.addCell(Float.toString(podatek));
 				pd_table.addCell(Float.toString(brutto));
 
 			} else {
 				pd_table.addCell("23%");
 				pd_table.addCell("vat musi byc 23 lub 7");
 				pd_table.addCell("vat musi byc 23 lub 7");
 				pd_table.addCell("musi byc 23 lub 7");
 
 				pd_table.addCell("7%");
 				pd_table.addCell("vat musi byc 23 lub 7");
 				pd_table.addCell("vat musi byc 23 lub 7");
 				pd_table.addCell("vat musi byc 23 lub 7");
 				pd_table.addCell("vat musi byc 23 lub 7");
 
 			}
 			// pd_table.addCell("3%");
 			// pd_table.addCell("tutaj suma bez 3%");
 			// pd_table.addCell("tutaj suma vat 3%");
 			// pd_table.addCell("tutaj suma brutto 3%");
 			//		
 			// pd_table.addCell("0%");
 			// pd_table.addCell("tutaj suma bez 0%");
 			// pd_table.addCell("tutaj suma vat 0%");
 			// pd_table.addCell("tutaj suma brutto 0%");
 			//		
 			kt_table.addCell(pd_table);
 
 			document.add(kt_table);
 			// koniec tabelka kontener
 
 		} catch (DocumentException docex) {
 			System.err.println(docex.getMessage());
 		} catch (IOException ioe) {
 			System.err.println(ioe.getMessage());
 		}
 
 		// step 5: we close the document
 		document.close();
 	}
 
 	public static void main(String[] args) {
 		Generuj pdf = new Generuj();
 	}
 }
 
 class Przelicz {
 	protected static float brutto(float netto, int vat) {
 		float brutto = netto + podatek(netto, vat);
 		brutto = (float) Math.round(brutto * 100) / 100;
 		return brutto;
 	}
 
 	protected static float netto(float brutto, int vat) {
 		float netto = brutto - podatek(brutto, vat);
 		netto = (float) Math.round(netto * 100) / 100;
 		return netto;
 	}
 
 	protected static float podatek(float netto, int vat) {
 		// zamienia vat na procent
 		float v = vat;
 		v = v / 100;
 		// skraca netto do dwóch liczb po przecinku
 		float kwota_vat = netto * v;
 		kwota_vat = kwota_vat * 100;
 		kwota_vat = (float) Math.round(kwota_vat) / 100;
 		return kwota_vat;
 	}
 }
