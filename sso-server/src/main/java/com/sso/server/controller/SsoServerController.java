package com.sso.server.controller;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.sso.core.plugin.CookieUtil;
import com.sso.core.plugin.model.ResultUser;
import com.sso.server.service.AuthSessionService;

@Controller
public class SsoServerController {
	
	@Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private AuthSessionService authSessionService;
	
	@RequestMapping("/index")
	public String firstCheck(HttpServletRequest request) {
		String originalUrl = request.getParameter("originalUrl");
		//判断当前用户是否已经登录
		HttpServletRequest req = (HttpServletRequest) request;
		String username = CookieUtil.getCookie(req, "zjqLoginMark");
		System.out.println("------------"+username+"------------");
		if (null==username) {
			username = request.getParameter("ssoToken");
		}
		String ssoToken = null;
		boolean loginFlag = false;
		if(username!=null && username.trim().length()>0) {
			//对用户先判断是否已经登陆过
			ssoToken = authSessionService.getUserToken(username);
			if(ssoToken!=null) {
				loginFlag = true;
			}
		}
		if(loginFlag) {
			//判断如果用户已经在SSO-Server认证过，直接发送token
//			if(tokenTrans(request,originalUrl,ssoToken,token)) {
//				
//			}
			if(originalUrl!=null) {
				if(originalUrl.contains("?")) {
					originalUrl = originalUrl + "&ssoToken="+ssoToken;
				}else {
					originalUrl = originalUrl + "?ssoToken="+ssoToken;
				}
			}
			return "redirect:"+originalUrl;
		}else {
			//需要替换成专业点的路径,自己登陆下了
			return "redirect:/loginPage?originalUrl="+request.getParameter("originalUrl");
		}
	}
	
	//登陆界面，返回的是页面地址
	@RequestMapping("/loginPage")
	public String index(HttpServletRequest request) {
		if(request.getParameter("originalUrl")!=null) {
			request.setAttribute("originalUrl", request.getParameter("originalUrl"));
		}
		return "loginIndex";
	}
	
//	private boolean tokenTrans(HttpServletRequest request, String originalUrl,String userName, String token) {
//		String[] paths = originalUrl.split("/");
//		String shortAppServerUrl = paths[2];
//		String returnUrl = "http://"+shortAppServerUrl+"/receiveToken?ssoToken="+token+"&userName="+userName;
//		//http://peer1:8088/receiveToken?ssoToken=80414bcb-a71d-48c8-bfee-098a303324d4&userName=xixi
//		return "success".equals(restTemplate.getForObject(returnUrl, String.class));
//		
//	}
	
	//登陆逻辑,返回的是令牌
	@RequestMapping(value="/doLogin",method=RequestMethod.POST)
	public String login(HttpServletRequest request, HttpServletResponse response,
			String userName, String password, String originalUrl) {
		if(authSessionService.verify(userName,password)) {
			String ssoToken = authSessionService.cacheSession(userName);
			request.setAttribute("helloName", userName);
			if (null!=originalUrl&&!"".equals(originalUrl)) {
//				if(tokenTrans(request,originalUrl,userName,token)) {
//					
//				}
				//跳转到提示成功的页面
				if(originalUrl!=null) {
					if(originalUrl.contains("?")) {
						originalUrl = originalUrl + "&ssoToken="+ssoToken;
					}else {
						originalUrl = originalUrl + "?ssoToken="+ssoToken;
					}
					request.setAttribute("originalUrl", originalUrl);
				}
			}
			//登录10分钟
			CookieUtil.addCookie(response, "zjqLoginMark", userName, 10, null);
			return "hello";//TO-DO 三秒跳转
		}
		//验证不通过，重新来吧
		if(originalUrl!=null) {
			request.setAttribute("originalUrl", originalUrl);
		}
		return "loginIndex";
	}
	
	//通过ssoToken查询用户信息
	@RequestMapping(value = "/selectToken", method = RequestMethod.POST)
	@ResponseBody
	public ResultUser selectToken(HttpServletRequest request, String ssoToken, String sonSystemCode, String delTokenAddress) {
		return authSessionService.selectToken(ssoToken, sonSystemCode, delTokenAddress);
	}

