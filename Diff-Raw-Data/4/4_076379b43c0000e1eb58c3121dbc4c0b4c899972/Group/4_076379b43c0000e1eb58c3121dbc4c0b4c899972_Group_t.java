 package org.softwaresynthesis.mytalk.server.abook;
 
 /**
  * Rappresenta un gruppo di una rubrica
  * utente
  * 
  * @author 	Andrea Meneghinello
  * @version	%I%, %G%
  */
 public class Group implements IGroup 
 {
 	private Long id;
 	private String name;
 	
 	/**
 	 * Crea un oggetto gruppo privo
 	 * di valori
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 */
 	public Group()
 	{
 	}
 	
 	public Group(Long identifier)
 	{
 		this.setId(identifier);
 	}
 
 	/**
 	 * Restituisce l'istanza dell'oggetto
 	 * come stringa in formato JSON
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @return	{@link String} dell'istanza
 	 * 			in formato JSON
 	 */
 	@Override
 	public String toJson() 
 	{
		String result = "{\"id\":\"" + this.getId() + "\"";
		result += "\"name\":\"" + this.getName() + "\"}";
 		return result;
 	}
 
 	/**
 	 * Restituisce l'identificatore univoco
 	 * del gruppo
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @return	{@link Long} che identifica univocamente
 	 * 			il gruppo
 	 */
 	@Override
 	public Long getId() 
 	{
 		return this.id;
 	}
 	
 	/**
 	 * Imposta l'identificatore univoco
 	 * del gruppo
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @param 	identifier	{@link Long} idenficatore
 	 * 						del gruppo
 	 */
 	protected void setId(Long identifier)
 	{
 		this.id = identifier;
 	}
 
 	/**
 	 * Restituisce il nome del gruppo
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @return	{@link String} con il nome
 	 * 			del gruppo
 	 */
 	@Override
 	public String getName() 
 	{
 		return this.name;
 	}
 
 	/**
 	 * Imposta il nome del gruppo
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 * @param	name	{@link String} nome del
 	 * 					gruppo
 	 */
 	@Override
 	public void setName(String name) 
 	{
 		this.name = name;
 	}
 }
