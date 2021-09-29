package p2pApp.p2pDownloader;

import java.io.File;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import p2pApp.AlternateIps;
import p2pApp.SearchResults;
import p2pApp.p2pIndexer.DirectoryReader;
import utility.PercentKeeper;

public class DownloadNodes {

	SearchResults searchResults;
	String status;
	String segMode;
	int totalParts= 0;
	short partsDone[];
	int partsCompleted=0;
	RandomAccessFile fos;
	HashMap<String, Integer> activeIps;
	ArrayList<AlternateIps> ips;
	int activeSeeds=1;
	public boolean isComplete= false;
	PercentKeeper percentKeeper;
	public boolean isPaused= false, isStopped= false;
	int delayCount= 10;
	
	public DownloadNodes(SearchResults sr){
		searchResults = sr;
		calculateSegMode();
		activeIps= new HashMap<String, Integer>();
		ips= sr.getAlternateIps();
//		ips.add(new AlternateIps("192.168.1.233", "34", sr.getFilename(), "3948", "abhi"));
//		ips.add(new AlternateIps("192.168.1.198", "34", sr.getFilename(), "3948", "abhi"));
//		ips.add(new AlternateIps("192.168.1.106", "34", sr.getFilename(), "3948", "abhi"));
//		ips.add(new AlternateIps("192.168.1.211", "34", sr.getFilename(), "3948", "abhi"));
		activeSeeds= activeSeeds + ips.size();
		percentKeeper= new PercentKeeper(Long.parseLong(searchResults.getFileSize()));
	}

	public DownloadNodes(SaveDownloadNodes sn){
		searchResults= sn.searchResults;
		status= sn.status;
		segMode= sn.segMode;
		totalParts= sn.totalParts;
		partsDone= sn.partsDone;
		partsCompleted= sn.partsCompleted;
		activeIps= new HashMap<String, Integer>();
		ips= sn.ips;
		activeSeeds= sn.activeSeeds;
		percentKeeper= new PercentKeeper(Long.parseLong(searchResults.getFileSize()),
				getPercentDone());
		
		for(int i=0; i<totalParts;i++){
			if(partsDone[i]<2)
				partsDone[i]=0;
		}
		
//		try{
//		fos = new FileOutputStream(
//				utility.Utilities.outputFolder+utility.Utilities.parseInvalidFilenames(searchResults.getFilename()),
//				true);
//		}
//		catch(Exception e){	}
	}
	
	public void resumeDownload(){
		if(!isPaused)
			return;
		
		isPaused= false;
		delayCount = 5;
		activeSeeds= 1;
		DownloadEngine.getInstance().resumeDownloadOfFile(this);
	}
	
	public void pauseDownload(){
		if(isPaused)
			return;
		
		isPaused= true;
		if(!isComplete)
		DownloadEngine.getInstance().pauseDownloadOfFile(this);
		activeIps.clear();
	}
	
	public void stopDownload(){
		if(isComplete)
			return;
		
		isPaused= true;
		isStopped= true;
		
		try{
			if(DownloadEngine.getInstance().stopDownloadOfFile(this)){
				if(fos!=null){
					fos.getChannel().close();
					fos.close();
				}
				new File(utility.Utilities.outputFolder+utility.Utilities.parseInvalidFilenames(searchResults.getFilename())).delete();
				new File("data/partials/"+searchResults.getHash()+".txt").delete();
			}
			fos= null;
		}
		catch(Exception e){
			System.out.println("Error while stopping the download: "+e.getMessage());

		}
	}
	
	public SearchResults getSearchResults(){
		return searchResults;
	}
	
	public int getPercent(){
		if(isComplete)
			return 100;
		return percentKeeper.getPercent();
	}
	
	public long getSizeDone(){
		return percentKeeper.getSizeDone();
	}
	
	public double getSpeed(){
		return getSpeed(1);
	}
	
	public double getSpeed(int multiplier){
		double s= percentKeeper.getSpeed();
		return BigDecimal.valueOf(s*multiplier/1024).setScale(1,
				RoundingMode.HALF_UP).doubleValue();
	}
	
	public void setStatus(String status){
		this.status= status;
	}
	
