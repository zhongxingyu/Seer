 package railsimulator.train;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import beans.*;
 
 import railsimulator.train.Bloquer;
 import dao.CantonDAO;
 import dao.FreinDAO;
 import dao.HibernateUtils;
 import dao.LigneDAO;
 import dao.PorteDAO;
 import dao.RailDAO;
 import dao.ReseauDAO;
 import dao.StationDAO;
 import dao.TrainDAO;
 import dao.TrainHoraireStationDAO;
 import dao.WagonDAO;
 
 public class TrainControler extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	public TrainControler() {
 		super();
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 	
 
 		String action = request.getParameter("action");
 		LigneDAO ligneDAO=new LigneDAO();
 		ReseauDAO reseauDAO = new ReseauDAO();
 		
 		if(action.equals("TestJavaScriptCode")){
 			String idLigne = request.getParameter("idLigne");
 			ligneDAO.getLigneByID(Integer.parseInt(idLigne));
 			List<Ligne> ligneList = ligneDAO.listerLigne();
 			if (ligneList.size()==0){
 				int idReseau = reseauDAO.createReseauReturnId("Test reseau");
 				Reseau reseau = reseauDAO.getReseauByID(idReseau);
 				ligneDAO.createLigne("Ligne Test", "test", reseau);
 				ligneList = ligneDAO.listerLigne();
 			}
 			
 			List<Station> listeStation = new StationDAO().listerStation();
 			List<Train> listeTrain = new TrainDAO().listerTrain();
 		
 			request.logout();
 			request.setAttribute("listeLigne", ligneList);
 			request.setAttribute("listeStation", listeStation);
 			request.setAttribute("listeTrain", listeTrain);
 			
 
 			this.getServletContext().getRequestDispatcher( "/WEB-INF/testVisuTrain.jsp").forward( request, response );
 
 		}
 
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		
 		String action = request.getParameter("action");
 		LigneDAO ligneDAO=new LigneDAO();
 		ReseauDAO reseauDAO = new ReseauDAO();
 		
 		//HttpSession session = request.getSession();
 		if (action.equals("purger")) {
 
 			Session se = null;
 			se = HibernateUtils.getSession();
 
 			Transaction t12 = se.beginTransaction();
 			Query create12 = se.createSQLQuery("delete from frein");
 			create12.executeUpdate();
 			t12.commit();
 			
 
 			Transaction t13 = se.beginTransaction();
 			Query create13 = se.createSQLQuery("delete from porte");
 			create13.executeUpdate();
 			t13.commit();
 
 
 			Transaction t14 = se.beginTransaction();
 			Query create14 = se.createSQLQuery("delete from wagon");
 			create14.executeUpdate();
 			t14.commit();
 			
 
 
 			Transaction t18 = se.beginTransaction();
 			Query create18 = se.createSQLQuery("delete from trainhorairestation");
 			create18.executeUpdate();
 			t18.commit();
 
 
 			Transaction t17 = se.beginTransaction();
 			Query create17=se.createSQLQuery("delete from parametrehoraire");
 			create17.executeUpdate();
 			t17.commit();
 
 			
 			Transaction t15 = se.beginTransaction();
 			Query create15 = se.createSQLQuery("delete from train");
 			create15.executeUpdate();
 			t15.commit();
 
 
 			Transaction t16 = se.beginTransaction();
 			Query create16 = se.createSQLQuery("delete from rail");
 			create16.executeUpdate();
 			t16.commit();
 			
 						
 
 			Transaction t10 = se.beginTransaction();
 			Query create10 = se.createSQLQuery("delete from canton");
 			create10.executeUpdate();
 			t10.commit();
 
 
 
 			Transaction t8 = se.beginTransaction();
 			Query create8 = se.createSQLQuery("delete from station");
 			create8.executeUpdate();
 			t8.commit();
 			
 			Transaction t2 = se.beginTransaction();
 			Query create2 = se.createSQLQuery("delete from ligne");
 			create2.executeUpdate();
 			t2.commit();
 			
 			
 			Transaction t7 = se.beginTransaction();
 			Query create7 = se.createSQLQuery("delete from reseau");
 			create7.executeUpdate();
 			t7.commit();
 			
 
 
 			se.close();
 			
 			request.logout();
 			this.getServletContext()
 					.getRequestDispatcher("/WEB-INF/visualiserTrain.jsp")
 					.forward(request, response);
 
 		}
 
 		if (action.equals("mock")) {
 			TrainDAO traindao = new TrainDAO();
 			WagonDAO wagondao = new WagonDAO();
 			PorteDAO portedao = new PorteDAO();
 			FreinDAO freindao = new FreinDAO();
 			TrainHoraireStationDAO trainHorairedao=new TrainHoraireStationDAO();
 			RailDAO rail_dao=new RailDAO();
 			Canton canton = new Canton();
 			Canton canton2 = new Canton();
 			Set<Wagon> listWagon = new HashSet<Wagon>();
 			Wagon wagon1 = new Wagon();
 			Wagon wagon2 = new Wagon();
 			Ligne ligne = new Ligne();
 			Ligne ligne2 = new Ligne();
 			Reseau reseau = new Reseau();
 			ReseauDAO reseau_dao = new ReseauDAO();
 			Set<Frein> listeFreins = new HashSet<Frein>();
 			Set<Porte> listePortes = new HashSet<Porte>();
 			Set<Frein> listeFreins2 = new HashSet<Frein>();
 			Set<Porte> listePortes2 = new HashSet<Porte>();
 			Frein frein1 = new Frein();
 			Frein frein2 = new Frein();
 			Frein frein3 = new Frein();
 			Frein frein4 = new Frein();
 			Porte porte1 = new Porte();
 			Porte porte2 = new Porte();
 			Porte porte3 = new Porte();
 			Porte porte4 = new Porte();
 
 			canton.setDistance(200);
 
 			frein1.setTemperature(90.5f);
 			// frein1.setWagon(wagon1);
 			frein2.setTemperature(100.5f);
 			frein3.setTemperature(12.5f);
 			frein4.setTemperature(93.5f);
 
 			porte1.setStatut(0);
 			porte2.setStatut(0);
 			porte3.setStatut(0);
 			porte4.setStatut(0);
 
 			listeFreins.add(frein1);
 			listeFreins.add(frein2);
 			listePortes.add(porte1);
 			listePortes.add(porte2);
 
 			listeFreins2.add(frein3);
 			listeFreins2.add(frein4);
 			listePortes2.add(porte3);
 			listePortes2.add(porte4);
 
 			wagon1.setListeFreins(listeFreins);
 			wagon1.setListePortes(listePortes);
 			wagon2.setListeFreins(listeFreins2);
 			wagon2.setListePortes(listePortes2);
 			listWagon.add(wagon1);
 			listWagon.add(wagon2);
 
 			//1 creation reseau
 			int idreseau = reseau_dao.createReseauReturnId("reseau");
 			reseau = reseau_dao.getReseauByID(idreseau);
 			
 			//2 creation ligne
 			LigneDAO ligne_dao = new LigneDAO();
 			int idligne = ligne_dao.createLigneReturnId("ligne 1", "Ligne1",reseau);
 			ligne = ligne_dao.getLigneByID(idligne);
 			int idligne2 =ligne_dao.createLigneReturnId("ligne 2", "Ligne2", reseau);
 			ligne2 = ligne_dao.getLigneByID(idligne2);
 
 			CantonDAO canton_dao = new CantonDAO();
 			Station station1 = new Station();
 			Station station2 = new Station();
 			Station station3 = new Station();
 			Station station4 = new Station();
 			Rail rail=new Rail();
 			Rail rail2=new Rail();
 
 			//3 creation stations
 			StationDAO station_dao = new StationDAO();
 			station1 = station_dao.getStationByID(station_dao
 					.createStationReturnId("station1", "station1", 50.2, 4.1,100,
 							ligne));
 			station2 = station_dao.getStationByID(station_dao
 					.createStationReturnId("station2", "station2", 50.2, 4.1,234,
 							ligne));
 			station3 = station_dao.getStationByID(station_dao
 					.createStationReturnId("station3", "station3", 50.2, 4.1,213,
 							ligne2));
 			station4 = station_dao.getStationByID(station_dao
 					.createStationReturnId("station4", "station4", 50.2, 4.1,245,
 							ligne2));
 
 			//4 creation canton
 			int idCanton = canton_dao.createCantonReturnId("canton1",
 					"canton1", station1, station2);
 			canton = canton_dao.getCantonByID(idCanton);
 			int idCanton2 = canton_dao.createCantonReturnId("canton2",
 					"canton2", station3, station4);
 			canton2 = canton_dao.getCantonByID(idCanton2);
 
 		
 			//5creation rail
 			int idRail=rail_dao.createRail("go");
 			System.out.println("idrail " +idRail);
 			rail = rail_dao.getRailByID(idRail);
 			System.out.println("rail" +rail.getIdRail());
 			int idRail2=rail_dao.createRail("back");
 			System.out.println("idrail2 " +idRail2);
 			rail2 = rail_dao.getRailByID(idRail2);
 			System.out.println("rail2" +rail2.getIdRail());
 			
 			//6 creation train
 			int idTrain = traindao.createTrain2(15,"place italie", canton, listWagon, ligne,rail);
 			Train train1 = traindao.getTrainByID(idTrain);
 			int idTrain2 = traindao.createTrain2(22,"bobigny", canton2, listWagon, ligne,rail2);
 			Train train2 = traindao.getTrainByID(idTrain2);
 
 			int idTrain3 = traindao.createTrain2(55,"porte dauphine", canton2, listWagon, ligne2,rail);
 			Train train3= traindao.getTrainByID(idTrain3);
 			int idTrain4 = traindao.createTrain2(60,"nation", canton, listWagon, ligne2,rail2);
 			Train train4 = traindao.getTrainByID(idTrain4);
 			List<Train> listeTrain = traindao.listerTrain();
 
 			for (Train train : listeTrain) {
 				System.out.println(train);
 			}
 
 			//7 creation wagon 6
 			Wagon wagon = wagondao.getWagonById(wagondao.createWagonReturnId(20,60,listePortes2, listeFreins2, train1));
 			Wagon wagonbis = wagondao.getWagonById(wagondao.createWagonReturnId(25,53,listePortes2, listeFreins2, train1));
 			Wagon wagon3 = wagondao.getWagonById(wagondao.createWagonReturnId(20,64,listePortes2, listeFreins2, train1));
 			Wagon wagon4 = wagondao.getWagonById(wagondao.createWagonReturnId(25,50,listePortes2, listeFreins2, train1));
 			Wagon wagon5 = wagondao.getWagonById(wagondao.createWagonReturnId(20,62,listePortes2, listeFreins2, train1));
 			
 
 			Wagon wagon_1 = wagondao.getWagonById(wagondao.createWagonReturnId(10,64,listePortes, listeFreins, train2));
 			Wagon wagon_2 = wagondao.getWagonById(wagondao.createWagonReturnId(15,63,listePortes, listeFreins, train2));
 			Wagon wagon_3 = wagondao.getWagonById(wagondao.createWagonReturnId(10,50,listePortes, listeFreins, train2));
 			Wagon wagon_4 = wagondao.getWagonById(wagondao.createWagonReturnId(15,56,listePortes, listeFreins, train2));
 			Wagon wagon_5 = wagondao.getWagonById(wagondao.createWagonReturnId(10,66,listePortes, listeFreins, train2));
 			
 
 
 			Wagon wagon_1_1 = wagondao.getWagonById(wagondao.createWagonReturnId(13,30,listePortes, listeFreins, train3));
 			Wagon wagon_2_1 = wagondao.getWagonById(wagondao.createWagonReturnId(14,40,listePortes, listeFreins, train3));
 			Wagon wagon_3_1 = wagondao.getWagonById(wagondao.createWagonReturnId(16,65,listePortes, listeFreins, train3));
 			Wagon wagon_4_1 = wagondao.getWagonById(wagondao.createWagonReturnId(17,40,listePortes, listeFreins, train3));
 			Wagon wagon_5_1 = wagondao.getWagonById(wagondao.createWagonReturnId(11,46,listePortes, listeFreins, train3));
 			
 			
 
 
 			Wagon wagon_1_2= wagondao.getWagonById(wagondao.createWagonReturnId(10,68,listePortes, listeFreins, train4));
 			Wagon wagon_2_2= wagondao.getWagonById(wagondao.createWagonReturnId(25,30,listePortes, listeFreins, train4));
 			Wagon wagon_3_2= wagondao.getWagonById(wagondao.createWagonReturnId(40,40,listePortes, listeFreins, train4));
 			Wagon wagon_4_2= wagondao.getWagonById(wagondao.createWagonReturnId(55,65,listePortes, listeFreins, train4));
 			Wagon wagon_5_2= wagondao.getWagonById(wagondao.createWagonReturnId(17,45,listePortes, listeFreins, train4));
 			
 			//8 creation porte =>6 portes par wagon
 			//----1erTRAIN------
 			portedao.createPorte(0, wagon);
 			portedao.createPorte(0, wagon);
 			portedao.createPorte(0, wagon);
 			portedao.createPorte(0, wagon);
 			portedao.createPorte(0, wagon);
 			portedao.createPorte(0, wagon);
 			
 			portedao.createPorte(0, wagonbis);
 			portedao.createPorte(0, wagonbis);
 			portedao.createPorte(0, wagonbis);
 			portedao.createPorte(0, wagonbis);
 			portedao.createPorte(0, wagonbis);
 			portedao.createPorte(0, wagonbis);
 			
 			portedao.createPorte(1, wagon3);
 			portedao.createPorte(1, wagon3);
 			portedao.createPorte(0, wagon3);
 			portedao.createPorte(0, wagon3);
 			portedao.createPorte(0, wagon3);
 			portedao.createPorte(0, wagon3);
 			
 			portedao.createPorte(0, wagon4);
 			portedao.createPorte(0, wagon4);
 			portedao.createPorte(0, wagon4);
 			portedao.createPorte(0, wagon4);
 			portedao.createPorte(0, wagon4);
 			portedao.createPorte(0, wagon4);
 			
 			portedao.createPorte(0, wagon5);
 			portedao.createPorte(0, wagon5);
 			portedao.createPorte(0, wagon5);
 			portedao.createPorte(0, wagon5);
 			portedao.createPorte(0, wagon5);
 			portedao.createPorte(0, wagon5);
 			portedao.createPorte(0, wagon5);
 			
 			//----2eme TRAIN------
 			portedao.createPorte(0, wagon_1);
 			portedao.createPorte(0, wagon_1);
 			portedao.createPorte(0, wagon_1);
 			portedao.createPorte(0, wagon_1);
 			portedao.createPorte(0, wagon_1);
 			portedao.createPorte(0, wagon_1);
 			
 			portedao.createPorte(0, wagon_2);
 			portedao.createPorte(0, wagon_2);
 			portedao.createPorte(0, wagon_2);
 			portedao.createPorte(0, wagon_2);
 			portedao.createPorte(0, wagon_2);
 			portedao.createPorte(0, wagon_2);
 			
 			portedao.createPorte(1, wagon_3);
 			portedao.createPorte(1, wagon_3);
 			portedao.createPorte(0, wagon_3);
 			portedao.createPorte(0, wagon_3);
 			portedao.createPorte(0, wagon_3);
 			portedao.createPorte(0, wagon_3);
 			
 			portedao.createPorte(0, wagon_4);
 			portedao.createPorte(0, wagon_4);
 			portedao.createPorte(0, wagon_4);
 			portedao.createPorte(1, wagon_4);
 			portedao.createPorte(1, wagon_4);
 			portedao.createPorte(0, wagon_4);
 			
 			portedao.createPorte(0, wagon_5);
 			portedao.createPorte(0, wagon_5);
 			portedao.createPorte(0, wagon_5);
 			portedao.createPorte(0, wagon_5);
 			portedao.createPorte(0, wagon_5);
 			portedao.createPorte(0, wagon_5);
 			portedao.createPorte(0, wagon_5);
 			
 			//----3eme TRAIN------
 			portedao.createPorte(0, wagon_1_1);
 			portedao.createPorte(0, wagon_1_1);
 			portedao.createPorte(0, wagon_1_1);
 			portedao.createPorte(0, wagon_1_1);
 			portedao.createPorte(0, wagon_1_1);
 			portedao.createPorte(0, wagon_1_1);
 			
 			portedao.createPorte(0, wagon_2_1);
 			portedao.createPorte(0, wagon_2_1);
 			portedao.createPorte(0, wagon_2_1);
 			portedao.createPorte(0, wagon_2_1);
 			portedao.createPorte(0, wagon_2_1);
 			portedao.createPorte(0, wagon_2_1);
 			
 			portedao.createPorte(1, wagon_3_1);
 			portedao.createPorte(1, wagon_3_1);
 			portedao.createPorte(0, wagon_3_1);
 			portedao.createPorte(0, wagon_3_1);
 			portedao.createPorte(0, wagon_3_1);
 			portedao.createPorte(0, wagon_3_1);
 			
 			portedao.createPorte(0, wagon_4_1);
 			portedao.createPorte(0, wagon_4_1);
 			portedao.createPorte(0, wagon_4_1);
 			portedao.createPorte(1, wagon_4_1);
 			portedao.createPorte(1, wagon_4_1);
 			portedao.createPorte(0, wagon_4_1);
 			
 			portedao.createPorte(0, wagon_5_1);
 			portedao.createPorte(0, wagon_5_1);
 			portedao.createPorte(0, wagon_5_1);
 			portedao.createPorte(0, wagon_5_1);
 			portedao.createPorte(0, wagon_5_1);
 			portedao.createPorte(0, wagon_5_1);
 			portedao.createPorte(0, wagon_5_1);
 			
 			//----4eme TRAIN------
 			portedao.createPorte(0, wagon_1_2);
 			portedao.createPorte(0, wagon_1_2);
 			portedao.createPorte(0, wagon_1_2);
 			portedao.createPorte(0, wagon_1_2);
 			portedao.createPorte(0, wagon_1_2);
 			portedao.createPorte(0, wagon_1_2);
 			
 			portedao.createPorte(0, wagon_2_2);
 			portedao.createPorte(0, wagon_2_2);
 			portedao.createPorte(0, wagon_2_2);
 			portedao.createPorte(0, wagon_2_2);
 			portedao.createPorte(0, wagon_2_2);
 			portedao.createPorte(0, wagon_2_2);
 			
 			portedao.createPorte(1, wagon_3_2);
 			portedao.createPorte(1, wagon_3_2);
 			portedao.createPorte(0, wagon_3_2);
 			portedao.createPorte(0, wagon_3_2);
 			portedao.createPorte(0, wagon_3_2);
 			portedao.createPorte(0, wagon_3_2);
 			
 			portedao.createPorte(0, wagon_4_2);
 			portedao.createPorte(0, wagon_4_2);
 			portedao.createPorte(0, wagon_4_2);
 			portedao.createPorte(1, wagon_4_2);
 			portedao.createPorte(1, wagon_4_2);
 			portedao.createPorte(0, wagon_4_2);
 			
 			portedao.createPorte(0, wagon_5_2);
 			portedao.createPorte(0, wagon_5_2);
 			portedao.createPorte(0, wagon_5_2);
 			portedao.createPorte(0, wagon_5_2);
 			portedao.createPorte(0, wagon_5_2);
 			portedao.createPorte(0, wagon_5_2);
 			portedao.createPorte(0, wagon_5_2);
 			
 			
 			//9 creation frein
 			freindao.createFrein(93.5f, wagon);
 			freindao.createFrein(100.5f, wagonbis);
 			freindao.createFrein(24.5f, wagon3);
 			freindao.createFrein(46.5f, wagon4);
 			freindao.createFrein(24.5f, wagon5);
 			
 
 			freindao.createFrein(93.5f, wagon_1);
 			freindao.createFrein(100.5f, wagon_2);
 			freindao.createFrein(24.5f, wagon_3);
 			freindao.createFrein(46.5f, wagon_4);
 			freindao.createFrein(24.5f, wagon_5);
 			
 
 			freindao.createFrein(93.5f, wagon_1_1);
 			freindao.createFrein(100.5f, wagon_2_1);
 			freindao.createFrein(24.5f, wagon_3_1);
 			freindao.createFrein(46.5f, wagon_4_1);
 			freindao.createFrein(24.5f, wagon_5_1);
 			
 
 			freindao.createFrein(93.5f, wagon_1_2);
 			freindao.createFrein(100.5f, wagon_2_2);
 			freindao.createFrein(24.5f, wagon_3_2);
 			freindao.createFrein(46.5f, wagon_4_2);
 			freindao.createFrein(24.5f, wagon_5_2);
 			
 			
 			//initialise le nb de wagon (champ) dans table train
 			int nbWagonT1= new WagonDAO().listerWagonCount(train1);
 			int nbWagonT2= new WagonDAO().listerWagonCount(train2);
 			int nbWagonT3= new WagonDAO().listerWagonCount(train3);
 			int nbWagonT4= new WagonDAO().listerWagonCount(train4);
 			
 			train1.setNombredewagon(nbWagonT1);
 			train2.setNombredewagon(nbWagonT2);
 			train3.setNombredewagon(nbWagonT3);
 			train4.setNombredewagon(nbWagonT4);
 			
 			traindao.modifierTrain(train1);
 			traindao.modifierTrain(train2);
 			traindao.modifierTrain(train3);
 			traindao.modifierTrain(train4);
 			
 			
 			//10 creation trainhorairestation
 			//=>train1
 			trainHorairedao.createTrainHoraire("06:00:00", "07:00:00", "08:00:00", station1, train1);
 			trainHorairedao.createTrainHoraire("06:03:42", "07:03:12", "08:03:12", station2, train1);
 			trainHorairedao.createTrainHoraire("06:06:16", "07:06:46", "09:06:46", station3, train1);
 			trainHorairedao.createTrainHoraire("06:09:50", "07:09:50", "22:09:50", station4, train1);
 			
 			//=>train2
 			trainHorairedao.createTrainHoraire("06:03:30", "07:35:00", "08:10:00", station1, train2);
 			trainHorairedao.createTrainHoraire("06:06:56", "07:38:26", "08:13:26", station2, train2);
 			trainHorairedao.createTrainHoraire("06:09:46", "07:41:16", "08:16:16", station3, train2);
 			trainHorairedao.createTrainHoraire("06:11:22", "07:44:22", "08:19:22", station4, train2);
 			
 			//=>train3
 			trainHorairedao.createTrainHoraire("06:00:00", "07:47:00", "08:33:00", station1, train3);
 			trainHorairedao.createTrainHoraire("06:03:26", "07:50:56", "08:36:56", station2, train3);
 			trainHorairedao.createTrainHoraire("06:06:50", "07:53:50", "08:39:50", station3, train3);
 			trainHorairedao.createTrainHoraire("06:09:52", "07:56:22", "08:42:22", station4, train3);
 			
 			//=>train4
 			trainHorairedao.createTrainHoraire("06:46:12", "08:23:12", "09:07:12", station1, train4);
 			trainHorairedao.createTrainHoraire("06:49:56", "08:26:26", "09:10:26", station2, train4);
 			trainHorairedao.createTrainHoraire("06:52:20", "08:29:50", "09:13:50", station3, train4);
 			trainHorairedao.createTrainHoraire("06:55:16", "08:32:46", "09:16:46", station4, train4);
 			
 			List<Ligne> listeLigne = new LigneDAO().listerLigne();
 			List<Station> listeStation = new StationDAO().listerStation();
 			request.setAttribute("listeTrain", listeTrain);			
 			request.setAttribute("listeLigne", listeLigne);
 			request.setAttribute("listeStation", listeStation);
 
 			request.logout();
 			this.getServletContext()
 					.getRequestDispatcher("/WEB-INF/visualiserTrain.jsp")
 					.forward(request, response);
 
 		}// fin if train
 
 		if (action.equals("visualiserTrain")) {
 			List<Ligne> listeLigne = new LigneDAO().listerLigne();
 			List<Station> listeStation = new StationDAO().listerStation();
 			List<Train> listeTrain = new TrainDAO().listerTrain();
 		
 			request.setAttribute("listeLigne", listeLigne);
 			request.setAttribute("listeStation", listeStation);
 			request.setAttribute("listeTrain", listeTrain);
 			
 
 			request.logout();
 			this.getServletContext()
 					.getRequestDispatcher("/WEB-INF/visualiserTrain.jsp")
 					.forward(request, response);
 
 		}// fin if visualiserTrain
 		
 		if (action.equals("VisualisationParStation")){
 			
 			String idLigne = request.getParameter("idLigne");
 			String idStation = request.getParameter("idStation");
 	
 			
 			
 			List<Station> listeStationByLigne = new StationDAO().listerStationByLigne(Integer.parseInt(idLigne));
 			
 			Bloquer.lock(idStation);
 			System.out.println("System.out.println(idStation)1;" +idStation);
 		
 			request.setAttribute("listeStationByLigne", listeStationByLigne);
 			request.setAttribute("idStation", idStation);
 		
 			
 			request.logout();
 			this.getServletContext()
 					.getRequestDispatcher("/WEB-INF/visuParStation.jsp")
 					.forward(request, response);
 		
 				
 		
 		}//fin VisualisationParStation
 
 		if (action.equals("VisualisationParTrain")){
 			
 			
 			String idLigne = request.getParameter("idLigne");
 			String idTrain = request.getParameter("idTrain");
 	
 			
 			
 			List<Train> listeTrainByLigne = new TrainDAO().listerTrainByLigne(Integer.parseInt(idLigne));
 			List<Wagon> listeWagon=new WagonDAO().listerWagonByIdTrain(Integer.parseInt(idTrain));
 			List<Porte> listePortes=new PorteDAO().listerPorte();
 			List<Frein> listeFreins=new FreinDAO().listerFrein();
 			List<TrainHoraireStation> listeTrainHoraireStation=new TrainHoraireStationDAO().listTrainHoraireStationById();
 			
 			
 			if(listeWagon.isEmpty()){
 
 				List<String> errorsMessage = new ArrayList<String>();
 				errorsMessage.add("Il n'y a pas de Wagon pr ce train");
 				request.setAttribute("errorsMessage", errorsMessage);
 				
 			}
 
 			
 			
 			Bloquer.lock(idTrain);
 			
 		
 			request.setAttribute("listeTrainByLigne", listeTrainByLigne);
 			request.setAttribute("idTrain", idTrain);
 			request.setAttribute("listeWagon", listeWagon);
 			request.setAttribute("listePortes", listePortes);
 			request.setAttribute("listeFreins", listeFreins);
 			request.setAttribute("listeTrainHoraireStation", listeTrainHoraireStation);
 			
 			
 			request.logout();
 			this.getServletContext()
 					.getRequestDispatcher("/WEB-INF/visuParTrain.jsp")
 					.forward(request, response);
 		
 			
 		}//fin VisualisationParTrain
 		
 
 		if (action.equals("deconnexionTrain")){
 			String idTrain = request.getParameter("idTrain");
 			
 			Bloquer.unlock(idTrain);
 			
 
 			List<Ligne> listeLigne = new LigneDAO().listerLigne();
 			List<Station> listeStation = new StationDAO().listerStation();
 			List<Train> listeTrain = new TrainDAO().listerTrain();
 			
 			request.setAttribute("listeLigne", listeLigne);
 			request.setAttribute("listeStation", listeStation);
 			request.setAttribute("listeTrain", listeTrain);
 			request.logout();
 			this.getServletContext()
 					.getRequestDispatcher("/WEB-INF/visualiserTrain.jsp")
 					.forward(request, response);
 		
 			
 		}//fin deconnexionTrain
 
 
 		if (action.equals("deconnexionStation")){
 			String idStation = request.getParameter("idStation");
 			
 			Bloquer.unlock(idStation);
 			
 
 			List<Ligne> listeLigne = new LigneDAO().listerLigne();
 			List<Station> listeStation = new StationDAO().listerStation();
 			List<Train> listeTrain = new TrainDAO().listerTrain();
 			
 			request.setAttribute("listeLigne", listeLigne);
 			request.setAttribute("listeStation", listeStation);
 			request.setAttribute("listeTrain", listeTrain);
 			request.logout();
 			this.getServletContext()
 					.getRequestDispatcher("/WEB-INF/visualiserTrain.jsp")
 					.forward(request, response);
 		
 			
 		}//fin deconnexion
 		
 
 		if(action.equals("TestJavaScriptCode")){
 			
 			List<Ligne> ligneList = ligneDAO.listerLigne();
 			if (ligneList.size()==0){
 				int idReseau = reseauDAO.createReseauReturnId("Test reseau");
 				Reseau reseau = reseauDAO.getReseauByID(idReseau);
 				ligneDAO.createLigne("Ligne Test", "test", reseau);
 				ligneList = ligneDAO.listerLigne();
 			}
 			
 			List<Station> listeStation = new StationDAO().listerStation();
 			List<Train> listeTrain = new TrainDAO().listerTrain();
 		
 			request.logout();
 			request.setAttribute("listeLigne", ligneList);
 			request.setAttribute("listeStation", listeStation);
 			request.setAttribute("listeTrain", listeTrain);
 			
 
 			this.getServletContext().getRequestDispatcher( "/WEB-INF/testVisuTrain.jsp").forward( request, response );
 
 		}
 
 		if(action.equals("testIntegration")){
 			
			//String idLigne=request.getParameter("idLigne").indexOf(1); //resulat _1faux
			//String idTrain=request.getParameter("idTrain").valueOf(1);
			//int idLigne = request.getParameter("idLigne").codePointAt(1);
			//String[] idLigne = request.getParameterValues("idLigne");
 			String idLigne = request.getParameter("idLigne");
 			String idTrain = request.getParameter("idTrain");
System.out.println("idLigne" +idLigne);
//System.out.println("idLigne2" +idLigne2);
System.out.println("idTrain" +idTrain);
 			
 			List<Train> listeTrainByLigne = new TrainDAO().listerTrainByLigne(Integer.parseInt(idLigne));
 			List<Wagon> listeWagon=new WagonDAO().listerWagonByIdTrain(Integer.parseInt(idTrain));
 			List<Porte> listePortes=new PorteDAO().listerPorte();
 			List<Frein> listeFreins=new FreinDAO().listerFrein();
 			List<TrainHoraireStation> listeTrainHoraireStation=new TrainHoraireStationDAO().listTrainHoraireStationById();
 			
 			
 			Bloquer.lock(idTrain);
 			
 			request.logout();
 			request.setAttribute("listeTrainByLigne", listeTrainByLigne);
 			request.setAttribute("idTrain", idTrain);
 			request.setAttribute("listeWagon", listeWagon);
 			request.setAttribute("listePortes", listePortes);
 			request.setAttribute("listeFreins", listeFreins);
 			request.setAttribute("listeTrainHoraireStation", listeTrainHoraireStation);
 			
 			this.getServletContext().getRequestDispatcher( "/WEB-INF/visuParTrainTest.jsp").forward( request, response );
 
 		}
		
		if (action.equals("deconnexionTrainTest")){
			String idTrain = request.getParameter("idTrain");
			
			Bloquer.unlock(idTrain);
			

			List<Ligne> listeLigne = new LigneDAO().listerLigne();
			List<Station> listeStation = new StationDAO().listerStation();
			List<Train> listeTrain = new TrainDAO().listerTrain();
			
			request.setAttribute("listeLigne", listeLigne);
			request.setAttribute("listeStation", listeStation);
			request.setAttribute("listeTrain", listeTrain);
			request.logout();
			this.getServletContext()
					.getRequestDispatcher("/WEB-INF/testVisuTrain.jsp")
					.forward(request, response);
		
			
		}//fin deconnexionTrain
 
		
 
 	}//fin Get
 
 }
