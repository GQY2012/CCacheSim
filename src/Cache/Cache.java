package Cache;

public class Cache {
	public CacheBlock[][] Cache;
	
	public int cacheSize;//cache大小
	public int blockSize;//块大小
	public int blockNum;//块数量
	public int blockOffset;//块寻址占地址位数
	public int blockNumInAGroup;//组内块数量
	public int groupNum;//组数量
	public int groupOffset;//组寻址占地址位数
	
	public Cache(int csize,int bsize, int way) {
		cacheSize = csize;
		blockSize = bsize;
		
	    blockNum = cacheSize/blockSize;
		blockOffset = (int) (Math.log(blockSize)/Math.log(2.0));
		
		blockNumInAGroup = way;  
        groupNum = blockNum / blockNumInAGroup;  
        groupOffset = (int) (Math.log(groupNum)/Math.log(2.0));
        
        Cache = new CacheBlock[groupNum][blockNumInAGroup];
	}
	
}
