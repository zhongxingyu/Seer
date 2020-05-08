 package com.github.realmcraft.realms.towns;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.*;
import com.sun.tools.javac.util.List;
 
 ////////////////////////////////////////////////////////////////////////////////
 /**
  * Represents a town.
  * @author Realmcraft Developer Team
  *
  */
 @Entity
 public class TownObj implements Serializable {
 	@Id
 	private String name;
 	@Basic
 	private String kingdom;
 	@Basic
 	private String townBoard;
 	@Basic
 	private long spawnWarp;
 	@Basic
 	private int numberOfBonusPlots;
 	@Basic
 	private Date createdOn;
 	@Basic
 	private String mayor;
 	@Basic
 	private List<String> residents;
 	@Basic
 	private List<String> invitedResidents; //Town staff can invite players to become residents
 	@Basic
 	private List<String> requestedResidents; //Players can request to become residents
 	@Basic
 	private List<String> assistants;
 	@Basic
 	private List<String> helpers;
 	
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the name of a Town object.
 	 * @return The name of the receiving town.
 	 */
 	public String getName() { return name; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the name of a Town object.
 	 * @param name Name of the town.
 	 * @return New name of the town.
 	 */
 	public String setName(String name) {
 		this.name = name;
 		return this.name;
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the kingdom of a Town object.
 	 * @return The kingdom of the receiving town.
 	 */
 	public String getKingdom() { return kingdom; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the kingdom of a Town object.
 	 * @param kingdom Kingdom of the town.
 	 * @return New kingdom of the town.
 	 */
 	public String setKingdom(String kingdom) {
 		this.kingdom = kingdom;
 		return this.kingdom;
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the town board of a Town object.
 	 * @return The town board of the receiving town.
 	 */
 	public String getTownBoard() { return townBoard; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the town board of a Town object.
 	 * @param townBoard Town board of the town.
 	 * @return New town board of the town.
 	 */
 	public String setTownBoard(String townBoard) {
 		this.townBoard = townBoard;
 		return this.townBoard;
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the spawn warp of a Town object.
 	 * @return The spawn warp of the receiving town.
 	 */
 	public long getSpawnWarp() { return spawnWarp; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the spawn warp of a Town object.
 	 * @param spawnWarp Spawn warp of the town.
 	 * @return New spawn warp of the town.
 	 */
 	public long setSpawnWarp(long spawnWarp) {
 		this.spawnWarp = spawnWarp;
 		return this.spawnWarp;
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the number of bonus plots of a Town object.
 	 * @return The number of bonus plots of the receiving town.
 	 */
 	public int getNumberOfBonusPlots() { return numberOfBonusPlots; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the number of bonus plots of a Town object.
 	 * @param numberOfBonusPlots Number of bonus plots of the town.
 	 * @return Number of bonus plots of the town.
 	 */
 	public int setNumberOfBonusPlots(int numberOfBonusPlots) {
 		this.numberOfBonusPlots = numberOfBonusPlots;
 		return this.numberOfBonusPlots;
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the created on of a Town object.
 	 * @return Date object of the creation of the receiving town.
 	 */
 	public Date getCreatedOn() { return createdOn; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the created on of a Town object.
 	 * @param createdOn Date object for created on of the town.
 	 * @return New creation date object of the town.
	 */b32
 	public Date setCreatedOn(Date createdOn) {
 		this.createdOn = createdOn;
 		return this.createdOn;
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the residents of a Town object.
 	 * @return String array of residents of the receiving town.
 	 */
 	public List<String> getResidents() { return residents; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the residents of a Town object.
 	 * @param residents String array of residents.
 	 * @return String array of residents of the object of the town.
 	 */
 	public List<String> setResidents(List<String> residents) {
 		this.residents = residents;
 		return this.residents;
 	}
 	
 	//--------------------------------------------------------------------------
 
 	/**
 	 * Returns the residents of a Town object.
 	 * @return True if player is resident, false is he/she is not.
 	 */
 	public boolean isResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty())
 		{
 			if(residents.contains(playerName)) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Adds an assistant to the assistants string of a Town object.
 	 * @param assistants String array of assistants.
 	 * @return String array of assistants of the object of the town.
 	 */
 	public boolean addResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && !isResident(playerName)) {
 			residents.add(playerName);
 			if(isResident(playerName) == true) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Removes an assistant from the assistants string of a Town object.
 	 * @param assistants String array of assistants.
 	 * @return Boolean of whether or not it successfully removed the assistant.
 	 */
 	public boolean removeResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && isResident(playerName)) {
 			
 			residents.remove(playerName);
 			
 			if(isResident(playerName) == false) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the residents of a Town object.
 	 * @return String array of residents of the receiving town.
 	 */
 	public List<String> getInvitedResidents() { return invitedResidents; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the residents of a Town object.
 	 * @param residents String array of residents.
 	 * @return String array of residents of the object of the town.
 	 */
 	public List<String> setInvitedResidents(List<String> invitedResidents) {
 		this.invitedResidents = invitedResidents;
 		return this.invitedResidents;
 	}
 	
 	//--------------------------------------------------------------------------
 
 	/**
 	 * Returns the residents of a Town object.
 	 * @return True if player is resident, false is he/she is not.
 	 */
 	public boolean isInvitedResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty())
 		{
 			if(invitedResidents.contains(playerName)) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Adds an assistant to the assistants string of a Town object.
 	 * @param assistants String array of assistants.
 	 * @return String array of assistants of the object of the town.
 	 */
 	public boolean addInvitedResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && !isInvitedResident(playerName) ) {
 			invitedResidents.add(playerName);
 			if(isInvitedResident(playerName) == true) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Removes an assistant from the assistants string of a Town object.
 	 * @param assistants String array of assistants.
 	 * @return Boolean of whether or not it successfully removed the assistant.
 	 */
 	public boolean removeInvitedResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && isInvitedResident(playerName)) {
 			
 			invitedResidents.remove(playerName);
 			
 			if(isInvitedResident(playerName) == false) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the residents of a Town object.
 	 * @return String array of residents of the receiving town.
 	 */
 	public List<String> getRequestedResidents() { return requestedResidents; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the residents of a Town object.
 	 * @param residents String array of residents.
 	 * @return String array of residents of the object of the town.
 	 */
 	public List<String> setRequestedResidents(List<String> requestedResidents) {
 		this.requestedResidents = requestedResidents;
 		return this.requestedResidents;
 	}
 	
 	//--------------------------------------------------------------------------
 
 	/**
 	 * Returns the residents of a Town object.
 	 * @return True if player is resident, false is he/she is not.
 	 */
 	public boolean isRequestedResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty())
 		{
 			if(requestedResidents.contains(playerName)) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Adds an assistant to the assistants string of a Town object.
 	 * @param assistants String array of assistants.
 	 * @return String array of assistants of the object of the town.
 	 */
 	public boolean addRequestedResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && !isRequestedResident(playerName)) {
 			requestedResidents.add(playerName);
 			if(isRequestedResident(playerName) == true) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Removes an assistant from the assistants string of a Town object.
 	 * @param assistants String array of assistants.
 	 * @return Boolean of whether or not it successfully removed the assistant.
 	 */
 	public boolean removeRequestedResident(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && isRequestedResident(playerName)) {
 			
 			requestedResidents.remove(playerName);
 			
 			if(isRequestedResident(playerName) == false) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the assistants of a Town object.
 	 * @return String array of assistants of the receiving town.
 	 */
 	public List<String> getAssistants() { return assistants; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the assistants of a Town object.
 	 * @param assistants String list of assistants.
 	 * @return String list of assistants of the object of the town.
 	 */
 	public List<String> setAssistants(List<String> assistants) {
 		this.assistants = assistants;
 		return this.assistants;
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the assistants of a Town object.
 	 * @return True if player is assistant, false is he/she is not.
 	 */
 	public boolean isAssistant(String playerName) {
 		if(playerName != null && !playerName.isEmpty())
 		{
 			if(assistants.contains(playerName)) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Adds an assistant to the assistants string of a Town object.
 	 * @param assistants String list of assistants.
 	 * @return Boolean of whether or not it actually added the player as an assistant.
 	 */
 	public boolean addAssistant(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && !isAssistant(playerName)) {
 			assistants.add(playerName);
 			if(isAssistant(playerName)) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Removes an assistant from the assistants string of a Town object.
 	 * @param assistants String list of assistants.
 	 * @return Boolean of whether or not it successfully removed the assistant.
 	 */
 	public boolean removeAssistant(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && isAssistant(playerName)) {
 			
 			assistants.remove(playerName);
 			
 			if(isAssistant(playerName)) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	//--------------------------------------------------------------------------
 	
 	/**
 	 * Returns the helpers of a Town object.
 	 * @return String list of helpers of the receiving town.
 	 */
 	public List<String> getHelpers() { return helpers; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the helpers of a Town object.
 	 * @param helpers String list of helpers.
 	 * @return String list of helpers of the object of the town.
 	 */
 	public List<String> setHelpers(List<String> helpers) {
 		this.helpers = helpers;
 		return this.helpers;
 	}
 	
 	//--------------------------------------------------------------------------
 
 	/**
 	 * Returns the helpers of a Town object.
 	 * @return True if player is helper, false is he/she is not.
 	 */
 	public boolean isHelper(String playerName) {
 		if(playerName != null && !playerName.isEmpty())
 		{
 			if(helpers.contains(playerName)) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Adds a helper to the helpers string list of a Town object.
 	 * @param playerName Player's name.
 	 * @return Boolean of whether or not the player was added as a helper.
 	 */
 	public boolean addHelper(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && !isHelper(playerName)) {
 			helpers.add(playerName);
 			if(isHelper(playerName) == true) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Removes a helper from the helpers string list of a Town object.
 	 * @param playerName Player's name
 	 * @return Boolean of whether or not it successfully removed the helper.
 	 */
 	public boolean removeHelper(String playerName) {
 		if(playerName != null && !playerName.isEmpty() && isHelper(playerName)) {
 			
 			helpers.remove(playerName);
 			
 			if(isHelper(playerName) == false) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Returns the mayor of a Town object.
 	 * @return String mayor of the receiving town.
 	 */
 	public String getMayor() { return mayor; }
 
 	//--------------------------------------------------------------------------	
 	
 	/**
 	 * Sets the mayor of a Town object.
 	 * @param mayor String array of helpers.
 	 * @return String mayor of the object of the town.
 	 */
 	public String setMayor(String mayor) {
 		this.mayor = mayor;
 		return this.mayor;
 	}
 	
 	
 } // end class Town
 ////////////////////////////////////////////////////////////////////////////////
