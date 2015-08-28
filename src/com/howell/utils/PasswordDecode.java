package com.howell.utils;
/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class PasswordDecode {
	public static byte[] decodePassword(byte[] array){
		for(int i = 0 ; i < array.length ; i++){
			array[i] ^= 0x57; 
		}
		return array;
	}
	
	public static byte[] decodeUsername(byte[] array){
		for(int i = 0 ; i < array.length ; i++){
			array[i] ^= 0x48; 
		}
		return array;
	}
}
