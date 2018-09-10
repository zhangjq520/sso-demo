package com.sso.client.plugin.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import com.sso.client.plugin.service.UserAccessService;

//判断没有登陆就跳转到sso认证中心
@Order(1)
@WebFilter(filterName = "ssoFilter", urlPatterns = "/hello", initParams ={@WebInitParam(name ="EXCLUDED_PAGES" , value = "/receiveToken,/ssoLogout,/ssoDeleteToken,/login")})
public class CommonAuthFilter implements Filter {
	
	@Autowired
	private UserAccessService userAccessService;
	
	private String excludedPages;
	private String[] excludedPageArray;
	
	@Value("${sso.server.url}")
	String ssoServerPath;
	
	@Value("${sso.client.login}")
	String ssoLogin;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		excludedPages = filterConfig.getInitParameter("EXCLUDED_PAGES");
		if(excludedPages!=null) {
			excludedPageArray = excludedPages.split(",");
		}
		
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		//判断当前用户是否已经登录
		HttpServletRequest req = (HttpServletRequest) request;
		String token = req.getParameter("token");
		if(token!=null 
				&& String.valueOf(token).trim().length()>0 
				&& userAccessService.getUserToken(token)!=null) {
			request.setAttribute("username", token);
			chain.doFilter(req, response);
		}else {
			boolean containtFlag = false;
			//http://peer1:8088/sendToken?ssoToken=c2ce29be-5adb-4aaf-82cc-2ba24330176e&userName=6677
			if(excludedPageArray!=null) {
				for(String excludeStr : excludedPageArray) {
					if(excludeStr.equals(req.getServletPath())) {
						containtFlag = true;
						break;
					}
				}
			}
			if(containtFlag) {
				chain.doFilter(req, response);
			}else {
				//其他情况都丢给SSO中心去处理
				 HttpServletResponse httpResponse = (HttpServletResponse)response;
//				 String originalUrl = "http://dc1.poc.com:9001/login";
				 httpResponse.sendRedirect(ssoServerPath+"/index?originalUrl="+ssoLogin);
			}
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
