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
import org.emop.sender.WeiboSender;
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
public class DoCancelFollowFans implements Runnable{
	private Log log = LogFactory.getLog("weibo.fans.import");
	
	private static Lock lock = new ReentrantLock();
	private TaodianApi api = null;
	private WeiboTaskItem task = null;
	
	public DoCancelFollowFans(WeiboTaskItem task, TaodianApi api){
		this.api = api;
		this.task = task;
	}
	
	
	@Override
	public void run() {
		Friendships fm = new Friendships();
		fm.client.setToken(task.param.get("access_token"));
		
		String outId = task.param.get("user_out_id");
		String fansId = task.param.get("fans_out_id");
		
		log.info("cance  out id:" + outId + "-->" + fansId);
		
		HTTPResult r = null;
		int retCode = 1;
		try {
			User user = fm.destroyFriendshipsDestroyById(fansId);
			log.info("do cancel follow ok, user id:" + user.getId());

		} catch (WeiboException e) {
			log.error("cancel:" + outId + "-->" + fansId + ":" + e.toString(), e);
			retCode = e.getErrorCode();
			if(e.getErrorCode() >= 10022 && e.getErrorCode() <= 10024){
				WeiboSender.ins.rateLimit.set(outId, e.getErrorCode(), 60 * 15);
			}
		}
		
		/**
		 * 无论是否成功都把，取消标志设置完成。避免一些错误阻塞队列。
		 * 例如：
		 * 1. 用户未关注
		 */
		Map<String, Object> p = new HashMap<String, Object>();
		p.put("user_out_id", outId);
		p.put("fans_out_id", fansId);

		p.put("action", "cancel_follow");
		p.put("ret_code", retCode);

		r = api.call("fans_user_status_change", p);			
		if(r.isOK){
			log.info("update status cancel status is ok");
		}
		
		
		p = new HashMap<String, Object>();
		p.put("task_id", task.taskId);
		r = api.call("queue_done_task", p);
		
	}
	
}
