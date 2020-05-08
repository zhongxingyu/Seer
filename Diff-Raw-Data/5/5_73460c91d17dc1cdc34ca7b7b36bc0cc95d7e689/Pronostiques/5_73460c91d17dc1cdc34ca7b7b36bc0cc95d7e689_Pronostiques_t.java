 package controllers;
 
 import java.util.Date;
 import java.util.List;
 
 import models.Journee;
 import models.Matche;
 import models.PointsJournee;
 import models.PointsSaison;
 import models.Pronostique;
 import models.Saison;
 import models.Sys_parameter;
 import models.Utilisateur;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security.Authenticated;
 import views.html.pronosticsForm;
 import views.html.index;
 import views.html.otherPronostics;
 
 @Authenticated(Secured.class)
 public class Pronostiques extends Controller  {
 
 	static Form<Pronostique> pronostiqueForm = form(Pronostique.class);
 	static Form<Journee> journeeForm = form(Journee.class);
 	static Form<Matche> matcheForm = form(Matche.class);
 	
 	private static void scoreCorrect(Utilisateur user, Matche match){
 		Sys_parameter system = Sys_parameter.find.byId((long) 1);
 		Saison saison = system.getSaisonEnCours();
 
 		Long idMatch = match.getId();
		Long idJournee = idMatch/10;
		if (idMatch%10 != 0) {
			idJournee++;
		}
 		Journee journee = Journee.find.where().eq("id", idJournee).findUnique();
 		
 		PointsJournee pointsJournee = PointsJournee.find.where().eq("journee", journee).eq("user", user).findUnique();
 		if(null!=pointsJournee) {
 			pointsJournee.ajouterPoints(15);
 			pointsJournee.incrementerScoresCorrects();
 		} else{
 			PointsJournee pointsJourneeNew = new PointsJournee();
 			pointsJourneeNew.user=user;
 			pointsJourneeNew.journee=journee;
 			pointsJourneeNew.points=15;
 			pointsJourneeNew.scoresCorrects=1;
 			pointsJourneeNew.save();
 		}
 		
 		PointsSaison pointsSaison = PointsSaison.find.where().eq("user", user).eq("saison", saison).findUnique();
 		if(null!=pointsSaison) {
 			pointsSaison.ajouterPointsTotalSaison(15);
 			pointsSaison.incrementerTotalScoresCorrects();
 		} else{
 			PointsSaison pointsSaisonNew = new PointsSaison();
 			pointsSaisonNew.user=user;
 			pointsSaisonNew.saison=saison;
 			pointsSaisonNew.nbFoisPremier=0;
 			pointsSaisonNew.pointsTotalSaison=15;
 			pointsSaisonNew.totalScoresCorrects=1;
 			pointsSaisonNew.save();
 		}
 	}
 	
 	private static void bonVainqueur(Utilisateur user, Matche match) {
 		Sys_parameter system = Sys_parameter.find.byId((long) 1);
 		Saison saison = system.getSaisonEnCours();
 
 		Long idMatch = match.getId();
 		Long idJournee = idMatch/10 + 1;
 		Journee journee = Journee.find.where().eq("id", idJournee).findUnique();
 		
 		PointsJournee pointsJournee = PointsJournee.find.where().eq("user", user).eq("journee", journee).findUnique();
 		if(null!=pointsJournee) {
 			pointsJournee.ajouterPoints(10);
 		} else{
 			PointsJournee pointsJourneeNew = new PointsJournee();
 			pointsJourneeNew.user=user;
 			pointsJourneeNew.journee=journee;
 			pointsJourneeNew.points=10;
 			pointsJourneeNew.scoresCorrects=0;
 			pointsJourneeNew.save();
 		}
 		
 		PointsSaison pointsSaison = PointsSaison.find.where().eq("user", user).eq("saison", saison).findUnique();
 		if(null!=pointsSaison) {
 			pointsSaison.ajouterPointsTotalSaison(10);
 		} else{
 			PointsSaison pointsSaisonNew = new PointsSaison();
 			pointsSaisonNew.user=user;
 			pointsSaisonNew.saison=saison;
 			pointsSaisonNew.nbFoisPremier=0;
 			pointsSaisonNew.pointsTotalSaison=10;
 			pointsSaisonNew.totalScoresCorrects=0;
 			pointsSaisonNew.save();
 		}
 	}
 	
 	public static Result calculPoints() {
 		Sys_parameter system = Sys_parameter.find.byId((long) 1);
 		Saison saison = system.getSaisonEnCours();
 		
 		Utilisateur user = Utilisateur.findByPseudo(request().username());
 		List<Utilisateur> utilisateurs = Utilisateur.find.all();
 		List<Matche> matches = Matche.find.where().isNotNull("scoreEquipe1").findList();
 		
 		for (Matche match : matches) {
 				List<Pronostique> pronostiques = Pronostique.find.where().eq("matche",match).eq("calcule",false).findList();
 				if(null!=pronostiques) {
 					for (Pronostique prono : pronostiques) {
 						Utilisateur utilisateur = prono.utilisateur;
 						if(prono.getVainqueur() == prono.getMatche().getVainqueur()) {
 							if( (prono.getPronoEquipe1() == prono.getMatche().getScoreEquipe1()) && (prono.getPronoEquipe2() == prono.getMatche().getScoreEquipe2()) ) {
 								scoreCorrect(utilisateur, match);
 							} else {
 								bonVainqueur(utilisateur, match);
 							}
 							
 						}
 						prono.setCalcule(true);
 						prono.update();
 					}
 				}
 		}
 		
 		List<Journee> journees = Journee.find.where().eq("calcule", 0).findList();
 		
 		for (Journee journee : journees) {
 			List<PointsJournee> pointsJournees = PointsJournee.find.where().eq("journee", journee).orderBy().desc("points").findList();
 			if (!pointsJournees.isEmpty()) {
 				Utilisateur premier = pointsJournees.get(0).getUser();
 				Integer scorePremier = pointsJournees.get(0).getPoints();
 				
 				PointsSaison pointsSaison = PointsSaison.find.where().eq("user", premier).eq("saison", saison).findUnique();
 				pointsSaison.incrementerNbFoisPremier();
 				Integer i = 1;
 				
 				while (pointsJournees.size()!=i && pointsJournees.get(i).getPoints()==scorePremier) {
 					Utilisateur egalite = pointsJournees.get(i).getUser();
 					
 					PointsSaison pointsSaisonEgalite = PointsSaison.find.where().eq("user", egalite).eq("saison", saison).findUnique();
 					pointsSaison.incrementerNbFoisPremier();
 					
 					i++;
 				}
 				
 			}
 			journee.setCalcule(true);
 			journee.update();
 		}
 		
 		
 		return redirect(
 				routes.Application.index()
 		);
 			
 	}
 		
 	
 	public static Result pronostics(String id) {
 		Long idLong;
 		if (id.equalsIgnoreCase("0")) {
 			System.out.println("Id vaut 0");
 			Date maintenant = new Date();
 			
 			List<Journee> journeesFutures = Journee.find.where().gt("dateJournee", maintenant).orderBy().asc("dateJournee").findList();
 			if(!journeesFutures.isEmpty()){
 				idLong = journeesFutures.get(0).id;
 			} else {
 				List<Journee> journees = Journee.find.orderBy().desc("dateJournee").findList();
 				idLong = journees.get(0).id;
 			}
 		} else {
 			idLong = Long.parseLong(id);
 		}
 		Utilisateur user = Utilisateur.findByPseudo(request().username());
 		
 		Sys_parameter system = Sys_parameter.find.byId((long) 1);
 		Saison saison = system.getSaisonEnCours();
 		Journee premiereJournee = saison.getPremiereJournee();
 		Journee derniereJournee= saison.getDerniereJournee();
 		
 		List<Journee> journees = Journee.find.where().le("id", derniereJournee.getId()).ge("id", premiereJournee.getId()).findList();
 		Journee journee = Journee.find.byId(idLong);
 		List<Utilisateur> users = Utilisateur.find.orderBy().desc("points").findList();
 		
 		List<Matche> matches = Journee.findMatchesById(idLong);
 		
 		Integer points = PointsSaison.find.where().eq("saison", saison).eq("user", user).findList().get(0).pointsTotalSaison;
 		
 		List<PointsSaison> pointsSaisons = PointsSaison.find.where().eq("saison", saison).orderBy().desc("pointsTotalSaison").findList();
 		
 		PointsJournee pointsJournee = PointsJournee.find.where().eq("user", user).eq("journee", journee).findUnique();
 		
 		List<Pronostique> pronostiques = Pronostique.find.where().eq("utilisateur",user).findList();
 		pronostiqueForm = form(Pronostique.class);
 		return ok(
 		pronosticsForm.render(matches,pronostiques,pronostiqueForm,user,pointsSaisons,journees,journee,points, pointsJournee)
 		);
 	}
   
 	public static Result save(String idMatche) {
 		Form<Pronostique> filledForm = pronostiqueForm.bindFromRequest();
 		if(filledForm.hasErrors()) {
 			return redirect(
 					routes.Application.index()
 			);
 		} else {
 			Date maintenant = new Date();
 			
 			Pronostique test = filledForm.get();
 			
 			Utilisateur user = Utilisateur.findByPseudo(request().username());
 			test.setUtilisateur(user);
 			
 			Matche matche = Matche.findById(Long.parseLong(idMatche));
 			test.setMatche(matche);
 			
 			test.setCalcule(false);
 			
 			if(test.getPronoEquipe1() > test.getPronoEquipe2()) {
 				test.setVainqueur(matche.getEquipe1());
 			}else if((test.getPronoEquipe1() < test.getPronoEquipe2())) {
 				test.setVainqueur(matche.getEquipe2());
 			}else {
 				test.setVainqueur(null);
 			}
 			
 			// récupération de la journée en cours
 			String idJournee = "0";
 			List<Journee> journees = Journee.find.where().gt("dateJournee", maintenant).findList();
 			for (Journee journee : journees) {
 				List<Matche> matches = journee.findMatchesById(journee.id);
 				if (matches.contains(matche)) {
 					idJournee = journee.id.toString();
 				}
 			}
 			
 			List<Pronostique> pronostic = Pronostique.find.where().eq("utilisateur", user).eq("matche", matche).findList();
 			
 			if(maintenant.before(matche.dateMatche)){
 				if(pronostic.isEmpty())
 				{
 					Pronostique.create(test);
 				} else {
 					test.id = pronostic.get(0).getId();
 					Pronostique.update(test);
 				}
 				return redirect(routes.Pronostiques.pronostics(idJournee));
 			} else {
 				return redirect(
 						routes.Application.index()
 				);
 			}
 		}
 	}
 	
 	public static Result otherPronostics(String pseudoUser, String idJournee) {
 		Long idLong;
 		
 		Date maintenant = new Date();
 		Sys_parameter system = Sys_parameter.find.byId((long) 1);
 		Saison saison = system.getSaisonEnCours();
 		Journee premiereJournee = saison.getPremiereJournee();
 		Journee derniereJournee= saison.getDerniereJournee();
 		
 		List<Journee> journeesPassees = Journee.find.where().le("id", derniereJournee.getId()).ge("id", premiereJournee.getId()).lt("dateJournee", maintenant).orderBy().desc("dateJournee").findList();
 		
 		if (idJournee.equalsIgnoreCase("0")) {
 			idLong = journeesPassees.get(0).id;
 		} else {
 			idLong = Long.parseLong(idJournee);
 		}
 		Utilisateur user = Utilisateur.findByPseudo(request().username());
 
 		Journee journee = Journee.find.byId(idLong);
 		
 		Utilisateur userSelectionne = Utilisateur.findByPseudo(pseudoUser);
 		
 		Integer points = PointsSaison.find.where().eq("saison", saison).eq("user", user).findList().get(0).pointsTotalSaison;
 		
 		List<Matche> matches = Journee.findMatchesById(idLong);
 		
 		List<PointsSaison> pointsSaisons = PointsSaison.find.where().eq("saison", saison).orderBy().desc("pointsTotalSaison").findList();
 
 		PointsJournee pointsJournee = PointsJournee.find.where().eq("user", userSelectionne).eq("journee", journee).findUnique();
 		
 		List<Pronostique> pronostiques = Pronostique.find.where().eq("utilisateur",userSelectionne).findList();
 		return ok(
 		otherPronostics.render(matches,pronostiques,user,userSelectionne,pointsSaisons,journeesPassees,journee,points,pointsJournee)
 		);
 	}
 	  
 	public static Result deletePronostique(Long id) {
 		return TODO;
 	}
 
 }
