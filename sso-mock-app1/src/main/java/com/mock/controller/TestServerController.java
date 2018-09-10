package com.mock.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.sso.client.plugin.service.UserAccessService;
import com.sso.core.plugin.model.ResultUser;

@Controller
public class TestServerController {
	
	@Autowired
    private RestTemplate restTemplate;
	
	@Value("${sso.server.url}")
	String ssoServerPath;
	
	@Value("${sso.client.logout}")
	String ssoLogout;
	
	@Autowired
	private UserAccessService userAccessService;
	
	@RequestMapping("/hello")
	public String hello() {
		return "hello";
	}
	
	@RequestMapping("/login")
	public String login(HttpServletRequest request, String ssoToken) {
		String returnUrl = ssoServerPath+"/selectToken";
		MultiValueMap<String, String> requestParamMap = new LinkedMultiValueMap<String, String>();
		//http://peer2:8089/varifyToken?address=peer1:8088&token=c2ce29be-5adb-4aaf-82cc-2ba24330176e
		requestParamMap.add("delTokenAddress", ssoLogout);
		requestParamMap.add("ssoToken", ssoToken);
		requestParamMap.add("sonSystemCode", "CRM");
		HttpHeaders headers = new HttpHeaders();
		//  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(requestParamMap, headers);
		ResultUser user=  restTemplate.postForObject(returnUrl, requestEntity, ResultUser.class);
		if("success".equals(user.getStatus())) {
			//创建局部会话，保存用户状态为已登陆
			String token = userAccessService.putUserStatus(user.getUsername(), ssoToken);
			request.setAttribute("token", token);
			request.setAttribute("username", user.getUsername());
			return "hello";
		}
		
		return "error";
	}
}
