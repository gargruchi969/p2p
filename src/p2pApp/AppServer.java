package p2pApp;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import baseServer.BaseNetworkEngine;
import p2pApp.p2pDownloader.DownloadResponse;
import p2pApp.p2pDownloader.UploadThread;
import p2pApp.p2pIndexer.TableHandler;
import p2pApp.p2pQueries.DownloadQuery;
import p2pApp.p2pQueries.GetDirQuery;
import p2pApp.p2pQueries.SearchQuery;
import tcpServer.BaseController;
import tcpUtilities.CallbackRegister;
import utility.LuceneHandler;
import utility.Query_v12;
public class AppServer {

	Query_v12 query;
	DataOutputStream output;
	SearchQuery searchQuery;

	public AppServer(Query_v12 query, DataOutputStream output){
		this.query= query;
		this.output= output;
		processRequest();
	}

	private void processRequest(){

		if(query.getResponseType().equals("SearchQuery")){
			searchQuery= (SearchQuery) utility.Utilities.getObjectFromJson(query.getPayload(), SearchQuery.class);

			if(searchQuery.mode.equals("search")){
				//				BaseNetworkEngine.getInstance().forwardRequests(query);
				SearchDatabase sd= new SearchDatabase(searchQuery);
				sendResults(sd.getResults(), sd.getSize());
			}

			if(searchQuery.mode.equals("results")){
				SearchTable.getInstance().addEntries(searchQuery.results, query.getSourceIp(), searchQuery.searchId);
				if(utility.Utilities.debugMode)
					echoResults(SearchTable.getInstance().getSearchTable());
				//UISearch.updateTable(SearchTable.getInstance().getSearchTable());
				//UIController.addResults(SearchTable.getInstance().getSearchTable());
				CallbackRegister.getInstance().notifyCallbacks("p2p-app-results", null);
			}
		}

		if(query.getResponseType().equals("DownloadQuery")){
			DownloadQuery dq= (DownloadQuery)utility.Utilities.getObjectFromJson(query.getPayload(), DownloadQuery.class);
			String path= TableHandler.getFilePath(dq.key);
			//		new DownloadResponse(path, output); 
			// DownloadResponse is for sequential download
			//UploadThread is for segmented downloading
			if(dq.mode.equals("single"))
				new DownloadResponse(path, output); 
			else
				new UploadThread(path, output, dq.part, dq.segMode);
		}

		if(query.getResponseType().equals("GetDirQuery")){
			GetDirQuery gdq= (GetDirQuery)utility.Utilities.getObjectFromJson(query.getPayload(), GetDirQuery.class);
			if(gdq.action.equals("search")){
				if(gdq.mode.equals("fileId")){
					sendDirFiles(gdq.getDirId, gdq.key, gdq.name);
				}
			}
			if(gdq.action.equals("results")){
				//DownloadEngine.getInstance().batchAdd(gdq.name, gdq.files);
				CallbackRegister.getInstance().notifyCallbacks("p2p-app-dir-listfiles", gdq);
				CallbackRegister.getInstance().notifyCallbacks("p2p-app-dir-listfiles-only", gdq);
			}
		}
	}

	private void sendResults(Object data, int size){
		try{
			BaseController.getInstance().sendRequest(data, query.getModule(), "SearchQuery", false, "", query.getSourceIp(), 1);
			if(size >= utility.Utilities.resultSetSize) 
				BaseNetworkEngine.getInstance().forwardRequests(query, true);
			else
				BaseNetworkEngine.getInstance().forwardRequests(query, false);
		}
		catch(Exception e){
			System.out.println("AppServer #1 "+e.getMessage());
		}
	}

	private void echoResults(ArrayList<SearchResults> al){
		for(int i=0; i<al.size();i++){
			System.out.println(i+" "+al.get(i).ip+" "+ al.get(i).filename+ " "+al.get(i).filesize +" "+al.get(i).userid );
		}
	}

	private void sendDirFiles(int id, String key, String name){

		ArrayList<SearchResults> al= null;
		if(TableHandler.tableType.equals("mysql")){
			List<Map<String, Object>> l= TableHandler.getFilesFromDir(key);
			al = new ArrayList<SearchResults>();
			for(int i=0;i<l.size();i++){
				al.add(new SearchResults("","",l.get(i).get("FileId").toString(), l.get(i).get("Path").toString().replaceFirst("(.*)"+name+"/", name+"/"), l.get(i).get("Hash").toString(), l.get(i).get("FileSize").toString(), l.get(i).get("Type").toString(), 0));
			}
		}
		else{
			al = LuceneHandler.getDirValues(TableHandler.INDEX_DIRECTORY, TableHandler.columns[0], "pathstring", key, name);
		}
		BaseController.getInstance().sendResponse(
				new GetDirQuery(id, "results", name, al), query.getModule(), "GetDirQuery", false, "", output);
		return;
	}
}
