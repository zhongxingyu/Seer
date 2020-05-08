 package org.tekila.musikjunker.web.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.DigestUtils;
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
 		
 		String randomHash = DigestUtils.md5DigestAsHex(Long.toString(System.currentTimeMillis()).getBytes());
 		
 		// do max 10 requests
 		int maxTries = 10;
 		int nbTries = 0;
 		List<Resource> lr = new ArrayList<Resource>();
 		while (lr.size() < size && nbTries++ < maxTries) {
 			DetachedCriteria criteria = DetachedCriteria.forClass(Resource.class);
 			criteria.add(Restrictions.gt("hash", randomHash));
 			criteria.add(Restrictions.eq("type", TypeResource.AUDIO));
 			criteria.add(Restrictions.ne("ignoreShuffle", true));
 			List<Resource> list = hibernateRepository.findByCriteria(criteria, 0, size);
 			lr.addAll(list);
 		}
 		
 		return lr;
 	}
 
 
 	@ResponseBody
 	@RequestMapping(value="/genre/random", method=RequestMethod.GET)
 	public List<Resource> randomByGenre(@RequestParam(value="n", required=false, defaultValue="10") int size,
 			@RequestParam(value="genre", required=true) String genre) {
 		
 		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
 		crit.add(Restrictions.ilike("metadata.genre", genre, MatchMode.ANYWHERE));
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
 		List<Resource> raw = hibernateRepository.findByCriteria(crit);
 		Collections.shuffle(raw);
 		return raw.subList(0, Math.min(size, raw.size()));
 	}
 
 	@ResponseBody
 	@RequestMapping(value="/stars/random", method=RequestMethod.GET)
 	public List<Resource> randomStars(@RequestParam(value="n", required=false, defaultValue="10") int size) {
 		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
 		crit.add(Restrictions.gt("stars", 0));
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
 		List<Resource> raw = hibernateRepository.findByCriteria(crit);
 		Collections.shuffle(raw);
 		return raw.subList(0, Math.min(size, raw.size()));
 	}
 
 	@ResponseBody
 	@RequestMapping(value="/dir/random", method=RequestMethod.GET)
 	public List<Resource> randomFromDir(@RequestParam(value="n", required=false, defaultValue="10") int size,
 			@RequestParam(value="dir", required=true) String dir) {
 		
 		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
 		crit.add(Restrictions.like("path", dir, MatchMode.START));
 		List<Resource> raw = hibernateRepository.findByCriteria(crit);
 		Collections.shuffle(raw);
 		return raw.subList(0, Math.min(size, raw.size()));
 	}
 
 
 
 }
