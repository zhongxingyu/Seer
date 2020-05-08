 package tracker.torrents;
 
 import tracker.cache.TorrentStatsCache;
 import tracker.util.FileUtils;
 
 import java.sql.Timestamp;
 
 public class TorrentData {
 	protected Long accountId;
 	protected Long torrentId;
	protected Long uploaded = 0L;
	protected Long downloaded = 0L;
	protected Long left = 0L;
 	protected Long uploadedSession;
 	protected Long downloadedSession;
 	protected Long leftSession;
 	protected Timestamp lastUpdate;
 
 	protected String title;
 	protected Timestamp creationDate;
 	protected byte[] infoHash;
 	protected Long size;
 
 	protected volatile TorrentStats torrentStats;
 
 	public Long getAccountId() { return this.accountId; }
 	public void setAccountId(Long accountId) { this.accountId = accountId; }
 
 	public Long getTorrentId() { return this.torrentId; }
 	public void setTorrentId(Long torrentId) throws Throwable {
 		this.torrentId = torrentId;
 	}
 
 	public Long getUploaded() { return this.uploaded; }
 	public void setUploaded(Long uploaded) { this.uploaded = uploaded; }
 
 	public Long getDownloaded() { return this.downloaded; }
 	public void setDownloaded(Long downloaded) { this.downloaded = downloaded; }
 
 	public Long getLeft() { return this.left; }
 	public void setLeft(Long left) { this.left = left; }
 
 	public Long getUploadedSession() { return this.uploadedSession; }
 	public void setUploadedSession(Long uploadedSession) { this.uploadedSession = uploadedSession; }
 
 	public Long getDownloadedSession() { return this.downloadedSession; }
 	public void setDownloadedSession(Long downloadedSession) { this.downloadedSession = downloadedSession; }
 
 	public Long getLeftSession() { return this.leftSession; }
 	public void setLeftSession(Long leftSession) { this.leftSession = leftSession; }
 
 	public Timestamp getLastUpdate() { return this.lastUpdate; }
 	public void setLastUpdate(Timestamp lastUpdate) { this.lastUpdate = lastUpdate; }
 
 	public String getTitle() { return this.title; }
 	public void setTitle(String title) { this.title = title; }
 
 	public Timestamp getCreationDate() { return creationDate; }
 	public void setCreationDate(Timestamp creationDate) { this.creationDate = creationDate; }
 
 	public byte[] getInfoHash() { return this.infoHash; }
 	public void setInfoHash(byte[] infoHash) { this.infoHash = infoHash; }
 
 	public Long getSize() { return this.size; }
 	public void setSize(Long size) { this.size = size; }
 
 	public Long getTotalUploaded() { return this.getUploaded() + this.getUploadedSession(); }
 	public Long getTotalDownloaded() { return this.getDownloaded() + this.getDownloadedSession(); }
 
 	public String getTotalUploadedStr() { return FileUtils.getSizeString(this.getTotalUploaded()); }
 	public String getTotalDownloadedStr() { return FileUtils.getSizeString(this.getTotalDownloaded()); }
 
 	public Integer getCompletedPercent() {
 		if (this.getLeftSession() == 0) {
 			return 100;
 		}
 
 		Float completedPercent = (1 - this.getLeftSession().floatValue() / this.getSize().floatValue()) * 100;
 
 		return completedPercent.intValue();
 	}
 
 	public String getUploadRatio() {
 		Float ratio = 0f;
 
 		if (this.getTotalUploaded() != 0) {
 			ratio = this.getTotalUploaded().floatValue() / this.getTotalDownloaded().floatValue();
 		}
 
 		return String.format("%.2f", ratio);
 	}
 
 	public TorrentStats getTorrentStats() throws Throwable {
 		if (this.torrentStats == null) {
 			synchronized (this) {
 				if (this.torrentStats == null) {
 					this.torrentStats = TorrentStatsCache.getInstance().getByTorrentId(this.getTorrentId());
 				}
 			}
 		}
 		return this.torrentStats;
 	}
 
 	public Integer getSeeds() throws Throwable { return this.getTorrentStats().getSeeds(); }
 	public Integer getPeers() throws Throwable { return this.getTorrentStats().getPeers(); }
 	public Integer getCompleted() throws Throwable { return this.getTorrentStats().getCompleted(); }
 }
