package com.sso.core.plugin.model;

import java.io.Serializable;

public class ResultUser implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;
	private String ssoToken;
	private String status;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getSsoToken() {
		return ssoToken;
	}
	public void setSsoToken(String ssoToken) {
		this.ssoToken = ssoToken;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}
