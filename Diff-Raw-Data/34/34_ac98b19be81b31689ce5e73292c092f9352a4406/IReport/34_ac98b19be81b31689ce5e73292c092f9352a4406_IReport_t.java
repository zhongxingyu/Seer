 package fm.audiobox.interfaces;
 
 
 public interface IReport {
 
   /**
    * Sends {@code message} and {@code extra data} to reporting Service
    * 
    * @param message the main message of the report log
    * @param extra message to be added to report log
    */
   public void report( String message, String extra );
   
   
   /**
    * Sends all information taken from {@code exception} and {@code extra data} to reporting Service
    * 
    * @param exception the exception to be logged
    * @param extra message to be added to report log
    */
   public void report( Throwable exception, String extra );
   
  
  /**
   * Sends all information taken from {@code exception} and {@code extra data} to reporting Service
   * 
   * @param message the text message to be logged
   * @param exception the exception to be logged
   * @param extra message to be added to report log
   */
  public void report( String message, Throwable exception, String extra );
  
  /**
   * Sends all information taken from {@code exception} and {@code extra data} to reporting Service
   * 
   * @param message the text message to be logged
   * @param exception the exception to be logged
   * @param extra array args key/value pair
   */
  public void report( String message, Throwable exception, String extra, String... data );
  
 }
