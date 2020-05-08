 package net.java.dev.cejug.classifieds.server.ejb3.bean;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
import net.java.dev.cejug.classifieds.server.ejb3.entity.CustomerEntity;
 import net.java.dev.cejug.classifieds.server.generated.contract.Channel;
 import net.java.dev.cejug.classifieds.server.generated.contract.Item;
 
 @Stateless
 public class RssChannelDao implements DomainModelFacade<Channel> {
 
 	@PersistenceContext(unitName = "classifieds")
 	private EntityManager manager;
 
 	@Override
 	public Channel create() throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void delete(Channel type) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public List<Channel> get(String query, int limit) throws Exception {
 		// TODO: DB access to retrieve channel data.
 
 		Channel channel = new Channel();
 		Item item = new Item();
		item.setAuthor(new CustomerEntity());
 		item.setTitle("RSS Example");
 		item.setDescription("This is an example of an Item");
 
 		channel.getItem().add(item);
 		channel.setTitle("kk");
 		channel.setDescription("this is a channel description");
 		channel.setLink("https://cejug-classifieds.dev.java.net/");
 		channel.setLastBuildDate(new Date().toString());
 		channel.setPubDate(new Date().toString());
 
 		List<Channel> response = new ArrayList<Channel>();
 		// response.add(channel);
 		return response;
 	}
 
 	@Override
 	public List<Channel> getAll(int limit) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void update(Channel entity) throws Exception {
 		manager.persist(entity);
 	}
 }
