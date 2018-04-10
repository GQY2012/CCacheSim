package Cache;

public class CacheBlock {
	public int blockAddress;
//	public int blockindex;//索引
	public long tag;//标记
	public boolean validbit;//有效位
	public boolean dirtybit;//脏位
	public int count;//LRU访问次数
	public int time;//块入cache事件
	
	
	public CacheBlock(int tag) {  
        this.tag = tag;  
        dirtybit = false;  
        count = 0;  
        time = -1;  
    }  
	
	public void setblockAddress(String instructionAddress,int blocksize) {
		this.blockAddress = Integer.parseInt(instructionAddress,16)/blocksize;
	}
	
	
}
