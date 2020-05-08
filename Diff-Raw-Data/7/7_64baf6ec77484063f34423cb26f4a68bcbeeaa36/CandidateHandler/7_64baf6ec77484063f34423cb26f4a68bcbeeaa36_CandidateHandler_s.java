 /*
  * Copyright (C) 2005-2008 Michael Keith, Australia Telescope National Facility, CSIRO
  * 
  * email: mkeith@pulsarastronomy.net
  * www  : www.pulsarastronomy.net
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package bookkeepr.jettyhandlers;
 
 import bookkeepr.BookKeepr;
 
 import bookkeepr.BookKeeprException;
 import bookkeepr.DummyIdAble;
 import bookkeepr.managers.CandidateManager;
 import bookkeepr.xml.IdAble;
 import bookkeepr.xml.XMLAble;
 import bookkeepr.xml.XMLReader;
 import bookkeepr.xmlable.BackgroundedTask;
 import bookkeepr.xmlable.BookkeeprHost;
 import bookkeepr.xmlable.CandidateListStub;
 import bookkeepr.xmlable.DatabaseManager;
 import bookkeepr.xmlable.RawCandidate;
 import bookkeepr.xmlable.RawCandidateStub;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Formatter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.tools.tar.TarEntry;
 import org.apache.tools.tar.TarInputStream;
 import org.mortbay.jetty.Request;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.xml.sax.SAXException;
 import bookkeepr.managers.TypeIdManager;
 import bookkeepr.xml.XMLWriter;
 import bookkeepr.xmlable.CandListSelectionRequest;
 import bookkeepr.xmlable.CandidateListIndex;
 import bookkeepr.xmlable.ClassifiedCandidate;
 import bookkeepr.xmlable.ClassifiedCandidateIndex;
 import bookkeepr.xmlable.ClassifiedCandidateSelectRequest;
 import bookkeepr.xmlable.Processing;
 import bookkeepr.xmlable.Psrxml;
 import bookkeepr.xmlable.RawCandidateMatched;
 import bookkeepr.xmlable.Session;
 import bookkeepr.xmlable.ViewedCandidates;
 import bookkeepr.xmlable.ViewedCandidatesIndex;
 import java.awt.image.BufferedImage;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.zip.GZIPOutputStream;
 import javax.imageio.ImageIO;
 import org.apache.http.Header;
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 
 /**
  *
  * @author kei041
  */
 public class CandidateHandler extends AbstractHandler {
 
     private CandidateManager candMan;
     private DatabaseManager dbMan;
     private BookKeepr bookkeepr;
     private int nAddSubmitted = 0;
 
     public CandidateHandler(CandidateManager candMan, DatabaseManager dbMan, BookKeepr bookkeepr) {
         this.candMan = candMan;
         this.dbMan = dbMan;
         this.bookkeepr = bookkeepr;
     }
 
     public void handle(String path, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
 
         if (path.startsWith("/cand/")) {
             ((Request) request).setHandled(true);
 
 
             if (path.startsWith("/cand/lists")) {
                 if (request.getMethod().equals("GET")) {
                     if (path.startsWith("/cand/lists/from/")) {
                         long targetId = Long.parseLong(path.substring(17), 16);
                         if (dbMan.getType(new DummyIdAble(targetId)) == TypeIdManager.getTypeFromClass(Psrxml.class)) {
                             CandListSelectionRequest req = new CandListSelectionRequest();
                             req.setAcceptPsrxmlIds(new long[]{targetId});
                             XMLWriter.write(response.getOutputStream(), candMan.getCandidateLists(req));
 
                         } else if (dbMan.getType(new DummyIdAble(targetId)) == TypeIdManager.getTypeFromClass(Processing.class)) {
                             CandListSelectionRequest req = new CandListSelectionRequest();
                             req.setAcceptProcessingIds(new long[]{targetId});
                             XMLWriter.write(response.getOutputStream(), candMan.getCandidateLists(req));
 
                         } else {
                             response.sendError(400, "Bad GET request for /cand/lists/from/...");
                             return;
                         }
                     } else {
                         CandidateListIndex idx = new CandidateListIndex();
                         List<IdAble> list = dbMan.getAllOfType(TypeIdManager.getTypeFromClass(CandidateListStub.class));
                         for (IdAble stub : list) {
                             idx.addCandidateListStub((CandidateListStub) stub);
                         }
                         OutputStream out = response.getOutputStream();
                         String hdr = request.getHeader("Accept-Encoding");
                         if (hdr != null && hdr.contains("gzip")) {
                             // if the host supports gzip encoding, gzip the output for quick transfer speed.
                             out = new GZIPOutputStream(out);
                             response.setHeader("Content-Encoding", "gzip");
                         }
                         XMLWriter.write(out, idx);
                         out.close();
                         return;
                     }
                 } else if (request.getMethod().equals("POST")) {
                     try {
                         XMLAble xmlable = XMLReader.read(request.getInputStream());
                         if (xmlable instanceof CandListSelectionRequest) {
                             CandListSelectionRequest req = (CandListSelectionRequest) xmlable;
                             OutputStream out = response.getOutputStream();
                             String hdr = request.getHeader("Accept-Encoding");
                             if (hdr != null && hdr.contains("gzip")) {
                                 // if the host supports gzip encoding, gzip the output for quick transfer speed.
                                 out = new GZIPOutputStream(out);
                                 response.setHeader("Content-Encoding", "gzip");
                             }
                             XMLWriter.write(out, candMan.getCandidateLists(req));
                             out.close();
                         } else {
                             response.sendError(400, "Bad POST request for /cand/lists");
                             return;
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(CandidateHandler.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                 } else if (request.getMethod().equals("DELETE")) {
                     String[] elems = path.substring(1).split("/");
                     if (elems.length < 3) {
                         response.sendError(400, "Bad DELETE request for /cand/lists/{clistId}");
                         return;
                     }
                     final long clistId = Long.parseLong(elems[2], 16);
                     CandidateListStub cls = (CandidateListStub) dbMan.getById(clistId);
                     if (cls == null) {
                         response.sendError(404, "Bad ClistID requested for deletion (no such candlist)");
                         Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, "Bad ClistID requested for deletion (no such candlist)");
                         return;
                     }
                     try {
                         this.candMan.deleteCandidateListAndCands(cls);
                         response.setStatus(202);
                     } catch (BookKeeprException ex) {
                         response.sendError(500, "Could not delete candidate list as requested");
                         Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, "Could not delete candidate list as requested", ex);
                         return;
                     }
 
                 } else {
                     response.sendError(400, "Bad non-GET/non-POST/non-DELETE request for /cand/lists");
                     return;
                 }
             } else if (path.startsWith("/cand/viewed")) {
                 if (request.getMethod().equals("GET")) {
                     ViewedCandidatesIndex idx = new ViewedCandidatesIndex();
                     List<IdAble> list = dbMan.getAllOfType(TypeIdManager.getTypeFromClass(ViewedCandidates.class));
                     for (IdAble stub : list) {
                         idx.addViewedCandidates((ViewedCandidates) stub);
                     }
                     OutputStream out = response.getOutputStream();
                     String hdr = request.getHeader("Accept-Encoding");
                     if (hdr != null && hdr.contains("gzip")) {
                         // if the host supports gzip encoding, gzip the output for quick transfer speed.
                         out = new GZIPOutputStream(out);
                         response.setHeader("Content-Encoding", "gzip");
                     }
                     XMLWriter.write(out, idx);
                     out.close();
 
                     return;
                 } else if (request.getMethod().equals("POST")) {
                     ViewedCandidates newViewed = null;
                     try {
                         XMLAble xmlable = XMLReader.read(request.getInputStream());
                         if (xmlable instanceof ViewedCandidates) {
                             newViewed = (ViewedCandidates) xmlable;
                         } else {
                             response.sendError(400, "Bad Content type in request /cand/viewed");
                             return;
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(CandidateHandler.class.getName()).log(Level.INFO, "Bad XML in client request to POST new viewed cands");
                         response.sendError(400, "Bad XML in request /cand/viewed");
                         return;
                     }
 
                     IdAble idable = this.dbMan.getById(newViewed.getId());
                     if (idable instanceof ViewedCandidates) {
                         newViewed.append(((ViewedCandidates) idable));
                     } else {
                         newViewed.setId(0);
                     }
 
                     Session session = new Session();
                     this.dbMan.add(newViewed, session);
                     try {
                         this.dbMan.save(session);
                     } catch (BookKeeprException ex) {
                         Logger.getLogger(CandidateHandler.class.getName()).log(Level.SEVERE, "Error saving viewed cands to database", ex);
                         response.sendError(500, "Error saving viewed cands to database");
                         return;
                     }
                     OutputStream out = response.getOutputStream();
                     String hdr = request.getHeader("Accept-Encoding");
                     if (hdr != null && hdr.contains("gzip")) {
                         // if the host supports gzip encoding, gzip the output for quick transfer speed.
                         out = new GZIPOutputStream(out);
                         response.setHeader("Content-Encoding", "gzip");
                     }
                     XMLWriter.write(out, newViewed);
                     out.close();
 
                     return;
                 } else {
                     response.sendError(400, "Bad non-GET/POST request /cand/viewed");
                     return;
                 }
             } else if (path.startsWith("/cand/psrcat")) {
                 if (request.getMethod().equals("GET")) {
                     String[] elems = path.substring(1).split("/");
                     if (elems.length > 2) {
                         try {
                             long id = Long.parseLong(elems[2], 16);
                             ClassifiedCandidate cand = (ClassifiedCandidate) dbMan.getById(id);
                             String str = cand.getPsrcatEntry().toString();
                             response.setContentType("text/plain");
                             OutputStream out = response.getOutputStream();
                             String hdr = request.getHeader("Accept-Encoding");
                             if (hdr != null && hdr.contains("gzip")) {
                                 // if the host supports gzip encoding, gzip the output for quick transfer speed.
                                 out = new GZIPOutputStream(out);
                                 response.setHeader("Content-Encoding", "gzip");
                             }
                             PrintWriter wr = new PrintWriter(out);
                             wr.write(str);
                             wr.close();
                             out.close();
 
                         } catch (ClassCastException ex) {
                             Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, "Invalid candidate id passed to /cand/psrcat", ex);
                             response.sendError(400, "Bad id in request for /cand/psrcat");
                         }
                     } else {
                         response.sendError(400, "Bad request for /cand/psrcat");
                     }
                 } else {
                     response.sendError(400, "Bad non-GET request for /cand/psrcat");
 
                 }
             } else if (path.startsWith("/cand/classified")) {
                 if (request.getMethod().equals("GET")) {
                     String[] elems = path.substring(1).split("/");
                     ClassifiedCandidateIndex idx = new ClassifiedCandidateIndex();
                     List<IdAble> list = dbMan.getAllOfType(TypeIdManager.getTypeFromClass(ClassifiedCandidate.class));
                     if (elems.length > 2) {
 
                         int cl = Integer.parseInt(elems[2]);
                         for (IdAble stub : list) {
                             if (((ClassifiedCandidate) stub).getCandClassInt() == cl) {
                                 idx.addClassifiedCandidate((ClassifiedCandidate) stub);
                             }
                         }
                     } else {
 
 
                         for (IdAble stub : list) {
                             idx.addClassifiedCandidate((ClassifiedCandidate) stub);
                         }
                     }
                     OutputStream out = response.getOutputStream();
                     String hdr = request.getHeader("Accept-Encoding");
                     if (hdr != null && hdr.contains("gzip")) {
                         // if the host supports gzip encoding, gzip the output for quick transfer speed.
                         out = new GZIPOutputStream(out);
                         response.setHeader("Content-Encoding", "gzip");
                     }
                     XMLWriter.write(out, idx);
                     out.close();
 
                     return;
                 } else if (request.getMethod().equals("POST")) {
                     // post a CandidateSelectRequest
                     ClassifiedCandidateSelectRequest ccsreq = null;
                     try {
                         XMLAble xmlable = XMLReader.read(request.getInputStream());
                         if (xmlable instanceof ClassifiedCandidateSelectRequest) {
                             ccsreq = (ClassifiedCandidateSelectRequest) xmlable;
                         } else {
                             response.sendError(400, "Bad item posted to /cand/classified for Searching");
                             return;
                         }
                     } catch (SAXException ex) {
                         response.sendError(400, "Bad item posted to /cand/classified for Searching");
                         return;
                     }
 
                     // reply with the searched index
                     ClassifiedCandidateIndex idx = candMan.searchCandidates(ccsreq);
                     OutputStream out = response.getOutputStream();
                     String hdr = request.getHeader("Accept-Encoding");
                     if (hdr != null && hdr.contains("gzip")) {
                         // if the host supports gzip encoding, gzip the output for quick transfer speed.
                         out = new GZIPOutputStream(out);
                         response.setHeader("Content-Encoding", "gzip");
                     }
                     XMLWriter.write(out, idx);
                     out.close();
 
                 } else {
                     response.sendError(400, "Bad non-POST/GET request /cand/classified");
                     return;
                 }
             } else if (path.startsWith("/cand/newclassified")) {
                 if (request.getMethod().equals("POST")) {
 
                     ClassifiedCandidate cand = null;
                     try {
                         XMLAble xmlable = XMLReader.read(request.getInputStream());
                         if (xmlable instanceof ClassifiedCandidate) {
                             cand = (ClassifiedCandidate) xmlable;
                         } else {
                             response.sendError(400, "Bad item posted to /cand/newclassified for Classifying");
                             return;
                         }
                     } catch (SAXException ex) {
                         response.sendError(400, "Bad item posted to /cand/newclassified for Classifying");
                         return;
                     }
 
                     cand.setId(0);
 
                     Session session = new Session();
                     dbMan.add(cand, session);
                     try {
                         dbMan.save(session);
                     } catch (BookKeeprException ex) {
                         Logger.getLogger(CandidateHandler.class.getName()).log(Level.SEVERE, "error saving database when adding new classified cand", ex);
                         response.sendError(500, "error saving database when adding new classified cand");
                     }
                     OutputStream out = response.getOutputStream();
                     String hdr = request.getHeader("Accept-Encoding");
                     if (hdr != null && hdr.contains("gzip")) {
                         // if the host supports gzip encoding, gzip the output for quick transfer speed.
                         out = new GZIPOutputStream(out);
                         response.setHeader("Content-Encoding", "gzip");
                     }
                     XMLWriter.write(out, cand);
                     out.close();
 
                     return;
                 } else {
                     response.sendError(400, "Bad non-POST request /cand/newclassified");
                     return;
                 }
             } else if (path.startsWith("/cand/add/")) {
                 // assume that 'add' is not an ID, since it would have server and type = 0, which is invalid.
                 if (request.getMethod().equals("POST")) {
                     String[] elems = path.substring(1).split("/");
                     if (elems.length < 4) {
                         response.sendError(400, "Bad URL for /cand/add/{psrxmlId}/{procId}");
                         return;
                     }
 
                     final long psrxmlId = Long.parseLong(elems[2], 16);
                     final long procId = Long.parseLong(elems[3], 16);
 
                     if (dbMan.getById(procId) == null) {
                         response.sendError(400, "Bad URL for /cand/add/{psrxmlId}/{procId}. ProcId " + Long.toHexString(procId) + " does not exist!");
                         return;
                     }
                     if (dbMan.getById(psrxmlId) == null) {
                         response.sendError(400, "Bad URL for /cand/add/{psrxmlId}/{procId}. PsrxmlId " + Long.toHexString(psrxmlId) + " does not exist!");
                         return;
                     }
 
                     synchronized (this) {
                         if (nAddSubmitted > 2) {
                             // too many jobs on. to prevent memory overload, end the session!
                             response.setHeader("Retry-After", "60");
                             response.sendError(503);
                             return;
                         } else {
                             //increment the workload counter
                             nAddSubmitted++;
                         }
                     }
 
                     final ArrayList<RawCandidate> rawCands = new ArrayList<RawCandidate>();
                     if (request.getContentType().equals("application/x-tar")) {
                         TarInputStream tarin = new TarInputStream(request.getInputStream());
                         while (true) {
                             // loop over all entries in the tar file.
                             TarEntry tarEntry = tarin.getNextEntry();
                             if (tarEntry == null) {
                                 break;
                             } else {
                             }
                             InputStream inStream;
 
 
 
                             /*
                              * There is some complication as the XML reader
                              * closes the input stream when it is done, but we
                              * don't want to do that as it closes the entire tar
                              * 
                              * So we define a 'UnclosableInputStream' that ignores
                              * the close() command. 
                              * 
                              * If we have a gzipped xml file, we should pass
                              * through a gzip input stream too.
                              */
                             if (tarEntry.getName().endsWith(".gz")) {
                                 inStream = new UncloseableInputStream(new GZIPInputStream(tarin));
                             } else {
                                 inStream = new UncloseableInputStream(tarin);
                             }
                             try {
                                 // parse the xml document.
                                 XMLAble xmlable = (XMLAble) XMLReader.read(inStream);
                                 if (xmlable instanceof RawCandidate) {
                                     rawCands.add((RawCandidate) xmlable);
                                 } else {
                                     response.sendError(400, "POSTed tar file MUST only contain raw candidates.");
                                     return;
                                 }
 
                             } catch (SAXException ex) {
                                 Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, null, ex);
                                 response.sendError(400, "POSTed tar file MUST only contain vaild xml candidates.");
                                 return;
                             }
 
                         }
                         // finaly, we can close the tar stream.
                         tarin.close();
                         Logger.getLogger(CandidateHandler.class.getName()).log(Level.INFO, "Received " + rawCands.size() + " raw candidates for processing from " + request.getRemoteAddr());
 
 
 
                         final BackgroundedTask bgtask = new BackgroundedTask("AddRawCandidates");
 
                         bgtask.setTarget(new Runnable() {
 
                             public void run() {
                                 candMan.addRawCandidates(rawCands, psrxmlId, procId);
                                 synchronized (CandidateHandler.this) {
                                     // decriment the counter for how many workloads are left to do.
                                     CandidateHandler.this.nAddSubmitted--;
                                 }
                             }
                         });
                         bookkeepr.getBackgroundTaskRunner().offer(bgtask);
                         StringBuffer buf = new StringBuffer();
                         Formatter formatter = new Formatter(buf);
                         formatter.format("%s/%s/%d", bookkeepr.getConfig().getExternalUrl(), "tasks", bgtask.getId());
                         response.setStatus(303);
                         response.addHeader("Location", buf.toString());
                         return;
                     } else {
                         response.sendError(400, "Must POST application/x-tar type documents to this URL");
                     }
                 } else {
                     response.sendError(400, "Bad non-POST request /cand/add");
                     return;
                 }
             } else {
                 // see if we are requesting and ID.
                 long targetId = 0;
                 String[] elems = path.substring(1).split("/");
 
                 // try and make an int from the passed value.
                 try {
                     if (elems.length < 2) {
                         response.sendError(400, "Bad URL for /cand/{id}");
                         return;
                     }
                     targetId = Long.parseLong(elems[1], 16);
                 } catch (NumberFormatException ex) {
                     Logger.getLogger(CandidateHandler.class.getName()).log(Level.INFO, "Recieved request for bad id " + elems[1]);
                     response.sendError(400, "Submitted URI was malformed\nMessage was '" + ex.getMessage() + "'");
                     return;
                 }
 
                 IdAble idable = dbMan.getById(targetId);
                 if (idable == null) {
                     Logger.getLogger(CandidateHandler.class.getName()).log(Level.INFO, "Recieved request for non-existing ID " + Long.toHexString(targetId));
                     response.sendError(400, "Submitted request was for non-existing ID " + Long.toHexString(targetId));
                     return;
                 }
 
                 if (idable instanceof CandidateListStub) {
                     if (request.getMethod().equals("GET")) {
                         int origin = dbMan.getOrigin(idable);
                         if (dbMan.getOriginId() == origin) {
                             // request for a local item...
                             response.setHeader("Content-Encoding", "gzip");
                             outputToInput(new FileInputStream(candMan.getCandidateListFile((CandidateListStub) idable)), response.getOutputStream());
 
                         } else {
                             HttpClient httpclient = bookkeepr.checkoutHttpClient();
 
                             try {
                                 // request for a remote item...
                                 // currently re-direct to the remote server.. perhaps we should pass through?
                                 //@TODO: Change this to a pass through rather than redirect so that ssh tunnels work.
                                 BookkeeprHost host = bookkeepr.getConfig().getBookkeeprHost(origin);
                                 String targetpath = host.getUrl() + path;
                                 HttpGet httpreq = new HttpGet(targetpath);
                                 HttpResponse httpresp = httpclient.execute(httpreq);
                                 for (Header head : httpresp.getAllHeaders()) {
                                     if (head.getName().equalsIgnoreCase("transfer-encoding")) {
                                         continue;
                                     }
                                     response.setHeader(head.getName(), head.getValue());
                                 }
                                 response.setStatus(httpresp.getStatusLine().getStatusCode());
                                 httpresp.getEntity().writeTo(response.getOutputStream());
 
                             } catch (URISyntaxException ex) {
                                 Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, "Bad uri specified for remote candidate", ex);
                                 response.sendError(500, "Bad uri specified for remote candidate");
                             } catch (HttpException ex) {
                                 Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, "Could not make a httpreqest for remote Candidate", ex);
                                 response.sendError(500, "Could not make a httpreqest for remote Candidate");
                             }
                             bookkeepr.returnHttpClient(httpclient);
 
 //                            if (host != null) {
 //                                response.setStatus(301);
 //                                response.addHeader("Location", host.getUrl() + path);
 //                            } else {
 //                                response.sendError(500, "Cannot find a bookkeepr server for origin id " + origin);
 //                            }
                         }
                     } else {
                         response.sendError(400, "Bad non-GET request for a candidate list");
                         return;
                     }
                 } else if (idable instanceof ClassifiedCandidate) {
 
                     // /cand/{id}
 
                     if (request.getMethod().equals("POST")) {
 //                        int origin = dbMan.getOrigin(idable);
 //                        if (dbMan.getOriginId() == origin) {
                         RawCandidateMatched stub = null;
                         try {
                             XMLAble xmlable = XMLReader.read(request.getInputStream());
                             if (xmlable instanceof RawCandidateMatched) {
                                 stub = (RawCandidateMatched) xmlable;
                             } else {
                                 response.sendError(400, "Bad item posted to /cand/... for Classifying");
                                 return;
                             }
                         } catch (SAXException ex) {
                             response.sendError(400, "Bad item posted to /cand/... for Classifying");
                             return;
                         }
                         ClassifiedCandidate c = null;
                         try {
                             c = (ClassifiedCandidate) ((ClassifiedCandidate) idable).clone();
                         } catch (CloneNotSupportedException ex) {
                             Logger.getLogger(CandidateHandler.class.getName()).log(Level.SEVERE, null, ex);
                             response.sendError(500, "Server error that cannot happen happened! Classified Candidates are Cloneable!");
                             return;
                         }
 
 
                         c.addRawCandidateMatched(stub);
 
                         Session session = new Session();
                         dbMan.add(c, session);
                         try {
                             dbMan.save(session);
                         } catch (BookKeeprException ex) {
                             Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, "Error when saving database adding raw to classified cand", ex);
                             response.sendError(500, "Error when saving database adding raw to classified cand");
 
                         }
 
                         OutputStream out = response.getOutputStream();
                         String hdr = request.getHeader("Accept-Encoding");
                         if (hdr != null && hdr.contains("gzip")) {
                             // if the host supports gzip encoding, gzip the output for quick transfer speed.
                             out = new GZIPOutputStream(out);
                             response.setHeader("Content-Encoding", "gzip");
                         }
                         XMLWriter.write(out, c);
                         out.close();
 
 
 
 
 //                            OutputStream out = response.getOutputStream();
 //                            String hdr = request.getHeader("Accept-Encoding");
 //                            if (hdr != null && hdr.contains("gzip")) {
 //                                // if the host supports gzip encoding, gzip the output for quick transfer speed.
 //                                out = new GZIPOutputStream(out);
 //                                response.setHeader("Content-Encoding", "gzip");
 //                            }
 //                            XMLWriter.write(out, idx);
 //                            out.close();
 //                        } else {
 //                            // request for a remote item...
 //                            // currently re-direct to the remote server.. perhaps we should pass through?
 //                            BookkeeprHost host = bookkeepr.getConfig().getBookkeeprHost(origin);
 //                            if (host != null) {
 //                                response.setStatus(301);
 //                                response.addHeader("Location", host.getUrl() + path);
 //                            } else {
 //                                response.sendError(500, "Cannot find a bookkeepr server for origin id " + origin);
 //                            }
 //                        }
                     } else {
                         response.sendError(400, "Bad non-POST request for a classified candidate");
                         return;
                     }
                 } else if (idable instanceof RawCandidateStub) {
                     if (request.getMethod().equals("GET")) {
                         int origin = dbMan.getOrigin(idable);
                         if (dbMan.getOriginId() == origin) {
 
                             if (elems.length > 2) {
                                 try {
                                     int h = 600;
                                     int w = 800;
                                     String[] parts = elems[2].split("x|\\.png");
                                     if (parts.length > 1) {
                                         try {
                                             h = Integer.parseInt(parts[1]);
                                             w = Integer.parseInt(parts[0]);
                                         } catch (NumberFormatException e) {
                                             h = 600;
                                             w = 800;
                                         }
                                     }
                                     RawCandidate cand = (RawCandidate) XMLReader.read(new GZIPInputStream(new FileInputStream(candMan.getRawCandidateFile((RawCandidateStub) idable))));
 
                                     response.setContentType("image/png");
                                     BufferedImage img = candMan.makeImageOf(cand, w, h);
                                     ImageIO.write(img, "png", response.getOutputStream());
                                     return;
                                 } catch (SAXException ex) {
                                     Logger.getLogger(CandidateHandler.class.getName()).log(Level.SEVERE, null, ex);
                                 }
                             }
 
                             // request for a local item...
                             response.setHeader("Content-Encoding", "gzip");
                             outputToInput(new FileInputStream(candMan.getRawCandidateFile((RawCandidateStub) idable)), response.getOutputStream());
 
 
 
                         } else {
                             HttpClient httpclient = bookkeepr.checkoutHttpClient();
 
                             try {
                                 // request for a remote item...
                                 // currently re-direct to the remote server.. perhaps we should pass through?
                                 //@TODO: Change this to a pass through rather than redirect so that ssh tunnels work.
                                 BookkeeprHost host = bookkeepr.getConfig().getBookkeeprHost(origin);
                                 String targetpath = host.getUrl() + path;
                                 HttpGet httpreq = new HttpGet(targetpath);
                                 HttpResponse httpresp = httpclient.execute(httpreq);
                                 for (Header head : httpresp.getAllHeaders()) {
                                     if (head.getName().equalsIgnoreCase("transfer-encoding")) {
                                         continue;
                                     }
                                     response.setHeader(head.getName(), head.getValue());
                                 }
                                 response.setStatus(httpresp.getStatusLine().getStatusCode());
                                 httpresp.getEntity().writeTo(response.getOutputStream());
 
                             } catch (URISyntaxException ex) {
                                 Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, "Bad uri specified for remote candidate", ex);
                                 response.sendError(500, "Bad uri specified for remote candidate");
                             } catch (HttpException ex) {
                                 Logger.getLogger(CandidateHandler.class.getName()).log(Level.WARNING, "Could not make a httpreqest for remote Candidate", ex);
                                 response.sendError(500, "Could not make a httpreqest for remote Candidate");
                             }
                             bookkeepr.returnHttpClient(httpclient);
                         }
 
                     } else {
                         response.sendError(400, "Bad non-GET request for a raw candidate");
                         return;
                     }
                 } else {
                     Logger.getLogger(CandidateHandler.class.getName()).log(Level.INFO, "Recieved request for non-candidate ID " + targetId);
                     response.sendError(400, "Submitted request was for non-candidate ID " + targetId);
                     return;
                 }
             }
 
         }
     }
 
     private void outputToInput(InputStream in, OutputStream out) throws FileNotFoundException, IOException, IOException {
 
         byte[] b = new byte[1024];
         while (true) {
             int count = in.read(b);
             if (count < 0) {
                 break;
             }
 
             out.write(b, 0, count);
         }
 
         in.close();
         out.close();
     }
 
     /**
      * Passes througn the commands to the internal reader, except for the close()
      * command. This prevents the stream from being prematurely closed by the 
      * XML reader.
      * 
      */
     private class UncloseableInputStream extends InputStream {
 
         InputStream in;
 
         UncloseableInputStream(InputStream in) {
             this.in = in;
         }
 
         @Override
         public long skip(long n) throws IOException {
             return in.skip(n);
         }
 
         @Override
         public synchronized void reset() throws IOException {
             in.reset();
         }
 
         @Override
         public int read(byte[] b, int off, int len) throws IOException {
             return in.read(b, off, len);
         }
 
         @Override
         public int read(byte[] b) throws IOException {
             return in.read(b);
         }
 
         public int read() throws IOException {
             return in.read();
         }
 
         @Override
         public boolean markSupported() {
             return in.markSupported();
         }
 
         @Override
         public synchronized void mark(int readlimit) {
             in.mark(readlimit);
         }
 
         @Override
         public void close() throws IOException {
         }
 
         @Override
         public int available() throws IOException {
             return in.available();
         }
     }
 }
