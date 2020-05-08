 package com.in6k.servlet;
 
 import com.in6k.dao.AccountDAOimpl;
 import com.in6k.dao.TransactionDAOimpl;
 import com.in6k.dao.UserDAOimpl;
 import com.in6k.entity.Account;
 import com.in6k.entity.Transaction;
 import com.in6k.entity.User;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Collection;
 
 public class TransactionServlet extends HttpServlet {
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         Collection<Transaction> transactions = null;
 
         try {
             transactions = TransactionDAOimpl.load();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         request.setAttribute("transactions", transactions);
 
         request.getRequestDispatcher("/Transaction.jsp").include(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         Integer accIdFrom = new Integer(request.getParameter("accIdFrom"));
         Integer accIdTo = new Integer(request.getParameter("accIdTo"));
 
         Account accountFrom = null;
         Account accountTo = null;
 
         try {
             accountFrom = AccountDAOimpl.getById(accIdFrom);
             accountTo = AccountDAOimpl.getById(accIdTo);
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         if (!(accIdFrom.equals(accIdTo))) {
             double summ = Double.parseDouble(request.getParameter("summ"));
 
             Transaction tr = new Transaction();
 
             tr.setCreditAccount(accountTo);
             tr.setDebetAccount(accountFrom);
             tr.setSum(summ);
 
             TransactionDAOimpl.save(tr);
 
             accountFrom.setTypeId((int) (accountFrom.getTypeId() - summ));
             accountTo.setTypeId((int) (accountTo.getTypeId() + summ));
 
             try {
                 AccountDAOimpl.update(accIdFrom, accountFrom);
                 AccountDAOimpl.update(accIdTo, accountTo);
             } catch (SQLException e) {
                 e.printStackTrace();
             }

         }
 
         response.sendRedirect("/account");
     }
 }
