package com.example.Bean;

public class ArticleBEAN {
	private String name; 
	private String description;
	private float minimumPrice;
	private String keyWord;
	private int creatorId;
	private int winningAuctionId;
	private int code;
	private String imagePath;
	private String encodedImage;
	public ArticleBEAN() {
		
	}
	
	public ArticleBEAN(String name2, int idArticle) {
		this.name = name2;
		this.code = idArticle;
	}
	public ArticleBEAN(int code, String name, String description, float minimumPrice, String keyWord) {
		this.name = name;
		this.code = code;
		this.description = description;
		this.minimumPrice = minimumPrice;
		this.keyWord = keyWord;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String descrption) {
		this.description = descrption;
	}
	public float getMinimumPrice() {
		return minimumPrice;
	}
	public void setMinimumPrice(float minimumPrice) {
		this.minimumPrice = minimumPrice;
	}
	public String getKeyWord() {
		return keyWord;
	}
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	public int getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
	public int getWinningAuctionId() {
		return winningAuctionId;
	}
	public void setWinningAuctionId(int winningAuctionId) {
		this.winningAuctionId = winningAuctionId;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public void setEncodedImage(String encodedImage) {
		this.encodedImage = encodedImage;
	}
	public String getEncodedImage() {
		return encodedImage;
	}
	
}
