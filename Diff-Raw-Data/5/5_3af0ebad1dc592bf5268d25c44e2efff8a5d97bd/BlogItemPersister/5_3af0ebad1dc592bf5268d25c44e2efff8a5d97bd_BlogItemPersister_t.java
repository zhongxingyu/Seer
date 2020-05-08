 package com.xebia.blogr.mailextractor;
 
 import org.json.JSONException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.util.Assert;
 
 import com.xebia.blogr.mongo.BlogRepository;
 
 /**
  * Writes {@link BlogItem}s to a {@link BlogRepository}.
  *
  * @author barend
  */
 public class BlogItemPersister {
 
 	private BlogRepository repository;
 
 	/**
 	 * @return the value of repository
 	 */
 	public BlogRepository getRepository() {
 		return repository;
 	}
 
 	/**
 	 * @param repository the new value for {@code repository}
 	 */
 	@Autowired 
 	@Required
 	public void setRepository(BlogRepository repository) {
 		this.repository = repository;
 	}
 	
 	/**
 	 * Writes a {@code BlogItem} to {@link BlogRepository#putJsonItem(String)} as json and returns the generated id. 
 	 * @param input the input blog item.
 	 * @return the generated id.
 	 * @throws JSONException if the blog item could not be converted to json.
 	 */
	public String persist(BlogItem input) throws MailInProcessingException {
 		Assert.notNull(getRepository());
 		return getRepository().putJsonItem(convert(input));
 	}
 
 	/**
 	 * Converts a blogitem to json.
 	 * @param item the blogitem.
 	 * @return the json.
 	 * @throws JSONException if the JSON conversion fails.
 	 */
	private String convert(BlogItem item) throws MailInProcessingException {
 		Assert.notNull(item);
 		return item.toJSON();
 	}
 }
