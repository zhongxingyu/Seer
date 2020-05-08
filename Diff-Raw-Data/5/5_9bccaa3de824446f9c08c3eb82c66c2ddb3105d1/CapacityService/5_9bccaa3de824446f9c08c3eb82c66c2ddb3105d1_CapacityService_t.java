 /**
  * 
  */
 package scstool.proc;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import scstool.obj.BillOfMaterial;
 import scstool.obj.Material;
 import scstool.obj.WaitingList;
 import scstool.obj.WorkPlan;
 import scstool.obj.Workplace;
 import scstool.utils.Repository;
 
 /**
  * Kapazitätenplanung
  * 
  * @author reinhold
  * 
  */
 public class CapacityService {
 
 	final static Double FIRST_SHIFT_SALARY = 0.45;
 	final static Double SECOND_SHIFT_SALARY = 0.55;
 	final static Double THIRD_SHIFT_SALARY = 0.7;
 	final static Double OVERTIME_SALARY = 0.9;
 	final static Integer FIRST_SHIFT = 2400;
 	final static Integer FIRST_SHIFT_OVERTIME = 3600;
 	final static Integer SECOND_SHIFT = 4800;
 	final static Integer SECOND_SHIFT_OVERTIME = 6000;
 	final static Integer THIRD_SHIFT = 7200;
 
 	private static final Double RISIKO = risk();
 
 	/**
 	 * @return
 	 */
 	private static double risk() {
 		return new Double(
 				1 + (Repository.getInstance().getRiskPercente() / 100));
 	}
 
 	/**
 	 * Kalkuliert die benötigte Kapazität für einen Arbeitsplatz ohne die
 	 * Materialien in der Warteschlange.
 	 * 
 	 * @param workplace
 	 *            der Arbeitsplatz
 	 * @param productionProgram
 	 *            das Produktionsprogramm als Integer Array(Index 0 die ID,Index
 	 *            1 die Anzahl)
 	 * @return die benötigte Kapazität in Minuten mit Rüstzeit
 	 */
 	public static Integer calculateWorkplaceCapacity(Workplace workplace,
 			List<Integer[]> productionProgram) {
 
 		List<Material> done = new ArrayList<>();
 		Integer result = 0;
 
 		// suche in dem Produktionsprogramm
 		// prodMat ist ein Integer[] auf Index 0 ist die Material ID und auf
 		// Index 1 die produzierende Anzahl
 		for (Integer[] prodMat : productionProgram) {
 
 			// suche in den Arbeitsplänen für einen Arbeitsplatz
 			for (WorkPlan plan : workplace.getWorkplan()) {
 
 				// suche in den Stücklisteneinträgen in den Arbeitsplänen
 				for (BillOfMaterial bom : plan.getBillOfMaterial()) {
 					// nach dem zu fertigenden Material
 					Material component = bom.getComponent();
 					// wenn das fertigende Material in der Liste enthalten ist
 					// wurde es schon hinzugefügt
 					if (!done.contains(component)) {
 						// wenn das Material im Produktionsprogramm gleich sind
 						if (prodMat[0] == component.getId()) {
 							// addiere die produzierende Zeit
 							result += (plan.getProductionTime() * prodMat[1]);
 							// addiere die Rüstzeit
 							result += plan.getSetupTime();
 							done.add(component);
 						}
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Rechnet die noch einzurechnende Kapazität für einen Arbeitsplatz.
 	 * 
 	 * @param workplace
 	 * @return
 	 */
 	public static Integer calculateWaitingListCapacity(Workplace workplace) {
 
 		Integer result = 0;
 		Repository repo = Repository.getInstance();
 		for (WaitingList wl : repo.getInWork()) {
 			result += wl.getTimeneed();
 		}
 
 		return result;
 	}
 
 	public LinkedHashMap<Workplace, Integer[]> capaciting() {
 		DatabaseContentHandler dbch = DatabaseContentHandler.get();
 		List<Integer[]> productionProgram = Repository.getInstance()
 				.getProductionProgram();
 		LinkedHashMap<Workplace, Integer[]> result = new LinkedHashMap<>();
 		for (Workplace workplace : dbch.getAllWorkplaces()) {
 			Integer capacity = calculateWorkplaceCapacity(workplace,
 					productionProgram);
 			capacity += calculateWaitingListCapacity(workplace);
 			Integer[] resultList = calculateShift(workplace, capacity);
 			result.put(workplace, resultList);
 		}
 		return result;
 	}
 
 	/**
 	 * Berechnet die Schicht und die Überstunden pro Tag.<br/>
 	 * Dabei ist auf dem Index 0 die Schicht <br/>
 	 * und auf dem Index 1 die Überstunden.
 	 * 
 	 * @param workplace
 	 *            der Arbeitsplatz
 	 * @param capacity
 	 *            die Kapazität
 	 * @return zwei Integer Werte auf dem Index 0 die Schicht auf dem Index 1
 	 *         die Überstunden pro Tag
 	 */
 	public static Integer[] calculateShift(Workplace workplace, Integer capacity) {
 		Double costsSecondShift = Double.MAX_VALUE;
 		Double costsThirdShift = Double.MAX_VALUE;
 		Double costsFirstShift = Double.MAX_VALUE;
 
 		if (capacity < FIRST_SHIFT_OVERTIME) {
 			costsFirstShift = getCosts(workplace, capacity, FIRST_SHIFT,
 					FIRST_SHIFT_SALARY);
 		}
 
 		if (capacity < SECOND_SHIFT_OVERTIME) {
 			costsSecondShift = getCosts(workplace, capacity, SECOND_SHIFT,
 					SECOND_SHIFT_SALARY);
 		}
 
 		if (capacity < THIRD_SHIFT) {
 			costsThirdShift = getCosts(workplace, capacity, THIRD_SHIFT,
 					THIRD_SHIFT_SALARY);
 		}
 
 		return chooseShift(capacity, costsFirstShift, costsSecondShift,
 				costsThirdShift);
 	}
 
 	/**
 	 * Berechnet die Kosten einer Schicht auf einer<br/>
 	 * bestimmten Maschine mit der angegebenen Kapazität
 	 * 
 	 * @param workplace
 	 *            der Arbeitsplatz für die Maschinenkosten
 	 * @param capacity
 	 *            die Kapazität
 	 * @param shift
 	 *            die Minuten einer Schicht
 	 * @return die Kosten
 	 */
 	private static Double getCosts(Workplace workplace, Integer capacity,
 			final Integer shift, Double salary) {
 		Double costsShift;
 		// Lohnkosten für eine Schicht
 		double salaryForShift = salary * shift;
 		// Variable Maschinenkosten
 		double varCostforShift = (workplace.getVarMachineCosts() - workplace
 				.getFixMachineCosts()) * capacity;
 		// Fixe Maschinenkosten
 		double fixCostforShift = workplace.getFixMachineCosts() * shift;
 		costsShift = salaryForShift + varCostforShift + fixCostforShift;
 
 		// Überstundenbezahlung
 		if (capacity > shift) {
 			costsShift += OVERTIME_SALARY * (capacity - shift);
 		}
 		return costsShift;
 	}
 
 	/**
 	 * Algorithmus zum Auswählen der Schicht.
 	 * 
 	 * @param capacity
 	 *            die Kapazität
 	 * @param costsFirstShift
 	 *            Kosten der ersten Schicht
 	 * @param costsSecondShift
 	 *            Kosten der zweiten Schicht
 	 * @param costsThirdShift
 	 *            Kosten der dritten Schicht
 	 * @return zwei Integer Werte auf dem Index 0 die Schicht auf dem Index 1
 	 *         die Überstunden pro Tag
 	 */
 	private static Integer[] chooseShift(Integer capacity,
 			Double costsFirstShift, Double costsSecondShift,
 			Double costsThirdShift) {
 		Integer[] result = new Integer[3];
 
 		if (costsFirstShift < costsSecondShift
 				&& costsFirstShift < costsThirdShift) {
 			result[0] = 1;
 			if (capacity > FIRST_SHIFT) {
 				result[1] = (int) (((capacity - FIRST_SHIFT) / 5) * RISIKO);
 			} else {
 				result[1] = 0;
 			}
 		}
 
 		if (costsSecondShift < costsThirdShift
 				&& costsSecondShift < costsFirstShift) {
			result[0] = 2;
 			if (capacity > SECOND_SHIFT) {
 				result[1] = (int) (((capacity - SECOND_SHIFT) / 5) * RISIKO);
 			} else {
 				result[1] = 0;
 			}
 		}
 
 		if (costsThirdShift < costsFirstShift
 				&& costsThirdShift < costsSecondShift) {
			result[0] = 3;
 			if (capacity > THIRD_SHIFT) {
 				result[1] = (int) (((capacity - THIRD_SHIFT) / 5) * RISIKO);
 			} else {
 				result[1] = 0;
 			}
 		}
 		
 		result[2] = capacity;
 		return result;
 	}
 }
