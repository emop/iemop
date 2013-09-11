package org.emop.sender.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.emop.sender.Version;
import org.json.simple.JSONValue;


public class StaticServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
    	doPost(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding("utf8");
		
		String path = request.getRequestURI();
		
		path = path.replaceAll("/static/", "");
		if(path.trim().length() < 2){
			path = "index.html";
		}
		
		Map<String, Object> param = new HashMap<String, Object>();
		
		Enumeration enumParam = request.getParameterNames();
		for(;enumParam.hasMoreElements();){
			String key = enumParam.nextElement() + "";
			if(key.startsWith("_")) continue;
			param.put(key, request.getParameter(key));
		}
		
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream("org/emop/sender/servlet/static/" + path);
		if(ins != null) {
			String type = getMimeTypeWithName(path);
			response.setContentType(type);
			if(isText(path)){
				outputText(ins, response.getWriter(), param);
			}else {
				byte[] buffer = new byte[64 * 1024];
				OutputStream os = response.getOutputStream();
				for(int len = 0; len >= 0; ){
					len = ins.read(buffer);
					if(len >= 0){
						os.write(buffer, 0, len);
					}
				}
				os.close();
			}		
		}else {
			response.setContentType("text/plain");
			response.getWriter().println("没有找到资源文件：" + path);
		}
    }

    protected void outputText(InputStream ins, PrintWriter writer, Map<String, Object> reqParam) throws IOException{
		byte[] buffer = new byte[64 * 1024];
    	
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		for(int len = 0; len >= 0; ){
			len = ins.read(buffer);
			if(len >= 0){
				temp.write(buffer, 0, len);
			}
		}
		Map<String, String> param = new HashMap<String, String>();		
		param.put("request_param", JSONValue.toJSONString(reqParam));
		initVariable(param);
		
		String content = new String(temp.toByteArray(), "UTF-8");
		content = processVariable(content, param);
		writer.print(content);
    }
    protected String processVariable(String content, Map<String, String> param){
    	
    	for(Entry<String, String> entry: param.entrySet()){
    		content = content.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
    	}
    	
    	return content;
    }
    
    protected void initVariable(Map<String, String> param){
    	String stylesheet = "<link href='/static/css/bootstrap.min.css' rel='stylesheet' media='screen'>" +	
    	"<link href='/static/css/page.css' rel='stylesheet' media='screen'>" +	
    	"<script type='text/javascript' language='javascript' src='/static/js/bootstrap.min.js'></script>";
    	
    	param.put("theme", stylesheet);
    	
		param.put("version", Version.getVersion());
		param.put("build_date", Version.getBuildDate());

    }
    
    protected String getMimeTypeWithName(String path){
    	path = path.trim().toLowerCase();
    	if(path.endsWith(".html")){
    		return "text/html";
    	}else if(path.endsWith(".css")){
    		return "text/css";    		
    	}else if(path.endsWith(".js")){
    		return "text/javascript";    	
    	}else if(path.endsWith(".txt")){
    		return "text/plain";   
    	}else if(path.endsWith(".jpeg") || path.endsWith(".jpg")){
    		return "image/jpeg"; 
    	}
    	return "text/html";
    	
    }
    
    protected boolean isText(String path){
    	return getMimeTypeWithName(path).startsWith("text");
    }
    
}
