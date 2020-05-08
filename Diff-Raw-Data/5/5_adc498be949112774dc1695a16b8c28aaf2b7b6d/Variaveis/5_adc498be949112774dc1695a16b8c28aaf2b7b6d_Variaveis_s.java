 package controllers;
 
 import java.util.*;
 import play.mvc.*;
 import play.data.*;
 import static play.data.Form.*;
 import play.*;
 import views.html.variavel.*;
 import javax.persistence.PersistenceException;
 import play.libs.Json;
<<<<<<< HEAD
import play.libs.Json.*;
 import com.avaje.ebean.*;
 import static play.libs.Json.toJson;
=======
>>>>>>> ac62a2d54cddcd0330d06528ba923790224fa2b9
 
 import models.*;
 
 public class Variaveis extends Controller {
     
     public static Result GO_HOME = redirect(routes.Variaveis.manter(0, "nome", "asc", ""));
     
     public static Result index() {
         return GO_HOME;
     }
     
     public static Result manter(int page, String sortBy, String order, String filter) {
             return ok(
             manter.render(
                 Variavel.page(page, 10, sortBy, order, filter),sortBy, order, filter)
             );
     }
     
     public static Result novaEditar(Long id, int quemChama) {
         Form<Variavel> variavelForm;
         if(id==0){
                 variavelForm = form(Variavel.class);
         }else{
                 variavelForm = form(Variavel.class).fill(Variavel.find.byId(id));
         }
         
         return ok(
             novoEditar.render(id, variavelForm, quemChama)
         );
     }
 
     public static Result salvar(Long id, int quemChama) {
         Form<Variavel> variavelForm = form(Variavel.class).bindFromRequest();
         if(variavelForm.hasErrors()) {
             return badRequest(novoEditar.render(id, variavelForm, quemChama));
         }
         if(quemChama==2 && id!=0){
             flash("success", "Variável " + variavelForm.get().nome + " foi editada com sucesso");
             variavelForm.get().update(id);
         }else if(quemChama==0){
             flash("success", "Variável " + variavelForm.get().nome + " foi incluida com sucesso");
             variavelForm.get().save();
         }else{
             variavelForm.get().save();
         }
         
         return ok(Json.toJson(variavelForm.get()));
     }
     
     public static Result deletar(Long id) {
         try{
             Variavel.find.ref(id).delete();
             flash("success", "Variavel excluída com sucesso");
             return GO_HOME;
         }catch(PersistenceException exception){
             flash("error", "Exclusão não permitida. Existem equações vinculadas a variavel");
         return GO_HOME;
         }   
     }
 
     public static Result findByLocal(Long id) {
          
         String sql =" SELECT " 
                       +"variavel.id," 
                       +"variavel.sigla," 
                       +"variavel.nome "
                     +" FROM "
                       +"public.variavel," 
                       +"public.local, "
                       +"public.trabalho_cientifico, "
                       +"public.trabalho_cientifico_equacao," 
                       +"public.trabalho_cientifico_modelo," 
                       +"public.modelo," 
                       +"public.equacao," 
                       +"public.modelo_variavel," 
                       +"public.equacao_variavel"
                     +" WHERE "
                       +"local.trabalho_cientifico_id = trabalho_cientifico.id AND " 
                       +"trabalho_cientifico.id = trabalho_cientifico_equacao.trabalho_cientifico_id AND "
                       +"trabalho_cientifico.id = trabalho_cientifico_modelo.trabalho_cientifico_id AND "
                       +"trabalho_cientifico_equacao.equacao_id = equacao.id AND "
                       +"trabalho_cientifico_modelo.modelo_id = modelo.id AND "
                       +"modelo.id = modelo_variavel.modelo_id AND "
                       +"equacao.id = equacao_variavel.equacao_id AND "
                       +"modelo_variavel.variavel_id = variavel.id AND "
                       +"equacao_variavel.variavel_id = variavel.id AND "
                       +"local.id = '" + id + "'";
          SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
         return ok(Json.toJson(sqlQuery.findList()));
 }
 }
