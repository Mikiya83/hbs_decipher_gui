package qnapdecryptgui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

	private static final String IMAGES_PADLOCK_JPG = "images/padlock.png";

	private static final String IMAGE_GITHUB = "images/github.png";

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

	URI uri;

	private static void open(URI uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException exc) {
				System.err.println("Couldn't open link to Github: " + exc.getMessage());
			}
		} else {
			System.err.println("Couldn't open link to Github, browser not supported.");
		}
	}

	public QnapdecryptPanel(QnapdecryptPresentationModel presentationModel) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (screenSize.getWidth() / 2);
		int height = (int) (screenSize.getHeight() / 4);

		this.setPreferredSize(new Dimension(width, height));
		this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		MouseAdapter mouseClick = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getSource().equals(sourceChoosen)) {
					sourceButton.doClick();
				} else if (evt.getSource().equals(destChoosen)) {
					destButton.doClick();
				}
			}
		};

		this.sourceLabel = new JLabel("Source : ");
		this.destLabel = new JLabel("Destination : ");
		this.sourceChoosen = new JTextField();
		sourceChoosen.setEditable(false);
		sourceChoosen.addMouseListener(mouseClick);
		this.recursive = new JCheckBox("Recursive mode (only if directory selected)");
		recursive.setToolTipText(
				"<HTML>Search files in sub-directories too. <b>Used it with caution</b>, it can take a lot of time and resources.</HTML>");

		this.destChoosen = new JTextField();
		destChoosen.setEditable(false);
		destChoosen.addMouseListener(mouseClick);

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
		JLabel title = new JLabel(
				"<HTML><b>Hybrid Backup Sync decipher utility</b> - Unofficial tool made by Mikiya -<HTML>");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		titlePanel.add(new JLabel(createImageIcon(IMAGES_PADLOCK_JPG)));
		titlePanel.add(title, BorderLayout.CENTER);

		try {
			uri = new URI("https://github.com/Mikiya83/hbs_decipher");
			JButton button = new JButton(createImageIcon(IMAGE_GITHUB));
			button.setContentAreaFilled(false);
			button.setBorder(BorderFactory.createEmptyBorder());
			button.setToolTipText("Check my GitHub repository for more informations");
			button.setCursor(new Cursor(Cursor.HAND_CURSOR));
			button.addActionListener(action -> {
				open(uri);
			});
			titlePanel.add(button);
		} catch (URISyntaxException e) {
			System.err.println("Couldn't create link to Github: " + e.getMessage());
		}

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
