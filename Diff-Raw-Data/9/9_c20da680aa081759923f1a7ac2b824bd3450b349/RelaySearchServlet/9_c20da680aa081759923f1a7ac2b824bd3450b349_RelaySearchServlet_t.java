 package org.torproject.ernie.web;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import java.math.*;
 import java.sql.*;
 import java.text.*;
 import java.util.*;
 import java.util.regex.*;
 
 import org.apache.commons.codec.*;
 import org.apache.commons.codec.binary.*;
 
 /**
  * Web page that allows users to search for relays in the descriptor
  * archives.
  *
  * Possible search terms for testing:
  * - gabelmoo
  * - gabelmoo 2010-09
  * - gabelmoo 2010-09-18
  * - gabelmoo F2044413DAC2E02E3D6BCF4735A19BCA1DE97281
  * - gabelmoo 80.190.246
  * - gabelmoo F2044413DAC2E02E3D6BCF4735A19BCA1DE97281 80.190.246
  * - 5898549205 dc737cc9dca16af6 79.212.74.45
  * - 5898549205 dc737cc9dca16af6
  * - 80.190.246.100
  * - F2044413DAC2E02E3D6BCF4735A19BCA1DE97281
  * - F2044413DAC2E02E3D6BCF4735A19BCA1DE97281 80.190.246
  * - 58985492
  * - 58985492 79.212.74.45
  */
 public class RelaySearchServlet extends HttpServlet {
 
   private static Pattern alphaNumDotDashSpacePattern =
       Pattern.compile("[A-Za-z0-9\\.\\- ]+");
 
   private static Pattern numPattern = Pattern.compile("[0-9]+");
 
   private static Pattern hexPattern = Pattern.compile("[A-Fa-f0-9]+");
 
   private static Pattern alphaNumPattern =
       Pattern.compile("[A-Za-z0-9]+");
 
   private static SimpleDateFormat dayFormat =
       new SimpleDateFormat("yyyy-MM-dd");
 
   private static SimpleDateFormat monthFormat =
       new SimpleDateFormat("yyyy-MM");
 
   private static SimpleDateFormat dateTimeFormat =
       new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
   static {
     dayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     monthFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
   }
 
   private Connection conn = null;
 
   public void init() {
 
     /* Try to load the database driver. */
     try {
       Class.forName("org.postgresql.Driver");
     } catch (ClassNotFoundException e) {
       /* Don't initialize conn and always reply to all requests with
        * "500 internal server error". */
       return;
     }
 
     /* Read JDBC URL from deployment descriptor. */
     String connectionURL = getServletContext().
         getInitParameter("jdbcUrl");
 
     /* Try to connect to database. */
     try {
       conn = DriverManager.getConnection(connectionURL);
     } catch (SQLException e) {
       conn = null;
     }
   }
 
   private void writeHeader(PrintWriter out) throws IOException {
 
 
     out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
           + "Transitional//EN\">\n"
         + "<html>\n"
         + "  <head>\n"
        + "    <title>Tor Metrics Portal: Relay Search</title>\n"
         + "    <meta http-equiv=\"content-type\" content=\"text/html; "
           + "charset=ISO-8859-1\">\n"
         + "    <link href=\"/css/stylesheet-ltr.css\" type=\"text/css\" "
           + "rel=\"stylesheet\">\n"
         + "    <link href=\"/images/favicon.ico\" "
           + "type=\"image/x-icon\" rel=\"shortcut icon\">\n"
         + "  </head>\n"
         + "  <body>\n"
         + "    <div class=\"center\">\n"
         + "      <table class=\"banner\" border=\"0\" cellpadding=\"0\" "
           + "cellspacing=\"0\" summary=\"\">\n"
         + "        <tr>\n"
         + "          <td class=\"banner-left\"><a "
           + "href=\"/index.html\"><img src=\"/images/top-left.png\" "
           + "alt=\"Click to go to home page\" width=\"193\" "
           + "height=\"79\"></a></td>\n"
         + "          <td class=\"banner-middle\">\n"
         + "            <a href=\"/\">Home</a>\n"
         + "            <a href=\"graphs.html\">Graphs</a>\n"
         + "            <a href=\"research.html\">Research</a>\n"
         + "            <a href=\"status.html\">Status</a>\n"
         + "            <br>\n"
         + "            <font size=\"2\">\n"
         + "              <a href=\"exonerator.html\">ExoneraTor</a>\n"
         + "              <a class=\"current\">Relay Search</a>\n"
         + "              <a href=\"consensus-health.html\">Consensus "
           + "Health</a>\n"
         + "            </font>\n"
         + "          </td>\n"
         + "          <td class=\"banner-right\"></td>\n"
         + "        </tr>\n"
         + "      </table>\n"
         + "      <div class=\"main-column\" style=\"margin:5; "
           + "Padding:0;\">\n"
         + "        <h2>Relay Search</h2>\n");
   }
 
   private void writeFooter(PrintWriter out) throws IOException {
     out.println("        <br>\n"
         + "      </div>\n"
         + "    </div>\n"
         + "    <div class=\"bottom\" id=\"bottom\">\n"
         + "      <p>This material is supported in part by the National "
           + "Science Foundation under Grant No. CNS-0959138. Any "
           + "opinions, finding, and conclusions or recommendations "
           + "expressed in this material are those of the author(s) and "
           + "do not necessarily reflect the views of the National "
           + "Science Foundation.</p>\n"
         + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
           + "href=\"https://www.torproject.org/docs/trademark-faq.html.en\">"
           + "registered trademarks</a> of The Tor Project, Inc.</p>\n"
         + "      <p>Data on this site is freely available under a <a "
           + "href=\"http://creativecommons.org/publicdomain/zero/1.0/\">"
           + "CC0 no copyright declaration</a>: To the extent possible "
           + "under law, the Tor Project has waived all copyright and "
           + "related or neighboring rights in the data. Graphs are "
           + "licensed under a <a "
           + "href=\"http://creativecommons.org/licenses/by/3.0/us/\">"
           + "Creative Commons Attribution 3.0 United States "
           + "License</a>.</p>\n"
         + "    </div>\n"
         + "  </body>\n"
         + "</html>");
     out.close();
   }
 
   public final String CONSENSUS_DIRECTORY =
       "/srv/metrics.torproject.org/ernie/directory-archive/consensus";
 
   public void doGet(HttpServletRequest request,
       HttpServletResponse response) throws IOException,
       ServletException {
 
     /* Measure how long it takes to process this request. */
     long started = System.currentTimeMillis();
 
     /* Get print writer and start writing response. We're wrapping the
      * PrintWriter, because we want it to auto-flush as soon as we have
      * written a line. */
     PrintWriter out = new PrintWriter(response.getWriter(), true);
     writeHeader(out);
 
     /* If we don't have a database, see if we have consensus on disk. */
     SortedSet<File> consensusDirectories = new TreeSet<File>();
     if (conn == null) {
       /* Check if we have a consensuses directory. */
       File consensusDirectory = new File(CONSENSUS_DIRECTORY);
       if (consensusDirectory.exists() && consensusDirectory.isDirectory()) {
         for (File yearFile : consensusDirectory.listFiles()) {
           for (File monthFile : yearFile.listFiles()) {
             consensusDirectories.add(monthFile);
           }
         }
       }
       if (consensusDirectories.isEmpty()) {
         out.println("<p><font color=\"red\"><b>Warning: </b></font>This "
             + "server doesn't have any relay lists available. If this "
             + "problem persists, please "
             + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
             + "know</a>!</p>\n");
         writeFooter(out);
         return;
       }
     }
 
     /* Read search parameter, if any. */
     String searchParameter = request.getParameter("search");
     if (searchParameter == null) {
       searchParameter = "";
     }
 
     /* Write search form. */
     out.print("        <p>Search for a relay in the relay descriptor "
           + "archive by typing (part of) a <b>nickname</b>, "
           + "<b>fingerprint</b>, or <b>IP address</b> and optionally up "
           + "to three <b>months (yyyy-mm)</b> or <b>days "
           + "(yyyy-mm-dd)</b> in the following search field and "
           + "clicking Search. The search will stop after 30 hits or, "
           + "unless you provide a month or a day, after parsing the last "
           + "30 days of relay lists.</p><br>\n"
         + "        <form action=\"relay-search.html\">\n"
         + "          <table>\n"
         + "            <tr>\n"
         + "              <td><input type=\"text\" name=\"search\""
           + (searchParameter.length() > 0 ? " value=\"" + searchParameter
           + "\"" : "") + "></td>\n"
         + "              <td><input type=\"submit\" value=\"Search\">"
           + "</td>\n"
         + "            </tr>\n"
         + "          </table>\n"
         + "        </form>\n"
         + "        <br>\n");
 
     /* No search parameter? We're done here. */
     if (searchParameter.length() == 0) {
       writeFooter(out);
       return;
     }
 
     /* Parse search parameter to identify what nickname, fingerprint,
      * and/or IP address to search for. A valid query contains no more
      * than one identifier for each of the fields. As a special case,
      * there are search terms consisting of 8 to 19 hex characters that
      * can be either a nickname or a fingerprint. */
     String searchNickname = "";
     String searchFingerprint = "";
     String searchIPAddress = "";
     SortedSet<String> searchFingerprintOrNickname = new TreeSet<String>();
     SortedSet<String> searchDays = new TreeSet<String>();
     SortedSet<String> searchMonths = new TreeSet<String>();
     SortedSet<Long> searchDayTimestamps = new TreeSet<Long>();
     SortedSet<Long> searchMonthTimestamps = new TreeSet<Long>();
     boolean validQuery = false;
 
     /* Only parse search parameter if it contains nothing else than
      * alphanumeric characters, dots, and spaces. */
     if (alphaNumDotDashSpacePattern.matcher(searchParameter).matches()) {
       SortedSet<String> searchTerms = new TreeSet<String>();
       if (searchParameter.trim().contains(" ")) {
         String[] split = searchParameter.trim().split(" ");
         for (int i = 0; i < split.length; i++) {
           if (split[i].length() > 0) {
             searchTerms.add(split[i]);
           }
         }
       } else {
         searchTerms.add(searchParameter.trim());
       }
 
       /* Parse each search term separately. */
       for (String searchTerm : searchTerms) {
 
         /* If the search term contains a dot, it can only be an IP
          * address. */
         if (searchTerm.contains(".") && !searchTerm.startsWith(".")) {
           String[] octets = searchTerm.split("\\.");
           if (searchIPAddress.length() > 0 || octets.length < 2 ||
               octets.length > 4) {
             validQuery = false;
             break;
           }
           boolean invalidOctet = false;
           StringBuilder sb = new StringBuilder();
           for (int i = 0; i < octets.length; i++) {
             if (!numPattern.matcher(octets[i]).matches() ||
                 octets[i].length() > 3 ||
                 Integer.parseInt(octets[i]) > 255) {
               invalidOctet = true;
               break;
             } else {
               sb.append("." + Integer.parseInt(octets[i]));
             }
           }
           if (invalidOctet) {
             validQuery = false;
             break;
           }
           if (octets.length < 4) {
             sb.append(".");
           }
           searchIPAddress = sb.toString().substring(1);
         }
 
         /* If the search term contains hyphens, it must be a month or a
          * day. */
         else if (searchTerm.contains("-") &&
             searchTerm.startsWith("20")) {
           try {
             if (searchTerm.length() == 10) {
               searchDayTimestamps.add(dayFormat.parse(searchTerm).
                   getTime());
               searchDays.add(searchTerm);
             } else if (searchTerm.length() == 7) {
               searchMonthTimestamps.add(monthFormat.parse(searchTerm).
                   getTime());
               searchMonths.add(searchTerm);
             } else {
               validQuery = false;
               break;
             }
           } catch (ParseException e) {
             validQuery = false;
             break;
           }
         }
 
         /* If the search term contains between 8 and 19 hex characters, it
          * could be either a nickname or a fingerprint. */
         else if (searchTerm.length() >= 8 && searchTerm.length() <= 19 &&
             hexPattern.matcher(searchTerm).matches()) {
           searchFingerprintOrNickname.add(searchTerm);
         }
 
         /* If the search term contains between 20 and 40 hex characters,
          * it must be a fingerprint. */
         else if (searchTerm.length() >= 20 && searchTerm.length() <= 40 &&
             hexPattern.matcher(searchTerm).matches()) {
           if (searchFingerprint.length() > 0) {
             validQuery = false;
             break;
           }
           searchFingerprint = searchTerm;
         }
 
         /* If the search term contains up to 19 alphanumerical characters,
          * it must be a nickname. */
         else if (searchTerm.length() <= 19 &&
             alphaNumPattern.matcher(searchTerm).matches()) {
           if (searchNickname.length() > 0) {
             validQuery = false;
             break;
           }
           searchNickname = searchTerm;
         }
 
         /* We didn't recognize this search term. */
         else {
           validQuery = false;
           break;
         }
 
         /* We recognized at least one search term, so that the query is
          * potentially valid. */
         validQuery = true;
       }
     }
 
     /* We can only accept at most two search terms for nickname and
      * fingerprint. */
     int items = searchFingerprintOrNickname.size();
     items += searchFingerprint.length() > 0 ? 1 : 0;
     items += searchNickname.length() > 0 ? 1 : 0;
     if (items > 2) {
       validQuery = false;
     }
 
     /* If we have two candidates for fingerprint or nickname and we can
      * recognize what one of them is, then we can conclude what the other
      * one must be. */
     else if (items == 2 && searchFingerprintOrNickname.size() == 1) {
       if (searchFingerprint.length() == 0) {
         searchFingerprint = searchFingerprintOrNickname.first();
       } else {
         searchNickname = searchFingerprintOrNickname.first();
       }
       searchFingerprintOrNickname.clear();
     }
 
     /* We only accept at most three months or days, or people could
      * accidentally keep the database busy. */
     if (searchDays.size() + searchMonths.size() > 3) {
       validQuery = false;
     }
 
     /* If the query is invalid, print out a general warning. */
     if (!validQuery) {
       out.write("        <p>Sorry, I didn't understand your query. "
           + "Please provide a nickname (e.g., \"gabelmoo\"), at least "
           + "the first 8 hex characters of a fingerprint (e.g., "
           + "\"F2044413\"), or at least the first two octets of an IPv4 "
           + "address in dotted-decimal notation (e.g., \"80.190\"). You "
           + "can also provide at most three months or days in ISO 8601 "
           + "format (e.g., \"2010-09\" or \"2010-09-17\").</p>\n");
       writeFooter(out);
       return;
     }
 
     /* Print out what we're searching for. */
     List<String> recognizedSearchTerms = new ArrayList<String>();
     if (searchNickname.length() > 0) {
       recognizedSearchTerms.add("nickname <b>" + searchNickname + "</b>");
     }
     if (searchFingerprint.length() > 0) {
       recognizedSearchTerms.add("fingerprint <b>" + searchFingerprint
           + "</b>");
     }
     for (String searchTerm : searchFingerprintOrNickname) {
       recognizedSearchTerms.add("nickname or fingerprint <b>" + searchTerm
           + "</b>");
     }
     if (searchIPAddress.length() > 0) {
       recognizedSearchTerms.add("IP address <b>" + searchIPAddress
           + "</b>");
     }
     List<String> recognizedIntervals = new ArrayList<String>();
     for (String searchTerm : searchMonths) {
       recognizedIntervals.add("in <b>" + searchTerm + "</b>");
     }
     for (String searchTerm : searchDays) {
       recognizedIntervals.add("on <b>" + searchTerm + "</b>");
     }
     out.write("        <p>Searching for relays with ");
     if (recognizedSearchTerms.size() == 1) {
       out.write(recognizedSearchTerms.get(0));
     } else if (recognizedSearchTerms.size() == 2) {
       out.write(recognizedSearchTerms.get(0) + " and "
           + recognizedSearchTerms.get(1));
     } else {
       for (int i = 0; i < recognizedSearchTerms.size() - 1; i++) {
         out.write(recognizedSearchTerms.get(i) + ", ");
       }
       out.write("and " + recognizedSearchTerms.get(
           recognizedSearchTerms.size() - 1));
     }
     if (recognizedIntervals.size() == 1) {
       out.write(" running " + recognizedIntervals.get(0));
     } else if (recognizedIntervals.size() == 2) {
       out.write(" running " + recognizedIntervals.get(0) + " and/or "
           + recognizedIntervals.get(1));
     } else if (recognizedIntervals.size() > 2) {
       out.write(" running ");
       for (int i = 0; i < recognizedIntervals.size() - 1; i++) {
         out.write(recognizedIntervals.get(i) + ", ");
       }
       out.write("and/or " + recognizedIntervals.get(
           recognizedIntervals.size() - 1));
     }
     out.write(" ...</p>\n");
     out.flush();
 
     /* If we have a database connection, search relays in the database. */
     if (conn != null) {
 
       StringBuilder query = new StringBuilder("SELECT validafter, "
           + "rawdesc FROM statusentry WHERE ");
       boolean addAnd = false;
       if (searchDayTimestamps.size() > 0 ||
           searchMonthTimestamps.size() > 0) {
         boolean addOr = false;
         query.append("(");
         for (long searchTimestamp : searchDayTimestamps) {
           query.append((addOr ? "OR " : "") + "(validafter >= '"
               + dateTimeFormat.format(searchTimestamp) + "' AND "
               + "validafter < '" + dateTimeFormat.format(searchTimestamp
               + 24L * 60L * 60L * 1000L) + "') ");
           addOr = true;
         }
         for (long searchTimestamp : searchMonthTimestamps) {
           Calendar firstOfNextMonth = Calendar.getInstance(
               TimeZone.getTimeZone("UTC"));
           firstOfNextMonth.setTimeInMillis(searchTimestamp);
           firstOfNextMonth.add(Calendar.MONTH, 1);
           query.append((addOr ? "OR " : "") + "(validafter >= '"
               + dateTimeFormat.format(searchTimestamp) + "' AND "
               + "validafter < '" + dateTimeFormat.format(
               firstOfNextMonth.getTimeInMillis()) + "') ");
           addOr = true;
         }
         query.append(") ");
       } else {
         query.append("validafter >= '" + dateTimeFormat.format(
             started - 30L * 24L * 60L * 60L * 1000L) + "' ");
       }
       if (searchNickname.length() > 0) {
         query.append("AND LOWER(nickname) LIKE '"
             + searchNickname.toLowerCase() + "%' ");
       }
       if (searchFingerprint.length() > 0) {
         query.append("AND fingerprint LIKE '"
             + searchFingerprint.toLowerCase() + "%' ");
       }
       if (searchIPAddress.length() > 0) {
         query.append("AND address LIKE '" + searchIPAddress + "%' ");
       }
       for (String search : searchFingerprintOrNickname) {
         query.append("AND (LOWER(nickname) LIKE '" + search.toLowerCase()
             + "%' OR fingerprint LIKE '" + search.toLowerCase() + "%') ");
       }
       query.append("ORDER BY validafter DESC, fingerprint LIMIT 31");
       out.println("<!-- " + query.toString() + " -->");
       int matches = 0;
       long startedQuery = System.currentTimeMillis();
       try {
         Statement statement = conn.createStatement();
         ResultSet rs = statement.executeQuery(query.toString());
         String lastValidAfter = null;
         while (rs.next()) {
           matches++;
           if (matches > 30) {
             break;
           }
           String validAfter = rs.getTimestamp(1).toString().
               substring(0, 19);
           if (!validAfter.equals(lastValidAfter)) {
             out.println("        <br><tt>valid-after "
                 + "<a href=\"consensus?valid-after="
                 + validAfter.replaceAll(":", "-").replaceAll(" ", "-")
                 + "\" target=\"_blank\">" + validAfter + "</a></tt><br>");
             lastValidAfter = validAfter;
             out.flush();
           }
           byte[] rawStatusEntry = rs.getBytes(2);
           try {
             String statusEntryLines = new String(rawStatusEntry,
                 "US-ASCII");
             String[] lines = statusEntryLines.split("\n");
             for (String line : lines) {
               if (line.startsWith("r ")) {
                 String[] parts = line.split(" ");
                 String descriptor = String.format("%040x",
                     new BigInteger(1, Base64.decodeBase64(parts[3]
                     + "==")));
                 out.println("    <tt>r " + parts[1] + " " + parts[2] + " "
                     + "<a href=\"descriptor.html?desc-id=" + descriptor
                     + "\" target=\"_blank\">" + parts[3] + "</a> "
                     + parts[4] + " " + parts[5] + " " + parts[6] + " "
                     + parts[7] + " " + parts[8] + "</tt><br>");
               } else {
                 out.println("    <tt>" + line + "</tt><br>");
               }
             }
             out.println("    <br>");
             out.flush();
           } catch (UnsupportedEncodingException e) {
             /* This shouldn't happen, because we know that ASCII is
              * supported. */
           }
         }
         statement.close();
       } catch (SQLException e) {
         out.println("<p><font color=\"red\"><b>Warning: </b></font>We "
             + "experienced an unknown database problem while running the "
             + "search. The query was '" + query + "'. If this problem "
             + "persists, please "
             + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
             + "know</a>!</p>\n");
         writeFooter(out);
         return;
       }
 
       /* Display total search time on the results page. */
       long searchTime = System.currentTimeMillis() - started;
       long queryTime = System.currentTimeMillis() - startedQuery;
       out.write("        <br><p>Found " + (matches > 30 ? "more than 30"
           : "" + matches) + " relays " + (matches > 30 ?
           "(displaying only the first 30 hits) " : "") + "in "
           + String.format("%d.%03d", searchTime / 1000, searchTime % 1000)
           + " seconds.</p>\n");
       if (searchTime > 10L * 1000L) {
         out.write("        <p>In theory, search time should not exceed "
             + "10 seconds. The query was '" + query + "'. If this or "
             + "similar searches remain slow, please "
             + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
             + "know</a>!</p>\n");
       }
 
       /* Finish writing response. */
       writeFooter(out);
       return;
     }
 
     /* Compile a regular expression pattern to parse r lines more
      * quickly. */
     StringBuilder patternBuilder = new StringBuilder("r ");
     if (searchNickname.length() > 0 || searchFingerprint.length() > 0) {
       if (searchNickname.length() > 0) {
         patternBuilder.append(searchNickname);
       }
       if (searchFingerprint.length() > 0) {
         try {
           patternBuilder.append(".*" + Base64.encodeBase64String(
               Hex.decodeHex((searchFingerprint
               + (searchFingerprint.length() % 2 == 1 ? "0" : "")).
               toCharArray())).substring(0, searchFingerprint.length() *
               2 / 3));
         } catch (DecoderException e) {
           /* We make sure this exception is never thrown by passing an
            * even number of only hex characters to Hex.decodeHex(). */
         }
       }
     } else if (searchFingerprintOrNickname.size() > 0) {
       List<String> searchTermsCopy = new ArrayList<String>(
           searchFingerprintOrNickname);
       if (searchTermsCopy.size() < 2) {
         searchTermsCopy.add("");
       }
       patternBuilder.append("(");
       for (int i = 0; i < 2; i++) {
         patternBuilder.append(searchTermsCopy.get(i));
         String searchTerm = searchTermsCopy.get((i + 1) % 2);
         if (searchTerm.length() > 0) {
           try{
             patternBuilder.append(".*" + Base64.encodeBase64String(
                 Hex.decodeHex((searchTerm + (searchTerm.length()
                 % 2 == 1 ? "0" : "")).toCharArray())).substring(0,
                 searchTerm.length() * 2 / 3));
           } catch (DecoderException e) {
             /* We make sure this exception is never thrown by passing an
              * even number of only hex characters to Hex.decodeHex(). */
           }
         }
         if (i == 0) {
           patternBuilder.append("|");
         }
       }
       patternBuilder.append(")");
     }
     if (searchIPAddress.length() > 0) {
       patternBuilder.append(".* " + searchIPAddress.replaceAll("\\.",
           "\\\\."));
     }
     patternBuilder.append(".*");
     String pattern = patternBuilder.toString();
     Pattern searchPattern = Pattern.compile(pattern);
 
     /* While parsing, memorize the r lines of the last 24 parsed
      * consensuses, so that we don't have to parse them again. */
     Set<String> failedRLines = new HashSet<String>();
     List<Set<String>> addedFailedRLines = new ArrayList<Set<String>>();
 
     /* Parse consensus files from newest to oldest. Stop after either
      * parsing 31 * 24 consensuses, finding 30 hits, or running out of
      * consensuses. */
     SortedSet<File> consensusDirsToParse = new TreeSet<File>();
     consensusDirsToParse.addAll(consensusDirectories);
     SortedSet<File> consensusesToParse = new TreeSet<File>();
     int matches = 0, consensusesParsed = 0;
     while (consensusesParsed < 31 * 24 && matches < 30 &&
         !(consensusDirsToParse.isEmpty() &&
         consensusesToParse.isEmpty())) {
 
       /* Only put consensuses of one month in the queue at the same
        * time. */
       while (consensusesToParse.isEmpty() &&
           !consensusDirsToParse.isEmpty()) {
         Stack<File> parse = new Stack<File>();
         File dir = consensusDirsToParse.last();
         parse.add(dir);
         consensusDirsToParse.remove(dir);
         while (!parse.isEmpty()) {
           File pop = parse.remove(0);
           if (pop.isDirectory()) {
             for (File file : pop.listFiles()) {
               parse.add(file);
             }
           } else {
             consensusesToParse.add(pop);
           }
         }
       }
       if (consensusesToParse.isEmpty()) {
         break;
       }
 
       /* Parse consensus at the head of the queue. */
       File consensus = consensusesToParse.last();
       consensusesToParse.remove(consensus);
       BufferedReader br = new BufferedReader(new FileReader(consensus));
       String line = null, validAfterLine = null;
       Set<String> currentlyAddedFailedRLines = new HashSet<String>();
       addedFailedRLines.add(currentlyAddedFailedRLines);
       while ((line = br.readLine()) != null) {
         if (line.startsWith("r ")) {
 
           /* If we already know this r line doesn't match our regular
            * expression, ignore it. */
           if (failedRLines.contains(line)) {
 
           /* If we don't know this r line yet, but it doesn't match our
            * regular expression, memorize it. */
           } else if (!searchPattern.matcher(line).matches()) {
             currentlyAddedFailedRLines.add(line);
             failedRLines.add(line);
 
           /* If this r line matches our regular expression, compare fields
            * to be certain we want this relay. */
           } else {
             String[] parts = line.split(" ");
             String nickname = parts[1];
             String address = parts[6];
             if (searchNickname.length() > 0 &&
                 !nickname.startsWith(searchNickname)) {
               continue;
             }
             if (searchIPAddress.length() > 0 &&
                 !address.startsWith(searchIPAddress)) {
               continue;
             }
             String fingerprint = String.format("%040x", new BigInteger(1,
                 Base64.decodeBase64(parts[2] + "=="))).toLowerCase();
             if (searchFingerprint.length() > 0 && !fingerprint.startsWith(
                 searchFingerprint.toLowerCase())) {
               continue;
             }
             boolean skip = false;
             for (String searchTerm : searchFingerprintOrNickname) {
               if (!nickname.startsWith(searchTerm) &&
                   !fingerprint.startsWith(searchTerm.toLowerCase())) {
                 skip = true;
                 break;
               }
             }
             if (skip) {
               continue;
             }
 
             /* This r line matches the search criteria. If this is the
              * first match in this consensus, print the valid-after
              * line. */
             if (validAfterLine != null) {
               out.println("        <br><tt>valid-after "
                   + "<a href=\"consensus?valid-after="
                   + validAfterLine.substring("valid-after ".length()).
                   replaceAll(":", "-").replaceAll(" ", "-")
                   + "\" target=\"_blank\">"
                   + validAfterLine.substring("valid-after ".length())
                   + "</a></tt><br>");
               validAfterLine = null;
             }
 
             /* And print the r line. */
             String descriptor = String.format("%040x", new BigInteger(1,
                 Base64.decodeBase64(parts[3] + "==")));
             out.println("    <tt>r " + parts[1] + " " + parts[2] + " "
                 + "<a href=\"descriptor.html?desc-id=" + descriptor
                 + "\" " + "target=\"_blank\">" + parts[3] + "</a> "
                 + parts[4] + " " + parts[5] + " " + parts[6] + " "
                 + parts[7] + " " + parts[8] + "</tt><br>");
             matches++;
           }
         } else if (line.startsWith("valid-after ")) {
           validAfterLine = line;
         }
       }
       br.close();
       consensusesParsed++;
 
       /* Forget about failed r lines if they are 24 consensuses apart. */
       while (addedFailedRLines.size() >= 24) {
         Set<String> removeFailedRLines = addedFailedRLines.remove(0);
         failedRLines.removeAll(removeFailedRLines);
       }
     }
 
     /* Display total search time on the results page. */
     long searchTime = System.currentTimeMillis() - started;
     out.write("        <br><p>Found " + matches + " relays in the last "
         + consensusesParsed + " known consensuses in "
         + String.format("%d.%03d", searchTime / 1000, searchTime % 1000)
         + " seconds.</p>\n");
 
     /* Finish writing response. */
     writeFooter(out);
   }
 }
 
