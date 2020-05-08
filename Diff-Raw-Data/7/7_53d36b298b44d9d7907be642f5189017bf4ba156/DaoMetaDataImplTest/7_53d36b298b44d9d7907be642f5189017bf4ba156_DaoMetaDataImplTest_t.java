 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dao.annotation.tiger.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.seasar.dao.AnnotationReaderFactory;
 import org.seasar.dao.DaoMetaData;
 import org.seasar.dao.IllegalSignatureRuntimeException;
 import org.seasar.dao.SqlCommand;
 import org.seasar.dao.dbms.Oracle;
 import org.seasar.dao.impl.BeanArrayMetaDataResultSetHandler;
 import org.seasar.dao.impl.BeanListMetaDataResultSetHandler;
 import org.seasar.dao.impl.BeanMetaDataResultSetHandler;
 import org.seasar.dao.impl.DaoMetaDataImpl;
 import org.seasar.dao.impl.DeleteBatchAutoStaticCommand;
 import org.seasar.dao.impl.InsertAutoDynamicCommand;
import org.seasar.dao.impl.InsertBatchAutoDynamicCommand;
 import org.seasar.dao.impl.SelectDynamicCommand;
 import org.seasar.dao.impl.UpdateBatchAutoStaticCommand;
 import org.seasar.dao.impl.UpdateDynamicCommand;
 import org.seasar.dao.unit.S2DaoTestCase;
 import org.seasar.extension.jdbc.impl.ObjectResultSetHandler;
 import org.seasar.framework.beans.BeanDesc;
 import org.seasar.framework.beans.PropertyDesc;
 import org.seasar.framework.beans.factory.BeanDescFactory;
 
 /**
  * @author higa
  *  
  */
 public abstract class DaoMetaDataImplTest extends S2DaoTestCase {
 
     /**
      * Constructor for InvocationImplTest.
      * 
      * @param arg0
      */
     public DaoMetaDataImplTest(String arg0) {
         super(arg0);
     }
 
     public static void main(String[] args) {
         junit.textui.TestRunner.run(DaoMetaDataImplTest.class);
     }
 
     protected abstract Class getDaoClass(String className);
 
     protected abstract Class getBeanClass(String className);
 
     protected abstract Object getBean(String className);
 
     public void testSelectBeanList() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getAllEmployees");
         assertNotNull("1", cmd);
         assertEquals("2", "SELECT * FROM emp", cmd.getSql());
         BeanListMetaDataResultSetHandler rsh = (BeanListMetaDataResultSetHandler) cmd
             .getResultSetHandler();
         assertEquals("3", getBeanClass("Employee"), rsh.getBeanMetaData()
             .getBeanClass());
     }
 
     public void testSelectBeanArray() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getAllEmployeeArray");
         assertNotNull("1", cmd);
         BeanArrayMetaDataResultSetHandler rsh = (BeanArrayMetaDataResultSetHandler) cmd
             .getResultSetHandler();
         assertEquals("2", getBeanClass("Employee"), rsh.getBeanMetaData()
             .getBeanClass());
     }
 
     public void testSelectBean() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployee");
         assertNotNull("1", cmd);
         assertEquals("2", BeanMetaDataResultSetHandler.class, cmd
             .getResultSetHandler().getClass());
         assertEquals("3", "empno", cmd.getArgNames()[0]);
     }
 
     public void testSelectObject() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getCount");
         assertNotNull("1", cmd);
         assertEquals("2", ObjectResultSetHandler.class, cmd
             .getResultSetHandler().getClass());
         assertEquals("3", "SELECT COUNT(*) FROM emp", cmd.getSql());
     }
 
     public void testUpdate() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeDao"));
         UpdateDynamicCommand cmd = (UpdateDynamicCommand) dmd
             .getSqlCommand("update");
         assertNotNull("1", cmd);
         assertEquals("2", "employee", cmd.getArgNames()[0]);
     }
 
     public void testInsertAutoTx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         InsertAutoDynamicCommand cmd = (InsertAutoDynamicCommand) dmd
             .getSqlCommand("insert");
         assertNotNull("1", cmd);
         Object emp = getBean("Employee");
         setProperty(emp, "empno", new Integer(99));
         setProperty(emp, "ename", "hoge");
         cmd.execute(new Object[] { emp });
     }
 
     private void setProperty(Object obj, String name, Object value) {
         BeanDesc desc = BeanDescFactory.getBeanDesc(obj.getClass());
         PropertyDesc propertyDesc = desc.getPropertyDesc(name);
         propertyDesc.setValue(obj, value);
     }
 
     private Object getProperty(Object obj, String name) {
         BeanDesc desc = BeanDescFactory.getBeanDesc(obj.getClass());
         PropertyDesc propertyDesc = desc.getPropertyDesc(name);
         return propertyDesc.getValue(obj);
     }
 
     public void testUpdateAutoTx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SqlCommand cmd = dmd.getSqlCommand("update");
         assertNotNull("1", cmd);
         SelectDynamicCommand cmd2 = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployee");
         Object emp = cmd2.execute(new Object[] { new Integer(7788) });
         setProperty(emp, "ename", "hoge2");
         cmd.execute(new Object[] { emp });
     }
 
     public void testDeleteAutoTx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SqlCommand cmd = dmd.getSqlCommand("delete");
         assertNotNull("1", cmd);
         SelectDynamicCommand cmd2 = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployee");
         Object emp = cmd2.execute(new Object[] { new Integer(7788) });
         cmd.execute(new Object[] { emp });
     }
 
     public void testIllegalAutoUpdateMethod() throws Exception {
         try {
             createDaoMetaData(getDaoClass("IllegalEmployeeAutoDao"));
             fail("1");
         } catch (IllegalSignatureRuntimeException ex) {
             System.out.println(ex.getSignature());
             System.out.println(ex);
         }
     }
 
     public void testSelectAuto() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployeeByDeptno");
         System.out.println(cmd.getSql());
     }
 
     public void testInsertBatchAuto() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
        InsertBatchAutoDynamicCommand cmd = (InsertBatchAutoDynamicCommand) dmd
             .getSqlCommand("insertBatch");
         assertNotNull("1", cmd);
     }
 
     public void testUpdateBatchAuto() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         UpdateBatchAutoStaticCommand cmd = (UpdateBatchAutoStaticCommand) dmd
             .getSqlCommand("updateBatch");
         assertNotNull("1", cmd);
     }
 
     public void testDeleteBatchAuto() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         DeleteBatchAutoStaticCommand cmd = (DeleteBatchAutoStaticCommand) dmd
             .getSqlCommand("deleteBatch");
         assertNotNull("1", cmd);
     }
 
     public void testCreateFindCommand() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
 
         SqlCommand cmd = dmd.createFindCommand(null);
         List employees = (List) cmd.execute(null);
         System.out.println(employees);
         assertTrue("1", employees.size() > 0);
     }
 
     public void testCreateFindCommand2() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SqlCommand cmd = dmd.createFindCommand(null);
         List employees = (List) cmd.execute(null);
         System.out.println(employees);
         assertTrue("1", employees.size() > 0);
     }
 
     public void testCreateFindCommand3() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
 
         SqlCommand cmd = dmd.createFindCommand("select * from emp");
         List employees = (List) cmd.execute(null);
         System.out.println(employees);
         assertTrue("1", employees.size() > 0);
     }
 
     public void testCreateFindCommand4() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
 
         SqlCommand cmd = dmd.createFindCommand("order by empno");
         List employees = (List) cmd.execute(null);
         System.out.println(employees);
         assertTrue("1", employees.size() > 0);
     }
 
     public void testCreateFindCommand5() throws Exception {
         DaoMetaDataImpl dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
 
         dmd.setDbms(new Oracle());
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .createFindCommand("empno = ?");
         System.out.println(cmd.getSql());
         assertTrue("1", cmd.getSql().endsWith(" AND empno = ?"));
     }
 
     public void testCreateFindBeanCommand() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
 
         SqlCommand cmd = dmd.createFindBeanCommand("empno = ?");
         Object employee = cmd.execute(new Object[] { new Integer(7788) });
         System.out.println(employee);
         assertNotNull("1", employee);
     }
 
     public void testCreateObjectBeanCommand() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
 
         SqlCommand cmd = dmd
             .createFindObjectCommand("select count(*) from emp");
         Integer count = (Integer) cmd.execute(null);
         assertEquals("1", 14, count.intValue());
     }
 
     public void testSelectAutoByQuery() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SqlCommand cmd = dmd.getSqlCommand("getEmployeesBySal");
         List employees = (List) cmd.execute(new Object[] { new Integer(0),
             new Integer(1000) });
         System.out.println(employees);
         assertEquals("1", 2, employees.size());
     }
 
     public void testSelectAutoByQueryMultiIn() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployeesByEnameJob");
         System.out.println(cmd.getSql());
         List enames = new ArrayList();
         enames.add("SCOTT");
         enames.add("MARY");
         List jobs = new ArrayList();
         jobs.add("ANALYST");
         jobs.add("FREE");
         List employees = (List) cmd.execute(new Object[] { enames, jobs });
         System.out.println(employees);
         //assertEquals("1", 2, employees.size());
     }
 
     public void testRelation() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("Employee2Dao"));
 
         SqlCommand cmd = dmd.getSqlCommand("getAllEmployees");
         List emps = (List) cmd.execute(null);
         System.out.println(emps);
         assertTrue("1", emps.size() > 0);
     }
 
     public void testGetDaoInterface() throws Exception {
         DaoMetaDataImpl dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         assertEquals("1", EmployeeDao.class, dmd
             .getDaoInterface(EmployeeDao.class));
         assertEquals("2", EmployeeDao.class, dmd
             .getDaoInterface(EmployeeDaoImpl.class));
     }
 
     public void testAutoSelectSqlByDto() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployeesBySearchCondition");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
         EmployeeSearchCondition dto = new EmployeeSearchCondition();
         dto.setDname("RESEARCH");
         List employees = (List) cmd.execute(new Object[] { dto });
         assertTrue("2", employees.size() > 0);
     }
 
     public void testAutoSelectSqlByDto2() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployeesByEmployee");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
         Object dto = getBean("Employee");
         setProperty(dto, "job", "MANAGER");
         List employees = (List) cmd.execute(new Object[] { dto });
         System.out.println(employees);
         //assertTrue("2", employees.size() > 0);
     }
 
     public void testAutoSelectSqlByDto3() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("Employee3Dao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployees");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
         Object dto = getBean("Employee3");
         BeanDesc desc = BeanDescFactory.getBeanDesc(dto.getClass());
         PropertyDesc propertyDesc = desc.getPropertyDesc("manager");
         propertyDesc.setValue(dto, (new Short((short) 7902)));
         List employees = (List) cmd.execute(new Object[] { dto });
         System.out.println(employees);
         assertTrue("2", employees.size() > 0);
     }
 
     public void testAutoSelectSqlByDto4() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("Employee3Dao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployees2");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
         assertTrue("2", cmd.getSql().endsWith(" ORDER BY empno"));
     }
 
     public void testAutoSelectSqlByDto5() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployeesBySearchCondition2");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
         Object dto = getBean("EmployeeSearchCondition");
         Object department = getBean("Department");
         setProperty(department, "dname", "RESEARCH");
         setProperty(dto, "department", department);
         List employees = (List) cmd.execute(new Object[] { dto });
         assertTrue("2", employees.size() > 0);
     }
 
     public void testAutoSelectSqlByDto6() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployeesBySearchCondition2");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
         Object dto = getBean("EmployeeSearchCondition");
         setProperty(dto, "department", null);
         List employees = (List) cmd.execute(new Object[] { dto });
         assertEquals("2", 0, employees.size());
     }
 
     public void testSelfReference() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("Employee4Dao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployee");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
         Object employee = cmd.execute(new Object[] { new Integer(7788) });
         System.out.println(employee);
         Object parent = getProperty(employee, "parent");
         assertEquals("2", new Long(7566), getProperty(parent, "empno"));
     }
 
     public void testSelfMultiPk() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("Employee5Dao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployee");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
     }
 
     public void testNotHavePrimaryKey() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("DepartmentTotalSalaryDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getTotalSalaries");
         assertNotNull("1", cmd);
         System.out.println(cmd.getSql());
         List result = (List) cmd.execute(null);
         System.out.println(result);
     }
 
     public void testSelectAutoFullColumnName() throws Exception {
         DaoMetaData dmd = createDaoMetaData(getDaoClass("EmployeeAutoDao"));
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployee");
         System.out.println(cmd.getSql());
     }
 
     public void testStartsWithOrderBy() throws Exception {
         DaoMetaDataImpl dmd = createDaoMetaData(getDaoClass("Employee6Dao"));
         Object condition = getBean("EmployeeSearchCondition");
         setProperty(condition, "dname", "RESEARCH");
         SelectDynamicCommand cmd = (SelectDynamicCommand) dmd
             .getSqlCommand("getEmployees");
         System.out.println(cmd.getSql());
         Object results = cmd.execute(new Object[] { condition });
         setProperty(condition, "orderByString", "ENAME");
         results = cmd.execute(new Object[] { condition });
     }
 
     public void testQueryAnnotationTx() throws Exception {
         DaoMetaDataImpl dmd = createDaoMetaData(getDaoClass("Employee7Dao"));
         SelectDynamicCommand cmd1 = (SelectDynamicCommand) dmd
             .getSqlCommand("getCount");
         UpdateDynamicCommand cmd2 = (UpdateDynamicCommand) dmd
             .getSqlCommand("deleteEmployee");
         System.out.println(cmd1.getSql());
         System.out.println(cmd2.getSql());
         assertEquals(new Integer(14), cmd1.execute(null));
         assertEquals(new Integer(1), cmd2.execute(new Object[] { new Integer(
             7369) }));
         assertEquals(new Integer(13), cmd1.execute(null));
     }
 
 }
