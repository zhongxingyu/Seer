 package com.ii2d.genthemall.maven.plugin;
 
 import groovy.util.ConfigObject;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import com.ii2d.dbase.util.Assert;
 import com.ii2d.genthemall.SourceGenerator;
import com.ii2d.genthemall.exception.GenthemallException;
 import com.ii2d.genthemall.source.DatabaseSource;
 
 /**
  * Goal which generate config file
  * 
  * @goal generate-config
  * 
  */
 public class GenerateConfigMojo extends AbstractMojo {
 
	private static final Log LOG = LogFactory.getLog(GenerateConfigMojo.class);
 	/**
 	 * @parameter expression="${configTemplateFilePath}" default-value=
 	 *            "classpath:com/ii2d/genthemall/template/commons/conf/mysql.conf"
 	 */
 	private String configTemplateFilePath;
 
 	/**
 	 * @parameter expression="${configDestPath}"
 	 *            default-value="${project.build.directory}/genthemall/"
 	 */
 	private String configDestPath;
 
 	/**
 	 * @parameter expression="${tables}"
 	 *            default-value="user,db,host,time_zone,time_zone_name"
 	 */
 	private String tables;
 	
 	/**
 	 * @parameter
 	 */
 	private BasicDataSource dataSource;
 
 	@Override
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		Assert.notNull(dataSource);
 		Assert.hasText(tables);

 		String tableArr[] = tables.split(",");
 		SourceGenerator g = new SourceGenerator();
 		for (String t : tableArr) {
 			t = StringUtils.trimToEmpty(t);
 			if (StringUtils.isNotBlank(t)) {
 				ConfigObject s = new DatabaseSource(dataSource, t);
 				g.addSource(s);
 				g.setTemplateFilePath(configTemplateFilePath);
 				g.setDestPath(configDestPath);
 			}
 		}
 		g.generate();
 	}
 
 }
