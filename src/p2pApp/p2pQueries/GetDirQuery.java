package p2pApp.p2pQueries;

import java.util.ArrayList;

import p2pApp.SearchResults;
import tcpServer.BaseController;

public class GetDirQuery {

	public int getDirId;
	public String action;
	public String mode;
	public String key;
	public String type;
	public String name;
	public ArrayList<SearchResults> files;
	public String ip;
	
	public GetDirQuery(String action, String mode, String key, String name){
		this.action= action;
		getDirId= utility.Utilities.getRandomNumber();
		type= "2";
		this.mode= mode;
		this.key= key;
		this.name= name;
	}
	
	public GetDirQuery(String action, String mode, String key, String name, String ip){
		this.action= action;
		getDirId= utility.Utilities.getRandomNumber();
		type= "2";
		this.mode= mode;
		this.key= key;
		this.name= name;
		this.ip= ip;
	}

	public GetDirQuery(int id, String action, String name, ArrayList<SearchResults> files){
		this.getDirId= id;
		this.action= action;
		this.name= name;
		this.files= files;
	}
	
	public GetDirQuery(SearchResults sr){
		this("search", "fileId", sr.getFileId(), sr.getFilename(), sr.getIp());
	}
	
	public static void sendDirQuery(SearchResults sr){
		BaseController.getInstance().sendRequest(
				new GetDirQuery("search", "fileId", sr.getFileId(), sr.getFilename()), "p2p-app", "GetDirQuery", true, "", sr.getIp(), 1);
	}
	
	public static void sendDirQuery(GetDirQuery gdq){
		BaseController.getInstance().sendRequest(
				gdq, "p2p-app", "GetDirQuery", true, "", gdq.getIp(), 1);
	}
	
	public int getDirId(){
		return getDirId;
	}
	
	public String getIp(){
		return ip;
	}
}
