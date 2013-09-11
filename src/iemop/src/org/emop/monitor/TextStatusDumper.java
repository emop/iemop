package org.emop.monitor;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.emop.sender.Version;


public class TextStatusDumper {
	private static DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void output(PrintWriter writer, StatusMonitor status){
		writer.println(Version.getName() + " " + Version.getVersion() + " build time:" + Version.getBuildDate());
		writer.println("Server started at:" + sdf.format(status.uptime));
		
		httpOutput(writer, status);
	}
	
	public static void httpOutput(PrintWriter writer, StatusMonitor status){
		writer.println("\n");
		writer.println("==========HTTP请求记录==========");
		writer.println("总调用次数:" + status.http.requestCount);
		
		writer.println("平均花费时间:" + status.http.averageElapsed + " ms, " +
				"最长时间:" + status.http.maxElapsed + " ms, " +
				"最短时间:" + status.http.minElapsed + " ms");
		writer.println("");
		writer.println("最近一次请求：");
		outHTTPRequest(writer, status.http.last);
		writer.println("");
		writer.println("----------时间最长记录-----------");
		for(Benchmark item : status.http.slowList.list()){
			outHTTPRequest(writer, item);
		}
	}
	
	public static void outHTTPRequest(PrintWriter writer, Benchmark item){
		if(item == null) return;
		String tmp = item.obj + "";
		tmp = tmp.replace("\n", "\\n");
		tmp = tmp.substring(0, Math.min(120, tmp.length()));
		
		String msg = String.format("%6d(ms) %s url:%s", item.elapsed, sdf.format(new Date(item.start)),
				tmp
				);
		writer.println(msg);
	}	
}
