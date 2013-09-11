package org.emop.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

import org.emop.api.TaodianApi;
import org.emop.cache.Cache;
import org.emop.cache.impl.SimpleCache;
import org.emop.http.HTTPResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mortbay.log.Log;

public class HTTPDataService implements DataService {
	private Cache cache = new SimpleCache();
	private TaodianApi api = null; // new TaodianApi();
	private ThreadPoolExecutor taskPool = null;
	
	public HTTPDataService(TaodianApi api, ThreadPoolExecutor taskPool){
		this.api = api;
		this.taskPool = taskPool;
	}
	
	@Override
	public List<WeiboSendRecord> getUserSendTask(int userId, int timingId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("user_auth_id", userId);
		param.put("timing_id", timingId);
		
		HTTPResult result = (HTTPResult) api.call("timing_waiting_send_list", param);
		
		List<WeiboSendRecord>  r = new ArrayList<WeiboSendRecord>();
		if(result.isOK){
			JSONArray array = (JSONArray)result.json.get("data");
			convertWeiboSendRecord(r, array);
		}
		
		return r;
	}	

	@Override
	public List<WeiboSendRecord> getSendTask(int weiboAppId, int size) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("weibo_app_id", weiboAppId);
		param.put("size", size);
		
		HTTPResult result = (HTTPResult) api.call("timing_waiting_send_list", param);
		List<WeiboSendRecord>  r = new ArrayList<WeiboSendRecord>();

		if(result.isOK){
			JSONArray array = (JSONArray)result.json.get("data");
			convertWeiboSendRecord(r, array);
		}
		
