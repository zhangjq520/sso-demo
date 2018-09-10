package com.sso.client.plugin.service;

public interface UserAccessService {
	
	String getUserToken(String user);
	
	String putUserStatus(String user, String flag);
	
	void deleteToken(String ssoToken, String username);
}
