package tcpServer;

//TCPServer.java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements Runnable { 

	final String TAG = "Server-Exception";
	int serverPort=0;
	ServerSocket listenSocket;
	
	TCPServer(){}
	
	TCPServer(int serverPort){
		this.serverPort= serverPort;
		new Thread(this).start();
	}

	public void run(){
		try{ 
			listenSocket = new ServerSocket(serverPort); 
			System.out.println("Server running at port: "+serverPort);

			while(true) { 
				Socket clientSocket = listenSocket.accept(); 
				new BaseReader(clientSocket);
			} 
		} 
		catch(IOException e) {
			System.out.println(TAG+"#1: "+e.getMessage());
		}
		catch(Exception e){
			System.out.println(TAG+"#2: "+e.getMessage());
		}
	}
	
	public void startAtPort(int port){
		this.serverPort= port;
		new Thread(this).start();
	}

	public void stop() throws IOException{

			if(!listenSocket.isClosed())
				listenSocket.close();
	}

	public boolean isServerRunning(){
		try{
			if(!listenSocket.isClosed() && listenSocket.isBound()){
				return true; 
			}
		}
		catch(Exception e){
			System.out.println(TAG+"#4: "+e.getMessage());
		}
		return false;
	}

	public String getServerInfo(){

		String data="n/a";
		try{
			data="Ip: "+utility.Utilities.getIpAddress()+"\nId: "+utility.Utilities.getSystemId();
		}
		catch(Exception e){
			System.out.println(TAG+"#5: "+e.getMessage());
		}
		return data;
	}
	
	public int getPort(){
		return serverPort;
	}

}

