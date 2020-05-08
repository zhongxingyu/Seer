 package railsimulator.tools;
 import java.util.ArrayList;   
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Session;
 
 import beans.AbonnementMany;
 import beans.Client;
 import beans.ClientPassTourniquet;
 import beans.Tourniquet;
 import dao.AbonnementManyDAO;
 import dao.ClientDAO;
 import dao.HibernateUtils;
 import dao.TourniquetDAO;
 import beans.ZoneAbo;
 import beans.ClientPassTourniquet;
 
 public class PassTourniquet {
 	
 	private Client client=new Client();
 	private Tourniquet tour ;
 	private AbonnementMany aboM ;
 	private List<ZoneAbo> zones = new ArrayList<ZoneAbo>();
 	private boolean verif = false;
 	private ClientPassTourniquet clientPassT;
 	private String msg = "";
 	private int idtransac;
 	
 	public PassTourniquet() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public Boolean PassTourniquet(Tourniquet tourniquet, AbonnementMany abo,int idtransaction){
 		//tour=new Tourniquet();
 		clientPassT = new ClientPassTourniquet();
 		aboM = new AbonnementMany();
 		tour = new Tourniquet();
 		tour = tourniquet;
 		Session session = HibernateUtils.getSession();
 		Session session2 = HibernateUtils.getSession();
 		aboM = abo;
 		idtransac = idtransaction;
 		System.out.println("Algo");
 		System.out.println("tour.getNum "+tour.getZoneAbo().getNumZone());
 		System.out.println("aboM.getClient "+aboM.getIdclient().getId());
 		for(int i=0;i<aboM.getZoneAbo().size();i++){
 			
 			System.out.println("boucle "+aboM.getZoneAbo().get(i).getId());
 			if(tour.getZoneAbo().getNumZone().equals(aboM.getZoneAbo().get(i).getNumZone())){
 				System.out.println("passage ok");
 //				System.out.println("tourniquet "+tour.getZoneAbo().getNumZone());
 //				System.out.println("abo zone "+aboM.getZoneAbo().get(i).getNumZone()+" id abo "+aboM.getId());
 //				System.out.println(aboM.getZoneAbo().contains(tour.getZoneAbo().getNumZone()));
 				verif = true;
 				break;
 			}
 			
 			else{
 				System.out.println("passage pas ok");
 				verif=false;
 				msg = "zone non autoris";
 //				System.out.println(aboM.getZoneAbo().contains(tour.getZoneAbo().getNumZone()));
 //				System.out.println("tourniquet "+tour.getZoneAbo().getNumZone());
 //				System.out.println("abo "+aboM.getZoneAbo().get(i).getNumZone());
 //				System.out.println("abo zone "+aboM.getZoneAbo().get(i).getNumZone()+" id abo "+aboM.getId());
 				//System.out.println("zone tourniquet "+tour.getZoneAbo().getNumZone());
 			}
 		}
 		//System.out.println("VERIF = "+verif);
 		Date dateClient =new Date();
 		Date today = new Date();
 		today.getTime();
 		dateClient = aboM.getIdclient().getDateFinAbo();
 	//	System.out.println("date now "+today);
 		//System.out.println("date fin ABo = "+dateClient);
 		long dif = dateClient.getTime() - today.getTime();
 		if(dif<0){
 			verif = false;
 			msg = "abonnement expir ";
 //			
 		}
 //		System.out.println("dif = "+dif);
 		if(verif==true){
 			
 			System.out.println("verif == true");
 			
 			Date date = new Date();
 			date.getTime();
 			System.out.println("avant new ClientpassTou");
 			clientPassT = new ClientPassTourniquet();
 			
 			clientPassT.setClient(abo.getIdclient());
 			System.out.println("clientPassT.abo "+clientPassT.getClient().getNom());
 			clientPassT.setTourniquet(tour);
 			System.out.println("clientPAssT.tour "+clientPassT.getTourniquet().getIdtourniquet());
 			tour.getClientPass();
 			System.out.println("tour size "+tour.getClientPass().size());
 			clientPassT.setCreated(date);
 			clientPassT.setIdtransaction(idtransac);
 			clientPassT.setValidation("accs autoris ");
 			System.out.println("zone tourniquet "+tour.getZoneAbo().getNumZone());
 			System.out.println(clientPassT.getClient().getNom());
 			session = HibernateUtils.getSession();
 			session.beginTransaction();
			//session.save(tour);
 			session.save(clientPassT);
 			System.out.println("id transaction "+clientPassT.getIdtransaction());
 			session.getTransaction().commit();
 		//	System.out.println("id transaction "+clientPassT.getIdtransaction());
 			session.close();
 			System.out.println("insert verif=true ");
 			
 		}
 		else{
 			verif=false;
 		//	System.out.println("passage pas ok");
 			//System.out.println(aboM.getZoneAbo().contains(tour.getZoneAbo().getNumZone()));
 			//System.out.println("Else tourniquet "+tour.getZoneAbo().getNumZone());
 //		
 		//	System.out.println(" Else zone tourniquet "+tour.getZoneAbo().getNumZone());
 			Date date = new Date();
 			date.getTime();
 			//session.clear();
 			Tourniquet tour2 = new Tourniquet();
 			tour2=tourniquet;
 		//	clientPassT = new ClientPassTourniquet();
 			clientPassT.setClient(abo.getIdclient());
 			clientPassT.setTourniquet(tour2);
 			//System.out.println(tourniquet.getClientPass().get(0));
 			//System.out.println("tour size "+tour.getClientPass().size());
 			tour2.getClientPass();
 			clientPassT.setCreated(date);
 			clientPassT.setIdtransaction(idtransac);
 			clientPassT.setValidation("abonnement non valide "+msg);
 //			System.out.println("zone tourniquet "+tour.getZoneAbo().getNumZone());
 //			System.out.println(clientPassT.getClient().getNom());
 			System.out.println("insert verif=false");
 	//		System.out.println("get id client "+aboM.getIdclient().getId());
 			//session.evict(clientPassT); 
 			
 			
 			
 		
 		session2 = HibernateUtils.getSession();
 		//session2.flush();
 			session2.beginTransaction();
 			//
 			
			session2.evict(tour); 
 			session2.save(tour2);
 			
 			session2.save(clientPassT);
 			session2.getTransaction().commit();
 			session2.close();
 			
 		}
 		
 		System.out.println("verif "+verif);
 		return verif;
 		
 		
 		
 	}
 	
 }
