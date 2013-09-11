package org.emop.model;

import java.io.Serializable;

public class SendResult implements Serializable{
	public long sid = 0;

	public String hasComment = "n";
	public long timingId = 0;
	public String msgId = "";
	public String retCode = "";
	public String retMsg = "";
	
	public String commentCode = "";
	public String commentMsg = "";

	public int hashCode(){
		return (int) sid;
	}
	
	public boolean equals(Object o){
		if(o instanceof SendResult){
			return o.hashCode() == this.hashCode();
		}else {
			return false;
		}
	}	
}
