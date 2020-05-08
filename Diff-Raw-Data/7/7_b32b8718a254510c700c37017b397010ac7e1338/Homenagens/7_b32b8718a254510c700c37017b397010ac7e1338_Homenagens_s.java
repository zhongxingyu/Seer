 package controllers;
 
 import static play.data.Form.form;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import models.Cidade;
 import models.Estado;
 import models.Homenageado;
 import models.Homenagem;
 import models.HomenagemFilter;
 import models.HomenagemImagem;
 import models.RawImage;
 import models.Usuario;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import play.Configuration;
 import play.Logger;
 import play.Play;
 import play.data.Form;
 import play.i18n.Messages;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Results;
 import play.mvc.Security;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import views.html.homenagem.createForm;
 import views.html.homenagem.editForm;
 import views.html.homenagem.list;
 
 @Security.Authenticated(Secured.class)
 public class Homenagens extends Controller {
     
 	/**
      * This result directly redirect to application home.
      */
     private static Result GO_HOME = redirect(
         routes.Homenagens.list(0, "descricao", "asc", Homenageado.MARCONI.name(), "", "", "", "", "", "")
     );
     
     private static Result goHome(Homenageado homenageado) {
     	if(homenageado != null) {
     		return redirect(routes.Homenagens.list(0, "descricao", "asc", homenageado.name(), "", "", "", "", "", ""));
     	} else {
     		return GO_HOME;
     	}
     }
     
     /**
      * Handle default path requests, redirect to homenagems list
      */
     public static Result index() {
         return goHome(null);
     }
     
     public static Result listarEstados(Long pais) {
         List<Estado> estados = Estado.listarPorPais(pais);
 
         return ok(Json.toJson(estados));
     }
     
     public static Result listarCidades(Long estado) {
         List<Cidade> cidades = Cidade.listarPorEstado(estado);
 
         return ok(Json.toJson(cidades));
     }
     
     public static Result list(int page, String sortBy, String order, String homenageado, String numeroRegistro, String descricao, String resumo, String quemEntregou, String dataRecebimentoInicio, String dataRecebimentoFim) {
     	Form<HomenagemFilter> homenagemFilterForm = form(HomenagemFilter.class).bindFromRequest();
         if(homenagemFilterForm.hasErrors()) {
             return badRequest(list.render(Usuario.consultarPorEmail(request().username()), 
             		Homenagem.page(page, 10, sortBy, order, homenagemFilterForm.get()),
                     sortBy, order, homenagemFilterForm
                 ));
         }
         return ok(
             list.render(Usuario.consultarPorEmail(request().username()), 
             		Homenagem.page(page, 10, sortBy, order, homenagemFilterForm.get()),
                 sortBy, order, homenagemFilterForm
             )
         );
     }
     
     /**
      * Display the 'edit form' of a existing Homenagem.
      *
      * @param id Id of the homenagem to edit
      */
     public static Result edit(Long id) {
     	Homenagem homenagem = Homenagem.find.byId(id);
         Form<Homenagem> homenagemForm = form(Homenagem.class).fill(
         		homenagem
         );
         return ok(
             editForm.render(Usuario.consultarPorEmail(request().username()), id, homenagemForm)
         );
     }
     
     /**
      * Handle the 'edit form' submission 
      *
      * @param id Id of the homenagem to edit
      */
     public static Result update(Long id) {
     	Form<Homenagem> homenagemForm = form(Homenagem.class).bindFromRequest();
         if(!validateForm(homenagemForm)) {
             return badRequest(createForm.render(Usuario.consultarPorEmail(request().username()), homenagemForm));
         }
         homenagemForm.get().update(id);
         flash("success", "A Homenagem \"" + homenagemForm.get().numeroRegistro + "\" foi atualizada");
         return goHome(homenagemForm.get() != null ? homenagemForm.get().homenageado : null);
     }
     
     /**
      * Display the 'new homenagem form'.
      */
     public static Result create() {
         Form<Homenagem> homenagemForm = form(Homenagem.class);
         return ok(
             createForm.render(Usuario.consultarPorEmail(request().username()), homenagemForm)
         );
     }
     
     /**
      * Handle the 'new homenagem form' submission 
      */
     public static Result save() {
     	Form<Homenagem> homenagemForm = form(Homenagem.class).bindFromRequest();
         if(!validateForm(homenagemForm)) {
             return badRequest(createForm.render(Usuario.consultarPorEmail(request().username()), homenagemForm));
         }
         homenagemForm.get().save();
         flash("success", Messages.get("homenagem.create.success", homenagemForm.get().numeroRegistro));
         return goHome(homenagemForm.get() != null ? homenagemForm.get().homenageado : null);
     }
     
     /**
      * Validate the form
      * @param homenagemForm
      * @return boolean
      */
     private static boolean validateForm(Form<Homenagem> homenagemForm) {
     	
     	boolean isValid = true; 
     	
     	String tipoHomenagem = form().bindFromRequest().get("tipoHomenagem.id");
     	
     	if(tipoHomenagem.equals("")) {
     		homenagemForm.reject("tipoHomenagem.id", "");
     		isValid = false;
     	}
     	
     	if(homenagemForm.hasErrors() || !isValid) {
     		homenagemForm.reject(Messages.get("formErrorMessage"));
     	}
     	
     	return isValid;
     }
     
     /**
      * Handle homenagem deletion
      */
     public static Result delete(Long id) {
     	Homenagem homenagem = Homenagem.find.ref(id);
     	Homenageado homenageado = homenagem.homenageado;
     	homenagem.delete();
         flash("success", "A Homenagem \"" + homenagem.numeroRegistro + "\" foi excluída");
         return goHome(homenageado);
     }
 
     public static Result print(String sortBy, String order, String homenageado, String numeroRegistro, String descricao, String resumo, String quemEntregou, String dataRecebimentoInicio, String dataRecebimentoFim) {
     	Form<HomenagemFilter> homenagemFilterForm = form(HomenagemFilter.class).bindFromRequest();
     	List<Homenagem> homenagemList = Homenagem.list(sortBy, order, homenagemFilterForm.get());
     	
     	Map<String, Object> reportParams = new HashMap<String, Object>();
     	
     	Homenageado homenageadoObj = homenagemFilterForm.get().homenageado;
     	if(homenageadoObj == null) {
     		homenageadoObj = Homenageado.valueOf(homenageado);
     	}
     	reportParams.put("REPORT_TITLE", "Homenagens - "+ homenageadoObj.getNomeComTratamento());
     	
         return ReportController.jasperDocument("homenagem_list", reportParams, new
     			JRBeanCollectionDataSource(homenagemList));
     }
     
     public static Result printSingle(Long id, String tipo) {
     	List<Homenagem> homenagemList = new ArrayList<Homenagem>();
     	Homenagem homenagem = Homenagem.find.byId(id);
     	homenagemList.add(homenagem);
     	
     	Map<String, Object> reportParams = new HashMap<String, Object>();
     	
     	reportParams.put("REPORT_TITLE", "Homenagem - "+ homenagem.homenageado.getNomeComTratamento());
     	
         return ReportController.jasperDocument(tipo, reportParams, new
     			JRBeanCollectionDataSource(homenagemList));
     }
     
     
     public static Result getAllImages(Long homenagemId) {
 		ObjectNode result = Json.newObject();
 		ArrayNode array = result.putArray("files");
 		
 		Homenagem homenagem = Homenagem.find.byId(homenagemId);
 
 		List<HomenagemImagem> bdImages = homenagem.imagens;
 		for (HomenagemImagem image : bdImages) {
 			array.add(dbImageToJson(image));
 		}
 
 		return Results.ok(result);
 	}
     
     /**
 	 * @return json with all infos of the newly created or updated images
 	 */
 	public static Result uploadImage(Long homenagemId) {
 		ObjectNode result = Json.newObject();
 		ArrayNode array = result.putArray("files");
 
 		try {
 			MultipartFormData body = Controller.request().body().asMultipartFormData();
 
 			List<FilePart> fileParts = body.getFiles();
 
 			for (FilePart filePart : fileParts) {
 				HomenagemImagem image = new HomenagemImagem();
 				image.homenagem = Homenagem.find.byId(homenagemId);
 				// image.setId(id);
 				image.setFilename(filePart.getFilename());
 				File imgFile = filePart.getFile();
 				byte[] bytes = FileUtils.readFileToByteArray(imgFile);
 				setRawImage(image, bytes);
 				if (!isAllowedMimetype(image)) {
 					return Results.status(400, "mimetype not allowed");
 				}
 				setRawThumbnail(image);
 				image.save();
 				array.add(dbImageToJson(image));
 			}
 
 			return Results.ok(result);
 		} catch (Exception e) {
 			Logger.warn(e.getMessage(), e);
 			return Results.status(400, e.getMessage());
 		}
 	}
 	
 	private static boolean isAllowedMimetype(HomenagemImagem image) {
 		Configuration config = Play.application().configuration();
 		int index = Arrays.binarySearch(config.getString("image.mimetypes.allowed").split(","), image.getImage().getMimetype());
 		return index >= 0;
 	}
 
 	private static void setRawImage(HomenagemImagem image, byte[] bytes) {
 		RawImage rawImage = image.getImage();
 		if (rawImage == null) {
 			rawImage = new RawImage();
 			image.setImage(rawImage);
 		}
 		rawImage.setImage(bytes);
 	}
 
 	private static void setRawThumbnail(HomenagemImagem image) {
 		Configuration config = Play.application().configuration();
 		int thumbnailWidth = config.getInt("thumbnails.width");
 		int thumbnailHeight = config.getInt("thumbnails.height");
 		RawImage thumbnail = image.getThumbnail();
 		if (thumbnail == null) {
 			thumbnail = new RawImage();
 			image.setThumbnail(thumbnail);
 		}
 		byte[] thumbnailBytes = image.getImage().createThumbnail(thumbnailWidth, thumbnailHeight);
 		thumbnail.setImage(thumbnailBytes);
 	}
     
     private static ObjectNode dbImageToJson(HomenagemImagem image) {
 		ObjectNode result = Json.newObject();
 		result.put("id", image.getId());
 		result.put("name", image.getFilename());
 		result.put("size", image.getImage().getImage().length);
 		result.put("url", "/image/" + image.getId());
 		result.put("thumbnail_url", "/thumbnail/" + image.getId());
 		result.put("delete_url", "/image/" + image.getId());
 		result.put("delete_type", "DELETE");
 		return result;
 	}
     
 }
