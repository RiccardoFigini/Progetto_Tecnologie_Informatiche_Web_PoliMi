package com.example.Bean;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
public class AuctionBEAN {
	private int id;
	private float startPrice;
	private int minimumRise;
	private Timestamp startDate; 
	private int userIdOwner;
	private String usernameOwner;
	private Float maxOffer;
	private Timestamp endDate;
	private int userIdOffer;
	private String usernameOffer;
	private List<ArticleBEAN> listOfArticle;
	private String remainingTime;
	private boolean isClosed;
	private boolean alreadyOffer;

	public boolean getIsClosed() {
		return isClosed;
	}
	public void setIsClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
	public List<ArticleBEAN> getListOfArticle() {
		return listOfArticle;
	}
	public void setListOfArticle(List<ArticleBEAN> listOfArticle) {
		this.listOfArticle = listOfArticle;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public float getStartPrice() {
		return startPrice;
	}
	public void setStartPrice(float startPrice) {
		this.startPrice = startPrice;
	}
	public int getMinimumRise() {
		return minimumRise;
	}
	public void setMinimumRise(int minimumRise) {
		this.minimumRise = minimumRise;
	}
	public Timestamp getStartDate() {
		return startDate;
	}
	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}
	public int getUserIdOwner() {
		return userIdOwner;
	}
	public void setUserIdOwner(int userIdOwner) {
		this.userIdOwner = userIdOwner;
	}
	public Float getMaxOffer() {
		return maxOffer;
	}
	public void setMaxOffer(Float maxOffer) {
		this.maxOffer = maxOffer;
	}
	public Timestamp getEndDate() {
		return endDate;
	}
	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}
	public int getUserIdOffer() {
		return userIdOffer;
	}
	public void setUserIdOffer(int userIdOffer) {
		this.userIdOffer = userIdOffer;
	}
	public String getUsernameOwner() {
		return usernameOwner;
	}
	public void setUsernameOwner(String usernameOwner) {
		this.usernameOwner = usernameOwner;
	}
	public String getUsernameOffer() {
		return usernameOffer;
	}
	public void setUsernameOffer(String usernameOffer) {
		this.usernameOffer = usernameOffer;
	}
	public String getRemainingTime() {
		return remainingTime;
	}
	public void setRemainingTime(String remainingTime) {
		this.remainingTime = remainingTime;
	}
	public boolean isClosed() {
		return isClosed;
	}
	public boolean isAlreadyOffer() {
		return alreadyOffer;
	}

	public void setClosed(boolean closed) {
		isClosed = closed;
	}

	public void setAlreadyOffer(boolean alreadyOffer) {
		this.alreadyOffer = alreadyOffer;
	}
	public void addArticle(ArticleBEAN articleBEAN)
	{
		if(listOfArticle==null)
			listOfArticle= new ArrayList<>();
		this.listOfArticle.add(articleBEAN);
	}
}
