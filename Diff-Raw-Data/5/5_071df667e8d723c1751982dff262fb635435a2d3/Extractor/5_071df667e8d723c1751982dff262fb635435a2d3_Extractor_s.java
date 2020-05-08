 package cz.opendata.linked.cz.ruian;
 
 import java.io.IOException;
 import java.net.URL;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
 import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
 import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
 import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
 import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
 import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
 import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
 import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
 
 @AsExtractor
 public class Extractor 
 extends ConfigurableBase<ExtractorConfig> 
 implements DPU, ConfigDialogProvider<ExtractorConfig> {
 
 	/**
 	 * DPU's configuration.
 	 */
 
 	private Logger logger = LoggerFactory.getLogger(DPU.class);
 
 	@OutputDataUnit(name = "XMLObce")
 	public RDFDataUnit outObce;	
 	
 	@OutputDataUnit(name = "XMLZsj")
 	public RDFDataUnit outZsj;	
 
 	public Extractor(){
 		super(ExtractorConfig.class);
 	}
 
 	@Override
 	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
 		return new ExtractorDialog();
 	}
 
 	public void execute(DPUContext ctx) throws DPUException
 	{
 		Cache.setInterval(config.interval);
 		Cache.setTimeout(config.timeout);
 		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
 		Cache.logger = logger;
 		Cache.rewriteCache = config.rewriteCache;
 		Scraper_parser s = new Scraper_parser();
 		s.logger = logger;
 		s.ctx = ctx;
 		s.obce = outObce;
 		s.zsj = outZsj;
 		s.outputFiles = config.passToOutput;
 
 		java.util.Date date = new java.util.Date();
 		long start = date.getTime();
 
 		//Download
 
 		try {
 			URL init = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
			URL initStat = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
 			if (config.rewriteCache)
 			{
 				Path path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku@vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
				Path pathStat = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku@vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
 				logger.info("Deleting " + path);
 				Files.deleteIfExists(path);
 				logger.info("Deleting " + pathStat);
 				Files.deleteIfExists(pathStat);				
 			}
 			
 			try {
 				s.parse(init, "init");
 				s.parse(initStat, "initStat");
 			} catch (BannedException b) {
 				logger.warn("Seems like we are banned for today");
 			}
 			
         	logger.info("Download done.");
 		
 		} catch (IOException e) {
 			logger.error(e.getLocalizedMessage());
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			logger.error("Interrupted");
 		}
 		
 		java.util.Date date2 = new java.util.Date();
 		long end = date2.getTime();
 
 		ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");
 
 	}
 
 	@Override
 	public void cleanUp() {	}
 
 }
