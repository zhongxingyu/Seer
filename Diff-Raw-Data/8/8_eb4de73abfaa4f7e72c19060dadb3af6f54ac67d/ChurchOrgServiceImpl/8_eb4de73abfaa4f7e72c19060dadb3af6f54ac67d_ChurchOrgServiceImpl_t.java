 package org.emsionline.emsiweb.service.jpa;
 
 import java.util.List;
 
 import org.emsionline.emsiweb.domain.ChurchOrg;
 import org.emsionline.emsiweb.repository.ChurchOrgRepository;
 import org.emsionline.emsiweb.service.ChurchOrgService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.google.common.collect.Lists;
 
 @Service("jpaChurchOrgService")
 @Repository
 @Transactional
 public class ChurchOrgServiceImpl implements ChurchOrgService {
 
 
 	@Autowired
 	private ChurchOrgRepository churchOrgRepository;
 
 	@Transactional(readOnly = true)
 	public List<ChurchOrg> findAll() {
 		return Lists.newArrayList(churchOrgRepository.findAll());
 	}
 
 	@Transactional(readOnly = true)
 	public ChurchOrg findById(Long id) {
 		return churchOrgRepository.findOne(id);
 	}
 
 	public ChurchOrg save(ChurchOrg church_org) {
 		return churchOrgRepository.save(church_org);
 	}
 
 	@Transactional(readOnly = true)
 	public Page<ChurchOrg> findAllByPage(Pageable pageable) {
 		return churchOrgRepository.findAll(pageable);
 	}
 
 }
