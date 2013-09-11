package org.emop.sender.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.emop.http.HTTPResult;
import org.emop.sender.HttpServer;
import org.json.simple.JSONValue;


public class ApiProxyServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
    	doPost(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding("utf8");
		response.setContentType("application/json");
		
		String api_name = request.getParameter("__api_name");
		
		if(api_name == null || api_name.length() == 0){
			api_name = "cms_api_info";
		}
		//Map<String, Object> apiResult = null;
		if(HttpServer.ins.authManager.authCheck(request) || api_name.equals("timing_user_login")) {
			Map<String, Object> param = new HashMap<String, Object>();
			
			Enumeration enumParam = request.getParameterNames();
			for(;enumParam.hasMoreElements();){
				String key = enumParam.nextElement() + "";
				if(key.startsWith("_")) continue;
				param.put(key, request.getParameter(key));
			}
			
			HTTPResult result = (HTTPResult) HttpServer.ins.api.call(api_name, param);
			if(api_name.equals("timing_user_login") && result.isOK){
				HttpServer.ins.authManager.updateSessionInfo(request, response, result);
			}
			
			JSONValue.writeJSONString(result.json, response.getWriter());
		}else {
			Map<String, Object> apiResult = new HashMap<String, Object>();
			apiResult.put("status", "ok");
			apiResult.put("code", "auth_error");
			
			JSONValue.writeJSONString(apiResult, response.getWriter());
		}
		
    }
    
 
    
}
