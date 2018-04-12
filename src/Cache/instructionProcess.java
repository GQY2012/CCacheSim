package Cache;

public class instructionProcess {
	static int gettag(String instructionAddress,int groupOffset,int blockOffset) {
		int address = Integer.parseInt(instructionAddress,16);
		return address >>> (blockOffset + groupOffset);
	}
	
	static int getindex(String instructionAddress,int groupOffset,int blockOffset) {
		int address = Integer.parseInt(instructionAddress,16);
		int taglen = 32-blockOffset-groupOffset;
		return address << taglen >>> taglen >>> (blockOffset);
	}
	
	public static void main(String args[]) {
		System.out.println(getindex("00006994",2,4));
		System.out.println(gettag("00006994",2,4));
	}
}
