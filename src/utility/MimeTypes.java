package utility;

import java.util.HashMap;

public class MimeTypes {
	
	private HashMap<String, String> mimes= null;
	private static MimeTypes mimeTypes;
	
	private MimeTypes(){
		mimes= new HashMap<String, String>();
		mimes.put("mp4", "video/mp4");
		mimes.put("mkv", "video/x-matroska");
		mimes.put("flv", "video/x-flv");
		mimes.put("mp3", "audio/mpeg");
	}
	
	public static MimeTypes getInstance(){
		if(mimeTypes==null)
			mimeTypes= new MimeTypes();
		return mimeTypes;
	}
	
	public String getMimeType(String filename){
		String ext= filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
		return mimes.get(ext);
	}
}
