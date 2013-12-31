package org.emop.sender.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Settings {
	
	public final static String HTTP_PORT = "http_port";
	public final static String TD_API_ROUTE = "td_api_route";
	public final static String TD_API_ID = "td_api_id";
	public final static String TD_API_SECRET = "td_api_secret";
	public final static String WEIBO_APP_ID = "weibo_app_id";
	public final static String WEIBO_TASK_QUEUE = "weibo_task_queue";
	public final static String WEIBO_TASK_ROOT = "task_root";

	
	public final static String IMAGE_CACHE_ROOT = "image_cache_root";
	
	
	public final static String CORE_ROUTE_THREAD_COUNT = "core_route_thread_count";
	public final static String MAX_ROUTE_THREAD_COUNT = "max_route_thread_count";
	
	public final static String PROXY_SECRET_KEY = "client_secret_key";
	public static final String REMOTE_DOMAIN = "remote_domain";
	public static final String INTERNAL_DOMAIN = "internal_domain";	
	
	private static Log log = LogFactory.getLog("settings");
	protected static Properties settings = System.getProperties();	
	protected static Properties userSettings = null;//System.getProperties();	

	
	private String confName = "iemop.conf";
	
	public String path = null;
	//private 
	//private String[] masterSettings = new String[]{};
	//private String[] routeSettings = new String[]{};
	public static Settings ins = null;
	
	public Settings(String path){
	//	this.confName = name;
		this.path = path;
		this.loadSettings();
		
		ins = this;
	}
	
	public void loadSettings(){
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/emop/sender/settings" + this.confName);
			if(is != null){
				settings.load(is);
			}else {
				log.info("Not found default configuration!" + this.confName);
			}
		} catch (IOException e) {
			log.error(e, e.getCause());
		}
		
		File f = new File(path);
		InputStream ins = null;
		userSettings = new Properties();
		if(f.isFile()){
			log.info("load settings from:" + this.path);
			try {
				ins = new FileInputStream(f);
				userSettings.load(ins);
			} catch (FileNotFoundException e) {
				log.error(e, e.getCause());
			} catch (IOException e) {
				log.error(e, e.getCause());
			} finally{
				if(ins != null) {
					try {
						ins.close();
					} catch (IOException e) {
					}
				}
			}
		}else {
			log.info("Not found file:" + this.path);
		}
	}
	
	public static void putSetting(String name, String val){
		userSettings.put(name, val);
	}
	
	public static String getString(String name, String def){
		String n = userSettings.getProperty(name, null);
		if(n == null){
			n = settings.getProperty(name, def);
		}
		return n;
	}
	
	public static int getInt(String name, int def){
		String val = getString(name, def + "");
		int intVal = def;
		try{
			if(val != null) intVal = Integer.parseInt(val);
		}catch(Exception e){
		}
		
		return intVal;
	}	
	
	public void save(){
		OutputStream os = null;
		try {
			log.info("Saveing settings to " + path);
			os = new FileOutputStream(new File(path));
			userSettings.store(os, "");
		} catch (IOException e) {
			log.error(e.toString(), e);
		} finally{
			if(os != null){
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
		
	}
		
}
