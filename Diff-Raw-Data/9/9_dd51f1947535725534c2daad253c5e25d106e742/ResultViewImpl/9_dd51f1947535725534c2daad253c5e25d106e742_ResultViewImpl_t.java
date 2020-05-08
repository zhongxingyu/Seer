 package ar.com.trabajos.broker.client.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import ar.com.trabajos.broker.client.messages.Mensajes;
 import ar.com.trabajos.broker.client.widget.PrecioFocusPanel;
 import ar.com.trabajos.broker.shared.entities.ResponseDTO;
 import ar.com.trabajos.broker.shared.enums.BrokerEnum;
 import ar.com.trabajos.broker.shared.enums.CoberturasEnum;
 import ar.com.trabajos.broker.shared.enums.DatosAutoEnum;
 import ar.com.trabajos.broker.shared.enums.DatosPersonaEnum;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ResultViewImpl extends Composite implements ResultView {
 
 	
 
 	@UiTemplate("ResultView.ui.xml")
 	interface ResultViewUiBinder extends UiBinder<Widget, ResultViewImpl> {}
 	private static ResultViewUiBinder uiBinder = 
 			GWT.create(ResultViewUiBinder.class);
 
 	@UiField PrecioFocusPanel ecRc;
 	@UiField PrecioFocusPanel ecTc;
 	@UiField PrecioFocusPanel ecTcf;
 	@UiField PrecioFocusPanel ecTcfg;
 	@UiField PrecioFocusPanel ecTr;
 	@UiField PrecioFocusPanel ecTdp;
 	
 	@UiField PrecioFocusPanel lRc;
 	@UiField PrecioFocusPanel lTc;
 	@UiField PrecioFocusPanel lTcf;
 	@UiField PrecioFocusPanel lTcfg;
 	@UiField PrecioFocusPanel lTr;
 	@UiField PrecioFocusPanel lTdp;
 	
 	@UiField PrecioFocusPanel nRc;
 	@UiField PrecioFocusPanel nTc;
 	@UiField PrecioFocusPanel nTcf;
 	@UiField PrecioFocusPanel nTcfg;
 	@UiField PrecioFocusPanel nTr;
 	@UiField PrecioFocusPanel nTdp;
 	
 	@UiField PrecioFocusPanel aRc;
 	@UiField PrecioFocusPanel aTc;
 	@UiField PrecioFocusPanel aTcf;
 	@UiField PrecioFocusPanel aTcfg;
 	@UiField PrecioFocusPanel aTr;
 	@UiField PrecioFocusPanel aTdp;
 	
 	@UiField (provided=true) 
 	HTMLPanel detallePlanPanel;
 	
 	@UiField Label datosPersona;
 	@UiField Label datosAuto;	
 	
 	private HashMap<BrokerEnum, HashMap<CoberturasEnum, String>> detallesCoberturas;
 	private ArrayList<PrecioFocusPanel> listaCamposGrupo;
 	private Presenter presenter;
 
 	public ResultViewImpl() {		
 		detallePlanPanel= new HTMLPanel("");
 		cargarDetallesPlan();
 		mostarSeleccionarCobertura();
 		initWidget(uiBinder.createAndBindUi(this));		
 		prepararHandlers();
 		inicializarGrupo();
 	}
 
 	
 
 	private void inicializarGrupo() {
 		listaCamposGrupo=new ArrayList<PrecioFocusPanel>();
 		listaCamposGrupo.add(ecRc);
 		listaCamposGrupo.add(ecTc);
 		listaCamposGrupo.add(ecTcf);
 		listaCamposGrupo.add(ecTcfg);
 		listaCamposGrupo.add(ecTr);
 		listaCamposGrupo.add(ecTdp);
 
 		listaCamposGrupo.add(lRc);
 		listaCamposGrupo.add(lTc);
 		listaCamposGrupo.add(lTcf);
 		listaCamposGrupo.add(lTcfg);
 		listaCamposGrupo.add(lTr);
 		listaCamposGrupo.add(lTdp);
 		
 		listaCamposGrupo.add(nRc);
 		listaCamposGrupo.add(nTc);
 		listaCamposGrupo.add(nTcf);
 		listaCamposGrupo.add(nTcfg);
 		listaCamposGrupo.add(nTr);
 		listaCamposGrupo.add(nTdp);
 		
 		listaCamposGrupo.add(aRc);
 		listaCamposGrupo.add(aTc);
 		listaCamposGrupo.add(aTcf);
 		listaCamposGrupo.add(aTcfg);
 		listaCamposGrupo.add(aTr);
 		listaCamposGrupo.add(aTdp);
 	}
 
 
 
 	private void mostarSeleccionarCobertura() {
 		detallePlanPanel.clear();
 		detallePlanPanel.add(new Label(Mensajes.SELECCIONAR_PLAN));		
 	}
 
 	private void prepararHandlers() {
 		
 		ecRc.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ELCOMERCIO).get(CoberturasEnum.RESPONSABILIDAD_CIVIL)), ClickEvent.getType());		
 		ecTdp.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ELCOMERCIO).get(CoberturasEnum.TOTAL_SIN_DANOS_PARCIALES)), ClickEvent.getType());
 		ecTc.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ELCOMERCIO).get(CoberturasEnum.TERCEROS_COPMLETOS)), ClickEvent.getType());
 		ecTcf.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ELCOMERCIO).get(CoberturasEnum.TERCEROS_COMPLETOS_FULL)), ClickEvent.getType());
 		ecTcfg.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ELCOMERCIO).get(CoberturasEnum.TERCEROS_COMPLETOS_FULL_GRANIZO)), ClickEvent.getType());
 		ecTr.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ELCOMERCIO).get(CoberturasEnum.TODO_RIESGO)), ClickEvent.getType());
 		
 		lRc.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.LIBERTY).get(CoberturasEnum.RESPONSABILIDAD_CIVIL)), ClickEvent.getType());
 		lTdp.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.LIBERTY).get(CoberturasEnum.TOTAL_SIN_DANOS_PARCIALES)), ClickEvent.getType());
 		lTc.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.LIBERTY).get(CoberturasEnum.TERCEROS_COPMLETOS)), ClickEvent.getType());
 		lTcf.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.LIBERTY).get(CoberturasEnum.TERCEROS_COMPLETOS_FULL)), ClickEvent.getType());
 		lTcfg.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.LIBERTY).get(CoberturasEnum.TERCEROS_COMPLETOS_FULL_GRANIZO)), ClickEvent.getType());
 		lTr.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.LIBERTY).get(CoberturasEnum.TODO_RIESGO)), ClickEvent.getType());
 		
 		nRc.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.NACION).get(CoberturasEnum.RESPONSABILIDAD_CIVIL)), ClickEvent.getType());
 		nTdp.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.NACION).get(CoberturasEnum.TOTAL_SIN_DANOS_PARCIALES)), ClickEvent.getType());
 		nTc.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.NACION).get(CoberturasEnum.TERCEROS_COPMLETOS)), ClickEvent.getType());
 		nTcf.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.NACION).get(CoberturasEnum.TERCEROS_COMPLETOS_FULL)), ClickEvent.getType());
 		nTcfg.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.NACION).get(CoberturasEnum.TERCEROS_COMPLETOS_FULL_GRANIZO)), ClickEvent.getType());
 		nTr.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.NACION).get(CoberturasEnum.TODO_RIESGO)), ClickEvent.getType());
 		
 		aRc.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ALLIANZ).get(CoberturasEnum.RESPONSABILIDAD_CIVIL)), ClickEvent.getType());
 		aTdp.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ALLIANZ).get(CoberturasEnum.TOTAL_SIN_DANOS_PARCIALES)), ClickEvent.getType());
 		aTc.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ALLIANZ).get(CoberturasEnum.TERCEROS_COPMLETOS)), ClickEvent.getType());
 		aTcf.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ALLIANZ).get(CoberturasEnum.TERCEROS_COMPLETOS_FULL)), ClickEvent.getType());
 		aTcfg.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ALLIANZ).get(CoberturasEnum.TERCEROS_COMPLETOS_FULL_GRANIZO)), ClickEvent.getType());
 		aTr.addHandler(new ResultViewImpl.CotizacionClickHandler(detallesCoberturas.get(BrokerEnum.ALLIANZ).get(CoberturasEnum.TODO_RIESGO)), ClickEvent.getType());
 	}
 	
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 	public Widget asWidget() {
 		return this;
 	}
 
 	@Override
 	public void setResponse(ResponseDTO response) {
 		switch (response.getBroker()) {
 		case ELCOMERCIO:
 			recibirRespuesta(response, ecRc, ecTc, ecTcf, ecTcfg, ecTr, ecTdp, "comercioRow");
 			break;
 		case LIBERTY:
 			recibirRespuesta(response, lRc, lTc, lTcf, lTcfg, lTr, lTdp, "libertyRow");
 			break;
 		case NACION:
 			recibirRespuesta(response, nRc, nTc, nTcf, nTcfg, nTr, nTdp, "nacionRow");
 			break;
 		case ALLIANZ:
 			recibirRespuesta(response, aRc, aTc, aTcf, aTcfg, aTr, aTdp, "allianzRow");
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void recibirRespuesta(ResponseDTO response, PrecioFocusPanel rc, PrecioFocusPanel tc, PrecioFocusPanel tcf, PrecioFocusPanel tcfg, PrecioFocusPanel tr, PrecioFocusPanel tt, String row) {
 		
 		if (response.getRc()==0 && response.getTc()==0 && response.getTcf()==0 && response.getTcfg()==0 && response.getTr()==0 && response.getTt()==0){
 			DOM.getElementById(row).getStyle().setDisplay(Style.Display.NONE);
 			DOM.getElementById(row + "Error").getStyle().setProperty("display", "table-row");
 			
 		}else {		
 			DOM.getElementById(row + "Error").getStyle().setDisplay(Style.Display.NONE);
 			DOM.getElementById(row).getStyle().setProperty("display", "table-row");
 			rc.clear();
 			if (response.getRc()!=0){
 				rc.add(new Label("$ "  + Integer.toString(response.getRc())));
 				rc.setActivo(true);
 			}else{
 				rc.add(new Image("img/cruz.png"));
 				rc.setActivo(false);
 			}
 			tc.clear();
 			if (response.getTc()!=0){
 				tc.add(new Label("$ "+ Integer.toString(response.getTc())));
 				tc.setActivo(true);
 			}else{
 				tc.add(new Image("img/cruz.png"));
 				tc.setActivo(false);
 			}
 			tcf.clear();
 			if (response.getTcf()!=0){
 				tcf.add(new Label("$ "+ Integer.toString(response.getTcf())));
 				tcf.setActivo(true);
 			}else{
 				tcf.add(new Image("img/cruz.png"));
 				tcf.setActivo(false);
 			}
 			tcfg.clear();
 			if (response.getTcfg()!=0){
 				tcfg.add(new Label("$ "+ Integer.toString(response.getTcfg())));
 				tcfg.setActivo(true);
 			}else{
 				tcfg.add(new Image("img/cruz.png"));
 				tcfg.setActivo(false);
 			}
 			tr.clear();
 			if (response.getTr()!=0){
 				tr.add(new Label("$ "+ Integer.toString(response.getTr())));
 				tr.setActivo(true);
 			}else{
 				tr.add(new Image("img/cruz.png"));
 				tr.setActivo(false);
 			}
 			tt.clear();
 			if (response.getTt()!=0){
 				tt.add(new Label("$ "+ Integer.toString(response.getTt())));
 				tt.setActivo(true);
 			}else{
 				tt.add(new Image("img/cruz.png"));
 				tt.setActivo(false);
 			}
 		}
 	}
 
 	@Override
 	public void clearResults() {
 		ecRc.clear();
 		ecTc.clear();
 		ecTcf.clear();
 		ecTcfg.clear();
 		ecTr.clear();
 		ecTdp.clear();
 			
 		lRc.clear();
 		lTc.clear();
 		lTcf.clear();
 		lTcfg.clear();
 		lTr.clear();
 		lTdp.clear();
 			
 		nRc.clear();
 		nTc.clear();
 		nTcf.clear();
 		nTcfg.clear();
 		nTr.clear();
 		nTdp.clear();
 		
 		aRc.clear();
 		aTc.clear();
 		aTcf.clear();
 		aTcfg.clear();
 		aTr.clear();
 		aTdp.clear();
 		
		DOM.getElementById("comercioRowError").getStyle().setDisplay(Style.Display.NONE);
		DOM.getElementById("comercioRow").getStyle().setProperty("display", "table-row");
		DOM.getElementById("libertyRowError").getStyle().setDisplay(Style.Display.NONE);
		DOM.getElementById("libertyRow").getStyle().setProperty("display", "table-row");
		DOM.getElementById("allianzRowError").getStyle().setDisplay(Style.Display.NONE);
		DOM.getElementById("allianzRow").getStyle().setProperty("display", "table-row");
		DOM.getElementById("nacionRowError").getStyle().setDisplay(Style.Display.NONE);
		DOM.getElementById("nacionRow").getStyle().setProperty("display", "table-row");
		
 		mostarSeleccionarCobertura();
 		
 		for (PrecioFocusPanel panel : listaCamposGrupo) {
 			panel.removeStyleName("precioactive");
 		}
 	}
 
 	@Override
 	public void setDatosPersona(HashMap<DatosPersonaEnum, String> datos) {
 		datosPersona.setText(datos.get(DatosPersonaEnum.NOMBRE_APELLIDO) + " - " + datos.get(DatosPersonaEnum.EDAD) + " años - " + datos.get(DatosPersonaEnum.PROVINCIA_STRING));		
 	}
 
 	@Override
 	public void setDatosAuto(HashMap<DatosAutoEnum, String> datos) {
 		datosAuto.setText(datos.get(DatosAutoEnum.MARCA_STRING) + " - " + datos.get(DatosAutoEnum.MODELO_AUTO_STRING) + " - " + datos.get(DatosAutoEnum.MODELO));		
 	}
 	
 	@Override
 	public void showLoadings(){
 		ecRc.add(new Image("img/loader.gif"));
 		ecTc.add(new Image("img/loader.gif"));
 		ecTcf.add(new Image("img/loader.gif"));
 		ecTcfg.add(new Image("img/loader.gif"));
 		ecTr.add(new Image("img/loader.gif"));
 		ecTdp.add(new Image("img/loader.gif"));
 			
 		lRc.add(new Image("img/loader.gif"));
 		lTc.add(new Image("img/loader.gif"));
 		lTcf.add(new Image("img/loader.gif"));
 		lTcfg.add(new Image("img/loader.gif"));
 		lTr.add(new Image("img/loader.gif"));
 		lTdp.add(new Image("img/loader.gif"));
 			
 		nRc.add(new Image("img/loader.gif"));
 		nTc.add(new Image("img/loader.gif"));
 		nTcf.add(new Image("img/loader.gif"));
 		nTcfg.add(new Image("img/loader.gif"));
 		nTr.add(new Image("img/loader.gif"));
 		nTdp.add(new Image("img/loader.gif"));
 			
 		aRc.add(new Image("img/loader.gif"));
 		aTc.add(new Image("img/loader.gif"));
 		aTcf.add(new Image("img/loader.gif"));
 		aTcfg.add(new Image("img/loader.gif"));
 		aTr.add(new Image("img/loader.gif"));
 		aTdp.add(new Image("img/loader.gif"));
 	}
 	
 	public class CotizacionClickHandler implements ClickHandler {
 		
 		private String mensaje;
 		
 		public CotizacionClickHandler(String mensajeMostrar) {
 			this.mensaje= mensajeMostrar;
 		}
 
 		@Override
 		public void onClick(ClickEvent event) {
 			PrecioFocusPanel panelClickeado = ((PrecioFocusPanel) event.getSource());
 			detallePlanPanel.clear();
 			if (panelClickeado.isActivo()){
 				detallePlanPanel.add(new Label (mensaje));
 				for (PrecioFocusPanel panel : listaCamposGrupo) {
 					panel.removeStyleName("precioactive");
 				}
 				panelClickeado.addStyleName("precioactive");
 			}else{
 				for (PrecioFocusPanel panel : listaCamposGrupo) {
 					panel.removeStyleName("precioactive");
 				}
 			}
 		}
 
 	}
 	private void cargarDetallesPlan() {
 		detallesCoberturas = new HashMap<BrokerEnum, HashMap<CoberturasEnum,String>>();
 		
 		HashMap<CoberturasEnum, String> coberturas = new HashMap<CoberturasEnum, String>();
 		coberturas.put(CoberturasEnum.RESPONSABILIDAD_CIVIL, "Responsabilidad Civil hacia terceros (*)");
 		coberturas.put(CoberturasEnum.TOTAL_SIN_DANOS_PARCIALES, "Responsabilidad Civil hacia terceros (*)  y Daños Totales (Robo Total, Incendio Total y Destrucción Total)");
 		coberturas.put(CoberturasEnum.TERCEROS_COPMLETOS, "Responsabilidad Civil hacia terceros (*), Robo Total y Parcial, Incendio Total y Parcial y Destrucción Total por Accidente con cláusula del 20% (1). Vidrios laterales y cerraduras  por intento de robo evento por año");
 		coberturas.put(CoberturasEnum.TERCEROS_COMPLETOS_FULL, "Responsabilidad Civil hacia terceros (*), Robo Total y Parcial, Incendio Total y Parcial y Destrucción Total por Accidente con cláusula del 80% (2). Vidrios laterales y cerraduras  por intento de robo un evento por año.");
 		coberturas.put(CoberturasEnum.TERCEROS_COMPLETOS_FULL_GRANIZO, "Responsabilidad Civil hacia terceros (*), Robo Total y Parcial, Incendio Total y Parcial y Destrucción Total por Accidente con cláusula del 80% (2). Vidrios laterales y cerraduras  por intento de robo un evento por año. Y beneficios adicionales, tales como:   Daños a cerraduras hasta un límite anual de $ 500.- por cualquier acontecimiento.   Parabrisas y lunetas hasta un límite anual de $5.000.- (Con excepción de T. del Fuego que no cuenta con esta cobertura).   Cláusula de destrucción total del 80%.   Reposición de la unidad OKM dentro del año.   Daños a consecuencia de robo hasta un límite de $ 5.000.-   Granizo hasta la suma de $ 5.000.-   Reposición a nuevo de neumáticos hasta vehículos con 2 años de antigüedad.");
 		coberturas.put(CoberturasEnum.TODO_RIESGO, "Comprende Responsabilidad Civil hacia terceros (*), Robo Total y Parcial, Incendio  Total y Parcial, Daño Parcial (según Fcia.) y Destrucción Total por Accidente con cláusula del 80% (2), y beneficios adicionales, tales como:   Daños a cerraduras hasta un límite anual de $ 500.-   Parabrisas y lunetas hasta un límite anual de $5.000.-   Cláusula de destrucción total del 80%.   Reposición de la unidad OKM dentro del año.   Daños a consecuencia de robo hasta un límite de $ 5.000   Granizo hasta la suma de $ 5.000.-   Reposición a nuevo de neumáticos a vehículos con hasta 2 años de antigüedad (año en curso y año anterior) en caso de Robo.");
 		detallesCoberturas.put(BrokerEnum.NACION, coberturas);
 		
 		coberturas = new HashMap<CoberturasEnum, String>();
 		coberturas.put(CoberturasEnum.RESPONSABILIDAD_CIVIL, "Responsabilidad Civil");
 		coberturas.put(CoberturasEnum.TOTAL_SIN_DANOS_PARCIALES, "Responsabilidad Civil. Robo / Hurto total. Incendio total. Pérdida Total por Accidente");
 		coberturas.put(CoberturasEnum.TERCEROS_COPMLETOS, "Responsabilidad Civil. Robo / Hurto parcial y total. Incendio parcial y total. Pérdida Total por Accidente. Pérdida Total por Accidente. Rastreo vehicular opcional.");
 		coberturas.put(CoberturasEnum.TERCEROS_COMPLETOS_FULL, "Responsabilidad Civil. Robo / Hurto parcial y total. Incendio parcial y total. Vidrios Laterales. Parabrisas y Luneta Trasera. Cerradura. Cobertura de Incendio sin Franquicia. GRANIZO ADICIONAL DE COBERTURA. Cláusula de destrucción total al 80%. Daños parciales como consecuencia de Robo. Reposición de cubiertas (sin consideración de desgaste, como consecuencia de Robo). Rastreo vehicular opcional.");
 		coberturas.put(CoberturasEnum.TERCEROS_COMPLETOS_FULL_GRANIZO, "Robo e Incendio Total en Garaje. Responsabilidad Civil. Robo / Hurto parcial. Robo / Hurto total. Incendio parcial. Incendio total. Pérdida Total por Accidente. Vidrios Laterales. Parabrisas y Luneta Trasera. Cerradura. Cobertura de Incendio sin Franquicia. GRANIZO ADICIONAL DE COBERTURA. Cláusula de destrucción total al 80%. Daños parciales como consecuencia de Robo. Reposición de cubiertas sin consideración de desgaste, como consecuencia de Robo.");
 		coberturas.put(CoberturasEnum.TODO_RIESGO, "Responsabilidad Civil. Robo / Hurto parcial y total. Incendio parcial y total. Vidrios Laterales. Parabrisas y Luneta Trasera. Cerradura. Cobertura de Incendio sin Franquicia. GRANIZO ADICIONAL DE COBERTURA. Cláusula de destrucción total al 80%. Daños parciales como consecuencia de Robo. Reposición de cubiertas (sin consideración de desgaste, como consecuencia de Robo). Rastreo vehicular opcional.");
 		detallesCoberturas.put(BrokerEnum.ELCOMERCIO, coberturas);
 		
 		coberturas = new HashMap<CoberturasEnum, String>();
 		coberturas.put(CoberturasEnum.RESPONSABILIDAD_CIVIL, "Responsabilidad Civil hacia terceros (*)");
 		coberturas.put(CoberturasEnum.TOTAL_SIN_DANOS_PARCIALES, "Responsabilidad Civil hacia terceros (*)  y Daños Totales (Robo Total, Incendio Total y Destrucción Total)");
 		coberturas.put(CoberturasEnum.TERCEROS_COPMLETOS, "Responsabilidad Civil hacia terceros (*), Robo Total y Parcial, Incendio Total y Parcial y Destrucción Total por Accidente con cláusula del 20% (1). Vidrios laterales y cerraduras  por intento de robo evento por año");
 		coberturas.put(CoberturasEnum.TERCEROS_COMPLETOS_FULL, "Responsabilidad Civil hacia terceros (*), Robo Total y Parcial, Incendio Total y Parcial y Destrucción Total por Accidente con cláusula del 80% (2). Vidrios laterales y cerraduras  por intento de robo un evento por año. Granizo incluido sin cargo.");
 		coberturas.put(CoberturasEnum.TERCEROS_COMPLETOS_FULL_GRANIZO, "Responsabilidad Civil hacia terceros (*), Robo Total y Parcial, Incendio Total y Parcial y Destrucción Total por Accidente con cláusula del 80% (2). Vidrios laterales y cerraduras  por intento de robo un evento por año. Granizo incluido sin cargo.");
 		coberturas.put(CoberturasEnum.TODO_RIESGO, "Comprende Responsabilidad Civil hacia terceros (*), Robo Total y Parcial, Incendio  Total y Parcial, Daño Parcial (según Fcia.) y Destrucción Total por Accidente con cláusula del 80% (2).");
 		detallesCoberturas.put(BrokerEnum.LIBERTY, coberturas);
 		
 		coberturas = new HashMap<CoberturasEnum, String>();
 		coberturas.put(CoberturasEnum.RESPONSABILIDAD_CIVIL, "Responsabilidad Civil hacia terceros transportados y no transportados, con límite de suma asegurada.");
 		coberturas.put(CoberturasEnum.TOTAL_SIN_DANOS_PARCIALES, "Responsabilidad Civil hacia terceros transportados y no transportados, con límite de suma asegurada; Pérdida Total únicamente, por Accidente, Incendio y Robo o Hurto.");
 		coberturas.put(CoberturasEnum.TERCEROS_COPMLETOS, "Responsabilidad Civil hacia terceros transportados y no transportados, con límite de suma asegurada; Pérdida Total por Accidente y Pérdida Total y Parcial por Incendio y Robo o Hurto + Daños en Parabrisas y/o Luneta + Beneficios Adicionales con opcional de granizo e inundación según zona de riesgo. ");
 		coberturas.put(CoberturasEnum.TERCEROS_COMPLETOS_FULL, "Responsabilidad Civil hacia terceros transportados y no transportados, con límite de suma asegurada; Robo o Hurto Total y Parcial, Incendio Total y Parcial y Daño Total y/o Parciales por Accidente, con una franquicia fija por evento, según zona de riesgo. ");
 		coberturas.put(CoberturasEnum.TERCEROS_COMPLETOS_FULL_GRANIZO, "Responsabilidad Civil hacia terceros transportados y no transportados, con límite de suma asegurada; Robo o Hurto Total y Parcial, Incendio Total y Parcial y Daño Total y/o Parciales por Accidente, con una franquicia fija por evento, según zona de riesgo. Cobertura de granizo hasta $5.000. ");
 		coberturas.put(CoberturasEnum.TODO_RIESGO, "Todo riesgo sin franquicia.");
 		detallesCoberturas.put(BrokerEnum.ALLIANZ, coberturas);
 				
 	}
 }
