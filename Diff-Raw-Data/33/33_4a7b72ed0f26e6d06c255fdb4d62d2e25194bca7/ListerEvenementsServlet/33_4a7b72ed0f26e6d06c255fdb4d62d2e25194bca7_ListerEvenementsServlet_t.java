 package org.ecn.edtemps.servlets.impl;
 
 import java.util.Date;
 
 import javax.json.JsonValue;
 import javax.servlet.http.HttpServletRequest;
 
 import org.ecn.edtemps.exceptions.EdtempsException;
 import org.ecn.edtemps.exceptions.ResultCode;
 import org.ecn.edtemps.json.JSONUtils;
 import org.ecn.edtemps.managers.BddGestion;
 import org.ecn.edtemps.managers.EvenementGestion;
 import org.ecn.edtemps.servlets.QueryWithIntervalServlet;
 
 public class ListerEvenementsServlet extends QueryWithIntervalServlet {
 
 	private static final long serialVersionUID = -2338486413889378986L;
 	
 	@Override
 	protected JsonValue doQuery(int userId, BddGestion bdd, Date dateDebut, Date dateFin, HttpServletRequest req) throws EdtempsException {
 		
 	String pathInfo = req.getPathInfo();
 	EvenementGestion evenementGestion = new EvenementGestion(bdd);
 			
 		switch(pathInfo) {
 		case "/groupe":
 			int idGroupe = getIntParam(req, "idGroupe");
 			return JSONUtils.getJsonArray(evenementGestion.listerEvenementsGroupe(idGroupe, dateDebut, dateFin, true));
 		case "/intervenant":
 			return JSONUtils.getJsonArray(evenementGestion.listerEvenementsIntervenant(userId, dateDebut, dateFin, true));
 		case "/salle":
 			int idSalle = getIntParam(req, "idSalle");
 			return JSONUtils.getJsonArray(evenementGestion.listerEvenementCompletsSalle(idSalle, dateDebut, dateFin, true));
		case "/groupescalendrier":
			int idCalendrier = getIntParam(req, "idCalendrier");
			return JSONUtils.getJsonArray(evenementGestion.listerEvenementsGroupesCalendrier(idCalendrier, dateDebut, dateFin, true));
 		default:
 			return null;
 		}
 	}
 	
 	protected int getIntParam(HttpServletRequest req, String nomParam) throws EdtempsException {
 		String strParam = req.getParameter(nomParam);
 		if(strParam == null) {
 			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre " + strParam + " manquant", null);
 		}
 		
 		try {
 			return Integer.parseInt(strParam);
 		}
 		catch(NumberFormatException e) {
 			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Paramètre idGroupe non numérique fourni", null);
 		}
 	}
 
 }
