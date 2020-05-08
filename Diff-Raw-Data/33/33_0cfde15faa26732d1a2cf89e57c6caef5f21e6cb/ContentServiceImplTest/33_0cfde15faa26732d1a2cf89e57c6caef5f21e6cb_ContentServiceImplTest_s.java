 package org.safaproject.safa.Service;
 
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.safaproject.safa.dao.ContentDAO;
 import org.safaproject.safa.exception.ContentNotFoundException;
 import org.safaproject.safa.model.content.Comment;
 import org.safaproject.safa.model.content.Content;
 import org.safaproject.safa.model.content.Resource;
 import org.safaproject.safa.model.content.builder.ContentBuilder;
 import org.safaproject.safa.model.indicator.Indicator;
 import org.safaproject.safa.model.tag.Tag;
 import org.safaproject.safa.model.user.User;
 import org.safaproject.safa.service.ContentServiceImpl;
 
 public class ContentServiceImplTest {
 
 	private ContentServiceImpl contentService;
 	private ContentDAO contentDAOMock;
 	private final static Long CONTENT_ID = 1l;
 
 	@Before
 	public void setUp() {
 		contentService = new ContentServiceImpl();
		contentDAOMock = mock(ContentDAO.class);
 	}
 
 	@Test
 	public void shallRetrieveContent() throws ContentNotFoundException {
 
 		final Long id = 1l;
 		final Content content = createContent();
		stub(contentDAOMock.findById(id)).toReturn(content);
 
 		contentService.setContentDAO(contentDAOMock);
 
 		final Content contentRecieved = contentService.get(id);
 
		assertEquals(id, contentRecieved.getContentId());
		assertEquals(content.getDescription(), contentRecieved.getDescription());
		assertEquals(content.getTitle(), contentRecieved.getTitle());
 
 	}
 
 	@Test(expected = ContentNotFoundException.class)
 	public void shallNotFoundContent() throws ContentNotFoundException {
 
		stub(contentDAOMock.findById(CONTENT_ID)).toReturn(null);
 
 		contentService.setContentDAO(contentDAOMock);
 
 		contentService.get(CONTENT_ID);
 
 	}
 
 	private Content createContent() {
 		final Set<Comment> comments = new HashSet<Comment>();
 		final Set<Indicator> indicators = new HashSet<Indicator>();
 		final Set<Resource> resourses = new HashSet<Resource>();
 		final Set<Tag> tags = new HashSet<Tag>();
 		final Resource thumbnail = new Resource();
 		ContentBuilder builder = new ContentBuilder();
 		builder.withAvailable(true).withComments(comments)
 				.withIndicators(indicators).withDescription("Very good")
 				.withResources(resourses).withReviewed(true).withTags(tags)
 				.withThumbnail(thumbnail).withTitle("Titanic")
 				.withUploadDate(new Date()).withUser(new User());
 		final Content content = builder.build();
 		content.setContentId(CONTENT_ID);
 		return content;
 	}
 
 }
