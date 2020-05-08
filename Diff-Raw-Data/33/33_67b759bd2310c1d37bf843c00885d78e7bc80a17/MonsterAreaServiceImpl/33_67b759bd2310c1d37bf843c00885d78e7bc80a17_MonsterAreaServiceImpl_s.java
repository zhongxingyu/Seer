 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.muni.fi.pa165.service.impl;
 
 import com.muni.fi.pa165.dao.MonsterAreaDao;
 import com.muni.fi.pa165.service.MonsterAreaService;
 import com.muni.fi.pa165.dto.MonsterAreaDto;
 import com.muni.fi.pa165.entities.Monsterarea;
 import javax.inject.Inject;
 import org.dozer.Mapper;
 import java.util.ArrayList;
 import java.util.List;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author irina
  */
 @Transactional
 public class MonsterAreaServiceImpl implements MonsterAreaService {
     //private static final Logger logger = Logger.getLogger(MonsterServiceImpl.class.getName());
 
     @Inject
     private MonsterAreaDao monsterAreaDao;
     @Inject
     private Mapper mapper;
 
     @Override
     public MonsterAreaDto save(MonsterAreaDto dto) {
         Monsterarea entity = mapper.map(dto, Monsterarea.class);
         monsterAreaDao.save(entity);
         return mapper.map(entity, MonsterAreaDto.class);
 
     }
 
     @Override
     public MonsterAreaDto update(MonsterAreaDto dto) {
 
         Monsterarea entity = mapper.map(dto, Monsterarea.class);
         monsterAreaDao.update(entity);
         return mapper.map(entity, MonsterAreaDto.class);
 
     }
 
     @Override
     public void delete(MonsterAreaDto dto) {
 
         monsterAreaDao.delete(mapper.map(dto, Monsterarea.class));
 
     }
 
     public void setDao(MonsterAreaDao dao) {
         this.monsterAreaDao = dao;
     }
 
     public void setMapper(Mapper mapper) {
         this.mapper = mapper;
     }
 
     @Override
     public List<MonsterAreaDto> findByMonsterId(Long id) {
         List<Monsterarea> ma = monsterAreaDao.getByMonsterId(id);
         List<MonsterAreaDto> result = new ArrayList<>();
         for (Monsterarea monsterA : ma) {
             result.add(mapper.map(monsterA, MonsterAreaDto.class));
         }
         return result;
     }
 
     @Override
     public List<MonsterAreaDto> findByAreaId(Long id) {
         List<Monsterarea> ma = monsterAreaDao.getByAreaId(id);
         List<MonsterAreaDto> result = new ArrayList<>();
         for (Monsterarea monsterA : ma) {
             result.add(mapper.map(monsterA, MonsterAreaDto.class));
         }
         return result;
     }
 
     @Override
     public List<MonsterAreaDto> findAll() {
         List<MonsterAreaDto> dtoList = new ArrayList<>();
         for (Monsterarea o : monsterAreaDao.findAll()) {
             dtoList.add(this.mapper.map(o, MonsterAreaDto.class));
         }
         return dtoList;
     }
 
     @Override
     public void delete(Long id) {
         monsterAreaDao.delete(id);
     }
 }
