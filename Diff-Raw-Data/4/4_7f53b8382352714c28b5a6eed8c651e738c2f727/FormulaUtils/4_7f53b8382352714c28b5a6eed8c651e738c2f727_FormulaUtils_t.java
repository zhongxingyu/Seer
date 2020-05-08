 /*
  *    GeoTools - The Open Source Java GIS Toolkit
  *    http://geotools.org
  *
  *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package it.geosolutions.destination.utils;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.NumberFormat;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.opengis.feature.simple.SimpleFeature;
 
 /**
  * Utility methods for risk formulas building.
  * 
  * @author "Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it"
  *
  */
 public class FormulaUtils {
 	
 	/** notHumanTargetsList */
 	public static final String notHumanTargetsList = "10,11,12,13,14,15,16";
 	/** humanTargetsList */
 	public static final String humanTargetsList = "1,2,4,5,6,7";
 	
	private static Pattern searchTargetConditional = Pattern.compile("#([^#]*?)#\\*%bersaglio\\(([0-9]+)\\)%", Pattern.CASE_INSENSITIVE);
	private static Pattern searchProcessingConditional = Pattern.compile("#([^#]*?)#\\*%elaborazione\\(([0-9]+)\\)%", Pattern.CASE_INSENSITIVE);
 	
 	private static NumberFormat doubleFormat = NumberFormat.getInstance(Locale.ENGLISH);
 	static {
 		doubleFormat.setGroupingUsed(false);
 	}
 	
 	/**
 	 * Checks that a given target is included in a comma delimited list.
 	 * 
 	 * @param target
 	 * @param targets
 	 * @return
 	 */
 	public static boolean checkTarget(int target, String targets) {
 		return (","+targets+",").contains(","+target+",");
 	}
 	
 	/**
 	 * User chose "all targets".
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public static boolean isAllTargets(int target) {
 		// TODO: find a better representation
 		return target == 100;
 	}
 
 	/**
 	 * User chose "all not human targets".
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public static boolean isAllNotHumanTargets(int target) {
 		// TODO: find a better representation
 		return target == 99;
 	}
 
 	/**
 	 * User chose "all human targets".
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public static boolean isAllHumanTargets(int target) {
 		// TODO: find a better representation
 		return target == 98;
 	}
 
 	/**
 	 * User chose a specific target.
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public static boolean isSimpleTarget(int target) {
 		return !isAllHumanTargets(target) && !isAllNotHumanTargets(target)
 				&& !isAllTargets(target);
 	}
 	
 	/**
 	 * Gets the current detail level from the input feature
 	 * @param features
 	 * @return
 	 */
 	public static int getLevel(SimpleFeatureCollection features) {
 		String typeName = features.getSchema().getTypeName();
 		if(typeName.contains("1")) {
 			return 1;
 		}
 		if(typeName.contains("2")) {
 			return 2;
 		}
 		// TODO: grid should not be useful anymore
 		if(typeName.contains("3") || typeName.contains("grid")) {
 			return 3;
 		}
 		return 0;
 	}
 	
 	/**
 	 * Calculates one or more formula values, for the given comma delimited id list.
 	 * Translates the given target number into the final list of targets (taking into
 	 * account groups of targets).
 	 * When all targets are selected a couple of values is calculated (for humans and
 	 * not humans targets), in all other cases, just one is enough.
 	 * 
 	 * @param conn
 	 * @param level
 	 * @param formulaDescriptor
 	 * @param ids
 	 * @param materials
 	 * @param scenarios
 	 * @param entities
 	 * @param severeness
 	 * @param target
 	 * @param features
 	 * @param precision
 	 * @throws SQLException
 	 */
 	public static void calculateFormulaValues(Connection conn, int level, int processing,
 			Formula formulaDescriptor, String ids, String fk_partner, String materials,
 			String scenarios, String entities, String severeness, String fpfield, int target,
 			Map<Number, SimpleFeature> features, int precision)
 			throws SQLException {
 	
 		String sql = formulaDescriptor.getSql();
 		
 		if(isSimpleTarget(target) || !formulaDescriptor.useTargets()) {
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, ids, fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio1", target + "", null, features, precision, null, null, null, null, null);
 		} else if(isAllHumanTargets(target)) {
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, ids, fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio1", "1,2,4,5,6,7", null, features, precision, null, null, null, null, null);
 		} else if(isAllNotHumanTargets(target)) {
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, ids, fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio1", "10,11,12,13,14,15,16", null, features, precision, null, null, null, null, null);			
 		} else if(isAllTargets(target)) {			
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, ids, fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio1", "1,2,4,5,6,7", null, features, precision, null, null, null, null, null);
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, ids, fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio2", "10,11,12,13,14,15,16", null, features, precision, null, null, null, null, null);
 		}				
 	}
 	
 	/**
 	 * Risk Calculator for formulas that don't depend on arcs.
 	 * 
 	 * @param conn
 	 * @param formulaDescriptor
 	 * @param materials
 	 * @param scenarios
 	 * @param entities
 	 * @param severeness
 	 * @param target
 	 * @return
 	 * @throws SQLException 
 	 */
 	public static Double[] calculateFormulaValues(Connection conn, int level, int processing, Formula formulaDescriptor,
 			String fk_partner, String materials, String scenarios, String entities,
 			String severeness, String fpfield, int target, Map<Integer, Map<Integer, Double>> deletedTargets, int precision) throws SQLException {
 
 		String sql = formulaDescriptor.getSql();
 		if(!formulaDescriptor.takeFromSource()) {
 			if(isSimpleTarget(target) || !formulaDescriptor.useTargets()) {
 				return new Double[] {calculateFormulaValues(conn, level, processing, formulaDescriptor,
 						"", fk_partner, materials, scenarios, entities,
 						severeness, fpfield, sql, "", target + "", deletedTargets,
 						null, precision, null, null, null, null, null).doubleValue(), 0.0};
 				
 			} else if(isAllHumanTargets(target)) {
 				return new Double[] {calculateFormulaValues(conn, level, processing, formulaDescriptor,
 						"", fk_partner, materials, scenarios, entities,
 						severeness, fpfield, sql, "", humanTargetsList, deletedTargets,
 						null, precision, null, null, null, null, null).doubleValue(), 0.0};
 				
 			} else if(isAllNotHumanTargets(target)) {
 				return new Double[] {calculateFormulaValues(conn, level, processing, formulaDescriptor,
 						"", fk_partner, materials, scenarios, entities,
 						severeness, fpfield, sql, "", notHumanTargetsList, deletedTargets,
 						null, precision, null, null, null, null, null).doubleValue(), 0.0};				
 			} else if(isAllTargets(target)) {
 				return new Double[] {calculateFormulaValues(conn, level, processing, formulaDescriptor,
 						"", fk_partner, materials, scenarios, entities,
 						severeness, fpfield, sql, "", humanTargetsList, deletedTargets,
 						null, precision, null, null, null, null, null).doubleValue(),
 						calculateFormulaValues(conn, level, processing, formulaDescriptor,
 						"", fk_partner, materials, scenarios, entities,
 						severeness, fpfield, sql, "", notHumanTargetsList, deletedTargets,
 						null, precision, null, null, null, null, null).doubleValue()};			
 				
 			}	
 		}
 		return new Double[] {0.0, 0.0};
 	}
 	
 	/**
 	 * Calculates one or more formula values, for the given comma delimited id list.
 	 * To do the calculus, launches a parametric SQL query (replacing placemarks with
 	 * user chosen inputs).
 	 * 
 	 * @param conn
 	 * @param level
 	 * @param formulaDescriptor
 	 * @param ids
 	 * @param materials
 	 * @param scenarios
 	 * @param entities
 	 * @param severeness
 	 * @param sql
 	 * @param field
 	 * @param targets
 	 * @param features
 	 * @param precision
 	 * @param cff 
 	 * @param psc 
 	 * @return
 	 * @throws SQLException
 	 */
 	public static Number calculateFormulaValues(Connection conn, int level, int processing, Formula formulaDescriptor,
 			String ids, String fk_partner, String materials, String scenarios, String entities,
 			String severeness, String fpfield, String sql, String field, String targets, 
 			Map<Integer, Map<Integer, Double>> changedTargets,
 			Map<Number, SimpleFeature> features, int precision, 
 			Map<Integer, Double> cff,  // optional List of csv "id_bersaglo,cff" values to use on the simulation
 			List<String> psc,  // optional List of csv id_sostanza,psc values to use on the simulation
 			Map<Integer, Double> padr, // optional List of csv id_sostanza,padr values to use on the simulation
 			Double pis,   // optional List of csv id_geo_arco,pis values to use on the simulation
 			Map<Integer, Double> damageValues
 	) throws SQLException {
 		
 
 		sql = sql.replace("%id_bersaglio%", targets);
 		sql = sql.replace("%id_sostanza%", materials);
 		sql = sql.replace("%id_scenario%", scenarios);
 		sql = sql.replace("%flg_lieve%", entities);
 		sql = sql.replace("%fp_field%", fpfield);
 		sql = sql.replace("%id_geo_arco%", ids);
 		sql = sql.replace("%id_gravita%", severeness);
 		sql = sql.replace("%livello%", level+"");
 		
 		if(damageValues != null) {
 			if(damageValues.size() > 0) {
 				String subQuery = "";
 				for(int bersaglio : damageValues.keySet()) {
 					double value = damageValues.get(bersaglio);
 					subQuery += " WHEN id_bersaglio="+bersaglio+" THEN "+doubleFormat.format(value);
 				}
 				sql = sql.replace("%danno%", "(CASE"+subQuery+" ELSE 0 END)");			
 			} else {
 				sql = sql.replace("%danno%", "0");
 			}
 		}
 		
 		
 		
 		// replace aggregated level
 		sql = sql.replace("siig_geo_ln_arco_3", "siig_geo_pl_arco_3");
 		
 		// PIS: simulation or not
 		sql = replacePis(sql, pis);
 		
 		// PADR: simulation or not
 		sql = replaceMultipleValues(materials, sql, padr, "padr", "id_sostanza");
 		
 		// CFF: simulation or not
 		sql = replaceMultipleValues(targets, sql, cff, "cff" , "id_bersaglio");
 		
 		// simulation targets (added, changed, removed)
 		sql = replaceSimulationTargets(targets, sql, changedTargets);
 		
 		// replace conditional params		
 		Matcher m = searchTargetConditional.matcher(sql);
 		while(m.find()) {
 			int target = Integer.parseInt(m.group(2));
 			if(FormulaUtils.checkTarget(target, targets)) {
 				sql = sql.replace(m.group(0), m.group(1));
 			} else {
 				sql = sql.replace(m.group(0), "0");
 			}
 			
 		}
 		
 		m = searchProcessingConditional.matcher(sql);
 		while(m.find()) {
 			int currentProcessing = Integer.parseInt(m.group(2));
 			if(currentProcessing == processing) {
 				sql = sql.replace(m.group(0), m.group(1));
 			} else {
 				sql = sql.replace(m.group(0), "1");
 			}
 			
 		}
 		
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		
 		try {
 			stmt = conn.prepareStatement(sql);
 			
 			rs = stmt.executeQuery();	
 			Number risk = 0.0;
 			while(rs.next()) {
 				
 				if(features != null) {
 					// accumulate
 					Number id = rs.getInt(1);
 					risk = fixDecimals(rs.getDouble(2), precision) + (features.get(id).getAttribute(field) != null ? (Double)features.get(id).getAttribute(field) : 0.0);
 					features.get(id).setAttribute(field, risk);				
 				} else {
 					risk = fixDecimals(rs.getDouble(1), precision);
 				}
 			}
 			return risk;
 		} finally {
 			if(rs != null) {
 				rs.close();				
 			}
 			if(stmt != null) {
 				stmt.close();
 			}
 			
 		}
 	}
 
 	
 	/**
 	 * @param targets
 	 * @param sql
 	 * @param changedTargets
 	 * @return
 	 */
 	private static String replaceSimulationTargets(String targets, String sql,
 			Map<Integer, Map<Integer, Double>> changedTargets) {
 		if(changedTargets != null) {
 			Matcher m = searchTargetConditional.matcher(sql);
 			while(m.find()) {
 				int target = Integer.parseInt(m.group(2));
 				if(FormulaUtils.checkTarget(target, targets) && changedTargets.containsKey(target)) {
 					String subQuery = "";
 					Map<Integer, Double> distances = changedTargets.get(target);
 					for(int distance : distances.keySet()) {
 						double value = distances.get(distance);
 						subQuery += " WHEN id_distanza="+distance+" THEN "+doubleFormat.format(value);
 					}
 					String simulated = m.group(0).replace("%simulazione(bersaglio)%", "(CASE"+subQuery+" ELSE 0 END)");				
 					sql = sql.replace(m.group(0), simulated);
 				}
 				
 			}
 		}
 		return sql.replaceAll("%simulazione\\(bersaglio\\)%", "0");
 	}
 
 	/**
 	 * @param materials
 	 * @param sql
 	 * @param padr
 	 * @return
 	 */
 	private static String replaceMultipleValues(String valueList, String sql,
 			Map<Integer, Double> userValues, String variable, String idName) {
 		String regex1 = "%simulazione\\("+variable+",([^,)]+),([^,)]+)\\)%";
 		String regex2 = "%simulazione\\("+variable+",([^,)]*?)\\)%";		
 		String replace = null;
 		if(userValues != null && userValues.size()>0) {
 			for(String valueEl : valueList.split(",")) {
 				int value = Integer.parseInt(valueEl);
 				Double candidateValue = userValues.get(value);
 				if(candidateValue != null) {
 					if(replace == null) {
 						replace = "";
 					}
 					replace += " WHEN %" +idName+ "% = " + value + " THEN " + doubleFormat.format(candidateValue);
 				}
 			}
 			
 		}
 		
 		if(replace == null) {
 			sql = sql.replaceAll(regex1, "$1").replaceAll(regex2, variable);
 		} else {
 			String realId = "";
 			Matcher m = Pattern.compile(regex1).matcher(sql);
 			if(m.find()) {
 				realId = m.group(2);
 			} else {
 				 m = Pattern.compile(regex2).matcher(sql);
 				 if(m.find()) {
 					realId = m.group(1);
 				 }
 			}
 			sql = sql.replaceAll(regex1, "(CASE "+replace.replace("%"+idName+"%", realId)+" ELSE $1 END)").replaceAll(regex2, "(CASE "+replace.replace("%"+idName+"%", realId)+" ELSE "+variable+" END)");
 		}
 		return sql;
 	}
 
 	/**
 	 * @param sql
 	 * @param pis
 	 * @return
 	 */
 	private static String replacePis(String sql, Double pis) {
 		String regex1 = "%simulazione\\(pis,([^,)]+)\\)%";
 		String regex2 = "%simulazione\\(pis\\)%";
 		if(pis == null) {
 			sql = sql.replaceAll(regex1, "$1").replaceAll(regex2, "pis");
 		} else {
 			// simulation
 			sql = sql.replaceAll(regex1, doubleFormat.format(pis)).replaceAll(regex2, doubleFormat.format(pis));
 		}
 		return sql;
 	}
 
 	/**
 	 * Similar to the original method calculateForumaValues, allows to specify some custom values.
 	 * It assumes the computation to be done on a single arc.
 	 * 
 	 * @param conn
 	 * @param level
 	 * @param formulaDescriptor
 	 * @param ids
 	 * @param materials
 	 * @param scenarios
 	 * @param entities
 	 * @param severeness
 	 * @param target
 	 * @param features
 	 * @param precision
 	 * @param cff 
 	 * @param psc 
 	 * @throws SQLException
 	 */
 	public static void calculateSimulationFormulaValuesOnSingleArc(Connection conn, int level, int processing,
 			Formula formulaDescriptor, int id_geo_arco, String fk_partner, String materials,
 			String scenarios, String entities, String severeness, String fpfield, int target,
 			Map<Integer, Map<Integer, Double>> changedTargets, Map<Number, SimpleFeature> features, int precision, 
 			Map<Integer, Double>  cff, List<String> psc, Map<Integer, Double> padr, Double pis, Map<Integer, Double> damageValues)
 			throws SQLException {
 	
 		String sql = formulaDescriptor.getSql();
 		
 		if(isSimpleTarget(target) || !formulaDescriptor.useTargets()) {
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, id_geo_arco+"", fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio1", target + "", changedTargets, features, precision,
 					cff, psc, padr, pis, damageValues);
 		} else if(isAllHumanTargets(target)) {
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, id_geo_arco+"", fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio1", "1,2,4,5,6,7", changedTargets, features, precision,
 					cff, psc, padr, pis, damageValues);
 		} else if(isAllNotHumanTargets(target)) {
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, id_geo_arco+"", fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio1", "10,11,12,13,14,15,16", changedTargets, features, precision,
 					cff, psc, padr, pis, damageValues);			
 		} else if(isAllTargets(target)) {			
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, id_geo_arco+"", fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio1", "1,2,4,5,6,7", changedTargets, features, precision,
 					cff, psc, padr, pis, damageValues);
 			calculateFormulaValues(conn, level, processing, formulaDescriptor, id_geo_arco+"", fk_partner, materials, scenarios,
 					entities, severeness, fpfield, sql, "rischio2", "10,11,12,13,14,15,16", changedTargets, features, precision,
 					cff, psc, padr, pis, damageValues);
 		}				
 	}
 	
 	/**
 	 * Rounds a number to the given decimals.
 	 * 
 	 * @param number
 	 * @param numDecimals
 	 * @return
 	 */
 	public static Double fixDecimals(double number , int numDecimals) {
 		double pow = Math.pow(10, numDecimals);
 		return Math.round(number * (int)pow) / pow;
 	}
 }
