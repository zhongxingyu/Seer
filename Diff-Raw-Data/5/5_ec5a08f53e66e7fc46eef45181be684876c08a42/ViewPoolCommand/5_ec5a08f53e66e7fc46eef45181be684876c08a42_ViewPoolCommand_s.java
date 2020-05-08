 package com.mierdasoft.bowlpoolmanager.controller;
 
 import java.io.IOException;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 
 import org.bibeault.frontman.*;
 
 import com.mierdasoft.bowlpoolmanager.model.*;
 import com.mierdasoft.bowlpoolmanager.model.dao.*;
 
 public class ViewPoolCommand implements Command {
 	@Override
 	public void execute(CommandContext context) {
 		DAOFactory daoFactory;
 		Pool pool;
 		PoolManager manager;
 		PoolManagerDAO poolManagerDAO;
 		UUID id;
 
 		daoFactory = new StaticDAOFactory();
 
 		poolManagerDAO = daoFactory.createPoolManagerDAO();
 
		poolManagerDAO.getPoolManager();
 
 		manager = poolManagerDAO.getPoolManager();
 
 		try {
			Object var = context.getScopedVariable("id", ScopedContext.PAGE);
 
 			id = UUID.fromString((String) var);
 
 			pool = manager.getPoolById(id);
 
 			context.setScopedVariable("pool", pool);
 			context.setScopedVariable("pageTitle", "View Pool");
 			context.setScopedVariable("forwardToView", "PoolPage");
 			context.forwardToCommand("ApplyTemplate");
 		}
 
 		catch (ServletException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
