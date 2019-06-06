package qnapdecryptgui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import qnapdecrypt.QNAPFileDecrypterEngine;

public class QnapdecryptPresentationModel {

	private class DecipherSwingWorker extends SwingWorker<Boolean, Void> {
		int index = 0;
		private CountDownLatch countdown;
		private boolean dirMode;
		private String password;
		private boolean recursiveMode;

		private JDialog waitDialog;

		public DecipherSwingWorker(CountDownLatch countdown, JDialog waitDialog, boolean dirMode, boolean recursiveMode,
				String password) {
			this.countdown = countdown;
			this.password = password;
			this.dirMode = dirMode;
			this.recursiveMode = recursiveMode;
			this.waitDialog = waitDialog;
		}

		@Override
		public Boolean doInBackground() {
			if (dirMode) {
				decipherMultipleFiles(srcFile, dstFile);
			} else {
				if (srcFile.canRead() && !srcFile.isDirectory()) {
					File outputFile = dstFile;
					if (dstFile.isDirectory()) {
						if (srcFile.getParentFile().equals(dstFile)) {
							if (srcFile.getName().endsWith(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION)) {
								outputFile = new File(dstFile + File.separator + PLAIN_NAME_PREFIX
										+ srcFile.getName().replaceAll(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION, ""));
							} else {
								outputFile = new File(dstFile + File.separator + PLAIN_NAME_PREFIX + srcFile.getName());
							}
						} else {
							if (srcFile.getName().endsWith(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION)) {
								outputFile = new File(dstFile + File.separator
										+ srcFile.getName().replaceAll(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION, ""));
							} else {
								outputFile = new File(dstFile + File.separator + srcFile.getName());
							}
						}
					} else if (srcFile.equals(dstFile)) {
						if (srcFile.getName().endsWith(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION)) {
							outputFile = new File(dstFile.getParent() + File.separator + PLAIN_NAME_PREFIX
									+ srcFile.getName().replaceAll(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION, ""));
						} else {
							outputFile = new File(
									dstFile.getParent() + File.separator + PLAIN_NAME_PREFIX + srcFile.getName());
						}
					}
					if (cipherEngine.doDecipherFile(srcFile, outputFile, password)) {
						successFiles.add(srcFile);
					} else {
						errorFiles.add(srcFile);
					}
				}
			}
			return true;
		}

		@Override
		public void done() {
			waitDialog.dispose();
			countdown.countDown();
		}

		private void decipherMultipleFiles(File cipherFile, File plainFile) {
			File cipherDir = cipherFile;
			File plainDir = plainFile;
			String[] cipheredListFiles = cipherDir.list();

			for (String eachCipheredFileName : cipheredListFiles) {
				String eachPlainFileName = eachCipheredFileName;
				if (cipherDir.equals(plainDir)) {
					if (eachCipheredFileName.endsWith(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION)) {
						eachPlainFileName = PLAIN_NAME_PREFIX
								+ eachCipheredFileName.replaceAll(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION, "");
					} else {
						eachPlainFileName = PLAIN_NAME_PREFIX + eachCipheredFileName;
					}
				} else if (eachPlainFileName.endsWith(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION)) {
					eachPlainFileName = eachCipheredFileName.replaceAll(QNAPFileDecrypterEngine.QNAP_BZ2_EXTENSION, "");
				}
				File eachCipherFile = new File(cipherDir + File.separator + eachCipheredFileName);
				File eachPlainFile = new File(plainDir + File.separator + eachPlainFileName);

				// Check recursive mode
				if (recursiveMode && eachCipherFile.isDirectory() && eachCipherFile.canRead()) {
					String newPlainDir = plainDir + File.separator + eachPlainFileName;
					String newCipherDir = cipherDir + File.separator + eachCipheredFileName;
					try {
						if (!Files.isDirectory(Paths.get(newPlainDir))) {
							Files.createDirectory(Paths.get(newPlainDir));
						}
						decipherMultipleFiles(new File(newCipherDir), new File(newPlainDir));
					} catch (IOException e) {
						errorFiles.add(eachCipherFile);
					}
				} else {
					if (!eachCipherFile.isDirectory() && eachCipherFile.canRead()) {
						if (cipherEngine.doDecipherFile(eachCipherFile, eachPlainFile, password)) {
							successFiles.add(eachCipherFile);
						} else {
							errorFiles.add(eachCipherFile);
						}
					}
				}

				// Cannot set progress in recursive mode, cannot predict how many files are
				// deciphered
				if (!recursiveMode) {
					int progress = (int) (((float) ++index / cipheredListFiles.length) * 100);
					setProgress(progress);
				}
			}
		}
	}

	private class RequestFocusListener implements AncestorListener {

