 // Copyright (c) 2006 ScenPro, Inc.
 
// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/FreestyleSearchForm.java,v 1.7 2008-06-17 19:44:05 hebell Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.freestylesearch.ui;
 
 import gov.nih.nci.cadsr.freestylesearch.util.Search;
 import gov.nih.nci.cadsr.freestylesearch.util.SearchAC;
 import gov.nih.nci.cadsr.freestylesearch.util.SearchException;
 import gov.nih.nci.cadsr.freestylesearch.util.SearchMatch;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.Globals;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.util.MessageResources;
 
 import org.jboss.Version;
 
 /**
  * The ActionForm mapping the JSP used by this example user interface.
  * 
  * @author lhebel Mar 7, 2006
  */
 public class FreestyleSearchForm extends ActionForm
 {
     /**
      * Constructor
      *
      */
     public FreestyleSearchForm()
     {
         // Set defaults for all data.
         _excludeRetired = false;
         _firstTime = "Y";
         _displayOptions = "N";
         _phrase = "";
         _limit = 100;
         _score = 3;
         _matching = SearchMatch.BEST;
         _types = new boolean[SearchAC.count()];
         for (int i = 0; i < _types.length; ++i)
             _types[i] = true;
     }
     
     /**
      * Set the search phrase input field.
      * 
      * @param val_ the search phrase, i.e. one or more terms
      */
     public void setPhrase(String val_)
     {
         _phrase = val_;
     }
 
     /**
      * Get the input search phrase.
      * 
      * @return return the user search phrase
      */
     public String getPhrase()
     {
         return _phrase;
     }
     
     /**
      * Get the maximum results limit.
      * 
      * @return the current maximum return limit.
      */
     public String getLimit()
     {
         return String.valueOf(_limit);
     }
     
     /**
      * Get the maximum results limit.
      * 
      * @return the current maximum return limit.
      */
     public int getLimitInt()
     {
         return _limit;
     }
 
     /**
      * Set the maximum results limit.
      * 
      * @param limit_ the limit for the result set.
      */
     public void setLimit(String limit_)
     {
         _limit = Integer.parseInt(limit_);
     }
     
     /**
      * Get the term comparison mode.
      * 
      * @return the term comparison mode, i.e. exact, partial or best.
      */
     public String getMatching()
     {
         return String.valueOf(_matching.toInt());
     }
     
     /**
      * Get the term comparison mode.
      * 
      * @return the term comparison mode, i.e. exact, partial or best.
      */
     public SearchMatch getMatchingEnum()
     {
         return _matching;
     }
 
     /**
      * Set the term comparison mode.
      * 
      * @param matching_ the integer term comparison as defined in SearchMatch.toInt().
      */
     public void setMatching(String matching_)
     {
         _matching = SearchMatch.valueOf(Integer.parseInt(matching_));
     }
     
     /**
      * Get the score group count limit.
      * 
      * @return the score group count limit
      */
     public String getScore()
     {
         return String.valueOf(_score);
     }
     
     /**
      * Get the score group count limit.
      * 
      * @return the score group count limit.
      */
     public int getScoreInt()
     {
         return _score;
     }
 
     /**
      * Set the score group count limit.
      * 
      * @param score_ the score group count limit.
      */
     public void setScore(String score_)
     {
         _score = Integer.parseInt(score_);
     }
     
     /**
      * Get the display options flag, used to control the "Options" on the user interface.
      * 
      * @return the display options flag
      */
     public String getDisplayOptions()
     {
         return _displayOptions;
     }
     
     /**
      * Set the display options flag, used to control the "Options" on the user interface.
      * 
      * @param opt_ the display options flag
      */
     public void setDisplayOptions(String opt_)
     {
         _displayOptions = opt_;
     }
     
     /**
      * Get the AC types selections.
      * 
      * @return the restriction settings by type
      */
     public boolean[] getTypes()
     {
         return _types;
     }
     
     /**
      * Get the first time flag, i.e. is this the first use of freestyle in the current browser session.
      * 
      * @return return the first time flag
      */
     public String getFirstTime()
     {
         return _firstTime;
     }
     
     /**
      * Set the browser session first time flag.
      * 
      * @param flag_ 'Y' for the first time, otherwise 'N'
      */
     public void setFirstTime(String flag_)
     {
         _firstTime = flag_;
     }
     
     /**
      * Set the exclude retired AC flag
      * 
      * @param flag_ "Y" to exclude reitred AC's, otherwise don't exclude them.
      */
     public void setExcludeRetired(String flag_)
     {
         if (flag_ != null && flag_.equals("Y"))
             _excludeRetired = true;
         else
             _excludeRetired = false;
     }
 
     /**
      * Get the exlude retired AC flag.
      * 
      * @return "Y" to exclude retired AC's.
      */
     public String getExcludeRetired()
     {
         return (_excludeRetired) ? "Y" : "N";
     }
 
     /**
      * Get the exlude retired AC flag.
      * 
      * @return "Y" to exclude retired AC's.
      */
     public boolean getExcludeRetiredBool()
     {
         return _excludeRetired;
     }
     
     /**
      * Set the exclude Test Context flag
      * 
      * @param flag_ "Y" to exclude "Test"
      */
     public void setExcludeTest(String flag_)
     {
         if (flag_ != null && flag_.equals("Y"))
             _excludeTest = true;
         else
             _excludeTest = false;
     }
     
     /**
      * Get the exclude Test Context flag
      * 
      * @return "Y" to exclude "Test"
      */
     public String getExcludeTest()
     {
         return (_excludeTest) ? "Y" : "N";
     }
     
     /**
      * Get the exclude Training Context flag
      * 
      * @return true to exclude "Training"
      */
     public boolean getExcludeTrainBool()
     {
         return _excludeTrain;
     }
     
     /**
      * Set the exclude Training Context flag
      * 
      * @param flag_ "Y" to exclude "Training"
      */
     public void setExcludeTrain(String flag_)
     {
         if (flag_ != null && flag_.equals("Y"))
             _excludeTrain = true;
         else
             _excludeTrain = false;
     }
     
     /**
      * Get the exclude Trianing Context flag
      * 
      * @return "Y" to exclude "Training"
      */
     public String getExcludeTrain()
     {
         return (_excludeTrain) ? "Y" : "N";
     }
     
     /**
      * Get the exclude Test Context flag
      * 
      * @return true to exclude "Test"
      */
     public boolean getExcludeTestBool()
     {
         return _excludeTest;
     }
     
     /**
      * Set the footer banner
      * 
      * @param val_ the footer
      */
     public void setFooter(String val_)
     {
         _footer = val_;
         _footer = null;
     }
     
     /**
      * Get the footer banner
      * 
      * @return the footer
      */
     public String getFooter()
     {
         return _footer;
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
         FreestylePlugIn ds = (FreestylePlugIn) request_.getSession().getServletContext().getAttribute(FreestylePlugIn._DATASOURCE);
 
         if (_footer == null)
         {
             MessageResources msgs = (MessageResources) request_.getSession().getServletContext().getAttribute(Globals.MESSAGES_KEY);
             String temp = msgs.getMessage(Search._vers);
             String jboss = Version.getInstance().getMajor() + "." + Version.getInstance().getMinor() + "." + Version.getInstance().getRevision();
             _footer = 
                 "<table class=\"table3\"><colgroup></colgroup><tbody class=\"secttbody\" />\n"
                     + "<tr><td class=\"ncifmenu\"><span style=\"color: #dddddd\">&nbsp;v&nbsp;"
                     + temp
                     + "&nbsp;(" + jboss + "/" + System.getProperty("java.version") + ")"
                     + "</span></td></tr>\n"
                     + "<tr>\n<td class=\"nciftrtable\">\n"
                    + "<a href=\"mailto:ncicb@pop.nci.nih.gov?subject=caDSR%20Freestyle%20Search\"><span class=\"wdemail\" title=\"Email NCICB Help Desk\">&#42;</span></a>\n"
                     + "<a target=\"_blank\" href=\"http://www.cancer.gov/\"><img border=\"0\" src=\"/freestyle/images/footer_nci.gif\" alt=\"National Cancer Institute Logo\" title=\"National Cancer Institute\"></a>\n"
                     + "<a target=\"_blank\" href=\"http://www.dhhs.gov/\"><img border=\"0\" src=\"/freestyle/images/footer_hhs.gif\" alt=\"Department of Health and Human Services Logo\" title=\"Department of Health and Human Services\"></a>\n"
                     + "<a target=\"_blank\" href=\"http://www.nih.gov/\"><img border=\"0\" src=\"/freestyle/images/footer_nih.gif\" alt=\"National Institutes of Health Logo\" title=\"National Institutes of Health\"></a>\n"
                     + "<a target=\"_blank\" href=\"http://www.usa.gov/\"><img border=\"0\" src=\"/freestyle/images/footer_usagov.gif\" alt=\"USA.gov\" title=\"USA.gov\"></a>\n"
                     + "</td>\n</tr>\n</table>\n";
         }
         
         Search var = new Search();
         var.setDataDescription(ds.getDataSource());
         String seedTime = null;
         try
         {
             seedTime = var.getLastSeedTimestampString();
         }
         catch (SearchException ex)
         {
             seedTime = ex.toString();
         }
         request_.setAttribute("seedTime", seedTime);
 
         // The absence of a search phrase is not really an error but we don't want
         // to proceed to the Action Class
         if (_phrase == null || _phrase.length() == 0)
         {
             errors.add("error", new ActionMessage("error.nosearch"));
         }
 
         // If this is not the first time so update the AC type selections.
         if (_firstTime.charAt(0) == 'N')
         {
             for(int i = 0; i < _types.length; ++i)
             {
                 _types[i] =  (request_.getParameter("restrict" + i) != null);
             }
         }
         else
         {
             _firstTime = "N";
         }
 
         // Set the attributes for proper display on the UI Options.
         for( int i = 0; i < _types.length; ++i)
         {
             if (_types[i])
                 request_.setAttribute("restrict" + i, "Y");
         }
 
         // Return
         return errors;
     }
 
     private static final long serialVersionUID = 88840366374682878L;
 
     private boolean _excludeRetired;
     private boolean _excludeTest;
     private boolean _excludeTrain;
     private String _phrase;
     private int _limit;
     private SearchMatch _matching;
     private int _score;
     private String _displayOptions;
     private boolean[] _types;
     private String _firstTime;
     private String _footer;
 }
