 package org.eclipse.emf.texo.test.model.samples.employee;
 
 import java.util.Date;
 import javax.persistence.Basic;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.Entity;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import org.eclipse.emf.texo.test.TexoTestObjectConverter;
 import org.eclipse.emf.texo.test.TexoTestQNameConverter;
 import org.eclipse.emf.texo.test.model.base.identifiable.Identifiable;
 import org.eclipse.emf.texo.test.models.annotations.MergeAnnotationOne;
 import org.eclipse.emf.texo.test.models.annotations.MergeAnnotationTwo;
 import org.eclipse.persistence.annotations.Converter;
 import org.eclipse.persistence.annotations.Converters;
 
 /**
  * A representation of the model object '<em><b>Employee</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 @Entity(name = "employee_Employee")
 @DiscriminatorColumn(length = 255)
 @Converters({ @Converter(converterClass = TexoTestObjectConverter.class, name = "TexoTestObjectConverter"),
     @Converter(converterClass = TexoTestQNameConverter.class, name = "TexoTestQNameConverter") })
 public class Employee extends Identifiable {
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Basic(optional = false)
   private String name = null;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Basic(optional = false)
   private int salary = 0;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Basic(optional = false)
   private int age = 0;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Basic(optional = false)
   @Temporal(TemporalType.DATE)
   private Date hireDate = null;
 
   /**
   * @return the hello string
    * @generated
    */
   public String helloWorld() {
     System.err.println("hello, I am Employee ");
     return " Employee";
   }
 
   /**
    * Returns the value of '<em><b>name</b></em>' feature.
    *
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>name</b></em>' feature
    * @generated
    */
   public String getName() {
     return name;
   }
 
   /**
    * Sets the '{@link Employee#getName() <em>name</em>}' feature.
    *
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param newName
    *          the new value of the '{@link Employee#getName() name}' feature.
    * @generated
    */
   public void setName(String newName) {
     name = newName;
   }
 
   /**
    * Returns the value of '<em><b>salary</b></em>' feature.
    *
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>salary</b></em>' feature
    * @generated
    */
   public int getSalary() {
     return salary;
   }
 
   /**
    * Sets the '{@link Employee#getSalary() <em>salary</em>}' feature.
    *
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param newSalary
    *          the new value of the '{@link Employee#getSalary() salary}' feature.
    * @generated
    */
   public void setSalary(int newSalary) {
     salary = newSalary;
   }
 
   /**
    * Returns the value of '<em><b>age</b></em>' feature.
    *
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>age</b></em>' feature
    * @generated
    */
   public int getAge() {
     return age;
   }
 
   /**
    * Sets the '{@link Employee#getAge() <em>age</em>}' feature.
    *
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param newAge
    *          the new value of the '{@link Employee#getAge() age}' feature.
    * @generated
    */
   public void setAge(int newAge) {
     age = newAge;
   }
 
   /**
    * Returns the value of '<em><b>hireDate</b></em>' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>hireDate</b></em>' feature
    * @generatedNot
    */
   @MergeAnnotationOne("should-not-be-removed")
   @MergeAnnotationTwo("should-not-be-removed")
   public Date getHireDate() {
     return hireDate;
   }
 
   /**
    * Sets the '{@link Employee#getHireDate() <em>hireDate</em>}' feature.
    *
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param newHireDate
    *          the new value of the '{@link Employee#getHireDate() hireDate}' feature.
    * @generated
    */
   public void setHireDate(Date newHireDate) {
     hireDate = newHireDate;
   }
 
   /**
    * A toString method which prints the values of all EAttributes of this instance. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String toString() {
     return "Employee " + " [name: " + getName() + "]" + " [salary: " + getSalary() + "]" + " [age: " + getAge() + "]"
         + " [hireDate: " + getHireDate() + "]";
   }
 
   /**
   * @return the boolean value
    * @generated
    */
   public boolean templateIsOverridden() {
     return true;
   }
 
   @MergeAnnotationOne("should-not-be-removed")
   public String methodRemains() {
     return "test that method is not removed";
   }
 
   /**
    * @generatedNOT
    */
   @MergeAnnotationOne("should-not-be-removed")
   public static class Info {
     private String name;
     private String test;
 
     public String getName() {
       return name;
     }
 
     public void setName(String name) {
       this.name = name;
     }
 
     public String getTest() {
       return test;
     }
 
     public void setTest(String test) {
       this.test = test;
     }
   }
 
   /**
    * @generatedNOT
    */
   @MergeAnnotationTwo("should-not-be-removed")
   public static class NotRemoved {
     @MergeAnnotationTwo("should-not-be-removed")
     private String name = "test";
 
     @MergeAnnotationTwo("should-not-be-removed")
     private String test;
 
     /**
      * @return the value
      * @generatedNOT
      */
     @MergeAnnotationTwo("should-not-be-removed")
     public String methodNotRemoved() {
       return "should not go away";
     }
 
     @MergeAnnotationTwo("should-not-be-removed")
     public void setName(String name) {
       this.name = name;
     }
   }
 }
