 package cs13.handlers;
 
 import com.google.common.collect.ImmutableMap;
 import groovy.util.Eval;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.webbitserver.HttpControl;
 import org.webbitserver.HttpHandler;
 import org.webbitserver.HttpRequest;
 import org.webbitserver.HttpResponse;
 
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class BasicQuestionHandler implements HttpHandler {
     public static final class ErrorMessages {
         public static final String NO_QUESTION = "Vous avez pas oublie la question ? Pas moyen d'y repondre...";
         public static final String BAD_QUESTION = "Je ne me prononce pas pour le moment, mais je vais y travailler";
     }
 
     private final Logger logger = LoggerFactory.getLogger("QUESTION");
 
     private static final Map<String, String> BASIC_QUESTIONS = new ImmutableMap.Builder<String, String>()
             .put("Quelle est ton adresse email", "xavier.hanin@gmail.com")
             .put("Es tu abonne a la mailing list(OUI/NON)", "OUI")
             .put("Es tu heureux de participer(OUI/NON)", "OUI")
             .put("Est ce que tu reponds toujours oui(OUI/NON)", "NON")
             .put("Es tu pret a recevoir une enonce au format markdown par http post(OUI/NON)", "OUI")
             .put("As tu bien recu le premier enonce(OUI/NON)", "OUI")
            .put("As tu passe une bonne nuit malgre les bugs de l etape precedente(PAS_TOP/BOF/QUELS_BUGS)", "PAS_TOP")
             .build();
 
     public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl httpControl) throws Exception {
         String q = request.queryParam("q");
         if (q == null) {
             respondError(response, 400, ErrorMessages.NO_QUESTION);
             return;
         }
 
         String r = BASIC_QUESTIONS.get(q);
         if (r == null) {
             // let's see if it's an expression
             // but first it seems it was not url encoded, so + get converted to spaces.
             // And we want point as decimal separator...
             q = q.replace(' ', '+').replace(',', '.');
             try {
                 r = String.valueOf(Eval.me(q)).replace('.', ',');
                 if (Pattern.compile("\\d+,0+").matcher(r).matches()) {
                     r = r.substring(0, r.indexOf(','));
                 }
             } catch (Exception e) {
                 respondError(response, 412, ErrorMessages.BAD_QUESTION);
                 return;
             }
         }
         logger.info("{} => {}", q, r);
         response.content(r).end();
     }
 
     private boolean isInt(double v) {
         return (v == Math.floor(v)) && !Double.isInfinite(v);
     }
 
     private HttpResponse respondError(HttpResponse response, int status, String r) {
         logger.info("{} : {}", status, r);
         return response.status(status).content(r).end();
     }
 }
