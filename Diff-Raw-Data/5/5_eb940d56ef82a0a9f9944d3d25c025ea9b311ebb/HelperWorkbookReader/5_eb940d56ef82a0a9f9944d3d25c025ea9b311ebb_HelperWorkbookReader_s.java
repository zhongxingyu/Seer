 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.cimmyt.cril.ibwb.provider.dao.helpers;
 
 import com.sun.rowset.CachedRowSetImpl;
 import ibfb.domain.core.Measurement;
 import ibfb.domain.core.MeasurementData;
 import java.math.BigInteger;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.*;
 import javax.management.Query;
 import javax.sql.rowset.RowSetMetaDataImpl;
 import org.apache.log4j.Logger;
 import org.cimmyt.cril.ibwb.domain.*;
 import org.cimmyt.cril.ibwb.provider.dao.DMSReaderDAO;
 import org.cimmyt.cril.ibwb.provider.utils.DecimalUtils;
 import org.hibernate.Hibernate;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 
 /**
  *
  * @author gamaliel
  */
 public class HelperWorkbookReader {
     
     private static final Logger log = Logger.getLogger(HelperWorkbookReader.class);
     private static final int ltype = 1;
     private static final int fname = 0;
     private static final int represNo = 0;
     private static final int ounitid = 0;
     private static final int FNAME = 1;
     private static final int LVALUE = 2;
     private static final int LTYPE = 3;
 
     public static ResultSet getTrialRandomizationFast(
             final int studyId,
             final int trialFactorId,
             final List<String> factoresPrincipales,
             final List<String> factoresSalida,
             final String trialName,
             Session session,
             boolean isLocal,
             boolean isCentral
             ) throws SQLException {
         SQLQuery query;
         
         log.info("Getting trial randomization");
         Integer numeroDeFactoresPrincipales = factoresPrincipales.size();
         String listaDeFactoresResultado = DMSReaderDAO.getFactoresParaUsoInQuery(factoresSalida);
         ResultSet pr = null;
         
         String consultaSQL = "SELECT represno, COUNT(*) FROM effect e "
                 + "INNER JOIN factor f ON e.factorid=f.factorid "
                 + "WHERE studyid=" + studyId + " AND "
                 + "f.factorid = f.labelid AND "
                 + "fname IN(" + DMSReaderDAO.getFactoresParaUsoInQuery(factoresPrincipales) + ") "
                 + "GROUP BY represno HAVING COUNT(*)=" + numeroDeFactoresPrincipales;
         query = session.createSQLQuery(consultaSQL);
         List resultado = query.list();
         
         log.info("Definiendo orden de busquedas");
         String orden;
         if (isLocal) {
             orden = "DESC";
         } else if (isCentral) {
             orden = "ASC";
         } else {
             orden = "DESC";
         }
         
         int trepresNo = 0;
         if (resultado != null) {
             if (resultado.size() > 0) {
                 Object[] fila = (Object[]) resultado.get(0);
                 trepresNo = (Integer) fila[represNo];
             } else {
                 return null;
             }
         } else {
             return null;
         }
         
         RowSetMetaDataImpl rsmd = new RowSetMetaDataImpl();
         consultaSQL = "SELECT count(*) FROM factor "
                 + "WHERE studyid=" + studyId
                 + " and fname IN(" + listaDeFactoresResultado + ")";
         
         int cuantosFR = 0;
         
         query = session.createSQLQuery(consultaSQL);
         Object tempObject = query.uniqueResult();
         
         if (tempObject instanceof BigInteger) {
             BigInteger temp = (BigInteger) tempObject;
             cuantosFR = temp.intValue();
         } else if (tempObject instanceof Integer) {
             Integer temp = (Integer) tempObject;
             cuantosFR = temp.intValue();
         }
         
         consultaSQL = "SELECT fname, ltype, labelid FROM factor "
                 + "WHERE studyid=" + studyId
                 + " and fname IN(" + listaDeFactoresResultado + ")"
                 + " ORDER BY labelid " + orden;
         
         query = session.createSQLQuery(consultaSQL);
         resultado = query.list();
         
         rsmd.setColumnCount(cuantosFR);
         int tconsecutivo = 0;
         for (Object fila : resultado) {
             tconsecutivo += 1;
             Object[] casilla = (Object[]) fila;
             rsmd.setColumnName(tconsecutivo, (String) casilla[fname]);
             String ltypeTemp = casilla[ltype].toString();
             if (ltypeTemp.equals("N")) {
                 rsmd.setColumnType(tconsecutivo, Types.INTEGER);
             } else {
                 rsmd.setColumnType(tconsecutivo, Types.VARCHAR);
             }
         }
         
         CachedRowSetImpl crs = new CachedRowSetImpl();
         int i889 = 0;
         crs.setMetaData(rsmd);
         String condicionWhere = "f.fname IN (" + listaDeFactoresResultado + ") AND studyid = " + studyId + " AND represno =" + trepresNo + "";
         if (trialFactorId > 0) {
             consultaSQL = "SELECT OUNITID FROM FACTOR F "
                     + "INNER JOIN (LEVEL_N L INNER JOIN OINDEX O "
                     + "ON (L.LEVELNO = O.LEVELNO) AND (L.FACTORID = O.FACTORID)) "
                     + "ON (F.FACTORID = L.FACTORID) "
                     + "AND (F.LABELID = L.LABELID) "
                     + "WHERE f.fname IN ('" + trialName + "') "
                     + "AND studyid = " + studyId
                     + " AND represno =" + trepresNo
                     + " AND lvalue = " + trialFactorId;
 
             query = session.createSQLQuery(consultaSQL);
             resultado = query.list();
 
             int cuantosRegistros = 0;
             String cadOunitid = "";
 
             if (resultado.size() == 0) {
                 return null;
             } else {
                 for (Object fila : resultado) {
                     cuantosRegistros += 1;
                     cadOunitid += fila.toString() + ",";
                 }
             }
             cadOunitid = cadOunitid.substring(0, cadOunitid.length() - 1);
             condicionWhere += " and ounitid in (" + cadOunitid + ")";
         }
 
         consultaSQL = "SELECT O.OUNITID, FNAME, LVALUE, LTYPE, F.LABELID "
                 + "FROM FACTOR F INNER JOIN (LEVEL_N L "
                 + "INNER JOIN OINDEX O ON (L.LEVELNO = O.LEVELNO) "
                 + "AND (L.FACTORID = O.FACTORID)) "
                 + "ON (F.FACTORID = L.FACTORID) "
                 + "AND (F.LABELID = L.LABELID) "
                 + "WHERE " + condicionWhere + "";
         consultaSQL += " UNION ";
         consultaSQL += "SELECT O.OUNITID, FNAME, LVALUE, LTYPE, F.LABELID "
                 + "FROM FACTOR F INNER JOIN (LEVEL_C L "
                 + "INNER JOIN OINDEX O ON (L.LEVELNO = O.LEVELNO) "
                 + "AND (L.FACTORID = O.FACTORID)) "
                 + "ON (F.FACTORID = L.FACTORID) "
                 + "AND (F.LABELID = L.LABELID) "
                 + "WHERE " + condicionWhere + "";
         consultaSQL += " ORDER BY OUNITID " + orden + ", LABELID " + orden;
 
         query = session.createSQLQuery(consultaSQL);
 
         query.addScalar("OUNITID", Hibernate.INTEGER);
         query.addScalar("FNAME", Hibernate.STRING);
         query.addScalar("LVALUE", Hibernate.STRING);
         query.addScalar("LTYPE", Hibernate.STRING);
         query.addScalar("LABELID", Hibernate.INTEGER);
 
         resultado = query.list();
 
         int tounitidAnt = 0;
         int tounitidActual = 0;
         String fname = "";
         int tlvalue = 0;
         for (Object fila : resultado) {
             Object[] celdas = (Object[]) fila;
 
             tounitidActual = (Integer) celdas[ounitid];
             if (tounitidAnt != tounitidActual) {
                 if (tounitidAnt != 0) {
                     crs.insertRow();
                 }
                 crs.moveToInsertRow();
                 for (i889 = 1; i889 <= cuantosFR; i889++) {
                     crs.updateNull(i889);
                 }
             }
             fname = (String) celdas[FNAME];
             String ltypeTemp = (String) celdas[LTYPE];
             ltypeTemp = ltypeTemp.trim().toUpperCase();
             if (ltypeTemp.equals("N")) {
                 if (celdas[2] instanceof String) {
                     String valueTemp = (String) celdas[LVALUE];
                     tlvalue = Integer.valueOf(valueTemp).intValue();
                 } else {
                     byte[] bytes = (byte[]) celdas[LVALUE];
                     String valueTemp = new String(bytes);
                     tlvalue = Integer.valueOf(valueTemp).intValue();
                 }
                 crs.updateInt(fname, tlvalue);
             } else {
                 if (celdas[2] instanceof String) {
                     crs.updateString(fname, (String) celdas[LVALUE]);
                 } else {
                     byte[] bytes = (byte[]) celdas[LVALUE];
                     String valueTemp = new String(bytes);
                     crs.updateString(fname, valueTemp);
                 }
 
             }
             tounitidAnt = tounitidActual;
         }
         if (tounitidAnt != 0) {
             crs.insertRow();
         }
         crs.moveToCurrentRow();
         crs.beforeFirst();
         pr = crs;
         log.info("Getting trial randomization.... DONE");
         return pr;
     }
     
     public static Study getFullFactorsByStudyIdAndEffectId(Study study, Integer effectId){
        SQLQuery query = null;
        
        return study;
     }
     
     public static List<Measurement> getTrialRandomizationVeryFast(
             final int studyId,
             final int trialFactorId,
             final List<String> factoresPrincipales,
             final List<String> factoresSalida,
             final String trialName,
             Session session,
             boolean isLocal,
             boolean isCentral
             ) throws SQLException {
         
         String factoresPrincipalesStr = DMSReaderDAO.getFactoresParaUsoInQuery(factoresPrincipales);
         String factoresResultadoStr = DMSReaderDAO.getFactoresParaUsoInQuery(factoresSalida);
         
         SQLQuery query = null;
         List resultado;
 
         log.info("Getting trial randomization fast");
         Integer numeroDeFactoresPrincipales = factoresPrincipales.size();
         
         Integer trepresNo = HelperWorkbookReader.getRepresno(
                 session,
                 query,
                 studyId,
                 factoresPrincipalesStr,
                 numeroDeFactoresPrincipales
                 );
         
         if (trepresNo == null) {
             log.error("Repres no encontrado.");
             return null;
         }
 
         log.info("Definiendo orden de busquedas");
         String orden = HelperWorkbookReader.getOrder(isLocal, isCentral);
         
         //GCP NEW SCHEMA, does not seem to need this anymore
         /*
         Integer cuantosFR = HelperWorkbookReader.getNumeroFactoresResultado(
                 session,
                 query,
                 studyId,
                 factoresResultadoStr
                 );
         
         if(! cuantosFR.equals(factoresSalida.size())){
             log.error("No se encontraron todos los factores solicitados.");
             return null;
         }
         * */
 
 //        resultado = HelperWorkbookReader.getFactoresResultado(
 //                session,
 //                query,
 //                studyId,
 //                factoresResultadoStr,
 //                orden
 //                );
         
         String condicionWhere = HelperWorkbookReader.getCondicionesWhere(
                 session,
                 query,
                 studyId,
                 trialFactorId,
                 trialName,
                 trepresNo,
                 factoresResultadoStr
                 );
         
         resultado = HelperWorkbookReader.getListFactorsAndLevels(
                 session,
                 query,
                 condicionWhere,
                 orden
                 );
         
         Integer ounitInicial;
         if(resultado == null){
             log.error("No se encontro ningun dato referente a los factores.");
             return null;
         }else if(resultado.isEmpty()){
             log.error("No se encontro ningun dato referente a los factores.");
             return null;
         }else{
             Object fila = resultado.get(0);
             Object[] celdas = (Object[]) fila;
             ounitInicial = (Integer) celdas[0];
         }
         Map<String, Integer> ordenFactoresSalida = new HashMap<String, Integer>();
         for(String factorS : factoresSalida){
             ordenFactoresSalida.put(factorS, factoresSalida.indexOf(factorS));
         }
         List<Object> factorLabelList = new ArrayList<Object>();
         for(int i = 0 ; i<factoresSalida.size() ; i++){
             factorLabelList.add("");
         }
         List<Measurement> measurementList = new ArrayList<Measurement>();
         Measurement measurement = new Measurement();
         Integer ounitTemp = ounitInicial.intValue();
         for(Object fila : resultado){
             Object[] celdas = (Object[]) fila;
             //Condiciones para cambio de fila
             if(!ounitTemp.equals(celdas[0])){
                 measurement.setFactorLabelData(factorLabelList);
                 measurement.setOunitId(ounitTemp);
                 measurementList.add(measurement);
                 measurement = new Measurement();
                 factorLabelList = new ArrayList<Object>();
                 for(int i = 0; i<factoresSalida.size() ; i++){
                     factorLabelList.add("");
                 }
                 ounitTemp = (Integer) celdas[0];
             }
             
             if (ordenFactoresSalida.get("TRIAL") != null && factorLabelList.get(ordenFactoresSalida.get("TRIAL")) != null) {
                 factorLabelList.set(ordenFactoresSalida.get("TRIAL"), celdas[5]);
             }
             factorLabelList.set(ordenFactoresSalida.get((String) celdas[1]), celdas[2]);
         }
         measurement.setFactorLabelData(factorLabelList);
         measurement.setOunitId(ounitTemp);
         measurementList.add(measurement);
         
         log.info("Getting trial randomization fast.... DONE");
         
         log.info("Getting variates fast....");
         List<Integer> listVariates = getVariatesByRepresno(session, query, trepresNo);
         if(listVariates == null){
             log.info("No se encontraron variates por recuperar.");
         }else if(listVariates.isEmpty()){
             log.info("No se encontraron variates por recuperar.");
         }else if(listVariates.size() == 0){
             log.info("No se encontraron variates por recuperar.");
         }else{
             List<DataN> dataNs = getDataN(session, listVariates, orden);
             List<DataC> dataCs = getDataC(session, listVariates, orden);
             List<Variate> variates = getVariates(session, listVariates, orden);
             Map<Integer, Integer> mapaVariates = new HashMap<Integer, Integer>();
 
             Map<Integer, Integer> mapaOunitId = new HashMap<Integer, Integer>();
 
             for(Variate variate : variates){
                 mapaVariates.put(variate.getVariatid(), variates.indexOf(variate));
             }
 
             for(Measurement measurement1 : measurementList){
                 measurement1.initMeasurementData(variates.size());
                 mapaOunitId.put(measurement1.getOunitId(), measurementList.indexOf(measurement1));
             }
 
             for(DataN dataN : dataNs){
                 Integer indiceY = mapaOunitId.get(dataN.getDataNPK().getOunitid());
                 Integer indiceX = mapaVariates.get(dataN.getDataNPK().getVariatid());
                 if(indiceY != null){
     //                log.info(" indice x: " + indiceX + " indice y: " + indiceY);
     //                if(indiceX == null || indiceY == null){
     //                    log.info(" indice x: " + dataN.getDataNPK().getVariatid() + " indice y: " + dataN.getDataNPK().getOunitid());
     //                }
                     Measurement measurementTemp = measurementList.get(indiceY);
                     MeasurementData measurementData = measurementTemp.getMeasurementsData().get(indiceX);
                     measurementData.setData(dataN);
                 }
             }
 
             for(DataC dataC : dataCs){
                 Integer indiceY = mapaOunitId.get(dataC.getDataCPK().getOunitid());
                 Integer indiceX = mapaVariates.get(dataC.getDataCPK().getVariatid());
                 if(indiceY != null){
                     Measurement measurementTemp = measurementList.get(indiceY);
                     MeasurementData measurementData = measurementTemp.getMeasurementsData().get(indiceX);
                     measurementData.setData(dataC);
                 }
             }
         }
         log.info("Getting variates fast.... DONE");
         return measurementList;
     }
     
     public static String getOrder(boolean local, boolean central){
         if (local) {
             return "DESC";
         } else if (central) {
             return "ASC";
         } else {
             return "DESC";
         }
     }
     
     public static String getScname(
             Session session,
             SQLQuery query,
             Integer studyid
             ){
         if(studyid == null){
             log.error("El siguiente studyid = " + studyid + " no es un estudio valido.");
         }
         /*String consultaSQL = "select SNAME as SNAME "
                 + "from study "
                 + "where study.STUDYID = " + studyid + ";";
         */
         //NEW SCHEMA assumption: id is a study id, not a dataset id
         //if id is a dataset id, it will return dataset name
         String consultaSQL = "SELECT name AS SNAME " 
                 + " FROM project "
                 + " WHERE project_id = " + studyid + ";";
         
         query = session.createSQLQuery(consultaSQL);
         query.addScalar("SNAME", Hibernate.STRING);
         Object snameTemp = query.uniqueResult();
         if (snameTemp == null){
             log.error("No se encontro el nombre del estudio para el id = " + studyid);
         }
         return (String)snameTemp;
     }
     
     public static List getFactoresPrincipales(
             Session session,
             SQLQuery query,
             Integer studyid,
             String orden
             ){
         List resultado;
 /*        
         String consultaSQL = "SELECT "
                 + "factor.FNAME as FNAME, "
                 + "tmstraits.trname as TRNAME, "
                 + "tmsscales.scname as SCNAME, "
                 + "factor.LABELID as LABELID "
                 + "from factor "
                 + "LEFT join tmsmeasuredin on "
                 + "factor.TID = tmsmeasuredin.traitid and "
                 + "tmsmeasuredin.scaleid = factor.SCALEID and "
                 + "factor.TMETHID = tmsmeasuredin.tmethid "
                 + "LEFT JOIN tmsscales ON "
                 + "tmsscales.scaleid = tmsmeasuredin.scaleid "
                 + "LEFT JOIN tmstraits ON "
                 + "tmstraits.tid = tmsmeasuredin.traitid "
                 + "where factor.STUDYID = "
                 + studyid
                 + " and factor.FACTORID factor= factor.LABELID "
                 + "order by LABELID " + orden + ";";
  */     //NEW SCHEMA  
         String consultaSQL = "SELECT " 
                 + "  fname.value AS FNAME "
                 + "  , factor.traitid AS TRNAME " 
                 + "  , scalerel.object_id AS SCNAME " 
                 + "  , factor.projectprop_id AS LABELID " 
                 + "  , factor.varid AS FACTORTERM "
                 + " FROM " 
                 + "  v_factor factor " 
                 + "  INNER JOIN projectprop fname ON fname.project_id = factor.project_id AND fname.rank = fname.rank " 
                 + "      AND fname.type_id = factor.storedinid " 
                 + "  INNER JOIN cvterm_relationship scalerel ON scalerel.type_id = 1220 AND scalerel.subject_id = factor.varid " 
                 + " WHERE "
                 + "  factor.factorid = factor.projectprop_id "
                 + "  AND factor.project_id = " + studyid 
                 + " ORDER BY " 
                 + "  factor.projectprop_id " + orden 
                 + ";";
 
         query = session.createSQLQuery(consultaSQL);
         query.addScalar("FNAME", Hibernate.STRING);
         query.addScalar("TRNAME", Hibernate.STRING);
         query.addScalar("SCNAME", Hibernate.STRING);
         query.addScalar("LABELID", Hibernate.INTEGER);
         query.addScalar("FACTORTERM", Hibernate.STRING);
         resultado = query.list();
         if(resultado == null){
             log.error("No se encontraron factores principales.");
         }
         return resultado;
     }
     
     public static List getFactoresSalida(
             Session session,
             SQLQuery query,
             Integer studyid,
             Integer numberEntry,
             String orden
             ){
         List resultado;
         /*
         String consultaSQL = "SELECT "
                 + "factor.FNAME as FNAME, "
                 + "tmstraits.trname as TRNAME, "
                 + "tmsscales.scname as SCNAME, "
                 + "factor.LABELID as LABELID "
                 + "from factor "
                 + "LEFT join tmsmeasuredin on "
                 + "factor.TID = tmsmeasuredin.traitid and "
                 + "tmsmeasuredin.scaleid = factor.SCALEID and "
                 + "factor.TMETHID = tmsmeasuredin.tmethid "
                 + "LEFT JOIN tmsscales ON "
                 + "tmsscales.scaleid = tmsmeasuredin.scaleid "
                 + "LEFT JOIN tmstraits ON "
                 + "tmstraits.tid = tmsmeasuredin.traitid "
                 + "where factor.STUDYID = "
                 + studyid
                 + " and factor.FACTORID = "
                 + numberEntry
                 + " order by LABELID " + orden + ";";
         */
         //NEW SCHEMA
         String consultaSQL = "SELECT " 
                 + "  fname.value AS FNAME " 
                 + "  , factor.traitid AS TRNAME " 
                 + "  , scalerel.object_id AS SCNAME " 
                 + "  , factor.projectprop_id AS LABELID " 
                 + "  , factorprop.value AS FACTORTERM "
                 + " FROM " 
                 + "  v_factor factor " 
                 + "  INNER JOIN projectprop fname ON fname.project_id = factor.project_id AND fname.rank = factor.rank " 
                 + "      AND fname.type_id = factor.storedinid " 
                 + "  INNER JOIN cvterm_relationship scalerel ON scalerel.type_id = 1220 AND scalerel.subject_id = factor.varid " 
                 + "  INNER JOIN projectprop factorprop ON factorprop.projectprop_id = factor.factorid "
                 + " WHERE " 
                 + "  factor.project_id = " + studyid 
                 + "  AND factor.factorid = " + numberEntry
                 + " ORDER BY " 
                 + "  factor.projectprop_id " + orden + ";";
         
         query = session.createSQLQuery(consultaSQL);
         query.addScalar("FNAME", Hibernate.STRING);
         query.addScalar("TRNAME", Hibernate.STRING);
         query.addScalar("SCNAME", Hibernate.STRING);
         query.addScalar("LABELID", Hibernate.INTEGER);
         query.addScalar("FACTORTERM", Hibernate.STRING);
         resultado = query.list();
         if(resultado == null){
             log.error("No se encontro ningun factor para los facores de salida.");
         }
         return resultado;
     }
 /*
     public static Integer getRepresno(
             Session session,
             SQLQuery query,
             Integer studyid,
             String factoresPrincipalesStr,
             Integer numeroDeFactoresPrincipales
             ){
         String consultaSQL = "SELECT represno, COUNT(*) FROM effect e "
                 + "INNER JOIN factor f ON e.factorid=f.factorid "
                 + "WHERE studyid=" + studyid + " AND "
                 + "f.factorid = f.labelid AND "
                 + "fname IN (" + factoresPrincipalesStr + ") "
                 + "GROUP BY represno HAVING COUNT(*) = " + numeroDeFactoresPrincipales;
         
         query = session.createSQLQuery(consultaSQL);
         List resultado = query.list();
         if (resultado != null) {
             if (resultado.size() > 0) {
                 Object[] fila = (Object[]) resultado.get(0);
                 return (Integer) fila[represNo];
             } else {
                 log.error("No se encontro el represNo");
                 return null;
             }
         } else {
             log.error("No se encontro el represNo");
             return null;
         }
     }
     */
     //getRepresno using the new schema.
     public static Integer getRepresno(
             Session session,
             SQLQuery query,
             Integer studyid,
             String factoresPrincipalesStr,
             Integer numeroDeFactoresPrincipales
             ){
        String consultaSQL = "SELECT " 
                 + "  ds.project_id AS represno " 
                 + "  , COUNT(*) AS countvalue" 
                 + " FROM " 
                 + "  project ds " 
                 + "  INNER JOIN project_relationship pr ON pr.type_id = 1150 AND pr.subject_project_id = ds.project_id " 
                 + "  INNER JOIN v_factor mainfactor ON mainfactor.project_id = ds.project_id AND mainfactor.projectprop_id = mainfactor.factorid "
                 + "  INNER JOIN projectprop fname ON fname.project_id = mainfactor.project_id AND fname.rank = mainfactor.rank " 
                 + "      AND fname.type_id = mainfactor.storedinid " 
                 + " WHERE " 
                 + "  pr.object_project_id = " + studyid  
                 + "  AND fname.value IN (" + factoresPrincipalesStr + ") " 
                 + " GROUP BY " 
                 + "  ds.project_id " 
                 + " HAVING " 
                 + "  COUNT(*) = " + numeroDeFactoresPrincipales;
                 
        query = session.createSQLQuery(consultaSQL);
        query.addScalar("represno", Hibernate.INTEGER);
        query.addScalar("countvalue", Hibernate.INTEGER);
        List resultado = query.list();
         if (resultado != null) {
             if (resultado.size() > 0) {
                 Object[] fila = (Object[]) resultado.get(0);
                 return (Integer) fila[represNo];
             } else {
                 log.error("No se encontro el represNo");
                 return null;
             }
         } else {
             log.error("No se encontro el represNo");
             return null;
         }
     }
 /*
     public static Integer getNumeroFactoresResultado(
             Session session,
             SQLQuery query,
             Integer studyid,
             String factoresResultadoStr
             ){
         String consultaSQL = "SELECT count(*) FROM factor "
                 + "WHERE studyid=" + studyid
                 + " and fname IN(" + factoresResultadoStr + ")";
         
         Integer cuantosFR = 0;
         
         query = session.createSQLQuery(consultaSQL);
         Object tempObject = query.uniqueResult();
         
         if (tempObject instanceof BigInteger) {
             BigInteger temp = (BigInteger) tempObject;
             cuantosFR = temp.intValue();
         } else if (tempObject instanceof Integer) {
             Integer temp = (Integer) tempObject;
             cuantosFR = temp.intValue();
         } else if(tempObject == null){
             log.error("No se encontro ningun factor resultado");
         }
         return cuantosFR;
     }
 */
     //using New Schema
     //assumes projectid passed is the studyid and not a datasetid
     public static Integer getNumeroFactoresResultado(
             Session session,
             SQLQuery query,
             Integer studyid,
             String factoresResultadoStr
             ){
         
         if (factoresResultadoStr == null || "".equals(factoresResultadoStr)) {
             log.error("No se encontro ningun factor resultado");
             return 0;
         }
         String consultaSQL = "SELECT COUNT(*)" 
                 + " FROM " 
                 + "  v_factor factor " 
                 + "  INNER JOIN projectprop fname ON fname.project_id = factor.project_id AND fname.rank = factor.rank AND fname.type_id = factor.storedinid " 
                 + " WHERE " 
                 + "  factor.project_id = " + studyid 
                 + "  AND fname.value IN (" + factoresResultadoStr + ")";
         
         Integer cuantosFR = 0;
         
         query = session.createSQLQuery(consultaSQL);
         Object tempObject = query.uniqueResult();
         
         if (tempObject instanceof BigInteger) {
             BigInteger temp = (BigInteger) tempObject;
             cuantosFR = temp.intValue();
         } else if (tempObject instanceof Integer) {
             Integer temp = (Integer) tempObject;
             cuantosFR = temp.intValue();
         } else if(tempObject == null){
             log.error("No se encontro ningun factor resultado");
         }
         return cuantosFR;
     }
   
 /*    public static List getFactoresResultado(
             Session session,
             SQLQuery query,
             Integer studyid,
             String factoresResultadoStr,
             String orden
             ){
         String consultaSQL = "SELECT fname, ltype, labelid FROM factor "
                 + "WHERE studyid=" + studyid
                 + " and fname IN(" + factoresResultadoStr + ")"
                 + " ORDER BY labelid " + orden;
         if(factoresResultadoStr == null){
             log.error("La lista de factores esta vacia.");
         }else if(factoresResultadoStr.isEmpty()){
             log.error("La lista de factores esta vacia.");
         }
         query = session.createSQLQuery(consultaSQL);
         List resultado = query.list();
         if(resultado == null){
             log.error("No se encontraron factores para regresar.");
         }
         return resultado;
     }
 */  //NEW SCHEMA
     public static List getFactoresResultado(
             Session session,
             SQLQuery query,
             Integer studyid,
             String factoresResultadoStr,
             String orden
             ){
             String consultaSQL = "SELECT " 
                     + "  fname.value AS fname " //fname
                     + "  , IF(dtyperel.object_id IN (1120, 1125, 1128, 1130), 'C', 'N') AS ltype " //ltype
                     + "  , stdvar.projectprop_id AS labelid " //labelid
                     + " FROM " 
                     + "  projectprop stdvar " 
                     + "  INNER JOIN projectprop fname ON fname.project_id = stdvar.project_id " 
                     + "    AND fname.rank = stdvar.rank AND fname.type_id NOT IN (1060, 1070, stdvar.value) " 
                     + "  INNER JOIN cvterm_relationship dtyperel ON dtyperel.type_id = 1105 " 
                     + "    AND dtyperel.subject_id = stdvar.value " 
                     + " WHERE " 
                     + "  stdvar.type_id = 1070 " 
                     + "  AND stdvar.project_id = " + studyid 
                     + "  AND fname.value IN (" + factoresResultadoStr + ") " 
                     + "  ORDER BY stdvar.projectprop_id " + orden;
         if(factoresResultadoStr == null){
             log.error("La lista de factores esta vacia.");
         }else if(factoresResultadoStr.isEmpty()){
             log.error("La lista de factores esta vacia.");
         }
         query = session.createSQLQuery(consultaSQL);
         query.addScalar("fname", Hibernate.STRING);
         query.addScalar("ltype", Hibernate.STRING);
         query.addScalar("labelid", Hibernate.INTEGER);
         
         List resultado = query.list();
         if(resultado == null){
             log.error("No se encontraron factores para regresar.");
         }
         return resultado;
     }
 /*    
     public static String getCondicionesWhere(
             Session session,
             SQLQuery query,
             Integer studyid,
             Integer trial,
             String trialName,
             Integer represNo,
             String factoresResultadoStr
             ){
         String condicionWhere = "f.fname IN (" + factoresResultadoStr + ") AND studyid = " + studyid + " AND represno =" + represNo + "";
         if (trial > 0) {
             String consultaSQL = "SELECT OUNITID FROM FACTOR F "
                     + "INNER JOIN (LEVEL_N L INNER JOIN OINDEX O "
                     + "ON (L.LEVELNO = O.LEVELNO) AND (L.FACTORID = O.FACTORID)) "
                     + "ON (F.FACTORID = L.FACTORID) "
                     + "AND (F.LABELID = L.LABELID) "
                     + "WHERE f.fname IN ('" + trialName + "') "
                     + "AND studyid = " + studyid
                     + " AND represno =" + represNo
                     + " AND lvalue = " + trial;
 
             query = session.createSQLQuery(consultaSQL);
             List resultado = query.list();
 
             int cuantosRegistros = 0;
             String cadOunitid = "";
 
             if (resultado.size() == 0) {
                 return null;
             } else {
                 for (Object fila : resultado) {
                     cuantosRegistros += 1;
                     cadOunitid += fila.toString() + ",";
                 }
             }
             cadOunitid = cadOunitid.substring(0, cadOunitid.length() - 1);
             condicionWhere += " and ounitid in (" + cadOunitid + ")";
         }
         return condicionWhere;
     }*/
     
     //New Schema
     public static String getCondicionesWhere(
             Session session,
             SQLQuery query,
             Integer studyid,
             Integer trial,
             String trialName,
             Integer represNo,
             String factoresResultadoStr
             ){
         
         if (factoresResultadoStr == null || "".equals(factoresResultadoStr)) {
             return null;
         }
         String condicionWhere = "fname.value IN (" + factoresResultadoStr + ")"
                 + " AND (prs.subject_project_id = " + studyid + " OR prd.subject_project_id = " + represNo + ") "
                 ;
         if (trial != null && trial > 0) {
             String consultaSQL = "SELECT " 
                     + "  level.nd_experiment_id AS OUNITID" 
                     + " FROM " 
                     + "  v_level level " 
                     + "  INNER JOIN projectprop stdvar ON stdvar.projectprop_id = level.labelid " 
                     + "  INNER JOIN projectprop fname ON fname.project_id = stdvar.project_id AND fname.rank = stdvar.rank AND fname.type_id = level.storedinid " 
                     + "  LEFT JOIN project_relationship prd ON prd.type_id IN = 1150 " 
                     + "      AND prd.subject_project_id = fname.project_id " 
                     + "  LEFT JOIN project_relationship prs ON prs.type_id IN = 1145 " 
                     + "      AND prs.subject_project_id = fname.project_id " 
                     + " WHERE " 
                     + "  fname.value = '" + trialName + "'" 
                     + "  AND (prs.subject_project_id = " + studyid + " OR prd.subject_project_id = " + represNo + ") " 
                     + "  AND level.lvalue = " + trial 
                     ;
 
             query = session.createSQLQuery(consultaSQL);
             List resultado = query.list();
 
             int cuantosRegistros = 0;
             String cadOunitid = "";
 
             if (resultado.size() == 0) {
                 return null;
             } else {
                 for (Object fila : resultado) {
                     cuantosRegistros += 1;
                     cadOunitid += fila.toString() + ",";
                 }
             }
             cadOunitid = cadOunitid.substring(0, cadOunitid.length() - 1);
             condicionWhere += " AND level.nd_experiment_id IN (" + cadOunitid + ") ";
         }
         return condicionWhere;
     }
 /*
     public static List getListFactorsAndLevels(
             Session session,
             SQLQuery query,
             String condicionWhere,
             String orden
             
             ){
         String consultaSQL = "SELECT O.OUNITID, FNAME, LVALUE, LTYPE, F.LABELID "
                 + "FROM FACTOR F INNER JOIN (LEVEL_N L "
                 + "INNER JOIN OINDEX O ON (L.LEVELNO = O.LEVELNO) "
                 + "AND (L.FACTORID = O.FACTORID)) "
                 + "ON (F.FACTORID = L.FACTORID) "
                 + "AND (F.LABELID = L.LABELID) "
                 + "WHERE " + condicionWhere + "";
         consultaSQL += " UNION ";
         consultaSQL += "SELECT O.OUNITID, FNAME, LVALUE, LTYPE, F.LABELID "
                 + "FROM FACTOR F INNER JOIN (LEVEL_C L "
                 + "INNER JOIN OINDEX O ON (L.LEVELNO = O.LEVELNO) "
                 + "AND (L.FACTORID = O.FACTORID)) "
                 + "ON (F.FACTORID = L.FACTORID) "
                 + "AND (F.LABELID = L.LABELID) "
                 + "WHERE " + condicionWhere + "";
         consultaSQL += " ORDER BY OUNITID " + orden + ", LABELID " + orden;
         
         query = session.createSQLQuery(consultaSQL);
         
         query.addScalar("OUNITID", Hibernate.INTEGER);
         query.addScalar("FNAME", Hibernate.STRING);
         query.addScalar("LVALUE", Hibernate.STRING);
         query.addScalar("LTYPE", Hibernate.STRING);
         query.addScalar("LABELID", Hibernate.INTEGER);
         
         List resultado = query.list();
         if(resultado == null){
             log.error("No se encontro ningun listado de factores y levesl a devolver");
             log.error("No se logro recuperar estructura");
         }else if(resultado.isEmpty()){
             log.error("No se encontro ningun listado de factores y levesl a devolver");
             log.error("No se logro recuperar estructura");
         }
         return resultado;
     }*/
     //new schema
     public static List getListFactorsAndLevels(
             Session session,
             SQLQuery query,
             String condicionWhere,
             String orden
             
             ){
         if (condicionWhere == null) {
             log.error("No se encontro ningun listado de factores y levesl a devolver");
             log.error("No se logro recuperar estructura");
             return null;
         }
         String consultaSQL = "SELECT " 
                 + "  level.nd_experiment_id AS OUNITID " 
                 + "  , fname.value AS FNAME " 
                 + "  , level.lvalue AS LVALUE " 
                 + "  , IF(level.dtypeid IN (1120, 1125, 1128, 1130), 'C', 'N') AS LTYPE " 
                 + "  , level.labelid AS LABELID " 
                 + "  , loc.description AS TRIAL"
                 + " FROM " 
                 + "  v_level level " 
                 + "  INNER JOIN projectprop stdvar ON stdvar.projectprop_id = level.labelid " 
                 + "  INNER JOIN projectprop fname ON fname.project_id = stdvar.project_id AND fname.rank = stdvar.rank AND fname.type_id = level.storedinid " 
                 + "  LEFT JOIN project_relationship prd ON prd.type_id = 1150 " 
                 + "      AND prd.subject_project_id = fname.project_id " 
                 + "  LEFT JOIN project_relationship prs ON prs.type_id = 1145 " 
                 + "      AND prs.subject_project_id = fname.project_id " 
                 + "  INNER JOIN nd_experiment exp ON exp.nd_experiment_id = level.nd_experiment_id "
                 + "  INNER JOIN nd_geolocation loc ON loc.nd_geolocation_id = exp.nd_geolocation_id "
                 + " WHERE " + condicionWhere
                 + " AND exp.type_id <> 1020 "
                 + " ORDER BY "
                 + " level.nd_experiment_id " + orden + ", level.labelid " + orden
                 ;
         
         query = session.createSQLQuery(consultaSQL);
         
         query.addScalar("OUNITID", Hibernate.INTEGER);
         query.addScalar("FNAME", Hibernate.STRING);
         query.addScalar("LVALUE", Hibernate.STRING);
         query.addScalar("LTYPE", Hibernate.STRING);
         query.addScalar("LABELID", Hibernate.INTEGER);
         query.addScalar("TRIAL", Hibernate.INTEGER);
         
         List resultado = query.list();
         if(resultado == null){
             log.error("No se encontro ningun listado de factores y levesl a devolver");
             log.error("No se logro recuperar estructura");
         }else if(resultado.isEmpty()){
             log.error("No se encontro ningun listado de factores y levesl a devolver");
             log.error("No se logro recuperar estructura");
         }
         return resultado;
     }
    
 /*    
     public static List<Integer> getVariatesByRepresno(
             Session session,
             SQLQuery query,
             Integer represNo
             ){
         String consultaSQL = "select variatid "
                 + "from veffect "
                 + "WHERE veffect.REPRESNO = " + represNo + ";";
         query = session.createSQLQuery(consultaSQL);
         query.addScalar("variatid", Hibernate.INTEGER);
         List resultado = query.list();
         if (resultado != null) {
             if (resultado.size() > 0) {
                 return resultado;
             } else {
                 log.error("No se encontro ningun variate para el represNo " + represNo);
                 return null;
             }
         } else {
             log.error("No se encontro ningun variate para el represNo " + represNo);
             return null;
         }
     }
 */
     //New Schema
     public static List<Integer> getVariatesByRepresno(
             Session session,
             SQLQuery query,
             Integer represNo
             ){
         String consultaSQL = "SELECT " 
                 + "  stdvar.projectprop_id AS variatid " 
                 + " FROM " 
                 + "  projectprop stdvar " 
                 + "  INNER JOIN cvterm_relationship cvr ON cvr.type_id = 1044 AND cvr.subject_id = stdvar.value "
                 + " WHERE " 
                 + "  stdvar.type_id = 1070 "
                 + "  AND cvr.object_id IN (1043, 1048) "
                 + "  AND stdvar.project_id = " + represNo + ";";
         
         query = session.createSQLQuery(consultaSQL);
         query.addScalar("variatid", Hibernate.INTEGER);
         List resultado = query.list();
         if (resultado != null) {
             if (resultado.size() > 0) {
                 return resultado;
             } else {
                 log.error("No se encontro ningun variate para el represNo " + represNo);
                 return null;
             }
         } else {
             log.error("No se encontro ningun variate para el represNo " + represNo);
             return null;
         }
     }
 /*    
     public static List<DataN> getDataN(
             Session session,
             List<Integer> variates,
             String orden
             ){
         if(variates == null){
             log.error("No existen variates para recuperar DATA_N");
         }else if(variates.isEmpty()){
             log.error("No existen variates para recuperar DATA_N");
         }
         List<DataN> resultado;
         String consultaHQL = "from DataN as dataN "
                 + "where dataN.dataNPK.variatid in (:VariatesStr) "
                 + "order by dataN.dataNPK.ounitid " + orden + ", dataN.dataNPK.variatid " + orden;
         org.hibernate.Query query = session.createQuery(consultaHQL);
         query.setParameterList("VariatesStr", variates.toArray());
         resultado = query.list();
         if(resultado != null){
             return resultado;
         }else{
             log.error("No se encontraron datos en data_n.");
             return null;
         }
     }
 */
     //New schema
     public static List<DataN> getDataN(
             Session session,
             List<Integer> variates,
             String orden
             ){
         if(variates == null){
             log.error("No existen variates para recuperar DATA_N");
         }else if(variates.isEmpty()){
             log.error("No existen variates para recuperar DATA_N");
         }
         List<DataN> resultado;
         String consultaSQL = "SELECT " 
                 + "  eph.nd_experiment_id AS ounitid " 
                 + "  , stdvar.projectprop_id AS variatid " 
                + "  , ph.value AS dvalue " 
                 + " FROM " 
                 + "  projectprop stdvar " 
                 + "  INNER JOIN cvterm_relationship dtyperel ON dtyperel.type_id = 1105 AND dtyperel.subject_id = stdvar.value " 
                 + "  INNER JOIN nd_experiment_project ep ON ep.project_id = stdvar.project_id " 
                 + "  INNER JOIN nd_experiment_phenotype eph ON eph.nd_experiment_id = ep.nd_experiment_id " 
                 + "  INNER JOIN phenotype ph ON ph.phenotype_id = eph.phenotype_id AND ph.observable_id = stdvar.value " 
                 + " WHERE " 
                 + "  dtyperel.object_id NOT IN (1120, 1125, 1128, 1130) " 
                 + "  AND stdvar.projectprop_id IN (" + getStringValue(variates) + ") "
                 + " ORDER BY "
                 + "  eph.nd_experiment_id " + orden + ", stdvar.projectprop_id " + orden
                 ;
         SQLQuery query = session.createSQLQuery(consultaSQL);
         query.addScalar("ounitid", Hibernate.INTEGER);
         query.addScalar("variatid", Hibernate.INTEGER);
         query.addScalar("dvalue", Hibernate.DOUBLE);
         List<Object[]> resultado2 = query.list();
         if(resultado2 != null){
             resultado = new ArrayList<DataN>();
             for (Object[] fila : resultado2) {
                 resultado.add(new DataN(Integer.valueOf(fila[0].toString())
                                         , Integer.valueOf(fila[1].toString())
                                         , Double.valueOf(fila[2].toString())));
             }
             return resultado;
         }else{
             log.error("No se encontraron datos en data_n.");
             return null;
         }
     }
 
     //FOR NEW SCHEMA
     private static String getStringValue(List<Integer> intList) {
         StringBuffer str = null;
         if (intList != null) {
             for (Integer anInt : intList) {
                 if (str == null) {
                     str = new StringBuffer();
                 } else {
                     str.append(", ");
                 }
                 str.append(anInt);
             }
         }
         return str == null ? "" : str.toString();
     }
     
 /*    
     public static List<DataC> getDataC(
             Session session,
             List<Integer> variates,
             String orden
             ){
         if(variates == null){
             log.error("No existen variates para recuperar DATA_C");
         }else if(variates.isEmpty()){
             log.error("No existen variates para recuperar DATA_C");
         }
         List<DataC> resultado;
         String consultaHQL = "from DataC as dataC "
                 + "where dataC.dataCPK.variatid in (:VariatesStr) "
                 + "order by dataC.dataCPK.ounitid " + orden + ", dataC.dataCPK.variatid " + orden;
         org.hibernate.Query query = session.createQuery(consultaHQL);
         query.setParameterList("VariatesStr", variates.toArray());
         resultado = query.list();
         if(resultado != null){
             return resultado;
         }else{
             log.error("No se encontraron datos en data_c.");
             return null;
         }
     }
 */
     //NEW SCHEMA
     public static List<DataC> getDataC(
             Session session,
             List<Integer> variates,
             String orden
             ){
         if(variates == null){
             log.error("No existen variates para recuperar DATA_C");
         }else if(variates.isEmpty()){
             log.error("No existen variates para recuperar DATA_C");
         }
         List<DataC> resultado;
         String consultaSQL = "SELECT " 
                 + "  eph.nd_experiment_id AS ounitid " 
                 + "  , stdvar.projectprop_id AS variatid " 
                + "  , ph.value AS dvalue " 
                 + " FROM " 
                 + "  projectprop stdvar " 
                 + "  INNER JOIN cvterm_relationship dtyperel ON dtyperel.type_id = 1105 AND dtyperel.subject_id = stdvar.value " 
                 + "  INNER JOIN nd_experiment_project ep ON ep.project_id = stdvar.project_id " 
                 + "  INNER JOIN nd_experiment_phenotype eph ON eph.nd_experiment_id = ep.nd_experiment_id " 
                 + "  INNER JOIN phenotype ph ON ph.phenotype_id = eph.phenotype_id AND ph.observable_id = stdvar.value " 
                 + " WHERE " 
                 + "  dtyperel.object_id IN (1120, 1125, 1128, 1130) " 
                 + "  AND stdvar.projectprop_id IN (" + getStringValue(variates) + ") "
                 + " ORDER BY "
                 + "  eph.nd_experiment_id " + orden + ", stdvar.projectprop_id " + orden
                 ;
         SQLQuery query = session.createSQLQuery(consultaSQL);
         query.addScalar("ounitid", Hibernate.INTEGER);
         query.addScalar("variatid", Hibernate.INTEGER);
         query.addScalar("dvalue", Hibernate.STRING);
         List<Object[]> resultado2 = query.list();
         if(resultado2 != null){
             resultado = new ArrayList<DataC>();
             for (Object[] fila : resultado2) {
                 resultado.add(new DataC(Integer.valueOf(fila[0].toString())
                                         , Integer.valueOf(fila[1].toString())
                                         , fila[2].toString()));
             }
             return resultado;
         }else{
             log.error("No se encontraron datos en data_c.");
             return null;
         }
     }
 /*
     public static List<Variate> getVariates(
             Session session,
             List<Integer> variates,
             String orden
             ){
         List<Variate> resultado;
         String consultaHQL = "from Variate as variate "
                 + "where variatid in (:VariatesStr) "
                 + "order by variatid " + orden;
         org.hibernate.Query query = session.createQuery(consultaHQL);
         query.setParameterList("VariatesStr", variates.toArray());
         resultado = query.list();
         if(resultado != null){
             return resultado;
         }else{
             log.error("No se encontraron variates a partir de la lista de variates  de veffect proporcionada.");
             return null;
         }
     }
 */
     //NEW SCHEMA
     public static List<Variate> getVariates(
             Session session,
             List<Integer> variates,
             String orden
             ){
         List<Variate> resultado;
         String consultaSQL = "SELECT " 
                 + "    pp.projectprop_id AS variatid " 
                 + "    , pp.project_id AS studyid " 
                 + "    , term.value AS vname " 
                 + "    , GROUP_CONCAT(IF(cvr.type_id = 1200, cvr.object_id, NULL)) AS traitid " 
                 + "    , GROUP_CONCAT(IF(cvr.type_id = 1220, cvr.object_id, NULL)) AS scaleid " 
                 + "    , GROUP_CONCAT(IF(cvr.type_id = 1210, cvr.object_id, NULL)) AS tmethid " 
                 + "    , GROUP_CONCAT(IF(cvr.type_id = 1105, IF(cvr.object_id IN (1120, 1125, 1128, 1130), 'C', 'N'), NULL)) AS dtype " 
                 + "    , GROUP_CONCAT(IF(cvr.type_id = 1225, obj.name, NULL)) AS vtype " 
                 + "    , GROUP_CONCAT(IF(cvr.type_id = 1044, cvr.object_id, NULL)) AS tid " 
                 + " FROM " 
                 + "    projectprop pp " 
                 + "    INNER JOIN cvterm_relationship cvr ON cvr.subject_id = pp.value " 
                 + "    INNER JOIN cvterm obj ON obj.cvterm_id = cvr.object_id " 
                 + "    INNER JOIN projectprop term ON term.project_id = pp.project_id "
                 + "      AND term.rank = pp.rank AND term.type_id IN (1043, 1048) "
                 + " WHERE " 
                 + "    pp.type_id = 1070 "
                 + "    AND pp.projectprop_id IN (" + getStringValue(variates) + ") " 
                 + " GROUP BY " 
                 + "    pp.projectprop_id " 
                 + " ORDER BY " 
                 + "    pp.projectprop_id " + orden
                 ;
 
         SQLQuery query = session.createSQLQuery(consultaSQL);
         query.addScalar("variatid", Hibernate.INTEGER);
         query.addScalar("studyid", Hibernate.INTEGER);
         query.addScalar("vname", Hibernate.STRING);
         query.addScalar("traitid", Hibernate.INTEGER);
         query.addScalar("scaleid", Hibernate.INTEGER);
         query.addScalar("tmethid", Hibernate.INTEGER);
         query.addScalar("dtype", Hibernate.STRING);
         query.addScalar("vtype", Hibernate.STRING);
         query.addScalar("tid", Hibernate.INTEGER);
         List<Object[]> resultado2 = query.list();
         if(resultado2 != null){
             resultado = new ArrayList<Variate>();
             for (Object[] fila : resultado2) {
                 Variate variate = new Variate();
                 variate.setVariatid(getIntegerValue(fila[0]));
                 variate.setStudyid(getIntegerValue(fila[1]));
                 variate.setVname(getStringValue(fila[2]));
                 variate.setTraitid(getIntegerValue(fila[3]));
                 variate.setScaleid(getIntegerValue(fila[4]));
                 variate.setTmethid(getIntegerValue(fila[5]));
                 variate.setDtype(getStringValue(fila[6]));
                 variate.setVtype(getStringValue(fila[7]));
                 variate.setTid(getIntegerValue(fila[8]));
                 resultado.add(variate);
             }
             return resultado;
         }else{
             log.error("No se encontraron variates a partir de la lista de variates  de veffect proporcionada.");
             return null;
         }
     }
     
     //FOR NEW SCHEMA
     private static String getStringValue(Object strObj) {
          return strObj != null ? strObj.toString() : null;
     }
     private static Integer getIntegerValue(Object intObj) {
         return intObj != null ? Integer.valueOf(intObj.toString()) : null;
     }
 
     //NEW SCHEMA TEAM: NOT USED, NO USAGES FOUND
     public static List<Factor> getFactors(
             Session session,
             Study study,
             List<Integer> labelIds,
             String orden
             ){
         List<Factor> resultado;
         String consultaHQL = "from Factor as f "
                 + "where f.labelid in (:FactorsStr) "
                 + "order by labelid " + orden
                 + ", order by labelid " + orden
                 ;
         org.hibernate.Query query = session.createQuery(consultaHQL);
         query.setParameterList("FactorsStr", labelIds.toArray());
         resultado = query.list();
         if(resultado != null){
             return resultado;
         }else{
             log.error("No se encontraron factores a partir de la lista de labels proporcionada.");
             return null;
         }
     }
 }
