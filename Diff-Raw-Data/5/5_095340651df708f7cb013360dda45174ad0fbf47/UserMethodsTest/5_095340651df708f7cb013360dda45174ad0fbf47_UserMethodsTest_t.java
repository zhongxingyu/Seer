 package ru.redcraft.pinterest4j;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import ru.redcraft.pinterest4j.Activity.ActivityType;
 import ru.redcraft.pinterest4j.core.NewBoardImpl;
 import ru.redcraft.pinterest4j.core.NewPinImpl;
 import ru.redcraft.pinterest4j.core.NewUserSettingsImpl;
 import ru.redcraft.pinterest4j.core.activities.CommentActivity;
 import ru.redcraft.pinterest4j.core.activities.CreateBoardActivity;
 import ru.redcraft.pinterest4j.core.activities.FollowBoardActivity;
 import ru.redcraft.pinterest4j.core.activities.FollowUserActivity;
 import ru.redcraft.pinterest4j.core.activities.PinActivity;
 import ru.redcraft.pinterest4j.exceptions.PinterestUserNotFoundException;
 
 public class UserMethodsTest extends PinterestTestBase {
 
 	@Test
 	public void updateUserSettingsWithEmptyValuesTest() {
 		String description = UUID.randomUUID().toString();
 		User user = pinterest1.getUser();
 		NewUserSettingsImpl settings = new NewUserSettingsImpl();
 		settings.setDescription(description);
 		User newUser = pinterest1.updateUser(settings);
 		assertEquals(description, newUser.getDescription());
 		assertEquals(user.getFullName(), newUser.getFullName());
 		assertEquals(user.getTwitterURL(), newUser.getTwitterURL());
 		assertEquals(user.getFacebookURL(), newUser.getFacebookURL());
 		assertEquals(user.getSiteURL(), newUser.getSiteURL());
 		assertEquals(user.getLocation(), newUser.getLocation());
 	}
 	
 	@Test
 	public void updateUserSettingsTest() {
 		NewUserSettingsImpl settings = new NewUserSettingsImpl();
 		String firstName = UUID.randomUUID().toString();
 		String lastName = UUID.randomUUID().toString();
 		String description = UUID.randomUUID().toString();
 		String location = UUID.randomUUID().toString();
 		String website = "http://" + UUID.randomUUID().toString() + ".com";
 		settings.setFirstName(firstName)
 				.setLastName(lastName)
 				.setDescription(description)
 				.setLocation(location)
 				.setWebsite(website)
 				.setImage(imageFile);
 		
 		User newUser = pinterest1.updateUser(settings);
 		assertEquals(description, newUser.getDescription());
 		assertEquals(firstName + " " + lastName, newUser.getFullName());
 		assertEquals(pinterest1Twitter, newUser.getTwitterURL());
 		assertEquals(pinterest1Facebook, newUser.getFacebookURL());
 		assertEquals(website, newUser.getSiteURL());
 		assertEquals(location, newUser.getLocation());
 	}
 	
 	@Test
 	public void getUserByNameTest() {
 		User user = pinterest1.getUser(id2.getLogin());
 		assertEquals(pinterest2.getUser().getUserName(), user.getUserName());
 		assertEquals(pinterest2.getUser().getFullName(), user.getFullName());
 	}
 	
 	@Test(expected=PinterestUserNotFoundException.class)
 	public void getUnexistentUser() {
 		pinterest1.getUser(UUID.randomUUID().toString());
 	}
 	
 	@Test
 	public void followUserTest() {
 		pinterest2.unfollowUser(pinterest1.getUser());
 		int followersCountForUser1 = pinterest1.getUser().refresh().getFollowersCount();
 		int followingCountForUser2 = pinterest2.getUser().refresh().getFollowingCount();
 		pinterest2.followUser(pinterest1.getUser());
 		assertEquals(followersCountForUser1 + 1, pinterest1.getUser().refresh().getFollowersCount());
 		assertEquals(followingCountForUser2 + 1, pinterest2.getUser().refresh().getFollowingCount());
 		assertEquals(pinterest2.getUser(), pinterest1.getUser().getFollowers().iterator().next());
 		assertEquals(pinterest1.getUser(), pinterest2.getUser().getFollowing().iterator().next());
 		assertTrue("Is not following", pinterest2.isFollowing(pinterest1.getUser()));
 		pinterest2.unfollowUser(pinterest1.getUser()); 
 		assertEquals(followersCountForUser1, pinterest1.getUser().refresh().getFollowersCount());
 		assertEquals(followingCountForUser2, pinterest2.getUser().refresh().getFollowingCount());
 		assertFalse("Is still following", pinterest2.isFollowing(pinterest1.getUser()));
 	}
 	
 	@Test
 	public void bordCountersTest() {
 		int boardCount = pinterest1.getUser().getBoardsCount();
 		int boardCountToCreate = 3;
 		List<Board> createdBoard = new ArrayList<Board>();
 		for(int i = 0; i < boardCountToCreate; ++i) {
 			NewBoardImpl newBoard = new NewBoardImpl(UUID.randomUUID().toString(), BoardCategory.CARS_MOTORCYCLES);
 			createdBoard.add(pinterest1.createBoard(newBoard));
 		}
 		assertEquals(boardCount + boardCountToCreate, pinterest1.getUser().refresh().getBoardsCount());
 		for(Board board : createdBoard) {
 			pinterest1.deleteBoard(board);
 		}
 		assertEquals(boardCount, pinterest1.getUser().refresh().getBoardsCount());
 	}
 	
 	@Test
 	public void pinCountersTest() {
 		int userPinCount = pinterest1.getUser().refresh().getPinsCount();
 		int pinCountToCreate = 3;
 		NewBoardImpl newBoard = new NewBoardImpl(UUID.randomUUID().toString(), BoardCategory.CARS_MOTORCYCLES);
 		Board board = pinterest1.createBoard(newBoard);
 		
 		String newDescription = UUID.randomUUID().toString();
 		NewPinImpl newPin = new NewPinImpl(newDescription, 0, webLink, null, imageFile);
 		for(int i = 0; i < pinCountToCreate; ++i) {
 			pinterest1.addPin(board, newPin);
 		}
 		assertEquals(userPinCount + pinCountToCreate, pinterest1.getUser().refresh().getPinsCount());
 		
 		int page = 1;
 		List<Pin> pins = pinterest1.getUser().getPins(page);
 		int counter = pins.size();
 		
 		while(pins.size() > 0) {
 			++page;
 			pins = pinterest1.getUser().getPins(page);
 			counter += pins.size();
 		}
 		assertEquals(userPinCount + pinCountToCreate, counter);
 		
 		counter = 0;
 		for(Pin pin : pinterest1.getUser().getPins()) {
 			pin.getId();
 			++counter;
 		}
 		assertEquals(userPinCount + pinCountToCreate, counter);
 		
 		pinterest1.deleteBoard(board);
 	}
 	
 	@Ignore
 	@Test
 	public void likesCountersTest() {
 		int userLikesCount = pinterest1.getUser().getLikesCount();
 		int userRealLikesCount = 0;
 		for(Pin pin : pinterest1.getUser().getLikes()) {
 			pin.getId();
 			++userRealLikesCount;
 		}
 		int pinCountToCreate = 3;
 		NewBoardImpl newBoard = new NewBoardImpl(UUID.randomUUID().toString(), BoardCategory.CARS_MOTORCYCLES);
 		Board board = pinterest2.createBoard(newBoard);
 		
 		String newDescription = UUID.randomUUID().toString();
 		NewPinImpl newPin = new NewPinImpl(newDescription, 0, webLink, null, imageFile);
 		for(int i = 0; i < pinCountToCreate; ++i) {
 			pinterest1.likePin(pinterest2.addPin(board, newPin));
 		}
 		assertEquals(userLikesCount + pinCountToCreate, pinterest1.getUser().refresh().getLikesCount());
 		
 		int page = 1;
 		List<Pin> pins = pinterest1.getUser().getLikes(page);
 		int counter = pins.size();
 		
 		while(pins.size() > 0) {
 			++page;
 			pins = pinterest1.getUser().getLikes(page);
 			counter += pins.size();
 		}
 		assertEquals(userRealLikesCount + pinCountToCreate, counter);
 		
 		counter = 0;
 		for(Pin pin : pinterest1.getUser().getLikes()) {
 			pin.getId();
 			++counter;
 		}
 		assertEquals(userRealLikesCount + pinCountToCreate, counter);
 		
 		pinterest2.deleteBoard(board);
 	}
 	
 	@Test
 	public void activityTest() throws InterruptedException {
 		//Create board
 		NewBoardImpl newBoard = new NewBoardImpl(UUID.randomUUID().toString(), BoardCategory.CARS_MOTORCYCLES);
 		Board createdBoard = pinterest1.createBoard(newBoard);
 		Board createdBoard2 = pinterest2.createBoard(newBoard);
 		//Create pin
 		String newDescription = UUID.randomUUID().toString();
 		double newPrice = 10;
 		NewPinImpl newPin = new NewPinImpl(newDescription, newPrice, webLink, imageLink, null);
 		Pin createdPin = pinterest1.addPin(createdBoard, newPin);
 		Pin createdPin2 = pinterest2.addPin(createdBoard2, newPin);
 		int waitPeriod = 2000;
 		Thread.sleep(waitPeriod);
 		//Repin
 		Pin repinedPin = pinterest1.repin(createdPin2, createdBoard, UUID.randomUUID().toString());
 		Thread.sleep(waitPeriod);
 		//Like
 		pinterest1.likePin(createdPin);
 		Thread.sleep(waitPeriod);
 		//Comment
 		String comment = UUID.randomUUID().toString();
		pinterest1.addComment(createdPin2, comment);
 		Thread.sleep(waitPeriod);
 		//Follow user
 		pinterest1.unfollowUser(pinterest2.getUser());
 		pinterest1.followUser(pinterest2.getUser());
 		Thread.sleep(waitPeriod);
 		//Follow board
 		pinterest1.followBoard(createdBoard2);
 		Thread.sleep(waitPeriod);
 		
 		Map<ActivityType, Activity> activityMap = new HashMap<ActivityType, Activity>();
 		for(Activity activity : pinterest1.getUser().getActivity()) {
 			activityMap.put(activity.getActivityType(), activity);
 		}
 		
 		assertEquals(createdBoard2, ((FollowBoardActivity)activityMap.get(ActivityType.FOLLOW_BOARD)).getBoard());
 		
 		assertEquals(pinterest2.getUser(), ((FollowUserActivity)activityMap.get(ActivityType.FOLLOW_USER)).getUser());
 		
		assertEquals(createdPin2, ((CommentActivity)activityMap.get(ActivityType.COMMENT)).getPin());
 		assertEquals(comment, ((CommentActivity)activityMap.get(ActivityType.COMMENT)).getCommentMessage());
 		
 		assertEquals(createdPin, ((PinActivity)activityMap.get(ActivityType.LIKE)).getPin());
 		
 		assertEquals(repinedPin, ((PinActivity)activityMap.get(ActivityType.REPIN)).getPin());
 		
 		assertEquals(createdPin, ((PinActivity)activityMap.get(ActivityType.PIN)).getPin());
 		
 		assertEquals(createdBoard, ((CreateBoardActivity)activityMap.get(ActivityType.CREATE_BOARD)).getBoard());
 		
 		pinterest1.deleteBoard(createdBoard);
 		pinterest2.deleteBoard(createdBoard2);
 	}
 	
 }
