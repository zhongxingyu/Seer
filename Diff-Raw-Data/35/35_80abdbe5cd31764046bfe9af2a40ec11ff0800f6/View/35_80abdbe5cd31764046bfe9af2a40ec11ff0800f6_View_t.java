 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import model.ParsePDF;
 import net.iharder.dnd.FileDrop;
 
 /**
  * $Id$
  * $LastChangedDate$
  * 
  * @author Fadi M. H. Asbih
  * @email fadi_asbih@yahoo.de
  * @version $Revision$
  * @copyright $Date$
  * 
  * TERMS AND CONDITIONS:
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 public class View extends JFrame implements ActionListener, Observer {
 
         private static final long serialVersionUID = 6177350218996491783L;
         private JEditorPane status;
         private String path;
         private ParsePDF pdf;
         private JProgressBar progressBar;
         private InputPanel ip;
         private RatedSubjectDialog rsd;
         private NonRatedSubjectDialog nrsd;
 
         public View(final ParsePDF pdf) throws Exception {
                 this.setPdf(pdf);
 
                 this.setTitle("LUH Notenspiegel Rechner");
                 this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 JPanel panel = new JPanel(); //Main Panel
                 
                 ip = new InputPanel(this, pdf);
                 
                 panel.setLayout(new BorderLayout());
                 panel.add(ip, BorderLayout.NORTH);
                 
                 this.add(panel);
                 this.pack();
                 this.setMinimumSize(new Dimension(310, 230));
                           
                 // Output
                 progressBar = new JProgressBar();
                 status = new JEditorPane();
                 status.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
                 status.setEditable(false);

                 status.setText(" <p><center>Notenspiegel einfach hier ziehen geht auch :-)"+
                 		"<br><br>"+
     					"<a href=\"https://github.com/iFadi/luh-nr/\">Project</a></center></p>");

                 status.setForeground(Color.black.darker());
                 panel.add(status);
                 panel.add(progressBar, BorderLayout.AFTER_LAST_LINE);

         		status.addHyperlinkListener(new HyperlinkListener() {
         		    public void hyperlinkUpdate(HyperlinkEvent e) {
         		        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
         		        	try {
         		        		if(e.getURL().toString().equals("http://rated")) {
         		        			rsd = new RatedSubjectDialog(getPdf());
         		        			rsd.setVisible(true);
 //        		        			System.out.println(e.getURL());
         		        		}
         		        		else if(e.getURL().toString().equals("http://nonrated")) {
         		        			nrsd = new NonRatedSubjectDialog(getPdf());
         		        			nrsd.setVisible(true);
 //        		        			openDialog(getPdf());
 //        		        			System.out.println(e.getURL());
         		        		}
         		        		else
         		        			Desktop.getDesktop().browse(e.getURL().toURI());
 							} catch (IOException e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							} catch (URISyntaxException e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							}
         		        }
         		    }
         		});
         		
                 this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 this.setLocationRelativeTo(null);
 //                this.setResizable(false);
 
                 
                 this.setVisible(true);
                 
                 //TESTING Drag n Drop
                 new  FileDrop(panel, new FileDrop.Listener()
                 {   public void  filesDropped(java.io.File[] files )
                     {   
                 	setPath(files[0].getAbsolutePath());
                 	try {
 						output(getPath());
 					} catch (Exception err) {
                         // TODO Auto-generated catch block
                         err.printStackTrace();
                         
                         getStatus().setText("<div style='margin-left:2px;'>"+
                         		"<center><font color=red>Hmm, irgendwo ist was schief gelaufen ...</font></center>"+
                         		"<br><br>"+err+
                         		"</div>");
 					}
                     }   // end filesDropped
                 }); // end F
         }
 
         public String getPath() {
                 return path;
         }
 
         public void setPath(String path) {
                 this.path = path;
         }
         
         public JEditorPane getStatus() {
                 return status;
         }
 
 		public ParsePDF getPdf() {
 			return pdf;
 		}
 
 		public void setPdf(ParsePDF pdf) {
 			this.pdf = pdf;
 		}
 
 		@Override
 		public void update(Observable arg0, Object arg1) {
 			try {
 				output("update");
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		public void output(String path) throws Exception {
 			if (!path.equals("update"))
 				pdf.parseFile(path);
 			getStatus().setText(
 					 "<div style='margin-left:2px;'><center>"+ pdf.getSubject()+
 					 "<br> <i>" + pdf.getCertificate()+"</i></center>"+
   				     "<br> Anzahl benotete Fächer: "+pdf.getNumberOfSubjectsWithGrade()+" [<i>"+(int)pdf.getWeightedCredits()+" CP</i>]"+" <a href=\"http://rated\">[+]</a>"+ 
   					 "<br> Anzahl unbenotete Fächer: "+pdf.getNumberOfSubjectsWithoutGrade()+" [<i>"+(int)pdf.getUnweightedCredits()+" CP</i>]"+" <a href=\"http://nonrated\">[+]</a>"+ 
   					 "<br> Anzahl gesamte bestandene Fächer: "+pdf.getNumberOfSubjects()+
 					 "<br><br> Credit Points: "+"<b>"+(int)pdf.getCredits()+"</b>"+
 					 "<br> Note: "+"<b>"+pdf.getFinalGrade()+"</b>"+
 					 "<br><br> Abschlussarbeit starten: "+pdf.getStartThesis()+
 					 "<br> Studium Geschafft in Prozent... "+
 					 "</div>");
             getStatus().setForeground(Color.black.darker());
             progressBar.setIndeterminate(false);
             progressBar.setValue((int)pdf.getPercent());
             progressBar.setStringPainted(true);
             
             this.setMinimumSize(new Dimension(310, 280));
             
 		}
 
 		public JProgressBar getProgressBar() {
 			return progressBar;
 		}
 
 		public void setProgressBar(JProgressBar progressBar) {
 			this.progressBar = progressBar;
 		}
 }
