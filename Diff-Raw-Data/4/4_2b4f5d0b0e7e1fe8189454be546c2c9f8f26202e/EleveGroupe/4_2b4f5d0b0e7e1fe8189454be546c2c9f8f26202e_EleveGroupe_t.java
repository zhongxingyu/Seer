 /****************************************************************************
 
 	ASCMII is a web application developped for the Ecole Centrale de Nantes
 	aiming to organize quizzes during courses or lectures.
     Copyright (C) 2013  Malik Olivier Boussejra
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
 
 ******************************************************************************/
 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import play.db.ebean.Model;
 
 @SuppressWarnings("serial")
 @Entity
 public class EleveGroupe extends Model {
 	@Id
 	public Long id;
 
 	public String groupe_nom;
 	
 	public static Finder<Long,EleveGroupe> find = new Finder<Long,EleveGroupe>(Long.class, EleveGroupe.class);
 
 	/**
 	 * Constructeur pour créer un groupe
 	 * @param groupe_nom
 	 */
 	public EleveGroupe(String groupe_nom) {
 		this.id=idNonUtilisee();
 		if(!groupe_nom.equals(""))
 			this.groupe_nom=groupe_nom;
 		else
 			this.groupe_nom="Groupe n°"+id;
 	}
 
 	@Override
 	public String toString(){
 		return id.toString();
 	}
 	
 	public static Long idNonUtilisee(){
 		List<EleveGroupe> gTemp = EleveGroupe.find.orderBy("id desc").findList();
 		if(!gTemp.isEmpty()){
 			return gTemp.get(0).id+1;
 		}else{
 			return 1L;
 		}
 	}
 
 	public void remove() {
 		List<EleveHasGroupe> ehgs = EleveHasGroupe.find.where().eq("groupe",this).findList();
 		for(EleveHasGroupe ehg : ehgs){
 			ehg.delete();
 		}
 		this.delete();
 		
 	}
	
	public static List<EleveGroupe> findAll(){
		return find.all();
	}
 }
