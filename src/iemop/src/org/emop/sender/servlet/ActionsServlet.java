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
import org.emop.sender.WeiboSender;
import org.json.simple.JSONValue;


public class ActionsServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
    	doPost(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding("utf8");
		response.setContentType("text/plain");
		
		String action = request.getParameter("action");
		if(action != null && action.equals("send_one")){
			doSendOne(request, response);
		}		
    }
    
    private void doSendOne(HttpServletRequest request, HttpServletResponse response) throws NumberFormatException, IOException{
    	String uid = request.getParameter("uid");
    	String tid = request.getParameter("tid");  
    	uid = uid == null ? 0 + "" : uid;
    	tid = tid == null ? 0 + "" : tid;
    	WeiboSender.ins.sendOne(Integer.parseInt(uid), Integer.parseInt(tid), 
    			response.getWriter());
    }
    
}
