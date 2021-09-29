package tcpUtilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utility.Query_v12;

public class PeersTable{
	
	private static PeersTable peersInstance;
	private List<PeersEntries> peersEntries;
	private Set<String> peersSystemIds;
	private List<PeersEntries> neighbourPeers;
	private String markedIp= null;
	
	private PeersTable(){
		peersEntries= new ArrayList<PeersEntries>();
		peersSystemIds= new HashSet<String>();
		neighbourPeers= new ArrayList<PeersEntries>();
	}
	
	public synchronized void addEntry(String i, String sid, String st, long t){
		if(peersSystemIds.contains(sid)){
			
			if(t<0)
				updateEntry(i, sid, st);
			else
				updateEntry(i, sid, st, t);
			return;
		}
		
		peersSystemIds.add(sid);
		if(t<0)
			t=new Date().getTime();
		peersEntries.add(new PeersEntries(i, sid, st, t));
	}
	
	public void addEntry(String i, String sid, String st){
		addEntry(i, sid, st, -1);
	}
	
	public PeersEntries getBySystemId(String id){
		if(peersSystemIds.contains(id)){
			for(int i=0;i<peersEntries.size();i++){
				if(peersEntries.get(i).systemId.equals(id))
					return peersEntries.get(i);
			}
		}
		return null;
	}
	
	public static PeersTable getInstance(){
		if(peersInstance==null) {
            peersInstance= new PeersTable();
        }
		return peersInstance;
	}
	
	public List<PeersEntries> getConnected(){
		return getConnected(peersEntries.size());
	}
	
	public List<PeersEntries> getConnected(int size){
		List<PeersEntries> tempList=new ArrayList<PeersEntries>();
		for(int i = 0; i < size; i++){
			if(peersEntries.get(i).status.equals("connected"))
				tempList.add(peersEntries.get(i));
		}
		return tempList;
	}
	
	public List<PeersEntries> getAll(){
		return peersEntries;
	}
	
	public List<PeersEntries> getPongList(){
		List<PeersEntries> tempList=new ArrayList<PeersEntries>();
		for(int i=0; i<tempList.size();i++){
			if(peersEntries.get(i).status.equals("connected") && !isNeighbourPresent(peersEntries.get(i).systemId))
				tempList.add(peersEntries.get(i));
		}
		tempList.addAll(neighbourPeers);
		return tempList;
	}
	
	public void updateEntry(String ip, String sid, String st, long t){
		for(int i=0; i<peersEntries.size();i++){
			if(peersEntries.get(i).systemId.equals(sid)){
				if(peersEntries.get(i).time < t){
					peersEntries.get(i).time = t;
					peersEntries.get(i).ip = ip;
					peersEntries.get(i).status = st;
				}
			}
		}
	}
	
	public void updateEntry(String ip, String sid, String st){
		for(int i=0; i<peersEntries.size();i++){
			if(peersEntries.get(i).systemId.equals(sid)){
					peersEntries.get(i).ip= ip;
					peersEntries.get(i).status= st;
			}
		}
	}
	
	public void updateStatus(String sid, String st){
		for(int i=0; i<peersEntries.size();i++){
			if(peersEntries.get(i).systemId.equals(sid)){
					peersEntries.get(i).status= st;
			}
		}
	}
	
	public void updateStatusByIp(String ip, String st){
		for(int i=0; i<peersEntries.size();i++){
			if(peersEntries.get(i).ip.equals(ip)){
					peersEntries.get(i).status= st;
			}
		}
	}
	
	public void echoEntries(){
		for(int i=0;i<peersEntries.size(); i++){
			PeersEntries pe= peersEntries.get(i);
			System.out.println(pe.ip+" "+pe.systemId+" "+pe.status);
		}
	}
	
	public void echoNeighbours(){
		for(int i=0; i<neighbourPeers.size(); i++){
			PeersEntries pe= neighbourPeers.get(i);
			System.out.println(pe.ip+" "+pe.systemId+" "+pe.status);
		}
	}
	
	public void echoEntries(String str, Object obj){
		System.out.println("hello from Peers Table: "+((Query_v12)obj).getSourceIp());
		echoEntries();
	}
	
	public List<PeersEntries> getNeighbourPeers(){
		return neighbourPeers;
	}
	
	public List<String> getNeighbourIps(){
		List<String> ips= new ArrayList<String>();
		for(int i=0;i<neighbourPeers.size();i++){
			ips.add(neighbourPeers.get(i).ip+" :: "+ neighbourPeers.get(i).systemId);
		}
		return ips;
	}
	
	public void addNeighbourPeers(String ip, String sid, String st, boolean force){
		addNeighbourPeers(ip, sid, st, new Date().getTime(), force);
	}
	
	public void addNeighbourPeers(String i, String sid, String st, long t, boolean force){
		
		if(isNeighbourPresent(sid))
			return;
		
		if(!utility.Utilities.selfNeighbour){
			if(utility.Utilities.getIpAddress().equals(i) || utility.Utilities.getSystemId().equals(sid))
				return;
		}
		
		if(neighbourPeers.size()<= utility.Utilities.neighbourPeersCount)
			neighbourPeers.add(new PeersEntries(i, sid, st, t));
		else{
			if(force){
				if(!removeMarked())
					neighbourPeers.remove(0);
				neighbourPeers.add(new PeersEntries(i, sid, st, t));
			}
		}
		
		CallbackRegister.getInstance().notifyCallbacks("tcp-server-neighbours", null);
	}
	
	public void remNeighbourPeerByIp(String ip){
		for(int i=0;i<neighbourPeers.size();i++){
			if(neighbourPeers.get(i).ip.equals(ip))
				neighbourPeers.remove(i);
		}
		CallbackRegister.getInstance().notifyCallbacks("tcp-server-neighbours", null);
	}
	
	private synchronized boolean removeMarked(){
		if(markedIp!=null){
			for(int i=0;i<neighbourPeers.size();i++){
				if(neighbourPeers.get(i).ip.equals(markedIp)){
					neighbourPeers.remove(i);
					markedIp= null;
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isNeighbourPresent(String sid){
		for(int i=0;i<neighbourPeers.size();i++){
			if(neighbourPeers.get(i).systemId.equals(sid))
				return true;
		}
		return false;
	}
	
	public boolean isNeighbourPresentByIp(String ip){
		for(int i=0;i<neighbourPeers.size();i++){
			if(neighbourPeers.get(i).ip.equals(ip))
				return true;
		}
		return false;
	}
	
	public void updateNeighbourPeer(String ip, String sid, String st, boolean force){
		for(int i=0;i<neighbourPeers.size();i++){
			if(neighbourPeers.get(i).ip.equals(ip) || neighbourPeers.get(i).systemId.equals(sid)){
				neighbourPeers.remove(i);
			}
		}
		if(!isNeighbourPresent(sid))
			addNeighbourPeers(ip, sid, st, force);
		else
			CallbackRegister.getInstance().notifyCallbacks("tcp-server-neighbours", null);
	}
	
	public void addEntryAndNeighbour(String ip, String sid, boolean force){
		addEntry(ip, sid, "connected");
		addNeighbourPeers(ip, sid, "connected", true);
	}
	
	public void markForRemoval(String ip){
		markedIp = ip;
	}
}

