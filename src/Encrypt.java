/**
 * 
 */

/**
 * @author rAsHtElL
 *
 */

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.security.InvalidKeyException;
import java.security.KeyStore.SecretKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.print.DocFlavor.STRING;

import com.sun.javafx.logging.Logger;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;


public class Encrypt extends Application  {
	
	TextArea inputArea;
	TextArea display;
	TextField keyTF;
	ToggleGroup radioButtonToggleGroup;
	RadioButton shiftCipherRB;
	RadioButton transpositionCipherRB;
	RadioButton vigenereCipherRB;
	RadioButton desRB;
	RadioButton aesRB;
	
	Logger logger;
	StringBuilder error;
	
	//For DES
	private static Cipher ecipher;
	private static Cipher dcipher;
	private static SecretKey esKey;

	
	/**
	 * 
	 */
	public Encrypt() {
		logger = new Logger();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);

	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Rashcryptor");
		
/*		shiftCipherExecute();
		
		transpositionCipherExecute();
		
		vigenereCipherExecute();
		
		desExecute();
*/		
		VBox vBox = new VBox();
		
		VBox inputVBox = new VBox();
		Text inputText = new Text("Input your classified message:");
		inputArea = new TextArea();
		inputVBox.setSpacing(5);
		inputVBox.getChildren().addAll(inputText, inputArea);
		
		VBox outputVBox = new VBox();
		Text outputText = new Text("Encrypted message:");
		display = new TextArea();
		display.setEditable(false);
		outputVBox.setSpacing(5);
		outputVBox.getChildren().addAll(outputText, display);
		
		VBox keyVBox = new VBox();
		Text keyText = new Text("Key: ");
		keyTF = new TextField();
		keyVBox.setSpacing(5);
		keyVBox.getChildren().addAll(keyText, keyTF);
		
		VBox radioButtonsVBox = new VBox();
		radioButtonToggleGroup = new ToggleGroup();
		Text selectRadioButtonTF = new Text("Select the type of encryption to perform");
		shiftCipherRB = new RadioButton("Shift Cipher");
		shiftCipherRB.setToggleGroup(radioButtonToggleGroup);
		shiftCipherRB.setSelected(true);
		transpositionCipherRB = new RadioButton("Transposition Cipher");
		transpositionCipherRB.setToggleGroup(radioButtonToggleGroup);
		vigenereCipherRB = new RadioButton("Vigenere Cipher");
		vigenereCipherRB.setToggleGroup(radioButtonToggleGroup);
		desRB = new RadioButton("DES");
		desRB.setToggleGroup(radioButtonToggleGroup);
		aesRB = new RadioButton("AES");
		aesRB.setToggleGroup(radioButtonToggleGroup);
		
		HBox encryptionTypeHB = new HBox(10);
		encryptionTypeHB.getChildren().addAll(shiftCipherRB, vigenereCipherRB, transpositionCipherRB, desRB, aesRB);
		radioButtonsVBox.setSpacing(5);
		radioButtonsVBox.getChildren().addAll(selectRadioButtonTF, encryptionTypeHB);
		
		HBox buttonsBox = new HBox();
		Button encryptButton = new Button("Encrypt");
//		encryptButton.setBackground(new Background(new BackgroundFill(Paint.valueOf("Green"), new CornerRadii(5), new Insets(0))));
		encryptButton.setTextFill(Paint.valueOf("Green"));
		
		encryptButton.setOnMouseClicked(x -> {			
			onClickHandler("encrypt");
		});
					
		Button decryptButton = new Button("Decrypt");
		decryptButton.setTextFill(Paint.valueOf("Red"));
		decryptButton.setOnMouseClicked(x->{
			onClickHandler("decrypt");	
		});
		buttonsBox.setSpacing(5);
		buttonsBox.getChildren().addAll(encryptButton, decryptButton);
		
		vBox.getChildren().addAll(inputVBox, outputVBox, keyVBox, radioButtonsVBox, buttonsBox);
		Insets rootInsets = new Insets(10, 10, 10, 10);
		vBox.setPadding(rootInsets);
		vBox.setSpacing(10);
		
