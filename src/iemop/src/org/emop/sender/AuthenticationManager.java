package org.emop.sender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.emop.http.HTTPResult;

/**
 * 简单的认证管理机制。 因为签入的Jetty没用加入Session机制。所以独立加入了一个简易的认证机制。
 * @author deonwu
 *
 */
public class AuthenticationManager {
	
	public AuthenticationManager(){
	}

    /**
     * 检查当前用户是否登陆。
     */
    public boolean authCheck(HttpServletRequest request){
    	return true;
    }

    /**
     * 把登陆结果保存到当前Session里面。
     */
    public void updateSessionInfo(HttpServletRequest request, HttpServletResponse response, HTTPResult result){
    }

}
