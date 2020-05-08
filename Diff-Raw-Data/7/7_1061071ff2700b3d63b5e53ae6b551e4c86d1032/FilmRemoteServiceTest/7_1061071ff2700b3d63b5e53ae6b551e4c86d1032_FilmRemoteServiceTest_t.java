 package com.alextim.diskarchive.dwr.services.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.apache.log4j.Logger;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.alextim.diskarchive.entity.Film;
 import com.alextim.diskarchive.services.IFilmService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={
		"file:src/main/webapp/WEB-INF/hibernate.xml",
		"file:src/main/webapp/WEB-INF/web-application-config.xml"}, 
 		inheritLocations = true)
 public class FilmRemoteServiceTest{
 	public static final Logger log = Logger.getLogger(FilmRemoteServiceTest.class);
 	
 	@Autowired
 	IFilmService filmService;
 	
 	@Test
 	public void testAddFilm() {
 		/*Mockery context = new Mockery();
 		final IFilmRemoteService service = context.mock(IFilmRemoteService.class);
 
 		context.checking(new Expectations() {{
 			oneOf(service).addFilm();
 		}});*/
 
 		List<Film> films = new ArrayList<Film>();
 		
 		FilmRemoteService filmRemoteService = new FilmRemoteService();
 		filmRemoteService.setFilmService(filmService);
 		
 		films = filmService.getFilms();
 		Assert.assertEquals(0, films.size());
 		
 		filmRemoteService.addFilm();
 		
 		films = filmRemoteService.getFilmService().getFilms();
		Assert.assertEquals(1, films.size());
 		
 		//context.assertIsSatisfied();
 	}
 }
