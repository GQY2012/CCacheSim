package Cache;

public class CacheBlock {
	public long tag;//标记
	public boolean validbit;//有效位
	public boolean dirtybit;//脏位
	public int LRUindex;//LRU需要的访问时间排位
	public long time;//FIFO需要的块入cache时间
	
	public CacheBlock(int tag,int LRUindex) {  
        this.tag = tag;  
        dirtybit = false;  
        time = -1;
        this.LRUindex = LRUindex;
    }  

	
	
}
