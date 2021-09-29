package tcpUtilities;

public class PeersEntries {
	public String ip;
	public String systemId;
	public String status;
	public long time;
	
	PeersEntries(String i, String sid, String st, long t){
		ip=i;
		systemId=sid;
		status=st;
		time=t;
	}
}
