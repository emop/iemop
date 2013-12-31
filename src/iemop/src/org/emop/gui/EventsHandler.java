package org.emop.gui;

import java.awt.CardLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.api.TaodianApi;
import org.emop.gui.events.BroadCastEvent;
import org.emop.gui.events.EventAction;
import org.emop.gui.xui.XUIContainer;
import org.emop.sender.HttpServer;
import org.emop.sender.WeiboSender;
import org.emop.sender.settings.Settings;
//import org.http.channel.client.ProxyClient;
//import org.http.channel.client.StatusListener;

public class EventsHandler {
	public static final String TD_APP_KEY = "td_app_key";
	public static final String TD_API_ROUTER = "td_api_router";
	public static final String TD_APP_SECRET = "td_app_secret";
	public static final String HTTP_PROXY = "http_proxy";
	
	public static final String STATUS_DOMAIN = "status_remote";
	public static final String STATUS_SETTINGPATH = "status_local";
	public static final String STATUS_REQUEST = "status_request";
	public static final String STATUS_TDAPI = "status_active_user";
	public static final String STATUS_UPDATED = "status_updated";	
	
	public static final String SAVE_SETTINGS = "saveSettings";
	public static final String CLOSE_SETTINGS = "closeSettings";
	public static final String SHOW_SETTINGS = "ShowSettings";
	public static final String SHOW_STATUS = "ShowStatus";
	
	public static final String HIDDEN_MAINFRAME = "HiddenMainFrame";
	public static final String OPEN_MAINFRAME = "OpenMainFrame";
	private final static DateFormat format= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Log log = LogFactory.getLog("gate");
	
	private XUIContainer xui = null;
	//private ProxyClient proxy = null;
	private static EventsHandler e = null;
	private static long lastUpdateTime = System.currentTimeMillis();
	
	public EventsHandler(XUIContainer xui, WeiboSender s){
		this.xui = xui;
		this.e = this;
	}
	
	@EventAction(order=1)
	public void XuiLoaded(final BroadCastEvent event){
	}
	
	/**
	 * 菜单-选择代理状态显示，
	 * @param event
	 */
	@EventAction(order=1)
	public void ProxyStatus(final BroadCastEvent event){
		JPanel actionPanel = (JPanel)xui.getByName("mainLayout");
		CardLayout layout = (CardLayout)actionPanel.getLayout();
		layout.show(actionPanel, "status");
	}

	/**
	 * 菜单-选择代理设置
	 * @param event
	 */
	@EventAction(order=1)
	public void UserSettings(final BroadCastEvent evnet){
		JPanel actionPanel = (JPanel)xui.getByName("mainLayout");
		CardLayout layout = (CardLayout)actionPanel.getLayout();
		layout.show(actionPanel, "login");		
	}
	
	/**
	 * 设置界面打开时触发.
	 * @param event
	 */
	@EventAction(order=1)
	public void ShowSettings(final BroadCastEvent event){
		JTextField field;
		
		field = (JTextField)xui.getByName(TD_APP_KEY);
		if(field != null){
			field.setText(Settings.getString(Settings.TD_API_ID, ""));
		}

		field = (JTextField)xui.getByName(TD_APP_SECRET);
		if(field != null){
			field.setText(Settings.getString(Settings.TD_API_SECRET, ""));
		}

		field = (JTextField)xui.getByName(TD_API_ROUTER);
		if(field != null){
			field.setText(Settings.getString(Settings.TD_API_ROUTE, "http://api.zaol.cn/api/route"));
		}
	}
	
	@EventAction(order=1)
	public void saveSettings(final BroadCastEvent event){
		JTextField field;
		
		field = (JTextField)xui.getByName(TD_APP_KEY);
		if(field != null){
			Settings.putSetting(Settings.TD_API_ID, field.getText());
		}

		field = (JTextField)xui.getByName(TD_APP_SECRET);
		if(field != null){
			Settings.putSetting(Settings.TD_API_SECRET, field.getText());
		}

		field = (JTextField)xui.getByName(TD_API_ROUTER);
		if(field != null){
			Settings.putSetting(Settings.TD_API_ROUTE, field.getText());
		}
		
		HttpServer.ins.loadApiSettings();
		
		Settings.ins.save();
		
		ProxyStatus(event);
	}
	
	@EventAction(order=1)
	public void closeSettings(final BroadCastEvent event){
		ProxyStatus(event);
	}
	
	/**
	 * 切换到状态显示面板
	 * @param event
	 */
	@EventAction(order=1)
	public void ShowStatus(final BroadCastEvent event){
		updateStatus();
	}
	
	/**
	 * 主窗口打开时触发，开始加载Proxy。
	 * @param event
	 */	
	@EventAction(order=1)
	public void OpenMainFrame(final BroadCastEvent event){
		log.info("OpenMainFrame......");
		/*
		String r = proxy.settings.getString(Settings.REMOTE_DOMAIN, "");
		String l = proxy.settings.getString(Settings.INTERNAL_DOMAIN, "");
		if(!proxy.isRunning){
			proxy.run();
		}
		*/
		
		String appId = Settings.getString(Settings.TD_API_ID, "");
		String secret = Settings.getString(Settings.TD_API_SECRET, "");
		
		JPanel actionPanel = (JPanel)xui.getByName("mainLayout");
		CardLayout layout = (CardLayout)actionPanel.getLayout();			
		
		if(appId == null || appId.length() == 0 || secret == null || secret.length() == 0){
			this.ShowSettings(event);
			log.info("OpenMainFrame......login");
			layout.show(actionPanel, "login");
		}else {
			this.ShowStatus(event);
			log.info("OpenMainFrame......status");
			layout.show(actionPanel, "status");
		}
	}
	
	/**
	 * 退出系统
	 * @param event
	 */
	@EventAction(order=1)
	public void Exit(final BroadCastEvent event){
		System.exit(0);
	}
	
	@EventAction(order=1)
	public void About(final BroadCastEvent event){
		JDialog about = (JDialog)xui.getByName("about");
		about.setLocationRelativeTo(about.getParent());
		about.setVisible(true);		
	}
	
	@EventAction(order=1)
	public void HiddenMainFrame(final BroadCastEvent event){
		
	}
	
	private void updateStatus(){
		JTextField field = (JTextField)xui.getByName(STATUS_DOMAIN);
		
		HttpServer s = HttpServer.ins;
		if(s != null && s.httpPort > 0){
			field.setText("http://127.0.0.1:" + (s != null ? s.httpPort : 8927));
		}else {
			field.setText("本地服务启动失败。");			
		}
		
		field = (JTextField)xui.getByName(STATUS_SETTINGPATH);
		if(field != null){
			field.setText(Settings.ins.path);			
		}
		
		field = (JTextField)xui.getByName(STATUS_TDAPI);
		if(field != null){
			field.setText(TaodianApi.okCount + "");
		}
		
		field = (JTextField)xui.getByName(STATUS_UPDATED);
		if(field != null){
			field.setText(WeiboSender.statusMsg);
		}
	}
	
	public static void refreshStatus(){
		if(e != null && System.currentTimeMillis() - lastUpdateTime > 1000){
			lastUpdateTime = System.currentTimeMillis();
			SwingUtilities.invokeLater(new Runnable() {
				public void run(){
					e.updateStatus();
				}
			});
		}
	}
}
