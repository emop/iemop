package org.emop.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.emop.gui.xui.XUIContainer;

/**
 * Internet: http://www.deonwu84.com:8080
 * Internal: http://www.deonwu84.com:8080
 * Requested: xxx
 * Active user: xxx
 * Updated: xxxx
 * @author deon
 *
 */
public class ProxyStatusPanel extends JPanel {

	public void initPanel(final XUIContainer xui){
		this.setLayout(new BorderLayout());	
        final JTextField localDomain = new JTextField("");
        final JTextField settingPath = new JTextField("");
       // final JTextField requestCount = new JTextField("");
        final JTextField tdApi = new JTextField("");
        final JTextField status = new JTextField("");
        
        JLabel localLabel = new JLabel("本地访问: ");
        JLabel settingsLabel = new JLabel("配置文件: ");
        //JLabel proxyLabel = new JLabel("服务: ");
        JLabel userLabel = new JLabel("冒泡成功次数: ");
        JLabel updatedLabel = new JLabel("状态: ");
        
        localDomain.setEditable(false);
        settingPath.setEditable(false);
        //requestCount.setEditable(false);
        tdApi.setEditable(false);
        status.setEditable(false);
                
        JPanel textControlsPane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        //GridBagConstraints c = new GridBagConstraints();

        textControlsPane.setLayout(gridbag);

        JLabel[] labels = {localLabel, settingsLabel,   userLabel, updatedLabel};
        JTextField[] textFields = {localDomain, settingPath, tdApi, status};
        addLabelTextRows(labels, textFields, gridbag, textControlsPane);

        //textControlsPane.add(actionLabel, c);
        /*
        textControlsPane.setBorder(BorderFactory.createCompoundBorder(
                                	BorderFactory.createTitledBorder("设置信息"),
                                	BorderFactory.createEmptyBorder(5,5,5,5)));
        */
        add(textControlsPane, BorderLayout.CENTER);
        
        //this.addComponentListener(l)
        this.addComponentListener(new ComponentAdapter(){
        	public void componentShown(ComponentEvent e){
        		if(xui.eventQueue != null){
        			xui.eventQueue.fireEvent(EventsHandler.SHOW_STATUS, e.getSource());
        		}
        	}
        });        
        /**
         * 注册GUI控件到XUI.
         */
        xui.addComponent(EventsHandler.STATUS_DOMAIN, localDomain);
        xui.addComponent(EventsHandler.STATUS_SETTINGPATH, settingPath);        
    //    xui.addComponent(EventsHandler.STATUS_REQUEST, requestCount);
        xui.addComponent(EventsHandler.STATUS_TDAPI, tdApi);
        xui.addComponent(EventsHandler.STATUS_UPDATED, status);
	}
	
    private void addLabelTextRows(JLabel[] labels, JTextField[] textFields,
            		GridBagLayout gridbag, Container container) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		int numLabels = labels.length;		
		for (int i = 0; i < numLabels; i++) {
			c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
			c.fill = GridBagConstraints.NONE;      //reset to default
			c.weightx = 0.0;                       //reset to default
			container.add(labels[i], c);
			
			c.gridwidth = GridBagConstraints.REMAINDER;     //end row
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			container.add(textFields[i], c);
		}
    }

}
