package qnapdecryptgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class QnapdecryptPanel extends JPanel {

	private static final String IMAGES_PADLOCK_JPG = "images/padlock.jpg";

	/**
	 * Default id
	 */
	private static final long serialVersionUID = -403257368452607292L;

	private JButton decipherButton;

	private JButton destButton;

	private JTextField destChoosen;

	private JLabel destLabel;

	private JButton sourceButton;

	private JTextField sourceChoosen;

	private JLabel sourceLabel;

	private JCheckBox recursive;

	public QnapdecryptPanel(QnapdecryptPresentationModel presentationModel) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (screenSize.getWidth() / 2);
		int height = (int) (screenSize.getHeight() / 4);

		this.setPreferredSize(new Dimension(width, height));
		this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		this.sourceLabel = new JLabel("Source : ");
		this.destLabel = new JLabel("Destination : ");
		this.sourceChoosen = new JTextField();
		sourceChoosen.setEditable(false);
		this.recursive = new JCheckBox("Recursive mode (if directory selected)");

		this.destChoosen = new JTextField();
		destChoosen.setEditable(false);

		this.sourceButton = new JButton("Source file/directory");
		this.destButton = new JButton("Destination file/directory");
		this.decipherButton = new JButton("Decipher !");
		decipherButton.setEnabled(false);

		decipherButton.addActionListener(presentationModel.getDecipherActionListener());
		sourceButton.addActionListener(presentationModel.getSourceActionListener());
		destButton.addActionListener(presentationModel.getDestinationActionListener());
		recursive.addItemListener(presentationModel.getRecursiveModeItemListener());

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JPanel titlePanel = new JPanel();
		JLabel title = new JLabel("Hybric Backup Sync decipher utility - Unofficial tool made by Mikiya");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		titlePanel.add(new JLabel(createImageIcon(IMAGES_PADLOCK_JPG)));
		titlePanel.add(title, BorderLayout.CENTER);
		titlePanel.add(new JLabel(createImageIcon(IMAGES_PADLOCK_JPG)));

		this.add(titlePanel);

		JPanel srcPanel = new JPanel();
		srcPanel.setLayout(new GridLayout(4, 2, 4, 0));
		srcPanel.add(sourceLabel);
		srcPanel.add(sourceChoosen);
		srcPanel.add(recursive);

		this.add(srcPanel);

		JPanel dstPanel = new JPanel();
		dstPanel.setLayout(new GridLayout(3, 2, 4, 0));
		dstPanel.add(destLabel);
		dstPanel.add(destChoosen);
		this.add(dstPanel);

		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1, 3, 4, 0));
		btnPanel.add(sourceButton);
		btnPanel.add(destButton);
		btnPanel.add(decipherButton);
		this.add(btnPanel);

		this.add(Box.createVerticalGlue());

		this.setVisible(true);

	}

	public JButton getDecipherButton() {
		return decipherButton;
	}

	public JTextField getDestinationTextField() {
		return this.destChoosen;
	}

	public JTextField getSourceTextField() {
		return this.sourceChoosen;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	private ImageIcon createImageIcon(String path) {
		URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, "");
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

}
