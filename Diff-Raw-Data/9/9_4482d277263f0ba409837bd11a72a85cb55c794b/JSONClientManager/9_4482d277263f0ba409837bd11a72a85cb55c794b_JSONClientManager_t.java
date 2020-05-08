 /**  
  *  Betaville JSON Connection
  *  Copyright (C) 2011 Skye Book
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.skyebook.betaville.json;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 
 import edu.poly.bxmc.betaville.jme.map.ILocation;
 import edu.poly.bxmc.betaville.jme.map.UTMCoordinate;
 import edu.poly.bxmc.betaville.model.City;
 import edu.poly.bxmc.betaville.model.Comment;
 import edu.poly.bxmc.betaville.model.Design;
 import edu.poly.bxmc.betaville.model.EmptyDesign;
 import edu.poly.bxmc.betaville.model.ProposalPermission;
 import edu.poly.bxmc.betaville.model.Wormhole;
 import edu.poly.bxmc.betaville.model.IUser.UserType;
 import edu.poly.bxmc.betaville.net.PhysicalFileTransporter;
 import edu.poly.bxmc.betaville.net.ProtectedManager;
 
 /**
  * @author Skye Book
  *
  */
 public class JSONClientManager implements ProtectedManager {
 
 	private String baseURL = "http://localhost/service/service.php";
 
 	private JsonFactory jsonFactory;
 
 	// Set to false if you'd prefer to have unzipped responses returned
 	private boolean useGZIP = true;
 
 	private static final String REQUEST_GZIP = "gz=1";
 
 	/**
 	 * 
 	 */
 	public JSONClientManager(){
 		jsonFactory = new JsonFactory();
 	}
 
 	private JsonParser doRequest(String request){
 		try {
 			URL url = null;
 			if(useGZIP){
 				url = new URL(baseURL+"?"+request+"&"+REQUEST_GZIP);
 			}
 			else{
 				url = new URL(baseURL+"?"+request+"&"+REQUEST_GZIP);
 			}
 
 			return jsonFactory.createJsonParser(url);
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#isBusy()
 	 */
 	@Override
 	public boolean isBusy() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#close()
 	 */
 	@Override
 	public void close() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#isAlive()
 	 */
 	@Override
 	public boolean isAlive() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#checkNameAvailability(java.lang.String)
 	 */
 	@Override
 	public boolean checkNameAvailability(String name) {
 		String request = "section=user&request=available&username="+name;
 		JsonParser response = doRequest(request);
 
 		try {
 			response.nextToken();
 			while(response.nextToken()!=JsonToken.END_OBJECT){
 				if(response.getCurrentName().equals("usernameAvailable")){
 					response.nextToken();
 					return response.getValueAsBoolean();
 				}
 			}
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#getUserEmail(java.lang.String)
 	 */
 	@Override
 	public String getUserEmail(String user) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findDesignByID(int)
 	 */
 	@Override
 	public Design findDesignByID(int designID) {
 		String request = "section=design&request=findbyid&id="+designID;
 		JsonParser response = doRequest(request);
 
 		return JSONConverter.toDesign(response);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findDesignsByName(java.lang.String)
 	 */
 	@Override
 	public List<Design> findDesignsByName(String name) {
		String request = "section=design&request=findbyname&name="+name;
		JsonParser response = doRequest(request);

		return JSONConverter.toDesignList(response);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findDesignsByUser(java.lang.String)
 	 */
 	@Override
 	public List<Design> findDesignsByUser(String user) {
		String request = "section=design&request=findbyuser&user="+user;
 		JsonParser response = doRequest(request);
 
 		return JSONConverter.toDesignList(response);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findDesignsByDate(java.util.Date)
 	 */
 	@Override
 	public List<Design> findDesignsByDate(Date date) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findAllDesignsByCity(int)
 	 */
 	@Override
 	public List<Design> findAllDesignsByCity(int cityID) {
 
 
 		String request = "section=design&request=findbycity&city="+cityID;
 		JsonParser response = doRequest(request);
 
 		return JSONConverter.toDesignList(response);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findDesignsByCitySetStartEnd(int, boolean, int, int)
 	 */
 	@Override
 	public List<Design> findDesignsByCitySetStartEnd(int cityID,
 			boolean onlyBase, int start, int end) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findBaseDesignsByCity(int)
 	 */
 	@Override
 	public List<Design> findBaseDesignsByCity(int cityID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findTerrainByCity(int)
 	 */
 	@Override
 	public List<Design> findTerrainByCity(int cityID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findVersionsOfProposal(int)
 	 */
 	@Override
 	public int[] findVersionsOfProposal(int proposalDesignID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#getProposalPermissions(int)
 	 */
 	@Override
 	public ProposalPermission getProposalPermissions(int designID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findAllProposals(int)
 	 */
 	@Override
 	public int[] findAllProposals(int designID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findAllProposalsNearLocation(edu.poly.bxmc.betaville.jme.map.UTMCoordinate, int)
 	 */
 	@Override
 	public List<Design> findAllProposalsNearLocation(UTMCoordinate coordinate,
 			int meterRadius) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#requestThumbnail(int)
 	 */
 	@Override
 	public PhysicalFileTransporter requestThumbnail(int designID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#requestFile(edu.poly.bxmc.betaville.model.Design)
 	 */
 	@Override
 	public PhysicalFileTransporter requestFile(Design design) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#requestFile(int)
 	 */
 	@Override
 	public PhysicalFileTransporter requestFile(int designID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#addCity(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public int addCity(String name, String state, String country) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findAllCities()
 	 */
 	@Override
 	public List<City> findAllCities() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findCitiesByName(java.lang.String)
 	 */
 	@Override
 	public List<Integer> findCitiesByName(String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findCitiesByState(java.lang.String)
 	 */
 	@Override
 	public List<Integer> findCitiesByState(String state) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findCitiesByCountry(java.lang.String)
 	 */
 	@Override
 	public List<Integer> findCitiesByCountry(String country) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findCityByID(int)
 	 */
 	@Override
 	public String[] findCityByID(int cityID) {
 
 		String request = "section=design&request=findbyid&id="+cityID;
 		JsonParser response = doRequest(request);
 
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#findCityByAll(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public String[] findCityByAll(String name, String state, String country) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#reportSpamComment(int)
 	 */
 	@Override
 	public void reportSpamComment(int commentID) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#getComments(int)
 	 */
 	@Override
 	public List<Comment> getComments(int designID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#checkUserLevel(java.lang.String, edu.poly.bxmc.betaville.model.IUser.UserType)
 	 */
 	@Override
 	public int checkUserLevel(String user, UserType userType) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#getDesignVersion()
 	 */
 	@Override
 	public long getDesignVersion() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#getUserLevel(java.lang.String)
 	 */
 	@Override
 	public UserType getUserLevel(String user) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#getWormholesWithin(edu.poly.bxmc.betaville.jme.map.UTMCoordinate, int, int)
 	 */
 	@Override
 	public List<Wormhole> getWormholesWithin(UTMCoordinate location,
 			int extentNorth, int extentEast) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#getAllWormholes()
 	 */
 	@Override
 	public List<Wormhole> getAllWormholes() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#getAllWormholesInCity(int)
 	 */
 	@Override
 	public List<Wormhole> getAllWormholesInCity(int cityID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.UnprotectedManager#synchronizeData(java.util.HashMap)
 	 */
 	@Override
 	public List<Design> synchronizeData(HashMap<Integer, Integer> hashMap) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#authenticateUser(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean authenticateUser(String name, String pass) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#startSession(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean startSession(String name, String pass) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#endSession(java.lang.String)
 	 */
 	@Override
 	public boolean endSession(String sessionToken) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#addUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean addUser(String name, String pass, String email,
 			String twitter, String bio) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#changePassword(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean changePassword(String name, String pass, String newPass) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#changeBio(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean changeBio(String name, String pass, String newBio) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#addDesign(edu.poly.bxmc.betaville.model.Design, java.lang.String, java.lang.String, edu.poly.bxmc.betaville.net.PhysicalFileTransporter)
 	 */
 	@Override
 	public int addDesign(Design design, String user, String pass,
 			PhysicalFileTransporter pft) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#addEmptyDesign(edu.poly.bxmc.betaville.model.EmptyDesign, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public int addEmptyDesign(EmptyDesign design, String user, String pass) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#addProposal(edu.poly.bxmc.betaville.model.Design, java.lang.String, java.lang.String, java.lang.String, edu.poly.bxmc.betaville.net.PhysicalFileTransporter, edu.poly.bxmc.betaville.net.PhysicalFileTransporter, edu.poly.bxmc.betaville.model.ProposalPermission)
 	 */
 	@Override
 	public int addProposal(Design design, String removables, String user,
 			String pass, PhysicalFileTransporter pft,
 			PhysicalFileTransporter thumbTransporter,
 			ProposalPermission permission) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#addVersion(edu.poly.bxmc.betaville.model.Design, java.lang.String, java.lang.String, java.lang.String, edu.poly.bxmc.betaville.net.PhysicalFileTransporter, edu.poly.bxmc.betaville.net.PhysicalFileTransporter)
 	 */
 	@Override
 	public int addVersion(Design design, String removables, String user,
 			String pass, PhysicalFileTransporter pft,
 			PhysicalFileTransporter thumbTransporter) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#addBase(edu.poly.bxmc.betaville.model.Design, java.lang.String, java.lang.String, edu.poly.bxmc.betaville.net.PhysicalFileTransporter)
 	 */
 	@Override
 	public int addBase(Design design, String user, String pass,
 			PhysicalFileTransporter pft) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#removeDesign(int, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public int removeDesign(int designID, String user, String pass) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#changeDesignName(int, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean changeDesignName(int designID, String user, String pass,
 			String newName) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#changeDesignFile(int, java.lang.String, java.lang.String, edu.poly.bxmc.betaville.net.PhysicalFileTransporter, boolean)
 	 */
 	@Override
 	public boolean changeDesignFile(int designID, String user, String pass,
 			PhysicalFileTransporter pft, boolean textureOnOff) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#changeDesignDescription(int, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean changeDesignDescription(int designID, String user,
 			String pass, String newDescription) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#changeDesignAddress(int, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean changeDesignAddress(int designID, String user, String pass,
 			String newAddress) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#changeDesignURL(int, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean changeDesignURL(int designID, String user, String pass,
 			String newURL) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#changeModeledDesignLocation(int, float, java.lang.String, java.lang.String, edu.poly.bxmc.betaville.jme.map.UTMCoordinate)
 	 */
 	@Override
 	public boolean changeModeledDesignLocation(int designID, float rotY,
 			String user, String pass, UTMCoordinate newLocation) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#faveDesign(int, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public int faveDesign(int designID, String user, String pass) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#addComment(edu.poly.bxmc.betaville.model.Comment, java.lang.String)
 	 */
 	@Override
 	public boolean addComment(Comment comment, String pass) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#deleteComment(int, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean deleteComment(int commentID, String user, String pass) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#addWormhole(edu.poly.bxmc.betaville.jme.map.ILocation, java.lang.String, int)
 	 */
 	@Override
 	public int addWormhole(ILocation location, String name, int cityID) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#deleteWormhole(int)
 	 */
 	@Override
 	public int deleteWormhole(int wormholeID) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#editWormholeName(int, java.lang.String)
 	 */
 	@Override
 	public int editWormholeName(int wormholeID, String newName) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.poly.bxmc.betaville.net.ProtectedManager#editWormholeLocation(int, edu.poly.bxmc.betaville.jme.map.UTMCoordinate)
 	 */
 	@Override
 	public int editWormholeLocation(int wormholeID, UTMCoordinate newLocation) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
