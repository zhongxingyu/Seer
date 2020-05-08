 package scstool.proc;
 
 import scstool.obj.Disposition;
 import scstool.obj.Material;
 import scstool.obj.SellWish;
 import scstool.utils.Repository;
 /**
  * @Autor Alexander
  * 
  */
 /**
  * @author haeff
  *
  */
 public class DispositionService {
 	
 	//Disposition P1
 	public void QueueInput1(){
 			
 		/**
 		 *  Dispositionen für P1 setzten
 		 */
 		
 		//P1
 		setPMaterialDispo(1);
 		
 		//E26 und 51
 		setEMaterialDispo(1, 26, 1);
 		setEMaterialDispo(1, 51, 1);
 		
 		//E16, 17 und 50
 		setEMaterialDispo(1, 16, 51);
 		setEMaterialDispo(1, 17, 51);
 		setEMaterialDispo(1, 50, 51);
 		
 		//E4, 10 und 49
 		setEMaterialDispo(1, 4, 50);
 		setEMaterialDispo(1, 10, 50);
 		setEMaterialDispo(1, 49, 50);
 		
 		//E7, 13 und 18
 		setEMaterialDispo(1, 7, 49);
 		setEMaterialDispo(1, 13, 49);
 		setEMaterialDispo(1, 18, 49);
 	}
 		
 	//Disposition P2
 	public void QueueInput2(){
 		
 		/**
 		 *  Dispositionen für P2 setzten
 		 */
 		
 		//P2
 		setPMaterialDispo(2);
 		
 		//E26 und 56
 		setEMaterialDispo(2, 26, 2);
 		setEMaterialDispo(2, 56, 2);
 		
 		//E16, 17 und 55
 		setEMaterialDispo(2, 16, 56);
 		setEMaterialDispo(2, 17, 56);
 		setEMaterialDispo(2, 55, 56);
 		
 		//E5, 11 und 54
 		setEMaterialDispo(2, 5, 55);
 		setEMaterialDispo(2, 11, 55);
 		setEMaterialDispo(2, 54, 55);
 		
 		//E8, 14 und 19
 		setEMaterialDispo(2, 8, 54);
 		setEMaterialDispo(2, 14, 54);
 		setEMaterialDispo(2, 19, 54);
 				
 	}
 	
 	//Disposition P3
 	public void QueueInput3(){
 			
 		/**
 		 *  Dispositionen für P3 setzten
 		 */
 		
 		//P3
 		setPMaterialDispo(3);
 		
 		//E26 und 31
 		setEMaterialDispo(3, 26, 3);
 		setEMaterialDispo(3, 31, 3);
 		
 		//E16, 17 und 30
 		setEMaterialDispo(3, 16, 31);
 		setEMaterialDispo(3, 17, 31);
 		setEMaterialDispo(3, 30, 31);
 		
 		//E6, 12 und 29
 		setEMaterialDispo(3, 6, 30);
 		setEMaterialDispo(3, 12, 30);
 		setEMaterialDispo(3, 29, 30);
 		
 		//E8, 14 und 19
 		setEMaterialDispo(3, 9, 29);
 		setEMaterialDispo(3, 15, 29);
 		setEMaterialDispo(3, 20, 29);
 				
 	}
 
 	/**
 	 * Setzt die Dispoition des P-Materials
 	 * 
 	 * @param P-Material fuer Stueckliste
 	 * @param P-Material,E-Material fuer das einzele Material selbst
 	 */
 	private void setPMaterialDispo(int product)
 	{
 		Repository repo = Repository.getInstance();
 		DatabaseContentHandler dbch = DatabaseContentHandler.get();
 		
 		
		SellWish sellWish = repo.getSellWish(product);
 		Material mat = dbch.findMaterial(product);
 		
 		Disposition disposition = new Disposition();
 		
 		//Vertertriebswunsch
 		disposition.setSalesOrders(sellWish.getN());
 		
 		// Auftr�ge in der Warteschlange (untergeordnet) immer fixer Wert f�r erste Position
 		disposition.setWaitingQueue1(0);
 		
 		//Lagerbestand geplant
 		disposition.setSafetyWarehousestock(repo.getStafetyStock(product));
 		
 		//Lagerbestand Vorperiode
 		//TODO Startamount richtig?
 		disposition.setWarehousestockPassedPeriod(mat.getStartamount());
 		
 		// Warteschlange (uebergeordnet)
		disposition.setWaitingQueue2(repo.getAmountOfWaitingMaterial(product));
 		
 		// Auftraege in Bearbeitung
		disposition.setOrdersInProgress(repo.getAmountOfMaterialInWork(product));
 		
 		// Produktionsauftraege kommende Periode
 		int count = disposition.getSalesOrders() + 
 				    disposition.getWaitingQueue1() + 
 				    disposition.getSafetyWarehousestock() - 
 				    disposition.getWarehousestockPassedPeriod() - 
 				    disposition.getWaitingQueue2() - 
 				    disposition.getOrdersInProgress();
 		disposition.setOrders(count);
 		
 		repo.addDispoistion(product, product, disposition);
 		
 	}
 	
 	/**
 	 * Setzt die Dispoition des E-Materials
 	 * 
 	 * @param P-Material fuer Stueckliste
 	 * @param P-Material,E-Material fuer das einzele Material selbst
 	 * @param E-Material -> Uebergeortnetes Material
 	 */
 	private void setEMaterialDispo(int product, int material, int preMaterial)
 	{
 		Repository repo = Repository.getInstance();
 		DatabaseContentHandler dbch = DatabaseContentHandler.get();
 		
 		Material mat = dbch.findMaterial(product);
 		
 		
 		Disposition disposition = new Disposition();
 		Disposition preDisposition = repo.getDispoitionByMaterial(product,preMaterial);
 		
 		disposition.setSalesOrders(preDisposition.getOrders());
 		
 		disposition.setWaitingQueue1(preDisposition.getWaitingQueue2());
 		
 		//Lagerbestand geplant
 		disposition.setSafetyWarehousestock(repo.getStafetyStock(material));
 		
 		//Lagerbestand Vorperiode
 		//TODO Startamount richtig?
 		disposition.setWarehousestockPassedPeriod(mat.getStartamount());
 		
 		// Warteschlange (uebergeordnet)
 		disposition.setWaitingQueue2(repo.getAmountOfWaitingMaterial(material));
 		
 		disposition.setOrdersInProgress(repo.getAmountOfMaterialInWork(material));
 		
 		// Produktionsauftraege kommende Periode
 		int count = disposition.getSalesOrders() + 
 					disposition.getWaitingQueue1() + 
 					disposition.getSafetyWarehousestock() - 
 					disposition.getWarehousestockPassedPeriod() - 
 					disposition.getWaitingQueue2() - 
 					disposition.getOrdersInProgress();	
 		
 		disposition.setOrders(count);
 		
 		repo.addDispoistion(product, material, disposition);
 	}
 }
