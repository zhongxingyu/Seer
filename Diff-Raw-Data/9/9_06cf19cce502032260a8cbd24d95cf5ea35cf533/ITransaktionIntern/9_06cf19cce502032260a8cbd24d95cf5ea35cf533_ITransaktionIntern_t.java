 package aip2.m.TransaktionModul;
 
 /**
  * Verwaltet Transaktionen es ist nur eine Transaktion gleichzeitig möglich, es
  * gibt keine geschachtelten Transaktionen.
  * 
  * Vor jeder Transaktion sollte geprüft werden ob bereits eine Transaktion
  * läuft.
  * 
  * 
  */
 public interface ITransaktionIntern {
 
 	/**
 	 * Prüft, ob bereits eine Transaktion läuft
 	 * 
 	 * @return laufendeTransaktion?
 	 */
 	boolean isRunningTransaction();
 
 	/**
 	 * startet eine neue Transaktion
 	 * 
	 * @pre erst prüfen mit isRunningTransaction(), ob schon eine Transaktion
	 *      läuft
 	 * @return false wenn keine Transaktion gestartet werden könnte, sonst true.
 	 *         //*@throws TransactionNotClosed //* um zu verhinden, dass
 	 *         Transaktionen nicht commitet werden
 	 */
 	boolean startTransaction();
 
 	/**
 	 * Commitet eine Transaktion
 	 * 
 	 * @return false wenn keine Verbindung zur DB besteht, sonst true.
 	 */
 	boolean commitTransaction();
 
 	/**
 	 * Vergisst eine Transaktion
 	 * 
 	 * @return false wenn keine Verbindung zur DB besteht, sonst true.
 	 */
 	boolean rollbackTransaction();
 
 }
