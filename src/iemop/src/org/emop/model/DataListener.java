package org.emop.model;

public interface DataListener {
	public static final int INIT_ERROR = 1;
	public static final int READ_LINE = 2;
	public static final int READ_DONE = 3;
	
	
	public void change(int status, String msg, String data);
}
