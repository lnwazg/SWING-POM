package com.lnwazg.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncrypUtils
{
    private static final String ALGORITHM = "AES";
    
    private static final byte[] ENCRYP_KEY = "2BAE6F6A001F1936".getBytes();
    
    private EncrypUtils()
    {
    
    }
    
    /**
     * 三重加密字符串
     * @param source
     * @return
     */
    public static String encrypt(String source)
    {
        try
        {
            SecretKey deskey = new SecretKeySpec(ENCRYP_KEY, ALGORITHM);
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            return parseByte2HexStr(c1.doFinal(source.getBytes()));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    /**
     * 三重解密字符串
     * @param target
     * @return
     */
    public static String decrypt(String target)
    {
        try
        {
            SecretKey deskey = new SecretKeySpec(ENCRYP_KEY, ALGORITHM);
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return new String(c1.doFinal(parseHexStr2Byte(target)));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    /**
     * 将二进制转换成16进制
     * 
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[])
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++)
        {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
    
    /**
     * 将16进制转换为二进制
     * 
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr)
    {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++)
        {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte)(high * 16 + low);
        }
        return result;
    }
    
    public static void main(String[] args)
        throws Exception
    {
        String szSrc = "中";
        System.out.println("之前:" + szSrc);
        String a = encrypt(szSrc);
        System.out.println("加密:" + a);
        System.out.printf("解密：" + decrypt(a));
        /*System.out.println(a.length());
        String b = decrypt("123456789012");
        System.out.println("解密:" + b);
        for (int i = 0; i < 100; i++) {
            System.out.println(encrypt(String.valueOf(i)));
        }*/
    }
}
