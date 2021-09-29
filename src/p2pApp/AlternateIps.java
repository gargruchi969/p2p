package p2pApp;

public class AlternateIps {
	
	String ip;
	String fileid;
	String filename;
	String filesize;
	String userid;
	
	public AlternateIps(String ip, String fileid, String filename, String filesize){
		this(ip, fileid, filename, filesize, null);
	}
	
	public AlternateIps(String ip, String fileid, String filename, String filesize, String userid){
		this.ip= ip;
		this.fileid= fileid;
		this.filename= filename;
		this.filesize= filesize;
		this.userid= userid;
	}
	
	public AlternateIps(SearchResults searchResults){
		this(searchResults.getIp(),
		searchResults.getFileId(), 
		searchResults.getFilename(),
		searchResults.getFileSize(),
		searchResults.getUserid());
	}
	
	public String getIp(){
		return ip;
	}
	
	public String getFileid(){
		return fileid;
	}
	
	public String getFilesize(){
		return filesize;
	}
	
	public String getFilename(){
		return filename;
	}
	
	public String getUserid(){
		return userid;
	}
	
	public void addNew(AlternateIps ip){
		
	}
}
