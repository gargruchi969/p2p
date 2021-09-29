package modules;

import java.io.DataOutputStream;
import tcpQueries.PingQuery;
import tcpServer.BaseController;
import tcpUtilities.CallbackRegister;
import tcpUtilities.PeersEntries;
import tcpUtilities.PeersTable;
import utility.Query_v12;

public class TCPClientModule {

	Query_v12 baseQuery;
	DataOutputStream output;
	
	public TCPClientModule(Query_v12 query, DataOutputStream output){
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
		else if(type.equals("PingQuery")){
				new PongModule(baseQuery, output);
		}
	}
	
	private void echoString(){
		BaseController.getInstance().sendResponse(baseQuery.getPayload(), baseQuery.getModule(), "string", false, baseQuery.getSourceSid(), output);
	}
}

class PongModule{
	
	Query_v12 baseQuery;
	DataOutputStream output;
	PongModule(Query_v12 q, DataOutputStream o){
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
		PingQuery pq= (PingQuery)(utility.Utilities.getObjectFromJson(baseQuery.getPayload(), Class.forName("tcpQueries.PingQuery")));
		processPongQuery(pq);
	}
	
	private void processPongQuery(PingQuery pq){
		
		boolean valid = false;
		
		if(pq.action.equals("pong")){
			PeersEntries pe;
			for(int i=0;i<pq.peers.size();i++){
				pe= pq.peers.get(i);
				PeersTable.getInstance().addEntry(pe.ip, pe.systemId, pe.status, pe.time);
			}
			PeersTable.getInstance().addEntry(baseQuery.getSourceIp(), baseQuery.getSourceSid(), "connected");
			valid= true;
		}
		
		if(pq.action.equals("pong-force")){
			PeersEntries pe;
			for(int i=0;i<pq.peers.size();i++){
				pe= pq.peers.get(i);
				PeersTable.getInstance().addEntry(pe.ip, pe.systemId, pe.status, pe.time);
			}
			PeersTable.getInstance().addEntry(baseQuery.getSourceIp(), baseQuery.getSourceSid(), "connected");
			PeersTable.getInstance().addNeighbourPeers(baseQuery.getSourceIp(), baseQuery.getSourceSid(), "connected", true);
		}
		
		if(pq.action.equals("pong-message")){
			System.out.println(pq.getExtraData());
			return;
		}
					
		if(valid)
			CallbackRegister.getInstance().notifyCallbacks(baseQuery.getModule()+"-"+pq.action, baseQuery);
		
		if(!valid && output!=null)
			new ErrorModule(baseQuery, output, "Invalid cases for pong query");
	}
}

