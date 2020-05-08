 package libreSubs.libreSubsSite.download;
 
import java.nio.charset.Charset;

 import libreSubs.libreSubsSite.CommonsParameters;
 import libreSubs.libreSubsSite.TextPage;
 import libreSubs.libreSubsSite.WicketApplication;
 import libreSubs.libreSubsSite.text.TextResource;
 
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.markup.html.DynamicWebResource;
 import org.apache.wicket.protocol.http.WebResponse;
 import org.libreSubsApplet.utils.LocaleUtil;
 import org.libreSubsEngine.subtitleRepository.repository.SubtitlesRepositoryHandler;
 
 @SuppressWarnings("serial")
 public class DownloadSubtitle extends DynamicWebResource {
 
 	public static final String RESOURCE_NAME = "download";
 	
 	public DownloadSubtitle() {
 		setCacheable(false);
 	}
 
 	public static String getDownloadURLPath() {
 		return WicketApplication.getBasePath() + RESOURCE_NAME;
 	}
 
 	@Override
 	protected DynamicWebResource.ResourceState getResourceState() {
 		final RequestCycle cycle = RequestCycle.get();
 		final WebResponse response = (WebResponse)cycle.getResponse();
 		setHeaderForSubtitle(response);
 		
 		final CommonsParameters parameters = new CommonsParameters(getParameters());
 		if (!parameters.hasAllObrigatoryParameters()) {
 			final String error = "Os seguintes parâmetros devem ser informados: "
 					+ parameters.getLackingParametersNames();
 			if(parameters.isCommandLine()) {
 				return new TextResource(error);
 			} else
 				TextPage.redirectToPageWithText(error);
 		}
 
 		final String language = parameters.getLanguage();
 		if (!LocaleUtil.isValidLanguage(language)) {
 			final String error = "O idioma " + language
 			+ " não é suportado.";
 			if(parameters.isCommandLine()) {
 				return new TextResource(error);
 			} else
 				TextPage.redirectToPageWithText(error);
 		}
 
 		final SubtitlesRepositoryHandler subtitlesRepositoryHandler = WicketApplication
 				.getSubtitlesRepositoryHandler();
 
 		final String subtitle = subtitlesRepositoryHandler.getSubtitleOrNull(
 				parameters
 				.getId(), language);
 
 		if (subtitle == null) {
 			final String error = "Legenda não encontrada.";
 			if(parameters.isCommandLine()) {
 				return new TextResource(error);
 			} else
 				TextPage.redirectToPageWithText(error);
 		}
 
 		return new ResourceState() {
 
 			@Override
 			public byte[] getData() {
				return subtitle.getBytes(Charset.forName("UTF-8"));
 			}
 
 			@Override
 			public String getContentType() {
 				return "text/srt";
 			}
 		};
 	}
 
 	private void setHeaderForSubtitle(final WebResponse response) {
 		final CommonsParameters parameters = new CommonsParameters(
 				getParameters());
 
 		if (subtitleRequestWillFail(parameters)) {
 			return;
 		}
 
 		final String filename = parameters.getFile();
 		if (filename == null)
 			response.setAttachmentHeader("legenda.srt");
 		else
 			response.setAttachmentHeader(filename);
 	}
 
 	private boolean subtitleRequestWillFail(
 			final CommonsParameters parameters) {
 		final String language = parameters.getLanguage();
 		final boolean isLackingParameters = !parameters
 				.hasAllObrigatoryParameters();
 		final boolean languageUnknown = !LocaleUtil.isValidLanguage(language);
 		final SubtitlesRepositoryHandler subtitlesRepositoryHandler = WicketApplication
 				.getSubtitlesRepositoryHandler();
 		final boolean subtitleDoesnExist = !subtitlesRepositoryHandler
 				.subtitleExists(
 				parameters.getId(), language);
 
 		return isLackingParameters || languageUnknown || subtitleDoesnExist;
 	}
 }
