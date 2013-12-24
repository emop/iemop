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
public class ImportWeiboFansTask implements Runnable{
	private Log log = LogFactory.getLog("weibo.fans.import");
	
	private static Lock lock = new ReentrantLock();
	private TaodianApi api = null;
	
	public ImportWeiboFansTask(TaodianApi api){
		this.api = api;
	}
	
	@Override
	public void run() {
		if(lock.tryLock()){
			try{
				importFans();
			}catch(Throwable e){
				log.error(e.toString(), e);
			}finally{
				lock.unlock();
			}
		}
		
	}

	
	private void importFans(){
		Map<String, Object> p = new HashMap<String, Object>();
		p.put("user_status", "fans_new");
		p.put("app_id", "2");
		//p.put("", value)
		
		HTTPResult r = api.call("timing_weibo_account_list", p);		
		if(r.json == null) return;
		
		JSONObject data = (JSONObject)r.json.get("data");
		if(data == null) return;
		
		JSONArray accountList = (JSONArray)data.get("data");
		if(accountList == null) return;
		
		log.info("Get new account list, size:" + accountList.size() + ", all account:" + data.get("count"));
		for(int i = 0; i < accountList.size(); i++){
			data = (JSONObject)accountList.get(i);
			importAccountFans(data);
		}
		//api.g
		
	}
	
	private void importAccountFans(final JSONObject account){
		log.info("Import fans from account:" + account.get("user_auth_id") + ", out_id:" + account.get("out_id") + ",nick:" + account.get("nick"));
		
		String userId = account.get("out_id") + "";
		if(WeiboSender.ins.rateLimit.get(userId) != null){
			log.info("ignore action rate limit. out id:" + userId);
			return;
		}		
		
		Friendships fm = new Friendships();
		fm.client.setToken(account.get("auth_key") + "");
		
		//try{
		UserWapper users = null;
		int nextPage = 0;
		int pageSize = 200;
		try {
			for(int i = 0; i < 80; i++){		
				users = fm.getFollowersById(account.get("out_id") + "", pageSize, nextPage);
				nextPage = (int) users.getNextCursor();
				
				List<String> data = new ArrayList<String>();
				for(User u : users.getUsers()){
					String fans = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", 
							u.getId(), u.getName(), u.getProfileImageUrl(),
							u.getGender(), u.isVerified() ? 1 : 0, u.getProvince(),
							u.getCity(), u.getFollowersCount(), u.getFriendsCount(),
							u.getStatusesCount()
							);
					data.add(fans);
					//log.info("fans data:" + fans);
				}

				log.info("import fans count:" + data.size() + ", cursor:" + nextPage);				
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("data", data);
				
				long st = System.currentTimeMillis();
				HTTPResult r = api.call("fans_import_fans_data", p);
				
				long et = System.currentTimeMillis() - st;
				if(r.isOK){
					log.info("import fans ok, elapsed:" + et);
				}else if(r.errorCode != null && r.errorCode.equals("too_fast")){
					try {
						log.info("The server response too fast, sleep 10s.");						
						Thread.sleep(10 * 1000);
						r = api.call("fans_import_fans_data", p);
					} catch (InterruptedException e) {						
					}
				}
				
				if(users.getUsers().size() < pageSize / 2 || nextPage == 0){
					break;
				}
				
			}

		} catch (WeiboException e) {
			log.error(e.toString(), e);
			WeiboSender.ins.rateLimit.set(userId, e.getErrorCode(), 60 * 15);
			if(e.getErrorCode() >= 10022 && e.getErrorCode() <= 10024){
			}else if(e.getErrorCode() == 21327){
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("out_id", userId);
				p.put("user_status", "expired");
				HTTPResult r = api.call("timing_weibo_account_update", p);				
				if(r.isOK){
					log.info("follow action to fast");
				}				
			}				
		}
			
		Map<String, Object> p = new HashMap<String, Object>();
		p.put("user_auth_id", account.get("user_auth_id"));
		p.put("user_status", "imported");

		log.info("change the user status as 'imported', auth id:" + account.get("user_auth_id"));		
		HTTPResult r = api.call("timing_weibo_account_update", p);
		
		if(r.isOK){
			log.info("import fans ok");
		}

	}
	
}