		Scene scene = new Scene(vBox, 600,600);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}

	private void onClickHandler(String mode){
		String message = inputArea.getText();
		String securedMessage = null;
		
		try {
			
			if (shiftCipherRB.isSelected() && !message.isEmpty()) {
				int key = Integer.parseInt(keyTF.getText());
				securedMessage = shiftCipher(message, key, mode);
			}
			
			if (transpositionCipherRB.isSelected() && !message.isEmpty()) {
				int key = Integer.parseInt(keyTF.getText());
				securedMessage = transpositionCipher(message, key, mode);
			}
			
			if (vigenereCipherRB.isSelected() && !message.isEmpty()) {
				String vigKey = keyTF.getText();
				securedMessage = vigenereCipher(message, vigKey, mode);
			}				
		} catch (NumberFormatException e) {
			String m = "The key has to be a number";
			logger.addMessage(m +":  " + e.getMessage());
			
			if (keyTF.getText().isEmpty()) {
				keyTF.setText("");
				keyTF.setText(m);
			}
		}
		
		if (desRB.isSelected() && !message.isEmpty()) {
			securedMessage = esExecute(mode, message, securedMessage, "DES");
		}
		
		if (aesRB.isSelected() && !message.isEmpty()) {
			securedMessage = esExecute(mode, message, securedMessage, "AES");
		}
		display.setText(securedMessage);
	}
	
	private void shiftCipherExecute() {
		String cipherText = "rashtell is a fucker";
		int key = 5;
		
		System.out.println(cipherText);
		String encrypted = shiftCipher(cipherText , key, "encrypt");
		System.out.println("Shift Cipher Encrypting .....");
		System.out.println(encrypted);
		String decrypted = shiftCipher(encrypted,key, "decrypt" );
		System.out.println("Shift Cipher Decrypting .....");
		System.out.println(decrypted);
		System.out.println();
	}
	
	private String shiftCipher(String cipher, int key, String mode) {
		
		char[] cipherArray = cipher.toCharArray();
		
		for (int i = 0; i < cipherArray.length; i++) {
			char c = cipherArray[i];
			
			if (mode.equalsIgnoreCase("encrypt")) {
				cipherArray[i] = (char) (c + key);
			}else if (mode.equalsIgnoreCase("decrypt")) {
				cipherArray[i] = (char) (c-key);
			}else {
				return "Mode is either encrypt or decrypt";
			}
		}
		
		cipher = "";
		for (int i = 0; i < cipherArray.length; i++) {
			char c = cipherArray[i];
			cipher += c;
		}
		
		return cipher;
	}

	private void transpositionCipherExecute() {
		String info = "This is your mission should you choose to accept";
		String encrypted = transpositionCipher(info, 8, "encrypt");
		String decrypted = transpositionCipher(encrypted, 8, "decrypt");
		System.out.println(info);
		System.out.println("Transposition Cipher Encrypting .....");
		System.out.println(encrypted);
		System.out.println("Transposition Cipher Decrypting .....");
		System.out.println(decrypted);
		System.out.println();
		
	}
	
	private String transpositionCipher(String cipher, int key, String mode) {
		char[] cipherArray = cipher.toCharArray();
		int cipherArrayLen = cipherArray.length;
		
		int size = (int) Math.ceil(cipherArrayLen/(double)key);
		
		if (mode.equals("decrypt")) {
			int temp = size;
			size = key;
			key = temp;
		}
		
		char[][] ciphe = new char[(int) size][key];
		int k=0;
		for (int i = 0; i < size; i++) {
			
			for (int j = 0; j < key; j++) {
				
				if (k >= cipherArrayLen) {
					break;
				}
				ciphe[i][j] = cipherArray[k++];
			}
		}
		
		 char[][] ciphered = new char[key][size];
		 for (int i = 0; i < key; i++) {
			
			 for (int j = 0; j < size; j++) {
				
				 ciphered[i][j] = ciphe[j][i];
			}
		}
		
		cipher = "";
		for (int i = 0; i < key; i++) {
			for (int j = 0; j <size ; j++) {
				char c = ciphered[i][j];
				cipher += c;
			}
//			cipher += "\n";

		}
		if (mode.equals("encrypt") || mode.equals("decrypt")) {
			return cipher;
		}else
			return "Mode can either be encrypt or decrypt";
		
	}

	private void vigenereCipherExecute() {
		String info = "vigenere";
		String key = "password";
		System.out.println(info);
		System.out.println("Vigenere Encrypting ........");
		String encrypted = vigenereCipher(info, key, "encrypt");
		System.out.println(encrypted);
		System.out.println("Vigenere Decrypting ........");
		String decrypted = vigenereCipher(encrypted, key, "decrypt");
		System.out.println(decrypted);
		System.out.println();
	}
	
	private String vigenereCipher(String cipher, String key, String mode) {
		
		int keyLen = key.length();
		int cipherLen = cipher.length();
		char[] ciphe = cipher.toCharArray();
		char[] keyy = key.toCharArray() ;
		char[] keyyy =  new char[cipherLen];
		
		if (keyLen >= 0) {
			int k = keyLen;
			for (int i = 0; i < cipherLen; i++) {
				k %= keyLen; 
				keyyy[i] = keyy[k++];
			
			}
		}
		
		if (mode.equals("encrypt")) {
			for (int i = 0; i < ciphe.length; i++) {
				ciphe[i] = (char) ((ciphe[i] + keyyy[i]));				
			}
		}
		else if (mode.equals("decrypt")) {
			for (int i = 0; i < ciphe.length; i++) {
				ciphe[i] = (char) (ciphe[i] - keyyy[i]);
			}
			
			
		}
		
		cipher = "";
		for (int i = 0; i < ciphe.length; i++) {
			char c = ciphe[i];
			cipher += c;
		}
		return cipher;
	}
	
	private void desExecute() {
		try {
			//generate secret key using DES algorithm
			esKey = KeyGenerator.getInstance("DES").generateKey();
			
			ecipher = Cipher.getInstance("DES");
			dcipher = Cipher.getInstance("DES");
			
			//Initialize the ciphers with the given key
			ecipher.init(Cipher.ENCRYPT_MODE, esKey);
			dcipher.init(Cipher.DECRYPT_MODE, esKey);
			
			String info = "This is a classified message !";
			
			String encrypted = desEncrypt(info);
			String decrypted = desDecrypt(encrypted);
			
			System.out.println(info);
			System.out.println("DES encrypting");
			System.out.println(encrypted);
			System.out.println("DES decrypting");
			System.out.println(decrypted);
						
		} catch (NoSuchAlgorithmException e) {
			System.out.println("No Such Algorithm: "+e.getMessage());
		}catch (NoSuchPaddingException e) {
			System.out.println("No Such Padding: "+e.getMessage());
		}catch (InvalidKeyException e) {
			System.out.println("Invalid Key: "+e.getMessage());
			return;
		}finally {
			System.out.println();
		}
	}

	private static String desEncrypt(String info) {
		try {
			// encode the string into a sequence of bytes using the named charset
			
			// storing the result into a new byte array
			
			byte[] utf8 = info.getBytes("UTF8");
			byte[] enc = ecipher.doFinal(utf8);
			
			// encode to base64
			enc = BASE64EncoderStream.encode(enc);
			
			
			return new String(enc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private static String desDecrypt(String encrypted) {
		try {
			// decode with base64 to get bytes
			byte[] dec = BASE64DecoderStream.decode(encrypted.getBytes());
			
			byte[] utf8 = dcipher.doFinal(dec);
			
			// create new string based on the specified charset
			return new String(utf8, "UTF8");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}


	private String esExecute(String mode, String message, String securedMessage, String algorithm) {
		try {
			//generate secret key using DES algorithm
			if (mode.equals("encrypt")) {
				esKey = KeyGenerator.getInstance(algorithm).generateKey();
				String encodedKey = Base64.getEncoder().encodeToString(esKey.getEncoded());
				keyTF.setText(encodedKey);
				
				ecipher = Cipher.getInstance(algorithm);
				//Initialize the ciphers with the given key
				ecipher.init(Cipher.ENCRYPT_MODE, esKey);					
				securedMessage = desEncrypt(message);
			}
			
			if (mode.equalsIgnoreCase("decrypt")) {
				byte[] decodedKey = Base64.getDecoder().decode(keyTF.getText());
				esKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
				
				dcipher = Cipher.getInstance(algorithm);
				//Initialize the ciphers with the given key
				dcipher.init(Cipher.DECRYPT_MODE, esKey);					
				securedMessage = desDecrypt(message);
			}
			
		} catch (NoSuchAlgorithmException e) {
			logger.addMessage("No Such Algorithm: "+e.getMessage());
		}catch (NoSuchPaddingException e) {
			logger.addMessage("No Such Padding: "+e.getMessage());
		}catch (InvalidKeyException e) {
			logger.addMessage("Invalid Key: "+e.getMessage());
			return null;
		}
		return securedMessage;
	}

	

}
