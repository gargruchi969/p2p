package utility;

public class PercentKeeper {

	int percent=0;
	long totalSize= 0;
	long sizeDone = 0;
	long time= 0;
	double speed= 0;
	long tempSize= 0;
	int initialDone= 0;
	public PercentKeeper(long totalSize){
		this.totalSize= totalSize;
		time= System.currentTimeMillis();
	}
	
	public PercentKeeper(long totalSize, int percent){
		this.totalSize= totalSize;
		time= System.currentTimeMillis();
		this.initialDone= percent;
	}
	
	public void init(){
		sizeDone=0;
		time= System.currentTimeMillis();
	}
	
	public PercentKeeper(){
		percent= 0;
	}
	
	public void setVal(int val){
		percent= val;
	}
	
	public int getVal(){
		return percent;
	}
	
	public void setDone(long size){
		sizeDone= size;
	}
	
	public long getSizeDone(){
		return sizeDone;
	}
	
	public void addDone(long size){
		try{
		tempSize= tempSize + size;
		sizeDone= sizeDone+ size;
		}
		catch(Exception e){
			System.out.println("Percent Keeper #1 "+ e.getMessage());
		}
	}
	
	public int getPercent(){
		return (percent= (int)(sizeDone*100/totalSize)+ initialDone);
	}
	
	public double getSpeed(){
		speed= tempSize / (System.currentTimeMillis()- time);
		time= System.currentTimeMillis();
		tempSize= 0;
		return speed;
	}
}
