 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Tsoha.service;
 
 import Tsoha.domain.Kommentti;
 import Tsoha.domain.Peli;
 import Tsoha.repository.PeliRepository;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Noemj
  */
 @Transactional
 @Service
 public class PeliServiceImpl implements PeliService {
 
     @Autowired
     PeliRepository peliRepository;
     
     @Autowired
     GenreService genreService;
 
     @Transactional
     @Override
     public void remove(Peli peli) {
         peli.getGenre().getPelit().remove(peli);
         genreService.add(peli.getGenre());
         peli.setGenre(null);
         peliRepository.delete(peli);
     }
 
     @Override
     public List<Peli> listAll() {
         return peliRepository.findAll();
     }
 
     @Override
     public Peli add(Peli peli) throws DataAccessException {
         return (peliRepository.save(peli));
     }
 
     @Override
     public Peli findPeli(Integer peliId) {
         return peliRepository.findOne(peliId);
     }
 
     @Override
     public List<Peli> findByLainassa(String lainassa) {
         return peliRepository.findByLainassa(lainassa);
     }
 }
