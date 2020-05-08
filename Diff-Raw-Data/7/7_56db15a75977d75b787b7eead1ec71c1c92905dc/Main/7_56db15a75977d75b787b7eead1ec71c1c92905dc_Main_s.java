 package jp.co.nttcom.camel.documentbuilder;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import javax.xml.bind.JAXB;
 import jp.co.nttcom.camel.documentbuilder.xml.model.Component;
 import jp.co.nttcom.camel.documentbuilder.xml.model.Model;
 import jp.co.nttcom.camel.documentbuilder.xml.plugin.Extension;
 import jp.co.nttcom.camel.documentbuilder.xml.plugin.Plugin;
 import jp.co.nttcom.camel.documentbuilder.xml.plugin.StringProperty;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 
 public class Main {
 
     public static void main(String[] args) throws Exception {
 
         Main main = new Main();
        main.run("C:\\home\\src.2\\camel\\branch-2.1-modeler2\\components", "model.xml");

     }
 
     public void run(String templates, String modelXml) throws IOException {
         // コンポーネントの雛形を取得
         List<Plugin> plugins = loadPlugin(templates);
 
         // Modeler上で設計したモデルを取得
         Model model = loadModel(modelXml);
 
         // モデルに雛形から取得した値をマージ
        model.copyFrom(plugins);
         
         // 各プロパティを表示順でソート
         sortByDisplayNo(model);
 
         // HTML出力
         BuilderFactory factory = BuilderFactory.getFactory("html");
         factory.build(model.getComponents(), new File("specification"));
     }
 
     private List<Plugin> loadPlugin(String source) {
         List<Plugin> plugins = new ArrayList<Plugin>();
         for (File file : getPluginXMLs(source)) {
             Plugin plugin = JAXB.unmarshal(file, Plugin.class);
             // 並び順を付与
             for (Extension extension : plugin.getExtensions()) {
                 int displayNo = 1;
                 for (StringProperty property : extension.getProperties().getStringProperties()) {
                     property.setDisplayNo(displayNo);
                     displayNo++;
                 }
             }
             plugins.add(plugin);
         }
         return plugins;
     }
     
     private static Collection<File> getPluginXMLs(String dir) {
         if (dir == null) {
             return Collections.EMPTY_LIST;
         }
 
         File target = new File(dir);
         if (!target.exists()) {
             return Collections.EMPTY_LIST;
         }
 
         return FileUtils.listFiles(target, FileFilterUtils.nameFileFilter("plugin.xml"), TrueFileFilter.TRUE);
     }
 
     private Model loadModel(String source) {
         Model model = JAXB.unmarshal(new File(source), Model.class);
         for (Component component : model.getComponents()) {
             String componentType = null;
             for (StringProperty property : component.getStringProperties()) {
                 if ("__componentType__".equals(property.getName())) {
                     componentType = property.getValue();
                 }
             }
             component.setPaletteLabel(componentType);
         }
         return model;
     }
 
     private void sortByDisplayNo(Model model) {
         for (Component component : model.getComponents()) {
             component.sort();
         }
     }
 }
