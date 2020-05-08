 package jp.ac.osaka_u.ist.sel.metricstool.main.plugin;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.METRICS_TYPE;
 
 
 /**
  * @author kou-tngt
  * 
  * (2006/11/17j ̃R[hQŌ^𗘗p邽߁CRpCpɓo^D
  * 
  * <p>
  * gNXvvOCp̒ۃNX
  * <p>
  * evOC͂̃NXpNX1Ȃ΂ȂȂD ܂C̃NXplugin.xmlt@CɎw̌`ŋLqȂ΂ȂȂD
  * <p>
  * mainW[͊evOCfBNgplugin.xmlt@CTA ɋLqĂC̃NXpNXCX^XA
  * e\bhʂď擾Aexecute\bhĂяoăgNXlv
  */
 public abstract class AbstractPlugin {
 
     /**
      * vOC̏ۑsσNXD AbstractPlugin̂݃CX^XłD
      * <p>
      * vOC̏𓮓IɕύXƍ̂ŁA̓NX̃CX^Xp Ƃ邱ƂŃvOC̕sϐD
      * evOC̏ۑPluginInfoCX^X̎擾ɂ {@link AbstractPlugin#getPluginInfo()}pD
      * 
      * @author kou-tngt
      * 
      */
     public class PluginInfo {
 
         /**
          * ftHg̃RXgN^
          */
         private PluginInfo() {
            final LANGUAGE[] languages = AbstractPlugin.this.getMesuarableLanguages();
            this.mesureableLanguages = new LANGUAGE[languages.length];
            System.arraycopy(languages, 0, this.mesureableLanguages, 0, languages.length);
             this.metricsName = AbstractPlugin.this.getMetricsName();
             this.metricsType = AbstractPlugin.this.getMetricsType();
             this.useClassInfo = AbstractPlugin.this.useClassInfo();
             this.useMethodInfo = AbstractPlugin.this.useMethodInfo();
             this.useFileInfo = AbstractPlugin.this.useFileInfo();
             this.useMethodLocalInfo = AbstractPlugin.this.useMethodLocalInfo();
         }
 
         /**
          * ̃vOCgNXvł錾ԂD
          * 
          * @return v\ȌSĊ܂ޔzD
          */
        public LANGUAGE[] getMesureableLanguages() {
            return mesureableLanguages;
         }
 
         /**
          * ̃vOCŎw肳ꂽŗp\ł邩ԂD
          * 
          * @param language p\ł邩𒲂ׂ
          * @return p\łꍇ trueCpłȂꍇ falseD
          */
        public boolean isMesureable(LANGUAGE language) {
            LANGUAGE[] mesureableLanguages = this.getMesureableLanguages();
            for (int i = 0; i < mesureableLanguages.length; i++) {
                if (language.equals(mesureableLanguages[i])) {
                     return true;
                 }
             }
             return false;
         }
 
         /**
          * ̃vOCv郁gNX̖OԂD
          * 
          * @return gNX
          */
         public String getMetricsName() {
             return metricsName;
         }
 
         /**
          * ̃vOCv郁gNX̃^CvԂD
          * 
          * @return gNX^Cv
          * @see jp.ac.osaka_u.ist.sel.metricstool.main.util.METRICS_TYPE
          */
         public METRICS_TYPE getMetricsType() {
             return metricsType;
         }
 
         /**
          * ̃vOCNXɊւ𗘗p邩ǂԂD
          * 
          * @return NXɊւ𗘗pꍇtrueD
          */
         public boolean isUseClassInfo() {
             return useClassInfo;
         }
 
         /**
          * ̃vOCt@CɊւ𗘗p邩ǂԂD
          * 
          * @return t@CɊւ𗘗pꍇtrueD
          */
         public boolean isUseFileInfo() {
             return useFileInfo;
         }
 
         /**
          * ̃vOC\bhɊւ𗘗p邩ǂԂD
          * 
          * @return \bhɊւ𗘗pꍇtrueD
          */
         public boolean isUseMethodInfo() {
             return useMethodInfo;
         }
 
         /**
          * ̃vOC\bhɊւ𗘗p邩ǂԂD
          * 
          * @return \bhɊւ𗘗pꍇtrueD
          */
         public boolean isUseMethodLocalInfo() {
             return useMethodLocalInfo;
         }
 
        private final LANGUAGE[] mesureableLanguages;
 
         private final String metricsName;
 
         private final METRICS_TYPE metricsType;
 
         private final boolean useClassInfo;
 
         private final boolean useFileInfo;
 
         private final boolean useMethodInfo;
 
         private final boolean useMethodLocalInfo;
     }
 
     /**
      * vOCۑĂ{@link PluginInfo}NX̃CX^XԂD
      * AbstractPluginCX^Xɑ΂邱̃\bh͕K̃CX^XԂC ̓ɕۑĂ͕sςłD
      * 
      * @return vOCۑĂ{@link PluginInfo}NX̃CX^X
      */
     public final PluginInfo getPluginInfo() {
         if (null == this.pluginInfo) {
             synchronized (this) {
                 if (null == this.pluginInfo) {
                     this.pluginInfo = new PluginInfo();
                 }
             }
         }
         return this.pluginInfo;
     }
 
     /**
      * ̃vOCgNXvł錾Ԃ pł錾ɐ̂vOĆÃ\bhI[o[ChKvD
      * 
      * @return v\ȌSĊ܂ޔz
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE
      */
    protected LANGUAGE[] getMesuarableLanguages() {
         return LANGUAGE.values();
     }
 
     /**
      * ̃vOCv郁gNX̖OԂۃ\bhD
      * 
      * @return gNX
      */
     protected abstract String getMetricsName();
 
     /**
      * ̃vOCv郁gNX̃^CvԂۃ\bhD
      * 
      * @return gNX^Cv
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.util.METRICS_TYPE
      */
     protected abstract METRICS_TYPE getMetricsType();
 
     /**
      * ̃vOCNXɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * NXɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return NXɊւ𗘗pꍇtrueD
      */
     protected boolean useClassInfo() {
         return false;
     }
 
     /**
      * ̃vOCt@CɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * t@CɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return t@CɊւ𗘗pꍇtrueD
      */
     protected boolean useFileInfo() {
         return false;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * \bhɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return \bhɊւ𗘗pꍇtrueD
      */
     protected boolean useMethodInfo() {
         return false;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bhD ftHgłfalseԂD
      * \bhɊւ𗘗pvOC͂̃\bhI[o[[htrueԂȂΐȂD
      * 
      * @return \bhɊւ𗘗pꍇtrueD
      */
     protected boolean useMethodLocalInfo() {
         return false;
     }
 
     /**
      * gNX͂X^[g钊ۃ\bhD
      */
     protected abstract void execute();
 
     /**
      * vOC̏ۑ{@link PluginInfo}NX̃CX^X getPluginInfo\bh̏̌Ăяoɂč쐬D
      * ȍ~ÃtB[h͏ɓCX^XQƂD
      */
     private PluginInfo pluginInfo;
 }
