 package eu.trentorise.smartcampus.vas.ifame.utils;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import jxl.Workbook;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDelGiorno;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDelMese;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDellaSettimana;
 import eu.trentorise.smartcampus.vas.ifame.model.Piatto;
 import eu.trentorise.smartcampus.vas.ifame.model.PiattoGiorno;
 import eu.trentorise.smartcampus.vas.ifame.repository.PiattoGiornoRepository;
 import eu.trentorise.smartcampus.vas.ifame.repository.PiattoRepository;
 
 public class GestoreMenu {
 
 	/**
 	 * prende dal primo giorno del mese fino all ultimo tutti i piatti e crea il
 	 * menu del mese. Prima fa una query sulla tabellla di relazione e poi
 	 * inerroga la tabella contenente i piatti
 	 */
 	public static MenuDelMese getMenuDelMese(
 			PiattoGiornoRepository piattoGiornoRepo,
 			PiattoRepository piattoRepo, int firstMonday, int lastDayInMonth) {
 
 		MenuDelMese mdm = new MenuDelMese();
 
 		ArrayList<MenuDellaSettimana> mdsList = new ArrayList<MenuDellaSettimana>();
 
 		// creo la prima settimana
 		MenuDellaSettimana firstWeekMenu = new MenuDellaSettimana();
 
 		// setto first e last
 		firstWeekMenu.setStart_day(1);
 		firstWeekMenu.setEnd_day(firstMonday - 1);
 
 		firstWeekMenu = getMenuDellaSettimana(piattoGiornoRepo, piattoRepo, 1,
 				firstMonday - 1);
 
 		mdsList.add(firstWeekMenu);
 
 		// ciclo di lunedi in lunedi
 		while (firstMonday < lastDayInMonth) {
 
 			int lastday = firstMonday + 6;
 
 			if (lastday > lastDayInMonth) {
 				lastday = lastDayInMonth;
 			}
 
 			mdsList.add(getMenuDellaSettimana(piattoGiornoRepo, piattoRepo,
 					firstMonday, lastday));
 
 			// add next days
 			firstMonday = firstMonday + 7;
 		}
 
 		mdm.setStart_day(1);
 		mdm.setEnd_day(lastDayInMonth);
 		mdm.setMenuDellaSettimana(mdsList);
 
 		return mdm;
 	}
 
 	/**
 	 * @param firstday
 	 *            of the week included
 	 * @param lastday
 	 *            of the week included
 	 * 
 	 */
 	private static MenuDellaSettimana getMenuDellaSettimana(
 			PiattoGiornoRepository piattoGiornoRepo,
 			PiattoRepository piattoRepo, int firstday, int lastday) {
 
 		ArrayList<MenuDelGiorno> mdgList = new ArrayList<MenuDelGiorno>();
 		MenuDellaSettimana weeklyMenu = new MenuDellaSettimana();
 		weeklyMenu.setStart_day(firstday);
 		weeklyMenu.setEnd_day(lastday);
 
 		for (int i = firstday; i <= lastday; i++) {
 
 			mdgList.add(getMenuDelGiorno(piattoGiornoRepo, piattoRepo, i));
 
 		}
 		weeklyMenu.setMenuDelGiorno(mdgList);
 
 		return weeklyMenu;
 	}
 
 	/**
 	 * prende la relazione dalla tabella dei menu e poi pesca i piatti dalla
 	 * tabella dei piatti e crea il menu
 	 */
 	public static MenuDelGiorno getMenuDelGiorno(
 			PiattoGiornoRepository piattoGiornoRepo,
 			PiattoRepository piattoRepository, int day) {
 
 		List<PiattoGiorno> piattoGiornoRelationList = piattoGiornoRepo
 				.getPiattiDelGiorno(day);
 
 		ArrayList<Piatto> piattiDelGiorno = new ArrayList<Piatto>();
 
 		for (PiattoGiorno piattoGiorno : piattoGiornoRelationList) {
 			piattiDelGiorno.add(piattoRepository.findOne(piattoGiorno
 					.getPiattoId()));
 		}
 
 		MenuDelGiorno mdg = new MenuDelGiorno();
 		mdg.setDay(day);
 		mdg.setPiattiDelGiorno(piattiDelGiorno);
 
 		return mdg;
 	}
 
 	/**
 	 * prende i piatti corrispondenti al giorno delle alternative
 	 */
 	public static List<Piatto> getAlternative(
 			PiattoGiornoRepository piattoGiornoRepo,
 			PiattoRepository piattoRepository) {
 
 		List<PiattoGiorno> alternativePiattoGiorno = piattoGiornoRepo
 				.getPiattiDelGiorno(ALTERNATIVE_DAY);
 
 		ArrayList<Piatto> piatti = new ArrayList<Piatto>();
 
 		for (PiattoGiorno piattoGiorno : alternativePiattoGiorno) {
 
 			Piatto piatto = piattoRepository
 					.findOne(piattoGiorno.getPiattoId());
 			piatti.add(piatto);
 		}
 
 		return piatti;
 	}
 
 	public static void inizializzazioneMenuDatabase(
 			PiattoGiornoRepository piattoGiornoRepo,
 			PiattoRepository piattoRepository, Workbook workbook) {
 
 		System.out.println("INFO: Aggiorno la tabella piatti");
 
 		updateTabellaPiatti(piattoRepository, workbook);
 
 		System.out
 				.println("INFO: Elimino e reindicizzo la tabella menu del mese");
 
 		inizializzaTabellaMenu(piattoGiornoRepo, piattoRepository, workbook);
 
 		System.out
 				.println("INFO: Elimino e reindicizzo la tabella alternative");
 
 		inizializzaTabellaAlternative(piattoGiornoRepo, piattoRepository,
 				workbook);
 
 		System.out.println("INFO: Fine procedura di inizializzazione database");
 	}
 
 	private static void updateTabellaPiatti(PiattoRepository piattoRepository,
 			Workbook workbook) {
 
 		// get the updated piatti and now do what you want
 		List<Piatto> piattiPresentiNelDb = piattoRepository.findAll();
 
 		// get the new piatti from the excel
 		MenuDelMese menuDelMese = NewMenuXlsUtil.getMenuDelMese(workbook);
 
 		// add to the set every single piatto
 		Set<Piatto> setPiatti = new HashSet<Piatto>();
 		for (MenuDellaSettimana mds : menuDelMese.getMenuDellaSettimana()) {
 			List<MenuDelGiorno> mdglist = mds.getMenuDelGiorno();
 			for (MenuDelGiorno mdg : mdglist) {
 				List<Piatto> piattiDelGiorno = mdg.getPiattiDelGiorno();
 				for (Piatto piatto : piattiDelGiorno) {
 
 					setPiatti.add(piatto);
 				}
 			}
 		}
 
 		// get the new alternative from the excel
 		List<Piatto> alternative = NewMenuXlsUtil.getAlternative(workbook);
 		// aggiungo le alternative al set di piatti
 		for (Piatto piatto : alternative) {
 
 			setPiatti.add(piatto);
 		}
 
 		int counterNuoviPiatti = 0;
 		// for each new piatto in the new menu check if it's in the db
 		for (Piatto nuovoPiatto : setPiatti) {
 			if (!piattiPresentiNelDb.contains(nuovoPiatto)) {
 
 				// is not in the db so save it
 				piattoRepository.saveAndFlush(nuovoPiatto);
 
 				// print the new Piatto inserted
 				System.out.println(++counterNuoviPiatti + ". "
 						+ nuovoPiatto.getPiatto_nome());
 			}
 		}
 		System.out
 				.println("INFO: Nuovi piatti inseriti: " + counterNuoviPiatti);
 	}
 
 	private static void inizializzaTabellaMenu(
 			PiattoGiornoRepository piattoGiornoRepo,
 			PiattoRepository piattoRepository, Workbook workbook) {
 
 		// delete always the old menu composition if present
 		piattoGiornoRepo.deleteAll();
 
 		// prendo tutti i piatti dal db
 		List<Piatto> piattiPresentiNelDb = piattoRepository.findAll();
 
 		int day = 1;
 
 		MenuDelMese mdm = NewMenuXlsUtil.getMenuDelMese(workbook);
 
 		for (MenuDellaSettimana mds : mdm.getMenuDellaSettimana()) {
 
 			List<MenuDelGiorno> mdglist = mds.getMenuDelGiorno();
 			for (MenuDelGiorno mdg : mdglist) {
 
 				List<Piatto> piattiDelGiorno = mdg.getPiattiDelGiorno();
 
 				int ordine = 1;
 				for (Piatto piatto : piattiDelGiorno) {
 					// questa condizione dovrebbe essere sempre soddisfatta
 					// perche prima li salvo nel db e poi cerco nel db i piatti
 					if (piattiPresentiNelDb.contains(piatto)) {
 
 						int indexOf = piattiPresentiNelDb.indexOf(piatto);
 						Piatto piattoDB = piattiPresentiNelDb.get(indexOf);
 
 						PiattoGiorno piattoGiorno = new PiattoGiorno();
 						piattoGiorno.setDay(day);
 						piattoGiorno.setPiattoId(piattoDB.getPiatto_id());
 						piattoGiorno.setOrdine(ordine++);
 
 						piattoGiornoRepo.saveAndFlush(piattoGiorno);
 
 						System.out.println("Menu: Giorno - " + day
 								+ " Piatto=[ ordine = "
 								+ piattoGiorno.getOrdine() + ", nome = "
 								+ piattoDB.getPiatto_nome() + " ]");
 					}
 				}
 				day++;
 			}
 		}
 
 	}
 
 	/** QUESTO NON VA MAI MODIFICATO */
 	private static final int ALTERNATIVE_DAY = 50;
 
 	/**
 	 * le alternative hanno un FINTO GIORNO che mi permette di prenderle senza
 	 * dover modificare la tabella piatto giorno. Gli setto questo giorno come
 	 * un giorno non del mese ovviamente
 	 */
 	private static void inizializzaTabellaAlternative(
 			PiattoGiornoRepository piattoGiornoRepo,
 			PiattoRepository piattoRepository, Workbook workbook) {
 
 		// elimino tutti i piatti alternative presenti
 		List<PiattoGiorno> lista = piattoGiornoRepo
 				.getPiattiDelGiorno(ALTERNATIVE_DAY);
 		piattoGiornoRepo.delete(lista);
 
 		// per sicurezza
 		piattoGiornoRepo.flush();
 
 		// prendo tutti i piatti dal db
 		List<Piatto> piattiPresentiNelDb = piattoRepository.findAll();
 
 		// get the new alternative from the excel
 		List<Piatto> alternative = NewMenuXlsUtil.getAlternative(workbook);
 		// aggiungo le alternative al set di piatti

		int ordine = 1;
 		for (Piatto piatto : alternative) {
 			// questa condizione dovrebbe essere sempre soddisfatta
 			// perche prima li salvo nel db e poi cerco nel db i piatti
 			if (piattiPresentiNelDb.contains(piatto)) {
 
 				int indexOf = piattiPresentiNelDb.indexOf(piatto);
 				Piatto piattoDB = piattiPresentiNelDb.get(indexOf);
 
 				PiattoGiorno piattoGiorno = new PiattoGiorno();
 
 				piattoGiorno.setDay(ALTERNATIVE_DAY);
 				piattoGiorno.setPiattoId(piattoDB.getPiatto_id());
				piattoGiorno.setOrdine(ordine++);
 
 				piattoGiornoRepo.saveAndFlush(piattoGiorno);
 
				System.out.println("Alternative: Piatto=[ ordine = "
						+ piattoGiorno.getOrdine() + ", nome = "
 						+ piattoDB.getPiatto_nome() + " ]");
 			}
 		}
 	}
 }
