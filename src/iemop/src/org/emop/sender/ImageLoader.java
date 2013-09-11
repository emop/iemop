package org.emop.sender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emop.cache.Cache;
import org.emop.cache.impl.SimpleCache;

public class ImageLoader {
	private Log log = LogFactory.getLog("weibo.imageloader");
	
	public Cache cache = new SimpleCache();
	private File root = null;
	
	public ImageLoader(File root){
		this.root = root;
	}

	public byte[] loadPicUrl(String url){
		if(url.contains("b0.upaiyun.com") && url.indexOf("!") <= 0){
			url += "!weibo";
		}
		Object tmp = cache.get(url, true);
		
		byte[] data = null;
		if(tmp == null){
			File f = getCacheFile(url);
			data = readFromFile(f);
			if(data == null){
				log.info("load image from url:" + url);
				data = readFromUrl(url);
				writeToFile(f, data);
			}else {
				if(log.isDebugEnabled()){
					log.debug("load image from disk cache:" + url);
				}				
			}
			
			cache.set(url, data, 60 * 5);
		}else {
			if(log.isDebugEnabled()){
				log.debug("load image from memory cache:" + url);
			}
			data = (byte[])tmp;
		}
		
		return data;
	}

	private byte[] readFromUrl(String url){		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream ins = null;
		byte[] data = null;
		try {
			byte[] buff = new byte[1024 * 100];
			URL u = new URL(url);
			ins = u.openStream();
			for(int len = 0; len >= 0; ){
				len = ins.read(buff);
				if(len > 0){
					os.write(buff, 0, len);
				}
			}
			os.close();
			data = os.toByteArray(); 
		} catch (MalformedURLException e) {
			log.error("Error image url:" + url);
		} catch (IOException e) {
			log.error(e.toString(), e);
		}finally{
			if(ins != null){
				try {
					ins.close();
				} catch (IOException e) {
				}
			}
		}
		
		return data;
	}
	
	private void writeToFile(File f, byte[] data){
		File p = f.getParentFile();
		if(!p.isDirectory()){
			p.mkdirs();
		}
		
		log.info("write image cache:" + f.getAbsolutePath());
		
		OutputStream os = null;
		try {
			os = new FileOutputStream(f);
			os.write(data);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}finally{
			if(os != null){
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}	
	}	
	
	private byte[] readFromFile(File file){
		if(!file.isFile()) return null;
		
		InputStream ins = null;
		byte[] data = null;
		try {
			data = new byte[(int) file.length()];
			ins = new FileInputStream(file);
			for(int len = 0; len < data.length; ){
				len += ins.read(data, len, data.length - len);
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
			data = null;
		}finally{
			if(ins != null){
				try {
					ins.close();
				} catch (IOException e) {
				}
			}
		}
		
		return data;
	}
	
	private File getCacheFile(String url){
		try {
			URL u = new URL(url);
			return new File(root, u.getPath());
		} catch (MalformedURLException e) {
		}
		
		return null;
	}

}
