// Copyright 12-7-2008 Tim Havinga
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

public class Mozaiek extends JApplet implements ActionListener {
	private final static long serialVersionUID = 1L;
	public static boolean isApplet;

	JButton resize, createPuzzel, solvePuzzel, save, open, exit, getallen;
	JTextField moeilijkheid, hokgrootte;
	JPanel imgPanel, progressPanel, contents;
	JScrollPane sp;
	MImage image;
	JLabel state;
	JProgressBar createProgress;
	JPopupMenu popup;
	JMenuItem slim, slimmer, slimst;

	int toRemove;

	public Mozaiek () {
		contents = new JPanel();
		contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));

		contents.add(createTopPanel());

		image = new MImage(10, 10, this);
		imgPanel = new JPanel(new BorderLayout());
		imgPanel.setBackground(Color.white);
		imgPanel.add(image, BorderLayout.CENTER);
		imgPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		sp = new JScrollPane(imgPanel);
		sp.getVerticalScrollBar().setUnitIncrement(20);
		sp.getHorizontalScrollBar().setUnitIncrement(20);
		sp.setBorder(null);
		contents.add(sp);

		contents.add(createBottomPanel());

		progressPanel = new JPanel();
		progressPanel.setBackground(Color.white);
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
		progressPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		contents.add(progressPanel);

		popup = new JPopupMenu();
		slim = new JMenuItem("Slim");
		slim.addActionListener(this);
		popup.add(slim);
		slimmer = new JMenuItem("Slimmer");
		slimmer.addActionListener(this);
		popup.add(slimmer);
		slimst = new JMenuItem("Slimst");
		slimst.addActionListener(this);
		popup.add(slimst);

		//setSize(800,730);
		//setTitle("Mozaiek");

		//addWindowListener(new WindowAdapter() {
		//	public void windowClosing(WindowEvent e) {
		//		System.exit(0) ; 
		//	}
		//});

		//setResizable(false);
		validate();
		//setVisible(true);
	}

	private JPanel createTopPanel() {		
		LineBorder lb = new LineBorder(Color.black);
		
		JPanel top = new JPanel();
		top.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.setBackground(Color.white);
		
		top.add(new JLabel("Tekenen:"));
		top.add(Box.createRigidArea(new Dimension(20, 0)));
		
		JComponent kleur = (JComponent) Box.createRigidArea(new Dimension(20,20));
		kleur.setOpaque(true);
		kleur.setBackground(Color.black);
		kleur.setBorder(lb);
		top.add(kleur);
		top.add(Box.createRigidArea(new Dimension(4, 0)));

		top.add(new JLabel("(gevuld): linkermuisknop"));
		top.add(Box.createRigidArea(new Dimension(20, 0)));

		kleur = (JComponent) Box.createRigidArea(new Dimension(20,20));
		kleur.setOpaque(true);
		kleur.setBackground(Color.white);
		kleur.setBorder(lb);
		top.add(kleur);
		top.add(Box.createRigidArea(new Dimension(4, 0)));
		
		top.add(new JLabel("(leeg): rechtermuisknop"));
		top.add(Box.createRigidArea(new Dimension(20, 0)));
		
		kleur = (JComponent) Box.createRigidArea(new Dimension(20,20));
		kleur.setOpaque(true);
		kleur.setBackground(new Color(192,192,192));
		kleur.setBorder(lb);
		top.add(kleur);
		top.add(Box.createRigidArea(new Dimension(4, 0)));
		
		top.add(new JLabel("(onbekend): scrollwiel"));

		top.add(Box.createHorizontalGlue());

		state = new JLabel("Tekenmodus");
		state.setForeground(Color.gray);
		top.add(state);
		return top;
	}

	private JPanel createBottomPanel() {
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.setBackground(Color.white);
		bottom.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		JPanel openSave = new JPanel(new GridLayout(2,1,0,3));
		openSave.setBackground(Color.white);
		open = new JButton("Open...");
		save = new JButton("Save...");
		open.addActionListener(this);
		save.addActionListener(this);
		openSave.add(open);
		openSave.add(save);
		bottom.add(openSave);
		bottom.add(Box.createRigidArea(new Dimension(10, 0)));

		JPanel labels = new JPanel();
		labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
		labels.setBackground(Color.white);
		labels.add(new JLabel("Moeilijkheidsgraad:"));
		labels.add(new JLabel("(% zichtbare getallen)"));
		labels.add(Box.createRigidArea(new Dimension(0, 6)));
		labels.add(new JLabel("Grootte van de vakjes:"));
		labels.add(Box.createRigidArea(new Dimension(0, 3)));
		bottom.add(labels);
		bottom.add(Box.createRigidArea(new Dimension(6, 0)));

		JPanel invoer = new JPanel(new GridLayout(2,1,0,3));
		invoer.setBackground(Color.white);
		moeilijkheid = new JTextField("25",5);
		hokgrootte = new JTextField("20",5);
		//moeilijkheid.setMaximumSize(new Dimension(100,30));
		//hokgrootte.setMaximumSize(new Dimension(100,30));
		moeilijkheid.addActionListener(this);
		hokgrootte.addActionListener(this);
		invoer.add(moeilijkheid);
		invoer.add(hokgrootte);
		bottom.add(invoer);
		bottom.add(Box.createRigidArea(new Dimension(10, 0)));

		JPanel puzzel = new JPanel(new GridLayout(2,1,0,3));
		puzzel.setBackground(Color.white);
		createPuzzel = new JButton("Maak een puzzel");
		solvePuzzel = new JButton("Los de puzzel op");
		createPuzzel.addActionListener(this);
		solvePuzzel.addActionListener(this);
		puzzel.add(createPuzzel);
		puzzel.add(solvePuzzel);
		bottom.add(puzzel);
		bottom.add(Box.createRigidArea(new Dimension(10, 0)));

		JPanel sizeNum = new JPanel(new GridLayout(2,1,0,3));
		sizeNum.setBackground(Color.white);
		resize = new JButton("Puzzelgrootte...");
		getallen = new JButton("Toon getallen");
		resize.addActionListener(this);
		getallen.addActionListener(this);
		sizeNum.add(resize);
		sizeNum.add(getallen);
		bottom.add(sizeNum);

		if (!isApplet) {
			bottom.add(Box.createRigidArea(new Dimension(10, 0)));
			exit = new JButton("Beëindigen");
			exit.addActionListener(this);
			bottom.add(exit);
		}
		bottom.setMaximumSize(new Dimension(800,50));
		return bottom;
	}

	public void updateState() {
		int s = image.getState();
		if (s == image.CREATE_MODE)
			state.setText("Tekenmodus");
		if (s == image.SOLVE_MODE)
			state.setText("Oplosmodus");
		if (s == image.SHOW_MODE)
			state.setText("Bekijkmodus");
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resize) {
			image.enterSize();
			resizeScrollPane();
		} else if (e.getSource() == save) {
			image.save();
		} else if (e.getSource() == open) {
			image.load();
			resizeScrollPane();
			image.setNumbersVisible(true);
		} else if (e.getSource() == solvePuzzel) {
			image.solve();
		} else if (e.getSource() == exit) {
			System.exit(0);
		} else if (e.getSource() == getallen) {
			if (image.getNumbersVisible()) {
				image.setNumbersVisible(false);
			} else {
				image.setNumbersVisible(true);
			}
			image.repaint();
		} else if (e.getSource() == hokgrootte) {
			String val = hokgrootte.getText();
			try {
				boolean corr = image.setSize(Integer.parseInt(val));
				if (corr) {
					resizeScrollPane();
				}
			}
			catch (NumberFormatException nfe) {
				JOptionPane op = new JOptionPane();
				op.showMessageDialog(this, "De grootte van de vakjes moet een getal zijn.", "Error", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (e.getSource() == createPuzzel) {
			if (image.getCreating()) {
				image.abortCreating();
			} else {
				popup.show(createPuzzel, createPuzzel.getX() + createPuzzel.getWidth(), createPuzzel.getY());
			}
		} else if (e.getSource() == slim) {
			startCreatimgPuzzle(1);
		} else if (e.getSource() == slimmer) {
			startCreatimgPuzzle(2);
		} else if (e.getSource() == slimst) {
			startCreatimgPuzzle(3);
		}
		updateState();
	}

	private void startCreatimgPuzzle(int slim) {
		try {
			int pct = Integer.parseInt(moeilijkheid.getText());
			if (pct < 0 || pct > 100) {
				JOptionPane op = new JOptionPane();
				op.showMessageDialog(this, "De moeilijkheid moet een percentage zijn,\n" +
					"Dus een geheel getal tussen de 0 en de 100.", "Geen percentage", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (image.countNulls() > 0) {
				JOptionPane op = new JOptionPane();
				op.showMessageDialog(this, "Het plaatje mag geen onbekende (grijze) vakjes bevatten.", "Error", JOptionPane.INFORMATION_MESSAGE);
			} else {
				addProgressBar(pct);
				image.setNumbersVisible(false);
				if (image.currentNumbersPossible(pct, slim)) {
					JOptionPane op = new JOptionPane();
					int ans = op.showConfirmDialog(this, "Wilt u doorgaan met de huidige getallen?", "Doorgaan", JOptionPane.YES_NO_OPTION);
					if (ans != op.YES_OPTION)
						image.checkNumbers();
				} else {
					image.checkNumbers();
				}
				createPuzzel.setText("Afbreken");
				image.startCreating(pct, slim);
			}
		} catch (NumberFormatException nfe) {
			JOptionPane op = new JOptionPane();
			op.showMessageDialog(this, "De moeilijkheid moet een getal zijn.", "Error", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void resizeScrollPane() {
		Dimension i = sp.getSize(null);
		Dimension j = image.getPreferredSize();
		int dHeight = (int) Math.max((i.getHeight() - j.getHeight())/2, 0);
		int dWidth = (int) Math.max((i.getWidth() - j.getWidth())/2, 0);
		if (j.getHeight() > i.getHeight()) dWidth = Math.max(dWidth - 8, 0); // vertical scrollbars
		if (j.getWidth() > i.getWidth()) dHeight = Math.max(dHeight - 8, 0); // horizontal scrollbars
		imgPanel.setBorder(new EmptyBorder(dHeight,dWidth,0,0));
		validate();
		updateState();
	}

	public void setNumbersVisible(boolean vis) {
		if (vis) {
			getallen.setText("Verberg getallen");
		} else {
			getallen.setText("Toon getallen");
		}
	}

	public void setCreatingPuzzle(boolean vis) {
		if (vis) {
			createPuzzel.setText("Afbreken");
		} else {
			createPuzzel.setText("Maak een puzzel");
		}
		open.setEnabled(!vis);
		save.setEnabled(!vis);
		resize.setEnabled(!vis);
		getallen.setEnabled(!vis);
		solvePuzzel.setEnabled(!vis);
	}

	public void setMoeilijkheid(int m) {
		moeilijkheid.setText("" + m);
	}

	public void addProgressBar(int percent) {
		progressPanel.setBorder(BorderFactory.createEmptyBorder(0,3,3,3));
		JLabel progress = new JLabel("Progress: ");
		progressPanel.add(progress);
		toRemove = image.getFields() * (100-percent) / 100;
		createProgress = new JProgressBar(SwingConstants.HORIZONTAL, 0, toRemove);
		createProgress.setStringPainted(true);
		createProgress.setString("0 / " + toRemove);
		progressPanel.add(createProgress);

		validate();
		resizeScrollPane();
	}

	public void deleteProgressBar() {
		progressPanel.removeAll();
		progressPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		validate();
		resizeScrollPane();
	}

	public void updateProgressBar(int value) {
		if (createProgress != null && value >= 0 && value < toRemove) {
			createProgress.setValue(value);
			createProgress.setString(value + " / " + toRemove);
		}
	}

	public JPanel getContents() {
		return contents;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				isApplet = false;
				JFrame frame = new JFrame("Mozaiek");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Mozaiek m = new Mozaiek();
				frame.getContentPane().add(m.getContents());

				frame.setSize(800,730);           // Set the size of the frame
				frame.setVisible(true);           // Show the frame
				m.resizeScrollPane();
			}
		});
	}

	public void init() {
		isApplet = true;
		Mozaiek m = new Mozaiek();
		getContentPane().add(m.getContents());
	}

	public void start() {
		resizeScrollPane();
	}
	public void stop() {}
	public void destroy() {}

	static void print (Boolean [][] array) {
		for (int i=0; i<array.length; i++) {
			for (int j=0; j<array[0].length; j++) {
				if (array[j][i] == null) {
					System.out.print("-");
				} else {
					System.out.print(array[j][i] ? "1" : "0");
				}
			}
			System.out.println();
		}
	}
}
