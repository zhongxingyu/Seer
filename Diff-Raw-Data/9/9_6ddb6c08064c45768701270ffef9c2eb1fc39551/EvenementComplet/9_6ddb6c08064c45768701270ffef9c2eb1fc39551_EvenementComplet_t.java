 package org.ecn.edtemps.models.identifie;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.json.JsonObjectBuilder;
 import javax.json.JsonValue;
 
 import org.ecn.edtemps.json.JSONUtils;
 import org.ecn.edtemps.models.Materiel;
 
 public class EvenementComplet extends EvenementIdentifie {
 
 	protected List<String> matieres;
 	protected List<String> types;
 	
 	
 	public EvenementComplet(String nom, Date dateDebut, Date dateFin,
 			List<Integer> idCalendriers, List<SalleIdentifie> salles,
 			List<UtilisateurIdentifie> intervenants,
 			List<UtilisateurIdentifie> responsables,
 			ArrayList<Materiel> materiels, int id, List<String> matieres, List<String> types) {
 		super(nom, dateDebut, dateFin, idCalendriers, salles, intervenants,
 				responsables, materiels, id);
 		
 		this.matieres = matieres;
 		this.types = types;
 	}
 
 
 	public List<String> getMatieres() {
 		return matieres;
 	}
 
 
 	public void setMatieres(List<String> matieres) {
 		this.matieres = matieres;
 	}
 
 
 	public List<String> getTypes() {
 		return types;
 	}
 
 
 	public void setTypes(List<String> types) {
 		this.types = types;
 	}
 	
 	@Override
 	protected JsonObjectBuilder makeJsonObjectBuilder() {
 		
 		// Ajout d'attributs au builder pour compléter l'objet JSON
		JsonObjectBuilder builder = super.makeJsonObjectBuilder();
 		
 		builder.add("matieres", JSONUtils.getJsonStringArray(matieres));
 		builder.add("types", JSONUtils.getJsonStringArray(types));
 		
 		return builder;
 	}
 
 }
