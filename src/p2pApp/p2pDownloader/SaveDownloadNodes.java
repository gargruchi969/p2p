package p2pApp.p2pDownloader;

import java.util.ArrayList;
import java.util.HashMap;

import p2pApp.AlternateIps;
import p2pApp.SearchResults;

public class SaveDownloadNodes {

	SearchResults searchResults;
	String status;
	String segMode;
	int totalParts= 0;
	short partsDone[];
	int partsCompleted=0;
	HashMap<String, Integer> activeIps;
	ArrayList<AlternateIps> ips;
	int activeSeeds=1;
	public boolean isComplete= false;
	public boolean isPaused= false;
	
	public SaveDownloadNodes(DownloadNodes node){
		searchResults= node.getSearchResults();
		status= node.status;
		segMode= node.segMode;
		totalParts= node.totalParts;
		partsDone= node.partsDone;
		partsCompleted= node.partsCompleted;
		activeIps= node.activeIps;
		ips= node.ips;
		activeSeeds= node.activeSeeds;
	}
}
