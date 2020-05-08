 package mosaic;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import mosaic.palettegenerator.ImageBasedColorPaletteGenerator;
 
 public class HomeServlet extends HttpServlet {
 
 	@Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		System.out.println("HomeServlet: got GET with " + req.getParameterNames());
		req.setAttribute("showImg", "false");
		req.getRequestDispatcher("/index.jsp").forward(req, resp);
     }
 
 	@Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		System.out.println("HomeServlet: got POST with " + req.getParameterNames());
 		req.setAttribute("showImg", "true");
 		req.getRequestDispatcher("/index.jsp").forward(req, resp);
     }
 	
 }
