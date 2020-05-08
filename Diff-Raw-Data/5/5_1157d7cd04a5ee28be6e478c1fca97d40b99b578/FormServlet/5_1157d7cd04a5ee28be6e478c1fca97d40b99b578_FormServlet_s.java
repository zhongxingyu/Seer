 package com.nacre.servlet;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xml.sax.SAXException;
 
 import com.nacre.service.FormFactory;
 import com.nacre.service.vo.Field;
 import com.nacre.service.vo.Form;
 
 public class FormServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final String XSD = "xsd";
 
 	private FormFactory formFactory = null;
 	protected transient final Log log = LogFactory.getLog(this.getClass());
 
 	@Override
 	public void init() throws ServletException {
 		loadSchema(this.getClass().getResource(this.getInitParameter(XSD)));
 		super.init();
 	}
 
 	private void loadSchema(URL... url) throws ServletException {
 		try {
 			formFactory = new FormFactory(url);
 		} catch (SAXException e) {
 			e.printStackTrace();
 			throw new ServletException(e);
 		}
 	}
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/html");
 		if ("true".equals(request.getParameter("reload"))) {
 			System.out.println("RELOADING");
 			loadSchema(this.getClass().getResource(this.getInitParameter(XSD)));
 		}
 		if (request.getParameter("type") != null) {
 			Form form = formFactory.getForm(request.getParameter("type"));
 			request.setAttribute("form", form);
 			request.getRequestDispatcher("/layouts/nacre.jsp").include(request, response);
 		} else if (request.getParameter("query") != null) {
 			Field field = formFactory.query(request.getParameter("query"));
 			request.setAttribute("field", field);
			request.getRequestDispatcher("/layouts/field.jsp").include(request, response);
 		}
 	}
 }
