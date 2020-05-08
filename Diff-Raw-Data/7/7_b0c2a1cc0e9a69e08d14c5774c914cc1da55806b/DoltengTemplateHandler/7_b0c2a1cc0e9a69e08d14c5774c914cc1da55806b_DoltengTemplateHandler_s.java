 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.eclipse.template;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import jp.aonir.fuzzyxml.FuzzyXMLDocument;
 import jp.aonir.fuzzyxml.FuzzyXMLElement;
 import jp.aonir.fuzzyxml.FuzzyXMLNode;
 import jp.aonir.fuzzyxml.FuzzyXMLParser;
 import jp.aonir.fuzzyxml.XPath;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.seasar.dolteng.core.convention.NamingConventionMirror;
 import org.seasar.dolteng.core.template.TemplateConfig;
 import org.seasar.dolteng.core.template.TemplateHandler;
 import org.seasar.dolteng.eclipse.DoltengCore;
 import org.seasar.dolteng.eclipse.model.RootModel;
 import org.seasar.dolteng.eclipse.model.impl.TableNode;
 import org.seasar.dolteng.eclipse.nls.Messages;
 import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
 import org.seasar.dolteng.eclipse.util.NameConverter;
 import org.seasar.dolteng.eclipse.util.ResourcesUtil;
 import org.seasar.framework.util.BooleanConversionUtil;
 import org.seasar.framework.util.CaseInsensitiveMap;
 import org.seasar.framework.util.OutputStreamUtil;
 import org.seasar.framework.util.StringUtil;
 
 /**
  * @author taichi
  * 
  */
 public class DoltengTemplateHandler implements TemplateHandler {
 
     private String typeName;
 
     private IProject project;
 
     private RootModel baseModel;
 
     private IProgressMonitor monitor;
 
     private int templateCount = 0;
 
     private List files = new ArrayList();
 
     /**
      * 
      */
     public DoltengTemplateHandler(String typeName, IProject project,
             TableNode node) {
         super();
         this.typeName = typeName;
         this.project = project;
         baseModel = new RootModel(createVariables(node.getMetaData().getName()));
         baseModel.initialize(node);
     }
 
     private Map createVariables(String tableName) {
         Map result = new CaseInsensitiveMap();
         DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
         result.putAll(NamingConventionMirror.toMap(pref.getNamingConvention()));
         String table = NameConverter.toCamelCase(tableName);
         result.put("table", StringUtil.decapitalize(table));
         result.put("table_capitalize", table);
 
         result.put("javasrcroot", pref.getDefaultSrcPath().removeFirstSegments(
                 1).toString());
         result.put("resourceroot", pref.getDefaultResourcePath()
                 .removeFirstSegments(1).toString());
         result.put("webcontentsroot", pref.getWebContentsRoot());
         String pkg = pref.getNamingConvention().getRootPackageNames()[0];
         result.put("rootpackagename", pkg);
         result.put("rootpackagepath", pkg.replace('.', '/'));
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.core.template.TemplateHandler#getTemplateConfigs(java.lang.String)
      */
     public TemplateConfig[] getTemplateConfigs() {
         List result = new ArrayList();
         try {
             URL url = DoltengCore.getDefault().getBundle().getEntry(
                     "template/fm/" + typeName + ".xml");
             FuzzyXMLParser parser = new FuzzyXMLParser();
             FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(url
                     .openStream()));
             FuzzyXMLNode[] list = XPath.selectNodes(doc.getDocumentElement(),
                     "//template");
             templateCount = list.length;
 
             for (int i = 0; i < list.length; i++) {
                 FuzzyXMLElement n = (FuzzyXMLElement) list[i];
                 TemplateConfig tc = new TemplateConfig();
                 tc.setTemplatePath(n.getAttributeNode("path").getValue());
                 FuzzyXMLNode[] children = n.getChildren();
                 for (int j = 0; j < children.length; j++) {
                     if (children[j] instanceof FuzzyXMLElement) {
                         n = (FuzzyXMLElement) children[j];
                         tc.setOverride(BooleanConversionUtil
                                 .toPrimitiveBoolean(n.getAttributeNode(
                                         "override").getValue()));
                         tc.setOutputPath(n.getAttributeNode("path").getValue());
                         tc.setOutputFile(n.getAttributeNode("name").getValue());
                         break;
                     }
                 }
                 result.add(tc);
             }
         } catch (IOException e) {
             DoltengCore.log(e);
         }
 
         return (TemplateConfig[]) result.toArray(new TemplateConfig[result
                 .size()]);
     }
 
     public RootModel getProcessModel(TemplateConfig config) {
         return baseModel;
     }
 
     public void prepare(IProgressMonitor monitor) {
         if (monitor != null) {
             this.monitor = monitor;
         } else {
             this.monitor = new NullProgressMonitor();
         }
     }
 
     public void begin() {
         monitor.beginTask(Messages.GENERATE_CODES, templateCount);
     }
 
     public OutputStream open(TemplateConfig config) {
         try {
             IPath p = new Path(config.resolveOutputPath(baseModel.getConfigs()))
                     .append(config.resolveOutputFile(baseModel.getConfigs()));
             monitor.subTask(p.toString());
 
             ResourcesUtil.createDir(this.project, config
                     .resolveOutputPath(baseModel.getConfigs()));
 
             IFile f = project.getFile(p);
             boolean is = true;
             if (f.exists()) {
                 is = false;
                 if (config.isOverride()) {
                     f.delete(true, null);
                     is = true;
                 }
             }
             if (is) {
                 f.create(new ByteArrayInputStream(new byte[0]), true, null);
                 this.files.add(f);
                 return new FileOutputStream(f.getLocation().toFile());
             } else {
                 return null;
             }
         } catch (Exception e) {
             DoltengCore.log(e);
             throw new RuntimeException(e);
         } finally {
             monitor.worked(1);
         }
     }
 
     public void done() {
         try {
             project.refreshLocal(IResource.DEPTH_INFINITE, null);
             monitor.done();
         } catch (CoreException e) {
             DoltengCore.log(e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.core.template.TemplateHandler#fail(org.seasar.dolteng.core.template.RootModel,
      *      java.lang.Exception)
      */
     public void fail(RootModel model, Exception e) {
         DoltengCore.log(e);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.core.template.TemplateHandler#close(java.io.OutputStream)
      */
     public void close(OutputStream stream) {
         OutputStreamUtil.close(stream);
     }
 
 }
