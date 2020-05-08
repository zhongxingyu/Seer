 package uk.ac.ebi.biosd.xs.service;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import javax.persistence.EntityManagerFactory;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import uk.ac.ebi.biosd.xs.export.AbstractXMLFormatter;
 import uk.ac.ebi.biosd.xs.export.AbstractXMLFormatter.SamplesFormat;
 import uk.ac.ebi.biosd.xs.init.EMFManager;
 
 public class ExportAll extends HttpServlet
 {
  static final String DefaultSchema = SchemaManager.STXML;
 
  static final String SchemaParameter = "schema";
  static final String ProfileParameter = "server";
  static final String LimitParameter = "limit";
  static final String ThreadsParameter = "threads";
  static final String SamplesParameter = "samples";
  static final String SourcesParameter = "sources";
  static final String SourcesByNameParameter = "sourcesByName";
  static final String SinceParameter = "since";
  static final String AttributesParameter = "showAttributes";
  static final String NamespaceParameter = "hideNS";
  static final String NoAccessControlParameter = "noAC";
  
 
  private static final long serialVersionUID = 1L;
  
  private static final int blockSize = 1000;
 
  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
   AbstractXMLFormatter formatter=null;
   
 
   
   long limit=-1;
   
   String limP =  request.getParameter(LimitParameter);
   
   if( limP != null )
   {
    try
    {
     limit = Long.parseLong(limP);
    }
    catch(Exception e)
    {
     response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
     response.getWriter().append("<html><body><span color='red'>Invalid "+LimitParameter+" parameter value. Sould be an integer value</span></body></html>");
     return;
    }
   }
   
   if( limit <=0 )
    limit=Long.MAX_VALUE;
   
   String thr =  request.getParameter(ThreadsParameter);
   int threadsNum = 1;
   
   if( thr != null )
   {
    try
    {
     threadsNum = Integer.parseInt(thr);
    }
    catch(Exception e)
    {
     response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
     response.getWriter().append("<html><body><span color='red'>Invalid "+ThreadsParameter+" parameter value. Sould be an integer value</span></body></html>");
     return;
    }
   }
   
   if( limit <=0 )
    limit=Long.MAX_VALUE;
   
   long since=-1;
   
   String sinsP =  request.getParameter(SinceParameter);
   
   if( sinsP != null )
   {
    try
    {
     since = Long.parseLong(sinsP);
    }
    catch(Exception e)
    {
     response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
     response.getWriter().append("<html><body><span color='red'>Invalid "+SinceParameter+" parameter value. Sould be an integer value</span></body></html>");
     return;
    }
   }
   
   
   SamplesFormat samples = SamplesFormat.EMBED;
   
   String smp = request.getParameter(SamplesParameter);
   
   if( smp != null )
   {
    try
    {
     samples = SamplesFormat.valueOf(smp);
    }
    catch(Exception e)
    {
     response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
     response.getWriter().append("<html><body><span color='red'>Invalid "+SamplesParameter+" parameter value. Sould be one of: "+Arrays.asList(SamplesFormat.values())+"</span></body></html>");
     return;
    }
   }
   
   String prof = request.getParameter(ProfileParameter);
   
   EntityManagerFactory emf = null;
   
   if( prof == null )
   {
    emf = EMFManager.getDefaultFactory();
    prof = "<default>";
   }
   else
    emf = EMFManager.getFactory(prof);
   
   if( emf == null )
   {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.getWriter().append("<html><body><span color='red'>Can't find profile: "+prof+"</span></body></html>");
    return;
   }
   
  
   response.setContentType("text/xml");
   Appendable out = response.getWriter();
   
   String prm = request.getParameter(NamespaceParameter);
   
   boolean hideNS = "true".equals( prm ) || "yes".equals( prm ) || "1".equals( prm );
 
   prm = request.getParameter(NoAccessControlParameter);
 
   boolean addAC = prm == null || ! ( "true".equals( prm ) || "yes".equals( prm ) || "1".equals( prm ) );
   
   prm = request.getParameter(AttributesParameter);
   
   boolean exportAttributes = "true".equals( prm ) || "yes".equals( prm ) || "1".equals( prm );
   
   String sch = request.getParameter(SchemaParameter);
   
   if( sch == null )
    sch = DefaultSchema;
 
   formatter = SchemaManager.getFormatter(sch, ! hideNS, exportAttributes, addAC, samples);
   
   if( formatter == null )
   {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.getWriter().append("<html><body><span color='red'>Invalid schema: '"+sch+"'</span></body></html>");
    return;
   }
   
   prm = request.getParameter(SourcesParameter);
   boolean exportSources = "true".equals( prm ) || "yes".equals( prm ) || "1".equals( prm );
   
   prm = request.getParameter(SourcesByNameParameter);
   boolean sourcesByName = "true".equals( prm ) || "yes".equals( prm ) || "1".equals( prm );
 
   
   Exporter expt = null;
   
   if( threadsNum == 1 )
    expt = new ExporterST(emf, formatter, exportSources, sourcesByName, blockSize);
   else
    expt = new ExporterMT(emf, formatter, exportSources, sourcesByName, blockSize, threadsNum);
  
   expt.export(since, out, limit);
   
  }
 
  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
   // TODO Auto-generated method stub
  }
 
 }
