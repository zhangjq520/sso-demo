package com.sso.server.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sso.core.plugin.model.ResultUser;
import com.sso.server.entity.TokenSession;
import com.sso.server.service.AuthSessionService;
import com.sso.server.service.RedisOperatorService;

@Service
public class AuthSessionServiceImpl implements AuthSessionService{
	
	@Value("${redis.expire.time}")
	private long redisExpireTime;
		
	@Autowired
	private RedisOperatorService redisOperatorService;

	@Override
	public boolean verify(String userName, String password) {
		// 根据自己数据库数据来校验
		return true;
	}

	@Override
	public String cacheSession(String userName) {
		//创建token
		String ssoToken = UUID.randomUUID().toString();
		redisOperatorService.putUserInfo(userName, ssoToken, redisExpireTime);
		TokenSession tokenSession = new TokenSession(ssoToken,userName); 
		redisOperatorService.putTokenInfo(ssoToken, tokenSession, redisExpireTime);
		return ssoToken;
	}

	@Override
	public ResultUser selectToken(String ssoToken, String sonSystemCode, String delTokenAddress) {
		ResultUser user = new ResultUser();
		TokenSession tokenSession = redisOperatorService.getTokenInfo(ssoToken);
		if(tokenSession!=null) {
			tokenSession.getAddressList().add(delTokenAddress);
			tokenSession.setTokenFlag(true);
			tokenSession.setSonSystemCode(sonSystemCode);
			redisOperatorService.putTokenInfo(ssoToken, tokenSession, redisExpireTime);
			user.setSsoToken(ssoToken);
			user.setStatus("success");
			user.setUsername(tokenSession.getUserName());
			return user;
		}
		user.setStatus("error");
		return user;
	}
	
	@Override
	public String varifySsoToken(String ssoToken) {
		TokenSession tokenSession = redisOperatorService.getTokenInfo(ssoToken);
		if(tokenSession!=null) {
			return "success";
		}
		return "error";
	}

	@Override
	public boolean checkUserLoginStatus(String userName,String address) {
		boolean flag = false;
		String token = redisOperatorService.getUserInfo(userName);
		if(token!=null) {
			TokenSession tokenSession = redisOperatorService.getTokenInfo(token);
			if(tokenSession!=null) {
				if(tokenSession.getAddressList().contains(address)) {
					flag =  true;
				}
			}
		}
		return flag;
	}

	@Override
	public String getUserToken(String userName) {
		String token = redisOperatorService.getUserInfo(userName);
		if(token==null) {
			return null;
		}else {
			if(redisOperatorService.getTokenInfo(token)!=null) {
				return token;
			}else {
				return null;
			}
			
		}
	}

	@Override
	public List<String> logoutByUser(String userName) {
		String ssoToken = redisOperatorService.getUserInfo(userName);
		redisOperatorService.deleteUserInfo(userName);
		if(ssoToken!=null) {
			return logoutByToken(ssoToken);
		}
		return null;
	}

	@Override
	public List<String> logoutByToken(String ssoToken) {
		if(ssoToken!=null) {
			TokenSession tokenSession = redisOperatorService.getTokenInfo(ssoToken);
			if(tokenSession!=null) {
				redisOperatorService.deleteTokenInfo(ssoToken);
				redisOperatorService.deleteUserInfo(tokenSession.getUserName());
				return tokenSession.getAddressList();
			}
		}
		return null;
	}
	
}
