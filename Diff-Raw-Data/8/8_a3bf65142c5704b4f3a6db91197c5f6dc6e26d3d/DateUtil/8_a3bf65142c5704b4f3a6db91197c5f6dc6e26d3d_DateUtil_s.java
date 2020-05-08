 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.common;
 
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateUtils;
 
 /**
  * This is a static utility class to handle Date.
  */
 public final class DateUtil {
    private static final SimpleDateFormat TIMESTAMP_FORMAT = 
        new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
     private static final String TIMESTAMP_UNAVAILABLE_STRING = "Unavailable";
     private static final Long MILLISECONDS_PER_SECOND = 1000L;
     private static final Long SECONDS_PER_MINUTE = 60L;
     private static DecimalFormat twoDigit = new DecimalFormat("00");
     private static final int TWELVE_HOURS = 12;
     
     private DateUtil() {
         
     }
 
     /**
      * Check for timeout based on the date against current time.
      * @param date the date to check for timeout
      * @return boolean
      */
     public static boolean isTimeout(Date date) {
         return DateUtils.addHours(date, TWELVE_HOURS).before(new Date());
     }
     
     /**
      * @param dateString string represent of Date
      * @return Date object
      * @throws ParseException parsing exception
      */
     public static Date createDate(String dateString) throws ParseException {
         if (StringUtils.isBlank(dateString)) {
             return null;
         }
         SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
         return (Date) formatter.parse(formatDate(dateString));
     }
     
     private static String formatDate(String dateString) throws ParseException {
         String[] dateElements = (dateString.contains("-"))
             ? dateString.split("-", 3) : dateString.split("/", 3);
         if (dateElements.length != 3) {
             throw new ParseException("Invalid date string: " + dateString, 0);
         }
         return twoDigit.format(Long.valueOf(dateElements[0])) + "/"
             + twoDigit.format(Long.valueOf(dateElements[1])) + "/"
             + dateElements[2];
     }
     
     /**
      * @param date the Date object
      * @return string date in "MM/dd/yyyy" format
      */
     public static String toString(Date date) {
         if (date == null) {
             return "";
         }
         SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
         return formatter.format(date);
     }
     
     /**
      * @param dates the list of dates to convert to our display format.
      * @return dates in "MM/dd/yyyy" format
      * @throws ParseException date parsing exception
      */
     public static List<String> toString(List<String> dates) throws ParseException {
         List<String> resultDates = new ArrayList<String>();
         for (String date : dates) {
             resultDates.add(toString(createDate(date)));
         }
         return resultDates;
     }
     
     /**
      * Compare the toString of the 2 dates.
      * 
      * @param date1 first date
      * @param date2 second date
      * @return boolean of string comparison
      */
     public static boolean equal(Date date1, Date date2) {
         return DateUtil.toString(date1).equalsIgnoreCase(DateUtil.toString(date2));
     }
 
     /**
      * @param date the Date object
      * @return string date in "yyyy/MM/dd" format
      */
     public static String toStringForComparison(Date date) {
         if (date == null) {
             return "";
         }
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
         return formatter.format(date);
     }
     
     /**
      * Returns the difference between two times in Minutes apart.
      * @param date1 first date.
      * @param date2 second date.
      * @return difference between the dates.
      */
     public static Long compareDatesInMinutes(Date date1, Date date2) {
         return Math.abs(date1.getTime() - date2.getTime()) / MILLISECONDS_PER_SECOND / SECONDS_PER_MINUTE;
     }
     
     /**
      * Used for all of the timestamps in caIntegrator2.
      * @param timeStamp to format to a displayable string.
      * @return displayable timestamp string.
      */
     public static String getDisplayableTimeStamp(Date timeStamp) {
         return timeStamp == null ? TIMESTAMP_UNAVAILABLE_STRING 
                : TIMESTAMP_FORMAT.format(timeStamp);
     }
 }
