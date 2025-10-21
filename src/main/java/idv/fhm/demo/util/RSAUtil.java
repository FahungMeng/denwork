package idv.fhm.demo.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import java.util.Base64;

public class RSAUtil {

	private PublicKey publicKey;
	private PrivateKey privateKey;

	// 建構子：產生金鑰對
	public RSAUtil() throws NoSuchAlgorithmException {
		generateKeyPair();
	}

	public static PublicKey getPublicKeyFromBase64(String base64PublicKey) throws GeneralSecurityException {
		byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey.getBytes());
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(keySpec);
	}
	/**
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
    public static String readFileAsString(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes, StandardCharsets.UTF_8);
    }
	/**
	 * 將 Base64 編碼的私鑰字串轉換為 PrivateKey 物件
	 *
	 * @param base64PrivateKey Base64 編碼的私鑰字串
	 * @return PrivateKey 物件
	 * @throws GeneralSecurityException 若解析失敗則拋出例外
	 * @throws IOException 
	 */
	public static PrivateKey getPrivateKeyFromBase64(String base64PrivateKey) throws GeneralSecurityException, IOException {
		
		byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey.getBytes());
	    try {
	        // 嘗試直接當作 PKCS#8
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
	        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        return keyFactory.generatePrivate(keySpec);
	    } catch (InvalidKeySpecException e) {
	        // 若失敗，代表可能是 PKCS#1，要轉換成 PKCS#8
	        try {
				return convertPKCS1ToPKCS8(keyBytes);
			} catch (GeneralSecurityException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    }
		return null;
	}
	private static PrivateKey convertPKCS1ToPKCS8(byte[] pkcs1Bytes) throws GeneralSecurityException, IOException {
	    // 手動封裝成 PKCS#8 結構：SEQUENCE { AlgorithmIdentifier, PrivateKey }
	    byte[] pkcs8Header = new byte[]{
	        0x30, (byte)0x82, // SEQUENCE
	        (byte)((pkcs1Bytes.length + 22) >> 8), (byte)((pkcs1Bytes.length + 22) & 0xff),
	        0x02, 0x01, 0x00, // version
	        0x30, 0x0d,       // AlgorithmIdentifier
	        0x06, 0x09, 0x2a, (byte)0x86, 0x48, (byte)0x86, (byte)0xf7, 0x0d, 0x01, 0x01, 0x01, // rsaEncryption OID
	        0x05, 0x00,       // NULL
	        0x04, (byte)0x82, // PrivateKey OCTET STRING
	        (byte)(pkcs1Bytes.length >> 8), (byte)(pkcs1Bytes.length & 0xff)
	    };

	    byte[] pkcs8bytes = new byte[pkcs8Header.length + pkcs1Bytes.length];
	    System.arraycopy(pkcs8Header, 0, pkcs8bytes, 0, pkcs8Header.length);
	    System.arraycopy(pkcs1Bytes, 0, pkcs8bytes, pkcs8Header.length, pkcs1Bytes.length);

	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8bytes);
	    return keyFactory.generatePrivate(keySpec);
	}

	// 產生公私鑰
	private void generateKeyPair() throws NoSuchAlgorithmException {
		File pk=new File("publickey.key");
		File prik=new File("privatekey.key");
		if (pk.exists() && prik.exists()) {
			try {
				this.publicKey=getPublicKeyFromBase64(readFileAsString(pk.getAbsolutePath()));
				this.privateKey=getPrivateKeyFromBase64(readFileAsString(prik.getAbsolutePath()));
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048); // 2048 bits 為安全等級建議值
			KeyPair keyPair = keyGen.generateKeyPair();
			this.publicKey = keyPair.getPublic();
			this.privateKey = keyPair.getPrivate();
			try (PrintWriter publicpw = new PrintWriter("publickey.key");
					PrintWriter privpw = new PrintWriter("privatekey.key");) {
				publicpw.print(getPublicKeyString());
				privpw.print(getPrivateKeyString());
				publicpw.flush();
				privpw.flush();
			} catch (FileNotFoundException e) {
				System.err.println("Generator key error cause:" + e.getMessage());

			}
		}
	}

	// 使用公鑰加密
	public String encode(String plainText) throws Exception {
		Cipher encryptCipher = Cipher.getInstance("RSA");
		encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

		byte[] encryptedBytes = encryptCipher.doFinal(plainText.getBytes("UTF-8"));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	// 使用私鑰解密
	public String decode(String encryptedText) throws Exception {
		byte[] bytes = Base64.getDecoder().decode(encryptedText);

		Cipher decryptCipher = Cipher.getInstance("RSA");
		decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

		byte[] decryptedBytes = decryptCipher.doFinal(bytes);
		return new String(decryptedBytes, "UTF-8");
	}

	// 取得 Base64 格式的公鑰與私鑰字串
	public String getPublicKeyString() {
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}

	public String getPrivateKeyString() {
		return Base64.getEncoder().encodeToString(privateKey.getEncoded());
	}

}