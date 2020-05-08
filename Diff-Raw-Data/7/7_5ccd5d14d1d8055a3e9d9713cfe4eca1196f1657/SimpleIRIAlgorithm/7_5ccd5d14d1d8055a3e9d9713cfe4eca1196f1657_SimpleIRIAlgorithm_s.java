 /*******************************************************************************
 AccumulationIRIAlgorithm.java
 Copyright (C) Nacho Uve
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *******************************************************************************/
 package es.udc.sextante.gridAnalysis.IRI;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
 import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
 import es.unex.sextante.core.AnalysisExtent;
 import es.unex.sextante.core.GeoAlgorithm;
 import es.unex.sextante.core.OutputObjectsSet;
 import es.unex.sextante.core.ParametersSet;
 import es.unex.sextante.core.Sextante;
 import es.unex.sextante.dataObjects.IFeature;
 import es.unex.sextante.dataObjects.IFeatureIterator;
 import es.unex.sextante.dataObjects.IRasterLayer;
 import es.unex.sextante.dataObjects.IRecord;
 import es.unex.sextante.dataObjects.IVectorLayer;
 import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
 import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
 import es.unex.sextante.exceptions.IteratorException;
 import es.unex.sextante.exceptions.NullParameterValueException;
 import es.unex.sextante.exceptions.OptionalParentParameterException;
 import es.unex.sextante.exceptions.RepeatedParameterNameException;
 import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
 import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
 import es.unex.sextante.exceptions.WrongParameterIDException;
 import es.unex.sextante.exceptions.WrongParameterTypeException;
 import es.unex.sextante.gui.core.SextanteGUI;
 import es.unex.sextante.gui.modeler.ModelAlgorithmIO;
 import es.unex.sextante.gui.settings.SextanteModelerSettings;
 import es.unex.sextante.outputs.FileOutputChannel;
 import es.unex.sextante.outputs.Output;
 import es.unex.sextante.parameters.Parameter;
 import es.unex.sextante.rasterize.rasterizeVectorLayer.RasterizeVectorLayerAlgorithm;
 import es.unex.sextante.vectorTools.autoincrementValue.AutoincrementValueAlgorithm;
 import es.unex.sextante.vectorTools.linesToEquispacedPoints.LinesToEquispacedPointsAlgorithm;
 
 
 /**
  * Estoy siguiendo las instruciones que hay en el DROPBOX de Concha To compile this algorithm it must be place on gridAnalysis
  * project of SEXTANTE.
  * 
  * En la zona de costa, la metodolog�a cambia un poco.
  * 
  * Observaciones: - Las unidades del SIG deben ser "METROS" - ACCFLOW debe contar celdas (autom�ticamente el algoritmo obtendr� la
  * cuenca en km2)
  * 
  * @author uve
  * 
  */
 public class SimpleIRIAlgorithm
 extends
 GeoAlgorithm {
 
     private static final int    MAX_IRI_DIST                = 5000;
 
     //OJO: ?Deberia tener solamente 1 punto?
     public static final String  VERTIDO                     = "VERTIDO";
     public static final String  DEM                         = "MDT";
     public static final String  ACCFLOW                     = "ACCFLOW";
     public static final String  HE_ATTRIB                   = "HE_ATTRIB";
 
     public static final String  RESULT                      = "RESULT";
 
     //Nombre de capas de FA
     private static final String captacions_existentes       = "captacions_existentes";
     private static final String captacions_propostas        = "captacions_propostas";
     private static final String espacios_protegidos         = "espacios_protegidos";
     private static final String zonas_piscicolas_protexidas = "zonas_piscicolas_protexidas";
     private static final String praias_marinas              = "praias_marinas";
     private static final String praias_fluviais             = "praias_fluviais";
     private static final String zonas_sensibles             = "zonas_sensibles";
     private static final String embalses                    = "embalses";
     private static final String bateas                      = "bateas";
     private static final String zonas_marisqueo             = "zonas_marisqueo";
     private static final String piscifactorias              = "piscifactorias";
 
     //// Variables
     //Habitantes equivalentes
     private Integer             HE_VALUE                    = -1;
 
     //Pesos de capas de FA
     private static final String captacions_exist_weight     = "captacions_exist_weight";
     private static final String captacions_propos_weight    = "captacions_propos_weight";
     private static final String espacios_proteg_weight      = "espaciosproteg_weight";
     private static final String zonas_piscico_weight        = "zonas_piscin_weight";
     private static final String praias_marinas_weight       = "prais_marinas_weight";
     private static final String praias_fluviais_weight      = "praias_fluviais_weight";
     private static final String zonas_sensibles_weight      = "zonas_sensibles_weight";
     private static final String embalses_weight             = "embalses_weight";
     private static final String bateas_weight               = "bateas_weight";
     private static final String zonas_marisqueo_weight      = "zonas_marisqueo_weight";
     private static final String piscifactorias_weight       = "piscifactorias_weight";
 
     //Capa de masas de agua
     private static final String WATER_BODIES                = "water_bodies";
     private static final String ECO_STATUS                  = "ecological_estatus";
 
     ///// Pesos de otras veriables
     private static final String WATERSHED_AREA_RADIO        = "watershed_area_radio";
 
     private static final String DBO_before                  = "DBO_before";
     private static final String MAX_DBO_after               = "MAX_DBO_after";
 
     private static final String HE_WEIGHT                   = "HE_WEIGHT";
     private static final String DIL_WEIGHT                  = "DIL_WEIGHT";
 
 
     //Habitantes equivalentes
     private int                 he_weight                   = 0;
 
     //Diluci�n
     private int                 dil_weight                  = 0;
 
     // OTHER PARAMETERS
     private static final String SAMPLE_DIST                 = "sample_dist";
     private static final String SEARCH_FACTOR_RADIO         = "search_factor_radio";
 
     private static final String perc_FACT                   = "perc_FACT";
     private static final String perc_DMA                    = "perc_DMA";
 
     // VARIABLES
     private int                 num_points;
     private int                 sample_dist;
     private int                 num_coastal_rings;
 
 
     IRasterLayer                demLyr;
 
     // Capa de puntos equiespaciados del rio (red de drenaje)
     IVectorLayer                network_lyr;
     // Capa de anillos (poligonos) del ultimo punto network_lyr cuando llega a la costa y no ha llegado a su longitud de estudio
     // Cada anillo es de la misma equidistancia
     IVectorLayer                networkRing_lyr;
 
     IVectorLayer                capt_existLyr;
     int                         capt_existWei;
     IVectorLayer                capt_propostLyr;
     int                         capt_propostWei;
     IVectorLayer                espacios_ProtLyr;
     int                         espacios_ProtWei;
     IVectorLayer                zpiscic_protLyr;
     int                         zpiscic_protWei;
     IVectorLayer                zsensiblesLyr;
     int                         zsensiblesWei;
     IVectorLayer                praias_marLyr;
     int                         praias_marWei;
     IVectorLayer                praias_fluLyr;
     int                         praias_fluWei;
     IVectorLayer                embalsesLyr;
     int                         embalsesWei;
     IVectorLayer                bateasLyr;
     int                         bateasWei;
     IVectorLayer                zmarisqueoLyr;
     int                         zmarisqueoWei;
     IVectorLayer                piscifactoriasLyr;
     int                         piscifactoriasWei;
 
     IVectorLayer                vertidoLyr;
     IVectorLayer                waterBodiesLyr;
     int                         ecoStatusAttribIdx;
 
     int                         fa_radio                    = 0;
     int                         perc_fact                   = 0;
     int                         perc_dma                    = 0;
 
     double                      WATERSHED_KM2               = 0;
 
     // VARIABLES DBO
     double                      cmez                        = 0.0;
     double                      crio                        = 0.0;
 
     //VARIABLES ECO WEIGHTS
     int                         ecoA_w                      = 0;
     int                         ecoB_w                      = 0;
     int                         ecoC_w                      = 0;
     int                         ecoD_w                      = 0;
     int                         ecoE_w                      = 0;
 
     //ARRAY IRI
     double[]                    iri_f1_array;
     double[]                    iri_f2_array;
     double[]                    iri_f3_array;
     double[]                    iri_f4_array;
     double[]                    iri_f5_array;
     double[]                    iri_f6_array;
     double[]                    iri_f7_array;
     double[]                    iri_f8_array;
     double[]                    iri_f9_array;
     double[]                    iri_f10_array;
     double[]                    iri_f11_array;
 
 
     @Override
     public void defineCharacteristics() {
 
 	setName("Simple IRI");
 	setGroup("AA_IRI Algorithms");
 	setUserCanDefineAnalysisExtent(true);
 
 	try {
 
 	    m_Parameters.addInputVectorLayer(VERTIDO, Sextante.getText("Vertidos"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
 
 	    try {
 		m_Parameters.addTableField(HE_ATTRIB, "Atributo Hab. Equiv.", VERTIDO);
 		//m_Parameters.addTableField(HE_ATTRIB, "Atributo Hab. Equiv.", VERTIDOS, true);
 	    }
 	    catch (final UndefinedParentParameterNameException e) {
 		e.printStackTrace();
 	    }
 	    catch (final OptionalParentParameterException e) {
 		e.printStackTrace();
 	    }
 
 	    // Pesos
 	    m_Parameters.addNumericalValue(HE_WEIGHT, "Peso HE", 25, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 	    m_Parameters.addNumericalValue(DIL_WEIGHT, "Peso DIL", 10, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    // Factores Ambientales
 	    m_Parameters.addInputVectorLayer(captacions_existentes, Sextante.getText("captacions_existentes"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(captacions_exist_weight, captacions_existentes, 10,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(captacions_propostas, Sextante.getText("captacions_propostas"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(captacions_propos_weight, captacions_propostas, 4,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(espacios_protegidos, Sextante.getText("Espacios_Protegidos"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(espacios_proteg_weight, espacios_protegidos, 15,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(zonas_piscicolas_protexidas, Sextante.getText("zonas_piscicolas_protexidas"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(zonas_piscico_weight, zonas_piscicolas_protexidas, 9,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(praias_marinas, Sextante.getText("Praias_marinas"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(praias_marinas_weight, praias_marinas, 4,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(praias_fluviais, Sextante.getText("Praias_fluviais"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(praias_fluviais_weight, praias_fluviais, 4,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(zonas_sensibles, Sextante.getText("Zonas_sensibles"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(zonas_sensibles_weight, zonas_sensibles, 7,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(embalses, Sextante.getText("Embalses_y_lagos"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(embalses_weight, embalses, 2, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(bateas, Sextante.getText("Bateas"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(bateas_weight, bateas, 4, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(zonas_marisqueo, Sextante.getText("Zonas_marisqueo"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(zonas_marisqueo_weight, zonas_marisqueo, 4,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    m_Parameters.addInputVectorLayer(piscifactorias, Sextante.getText("Piscifactorias"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    m_Parameters.addNumericalValue(piscifactorias_weight, piscifactorias, 2,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    // MASAS DE AGUA Y SU ESTADO ECOLOGICO
 	    m_Parameters.addInputVectorLayer(WATER_BODIES, Sextante.getText("Masas_de_agua"),
 		    AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
 	    try {
 		m_Parameters.addTableField(ECO_STATUS, "Estado ecologico", WATER_BODIES);
 	    }
 	    catch (final UndefinedParentParameterNameException e) {
 		e.printStackTrace();
 	    }
 	    catch (final OptionalParentParameterException e) {
 		e.printStackTrace();
 	    }
 
 	    // MDT y ACCFLOW
 	    m_Parameters.addInputRasterLayer(DEM, Sextante.getText("MDT"), true);
 	    m_Parameters.addInputRasterLayer(ACCFLOW, Sextante.getText("ACCFLOW"), true);
 
 	    // Parametros de la red de drenaje
 	    m_Parameters.addNumericalValue(SAMPLE_DIST, "Distancia entre puntos del rio", 100,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 	    m_Parameters.addNumericalValue(WATERSHED_AREA_RADIO, "Radio para buscar el max area de cuenca", 100,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 	    m_Parameters.addNumericalValue(SEARCH_FACTOR_RADIO, "Radio para buscar factores amb.", 100,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    //Par�metros para el c�lculo de la diluci�n
 	    //Tambi�n llamado "C_mez"
 	    m_Parameters.addNumericalValue(MAX_DBO_after,
 		    "Concentraci�n de DBO m�xima permitida del r�o despu�s del vertido, en ppm", 6.0,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
 	    //Tambi�n llamado "C_rio"
 	    m_Parameters.addNumericalValue(DBO_before, "Concentraci�n de DBO del r�o antes del vertido, en ppm", 3.0,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
 
 	    //Importancia de los dos tipos de IRI
 	    m_Parameters.addNumericalValue(perc_DMA, "Importancia relativa de IRI_dma", 40,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 	    m_Parameters.addNumericalValue(perc_FACT, "Importancia relativa de IRI_fact", 60,
 		    AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    //Pesos seg�n el estado ecol�gico
 	    m_Parameters.addNumericalValue("ecoW_E", "ecoW_E", 100, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 	    m_Parameters.addNumericalValue("ecoW_D", "ecoW_D", 64, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 	    m_Parameters.addNumericalValue("ecoW_C", "ecoW_C", 20, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 	    m_Parameters.addNumericalValue("ecoW_B", "ecoW_B", 1, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 	    m_Parameters.addNumericalValue("ecoW_A", "ecoW_A", 0, AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER);
 
 	    // Registro los Outputs
 	    addOutputRasterLayer("RESULT_rasterize_vert", "RESULT_rasterize_vert", 1);
 	    addOutputVectorLayer("RESULT_network", "RESULT_network", AdditionalInfoVectorLayer.SHAPE_TYPE_LINE);
 	    addOutputVectorLayer("RESULT_netPoints", "RESULT_netPoints", AdditionalInfoVectorLayer.SHAPE_TYPE_POINT);
 	    addOutputVectorLayer("RESULT_netPoints50", "RESULT_netPoints50", AdditionalInfoVectorLayer.SHAPE_TYPE_POINT);
 	    addOutputVectorLayer("RESULT_networkRing", "RESULT_networkRing", AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON);
 
 	    addOutputVectorLayer("IRI_network", "IRI_network", AdditionalInfoVectorLayer.SHAPE_TYPE_POINT);
 	    addOutputVectorLayer("IRI_sumarize", "IRI_sumarize", AdditionalInfoVectorLayer.SHAPE_TYPE_POINT);
 
 
 	}
 	catch (final RepeatedParameterNameException e) {
 	    Sextante.addErrorToLog(e);
 	}
 
     }
 
 
     private void initVariables() {
 
 	try {
 
 	    demLyr = m_Parameters.getParameterValueAsRasterLayer(this.DEM);
 
 	    vertidoLyr = m_Parameters.getParameterValueAsVectorLayer(VERTIDO);
 	    final int he_idx = m_Parameters.getParameterValueAsInt(HE_ATTRIB);
 
 	    final IFeatureIterator iter = vertidoLyr.iterator();
 	    // Only 1 iteration per vertido layer
 	    for (int i = 0; (i < 1) && iter.hasNext(); i++) {
 		IFeature n;
 		try {
 		    n = iter.next();
 		    final IRecord r = n.getRecord();
 		    System.out.println("****************" + he_idx);
 		    HE_VALUE = Integer.valueOf(r.getValue(he_idx).toString());
 		    System.out.println("**************** HE_VALUE: " + HE_VALUE);
 		}
 		catch (final IteratorException e) {
 		    e.printStackTrace();
 		}
 	    }
 
 	    capt_existLyr = m_Parameters.getParameterValueAsVectorLayer(captacions_existentes);
 	    //podemos usar fa1_w
 	    capt_existWei = m_Parameters.getParameterValueAsInt(captacions_exist_weight);
 
 	    capt_propostLyr = m_Parameters.getParameterValueAsVectorLayer(captacions_propostas);
 	    //podemos usar fa2_w
 	    capt_propostWei = m_Parameters.getParameterValueAsInt(captacions_propos_weight);
 
 	    espacios_ProtLyr = m_Parameters.getParameterValueAsVectorLayer(espacios_protegidos);
 	    //podemos usar fa3_w
 	    espacios_ProtWei = m_Parameters.getParameterValueAsInt(espacios_proteg_weight);
 
 	    zpiscic_protLyr = m_Parameters.getParameterValueAsVectorLayer(zonas_piscicolas_protexidas);
 	    //podemos usar fa4_w
 	    zpiscic_protWei = m_Parameters.getParameterValueAsInt(zonas_piscico_weight);
 
 	    praias_marLyr = m_Parameters.getParameterValueAsVectorLayer(praias_marinas);
 	    //podemos usar fa5_w
 	    praias_marWei = m_Parameters.getParameterValueAsInt(praias_marinas_weight);
 
 	    praias_fluLyr = m_Parameters.getParameterValueAsVectorLayer(praias_fluviais);
 	    //podemos usar fa6_w
 	    praias_fluWei = m_Parameters.getParameterValueAsInt(praias_fluviais_weight);
 
 	    zsensiblesLyr = m_Parameters.getParameterValueAsVectorLayer(zonas_sensibles);
 	    //podemos usar fa7_w
 	    zsensiblesWei = m_Parameters.getParameterValueAsInt(zonas_sensibles_weight);
 
 	    embalsesLyr = m_Parameters.getParameterValueAsVectorLayer(embalses);
 	    //podemos usar fa8_w
 	    embalsesWei = m_Parameters.getParameterValueAsInt(embalses_weight);
 
 	    bateasLyr = m_Parameters.getParameterValueAsVectorLayer(bateas);
 	    //podemos usar fa9_w
 	    bateasWei = m_Parameters.getParameterValueAsInt(bateas_weight);
 
 	    zmarisqueoLyr = m_Parameters.getParameterValueAsVectorLayer(zonas_marisqueo);
 	    //podemos usar fa10_w
 	    zmarisqueoWei = m_Parameters.getParameterValueAsInt(zonas_marisqueo_weight);
 
 	    piscifactoriasLyr = m_Parameters.getParameterValueAsVectorLayer(piscifactorias);
 	    //podemos usar fa11_w
 	    piscifactoriasWei = m_Parameters.getParameterValueAsInt(piscifactorias_weight);
 
 	    //DMA Layer
 	    waterBodiesLyr = m_Parameters.getParameterValueAsVectorLayer(WATER_BODIES);
 	    ecoStatusAttribIdx = m_Parameters.getParameterValueAsInt(ECO_STATUS);
 
 
 	    fa_radio = m_Parameters.getParameterValueAsInt(SEARCH_FACTOR_RADIO);
 	    perc_fact = m_Parameters.getParameterValueAsInt(perc_FACT);
 	    perc_dma = m_Parameters.getParameterValueAsInt(perc_DMA);
 	    he_weight = m_Parameters.getParameterValueAsInt(HE_WEIGHT);
 	    dil_weight = m_Parameters.getParameterValueAsInt(DIL_WEIGHT);
 
 	    sample_dist = m_Parameters.getParameterValueAsInt(SAMPLE_DIST);
 	    num_points = (MAX_IRI_DIST / sample_dist);
 
 	    ecoA_w = m_Parameters.getParameterValueAsInt("ecoW_A");
 	    ecoB_w = m_Parameters.getParameterValueAsInt("ecoW_B");
 	    ecoC_w = m_Parameters.getParameterValueAsInt("ecoW_C");
 	    ecoD_w = m_Parameters.getParameterValueAsInt("ecoW_D");
 	    ecoE_w = m_Parameters.getParameterValueAsInt("ecoW_E");
 
 	    cmez = m_Parameters.getParameterValueAsDouble(MAX_DBO_after);
 	    crio = m_Parameters.getParameterValueAsDouble(DBO_before);
 
 	}
 	catch (final WrongParameterTypeException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	catch (final WrongParameterIDException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	catch (final NullParameterValueException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 
     }
 
 
     @Override
     public boolean processAlgorithm() throws GeoAlgorithmExecutionException {
 	initVariables();
 	final int i = 0;
 
 	final int heAttr = m_Parameters.getParameterValueAsInt(HE_ATTRIB);
 	//vertidoLyr.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
 
 	//////////////////////
 	// RASTERIZE
 	final RasterizeVectorLayerAlgorithm alg = new RasterizeVectorLayerAlgorithm();
 	ParametersSet params = alg.getParameters();
 	params.getParameter(RasterizeVectorLayerAlgorithm.LAYER).setParameterValue(vertidoLyr);
 	params.getParameter(RasterizeVectorLayerAlgorithm.FIELD).setParameterValue(heAttr);
 
 	OutputObjectsSet oo = alg.getOutputObjects();
 	//final Output output = oo.getOutput(RasterizeVectorLayerAlgorithm.RESULT);
 
 	AnalysisExtent extent = new AnalysisExtent(demLyr);
 	extent.setCellSize(demLyr.getLayerCellSize());
 	extent.enlargeOneCell();
 
 	alg.setAnalysisExtent(extent);
 
 	System.out.println("-------------------------- RASTERIZE");
 
 	boolean bSucess = alg.execute(m_Task, m_OutputFactory);
 
 	IRasterLayer resultRasterize = null;
 
 	if (bSucess) {
 	    //output = oo.getOutput(RasterizeVectorLayerAlgorithm.RESULT);
 	    //m_OutputObjects.getOutput(RESULT).setOutputObject(output.getOutputObject());
 
 	    resultRasterize = (IRasterLayer) oo.getOutput(RasterizeVectorLayerAlgorithm.RESULT).getOutputObject();
 	    m_OutputObjects.getOutput("RESULT_rasterize_vert").setOutputObject(resultRasterize);
 	    //return true;
 	}
 	else {
 	    return false;
 	}
 
 	System.out.println(resultRasterize.getMaxValue());
 
 	//////////////////////
 	// CHANNEL NETWORK
 
 	//Load model
 	String modelsFolder = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
 	GeoAlgorithm geomodel = ModelAlgorithmIO.loadModelAsAlgorithm(modelsFolder + "/" +"iri_channel_step2.model");
 
 	geomodel.setAnalysisExtent(extent);
 
 	params = geomodel.getParameters();
 
 	for (int j=0; j < params.getNumberOfParameters(); j++) {
 	    Parameter p = params.getParameter(j);
 	    if (p.getParameterDescription().equalsIgnoreCase("Punto")){
 		params.getParameter(j).setParameterValue(vertidoLyr);
 	    } else if (p.getParameterDescription().equalsIgnoreCase("MDT")){
 		params.getParameter(j).setParameterValue(demLyr);
 	    } else if (p.getParameterDescription().equalsIgnoreCase("pto_rasterized")){
 		params.getParameter(j).setParameterValue(resultRasterize);
 	    } else if (p.getParameterDescription().equalsIgnoreCase("rios")){
 		params.getParameter(j).setParameterValue(waterBodiesLyr);
 	    }
 	}
 
 	//	demLyr = m_Parameters.getParameterValueAsRasterLayer(this.DEM);
 	//
 	//	final IRIChannelNetworkAlgorithm algCN = new IRIChannelNetworkAlgorithm();
 	//	params = algCN.getParameters();
 	//	params.getParameter(ChannelNetworkAlgorithm.DEM).setParameterValue(demLyr);
 	//	params.getParameter(ChannelNetworkAlgorithm.THRESHOLDLAYER).setParameterValue(resultRasterize);
 	//	params.getParameter(ChannelNetworkAlgorithm.THRESHOLD).setParameterValue(0);
 	//	params.getParameter(ChannelNetworkAlgorithm.METHOD).setParameterValue(0); //Usar ChannelNetworkAlgorithm.METHOD_GREATER_THAN
 
 	oo = geomodel.getOutputObjects();
 
 	extent = new AnalysisExtent(demLyr);
 	extent.setCellSize(demLyr.getLayerCellSize());
 	extent.enlargeOneCell();
 
 	geomodel.setAnalysisExtent(extent);
 	System.out.println("-------------------------- CHANNEL NETWORK");
 
 	bSucess = geomodel.execute(m_Task, m_OutputFactory);
 	IVectorLayer resultNetwork = null;
 
 	if (bSucess) {
 
 	    OutputObjectsSet outputs = geomodel.getOutputObjects();
 
 	    for (int j = 0; j < outputs.getOutputLayersCount(); j++) {
 		Output o = outputs.getOutput(j);
 		System.out.println(j + " output name: " + o.getDescription() + "  " + o.getName() + " " + o.getTypeDescription()) ;
 		if (o.getDescription().equalsIgnoreCase("IRI_river")){
 		    resultNetwork = (IVectorLayer) o.getOutputObject();
 		    resultNetwork.open();
 		    System.out.println("resultNetwork.feats: " +  resultNetwork.getShapesCount());
 		    m_OutputObjects.getOutput("RESULT_network").setOutputObject(resultNetwork);
 		    resultNetwork.close();
 		}
 	    }
 	} else {
 	    System.out.println("NOT SUCCESS THE GEOMODEL.EXECUTE!!!!!!!!!!!");
 	}
 
 
 	//To avoid more than one network
 	resultNetwork.addFilter(new FirstFeaturesVectorFilter(1));
 
 	resultNetwork.open();
 	System.out.println("");
 	System.out.println("Network lines: " + resultNetwork.getShapesCount());
 	System.out.println("Network line LENGHT: " + getFirstFeature(resultNetwork).getGeometry().getLength());
 	System.out.println("");
 	resultNetwork.close();
 
 	//////////////////////
 	// NETWORK TO POINTS
 	final LinesToEquispacedPointsAlgorithm algLines = new LinesToEquispacedPointsAlgorithm();
 	params = algLines.getParameters();
 	params.getParameter(algLines.LINES).setParameterValue(resultNetwork);
 	params.getParameter(algLines.DISTANCE).setParameterValue(sample_dist);
 
 	oo = algLines.getOutputObjects();
 
 	extent = new AnalysisExtent(resultNetwork);
 	extent.setCellSize(demLyr.getLayerCellSize());
 	extent.enlargeOneCell();
 
 	algLines.setAnalysisExtent(extent);
 
 	System.out.println("-------------------------- NETWORK TO POINTS");
 	System.out.println("Feat count (filter): " + resultNetwork.getShapesCount());
 	bSucess = algLines.execute(m_Task, m_OutputFactory);
 
 	System.out.println("-----------------------");
 	IVectorLayer resultNet_Points = null;
 
 	if (bSucess) {
 
 	    resultNet_Points = (IVectorLayer) oo.getOutput(algLines.RESULT).getOutputObject();
 
 	    // NOT OUT... before autoincrement!!
 	    m_OutputObjects.getOutput("RESULT_netPoints").setOutputObject(resultNet_Points);
 
 	}
 	else {
 	    return false;
 	}
 
 	//////////////////////
 	// AUTOINCREMENT and GET ONLY ???50??? points
 
 	final AutoincrementValueAlgorithm algAuto = new AutoincrementValueAlgorithm();
 
 	params = algAuto.getParameters();
 	params.getParameter(algAuto.LAYER).setParameterValue(resultNet_Points);
 	params.getParameter(algAuto.FIELD).setParameterValue(0); // I know it is "ID" attrib.
 
 	//params.getParameter(algLines.RESULT).setParameterValue(resultRasterize);
 
 	oo = algAuto.getOutputObjects();
 
 	extent = new AnalysisExtent(resultNetwork);
 	extent.setCellSize(demLyr.getLayerCellSize());
 	extent.enlargeOneCell();
 
 	algAuto.setAnalysisExtent(extent);
 
 	System.out.println("-------------------------- ONLY 50 NETWORK POINTS");
 
 	bSucess = algAuto.execute(m_Task, m_OutputFactory);
 
 	IVectorLayer resultNet_Points2 = null;
 
 	IVectorLayer vectLyr = null;
 
 	if (bSucess) {
 
 	    resultNet_Points2 = (IVectorLayer) oo.getOutput(algAuto.RESULT).getOutputObject();
 
 	    //TODO Here we would like to create a new layer calling...
 	    //resultNet_Points3 = getFirstFeatures(resultNet_Points2, num_points);
 
 	    resultNet_Points2.open();
 	    System.out.println("-------------------------resultNet_Points2.count(): " + resultNet_Points2.getShapesCount());
 	    resultNet_Points2.close();
 
 	    //resultNet_Points2.addFilter(new FirstFeaturesVectorFilter(num_points));
 
 	    // Set the layer used after
 	    network_lyr = getFirstFeatures(resultNet_Points2, num_points);
 
 	    network_lyr.open();
 	    System.out.println("-------------------------network_lyr.count(): " + network_lyr.getShapesCount());
 	    network_lyr.close();
 
 	}
 	else {
 	    return false;
 	}
 
 
 	//////////////////////////////////////////////////////////////////////////////////////////////////////////
 	// COASTAL
 
 	//////////////////////
 	// COASTAL RINGS
 
 	// See if the waste_water_spill affects a coastal sector
 	// Primero se calcula con anillos (cortados s�lo en la zonas de mar)
 	// Luego se crean puntos (siguiendo la direcci�n y sentido del r�o en tierra) para la representaci�n final
 	networkRing_lyr = null;
 	network_lyr.open();
 
 	int num_points_network = network_lyr.getShapesCount();
 	if (num_points_network > 0 && num_points_network < num_points ) {
 	    System.out.println("-------------------------- COASTAL RINGS ARE NECESSARY -------------------------- "
 		    +  num_points_network);
 
 	    // Create a layer with the last point of the network
 	    final IVectorLayer lastNetwork = network_lyr;
 	    lastNetwork.addFilter(new LastFeaturesVectorFilter( num_points_network, 1));
 
 	    num_coastal_rings = num_points - num_points_network;
 	    System.out.println("---------> num_rings: " + num_coastal_rings);
 
 	    final IFeature firstfeature = getFirstFeature(lastNetwork);
 	    Geometry geom = firstfeature.getGeometry().buffer(num_coastal_rings * sample_dist);
 	    final Envelope env = geom.getEnvelopeInternal();
 
 	    extent = new AnalysisExtent();
 	    final boolean recalculate_cellsize = true;
 	    extent.setXRange(env.getMinX(), env.getMaxX(), recalculate_cellsize);
 	    extent.setYRange(env.getMinY(), env.getMaxY(), recalculate_cellsize);
 	    extent.setCellSize(20.);
 
 	    //Load model
 	    modelsFolder = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
 	    geomodel = ModelAlgorithmIO.loadModelAsAlgorithm(modelsFolder + "/" +"bufferRing_clip_ocean-land.model");
 
 	    geomodel.setAnalysisExtent(extent);
 
 	    params = geomodel.getParameters();
 
 	    for (int j=0; j < params.getNumberOfParameters(); j++) {
 		Parameter p = params.getParameter(j);
 		if (p.getParameterDescription().equalsIgnoreCase("Point")){
 		    params.getParameter(j).setParameterValue(lastNetwork);
 		}
 		if (p.getParameterDescription().equalsIgnoreCase("Raster")){
 		    params.getParameter(j).setParameterValue(demLyr);
 		}
 		if (p.getParameterDescription().equalsIgnoreCase("Buffer_dist")){
 		    params.getParameter(j).setParameterValue(new Double(sample_dist));
 		}
 		if (p.getParameterDescription().equalsIgnoreCase("Rings_number")){
 		    params.getParameter(j).setParameterValue(new Double(num_coastal_rings));
 		}
 	    }
 
 	    bSucess = geomodel.execute(m_Task, m_OutputFactory);
 
 	    if (bSucess) {
 		OutputObjectsSet outputs = geomodel.getOutputObjects();
 		for (int j = 0; j < outputs.getOutputLayersCount(); j++) {
 		    Output o = outputs.getOutput(j);
 		    System.out.println(j + " output name: " + o.getDescription() + "  " + o.getName() + " " + o.getTypeDescription()) ;
 		    if (o.getDescription().equalsIgnoreCase("Intersection")){
 			networkRing_lyr = (IVectorLayer) o.getOutputObject();
 			networkRing_lyr.open();
 			System.out.println("networkRing_lyr.feats: " +  networkRing_lyr.getShapesCount());
 			m_OutputObjects.getOutput("RESULT_networkRing").setOutputObject(networkRing_lyr);
 			networkRing_lyr.close();
 		    }
 		}
 	    } else {
 		System.out.println("NOT SUCCESS THE GEOMODEL.EXECUTE!!!!!!!!!!!");
 	    }
 
 	}
 
 	network_lyr.close();
 	network_lyr.removeFilters();
 
 	//S� que el valor que me interesa del mar es el ultimo attribute
 	if (networkRing_lyr != null) {
 	    int last_attribute_idx = networkRing_lyr.getFieldCount()-1;
 
 	    networkRing_lyr.addFilter(new SimpleAttributeVectorFilter(last_attribute_idx, "=", 1));
 	    networkRing_lyr.open();
 	    System.out.println("networkRing_lyr.feats: " +  networkRing_lyr.getShapesCount());
 	    networkRing_lyr.close();
 
 	}
 	// END COASTAL
 	//////////////////////////////////////////////////////////////////////////////////////////////////////////
 
 
 	//////////////////////
 	// CALCULAR MAX WATERSHED
 
 	final int watershed_dist = m_Parameters.getParameterValueAsInt(WATERSHED_AREA_RADIO);
 	final IRasterLayer accflow = m_Parameters.getParameterValueAsRasterLayer(ACCFLOW);
 
 	final IFeature firstfeature = getFirstFeature(vertidoLyr);
 	Geometry geom = firstfeature.getGeometry().buffer(watershed_dist);
 	final Envelope env = geom.getEnvelopeInternal();
 
 	extent = new AnalysisExtent();
 	final boolean recalculate_cellsize = true;
 	extent.setXRange(env.getMinX(), env.getMaxX(), recalculate_cellsize);
 	extent.setYRange(env.getMinY(), env.getMaxY(), recalculate_cellsize);
 	extent.setCellSize(demLyr.getLayerCellSize());
 
 	//extent.enlargeOneCell();
 	accflow.open();
 	accflow.setWindowExtent(extent);
 
 	final double max_value = accflow.getMaxValue();
 	final double cell_size_km = (accflow.getLayerCellSize() / 1000);
 	WATERSHED_KM2 = max_value * cell_size_km * cell_size_km;
 	System.out.println("-----------> Max value accflow: " + max_value);
 	System.out.println("-----------> WATERSHED_km2: " + WATERSHED_KM2);
 	System.out.println(extent.toString());
 
 	accflow.close();
 
 	//////////////////////
 	// CALCULAR FA
 
 	System.out.println("-------------------------- CALCULE FA Distance");
 	calculateArrayIRI();
 
 	//////////////////////
 	// CALCULAR ECOLOGICAL STATUS
 	System.out.println("-------------------------- CALCULE ECOLOGICAL STATUS");
 	final Object[][] dma_status_iri_array = calculateIRI_DMA_array(waterBodiesLyr);
 
 
 	//////////////////////
 	// CALCULAR HE
 	System.out.println("-------------------------- CALCULE HE");
 	final double iri_he = (double) (he_weight * perc_fact * HE_VALUE) / 100000;
 	System.out.println("   P_HE: " + he_weight);
 	System.out.println("   perc_fact: " + perc_fact);
 	System.out.println("   he: " + HE_VALUE);
 	System.out.println("   IRI_HE: " + iri_he);
 
 	//////////////////////
 	// CALCULAR DILUCION
 	System.out.println("-------------------------- CALCULE DILUTION");
 
 	double iri_dil = 0.0;
 	// Se calcula solamente para el punto de vertido (distancia = 0)
 	final double concentracion_max = getMaxConcentration(0, cmez, crio, WATERSHED_KM2, HE_VALUE);
 	System.out.println("   concentracion_max: " + concentracion_max);
 
 	if (concentracion_max >= 300) {
 	    iri_dil = 0.0;
 	}
 	else {
 	    iri_dil = (dil_weight * perc_fact / 100) * (1 - (concentracion_max / 300));
 	}
 	System.out.println("   dil_weight: " + dil_weight);
 	System.out.println("   perc_fact: " + perc_fact);
 	System.out.println("   IRI_DILUCION: " + iri_dil);
 
 
 	//////////////////////
 	// PREPARE OUTPUTS
 	System.out.println("-------------------------- PREPARE OUTPUTS:  IRI Network Points");
 
 	final IVectorLayer result = getNewVectorLayer("IRI_network", Sextante.getText("IRI_network_result"),
 		IRINetworkChannelLayer.shapetype, IRINetworkChannelLayer.fieldTypes, IRINetworkChannelLayer.fieldNames);
 
 	network_lyr.open();
 	final IFeatureIterator iter = network_lyr.iterator();
 	int ii1 = 0;
 	/////////////////////////////////
 	// 1.- Puntos en tierra
 	for (; iter.hasNext(); ii1++) {
 	    geom = iter.next().getGeometry();
 	    final Object[] attributes = new Object[IRINetworkChannelLayer.fieldNames.length];
 	    attributes[0] = ii1 * sample_dist;
 	    attributes[1] = iri_f1_array[ii1];
 	    attributes[2] = iri_f2_array[ii1];
 	    attributes[3] = iri_f3_array[ii1];
 	    attributes[4] = iri_f4_array[ii1];
 	    attributes[5] = iri_f5_array[ii1];
 	    attributes[6] = iri_f6_array[ii1];
 	    attributes[7] = iri_f7_array[ii1];
 	    attributes[8] = iri_f8_array[ii1];
 	    attributes[9] = iri_f9_array[ii1];
 	    attributes[10] = iri_f10_array[ii1];
 	    attributes[11] = iri_f11_array[ii1];
 	    attributes[12] = String.valueOf(dma_status_iri_array[0][ii1]);
 	    attributes[13] = dma_status_iri_array[1][ii1];
 
 	    System.out.println(ii1 + " IRI_Net: " + geom.getCoordinate().x);
 
 	    result.addFeature(geom, attributes);
 	}
 	iter.close();
 	System.out.println("******************* Puntos en tierra: " + ii1);
 
 	/////////////////////////////////
 	// 2.- Puntos en el mar
 	Geometry[] geoms = getGeometriesOnSameDirection(network_lyr, num_coastal_rings, sample_dist);
 	System.out.println("******************* Puntos Teoricos en el mar: " + geoms.length);
 
 	for (int k = 0; k < geoms.length; ii1++, k++) {
 	    geom = geoms[k];
 	    final Object[] attributes = new Object[IRINetworkChannelLayer.fieldNames.length];
 	    attributes[0] = ii1 * sample_dist;
 	    attributes[1] = iri_f1_array[ii1];
 	    attributes[2] = iri_f2_array[ii1];
 	    attributes[3] = iri_f3_array[ii1];
 	    attributes[4] = iri_f4_array[ii1];
 	    attributes[5] = iri_f5_array[ii1];
 	    attributes[6] = iri_f6_array[ii1];
 	    attributes[7] = iri_f7_array[ii1];
 	    attributes[8] = iri_f8_array[ii1];
 	    attributes[9] = iri_f9_array[ii1];
 	    attributes[10] = iri_f10_array[ii1];
 	    attributes[11] = iri_f11_array[ii1];
 	    attributes[12] = String.valueOf(dma_status_iri_array[0][ii1]);
 	    attributes[13] = dma_status_iri_array[1][ii1];
 
 	    System.out.println(ii1 + ": IRI_rings: " + geom.getCoordinate().x);
 	    result.addFeature(geom, attributes);
 	}
 
 	network_lyr.close();
 
 	System.out.println("-------------------------- PREPARE OUTPUTS:  IRI Sumarize");
 
 	final IVectorLayer result2 = getNewVectorLayer("IRI_sumarize", Sextante.getText("IRI_sumarize_2010"),
 		IRISumarizeLayer.shapetype, IRISumarizeLayer.fieldTypes, IRISumarizeLayer.fieldNames);
 
 	final double[] attributes = new double[IRISumarizeLayer.fieldNames.length];
 
 	geom = firstfeature.getGeometry();
 	for (int i1 = 0; i1 < attributes.length; i1++) {
 	    attributes[i1] = 0.0;
 	}
 
 	for (int i1 = 0; i1 < iri_f1_array.length; i1++) {
 	    attributes[5] = attributes[5] + iri_f1_array[i1];
 	    attributes[6] = attributes[6] + iri_f2_array[i1];
 	    attributes[7] = attributes[7] + iri_f3_array[i1];
 	    attributes[8] = attributes[8] + iri_f4_array[i1];
 	    attributes[9] = attributes[9] + iri_f5_array[i1];
 	    attributes[10] = attributes[10] + iri_f6_array[i1];
 	    attributes[11] = attributes[11] + iri_f7_array[i1];
 	    attributes[12] = attributes[12] + iri_f8_array[i1];
 	    attributes[13] = attributes[13] + iri_f9_array[i1];
 	    attributes[14] = attributes[14] + iri_f10_array[i1];
 	    attributes[15] = attributes[15] + iri_f11_array[i1];
 	    //IRI_DMA
 	    attributes[1] = attributes[1] + (Double) dma_status_iri_array[1][i1];
 	}
 
 	//IRI_FACT
 	attributes[2] = sumArray(attributes, 3, 15);
 	//IRI_HE
 	attributes[3] = iri_he;
 	//IRI_DIL
 	attributes[4] = iri_dil;
 	//CUENCA
 	attributes[16] = WATERSHED_KM2;
 	//IRI
 	attributes[0] = attributes[1] + attributes[2];
 
 	final Object[] attr = new Object[attributes.length];
 	for (int i1 = 0; i1 < attributes.length; i1++) {
 	    attr[i1] = new Double(attributes[i1]);
 	}
 
 	result2.addFeature(geom, attr);
 
 	return !m_Task.isCanceled();
     }
 
 
     private void calculateArrayIRI() {
 	System.out.println("calculateIRI_FA_array(capt_existLyr, capt_existWei)");
 	iri_f1_array = calculateIRI_FA_array(capt_existLyr, capt_existWei);
 	System.out.println("calculateIRI_FA_array(capt_propostLyr, capt_propostWei);");
 	iri_f2_array = calculateIRI_FA_array(capt_propostLyr, capt_propostWei);
 	System.out.println("calculateIRI_FA_array(espacios_ProtLyr, espacios_ProtWei);");
 	iri_f3_array = calculateIRI_FA_array(espacios_ProtLyr, espacios_ProtWei);
 	System.out.println("calculateIRI_FA_array(zpiscic_protLyr, zpiscic_protWei);");
 	iri_f4_array = calculateIRI_FA_array(zpiscic_protLyr, zpiscic_protWei);
 	System.out.println("calculateIRI_FA_array(praias_marLyr, praias_marWei);");
 	iri_f5_array = calculateIRI_FA_array(praias_marLyr, praias_marWei);
 	System.out.println("calculateIRI_FA_array(praias_fluLyr, praias_fluWei);");
 	iri_f6_array = calculateIRI_FA_array(praias_fluLyr, praias_fluWei);
 	System.out.println("calculateIRI_FA_array(zsensiblesLyr, zsensiblesWei);");
 	iri_f7_array = calculateIRI_FA_array(zsensiblesLyr, zsensiblesWei);
 	System.out.println("calculateIRI_FA_array(embalsesLyr, embalsesWei);");
 	iri_f8_array = calculateIRI_FA_array(embalsesLyr, embalsesWei);
 	System.out.println("calculateIRI_FA_array(bateasLyr, bateasWei);");
 	iri_f9_array = calculateIRI_FA_array(bateasLyr, bateasWei);
 	System.out.println("calculateIRI_FA_array(zmarisqueoLyr, zmarisqueoWei);");
 	iri_f10_array = calculateIRI_FA_array(zmarisqueoLyr, zmarisqueoWei);
 	System.out.println("calculateIRI_FA_array(piscifactoriasLyr, piscifactoriasWei);");
 	iri_f11_array = calculateIRI_FA_array(piscifactoriasLyr, piscifactoriasWei);
     }
 
 
     private double getMaxConcentration(final int dist,
 	    final double conc_mez,
 	    final double conc_rio,
 	    final double watershed_km2,
 	    final int he) {
 
 	final double _7Q10 = (3.1 * Math.pow(watershed_km2, 0.8736));
 	final double cmax = conc_mez + (432 * ((_7Q10 * (conc_mez - conc_rio)) / (he * Math.exp(-0.0009 * dist))));
 	return cmax;
     }
 
 
     private double[] calculateIRI_FA_array(final IVectorLayer factLyr,
 	    final int fa_weight) {
 
 	final boolean[] pt_has_fa_array = new boolean[num_points];
 	final double[] iri_fa_array = new double[num_points];
 
 	final AnalysisExtent extent = new AnalysisExtent(network_lyr);
	factLyr.addFilter(new BoundingBoxFilter(extent));
 
 	try {
 	    boolean coastal_rings = (networkRing_lyr != null);
 	    int num_rings = 0;
 
 	    if (coastal_rings){
 		networkRing_lyr.open();
 		num_rings = networkRing_lyr.getShapesCount();
 		networkRing_lyr.close();
 	    }
 
 	    final IFeatureIterator iter1 = factLyr.iterator();
 	    for (int j = 0; iter1.hasNext(); j++) {
 		final IFeature feat1 = iter1.next();
 		final Geometry geom1 = feat1.getGeometry();
 		System.out.println("Feat FA: " + j);
 
 		network_lyr.open();
 		final IFeatureIterator iter2 = network_lyr.iterator();
 
 		IFeatureIterator iter3 = null; // Coastal rings iterator
 		if (coastal_rings){
 		    iter3 = networkRing_lyr.iterator();
 		}
 
 		double distance = Double.MAX_VALUE;
 		double min_dist = Double.MAX_VALUE;
 		int min_idx = -1;
 		if (factLyr.getShapeType() == IVectorLayer.SHAPE_TYPE_POINT) {
 		    // When it is a point layer gets the closest networkchannel point or coastal ring
 		    int k = 0;
 		    for (; iter2.hasNext(); k++) {
 			final IFeature feat2 = iter2.next();
 			final Geometry geom2 = feat2.getGeometry();
 			distance = geom1.distance(geom2);
 			if ((distance <= fa_radio) && (distance < min_dist)) {
 			    System.out.println("Pto red: " + k + " Distance: " + distance);
 			    min_dist = distance;
 			    min_idx = k;
 			}
 		    }
 
 		    // Now coastal rings, if exists
 		    if (k <= num_points && coastal_rings) {
 			for (; iter3.hasNext(); k++){
 			    final IFeature feat3 = iter3.next();
 			    final Geometry geom3 = feat3.getGeometry();
 			    distance = geom1.distance(geom3);
 			    if ((distance <= fa_radio) && (distance < min_dist)) {
 				System.out.println("RING red: " + k + " Distance: " + distance);
 				min_dist = distance;
 				min_idx = k;
 			    }
 			}
 		    }
 
 		}
 		else {
 		    // When it is a polygon/linestring layer gets all networkchannel points on the buffer
 		    int k = 0;
 		    for (; iter2.hasNext(); k++) {
 			final IFeature feat2 = iter2.next();
 			final Geometry geom2 = feat2.getGeometry();
 			distance = geom1.distance(geom2);
 			if ((distance <= fa_radio)) {
 			    pt_has_fa_array[k] = true;
 			    System.out.println("Pto red: " + k + " Distance: " + distance);
 			}
 		    }
 
 		    // Now coastal rings, if exists
 		    if (k < num_points && coastal_rings) {
 			for (; k < num_points && iter3.hasNext(); k++){
 			    final IFeature feat3 = iter3.next();
 			    if (feat3 == null) {
 				continue;
 			    }
 			    final Geometry geom3 = feat3.getGeometry();
 			    distance = geom1.distance(geom3);
 			    if ((distance <= fa_radio)) {
 				pt_has_fa_array[k] = true;
 				System.out.println("RING red: " + k + " Distance: " + distance);
 			    }
 			}
 		    }
 		}
 		network_lyr.close();
 		if (factLyr.getShapeType() == IVectorLayer.SHAPE_TYPE_POINT) {
 		    if (min_idx > -1) {
 			pt_has_fa_array[min_idx] = true;
 		    }
 		    else {
 			System.out.println("FACTOR not found");
 		    }
 		}
 		iter1.close();
 		iter2.close();
 		if (iter3 != null) {
 		    iter3.close();
 		}
 	    }
 
 	    for (int h = 0; h < num_points; h++) {
 		if (pt_has_fa_array[h]) {
 		    if (factLyr.getShapeType() == IVectorLayer.SHAPE_TYPE_POINT) {
 			iri_fa_array[h] = calculateIRI_point(h * sample_dist, sample_dist, fa_weight, perc_fact);
 		    }
 		    else if (factLyr.getShapeType() == IVectorLayer.SHAPE_TYPE_POLYGON) {
 			iri_fa_array[h] = calculateIRI_polygon(h * sample_dist, sample_dist, fa_weight, perc_fact);
 		    }
 		    else if (factLyr.getShapeType() == IVectorLayer.SHAPE_TYPE_LINE) {
 			iri_fa_array[h] = calculateIRI_line(h * sample_dist, sample_dist, fa_weight, perc_fact);
 		    }
 		    else {
 			System.out.println(" EEEEEEEEEEEEEEEERRRRRRRRRRRRRRRRRRRRROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORRRRRRRRRRRRRRRRRR ");
 		    }
 		    System.out.println(h + " IRI: " + iri_fa_array[h]);
 		}
 		else {
 		    iri_fa_array[h] = 0.0;
 		}
 	    }
 	}
 	catch (final IteratorException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	return iri_fa_array;
     }
 
 
     //      // [0][] => status
     //      // [1][] => iri
     //      Object[][] dma_status_iri_array = new Object[2][num_points];
     private Object[][] calculateIRI_DMA_array(final IVectorLayer dmaLyr) {
 
 	final char[] status_dma_array = new char[num_points];
 	final double[] iri_dma_array = new double[num_points];
 
 	for (int i = 0; i < num_points; i++) {
 	    status_dma_array[i] = 'A';
 	}
 
 	final AnalysisExtent extent = new AnalysisExtent(network_lyr);
 	dmaLyr.addFilter(new BoundingBoxFilter(extent));
 	try {
 	    final IFeatureIterator iter1 = dmaLyr.iterator();
 	    for (int j = 0; iter1.hasNext(); j++) {
 		final IFeature feat1 = iter1.next();
 		final Geometry geom1 = feat1.getGeometry();
 		final String status = ((String) feat1.getRecord().getValue(ecoStatusAttribIdx)).toUpperCase().trim();
 		char status_char = 'E';
 		if (status.length() > 0) {
 		    status_char = status.charAt(0);
 		}
 		System.out.println("Feat DMA: " + j + "   ECO: " + status_char);
 
 		network_lyr.open();
 		final IFeatureIterator iter2 = network_lyr.iterator();
 		double distance = Double.MAX_VALUE;
 		final double min_dist = Double.MAX_VALUE;
 		final int min_idx = -1;
 		// When it is a polygon/linestring layer gets all networkchannel points on the buffer
 		// Gets the worst ecological status
 		for (int k = 0; iter2.hasNext(); k++) {
 		    final IFeature feat2 = iter2.next();
 		    final Geometry geom2 = feat2.getGeometry();
 		    distance = geom1.distance(geom2);
 		    if ((distance <= fa_radio) && (status_char > status_dma_array[k])) {
 			status_dma_array[k] = status_char;
 			System.out.println("Pto red: " + k + " Distance: " + distance + "  ***ECO_STATUS***: " + status);
 		    }
 		}
 		network_lyr.close();
 		iter1.close();
 		iter2.close();
 	    }
 
 
 	    for (int h = 0; h < num_points; h++) {
 		if (status_dma_array[h] == 'A') {
 		    iri_dma_array[h] = calculateIRI_polygon(h * sample_dist, sample_dist, ecoA_w, perc_dma);
 		}
 		else if (status_dma_array[h] == 'B') {
 		    iri_dma_array[h] = calculateIRI_polygon(h * sample_dist, sample_dist, ecoB_w, perc_dma);
 		}
 		else if (status_dma_array[h] == 'C') {
 		    iri_dma_array[h] = calculateIRI_polygon(h * sample_dist, sample_dist, ecoC_w, perc_dma);
 		}
 		else if (status_dma_array[h] == 'D') {
 		    iri_dma_array[h] = calculateIRI_polygon(h * sample_dist, sample_dist, ecoD_w, perc_dma);
 		}
 		else if (status_dma_array[h] == 'E') {
 		    iri_dma_array[h] = calculateIRI_polygon(h * sample_dist, sample_dist, ecoE_w, perc_dma);
 		}
 		System.out.println(h + " IRI_DMA: " + iri_dma_array[h] + "  (" + status_dma_array[h] + ")");
 	    }
 	}
 	catch (final IteratorException e) {
 	    e.printStackTrace();
 	}
 
 	final Object[][] status_iri_array = new Object[2][num_points];
 	for (int i = 0; i < num_points; i++) {
 	    status_iri_array[0][i] = status_dma_array[i];
 	    status_iri_array[1][i] = iri_dma_array[i];
 	}
 	return status_iri_array;
     }
 
 
     private IVectorLayer getFirstFeatures(final IVectorLayer vectLyr,
 	    final int num_points) {
 
 	IVectorLayer aux = null;
 	try {
 	    aux = m_OutputFactory.getNewVectorLayer("feats_50", IVectorLayer.SHAPE_TYPE_POINT, vectLyr.getFieldTypes(),
 		    vectLyr.getFieldNames(), new FileOutputChannel(m_OutputFactory.getTempVectorLayerFilename()), vectLyr.getCRS());
 	}
 	catch (final UnsupportedOutputChannelException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	    return null;
 	}
 
 	vectLyr.addFilter(new FirstFeaturesVectorFilter(num_points));
 	vectLyr.open();
 
 	try {
 	    final IFeatureIterator iter = vectLyr.iterator();
 	    for (int i = 0; iter.hasNext(); i++) {
 		aux.addFeature(iter.next());
 	    }
 	}
 	catch (final IteratorException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 
 	try {
 	    aux.postProcess();
 	}
 	catch (final Exception e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 
 
 	aux.open();
 	System.out.println("-.---- getFirstFeatures.aux: " + aux.getShapesCount());
 	System.out.println("-.---- getFirstFeatures.vect: " + vectLyr.getShapesCount());
 	aux.close();
 	vectLyr.close();
 
 	return aux;
 
     }
 
 
     private IFeature getFirstFeature(final IVectorLayer layer) {
 	IFeature feat = null;
 	final IFeatureIterator iter = layer.iterator();
 	for (; iter.hasNext();) {
 	    try {
 		feat = iter.next();
 		break;
 	    }
 	    catch (final IteratorException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	    }
 	}
 	return feat;
     }
 
 
     private IFeature getLastFeature(final IVectorLayer layer) {
 	IFeature feat = null;
 	layer.open();
 	final IFeatureIterator iter = layer.iterator();
 	for (; iter.hasNext();) {
 	    try {
 		feat = iter.next();
 	    }
 	    catch (final IteratorException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	    }
 	}
 	layer.close();
 	return feat;
     }
 
 
     //xp = distancia al vertido
     //dist_points = distancia entre puntos
     //weight
     //percFact
     private double calculateIRI_point(final int xp,
 	    final int dist_points,
 	    final int weight,
 	    final int percent_fact) {
 	final double value = ((double) (percent_fact * weight) / 100) * Math.exp(-0.0009 * xp);
 	return value;
     }
 
 
     private double calculateIRI_line(final int xp,
 	    final int dist_points,
 	    final int weight,
 	    final int percent_fact) {
 
 	return calculateIRI_polygon(xp, dist_points, weight, percent_fact);
     }
 
 
     private double calculateIRI_polygon(final int xp,
 	    final int dist_points,
 	    final int weight,
 	    final int percent_fact) {
 
 	double value = 0.0;
 	final double constant = -1.011233793;
 	final double dist_2 = ((double) dist_points / 2);
 	final double mul_fact1 = ((double) (percent_fact * weight) / 100);
 	if (xp == 0) {
 	    final double exp = Math.exp(-0.0009 * (xp + dist_2));
 	    value = mul_fact1 * (constant * (exp - 1));
 	}
 	else if (xp == 5000) {
 	    final double exp1 = Math.exp(-0.0009 * 5000);
 	    final double exp2 = Math.exp(-0.0009 * (xp - dist_2));
 	    value = mul_fact1 * (constant * (exp1 - exp2));
 	}
 	else {
 	    final double exp1 = Math.exp(-0.0009 * (xp + dist_2));
 	    final double exp2 = Math.exp(-0.0009 * (xp - dist_2));
 	    value = mul_fact1 * (constant * (exp1 - exp2));
 	}
 	return value;
     }
 
 
     private double sumArray(final double[] array,
 	    final int ini_idx,
 	    final int end_idx) {
 	double value = 0;
 	for (int j = ini_idx; j <= end_idx; j++) {
 	    value = value + array[j];
 	}
 	return value;
     }
 
 
     private Geometry[] getGeometriesOnSameDirection(IVectorLayer vectLyr, int num_geoms, int dist) {
 	Geometry[] geoms = new Geometry[num_geoms];
 
 	vertidoLyr.open();
 	vectLyr.addFilter(new LastFeaturesVectorFilter(vectLyr.getShapesCount(), 2));
 
 	Coordinate p1 = null, p2 = null;
 	IFeatureIterator iter = vectLyr.iterator();
 	try {
 	    if (iter.hasNext()){
 		p1 = iter.next().getGeometry().getCoordinate();
 	    }
 	    if (iter.hasNext()){
 		p2 = iter.next().getGeometry().getCoordinate();
 	    }
 	} catch (IteratorException e) {
 	    e.printStackTrace();
 	}
 	vectLyr.removeFilters();
 	vertidoLyr.close();
 
 	double x = 0;
 	double y = 0;
 	double dx = 0;
 	double dy = 0;
 	if (p2 == null){
 	    //Go to north
 	    dx = 0;
 	    dy = dist;
 	    if (p1 != null){
 		x = p1.x;
 		y = p1.y;
 	    } else {
 		return null;
 	    }
 	} else {
 	    x = p2.x - p1.x;
 	    y = p2.y - p1.y;
 	    double module = Math.sqrt(x*x+y*y);
 	    dx = (dist * x)/module ;
 	    dy = (dist * y)/module;
 	    x = p2.x;
 	    y = p2.y;
 	}
 	GeometryFactory gf = new GeometryFactory();
 	for (int i = 0; i < num_geoms; i++){
 	    x = x + dx;
 	    y = y + dy;
 	    geoms[i] = gf.createPoint(new Coordinate(x, y));
 	}
 
 	return geoms;
     }
 
 }
