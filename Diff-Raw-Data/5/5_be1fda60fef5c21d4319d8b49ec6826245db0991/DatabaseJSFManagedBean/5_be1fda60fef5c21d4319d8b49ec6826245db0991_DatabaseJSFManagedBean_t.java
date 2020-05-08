 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mwr.controller;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import com.mwr.database.*;
 import com.mwr.businesslogic.TokenGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 /**
  *
  * @author Heindrich
  */
 @ManagedBean(name="bean")
 @SessionScoped
 public class DatabaseJSFManagedBean {
 
     private Session session;
     private HibernateUtil helper;
     private Employee employee;
     private Device device; 
     private Devicenotregistered device_not;    
     private List<Devicenotregistered> waitingList;
     private List<Device> deviceList;
     private String mac;
     private String serial;
     private String android;
     private Settings latestSetting;
     private List<Blacklistedapplications> apps;
     private Scanresults results;
     private List<Scanresults> device_results;
     
 
     /**
      * Creates a new instance of DatabaseJSFManagedBean
      */
     public DatabaseJSFManagedBean() {
 
         session = HibernateUtil.getSessionFactory().openSession();
          
     }
     
     public void addToWaitingList(String mac, String android, String serial, String make, String model, String username, String password, String idnumber, String name, String surname) throws NoSuchAlgorithmException
     {   
         TokenGenerator gen = new TokenGenerator();
         String token = gen.generateToken(mac, android, serial);    
         DevicenotregisteredId devicePK = new DevicenotregisteredId(mac,android,serial); 
         device_not = new Devicenotregistered(devicePK, make,model, username,  password, idnumber, name, surname,token);
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(device);
         session.getTransaction().commit();
         session.close();
     }
     
   
     public Employee addEmployee(String username, String password, String name, String surname, String idnumber)
     {
         employee = new Employee(username,password,new Date(),name,surname,idnumber);
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(employee);
         session.getTransaction().commit();
         session.close();
         return employee;
     }
     
     
     
     public void addDevice(Devicenotregistered d)
     {
          
         Employee emp = addEmployee(d.getUsername(),d.getPassword(),d.getName(),d.getSurname(),d.getIdnumber());
         DeviceId id = new DeviceId(d.getId());
         Device dev = new Device(id, emp,d.getMake(),d.getModel(), new Date());
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(dev);
         session.getTransaction().commit();
         session.close();
     }
     
     
     public List getWaitingList()
     {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Devicenotregistered");
         waitingList = query.list();
         return waitingList;
     }
     
     public List getDeviceList()
     {
         session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Device");
         deviceList = query.list();
         return deviceList;
     }
     
     public boolean addScanResults(String mac, String serial,String androidID, boolean rooted, boolean debug, boolean unknown, String installedApps,int api)
     {
         getLatestSetting();
         getApps();
         int totalScore = 0;
         int rootScore = 0;
         int debugScore = 0;
         int unknownScore = 0;
         int appScore = 0;
         int apiScore = 0;
         
         if (rooted)
             rootScore = latestSetting.getRootedWeight();
         if (debug)
             debugScore = latestSetting.getDebugWeight();
         if (unknown)
             unknownScore = latestSetting.getUnknownSourcesWeight();
         
        String blacklistedApps = "";
        String[] appArray = installedApps.split(",");
        for (int i=0;i<appArray.length;i++)
        {
            int k = 0;
            while (k<apps.size())
            {
                if (appArray[i].contains(apps.get(k).getAppName()))
                {
                    
                     blacklistedApps += appArray[i];
                     if (apps.get(k).getAppCategory().equals("Low"))
                         appScore += latestSetting.getLowRiskApp();
                     else if (apps.get(k).getAppCategory().equals("Medium"))
                         appScore += latestSetting.getMediumRiskApp();
                     else if (apps.get(k).getAppCategory().equals("High"))
                         appScore += latestSetting.getHighRiskApp();
                     else if (apps.get(k).getAppCategory().equals("Blocked"))
                         appScore += latestSetting.getBlockedApp();
                }
                   
                k++;
            }
            
        }
 
        apiScore = (api - 17)*latestSetting.getOsweight();       
        totalScore = rootScore + debugScore + unknownScore + appScore + apiScore;
        boolean allowed = false;
        if (totalScore < latestSetting.getAccessScore() )
            allowed = true;
        
        results = new Scanresults(latestSetting, new Date(),rooted, rootScore,debug, debugScore, unknown, unknownScore, blacklistedApps, appScore, Integer.toString(api), apiScore, totalScore, allowed, mac, androidID, serial);
        session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(results);
         session.getTransaction().commit();
         session.close();
         
         return allowed;
         
     }
     
     public Settings getLatestSetting()
     {
         session = helper.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Settings order by SettingDate desc");
         List settings = query.list();
         latestSetting = (Settings)settings.get(0);
         return latestSetting;
     }
     
     public List getApps()
     {
        session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Blacklistedapplications");
         apps = query.list();
         return apps;
     }
     
     public String setDevice(DeviceId id)
     {
         session = helper.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Scanresults where device_MACAddress = :mac and device_UID = :uid and device_SerialNumber = :serial order by Date desc");
         query.setParameter("mac", id.getMacaddress());
         query.setParameter("uid", id.getUid());
         query.setParameter("serial", id.getSerialNumber());
         device_results = query.list();
         return "scan.xhtml";
         
     }
     
     public List getDevice_Results()
     {
         return device_results;
     }
     
     
 
     
   
 
     
 //    public String getDevices()
 //    {
 //        session = helper.getSessionFactory().openSession();
 //        session.beginTransaction();
 //
 //    }
 }
