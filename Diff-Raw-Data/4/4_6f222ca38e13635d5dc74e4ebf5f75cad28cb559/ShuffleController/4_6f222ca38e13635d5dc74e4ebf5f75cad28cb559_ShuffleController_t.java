 package org.tekila.musikjunker.web.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.tekila.musikjunker.domain.Resource;
 import org.tekila.musikjunker.domain.TypeResource;
 import org.tekila.musikjunker.repository.HibernateRepository;
 
 /**
  * @author lc
  *
  */
 @Controller
 public class ShuffleController {
 
 	@Autowired
 	private HibernateRepository hibernateRepository;
 	
 	@ResponseBody
 	@RequestMapping(value="/random", method=RequestMethod.GET)
 	public List<Resource> random(@RequestParam(value="n", required=false, defaultValue="10") int size) {
 		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
 		crit.setProjection(Projections.max("id"));
 		
 		long maxId = hibernateRepository.findNumber(crit);
 		List<Resource> lr = new ArrayList<Resource>();
 		Random r = new Random();
 		
 		// do max 100 requests
 		int maxTries = 10*size;
 		int nbTries = 0;
 		while (lr.size() < size && nbTries++ < maxTries) {
			Long id = (long) r.nextInt(1 + (int) maxId);
 			Resource rr = hibernateRepository.get(Resource.class, id);
 			if (rr != null && rr.getType() == TypeResource.AUDIO) {
 				lr.add(rr);
 			}
 		}
 		
 		return lr;
 	}
 
 
 	@ResponseBody
 	@RequestMapping(value="/genre/random", method=RequestMethod.GET)
 	public List<Resource> randomByGenre(@RequestParam(value="n", required=false, defaultValue="10") int size,
 			@RequestParam(value="genre", required=true) String genre) {
 		
 		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
 		crit.add(Restrictions.ilike("metadata.genre", genre, MatchMode.ANYWHERE));
 		List<Resource> raw = hibernateRepository.findByCriteria(crit);
 		Collections.shuffle(raw);
 		return raw.subList(0, Math.min(size, raw.size()));
 	}
 
 
 }
