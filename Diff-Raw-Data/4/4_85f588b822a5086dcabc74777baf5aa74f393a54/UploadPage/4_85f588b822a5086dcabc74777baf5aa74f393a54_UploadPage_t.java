 package libreSubs.libreSubsSite.upload;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import libreSubs.libreSubsSite.ErrorPage;
import libreSubs.libreSubsSite.WicketApplication;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.http.WebRequest;
 import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
 import org.apache.wicket.util.lang.Bytes;
 import org.apache.wicket.util.upload.FileItem;
 import org.wicketstuff.annotation.mount.MountPath;
 
 @MountPath(path = "upload")
 public class UploadPage extends WebPage {
 
 	private static final Bytes MAX_SUB_SIZE = Bytes.kilobytes(200);
 	public static final String RESOURCE_NAME = "upload";
 
 	public UploadPage(final PageParameters pageParameters) {
 		if (pageParameters.size() <= 0) {
 
 			final MultipartServletWebRequest multipartWebRequest = (MultipartServletWebRequest) ((WebRequest) getRequest())
 					.newMultipartWebRequest(MAX_SUB_SIZE);
 
 			getRequestCycle().setRequest(multipartWebRequest);
 			@SuppressWarnings("unchecked")
 			final PageParameters postParameters = new PageParameters(
 					multipartWebRequest.getParameterMap());
 
 			final Object idParam = postParameters.get("id");
 			final Object langParam = postParameters.get("lang");
 
 			if (idParam == null) {
 				ErrorPage.redirectToError("Sha1 precisa ser informado.");
 			}
 			if (langParam == null) {
 				ErrorPage.redirectToError("LÃ­ngua precisa ser informada.");
 			}
 
 			final Map<String, FileItem> files = multipartWebRequest.getFiles();
 			final FileItem fileParam = files.get("file");
 			if (fileParam == null) {
 				ErrorPage.redirectToError("Arquivo precisa ser informado.");
 			}
 
 			File tmpFile = null;
 			try {
 				tmpFile = File.createTempFile("libresub", ".tmp");
 			} catch (final IOException e) {
 				ErrorPage
 						.redirectToError("Erro no servidor ao tentar criar arquivo temporário de legenda.");
 			}
 
 			try {
 				fileParam.write(tmpFile);
 			} catch (final Exception e) {
 				ErrorPage
 						.redirectToError("Erro no servidor ao tentar escrever em arquivo temporário de legenda.");
 			}
 
 			final String id = ((String[]) idParam)[0];
 			final String lang = ((String[]) langParam)[0];
 
 			try {
 				new SubtitleUploader().upload(id, lang, tmpFile);
 			} catch (final SubtitleUploadingException e) {
 				ErrorPage.redirectToError(e.getMessage());
 			}
 
 		}
 	}
 
 	public static String getUploadURLPath() {
		return WicketApplication.getBasePath() + RESOURCE_NAME;
 	}
 
 }
