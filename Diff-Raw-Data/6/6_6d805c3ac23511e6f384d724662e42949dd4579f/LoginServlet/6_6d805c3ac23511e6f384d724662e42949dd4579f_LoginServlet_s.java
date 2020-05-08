 package com.jumbalakka.nobs.web.servlet;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import com.jumbalakka.commons.config.JumbalakkaPropertyPlaceHolderConfigure;
 import com.jumbalakka.commons.spring.SpringContextUtils;
 import com.jumbalakka.commons.web.JumbalakkaAbstractServlet;
 import com.jumbalakka.nobs.dao.NobsDAO;
 import com.jumbalakka.nobs.exception.NobsException;
 import com.jumbalakka.nobs.type.NobsUser;
 
 public class LoginServlet extends JumbalakkaAbstractServlet
 {
 	public static final String	LOGONPAGE	= "index.jsp";
 	public static String SESSION_VAR_USER = 
 			JumbalakkaPropertyPlaceHolderConfigure.getPropertyValue( "session.user", "user" );
 	
 	@Override
 	protected void jumbDoGet( HttpServletRequest req, HttpServletResponse resp )
 			throws ServletException, IOException
 	{
 		super.doPost( req, resp );
 	}
 
 	@Override
 	protected void jumbDoPost( HttpServletRequest req, HttpServletResponse resp )
 			throws ServletException, IOException
 	{
 		NobsUser user = 
 				(NobsUser) req.getSession().getAttribute( SESSION_VAR_USER ); 
 		if( user == null )
 		{
 			String uname = req.getParameter( "user" );
 			String password = req.getParameter( "passwd" );
 			NobsDAO dao = SpringContextUtils.getBean( "nobsDAO", NobsDAO.class );
 			try{
 				dao.authUser( uname, password );
 				user = dao.getUser( uname );
 				setSessionAttribute( req.getSession(), SESSION_VAR_USER, user );
 				req.getSession().setMaxInactiveInterval( 360*6 );
 				dao.logInfo( user, user.getUserid(), "LOGON", "User Logged In!" );
 			}catch( NobsException e )
 			{
 				setErrorMessage( req, e.getMessage() );
 				doForward( req, resp, LOGONPAGE );
 				return;
 			}
 		}
		doRedirect( resp, "nobs/index.jsp" );
 	}
 }
