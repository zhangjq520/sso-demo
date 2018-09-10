package com.sso.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.sso.core.plugin.service.RedisService;
import com.sso.server.entity.TokenSession;
import com.sso.server.service.RedisOperatorService;

@Service
public class RedisOperatorServiceImpl implements RedisOperatorService{
	
	private final static long EXPRIRETIME = 600;
	
	@Autowired
	private RedisService redisService;
	
	@Override
	public void putUserInfo(String userName, String token, long expireTime) {
		redisService.set(userName, token);
		if (expireTime<=0) {
			expireTime = EXPRIRETIME;
		}
		redisService.expire(userName, expireTime);
	}
	
	@Override
	public void deleteUserInfo(String userName) {
		redisService.remove(userName);
	}

	@Override
	public void putTokenInfo(String tokenKey, TokenSession tokenSession, long expireTime) {
		redisService.set(tokenKey, JSON.toJSON(tokenSession).toString());
		if (expireTime<=0) {
			expireTime = EXPRIRETIME;
		}
		redisService.expire(tokenKey, expireTime);
		
	}
	
	@Override
	public void deleteTokenInfo(String tokenKey) {
		redisService.remove(tokenKey);
		
	}

	@Override
	public String getUserInfo(String userName) {
		return redisService.get(userName); 
	}

	@Override
	public TokenSession getTokenInfo(String tokenKey) {
		String tem = redisService.get(tokenKey);
		if (null==tem||"".equals(tem)) {
			return null;
		}
		return JSON.parseObject(tem, TokenSession.class);
	}

}
