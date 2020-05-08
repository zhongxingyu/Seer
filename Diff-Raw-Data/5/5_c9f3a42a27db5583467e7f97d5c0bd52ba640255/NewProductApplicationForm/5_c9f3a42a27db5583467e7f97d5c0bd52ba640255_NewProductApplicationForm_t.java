 /*
  * $Id:$
  *
  * Copyright (C) 2002 Idega hf. All Rights Reserved.
  *
  * This software is the proprietary information of Idega hf.
  * Use is subject to license terms.
  *
  */
 package is.idega.idegaweb.atvr.supplier.application.presentation;
 
 import com.idega.presentation.Block;
 import com.idega.presentation.IWContext;
 import com.idega.presentation.PresentationObject;
 import com.idega.presentation.Table;
 import com.idega.presentation.ui.DropdownMenu;
 import com.idega.presentation.ui.Form;
 import com.idega.presentation.ui.SubmitButton;
 import com.idega.presentation.ui.TextInput;
 import com.idega.util.IWTimestamp;
 
 import is.idega.idegaweb.atvr.supplier.application.business.NewProductApplicationBusiness;
 import is.idega.idegaweb.atvr.supplier.application.data.NewProductApplication;
 
 import java.rmi.RemoteException;
 
 /**
  * This class does something very clever.....
  * 
  * @author <a href="palli@idega.is">Pall Helgason</a>
  * @version 1.0
  */
 public class NewProductApplicationForm extends Block {
 	private final static String IW_BUNDLE_IDENTIFIER = "is.idega.idegaweb.atvr";
 
 	private final static int ACTION_VIEW_FORM = 0;
 	private final static int ACTION_SUBMIT_FORM = 1;
 
 	private final static int TYPE_TRIAL = 0;
 	private final static int TYPE_SPECIAL = 1;
 	private final static int TYPE_MONTH = 2;
 	private final static int TYPE_TOBACCO = 3;
 
 	private final static String PARAM_FORM_TYPE = "npa_type";
 	private final static String PARAM_FORM_CATEGORY = "npa_cat";
 	private final static String PARAM_FORM_SUBMIT = "npa_submit";
	private final static String PARAM_FORM_SUBMIT_X = "npa_submit.x";
	private final static String PARAM_FORM_SUBMIT_Y = "npa_submit.y";
 
 	private final static String PARAM_DESC = "npa_desc";
 	private final static String PARAM_DESC2 = "npa_desc2";
 	private final static String PARAM_QUANTITY = "npa_qty";
 	private final static String PARAM_STRENGTH = "npa_str";
 	private final static String PARAM_PRODUCER = "npa_prdc";
 	private final static String PARAM_COUNTRY = "npa_ctr";
 	private final static String PARAM_BAR_CODE = "npa_bar";
 	private final static String PARAM_SUB_CATEGORY = "npa_sub_cat";
 	private final static String PARAM_AMOUNT = "npa_amount";
 	private final static String PARAM_WEIGHT = "npa_weigth";
 	private final static String PARAM_MONOXIDE = "npa_monoxide";
 
 	private String _type = "0";
 
 	public String getBundleIdentifier() {
 		return IW_BUNDLE_IDENTIFIER;
 	}
 
 	private void control(IWContext iwc) {
		if (iwc.getParameter(PARAM_FORM_SUBMIT) == null && iwc.getParameter(PARAM_FORM_SUBMIT_X) == null && iwc.getParameter(PARAM_FORM_SUBMIT_Y) == null) 
 			showForm(iwc);
 		else
 			submitForm(iwc);
 	}
 
 	private void submitForm(IWContext iwc) {
 		try {
 			NewProductApplication appl = getApplicationBusiness(iwc).getNewApplication();
 
 			String desc = iwc.getParameter(PARAM_DESC);
 			String desc2 = iwc.getParameter(PARAM_DESC2);
 			String qty = iwc.getParameter(PARAM_QUANTITY);
 			String str = iwc.getParameter(PARAM_STRENGTH);
 			String prod = iwc.getParameter(PARAM_PRODUCER);
 			String ctry = iwc.getParameter(PARAM_COUNTRY);
 			String bar = iwc.getParameter(PARAM_BAR_CODE);
 			String amount = iwc.getParameter(PARAM_AMOUNT);
 			String cat = iwc.getParameter(PARAM_FORM_CATEGORY);
 			String sub = iwc.getParameter(PARAM_SUB_CATEGORY);
 			String weight = iwc.getParameter(PARAM_WEIGHT);
 			String monoxide = iwc.getParameter(PARAM_MONOXIDE);
 			
 			appl.setApplicationType(_type);
 			appl.setDescription(desc);
 			appl.setDescription2(desc2);
 			appl.setQuantity(qty);
 			appl.setStrength(str);
 			appl.setProducer(prod);
 			appl.setCountryOfOrigin(ctry);
 			appl.setBarCode(bar);
 			appl.setAmount(amount);
 			appl.setWeigth(weight);
 	//		appl.setProductCategoryId();
 			appl.setSupplierId(iwc.getUserId());
 			appl.setApplicationSent(IWTimestamp.getTimestampRightNow());
 			if (monoxide != null)
 				appl.setCarbonMonoxide(Float.parseFloat(monoxide));
 			
 			getApplicationBusiness(iwc).insertApplication(appl);
 			
 			add("Umskn send");
 			
 			return;
 		}
 		catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		add("Villa vi sendingu umsknar");
 	}
 
 	/*
 	 * 
 	 */
 	private void showForm(IWContext iwc) {
 		int typeId = parseType(_type);
 
 		Form form = new Form();
 
 		if (typeId == TYPE_TRIAL || typeId == TYPE_SPECIAL || typeId == TYPE_MONTH) {
 			Table t = new Table(2, 12);
 			t.add("Lsing", 1, 1);
 			t.add("Lsing 2", 1, 2);
 			t.add("Milliltrar", 1, 3);
 			t.add("Vnstyrkur", 1, 4);
 			t.add("Framleiandi", 1, 5);
 			t.add("Upprunaland", 1, 6);
 			t.add("Strikamerki", 1, 7);
 			t.add("Vruflokkur", 1, 8);
 			t.add("Flokksdeild", 1, 9);
 			t.add("Flskur pr. ks.", 1, 10);
 
 			TextInput desc = new TextInput(PARAM_DESC);
 			TextInput desc2 = new TextInput(PARAM_DESC2);
 			TextInput qty = new TextInput(PARAM_QUANTITY);
 			TextInput str = new TextInput(PARAM_STRENGTH);
 			TextInput prod = new TextInput(PARAM_PRODUCER);
 			TextInput ctry = new TextInput(PARAM_COUNTRY);
 			TextInput bar = new TextInput(PARAM_BAR_CODE);
 			TextInput bottles = new TextInput(PARAM_AMOUNT);
 
 			t.add(desc, 2, 1);
 			t.add(desc2, 2, 2);
 			t.add(qty, 2, 3);
 			t.add(str, 2, 4);
 			t.add(prod, 2, 5);
 			t.add(ctry, 2, 6);
 			t.add(bar, 2, 7);
 			t.add(bottles, 2, 10);
 
 			String selected = iwc.getParameter(PARAM_FORM_CATEGORY);
 
 			DropdownMenu category = (DropdownMenu) getCategoryDropdown(iwc, selected);
 //			category.setToSubmit();
 			t.add(category, 2, 8);
 			t.add(getSubCategoryDropdown(iwc, selected), 2, 9);
 
 			SubmitButton submit = new SubmitButton(PARAM_FORM_SUBMIT, "Senda");
 			submit.setAsImageButton(true);
 			t.setAlignment(2, 12, "Right");
 			t.add(submit, 2, 12);
 
 			form.add(t);
 		}
 		else if (typeId == TYPE_TOBACCO) {
 			Table t = new Table(2, 10);
 			t.add("Lsing", 1, 1);
 			t.add("Vruflokkur", 1, 2);
 			t.add("Flokksdeild", 1, 3);
 			t.add("Framleiandi", 1, 4);
 			t.add("Upprunaland", 1, 5);
 			t.add("Tjrumagn", 1, 6);
 			t.add("yngd tbaks", 1, 7);
 			t.add("Magn kolmnoxs",1,8);
 
 			TextInput desc = new TextInput(PARAM_DESC);
 			TextInput prod = new TextInput(PARAM_PRODUCER);
 			TextInput ctry = new TextInput(PARAM_COUNTRY);
 			TextInput amount = new TextInput(PARAM_AMOUNT);
 			TextInput weight = new TextInput(PARAM_WEIGHT);
 			TextInput monoxide = new TextInput(PARAM_MONOXIDE);
 			monoxide.setAsFloat();
 
 			t.add(desc, 2, 1);
 			t.add(prod, 2, 4);
 			t.add(ctry, 2, 5);
 			t.add(amount, 2, 6);
 			t.add(weight, 2, 7);
 			t.add(monoxide,2,8);
 
 			String selected = iwc.getParameter(PARAM_FORM_CATEGORY);
 
 			DropdownMenu category = (DropdownMenu) getCategoryDropdown(iwc, selected);
 //			category.setToSubmit();
 			t.add(category, 2, 2);
 			t.add(getSubCategoryDropdown(iwc, selected), 2, 3);
 
 			SubmitButton submit = new SubmitButton(PARAM_FORM_SUBMIT, "Senda");
 			submit.setAsImageButton(true);
 			t.setAlignment(2, 10, "Right");
 			t.add(submit, 2, 10);
 
 			form.add(t);
 		}
 
 		add(form);
 	}
 
 	private int parseType(String type) {
 		if (type == null)
 			return TYPE_TRIAL;
 
 		return Integer.parseInt(type);
 	}
 
 	public void main(IWContext iwc) {
 		control(iwc);
 	}
 
 //	private PresentationObject getTypeDropdown(IWContext iwc, int selected) {
 //		DropdownMenu menu = new DropdownMenu(PARAM_FORM_TYPE);
 //		menu.addMenuElement(TYPE_TRIAL, "Reynsla");
 //		menu.addMenuElement(TYPE_SPECIAL, "Srlisti");
 //		menu.addMenuElement(TYPE_MONTH, "Mnaarfl.");
 //		menu.addMenuElement(TYPE_TOBACCO, "Tbak");
 //
 //		menu.setSelectedElement(selected);
 //
 //		return menu;
 //	}
 
 	private PresentationObject getCategoryDropdown(IWContext iwc, String selected) {
 		DropdownMenu menu = new DropdownMenu(PARAM_FORM_CATEGORY);
 		menu.addMenuElement("01.","Rauvn");
 		menu.addMenuElement("02.","Hvtvn");
 		menu.addMenuElement("03.","Rsavn");
 		menu.addMenuElement("04.","Freyivn");
 		menu.addMenuElement("05.","Styrkt vn");
 		menu.addMenuElement("06.","vaxtavn");
 		menu.addMenuElement("10.","Brand");
 		menu.addMenuElement("11.","vaxtabrand");
 		menu.addMenuElement("13.","Visk");
 		menu.addMenuElement("14.","Romm");
 		menu.addMenuElement("15.","Tequila o.fl.");
 		menu.addMenuElement("16.","krydda brennivn og vodka");
 		menu.addMenuElement("17.","Gin & snever");
 		menu.addMenuElement("18.","Snafs");
 		menu.addMenuElement("20.","Lkjr");
 		menu.addMenuElement("21.","Bitterar, kryddvn, aperitfar");
 		menu.addMenuElement("23.","Blandair drykkir");
 		menu.addMenuElement("36.","Umbir");
 		menu.addMenuElement("60.","Lagerbjr");
 		menu.addMenuElement("61.","l");
 		menu.addMenuElement("62.","Arar bjrtegundir");
 		menu.addMenuElement("89.","Niurlagir vextir");
 		menu.addMenuElement("90.","Neftbak");
 		menu.addMenuElement("91.","Reyktbak");
 		menu.addMenuElement("92.","Vindlingar");
 		menu.addMenuElement("93.","Vindlar");
 		menu.addMenuElement("94.","Munntbak");
 		
 
 		menu.setSelectedElement(selected);
 
 		return menu;
 	}
 
 	private PresentationObject getSubCategoryDropdown(IWContext iwc, String category) {
 		DropdownMenu menu = new DropdownMenu(PARAM_SUB_CATEGORY);
 
 		menu.addMenuElement("01.1","Rauvn - strri en 750 ml");
 		menu.addMenuElement("01.10","Rauvn Argentna");
 		menu.addMenuElement("01.11","Rauvn Chile");
 		menu.addMenuElement("01.12","Rauvn Suur-Afrka");
 		menu.addMenuElement("01.13","Rauvn strala, Nja Sjland");
 		menu.addMenuElement("01.13.1","Rauvn Suur-strala");
 		menu.addMenuElement("01.2","Rauvn - minni en 500 ml");
 		menu.addMenuElement("01.3","Rauvn Frakkland");
 		menu.addMenuElement("01.3.1","Rauvn Bordeaux/Bergerac");
 		menu.addMenuElement("01.3.1.1","Rauv. Medoc,Graves,Libournais");
 		menu.addMenuElement("01.3.2","Rauvn Brgund");
 		menu.addMenuElement("01.3.2.1","Rauvn Beaujolais");
 		menu.addMenuElement("01.3.2.2","Cote de Nuits, Cote de Beaune");
 		menu.addMenuElement("01.3.3","Rauvn Rn og Prvens");
 		menu.addMenuElement("01.4","Rauvn tala");
 		menu.addMenuElement("01.4.1","Rauvn Norur-tala");
 		menu.addMenuElement("01.4.1.1","Rauvn Toskana");
 		menu.addMenuElement("01.4.1.2","Rauvn Piemonte");
 		menu.addMenuElement("01.4.1.3","Rauvn Veneto");
 		menu.addMenuElement("01.4.2","Rauvn Suur-tala");
 		menu.addMenuElement("01.5","Rauvn Spnn");
 		menu.addMenuElement("01.5.1","Rauvn Rioja");
 		menu.addMenuElement("01.5.2","Rauvn Katalna");
 		menu.addMenuElement("01.6","Rauvn Portgal");
 		menu.addMenuElement("01.7","Rauvn Evrpa anna");
 		menu.addMenuElement("01.8","Rauvn Washington, Oregon");
 		menu.addMenuElement("01.9","Rauvn Kaliforna");
 		menu.addMenuElement("01.9.1","Rauvn Napa og Sonoma");
 		menu.addMenuElement("01.99","Rauvn - nnur");
 		menu.addMenuElement("02.1","Hvtvn - strri en 750 ml");
 		menu.addMenuElement("02.10","Hvtvn Kaliforna");
 		menu.addMenuElement("02.10.1","Hvtvn Napa og Sonoma");
 		menu.addMenuElement("02.11","Hvtvn Chile");
 		menu.addMenuElement("02.12","Hvtvn Suur-Afrka");
 		menu.addMenuElement("02.13","Hvtvn strala");
 		menu.addMenuElement("02.13.1","Hvtvn Nja Sjland");
 		menu.addMenuElement("02.2","Hvtvn - minni en 500 ml");
 		menu.addMenuElement("02.3","Hvtvn Frakkland");
 		menu.addMenuElement("02.3.1","Hvtvn Bordeaux");
 		menu.addMenuElement("02.3.2","Hvtvn Brgund");
 		menu.addMenuElement("02.3.3","Hvtvn Alsace");
 		menu.addMenuElement("02.3.4","Hvtvn Loire");
 		menu.addMenuElement("02.4","Hvtvn tala");
 		menu.addMenuElement("02.5","Hvtvn Spnn");
 		menu.addMenuElement("02.6","Hvtvn Portgal");
 		menu.addMenuElement("02.7","Hvtvn skaland");
 		menu.addMenuElement("02.7.1","Hvtvn Riesling - Qmp");
 		menu.addMenuElement("02.8","Hvtvn Evrpa anna");
 		menu.addMenuElement("02.9","Hvtvn Washington og Oregon");
 		menu.addMenuElement("02.90","Hvtvn - st");
 		menu.addMenuElement("02.99","Hvtvn - nnur");
 		menu.addMenuElement("03.1","Rsavn strri en 750 ml");
 		menu.addMenuElement("03.2","Rsavn Blush - Roavn");
 		menu.addMenuElement("03.9","Rsavn - nnur");
 		menu.addMenuElement("04.1","Champagne");
 		menu.addMenuElement("04.2","Freyvn Asti");
 		menu.addMenuElement("04.9","Freyivn - nnur");
 		menu.addMenuElement("05.1.1","Srr - Fino og skyldar tegund");
 		menu.addMenuElement("05.1.2","Srr - Amontillado og skyldar");
 		menu.addMenuElement("05.1.3","Srr - Olroso og skyldar teg");
 		menu.addMenuElement("05.2.1","Portvn - hvt");
 		menu.addMenuElement("05.2.2","Portvn - tunnurosku (Tawny)");
 		menu.addMenuElement("05.2.3","Portvn - rau (Ruby)");
 		menu.addMenuElement("05.2.3.1","Portvn - rgangsvn, rau");
 		menu.addMenuElement("05.9","Styrkt vn - nnur");
 		menu.addMenuElement("06.1","Sder");
 		menu.addMenuElement("06.2","vaxtavn");
 		menu.addMenuElement("06.3","Hrsgrjnavn");
 		menu.addMenuElement("06.9","vaxtavn - blndur");
 		menu.addMenuElement("10.1","Cognac VS og VSOP");
 		menu.addMenuElement("10.1.1","Cognac - nnur");
 		menu.addMenuElement("10.2","Armagnac");
 		menu.addMenuElement("10.9","Brand - nnur");
 		menu.addMenuElement("11.1","Calvados");
 		menu.addMenuElement("11.2","Anna vaxtabrand");
 		menu.addMenuElement("11.3","Hratbrand / Grappa");
 		menu.addMenuElement("13.1","Visk - Skoskt");
 		menu.addMenuElement("13.1.1","Visk - Skoskt malt");
 		menu.addMenuElement("13.2","Visk - rskt");
 		menu.addMenuElement("13.9","Visk - nnur");
 		menu.addMenuElement("14.1","Hvtt Romm fr Vestur-Indum");
 		menu.addMenuElement("14.2","Ljst Romm fr Vestur-Indum");
 		menu.addMenuElement("14.3","Dkkt Romm fr Vestur-Indum");
 		menu.addMenuElement("14.9","Romm - nnur, .m.t. Kryddromm");
 		menu.addMenuElement("15.1","Tequila");
 		menu.addMenuElement("16.1","Vodka");
 		menu.addMenuElement("16.2","Anna krydda brennivn");
 		menu.addMenuElement("17.1","Gin");
 		menu.addMenuElement("17.2","Snever");
 		menu.addMenuElement("18.1","Akvavt");
 		menu.addMenuElement("18.2","Ans");
 		menu.addMenuElement("18.9","Arir Snafsar");
 		menu.addMenuElement("20.1","Rjmalkjr");
 		menu.addMenuElement("20.2","Hnetu og baunalkjr");
 		menu.addMenuElement("20.2.1","Kaffi/Kaklkjr");
 		menu.addMenuElement("20.2.2","Kkoslkjr");
 		menu.addMenuElement("20.3","Grasa og Kryddlkjr");
 		menu.addMenuElement("20.3.1","Mintulkjr");
 		menu.addMenuElement("20.3.2","Lkjr me ansbragi");
 		menu.addMenuElement("20.4","vaxtalkjr");
 		menu.addMenuElement("20.4.1","Epla/Perulkjr");
 		menu.addMenuElement("20.4.2","Ferskju/Aprkskulkjr");
 		menu.addMenuElement("20.4.3","Struslkjr");
 		menu.addMenuElement("20.4.4","Berjalkjr");
 		menu.addMenuElement("20.9","Arir lkjrar");
 		menu.addMenuElement("21.1","Bitter");
 		menu.addMenuElement("21.2","Kryddvn");
 		menu.addMenuElement("21.2.1","Kryddvn - Vermt");
 		menu.addMenuElement("21.3","Apertfar");
 		menu.addMenuElement("23.1","Blandair drykkir - undir 6,5%");
 		menu.addMenuElement("23.9","Blandair drykkir - arir");
 		menu.addMenuElement("60.1","Lager - ljs  flskum");
 		menu.addMenuElement("60.1.1","Lager - ljs  fl. slenskur");
 		menu.addMenuElement("60.2","Lager - ljs  dsum");
 		menu.addMenuElement("60.2.1","Lager - ljs  dsum slenskur");
 		menu.addMenuElement("60.3","Lager - millidkkur/dkkur");
 		menu.addMenuElement("60.4","Lager - sterkur a.m.k. 6,2%");
 		menu.addMenuElement("60.9","Lager - annar");
 		menu.addMenuElement("61.1","l - Belga");
 		menu.addMenuElement("61.2","l - Bretland og rland");
 		menu.addMenuElement("61.3","l - skaland");
 		menu.addMenuElement("61.4","l - Stt og portari");
 		menu.addMenuElement("61.9","l - anna");
 		menu.addMenuElement("62.1","Hveitibjr");
 		menu.addMenuElement("62.2","Lambik");
 		menu.addMenuElement("62.9","Annar bjr");
 		menu.addMenuElement("92.1","Vindlingar - tjara 0 til 4 mg");
 		menu.addMenuElement("92.2","Vindlingar - tjara 5 til 7 mg");
 		menu.addMenuElement("92.3","Vindlingar - tjara yfir 7 mg");
 		menu.addMenuElement("93.1","Vindlar tbak < 3,15");
 		menu.addMenuElement("93.2","Vindlar tbak 3,15 - 4,25");
 		menu.addMenuElement("93.3","Vindlar tbak > 4,25");
 
 		return menu;
 	}
 	
 	private NewProductApplicationBusiness getApplicationBusiness(IWContext iwc) throws Exception {
 		return (NewProductApplicationBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, NewProductApplicationBusiness.class);
 	}	
 	
 	public void setApplicationType(String type) {
 		_type = type;	
 	}			
 }
