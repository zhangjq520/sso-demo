package com.sso.client.plugin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sso.client.plugin.service.RedisService;
import com.sso.client.plugin.service.UserAccessService;

@Service
public class UserAccessServiceImpl implements UserAccessService{
	
	private final static int EXPRIRETIME = 60;
	
	@Autowired
	private RedisService redisService;

	@Override
	public String getUserToken(String user) {
		String token = redisService.get(user);
		if(token==null) {
			return null;
		}
		return token;
	}

	@Override
	public void putUserStatus(String user, String ssoToken) {
		redisService.set(user, ssoToken);
		redisService.expire(user, EXPRIRETIME);
	}
	
	@Override
	public void deleteToken(String token) {
		redisService.remove(token);
	}

}
