 /*
  * Copyright 2011 Damien Bourdette
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.dbourdette.otto.web.controller;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
import com.github.dbourdette.otto.graph.Graph;
import com.github.dbourdette.otto.source.DBSource;
import com.github.dbourdette.otto.source.DefaultGraphParameters;
import com.github.dbourdette.otto.source.Sources;
import com.github.dbourdette.otto.web.form.GraphForm;
 import com.mongodb.DBObject;
 
 /**
  * @author damien bourdette
  * @version \$Revision$
  */
 @Controller
 public class GraphController {
 
     @Inject
     private Sources sources;
 
     @RequestMapping({"/sources/{name}/graph"})
     public String graph(@PathVariable String name, @Valid GraphForm form, BindingResult result, Model model, HttpServletRequest request) {
         DBSource source = sources.getSource(name);
         DefaultGraphParameters parameters = source.getDefaultGraphParameters();
 
         Map<String, String> map = request.getParameterMap();
 
         if (!map.containsKey("stepInMinutes")) {
             form.setStepInMinutes(parameters.getStepInMinutes());
         }
 
         if (!map.containsKey("splitColumn")) {
             form.setSplitColumn(parameters.getSplitColumn());
         }
 
         if (!map.containsKey("sumColumn")) {
             form.setSumColumn(parameters.getSumColumn());
         }
 
         model.addAttribute("navItem", "graph");
         model.addAttribute("form", form);
         model.addAttribute("graph", buildGraph(name, form).toGoogleHtml(1080, 750));
 
         return "sources/graph";
     }
 
     @RequestMapping({"/sources/{name}/graph.csv"})
     public void csv(@PathVariable String name, GraphForm form, HttpServletResponse response) throws IOException {
         response.setContentType("application/csv");
 
         response.getWriter().write(buildGraph(name, form).toCsv());
     }
 
     @RequestMapping({"/sources/{name}/graph/table"})
     public String table(@PathVariable String name, GraphForm form, Model model) throws IOException {
         model.addAttribute("table", buildGraph(name, form).toHtmlTable());
 
         return "sources/graph_table";
     }
 
     private Graph buildGraph(String name, GraphForm form) {
         DateMidnight start = new DateMidnight(form.getStart());
         DateMidnight end = new DateMidnight(form.getEnd());
 
         Interval interval = new Interval(start, end);
 
         Graph graph = new Graph();
 
         graph.setRows(interval, Duration.standardMinutes(form.getStepInMinutes()));
 
         DBSource source = sources.getSource(name);
 
         Iterator<DBObject> events = source.findEvents(interval);
 
         while (events.hasNext()) {
             DBObject event = events.next();
 
            String columnName = getColumnName(name, form, event);
 
             graph.ensureColumnsExists(columnName);
 
             Date date = (Date) event.get("date");
 
             if (StringUtils.isEmpty(form.getSumColumn())) {
                 graph.increaseValue(columnName, new DateTime(date));
             } else {
                 Integer value = (Integer) event.get(form.getSumColumn());
 
                 graph.increaseValue(columnName, new DateTime(date), value);
             }
         }
 
         if (graph.getColumnCount() == 0) {
             graph.ensureColumnExists("no data");
         }
 
         return graph;
     }

    private String getColumnName(String name, GraphForm form, DBObject event) {
        if (StringUtils.isEmpty(form.getSplitColumn())) {
            return name;
        }

        Object value = event.get(form.getSplitColumn());

        if (value == null) {
            return name;
        }

        return value.toString();
    }
 }
