package modules;

import java.io.DataOutputStream;

import tcpQueries.PingQuery;
import tcpServer.BaseController;
import tcpUtilities.CallbackRegister;
import tcpUtilities.PeersEntries;
import tcpUtilities.PeersTable;
import utility.Query_v12;
import baseServer.BaseNetworkEngine;

public class TCPServerModule {

	Query_v12 baseQuery;
	DataOutputStream output;
	
	public TCPServerModule(Query_v12 query, DataOutputStream output){
		this.baseQuery= query;
		this.output= output;
		
		processRequest();
	}
	
	private String getResType(){
		return baseQuery.getResponseType();
	}
	
	private void processRequest(){
		String type= getResType();
		if(type.equals("string")){
			echoString();
		}
		else{
			if(type.contains("PingQuery")){
				new PingModule(baseQuery, output);
			}
		}
	}
	private void echoString(){
		BaseController.getInstance().sendResponse(baseQuery.getPayload(), baseQuery.getModule(), "string", false, baseQuery.getSourceSid(), output);
	}
}

class PingModule{
	
	Query_v12 baseQuery;
	DataOutputStream output;
	PingModule(Query_v12 q, DataOutputStream o){
		baseQuery=q;
		output=o;
		
		try{
			getBaseObject();
		}
		catch(ClassNotFoundException ce){
			System.out.println("Not found class: "+ce.getMessage());
		}
	}
	
	private void getBaseObject() throws ClassNotFoundException{
		PingQuery pq= PingQuery.class.cast(utility.Utilities.getObjectFromJson(baseQuery.getPayload(), Class.forName("tcpQueries.PingQuery")));
		processPingQuery(pq);
	}
	
	private void processPingQuery(PingQuery pq){
		
		boolean valid= false;
		
		if(pq.action.equals("ping")){
			PeersTable.getInstance().addEntry(baseQuery.getSourceIp(), baseQuery.getSourceSid(), "connected");
			valid = true;
			if(output!=null)
				BaseController.getInstance().sendResponse(new PingQuery("pong","valid",PeersTable.getInstance().getConnected()), "tcp-server", "PingQuery", false, baseQuery.getSourceSid(), output);
		}
		
		if(pq.action.equals("ping-force")){
			PeersTable.getInstance().addEntry(baseQuery.getSourceIp(), baseQuery.getSourceSid(), "connected");
			PeersTable.getInstance().addNeighbourPeers(baseQuery.getSourceIp(), baseQuery.getSourceSid(), "connected", false);
			
			if(output!=null)
				BaseController.getInstance().sendResponse(new PingQuery("pong-force","valid",PeersTable.getInstance().getConnected()), "tcp-server", "PingQuery", false, baseQuery.getSourceSid(), output);
			return;
		}
		
		if(pq.action.equals("ping-stop")){
			PeersTable.getInstance().addEntry(baseQuery.getSourceIp(), baseQuery.getSourceSid(), "disconnected");
			BaseNetworkEngine.getInstance().manageNeighboursList(baseQuery.getSourceIp(), true);
			valid = true;
			if(output!=null)
				BaseController.getInstance().sendResponse(new PingQuery("pong","valid",PeersTable.getInstance().getAll()), "tcp-server", "PingQuery", false, baseQuery.getSourceSid(), output);
		}
		
		if(pq.action.equals("ping-updates")){
			PeersEntries pe;
			for(int i=0;i<pq.peers.size();i++){
				pe= pq.peers.get(i);
				PeersTable.getInstance().addEntry(pe.ip, pe.systemId, pe.status, pe.time);
			}
			
			if(output!=null)
				BaseController.getInstance().sendResponse(new PingQuery("pong","valid",PeersTable.getInstance().getAll()), "tcp-server", "PingQuery", false, baseQuery.getSourceSid(), output);
			return;
		}
		
		if(pq.action.equals("ping-point")){
			
			valid = true;
			
			if(utility.Utilities.getSystemId().equals(baseQuery.getDestSid())){
				if(output==null)
					BaseController.getInstance().sendRequest(new PingQuery("pong-point","match",null), "tcp-server", "PingQuery", false, baseQuery.getSourceSid(), baseQuery.getSourceIp());
				else 
					BaseController.getInstance().sendResponse(new PingQuery("pong-point","match",null), "tcp-server", "PingQuery", false, baseQuery.getSourceSid(), output);				
			}
			else{
				if(output==null)
					BaseNetworkEngine.getInstance().forwardRequests(baseQuery);
				else 
					BaseController.getInstance().sendResponse(new PingQuery("pong-point","mismatch", PeersTable.getInstance().getAll()), "tcp-server", "PingQuery", false, baseQuery.getSourceSid(), output);
			}
		}
		
		if(pq.action.equals("pong-point")){
			valid = true;
			if(pq.result.equals("match")){
				PeersTable.getInstance().addEntry(baseQuery.getSourceIp(), baseQuery.getSourceSid(), "connected");
			}
		}
		
		if(pq.action.equals("ping-message")){
			System.out.println(pq.getExtraData());
			return;
		}
		
		if(pq.action.equals("ping-message-all")){
			//System.out.println(pq.getExtraData());
			//valid= true;
			BaseNetworkEngine.getInstance().forwardRequests(baseQuery);
			CallbackRegister.getInstance().notifyCallbacks("tcp-server-"+pq.action, pq);
		}
		
		if(valid)
			CallbackRegister.getInstance().notifyCallbacks("tcp-server-"+pq.action, baseQuery);
			
		if(!valid && output!=null)
			new ErrorModule(baseQuery, output, "Invalid cases for ping query");
	}
	
}


