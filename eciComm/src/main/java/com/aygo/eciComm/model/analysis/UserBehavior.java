package com.aygo.eciComm.model.analysis;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class UserBehavior {
	private String userId;
	private String sessionId;
	private List<String> viewedProducts;
	private List<String> purchasedProducts;
	private Map<String, Integer> categoryViews;
	private Map<String, Double> priceRangePreferences;
	private List<String> searchQueries;
	private Instant lastActivityTime;
	private Duration averageSessionDuration;
	private Integer cartAbandonment;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public List<String> getViewedProducts() {
		return viewedProducts;
	}

	public void setViewedProducts(List<String> viewedProducts) {
		this.viewedProducts = viewedProducts;
	}

	public List<String> getPurchasedProducts() {
		return purchasedProducts;
	}

	public void setPurchasedProducts(List<String> purchasedProducts) {
		this.purchasedProducts = purchasedProducts;
	}

	public Map<String, Integer> getCategoryViews() {
		return categoryViews;
	}

	public void setCategoryViews(Map<String, Integer> categoryViews) {
		this.categoryViews = categoryViews;
	}

	public Map<String, Double> getPriceRangePreferences() {
		return priceRangePreferences;
	}

	public void setPriceRangePreferences(Map<String, Double> priceRangePreferences) {
		this.priceRangePreferences = priceRangePreferences;
	}

	public List<String> getSearchQueries() {
		return searchQueries;
	}

	public void setSearchQueries(List<String> searchQueries) {
		this.searchQueries = searchQueries;
	}

	public Instant getLastActivityTime() {
		return lastActivityTime;
	}

	public void setLastActivityTime(Instant lastActivityTime) {
		this.lastActivityTime = lastActivityTime;
	}

	public Duration getAverageSessionDuration() {
		return averageSessionDuration;
	}

	public void setAverageSessionDuration(Duration averageSessionDuration) {
		this.averageSessionDuration = averageSessionDuration;
	}

	public Integer getCartAbandonment() {
		return cartAbandonment;
	}

	public void setCartAbandonment(Integer cartAbandonment) {
		this.cartAbandonment = cartAbandonment;
	}
}
