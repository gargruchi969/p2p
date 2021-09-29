package modules;

import tcpQueries.PingQuery;
import tcpServer.BaseController;

public class PingSeqModule extends Thread {

	String baseIp;
	
	public PingSeqModule(String baseip){
		this.baseIp= baseip;
		this.start();
	}
	
	public void run(){
		sendPingSequence();
	}
	
	
	public void sendPingSequence(){
		
		BaseController baseController= BaseController.getInstance();
		String ip="";
		for(int i=1;i<255;i++){
			for(int j=1;j<255;j++){
				ip= baseIp+"."+i+"."+j;
				baseController.sendRequest(new PingQuery("ping",null,null), "tcp-server", "PingQuery", true, "", ip);
			}
		}
	}
}
