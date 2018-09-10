package com.sso.client.plugin.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sso.client.plugin.service.UserAccessService;
import com.sso.core.plugin.service.RedisService;

@Service
public class UserAccessServiceImpl implements UserAccessService{
	
	@Value("${redis.expire.time}")
	private long redisExpireTime;
	
	@Autowired
	private RedisService redisService;

	@Override
	public String getUserToken(String token) {
		String username = redisService.get(token);
		if(username==null) {
			return null;
		}
		return username;
	}

	@Override
	public String putUserStatus(String username, String ssoToken) {
		redisService.set(username, ssoToken);
		redisService.expire(username, redisExpireTime);
		String token = UUID.randomUUID().toString();
		redisService.set(token, username);
		redisService.expire(token, redisExpireTime);
		redisService.set(ssoToken, token);
		redisService.expire(ssoToken, redisExpireTime);
		
		return token;
	}
	
	@Override
	public void deleteToken(String ssoToken, String username) {
		if (StringUtils.isEmpty(username)) {
			username = redisService.get(ssoToken);
		}
		if (StringUtils.isEmpty(ssoToken)) {
			ssoToken = redisService.get(username);
		}
		String token = redisService.get(ssoToken);
		redisService.remove(username);
		redisService.remove(token);
		redisService.remove(ssoToken);
	}

}
