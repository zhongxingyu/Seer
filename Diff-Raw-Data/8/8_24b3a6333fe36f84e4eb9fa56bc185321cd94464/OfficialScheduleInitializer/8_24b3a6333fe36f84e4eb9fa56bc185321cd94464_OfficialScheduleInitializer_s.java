 package com.raccoonfink.cruisemonkey.server;
 
 import java.util.Date;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.raccoonfink.cruisemonkey.dao.EventDao;
 import com.raccoonfink.cruisemonkey.model.Event;
 
 public class OfficialScheduleInitializer implements InitializingBean {
 	@Autowired
 	EventDao m_eventDao;
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (m_eventDao.findAll().size() == 0) {
			m_eventDao.save(new Event(null, "CocoCay - Bahamas", "Surrounded by the gentle, translucent waters of the Bahamas chain lies the secluded island of Coco Cay®. With its white-sand beaches and spectacular surroundings, CocoCay® is a wonderland of adventure. Reserved exclusively for Royal Caribbean guests, this tropical paradise has recently been updated with new aquatic facilities, nature trails and a ton of great places to just sit back, relax and enjoy a tropical drink.", new Date(1360584000000L), new Date(1360616400000L), "official"));
			m_eventDao.save(new Event(null, "Charlotte Amalie - St. Thomas", "St. Thomas is known as an idyllic vacation spot today, but its history is not so peaceful. In the 18th century, the island was at the center of a bustling pirate culture, as swashbuckling pirates such as Blackbeard and Drake traded stolen wares in the port of Charlotte Amalie. This world-renowned Caribbean island is home to amazing beaches, gorgeous sea and landscapes and unbelievable duty-free shopping.", new Date(1360771200000L), new Date(1360796400000L), "official"));
			m_eventDao.save(new Event(null, "Philipsburg - St. Maarten", "When the Spanish closed their colonial fort on St. Maarten in 1648, a few Dutch and French soldiers hid on the island and decided to share it. Soon after, the Netherlands and France signed a formal agreement to split St. Maarten in half, as it is today. Philipsburg displays its Dutch heritage in its architecture and landscaping. The island offers endless stretches of beach, beautiful landscapes and great shopping.", new Date(1360846800000L), new Date(1360879200000L), "official"));
 		}
 	}
 
 }
