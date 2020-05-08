 // Copyright (c) 2008 ScenPro, Inc.
 
// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/CdeBrowserForm.java,v 1.1 2008-06-16 20:48:22 hebell Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.freestylesearch.ui;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 
 /**
  * @author lhebel
  *
  */
 public class CdeBrowserForm extends ActionForm
 {
 
     private String _publicId;
     private String _vers;
     private String _idseq;
     private String _cdebrowser;
    private static final String _deUrl = "/CDEBrowser/search?dataElementDetails=9&p_de_idseq=$IDSEQ$&PageId=DataElementsGroup&queryDE=yes&FirstTimer=0";
     
     private static final long serialVersionUID = 5305822440080952381L;
     private static final Logger _logger = Logger.getLogger(CdeBrowserForm.class);
 
     /**
      * 
      */
     public CdeBrowserForm()
     {
         super();
     }
 
     /**
      * Get the public id
      * 
      * @return the public id
      */
     public String getPublicId()
     {
         return _publicId;
     }
     
     /**
      * Set the public id
      * 
      * @param val_ the public id
      */
     public void setPublicId(String val_)
     {
         _publicId = val_;
     }
 
     /**
      * Get the version
      * 
      * @return the version
      */
     public String getVersion()
     {
         return _vers;
     }
     
     /**
      * Set the version
      * 
      * @param val_ the version
      */
     public void setVersion(String val_)
     {
         _vers = val_;
     }
 
     /**
      * Get the idseq
      * 
      * @return the idseq
      */
     public String getIdseq()
     {
         return _idseq;
     }
     
     /**
      * Set the idseq
      * 
      * @param val_ the idseq
      */
     public void setIdseq(String val_)
     {
         _idseq = val_;
     }
 
     /**
      * Get the DE URL
      * 
      * @return the DE URL
      */
     public String getDeURL()
     {
         return _cdebrowser + _deUrl;
     }
     
     /**
      * Validate the content of the Edit Screen.
      * 
      * @param mapping_
      *        The action map defined for Edit.
      * @param request_
      *        The servlet request object.
      * @return Any errors found.
      */
     public ActionErrors validate(ActionMapping mapping_,
         HttpServletRequest request_)
     {
         ActionErrors errors = new ActionErrors();
         
         if (_publicId == null || _publicId.length() == 0)
         {
             errors.add("error", new ActionMessage("error.nopublicid"));
             return errors;
         }
         
         if (_vers == null || _vers.length() == 0)
         {
             errors.add("error", new ActionMessage("error.noversion"));
             return errors;
         }
 
         FreestylePlugIn ds = (FreestylePlugIn) request_.getSession().getServletContext().getAttribute(FreestylePlugIn._DATASOURCE);
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
 
         try
         {
             // Get the idseq using the public id and version.
             conn = ds.getDataSource().getConnection();
             pstmt = conn.prepareStatement("select de_idseq from sbr.data_elements_view where cde_id = ? and version = ?");
             pstmt.setString(1, _publicId);
             pstmt.setString(2, _vers);
             rs = pstmt.executeQuery();
             if (rs.next())
             {
                 _idseq = rs.getString(1);
             }
             else
             {
                 errors.add("error", new ActionMessage("error.node"));
                 throw new Exception(_publicId + "v" + _vers + " not a Data Element");
             }
             rs.close();
             pstmt.close();
 
             // Get the CDE Browser URL
             pstmt = conn.prepareStatement("select value from sbrext.tool_options_view_ext where tool_name = 'CDEBrowser' and property = 'URL'");
             rs = pstmt.executeQuery();
             if (rs.next())
             {
                 _cdebrowser = rs.getString(1);
             }
             else
             {
                 errors.add("error", new ActionMessage("error.nocdebrowser"));
                 throw new Exception("CDE Browser URL is missing");
             }
         }
         catch (Exception ex)
         {
             _logger.error(ex.toString(), ex);
         }
         finally
         {
             if (rs != null)
             {
                 try { rs.close(); } catch(Exception ex) { }
             }
             if (pstmt != null)
             {
                 try { pstmt.close(); } catch(Exception ex) { }
             }
             if (conn != null)
             {
                 try { conn.close(); } catch(Exception ex) { }
             }
         }
         
         return errors;
     }
 }
