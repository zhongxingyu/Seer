 package com.programmingteam;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.programmingteam.qsync.QSync;
 import com.programmingteam.qsync.QSyncImport;
 import com.programmingteam.qsync.QSyncVcxproj;
 import com.programmingteam.vs2010.VcxprojSync;
 
 public class Main
 {
 	public static Options OPTS;
 	
 	public static void main(String[] args)
 	{	
 		OPTS = new Options(args);
 		
 		File qsyncFile = new File(OPTS.getFile());
 		if(!qsyncFile.exists())
 		{
 			Log.e("Configuration file not found (" + OPTS.getFile() + ")");
 			System.exit(-1);
 		}
 		
 		//Read file
 		QSync qsync = new QSync(qsyncFile);
 		qsync.debugPrint();
 		Log.v("Qsyn created ok.");
 		
 		List<QSyncVcxproj> qsyncProjs = qsync.getProjects();
 		for(QSyncVcxproj qsyncProj : qsyncProjs)
 		{
 			Log.d(">>> Sync: " + qsyncProj.getVcxproj() + "");
 			VcxprojSync vcxprojSync = new VcxprojSync(qsyncProj.getVcxproj(), qsyncProj.getVcxprojFilters());
 			for(QSyncImport imp: qsyncProj.getImportList())
 			{
 				Log.d("Parsing <import tofilter=\"" + imp.getToFilter() + "\">");
 				vcxprojSync.invalidateFilters(imp.getToFilter());
 				
 				ArrayList<File> dirList = new ArrayList<File>();
				dirList.add(new File(imp.getInclude()));
				dirList.add(new File(imp.getSrc()));
 				
 				while(dirList.size()>0)
 				{
 					File dir = dirList.get(0);
 					dirList.remove(0);
 					File listFiles[] = dir.listFiles();
 					if(listFiles==null)
 					{
 						Log.e("Directory does not exist! " + dir);
 						System.exit(-1);
 					}
 					for(int i=0; i<listFiles.length; ++i)
 					{
 						if(listFiles[i].isDirectory())
 						{
 							dirList.add(listFiles[i]);
 							
 							if(imp.isIncludeEmptyDirs())
 							{
 								String toFilter = listFiles[i].getAbsolutePath()
 										.replace(imp.getInclude(), imp.getToFilter())
 										.replace(imp.getSrc(), imp.getToFilter());
 
 								toFilter = Helpers.stripSlashes(toFilter);
 								vcxprojSync.syncFilter(toFilter);
 							}
 						}
 						else
 						{
 							//TODO add handling misc
 							boolean include =false;
 							if( Helpers.isCompile(listFiles[i], qsync.getCompileExt()) ||
 								(include=Helpers.isInclude(listFiles[i], qsync.getIncludeExt())))
 							{
 								if(include && !imp.matchesInclue(listFiles[i].getName()))
 								{
 									Log.v("Skipping file: "+listFiles[i]+" (not matching regexp)");
 									continue;
 								}
 								if(!include && !imp.matchesSrc(listFiles[i].getName()))
 								{
 									Log.v("Skipping file: "+listFiles[i]+" (not matching regexp)");
 									continue;
 								}
 								
 								boolean isExcludedFromBuild = false;
 								if(include)
 									isExcludedFromBuild = imp.isExcludedInc(""+listFiles[i].getName());
 								else
 									isExcludedFromBuild = imp.isExcludedSrc(""+listFiles[i].getName());
 								
 								VcxprojSync.SyncType syncType = VcxprojSync.SyncType.COMPILE;
 								if(include) syncType = VcxprojSync.SyncType.INCLUDE;
 								
 								String toFilter = listFiles[i].getAbsolutePath()
 										.replace(imp.getInclude(), imp.getToFilter())
 										.replace(imp.getSrc(), imp.getToFilter());
 								toFilter = Helpers.getPath(toFilter);
 								toFilter = Helpers.stripSlashes(toFilter);
 								vcxprojSync.syncFile(
 										qsyncProj.getRelativeFile(listFiles[i]), 
 										toFilter, 
 										syncType, 
 										isExcludedFromBuild);
 							}
 						}
 					}
 				}
 				vcxprojSync.printLog(imp.getToFilter());
 			}
 				
 			//TODO save files!
 			if(!OPTS.isPretend())
 			{
 				vcxprojSync.saveVcxproj(OPTS.getOutput());
 				vcxprojSync.saveVcxprojFilters(OPTS.getOutput());
 			}
 			else
 			{
 				Log.d("Pretend option: skipping file save...");
 			}
 			Log.d("Done.");
 		}
 
 	}
 }
