package tcpClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import modules.ErrorModule;
import modules.TCPClientModule;
import p2pApp.AppServer;
import tcpUtilities.CallbackRegister;
import utility.Query_v12;

public class TCPClient extends Thread {

	private int serverPort;
	private String ip;
	private String data;
	private Socket s= null;
	boolean response= false;
	private static int requestCount=0;
	
	public void sendRequestIntoNetwork(int serverPort, String ip, String data, boolean response){
		this.serverPort= serverPort;
		this.ip= ip;
		this.data= data;
		this.response= response;
		validateRequest();
	}

	public static int getRequestCount(){
		return requestCount;
	}
	
	private void validateRequest(){
		
		if(requestCount++ >= utility.Utilities.maxParallelClientRequests){
			new ErrorModule("System Overload: Too many requests being made!");
			return;
		}
		
		if(!utility.Utilities.selfRequest && ip.equals(utility.Utilities.getIpAddress()))
			return;
		
		this.start();
	}
	
	public void run(){

		try{ 
			s = new Socket();
			s.connect(new InetSocketAddress(ip, serverPort), utility.Utilities.connectionTimeout);
			
			DataInputStream input = new DataInputStream( s.getInputStream()); 
			DataOutputStream output = new DataOutputStream( s.getOutputStream()); 
			output.writeInt(data.length());
			output.writeBytes(data);
			
			if(response){
				
				int nb = input.readInt();
				byte []digit= new byte[nb];
				for(int i = 0; i < nb; i++)
					digit[i] = input.readByte();
				String st = new String(digit);

				Query_v12 query= utility.Utilities.getQueryObject(st);
				
				if(!query.getResponse())
					output= null;
				
				if(utility.Utilities.debugMode)
				System.out.println("Received from Server ("+ip+":"+serverPort+")\n"+st);

				switch(query.getModule()){
				
					case "tcp-server": 
						new TCPClientModule(query, output);
						break;
						
					case "error":
						new ErrorModule(query.getPayload());
						break;
					
					case "p2p-app":
						new AppServer(query, output);
						break;
						default:
							new ErrorModule(query, output, "No such module found!");
				}
			}
			else{
				input=null;
			}
		}
		catch (UnknownHostException e){ 
			System.out.println("Socket:"+e.getMessage());}
		
		catch (EOFException e){
			System.out.println("EOF:"+e.getMessage()); }
		
		catch (IOException e){
			System.out.println("IO:"+e.getMessage());
			if(e.getMessage().contains("timed out")){
				CallbackRegister.getInstance().notifyCallbacks("ConnectionError", new String[]{"TimedOut", this.ip});
			} 
			else{
				CallbackRegister.getInstance().notifyCallbacks("ConnectionError", new String[]{"Refused", this.ip});
			}
		}

		finally {
			requestCount--;
			
			if(s!=null){	
				try {
					s.close();
				}
				catch (IOException e){
					System.out.println(e.getMessage());
				}
			}
		}
	}
}

