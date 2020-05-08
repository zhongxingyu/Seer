 package controllers;
 
 import backend.PlasteBot;
 import backend.Pygments;
 import jobs.IrcMessageJob;
 import models.Paste;
 import net.sf.jmimemagic.Magic;
 import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
 import play.Logger;
 import play.data.validation.Valid;
 import play.mvc.Controller;
 import play.mvc.Router;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Application extends Controller {
 	public static void gotoNewPaste() {
 		newPaste();
 	}
 
 	public static void index() {
 		List<Paste> pastes = Paste.find("order by pastedAt desc").fetch();
 		render(pastes);
 	}
 
 	public static void show(Long id) {
 		final Paste paste = Paste.findById(id);
 		notFoundIfNull(paste);
		final boolean includesCode = !StringUtils.isEmpty(paste.code);
		final String highlightedCode = includesCode ? Pygments.highlight(paste.code, paste.codeMimeType) : null;
 		render(paste, highlightedCode);
 	}
 
 	public static void newPaste() {
 		final List<String> nicks = PlasteBot.getInstance().getNicks();
 		final Map<String,String> lexers = Pygments.lexers();
 		render(nicks, lexers);
 	}
 
 	public static void post(final File attachment, @Valid final Paste paste) {
         if(validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             newPaste();
         }
 
 		if (attachment != null && attachment.canRead()) {
 			final FileInputStream inputStream;
 			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 			try {
 				inputStream = new FileInputStream(attachment);
 				IOUtils.copy(inputStream, outputStream);
 			} catch (FileNotFoundException e) {
 				Logger.error("Play gave as u file that cannot be found", e);
 				error("Unable to process the uploaded file");
 			} catch (IOException e) {
 				Logger.error("Failed to copy the uploaded file to a byte array", e);
 				error("Unable to process the uploaded file");
 			}
 			final byte[] contents = outputStream.toByteArray();
 			paste.attachment = contents;
 
 			try {
 				paste.attachmentMimeType = Magic.getMagicMatch(contents).getMimeType();
 				Logger.info("Mime type: " + paste.attachmentMimeType);
 			} catch (Exception e) {
 				Logger.warn("Unable to determine mimetype for attachment", e);
 				paste.attachmentMimeType = "application/octet-stream";
 			}
 
 			final String[] pathParts = attachment.getName().split("/");
 			paste.attachmentFilename = pathParts[pathParts.length - 1];
 		}
 
 		paste.save();
 
 
 		final HashMap<String, Object> args = new HashMap<String, Object>();
 		args.put("id", paste.id);
 		final String url = Router.getFullUrl("Application.show", args);
 		IrcMessageJob.sendMessage(paste, url);
 
 		show(paste.id);
 	}
 
 	public static void attachment(Long id) {
 		final Paste paste = Paste.findById(id);
 		notFoundIfNull(paste);
 
 		response.contentType = paste.attachmentMimeType;
 		response.setHeader("Content-Disposition", "attachment; filename=" + paste.attachmentFilename + ";");
 		try {
 			response.out.write(paste.attachment);
 		} catch (IOException e) {
 			Logger.error("Failed to write attachment to response", e);
 			response.contentType = "text/html";
 			error("Failed to send attachment");
 		}
 	}
 }
