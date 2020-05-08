 package com.pocketcookies.pepco.web;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.pocketcookies.pepco.model.Summary;
 import com.pocketcookies.pepco.model.dao.SummaryDAO;
 
 @Controller
 public class PepcoController {
 	private final SummaryDAO summaryDao;
 
 	@Autowired
 	public PepcoController(@Qualifier("summaryDao") final SummaryDAO summaryDao) {
 		this.summaryDao = summaryDao;
 	}
 
 	@RequestMapping(value = "/")
 	public ModelAndView index() {
		final Summary summary= this.summaryDao.getMostRecentSummary();
 		// final Summary previousSummary = summaries.get(1);
 		final ModelAndView mav = new ModelAndView("pepco.homepage");
		mav.addObject("summary", summary);
 		return mav;
 	}
 
 	@RequestMapping(value = "/summary-data", method = RequestMethod.GET)
 	@ResponseBody
 	public String summary() throws JSONException {
 		final JSONArray dcdata = new JSONArray(), pgdata = new JSONArray(), montdata = new JSONArray();
 		final JSONObject dc = new JSONObject().put("label", "DC")
 				.put("color", "#0157AB").put("data", dcdata);
 		final JSONObject pg = new JSONObject().put("label", "Prince George")
 				.put("color", "#888888").put("data", pgdata);
 		final JSONObject mont = new JSONObject().put("label", "Montgomery")
 				.put("color", "#21941B").put("data", montdata);
 
 		for (final Summary s : this.summaryDao.getSummaries(null, null, false,
 				0)) {
 			dcdata.put(new JSONArray().put(s.getWhenGenerated().getTime()).put(
 					s.getDcAffectedCustomers()));
 			pgdata.put(new JSONArray().put(s.getWhenGenerated().getTime()).put(
 					s.getPgAffectedCustomers()));
 			montdata.put(new JSONArray().put(s.getWhenGenerated().getTime())
 					.put(s.getMontAffectedCustomers()));
 		}
 
 		return new JSONArray().put(dc).put(pg).put(mont).toString();
 	}
 }
