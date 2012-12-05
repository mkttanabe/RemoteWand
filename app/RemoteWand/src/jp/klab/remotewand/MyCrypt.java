/*
* Copyright (C) 2012 KLab Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package jp.klab.remotewand;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MyCrypt {
	private final static String TAG = "RemoteWand";
	public static String Crypt(String str, String password) {
		String strCrypt = null;
		String passStr = password;
		int len = passStr.length();
		if (len < 16) {
			for (int i = 0; i + len < 16; i++) {
				int num = i;
				if (num >= 10) {
					num -= 10;
				}
				passStr = passStr + num;
			}
		} else if (len > 16) {
			passStr = passStr.substring(0, 16);
		}
        StringBuffer sb = new StringBuffer(passStr);
        String ivStr = sb.reverse().toString();
        byte[] key = passStr.getBytes();
        byte[] iv  = ivStr.getBytes();
		
        SecretKey sKey = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParam = new IvParameterSpec(iv);
        Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			_Log.e(TAG, "Crypt: " + e);
			return null;
		} catch (NoSuchPaddingException e) {
			_Log.e(TAG, "Crypt: " + e);
			return null;
		}
        try {
			cipher.init(Cipher.ENCRYPT_MODE, sKey, ivParam);
		} catch (InvalidKeyException e) {
			_Log.e(TAG, "Crypt: " + e);
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			_Log.e(TAG, "Crypt: " + e);
			return null;
		}        
        byte[] dataCrypt = null;
		try {
			dataCrypt = cipher.doFinal(str.getBytes());
		} catch (IllegalBlockSizeException e) {
			_Log.e(TAG, "Crypt: " + e);
			return null;
		} catch (BadPaddingException e) {
			_Log.e(TAG, "Crypt: " + e);
			return null;
		}
		strCrypt = toHexString(dataCrypt);
        _Log.d(TAG, "ENCRYPTED : " + strCrypt);

/* test	
        try {
			cipher.init(Cipher.DECRYPT_MODE, cipherKey, ivSpec);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
        byte[] output = null;
		try {
			output = cipher.doFinal(cipherText);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}    
		//_Log.d(TAG, "DECRYPTED : " + new String(output));        
*/                
		return strCrypt;
	}

    private static String toHexString(byte data[]) {
        StringBuffer buf = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            int val = data[i] & 0xff;
            if (val < 0x10) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(val));
        }
        return buf.toString();
    }

}
