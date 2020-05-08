 package com.nelsonjrodrigues.twitter.services;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 
 import com.nelsonjrodrigues.twitter.data.model.Follower;
 import com.nelsonjrodrigues.twitter.data.model.User;
 import com.nelsonjrodrigues.twitter.repositories.FollowerRepository;
 import com.nelsonjrodrigues.twitter.repositories.UserRepository;
 
 @Service
 @Transactional
 public class UserServiceImpl implements UserService {
 
 	@Autowired
 	private UserRepository userRepository;
 
 	@Autowired
 	private FollowerRepository followerRepository;
 
 	@Override
 	public List<User> followers(String username) {
 		Assert.hasText(username);
 
 		// make sure id's are valid
 		User user = userRepository.findByUsername(username);
 
 		return followerRepository.findFollowers(user.getId());
 	}
 
 	@Override
 	public List<User> following(String username) {
 		Assert.hasText(username);
 
 		// make sure id's are valid
 		User follower = userRepository.findByUsername(username);
 
 		return followerRepository.findFollowing(follower.getId());
 	}
 
 	@Override
 	public void follow(String followerUsername, String username) {
 		Assert.hasText(followerUsername);
 		Assert.hasText(username);
 
 		// make sure id's are valid
 		User followerUser = userRepository.findByUsername(followerUsername);
		User followedUser = userRepository.findByUsername(username);
 
 		Follower follower = new Follower();
 		follower.setFollowerId(followerUser.getId());
 		follower.setUserId(followedUser.getId());
 
 		followerRepository.save(follower);
 	}
 
 	@Override
 	public void unfollow(String followerUsername, String username) {
 		Assert.hasText(followerUsername);
 		Assert.hasText(username);
 
 		// make sure id's are valid
		User followerUser = userRepository.findByUsername(followerUsername);
		User followedUser = userRepository.findByUsername(username);
 
 		Follower findFollower = followerRepository.findFollower(followedUser.getId(), followerUser.getId());
 
 		followerRepository.delete(findFollower);
 	}
 
 }
