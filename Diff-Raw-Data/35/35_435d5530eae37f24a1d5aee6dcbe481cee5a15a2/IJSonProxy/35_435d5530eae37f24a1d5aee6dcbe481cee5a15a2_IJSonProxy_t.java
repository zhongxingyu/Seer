 package pl.edu.agh.io.coordinator.utils.net;
 
 import java.util.Set;
 
 import pl.edu.agh.io.coordinator.resources.Group;
 import pl.edu.agh.io.coordinator.resources.Layer;
 import pl.edu.agh.io.coordinator.resources.MapItem;
 import pl.edu.agh.io.coordinator.resources.Message;
 import pl.edu.agh.io.coordinator.resources.Point;
 import pl.edu.agh.io.coordinator.resources.User;
 import pl.edu.agh.io.coordinator.resources.UserItem;
 import pl.edu.agh.io.coordinator.resources.UserState;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotCreateGroupException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotLogInException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotRemoveException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidGroupException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidLayerException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidMapItemException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidSessionIDException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidUserException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidUserItemException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.NetworkException;
 
 public interface IJSonProxy {
 
 	// Creates session and sets sessionID
 	public void login(String userID, String password) throws CouldNotLogInException, NetworkException;
 
 	// Ends session, (sets sessionID to -1 even if InvalidSessionIDException
 	// occured)
 	public void logout() throws InvalidSessionIDException, NetworkException;
 
 	// Returns set of MapItems from given layer
 	public Set<MapItem> getMapItems(Layer layer) throws InvalidSessionIDException, InvalidLayerException, NetworkException;
 
 	// Returns all possible layers
 	public Set<Layer> getLayers() throws InvalidSessionIDException, NetworkException;
 
 	// Returns generated MapItem (with it's id)
 	public MapItem addItemToLayer(Layer layer, Point point, String data) throws InvalidSessionIDException, InvalidLayerException,
 			NetworkException;
 
 	public void removeMapItem(MapItem item) throws InvalidSessionIDException, InvalidMapItemException, NetworkException;
 
 	public void updateSelfState(UserState newState) throws InvalidSessionIDException, NetworkException;
 
	public UserState getUserState(String user) throws InvalidSessionIDException, InvalidUserException, NetworkException;
	
 	// Returns all possible UserItems
 	public Set<UserItem> getPossibleUserItems() throws InvalidSessionIDException, NetworkException;
 
 	public void addItemToUser(User user, UserItem item) throws InvalidSessionIDException, InvalidUserException,
 			InvalidUserItemException, NetworkException;
 
 	public void removeItemFromUser(User user, UserItem item) throws InvalidSessionIDException, InvalidUserException,
 			InvalidUserItemException, CouldNotRemoveException, NetworkException;
 
 	public Set<User> getUsers() throws InvalidSessionIDException, NetworkException;
 
 	// Updates user information (ex. it's items)
 	public Set<String> getUserItems(User user) throws InvalidSessionIDException, InvalidUserException, NetworkException;
 
 	public Set<Group> getGroups() throws InvalidSessionIDException, NetworkException;
 
 	public void createGroup(Group group) throws InvalidSessionIDException, CouldNotCreateGroupException, NetworkException;
 
 	public void addToGroup(User user, Group group) throws InvalidSessionIDException, InvalidUserException, InvalidGroupException,
 			NetworkException;
 
 	public void removeFromGroup(User user, Group group) throws InvalidSessionIDException, InvalidUserException,
 			InvalidGroupException, CouldNotRemoveException, NetworkException;
 
 	public Set<String> getGroupUsers(Group group) throws InvalidSessionIDException, InvalidGroupException, NetworkException;
 
 	// Sends new message
 	public void sendMessage(String message) throws InvalidSessionIDException, NetworkException;
 
 	// Receives new messages from server
 	public Set<Message> getMessages() throws InvalidSessionIDException, NetworkException;
 
 }
