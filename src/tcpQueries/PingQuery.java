package tcpQueries;

import java.util.List;

import tcpUtilities.PeersEntries;

public class PingQuery {

	public String action;
	public String result;
	public List<PeersEntries> peers;
	String extraData;
	public PingQuery(){
		action="ping";
		result=null;
		peers=null;
		extraData=null;
	}
	
	public PingQuery(String action, String result, List<PeersEntries> peers){
		this(action, result, peers, null);
	}
	
	public PingQuery(String action, String result, List<PeersEntries> peers, String data){
		this.action= action;
		this.result= result;
		this.peers= peers;
		this.extraData= data;
	}
		
	public String getExtraData(){
		if(extraData==null)
			extraData="";
		return extraData;
	}
	
}
