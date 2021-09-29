package p2pApp;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchTable {

	ArrayList<SearchResults> searchResults;
	HashMap<String, Integer> existingHashes;
	private static SearchTable searchTable;
	private int searchId;
	private int loadedSize= 0;

	private SearchTable(){
		searchResults= new ArrayList<SearchResults>();
		existingHashes= new HashMap<String, Integer>();
	}

	public static SearchTable getInstance(){
		if(searchTable==null){
			searchTable= new SearchTable();
		}
		return searchTable;
	}

	public void addEntries(ArrayList<SearchResults> sr, String ip, int id){
		// check for active search id and then add the entries

		if(id!=searchId)
			return;

		for(int i=0;i<sr.size();i++){
			SearchResults temp= sr.get(i);
			addNew( new SearchResults(ip, temp.userid, temp.fileid, utility.Utilities.parseInvalidFilenames(temp.filename), temp.hash, temp.filesize, temp.type, temp.stream));
		}

	}

	public int getSize(){
		return loadedSize;
	}
	
	public ArrayList<SearchResults> getSearchTable(){
		return searchResults;
	}

	public int getNewSearchId(){
		return getNewSearchId(true);
	}

	public int getNewSearchId(boolean refresh){
		if(refresh){
			searchResults.clear();
			existingHashes.clear();
			loadedSize= 0;
		}
		searchId=utility.Utilities.getRandomNumber();
		return searchId;
	}

	public SearchResults getFromSearchTable(int index){
		if(index< searchResults.size())
			return searchResults.get(index);
		return null;
	}

	synchronized private void addNew(SearchResults sr){

		if(sr.hash.equals("null")){
			loadedSize++;
			searchResults.add(sr);
			return;
		}

		if(existingHashes.containsKey(sr.hash)){
			searchResults.get(existingHashes.get(sr.hash)).addAlternateIps(sr.ip, sr.fileid, sr.filename, sr.filesize, sr.userid);
			return;
		}
		
		searchResults.add(sr);
		existingHashes.put(sr.hash, loadedSize);
		loadedSize++;
		return ;
	}
	
	public SearchResults processResults(SearchResults sr){
		if(existingHashes.containsKey(sr.hash)){
			sr.addAlternateIps(searchResults.get(existingHashes.get(sr.hash)).getAlternateIps());
		}
		return sr;
	}
}
