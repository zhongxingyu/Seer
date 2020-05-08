 package com.epam.lab.intouch.web.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.finder.MemberFinder;
 import com.epam.lab.intouch.controller.skill.SkillController;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.model.member.info.skill.SkillType;
 import com.epam.lab.intouch.web.util.request.parser.MemberSearchParser;
 
 public class MemberSearchServlet extends HttpServlet {
 	private final static Logger LOG = LogManager.getLogger(MemberSearchServlet.class);
 	private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String queryString = request.getParameter("query");
 		
 		if(queryString != null && !queryString.isEmpty()) {
 			request.setAttribute("queryString", queryString);
 
 			String query = new MemberSearchParser().getMemberInfoQuery(request);
 
 			LOG.debug("Result query: " + query);
 			List<Member> members = new ArrayList<Member>();
 			try {
 				members = new MemberFinder().findMembers(query);
 			} catch (DataAccessingException e) {
 				LOG.error("Problems with data accessing!", e);
 			}
 
 			request.setAttribute("members", members);
 		}
 		
 		this.getAllSkills(request);
 		getServletConfig().getServletContext().getRequestDispatcher("/pages/memberSearch.jsp").forward(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		MemberFinder finder = new MemberFinder();
 		MemberSearchParser parser = new MemberSearchParser();
 		String query = parser.getQuery(request);
 
 		LOG.debug("Result query: "+query);
 		List<Member> members = new ArrayList<Member>();
 		try {
 			members = finder.findMembers(query);
 		} catch (DataAccessingException e) {
 			LOG.error("Problems with data accessing!", e);
 		}
 
 		request.setAttribute("members", members);
 		this.getAllSkills(request);
 		getServletConfig().getServletContext().getRequestDispatcher("/pages/memberSearch.jsp").forward(request, response);
 	}
 	
 	private void getAllSkills(final HttpServletRequest request) {
 		SkillController skillController = new SkillController();
 		try {
 			request.setAttribute("languageSkills", skillController.getSkills(SkillType.LANGUAGE));
 			request.setAttribute("programmingSkills", skillController.getSkills(SkillType.PROGRAMMING));
 			request.setAttribute("technologySkills", skillController.getSkills(SkillType.TECHNOLOGY));
 		} catch (DataAccessingException ex) {
 			LOG.error("Something wrong with data accessing! ", ex);
 		}
 	}
 
 }
