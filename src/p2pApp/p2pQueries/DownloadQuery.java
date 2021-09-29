package p2pApp.p2pQueries;


public class DownloadQuery {

	public int downloadQueryId;
	public String mode;
	public String key;
	public String segMode;
	public int part;
	
	public DownloadQuery(String mode, String key){
		this.mode= mode;
		this.key= key;
		this.segMode= "single";
		part= 0;
	}
	
	public DownloadQuery(String mode, String key, String segMode, int part){
		this.mode= mode;
		this.key= key;
		this.segMode= segMode;
		this.part= part;
	}
}
