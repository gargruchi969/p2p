package p2pApp.p2pQueries;

import java.util.ArrayList;

import p2pApp.SearchResults;

public class SearchQuery {

	public int searchId;
	public String data;
	public ArrayList<SearchResults> results;
	public String mode;
	public String type;
	
	public SearchQuery(int searchId, String mode, String data, ArrayList<SearchResults> results){
		this(searchId, mode, data, results, "keyword");
	}
	
	public SearchQuery(int searchId, String mode, String data, ArrayList<SearchResults> results, String type){
		this.searchId= searchId;
		this.mode= mode;
		this.data= data;
		this.results = results;
		this.type= type;
	}
}
