 /*
  * Copyright (C) 2013 TestUrSelf Project 
  *
  * Guillaume BALAS
  *
  * Licensed under the Creative Commons BY-NC-ND.
  * You may obtain a copy of the License at  * 
  *
  *      http://creativecommons.org/licenses/by-nc-nd/3.0/
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 /*
  * 
  * 
 * 
  * All my apologise for this mess.
  * I lost the orignal code. In order to retreive my own work, 
  * I had to do a reverse-engineering job from a class file.
  * 
  * 
  */
 
 
 package fr.supinfo.testurselfserver.dao;
 
 import fr.supinfo.testurselfserver.entity.Question;
 import fr.supinfo.testurselfserver.entity.Quizz;
 import fr.supinfo.testurselfserver.entity.Response;
 import fr.supinfo.testurselfserver.tools.MyTools;
 import fr.supinfo.testurselfserver.entity.Package;
 import fr.supinfo.testurselfserver.entity.PackageInfo;
 import fr.supinfo.testurselfserver.listener.StartScheduler;
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PackageDao {
 
     private Connection connection;
     private static String QUERY_LIST_QUIZZ;
     private static String QUERY_QUESTION_ID;
     private static String QUERY_GET_QUESTION;
     private static String QUERY_ANSWER;
     private Logger log;
 
     public PackageDao(Connection connection) {
         log = LoggerFactory.getLogger(PackageDao.class);
         this.connection = connection;
     }
 
     @SuppressWarnings("CallToThreadDumpStack")
     public ArrayList getAllPackage() {
        Package.listPackage.clear();
        PackageInfo.packageInfoList.clear();
        Quizz.quizzList.clear();
 
 
         try {
 
             /*
              *
              * For all the courses
              *
              */
             for (ResultSet result = connection.createStatement().executeQuery("SELECT course.code code, course.title title, course.tutor_name tutor, course.cou"
                     + "rse_language language, course.creation_date create_date, course.department_name department FROM dokeos_main.course_"
                     + "rel_user course_rel_user LEFT JOIN dokeos_main.course course ON course.code = co"
                     + "urse_rel_user.course_code LEFT JOIN dokeos_user.user_course_category user_course"
                     + "_category ON course_rel_user.user_course_cat = user_course_category.id WHERE cou"
                     + "rse_rel_user.user_id =1 LIMIT 0 , 30;"); result.next();) {
 
                 String codeCourse = result.getObject(1).toString();
 
 
                 int versionCourse;
                 try {
                     Object ojectVersion = result.getObject(6);
 
                     if (ojectVersion == null) {
                         versionCourse = 0;
                     } else {
                         String stringVersion = ojectVersion.toString();
                         System.out.println("HEY YOU : " + stringVersion + result.getObject(5).toString());
                         versionCourse = Integer.parseInt(stringVersion);
                     }
 
                 } catch (Exception e) {
                     e.printStackTrace();
                     versionCourse = 0;
                 }
 
                 log.debug("Code Course : ", codeCourse);
                 log.debug("Version : ", versionCourse);
 
                 Package currentPackage = new Package();
                 currentPackage.setCourse_code(codeCourse);
                 currentPackage.setVersion(versionCourse);
 
                 QUERY_LIST_QUIZZ = (new StringBuilder("SELECT id, title TYPE , active, description FROM  `dokeos_")).append(codeCourse).append("`.`quiz` q WHERE active='1' ORDER BY position LIMIT 0 , 51").toString();
 
 
                 /*
                  *
                  * For all the Quizz
                  *
                  */
                 for (ResultSet resultQuizz = connection.createStatement().executeQuery(QUERY_LIST_QUIZZ); resultQuizz.next();) {
 
                     QUERY_QUESTION_ID = (new StringBuilder("SELECT question.id FROM  `dokeos_")).append(result.getObject(1).toString()).append("`.`quiz` quiz,  `dokeos_").append(result.getObject(1).toString()).append("`.`quiz_question` question,  `dokeos_").append(result.getObject(1).toString()).append("`.`quiz_rel_question` rel_question WHERE quiz.id = rel_question.exercice_id AND "
                             + "rel_question.question_id = question.id AND quiz.id =").append(resultQuizz.getObject(1).toString()).append(" ORDER BY rel_question.question_order LIMIT 0 , 30").toString();
                     System.out.println(QUERY_QUESTION_ID);
                     ResultSet resultQuestion = connection.createStatement().executeQuery(QUERY_QUESTION_ID);
 
                     currentPackage.setCourse_code(result.getObject(1).toString());
                     currentPackage.setCourse_title(MyTools.html2text(result.getObject(2).toString()));
                     currentPackage.setTutor_name(result.getObject(3).toString());
                     currentPackage.setLanguage(result.getObject(4).toString());
                     currentPackage.setUrl_dl("courses/" + currentPackage.getCourse_code() + ".zip");
 
                     //currentQuizz.setCreation_date(result.getObject(5).toString());
                     Quizz currentQuizz = new Quizz();
                     currentQuizz.setQuizzName(resultQuizz.getString(2).toString());
 
                     /*
                      *
                      * For all the question
                      *
                      */
                     while (resultQuestion.next()) {
                         List list_question_id = new ArrayList();
                         list_question_id.add(Integer.valueOf(((Integer) resultQuestion.getObject(1)).intValue()));
                         QUERY_GET_QUESTION = (new StringBuilder("SELECT question,description,picture FROM `dokeos_")).append(codeCourse).append("`.`quiz_question` WHERE id='").append(((Integer) resultQuestion.getObject(1)).intValue()).append("' order by position").toString();
                         ResultSet resultQuestionById = connection.createStatement().executeQuery(QUERY_GET_QUESTION);
                         System.out.println(QUERY_GET_QUESTION);
                         if (resultQuestionById.next()) {
                             String questionString = MyTools.html2text(resultQuestionById.getString(1));
                             String htmlMedia = resultQuestionById.getString(2);
 
                             log.info(questionString);
                             log.info(resultQuestionById.getString(2));
 
                             QUERY_ANSWER = (new StringBuilder("SELECT id,answer,correct,comment FROM `dokeos_")).append(codeCourse).append("`.`quiz_answer` WHERE question_id ='").append(((Integer) resultQuestion.getObject(1)).intValue()).append("' ORDER BY position").toString();
                             ResultSet resultAnswer = connection.createStatement().executeQuery(QUERY_ANSWER);
                             List listResponse = new ArrayList();
 
                             for (; resultAnswer.next(); listResponse.add(new Response(Boolean.valueOf(resultAnswer.getBoolean(3)), MyTools.html2text(resultAnswer.getString(2)), MyTools.html2text(resultAnswer.getString(4))))) {
                                 log.debug(resultAnswer.getString(2));
                             }
 
                             Question q = new Question(questionString);
                             q.setResponseList(listResponse);
                             q.setHtmlMedia(htmlMedia);
                             currentQuizz.addQuestion(q);
 
 
                         }
                     }
                     currentQuizz.setNombreQuestions(currentQuizz.getQuestionList().size());
                     Quizz.quizzList.add(currentQuizz);
                     currentPackage.getListQuizz().add(currentQuizz);
                 }
             }
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public void savePackageToZip(Package pa) throws IOException {
 
         ObjectMapper mapper = new ObjectMapper();
         ArrayList<File> myFiles = new ArrayList<>();
         File myFile = null;
 
         //create json file
         for (Quizz q : pa.getListQuizz()) {
             myFile = new File(StartScheduler.path + q.getQuizzName() + ".json");
             myFiles.add(myFile);
             mapper.writeValue(myFile, q);
         }
 
         //zip the json
         MyTools.writeZip(StartScheduler.path + pa.getCourse_code() + ".zip", myFiles);
 
         //delete json file
         for (File file : myFiles) {
             file.delete();
         }
 
     }
 }
