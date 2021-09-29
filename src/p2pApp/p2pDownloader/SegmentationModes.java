package p2pApp.p2pDownloader;

public class SegmentationModes {

	public String name= "";
	public long size= 0;
	
	public SegmentationModes(String str){
		name= str;
		initValues();
	}
	
	public SegmentationModes(long totalSize){
		setupValues(totalSize);
	}
	
	private void initValues(){
		switch(name){
		case "very-short":
			size= 8;
			break;
		case "short":
			size= 24*1000*1000;
			break;
		case "medium":
			size= 80*1000*1000;
			break;
		case "long":
			size= 160*1000*1000;
			break;
		case "single":
			size= Long.MAX_VALUE;
			break;
		}
	}
	
	private void setupValues(long totalSize){
		
		if(totalSize < 40){
			name= "very-short";
		}
		else if(totalSize < 500*1000*1000){
			name= "short";
		}
		else if(totalSize < 4000*1000*1000){
			name= "medium";
		}
		else {
			name= "long";
		}
		initValues();
	}
	
	public String getName(){
		return name;
	}
	
	public long getSize(){
		return size;
	}
	
	public void setSingleMode(){
		name= "single";
	}
}
