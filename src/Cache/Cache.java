package Cache;

public class Cache {
	public CacheBlock[][] Cache;//一维坐标：组号，二维坐标：组内块号
	
	public int cacheSize;//cache大小
	public int blockSize;//块大小
	public int blockNum;//块数量
	public int groupNum;//组数量
	public int blockNumInAGroup;//组内块数量
	public int groupOffset;//组寻址占地址位数
	public int blockOffset;//块寻址占地址位数
	
	public Cache(int csize,int bsize, int way) {
		cacheSize = csize;
		blockSize = bsize;
		
	//	System.out.println("cs" + cacheSize);
	//	System.out.println("bs" + blockSize);
		
	    blockNum = cacheSize/blockSize;
		blockOffset = (int) (Math.log(blockNum)/Math.log(2.0));
		
	//	System.out.println("bN" + blockNum);
	//	System.out.println("bF" + blockOffset);
		
		if(way == 0) {//直接映射
			groupNum = blockNum;
		}
		else
			groupNum = way;//组相联
		blockNumInAGroup = blockNum / groupNum;  
        groupOffset = (int) (Math.log(groupNum)/Math.log(2.0));
        
     // System.out.println("gN" + groupNum);
     // System.out.println("bNg" + blockNumInAGroup);
	//	System.out.println("gF" + groupOffset);
        
        Cache = new CacheBlock[groupNum][blockNumInAGroup];
        for (int i = 0; i < groupNum; i++) {  
            for (int j = 0; j < blockNumInAGroup; j++) {  
                Cache[i][j] = new CacheBlock(-1);  
            }  
        }  
        
	}
	
	public boolean read(int tag,int index) {
		for(int i = 0;i < blockNumInAGroup;i++) {
			if(Cache[index][i].tag == tag && Cache[index][i].validbit == true) {
				Cache[index][i].count++;
				return true;
			}
		}
		return false;
	}
	
	public boolean write(int tag,int index) {
		return false;
	}
	
	public void replace(int tag,int index,int replacetype) {
		if(replacetype == 0) {//LRU
			
		}
		else if(replacetype == 1) {//FIFO
			
		}
		else {//Rand
			int randblockNumInAGroup = (int) Math.random()*blockNumInAGroup;
			loadtoCache(tag,index,randblockNumInAGroup);
		}
	}
	
	public void loadtoCache(int tag,int index,int blockindex) {
		Cache[index][blockindex].tag = tag;
		Cache[index][blockindex].validbit = true;
		Cache[index][blockindex].dirtybit = true;
	}
	
	public static void main(String args[]) {
		Cache cache = new Cache(2048,16,2);
		System.out.println((int) Math.random()*cache.blockNumInAGroup);
	}
	
}