	public void removeIp(String ip, int pt){
		
		if(ip.equals(searchResults.getIp())){
			activeSeeds--;
		}
		else{
			
			for(int i=0;i<ips.size();i++)
				if(ips.get(i).getIp().equals(ip)){
					ips.remove(i);
					activeSeeds--;
					break;
				}
		}
		if(activeIps.containsKey(ip)){
			activeIps.remove(ip);
		}
		partsDone[pt]=0;
		if(activeSeeds > 0){
			DownloadEngine.getInstance().startDownloading();
		}
	}
	
	private void calculateSegMode(){
		long size= Long.parseLong(searchResults.getFileSize());
		SegmentationModes sm= new SegmentationModes(size);
		segMode= sm.getName();
		totalParts= (int)Math.ceil(((double)size/sm.getSize()));
		partsDone= new short[totalParts];
	}

	public void setPartStatus( int part, short s){
		partsDone[part]= s;
	}
	
	public void addPartsDone(String ip, int part){
		
//		if(getPercentDone()>40){
//			pauseDownload();
//			return;
//		}
		
		if(activeIps.containsKey(ip))
			activeIps.remove(ip);
		
		if(partsDone[part]<2){
			partsDone[part]=2;
			partsCompleted++;
		}
		if(partsCompleted>=totalParts && !isComplete){
			try{
			isComplete= true;
//			CallbackRegister.getInstance().notifyCallbacks(
//					"p2p-app-download-file-"+searchResults.getIp()+"-"+searchResults.getFileId(), searchResults);
			
			DirectoryReader.updateTableOnDownload(
					utility.Utilities.outputFolder + 
					utility.Utilities.parseInvalidFilenames(searchResults.getFilename()), searchResults.getHash());
			
			new File("data/partials/"+searchResults.getHash()+".txt").delete();
			fos.getChannel().close();
			fos.close();
			}
			catch(Exception e){
				System.out.println("From download nodes "+e.getMessage());
			}
		}
		//System.out.println("Percent: "+ percentKeeper.getPercent()+ " "+ percentKeeper.getSpeed());
		DownloadEngine.getInstance().startDownloading();
	}

	public int getRemainingPart(){
		for(int i=0;i<totalParts;i++){
			if(partsDone[i]==0)
				return i;
		}
		return -1;
	}

	public boolean downloadFile(){
		try{
			
			int pt= getRemainingPart();
			if(pt<0)
				return true;
			
			if(partsCompleted==0)
				percentKeeper.init();
			
			if(utility.Utilities.singleMode){
				new DownloadRequest(searchResults.getFileId(), searchResults.getIp(),
						searchResults.getFilename(), searchResults.getUserid(),
						this);
				partsDone[0]= 1;
				totalParts= 1;
				return true;
			}
			
			if(isPaused==true)
				return false;
			
			if(delayCount==0){
				pauseDownload();
				return false;
			}
			
			if(fos==null)
				fos = new RandomAccessFile(utility.Utilities.outputFolder+utility.Utilities.parseInvalidFilenames(searchResults.getFilename()),"rw");
			
			if(DownloadThread.DownloadThreadsCount >= utility.Utilities.maxDownloadThreadCount)
				return false;
			
			AlternateIps sip= new AlternateIps(searchResults);
			sip= getNextIp(sip, pt);
			
			if(sip==null)
				return false;
			
			if(activeSeeds<=0){
				delayCount--;
			}
			
			partsDone[pt]= 1;
				new DownloadThread(sip.getIp(), sip.getFileid(), sip.getUserid(), pt, segMode, sip.getFilename(), fos.getChannel(), this,
						(activeSeeds<=0? 1000: 0));
		}
		catch(Exception e){
			System.out.println("Download node #1 "+e.getMessage());
		}
		return false;
	}
	
	private AlternateIps getNextIp(AlternateIps sip, int pt){
		
		String ip = sip.getIp();
		int index= -1;
		
		while(activeIps.containsKey(ip)){
			ip = null;
			sip = null;
			
			if(ips== null)
				return null;
			if(++index < ips.size()){
				ip= ips.get(index).getIp();
			}
		}
		if(ip!=null){
			activeIps.put(ip, pt);
			if(index>-1)
				sip= ips.get(index);
		}
		return sip;
	}
	
	public int getPercentDone(){
		return (partsCompleted* 100 /totalParts);
	}
	
	public int getAvailableIps(){
		return (activeSeeds - activeIps.size());
	}
}
