 package org.jbundle.res.base.screen;
 /**
  * @(#)HtmlResources.java  0.00 1-Jan-10 Don Corley
  *
  * Copyright © 2010 tourgeek.com. All Rights Reserved.
  *   don@donandann.com
  */
 import java.util.*;
 
 /**
  * HtmlResources - Resources.
 Html strings
  */
 public class HtmlResources extends ListResourceBundle
 {
     public Object[][] getContents() {
         return contents;
     }
    static final Object[][] contents = {
      // LOCALIZE THIS
     {"Email", "Email"},
     {"English", "English"},
     {"Espanol", "Espa\u00F1ol"},
     {"Help", "Help"},
     {"Home", "Home"},
     {"htmlAppMenu", "<items columns=\"3\">" + "\n" +
         " <td align=\"left\" valign=\"top\">" + "\n" +
         "     <hr/>" + "\n" +
         "     <a href=\"<link/>\"><img src=\"<icon/>\" alt=\"<menutitle/>\" width=\"24\" height=\"24\" border=\"0\"  align=\"center\" /></a>" + "\n" +
         "     <a href=\"<link/>\"><menutitle/></a>" + "\n" +
         " <br/>" + "\n" +
         "<javalogo><img src=\"images/buttons/Java.gif\" width=\"16\" height=\"16\" border=\"0\" />&#160;</javalogo>" + "\n" +
         "<span style=\"font-size: 10pt\"><menudesc/></span>" + "\n" +
         " </td>" + "\n" +
         "</items>"},
     {"htmlBanner", "<table bgcolor=\"#B0B0E0\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">" + "\n" +
         "<tr>" + "\n" +
         "<td><a href=\"/\"><img src=\"images/banners/468x60.jpg\" width=\"468\" height=\"60\" border=\"0\" /></a></td>" + "\n" +
         "</tr>" + "\n" +
         "</table>"},
     {"htmlCellDefault", "<td align=\"center\" valign=\"top\">" + "\n" +
         "    <a href=\"<link/>\">" + "\n" +
         "        <img src=\"<icon/>\" alt=\"<menutitle/>\" width=\"24\" height=\"24\" />" + "\n" +
         "        <br/>" + "\n" +
         "        <menutitle/>" + "\n" +
         "    </a>" + "\n" +
         "</td>"},
     {"htmlContentEnd", "<!-- End Content Area -->" + "\n" +
         "</td>"},
     {"htmlContentStart", "<td valign=\"top\">" + "\n" +
         "<!-- Start Content Area -->"},
     {"htmlDefault", "<hr>" + "\n" +
         "<form action=\"app\" method=\"get\">" + "\n" +
         "<pre>" + "\n" +
         "Record : <input type=\"text\" length=\"50\" name=record value=\"<record/>\" />" + "\n" +
         "Screen : <input type=\"text\" length=\"50\" name=screen value=\"<screen/>\" />" + "\n" +
         " Limit : <input type=\"text\" maxlength=\"7\" name=limit value=\"100\" />" + "\n" +
         "</pre>" + "\n" +
         "<input type=\"submit\" name=\"move\" value=\"Submit\" />" + "\n" +
         "</form>" + "\n" +
         "<hr/>" + "\n" +
         "<form action=\"app\" method=\"get\">" + "\n" +
         "<pre>" + "\n" +
         "Menu : <input type=\"text\" length=\"50\" name=\"menu\" />" + "\n" +
         "</pre>" + "\n" +
         "<input type=\"submit\" name=\"move\" value=\"Submit\" />" + "\n" +
         "</form>" + "\n" +
         "<hr>" + "\n" +
         "<form action=\"app\" method=\"get\">" + "\n" +
         "<input type=\"hidden\" name=\"applet\" value=\"\" />" + "\n" +
         "<pre>" + "\n" +
         "Record : <input type=\"text\" length=\"50\" name=record value=\"<record/>\" />" + "\n" +
         "Screen : <input type=\"text\" length=\"50\" name=screen value=\"<screen/>\" />" + "\n" +
         "</pre>" + "\n" +
         "<input type=\"submit\" name=\"move\" value=\"Submit\" />" + "\n" +
         "</form>" + "\n" +
         "<hr/>"},
     {"htmlHeaderEnd", "<title><menutitle/></title>" + "\n" +
         "<meta name=\"Keywords\" content=\"<keywords/>travel, tours, custom tours, air, hotel, sightseeing, transfer, cruise, car rental, airport transfer, flight, travel agent\">" + "\n" +
         "<meta name=\"description\" content=\"<menudesc/>\">" + "\n" +
         "</head>" + "\n" +
         "<body marginwidth=\"0\" marginheight=\"0\">"},
     {"htmlHeaderEndDefault", "<title><menutitle/></title>" + "\n" +
         "</head>" + "\n" +
         "<body>" + "\n" +
         "<h1><menutitle/></h1>"},
     {"htmlHeaderStart", "<html>" + "\n" +
         "<head>" + "\n" +
         "<link rel=\"stylesheet\" type=\"text/css\" href=\"org/jbundle/res/docs/styles/css/style.css\" title=\"basicstyle\">" + "\n" +
         "<link rel=\"shortcut icon\" href=\"images/com/favicon.ico\" type=\"image/x-icon\">"},
     {"htmlHelp", "<table border=\"0\" cellpadding=\"5\" width=\"100%\">" + "\n" +
         "<tr>" + "\n" +
         "<td align=\"center\" colspan=\"2\"><font size=\"+2\" style=\"bold\"><menutitle/></font></td>" + "\n" +
         "</tr>" + "\n" +
         "<tr>" + "\n" +
         "<td valign=\"top\" align=\"right\"><font style=\"bold\" size=\"+1\">Description:</font></td>" + "\n" +
         "<td valign=\"top\" align=\"left\" width=\"100%\"><i><menudesc/></i></td>" + "\n" +
         "</tr>" + "\n" +
         "<tr>" + "\n" +
         "<td valign=\"top\" align=\"right\"><font style=\"bold\" size=\"+1\">Operation:</font></td>" + "\n" +
         "<td valign=\"top\" align=\"left\" width=\"100%\"><help/></td>" + "\n" +
         "</tr>" + "\n" +
         "<optional field=\"SeeAlso\">" + "\n" +
         "<tr>" + "\n" +
         "<td valign=\"top\" align=\"right\"><font style=\"bold\" size=\"+1\">See Also:</font></td>" + "\n" +
         "<td valign=\"top\" align=\"left\" width=\"100%\">" + "\n" +
         "<seealso>" + "\n" +
         "<a href=\"<link/>&help=\"><menutitle/></a><br/>" + "\n" +
         "</seealso></td>" + "\n" +
         "</tr>" + "\n" +
         "</optional>" + "\n" +
         "<optional field=\"TechnicalInfo\">" + "\n" +
         "<tr>" + "\n" +
         "<td valign=\"top\" align=\"right\"><font style=\"bold\" size=\"+1\">Technical:</font></td>" + "\n" +
         "<td valign=\"top\" align=\"left\" width=\"100%\"><technical/><br>" + "\n" +
         "<a href=\"/html/help/programmer/app/com/tourapp/<PackageDir/>/<field name=ClassName/>.html\">JavaDoc</a><br>" + "\n" +
         "<a href=\"/source/com/tourapp/<PackageDir/>/<field name=ClassName/>.java\">Java Code</a><br>" + "\n" +
         "<techinfo/></td>" + "\n" +
         "</tr>" + "\n" +
         "</optional>" + "\n" +
         "</table>"},
     {"htmlHelpDefault", "<table width=\"100%\" border=\"0\" cellpadding=\"5\">" + "\n" +
         "<tr>" + "\n" +
         "<td colspan=\"2\"><font size=\"+2\" style=\"bold\"><center>Basic Help Index</center></font></td>" + "\n" +
         "</tr><tr>" + "\n" +
         "<td align=center><a href=\"?help=Screen\">Data Entry Forms</A></TD>" + "\n" +
         "<td align=center><a href=\"?help=GridScreen\">Data Display Tables</A></TD>" + "\n" +
         "</tr><tr>" + "\n" +
         "<td align=center><a href=\"?help=ToolScreen\">Toolbar Button Functions</A></TD>" + "\n" +
         "<td align=center><a href=\"?help=MenuScreen\">Using Menus</A></P>" + "\n" +
         "</tr>" + "\n" +
         "</table>" + "\n" +
         "" + "\n" +
         "<hr/>" + "\n" +
         "" + "\n" +
         "<table width=\"100%\" border=\"0\" cellpadding=\"5\">" + "\n" +
         "<tr>" + "\n" +
         "<td colspan=\"3\"><font size=\"+2\" style=\"bold\"><center>Application Systems</center></font></td>" + "\n" +
         "</tr><tr>" + "\n" +
         "<td><a href=\"?menu=assetdr&help=\">Asset Debt System</a></td>" + "\n" +
         "<td><a href=\"?menu=agency&help=\">Agency Maintenance System</a></td>" + "\n" +
         "<td><a href=\"?menu=acctpay&help=\">Accounts Payable System</a></td>" + "\n" +
         "</tr><tr>" + "\n" +
         "<td><a href=\"?menu=acctrec&help=\">Accounts Receivable System</a></td>" + "\n" +
         "<td><a href=\"?menu=booking&help=\">Booking System</a></td>" + "\n" +
         "<td><a href=\"?menu=payroll&help=\">Payroll System</a></td>" + "\n" +
         "</tr><tr>" + "\n" +
         "<td><a href=\"?menu=genled&help=\">General Ledger System</a></td>" + "\n" +
         "<td><a href=\"?menu=ticket&help=\">Ticketing System</a></td>" + "\n" +
         "<td><a href=\"?menu=market&help=\">Marketing System</a></td>" + "\n" +
         "</tr>" + "\n" +
         "</table>"},
     {"htmlHelpMenu", "<center><h2><menutitle/></h2></center>" + "\n" +
         "<h3>Description:</h3>" + "\n" +
         "<p><menudesc/></p>" + "\n" +
         "<h3>Menu items:</h3>" + "\n" +
         "<items columns=\"1\">" + "\n" +
         "<td><a href=<link/>><img src=\"<icon/>\" width=\"24\" height=\"24\" alt=\"Run <menutitle/>\" /></a></td>" + "\n" +
         "<td><a href=\"<link/>&help=\"><img src=\"images/icons/Help.gif\" width=\"24\" height=\"24\" alt=\"Help for <menutitle/>\" /></a></td>" + "\n" +
         "<td><a href=\"<link/>&help=\">" + "\n" +
         "<menutitle/></a></td><td><menudesc/></td>" + "\n" +
         "</items>"},
     {"htmlHelpMenubar", "<table bgcolor=\"#b0b0e0\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">" + "\n" +
         " <tr>" + "\n" +
         " <td> <a href=\"?menu=\"><img src=\"images/nav/Home.gif\" alt=\"Home\" border=\"0\" width=\"75\" height=\"21\" /></a></td>" + "\n" +
         " <td> <a href=\"?screen=<screen/>&record=<record/>&tech=&help=\"><img src=\"images/nav/Tech.gif\" alt=\"Programmer's technical Information\" border=\"0\" width=\"75\" height=\"21\" /></a></td>" + "\n" +
         " <td> <a href=\"?screen=<screen/>&record=<record/>\"><img src=\"images/nav/Run.gif\" alt=\"Run this program\" border=\"0\" width=\"75\" height=\"21\" /></a></td>" + "\n" +
         " <td> <a href=\"?record=.main.Message&command=New&screenname=<screen/>&recordname=<record/>\"><img src=\"images/nav/Bugs.gif\" alt=\"Report a bug in this program\" border=\"0\" width=\"75\" height=\"21\" /></a></td>" + "\n" +
         " <td width=\"100%\" align=\"center\">     <font face=\"Arial, Helvetica, SanSerif\" color=\"white\"><strong>Help System</strong></font></td>" + "\n" +
         " <td align=\"right\"> <a href=\"?help=\"><img src=\"images/nav/Help.gif\" alt=\"Main Help Menu\" border=\"0\" width=\"75\" height=\"21\" /></a></td>" + "\n" +
         " </tr>" + "\n" +
         " <tr>" + "\n" +
         " <td bgcolor=\"black\" background=\"\" width=\"100%\" height=\"1\" colspan=\"5\"><img src=\"images/graphics/1ptrans.gif\" width=\"1\" height=\"1\" alt=\"\" border=\"0\" /></td>" + "\n" +
         " </tr>" + "\n" +
         "</table>"},
     {"htmlLogo", "<table cellpadding=\"0\" cellspacing=\"0\" class=\"top-menu\">" + "\n" +
         "<tr>" + "\n" +
         "<td>" + "\n" +
         "<a href=\"?menu=Main\">" + "\n" +
         "<img src=\"{logopath}\" width=\"70\" height=\"60\" border=\"0\" />" + "\n" +
         "</a>" + "\n" +
         "</td>" + "\n" +
         "<td align=\"center\" valign=\"bottom\">" + "\n" +
         "<a href=\"?menu=Main\">" + "\n" +
        "<img src=\"images/com/name.gif\" border=\"0\" /><br/>" + "\n" +
         "<span style=\"color: white; font-family: helvetica, arial, san-serif; font-weight: normal\">&#160;&#160;{mission}</span></a></td>" + "\n" +
         "<td width=\"100%\">&#160;</td>" + "\n" +
         "<td valign=\"top\">" + "\n" +
         "<form action=\"?menu=&amp;preferences=\" method=\"post\">" + "\n" +
         "<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\">" + "\n" +
         " <tr>" + "\n" +
         "  <td align=\"right\" valign=\"baseline\">" + "\n" +
         "   <input type=\"hidden\" name=\"menu\" value=\"\" /><span style=\"font-size: 10pt; color: white; font-family: helvetica, arial, san-serif\">&#160;{Email}:&#160;</span>" + "\n" +
         "  </td>" + "\n" +
         "  <td align=\"left\" valign=\"baseline\" colspan=\"3\"><input type=\"text\" size=\"20\" name=\"user\" value=\"<user/>\" /></td>" + "\n" +
         "  <td align=\"right\" valign=\"baseline\">" + "\n" +
         "   <input type=\"hidden\" name=\"menu\" value=\"\" /><span style=\"font-size: 10pt; color: white; font-family: helvetica, arial, san-serif\">&#160;{Password}:&#160;</span>" + "\n" +
         "  </td>" + "\n" +
         "  <td align=\"left\" valign=\"baseline\" colspan=\"2\"><input type=\"password\" size=\"20\" name=\"password\" value=\"\" /></td>" + "\n" +
         " </tr>" + "\n" +
         " <tr>" + "\n" +
         " <td align=\"right\" valign=\"baseline\">" + "\n" +
         "   <span style=\"font-size: 10pt; color: white; font-family: helvetica, arial, san-serif\">&#160;Language:&#160;</span>" + "\n" +
         "  </td>" + "\n" +
         "  <td align=\"left\" valign=\"baseline\"><select" + "\n" +
         "   name=\"language\" onChange=\"submit();\">" + "\n" +
         "         <option selected=\"true\" value=\"<language/>\">(default)</option>" + "\n" +
         "         <option value=\"en\">English</option>" + "\n" +
         "         <option value=\"es\">Espa\u00F1ol</option>" + "\n" +
         "      </select>" + "\n" +
         "   </td>" + "\n" +
         "   <td align=\"left\" valign=\"baseline\">" + "\n" +
         "   <a href=\"?menu=&amp;language=en&amp;preferences=\"><img src=\"images/flags/english.gif\" width=\"16\" height=\"16\" alt=\"{English}\" border=\"0\" /></a>" + "\n" +
         "   </td>" + "\n" +
         "   <td align=\"left\" width=\"50%\" valign=\"baseline\">" + "\n" +
         "   <a href=\"?menu=&amp;language=es&amp;preferences=\"><img src=\"images/flags/espanol.gif\" width=\"16\" height=\"16\" alt=\"{Espanol}\" border=\"0\" /></a>" + "\n" +
         "   <input type=\"hidden\" name=\"preferences\" value=\"\" />" + "\n" +
         "  </td>" + "\n" +
         "  <td align=\"right\" valign=\"baseline\">" + "\n" +
         "   <span style=\"font-size: 10pt; color: white; font-family: helvetica, arial, san-serif\">&#160;{Save}:&#160;</span>" + "\n" +
         "  </td>" + "\n" +
         "  <td align=\"left\" valign=\"baseline\"><input type=\"checkbox\" name=\"saveuser\" value=\"Yes\" checked=\"true\" /></td>" + "\n" +
         "  <td align=\"right\"><input type=\"submit\" value=\"{Login}\" /></td>" + "\n" +
         " </tr>" + "\n" +
         " </table>" + "\n" +
         "</form>" + "\n" +
         "</td>" + "\n" +
         "  <td valign=\"top\">" + "\n" +
         "   <a href=\"?menu=&amp;logos=No&amp;preferences=\"><img src=\"images/buttons/Close.gif\" alt=\"Close the header pane\" border=\"0\" width=\"16\" height=\"16\" /></a>" + "\n" +
         "  </td>" + "\n" +
         "</tr>" + "\n" +
         "</table>"},
     {"htmlMenubar", "<table class=\"top-menu\" cellpadding=\"0\" cellspacing=\"0\">" + "\n" +
         "<tr>" + "\n" +
         "<td><span class=\"link\"><a href=\"?menu=Main\"><img class=\"button\" src=\"images/buttons/Home.gif\" width=\"16\" height=\"16\" alt=\"Site home page\" /></a><span><a class=\"button\" href=\"?menu=Main\">{Home}</a></span></span>" + "\n" +
         "<span class=\"link\"><a href=\"?menu=\"><img class=\"button\" src=\"images/buttons/MyHome.gif\" width=\"16\" height=\"16\" alt=\"Your home page\" /></a><span><a class=\"button\" href=\"?menu=\">{My Home}</a></span></span></td>" + "\n" +
         "<td class=\"menu-title\"><span class=\"spantitle\"><menutitle/></span></td>" + "\n" +
         "<td class=\"help-button\">" + "\n" +
         "<span class=\"spantitle\"><user/></span>" + "\n" +
         "<span class=\"link\"><a href=\"?user=&amp;menu=\"><img class=\"button\" src=\"images/buttons/Logout.gif\" width=\"16\" height=\"16\" alt=\"Sign out this user\" /></a><a class=\"button\" href=\"?user=&amp;menu=\">{Logout}</a></span>" + "\n" +
         "<span class=\"link\"><a href=\"?screen=.main.user.screen.UserPreferenceScreen&amp;java=no\"><img class=\"button\" src=\"images/buttons/Settings.gif\" width=\"16\" height=\"16\" alt=\"View or change preferences\" /></a><a class=\"button\" href=\"?screen=.main.user.screen.UserPreferenceScreen&amp;java=no\">{Settings}</a></span>" + "\n" +
         "<span class=\"link\"><a href=\"appxhtml?help=&amp;screen=.base.screen.model.MenuScreen&amp;menu='<url/>'&amp;class=.base.screen.model.report.HelpScreen\"><img class=\"button\" src=\"images/buttons/Help.gif\" width=\"16\" height=\"16\" alt=\"Help for the current screen\" /></a><a class=\"button\" href=\"appxhtml?help=&amp;screen=.base.screen.model.MenuScreen&amp;menu='<url/>'&amp;class=.base.screen.model.report.HelpScreen\">{Help}</a></span>" + "\n" +
         "</td>" + "\n" +
         "</tr><tr style=\"background-color: black\"><td colspan=\"6\" height=\"1\" style=\"width: 100%;\"></td></tr>" + "\n" +
         "</table>"},
     {"htmlMenubarAnon", "<table class=\"top-menu\" cellpadding=\"0\" cellspacing=\"0\">" + "\n" +
         "<tr>" + "\n" +
         "<td><span class=\"link\"><a href=\"?menu=Main\"><img class=\"button\" src=\"images/buttons/Home.gif\" width=\"16\" height=\"16\" alt=\"Site home page\" /></a><span><a class=\"button\" href=\"?menu=Main\">{Home}</a></span></span>" + "\n" +
         "<span class=\"link\"><a href=\"?menu=\"><img class=\"button\" src=\"images/buttons/MyHome.gif\" width=\"16\" height=\"16\" alt=\"Your home page\" /></a><span><a class=\"button\" href=\"?menu=\">{My Home}</a></span></span></td>" + "\n" +
         "<td class=\"menu-title\"><span class=\"spantitle\"><menutitle/></span></td>" + "\n" +
         "<td class=\"help-button\">" + "\n" +
         "<span class=\"spantitle\"><user/></span>" + "\n" +
         "<span class=\"link\"><a href=\"?screen=.main.user.screen.UserLoginScreen&amp;java=no\"><img class=\"button\" src=\"images/buttons/Login.gif\" width=\"16\" height=\"16\" alt=\"Sign in this user\" /></a><a class=\"button\" href=\"?screen=.main.user.screen.UserLoginScreen&amp;java=no\">{Login}</a></span>" + "\n" +
         "<span class=\"link\"><a href=\"?screen=.main.user.screen.UserPreferenceScreen&amp;java=no\"><img class=\"button\" src=\"images/buttons/Settings.gif\" width=\"16\" height=\"16\" alt=\"View or change preferences\" /></a><a class=\"button\" href=\"?screen=.main.user.screen.UserPreferenceScreen&amp;java=no\">{Settings}</a></span>" + "\n" +
         "<span class=\"link\"><a href=\"appxhtml?help=&amp;screen=.base.screen.model.MenuScreen&amp;menu='<url/>'&class=.base.screen.model.report.HelpScreen\"><img class=\"button\" src=\"images/buttons/Help.gif\" width=\"16\" height=\"16\" alt=\"Help for the current screen\" /></a><a class=\"button\" href=\"appxhtml?help=&amp;screen=.base.screen.model.MenuScreen&amp;menu='<url/>'&amp;class=.base.screen.model.report.HelpScreen\">{Help}</a></span>" + "\n" +
         "</td>" + "\n" +
         "</tr><tr style=\"background-color: black\"><td colspan=\"6\" height=\"1\" style=\"width: 100%;\"></td></tr>" + "\n" +
         "</table>"},
     {"htmlNavAfterContent", "</tr>" + "\n" +
         "<tr id=\"navAfterContent\" class=\"navAfterContent\">" + "\n" +
         "<th id=\"navStartVShadow\" class=\"navStart\" style=\"background-image: url(images/graphics/NavVShadow.gif);\"><img src=\"images/graphics/1ptrans.gif\" width=\"1\" height=\"1\" /></th>" + "\n" +
         "<th id=\"navStartSECorner\" class=\"navStartShadow\" style=\"background-image: url(images/graphics/NavSECorner.gif);\"><img src=\"images/graphics/1ptrans.gif\" width=\"1\" height=\"1\" /></th>"},
     {"htmlNavEnd", "<!-- End Nav menu -->" + "\n" +
         "</th>" + "\n" +
         "<th id=\"navStartShadow\" class=\"navStartShadow\" style=\"background-image: url(images/graphics/NavHShadow.gif);\"><img src=\"images/graphics/1ptrans.gif\" width=\"1\" height=\"1\" /></th>"},
     {"htmlNavIconsOnlyAfterContent", "</tr>" + "\n" +
         "<tr id=\"navAfterContent\" class=\"navAfterContent\">" + "\n" +
         "<th id=\"navStartVShadow\" class=\"navIconsOnlyStart\" style=\"background-image: url(images/graphics/NavVShadow.gif);\"><img src=\"images/graphics/1ptrans.gif\" width=\"1\" height=\"1\" /></th>" + "\n" +
         "<th id=\"navStartSECorner\" class=\"navStartShadow\" style=\"background-image: url(images/graphics/NavSECorner.gif);\"><img src=\"images/graphics/1ptrans.gif\" width=\"1\" height=\"1\" /></th>"},
     {"htmlNavIconsOnlyStart", "<th id=\"navStart\" class=\"navIconsOnlyStart\" style=\"background-image: url(images/graphics/NavBack.gif);\">" + "\n" +
         " <!-- Start Nav Menu -->" + "\n" +
         " <div align=\"right\">" + "\n" +
         "<a href=\"?menu=&amp;navmenus=&amp;preferences=\"><img src=\"images/buttons/Expand.gif\" alt=\"Change the menu pane to full descriptions\" border=\"0\" width=\"16\" height=\"16\" /></a><a href=\"?menu=&amp;navmenus=No&amp;preferences=\"><img src=\"images/buttons/Close.gif\" alt=\"Close the menu pane\" border=\"0\" width=\"16\" height=\"16\" /></a></div>" + "\n" +
         " <hr/>"},
     {"htmlNavMenu", "<a href=\"?menu=Home\"><img src=\"images/buttons/Home.gif\" width=\"16\" height=\"16\" alt=\"Home\" class=\"button\"/>&#160;Home</a><br/>" + "\n" +
         "<a href=\"?menu=\"><img src=\"images/buttons/MyHome.gif\" width=\"16\" height=\"16\" alt=\"My home\" class=\"button\"/>&#160;My home</a><br/>" + "\n" +
         "<a href=\"?screen=.main.user.screen.UserPreferenceScreen&java=no\"><img src=\"images/buttons/Settings.gif\" width=\"16\" height=\"16\" alt=\"View or change settings\" class=\"button\"/>&#160;Settings</a><br/>" + "\n" +
         "<a href=\"?screen=.main.user.screen.UserLoginScreen&java=no\"><img src=\"images/buttons/Login.gif\" width=\"16\" height=\"16\" alt=\"Sign in\" class=\"button\"/>&#160;Sign in</a><br/>" + "\n" +
         "<a href=\"?user=&amp;menu=\"><img src=\"images/buttons/Logoff.gif\" width=\"16\" height=\"16\" alt=\"Sign out\" class=\"button\"/>&#160;Sign out</a><br/>" + "\n" +
         "<a href=\"?help=&class=.base.screen.model.report.HelpScreen\"><img src=\"images/buttons/Help.gif\" width=\"16\" height=\"16\" alt=\"Help for the current screen\" class=\"button\"/>&#160;Help</a><br/>"},
     {"htmlNavMenuDon", "      <tr valign=\"top\">" + "\n" +
         "        <td width=\"15%\"  bgcolor=\"#b0b0e0\">" + "\n" +
         "<div align=right><a href=\"?menu=&navmenus=Icons+Only&preferences=\"><img src=\"images/buttons/Contract.gif\" alt=\"Change the menu pane to Icons only\" border=\"0\" width=\"16\" height=\"16\" /></a><a href=\"?menu=&navmenus=No&preferences=\"><img src=\"images/buttons/Close.gif\" alt=\"Close the menu pane\" border=\"0\" width=\"16\" height=\"16\"></a></div>" + "\n" +
         "        <strong>Don's personal menu</strong><br><hr>" + "\n" +
         "        <a href=\"app?menu=\"><img src=\"images/buttons/Home.gif\" align=\"center\" alt=\"Home\" border=\"0\" width=\"16\" height=\"16\" />" + "\n" +
         "                Home</a><br/>" + "\n" +
         "        <a href=\"booking/entry\"><img src=\"images/buttons/Mail.gif\" align=\"center\" alt=\"E-Mail\" border=\"0\" width=\"16\" height=\"16\" />" + "\n" +
         "                E-Mail Center</a><br/>" + "\n" +
         "         </td>" + "\n" +
         "<td>"},
     {"htmlNavMenuIconsOnly", "<a href=\"?menu=Home\"><img src=\"images/buttons/Home.gif\" width=\"16\" height=\"16\" alt=\"Home\" class=\"button\"/></a><br/>" + "\n" +
         "<a href=\"?menu=\"><img src=\"images/buttons/MyHome.gif\" width=\"16\" height=\"16\" alt=\"My home\" class=\"button\"/></a><br/>" + "\n" +
         "<a href=\"?screen=.main.user.screen.UserPreferenceScreen&java=no\"><img src=\"images/buttons/Settings.gif\" width=\"16\" height=\"16\" alt=\"View or change settings\" class=\"button\"/></a><br/>" + "\n" +
         "<a href=\"?screen=.main.user.screen.UserLoginScreen&java=no\"><img src=\"images/buttons/Login.gif\" width=\"16\" height=\"16\" alt=\"Sign in\" class=\"button\"/></a><br/>" + "\n" +
         "<a href=\"?user=&amp;menu=\"><img src=\"images/buttons/Logoff.gif\" width=\"16\" height=\"16\" alt=\"Sign out\" class=\"button\"/></a><br/>" + "\n" +
         "<a href=\"?help=&class=.base.screen.model.report.HelpScreen\"><img src=\"images/buttons/Help.gif\" width=\"16\" height=\"16\" alt=\"Help for the current screen\" class=\"button\"/></a><br/>"},
     {"htmlNavStart", "<th id=\"navStart\" class=\"navStart\" style=\"background-image: url(images/graphics/NavBack.gif);\">" + "\n" +
         "<!-- Start Nav Menu -->" + "\n" +
         "<div align=\"right\">" + "\n" +
         "<a href=\"?menu=&amp;navmenus=IconsOnly&amp;preferences=\"><img src=\"images/buttons/Contract.gif\" alt=\"Change the menu pane to Icons only\" border=\"0\" width=\"16\" height=\"16\" /></a><a href=\"?menu=&amp;navmenus=No&amp;preferences=\"><img src=\"images/buttons/Close.gif\" alt=\"Close the menu pane\" border=\"0\" width=\"16\" height=\"16\" /></a>" + "\n" +
         "</div>" + "\n" +
         "<strong>Navigation menu</strong>" + "\n" +
         "<br/>" + "\n" +
         "<hr/>"},
     {"htmlTableEndDefault", "</tr>" + "\n" +
         "</table>"},
     {"htmlTableStart", "<table border=\"0\" width=\"100%\" height=\"50%\" cellspacing=\"0\" cellpadding=\"5\">" + "\n" +
         "<tr valign=\"top\">"},
     {"htmlTableStartDefault", "<table border=\"0\" width=\"100%\" cellspacing=\"10\">" + "\n" +
         "<tr>"},
     {"htmlTrailer", "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">" + "\n" +
         "<tr valign=\"center\">" + "\n" +
         "<td align=\"left\" valign=\"center\">" + "\n" +
        "<a href=\"http://www.tourgeek.com\"><img src=\"images/com/tourgeek/nameblack.gif\" border=\"0\" /></a><br />" + "\n" +
         "<span style=\"font-size: 8pt; color: black; font-family: helvetica, arial, san-serif\">\u00A9 Copyright 2010 <a href=\"http://www.tourgeek.com\">tourgeek<span style=\"color: red; font-weight: bold\">.</span>com</a>. All rights reserved.&#160;&#160;&#160;&#160;</span><br />" + "\n" +
         "<span style=\"font-size: 8pt; color: black; font-family: helvetica, arial, san-serif\"><a href=\"mailto:webmaster@tourgeek.com\"><img src=\"images/buttons/Mail.gif\" width=\"16\" height=\"16\" border=\"0\" />&#160;e-mail the webmaster.</a></span>" + "\n" +
         "<br /><span style=\"color: red; font-size: 8pt\"><b>Note:</b> This a demo site. You can't really book tours here.<br /></span>" + "\n" +
         "</td>" + "\n" +
         "<td align=\"right\"></td>" + "\n" +
         "<td align=\"right\" valign=\"top\"><a href=\"./?menu=&trailers=No&preferences=\"><img src=\"images/buttons/Close.gif\" alt=\"Close the footer pane\" border=\"0\" width=\"16\" height=\"16\" /></a></td>" + "\n" +
         "</tr>" + "\n" +
         "</table>"},
     {"Login", "Sign in"},
     {"logopath", "images/com/logo.gif"},
     {"Logout", "Sign out"},
     {"mission", "Build&#160;it&#160;yourself&#160;tours"},
     {"My Home", "My Home"},
     {"Password", "Password"},
     {"Save", "Save"},
     {"Settings", "Settings"}        // END OF MATERIAL TO LOCALIZE
     };
 }
