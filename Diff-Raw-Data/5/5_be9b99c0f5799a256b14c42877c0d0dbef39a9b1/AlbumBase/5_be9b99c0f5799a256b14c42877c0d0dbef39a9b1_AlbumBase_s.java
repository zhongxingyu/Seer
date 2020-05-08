 package com.github.jkschoen.jsma.model;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 
 
 @XmlAccessorType(XmlAccessType.FIELD)
 public abstract class AlbumBase {
 	
 	@XmlElement(name="id")
 	private Integer id;
 	
 	@XmlElement(name="AlbumTemplateID")
 	private Integer albumTemplateID; 
 	
 	@XmlElement(name="BackPrinting")
 	private String backPrinting;
 	
 	@XmlElement(name="BoutiquePackaging")
 	private Integer boutiquePackaging;
 	
 	@XmlElement(name="CanRank")
 	private Boolean canRank;
 
 	@XmlElement(name="Clean")
 	private Boolean clean;
 	
 	@XmlElement(name="ColorCorrection")
 	private Integer colorCorrection;
 	
 	@XmlElement(name="Comments")
 	private Boolean comments;
 	
 	@XmlElement(name="Community")
 	private Community community;
 	
 	@XmlElement(name="Exif")
 	private Boolean exif;
 	
 	@XmlElement(name="External")
 	private Boolean external;
 	
 	@XmlElement(name="FamilyEdit")
 	private Boolean familyEdit;
 	
 	@XmlElement(name="Filenames")
 	private Boolean filenames;
 	
 	@XmlElement(name="FriendEdit")
 	private Boolean friendEdit;
 	
 	@XmlElement(name="Geography")
 	private Boolean geography;
 	
 	@XmlElement(name="GuestUploadURL")
 	private String guestUploadURL;
 
 	@XmlElement(name="Header")
 	private Boolean header;
 	
 	@XmlElement(name="HideOwner")
 	private Boolean hideOwner;
 	
 	@XmlElement(name="InterceptShipping")
 	private Integer interceptShipping;
 	
 	@XmlElement(name="Larges")
 	private Boolean larges;
 	
 	@XmlElement(name="Originals")
 	private Boolean originals;
 	
 	@XmlElement(name="PackingBranding")
 	private Boolean packagingBranding;
 	
 	@XmlElement(name="Password")
 	private String password;
 	
 	@XmlElement(name="PasswordHint")
 	private String passwordHint;
 	
 	@XmlElement(name="Position")
 	private Integer position;
 	
 	@XmlElement(name="Printable")
 	private Boolean printable;
 	
 	@XmlElement(name="Printmark")
 	private Printmark printmark;
 
 	@XmlElement(name="ProofDays")
 	private Integer proofDays;
 	
	@XmlElement(name="ProtectedRightClick")
 	private Boolean protectedRightClick;
 	
	@XmlElement(name="PublicAlbum")
 	private Boolean publicAlbum;
 	
 	@XmlElement(name="Share")
 	private Boolean share;
 	
 	@XmlElement(name="SmugSearchable")
 	private Boolean smugSearchable;
 	
 	@XmlElement(name="SortDirection")
 	private Boolean sortDirection;
 	
 	@XmlElement(name="SortMethod")
 	private String sortMethod;
 	
 	@XmlElement(name="SquareThumbds")
 	private Boolean squareThumbs;
 	
 	@XmlElement(name="TemplateID")
 	private Integer template;
 	
 	@XmlElement(name="UnsharpAmount")
 	private Float unsharpAmount;
 
 	@XmlElement(name="UnsharpRadius")
 	private Float unsharpRadius;
 	
 	@XmlElement(name="UnsharpSigma")
 	private Float unsharpSigma;
 	
 	@XmlElement(name="UnsharpThreshold")
 	private Float unsharpThreshold;
 	
 	@XmlElement(name="UploadKey")
 	private String uploadKey;
 	
 	@XmlElement(name="Watermark")
 	private Watermark watermark;
 	
 	@XmlElement(name="Watermarking")
 	private Boolean watermarking;
 	
 	@XmlElement(name="WorldSearchable")
 	private Boolean worldSearchable;
 	
 	@XmlElement(name="X2Larges")
 	private Boolean x2Larges;
 	
 	@XmlElement(name="X3Larges")
 	private Boolean x3Larges;
 	
 	@XmlElement(name="XLarges")
 	private Boolean xLarges;
 	
 	/***
 	 * The id for this album.
 	 */
 	public Integer getId() {
 		return id;
 	}
 
 	/***
 	 * Set the id for this album.
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 	public Integer getAlbumTemplateID() {
 		return albumTemplateID;
 	}
 
 	public void setAlbumTemplateID(Integer albumTemplateID) {
 		this.albumTemplateID = albumTemplateID;
 	}
 
 	/**
 	 * The text to be printed on the back of prints purchased from this album.
 	 * (owner, pro accounts only)
 	 */
 	public String getBackPrinting() {
 		return backPrinting;
 	}
 
 	/**
 	 * Sets the text to be printed on the back of prints purchased from this album.
 	 * (owner, pro accounts only)
 	 */
 	public void setBackPrinting(String backPrinting) {
 		this.backPrinting = backPrinting;
 	}
 
 	/**
 	 * Enable boutique packaging for orders from this album.
      * (owner, pro accounts only)
      * Values:
      *   0 - No
      *   1 - Yes
      *   2 - Inherit
 	 */
 	public Integer getBoutiquePackaging() {
 		return boutiquePackaging;
 	}
 
 	/**
 	 * Sets whether to enable boutique packaging for orders from this album.
      * (owner, pro accounts only)
      * Values:
      *   0 - No
      *   1 - Yes
      *   2 - Inherit
 	 */
 	public void setBoutiquePackaging(Integer boutiquePackaging) {
 		this.boutiquePackaging = boutiquePackaging;
 	}
 
 	/**
 	 * Allow images from this album to be ranked using PhotoRank.
 	 */
 	public Boolean getCanRank() {
 		return canRank;
 	}
 
 	/**
 	 * Sets whether to allow images from this album to be ranked using PhotoRank.
 	 */
 	public void setCanRank(Boolean canRank) {
 		this.canRank = canRank;
 	}
 
 	/**
 	 * Hide the Description and LastUpdated for this album on the homepage and category pages.
 	 */
 	public Boolean getClean() {
 		return clean;
 	}
 
 	/**
 	 * Sets whether to hide the Description and LastUpdated for this album on the homepage and category pages.
 	 */
 	public void setClean(Boolean clean) {
 		this.clean = clean;
 	}
 
 	/**
 	 * The color correction setting for this album.
 	 * (owner, pro accounts only)
      * Values:
      *   0 - No
      *   1 - Yes
      *   2 - Inherit
 	 */
 	public Integer getColorCorrection() {
 		return colorCorrection;
 	}
 
 	/**
 	 * Sets the color correction setting for this album.
 	 * (owner, pro accounts only)
      * Values:
      *   0 - No
      *   1 - Yes
      *   2 - Inherit
 	 */
 	public void setColorCorrection(Integer colorCorrection) {
 		this.colorCorrection = colorCorrection;
 	}
 
 	/**
 	 * Allow visitors to leave comments on this album.
 	 */
 	public Boolean getComments() {
 		return comments;
 	}
 
 	/**
 	 * Sets whether to allow visitors to leave comments on this album.
 	 */
 	public void setComments(Boolean comments) {
 		this.comments = comments;
 	}
 
 	/**
 	 * The community that this album belongs to.
 	 */
 	public Community getCommunity() {
 		return community;
 	}
 
 	/**
 	 * Sets the community that this album belongs to.
 	 */
 	public void setCommunity(Community community) {
 		this.community = community;
 	}
 
 	/**
 	 * Allow EXIF data to be viewed for images from this album.
 	 */
 	public Boolean getExif() {
 		return exif;
 	}
 
 	/**
 	 * Sets whether to allow EXIF data to be viewed for images from this album.
 	 */
 	public void setExif(Boolean exif) {
 		this.exif = exif;
 	}
 
 	/**
 	 * Allow images from this album to be linked externally outside SmugMug.
 	 */
 	public Boolean getExternal() {
 		return external;
 	}
 
 	/**
 	 * Sets whether to allow images from this album to be linked externally outside SmugMug.
 	 */
 	public void setExternal(Boolean external) {
 		this.external = external;
 	}
 
 	/**
 	 * Allow family to edit the captions and keywords of the images in this album.
 	 */
 	public Boolean getFamilyEdit() {
 		return familyEdit;
 	}
 
 	/**
 	 * Sets whether to allow family to edit the captions and keywords of the images in this album.
 	 */
 	public void setFamilyEdit(Boolean familyEdit) {
 		this.familyEdit = familyEdit;
 	}
 
 	/**
 	 * Show filename for images uploaded with no caption to this album. 
 	 */
 	public Boolean getFilenames() {
 		return filenames;
 	}
 
 	/**
 	 * Sets whether to show filename for images uploaded with no caption to this album. 
 	 */
 	public void setFilenames(Boolean filenames) {
 		this.filenames = filenames;
 	}
 
 	/**
 	 * Allow friends to edit the captions and keywords of the images in this album.
 	 */
 	public Boolean getFriendEdit() {
 		return friendEdit;
 	}
 
 	/**
 	 * Sets whether to allow friends to edit the captions and keywords of the images in this album.
 	 */
 	public void setFriendEdit(Boolean friendEdit) {
 		this.friendEdit = friendEdit;
 	}
 
 	/**
 	 * Enable mapping features for this album.
 	 */
 	public Boolean getGeography() {
 		return geography;
 	}
 
 	/**
 	 * Sets whether to enable mapping features for this album.
 	 */
 	public void setGeography(Boolean geography) {
 		this.geography = geography;
 	}
 
 	/**
 	 * The URL to allow guests to upload to this gallery.
 	 */
 	public String getGuestUploadURL() {
 		return guestUploadURL;
 	}
 
 	/**
 	 * Set the URL to allow guests to upload to this gallery.
 	 */
 	public void setGuestUploadURL(String guestUploadURL) {
 		this.guestUploadURL = guestUploadURL;
 	}
 
 	/**
 	 * Default this album to the standard SmugMug appearance.
      *
      * Values:
      * false - Custom
      * true - SmugMug
 	 */
 	public Boolean getHeader() {
 		return header;
 	}
 
 	/**
 	 * Sets whether to default this album to the standard SmugMug appearance.
      *
      * Values:
      * false - Custom
      * true - SmugMug
 	 */
 	public void setHeader(Boolean header) {
 		this.header = header;
 	}
 
 	/**
 	 * Hide the owner of this album.
 	 */
 	public Boolean getHideOwner() {
 		return hideOwner;
 	}
 
 	/**
 	 * Sets whether to hide the owner of this album.
 	 */
 	public void setHideOwner(Boolean hideOwner) {
 		this.hideOwner = hideOwner;
 	}
 
 	/**
 	 * Enable intercept shipping (personal delivery) for orders from this album.
 	 * 
      * Values:
      * 0 - No
      * 1 - Yes
      * 2 - Inherit
 	 */
 	public Integer getInterceptShipping() {
 		return interceptShipping;
 	}
 
 	/**
 	 * Sets whether to enable intercept shipping (personal delivery) for orders from this album.
 	 * 
      * Values:
      * 0 - No
      * 1 - Yes
      * 2 - Inherit
 	 */
 	public void setInterceptShipping(Integer interceptShipping) {
 		this.interceptShipping = interceptShipping;
 	}
 
 	/**
 	 * Allow viewing of Large images for this album.
 	 */
 	public Boolean getLarges() {
 		return larges;
 	}
 
 	/**
 	 * Sets whether to allow viewing of Large images for this album.
 	 */
 	public void setLarges(Boolean larges) {
 		this.larges = larges;
 	}
 
 	/**
 	 * Allow viewing of Original images for this album.
 	 */
 	public Boolean getOriginals() {
 		return originals;
 	}
 
 	/**
 	 * Sets whether to allow viewing of Original images for this album.
 	 */
 	public void setOriginals(Boolean originals) {
 		this.originals = originals;
 	}
 
 	/**
 	 * Enable packaging branding for orders from this album.
 	 * (owner, pro accounts only)
 	 */
 	public Boolean getPackagingBranding() {
 		return packagingBranding;
 	}
 
 	public void setPackagingBranding(Boolean packagingBranding) {
 		this.packagingBranding = packagingBranding;
 	}
 
 	/**
 	 * Sets whether to enable packaging branding for orders from this album.
 	 * (owner, pro accounts only)
 	 */
 	public void getPackagingBranding(Boolean packagingBranding) {
 		this.packagingBranding = packagingBranding;
 	}
 
 	/**
 	 * The password for this album.
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * Sets the password for this album.
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * The password hint for this album.
 	 */
 	public String getPasswordHint() {
 		return passwordHint;
 	}
 
 	/**
 	 * Sets the password hint for this album.
 	 */
 	public void setPasswordHint(String passwordHint) {
 		this.passwordHint = passwordHint;
 	}
 
 	/**
 	 * The position of this album within the site.
 	 */
 	public Integer getPosition() {
 		return position;
 	}
 
 	/**
 	 * Sets the position of this album within the site.
 	 */
 	public void setPosition(Integer position) {
 		this.position = position;
 	}
 
 	/**
 	 * Allow images from this album to purchased as a print, merchandise or digital download.
 	 */
 	public Boolean getPrintable() {
 		return printable;
 	}
 
 	/**
 	 * Sets whether to allow images from this album to purchased as a print, merchandise or digital download.
 	 */
 	public void setPrintable(Boolean printable) {
 		this.printable = printable;
 	}
 
 	/**
 	 * The printmark applied to images of this album.
 	 */
 	public Printmark getPrintmark() {
 		return printmark;
 	}
 
 	/**
 	 * Sets the printmark applied to images of this album.
 	 */
 	public void setPrintmark(Printmark printmark) {
 		this.printmark = printmark;
 	}
 
 	/**
 	 * The number of days an order is held for a pro to proof prior to being sent to print.
 	 * (owner, pro accounts only)
 	 */
 	public Integer getProofDays() {
 		return proofDays;
 	}
 
 	/**
 	 * Sets the number of days an order is held for a pro to proof prior to being sent to print.
 	 * (owner, pro accounts only)
 	 */
 	public void setProofDays(Integer proofDays) {
 		this.proofDays = proofDays;
 	}
 
 	/**
 	 * Enable right-click protection for this album.
 	 * (power & pro accounts only)
 	 */
 	public Boolean getProtectedRightClick() {
 		return protectedRightClick;
 	}
 
 	/**
 	 * Sets whether to enable right-click protection for this album.
 	 * (power & pro accounts only)
 	 */
 	public void setProtectedRightClick(Boolean protectedRightClick) {
 		this.protectedRightClick = protectedRightClick;
 	}
 
 	/**
 	 * Display this album publicly.
 	 */
 	public Boolean getPublicAlbum() {
 		return publicAlbum;
 	}
 
 	/**
 	 * Sets whether to display this album publicly.
 	 */
 	public void setPublicAlbum(Boolean publicAlbum) {
 		this.publicAlbum = publicAlbum;
 	}
 
 	/**
 	 * Display the Share button for this album.
 	 */
 	public Boolean getShare() {
 		return share;
 	}
 
 	/**
 	 * Sets whether to display the Share button for this album.
 	 */
 	public void setShare(Boolean share) {
 		this.share = share;
 	}
 
 	/**
 	 * Allow SmugMug to index images from this album.
 	 */
 	public Boolean getSmugSearchable() {
 		return smugSearchable;
 	}
 
 	/**
 	 * Sets whether to allow SmugMug to index images from this album.
 	 */
 	public void setSmugSearchable(Boolean smugSearchable) {
 		this.smugSearchable = smugSearchable;
 	}
 
 	/**
 	 * The direction used for sorting images within this album.
      *
      * Values:
      * false - Ascending (1-99, A-Z, 1980-2004, etc)
      * true - Descending (99-1, Z-A, 2004-1980, etc)
 	 */
 	public Boolean getSortDirection() {
 		return sortDirection;
 	}
 
 	/**
 	 * Sets the direction used for sorting images within this album.
      *
      * Values:
      * false - Ascending (1-99, A-Z, 1980-2004, etc)
      * true - Descending (99-1, Z-A, 2004-1980, etc)
 	 */
 	public void setSortDirection(Boolean sortDirection) {
 		this.sortDirection = sortDirection;
 	}
 
 	/**
 	 * The method used for sorting images within this album.
      *
      * Values:
      * Position - None
      * Caption - By caption
      * FileName - By filenames
      * Date - By date uploaded
      * DateTime - By date modified (if available)
      * DateTimeOriginal - By date taken (if available)
 	 */
 	public String getSortMethod() {
 		return sortMethod;
 	}
 
 	/**
 	 * Sets the method used for sorting images within this album.
      *
      * Values:
      * Position - None
      * Caption - By caption
      * FileName - By filenames
      * Date - By date uploaded
      * DateTime - By date modified (if available)
      * DateTimeOriginal - By date taken (if available)
 	 */
 	public void setSortMethod(String sortMethod) {
 		this.sortMethod = sortMethod;
 	}
 
 	/**
 	 * Enable automatic square cropping of thumbnails for this album.
 	 */
 	public Boolean getSquareThumbs() {
 		return squareThumbs;
 	}
 
 	/**
 	 * Sets whether to enable automatic square cropping of thumbnails for this album.
 	 */
 	public void setSquareThumbs(Boolean squareThumbs) {
 		this.squareThumbs = squareThumbs;
 	}
 
 	/**
 	 * The style template applied to this album.
 	 * The id of a specific style template.
 	 * 
      * Values:
      * 0 - Viewer Choice (default)
      * 3 - SmugMug
      * 4 - Traditional
      * 7 - All Thumbs
      * 8 - Slideshow
      * 9 - Journal (Old)
      * 10 - SmugMug Small
      * 11 - Filmstrip
      * 12 - Critique
      * 16 - Journal
      * 17 - Thumbnails
 	 */
 	public Integer getTemplate() {
 		return template;
 	}
 
 	/**
 	 * Sets the style template applied to this album.
 	 */
 	public void setTemplate(Integer template) {
 		this.template = template;
 	}
 
 	/**
 	 * The Amount setting used for sharpening display copies of images in this album.
 	 * (owner, power & pro accounts only)
 	 */
 	public Float getUnsharpAmount() {
 		return unsharpAmount;
 	}
 
 	/**
 	 * Sets the Amount setting used for sharpening display copies of images in this album.
 	 * (owner, power & pro accounts only)
 	 */
 	public void setUnsharpAmount(Float unsharpAmount) {
 		this.unsharpAmount = unsharpAmount;
 	}
 
 	/**
 	 * The Radius setting used for sharpening display copies of images in this album.
 	 * (owner, power & pro accounts only)
 	 */
 	public Float getUnsharpRadius() {
 		return unsharpRadius;
 	}
 
 	/**
 	 * Sets the Radius setting used for sharpening display copies of images in this album.
 	 * (owner, power & pro accounts only)
 	 */
 	public void setUnsharpRadius(Float unsharpRadius) {
 		this.unsharpRadius = unsharpRadius;
 	}
 
 	/**
 	 * The Sigma setting used for sharpening display copies of images in this album.
 	 * (owner, power & pro accounts only)
 	 */
 	public Float getUnsharpSigma() {
 		return unsharpSigma;
 	}
 
 	/**
 	 * Sets the Sigma setting used for sharpening display copies of images in this album.
 	 * (owner, power & pro accounts only)
 	 */
 	public void setUnsharpSigma(Float unsharpSigma) {
 		this.unsharpSigma = unsharpSigma;
 	}
 
 	/**
 	 * The Threshold setting used for sharpening display copies of images in this album.
 	 * (owner, power & pro accounts only)
 	 */
 	public Float getUnsharpThreshold() {
 		return unsharpThreshold;
 	}
 
 	/**
 	 * Sets the Threshold setting used for sharpening display copies of images in this album.
 	 * (owner, power & pro accounts only)
 	 */
 	public void setUnsharpThreshold(Float unsharpThreshold) {
 		this.unsharpThreshold = unsharpThreshold;
 	}
 
 	/**
 	 * The key to allow guest uploading to this album.
 	 */
 	public String getUploadKey() {
 		return uploadKey;
 	}
 
 	/**
 	 * Sets the key to allow guest uploading to this album.
 	 */
 	public void setUploadKey(String uploadKey) {
 		this.uploadKey = uploadKey;
 	}
 
 	/**
 	 * The watermark applied to images of this album.
 	 */
 	public Watermark getWatermark() {
 		return watermark;
 	}
 
 	/**
 	 * Sets whether the watermark applied to images of this album.
 	 */
 	public void setWatermark(Watermark watermark) {
 		this.watermark = watermark;
 	}
 
 	/**
 	 * Enable automatic watermarking of images for this album.
 	 */
 	public Boolean getWatermarking() {
 		return watermarking;
 	}
 
 	/**
 	 * Sets whether to enable automatic watermarking of images for this album.
 	 */
 	public void setWatermarking(Boolean watermarking) {
 		this.watermarking = watermarking;
 	}
 
 	/**
 	 * Allow search engines to index images from this album.
 	 */
 	public Boolean getWorldSearchable() {
 		return worldSearchable;
 	}
 
 	/**
 	 * Sets whether to allow search engines to index images from this album.
 	 */
 	public void setWorldSearchable(Boolean worldSearchable) {
 		this.worldSearchable = worldSearchable;
 	}
 
 	/**
 	 * Allow viewing of X2Large images for this album.
 	 */
 	public Boolean getX2Larges() {
 		return x2Larges;
 	}
 
 	/**
 	 * Sets whether to allow viewing of X2Large images for this album.
 	 */
 	public void setX2Larges(Boolean x2Larges) {
 		this.x2Larges = x2Larges;
 	}
 
 	/**
 	 * Allow viewing of X3Large images for this album.
 	 */
 	public Boolean getX3Larges() {
 		return x3Larges;
 	}
 
 	/**
 	 * Sets if allow viewing of X3Large images for this album.
 	 */
 	public void setX3Larges(Boolean x3Larges) {
 		this.x3Larges = x3Larges;
 	}
 
 	/**
 	 * Allow viewing of XLarge images for this album.
 	 */
 	public Boolean getxLarges() {
 		return xLarges;
 	}
 
 	/**
 	 * Sets if to allow viewing of XLarge images for this album.
 	 */
 	public void setxLarges(Boolean xLarges) {
 		this.xLarges = xLarges;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((backPrinting == null) ? 0 : backPrinting.hashCode());
 		result = prime
 				* result
 				+ ((boutiquePackaging == null) ? 0 : boutiquePackaging
 						.hashCode());
 		result = prime * result + ((canRank == null) ? 0 : canRank.hashCode());
 		result = prime * result + ((clean == null) ? 0 : clean.hashCode());
 		result = prime * result
 				+ ((colorCorrection == null) ? 0 : colorCorrection.hashCode());
 		result = prime * result
 				+ ((comments == null) ? 0 : comments.hashCode());
 		result = prime * result
 				+ ((community == null) ? 0 : community.hashCode());
 		result = prime * result + ((exif == null) ? 0 : exif.hashCode());
 		result = prime * result
 				+ ((external == null) ? 0 : external.hashCode());
 		result = prime * result
 				+ ((familyEdit == null) ? 0 : familyEdit.hashCode());
 		result = prime * result
 				+ ((filenames == null) ? 0 : filenames.hashCode());
 		result = prime * result
 				+ ((friendEdit == null) ? 0 : friendEdit.hashCode());
 		result = prime * result
 				+ ((geography == null) ? 0 : geography.hashCode());
 		result = prime * result
 				+ ((guestUploadURL == null) ? 0 : guestUploadURL.hashCode());
 		result = prime * result + ((header == null) ? 0 : header.hashCode());
 		result = prime * result
 				+ ((hideOwner == null) ? 0 : hideOwner.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime
 				* result
 				+ ((interceptShipping == null) ? 0 : interceptShipping
 						.hashCode());
 		result = prime * result + ((larges == null) ? 0 : larges.hashCode());
 		result = prime * result
 				+ ((originals == null) ? 0 : originals.hashCode());
 		result = prime * result
 				+ ((packagingBranding == null) ? 0 : packagingBranding.hashCode());
 		result = prime * result
 				+ ((password == null) ? 0 : password.hashCode());
 		result = prime * result
 				+ ((passwordHint == null) ? 0 : passwordHint.hashCode());
 		result = prime * result
 				+ ((position == null) ? 0 : position.hashCode());
 		result = prime * result
 				+ ((printable == null) ? 0 : printable.hashCode());
 		result = prime * result
 				+ ((printmark == null) ? 0 : printmark.hashCode());
 		result = prime * result
 				+ ((proofDays == null) ? 0 : proofDays.hashCode());
 		result = prime
 				* result
 				+ ((protectedRightClick == null) ? 0 : protectedRightClick
 						.hashCode());
 		result = prime * result
 				+ ((publicAlbum == null) ? 0 : publicAlbum.hashCode());
 		result = prime * result + ((share == null) ? 0 : share.hashCode());
 		result = prime * result
 				+ ((smugSearchable == null) ? 0 : smugSearchable.hashCode());
 		result = prime * result
 				+ ((sortDirection == null) ? 0 : sortDirection.hashCode());
 		result = prime * result
 				+ ((sortMethod == null) ? 0 : sortMethod.hashCode());
 		result = prime * result
 				+ ((squareThumbs == null) ? 0 : squareThumbs.hashCode());
 		result = prime * result
 				+ ((template == null) ? 0 : template.hashCode());
 		result = prime * result
 				+ ((unsharpAmount == null) ? 0 : unsharpAmount.hashCode());
 		result = prime * result
 				+ ((unsharpRadius == null) ? 0 : unsharpRadius.hashCode());
 		result = prime * result
 				+ ((unsharpSigma == null) ? 0 : unsharpSigma.hashCode());
 		result = prime
 				* result
 				+ ((unsharpThreshold == null) ? 0 : unsharpThreshold.hashCode());
 		result = prime * result
 				+ ((uploadKey == null) ? 0 : uploadKey.hashCode());
 		result = prime * result
 				+ ((watermark == null) ? 0 : watermark.hashCode());
 		result = prime * result
 				+ ((watermarking == null) ? 0 : watermarking.hashCode());
 		result = prime * result
 				+ ((worldSearchable == null) ? 0 : worldSearchable.hashCode());
 		result = prime * result
 				+ ((x2Larges == null) ? 0 : x2Larges.hashCode());
 		result = prime * result
 				+ ((x3Larges == null) ? 0 : x3Larges.hashCode());
 		result = prime * result + ((xLarges == null) ? 0 : xLarges.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		AlbumBase other = (AlbumBase) obj;
 		if (backPrinting == null) {
 			if (other.backPrinting != null)
 				return false;
 		} else if (!backPrinting.equals(other.backPrinting))
 			return false;
 		if (boutiquePackaging == null) {
 			if (other.boutiquePackaging != null)
 				return false;
 		} else if (!boutiquePackaging.equals(other.boutiquePackaging))
 			return false;
 		if (canRank == null) {
 			if (other.canRank != null)
 				return false;
 		} else if (!canRank.equals(other.canRank))
 			return false;
 		if (clean == null) {
 			if (other.clean != null)
 				return false;
 		} else if (!clean.equals(other.clean))
 			return false;
 		if (colorCorrection == null) {
 			if (other.colorCorrection != null)
 				return false;
 		} else if (!colorCorrection.equals(other.colorCorrection))
 			return false;
 		if (comments == null) {
 			if (other.comments != null)
 				return false;
 		} else if (!comments.equals(other.comments))
 			return false;
 		if (community == null) {
 			if (other.community != null)
 				return false;
 		} else if (!community.equals(other.community))
 			return false;
 		if (exif == null) {
 			if (other.exif != null)
 				return false;
 		} else if (!exif.equals(other.exif))
 			return false;
 		if (external == null) {
 			if (other.external != null)
 				return false;
 		} else if (!external.equals(other.external))
 			return false;
 		if (familyEdit == null) {
 			if (other.familyEdit != null)
 				return false;
 		} else if (!familyEdit.equals(other.familyEdit))
 			return false;
 		if (filenames == null) {
 			if (other.filenames != null)
 				return false;
 		} else if (!filenames.equals(other.filenames))
 			return false;
 		if (friendEdit == null) {
 			if (other.friendEdit != null)
 				return false;
 		} else if (!friendEdit.equals(other.friendEdit))
 			return false;
 		if (geography == null) {
 			if (other.geography != null)
 				return false;
 		} else if (!geography.equals(other.geography))
 			return false;
 		if (guestUploadURL == null) {
 			if (other.guestUploadURL != null)
 				return false;
 		} else if (!guestUploadURL.equals(other.guestUploadURL))
 			return false;
 		if (header == null) {
 			if (other.header != null)
 				return false;
 		} else if (!header.equals(other.header))
 			return false;
 		if (hideOwner == null) {
 			if (other.hideOwner != null)
 				return false;
 		} else if (!hideOwner.equals(other.hideOwner))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (interceptShipping == null) {
 			if (other.interceptShipping != null)
 				return false;
 		} else if (!interceptShipping.equals(other.interceptShipping))
 			return false;
 		if (larges == null) {
 			if (other.larges != null)
 				return false;
 		} else if (!larges.equals(other.larges))
 			return false;
 		if (originals == null) {
 			if (other.originals != null)
 				return false;
 		} else if (!originals.equals(other.originals))
 			return false;
 		if (packagingBranding == null) {
 			if (other.packagingBranding != null)
 				return false;
 		} else if (!packagingBranding.equals(other.packagingBranding))
 			return false;
 		if (password == null) {
 			if (other.password != null)
 				return false;
 		} else if (!password.equals(other.password))
 			return false;
 		if (passwordHint == null) {
 			if (other.passwordHint != null)
 				return false;
 		} else if (!passwordHint.equals(other.passwordHint))
 			return false;
 		if (position == null) {
 			if (other.position != null)
 				return false;
 		} else if (!position.equals(other.position))
 			return false;
 		if (printable == null) {
 			if (other.printable != null)
 				return false;
 		} else if (!printable.equals(other.printable))
 			return false;
 		if (printmark == null) {
 			if (other.printmark != null)
 				return false;
 		} else if (!printmark.equals(other.printmark))
 			return false;
 		if (proofDays == null) {
 			if (other.proofDays != null)
 				return false;
 		} else if (!proofDays.equals(other.proofDays))
 			return false;
 		if (protectedRightClick == null) {
 			if (other.protectedRightClick != null)
 				return false;
 		} else if (!protectedRightClick.equals(other.protectedRightClick))
 			return false;
 		if (publicAlbum == null) {
 			if (other.publicAlbum != null)
 				return false;
 		} else if (!publicAlbum.equals(other.publicAlbum))
 			return false;
 		if (share == null) {
 			if (other.share != null)
 				return false;
 		} else if (!share.equals(other.share))
 			return false;
 		if (smugSearchable == null) {
 			if (other.smugSearchable != null)
 				return false;
 		} else if (!smugSearchable.equals(other.smugSearchable))
 			return false;
 		if (sortDirection == null) {
 			if (other.sortDirection != null)
 				return false;
 		} else if (!sortDirection.equals(other.sortDirection))
 			return false;
 		if (sortMethod == null) {
 			if (other.sortMethod != null)
 				return false;
 		} else if (!sortMethod.equals(other.sortMethod))
 			return false;
 		if (squareThumbs == null) {
 			if (other.squareThumbs != null)
 				return false;
 		} else if (!squareThumbs.equals(other.squareThumbs))
 			return false;
 		if (template == null) {
 			if (other.template != null)
 				return false;
 		} else if (!template.equals(other.template))
 			return false;
 		if (unsharpAmount == null) {
 			if (other.unsharpAmount != null)
 				return false;
 		} else if (!unsharpAmount.equals(other.unsharpAmount))
 			return false;
 		if (unsharpRadius == null) {
 			if (other.unsharpRadius != null)
 				return false;
 		} else if (!unsharpRadius.equals(other.unsharpRadius))
 			return false;
 		if (unsharpSigma == null) {
 			if (other.unsharpSigma != null)
 				return false;
 		} else if (!unsharpSigma.equals(other.unsharpSigma))
 			return false;
 		if (unsharpThreshold == null) {
 			if (other.unsharpThreshold != null)
 				return false;
 		} else if (!unsharpThreshold.equals(other.unsharpThreshold))
 			return false;
 		if (uploadKey == null) {
 			if (other.uploadKey != null)
 				return false;
 		} else if (!uploadKey.equals(other.uploadKey))
 			return false;
 		if (watermark == null) {
 			if (other.watermark != null)
 				return false;
 		} else if (!watermark.equals(other.watermark))
 			return false;
 		if (watermarking == null) {
 			if (other.watermarking != null)
 				return false;
 		} else if (!watermarking.equals(other.watermarking))
 			return false;
 		if (worldSearchable == null) {
 			if (other.worldSearchable != null)
 				return false;
 		} else if (!worldSearchable.equals(other.worldSearchable))
 			return false;
 		if (x2Larges == null) {
 			if (other.x2Larges != null)
 				return false;
 		} else if (!x2Larges.equals(other.x2Larges))
 			return false;
 		if (x3Larges == null) {
 			if (other.x3Larges != null)
 				return false;
 		} else if (!x3Larges.equals(other.x3Larges))
 			return false;
 		if (xLarges == null) {
 			if (other.xLarges != null)
 				return false;
 		} else if (!xLarges.equals(other.xLarges))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "id=" + id + ", backPrinting=" + backPrinting
 				+ ", boutiquePackaging=" + boutiquePackaging + ", canRank="
 				+ canRank + ", clean=" + clean + ", colorCorrection="
 				+ colorCorrection + ", comments=" + comments + ", community="
 				+ community + ", exif=" + exif + ", external=" + external
 				+ ", familyEdit=" + familyEdit + ", filenames=" + filenames
 				+ ", friendEdit=" + friendEdit + ", geography=" + geography
 				+ ", guestUploadURL=" + guestUploadURL + ", header=" + header
 				+ ", hideOwner=" + hideOwner + ", interceptShipping="
 				+ interceptShipping + ", larges=" + larges + ", originals="
 				+ originals + ", packagingBranding=" + packagingBranding
 				+ ", password=" + password + ", passwordHint=" + passwordHint
 				+ ", position=" + position + ", printable=" + printable
 				+ ", printmark=" + printmark + ", proofDays=" + proofDays
 				+ ", protectedRightClick=" + protectedRightClick
 				+ ", publicAlbum=" + publicAlbum + ", share=" + share
 				+ ", smugSearchable=" + smugSearchable + ", sortDirection="
 				+ sortDirection + ", sortMethod=" + sortMethod
 				+ ", squareThumbs=" + squareThumbs + ", template=" + template
 				+ ", unsharpAmount=" + unsharpAmount + ", unsharpRadius="
 				+ unsharpRadius + ", unsharpSigma=" + unsharpSigma
 				+ ", unsharpThreshold=" + unsharpThreshold + ", uploadKey="
 				+ uploadKey + ", watermark=" + watermark + ", watermarking="
 				+ watermarking + ", worldSearchable=" + worldSearchable
 				+ ", x2Larges=" + x2Larges + ", x3Larges=" + x3Larges
 				+ ", xLarges=" + xLarges;
 	}
 }
