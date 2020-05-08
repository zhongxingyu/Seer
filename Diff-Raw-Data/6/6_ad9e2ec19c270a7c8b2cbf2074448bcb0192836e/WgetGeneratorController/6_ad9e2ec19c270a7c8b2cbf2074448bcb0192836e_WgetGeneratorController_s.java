 
 /*****************************************************************************
  * Copyright  2011 , UT-Battelle, LLC All rights reserved
  *
  * OPEN SOURCE LICENSE
  *
  * Subject to the conditions of this License, UT-Battelle, LLC (the
  * Licensor) hereby grants to any person (the Licensee) obtaining a copy
  * of this software and associated documentation files (the "Software"), a
  * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
  * copy, modify, merge, publish, distribute, and/or sublicense copies of the
  * Software.
  *
  * 1. Redistributions of Software must retain the above open source license
  * grant, copyright and license notices, this list of conditions, and the
  * disclaimer listed below.  Changes or modifications to, or derivative works
  * of the Software must be noted with comments and the contributor and
  * organizations name.  If the Software is protected by a proprietary
  * trademark owned by Licensor or the Department of Energy, then derivative
  * works of the Software may not be distributed using the trademark without
  * the prior written approval of the trademark owner.
  *
  * 2. Neither the names of Licensor nor the Department of Energy may be used
  * to endorse or promote products derived from this Software without their
  * specific prior written permission.
  *
  * 3. The Software, with or without modification, must include the following
  * acknowledgment:
  *
  *    "This product includes software produced by UT-Battelle, LLC under
  *    Contract No. DE-AC05-00OR22725 with the Department of Energy.
  *
  * 4. Licensee is authorized to commercialize its derivative works of the
  * Software.  All derivative works of the Software must include paragraphs 1,
  * 2, and 3 above, and the DISCLAIMER below.
  *
  *
  * DISCLAIMER
  *
  * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
  * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
  * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
  * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
  * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
  * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
  * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
  * liabilities, penalties, fines, claims, causes of action, and costs and
  * expenses, caused by, resulting from or arising out of, in whole or in part
  * the use, storage or disposal of the SOFTWARE.
  *
  *
  ******************************************************************************/
 
 package org.esgf.filedownload;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.esgf.metadata.JSONException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 /**
  * Implementation of a controller that generates wget scripts on the fly.  
  * A request is sent from the front end to generate this script.
  * Contained in its query string are the following parameters:
  * - id name of the dataset
  * - array of the individual file names contained in the batch of files requested for download
  * - a 'create' or 'delete' query variable (in lieu of http PUT and DELETE)
  *
  *
  * @author john.harney
  *
  */
 @Controller
 @RequestMapping("/wgetproxy")
 public class WgetGeneratorController {
 
   
   private final static Logger LOG = Logger.getLogger(WgetGeneratorController.class);
 
   /**
    * 
    * @param request
    * @param response
    * @throws IOException
    * @throws JSONException
    * @throws ParserConfigurationException
    * @Deprecated
    */
   @RequestMapping(method=RequestMethod.GET)
   public @ResponseBody void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ParserConfigurationException {
     
     LOG.debug("doGet wgetproxy");
     createWGET(request, response);
     
   } //end doGet
 
   
   
   /** doPost(HttpServletRequest request, HttpServletResponse response) 
    * The wget file is primarily created through the post method
    * 
    * @param request 
    * @param response 
    * @throws IOException
    * @throws JSONException
    * @throws ParserConfigurationException
    */
   @RequestMapping(method=RequestMethod.POST)
   public @ResponseBody void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ParserConfigurationException {
     
     LOG.debug("doPost wgetproxy");
     
     //When the type parameter is "create", call the script generator method
     if(request.getParameter("type").equals("create")) {
       createWGET(request, response);
     }
     
   } //end doPost
 
   
   /** createWGET(HttpServletRequest request, HttpServletResponse response)
    * 
    * 
    * @param request passed from the doPost method
    * @param response x-sh response type 
    */
   private void createWGET(HttpServletRequest request, HttpServletResponse response) throws IOException, ParserConfigurationException {
     
     // Create the file name
     // The file name is the id of the dataset + ".sh"
     String filename = request.getParameter("id") + ".sh";
     String security = request.getParameter("security");
     
     LOG.debug("filename = " + filename);
     LOG.debug("security->" + security);
     queryStringInfo(request);
     
     // create content of the wget script
     String wgetText = "#!/bin/sh\n";
     
     if(security.equalsIgnoreCase("standard")) {
         wgetText += "# ESG Federation download script\n";
         wgetText += "#\n";
         wgetText += "# Template version: 0.2\n";
         wgetText += "# Generated by the all new ESGF Gateway\n";
         wgetText += "#";
         wgetText += "##############################################################################\n\n\n";
         wgetText += "download() {\n";
 
         //add the "child urls" to the wget script
         //the child_urls are the files that were selected in the datacart
         //probably need to change the name "child urls" to something more relevant
         for(int i=0;i<request.getParameterValues("child_url").length;i++) {
           LOG.debug("CHILD_URL: " + request.getParameterValues("child_url")[i]);
          wgetText += "\twget '" 
                   + " --certificate ~/.esg/credentials.pem --private-key ~/.esg/credentials.pem "
                   + request.getParameterValues("child_url")[i] + "'\n";
         }
         wgetText += "}\n";
         wgetText += "#\n# MAIN \n#\n";
         wgetText += "download\n";
 
         
     } 
     else {
         
 
         wgetText += "#\n";
         wgetText += "# Script generated user OpenID: ${downloadScriptData.user.openid}\n";
         wgetText += "#\n";
         wgetText += "<#-- TODO: Download and launch MyProxyLogon script -->\n";
         wgetText += "<#-- TODO: Report wget failures -->\n";
         wgetText += "#\n";
         wgetText += "<#if downloadScriptData.hasCertificateDownloads()>\n";
         wgetText += "##############################################################################\n";
         wgetText += "#\n";
         wgetText += "# Your download selection includes data secured using ESG\n";
         wgetText += "# certificate-based security.  In order to access the download URLs\n";
         wgetText += "# you must first obtain a credentials file from your home Gateway's\n";
         wgetText += "# MyProxy server.\n";
         wgetText += "#\n";
         wgetText += "# If you don't already have a myproxy client you can download the\n";
         wgetText += "# MyProxyLogon Java client from\n";
         wgetText += "#   ${downloadScriptData.gateway.baseURL}/webstart/myProxyLogon/MyProxyLogon-ESG.jar\n";
         wgetText += "#\n";
         wgetText += "# Then execute it as follows:\n";
         wgetText += "#  $ java -jar MyProxyLogon-ESG.jar -u <username>\\ \n";
         wgetText += "#         -h ${downloadScriptData.myProxyEndpoint.host}\\ \n";
         wgetText += "#         -p ${downloadScriptData.myProxyEndpoint.port?c}\\\n";
         wgetText += "#\n";
         wgetText += "# Further information is available at\n";
         wgetText += "#   ${downloadScriptData.gateway.baseURL}/help/download-help.htm\n";
         wgetText += "#\n";
         wgetText += "##############################################################################\n";
         wgetText += "\n";
         wgetText += "##############################################################################\n";
         wgetText += "#\n";
         wgetText += "# Script defaults\n";
         wgetText += "#\n";
         wgetText += "${r\n";
         wgetText += "# ESG_HOME should point to the directory containing ESG credentials.\n";
         wgetText += "#   Default is $HOME/.esg.\n";
         wgetText += "ESG_HOME=${ESG_HOME:-$HOME/.esg}\n";
         wgetText += "ESG_CREDENTIALS=${X509_USER_PROXY:-$ESG_HOME/credentials.pem}\n";
         wgetText += "ESG_CERT_DIR=${X509_CERT_DIR:-$ESG_HOME/certificates}\n";
         wgetText += "COOKIE_JAR=$ESG_HOME/cookies\n";
         wgetText += "CERT_EXPIRATION_WARNING=$((60 * 60 * 1))    #One hour (in seconds)\n";
         wgetText += "# Configure checking of server SSL certificates.\n";
         wgetText += "#   Disabling server certificate checking can resolve problems with myproxy\n";
         wgetText += "#   servers being out of sync with datanodes.\n";
         wgetText += "CHECK_SERVER_CERT=${CHECK_SERVER_CERT:-Yes}\n";
         wgetText += "\"}\n";
         wgetText += "\n";
         wgetText += "</#if>\n";
         wgetText += "usage() {\n";
         wgetText += "\techo \"Usage: $(basename $0) [flags]\"\n";
         wgetText += "\techo \"Flags is one of:\"\n";
         wgetText += "sed -n '/^while getopts/,/^done/  s/^\\([^)]*\\)[^#]*#\\(.*$\\)/\\1 - \\2/p' $0\n";
         wgetText += "}\n";
         wgetText += "#defaults\n";
         wgetText += "debug=0\n";
         wgetText += "clean_work=1\n";
         wgetText += "\n";
         wgetText += "#parse flags\n";
         wgetText += "<#if downloadScriptData.hasCertificateDownloads()>\n";
         wgetText += "while getopts ':c:pdq' OPT; do\n";
         wgetText += "<#else>\n";
         wgetText += "while getopts ':pdq' OPT; do\n";
         wgetText += "</#if>\n";
         wgetText += "    case $OPT in\n";
         wgetText += "<#if downloadScriptData.hasCertificateDownloads()>\n";
         wgetText += "        c) ESG_CREDENTIALS=\"$OPTARG\";;  #<cert> use this certificate for authentication.\n";
         wgetText += "</#if>\n";
         wgetText += "        p) clean_work=0;;       #   preserve data that failed checksum\n";
         wgetText += "        d) debug=1;;            #   display debug information\n";
         wgetText += "        q) quiet=1;;            #   be less verbose\n";
         wgetText += "        \\?) echo \"Unknown option '$OPTARG'\" >&2 && usage && exit 1;;\n";
         wgetText += "        \\:) echo \"Missing parameter for flag '$OPTARG'\" >&2 && usage && exit 1;;\n";
         wgetText += "    esac\n";
         wgetText += "done\n";
         wgetText += "shift $(($OPTIND - 1))\n";
         wgetText += "\n";
         wgetText += "##############################################################################\n";
         wgetText += "<#if downloadScriptData.hasCertificateDownloads()>\n";
         wgetText += "# Retrieve ESG credentials (not done yet)\n";
         wgetText += "get_credentials() {\n";
         wgetText += "    cat <<EOF\n";
         wgetText += "Your download selection includes data secured using ESG\n";
         wgetText += "certificate-based security.  In order to access the download URLs\n";
         wgetText += "you must first obtain a credentials file from your home Gateway's\n";
         wgetText += "MyProxy server at ${downloadScriptData.myProxyEndpoint.host}:${downloadScriptData.myProxyEndpoint.port?c}\n";
         wgetText += "\n";
         wgetText += "If you don't already have a myproxy client you can download the\n";
         wgetText += "MyProxyLogon Java client from\n";
         wgetText += "${downloadScriptData.gateway.baseURL}/webstart/myProxyLogon/MyProxyLogon-ESG.jar\n";
         wgetText += "Then execute it as follows:\n";
         wgetText += "$ java -jar MyProxyLogon-ESG.jar -u <username> -h ${downloadScriptData.myProxyEndpoint.host} -p ${downloadScriptData.myProxyEndpoint.port?c}\n";
         wgetText += "Further information is available at\n";
         wgetText += "  ${downloadScriptData.gateway.baseURL}/help/download-help.htm\n";
         wgetText += "EOF\n";
         wgetText += "    exit 1\n";
         wgetText += "}\n";
         wgetText += "\n";
         wgetText += "# check the certificate validity\n";
         wgetText += "check_cert() {\n";
         wgetText += "#chek openssl and certificate\n";
         wgetText += "    if (which openssl &>/dev/null); then\n";
         wgetText += "        if ! openssl x509 -checkend 0 -noout -in $ESG_CERT; then\n";
         wgetText += "            echo \"The Certificate has expired, please renew.\"\n";
         wgetText += "            return 1\n";
         wgetText += "        else\n";
         wgetText += "            if ! openssl x509 -checkend $CERT_EXPIRATION_WARNING -noout -in $ESG_CERT; then\n";
         wgetText += "                echo \"The certificate expires in less than $((CERT_EXPIRATION_WARNING / 60 / 60)) hour(s), please renew.\"\n";
         wgetText += "                return 2\n";
         wgetText += "            fi\n";
         wgetText += "        fi\n";
         wgetText += "    fi\n";
         wgetText += "}\n";
         wgetText += "\n";
         wgetText += "#\n";
         wgetText += "# Detect ESG credentials\n";
         wgetText += "#\n";
         wgetText += "find_credentials() {\n";
         wgetText += "\n";
         wgetText += "    if [[ -f \"$ESG_CREDENTIALS\" ]]; then\n";
         wgetText += "        # file found, proceed.\n";
         wgetText += "        ESG_CERT=\"$ESG_CREDENTIALS\"\n";
         wgetText += "        ESG_KEY=\"$ESG_CREDENTIALS\"\n";
         wgetText += "    elif [[ -f \"$X509_USER_CERT\" && -f \"$X509_USER_KEY\" ]]; then\n";
         wgetText += "        # second try, use these certificates.\n";
         wgetText += "        ESG_CERT=\"$X509_USER_CERT\"\n";
         wgetText += "        ESG_KEY=\"$X509_USER_KEY\"\n";
         wgetText += "    else\n";
         wgetText += "        # If credentials are not present exit\n";
         wgetText += "        echo \"No ESG Credentials found in $ESG_CREDENTIALS\" >&2\n";
         wgetText += "            get_credentials\n";
         wgetText += "    fi\n";
         wgetText += "\n";
         wgetText += "\n";
         wgetText += "#chek openssl and certificate\n";
         wgetText += "    if (which openssl &>/dev/null); then\n";
         wgetText += "        if ( openssl version | grep 'OpenSSL 1\\.0' ); then\n";
         wgetText += "\n";
         wgetText += "\n";
         wgetText += "            echo '** WARNING: ESGF Host certificate checking is not compatible with OpenSSL 1.0+'\n";
         wgetText += "            echo '**          ESGF Certificate directory is not being consulted'\n";
         wgetText += "            #Drop CA check, because it won't work as hashing of certs has change.\n";
         wgetText += "            unset CHECK_SERVER_CERT\n";
         wgetText += "        fi\n";
         wgetText += "        check_cert || { (($?==1)); exit 1; }\n";
         wgetText += "    fi\n";
         wgetText += "\n";
         wgetText += "    if [[ $CHECK_SERVER_CERT == \"Yes\" ]]; then\n";
         wgetText += "        [[ -d \"$ESG_CERT_DIR\" ]] || { echo \"CA certs not found. Aborting.\"; exit 1; }\n";
         wgetText += "        PKI_WGET_OPTS=\"--ca-directory=$ESG_CERT_DIR\"\n";
         wgetText += "    fi\n";
         wgetText += "\n";
         wgetText += "    PKI_WGET_OPTS=\"$PKI_WGET_OPTS --certificate=$ESG_CERT --private-key=$ESG_KEY --save-cookies=$COOKIE_JAR --load-cookies=$COOKIE_JAR\"\n";
         wgetText += "}\n";
         wgetText += "</#if>\n";
         wgetText += "\n";
         wgetText += "download() {\n";
         wgetText += "<#if downloadScriptData.hasCertificateDownloads()>\n";
         wgetText += "    wget=\"wget -c $PKI_WGET_OPTS\"\n";
         wgetText += "<#else>\n";
         wgetText += "    wget=\"wget -c\"\n";
         wgetText += "</#if>\n";
         wgetText += "    ((quiet)) && wget=\"$wget -q\"\n";
         wgetText += "    while read line\n";
         wgetText += "    do\n";
         wgetText += "        # read csv here document into proper variables\n";
         wgetText += "        eval $(awk -F \"' '\" '{$0=substr($0,2,length($0)-2); $3=tolower($3); print \"file=\\\"\"$1\"\\\";url=\\\"\"$2\"\\\";chksum_type=\\\"\"$3\"\\\";chksum=\\\"\"$4\"\\\"\"}' <(echo $line) )\n";
         wgetText += "        #Process the file\n";
         wgetText += "        echo -n \"$file ...\"\n";
         wgetText += "        [[ -f \"$file\" ]] && echo -n \"  continuing download ...\" || echo -n \"  starting downloading ...\"\n";
         wgetText += "        while : ; do\n";
         wgetText += "                # (if we had the file size, we could check before trying to complete)\n";
         wgetText += "                $wget -O \"$file\" $url || { failed=1; break; }\n";
         wgetText += "                #check if file is there\n";
         wgetText += "                if [[ -f $file ]]; then\n";
         wgetText += "                        ((debug)) && echo file found\n";
         wgetText += "                        if ! check_chksum \"$file\" $chksum_type $chksum; then\n";
         wgetText += "                                echo \"  $chksum_type failed!\"\n";
         wgetText += "                                if ((clean_work)); then\n";
         wgetText += "                                        rm $file\n";
         wgetText += "                                        #try again\n";
         wgetText += "                                        echo -n \"  re-downloading...\"\n";
         wgetText += "                                        continue\n";
         wgetText += "                                else\n";
         wgetText += "                                        echo \"  don't use -p or remove manually.\"\n";
         wgetText += "                                fi\n";
         wgetText += "                        else\n";
         wgetText += "                                echo \"  $chksum_type ok. done!\"\n";
         wgetText += "                        fi\n";
         wgetText += "                fi\n";
         wgetText += "                #done!\n";
         wgetText += "                break\n";
         wgetText += "        done\n";
         wgetText += "        if ((failed)); then\n";
         wgetText += "            echo \"download failed\"\n";
         wgetText += "<#if downloadScriptData.hasCertificateDownloads()>\n";
         wgetText += "            # most common failure is certificate expiration, so check this\n";
         wgetText += "            check_cert\n";
         wgetText += "</#if>\n";
         wgetText += "            unset failed\n";
         wgetText += "        fi\n";
         wgetText += "    done <<EOF--dataset.file.url.chksum_type.chksum\n";
         wgetText += "<#list downloadScriptData.retrievableFiles as fileDownload>\n";
         wgetText += "'${fileDownload.fileAccessPoint.logicalFile.name}' '${fileDownload.downloadURI}' '${(fileDownload.fileAccessPoint.logicalFile.checksums[0].algorithm)!\"\"}' '${(fileDownload.fileAccessPoint.logicalFile.checksums[0].checksum)!\"\"}'\n";
         wgetText += "</#list>\n";
         wgetText += "EOF--dataset.file.url.chksum_type.chksum\n";
         wgetText += "\n";
         wgetText += "}\n";
         wgetText += "\n";
         wgetText += "#\n";
         wgetText += "# MAIN\n";
         wgetText += "#\n";
         wgetText += "echo \"Running $(basename $0) version: $version\"\n";
         wgetText += "<#if downloadScriptData.hasCertificateDownloads()>\n";
         wgetText += "find_credentials\n";
         wgetText += "</#if>\n";
         wgetText += "download\n";
         
         /*
 
 
         */
 
     }
     		
     
     //attach the sh file extension to the response
     response.setContentType("text/x-sh");
     response.addHeader("Content-Disposition", "attachment; filename=" + filename );
     response.setContentLength((int) wgetText.length());
    
     PrintWriter out = response.getWriter();
     out.print(wgetText);
 
     LOG.debug("Finishing writing wget stream");
 
   } //end createWGET
 
 
   /**
    * queryStringInfo(HttpServletRequest request)
    * Private method printing out the contents of the request.  Used mainly for debugging.
    * 
    * @param request
    */
   private void queryStringInfo(HttpServletRequest request) {
     
     LOG.debug("Query parameters");
     LOG.debug("\tId");
     LOG.debug("\t\t" + request.getParameterValues("id")[0]);
     LOG.debug("\tType");
     LOG.debug("\t\t" + request.getParameterValues("type")[0]);
     LOG.debug("\tChild urls");
     for(int i=0;i<request.getParameterValues("child_url").length;i++) {
       LOG.debug("\t\t" + request.getParameterValues("child_url")[i]);
     }
     LOG.debug("\tChild ids");
     for(int i=0;i<request.getParameterValues("child_id").length;i++) {
       LOG.debug("\t\t" + request.getParameterValues("child_id")[i]);
     }
     
   } //end queryStringInfo
 
   
   
 } //end servlet class
