package org.emop.model;

import java.io.Serializable;
import java.util.Date;

public class WeiboSendRecord implements Serializable{
	public static final int TEXT_IN_IMG = 1;

	private static final long serialVersionUID = -8188681653093236090L;
	
	/**
	 * 刚创建，还没有做内容检查和格式化。比如淘客链接转换什么的。
	 */
	public static final int STATUS_NEW = 0;
	
	/**
	 * 内容准备完成，等待发送。
	 */
	public static final int STATUS_READY = 10;

	/**
	 * 文案出错。
	 */
	public static final int STATUS_ERROR = 12;
	

	/**
	 * 账号或应用状态出错，不能发送。例如，授权过期，禁用等。
	 */
	public static final int STATUS_NO_SEND = 13;

	
	/**
	 * 已经放到发送队列，进行发送中。
	 */
	public static final int STATUS_SENDING = 20;
	
	/**
	 * 发送成功
	 */
	public static final int STATUS_SENDED_OK = 31;

	/**
	 * 发送失败
	 */
	public static final int STATUS_SENDED_ERR = 32;
	
	public long timingId = 0;
	public int appId;
	public int userAuthId;
	public int status;
	public int source;
	public String weiboText;
	public String imgUrl;
	public int libId;
	public long shopId;
	public long numIid;

	public Date sendTime;	
	public Date realSendTime;
	public Date createTime;
	
	public String accessToken;
	public String comments;
	public String flags;
	
	/**
	 * 在有些业务里面，需要检查微博应用。
	 */
	public long weiboAppId = 0;
	/**
	 * 发送队列中的ID，只在接口里面用的。
	 */
	public long sid;

	public int hashCode(){
		return (int)((timingId * 100) + userAuthId % 100);
	}
	
	public String getFlag(int index){
		if(this.flags == null) return "";
		String[] tmp = this.flags.split(",");
		if(tmp.length < index) return "";
		return tmp[index - 1].trim();
	}	
	
	public boolean equals(Object o){
		if(o instanceof WeiboSendRecord){
			return o.hashCode() == this.hashCode();
		}else {
			return false;
		}
	}
}
