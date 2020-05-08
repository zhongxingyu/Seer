 package com.github.hiuprocon.pve.ui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.util.regex.Matcher;
 import java.util.ResourceBundle;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.text.*;
 import javax.tools.JavaCompiler;
 //import javax.tools.ToolProvider;
 //import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
 import org.eclipse.jdt.internal.compiler.batch.Main;
 
 public class SimpleIDE extends JDialog implements ActionListener {
     private static final long serialVersionUID = 1L;
     String workDir;
     String filePath;
     JavaCompiler compiler;
     Main compilerMain;
 
     JButton openB;
     JButton saveB;
     JButton compileB;
     JButton makeJarB;
     JTextArea editor;
     JTextArea outputTA;
     JTextAreaOutputStream jtaos;
     Executor e;
     //i18n
     ResourceBundle messages;
 
     String i18n(String s) {
         return messages.getString(s);
     }
 
     public SimpleIDE(Frame owner) {
         super(owner);
         PropertiesControl pc = new PropertiesControl("UTF-8");
        messages = ResourceBundle.getBundle("com.github.hiuprocon.pve.ui.Messages",pc);
 
         //compiler = ToolProvider.getSystemJavaCompiler();
         try {
             compiler = com.sun.tools.javac.api.JavacTool.create();
         } catch (NoClassDefFoundError e) {
             compiler = null;
             //e.printStackTrace();
         }
         //compiler = new EclipseCompiler();
         e = Executors.newSingleThreadExecutor();
 
         VBox mainBox = new VBox();
         this.add(mainBox);
         HBox buttonsBox = new HBox();
         mainBox.myAdd(buttonsBox,0);
         openB = new JButton(i18n("ide.open"));
         openB.addActionListener(this);
         buttonsBox.myAdd(openB,0);
         saveB = new JButton(i18n("ide.save"));
         saveB.addActionListener(this);
         buttonsBox.myAdd(saveB,0);
         compileB = new JButton(i18n("ide.compile"));
         compileB.addActionListener(this);
         buttonsBox.myAdd(compileB,0);
         makeJarB = new JButton(i18n("ide.makeJar"));
         makeJarB.addActionListener(this);
         buttonsBox.myAdd(makeJarB,0);
         buttonsBox.myAdd(Box.createHorizontalGlue(),1);
         editor = new JTextArea(30,80);
         JScrollPane scroll = new JScrollPane(editor);
         scroll.setRowHeaderView(new LineNumberView(editor));
         scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         editor.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
         mainBox.myAdd(scroll,1);
         outputTA = new JTextArea(15,80);
         mainBox.myAdd(new JScrollPane(outputTA),1);
         jtaos = new JTextAreaOutputStream(outputTA,System.out);
 
         OutputStreamWriter osw = new OutputStreamWriter(jtaos);
         PrintWriter out = new PrintWriter(osw);
         osw = new OutputStreamWriter(jtaos);
         PrintWriter err = new PrintWriter(osw);
 
         compilerMain = new Main(out,err,false,null,null);
     }
     void setEnable(boolean b) {
         openB.setEnabled(b);
         saveB.setEnabled(b);
         compileB.setEnabled(b);
         makeJarB.setEnabled(b);
         editor.setEditable(b);
     }
     public void popup(String workDir) {
         this.workDir = workDir;
 
         //if (compiler==null) {
         if (compilerMain==null) {
             editor.setText(i18n("ide.compilerWarning"));
             this.setEnable(false);
         } else if (workDir==null) {
             editor.setText(i18n("ide.workingFolderWarning"));
             this.setEnable(false);
         } else {
             editor.setText(i18n("ide.emptyWarning"));
             this.setEnable(false);
             openB.setEnabled(true);
         }
 
         this.setModal(true);
         this.pack();
         this.setVisible(true);
     }
     @Override
     public void actionPerformed(ActionEvent ae) {
         Object s = ae.getSource();
         if (s==openB) {
             openFile();
         } else if (s==saveB) {
             saveFile();
         } else if (s==compileB) {
             compile();
         } else if (s==makeJarB) {
             makeJarFile();
         } else {
             System.out.println("gaha:????");
         }
     }
     void openFile() {
         JFileChooser chooser = new JFileChooser(workDir);
         FileNameExtensionFilter filter = new FileNameExtensionFilter(
             "Java Source File", "java");
         chooser.setFileFilter(filter);
         int returnVal = chooser.showOpenDialog(this);
         if(returnVal == JFileChooser.APPROVE_OPTION) {
             File f = chooser.getSelectedFile();
             try {
                 if (!f.exists())
                     f.createNewFile();
                 FileReader fr = new FileReader(f);
                 BufferedReader br = new BufferedReader(fr);
                 editor.setText("");
                 String line = br.readLine();
                 while (line!=null) {
                     editor.append(line+"\n");
                     line = br.readLine();
                 }
                 filePath = f.getAbsolutePath();
                 setEnable(true);
                 editor.setCaretPosition(0);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     void saveFile() {
         try {
             FileWriter fw = new FileWriter(filePath);
             PrintWriter pw = new PrintWriter(fw);
             pw.print(editor.getText());
             pw.flush();
             outputTA.append(i18n("ide.saveSuccess")+"\n");
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     void compile() {
         e.execute(new Runnable() {
             public void run() {
                 if (compiler!=null)
                     compile1();
                 else
                     compile2();
             }
         });
     }
     void prepareJars() {
         String vecmathPath = workDir+File.separator+"vecmath.jar";
         String a3carsimPath = workDir+File.separator+"a3carsim-api.jar";
         File vpF = new File(vecmathPath);
         File apF = new File(a3carsimPath);
         if (!vpF.exists()) {
             outputTA.append(i18n("ide.downloadInfo1"));
             boolean b = dl("http://acerola3d.sourceforge.jp/jws/acerola3d/all/vecmath.jar",vecmathPath);
             if (b)
                 outputTA.append(i18n("ide.success")+"\n");
             else
                 outputTA.append(i18n("ide.fail")+"\n");
         }
         if (!apF.exists()) {
             outputTA.append(i18n("ide.downloadInfo2"));
             boolean b = dl("http://kenji0717.github.com/A3CarSim/jws/A3CarSim/a3carsim-api.jar",a3carsimPath);
             if (b)
                 outputTA.append(i18n("ide.success")+"\n");
             else
                 outputTA.append(i18n("ide.fail")+"\n");
         }
     }
     boolean dl(String s,String d) {
         try {
             URL url = new URL(s);
             URLConnection conn = url.openConnection();
             InputStream in = conn.getInputStream();
 
             File file = new File(d);
             FileOutputStream out = new FileOutputStream(file, false);
             byte[] bytes = new byte[512];
             while(true){
                 int ret = in.read(bytes);
                 if(ret == -1) break;
                 out.write(bytes, 0, ret);
             }
 
             out.close();
             in.close();
             return true;
         }catch(Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 
     void compile1() {
         outputTA.setText("");
         prepareJars();
         String classPath = System.getProperty("java.class.path");
         classPath=classPath+File.pathSeparator+workDir;
         classPath=classPath+File.pathSeparator+workDir+File.separator+"vecmath.jar";
         classPath=classPath+File.pathSeparator+workDir+File.separator+"a3carsim-api.jar"+File.pathSeparator;
         System.out.println("CLASSPATH:"+classPath);
         int result = compiler.run(System.in,jtaos,jtaos,"-cp",classPath,"-d",workDir,filePath);
         if (result==0) {
             outputTA.append(i18n("ide.compileSuccess")+"\n");
         }
     }
 
     void compile2() {
         outputTA.setText("");
         prepareJars();
         String classPath = System.getProperty("java.class.path");
         String ss1 = Matcher.quoteReplacement("\\\\");
         String ss2 = Matcher.quoteReplacement("\\");
         classPath = classPath.replaceAll(ss1,ss2);
         classPath=classPath+File.pathSeparator+workDir;
         classPath=classPath+File.pathSeparator+workDir+File.separator+"vecmath.jar";
         classPath=classPath+File.pathSeparator+workDir+File.separator+"a3carsim-api.jar"+File.pathSeparator;
         System.out.println("CLASSPATH:"+classPath);
         boolean result = compilerMain.compile(new String[]{"-1.6","-cp",classPath,filePath});
         if (result==true) {
             outputTA.append(i18n("ide.compileSuccess")+"\n");
         }
     }
     void makeJarFile() {
         JFileChooser chooser = new JFileChooser();
         FileNameExtensionFilter filter = new FileNameExtensionFilter(
             "JAR File", "jar");
         chooser.setFileFilter(filter);
         int returnVal = chooser.showOpenDialog(this);
         if(returnVal == JFileChooser.APPROVE_OPTION) {
             try {
                 File f = chooser.getSelectedFile();
                 JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
                 File ff = new File(workDir);
                 for (File fff:ff.listFiles()) {
                     makeJarFileRec(jos,fff,"");
                 }
                 jos.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
             outputTA.append(i18n("ide.makeJarSuccess")+"\n");
         }
     }
     void makeJarFileRec(JarOutputStream jos,File f,String path) throws Exception {
         if (f.isDirectory()) {
             File files[] = f.listFiles();
             for (File ff:files) {
                 makeJarFileRec(jos,ff,path+f.getName()+"/");
             }
         } else if (f.isFile()) {
             if (f.getName().equals("vecmath.jar"))
                 return;
             if (f.getName().equals("a3carsim-api.jar"))
                 return;
             if (f.getName().endsWith(".jar"))
                 return;
             FileInputStream fis = new FileInputStream(f);
             byte[] buf = new byte[1024];
             JarEntry je = new JarEntry(path+f.getName());
             jos.putNextEntry(je);
             int len = 0;
             while ((len=fis.read(buf))!=-1) {
                 jos.write(buf,0,len);
             }
             fis.close();
             jos.closeEntry();
         }
     }
 }
 
 //http://terai.xrea.jp/Swing/LineNumber.html
 class LineNumberView extends JComponent {
     private static final long serialVersionUID = 1L;
     private static final int MARGIN = 5;
     private final JTextArea text;
     private final FontMetrics fontMetrics;
     private final int topInset;
     private final int fontAscent;
     private final int fontHeight;
 
     public LineNumberView(JTextArea textArea) {
         text = textArea;
         Font font   = text.getFont();
         fontMetrics = getFontMetrics(font);
         fontHeight  = fontMetrics.getHeight();
         fontAscent  = fontMetrics.getAscent();
         topInset    = text.getInsets().top;
         text.getDocument().addDocumentListener(new DocumentListener() {
             @Override public void insertUpdate(DocumentEvent e) {
                 repaint();
             }
             @Override public void removeUpdate(DocumentEvent e) {
                 repaint();
             }
             @Override public void changedUpdate(DocumentEvent e) {}
         });
         text.addComponentListener(new ComponentAdapter() {
             @Override public void componentResized(ComponentEvent e) {
                 revalidate();
                 repaint();
             }
         });
         setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
         setOpaque(true);
         setBackground(Color.WHITE);
     }
     private int getComponentWidth() {
         Document doc  = text.getDocument();
         Element root  = doc.getDefaultRootElement();
         int lineCount = root.getElementIndex(doc.getLength());
         int maxDigits = Math.max(3, String.valueOf(lineCount).length());
         return maxDigits*fontMetrics.stringWidth("0")+MARGIN*2;
     }
     public int getLineAtPoint(int y) {
         Element root = text.getDocument().getDefaultRootElement();
         int pos = text.viewToModel(new Point(0, y));
         return root.getElementIndex(pos);
     }
     public Dimension getPreferredSize() {
         return new Dimension(getComponentWidth(), text.getHeight());
     }
     @Override public void paintComponent(Graphics g) {
         Rectangle clip = g.getClipBounds();
         g.setColor(getBackground());
         g.fillRect(clip.x, clip.y, clip.width, clip.height);
         g.setColor(getForeground());
         int base  = clip.y - topInset;
         int start = getLineAtPoint(base);
         int end   = getLineAtPoint(base+clip.height);
         int y = topInset-fontHeight+fontAscent+start*fontHeight;
         for(int i=start;i<=end;i++) {
             String text = String.valueOf(i+1);
             int x = getComponentWidth()-MARGIN-fontMetrics.stringWidth(text);
             y = y + fontHeight;
             g.drawString(text, x, y);
         }
     }
 }
