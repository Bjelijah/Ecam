package com.howell.updateCameraUtil;

public class UpdateCameraUtils {
	public static boolean needToUpdate(String curVer,String newVer){
		String[] s = curVer.split("\\.");
		String[] s2 = newVer.split("\\.");
		int firstCurVerNum = Integer.valueOf(s[0]);
		System.out.println(firstCurVerNum);
		int secondCurVerNum = Integer.valueOf(s[1]);
		System.out.println(secondCurVerNum);
		int thirdCurVerNum = Integer.valueOf(s[2]);
		System.out.println(thirdCurVerNum);
		
		int firstNewVerNum = Integer.valueOf(s2[0]);
		System.out.println(firstNewVerNum);
		int secondNewVerNum = Integer.valueOf(s2[1]);
		System.out.println(secondNewVerNum);
		int thirdNewVerNum = Integer.valueOf(s2[2]);
		System.out.println(thirdNewVerNum);
		
		if(firstCurVerNum > firstNewVerNum){
			return false;
		}else if(firstCurVerNum < firstNewVerNum){
			return true;
		}else{
			if(secondCurVerNum  > secondNewVerNum){
				return false;
			}else if(secondCurVerNum < secondNewVerNum){
				return true;
			}else{
				if(thirdCurVerNum > thirdNewVerNum){
					return false;
				}else if(thirdCurVerNum < thirdNewVerNum){
					return true;
				}else{
					return false;
				}
			}
		}
		
		
	}
}
