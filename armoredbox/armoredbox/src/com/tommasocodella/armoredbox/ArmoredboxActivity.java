package com.tommasocodella.armoredbox;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ArmoredboxActivity extends Activity {
	private TextView plainText;
	private TextView cipherText;
	private TextView cipherTextVerify;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        plainText = (TextView) findViewById(R.id.plainText);
        cipherText = (TextView) findViewById(R.id.cipherText);
        cipherTextVerify = (TextView) findViewById(R.id.validationBox);
        Button encryptButton = (Button) findViewById(R.id.encryptButton);
        
        encryptButton.setOnClickListener(new EncryptListener());
    }
    
    private class EncryptListener implements OnClickListener{
    	String plaintext = "";
    	KeyPair kp;
    	
    	public byte[] rsaEncrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, kp.getPublic());
			byte[] cipherData = cipher.doFinal(data);
			return cipherData;
    	}
    	
    	public byte[] rsaDecrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, kp.getPrivate());
			byte[] cipherData = cipher.doFinal(data);
			return cipherData;
    	}
    	
    	
    	@Override
		public void onClick(View v) {
    		plaintext = plainText.getText().toString();
			
    		try {
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				kpg.initialize(2048);
				kp = kpg.genKeyPair();
				
				try {
					byte[] encBytes = rsaEncrypt(plaintext.getBytes());
				    byte[] decBytes = rsaDecrypt(encBytes);
				    
					cipherText.setText(new String(encBytes));
					cipherTextVerify.setText(new String(decBytes));
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
    		
    		
		}
    	
    }
}