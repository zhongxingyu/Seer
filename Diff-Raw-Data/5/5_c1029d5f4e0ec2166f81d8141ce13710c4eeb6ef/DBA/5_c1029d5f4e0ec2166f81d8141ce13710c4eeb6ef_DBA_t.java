 package org.footware.server;
 
 import org.footware.server.db.Comment;
 import org.footware.server.db.Tag;
 import org.footware.server.db.Track;
 import org.footware.server.db.User;
 import org.footware.server.db.util.HibernateUtil;
 import org.footware.server.db.util.UserUtil;
 import org.footware.shared.dto.TagDTO;
 import org.footware.shared.dto.UserDTO;
 import org.hibernate.HibernateException;
 import org.hibernate.classic.Session;
 import org.junit.Test;
 
 public class DBA {
 
 	private Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 
 //	@Before
 //	public void setUp() throws Exception {
 //	}
 //
 //	@After
 //	public void tearDown() throws Exception {
 //	}
 	
 	@Test
 	public void t10_newUser() {
 		String email = "test@footware.org";
 		String pwd = "test";
 		UserDTO new_user = new UserDTO();
 		new_user.setEmail(email);
 		new_user.setPassword(pwd);
 		Long id = (Long) session.save(new User(new_user));
 		assert (id != null);
 		
 		try {
 			User u = (User) session.load(User.class, id);
 			assert (u != null);
 			assert (u.getEmail().equals(email));
 		} catch (HibernateException e) {
 			assert (false);
 		}
 	}
 	
 	@Test
 	public void t20_deactivateUser() {
		User u = UserUtil.getByEmail("test@footware.org");
		assert (u != null);
		u.setDisabled(true);
		//TODO
 	}
 	
 	@Test
 	public void t30_addTrack() {
 		User u = UserUtil.getByEmail("test@footware.org");
 		assert (u != null);
 		Track t = new Track(u, "foo", "/foo");
 		
 		Long id = (Long) session.save(t);
 		assert (id != null);
 	}
 	
 	@Test
 	public void t40_addTrackComment() {
 		User u = UserUtil.getByEmail("test@footware.org");
 		assert (u != null);
 
 		Track[] tracks = new Track[0];
 		u.getTracks().toArray(tracks);
 		assert (tracks.length > 0);
 
 		Comment c = new Comment("test comment", u);
 		tracks[0].addComment(c);
 		session.update(tracks[0]);
 
 		Long id = (Long) session.save(c);
 		assert (id != null);
 
 		u = UserUtil.getByEmail("test@footware.org");
 		u.getTracks().toArray(tracks);
 		boolean found = false;
 		for (Comment tc : tracks[0].getComments()) {
 			if (tc.getText().equals("test comment")) {
 				found = true;
 				break;
 			}
 		}
 		assert (found);
 	}
 
 	@Test
 	public void t50_addTag() {
 		TagDTO new_tag = new TagDTO("tag");
 		Long id = (Long) session.save(new Tag(new_tag));
 		assert (id != null);
 
 		Tag t = (Tag) session.load(Tag.class, id);
 		assert (t != null);
 		assert (t.getTag().equals("tag"));
 	}
 
 }
