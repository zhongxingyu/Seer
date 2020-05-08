 /*
  * (C) Copyright 2006-20011 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Nuxeo - initial API and implementation
  *
  */
 
 package org.nuxeo.ecm.platform.reporting.tests;
 
 import static org.junit.Assert.*;
 import static org.nuxeo.ecm.platform.reporting.api.Constants.BIRT_REPORT_INSTANCE_TYPE;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.inject.Inject;
 import org.eclipse.birt.report.engine.api.HTMLRenderOption;
 import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.nuxeo.common.utils.FileUtils;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.EventService;
 import org.nuxeo.ecm.core.test.CoreFeature;
 import org.nuxeo.ecm.platform.reporting.api.ReportInstance;
 import org.nuxeo.ecm.platform.reporting.api.ReportModel;
 import org.nuxeo.ecm.platform.reporting.report.ReportParameter;
import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.test.runner.Deploy;
 import org.nuxeo.runtime.test.runner.Features;
 import org.nuxeo.runtime.test.runner.FeaturesRunner;
 
 @RunWith(FeaturesRunner.class)
 @Features(CoreFeature.class)
 @Deploy({ "org.nuxeo.ecm.platform.birt.reporting" })
 public class TestAdapters {
 
     String reportPath = null;
 
     @Inject
     protected CoreSession session;
 
     protected DocumentModel createModelDoc() throws Exception {
 
         DocumentModel model = session.createDocumentModel("/", "model",
                 "BirtReportModel");
 
         File report = FileUtils.getResourceFileFromContext("reports/VCSReportWithParams.rptdesign");
 
         model.setPropertyValue("dc:title", "My model");
         model.setPropertyValue("birtmodel:reportName", "VCSReportWithParams");
         model.setPropertyValue("file:content", new FileBlob(report));
 
         model = session.createDocument(model);
         session.save();
        Framework.getService(EventService.class).waitForAsyncCompletion();
 
         return model;
     }
 
 
     @After
     public void cleanUpTempForlder() {
         if (reportPath != null) {
             FileUtils.deleteTree(new File(reportPath));
             reportPath = null;
         }
     }
 
     protected DocumentModel createInstanceDoc(ReportModel model)
             throws Exception {
 
         DocumentModel instance = session.createDocumentModel("/", "instance",
                 "BirtReport");
 
         instance.setPropertyValue("dc:title", "My instance");
         instance.setPropertyValue("birt:modelRef", model.getId());
 
         instance = session.createDocument(instance);
         session.save();
        Framework.getService(EventService.class).waitForAsyncCompletion();
 
         return instance;
     }
 
 
     @Test
     public void testParams() throws Exception {
 
         DocumentModel model = createModelDoc();
         ReportModel reportModel = model.getAdapter(ReportModel.class);
         assertNotNull(reportModel);
 
         // create instance
         DocumentModel instance = createInstanceDoc(reportModel);
         ReportInstance reportInstance = instance.getAdapter(ReportInstance.class);
         assertNotNull(reportInstance);
         reportInstance.setParameter("docType", "$docType$");
 
         List<ReportParameter> allParams = reportInstance.getReportParameters();
         assertEquals(4, allParams.size());
 
         List<ReportParameter> userParams = reportInstance.getReportUserParameters();
         assertEquals(3, userParams.size());
 
         reportModel.setParameter("modelParam", "fromModel");
         session.save();
 
         userParams = reportInstance.getReportUserParameters();
         assertEquals(2, userParams.size());
 
         reportInstance.setParameter("instanceParam", "fromInstance");
         session.save();
 
         userParams = reportInstance.getReportUserParameters();
         assertEquals(1, userParams.size());
 
     }
 
     @Test
     public void testAdapters() throws Exception {
 
         DocumentModel model = createModelDoc();
 
         ReportModel reportModel = model.getAdapter(ReportModel.class);
         assertNotNull(reportModel);
 
         List<ReportParameter> reportParams = reportModel.getReportParameters();
         assertEquals(4, reportParams.size());
 
         Map<String, String> params = reportModel.getStoredParameters();
 
         reportModel.setParameter("modelParam", "fromModel");
 
         session.save();
 
         // check params
         params = reportModel.getStoredParameters();
         assertTrue(params.containsKey("modelParam"));
 
         // recheck after full refresh
         model = session.getDocument(model.getRef());
         reportModel = model.getAdapter(ReportModel.class);
         params = reportModel.getStoredParameters();
         assertTrue(params.containsKey("modelParam"));
 
         // create instance
         DocumentModel instance = createInstanceDoc(reportModel);
         ReportInstance reportInstance = instance.getAdapter(ReportInstance.class);
         assertNotNull(reportInstance);
 
         assertNotNull(reportInstance.getModel());
 
         reportInstance.setParameter("instanceParam", "fromInstance");
         session.save();
         params = reportInstance.getStoredParameters();
         assertNotNull(params.get("instanceParam"));
 
         // test rendering
 
         String dirPath = new Path(System.getProperty("java.io.tmpdir")).append(
                 "birt-test-report-doc-" + System.currentTimeMillis()).toString();
         reportPath = dirPath;
         File baseDir = new File(dirPath);
         baseDir.mkdir();
         File imagesDir = new File(dirPath + "/images");
         imagesDir.mkdir();
         File result = new File(dirPath + "/report");
 
         OutputStream out = new FileOutputStream(result);
 
         HTMLRenderOption options = new HTMLRenderOption();
         options.setImageHandler(new HTMLServerImageHandler());
         options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);
         options.setOutputStream(out);
         options.setBaseImageURL("images");
         options.setImageDirectory(imagesDir.getAbsolutePath());
 
         Map<String, Object> userParams = new HashMap<String, Object>();
         userParams.put("docType", "$docType$");
         userParams.put("userParam", "fromUser");
         reportInstance.render(options, userParams);
 
         String generatedHtml = FileUtils.readFile(result);
 
         // model param
         assertTrue(generatedHtml.contains("fromModel"));
         // instance param
         assertTrue(generatedHtml.contains("fromInstance"));
         // user param
         assertTrue(generatedHtml.contains("fromUser"));
         // ctx param
         assertTrue(generatedHtml.contains(BIRT_REPORT_INSTANCE_TYPE));
 
         // query result
         assertTrue(generatedHtml.contains(model.getId()));
         assertTrue(generatedHtml.contains(instance.getId()));
 
     }
 }
