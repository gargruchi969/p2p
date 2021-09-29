package tcpServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import tcpClient.TCPClient;
import utility.Query_v12;

public class BaseController {
	private ArrayList<Integer> queryIdSet;
	private int MAX_SIZE_SET= utility.Utilities.maxQuerySet;
	private static BaseController baseInstance;
	private TCPServer server=null;

	private BaseController(){
		queryIdSet= new ArrayList<Integer>(MAX_SIZE_SET);
	}

	public static BaseController getInstance(){
		if(baseInstance==null)
			baseInstance= new BaseController();
		return baseInstance;
	}


	public void startServer(){
		if(server!= null && server.getPort()==utility.Utilities.serverPort){
			System.out.println("Server already running at port: "+utility.Utilities.serverPort);
			return;
		}

		server= new TCPServer(utility.Utilities.serverPort);
	}

	public void startServer(int port){
		if(server.isServerRunning() && server.getPort()==port)
			System.out.println("Server already running at port: "+port);
		server= new TCPServer(port);
	}

	public void stopServer() throws IOException{
		if(server.isServerRunning()){
			server.stop();
			server= null;
		}
//		System.exit(0);
	}
	
	public boolean isServerRunning(){
		if(server==null)
			return false;
		return server.isServerRunning();
	}
	
	public int getServerPort(){
		return server.getPort();
	}

	public void sendRequest(String data, String module, String rtype, boolean response, String destId, String destIp){
		sendRequest(data, module, rtype, response, destId, destIp, 1);
	}

	public void sendRequest(Object data, String module, String rtype, boolean response, String destId, String destIp){
		sendRequest(data, module, rtype, response, destId, destIp, 1);
	}

	public void sendRequest(String data, String module, String rtype, boolean response, String destId, String destIp, int hopCount){	
		String requestData= utility.Utilities.makeRequest(getNewQueryId(), data, module, destId, null, null, null, response, hopCount);
		new TCPClient().sendRequestIntoNetwork(utility.Utilities.serverPort, destIp, requestData, response);
	}

	public void sendRequest(Object data, String module, String rtype, boolean response, String destId, String destIp, int hopCount){
		sendRequest(getNewQueryId(), data, module, rtype, response, destId, destIp, hopCount);
	}

	public void sendRequest(int queryId, Object data, String module, String rtype, boolean response, String destId, String destIp, int hopCount){
		String requestData= utility.Utilities.makeRequest(queryId, data, module, destId, null, null, null, response, rtype, hopCount);
		new TCPClient().sendRequestIntoNetwork(utility.Utilities.serverPort, destIp, requestData, response);
	}

	public void sendResponse(String data, String module, String rtype, boolean response, String destId, DataOutputStream output){
		if(output==null)
			return;
		try {
			String responseData= utility.Utilities.makeRequest(data, module, destId, null, null, null, response);
			output.writeInt(responseData.length());
			output.writeBytes(responseData);
		} catch (IOException e) {
			System.out.println("Base Controller #1 "+e.getMessage());
		}
		catch (Exception e) {
			System.out.println("Base Controller #2 "+e.getMessage());
		}
	}

	public void sendResponse(Object data, String module, String rtype, boolean response, String destId, DataOutputStream output){
		if(output==null)
			return;
		try {
			String responseData= utility.Utilities.makeRequest(data, module, destId, null, null, null, response,rtype);
			output.writeInt(responseData.length());
			output.writeBytes(responseData);
		} catch (IOException e) {
			System.out.println("Base Controller #1 "+e.getMessage());
		}
		catch (Exception e) {
			System.out.println("Base Controller #2 "+e.getMessage());
		}
	}

	//TCP Server query set functions

	public int addToQuerySet(int val){
		if(queryIdSet.size()>=MAX_SIZE_SET){
			queryIdSet.remove(0);
		}
		queryIdSet.add(val);
		return val;
	}

	public boolean isPresent(int val){
		return isPresent(val, true);
	}

	public boolean isPresent(int val, boolean add){
		try{
			if(!queryIdSet.contains(val)){
				if(add)
					addToQuerySet(val);
				return false;
			}
			return true;
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		return false;
	}

	public int getNewQueryId(){
		return getNewQueryId(utility.Utilities.selfExplicit);
	}

	public int getNewQueryId(boolean add){

		while(true){
			int val=utility.Utilities.getRandomNumber();
			if(!isPresent(val, add))
				return val;
		}
	}

	public boolean allowFurtherProcessing(Query_v12 query){

		if(isPresent(query.getQueryId()) || query.getHopCount()<=0)
			return false;

		return true;
	}
}