		return r;
	}
	
	private void convertWeiboSendRecord(List<WeiboSendRecord>  r, JSONArray array){
		for(int i = 0; i < array.size(); i++){
			JSONObject obj = (JSONObject)array.get(i);
			WeiboSendRecord send = new WeiboSendRecord();
			send.sid = (Long)obj.get("sid");
			send.userAuthId = Integer.parseInt(obj.get("user_auth_id") + "");
			send.weiboText = (String)obj.get("weibo_text");
			send.imgUrl = (String)obj.get("img_url");
			send.comments = (String)obj.get("comments");
			send.accessToken = (String)obj.get("access_token");
			send.flags = obj.get("flags") + "";
			r.add(send);
		}
	}

	@Override
	public HTTPResult saveSendResult(SendResult r) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("sid", r.sid);
		param.put("msg_id", r.msgId);

		param.put("has_comment", r.hasComment);

		param.put("ret_code", r.retCode);
		param.put("ret_msg", r.retMsg);
		param.put("comment_code", r.commentCode);
		param.put("comment_msg", r.commentMsg);
		
		
		return (HTTPResult) api.call("timing_send_result_update", param);
	}

	@Override
	public void importData(String dataType, BufferedReader reader, DataListener listener) throws IOException {
		if(dataType == null){
			listener.change(DataListener.INIT_ERROR, "数据类型为空", "");
		}
		if(dataType.equals("auth_user")){
			importAuthUserData(reader, listener);
		}else if(dataType.equals("weibo_lib")){
			importWeiboLibData(reader, listener);
		}else if(dataType.equals("timing")){
			importTimingData(reader, listener);
		}else if(dataType.equals("template")){
			importTemplateData(reader, listener);
		}else if(dataType.equals("short_text")){
			importTemplateTextData(reader, listener);
		}else {
			listener.change(DataListener.INIT_ERROR, "未知导入的数据类型:" + dataType, "");			
		}
		
	}

	protected void importAuthUserData(BufferedReader reader, DataListener listener) throws IOException {

		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			line = line.trim();
			if(line.startsWith("#") || line.length() == 0) continue;
			String[] tmp = line.split(";");
			if(tmp.length < 3) continue;
			final Map<String, Object> data = new HashMap<String, Object>();
			
			Log.debug("import user data:" + line);
			//微博应用;登陆名;登陆密码;淘宝PID;用户标签1,用户标签2
			switch(tmp.length){
				case 5:
					data.put("tags", tmp[4].trim());
				case 4:
					data.put("taobao_pid", tmp[3].trim());
				case 3:
					data.put("login_password", tmp[2].trim());
					data.put("login_user", tmp[1].trim());
					data.put("weibo_app_name", tmp[0].trim());
			}
			
			taskPool.execute(new Runnable(){
				@Override
				public void run() {
					api.call("timing_weibo_account_update", data);
				}
				
			});
			listener.change(DataListener.READ_LINE, ".", "");

		}
		listener.change(DataListener.READ_DONE, "数据导入完成", "");
		
	}

	protected void importWeiboLibData(BufferedReader reader, DataListener listener) throws IOException {
		//weibo_lib
		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			line = line.trim();
			if(line.startsWith("#") || line.length() == 0) continue;
			String[] tmp = line.split(";");
			if(tmp.length < 3) continue;
			final Map<String, Object> data = new HashMap<String, Object>();
			
			Log.debug("import weibo lib:" + line);
			//内容来源;类容标签;文本内容;图片地址;商品ID;冒泡库ID;
			switch(tmp.length){
				case 6:
					data.put("emop_lib_id", tmp[5].trim());			
				case 5:
					data.put("num_iid", tmp[4].trim());
				case 4:
					data.put("img_url", tmp[3].trim());
				case 3:
					data.put("weibo_text", tmp[2].trim());
					data.put("tags", tmp[1].trim());
					data.put("source", tmp[0].trim());
			}
			
			taskPool.execute(new Runnable(){
				@Override
				public void run() {
					api.call("timing_weibo_lib_update", data);
				}
				
			});
			listener.change(DataListener.READ_LINE, ".", "");
	
		}		
		listener.change(DataListener.READ_DONE, "数据导入完成", "");
	}

	protected void importTimingData(BufferedReader reader, DataListener listener) throws IOException {
		//boolean is
		final Map<String, Object> data = new HashMap<String, Object>();

		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			line = line.trim();
			if(line.startsWith("#") || line.length() == 0) continue;
			String[] tmp = line.split(";");
			if(tmp.length >=2){
				data.put("name", tmp[0].trim());
				data.put("user_group", tmp[1].trim());				
			}
			if(tmp.length >=3){
				try{
					data.put("status", Integer.parseInt(tmp[2]));
				}catch(Exception e){				
				}
			}
			//status
			listener.change(DataListener.READ_LINE, ".", "");			
			break;
		}

		Pattern pattern = Pattern.compile("^(\\d){2}:(\\d){2}$");
		String timing = "";
		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			line = line.trim();
			if(line.startsWith("#") || line.length() == 0) continue;
			String[] tmp = line.split(";");

			if(tmp.length < 3) continue;
			
			Log.debug("import timing data:" + line);
			//时间点;内容分组;内容标签;发布模版ID
			listener.change(DataListener.READ_LINE, ".", "");
			
			if(pattern.matcher(tmp[0].trim()).matches()){
				timing += line + "\n";
			}
		}		

		data.put("data", timing);
		
		taskPool.execute(new Runnable(){
			@Override
			public void run() {
				api.call("timing_auto_send_update", data);
			}
			
		});
		
		listener.change(DataListener.READ_DONE, "数据导入完成", "");
		
	}

	protected void importTemplateTextData(BufferedReader reader, DataListener listener) throws IOException {
		//weibo_lib
		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			line = line.trim();
			if(line.startsWith("#") || line.length() == 0) continue;
			String[] tmp = line.split(";", 2);
			if(tmp.length < 2) continue;
			final Map<String, Object> data = new HashMap<String, Object>();
			
			Log.debug("import template text:" + line);
			//分组名;文本
			
			switch(tmp.length){
				case 2:
					data.put("text", tmp[1].trim());
					data.put("group_name", tmp[0].trim());
			}
			
			taskPool.execute(new Runnable(){
				@Override
				public void run() {
					api.call("timing_template_text_update", data);
				}
				
			});
			listener.change(DataListener.READ_LINE, ".", "");
	
		}		
		listener.change(DataListener.READ_DONE, "数据导入完成", "");
	}
	
	protected void importTemplateData(BufferedReader reader, DataListener listener) throws IOException {
		//weibo_lib
		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			line = line.trim();
			if(line.startsWith("#") || line.length() == 0) continue;
			String[] tmp = line.split(";");
			if(tmp.length < 3) continue;
			final Map<String, Object> data = new HashMap<String, Object>();
			
			Log.debug("import weibo template:" + line);
			//模版名;发送应用;短网址域名;是否放到评论;内容后缀;评论前缀
			
			switch(tmp.length){
				case 6:
					data.put("weibo_comment", tmp[5].trim());
				case 5:
					data.put("weibo_text", tmp[4].trim());				
				case 4:
					String[] ff = tmp[3].trim().split(",", 2);
					data.put("link_in_comments", ff[0]);
					if(ff.length > 1){
						data.put("flags", ff[1]);
					}
				case 3:
					data.put("link_domain", tmp[2].trim());
					data.put("weibo_app_name", tmp[1].trim());
					data.put("template_name", tmp[0].trim());
			}
			
			taskPool.execute(new Runnable(){
				@Override
				public void run() {
					api.call("timing_content_template_update", data);
				}
				
			});
			listener.change(DataListener.READ_LINE, ".", "");
	
		}		
		listener.change(DataListener.READ_DONE, "数据导入完成", "");
	}	
	
	@Override
	public List<WeiboTaskItem> getWeiboActionTask(int appId, String queue,
			int size) {
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("queue_name", queue);
		if(appId > 0){
			param.put("app_id", appId);
		}
		param.put("size", size);
		
		HTTPResult result = (HTTPResult) api.call("queue_task_list", param);
		
		List<WeiboTaskItem>  r = new ArrayList<WeiboTaskItem>();
		if(result.isOK){
			JSONArray array = (JSONArray)result.json.get("data");
			for(int i = 0; i < array.size(); i++){
				JSONObject obj = (JSONObject)array.get(i);
				WeiboTaskItem task = new WeiboTaskItem();
				
				task.taskId = Long.parseLong(obj.get("task_id") + "");
				if(obj.containsKey("action")){
					task.action = obj.get("action") + "";
					obj.remove("action");
				}
				if(task.action == null || task.action.trim().length() == 0){
					task.action = queue;
				}
				task.authId = obj.get("user_auth_id") + "";
				if(task.authId == null || task.authId.trim().length() == 0){
					task.authId = appId + "";
				}
				obj.remove("task_id");
				if(obj.containsKey("user_auth_id")){
					obj.remove("user_auth_id");
				}
				task.param.putAll(obj);				
				r.add(task);
			}
		}
		return r;
	}
	
	
}
