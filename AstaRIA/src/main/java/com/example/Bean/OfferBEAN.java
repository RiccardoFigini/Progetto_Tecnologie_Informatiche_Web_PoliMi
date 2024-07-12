package com.example.Bean;

import java.sql.Timestamp;

public class OfferBEAN {
	private int auctionOfferId;
	private int clientId;
	private int auctionId;
	private float price;
	private Timestamp datatime;
	private String userOffer; //Username di chi ha fatto l'offera
	private String userAddress;
	
	public int getAuctionOfferId() {
		return auctionOfferId;
	}
	public void setAuctionOfferId(int auctionOfferId) {
		this.auctionOfferId = auctionOfferId;
	}
	public int getClientId() {
		return clientId;
	}
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	public int getAuctionId() {
		return auctionId;
	}
	public void setAuctionId(int auctionId) {
		this.auctionId = auctionId;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public Timestamp getDatatime() {
		return datatime;
	}
	public void setDatatime(Timestamp datatime) {
		this.datatime = datatime;
	}
	public String getUserOffer() {
		return userOffer;
	}
	public void setUserOffer(String userOffer) {
		this.userOffer = userOffer;
	}
	public String getUserAddress() {
		return userAddress;
	}
	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}
	
}
