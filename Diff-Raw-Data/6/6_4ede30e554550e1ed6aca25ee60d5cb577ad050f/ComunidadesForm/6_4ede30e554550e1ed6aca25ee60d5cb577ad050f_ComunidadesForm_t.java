 package es.udc.cartolab.gvsig.fonsagua.forms.comunidades;
 
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.icarto.gvsig.navtableforms.BasicAbstractForm;
 import es.icarto.gvsig.navtableforms.gui.tables.TableHandler;
 import es.icarto.gvsig.navtableforms.gui.tables.VectorialTableHandler;
 import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
 import es.udc.cartolab.gvsig.fonsagua.FonsaguaConstants;
 import es.udc.cartolab.gvsig.fonsagua.croquis.listeners.AddCroquisListener;
 import es.udc.cartolab.gvsig.fonsagua.croquis.listeners.ShowCroquisListener;
 import es.udc.cartolab.gvsig.fonsagua.croquis.ui.CroquisButtons;
 import es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento.AbastecimientosForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.factories.FonsaguaTableFormFactory;
 import es.udc.cartolab.gvsig.fonsagua.forms.relationship.TableRelationship;
 import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
 
 @SuppressWarnings("serial")
 public class ComunidadesForm extends BasicAbstractForm {
 
     public static final String NAME = "comunidades";
     public static final String PKFIELD = "cod_comunidad";
 
     private TableHandler adescosHandler;
     private TableHandler entrevistadoresHandler;
     private TableHandler entrevistadosHandler;
     private TableHandler subcuencasHandler;
     private TableHandler cargosPublicosHandler;
     private TableHandler ongsHandler;
     private TableHandler otrasOrganizacionesHandler;
     private TableHandler tiposCultivosHandler;
     private TableHandler produccionConsumoHandler;
     private TableHandler ganaderiaHandler;
     private TableHandler cooperativasHandler;
     private TableHandler capacitacionesRiesgosHandler;
     private TableHandler implicacionComunidadHandler;
     private TableHandler valoracionSistemaHandler;
     private TableHandler datosConsumoHandler;
     private TableHandler habitosConsumoHandler;
     private VectorialTableHandler fuentesContaminacionHandler;
     private VectorialTableHandler puntosViviendasHandler;
     private VectorialTableHandler centrosEducativosHandler;
     private VectorialTableHandler centrosSaludHandler;
     private VectorialTableHandler areasPotencialesRiegoHandler;
     private VectorialTableHandler otrosServiciosHandler;
     private VectorialTableHandler amenazasHandler;
 
     private TableRelationship abastecimientosRelationship;
 
     private CroquisButtons croquisButtons;
 
     public ComunidadesForm(FLyrVect layer) {
 	super(layer);
 	viewInfo.setTitle("Comunidades");
 	TOCLayerManager toc = new TOCLayerManager();
 	adescosHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		AdescosForm.NAME, getWidgetComponents(), PKFIELD,
 		AdescosForm.colNames, AdescosForm.colAlias);
 	entrevistadoresHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		EntrevistadoresForm.NAME, getWidgetComponents(), PKFIELD,
 		EntrevistadoresForm.colNames, EntrevistadoresForm.colAlias);
 	entrevistadosHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		EntrevistadosForm.NAME, getWidgetComponents(), PKFIELD,
 		EntrevistadosForm.colNames, EntrevistadosForm.colAlias);
 	subcuencasHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		SubcuencasForm.NAME, getWidgetComponents(), PKFIELD,
 		SubcuencasForm.colNames, SubcuencasForm.colAlias);
 	cargosPublicosHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		CargosPublicosForm.NAME, getWidgetComponents(), PKFIELD,
 		CargosPublicosForm.colNames, CargosPublicosForm.colAlias);
 	ongsHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		OngsForm.NAME, getWidgetComponents(), PKFIELD,
 		OngsForm.colNames, OngsForm.colAlias);
 	otrasOrganizacionesHandler = new TableHandler(
 		FonsaguaConstants.dataSchema, OtrasOrganizacionesForm.NAME,
 		getWidgetComponents(), PKFIELD,
 		OtrasOrganizacionesForm.colNames,
 		OtrasOrganizacionesForm.colAlias);
 	tiposCultivosHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		TiposCultivosForm.NAME, getWidgetComponents(), PKFIELD,
 		TiposCultivosForm.colNames, TiposCultivosForm.colAlias);
 	produccionConsumoHandler = new TableHandler(
 		FonsaguaConstants.dataSchema, ProduccionConsumoForm.NAME,
 		getWidgetComponents(), PKFIELD, ProduccionConsumoForm.colNames,
 		ProduccionConsumoForm.colAlias);
 	ganaderiaHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		GanaderiaForm.NAME, getWidgetComponents(), PKFIELD,
 		GanaderiaForm.colNames, GanaderiaForm.colAlias);
 	cooperativasHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		CooperativasForm.NAME, getWidgetComponents(), PKFIELD,
 		CooperativasForm.colNames, CooperativasForm.colAlias);
 	capacitacionesRiesgosHandler = new TableHandler(
 		FonsaguaConstants.dataSchema, CapacitacionesRiesgosForm.NAME,
 		getWidgetComponents(), PKFIELD,
 		CapacitacionesRiesgosForm.colNames,
 		CapacitacionesRiesgosForm.colAlias);
 	implicacionComunidadHandler = new TableHandler(
 		FonsaguaConstants.dataSchema, ImplicacionComunidadForm.NAME,
 		getWidgetComponents(), PKFIELD,
 		ImplicacionComunidadForm.colNames,
 		ImplicacionComunidadForm.colAlias);
 	valoracionSistemaHandler = new TableHandler(
 		FonsaguaConstants.dataSchema, ValoracionSistemaForm.NAME,
 		getWidgetComponents(), PKFIELD, ValoracionSistemaForm.colNames,
 		ValoracionSistemaForm.colAlias);
 	datosConsumoHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		DatosConsumoForm.NAME, getWidgetComponents(), PKFIELD,
 		DatosConsumoForm.colNames, DatosConsumoForm.colAlias);
 	habitosConsumoHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		HabitosConsumoForm.NAME, getWidgetComponents(), PKFIELD,
 		HabitosConsumoForm.colNames, HabitosConsumoForm.colAlias);
 	fuentesContaminacionHandler = new VectorialTableHandler(
 		toc.getLayerByName(FuentesContaminacionForm.NAME),
 		getWidgetComponents(), PKFIELD,
 		FuentesContaminacionForm.colNames,
 		FuentesContaminacionForm.colAlias);
 	puntosViviendasHandler = new VectorialTableHandler(
 		toc.getLayerByName(PuntosViviendasForm.NAME),
 		getWidgetComponents(), PKFIELD, PuntosViviendasForm.colNames,
 		PuntosViviendasForm.colAlias);
 	centrosEducativosHandler = new VectorialTableHandler(
 		toc.getLayerByName(CentrosEducativosForm.NAME),
 		getWidgetComponents(), PKFIELD, CentrosEducativosForm.colNames,
 		CentrosEducativosForm.colAlias);
 	centrosSaludHandler = new VectorialTableHandler(
 		toc.getLayerByName(CentrosSaludForm.NAME),
 		getWidgetComponents(), PKFIELD, CentrosSaludForm.colNames,
 		CentrosSaludForm.colAlias);
 	areasPotencialesRiegoHandler = new VectorialTableHandler(
 		toc.getLayerByName(AreasPotencialesRiegoForm.NAME),
 		getWidgetComponents(), PKFIELD,
 		AreasPotencialesRiegoForm.colNames,
 		AreasPotencialesRiegoForm.colAlias);
 	otrosServiciosHandler = new VectorialTableHandler(
 		toc.getLayerByName(OtrosServiciosForm.NAME),
 		getWidgetComponents(), PKFIELD, OtrosServiciosForm.colNames,
 		OtrosServiciosForm.colAlias);
 	amenazasHandler = new VectorialTableHandler(
 		toc.getLayerByName(AmenazasForm.NAME), getWidgetComponents(),
 		PKFIELD, AmenazasForm.colNames, AmenazasForm.colAlias);
 	abastecimientosRelationship = new TableRelationship(
		getWidgetComponents(), NAME, PKFIELD, AbastecimientosForm.NAME,
		AbastecimientosForm.PKFIELD, "r_abastecimientos_comunidades",
		FonsaguaConstants.dataSchema);
 
     }
 
     private void addCroquisButtons() {
 	String comunidadId = ((JTextField) getFormBody().getComponentByName(
 		PKFIELD)).getText();
 	JPanel actionsToolBar = this.getActionsToolBar();
 	if (croquisButtons == null) {
 	    croquisButtons = new CroquisButtons(comunidadId);
 	    actionsToolBar.add(croquisButtons.getAddCroquisButton());
 	    actionsToolBar.add(croquisButtons.getShowCroquisButton());
 	} else {
 	    croquisButtons.setAddlistener(new AddCroquisListener(comunidadId));
 	    croquisButtons
 		    .setShowlistener(new ShowCroquisListener(comunidadId));
 	}
 
     }
 
     @Override
     protected void fillSpecificValues() {
 	addCroquisButtons();
 
 	String codComunidad = getFormController().getValue(PKFIELD);
 	adescosHandler.fillValues(codComunidad);
 	entrevistadoresHandler.fillValues(codComunidad);
 	entrevistadosHandler.fillValues(codComunidad);
 	subcuencasHandler.fillValues(codComunidad);
 	cargosPublicosHandler.fillValues(codComunidad);
 	ongsHandler.fillValues(codComunidad);
 	otrasOrganizacionesHandler.fillValues(codComunidad);
 	tiposCultivosHandler.fillValues(codComunidad);
 	produccionConsumoHandler.fillValues(codComunidad);
 	ganaderiaHandler.fillValues(codComunidad);
 	cooperativasHandler.fillValues(codComunidad);
 	capacitacionesRiesgosHandler.fillValues(codComunidad);
 	implicacionComunidadHandler.fillValues(codComunidad);
 	valoracionSistemaHandler.fillValues(codComunidad);
 	datosConsumoHandler.fillValues(codComunidad);
 	habitosConsumoHandler.fillValues(codComunidad);
 	fuentesContaminacionHandler.fillValues(codComunidad);
 	puntosViviendasHandler.fillValues(codComunidad);
 	centrosEducativosHandler.fillValues(codComunidad);
 	centrosSaludHandler.fillValues(codComunidad);
 	areasPotencialesRiegoHandler.fillValues(codComunidad);
 	otrosServiciosHandler.fillValues(codComunidad);
 	amenazasHandler.fillValues(codComunidad);
 
 	abastecimientosRelationship
 		.setPrimaryPKValue(((JTextField) getWidgetComponents().get(
 			abastecimientosRelationship.getPrimaryPKName()))
 			.getText());
     }
 
     @Override
     protected void setListeners() {
 	super.setListeners();
 	TOCLayerManager toc = new TOCLayerManager();
 	adescosHandler.reload(new AdescosForm());
 	entrevistadoresHandler.reload(new EntrevistadoresForm());
 	entrevistadosHandler.reload(new EntrevistadosForm());
 	subcuencasHandler.reload(new SubcuencasForm());
 	cargosPublicosHandler.reload(new CargosPublicosForm());
 	ongsHandler.reload(new OngsForm());
 	otrasOrganizacionesHandler.reload(new OtrasOrganizacionesForm());
 	tiposCultivosHandler.reload(new TiposCultivosForm());
 	produccionConsumoHandler.reload(new ProduccionConsumoForm());
 	ganaderiaHandler.reload(new GanaderiaForm());
 	cooperativasHandler.reload(new CooperativasForm());
 	capacitacionesRiesgosHandler.reload(new CapacitacionesRiesgosForm());
 	implicacionComunidadHandler.reload(new ImplicacionComunidadForm());
 	valoracionSistemaHandler.reload(new ValoracionSistemaForm());
 	datosConsumoHandler.reload(new DatosConsumoForm());
 	habitosConsumoHandler.reload(new HabitosConsumoForm());
 	fuentesContaminacionHandler.reload(FuentesContaminacionForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	puntosViviendasHandler.reload(PuntosViviendasForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	centrosEducativosHandler.reload(CentrosEducativosForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	centrosSaludHandler.reload(CentrosSaludForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	areasPotencialesRiegoHandler.reload(AreasPotencialesRiegoForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	otrosServiciosHandler.reload(OtrosServiciosForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	amenazasHandler.reload(AmenazasForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 
 	abastecimientosRelationship.reload();
     }
 
     @Override
     protected void removeListeners() {
 	super.removeListeners();
 	adescosHandler.removeListeners();
 	entrevistadoresHandler.removeListeners();
 	entrevistadosHandler.removeListeners();
 	subcuencasHandler.removeListeners();
 	cargosPublicosHandler.removeListeners();
 	ongsHandler.removeListeners();
 	otrasOrganizacionesHandler.removeListeners();
 	tiposCultivosHandler.removeListeners();
 	produccionConsumoHandler.removeListeners();
 	ganaderiaHandler.removeListeners();
 	cooperativasHandler.removeListeners();
 	capacitacionesRiesgosHandler.removeListeners();
 	implicacionComunidadHandler.removeListeners();
 	valoracionSistemaHandler.removeListeners();
 	datosConsumoHandler.removeListeners();
 	habitosConsumoHandler.removeListeners();
 	fuentesContaminacionHandler.removeListeners();
 	puntosViviendasHandler.removeListeners();
 	centrosEducativosHandler.removeListeners();
 	centrosSaludHandler.removeListeners();
 	areasPotencialesRiegoHandler.removeListeners();
 	otrosServiciosHandler.removeListeners();
 	amenazasHandler.removeListeners();
     }
 
     @Override
     public void onPositionChange(PositionEvent e) {
 	super.onPositionChange(e);
 	fillSpecificValues();
 	this.repaint(); // will force embedded tables to refresh
     }
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
 }
