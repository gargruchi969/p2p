package p2pApp;

import java.util.ArrayList;

public class SearchResults {

	String ip;
	String filename;
	String hash;
	String filesize;
	String userid;
	String fileid;
	String path;
	String type;
	int stream;
	
	ArrayList<AlternateIps> altIps;
	
	public SearchResults(String ip, String userid, String fileid, String filename, String hash, String filesize, int stream){
		this(ip, userid, fileid, filename, hash, filesize, "1", stream);
	}
	
	public SearchResults(String ip, String userid, String fileid, String filename, String hash, String filesize, String type, int stream){
		this(ip, userid, fileid, filename, hash, filesize, new ArrayList<AlternateIps>(), type, stream);
	}
	
	public SearchResults(String fileid, String filename, String hash, String filesize){
		this.fileid= fileid;
		this.filename= filename;
		this.hash= hash;
		this.filesize= filesize;
	}
	
//	public SearchResults(String ip, String userid, String fileid, String filename, String hash, String filesize, ArrayList<AlternateIps> altIps, String type){
//		this.ip= ip;
//		this.fileid= fileid;
//		this.filename= filename;
//		this.hash= hash;
//		this.filesize= filesize;
//		this.userid= userid;
//		this.altIps= altIps;
//		this.type= type;
//		stream= 0;
//	}
	
	public SearchResults(String ip, String userid, String fileid, String filename, String hash, String filesize, ArrayList<AlternateIps> altIps, String type, int stream){
		this.ip= ip;
		this.fileid= fileid;
		this.filename= filename;
		this.hash= hash;
		this.filesize= filesize;
		this.userid= userid;
		this.altIps= altIps;
		this.type= type;
		this.stream= stream;
	}
	
	public String getFileId(){
		return fileid;
	}
	
	public String getIp(){
		return ip;
	}
	
	public String getFilename(){
		return filename;
	}
	
	public String getUserid(){
		return userid;
	}
	
	public String getFileSize(){
		return filesize;
	}
	
	public void addAlternateIps(String ip, String key, String filename, String filesize, String userid){
		if(altIps==null)
			altIps= new ArrayList<AlternateIps>();
		addNewAltIp(new AlternateIps(ip, key, filename, filesize, userid));
	}
	
	public ArrayList<AlternateIps> getAlternateIps(){
		return altIps;
	}
	
	public void addAlternateIps(ArrayList<AlternateIps> ips){
		if(ips == null || ips.size()==0)
			return;
		for(int i=0;i<ips.size();i++)
			addNewAltIp(ips.get(i));
	}
	
	public String getType(){
		return type;
	}
	
	public void setFilename(String name){
		filename= name;
	}
	
	public String getHash(){
		return hash;
	}
	
	public void addNewAltIp(AlternateIps aip){
		if(aip.getIp().equals(ip)){
			return;
		}
		for(int i=0;i<altIps.size();i++){
			if(aip.getIp().equals(altIps.get(i).getIp()))
				return;
		}
		altIps.add(aip);
	}
	
	public int getStream(){
		return stream;
	}
}

