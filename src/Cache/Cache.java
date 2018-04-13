package Cache;

public class Cache {
	public CacheBlock[][] Cache;//一维坐标：组号index,二维坐标：组内块号blockOffset
	
	public int cacheSize;//cache大小
	public int blockSize;//块大小
	
	public int blockNum;//块数量
	public int groupNum;//组数量
	public int blockNumInAGroup;//组内块数量
	
	public int groupOffset;//组号占地址位数
	public int blockOffset;//块号占地址位数
	
	public long[] inCacheTime;
	/**
	 * 主存地址分段为：
	 * tag groupOffset blockOffset
	 * cache地址分段位：
	 * tag index blockOffset
	 */
	
	public Cache(int csize,int bsize, int way) {
		cacheSize = csize;
		blockSize = bsize;
	    blockNum = cacheSize/blockSize;
		blockOffset = (int) (Math.log(blockSize)/Math.log(2.0));
		
		blockNumInAGroup = way;  
		groupNum = blockNum/blockNumInAGroup;
        groupOffset = (int) (Math.log(groupNum)/Math.log(2.0));
        /*
        System.out.println("cs" + cacheSize);
		System.out.println("bs" + blockSize);
		System.out.println("bN" + blockNum);
        System.out.println("gN" + groupNum);
        System.out.println("bNg" + blockNumInAGroup);
		System.out.println("gF" + groupOffset);
		System.out.println("bF" + blockOffset);
        */
        Cache = new CacheBlock[groupNum][blockNumInAGroup];
        for (int i = 0; i < groupNum; i++) {  
            for (int j = 0; j < blockNumInAGroup; j++) {  
                Cache[i][j] = new CacheBlock(-1,j);  
            }  
        }  
        
        inCacheTime = new long[groupNum];
        
	}
	
	public boolean read(int tag,int index) {
		for(int i = 0;i < blockNumInAGroup;i++) {
			if(Cache[index][i].tag == tag /*&& Cache[index][i].validbit == true*/) {
				int t = Cache[index][i].LRUindex;
				for(int j = 0; j < blockNumInAGroup; j++)
					if(Cache[index][j].LRUindex > t)
						Cache[index][j].LRUindex--;
				Cache[index][i].LRUindex = blockNumInAGroup - 1;
				return true;
			}
		}
		return false;
	}
	
	public boolean write(int tag,int index,int writeType) {
		for(int i = 0;i < blockNumInAGroup;i++) {
			if(Cache[index][i].tag == tag /*&& Cache[index][i].validbit == true*/) {
				Cache[index][i].dirtybit = true;
				int t = Cache[index][i].LRUindex;
				for(int j = 0; j < blockNumInAGroup; j++)
					if(Cache[index][j].LRUindex > t)
						Cache[index][j].LRUindex--;
				Cache[index][i].LRUindex = blockNumInAGroup - 1;
				
				if(writeType == 0) {//写回
					
				}
				else if(writeType == 1) {//写直达
					Cache[index][i].dirtybit = false;
				}
				return true;
			}
				
		}
		return false;
	}
	
	public boolean prefetch(int tag,int index,int replaceType,int writeType) {
		if(index == Math.pow(2,groupOffset) - 1) {
			index = 0;
			if(tag == Math.pow(2,32 - groupOffset - blockOffset) - 1) {
				tag = 0;
			}
			else {
				tag++;
			}
		}
		else {
			index++;
		}
		boolean isHit = read(tag,index);
		if(!isHit)
			replace(tag,index,replaceType,writeType);
		
		return isHit;
	}
	
	public void replace(int tag,int index,int replaceType,int writeType) {
		if(replaceType == 0) {//LRU
			int blockIndex = 0;  
            for (int i = 0; i < blockNumInAGroup; i++)  { 
            	if(Cache[index][i].LRUindex == 0) {
            		blockIndex = i; 
            		break;
            	}
            }
        	Cache[index][blockIndex].LRUindex = blockNumInAGroup;
        	for (int i = 0; i < blockNumInAGroup; i++) { 
        		Cache[index][i].LRUindex--;
        	}
            loadtoCache(tag, index, blockIndex, writeType); 
		}
		else if(replaceType == 1) {//FIFO
			int blockIndex = 0;  
            for (int i = 1; i < blockNumInAGroup; i++) {  
                if (Cache[index][blockIndex].time > Cache[index][i].time) {  
                	blockIndex = i;  
                }  
            }  
            loadtoCache(tag, index, blockIndex,writeType);  
		}
		else if(replaceType == 2){//Random
			int randBlockIndex = (int) (Math.random()*blockNumInAGroup);
			loadtoCache(tag,index,randBlockIndex,writeType);
		}
	}
	
	public void loadtoCache(int tag,int index,int blockindex,int writeType) {
		if (writeType == 0 && Cache[index][blockindex].dirtybit) { //写回法替换 

		} 
		
		Cache[index][blockindex].tag = tag;
		Cache[index][blockindex].validbit = true;
		Cache[index][blockindex].dirtybit = false;
		Cache[index][blockindex].time = inCacheTime[index];  
		inCacheTime[index]++;
	}
	
	public static void main(String args[]) {
		Cache cache = new Cache(2048,16,4);
	}
	
}
