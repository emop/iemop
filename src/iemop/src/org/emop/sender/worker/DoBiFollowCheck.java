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

import weibo4j.Friendships;
import weibo4j.model.Paging;
import weibo4j.model.WeiboException;

/**
 * 支持导入授权账号的粉丝，参与互粉功能。
 * @author deonwu
 *
 */
public class DoBiFollowCheck implements Runnable{
	private Log log = LogFactory.getLog("weibo.fans.import");
	
	private static Lock lock = new ReentrantLock();
	private TaodianApi api = null;
	private WeiboTaskItem task;

	public DoBiFollowCheck(WeiboTaskItem task, TaodianApi api){
		this.api = api;
		this.task = task;
	}
	
	@Override
	public void run() {
		Friendships fm = new Friendships();
		fm.client.setToken(task.param.get("access_token"));

		int pageSize = 500;
		String userId = task.param.get("user_out_id");
		
		if(WeiboSender.ins.rateLimit.get(userId) != null){
			log.info("ignore action rate limit. out id:" + userId);
			return;
		}
		
		try {
			Paging page = new Paging();
			page.setCount(pageSize);
			for(int i = 0; i < 80; i++){
				page.setPage(i + 1);
				String[] ids = fm.getFriendsBilateralIds(userId, 0, page);
			
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("user_out_id", userId);
				
				List<String> idList = new ArrayList<String>();
				for(String e:ids){
					idList.add(e);
				}
				p.put("fans", idList);
				
				p.put("action", "bi_follow_list");
				
				long st = System.currentTimeMillis();
				HTTPResult r = this.api.call("fans_user_status_change", p);
				
				long et = System.currentTimeMillis() - st;
				if(r.isOK){
					log.info("import user bilatreral ids, size:" + ids.length + ", elpased:" + et);
				}
				
				if(ids.length < pageSize / 2){
					break;
				}
			}

		} catch (WeiboException e) {
			log.error(e.toString(), e);
			if(e.getErrorCode() >= 10022 && e.getErrorCode() <= 10024){
				WeiboSender.ins.rateLimit.set(userId, e.getErrorCode(), 60 * 15);
			}			
		}
		
		Map<String, Object> p = new HashMap<String, Object>();		
		p = new HashMap<String, Object>();
		p.put("task_id", task.taskId);
		api.call("queue_done_task", p);		
	}


	
}
