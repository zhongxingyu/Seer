 package com.prebeg.ihznet.data.scraper;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Component;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebRequest;
 import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.html.HtmlOption;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
 import com.gargoylesoftware.htmlunit.html.HtmlSelect;
 import com.gargoylesoftware.htmlunit.html.HtmlTable;
 import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
 import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
 import com.prebeg.ihznet.model.Linija;
 import com.prebeg.ihznet.model.ListaLinija;
 import com.prebeg.ihznet.model.ListaOznaka;
 import com.prebeg.ihznet.model.ListaStajalista;
 import com.prebeg.ihznet.model.Putovanje;
 import com.prebeg.ihznet.model.Raspored;
 import com.prebeg.ihznet.model.Stajaliste;
 import com.prebeg.ihznet.service.OznakaResolverService;
 import com.prebeg.ihznet.service.PeriodService;
 
 @Component
 public class RasporedScraper {
 	
 	private transient final Log log = LogFactory.getLog(getClass());
 	
 	@Resource
 	OznakaResolverService oznakaResolverService;
 	
 	@Resource
 	PeriodService periodService;
 	
 	//private String baseurl = "http://vred.hznet.hr/hzinfo/?category=hzinfo&service=vred3&nkod1=&nkdo1=&lang=hr&screen=4";
 	private String baseurl = "http://vred.hzinfra.hr/hzinfo/?category=hzinfo&service=vred3&nkod1=&nkdo1=&lang=hr&screen=4";
 
 	
 	private WebClient getWebClient() {
 		WebClient wc = new WebClient(BrowserVersion.FIREFOX_3_6);
 		wc.setCssEnabled(true);
 		wc.setJavaScriptEnabled(true);		
 		wc.setAjaxController(new NicelyResynchronizingAjaxController());
 		
 		return wc;
 	}
 	
 	
 	private HtmlPage getTimeTablePage_no_js(Integer odlazniKolodvorId, Integer dolazniKolodvorId, String datum, String dv) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
 		
 		WebClient wc = getWebClient();
 		
 		WebRequest webRequest = new WebRequest(new URL(baseurl));
 		
 		/* I can't even explain how important this crap is 
 		 * If you set this in some other form, Đ ŠČ chars 
 		 * will be encoded in different encoding and urls 
 		 * will break on you 
 		 */
 		webRequest.setCharset("Windows-1250");
 		
 		final HtmlPage page = wc.getPage(webRequest);		
 		
 		HtmlForm searchForm = page.getForms().get(0);
 	
 		final HtmlSelect NKDO1Select = (HtmlSelect) searchForm.getSelectByName("NKDO1");
 		final HtmlSelect NKOD1Select = (HtmlSelect) searchForm.getSelectByName("NKOD1");
 		
 		final HtmlOption NKDO1opt = NKDO1Select.getOption(dolazniKolodvorId); 
 		final HtmlOption NKOD1opt = NKOD1Select.getOption(odlazniKolodvorId); 
 		
 		NKDO1Select.setSelectedAttribute(NKDO1opt, true);
 		NKOD1Select.setSelectedAttribute(NKOD1opt, true);
 		
 		//log.info("resolved NKDO1 as " + NKDO1opt.asText());
 		//log.info("resolved NKOD1 as " + NKOD1opt.asText());
 		
 		/* datum */
 		final HtmlSelect DTSelect = (HtmlSelect) searchForm.getSelectByName("DT");
 		DTSelect.setSelectedAttribute(datum, true);
 				
 		/* lista */
 		final HtmlRadioButtonInput dRadio = searchForm.getRadioButtonsByName("DV").get(0);
 		final HtmlRadioButtonInput sRadio = searchForm.getRadioButtonsByName("DV").get(1);
 		final HtmlRadioButtonInput aRadio = searchForm.getRadioButtonsByName("DV").get(2);
 		
 		if (dv.equals("D")) {
 			dRadio.setChecked(true);
 			sRadio.setChecked(false);
 			aRadio.setChecked(false);
 		} else if (dv.equals("S")) {
 			dRadio.setChecked(false);
 			sRadio.setChecked(true);
 			aRadio.setChecked(false);
 		} else if (dv.equals("A")) {
 			dRadio.setChecked(false);
 			sRadio.setChecked(false);
 			aRadio.setChecked(true);
 		}
 		
 		final HtmlInput prikaziButton = (HtmlInput) searchForm.getInputsByValue("Prikaži vlakove koji voze na relaciji").get(0);
 		
 		final HtmlPage timeTablePage = (HtmlPage) prikaziButton.click();
 		
 		//log.info(timeTablePage.asXml());
 		
 		return timeTablePage;
 	}
 	
 	private HtmlPage getTimeTablePage(Integer odlazniKolodvorId, Integer dolazniKolodvorId, String datum, String dv) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
 		
 		WebClient wc = getWebClient();
 		
 		WebRequest webRequest = new WebRequest(new URL(baseurl));
 		
 		/* I can't even explain how important this crap is 
 		 * If you set this in some other form, Đ ŠČ chars 
 		 * will be encoded in different encoding and urls 
 		 * will break on you 
 		 */
 		webRequest.setCharset("Windows-1250");
 		
 		final HtmlPage page = wc.getPage(webRequest);		
 		
 		HtmlForm searchForm = page.getForms().get(0);
 		
 		//log.info(searchForm.asXml());
 	
 		final HtmlSelect NKOD1Select = (HtmlSelect) searchForm.getSelectByName("ddList1");
 		final HtmlSelect NKDO1Select = (HtmlSelect) searchForm.getSelectByName("ddList2");
 		
 		final HtmlOption NKOD1opt = NKOD1Select.getOption(odlazniKolodvorId); 
 		final HtmlOption NKDO1opt = NKDO1Select.getOption(dolazniKolodvorId); 
 		
     log.info(NKOD1opt.getValueAttribute() + " -> " + NKDO1opt.getValueAttribute());
 		//log.info("resolved NKOD1 as " + NKOD1opt.getValueAttribute());
 		//log.info("resolved NKDO1 as " + NKDO1opt.getValueAttribute());
 			
 			NKOD1Select.click();
 			NKOD1opt.setSelected(true);
 			NKOD1Select.setSelectedAttribute(NKOD1opt, true);
 			NKOD1opt.click();
 			
 			NKDO1Select.click();
 			NKDO1Select.setSelectedAttribute(NKDO1opt, true);
 			NKDO1opt.setSelected(true);
 			NKDO1opt.click();
 			
 			final HtmlInput NKDO1Input = (HtmlInput) searchForm.getInputByName("NKDO1");
 			final HtmlInput NKOD1Input = (HtmlInput) searchForm.getInputByName("NKOD1");
 			
 			
 			//log.info("NKOD1 INPUT:" + NKOD1Input.getTextContent());
 			//log.info("NKDO1 INPUT:" + NKDO1Input.getTextContent());
 		
 		/* datum */
 		final HtmlSelect DTSelect = (HtmlSelect) searchForm.getSelectByName("DT");
 		DTSelect.setSelectedAttribute(datum, true);
 				
 		/* lista */
 		final HtmlRadioButtonInput dRadio = searchForm.getRadioButtonsByName("DV").get(0);
 		final HtmlRadioButtonInput sRadio = searchForm.getRadioButtonsByName("DV").get(1);
 		final HtmlRadioButtonInput aRadio = searchForm.getRadioButtonsByName("DV").get(2);
 		
 		if (dv.equals("D")) {
 			dRadio.setChecked(true);
 			sRadio.setChecked(false);
 			aRadio.setChecked(false);
 		} else if (dv.equals("S")) {
 			dRadio.setChecked(false);
 			sRadio.setChecked(true);
 			aRadio.setChecked(false);
 		} else if (dv.equals("A")) {
 			dRadio.setChecked(false);
 			sRadio.setChecked(false);
 			aRadio.setChecked(true);
 		}
 		
 		final HtmlInput prikaziButton = (HtmlInput) searchForm.getInputsByValue("Prikaži vlakove koji voze na relaciji").get(0);
 		
 		//final HtmlInput prikaziButton = (HtmlInput) searchForm.getInputsByValue("Prika&#382;i vlakove koji voze na relaciji").get(0);
 		//final HtmlInput prikaziButton = (HtmlInput) searchForm.getInputsByValue("Prika\\&#382;i vlakove koji voze na relaciji").get(0);
 
 		
 		final HtmlPage timeTablePage = (HtmlPage) prikaziButton.click();
 		
 		//log.info(timeTablePage.asXml());
 		
 		return timeTablePage;
 	}
 
 
 
 	public Raspored getRaspored(Integer odlazniKolodvorId, Integer dolazniKolodvorId, String datum, String dv) {
 		
 		Raspored raspored = new Raspored();
 		
 		List<Putovanje> novaPutovanja = null;
 		
 		try {
 			
 			HtmlPage timeTablePage = getTimeTablePage(odlazniKolodvorId, dolazniKolodvorId, datum, dv);
 			
 			if (dv.equals("D")) {
 				novaPutovanja = getIzravnaPutovanja(timeTablePage);
 			} else if (dv.equals("S")) {
 				novaPutovanja = getSvaPutovanjaSMogucimVezama(timeTablePage);
 			} else if (dv.equals("A")) {
 				novaPutovanja = getSvaPutovanjaSaSvimVezama(timeTablePage);
 			}
 			
 			if (novaPutovanja != null)
 				raspored.addPutovanja(novaPutovanja);
 			
		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return raspored;
 	}
 	
 	private List<Putovanje> getIzravnaPutovanja(HtmlPage timeTablePage) throws FailingHttpStatusCodeException, MalformedURLException, IOException {	
 				
 		if (timeTablePage.asText().contains("IZRAVNE I NEIZRAVNE VEZE")) {
 			return null;
 		}
 		
 		HtmlTable timeTable = (HtmlTable)timeTablePage.getElementsByTagName("table").item(1);
 				
 		List<Putovanje> putovanja = new LinkedList<Putovanje>();
 		
 		for (HtmlTableRow row : timeTable.getRows()) {
 			 
 			//skip header
 			if (row == timeTable.getRow(0))
 				continue;			
 			
 			HtmlTableCell urlCell = row.getCells().get(1);			
 			
 			HtmlAnchor putovanjeUrl = (HtmlAnchor)urlCell.getElementsByTagName("a").get(0);
 			
 			Putovanje putovanje = scrapePutovanje(putovanjeUrl);
 			
 			if (putovanje != null) {
 				putovanje.setBrojPresjedanja(putovanje.getListaLinija().getLinije().size() - 1);
 				
 				if (putovanje.getBrojPresjedanja() == 0)
 					putovanje.setIzravno(true);
 				else
 					putovanje.setIzravno(false);
 				
 				putovanja.add(putovanje);
 				
 			}
 		}
 		
 		return putovanja;
 	}
 	
 private List<Putovanje> getSvaPutovanjaSMogucimVezama(HtmlPage timeTablePage) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
 		
 		HtmlTable timeTable = (HtmlTable)timeTablePage.getElementsByTagName("table").item(1);
 
 		List<Putovanje> putovanja = new LinkedList<Putovanje>();
 		
 		for (HtmlTableRow row : timeTable.getRows()) {
 			
 			// skip header
 			if (row == timeTable.getRow(0))
 				continue;
 				
 			HtmlTableCell tableCell = row.getCell(1);
 			HtmlAnchor putovanjeUrl = (HtmlAnchor)tableCell.getElementsByTagName("a").get(0);
 			Putovanje putovanje = scrapePutovanje(putovanjeUrl);
 			
 			if (putovanje != null) {
 				putovanja.add(putovanje);
 			}
 		}
 		
 		return putovanja;
 	}
 
 	
 	private List<Putovanje> getSvaPutovanjaSMogucimVezama_no_js(HtmlPage timeTablePage) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
 		
 		HtmlTable timeTable = (HtmlTable)timeTablePage.getElementsByTagName("table").item(1);
 
 		List<Putovanje> putovanja = new LinkedList<Putovanje>();
 		
 		for (HtmlTableRow row : timeTable.getRows()) {
 			
 			// skip header
 			if (row == timeTable.getRow(0))
 				continue;
 				
 			HtmlTableCell tableCell = row.getCell(2);
 			
 			for (HtmlElement he: tableCell.getElementsByTagName("table")) {
 				
 
 				// skip direct travel
 				if (he == tableCell.getElementsByTagName("table").item(0))
 					continue;
 				
 				HtmlTableRow mogucePutovanje = ((HtmlTable)he).getRow(0);
 				
 				HtmlTableCell urlCell = mogucePutovanje.getCells().get(2);
 				HtmlAnchor putovanjeUrl = (HtmlAnchor)urlCell.getElementsByTagName("a").get(1);
 				
 				//System.out.println("##############################\n"+ putovanjeUrl.asText() + "##############################\n");
 
 				//System.out.println("scraping " + putovanjeUrl);
 				Putovanje putovanje = scrapePutovanje(putovanjeUrl);
 				
 				if (putovanje != null) {
 					putovanja.add(putovanje);
 				}	
 			}
 		}
 		
 
 		return putovanja;
 	}
 	
 	private List<Putovanje> getSvaPutovanjaSaSvimVezama(HtmlPage timeTablePage) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
 		return null;
 	}
 	
 	
 	
 	private Putovanje scrapePutovanje(HtmlAnchor url) throws IOException {
 		
 		HtmlPage putovanjePage = url.click();
 		
 		//log.info(putovanjePage.asText());
 		
 		if (putovanjePage.asText().contains("ZA NASTAVAK PUTOVANJA") || putovanjePage.asText().contains("HTTP Error 404")) {
 			return null;
 		}
 			
 		HtmlTable timeTable = (HtmlTable)putovanjePage.getElementsByTagName("table").item(1);
 		
 		Putovanje putovanje = new Putovanje();
 		Linija linija = null;
 		Stajaliste stajaliste = null;
 		ListaLinija listaLinija = new ListaLinija();
 		ListaStajalista listaStajalista = null;
 			
 		for (HtmlTableRow row : timeTable.getRows()) {
 			
 			// skip header and last row
 			if (row == timeTable.getRow(0) || row == timeTable.getRow(timeTable.getRowCount() - 1))
 				continue;
 			
 			HtmlTableCell KolodvorCell = row.getCells().get(0);
 			HtmlTableCell DolazakCell = row.getCells().get(1);	
 			HtmlTableCell OdlazakCell = row.getCells().get(2);
 			HtmlTableCell CekanjeCell = row.getCells().get(3);
 			HtmlTableCell VlakCell = row.getCells().get(4);
 			HtmlTableCell KatCell = row.getCells().get(5);
 			
 			String kolodvor = row.getCells().get(0).asText().trim();
 			String dolazak = row.getCells().get(1).asText().trim();	
 			String odlazak = row.getCells().get(2).asText().trim();
 			String cekanje = row.getCells().get(3).asText().trim();
 			String vlak = row.getCells().get(4).asText().trim();
 			String kat = row.getCells().get(5).asText().trim();			
 			
 			/* open new line if */
 			if (
 				(dolazak.isEmpty() && !odlazak.isEmpty()) || 
 				(row == timeTable.getRow(1)) ||
 				(kat.contains("PRESJEDANJE"))
 			){
 				//System.out.println("opening line");
 				
 				stajaliste = new Stajaliste();
 				stajaliste.setStajaliste(kolodvor);
 				stajaliste.setVrijemeDolaskaStajaliste(dolazak);
 				stajaliste.setVrijemeOdlaskaStajaliste(odlazak);
 				
 				listaStajalista = new ListaStajalista();
 				listaStajalista.add(stajaliste);
 				
 				linija = new Linija();
 				linija.setOdlazniKolodvor(kolodvor);
 				linija.setVrijemeOdlaska(odlazak);
 				linija.setListaStajalista(listaStajalista);
 				
 				
 				
 				
 				
 				ListaOznaka oznake = oznakaResolverService.resolve(KatCell);
 				linija.setOznake(oznake);
 			} 
 			/* close current line if */
 			else if (
 				(odlazak.isEmpty() && !dolazak.isEmpty()) ||
 				(row == timeTable.getRow(timeTable.getRowCount() - 2)) ||
 				(!cekanje.isEmpty())  ||
 				(timeTable.getRow(row.getIndex() + 1).getCell(0) != null && timeTable.getRow(row.getIndex() + 1).getCell(5).asText().trim().contains("PRESJEDANJE"))
 			){
 				//System.out.println("closing line");
 				linija.setDolazniKolodvor(kolodvor);
 				linija.setVrijemeDolaska(dolazak);
 				linija.setNazivLinije(vlak);
 				
 				String vrijemeVoznje = periodService.calulatePeriodFromAToB(linija.getVrijemeOdlaska(), linija.getVrijemeDolaska());
 				
 				//log.info("setting vrijemeVoznje:" + vrijemeVoznje);
 				
 				stajaliste = new Stajaliste();
 				stajaliste.setStajaliste(kolodvor);
 				stajaliste.setVrijemeDolaskaStajaliste(dolazak);
 				stajaliste.setVrijemeOdlaskaStajaliste(odlazak);
 				
 				listaStajalista.add(stajaliste);
 
 				
 				linija.setTrajanjeVoznje(vrijemeVoznje);
 				listaLinija.add(linija);
 			} else {
 				/* samo stanica */
 				stajaliste = new Stajaliste();
 				stajaliste.setStajaliste(kolodvor);
 				stajaliste.setVrijemeDolaskaStajaliste(dolazak);
 				stajaliste.setVrijemeOdlaskaStajaliste(odlazak);
 				
 				listaStajalista.add(stajaliste);
 			}
 		}
 		
 		putovanje.setListaLinija(listaLinija);
 		Integer brojPresjedanja = putovanje.getListaLinija().getLinije().size() - 1;
 				
 		if (brojPresjedanja == 0) {
 			putovanje.setIzravno(true);
 			
 			String trajanjePutovanja = null;
 			
 			Pattern trajanjePutovanjaPattern = Pattern.compile("Ukupno trajanje putovanja: ([0-9]+:[0-9]+)");
 			Matcher trajanjePutovanjaMatcher = trajanjePutovanjaPattern.matcher(putovanjePage.asText());
 
 			if (trajanjePutovanjaMatcher.find()) {
 				trajanjePutovanja = trajanjePutovanjaMatcher.group(1);
 			}
 			
 			putovanje.setUkupnoTrajanjePutovanja(trajanjePutovanja);
 			
 		} else {
 			putovanje.setIzravno(false);
 			putovanje.setBrojPresjedanja(brojPresjedanja);
 			
 			String trajanjePutovanja = null;
 			String trajanjeVoznje = null;
 			String trajanjeCekanja = null;
 			
 			/*
 			</TABLE><P><FONT FACE=Verdana,Arial,Helvetica SIZE=1 COLOR="#000000">
 			Broj presjedanja:  1                                                            <BR>
 			Trajanje vožnje: 09:14                                                          <BR>
 			Vrijeme čekanja: 00:39                                                          <BR>
 			     Ukupno trajanje putovanja: 09:53                                           <BR>
 			     */
 			
 			Pattern trajanjePutovanjaPattern = Pattern.compile("Ukupno trajanje putovanja: ([0-9]+:[0-9]+)");
 			Matcher trajanjePutovanjaMatcher = trajanjePutovanjaPattern.matcher(putovanjePage.asText());
 			
 			Pattern trajanjeVoznjePattern = Pattern.compile("Trajanje vo.*?nje: ([0-9]+:[0-9]+)");
 			Matcher trajanjeVoznjeMatcher = trajanjeVoznjePattern.matcher(putovanjePage.asText());
 			
 			Pattern trajanjeCekanjaPattern = Pattern.compile("Vrijeme .*ekanja: ([0-9]+:[0-9]+)");
 			Matcher trajanjeCekanjaMatcher = trajanjeCekanjaPattern.matcher(putovanjePage.asText());
 
 			if (trajanjePutovanjaMatcher.find()) {
 				trajanjePutovanja = trajanjePutovanjaMatcher.group(1);
 			}
 
 			if (trajanjeVoznjeMatcher.find()) {
 				trajanjeVoznje = trajanjeVoznjeMatcher.group(1);
 			}
 
 			if (trajanjeCekanjaMatcher.find()) {
 				trajanjeCekanja = trajanjeCekanjaMatcher.group(1);
 			}
 
 
 			putovanje.setUkupnoTrajanjePutovanja(trajanjePutovanja);
 			putovanje.setUkupnoTrajanjeVoznje(trajanjeVoznje);
 			putovanje.setUkupnoTrajanjeCekanja(trajanjeCekanja);
 		}
 		
 		return putovanje;
 	}
 	
 	
 }
