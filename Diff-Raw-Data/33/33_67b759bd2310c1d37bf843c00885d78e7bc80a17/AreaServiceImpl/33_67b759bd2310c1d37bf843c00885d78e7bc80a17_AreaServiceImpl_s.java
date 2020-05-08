 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.muni.fi.pa165.service.impl;
 
 import com.muni.fi.pa165.dao.AreaDao;
 import com.muni.fi.pa165.service.AreaService;
 import com.muni.fi.pa165.dto.AreaDto;
 import com.muni.fi.pa165.entities.Area;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 import org.dozer.Mapper;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Auron
  */
 @Transactional
 public class AreaServiceImpl implements AreaService {
 
     private static final Logger logger = Logger.getLogger(AreaServiceImpl.class.getName());
     @Inject
     private AreaDao areaDao;
     @Inject
     private Mapper mapper;
 
     @Override
     public AreaDto save(AreaDto dto) {
 
         Area entity = mapper.map(dto, Area.class);
         areaDao.save(entity);
         return mapper.map(entity, AreaDto.class);
 
 
     }
 
     @Override
     public AreaDto update(AreaDto dto) {
 
         Area entity = mapper.map(dto, Area.class);
         areaDao.update(entity);
         return mapper.map(entity, AreaDto.class);
 
     }
 
     @Override
     public void delete(AreaDto dto) {
 
         areaDao.delete(mapper.map(dto, Area.class));
 
     }
 
     @Override
     public AreaDto findById(Long id) {
 
         return mapper.map(areaDao.findById(id), AreaDto.class);
 
     }
 
     public void setDao(AreaDao dao) {
         this.areaDao = dao;
     }
 
     public void setMapper(Mapper mapper) {
         this.mapper = mapper;
     }
 
     @Override
     public void delete(Long id) {
         areaDao.delete(id);
     }
 
     @Override
     public List<AreaDto> findAll() {
         List<AreaDto> dtoList = new ArrayList<>();
         for (Area o : areaDao.findAll()) {
             dtoList.add(this.mapper.map(o, AreaDto.class));
         }
         return dtoList;
     }
 }
