 package models;
 
 import java.io.File;
 import java.util.List;
 
 import javax.persistence.*;
 
 import com.avaje.ebean.Ebean;
 
 import play.db.ebean.*;
 
 @Entity
 public class Joueur extends Model {
 	public static Finder<String, Joueur> find = new Finder<String, Joueur>(
 		String.class, Joueur.class
 	);
 
 	@Id
 	public String pseudo;
 	public String password;
 	public Integer score;
 	public Integer xpVitesse;
 	public Integer xpEndurance;
 	public Boolean estAdmin;
 
 	public Joueur(String pseudo, String password, Integer score,
 	              Integer xpVitesse, Integer xpEndurance, Boolean estAdmin) {
 		this.pseudo = pseudo;
 		this.password = password;
 		this.score = score;
 		this.xpVitesse = xpVitesse;
 		this.xpEndurance = xpEndurance;
 		this.estAdmin = estAdmin;
 	}
 	public Joueur(String pseudo, String password) {
 		this(pseudo, password, 0, 0, 0, false);
 	}
 	
 	public static Joueur authenticate(String pseudo, String password) {
 		return find.where().eq("pseudo", pseudo)
 		                   .eq("password", password)
 		                   .findUnique();
 	}
 	
 	public void augmenterScore(Integer add) {
 		this.score += add;
 	}
 	
 	public int getProgression() {
 		int totalQuetes = Quete.find.findList().size();
		if(totalQuetes == 0) {
			return 100;
		}

 		int totalTerminee = 0;
 		
 		List<Seance> listeSeances = Seance.find.where().eq("joueur.pseudo", pseudo).findList();
 		for(Seance s : listeSeances) {
 			if(s.etat == Seance.Etat.TERMINEE ||
 			   s.etat == Seance.Etat.VALIDEE) {
 				totalTerminee++;
 			}
 		}
 		
 		return totalTerminee * 100 / totalQuetes;
 	}
 	
 	public String getAvatar() {
 		File file = new File("public/media/avatars/" + this.pseudo + ".png");
 		if(file.exists()) {
 			return "media/avatars/" + this.pseudo + ".png";
 		} else {
 			return "media/avatars/default.png";
 		}
 	}
 
 	public static List<Joueur> listByScore() {
 		return find.where().orderBy("score desc").findList();
 	}
 
 	public static List<Joueur> listByPseudo() {
 		return find.where().orderBy("pseudo").findList();
 	}
 	
 	public static Boolean exist(String pseudo) {
 		return find.byId(pseudo) != null;
 	}
 	
 	public static Boolean estAdmin(String pseudo) {
 		return find.byId(pseudo).estAdmin;
 	}
 	
 	public static Joueur create(String pseudo, String password) {
 		Joueur j = new Joueur(pseudo, password);
 		j.save();
 		
 		Seance.create(j, Quete.getQueteInitiale());
 		
 		return j;
 	}
 	
 	public static void update(String pseudo, Integer score,
 	                          Integer xpVit, Integer xpEnd) {
 		Joueur j = find.byId(pseudo);
 		j.score = score;
 		j.xpVitesse = xpVit;
 		j.xpEndurance = xpEnd;
 		j.save();
 	}
 	
 	public static void delete(Joueur j) {
 		Seance.deleteByJoueur(j.pseudo);
 		Ebean.delete(j);
 	}
 }
