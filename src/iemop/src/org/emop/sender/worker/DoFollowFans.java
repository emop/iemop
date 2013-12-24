package org.emop.sender.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.api.TaodianApi;
import org.emop.http.HTTPResult;
import org.emop.model.WeiboTaskItem;
import org.emop.sender.WeiboSender;

import weibo4j.Friendships;
import weibo4j.model.User;
import weibo4j.model.WeiboException;

/**
 * 支持导入授权账号的粉丝，参与互粉功能。
 * @author deonwu
 *
 */
public class DoFollowFans implements Runnable{
	private Log log = LogFactory.getLog("weibo.fans.import");
	
	private static Lock lock = new ReentrantLock();
	private TaodianApi api = null;
	private WeiboTaskItem task;
	
	public DoFollowFans(WeiboTaskItem task, TaodianApi api){
		this.api = api;
		this.task = task;
	}
	
	@Override
	public void run() {
		Friendships fm = new Friendships();
		fm.client.setToken(task.param.get("access_token"));
		
		String outId = task.param.get("user_out_id");
		String fansId = task.param.get("fans_out_id");
		
		log.info("do follow out id:" + outId + "-->" + fansId);
		
		if(WeiboSender.ins.rateLimit.get(outId) != null){
			log.info("ignore action rate limit. out id:" + outId);
			return;
		}
		
		HTTPResult r = null;
		int retCode = 1;

		try {
			User user = fm.createFriendshipsById(fansId);
			
			log.info("do follow ok, user id:" + outId + "-->" + fansId);		
			
		} catch (WeiboException e) {
			log.error("follow:" + outId + "-->" + fansId + ":" + e.toString(), e);
			retCode = e.getErrorCode();
			WeiboSender.ins.rateLimit.set(outId, e.getErrorCode(), 60 * 15);
			if(e.getErrorCode() >= 10022 && e.getErrorCode() <= 10024){
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("user_out_id", outId);
				p.put("action", "follow_fast");
				r = api.call("fans_user_status_change", p);				
				if(r.isOK){
					log.info("follow action to fast");
				}
			}else if(e.getErrorCode() == 21327){
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("out_id", outId);
				p.put("user_status", "expired");
				r = api.call("timing_weibo_account_update", p);				
				if(r.isOK){
					log.info("account is expired.");
				}				
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
		p.put("ret_code", retCode);
		p.put("action", "do_follow");

		r = api.call("fans_user_status_change", p);
		
		if(r.isOK){
			log.info("update do follow status ok");
		}
		
		
		p = new HashMap<String, Object>();
		p.put("task_id", task.taskId);
		r = api.call("queue_done_task", p);		
	}

	

	
}
