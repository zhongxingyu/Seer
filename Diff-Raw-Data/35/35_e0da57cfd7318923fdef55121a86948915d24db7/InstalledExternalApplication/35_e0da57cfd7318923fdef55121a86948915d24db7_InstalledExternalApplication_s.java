 package de.da_sense.moses.client.abstraction.apks;
 
 import de.da_sense.moses.client.util.Log;
 
 /**
  * References an installed application (additional to ExternalApplication, the
  * package name of the installed application must be specified)
  * 
  * @author Simon L, Wladimir Schmidt
  * 
  */
 public class InstalledExternalApplication extends ExternalApplication {
 
 	public static final String SEPARATOR = "#IEA#";
 	private String packageName;
 	private boolean wasInstalledAsUserStudy;
 	private boolean updateAvailable;
 	private String installedVersion;
 
 	/**
 	 * Creates the reference to the external application by specifying the
 	 * package name and version number
 	 * 
 	 * @param packageName
 	 *            the name of the package of the application
 	 * @param ID
 	 *            the moses id of the application
 	 * @param wasInstalledAsUserStudy
 	 * @param version
 	 *            the version of the app which is installed
 	 */
 	private InstalledExternalApplication(String packageName, String ID,
 			boolean wasInstalledAsUserStudy, String version) {
 		super(Integer.valueOf(ID));
 		Log.d("InstalledExternalApp", "Here4");
 		
 		// assume this version as the newest version
 		String newestVersion = version;
 		super.setNewestVersion(newestVersion);
 		this.installedVersion = version;
 
 		this.wasInstalledAsUserStudy = wasInstalledAsUserStudy;
 		this.packageName = packageName;
 
 		this.updateAvailable = false;
 	}
 
 	/**
 	 * Creates the instance by adapting an already existing ExternalApplication
 	 * reference. This has the advantage of copying already retrieved name and
 	 * description over.
 	 * 
 	 * @param packageName
 	 *            the package name of the installed app
 	 * @param externalApp
 	 *            the preexisting reference that will be adapted
 	 * @param wasInstalledAsUserStudy
 	 * @param version
 	 *            the version of the app which is installed
 	 */
 	private InstalledExternalApplication(String packageName,
 			ExternalApplication externalApp, boolean wasInstalledAsUserStudy,
 			String version) {
 
 		this(packageName, externalApp.getID(), wasInstalledAsUserStudy, version);
 		Log.d("InstalledExternalApp", "Here3");
 		
 		if (externalApp.isDescriptionSet()) {
 			setDescription(externalApp.getDescription());
 		}
 		if (externalApp.isNameSet()) {
 			setName(externalApp.getName());
 		}
 
 		if (externalApp.isNewestVersionSet()) {
 			super.setNewestVersion(externalApp.getNewestVersion());
 		} else {
 			// assume this version as the newest version
 			String newestVersion = version;
 			super.setNewestVersion(newestVersion);
 		}
 		if(externalApp.isStartDateSet()){
             setStartDate(externalApp.getStartDate());
         }
 		if(externalApp.isEndDateSet()){
 		    setEndDate(externalApp.getEndDate());
 		}
 		if(externalApp.isApkVersionSet()){
 		    setApkVersion(externalApp.getApkVersion());
 		}
 	}
 
 	/**
 	 * Creates the instance by adapting an already existing ExternalApplication
 	 * reference. This has the advantage of copying already retrieved name and
 	 * description over. The external application object must have its
 	 * installed version set!
 	 * 
 	 * @param packageName
 	 *            the package name of the installed app
 	 * @param externalApp
 	 *            the preexisting reference that will be adapted
 	 * @param wasInstalledAsUserStudy
 	 */
 	public InstalledExternalApplication(String packageName,
 			ExternalApplication externalApp, boolean wasInstalledAsUserStudy) {
 
 		this(packageName, externalApp, wasInstalledAsUserStudy, externalApp
 				.getNewestVersion());
 	}
 
 	/**
 	 * Creates the instance by adapting an already existing ExternalApplication
 	 * reference. This has the advantage of copying already retrieved name and
 	 * description over.
 	 * 
 	 * 
 	 * @param packageName
 	 * @param exApp
 	 * @param wasInstalledAsUserStudy
 	 * @param version
 	 * @param updateAvailable
 	 */
 	public InstalledExternalApplication(String packageName,
 			ExternalApplication exApp, boolean wasInstalledAsUserStudy, String version,
 			boolean updateAvailable) {
 		this(packageName, exApp, wasInstalledAsUserStudy, version);
 		setUpdateAvailable(updateAvailable);
 	}
 // TODO REMOVE THESE FOSSILS 
 //	/**
 //	 * 
 //	 * @param baseActivity
 //	 * @param o
 //	 */
 //	@Deprecated
 //	protected void fetchUpdatedInfo(final Activity baseActivity,
 //			final UpdateObserver o) {
 //		final ExternalApplicationInfoRetriever infoRequester = 
 //				new ExternalApplicationInfoRetriever(
 //				this.getID(), baseActivity);
 //		final ProgressDialog progressDialog = ProgressDialog.show(baseActivity,
 //				"Loading...", "Loading userstudy information", true, true,
 //				new OnCancelListener() {
 //					@Override
 //					public void onCancel(DialogInterface dialog) {
 //						infoRequester.cancel();
 //						o.manual_abort();
 //					}
 //				});
 //		infoRequester.sendEvenWhenNoNetwork = false;
 //		infoRequester.addObserver(new Observer() {
 //			@Override
 //			public void update(Observable observable, Object data) {
 //				if (infoRequester.getState() == State.DONE) {
 //					final ExternalApplication updatedApplication = new ExternalApplication(Integer.valueOf(InstalledExternalApplication.this.getID()));
 //					updatedApplication.setName(infoRequester.getResultName());
 //					updatedApplication.setDescription(infoRequester
 //							.getResultDescription());
 //					updatedApplication.setApkVersion(infoRequester
 //							.getResultApkVersion());
 //					updatedApplication.setEndDate(infoRequester
 //							.getResultEndDate());
 //					updatedApplication.setStartDate(infoRequester
 //							.getResultStartDate());
 //					progressDialog.dismiss();
 //					WelcomeActivity.getInstance()
 //					.showAvailableDetails(updatedApplication,
 //							baseActivity, new Runnable() {
 //								@Override
 //								public void run() {
 //									startInstallUpdate(updatedApplication,
 //											baseActivity, o);
 //								}
 //							}, new Runnable() {
 //								@Override
 //								public void run() {
 //									o.manual_abort();
 //								}
 //							});
 //				}
 //				if (infoRequester.getState() == State.ERROR) {
 //					Log.e("MoSeS.USERSTUDY",
 //							"Wanted to display user study, but couldn't get app informations because of: ",
 //							infoRequester.getException());
 //					progressDialog.dismiss();
 //					showMessageBoxError(baseActivity, "Error",
 //							"Error when retrieving update information.",
 //							errorMessageBoxOkayBtnListener(o));
 //				}
 //				if (infoRequester.getState() == State.NO_NETWORK) {
 //					Log.d("MoSeS.USERSTUDY",
 //							"Wanted to display user study, but couldn't get app informations because of: ",
 //							infoRequester.getException());
 //					progressDialog.dismiss();
 //					showMessageBoxErrorNoConnection(baseActivity, o);
 //				}
 //			}
 //		});
 //		infoRequester.start();
 //	}
 	
 
 //	private int totalSize = -1;
 //
 //	/**
 //	 * 
 //	 * @param updatedApplication
 //	 * @param baseActivity
 //	 * @param o
 //	 */
 //	private void startInstallUpdate(
 //			final ExternalApplication updatedApplication,
 //			final Activity baseActivity, final UpdateObserver o) {
 //		final ProgressDialog progressDialog = new ProgressDialog(baseActivity);
 //		final ApkDownloadManager downloader = new ApkDownloadManager(this,
 //				baseActivity, new ExecutableForObject() {
 //
 //					@Override
 //					public void execute(final Object o) {
 //						if (o instanceof Integer) {
 //							baseActivity.runOnUiThread(new Runnable() {
 //
 //								@Override
 //								public void run() {
 //									if (totalSize == -1) {
 //										totalSize = (Integer) o / 1024;
 //										progressDialog.setMax(totalSize);
 //									} else {
 //										progressDialog
 //												.incrementProgressBy(((Integer) o / 1024)
 //														- progressDialog
 //																.getProgress());
 //									}
 //								}
 //							});
 //
 //						}
 //					}
 //				});
 //
 //		progressDialog.setTitle(baseActivity.getApplicationContext().getString(R.string.downloadingApp));
 //		progressDialog.setMessage(baseActivity.getApplicationContext().getString(R.string.pleaseWait));
 //		progressDialog.setMax(0);
 //		progressDialog.setProgress(0);
 //		progressDialog.setOnCancelListener(new OnCancelListener() {
 //
 //			@Override
 //			public void onCancel(DialogInterface dialog) {
 //				downloader.cancel();
 //			}
 //		});
 //		progressDialog.setCancelable(true);
 //		progressDialog.setButton(baseActivity.getApplicationContext().getString(R.string.cancel), (DialogInterface.OnClickListener) null);
 ////		progressDialog.setButton(baseActivity.getApplicationContext().getString(R.string.cancel),
 ////				new DialogInterface.OnClickListener() {
 ////					@Override
 ////					public void onClick(DialogInterface dialog, int which) {
 ////						if (progressDialog.isShowing())
 ////							progressDialog.cancel();
 ////					}
 ////				});
 //		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 //		Observer observer = new Observer() {
 //			@Override
 //			public void update(Observable observable, Object data) {
 //				if (downloader.getState() == ApkDownloadManager.State.ERROR_NO_CONNECTION) {
 //					progressDialog.dismiss();
 //					showMessageBoxErrorNoConnection(baseActivity, o);
 //				} else if (downloader.getState() == ApkDownloadManager.State.ERROR) {
 //					progressDialog.dismiss();
 //					showMessageBoxErrorDownloading(downloader, baseActivity, o);
 //				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
 //					progressDialog.dismiss();
 //					installDownloadedApk(downloader.getDownloadedApk(),
 //							updatedApplication, baseActivity, o);
 //				}
 //			}
 //		};
 //		downloader.addObserver(observer);
 //		totalSize = -1;
 //		progressDialog.show();
 //		downloader.start();
 //	}
 
 	// /-----------------------------
 
 //	/**
 //	 * TODO Verify:
 //	 * Installs a downloaded APK
 //	 * @param result The downloaded APK
 //	 * @param updatedApplication The Instance of External Application for this APK
 //	 * @param baseActivity
 //	 * @param o Implementation of UpdateObserver
 //	 */
 //	private void installDownloadedApk(final File result,
 //			final ExternalApplication updatedApplication,
 //			final Activity baseActivity, final UpdateObserver o) {
 //		final ApkInstallManager installer = new ApkInstallManager(result, this, baseActivity.getApplicationContext());
 //		installer.addObserver(new Observer() {
 //			@Override
 //			public void update(Observable observable, Object data) {
 //				
 //				Context context = baseActivity.getApplicationContext();
 //				
 //				if (installer.getState() == ApkInstallManager.State.ERROR) {
 //					showMessageBoxError(
 //							baseActivity,
 //							context.getString(R.string.error),
 //							context.getString(R.string.userStudy_errorMessage2),
 //							errorMessageBoxOkayBtnListener(o));
 //				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_CANCELLED) {
 //					o.manual_abort();
 //				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_COMPLETED) {
 //					try {
 //						o.success(ApkInstallManager.registerInstalledApk(
 //								result, updatedApplication, baseActivity,
 //								InstalledExternalApplication.this
 //										.wasInstalledAsUserStudy()));
 //					} catch (IOException e) {
 //						Log.e("MoSeS.Install",
 //								"Problems with extracting package name from apk, or problems with the InstalledExternalApplicationsManager after installing an app");
 //						showMessageBoxError(
 //								baseActivity,
 //								context.getString(R.string.error),
 //								context.getString(R.string.userStudy_errorMessage_saveDatabase),
 //								errorMessageBoxOkayBtnListener(o));
 //					}
 //				}
 //			}
 //		});
 //		installer.start();
 //	}
 
 //	/**
 //	 * Show an Alert Dialog which informs the user about a missing 
 //	 * internet connection.
 //	 * @param baseActivity the base activity for the dialog
 //	 * @param o An implementation of UpdateObserver
 //	 */
 //	private void showMessageBoxErrorNoConnection(Activity baseActivity,
 //			UpdateObserver o) {
 //		showMessageBoxError(
 //				baseActivity,
 //				baseActivity.getApplicationContext().getString(R.string.no_internet_connection),
 //				baseActivity.getApplicationContext().getString(R.string.noInternetConnection_message),
 //				errorMessageBoxOkayBtnListener(o));
 //	}
 //
 //	/**
 //	 * Show an Alert Dialog which informs the user about an error during 
 //	 * the app download.
 //	 * @param downloader
 //	 * @param baseActivity the base activity for the dialog
 //	 * @param o
 //	 */
 //	private void showMessageBoxErrorDownloading(
 //			ApkDownloadManager downloader, Activity baseActivity,
 //			UpdateObserver o) {
 //		showMessageBoxError(baseActivity, baseActivity.getApplicationContext().getString(R.string.error),
 //				baseActivity.getApplicationContext().getString(R.string.downloadApk_errorMessage, downloader.getErrorMsg()),
 //				errorMessageBoxOkayBtnListener(o));
 //	}
 //
 //	/**
 //	 * General method for showing an alert dialog to the user.
 //	 * @param baseActivity the base activity for the dialog
 //	 * @param title the title for the alert dialog
 //	 * @param msg the message to show in the dialog
 //	 * @param onClickListener the onClickListener for the button
 //	 */
 //	private void showMessageBoxError(Activity baseActivity, String title,
 //			String msg, DialogInterface.OnClickListener onClickListener) {
 //		new AlertDialog.Builder(baseActivity).setMessage(msg).setTitle(title)
 //				.setCancelable(true).setNeutralButton(baseActivity.getApplicationContext().getString(R.string.ok), onClickListener)
 //				.show();
 //	}
 //
 //	/**
 //	 * TODO: Verify if comment is valid
 //	 * Creates an DialogInterface.OnClickListener for the Implementation of UpdateObserver
 //	 * which only uses the unsuccessful_exit method. Use only as an OnClickListener for
 //	 * Errormessages.
 //	 * @param o Implementation of UpdateObserver
 //	 * @return
 //	 */
 //	private DialogInterface.OnClickListener errorMessageBoxOkayBtnListener(
 //			final UpdateObserver o) {
 //		return new DialogInterface.OnClickListener() {
 //			@Override
 //			public void onClick(DialogInterface dialog, int whichButton) {
 //				o.unsuccessful_exit();
 //			}
 //		};
 //	}
 
 	/**
 	 * @return the package name of the referenced application
 	 */
 	public String getPackageName() {
 		return packageName;
 	}
 //
 //	private boolean wasInstalledAsUserStudy() {
 //		return wasInstalledAsUserStudy;
 //	}
 
 	/**
 	 * @return the version of the app which was installed
 	 */
 	public String getInstalledVersion() {
 		return installedVersion;
 	}
 
 	/**
 	 * @param installedVersion
 	 *            sets the installed version
 	 */
 	public void setInstalledVersion(String installedVersion) {
 		this.installedVersion = installedVersion;
 	}
 
 	/**
 	 * @see de.da_sense.moses.client.abstraction.apks.ExternalApplication#toString()
 	 */
 	@Override
 	public String toString() {
 		return packageName;
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof InstalledExternalApplication) {
 			return (this.getID().equals(((InstalledExternalApplication) o).getID()));
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * @see de.da_sense.moses.client.abstraction.apks.ExternalApplication#asOnelineString()
 	 */
 	@Override
 	public String asOnelineString() {
 		String oneLine = super.asOnelineString() + SEPARATOR + this.getPackageName()
 				+ SEPARATOR
 				+ Boolean.valueOf(wasInstalledAsUserStudy).toString()
 				+ SEPARATOR + installedVersion + SEPARATOR
 				+ Boolean.valueOf(updateAvailable).toString();
 		Log.d("IEA", "returning one line String: " + oneLine);
 		return oneLine;
 	}
 
 	/**
 	 * gets updateAvailable
 	 * @return Boolean updateAvailable
 	 */
 	public boolean isUpdateAvailable() {
 		return updateAvailable;
 	}
 
 	/**
 	 * Sets updateAvailable
 	 * @param updateAvailable
 	 */
 	public void setUpdateAvailable(boolean updateAvailable) {
 		this.updateAvailable = updateAvailable;
 	}
 	
 	/**
 	 * Return boolean to check if an update is available.
 	 * @return true if update is available
 	 */
 	public boolean getUpdateAvailable() {
 		return updateAvailable;
 	}
 
 	/**
 	 * @see de.da_sense.moses.client.abstraction.apks.ExternalApplication#isDataComplete()
 	 */
 	@Override
 	public boolean isDataComplete() {
 		return super.isDataComplete();
 	}
 
 }
