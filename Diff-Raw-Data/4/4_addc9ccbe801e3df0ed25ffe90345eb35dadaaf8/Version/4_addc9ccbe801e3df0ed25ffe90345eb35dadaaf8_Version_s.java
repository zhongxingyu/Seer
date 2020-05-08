 package org.svenk.redmine.core.client.container;
 
 import org.eclipse.mylyn.commons.core.StatusHandler;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.svenk.redmine.core.RedmineCorePlugin;
 
 public class Version {
 
 	public Plugin plugin;
 
 	public Redmine redmine;
 
 	public enum Release {
 		REDMINE_0_8_7(0, 8, 7),
 		PLUGIN_2_6(2, 6);
 
 		public final int major;
 		public final int minor;
 		public final int tiny;
 
 		Release(int major, int minor) {
 			this(major, minor, 0);
 		}
 
 		Release(int major, int minor, int tiny) {
 			this.major = major;
 			this.minor = minor;
 			this.tiny = tiny;
 		}
 	}
 	
 	public static class Plugin extends SubVersion {
 
 		public static Plugin fromString(String globalVersionString) {
 			Plugin p = null;
 			
 			int vPos = globalVersionString.indexOf("v");
 			if (vPos >= 0 && vPos < globalVersionString.length() - 1) {
 				String sub = globalVersionString.substring(vPos + 1);
 
 				String[] parts = sub.split("\\.");
 				if (parts != null && parts.length >= 2) {
 					try {
 						p = new Plugin();
 						p.major = Integer.parseInt(parts[0]);
 						p.minor = Integer.parseInt(parts[1]);
 						if (parts != null && parts.length >= 3) {
 							p.tiny = Integer.parseInt(parts[2]);
 						}
 					} catch (NumberFormatException e) {
 						p = null;
 						StatusHandler.fail(RedmineCorePlugin.toStatus(e, null, e.getMessage()));
 					}
 				}
 			} else {
 				p = fromString(Plugin.class, globalVersionString);
 			}
 
 			return p;
 		}
 
 	}
 
 	public static class Redmine extends SubVersion {
 
 		public static Redmine fromString(String globalVersionString) {
 			int vPos = globalVersionString.indexOf("v");
 			if (vPos >= 0) {
 				globalVersionString = globalVersionString.substring(0, vPos);
 			}
 			return fromString(Redmine.class, globalVersionString);
 		}
 
 	}
 
 	private abstract static class SubVersion implements Comparable<Release> {
 
 		public int major;
 
 		public int minor;
 
 		public int tiny;
 
 		protected static <T extends SubVersion> T fromString(Class<T> clazz, String globalVersionString) {
 			String[] parts = globalVersionString.split("\\.");
 			if (parts != null && parts.length >= 3) {
 				try {
 					T version = clazz.newInstance();
 					version.major = Integer.parseInt(parts[0]);
 					version.minor = Integer.parseInt(parts[1]);
 					version.tiny = Integer.parseInt(parts[2]);
 					return version;
 				} catch (Exception e) {
 					StatusHandler.fail(RedmineCorePlugin.toStatus(e, null, e.getMessage()));
 				}
 			}
 			return null;
 		}
 
 		public int compareTo(Release release) {
 			if (major < release.major) {
 				return -1;
 			}
 			if (major > release.major) {
 				return 1;
 			}
 			if (minor < release.minor) {
 				return -1;
 			}
 			if (minor > release.minor) {
 				return 1;
 			}
 			if (tiny < release.tiny) {
 				return -1;
 			}
 			if (tiny > release.tiny) {
 				return 1;
 			}
 			return 0;
 		}
 
 		public int compareTo(SubVersion version) {
 			if (major < version.major) {
 				return -1;
 			}
 			if (major > version.major) {
 				return 1;
 			}
 			if (minor < version.minor) {
 				return -1;
 			}
 			if (minor > version.minor) {
 				return 1;
 			}
 			if (tiny < version.tiny) {
 				return -1;
 			}
 			if (tiny > version.tiny) {
 				return 1;
 			}
 			return 0;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("%d.%d.%d", major, minor, tiny);
 		}
 
 	}
 	
 	@Override
 	public String toString() {
 		if (redmine!=null && plugin!=null) {
 			return redmine.toString() + "v" + plugin.toString();
 		}
 		return TaskRepository.NO_VERSION_SPECIFIED;
 	}
 
 }
