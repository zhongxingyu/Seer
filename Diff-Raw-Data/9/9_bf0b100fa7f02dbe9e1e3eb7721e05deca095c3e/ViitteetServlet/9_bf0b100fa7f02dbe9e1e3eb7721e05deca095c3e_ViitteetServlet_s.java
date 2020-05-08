 package ohtu.viitearto.servlets;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import ohtu.viitearto.Rekisteri;
 import ohtu.viitearto.Tietoturva;
 import ohtu.viitearto.Viite;
 
 
 public class ViitteetServlet extends HttpServlet {
 
     private Tietoturva security = new Tietoturva();
     private Rekisteri rekisteri = Rekisteri.getInstance();
     private String lomakeTyyppi;
     private TreeMap<String, String> lomakeTiedot = new TreeMap<String, String>();
     private String viiteTyyppi;
     private List<Viite> hakuTulokset;
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         request.setCharacterEncoding("UTF-8");
         
         if (hakuTulokset != null)
             request.setAttribute("hakuTulokset", hakuTulokset);
         
         if (lomakeTyyppi != null) { // asetetaan viitteen lisäämistä varten täytettävät kentät
             viiteTyyppi = null;
             lomakeTiedot.clear();
             
             if (lomakeTyyppi.equals("book")) {
                 bookLomake(lomakeTiedot);
                 viiteTyyppi = "Book";
             } else if (lomakeTyyppi.equals("inproceedings")) {
                 inproceedingsLomake(lomakeTiedot);
                 viiteTyyppi = "Inproceedings";
             } else if (lomakeTyyppi.equals("article")) {
                 articleLomake(lomakeTiedot);
                 viiteTyyppi = "Article";
             }
             
            lomakeTiedot.put("tag", "Tagit: ");
             request.setAttribute("tiedot", lomakeTiedot);
             lomakeTyyppi = null;
         }
         
         request.setAttribute("type", viiteTyyppi); // viitteen tyyppi
         
         if (security.onkoVirheita())
             request.setAttribute("tiedot", lomakeTiedot);
         
         request.setAttribute("errors", security.getVirheIlmoitukset()); // näytetään virheet, jos niitä on
         security.nollaaVirheet();
         
         request.setAttribute("viitteet", rekisteri.getViitteet()); // hakee viitteet tietokannasta
         
         RequestDispatcher dispatcher =
                 request.getRequestDispatcher("WEB-INF/views/viitteet.jsp");
         dispatcher.forward(request, response); // ohjaudutaan index.jsp-sivulle
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         hakuTulokset = (List<Viite>) request.getAttribute("tulokset");
         lomakeTyyppi = request.getParameter("viiteTyyppi");
         
         response.sendRedirect(request.getRequestURI()); // POST-pyynnöt ohjataan doGetille
     }
     
     private void articleLomake(TreeMap<String, String> tiedot) {
         tiedot.put("title", "<font color=\"red\">*</font> Title: ");
         tiedot.put("author", "<font color=\"red\">*</font> Author: ");
         tiedot.put("publisher", "Publisher: ");
         tiedot.put("year", "Year: ");
         tiedot.put("address", "Address: ");
         tiedot.put("pages", "Pages: ");
         tiedot.put("journal", "<font color=\"red\">*</font> Journal: ");
         tiedot.put("volume", "Volume: ");
         tiedot.put("number", "Number: ");
     }
 
     private void inproceedingsLomake(TreeMap<String, String> tiedot) {
         tiedot.put("title", "<font color=\"red\">*</font> Title: ");
         tiedot.put("author", "<font color=\"red\">*</font> Author: ");
         tiedot.put("publisher", "Publisher: ");
         tiedot.put("year", "Year: ");
         tiedot.put("address", "Address: ");
         tiedot.put("booktitle", "<font color=\"red\">*</font> Booktitle: ");
         tiedot.put("pages", "Pages: ");
     }
 
     private void bookLomake(TreeMap<String, String> tiedot) {
         tiedot.put("title", "<font color=\"red\">*</font> Title: ");
         tiedot.put("author", "<font color=\"red\">*</font> Author: ");
         tiedot.put("year", "Year: ");
         tiedot.put("publisher", "Publisher: ");
         tiedot.put("address", "Address: ");
     }
 }
