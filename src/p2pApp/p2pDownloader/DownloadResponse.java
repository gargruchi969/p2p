package p2pApp.p2pDownloader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DownloadResponse {

	String filepath;
	DataOutputStream output;
	public DownloadResponse(String filepath, DataOutputStream output){
		
		this.filepath= filepath;
		this.output= output;
		
		sendFile();
	}
	
	private void sendFile(){
		
		File myFile = new File(filepath);
        try {
        	send(myFile);
        } catch (FileNotFoundException ex) {
            System.out.println("Download Response #1: "+ex.getMessage());
        }catch(Exception e){
        	System.out.println("Download Response #2: "+e.getMessage());
        }
        
        
	}
      
	public void send(File myFile) throws Exception{
		
		System.out.println("Sending file: "+filepath);
		int n = 0;
	    byte[]buf = new byte[utility.Utilities.bufferSize];
	    
	    output.writeLong(myFile.length());
		output.flush();
		
		FileInputStream fis = new FileInputStream(myFile);
	    while((n =fis.read(buf)) != -1){
	    	output.write(buf,0,n);
	        output.flush();
	    }
	    fis.close();
	    output.close();
	    System.out.println("File sending completed");
	}
}