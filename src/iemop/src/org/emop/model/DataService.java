package org.emop.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.emop.http.HTTPResult;

public interface DataService {

	public List<WeiboSendRecord> getSendTask(int weiboAppId, int size);
	
	public HTTPResult saveSendResult(SendResult r);

	public void importData(String dataType, BufferedReader reader, DataListener listener) throws IOException;
	
	public List<WeiboTaskItem> getWeiboActionTask(int appId, String queue, int size);	
	
	public List<WeiboSendRecord> getUserSendTask(int userId, int timingId);
}
