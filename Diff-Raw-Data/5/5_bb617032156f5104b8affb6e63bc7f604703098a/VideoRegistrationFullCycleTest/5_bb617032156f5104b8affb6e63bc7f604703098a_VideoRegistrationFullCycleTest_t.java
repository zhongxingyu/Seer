 	package org.test.streaming;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.test.streaming.monitor.CachoRegistration;
 import org.test.streaming.monitor.Notifier;
 import org.test.streaming.monitor.RegistrationResponse;
 import org.test.streaming.monitor.UserRegistration;
 import org.test.streaming.monitor.VideoRegistration;
 
 public class VideoRegistrationFullCycleTest {
 
 
 	String videoChunks;
 	/**
 	 * El indice tiene que estar levantado. 
 	 */
 	@Test
 	public void testRestrievalPlan(){
 
 		Conf conf = new Conf("/alt-test-conf.properties");
 		String videoFileName = conf.get("test.video.file.name");
 		File video = new File(conf.getCachosDir(), videoFileName);
 		
 		Assert.assertTrue("file: "+video.getAbsolutePath()+" does not exist", video.exists());
 		
 		Notifier notifier = new Notifier(conf);
 
 		/*
 		 * parte 1:
 		 * registro usuario
 		 * el usuario regitra un video
 		 * pide el retrieval plan para ese video
 		 */
 		Assert.assertTrue(StringUtils.isNotEmpty(conf.get("test.user.id")));
 
		User user = new User(conf.get("test.user.id"), "user@test.com", "localhost", "8080", "10002");
 		UserRegistration userRegistration = new UserRegistration(user, conf);
 		userRegistration.go();
 		
 		VideoRegistration videoRegistration = new VideoRegistration(video, conf);
 		RegistrationResponse videoRegistrationResponse = videoRegistration.register();
 		
 		Assert.assertNotNull(videoRegistrationResponse);
 		/*
 		 * no hay error de conexion (CONN_ERROR)
 		 */
 		Assert.assertTrue(videoRegistrationResponse.getCode().equals("OK") || videoRegistrationResponse.getCode().equals("ERROR"));
 		
 		WatchMovieRetrievalPlan retrievalPlan = (WatchMovieRetrievalPlan) notifier.getRetrievalPlan(videoRegistrationResponse.getId(), conf.get("test.user.id"));
 		
 		Assert.assertEquals(retrievalPlan.getRequests().size(), 1);
 		Assert.assertEquals(retrievalPlan.getRequests().get(0).getHost(), user.getIp());
 		Assert.assertEquals(retrievalPlan.getVideoId(), videoRegistrationResponse.getId());
 		Assert.assertEquals(video.length(), retrievalPlan.getRequests().get(0).getRequest().getLength());
 		
 		/*
 		 * registro otro usuario
 		 * el usuario registra un cacho del mismo video
 		 * pido el retrieval plan  
 		 */
		User otroUser = new User("otro-user-test", "otro-user@test.com", "1.1.1.1", "8080", "10002");
 		UserRegistration otherUserRegistration = new UserRegistration(otroUser, conf);
 		otherUserRegistration.go();
 		int chunkLenght = 1024*1024;
 		
 		
 		MovieCachoFile movieCachoFile = new MovieCachoFile(new MovieCacho(0,chunkLenght), video);
 		CachoRegistration cachoRegistration = new CachoRegistration(conf, movieCachoFile, new MovieCachoHasher().hashMovieCachoFile(movieCachoFile, chunkLenght));
 		
 		cachoRegistration.setUserId(otroUser.getId());
 		
 		RegistrationResponse cachoRegistrationResponse = cachoRegistration.register();
 		
 		Assert.assertNotNull(cachoRegistrationResponse);
 		/*
 		 * no hay error de conexion (CONN_ERROR)
 		 */
 		Assert.assertTrue(cachoRegistrationResponse.getCode().equals("OK") || cachoRegistrationResponse.getCode().equals("ERROR"));
 		
 		WatchMovieRetrievalPlan secondRetrievalPlan = (WatchMovieRetrievalPlan) notifier.getRetrievalPlan(videoRegistrationResponse.getId(), otroUser.getId());
 		
 		Assert.assertEquals(secondRetrievalPlan.getRequests().size(), 2);
 		Assert.assertNotSame(secondRetrievalPlan.getVideoId(), cachoRegistrationResponse.getId());
 
 		List<String> ips = (List<String>) CollectionUtils.collect(secondRetrievalPlan.getRequests(), new Transformer(){
 
 			@Override
 			public Object transform(Object input) {
 				CachoRetrieval cachoRetrieval = (CachoRetrieval)input;
 				return cachoRetrieval.getHost();
 			}
 			
 		});
 		
 		Assert.assertTrue(ips.contains(user.getIp()));
 		Assert.assertTrue(ips.contains(otroUser.getIp()));
 		
 		Collection<Integer> lenghts = CollectionUtils.collect(secondRetrievalPlan.getRequests(), new Transformer(){
 
 			@Override
 			public Object transform(Object input) {
 				CachoRetrieval cachoRetrieval = (CachoRetrieval)input;
 				return cachoRetrieval.getRequest().getLength();
 			}
 			
 		});
 
 		Assert.assertTrue(lenghts.contains(chunkLenght));
 		Assert.assertTrue(lenghts.contains(((int)video.length() - chunkLenght)));
 	}
 }
