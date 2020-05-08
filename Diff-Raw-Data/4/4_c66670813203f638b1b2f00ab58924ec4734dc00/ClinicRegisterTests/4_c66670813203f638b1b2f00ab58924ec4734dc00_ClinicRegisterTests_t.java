 package com.heren.his.dao;
 
 import com.heren.his.register.domain.*;
 import org.junit.Test;
 
 import java.util.ArrayList;
import com.heren.his.register.domain.ClinicRegister;
import com.heren.his.register.domain.ClinicRegisterType;
import com.heren.his.register.domain.Department;
import com.heren.his.register.domain.PeriodOfValidity;
 import java.util.Date;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class ClinicRegisterTests extends DomainTests{
 
     private final ClinicRegisterDAO<Department> departmentDAO;
     private final ClinicRegisterDAO<ClinicRegisterType> clinicRegisterTypeDAO;
     private final ClinicRegisterDAO<ClinicRegister> clinicRegisterDAO;
     private final ClinicRegisterDAO<BigDepartment> bigDepartmentDAO;
 
     public ClinicRegisterTests() {
         departmentDAO = new ClinicRegisterDAO<>(entityManager);
         clinicRegisterTypeDAO = new ClinicRegisterDAO<>(entityManager);
         clinicRegisterDAO = new ClinicRegisterDAO<>(entityManager);
         bigDepartmentDAO = new ClinicRegisterDAO<>(entityManager);
     }
 
     @Test
     public void should_persist_clinic_register(){
         Department department = new Department("department");
         departmentDAO.persist(department);
 
         ClinicRegisterType clinicRegisterType = new ClinicRegisterType("clinic_register_type", ClinicRegisterType.Type.EXPERT, department, "digitCode", "pinyinCode");
         clinicRegisterTypeDAO.persist(clinicRegisterType);
 
         ClinicRegister clinicRegister = new ClinicRegister(clinicRegisterType, new PeriodOfValidity(new Date(), PeriodOfValidity.Period.MORNING), 20, 20, false);
         clinicRegisterDAO.persist(clinicRegister);
 
         clearCache();
 
         ClinicRegister savedClinicRegister = clinicRegisterDAO.load(ClinicRegister.class, clinicRegister.getId());
         assertThat(savedClinicRegister.getClinicRegisterType(), is(clinicRegisterType));
     }
 
     @Test
     public void should_get_clinic_registers_by_department(){
         BigDepartment bigDepartment = new BigDepartment("big_department");
         bigDepartmentDAO.persist(bigDepartment);
 
         Department department = new Department("department", bigDepartment);
         departmentDAO.persist(department);
 
         ClinicRegisterType clinicRegisterType1= new ClinicRegisterType("clinic_register_type1", ClinicRegisterType.Type.EXPERT, department, "digitCode1", "pinyinCode1");
         clinicRegisterTypeDAO.persist(clinicRegisterType1);
 
         ClinicRegisterType clinicRegisterType2 = new ClinicRegisterType("clinic_register_type2", ClinicRegisterType.Type.CONSULTING_ROOM, department, "digitCode2", "pinyinCode2");
         clinicRegisterTypeDAO.persist(clinicRegisterType2);
 
         ClinicRegister clinicRegister1 = new ClinicRegister(clinicRegisterType1, new PeriodOfValidity(new Date(), PeriodOfValidity.Period.MORNING), 20, 20, false);
         clinicRegisterDAO.persist(clinicRegister1);
         ClinicRegister clinicRegister2 = new ClinicRegister(clinicRegisterType1, new PeriodOfValidity(new Date(), PeriodOfValidity.Period.AFTERNOON), 10, 10, false);
         clinicRegisterDAO.persist(clinicRegister2);
 
         ClinicRegister clinicRegister3 = new ClinicRegister(clinicRegisterType2, new PeriodOfValidity(new Date(), PeriodOfValidity.Period.MORNING), 20, 20, false);
         clinicRegisterDAO.persist(clinicRegister3);
         ClinicRegister clinicRegister4 = new ClinicRegister(clinicRegisterType2, new PeriodOfValidity(new Date(), PeriodOfValidity.Period.AFTERNOON), 10, 10, false);
         clinicRegisterDAO.persist(clinicRegister4);
 
         clearCache();
 
         ArrayList<ClinicRegister> clinicRegisters = new ArrayList<>();
         BigDepartment savedBigDepartment = bigDepartmentDAO.load(BigDepartment.class, bigDepartment.getId());
         for (Department _department : savedBigDepartment.getDepartments()) {
             for (ClinicRegisterType clinicRegisterType : _department.getClinicRegisterTypes()) {
                 for (ClinicRegister clinicRegister : clinicRegisterType.getClinicRegisters()) {
                     clinicRegisters.add(clinicRegister);
                 }
             }
         }
 
         assertThat(clinicRegisters.size(), is(4));
     }
 }