	// 校验token并注册地址
	@RequestMapping(value = "/varifyToken", method = RequestMethod.POST)
	@ResponseBody
	public String varifyToken(String ssoToken) {
		return authSessionService.varifySsoToken(ssoToken);
	}
	
	@RequestMapping(value="/logoutByUser",method=RequestMethod.GET)
	@ResponseBody
	public String logoutByUser(String userName) {
		String ssoToken = authSessionService.getUserToken(userName);
		if(ssoToken!=null) {
			List<String> addressList = authSessionService.logoutByUser(userName);
			if(addressList!=null) {
				addressList.stream().forEach(s -> sendLogout2Client(s,ssoToken,userName));
			}
		}
		return "Done";
	}
	
	@RequestMapping(value="/logoutByToken",method=RequestMethod.GET)
	@ResponseBody
	public String logoutByToken(String ssoToken) {
		List<String> addressList = authSessionService.logoutByToken(ssoToken);
		if(addressList!=null) {
			addressList.stream().forEach(s -> sendLogout2Client(s,ssoToken,null));
		}
		return "logout";
	}
	
	private void sendLogout2Client(String address,String ssoToken, String username) {
		String returnUrl =address +"?ssoToken="+ssoToken+"&username="+username;
		try {
			restTemplate.getForObject(returnUrl, String.class);
		}catch(Exception e) {
			//Log and do nothing
		}
	}
	
	/**
	 * 生成验证码
	 * @param request  Http请求
	 * @param response Http响应
	 * @param session  Http会话
	 */
	@RequestMapping(value="/verifycode")
	public void verifyCode(HttpServletRequest request, HttpServletResponse response,HttpSession session){
	    try {
	        int width = 60;
	        int height = 30;
	        Random random = new Random();
	        //设置response头信息
	        //禁止浏览器缓存
	        response.setHeader("Pragma", "No-cache");
	        response.setHeader("Cache-Control","no-cache");
	        response.setDateHeader("Expires",0);            
	        BufferedImage image = new BufferedImage(width, height,1);//生成缓冲区image类

	        Graphics graphics = image.getGraphics();//产生image类的Graphics用于绘制操作
	        //Graphics类的样式
	        graphics.setColor(this.getRandColor(10,255));
	        graphics.setFont(new Font("Times New Roman",0,28));
	        graphics.fillRect(0, 0, width, height);         
	        for(int i=0;i<40;i++){//绘制干扰线
	            graphics.setColor(this.getRandColor(100,200));
	            int x = random.nextInt(width);
	            int y = random.nextInt(height);
	            int x1 = random.nextInt(12);
	            int y1 = random.nextInt(12);
	            graphics.drawLine(x, y, x + x1, y + y1);
	        }
	        //绘制字符
	        String verifycode = "";
	        for(int i=0;i<4;i++){
	            String rand = String.valueOf(random.nextInt(10));
	            verifycode = verifycode + rand;
	            graphics.setColor(new Color(20+random.nextInt(110),20+random.nextInt(110),20+random.nextInt(110)));
	            graphics.drawString(rand,12*i+6, 26);
	        }
	        System.out.println("当前: "+verifycode);
	        session.setAttribute("VerifyCode", verifycode);//将字符保存到session中用于前端的验证
	        graphics.dispose();
	        ImageIO.write(image,"JPEG", response.getOutputStream());
	        response.getOutputStream().flush();         
	    } catch (Exception e) {
	        throw new RuntimeException();
	    } 

	}
	
	/**
	 * 随机颜色
	 * @param bcolor 开始颜色值
	 * @param ecolor 结束颜色值
	 * @return
	 */
	private Color getRandColor(int bcolor,int ecolor){
	    Random random = new Random();
	    int r = bcolor + random.nextInt(ecolor - bcolor);
	    int g = bcolor + random.nextInt(ecolor - bcolor);
	    int b = bcolor + random.nextInt(ecolor - bcolor);
	    return new Color(r,g,b);
	}
}
