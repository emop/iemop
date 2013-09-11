package org.emop.sender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.model.DataService;
import org.emop.model.SendResult;
import org.emop.model.WeiboSendRecord;
import org.emop.model.WeiboTaskItem;
import org.emop.sender.worker.WeiboTask;


public class WeiboSender {
	private Log log = LogFactory.getLog("weibo.sender");

	/**
	 * 最多只能有500条微博在发送队列中。
	 */
	private static final int MAX_SENDING_QUEUE = 500;
	/**
	 * 发送微博的线程池
	 */
	protected ThreadPoolExecutor weiboPool = null;
	
	/**
	 * 任务跟踪，冒泡API通信的线程池。
	 */
	protected ThreadPoolExecutor taskPool = null;
	
	protected DataService api = null;

	/**
	 * 保存已经发送过的任务。避免重复发送。
	 */
	private ArrayBlockingQueue<Long> doneTask = new ArrayBlockingQueue<Long>(MAX_SENDING_QUEUE * 20);
	
	private ArrayBlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<Runnable>(MAX_SENDING_QUEUE);
	
	private Timer timer = new Timer();
	
	public static WeiboSender ins = null;
	public ImageLoader imageLoader = null;
	public File taskRoot = null;
	public int taskId = 1;
	/**
	 * 在执行发送任务的微博应用列表。
	 */
	public List<Integer> weiboApps = new ArrayList<Integer>(); 

	public List<String> actionQueue = new ArrayList<String>(); 
	
	public WeiboSender(DataService api){
		this.api = api;
		
		ins = this;
	}
	
	public void start(List<Integer> weibos, ImageLoader loader, ThreadPoolExecutor taskExecutor){
		this.imageLoader = loader;
		this.weiboApps.addAll(weibos);
		
		/**
		 * 小于coreSize自动增加新线程，
		 * 大于coreSize放到Queue里面，
		 * Queue满后开始创建新线程，至到maxSize
		 */
		int core_thread_count = 50;
		weiboPool = new ThreadPoolExecutor(
				core_thread_count,
				core_thread_count,
				10, 
				TimeUnit.SECONDS, 
				taskQueue
				);
		this.taskPool = taskExecutor;

		
		/**
		 * 每隔 3 秒检查一次发送队列。
		 */
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				weiboPool.execute(new WeiboSendTaskWorker());
				taskPool.execute(new WeiboActionTaskWorker());
			}			
		}, 1000L, 3000L);	
		
	}
	
	public void checkWeiboTask(int appId, String queue){
		List<WeiboTaskItem> tasks = api.getWeiboActionTask(appId, queue, 50);
		if(tasks.size() > 0){
			log.info("get weibo action task, app id:" + appId + ", queue:" + queue + ", size:" + tasks.size());
			PrintWriter writer = null;
			try {
				writer = getTaskWriter();
				for(WeiboTaskItem i : tasks){
					String t = String.format("%s:%s-->%s@%s", i.taskId, i.authId, i.action, i.encodeParam());
					writer.println(t);
					log.info("task:" + t);
				}				
			} catch (Exception e) {
				log.error(e.toString(), e);
			}finally{
				if(writer != null){
					writer.flush();
					writer.close();
				}
			}
		}
	}
		
	protected PrintWriter getTaskWriter() throws FileNotFoundException, UnsupportedEncodingException{
		taskId = (taskId + 1) % 10000 + 1;
		String name = String.format("%04d.waiting", taskId);
		File f = new File(taskRoot, name);
		log.info("write task to:" + f.getAbsolutePath());
		return new PrintWriter(f, "UTF-8"); 		
	}
	
	
	public void sendOne(int userId, int timingId, PrintWriter out){
		out.println("Loading task:" + timingId + ", user id:" + userId);
		List<WeiboSendRecord> data = api.getUserSendTask(userId, timingId);
		if(data.size() > 0){
			WeiboSendRecord r = data.get(0);
			WeiboTask task = new WeiboTask();
			
			out.println("Start send task:" + r.timingId + ", flags:" + r.flags);
			out.println("weibo imgUrl:" + r.imgUrl);
			out.println("weibo text:" + r.weiboText);
			out.println("weibo comment:" + r.comments);
			
			final SendResult s = task.send(r, imageLoader);
			
			out.println("Weobo Result:" + s.retCode + ", msg:" + s.retMsg);
			out.println("Comment Result:" + s.commentCode + ", msg:" + s.commentMsg);
			out.println("DONE");
			taskPool.execute(new Runnable(){
				public void run(){
					api.saveSendResult(s);
				}
			});	
			
		}else {
			out.println("Not found recored with tid:" + timingId);
		}
	}
	
	class WeiboWorker implements Runnable{
		private WeiboSendRecord r =  null;
		public WeiboWorker(WeiboSendRecord send){
			this.r = send;
		}

		@Override
		public void run() {
			WeiboTask task = new WeiboTask();
			
			final SendResult s = task.send(r, imageLoader);
			taskPool.execute(new Runnable(){
				public void run(){
					api.saveSendResult(s);
				}
			});				
		}
	}
	
	class WeiboActionTaskWorker implements Runnable{
		
		@Override
		public void run() {
			try{
				//log.debug("get weibo action task:" + actionQueue.size());
				for(String queue : actionQueue){
					int appId = 0;
					String queueName = "";
					String[] tmp = queue.split(",");
					if(tmp.length == 1){
						queueName = tmp[0];
					}else if(tmp.length > 1){
						appId = Integer.parseInt(tmp[0].trim());
						queueName = tmp[1].trim();
					}
					
					final int pAppId = appId;
					final String pQueue = queueName;
					taskPool.execute(new Runnable(){
						public void run() {
							checkWeiboTask(pAppId, pQueue);
						}
					});
				}
			}catch(Throwable e){
				log.error(e.toString(), e);
			}
		}		
	}	
	
	class WeiboSendTaskWorker implements Runnable{
		
		@Override
		public void run() {
			try{
				for(int weiboAppId : weiboApps){
					int capacity = taskQueue.remainingCapacity();
					List<WeiboSendRecord> tasks = api.getSendTask(weiboAppId, capacity);
					if(tasks == null){
						log.info("Failed to get weibo task....");
						continue;
					}
					log.info("Get weibo task, weibo app id:" + weiboAppId +" count:" + tasks.size());
					for(WeiboSendRecord task : tasks){
						if(doneTask.contains(task.sid)) {
							continue;
						}else {
							log.info("Add weibo send task, :" + task.sid);							
							doneTask.add(task.sid);
							weiboPool.execute(new WeiboWorker(task));
						}
					}
				}
			}catch(Throwable e){
				log.error(e.toString(), e);
			}
		}		
	}	
}
