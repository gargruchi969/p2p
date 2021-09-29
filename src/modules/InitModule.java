package modules;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import baseServer.BaseNetworkEngine;
import p2pApp.SearchTable;
import p2pApp.p2pDownloader.DownloadEngine;
import p2pApp.p2pDownloader.DownloadNodes;
import p2pApp.p2pDownloader.SaveDownloadNodes;
import p2pApp.p2pIndexer.TableHandler;
import tcpUtilities.CallbackRegister;
import tcpUtilities.PeersEntries;
import tcpUtilities.PeersTable;
import utility.MySqlHandler;

public class InitModule {

	PeersTable peersTable;
	CallbackRegister callbackRegis;
	BaseNetworkEngine networkEngine;

	public InitModule(){

		peersTable= PeersTable.getInstance();
		callbackRegis= CallbackRegister.getInstance();
		networkEngine= BaseNetworkEngine.getInstance();
		SearchTable.getInstance();
	}

	public void initSystem() throws Exception{
		initPingPongCallbacks();
		initDirs();
		loadSystemVariables();
		initUserValues();
		initSystemValues();

		getPausedDownloads();
		initPeersTable();
	}

	private void initPingPongCallbacks(){
		callbackRegis.registerForCallback("tcp-server-pong", "baseServer.BaseNetworkEngine", "manageNeighboursList", false, networkEngine);
		callbackRegis.registerForCallback("tcp-server-ping", "baseServer.BaseNetworkEngine", "manageNeighboursList", false, networkEngine);
		callbackRegis.registerForCallback("ConnectionError", "baseServer.BaseNetworkEngine", "ConnectErrorHandler", false, networkEngine);
	}

	private void initSystemValues() throws Exception{

		String ip;
		System.out.println("\nInitialising System variables... \n");
		System.out.println("User-Ip: " + (ip= utility.Utilities.getIpAddress(utility.Utilities.baseIp)));
		System.out.println("User-Id: " + utility.Utilities.getSystemId()+"\n");

		if(ip==null){
			throw new Exception("** Unable to find a network connection. Connect to network. **");
		}

		ArrayList<String> names= new ArrayList<String>();
		for(int i=0;i<utility.Utilities.inputFolders.length;i++){
			if(utility.Utilities.inputFolders[i].trim().length()>2)
				names.add(utility.Utilities.inputFolders[i].trim().replace("\\", "/"));
		}
		names.add(utility.Utilities.outputFolder.trim().replace("\\","/"));

		if(!new File(utility.Utilities.outputFolder).exists()){
			new File(utility.Utilities.outputFolder).mkdirs();
		}

		System.out.println("Loading databases... \n");

		try{
			if(TableHandler.tableType.equals("mysql")){
				MySqlHandler.getInstance().TestDatabase();
				p2pApp.p2pIndexer.DirectoryReader.DR_init(names, true);
			}
			else{
				p2pApp.p2pIndexer.DirectoryReader.getInstance().indexDirectories(names);
			}
		}
		catch(Exception e){
			throw e;
		}

		System.out.println("Finished Loading databases... \n");

	}

	private void initPeersTable(){

		String s= utility.Utilities.readFromIpFile("data/ips.dat");
		try{

			String [] arr= s.split("\n");
			for(int i=0;i <arr.length;i++){
				String []temp= arr[i].split(" ");
				PeersTable.getInstance().addEntry(temp[0], temp[1], "connected");
			}

			List<PeersEntries> pe= PeersTable.getInstance().getAll();
			for(int i=0;i<pe.size();i++){
				utility.Utilities.writeToFile("data/ips.dat", pe.get(i).ip+" "+pe.get(i).systemId, false);
			}
		}
		catch(Exception e){

		}
	}

	private void initUserValues() throws Exception{

		try{
			Properties props = new Properties();
			FileInputStream fis = null;
			fis = new FileInputStream("data/config.properties");
			props.load(fis);

			utility.Utilities.baseIp= props.getProperty("p2p.baseIp");
			utility.Utilities.outputFolder= props.getProperty("p2p.outputFolder");
			utility.Utilities.streamLocation= props.getProperty("p2p.streamPlayer");
			utility.Utilities.userName= props.getProperty("p2p.userName");
			
			if(utility.Utilities.userName==null || utility.Utilities.userName.length()==0)
				utility.Utilities.userName= "User";
			
			String str= props.getProperty("p2p.inputFolder");
			utility.Utilities.inputFolders= str.split(",");
			utility.Utilities.setSystemId(props.getProperty("p2p.systemId"));
		}
		catch(Exception e){
			throw new Exception("Error: Config file not found. "+e.getMessage()+" Go to settings to create a new file.");
		}
	}

