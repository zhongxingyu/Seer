 package com.vaguehope.takeshi.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.vz.mongodb.jackson.DBCursor;
 import net.vz.mongodb.jackson.JacksonDBCollection;
 import net.vz.mongodb.jackson.WriteResult;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.mongodb.DB;
 import com.mongodb.Mongo;
 import com.vaguehope.takeshi.helpers.ServletHelper;
 import com.vaguehope.takeshi.model.Castle;
 import com.vaguehope.takeshi.model.CastleId;
 
 /**
  * http://wiki.fasterxml.com/JacksonInFiveMinutes
  * https://github.com/vznet/mongo-jackson-mapper
  */
 public class DataServlet extends HttpServlet {
 
 	public static final String CONTEXT = "/data";
 
 	private static final Logger LOG = LoggerFactory.getLogger(DataServlet.class);
 	private static final long serialVersionUID = 7860470592232818713L;
 	private static final String DBNAME = "takeshi";
 	private static final String COLL_CASTLES = "castles";
 	private static final String PARAM_ID = "id";
 	private static final String PARAM_NEW = "new";
 	private static final String CONTENT_TYPE_JSON = "text/json;charset=UTF-8";
 	private static final String CONTENT_TYPE_PLAIN = "text/plain;charset=UTF-8";
 
 	private final ObjectMapper mapper = new ObjectMapper();
 	private final DB db;
 	private final JacksonDBCollection<Castle, String> collCastles;
 
 	public DataServlet (Mongo mongo) {
 		this.db = mongo.getDB(DBNAME);
 		this.collCastles = JacksonDBCollection.wrap(this.db.getCollection(COLL_CASTLES), Castle.class, String.class);
 	}
 
 	@Override
 	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter w = resp.getWriter();
 		String id = req.getParameter(PARAM_ID);
 		if (id != null && !id.isEmpty()) {
 			Castle result = this.collCastles.findOneById(id);
 			if (result != null) {
 				resp.setContentType(CONTENT_TYPE_JSON);
 				this.mapper.writeValue(w, result);
 			}
 			else {
 				ServletHelper.error(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID: " + id);
 			}
 		}
 		else {
 			resp.setContentType(CONTENT_TYPE_PLAIN);
 			List<CastleId> ids = Lists.newArrayList();
 			DBCursor<Castle> cursor = this.collCastles.find();
 			while (cursor.hasNext()) {
 				Castle next = cursor.next();
 				ids.add(new CastleId(next));
 			}
 			resp.setContentType(CONTENT_TYPE_JSON);
 			this.mapper.writeValue(w, ids);
 		}
 	}
 
 	@Override
 	protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!doNew(req, resp)) doUpdate(req, resp);
 	}
 
	private boolean doNew (HttpServletRequest req, HttpServletResponse resp) {
 		String name = req.getParameter(PARAM_NEW);
 		if (name != null && !name.isEmpty()) {
 			Castle castle = new Castle(name);
 			WriteResult<Castle, String> result = this.collCastles.insert(castle);
 			LOG.info("Created castle: id={}", result.getSavedId());
 			return true;
 		}
 		return false;
 	}
 
 	@SuppressWarnings("boxing")
 	private void doUpdate (HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String json = ServletHelper.validateStringParam(req, resp, "json");
 		if (json == null) return;
 
 		Castle castle = this.mapper.readValue(json, Castle.class);
 		if (castle.getId() == null || castle.getId().isEmpty()) {
 			ServletHelper.error(resp, HttpServletResponse.SC_BAD_REQUEST, "Castle ID is not valid: " + castle.getId());
 			return;
 		}
 
 		WriteResult<Castle, String> result = this.collCastles.updateById(castle.getId(), castle);
 		if (result.getN() != 1) {
 			ServletHelper.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save castle: " + castle.getId());
 			return;
 		}
 		LOG.info("Saved castle: id={} n={}", castle.getId(), castle.getNodes().size());
 	}
 
 }
