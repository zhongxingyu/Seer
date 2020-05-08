 import java.awt.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.bson.types.ObjectId;
 
 
 public final class AlgoLegerBayes extends AlgoLeger {
 //test
 	private static AlgoLegerBayes singleton;
 	
 	private AlgoLegerBayes() {
 		super();
 		singleton= this ;
 	}
 	
 	public static AlgoLeger getAlgo(){
 		
 		if (singleton==null)
 		{
 			return new AlgoLegerBayes();
 		}
 		
 		return singleton;
 	}
 
 	@Override
 	public Recommendation answers(Request req) throws ExceptionRecoNotValid {
 		DataUserNode user;
 		if (req.getUrl()=="test" ) 
 		{
			//gn des Upages
 			//DataUPage jeanMichLeMonde= new DataUPage(new ObjectId(), 0.7, "www.lemonde.fr");
 			DataUPage jeanMichLeFigaro= new DataUPage(new ObjectId(), 0.8, "www.lefigaro.fr");
			DataUPage jeanMichLEquipe= new DataUPage(new ObjectId(), 0.5, "www.lquipe.fr");
 			DataUPage jeanMichLinux= new DataUPage(new ObjectId(), 0.1, "www.linux.org");
 			
 			DataUPage leGeekLinux= new DataUPage(new ObjectId(), 0.8, "www.linux.org");
 			DataUPage leGeekTechCrunch= new DataUPage(new ObjectId(), 0.95, "www.techcrunch.com");
 			DataUPage leGeekOpLib= new DataUPage(new ObjectId(), 0.6, "www.opinionlibre.fr");
 			DataUPage leGeekLeMonde= new DataUPage(new ObjectId(), 0.01, "www.lemonde.fr");
 			
 			DataUPage jeanJauresLeMonde= new DataUPage(new ObjectId(), 0.5, "www.lemonde.fr");
 			DataUPage jeanJauresLeFigaro= new DataUPage(new ObjectId(), 0.3, "www.lefigaro.fr");
			DataUPage jeanJauresLEquipe= new DataUPage(new ObjectId(), 0.6, "www.lquipe.fr");
			DataUPage jeanJauresLHuma= new DataUPage(new ObjectId(), 0.9, "www.lhumanit.fr");
 			
 			ArrayList<DataUPage> jeanMichUPage= new ArrayList<DataUPage>();
 			jeanMichUPage.add(jeanMichLinux);
 			jeanMichUPage.add(jeanMichLeFigaro);
 			//jeanMichUPage.add(jeanMichLeMonde);
 			jeanMichUPage.add(jeanMichLEquipe);
 			
 			ArrayList<DataUPage> leGeekUPage= new ArrayList<DataUPage>();
 			leGeekUPage.add(leGeekLinux);
 			leGeekUPage.add(leGeekOpLib);
 			leGeekUPage.add(leGeekTechCrunch);
 			leGeekUPage.add(leGeekLeMonde);
 			
 			ArrayList<DataUPage> jeanJauresUPage= new ArrayList<DataUPage>();
 			jeanJauresUPage.add(jeanJauresLeFigaro);
 			jeanJauresUPage.add(jeanJauresLeMonde);
 			jeanJauresUPage.add(jeanJauresLEquipe);
 			jeanJauresUPage.add(jeanJauresLHuma);
 			
			//on cre les persos
 			DataUserNode jeanMich = new DataUserNode("jeanMich", new ObjectId(), new ArrayList<DataUserRelation>()  , jeanMichUPage);
 			DataUserNode leGeek = new DataUserNode("leGeek", new ObjectId(), new ArrayList<DataUserRelation>()  , leGeekUPage);
 			DataUserNode jeanJaures = new DataUserNode("JeanJaures", new ObjectId(), new ArrayList<DataUserRelation>()  , jeanJauresUPage);
			//on implmente les liens d'amiti
 			DataUserRelation jmfriends = new DataUserRelation(leGeek);
 			DataUserRelation lgfriends = new DataUserRelation(jeanMich);
 			
 			ArrayList<DataUserRelation> jmUserRelations = new ArrayList<DataUserRelation>();
 			jmUserRelations.add(jmfriends);
 			jmUserRelations.add(new DataUserRelation(jeanJaures));
 			jeanMich.setFriends(jmUserRelations);
 			ArrayList<DataUserRelation> lgUserRelations = new ArrayList<DataUserRelation>();
 			lgUserRelations.add(lgfriends);
 			leGeek.setFriends(lgUserRelations);
 			
 			//on calcule les probas 
 			jeanMich.updateProbabilities();
 			leGeek.updateProbabilities();
 			
 			
 			
 			user = jeanMich;
 		}
 		else {
 			user = Interprete.db2DataUserNodeHard(req.getUserId()); // on reccupere l'utilisateur qui fait sa requete
 		}
 		
 		HashMap<String, ArrayList<Composite>> pages = new HashMap<String, ArrayList<Composite>>(); //on associe url-> avec un object qui contient un user et sa upage
 		for (DataUserRelation edge : user.getFriends()) {
 			for (DataUPage page: edge.friend.getUPages()) { //on parcourt toutes les pages des recommendeurs
 				if (pages.containsKey(page.getUrl())) { 
 					// pour les stocker en hashant grace a l'url de la page
 					pages.get(page.getUrl()).add(new Composite(edge.friend, page, edge.crossProbability));
 				} else {
 					ArrayList<Composite> tmp= new ArrayList<Composite>();
 					tmp.add(new Composite(edge.friend, page, edge.crossProbability));
 					pages.put(page.getUrl(), tmp);
 				}
 			}
 		}
 		
  		//on va supprimer toutes les pages qu'il a deja vu
 		for (DataUPage page : user.getUPages()) {
 			pages.remove(page.getUrl());
 		}
 		
 		TreeSet<Composite> bestReco = new TreeSet<Composite>(); //on stocke les trois meilleurs proba  
 		int nbReco=Math.min(10, pages.size());
 		
		for (int j=0; j<nbReco; j++) bestReco.add(new Composite(null,null,-1));
 		
 		//on va ensuite calculer toutes les probabilités 
 		for (ArrayList<AlgoLegerBayes.Composite> cc : pages.values()) { //il y a peut etre une optimisation a faire sur la facon dont on stocke et parcourt cette table de hashage
 			for(Composite c :cc) {
 				c.crossProbability =  c.crossProbability / c.user.uPageMean * c.page.pageRank;
 				System.out.println( c.page.getUrl()+" : " + c.crossProbability);
				bestReco.add(c);//TODO : IMPORTANT si deux pages ont la mme proba, on les crase!!!
 				bestReco.remove(bestReco.first());
 			}
 		}
 //		
 //		TreeMap<Double,String> bestsReco = new TreeMap<Double,String>(); //on stocke les trois meilleurs proba  
 //		for(int i=0;i<10;i++){bestsReco.put((double)0, "");}
 //		//on initialise à 3 meilleures reco, nombre qu'on maintient ensuite
//		//TODO : en faite non, on crase toujours la mme cl.
 //		
 //		//on va ensuite calculer toutes les probabilités 
 //		for (ArrayList<AlgoLegerBayes.Composite> cc : pages.values()) { //il y a peut etre une optimisation a faire sur la facon dont on stocke et parcourt cette table de hashage
 //			for(Composite c :cc) {
 //				double proba =  c.crossProbability / c.user.uPageMean * c.page.pageRank;
 //				System.out.println( c.page.getUrl()+" : " + proba);
//				bestsReco.put(proba, c.page.getUrl());//TODO : IMPORTANT si deux pages ont la mme proba, on les crase!!!
 //				bestsReco.remove(bestsReco.firstKey()); //on maintient seulement 3 meilleures
 //			}
 //		}
 		
 //		double sum=0;	
 //		for ( double proba : bestReco.descendingKeySet())
 //		{
 //			System.out.println( bestReco.get( proba)+" : " + proba);
 //			sum += proba;
 //		}
 //		double var= Math.random() * sum;
 //		Iterator<Double> probs = bestsReco.descendingKeySet().iterator();
 //		double bestKey = probs.next(); 
 //		while (probs.hasNext() && var-bestKey >0 )
 //		{
 //			bestKey = probs.next();
 //			var-=bestKey;
 //		}		
 //		
 		return new Recommendation(bestReco.last().toString());
 	}
 	
 	
 	public class Composite implements Comparable<Composite> {
 		DataUserNode user;
 		DataUPage page;
 		double crossProbability;
 		
 		public Composite(DataUserNode user, DataUPage page , double proba) {
 			this.user=user;
 			this.page =page;
 			this.crossProbability = proba; //la proba P(A inter B)
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			try {
 				Composite c = (Composite) obj;
 				return ( this.page.getMongoId()==c.page.getMongoId() && this.user.getMongoId()==c.user.getMongoId());
 			
 			}catch(Exception e){ return false;}
 			
 		}
 
 		@Override
 		public int compareTo(Composite arg0) {
 			if (this.crossProbability < arg0.crossProbability) return -1;
 			if (this.crossProbability > arg0.crossProbability) return 1;
 			//same proba;
 			return this.page.getUrl().compareTo(arg0.page.getUrl());
 		}
 
 		private AlgoLegerBayes getOuterType() {
 			return AlgoLegerBayes.this;
 		}
 	}
 	
 	
 	
 }
