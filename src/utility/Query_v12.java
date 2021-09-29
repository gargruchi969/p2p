package utility;

public class Query_v12 {
	
	private int queryId;
	private String module;
	private String code;
	private String payload;
	private String sourceSid;
	private String destSid;
	private String sourceIp;
	private String sourcePort;
	private String destPort;
	private boolean response;
	private String responseType;
	private int hopCount;
	
	Query_v12(String m, String p){
		this(utility.Utilities.getRandomNumber(), m, p, null, null, null, null, null, null, false, null);
	}
	
	Query_v12(int id, String m, String p, String ss, String ds, String si, String c, String sp, String dp, boolean r, String rt){
		this(id, m, p, ss, ds, si, c, sp, dp, r, rt, utility.Utilities.maxHopCount);
	}
	
	Query_v12(int id, String m, String p, String ss, String ds, String si, String c, String sp, String dp, boolean r, String rt, int hc){
		queryId=id;
		module=m;
		payload=p;
		sourceSid=ss;
		destSid=ds;
		sourceIp=si;
		code=c;
		sourcePort=sp;
		destPort= dp;
		response= r;
		responseType = rt;
		setHopCount(hc);
	}
	
	public String getModule(){
		return module;
	}
	
	public String getPayload(){
		return payload;
	}
	
	public String getCode(){
		if(code == null)
			code = "";
		return code;
	}
	
	public String getSourceSid(){
		if(sourceSid == null)
			sourceSid = "";
		return sourceSid;
	}
	
	public String getDestSid(){
		if(destSid == null)
			destSid = "";
		return destSid;
	}
	
	public String getSourceIp(){
		if(sourceIp == null)
			sourceIp = "";
		return sourceIp;
	}
	
	public int getSourcePort(){
		try {
			return Integer.parseInt(sourcePort);
		} catch (NumberFormatException e) {
			System.out.println("Queries: Unable to parse source port. "+e.getMessage());
		}
		return 0;
	}
	
	public int getDestPort(){
		try {
			return Integer.parseInt(destPort);
		} catch (NumberFormatException e) {
			System.out.println("Queries: Unable to parse destination port. "+e.getMessage());
		}
		return 0;
	}
	
	public int getHopCount(){
		return hopCount;
	}
	
	public boolean getResponse(){
		return response;
	}
	
	public int getQueryId(){
		return queryId;
	}
	
	public String getResponseType(){
		if(responseType ==null)
			responseType ="string";
		return responseType;
	}
	
	public void setResponse(boolean x){
		response=x;
	}
	
	public void setResponseType(String t){
		responseType =t;
	}
	
	public void setQueryId(int i){
		queryId=i;
	}
	
	public int setRandomId(){
		return (queryId= utility.Utilities.getRandomNumber());
	}
	
	public void setHopCount(int x){
		if(x>utility.Utilities.maxHopCount)
			x=utility.Utilities.maxHopCount;
		hopCount= x;
	}

	public void decrementHop(){
		decrementHop(false);
	}
	
	public void decrementHop(boolean found){
		if(found)
			hopCount= hopCount / 3;
		else
			hopCount--;
	}
}
