package qnapdecryptgui;

import java.awt.Font;
import java.awt.Toolkit;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Set;

import javax.crypto.Cipher;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import qnapdecrypt.QNAPFileDecrypterEngine;

public class QnapdecryptGui {

	private static final int AES_KEY_STRENGTH = 256;

	private static final int DPI_DEFAULT = 96;

	private static final String JAVA_7_JCE = "http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html";

	private static final String JAVA_7_VERSION = "1.7";

	private static final String JAVA_8_JCE = "http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html";

	private static final String JAVA_8_VERSION = "1.8";

	private static final String RESIZABLE_OPTION = "r";

	private static final String RESIZE_NAME_FILE = "resizeEnable";

	public static void CreateDialogErrorAndExit(final String error) {
		JOptionPane.showMessageDialog(new JFrame(), error, "Utility error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	public static void main(String[] args) {
		boolean resizable = false;
		if ((args != null && args.length > 0 && args[0].equals(RESIZABLE_OPTION))
				|| (Files.exists(Paths.get("./" + RESIZE_NAME_FILE)))) {
			resizable = true;
		}

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		// Dirty trick to be readable on High dpi screens
		int res = Toolkit.getDefaultToolkit().getScreenResolution();
		if (res > DPI_DEFAULT) {
			if (res < 125) {
				setDefaultSize(14);
			} else if (res < 150) {
				setDefaultSize(16);
			} else if (res < 200) {
				setDefaultSize(18);
			} else if (res < 300) {
				setDefaultSize(20);
			} else {
				setDefaultSize(26);
			}
		}

		// Check property after update 151
		boolean jceApplied = false;
		try {
			// try the property after update 151
			Security.setProperty("crypto.policy", "unlimited");
			if (Cipher.getMaxAllowedKeyLength("AES") >= AES_KEY_STRENGTH) {
				// Update applied so property can be set !
				jceApplied = true;
			}
		} catch (SecurityException exc) {
			// Cannot write permission, do not crash on it it can be normal,
			// try to override JCE file next
		} catch (NoSuchAlgorithmException exc2) {
			CreateDialogErrorAndExit("AES not available in this JRE.");
		}

		// Check JCE
		try {
			if (Cipher.getMaxAllowedKeyLength("AES") < AES_KEY_STRENGTH && !jceApplied) {
				String linkJCE = "Link not found for JCE policy";
				if (System.getProperty("java.version").startsWith(JAVA_7_VERSION)) {
					linkJCE = JAVA_7_JCE;
				} else if (System.getProperty("java.version").startsWith(JAVA_8_VERSION)) {
					linkJCE = JAVA_8_JCE;
				} else {
					CreateDialogErrorAndExit("JAVA version not supported, install JCE policy on JRE 7 / 8.");
				}
				CreateDialogErrorAndExit(
						"Required JCE policy not installed, use this for your version : " + System.lineSeparator()
								+ linkJCE + System.lineSeparator() + "Instructions are provided in the JCE archive.");
			}
		} catch (NoSuchAlgorithmException e) {
			CreateDialogErrorAndExit("AES not available in this JRE.");
		}

		QNAPFileDecrypterEngine engine = new QNAPFileDecrypterEngine(false, false);

		QnapdecryptPresentationModel presentationModel = new QnapdecryptPresentationModel(engine);
		QnapdecryptPanel panel = new QnapdecryptPanel(presentationModel);
		presentationModel.setPanel(panel);

		JFrame qnapChooser = new JFrame("Decipher Hybrid Backup Sync utility");
		qnapChooser.add(panel);
		qnapChooser.setResizable(resizable);
		qnapChooser.pack();
		qnapChooser.setLocationRelativeTo(null);
		qnapChooser.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		qnapChooser.setVisible(true);

	}

	public static void setDefaultSize(int size) {
		Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
		Object[] keys = keySet.toArray(new Object[keySet.size()]);

		for (Object key : keys) {

			if (key != null && key.toString().toLowerCase().contains("font")) {
				Font font = UIManager.getDefaults().getFont(key);
				if (font != null) {
					font = font.deriveFont((float) size);
					UIManager.put(key, font);
				}
			}
		}
	}

}
