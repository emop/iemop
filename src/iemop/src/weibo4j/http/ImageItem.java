package weibo4j.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import weibo4j.model.Constants;
import weibo4j.model.WeiboException;


/**
 * 临时存储上传图片的内容，格式，文件信息等
 * 
 */
public class ImageItem {
	private byte[] content;
	private String name;
	private String contentType;
	public ImageItem(byte[] content) throws WeiboException {
	    this(Constants.UPLOAD_MODE,content);
	}
	public ImageItem(String name,byte[] content) throws WeiboException{
		String imgtype = null;
		try {
		    imgtype = getContentType(content);
		} catch (IOException e) {
		    throw new WeiboException(e);
		}
		
	    if(imgtype!=null&&(imgtype.equalsIgnoreCase("image/gif")||imgtype.equalsIgnoreCase("image/png")
	            ||imgtype.equalsIgnoreCase("image/jpeg"))){
	    	this.content=content;
	    	this.name=name;
	    	this.contentType=imgtype;
	    }else{
	    	throw new WeiboException(
            "Unsupported image type, Only Suport JPG ,GIF,PNG!");
	    }
	}
	
	public byte[] getContent() {
		return content;
	}
	public String getName() {
		return name;
	}
	public String getContentType() {
		return contentType;
	}

	public static String getContentType(byte[] mapObj) throws IOException {
		return "image/jpeg";
	}
}
