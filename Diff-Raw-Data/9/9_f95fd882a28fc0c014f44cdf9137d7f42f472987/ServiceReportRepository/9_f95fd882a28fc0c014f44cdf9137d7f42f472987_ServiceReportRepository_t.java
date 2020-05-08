 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.ithb.si.made.mtmgmt.core.persistence.repository;
 
 import java.util.List;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.MachineModelPartEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.ServiceReportEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.SpbuEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.SpbuMachineEntity;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.jpa.repository.JpaRepository;
 
 /**
  *
  * @author Uyeee
  */
 public interface ServiceReportRepository extends JpaRepository<ServiceReportEntity, Long> {
 
 	List<ServiceReportEntity> findBySpbuMachineEntity_SpbuEntity(SpbuEntity spbuEntity, Sort sort);
 
 	List<ServiceReportEntity> findBySpbuMachineEntity(SpbuMachineEntity spbuMachineEntity, Sort sort);
 
 	List<ServiceReportEntity> findBySpbuMachineEntityAndMachineModelPartEntity(SpbuMachineEntity spbuMachineEntity, MachineModelPartEntity machineModelPartEntity, Sort sort);
 
	List<ServiceReportEntity> findBySpbuMachineEntityAndMachineModelPartEntity(SpbuMachineEntity spbuMachineEntity, MachineModelPartEntity machineModelPartEntity, Pageable pageable);
 }
