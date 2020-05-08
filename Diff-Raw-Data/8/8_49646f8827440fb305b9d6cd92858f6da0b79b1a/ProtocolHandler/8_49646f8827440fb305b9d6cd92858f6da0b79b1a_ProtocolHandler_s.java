 package com.eyecall.connection;
 
 import java.util.Map;
 
 public interface ProtocolHandler {
 	
 	public static final String KEY_NAME = "name";
 	
 	// Received by Volunteer
 	/**
 	 * Er is een unieke sleutel toegekend aan de client.<br>
 	 * key [String]: de gegenereerde sleutel
 	 */
 	public static final String ASSIGN_KEY = "assign_key";
 	/**
 	 * Een VBP heeft een nieuw hulpverzoek ingediend en deze vrijwilliger wordt opgeroepen om te helpen.<br>
 	 * request_id [integer]: de identifier van deze request<br>
 	 * longitude [double] De breedtegraad van de VBP�s locatie<br>
 	 * latitude [double] De hoogtegraad van de VBP�s locatie
 	 */
 	public static final String NEW_REQUEST = "new_request";
 	/**
 	 * Een acceptatie van hulpverzoek door de vrijwilliger wordt toegekend door de server.<br>
 	 * request_id [integer]: de identifier van het hulpverzoek dat is toegekend
 	 */
 	public static final String ACKNOWLEDGE_HELP = "acknowledge_help";
 	/**
 	 * De locatie van de VBP wordt naar de vrijwilliger verstuurd en periodiek geupdatet<br>
 	 * longitude [double] De breedtegraad van de VBP�s locatie<br>
 	 * latitude [double] De hoogtegraad van de VBP�s locatie
 	 */
 	public static final String UPDATE_LOCATION = "update_location";
 	/**
 	 * Video- en geluidsdata wordt ontvangen van de VBP (via de server)<br>
 	 * data [byte[]]: De video- en geluidsdata.
 	 */
 	public static final String MEDIA_DATA = "media_data";
 	
 	// Received by VIP and Volunteer
 	/**
 	 * De ander heeft de verbinding verbroken.<br>
 	 * Geen parameters
 	 */
 	public static final String OTHER_DISCONNECTED = "other_disconnected";
 	
 	// Received by VIP
 	/**
 	 * Het hulpverzoek wat verstuurd is door de VBP is door een vrijwilliger geaccepteerd. <br>
 	 * Geen parameters
 	 */
 	public static final String REQUEST_GRANTED = "request_granted";
 	/**
 	 * Het verzoek wordt door niemand geaccepteerd.<br>
 	 * Geen parameters
 	 */
 	public static final String REQUEST_DENIED = "request_denied";
 	/**
 	 * De vrijwilliger kan niet langer helpen en heeft de hulpverzoek doorverwezen<br>
 	 * Geen parameters
 	 */
 	public static final String REQUEST_FORWARDED = "request_forwarded";
 	/**
 	 * De geluidsdata verstuurd door de vrijwilliger (via de server) wordt ontvangen.<br>
 	 * data [byte[]]: De geluidsdata
 	 */
 	public static final String AUDIO_DATA = "audio_data";
 	
 	// Received by Volunteer and Server
 	/**
 	 * 
 	 * GEVAL 1 (ontv: Server): Het verzoek wordt geannuleerd door de VBP<br><br>
 	 * GEVAL 2 (ontv: Volunteer): Het verzoek wordt geannuleerd omdat iemand anders het verzoek heeft geaccepteerd.<br>
 	 * request_id [integer]:de identifier van het hulpverzoek
 	 */
 	public static final String CANCEL_REQUEST = "cancel_request";
 	
 	// Received by Server
 	/**
 	 * Aanvragen van een unieke sleutel voor de VBP<br>
 	 * Geen parameters
 	 */
 	public static final String OBTAIN_KEY = "obtain_key";
 	/**
 	 * accepteren van een verzoek<br>
 	 * request_id [integer]: de identifier van de request die geaccepteerd wordt.<br>
 	 * volunteer_id [integer]: de identifier van deze vrijwilliger
 	 */
 	public static final String ACCEPT_REQUEST = "accept_request";
 	/**
 	 * weigeren van een verzoek<br>
 	 * request_id [integer]: de identifier van de request die geweigerd wordt.
 	 */
 	public static final String REJECT_REQUEST = "reject_request";
 	/**
 	 * Aanvragen van een hulpverzoek waarbij de huidige locatie wordt meegegeven<br>
 	 * latitude [double]: lengtegraad van de locatie<br>
 	 * longitude [double]: breedtegraad van de locatie
 	 */
 	public static final String REQUEST_HELP = "request_help";
 	/**
 	 * Verbreken van een actieve verbinding aan een van beide kanten.<br>
 	 * Geen parameters
 	 */
 	public static final String DISCONNECT = "disconnect";
 	/**
 	 * Een vrijwilliger kan niet langer helpen een geeft aan dat het hulpverzoek moet worden doorgestuurd<br>
 	 * Geen parameters
 	 */
 	public static final String FORWARD_REQUEST = "forward_request";
 	/**
 	 * Er worden twee vragen gesteld nadat een hulpverzoek is afgerond. De eerste: was het hulpverzoek succesvol? En de tweede: Wil de vrijwilliger vaker hulpoproepen ontvangen vanuit deze omgeving? <br>
 	 * latitude [double]: lengtegraad van de locatie<br>
 	 * longitude [double]: breedtegraad van de locatie<br>
 	 * succeeded [boolean] Was de verbinding succesvol<br>
 	 * preferred [boolean] Wil de vrijwilliger vaker hulpverzoeken ontvangen vanaf deze locatie
 	 */
 	public static final String REQUEST_FEEDBACK = "request_feedback";
 	/**
 	 * De lijst met locaties van een vrijwilliger wordt bijgewerkt<br>
 	 * latitude [double]: hoogtegraad van de locatie<br>
 	 * longitude [double]: breedtegraad van de locatie<br>
	 * action [�add�|�delete�]:<br>
	 * �add�: voeg deze locatie toe<br>
	 * �delete�: verwijder deze locatie<br>
	 * type [�preferred�|�non-preferred�]
 	 */
 	public static final String UPDATE_LOCATION = "update_location";
 	
 	
 	public State handleMessage(State state, String name, Map<String, Object> params);
 }
