package Cache;

public class instructionProcess {
	static int getindex(String instructionAddress,int groupNum) {
	//	System.out.println(instructionAddress.substring(instructionAddress.length() - groupOffset));
	//	String address = Integer.toBinaryString(Integer.valueOf(instructionAddress,16)).toString();
	//	System.out.println(address);
	//	System.out.println(Integer.parseInt(address.substring(address.length() - groupOffset),2));
	//	System.out.println(Integer.parseInt(instructionAddress,16)%groupNum);
		return Integer.parseInt(instructionAddress,16)%groupNum;
	}
	
	static int gettag(String instructionAddress,int groupOffset) {
//		System.out.println(instructionAddress.substring(instructionAddress.length() - groupOffset));
		int address = Integer.parseInt(instructionAddress,16);
//		System.out.println(Integer.parseInt(address.substring(0,address.length() - groupOffset),2));
//		System.out.println(address >> groupOffset);
		return address >> groupOffset;
	}
	
	public static void main(String args[]) {
		getindex("0000000c",8);
		gettag("0000000c",3);
	}
}
