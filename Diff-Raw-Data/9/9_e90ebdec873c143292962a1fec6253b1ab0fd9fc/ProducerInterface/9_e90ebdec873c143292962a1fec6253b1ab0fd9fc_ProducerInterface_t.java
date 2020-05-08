 package sbc.gui;
 
/**
 * callback interface for producer
 */
 public interface ProducerInterface {
 	/**
 	 * callback for creating the producers
 	 * @param chicken
 	 * @param eggs
 	 * @param choco
 	 * @param chocoRabbits
 	 */
 	public void createProducers(int chicken, int eggs, int choco, int chocoRabbits);
 }
