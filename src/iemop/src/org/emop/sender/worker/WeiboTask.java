package org.emop.sender.worker;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.cache.Cache;
import org.emop.cache.impl.SimpleCache;
import org.emop.http.HTTPClient;
import org.emop.http.HTTPResult;
import org.emop.model.SendResult;
import org.emop.model.WeiboSendRecord;
import org.emop.sender.ImageLoader;
import org.mortbay.jetty.security.Credential.MD5;

import weibo4j.Comments;
import weibo4j.Timeline;
import weibo4j.http.ImageItem;
import weibo4j.model.Comment;
import weibo4j.model.Status;
import weibo4j.model.WeiboException;

public class WeiboTask {
	private Log log = LogFactory.getLog("weibo.sender");
	private static Cache cache = new SimpleCache();
	private HTTPClient client = null;
	
	public SendResult send(WeiboSendRecord weibo, ImageLoader loader){
		SendResult r = new SendResult();
		
		r.sid = weibo.sid;
		r.retCode = "ok";
		try{
			checkTextInImage(weibo, loader);
			Status st = doSendStatus(weibo, loader);
			if(st != null){
				r.msgId = st.getMid();				
			}
			String str = String.format("Send weibo:%s, token:%s, text:%s,  url:%s, comments:%s, result:[%s], ret:[%s]", 
					weibo.sid, weibo.accessToken, weibo.weiboText, weibo.imgUrl, weibo.comments, r.msgId, r.retCode);
			
			log.info(str);				
		}catch (WeiboException e) {
			r.retCode = e.getErrorCode() + "";
			r.retMsg = e.getError();
			String info = String.format("Send weibo:%s, token:%s, text:%s,  url:%s, comments:%s, error:[%s], ret:[%s]", 
					weibo.sid, weibo.accessToken, weibo.weiboText, weibo.imgUrl, weibo.comments, r.retMsg, r.retCode);			
			log.error(info + "\n" + e.toString(), e);
		}
		
		if(weibo.comments != null && weibo.comments.length() > 0){
			r.hasComment = "y";
			if(r.msgId != null && r.msgId.length() > 0){
				try {
					createComment(weibo.accessToken, weibo.comments, r.msgId);
				} catch (WeiboException e) {
					log.error(e.toString(), e);
					r.retCode = e.getErrorCode() + "";
					r.retMsg = e.getError();
				}
			}
		}
		
		return r;
	}
	
	protected Status doSendStatus(WeiboSendRecord weibo, ImageLoader loader) throws WeiboException{
		
		Timeline tm = new Timeline();
		tm.client.setToken(weibo.accessToken);
		byte[] data = null;
		if(weibo.imgUrl != null && weibo.imgUrl.length() > 0){
			data = loader.loadPicUrl(weibo.imgUrl);
		}
		Status status = null;
		if(data != null){
			ImageItem image = new ImageItem(data);		
			status = tm.UploadStatus(weibo.weiboText, image);
		}else {
			status = tm.UpdateStatus(weibo.weiboText);	
		}
		
		return status;
	}
	
	protected Comment createComment(String token, String comment, String msgId) throws WeiboException{
		Comments cm = new Comments();
		cm.client.setToken(token);
		Comment result = cm.createComment(comment, msgId);
		
		return result;
	}
	
	/**
	 * 检查是否有，文案放图片的标志。如果有就把文案显示到图片里面。
	 * @param weibo
	 * @param loader
	 */
	protected void checkTextInImage(WeiboSendRecord weibo, ImageLoader loader){
		String flag = weibo.getFlag(WeiboSendRecord.TEXT_IN_IMG);
		log.info("text in image flag:" + flag + ", flgs:" + weibo.flags);
		if(flag.equals("y")){
			String imgCache = MD5.digest(weibo.weiboText + weibo.imgUrl);
			Object url = cache.get(imgCache);
			log.info("Get text image with cache:" + imgCache);
			if(url == null){
				url = this.getIamgeUrl(weibo.weiboText, weibo.imgUrl);
				if(url != null){
					cache.set(imgCache, url, 60 * 5);
				}
			}
			if((url + "").startsWith("http:")){
				weibo.imgUrl = url + "";
				if(weibo.comments != null && weibo.comments.indexOf("XXX") > 0){
					String[] tmp = weibo.comments.split("XXX", 2);
					if(tmp.length > 1){
						weibo.weiboText = tmp[0];
						weibo.comments = tmp[1];
					}
				}else {
					weibo.weiboText = "[呵呵]";
				}
			}
		}
	}
	
	private String getIamgeUrl(String text, String url){
		if(client == null){
			client = HTTPClient.create();
		}
		HashMap<String, Object> p = new HashMap<String, Object>();
		p.put("img", url);
		p.put("text", text);
		HTTPResult r = client.post("http://3.emopwx.sinaapp.com/app/image/app/text_image", p);
		
		return r.getString("image_url");		
	}

}
