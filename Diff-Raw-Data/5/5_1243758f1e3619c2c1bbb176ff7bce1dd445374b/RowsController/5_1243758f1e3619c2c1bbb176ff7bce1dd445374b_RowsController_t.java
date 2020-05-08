 package edu.helsinki.sulka.controllers;
 
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import edu.helsinki.sulka.models.Row;
 import edu.helsinki.sulka.services.RowsService;
 
 /**
  * Handles requests for ringing and control rows.
  */
 @Controller
 public class RowsController extends JSONController {
 	@Autowired
 	private Logger logger;
 	
 	@Autowired
 	private RowsService rowsService;
 
 	/**
 	 * Returns ringing rows by filters.
 	 */
	@RequestMapping(value = "/api/rows/ringings", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
 	@ResponseBody
 	public ListResponse<Row> ringings(
 			Locale locale, Model model,
 			@RequestParam(value="municipality", required=false) String[] municipalities,
 			@RequestParam(value="species", required=false) String[] species,
 			@RequestParam(value="ringPrefix", required=false) String ringPrefix,
 			@RequestParam(value="sort", required=false) String[] sort
 			) throws RowsService.QueryException {
 		return new ListResponse<Row>(rowsService.getRows(new long[] { 846 }, municipalities, species, ringPrefix, sort));
 	}
 	
 	/**
 	 * Returns control rows by filters.
 	 */
	@RequestMapping(value = "/api/rows/controls", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
 	@ResponseBody
 	public ListResponse<Row> controls(
 			Locale locale, Model model,
 			@RequestParam(value="municipality", required=false) String[] municipalities,
 			@RequestParam(value="species", required=false) String[] species,
 			@RequestParam(value="ringPrefix", required=false) String ringPrefix,
 			@RequestParam(value="sort", required=false) String[] sort
 			) throws RowsService.QueryException {
 		return new ListResponse<Row>(rowsService.getRows(new long[] { 846 }, municipalities, species, ringPrefix, sort));
 	}
 }
