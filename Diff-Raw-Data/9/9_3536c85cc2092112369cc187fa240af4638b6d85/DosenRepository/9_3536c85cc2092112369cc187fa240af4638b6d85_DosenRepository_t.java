 package id.ac.pcr.springhibernate.repository;
 
 import id.ac.pcr.springhibernate.model.Dosen;
import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Deny Prasetyo
  * Date: 4/4/13
  * Time: 2:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public interface DosenRepository extends PagingAndSortingRepository<Dosen, Long> {

    @Query("SELECT o FROM Dosen o WHERE o.niy=:niy AND o.nama=:asdf")
    List<Dosen> findByNiyAndNama(@Param("niy") String niy, @Param("asdf") String nama);
 }
