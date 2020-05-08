 import com.github.mustachejava.DefaultMustacheFactory;
 import com.github.mustachejava.Mustache;
 import com.google.common.collect.ImmutableMap;
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
 import java.io.File;
 import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.Date;
 import java.util.Map;
 
import static java.lang.String.format;

 @Singleton
 @Path("/")
 public class FightResource {
 	private static final long ONE_MONTH = 1000L * 3600 * 24 * 30;
 
 	private final Talks talks;
 	private final Scorer scorer;
 	private final TopFights topFights;
 
 	@Inject
 	public FightResource(Talks talks, Scorer scorer, TopFights topFights) {
 		this.talks = talks;
 		this.scorer = scorer;
 		this.topFights = topFights;
 	}
 
 	@GET
 	public Response index() {
 		return Response.seeOther(URI.create("/fight/AngularJS/JavaFX")).build();
 	}
 
 	@GET
 	@Path("index.html")
 	public Response oldIndex() {
 		return index();
 	}
 
 	@GET
 	@Path("fight")
 	public Response fight() {
 		return index();
 	}
 
 	@GET
 	@Path("words")
 	@Produces("application/javascript;charset=UTF-8")
 	public String words() {
 		return new Gson().toJson(talks.extractWords());
 	}
 
 	@GET
 	@Path("style.less")
 	@Produces("text/css;charset=UTF-8")
 	public File style() {
 		return new File("web/style.less");
 	}
 
 	@GET
 	@Path("images/bg.jpeg")
 	@Produces("image/jpeg")
 	public Response background() {
 		return staticResource("web/bg.jpeg");
 	}
 
 	@GET
 	@Path("images/star.png")
 	@Produces("image/png")
 	public Response star() {
 		return staticResource("web/star.png");
 	}
 
 	@GET
 	@Path("fight/{left}/{right}")
 	@Produces("text/html;charset=UTF-8")
	public String fight(@PathParam("left") String leftKeyword, @PathParam("right") String rightKeyword) throws UnsupportedEncodingException {
 		topFights.log(leftKeyword, rightKeyword);
 		TopFight topFight = topFights.get();
 
 		Map<String, Object> templateData = ImmutableMap.<String, Object>builder()
 			.put("leftKeyword", leftKeyword)
 			.put("rightKeyword", rightKeyword)
 			.put("leftScore", scorer.get(leftKeyword))
 			.put("rightScore", scorer.get(rightKeyword))
 			.put("topLeftKeyword", topFight.getLeft())
 			.put("topRightKeyword", topFight.getRight())
			.put("url", format("http://fight.code-story.net/fight/%s/%s", leftKeyword, rightKeyword).replace(" ", "%20"))
 			.build();
 
 		Mustache indexTemplate = new DefaultMustacheFactory().compile("web/index.html");
 		return indexTemplate.execute(new StringWriter(), templateData).toString();
 	}
 
 	private static Response staticResource(String path) {
 		File file = new File(path);
 		long modified = file.lastModified();
 
 		return Response.ok(file).lastModified(new Date(modified)).expires(new Date(modified + ONE_MONTH)).build();
 	}
 }
