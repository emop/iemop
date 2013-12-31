package org.emop.sender;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.gui.GUIMain;
import org.emop.sender.settings.Settings;

public class Main {
	private Log log = LogFactory.getLog("wx.main");
	public static final String version = Version.getVersion(); 
	
	public static final String VERSION = "version";
	public static final String PREFIX = "prefix";
	public static final String HTTPPORT = "http_port";	
	public static final String HTTP_URL = "http_url";
	public static final String NO_GUI = "no_gui";
	
	public static void main(String[] args) throws IOException{
		Options options = new Options();
		options.addOption(VERSION, false, "show version.");
		options.addOption(PREFIX, true, "the prefix of HTTP service.");
		options.addOption(HTTPPORT, true, "http listen port.");
		options.addOption(NO_GUI, false, "disable gui windows");

		CommandLine cmd = null;
		
		String jarUrl = "iemop.conf";
		try {
			jarUrl = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		File jarPath = new File(jarUrl);
		File workRoot = jarPath.getParentFile();
		System.setProperty("user.dir", workRoot.getAbsolutePath());
		
		try{
			CommandLineParser parser = new PosixParser();
			cmd = parser.parse(options, args);			
		}catch(ParseException e){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("WeiboSender", options);
			System.exit(-1);
		}
		
		if(cmd.hasOption(VERSION)){
			System.out.println("WeiboSender " + Version.getVersion());
			return;
		}else {
			String httpPort = cmd.getOptionValue(HTTPPORT, "8927");
			initLog4jFile("server.log");
			Settings s = new Settings(new File(workRoot, "iemop.conf").getAbsolutePath());
			Settings.putSetting(Settings.HTTP_PORT, httpPort);
			//s.save();
			startCleanLog(s, "server.log");
			new HttpServer(s).run();
		}
		
		if(!cmd.hasOption(NO_GUI)){
			GUIMain.main(args);
			//return;
		}

		System.out.println("Stopped.");
	}	
	
	private static void initLog4jFile(String name){
		//LogFactory.getLog("main");
		org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
		try {
			root.addAppender(new org.apache.log4j.DailyRollingFileAppender(root.getAppender("S").getLayout(),
					"logs/" + name, 
					".yy-MM-dd"));
		} catch (IOException e) {
			System.out.println("Failed to add file appender.");
			// TODO Auto-generated catch block
		}
		
		root.info(Version.getName() + " " + Version.getVersion());
		root.info("build at " + Version.getBuildDate());
		root.info("java.home:" + System.getProperty("java.home"));
		root.info("java.runtime.version:" + System.getProperty("java.runtime.version"));
		root.info("java.runtime.name:" + System.getProperty("java.runtime.name"));
		
	}
	
	private static void startCleanLog(final Settings s, final String name){
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
				try{
					updateLog4jLevel(s, name);
				}catch(Throwable e){
					root.info(e.toString());
				}
			}
		}, 100, 1000 * 3600 * 12);		
	}	
	
	private static void updateLog4jLevel(Settings s, String name){
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        String level = Settings.getString("log_level", "debug").toLowerCase().trim();
        if(level.equals("trace")){
                root.setLevel(org.apache.log4j.Level.TRACE);
        }else if(level.equals("debug")){
                root.setLevel(org.apache.log4j.Level.DEBUG);
        }else if(level.equals("info")){
                root.setLevel(org.apache.log4j.Level.INFO);
        }else if(level.equals("warn")){
                root.setLevel(org.apache.log4j.Level.WARN);
        }
        File r = new File("logs");
        
        int max_log_days = Settings.getInt("max_log_days", 10);                
        Date d = new Date(System.currentTimeMillis() - 1000 * 3600 * 24 * max_log_days);                
        DateFormat format= new SimpleDateFormat("yy-MM-dd");            
        root.debug("Remove log before " + format.format(d));
        for(File log : r.listFiles()){
                if(!log.getName().startsWith(name))continue;
                String[] p = log.getName().split("\\.");
                String logDate = p[p.length -1];
                if(logDate.indexOf("-") > 0){
                        try {
                                if(format.parse(logDate).getTime() < d.getTime()){
                                        root.info("remove old log file:" + log.getName());
                                        log.delete();
                                }
                        } catch (Exception e) {
                                root.info(e.toString());
                        }
                }
        }
	}	
}
