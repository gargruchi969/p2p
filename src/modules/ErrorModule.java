package modules;

import java.io.DataOutputStream;

import tcpServer.BaseController;
import utility.Query_v12;

public class ErrorModule {
	Query_v12 baseQuery;
	DataOutputStream output;
	
	public ErrorModule(){
		this("Unable to process the request. An error occured!");
	}
	
	public ErrorModule(String msg){
		echoMessage(msg);
	}
	
	public ErrorModule(Query_v12 q, DataOutputStream o){
		this(q,o, "Invalid request!");
	}
	
	public ErrorModule(Query_v12 q, DataOutputStream o, String msg){
		baseQuery= q;
		output=o;
		sendErrorResponse(msg);
	}
	
	private void sendErrorResponse(String msg){
		if(output==null)
			return;
		BaseController.getInstance().sendResponse(msg, "error", "string", false, baseQuery.getSourceSid(), output);
		return;
	}
	
	private void echoMessage(String msg){
		System.out.println(" "+msg+" ");
	}
}
