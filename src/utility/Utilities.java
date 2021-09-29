package utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utilities{

	private static String TAG="Utilities ";
	private static String ipAddress="";
	private static String systemId="";
	public static int serverPort= 7000;
	public static int streamPort= 8000;
	public static int neighbourPeersCount= 3;
	public static int maxSimultaneousRequests= 3;
	public static int connectionTimeout= 3000;
	public static int maxHopCount=7;
	public static int maxParallelClientRequests= 20;
	public static int maxParallelServerRequests= 50;
	public static boolean selfExplicit= false;
	public static boolean selfNeighbour= true;
	public static boolean selfRequest= true;
	public static String modulesBasePath= "lib/app-modules/jars/";
	public static String baseIp= "172";
	public static String outputFolder= "D:/p2p/Downloads/";
	public static int activeSearchId= 0;
	public static int bufferSize= 8192;
	public static int maxDownloadThreadCount= 4;
	public static int maxParallelDownloads= 3;
	public static String[] inputFolders;
	public static int resultSetSize= 2;
	public static int maxQuerySet= 100;
	public static String streamLocation = "";
	public static String searchCol= "Path";
	public static String userName= "";
	public static boolean debugMode= true;
	public static boolean singleMode= false;
	public static boolean defaultMode= true;
	
	public static String getIpAddress(){
		if(ipAddress==null || ipAddress.length()<=4){
			ipAddress=getLocalIpAddress();
		}
		return ipAddress;	
	}

	public static String getIpAddress(String baseIp){
		if(ipAddress==null || ipAddress.length()<=4 || !ipAddress.contains(baseIp)){
			ipAddress= getValidIpAddress(baseIp);
		}
		return ipAddress;
	}

	private static String getValidIpAddress(String baseIp){

		try{
			String addr= InetAddress.getLocalHost().toString();
			if(addr.contains(baseIp))
				return stripSlashesFromIp(addr, baseIp);

			List<InterfaceAddress> li;
			Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
			while(ni.hasMoreElements()){
				li= ni.nextElement().getInterfaceAddresses();
				int i=0;
				while(i<li.size()){
					if((addr=li.get(i++).getAddress().toString()).contains(baseIp))
						return stripSlashesFromIp(addr, baseIp);
				}
			}
		}
		catch(Exception e){
			System.out.println(TAG+"#1 "+e.getMessage());
		}
		return null;
	}

	private static String stripSlashesFromIp(String addr, String base){
		return addr.substring(addr.indexOf(base));
	}

	private static String getLocalIpAddress(){
		try{
			String t= InetAddress.getLocalHost().toString();
			if(t.indexOf("/")==-1)
				return t;
			return t.substring(t.indexOf("/")+1);
		}
		catch(Exception e){
			System.out.println(TAG+"#2 "+e.getMessage());
		}
		return null;
	}

	public static void setSystemId(String x){
		systemId= x;
	}
	
	public static String getSystemId(){
		
		if(systemId!=null && !systemId.isEmpty())
			return systemId;
		byte[] mac;
		StringBuilder sb= new StringBuilder();
		int j=0;
		try{
			for(int k=0;k<100 && j<3;k++){

				NetworkInterface nif= NetworkInterface.getByIndex(k);
				if(nif==null || (mac=nif.getHardwareAddress())==null)
					continue;
				for (int i = 0; i < mac.length; i++) {
					sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
				}
				if(mac.length>0)
					j++;
			}

			byte bytes[] = sb.toString().getBytes();
			Checksum checksum= new CRC32();
			checksum.update(bytes, 0, bytes.length);

			return (systemId=String.format("%x",checksum.getValue()));
		}		
		catch(Exception e){
			System.out.println(TAG+"#3 "+e.getMessage());
		}
		return null;
	}

	public static String getSystemMAC(){

		try {
			InetAddress ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
			return sb.toString();
		} catch (Exception e) {
			System.out.println(TAG+"#7 "+e.getMessage());
		} 
		return null;
	}

	public static String makeRequest(Object obj, String type){
		try{
			return getJsonString(new Query_v12(type, getJsonString(obj)));
		}
		catch(Exception e){
			System.out.println(TAG+"#4 "+e.getMessage());
		}
		return null;
	}

	public static String makeRequest(String data, String type){
		try{
			return getJsonString(new Query_v12(type, getJsonString(data)));
		}
		catch(Exception e){
			System.out.println(TAG+"#9 "+e.getMessage());
		}
		return null;
	}
	
	public static String makeRequest(String data, String module, String destId, String code, String sourcePort, String destPort, boolean res){
		return makeRequest(getRandomNumber(), data, module, destId, code, sourcePort, destPort, res,1);
	}
	
	public static String makeRequest(Object data, String module, String destId, String code, String sourcePort, String destPort, boolean res, String className){
		return makeRequest(getRandomNumber(), data, module, destId, code, sourcePort, destPort, res, className,1);
	}
	
	public static String makeRequest(String data, String module, String destId, String code, String sourcePort, String destPort, boolean res, int hopCount){
		return makeRequest(getRandomNumber(), data, module, destId, code, sourcePort, destPort, res, hopCount);
	}
	
	public static String makeRequest(Object data, String module, String destId, String code, String sourcePort, String destPort, boolean res, String className, int hopCount){
		return makeRequest(getRandomNumber(), data, module, destId, code, sourcePort, destPort, res, className, hopCount);
	}
	
	public static String makeRequest(int qid, String data, String module, String destId, String code, String sourcePort, String destPort, boolean res){
		return makeRequest(getRandomNumber(), data, module, destId, code, sourcePort, destPort, res,1);
	}
	
	public static String makeRequest(int qid, Object data, String module, String destId, String code, String sourcePort, String destPort, boolean res, String className){
		return makeRequest(getRandomNumber(), data, module, destId, code, sourcePort, destPort, res, className,1);
	}

	public static String makeRequest(int qid, String data, String module, String destId, String code, String sourcePort, String destPort, boolean res, int hopCount){
		try{
			return getJsonString(new Query_v12(qid, module, getJsonString(data), getSystemId(), destId, getIpAddress(), code, sourcePort, destPort, res, "string", hopCount));
		}
		catch(Exception e){
			System.out.println(TAG+"#9 "+e.getMessage());
		}
		return null;
	}
	
	public static String makeRequest(int qid, Object data, String module, String destId, String code, String sourcePort, String destPort, boolean res, String className, int hopCount){
		try{
			return getJsonString(new Query_v12(qid, module, getJsonString(data), getSystemId(), destId, getIpAddress(), code, sourcePort, destPort, res, className, hopCount));
		}
		catch(Exception e){
			System.out.println(TAG+"#9 "+e.getMessage());
		}
		return null;
	}
	
	public static String makeRequest(List<?> list, String type){
		try{
			return getJsonString(new Query_v12(type, getJsonString(list)));
		}
		catch(Exception e){
			System.out.println(TAG+"#10 "+e.getMessage());
		}
		return null;
	}

	public static Query_v12 getQueryObject(String data){
		return Query_v12.class.cast(getObjectFromJson(data, Query_v12.class));
	}

	public static String getJsonString(Object obj){
		Gson gson = new GsonBuilder().create();
		return gson.toJson(obj);
	}

	public static Object getObjectFromJson(String jsonString, Class<?> toClass){  
		try {
			return new Gson().fromJson(jsonString, toClass);
		} catch (Exception e) {
			System.out.println(TAG+"#5 "+e.getMessage());
		}
		return null;
	}

	public static String getBaseSystemId(){

		String result=null;

		try{
			String command = "ipconfig /all";
			Process p = Runtime.getRuntime().exec(command);

			BufferedReader inn = new BufferedReader(new InputStreamReader(p.getInputStream()));
			Pattern pattern = Pattern.compile(".*Physical Addres.*: (.*)");

			while (true) {
				String line = inn.readLine();

				if (line == null)
					break;

				Matcher mm = pattern.matcher(line);
				if (mm.matches()) {
					result=result+mm.group(1);
				}
			}

			byte bytes[] = result.getBytes();
			Checksum checksum= new CRC32();
			checksum.update(bytes, 0, bytes.length);
			return String.format("%x", checksum.getValue());
		}

		catch(Exception e){
			System.out.println(TAG+"#6 "+e.getMessage());
		}
		return null;
	}

	public static String getSlowerSystemId(){
		byte[] mac;
		StringBuilder sb= new StringBuilder();
		try{
			Enumeration<NetworkInterface> nic = NetworkInterface.getNetworkInterfaces();
			while(nic.hasMoreElements()){
				Enumeration<InetAddress> ia= nic.nextElement().getInetAddresses();
				while(ia.hasMoreElements()){
					NetworkInterface nid= NetworkInterface.getByInetAddress(ia.nextElement());
					mac= nid.getHardwareAddress();
					for (int i = 0; i < mac.length; i++) {
						sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
					}
				}
			}

			byte bytes[] = sb.toString().getBytes();
			Checksum checksum= new CRC32();
			checksum.update(bytes, 0, bytes.length);

			return String.format("%x",checksum.getValue());
		}		
		catch(Exception e){
			System.out.println(TAG+"#8 "+e.getMessage());
		}
		return null;
	}
	
	public static int getRandomNumber(){
		return ThreadLocalRandom.current().nextInt(1000000,1000000000);
	}
	
	public static String humanReadableByteCount(String bytes, boolean si) {
		return humanReadableByteCount( Long.parseLong(bytes), si);
	}
	
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static void writeToFile(String path, ArrayList<String> al){
		
		try{
			
		FileWriter fw= new FileWriter(path);
		for(int i=0;i<al.size();i++){
			fw.write(al.get(i));
		}
		fw.close();
		}
		catch(Exception e){
			System.out.println("FileWrite Exception #1 " + e.getMessage());
		}
		
	}
	
	public static void writeToFile(String path, String data, boolean append){
		
		try{
			
		FileWriter fw= new FileWriter(path, append);
		fw.write(data+"\n");
		fw.close();
		}
		catch(Exception e){
			System.out.println("FileWrite Exception #2 " + e.getMessage());
		}
		
	}
	
	public static void writeObjectToFile(String path, Object obj){
		
		try{
			
		FileOutputStream fos= new FileOutputStream(path);
		ObjectOutputStream oos= new ObjectOutputStream(fos);
		oos.writeObject(obj);
		oos.close();
		fos.close();
		}
		catch(Exception e){
			System.out.println("FileWriteToObject Exception #3 " + e.getMessage());
		}
		
	}
	public static String readFromIpFile(String path){
	
	    String s="";
		
		 try{  
			    FileInputStream fin=new FileInputStream(path);  
			    int i=0;  
			    while((i=fin.read())!=-1){  
			     s=s+(char)i; 
			    }  
			    fin.close();  
			  }
		 catch(Exception e){
				  System.out.println("FileRead Exception #1 "+e.getMessage());
		 }
		 return s;
	}  
	
	public static String parseInvalidFilenames(String filename){
		return filename.replace("?", "").trim();
	}
	
	private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validateIp(final String ip) {
        return PATTERN.matcher(ip).matches();
    }
    
    public static void streamOnCommandLine(String ip, int stream, String fileid, String filename){
    	
    	try {
    		String streamUrl = "http://"+ip+":8000/?f="+fileid+"&NAME= "+ filename;
			Runtime.getRuntime().exec(new String[] {streamLocation, streamUrl});
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
