package org.emop.sender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.api.TaodianApi;
import org.emop.model.DataService;
import org.emop.model.HTTPDataService;
import org.emop.monitor.StatusMonitor;
import org.emop.sender.settings.Settings;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;


public class HttpServer {
	public Settings settings = null;
	private Log log = LogFactory.getLog("wx.http");
	
	//public TimingService timingService = null;
	public AuthenticationManager authManager = null;
	public TaodianApi api = null;
	public DataService dataService = null;
	public static HttpServer ins = null;
	
	public int httpPort = 0;
	protected ThreadPoolExecutor taskPool = null;

	
	public HttpServer(Settings s){
		this.settings = s;
		
		ins = this;
	}	
	
	public void run(){
		StatusMonitor.startMonitor();		
		loadApiSettings();
		
		authManager = new AuthenticationManager();
		
		taskPool = new ThreadPoolExecutor(
				50,
				50 * 2,
				10, 
				TimeUnit.SECONDS, 
				new ArrayBlockingQueue<Runnable>(500)
				);		
		//timingService = new TimingService(api);
		dataService = new HTTPDataService(api, taskPool);
		
		WeiboSender sender = new WeiboSender(dataService);
		
		String weibos = settings.getString(Settings.WEIBO_APP_ID, "");
		
		log.info("Starting send weibo app:" + weibos);
		List<Integer> ids = new ArrayList<Integer>();
		for(String id : weibos.split(",")){
			try{
				if(id.trim().length() > 0){
					ids.add(Integer.parseInt(id.trim()));
				}
			}catch(Exception e){				
			}
		}
		
		String root = settings.getString(Settings.WEIBO_TASK_ROOT, "webrobot");
		sender.taskRoot = new File(root);
		if(!sender.taskRoot.isDirectory()){
			sender.taskRoot.mkdirs();
		}
		
		String followQueue = settings.getString(Settings.WEIBO_TASK_QUEUE, "");
		
		log.info("Starting follow task queue:" + followQueue);
		for(String id : followQueue.split(";")){
			if(id.trim().length() > 0){
				log.info("add follow task queue:" + id);
				sender.actionQueue.add(id);
			}
		}
		
		
		String cache = settings.getString(Settings.IMAGE_CACHE_ROOT, "imageCache");
		ImageLoader loader = new ImageLoader(new File(cache));
		
		sender.start(ids, loader, taskPool);
		
		startHTTPServer();
	}
	
	private void startHTTPServer(){
		httpPort = settings.getInt(Settings.HTTP_PORT, -1);
		Server server = new Server(httpPort);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        
        if(settings.getInt("disable_gui", 0) == 0){        
	        handler.addServletWithMapping("org.emop.sender.servlet.DataImportServlet", "/import/*");
	        handler.addServletWithMapping("org.emop.sender.servlet.ApiProxyServlet", "/api/*");
	        handler.addServletWithMapping("org.emop.sender.servlet.ActionsServlet", "/actions/*");
	        handler.addServletWithMapping("org.emop.sender.servlet.StatusServlet", "/status/*");
	        handler.addServletWithMapping("org.emop.sender.servlet.StaticServlet", "/static/*");
	        handler.addServletWithMapping("org.emop.sender.servlet.StaticServlet", "/");
        }else {
	        handler.addServletWithMapping("org.emop.sender.servlet.StatusServlet", "/*");
        }
        
        try {
        	log.info("Start http server at " + httpPort);
			server.start();
			server.join();
		} catch (Exception e) {
			httpPort = -1;
			log.error(e.toString(), e);
		}
	}	
	
	public void loadApiSettings(){
		String appKey = Settings.getString(Settings.TD_API_ID, "");
		String appSecret = Settings.getString(Settings.TD_API_SECRET, "");
		
		log.info("connect with taodian app key:" + appKey + ", secret:" + appSecret);
		
		api = new TaodianApi(appKey, appSecret, Settings.getString(Settings.TD_API_ROUTE, "http://api.zaol.cn/api/route"));
	}

}
