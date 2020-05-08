 package com.lebk.dao.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.lebk.dao.*;
 import com.lebk.po.Businesstype;
 import com.lebk.po.Ptdetails;
 import com.lebk.po.Ptsize;
 import com.lebk.util.HibernateUtil;
 
 /**
  * Author: lebk.lei@gmail.com Date: 2013-11-7
  */
 public class PtDetailsDaoImpl implements PtDetailsDao
 {
   static Logger logger = Logger.getLogger(PtDetailsDaoImpl.class);
 
   public boolean addPtDetail(Integer poId, Integer btId, Integer pNum, Integer opUserId)
   {
 
     if (pNum <= 0)
     {
       logger.error("the product number should be greater than 0, 0 or negative number is not allowed!, pNum:" + pNum);
       return false;
     }
     Session session = HibernateUtil.getSessionFactory().openSession();
 
     Transaction transaction = null;
 
     try
     {
       logger.info("begin to add the product detail information for poId:" + poId);
 
       transaction = session.beginTransaction();
       Ptdetails pd = new Ptdetails();
 
       pd.setPoId(poId);
       pd.setBtId(btId);
       pd.setNum(pNum);
       pd.setOpUserId(opUserId);
       pd.setDate(new Date());
       session.save(pd);
       transaction.commit();
       logger.info("add product detail successfully");
       return true;
 
     } catch (HibernateException e)
     {
 
       transaction.rollback();
       logger.error(e.toString());
       e.printStackTrace();
 
     } finally
     {
 
       session.close();
 
     }
     logger.info("add product detail fail");
     return false;
   }
 
   public boolean deletePtDetail(Integer id)
   {
 
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = null;
     try
     {
       transaction = session.beginTransaction();
       Ptdetails ps = (Ptdetails) session.get(Ptdetails.class, id);
       session.delete(ps);
       transaction.commit();
       logger.info("delete product detail id: " + id + " successfully");
       return true;
     } catch (HibernateException e)
     {
       transaction.rollback();
       e.printStackTrace();
     } finally
     {
       session.close();
     }
     logger.error("fail to product detail with id: " + id);
 
     return false;
   }
 
   public List<Ptdetails> getAllPtDetails()
   {
     Session session = HibernateUtil.getSessionFactory().openSession();
     List pdl = new ArrayList<Ptdetails>();
     Transaction transaction = null;
 
     try
     {
       transaction = session.beginTransaction();
       List pdq = session.createQuery("from " + Ptdetails.class.getName()).list();
       for (Iterator it = pdq.iterator(); it.hasNext();)
       {
         Ptdetails pd = (Ptdetails) it.next();
         pdl.add(pd);
       }
       transaction.commit();
     } catch (HibernateException e)
     {
       transaction.rollback();
       e.printStackTrace();
 
     } finally
     {
       session.close();
     }
     return pdl;
   }
 
   public List<Ptdetails> getAllPtDetailsbyPoId(Integer poId)
   {
     Session session = HibernateUtil.getSessionFactory().openSession();
     List pdl = new ArrayList<Ptdetails>();
     Transaction transaction = null;
 
     try
     {
       transaction = session.beginTransaction();
       List pdq = session.createQuery("from " + Ptdetails.class.getName() + " where poId=" + poId).list();
       for (Iterator it = pdq.iterator(); it.hasNext();)
       {
         Ptdetails pd = (Ptdetails) it.next();
         pdl.add(pd);
       }
       transaction.commit();
     } catch (HibernateException e)
     {
       transaction.rollback();
       e.printStackTrace();
 
     } finally
     {
       session.close();
     }
     return pdl;
   }
 
   private List<Integer> getAllIdsByPoId(Integer poId)
   {
     List<Integer> idl = new ArrayList<Integer>();
 
     List<Ptdetails> pdl = this.getAllPtDetailsbyPoId(poId);
     Iterator it = pdl.iterator();
     while (it.hasNext())
     {
       Ptdetails pd = (Ptdetails) it.next();
       idl.add(pd.getId());
     }
     return idl;
 
   }
 
   public boolean deletePtDetialByPoId(Integer poId)
   {
 
     List<Integer> idl = this.getAllIdsByPoId(poId);
 
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = null;
     try
     {
       transaction = session.beginTransaction();
       Iterator it = idl.iterator();
       while (it.hasNext())
       {
         Integer id = (Integer) it.next();
         Ptdetails ps = (Ptdetails) session.get(Ptdetails.class, id);
         session.delete(ps);
       }
       transaction.commit();
       logger.info("delete product detail by poId: " + poId + " successfully");
 
       return true;
     } catch (HibernateException e)
     {
       transaction.rollback();
       e.printStackTrace();
     } finally
     {
       session.close();
     }
     logger.error("fail to product detail with poId: " + poId);
 
     return false;
   }
 
   public boolean cleanUpAll()
   {
     List<Integer> idl = new ArrayList<Integer>();
     List<Ptdetails> ptdl = this.getAllPtDetails();
     for (Iterator it = ptdl.iterator(); it.hasNext();)
     {
       Ptdetails pd = (Ptdetails) it.next();
 
       idl.add(pd.getId());
     }
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = null;
     try
     {
       transaction = session.beginTransaction();
       Iterator it = idl.iterator();
       while (it.hasNext())
       {
         Integer id = (Integer) it.next();
         Ptdetails ps = (Ptdetails) session.get(Ptdetails.class, id);
         session.delete(ps);
       }
       transaction.commit();
       return true;
     } catch (HibernateException e)
     {
       transaction.rollback();
       e.printStackTrace();
     } finally
     {
       session.close();
     }
     logger.error("fail to clear all the ptdetails");
 
     return false;
   }
 
   public List<Ptdetails> searchProductDetails(List<Integer> poIdList, Date startDate, Date endDate)
   {
     // build the where clause
     StringBuffer whereClause = new StringBuffer(" where poId in(");
     for (Integer poId : poIdList)
     {
       whereClause.append(poId + ",");
     }
     if (whereClause.lastIndexOf(",") != -1)
     {
       whereClause.delete(whereClause.lastIndexOf(","), whereClause.length());
     }
     whereClause.append(")");
 
     if (startDate != null)
     {
      whereClause.append(" and date(date) >= '" + new java.sql.Date(startDate.getTime()) + "'");
     }
     if (endDate != null)
     {
      whereClause.append(" and date(date) <= '" + new java.sql.Date(endDate.getTime()) + "'");
     }
     logger.info("The where clause is:" + whereClause);
 
     Session session = HibernateUtil.getSessionFactory().openSession();
     List pdl = new ArrayList<Ptdetails>();
     Transaction transaction = null;
 
     try
     {
       transaction = session.beginTransaction();
       List pdq = session.createQuery("from " + Ptdetails.class.getName() + whereClause).list();
       for (Iterator it = pdq.iterator(); it.hasNext();)
       {
         Ptdetails pd = (Ptdetails) it.next();
         pdl.add(pd);
       }
       transaction.commit();
     } catch (HibernateException e)
     {
       transaction.rollback();
       e.printStackTrace();
 
     } finally
     {
       session.close();
     }
     return pdl;
   }
 
 }
