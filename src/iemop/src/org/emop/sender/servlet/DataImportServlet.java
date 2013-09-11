package org.emop.sender.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.model.DataListener;
import org.emop.sender.HttpServer;


public class DataImportServlet extends HttpServlet {
	private Log log = LogFactory.getLog("wx.http");

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
    	doPost(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
    	
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding("utf8");
		
		if(HttpServer.ins.authManager.authCheck(request)){
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			
			if(isMultipart){
				ServletFileUpload upload = new ServletFileUpload();
				FileItemIterator iter = null;
				String encoding = "";
				String dataType = "";
				
				try{
					iter = upload.getItemIterator(request);				
					
					while (iter.hasNext()) {
					    FileItemStream item = iter.next();
					    String name = item.getFieldName();
					    InputStream stream = item.openStream();
					    if (item.isFormField()) {
					    	if(name.equals("data_encoding")){
					    		encoding = Streams.asString(stream);
					    	}else if(name.equals("data_type")){
					    		dataType = 	Streams.asString(stream);
					    	}
					    } else {
					    	importData(dataType, encoding, stream, response);
					    }
					}
				}catch(Exception e){
					log.error(e.toString(), e);
				}
			}else {
				response.setContentType("text/plain");
				response.getWriter().println("没有文件上传。");				
			}
			
		}else {
			response.setContentType("text/plain");
			response.getWriter().println("请登陆后完成操作。");
		}
    }
    
    protected void importData(String type, String encoding, InputStream in, HttpServletResponse response) throws IOException{
    	//HttpServer.ins.dataService.importData(dataType, reader, listener);	
    	BufferedReader reader = null;
    	if(encoding != null){
    		encoding = "UTF-8";
    	}
    	try {
			reader = new BufferedReader(new InputStreamReader(in, encoding));
		} catch (UnsupportedEncodingException e) {
			log.error(e.toString(), e);
		}
    	
    	final PrintWriter writer = response.getWriter();
    	
    	writer.println("开始上传文件....");    	
    	HttpServer.ins.dataService.importData(type, reader, new DataListener(){

			@Override
			public void change(int status, String msg, String data) {
				if(status == DataListener.READ_LINE){
					writer.print(". ");
				}else if(status == DataListener.READ_DONE) {
					writer.println("\n数据导入：" + msg);
				}
			}
    	});
    }
    
 
    
}
