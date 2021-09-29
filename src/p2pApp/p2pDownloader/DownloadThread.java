package p2pApp.p2pDownloader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import p2pApp.p2pQueries.DownloadQuery;

public class DownloadThread extends Thread{

	String userIp;
	String fileId;
	String userId;
	int part;
	String segMode;
	String filename;
	FileChannel fos;
	DownloadNodes node;
	static int DownloadThreadsCount=0;
	int delay= 0;
	
	public DownloadThread(String userIp, String fileId, String userId, int part, String segMode, String filename, FileChannel fos, DownloadNodes node){
		
		this.userIp= userIp;
		this.fileId= fileId;
		this.userId= userId;
		this.part= part;
		this.segMode= segMode;
		this.filename= filename;
		this.fos= fos;
		this.node= node;
		
		this.start();
	}
	
	public DownloadThread(String userIp, String fileId, String userId, int part, String segMode, String filename, FileChannel fos, DownloadNodes node, int delay){
		
		this.userIp= userIp;
		this.fileId= fileId;
		this.userId= userId;
		this.part= part;
		this.segMode= segMode;
		this.filename= filename;
		this.fos= fos;
		this.node= node;
		this.delay= delay;
		
		this.start();
	}

	public int getDelay(){
		return delay;
	}
	
	@Override
	public void run(){
		Socket clientSocket = null;
		DownloadThreadsCount++;
		
		try {
			
			if(delay>0){
				Thread.sleep(delay);
			}
			clientSocket = new Socket();
			clientSocket.connect(new InetSocketAddress(userIp, utility.Utilities.serverPort),
					utility.Utilities.connectionTimeout);
			startDownload(clientSocket);
		}
		catch(Exception e){
			System.out.println("Download Thread #1 "+e.getMessage()+ " : "+filename+":"+part);
			if(e.getMessage().contains("timed out")){
				node.removeIp(userIp, part);
			}
		}
		finally{
			DownloadThreadsCount--;
		}
	}

	private void startDownload(Socket clientSocket) throws Exception{
		DataInputStream input = new DataInputStream( clientSocket.getInputStream()); 
		DataOutputStream output = new DataOutputStream( clientSocket.getOutputStream()); 

		String data= utility.Utilities.makeRequest(new DownloadQuery("fileId",fileId, segMode, part), "p2p-app", userId, null, ""+clientSocket.getPort(), ""+utility.Utilities.serverPort, true, "DownloadQuery");
		output.writeInt(data.length());
		output.writeBytes(data);

		readInputStream(input);
	}

	private void readInputStream(DataInputStream input) throws Exception{

		System.out.println("Downloading file: "+ filename + " : "+ part);
		long size= input.readLong();
		int n=0;
		byte[]buf = new byte[utility.Utilities.bufferSize];
		//  FileOutputStream fos = new FileOutputStream(utility.Utilities.outputFolder+utility.Utilities.parseInvalidFilenames(filename));
		fos.position(part*(new SegmentationModes(segMode)).getSize());
		while (size > 0 && (n = input.read(buf, 0, (int)Math.min(buf.length, size))) != -1){

			if(node.isPaused){
				input.close();
				node.setPartStatus(part, (byte)0);
				return;
			}
			
			ByteBuffer bf= ByteBuffer.wrap(buf,0, n);
			
			while(bf.hasRemaining()){
				fos.write(bf);
			}
			//fos.flush();
			size -= n;
			node.percentKeeper.addDone(n);
		}
		fos.force(true);
		node.addPartsDone(userIp, part);
		System.out.println("Downloaded file done: "+ filename + " : "+ part);
	}
}

