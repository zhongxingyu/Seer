 package info.sholes.camel.updater;
 
 import info.sholes.camel.updater.DownloadHelper.Downloadable;
 import info.sholes.camel.updater.DownloadHelper.RomDescriptor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.List;
 import java.util.Properties;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Updater extends Activity {
 	private DownloadHelper dh = null;
 	private File update_zip = null;
 	private File flash_image = null;
 	private File recovery_image = null;
 	private RomDescriptor selected_rom = null;
 	private File rom_tgz = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 			public void uncaughtException(Thread thread, Throwable ex) {
 				addText("Whoa! Uncaught exception!");
 				showException(ex);
 			}
 		});
 		setContentView(R.layout.main);
 
 		try {
 			Properties p = new Properties();
 			p.load(new FileInputStream("/system/build.prop"));
 			addText("Current ROM: " + p.getProperty("ro.build.display.id"));
 
 			dh = new DownloadHelper(this);
 
 			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
 			addText("Version: " + pi.versionCode + " (" + pi.versionName + ")");
 
			if("Droid".equals(p.getProperty("ro.product.model"))) {
 				addText("Manufacturer: " + p.getProperty("ro.product.manufacturer"));
 				addText("Device: " + p.getProperty("ro.product.device"));
 				addText("Brand: " + p.getProperty("ro.product.brand"));
				addText("Model: " + p.getProperty("ro.product.model"));
 				addText(getString(R.string.droid_only));
 				return;
 			}
 
 			if(dh.checkVersion(pi))
 				return;
 		} catch(Exception e) {
 			showException(e);
 			return;
 		}
 
 		update_zip = new File("/sdcard/update.zip");
 		String tmp = getDir("tmp", MODE_WORLD_READABLE).getAbsolutePath();
 		flash_image = new File(tmp + "/flash_image");
 		recovery_image = new File(tmp + "/recovery.img");
 
 		checkRoot();
 	}
 
 	private void checkRoot() {
 		if(!new File("/system/bin/su").exists()) {
 			notRooted();
 			return;
 		}
 
 		// SU exists, check if it works
 		Toast t = Toast.makeText(this, "Check the remember box and allow", Toast.LENGTH_LONG);
 		t.show();
 		try {
 			if(SuperUser.isRemembered(this)) {
 				t.cancel();
 				rootVerified();
 				return;
 			}
 		} catch (Exception e) {
 			// Whoa! What happened?
 			showException(e);
 			return;
 		}
 
 		// Didn't work - tell them to check the remember box
 		new AlertDialog.Builder(this)
 		.setMessage("Looks like your phone is rooted, but you haven't given SMUpdater access yet. You *must* check the remember box!")
 		.setPositiveButton(
 				"Check again",
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 						checkRoot();
 					}
 				}
 		)
 		.show();
 	}
 
 	private void notRooted() {
 		new AlertDialog.Builder(this)
 		.setMessage(R.string.not_rooted)
 		.setCancelable(false)
 		.setPositiveButton(
 				R.string.not_rooted_pos,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dh.resetDownloadAttempts();
 						doRoot();
 					}
 				}
 		)
 		.setNegativeButton(
 				R.string.not_rooted_neg,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						System.exit(1);
 					}
 				}
 		)
 		.show();
 	}
 
 	private void rootVerified() {
 		dh.resetDownloadAttempts();
 		doFlashImageDownload();
 	}
 
 	public void callback(Callback c) {
 		switch(c) {
 		case ROOT:
 			doRoot();
 			return;
 		case FLASH_IMAGE_DOWNLOAD:
 			doFlashImageDownload();
 			return;
 		case RECOVERY_IMAGE_DOWNLOAD:
 			doRecoveryImageDownload();
 			return;
 		case ROM_DOWNLOAD:
 			doRomDownload();
 			return;
 		default:
 			addText("Unknown callback: " + c.name());
 			return;
 		}
 	}
 
 	private void doRoot() {
 		try {
 			File f = dh.downloadFile(Downloadable.ROOT, update_zip, Callback.ROOT);
 			if(f == null) {
 				// Wait for the callback
 				return;
 			}
 		} catch(Exception e) {
 			showException(e);
 			return;
 		}
 
 		addText(getString(R.string.exploit_ready));
 		// Display a pop-up explaining how to root
 		new AlertDialog.Builder(this)
 		.setMessage(R.string.reboot_recovery)
 		.setCancelable(false)
 		.show();
 	}
 
 	private void doFlashImageDownload() {
 		try {
 			File f = dh.downloadFile(Downloadable.FLASH_IMAGE, flash_image, Callback.FLASH_IMAGE_DOWNLOAD);
 			if(f == null) {
 				// Wait for the callback
 				return;
 			}
 
 			Runtime.getRuntime().exec("chmod 755 " + flash_image.getAbsolutePath());
 		} catch(Exception e) {
 			showException(e);
 			return;
 		}
 
 		dh.resetDownloadAttempts();
 		doRecoveryImageDownload();
 	}
 
 	private void doRecoveryImageDownload() {
 		try {
 			File f = dh.downloadFile(Downloadable.RECOVERY_IMAGE, recovery_image, Callback.RECOVERY_IMAGE_DOWNLOAD);
 			if(f == null) {
 				// Wait for the callback
 				return;
 			}
 		} catch(Exception e) {
 			showException(e);
 			return;
 		}
 
 		doFlashRecovery();
 	}
 
 	private void doFlashRecovery() {
 		// Recovery image downloaded, flash it if needed
 
 		try {
 			// Calculate md5 of the recovery block, mtdblock3
 			int length = (int)recovery_image.length();
 			// TODO: validate length <= the block size
 			String command = "dd if=/dev/block/mtdblock3 count=1 bs=" + length;
 			String current_md5 = SuperUser.oneShotMd5(command, length);
 			String expected_md5 = Downloadable.RECOVERY_IMAGE.getMd5();
 
 			if(expected_md5.equals(current_md5)) {
 				dh.resetDownloadAttempts();
 				showRomMenu();
 				return;
 			} else {
 				addText(command + " = " + current_md5);
 				new AlertDialog.Builder(this)
 				.setMessage(R.string.confirm_flash_recovery)
 				.setCancelable(false)
 				.setPositiveButton(
 						R.string.confirm_flash_recovery_yes,
 						new OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								confirmFlashRecovery();
 							}
 						}
 				)
 				.setNegativeButton(
 						R.string.confirm_flash_recovery_no,
 						new OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								System.exit(1);
 							}
 						}
 				)
 				.show();
 				return;
 			}
 		} catch (Exception e) {
 			showException(e);
 			return;
 		}
 	}
 
 	private void confirmFlashRecovery() {
 		// Double-check that they are sure
 		new AlertDialog.Builder(this)
 		.setMessage(R.string.confirm_flash_recovery_2)
 		.setCancelable(false)
 		.setPositiveButton(
 				R.string.confirm_flash_recovery_2_no,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						System.exit(1);
 					}
 				}
 		)
 		.setNegativeButton(
 				R.string.confirm_flash_recovery_2_yes,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						confirmedFlashRecovery();
 					}
 				}
 		)
 		.show();
 	}
 
 	private void confirmedFlashRecovery() {
 		// They really want this to happen
 
 		final String command = flash_image.getAbsolutePath() + " recovery " + recovery_image.getAbsolutePath();
 
 		ProgressDialog pd = new ProgressDialog(this);
 		pd.setCancelable(false);
 		pd.setMessage(command);
 		pd.setIndeterminate(true);
 		pd.show();
 
 		try {
 			SuperUser.oneShot(command);
 		} catch (Exception e) {
 			showException(e);
 			pd.hide();
 			return;
 		}
 
 		pd.hide();
 		// Go back and check the MD5
 		doFlashRecovery();
 	}
 
 	private void showRomMenu() {
 		final List<RomDescriptor> roms = dh.getRoms();
 
 		if(roms.size() == 0) {
 			addText(getString(R.string.rom_menu_no_roms));
 		} else if(roms.size() > 1) {
 			String[] romNames = new String[roms.size()];
 			for(int i = 0; i < romNames.length; i++)
 				romNames[i] = roms.get(i).name;
 			selected_rom = roms.get(roms.size()-1);
 
 			new AlertDialog.Builder(this)
 			.setTitle(R.string.rom_menu)
 			.setCancelable(false)
 			.setSingleChoiceItems(
 					romNames,
 					roms.indexOf(selected_rom),
 					new OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							selected_rom = roms.get(which);
 						}
 					}
 			)
 			.setPositiveButton(
 					R.string.rom_menu_pos,
 					new OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 							if(selected_rom != null)
 								selectRom(selected_rom);
 							else
 								showRomMenu();
 						}
 					}
 			)
 			.setNegativeButton(
 					R.string.rom_menu_neg,
 					new OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							System.exit(1);
 						}
 					}
 			)
 			.show();
 		} else {
 			selectRom(roms.get(0));
 		}
 	}
 
 	private void selectRom(RomDescriptor rom) {
 		selected_rom = rom;
 		String url = rom.url;
 		rom_tgz = new File("/sdcard/" + url.substring(url.lastIndexOf('/')+1));
 
 		String tgz = rom_tgz.getAbsolutePath();
 		final String rom_tar = tgz.substring(0, tgz.length() - 3) + "tar";
 		if(!rom_tgz.exists() && new File(rom_tar).exists()) {
 			new AlertDialog.Builder(this)
 			.setMessage("Found " + rom_tar + "; we probably shouldnt download the tgz again")
 			.setCancelable(false)
 			.setPositiveButton(
 					R.string.select_rom_download_tgz,
 					new OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							doRomDownload();
 						}
 					}
 			)
 			.setNeutralButton(
 					R.string.select_rom_flash_tar,
 					new OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							doRomInstall(new File(rom_tar));
 						}
 					}
 			)
 			.setNegativeButton(
 					R.string.select_rom_neg,
 					new OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							System.exit(1);
 						}
 					}
 			)
 			.show();
 			return;
 		}
 
 		doRomDownload();
 	}
 
 	private void doRomDownload() {
 		try {
 			File f = dh.downloadRom(selected_rom, rom_tgz);
 			if(f == null) {
 				// Wait for the callback
 				return;
 			}
 
 			doRomInstall(f);
 		} catch(Exception e) {
 			showException(e);
 			return;
 		}
 	}
 
 	private void doRomInstall(final File rom) {
 		// ROM is downloaded, ready to ask user about options
 		new AlertDialog.Builder(this)
 		.setMessage(R.string.confirm_flash_rom)
 		.setCancelable(false)
 		.setPositiveButton(
 				R.string.confirm_flash_rom_yes,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						confirmedRomInstall(rom);
 					}
 				}
 		)
 		.setNegativeButton(
 				R.string.confirm_flash_rom_no,
 				new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						System.exit(1);
 					}
 				}
 		)
 		.show();
 	}
 
 	private void confirmedRomInstall(File rom) {
 		try {
 			switch(selected_rom.type) {
 			case ROM_TGZ:
 				SuperUser.oneShot("echo -n \"--install_tgz=" + rom.getAbsolutePath() + "\" > /cache/recovery/command");
 				break;
 			case UPDATE_ZIP:
 				String path = rom.getAbsolutePath();
 				if(!path.startsWith("/sdcard/"))
 					throw new Exception("Roms go on /sdcard...what happened? " + path);
 				path = "SDCARD:" + path.substring(8);
 				// SPRecovery blocks update.zip via --update_pacakge, --allow_update_package let it know we're not Google
 				SuperUser.oneShot("echo \"--allow_update_package\" > /cache/recovery/command");
 				SuperUser.oneShot("echo -n \"--update_package=" + path + "\" >> /cache/recovery/command");
 				break;
 			default:
 				throw new Exception("Unknown rom type: " + selected_rom.type.name());
 			}
 			SuperUser.oneShot("/system/bin/reboot recovery");
 		} catch(Exception e) {
 			showException(e);
 		}
 	}
 
 	protected void showException(Throwable ex) {
 		ex.printStackTrace();
 		StringWriter sw = new StringWriter();
 		ex.printStackTrace(new PrintWriter(sw));
 		addText(sw.toString());
 	}
 
 	protected void addText(String text) {
 		TextView tvText = new TextView(this);
 		tvText.setText(text);
 		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout01);
 		ll.addView(tvText);
 	}
 }
