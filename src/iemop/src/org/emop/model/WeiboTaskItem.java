package org.emop.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WeiboTaskItem {
	public long taskId;
	public String action;
	public String authId;
	
	public Map<String, String> param = new HashMap<String, String>();
	
	public String encodeParam(){
		StringBuffer b = new StringBuffer();
		for(Entry<String, String> e : param.entrySet()){
			try {
				b.append(e.getKey() + "=" + URLEncoder.encode(e.getValue(), "UTF-8") + ",");
			} catch (UnsupportedEncodingException e1) {
			}
		}
		return b.toString();
	}
}