		@Override
		public void ancestorAdded(final AncestorEvent event) {
			final AncestorListener al = this;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					JComponent component = event.getComponent();
					component.requestFocusInWindow();
					component.removeAncestorListener(al);
				}
			});
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			// Does nothing
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			// Does nothing
		}
	}

	private static final int MAX_PWD_LENGTH = 64;

	private static final String NAME_FILE_REPORT = "HBSUtility_report.txt";

	private static final String PLAIN_NAME_PREFIX = "plain_";

	QNAPFileDecrypterEngine cipherEngine;

	ActionListener decipherActionListener;

	private ActionListener destinationActionListener;

	private File dstFile;

	private List<File> errorFiles = new ArrayList<>();

	private QnapdecryptPanel panel;

	private ItemListener recursiveListener;

	private boolean recursiveMode = false;

	private ActionListener sourceActionListener;

	private File srcFile;

	private List<File> successFiles = new ArrayList<>();

	public QnapdecryptPresentationModel(QNAPFileDecrypterEngine engine) {
		cipherEngine = engine;
	}

	public ActionListener getDecipherActionListener() {
		if (decipherActionListener == null) {
			decipherActionListener = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (srcFile != null && dstFile != null && srcFile.canRead() && dstFile.canRead()) {
						// Check if directory mode selected
						if (srcFile.isDirectory()) {
							if (dstFile.isDirectory()) {
								cipherEngine.setDirMode(true);
								askPasswordAndDecipher(true, recursiveMode);
							} else {
								JOptionPane.showMessageDialog(new JFrame(),
										"Cannot decipher a directory in a single file, use a directory as destination when the source is a directory.",
										"Utility error", JOptionPane.ERROR_MESSAGE);
							}
						} else {
							cipherEngine.setDirMode(false);
							askPasswordAndDecipher(false, false);
						}
					} else {
						JOptionPane.showMessageDialog(new JFrame(), "I/O Error, cannot read source or destination.",
								"Utility error", JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}
		return decipherActionListener;
	}

	public ActionListener getDestinationActionListener() {
		if (destinationActionListener == null) {
			destinationActionListener = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser;
					if (dstFile != null) {
						fileChooser = new JFileChooser(dstFile);
					} else {
						fileChooser = new JFileChooser(".");
					}
					fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					fileChooser.setMultiSelectionEnabled(false);
					int returnVal = fileChooser.showOpenDialog(panel);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						dstFile = fileChooser.getSelectedFile();
						panel.getDestinationTextField().setText(dstFile.getAbsolutePath());
						panel.getDestinationTextField().setToolTipText(dstFile.getAbsolutePath());
						updateDecipherButtonStatus();
					}
				}
			};
		}
		return destinationActionListener;
	}

	public ItemListener getRecursiveModeItemListener() {
		if (recursiveListener == null) {
			recursiveListener = new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					recursiveMode = (e.getStateChange() == ItemEvent.SELECTED);
				}
			};
		}
		return recursiveListener;
	}

	public ActionListener getSourceActionListener() {
		if (sourceActionListener == null) {
			sourceActionListener = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser;
					if (srcFile != null) {
						fileChooser = new JFileChooser(srcFile);
					} else {
						fileChooser = new JFileChooser(".");
					}
					fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					fileChooser.setMultiSelectionEnabled(false);
					int returnVal = fileChooser.showOpenDialog(panel);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						srcFile = fileChooser.getSelectedFile();
						panel.getSourceTextField().setText(srcFile.getAbsolutePath());
						panel.getSourceTextField().setToolTipText(srcFile.getAbsolutePath());
						updateDecipherButtonStatus();
					}
				}
			};
		}
		return sourceActionListener;
	}

	public void setPanel(QnapdecryptPanel panel) {
		this.panel = panel;

	}

	protected void updateDecipherButtonStatus() {
		if (dstFile != null && srcFile != null) {
			panel.getDecipherButton().setEnabled(true);
		}
	}

	/**
	 * Ask user password and decipher.
	 */
	private void askPasswordAndDecipher(boolean dirMode, boolean recursiveMode) {
		errorFiles.clear();
		successFiles.clear();
		// Ask user for password
		final JPasswordField passField = new JPasswordField(MAX_PWD_LENGTH);

		JOptionPane passDialog = createUserPasswordDialog(passField);

		if (passDialog.getValue() != null && passDialog.getValue().equals(JOptionPane.OK_OPTION)) {
			String password = new String(passField.getPassword());
			// Clear user entry
			Arrays.fill(passField.getPassword(), '0');

			CountDownLatch waitDecipher = new CountDownLatch(1);

			JPanel waitPanel = new JPanel();
			final JLabel progress = new JLabel("Deciphering ...   ");
			waitPanel.add(progress);

			final JProgressBar bar;
			if (dirMode) {
				bar = new JProgressBar(0, 100);
				waitPanel.add(bar);
			} else {
				bar = null;
			}

			final JOptionPane waitOptionPane = new JOptionPane(waitPanel, JOptionPane.INFORMATION_MESSAGE,
					JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);
			JDialog waitDialog;
			if (!recursiveMode) {
				waitDialog = waitOptionPane.createDialog(panel, "Patience ...");
			} else {
				waitDialog = waitOptionPane.createDialog(panel,
						"Patience, in recursive mode, progress cannot be measured ...");
			}

			final DecipherSwingWorker worker = new DecipherSwingWorker(waitDecipher, waitDialog, dirMode, recursiveMode,
					password);
			worker.execute();

			if (bar != null) {
				worker.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						bar.setValue(worker.getProgress());
					}
				});
			}

			waitDialog.setModal(true);
			waitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			waitDialog.setVisible(true);

			try {
				// Do not set time limit, we cannot predict decipher time (depend on files
				// number and size)
				waitDecipher.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			password = "";

			if (errorFiles.isEmpty() && !successFiles.isEmpty()) {
				JOptionPane.showMessageDialog(new JFrame(), "All files properly deciphered.", "Utility Info",
						JOptionPane.INFORMATION_MESSAGE);
			} else if (!errorFiles.isEmpty() && !successFiles.isEmpty()) {
				JOptionPane.showMessageDialog(new JFrame(),
						"Files deciphered but some files failed or be ignored." + System.lineSeparator()
								+ "More informations are available in " + NAME_FILE_REPORT
								+ " file in destination folder.",
						"Utility Warning", JOptionPane.WARNING_MESSAGE);
			} else if (!errorFiles.isEmpty() && successFiles.isEmpty()) {
				JOptionPane.showMessageDialog(new JFrame(),
						"All files fail to deciphered." + System.lineSeparator() + "More informations are available in "
								+ NAME_FILE_REPORT + " file in destination folder.",
						"Utility error", JOptionPane.ERROR_MESSAGE);
			} else if (errorFiles.isEmpty() && successFiles.isEmpty()) {
				JOptionPane.showMessageDialog(new JFrame(), "No files to deciphered.", "Utility Info",
						JOptionPane.INFORMATION_MESSAGE);
			}

			writeReportFile();
		}
	}

	@SuppressWarnings("serial")
	private JOptionPane createUserPasswordDialog(final JPasswordField passField) {

		// Add many methods to get focus (many problems without them with
		// different OS)
		passField.addAncestorListener(new RequestFocusListener());
		passField.addHierarchyListener(new HierarchyListener() {

			@Override
			public void hierarchyChanged(HierarchyEvent event) {
				try {
					// Do not remove this, on linux problem without waiting
					Thread.sleep(20);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				final Component component = event.getComponent();
				if (component.isShowing() && (event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					final Window toplevel = SwingUtilities.getWindowAncestor(component);
					toplevel.addWindowFocusListener(new WindowAdapter() {

						@Override
						public void windowGainedFocus(WindowEvent e) {
							component.requestFocus();
							toplevel.removeWindowFocusListener(this);
						}
					});
					component.removeHierarchyListener(this);
				}
			}
		});

		final JOptionPane passDialog = new JOptionPane(passField, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION) {

			@Override
			public void setInitialValue(Object newInitialValue) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						// Add many methods to get focus (many problems without
						// them with different OS)
						passField.requestFocusInWindow();
					}
				});
			};
		};

		JDialog dialog = passDialog.createDialog(panel, "Please enter User-Password for ciphered files");
		dialog.setModal(true);
		dialog.setVisible(true);

		return passDialog;
	}

	private void writeReportFile() {
		// Write errors in file
		Path resFile;
		if (dstFile.isDirectory()) {
			resFile = Paths.get(dstFile.getAbsolutePath() + File.separator + NAME_FILE_REPORT);
		} else {
			resFile = Paths.get(dstFile.getParentFile().getAbsolutePath() + File.separator + NAME_FILE_REPORT);
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Files in success for decipher operations in Hybrid Backup Sync utility :"
				+ System.lineSeparator() + System.lineSeparator());
		for (File eachSuccessPath : successFiles) {
			builder.append(eachSuccessPath.getAbsolutePath() + System.lineSeparator());
		}
		builder.append(System.lineSeparator() + "----------------" + System.lineSeparator() + System.lineSeparator());
		builder.append("Files in error for decipher operations in Hybrid Backup Sync utility :" + System.lineSeparator()
				+ System.lineSeparator());
		for (File eachErrorPath : errorFiles) {
			builder.append(eachErrorPath.getAbsolutePath() + System.lineSeparator());
		}

		byte[] buf = builder.toString().getBytes();
		try {
			Files.write(resFile, buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
