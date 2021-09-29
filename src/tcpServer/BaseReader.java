package tcpServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import modules.ErrorModule;
import modules.TCPServerModule;
import p2pApp.AppServer;
import utility.Query_v12;

public class BaseReader extends Thread {

	DataInputStream input; 
	DataOutputStream output; 
	Socket clientSocket; 
	final private String TAG= "Base Reader";
	private static int serverRequestCount=0;
	
	public BaseReader (Socket aClientSocket) { 
		
		try { 
			clientSocket = aClientSocket; 
			input = new DataInputStream( clientSocket.getInputStream()); 
			output =new DataOutputStream( clientSocket.getOutputStream()); 
			this.start(); 
		} 
		catch(IOException e) {
			System.out.println(TAG+"#1 "+e.getMessage());
		}
		catch(Exception e){
			System.out.println(TAG+"#2 "+e.getMessage());
		}
	}
	
	public static int getRequestCount(){
		return serverRequestCount;
	}
	
	public void run(){
		
		if(serverRequestCount++ >= utility.Utilities.maxParallelServerRequests){
			new ErrorModule("System Overload: Too many requests to the Server!");
			return;
		}
		
		try{
			int readLen= input.readInt();
			byte[] digit = new byte[readLen];

			for(int i = 0; i < readLen; i++)
				digit[i] = input.readByte();
			String data= new String(digit);
			Query_v12 query= utility.Utilities.getQueryObject(data);
			
			if(!BaseController.getInstance().allowFurtherProcessing(query))
				return;
			
			if(utility.Utilities.debugMode)
			System.out.println ("Received from Client : " + 
					clientSocket.getInetAddress() + ":" +
					clientSocket.getPort() + "\n" + data);
			
			
			if(!query.getResponse())
				output= null;
			
			switch(query.getModule()){
			
			case "tcp-server":
				new TCPServerModule(query, output);
				break;
				
			case "error":
				new ErrorModule(query.getPayload());
				break;
				
			case "p2p-app":
				new AppServer(query, output);
				break;
				
				default:
					new ErrorModule(query, output, "No such module found!");
					break;
			}
		}
			
		catch(IOException e){
			
		}
		catch(Exception e){
			
		}
		finally{
			serverRequestCount--;
		}
	}
}
