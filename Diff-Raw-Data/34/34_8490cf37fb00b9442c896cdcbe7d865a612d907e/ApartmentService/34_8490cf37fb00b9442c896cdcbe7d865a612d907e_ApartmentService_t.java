 package com.hansson.rento;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.apartments.blekinge.Hermanssonbolagen;
 import com.hansson.rento.apartments.blekinge.TrossoWamoFastigheter;
 import com.hansson.rento.apartments.blekinge.karlshamn.Karlshamnsbostader;
 import com.hansson.rento.apartments.blekinge.karlshamn.KjellsonsSkogOchFastighetsForvaltning;
 import com.hansson.rento.apartments.blekinge.karlshamn.StrandbergsFastigheter;
 import com.hansson.rento.apartments.blekinge.karlshamn.ThernstromsForvaltning;
 import com.hansson.rento.apartments.blekinge.karlskrona.BengtAkessonFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.HansAkessonFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.KSFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.Karlskronahem;
 import com.hansson.rento.apartments.blekinge.karlskrona.KarlskronahemStudent;
 import com.hansson.rento.apartments.blekinge.karlskrona.LindebergFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.MagistratusFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.PBAStudent;
 import com.hansson.rento.apartments.blekinge.karlskrona.Utklippan;
 import com.hansson.rento.apartments.multiple.Akelius;
 import com.hansson.rento.apartments.multiple.CAFastigheter;
 import com.hansson.rento.apartments.multiple.HSBSydost;
 import com.hansson.rento.apartments.multiple.Heimstaden;
 import com.hansson.rento.apartments.multiple.Krebo;
 import com.hansson.rento.apartments.multiple.M2Gruppen;
 import com.hansson.rento.apartments.multiple.PBAFastigheter;
 import com.hansson.rento.apartments.multiple.SolvedalsForvaltning;
 import com.hansson.rento.apartments.multiple.SvenskaBostadsfonden;
 import com.hansson.rento.apartments.skane.BroBizForvaltningsAB;
 import com.hansson.rento.apartments.smaland.vaxjo.BoplatsVaxjo;
 import com.hansson.rento.apartments.smaland.vaxjo.BoplatsVaxjoStudent;
 import com.hansson.rento.dao.ApartmentDAO;
 import com.hansson.rento.entities.Apartment;
 
 @Service
 public class ApartmentService {
 
 	// * ********CAUTION********
 	// * Entering html scraping area.. prepare yourself for some nasty stuff!
 	// * ********CAUTION********
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 	private List<ApartmentsInterface> mLandlords = new LinkedList<ApartmentsInterface>() {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -2411798345463453006L;
 		// Add new implementations of the ApartmentsInterface here to include
 		// them in the scan loop
 		{

			add(new Akelius());
 			
 			//Växjö
 			add(new BoplatsVaxjo());
 			add(new BoplatsVaxjoStudent());
 			
 			//Karlshamn
 			add(new Hermanssonbolagen());
 //			add(new JnVonBergen());
 			add(new Karlshamnsbostader());
 			add(new KjellsonsSkogOchFastighetsForvaltning());
 			add(new SolvedalsForvaltning());
 			add(new StrandbergsFastigheter());
 			add(new ThernstromsForvaltning());
 			//Karlskrona
 			add(new BengtAkessonFastigheter());
 			add(new HansAkessonFastigheter());
 			add(new Karlskronahem());
 			add(new KarlskronahemStudent());
 			add(new KonradssonsFastigheter());
 			add(new KSFastigheter());
 			add(new LindebergFastigheter());
 			add(new MagistratusFastigheter());
 			add(new PBAStudent());
 			add(new TrossoWamoFastigheter());
 			add(new Utklippan());
 			//Multiple

 			add(new BroBizForvaltningsAB());
 			add(new CAFastigheter());
 			add(new Heimstaden());
 			add(new HSBSydost());
 			add(new Krebo());
			add(new M2Gruppen());
 			add(new PBAFastigheter());
 			add(new SvenskaBostadsfonden());
 		}
 	};
 
 	@Autowired
 	private ApartmentDAO mApartmentDAO;
 
 	// Every 4 hours
 	@Scheduled(fixedDelayString = "14400000")
 	public void updateApartmentList() {
 		for (ApartmentsInterface landlord : mLandlords) {
 			try {
 				List<Apartment> currentApartments = landlord.getAvailableApartments();
 				List<Apartment> storedApartments = mApartmentDAO.findAllByLandlord(landlord.getLandlord());
 
 				if (currentApartments == null) {
 					currentApartments = new LinkedList<Apartment>();
 				}
 				if (storedApartments == null) {
 					storedApartments = new LinkedList<Apartment>();
 				}
 
 				for (Apartment apartment : currentApartments) {
 					if (!storedApartments.contains(apartment)) {
 						mApartmentDAO.create(apartment);
 						mLog.info("Created: " + apartment);
 					}
 				}
 
 				for (Apartment apartment : storedApartments) {
 					if (!currentApartments.contains(apartment)) {
 						mApartmentDAO.delete(apartment);
 						mLog.info("Deleted: " + apartment);
 					}
 				}
 			} catch (Exception e) {
 				mLog.error("Error at: " + landlord.getLandlord());
 				e.printStackTrace();
 			}
 		}
 
 	}
 }