	private void getPausedDownloads(){

		ArrayList<DownloadNodes> an= new ArrayList<DownloadNodes>();
		File aFile= new File("data/partials");
		File[] listOfFiles = aFile.listFiles();
		if(listOfFiles!=null) {
			for (int i = 0; i < listOfFiles.length; i++){
				try{
					String content= new String(Files.readAllBytes(Paths.get(listOfFiles[i].getAbsolutePath())));
					//listOfFiles[i].delete();
					DownloadNodes dn=new DownloadNodes((SaveDownloadNodes)utility.Utilities.getObjectFromJson(content, SaveDownloadNodes.class)); 
					if(new File(utility.Utilities.outputFolder+utility.Utilities.parseInvalidFilenames(dn.getSearchResults().getFilename())).exists())
						an.add(dn);
					else
						listOfFiles[i].delete();
				}
				catch(Exception e){
					listOfFiles[i].delete();
					System.out.println("Error while loading paused downloads: "+e.getMessage());
				}
			}
			DownloadEngine.getInstance().AddPausedDownloads(an);
		}
	}

	private void initDirs(){

		if(!new File("data/partials").exists())
			new File("data/partials").mkdirs();

		if(!new File("data/db-ms.properties").exists()){
			String data= "# mysql properties\n"+
					"# complete url= url+':'+port+'/'+database\n\n"+
					"mysql.driver=com.mysql.jdbc.Driver\n"+
					"mysql.url=jdbc:mysql://localhost\n"+
					"mysql.port=3306\n"+
					"mysql.database=test\n"+
					"mysql.username=root\n"+
					"mysql.password=\n"+
					"mysql.unicode=useUnicode=yes&characterEncoding=UTF-8\n";

			utility.Utilities.writeToFile("data/db-ms.properties", data, false);
		}

		if(!new File("data/system.properties").exists()){
			String data= "#system variables\n\n"+
					"neighbourCount= 3\n"+
					"maxRequests= 3\n"+
					"connectTimeout= 3000\n"+
					"maxHop= 7\n"+
					"maxClientRequest= 20\n"+
					"maxServerResponse= 50\n"+
					"selfExplicit= false\n"+
					"selfNeighbour= true\n"+
					"selfRequest= true\n"+
					"bufferSize= 8192\n"+
					"maxDownloadThread= 4\n"+
					"maxParallelDownloads= 3\n"+ 
					"resultSetSize= 2\n"+
					"maxQuerySet= 100\n"+
					"defaultMode= true\n"+
					"singleMode= false\n"+
					"debugMode= false";
			
			utility.Utilities.writeToFile("data/system.properties", data, false);
		}
	}

	private void loadSystemVariables(){

		try{
			Properties props = new Properties();
			FileInputStream fis = null;
			fis = new FileInputStream("data/system.properties");
			props.load(fis);

			utility.Utilities.defaultMode= Boolean.parseBoolean(props.getProperty("defaultMode").trim());
			
			if(!utility.Utilities.defaultMode){
			
				utility.Utilities.neighbourPeersCount= Integer.parseInt(props.getProperty("neighbourCount").trim());
				utility.Utilities.maxSimultaneousRequests= Integer.parseInt(props.getProperty("maxRequests").trim());
				utility.Utilities.connectionTimeout= Integer.parseInt(props.getProperty("connectTimeout").trim());
				utility.Utilities.maxHopCount=Integer.parseInt(props.getProperty("maxHop").trim());
				utility.Utilities.maxParallelClientRequests= Integer.parseInt(props.getProperty("maxClientRequest").trim());
				utility.Utilities.maxParallelServerRequests= Integer.parseInt(props.getProperty("maxServerResponse").trim());
				utility.Utilities.selfExplicit= Boolean.parseBoolean(props.getProperty("selfExplicit").trim());
				utility.Utilities.selfNeighbour= Boolean.parseBoolean(props.getProperty("selfNeighbour").trim());
				utility.Utilities.selfRequest= Boolean.parseBoolean(props.getProperty("selfRequest").trim());
				utility.Utilities.bufferSize= Integer.parseInt(props.getProperty("bufferSize").trim());
				utility.Utilities.maxDownloadThreadCount= Integer.parseInt(props.getProperty("maxDownloadThread").trim());
				utility.Utilities.maxParallelDownloads= Integer.parseInt(props.getProperty("maxParallelDownloads").trim());
				utility.Utilities.resultSetSize= Integer.parseInt(props.getProperty("resultSetSize").trim());
				utility.Utilities.maxQuerySet= Integer.parseInt(props.getProperty("maxQuerySet"));
				utility.Utilities.debugMode= Boolean.parseBoolean(props.getProperty("debugMode").trim());
				utility.Utilities.singleMode= Boolean.parseBoolean(props.getProperty("singleMode").trim());
			}
			fis.close();
		}
		catch(Exception e){
			System.out.println("Error while loading system variables. "+e.getMessage());
		}

	}
}
