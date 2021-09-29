package baseServer;

import java.util.List;

import tcpClient.TCPClient;
import tcpQueries.PingQuery;
import tcpServer.BaseController;
import tcpUtilities.PeersEntries;
import tcpUtilities.PeersTable;
import utility.Query_v12;

public class BaseNetworkEngine {

	private static BaseNetworkEngine baseEngineInstance;
	private PeersTable peersTable;
	
	private BaseNetworkEngine(){
		peersTable= PeersTable.getInstance();
	}

	// Ensures a single instance of this class.
	public static BaseNetworkEngine getInstance(){
		if(baseEngineInstance == null)
			baseEngineInstance = new BaseNetworkEngine();
		return baseEngineInstance;
	}
	
	public void manageNeighboursList() {
		PeersTable pt= peersTable;
		List<PeersEntries> pe = pt.getConnected();
			
		for(int i=0;i< pe.size() && pt.getNeighbourPeers().size()<=utility.Utilities.neighbourPeersCount; i++){
			if(!pt.isNeighbourPresentByIp(pe.get(i).ip)){
				pt.addNeighbourPeers(pe.get(i).ip, pe.get(i).systemId, "unknown", false);
				BaseController.getInstance().sendRequest(
						new PingQuery("ping",null,null), "tcp-server",
						"PingQuery", true, "", pe.get(i).ip);
			}
		}
	}
	
	public void manageNeighboursList(List<PeersEntries> pe){
		
		if(pe.size()<1)
			manageNeighboursList();
		
		PeersTable pt= peersTable;
		for(int i=0;i< pe.size() && pt.getNeighbourPeers().size()<=utility.Utilities.neighbourPeersCount; i++){
			if(!pt.isNeighbourPresentByIp(pe.get(i).ip)){
				pt.addNeighbourPeers(pe.get(i).ip, pe.get(i).systemId, "unknown", false);
				BaseController.getInstance().sendRequest(new PingQuery("ping",null,null), "tcp-server", "PingQuery", true, "", pe.get(i).ip);
			}
		}
	}
	
	public void manageNeighboursList(String ip, boolean reflectDisconnected){		

		if(ip!=null){
			peersTable.remNeighbourPeerByIp(ip);
			if(reflectDisconnected){
				peersTable.updateStatusByIp(ip, "disconnected");
			}
		}
		manageNeighboursList();
	}
	
		
	public int sendMultipleRequests(Object obj, String module, String rtype, boolean response){
		return sendMultipleRequests(obj, module, rtype, response, utility.Utilities.maxHopCount);
	}
	
	public int sendMultipleRequests(Object obj, String module, String rtype, boolean response, int hopCount){
		BaseController bControl= BaseController.getInstance();
		int qId= bControl.getNewQueryId();
		for(int i=0; i<peersTable.getNeighbourPeers().size() && i<utility.Utilities.maxSimultaneousRequests; i++){
			bControl.sendRequest(qId, obj, module, rtype, response, peersTable.getNeighbourPeers().get(i).systemId, peersTable.getNeighbourPeers().get(i).ip, hopCount);
		}
		return qId;
	}
	
	public void forwardRequests(Query_v12 query){
		forwardRequests(query, false);
	}
	
	public void forwardRequests(Query_v12 query, boolean found){
		query.decrementHop(found);
		query.setResponse(false);
		if(query.getHopCount()<=0){
			return;
		}
		List<PeersEntries> tempList= peersTable.getNeighbourPeers();
		for(int i=0; i<tempList.size() && i<utility.Utilities.maxSimultaneousRequests; i++){
			new TCPClient().sendRequestIntoNetwork(utility.Utilities.serverPort, tempList.get(i).ip, utility.Utilities.getJsonString(query), false);
		}
	}
	
	public boolean connectToNetwork(){
		
		if(peersTable.getConnected().size()==0){
			System.out.println("No peers found. Please ping an active user to connect!");
			return false;
		}
		manageNeighboursList();
		return true;
	}
	
	//Callback Functions
	
	public void manageNeighboursList(String action, Object obj){
		try{
			if(action.equals("tcp-server-pong")){
				//UISearch.enableSearchButon(true);
				Query_v12 query= (Query_v12)obj;
				peersTable.updateNeighbourPeer(query.getSourceIp(), query.getSourceSid(), "connected", true);
				persistActivePeers(((PingQuery)utility.Utilities.getObjectFromJson(query.getPayload(), PingQuery.class)).peers, query.getSourceIp(), query.getSourceSid());
				manageNeighboursList(((PingQuery)utility.Utilities.getObjectFromJson(query.getPayload(), PingQuery.class)).peers);
			}
			
			if(action.equals("tcp-server-ping")){
				Query_v12 query= Query_v12.class.cast(obj);
				peersTable.updateNeighbourPeer(query.getSourceIp(), query.getSourceSid(), "connected", false);
				persistActivePeers(null, query.getSourceIp(), query.getSourceSid());
			}
		}
		catch(Exception e){
			System.out.println("Network Engine #1: "+ e.getMessage());
		}
	}

//	public void TimedOutHandler(String action, Object obj){
//		String ip= obj.toString();
//		manageNeighboursList(ip, true);
//	}
	
	public void ConnectErrorHandler(String action, Object obj){
		String[] msg= (String[])obj;
		if(msg[0].equals("TimedOut")){
			manageNeighboursList(msg[1], true);
		}
		else{
			PeersTable.getInstance().markForRemoval(msg[1]);
		}
	}
	
	private void persistActivePeers(List<PeersEntries> pe, String ip, String sid){
		if(pe!=null){
			for(int i=0;i<pe.size();i++){
				utility.Utilities.writeToFile("data/ips.dat", pe.get(i).ip + " "+ pe.get(i).systemId, true);
			}
		}
		if(ip!=null)
		utility.Utilities.writeToFile("data/ips.dat", ip+ " "+ sid, true);
	}

}
