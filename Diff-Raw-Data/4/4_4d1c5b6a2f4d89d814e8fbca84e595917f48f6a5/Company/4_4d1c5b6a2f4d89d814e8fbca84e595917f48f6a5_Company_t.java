 /**
  * Ian Dimayuga
  * EECS293 HW1
  */
 package edu.cwru.icd3;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 import java.util.Set;
 
 /**
 * A data structure to represent a strictly hierarchical organization of employees.
 *
 * @author ian
  */
 public class Company {
     /**
      * Map of employee name to manager name. Each employee has one manager, with the exception of the head.
      */
     private Map<String, String> m_managerMap;
 
     /**
      * Map of employee name to the set of direct subordinates.
      */
     private Map<String, Set<String>> m_employeeMap;
 
     /**
      * Name of head of company.
      */
     private String m_head;
 
     /**
      * Creates an empty Company with no employees.
      */
     public Company() {
         m_managerMap = new HashMap<String, String>();
         m_employeeMap = new HashMap<String, Set<String>>();
         m_head = null;
     }
 
     /**
      * Adds an employee to the company, specifying their manager.
      *
      * @param employee
      *            Name of the employee to be added.
      * @param manager
      *            Name of an existing employee to be the manager of the new employee. A manager of null is used if and
      *            only if the Company is currently empty, and the new employee will be the head.
      * @throws NullPointerException
      *             If employee is null.
      * @throws IllegalArgumentException
      *             If the specified manager is null but the Company is not empty.
      * @throws NoSuchElementException
      *             If the specified manager is nonexistent.
      * @throws IllegalArgumentException
      *             If there is already an employee with that name in the Company.
      */
     public void add(String employee, String manager) {
         // Null-check employee
         if (employee == null) {
             throw new NullPointerException("employee cannot be null");
         }
         if (manager == null) {
             // If manager is null, but the Company isn't empty, throw exception
             if (m_employeeMap.size() > 0) {
                 throw new IllegalArgumentException("manager is null, but Company is not empty.");
             }
         } else if (!m_employeeMap.containsKey(manager)) {
             // If manager doesn't exist in the Company, throw exception
             // This includes the case where the Company is empty but a manager was specified
             throw new NoSuchElementException(String.format("Company has no employee with name %s.", manager));
         }
         // If employee already exists in the Company, throw exception
         if (m_managerMap.containsKey(employee)) {
             throw new IllegalArgumentException(
                     String.format("Company already contains an employee named %s.", employee));
         }
         // Add employee to Company, and to manager's set of direct reports if manager exists
         if (manager != null) {
             m_employeeMap.get(manager).add(employee);
         } else {
             // New employee is the head
             m_head = employee;
         }
         m_employeeMap.put(employee, new HashSet<String>());
         m_managerMap.put(employee, manager);
     }
 
     /**
      * Adds a group of employees to the company, specifying their manager. The Company must be non-empty.
      *
      * @param employees
      *            Names of the employees to be added. Employees already in the Company are ignored.
      * @param manager
      *            Name of an existing employee to be the manager of the new employees. Cannot be null.
      * @throws NullPointerException
      *             If employees is null.
      * @throws IllegalArgumentException
      *             If the specified manager is null.
      * @throws NoSuchElementException
      *             If the specified manager is nonexistent.
      */
     public void addAll(Set<String> employees, String manager) {
         // Null-check manager
         if (manager == null) {
             throw new IllegalArgumentException("manager cannot be null");
         }
         if (!m_employeeMap.containsKey(manager)) {
             // If manager doesn't exist in the Company, throw exception
             throw new NoSuchElementException(String.format("Company has no employee with name %s.", manager));
         }
 
         // Ignore existing employees
         Set<String> culledEmployees = new HashSet<String>(employees);
         culledEmployees.removeAll(m_managerMap.keySet());
 
         // Add remaining employees to Company
         m_employeeMap.get(manager).addAll(culledEmployees);
         for (String employee : culledEmployees) {
             m_managerMap.put(employee, manager);
         }
     }
 
     /**
      * Removes an employee from the Company, including from any list of direct reports. The employee may not be a
      * manager.
      *
      * @param employee
      *            Name of the employee to delete.
      * @throws NullPointerException
      *             If employee is null.
      * @throws NoSuchElementException
      *             If employee is nonexistent.
      * @throws IllegalArgumentException
      *             If the employee has any direct reports.
      */
     public void delete(String employee) {
         // Null-check employee
         if (employee == null) {
             throw new NullPointerException("employee cannot be null");
         }
 
         // Check for existence
         if (!m_managerMap.containsKey(employee)) {
             throw new NoSuchElementException(String.format("Company has no employee with name %s.", employee));
         }
         // Check for direct reports
         if (m_employeeMap.get(employee).size() > 0) {
             throw new IllegalArgumentException(String.format("Employee '%s' has direct reports and cannot be deleted.",
                     employee));
         }
 
         String manager = m_managerMap.get(employee);
         if (manager != null) {
             // Remove employee from manager's department (unless manager is null)
             m_employeeMap.get(manager).remove(employee);
         } else {
             // employee was head of company
             m_head = null;
         }
         // Delete records of employee
         m_managerMap.remove(employee);
         m_employeeMap.remove(employee);
     }
 
     /**
      * Gets the name of an employee's manager.
      *
      * @param employee
      *            The name of the employee to query.
      * @return The manager of the employee, or null if the employee is the head.
      * @throws NullPointerException
      *             If employee is null.
      * @throws NoSuchElementException
      *             If employee is nonexistent.
      */
     public String managerOf(String employee) {
         // Null-check employee
         if (employee == null) {
             throw new NullPointerException("employee cannot be null");
         }
 
         // Check for existence
         if (!m_managerMap.containsKey(employee)) {
             throw new NoSuchElementException(String.format("Company has no employee with name %s.", employee));
         }
 
         // Look up manager
         return m_managerMap.get(employee);
     }
 
     /**
      * Gets the names of all Company managers.
      *
      * @return A set of all employees who have at least one direct report.
      */
     public Set<String> managerSet() {
         return new HashSet<String>(m_managerMap.values());
     }
 
     /**
      * Generates a Company from an employee and all subordinates.
      *
      * @param manager
      *            Name of the employee who is head of the department.
      * @return A new Company with the head and subordinates populated. If manager is nonexistent, an empty Company will
      *         be returned.
      * @throws NullPointerException
      *             If manager is null.
      */
     public Company departmentOf(String manager) {
         // Null-check manager
         if (manager == null) {
             throw new NullPointerException("manager cannot be null");
         }
 
         Company dept = new Company();
 
         // Check for existence
         if (!m_employeeMap.containsKey(manager)) {
             return dept;
         }
 
         dept.add(manager, null);
 
         // Execute a breadth-first traversal starting with manager
         Queue<String> queue = new LinkedList<String>();
         queue.add(manager);
 
         while (queue.size() > 0) {
             String employee = queue.remove();
             dept.addAll(m_employeeMap.get(employee), employee);
             queue.addAll(m_employeeMap.get(employee));
         }
 
         return dept;
     }
 
     /**
      * Get a list of all employees sorted first by the size of their department, then alphabetically by name.
      *
      * @return A list of employes by department size.
      */
     public List<String> managersByDepartmentSize() {
         // Check for empty Company
         if (m_head == null) {
             return new ArrayList<String>();
         }
 
         final Map<String, Integer> subordinates = new HashMap<String, Integer>();
         subordinates.put(m_head, countSubordinates(m_head, subordinates));
 
         // Define Comparator which uses the map and defaults to alphabetical otherwise
         Comparator<String> deptComparator = new Comparator<String>() {
             @Override
             public int compare(String arg0, String arg1) {
                 int comp = subordinates.get(arg0) - subordinates.get(arg1);
                 return comp == 0 ? arg0.compareTo(arg1) : comp;
             }
         };
 
         // Sort a list of managers by the Comparator
         ArrayList<String> managers = new ArrayList<String>(subordinates.keySet());
         Collections.sort(managers, deptComparator);
         return managers;
     }
 
     /**
      * Get the department size of a manager and pass back a map of subordinates with department size.
      *
      * @param manager
      *            Name of the manager.
      * @param subordinates
      *            Output map from subordinates to sizes.
      * @return Total size of the manager's department.
      */
     private int countSubordinates(String manager, Map<String, Integer> subordinates) {
         Set<String> employees = m_employeeMap.get(manager);
 
         if (employees.size() == 0) {
             return 0;
         }
 
         int total = 0;
 
         // Recursively enumerate subordinates
         for (String employee : employees) {
             int subtotal = countSubordinates(employee, subordinates);
             // Add subordinate to map
             subordinates.put(employee, subtotal);
             // Add subordinate's count to own total, including direct subordinate
             total += subtotal + 1;
         }
 
         return total;
     }
 }
