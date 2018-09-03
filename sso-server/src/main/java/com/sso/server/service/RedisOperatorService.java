package com.sso.server.service;

import com.sso.server.entity.TokenSession;

public interface RedisOperatorService {
	
	void putUserInfo(String userName, String token, int expireTime);
	
	void putTokenInfo(String tokenKey, TokenSession tokenSession, int expireTime);
	
	String getUserInfo(String userName);
	
	TokenSession getTokenInfo(String tokenKey);
	
	void deleteUserInfo(String userName);
	
	void deleteTokenInfo(String tokenKey);

}
