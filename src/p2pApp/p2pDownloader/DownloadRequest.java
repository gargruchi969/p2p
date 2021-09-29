package p2pApp.p2pDownloader;

import static utility.Utilities.outputFolder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import p2pApp.p2pQueries.DownloadQuery;


public class DownloadRequest extends Thread {

	String fileId;
	String userIp;
	String userId;
	String filename;
	DownloadNodes node;

	public DownloadRequest(String fileId, String userIp){
		this(fileId, userIp, null, null);
	}

	public DownloadRequest(String fileId, String userIp, String filename){
		this(fileId, userIp, filename, null);
	}

	public DownloadRequest(String fileId, String userIp, String filename, String userId){
		this.fileId= fileId;
		this.userIp= userIp;
		this.userId= userId;
		this.filename= filename;

		this.start();
	}

	public DownloadRequest(String fileId, String userIp, String filename, String userId, DownloadNodes dn){
		this.fileId= fileId;
		this.userIp= userIp;
		this.userId= userId;
		this.filename= filename;
		this.node= dn;
		this.start();
	}

	public void run(){

		Socket clientSocket = null;

		try {
			clientSocket = new Socket( userIp, utility.Utilities.serverPort);
			startDownload(clientSocket);

		} catch (Exception e) {

		}
	}

	private void startDownload(Socket clientSocket) throws Exception{	

		File theDir = new File(outputFolder);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

		String data= utility.Utilities.makeRequest(new DownloadQuery("fileId",fileId), "p2p-app", userId, null, ""+clientSocket.getPort(), ""+utility.Utilities.serverPort, true, "DownloadQuery");

		DataInputStream input = new DataInputStream( clientSocket.getInputStream()); 
		DataOutputStream output = new DataOutputStream( clientSocket.getOutputStream()); 

		output.writeInt(data.length());
		output.writeBytes(data);

		try {
			long size= input.readLong();
			int n = 0;
			byte[]buf = new byte[utility.Utilities.bufferSize];

			System.out.println("Downloading file (singleMode): "+filename);

			FileOutputStream fos = new FileOutputStream(utility.Utilities.outputFolder+utility.Utilities.parseInvalidFilenames(filename));

			while (size > 0 && (n = input.read(buf, 0, (int)Math.min(buf.length, size))) != -1)
			{
				if(node.isPaused){
					input.close();
					node.setPartStatus(0, (byte)0);
					fos.close();
					return;
				}
				
				fos.write(buf,0,n);
				fos.flush();
				size -= n;
				node.percentKeeper.addDone(n);
			}
			fos.close();
			node.addPartsDone(userIp,0);
			System.out.println("Download completed: "+ filename);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}	
}
