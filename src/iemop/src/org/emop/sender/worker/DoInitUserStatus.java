package org.emop.sender.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.api.TaodianApi;
import org.emop.http.HTTPResult;
import org.emop.model.WeiboTaskItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import weibo4j.Friendships;
import weibo4j.model.User;
import weibo4j.model.UserWapper;
import weibo4j.model.WeiboException;

/**
 * 支持导入授权账号的粉丝，参与互粉功能。
 * @author deonwu
 *
 */
public class DoInitUserStatus implements Runnable{
	private Log log = LogFactory.getLog("weibo.fans.import");
	
	private TaodianApi api = null;
	private WeiboTaskItem task = null;
	
	public DoInitUserStatus(WeiboTaskItem task, TaodianApi api){
		this.api = api;
		this.task = task;
	}
	
	@Override
	public void run() {
		Friendships fm = new Friendships();		
		log.info("start init follow status， out id:" + task.param.get("user_out_id"));		
		
		String token = task.param.get("access_token");
		if(token == null || token.length() == 0){
			log.info("Not not found access token");		

			return;
		}
		fm.client.setToken(task.param.get("access_token"));
		
		
		this.updateFansList(fm);
		this.updateFriendsList(fm);
		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put("user_out_id", task.param.get("user_out_id"));
		p.put("action", "start_task");

		HTTPResult r = api.call("fans_user_status_change", p);
		
		if(r.isOK){
			log.info("import user status ok");
		}
		
		p = new HashMap<String, Object>();
		p.put("task_id", task.taskId);
		r = api.call("queue_done_task", p);

	}

	private void updateFriendsList(Friendships api){
		int pageSize = 1500;
		String userId = task.param.get("user_out_id");
		try {
			for(int i = 0; i < 80; i++){		
				String[] ids = api.getFriendsIdsByUid(userId, pageSize, pageSize * i);
			
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("user_out_id", userId);
				
				List<String> idList = new ArrayList<String>();
				for(String e:ids){
					idList.add(e);
				}
				p.put("friends_list", idList);
								
				long st = System.currentTimeMillis();
				HTTPResult r = this.api.call("fans_import_user_data", p);
				
				long et = System.currentTimeMillis() - st;
				if(r.isOK){
					log.info("import user friends, size:" + ids.length + ", elpased:" + et);
				}
				
				if(ids.length < pageSize / 2){
					break;
				}
			}

		} catch (WeiboException e) {
			if(e.getErrorCode() == 21327){
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("user_out_id", userId);
				p.put("user_status", "expired");
				this.api.call("timing_weibo_account_update", p);
			}
			log.error(e.toString(), e);
		}
	}
	
	private void updateFansList(Friendships api){
		int pageSize = 1500;
		String userId = task.param.get("user_out_id");
		try {
			for(int i = 0; i < 80; i++){		
				String[] ids = api.getFriendsIdsByUid(userId, pageSize, pageSize * i);
			
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("user_out_id", userId);
				
				List<String> idList = new ArrayList<String>();
				for(String e:ids){
					idList.add(e);
				}
				p.put("fans_list", idList);
				
				long st = System.currentTimeMillis();
				HTTPResult r = this.api.call("fans_import_user_data", p);
				
				long et = System.currentTimeMillis() - st;
				if(r.isOK){
					log.info("import user fans, size:" + ids.length + ", elpased:" + et);
				}
				
				if(ids.length < pageSize / 2){
					break;
				}
			}

		} catch (WeiboException e) {
			log.error(e.toString(), e);
		}
	}
	
}
