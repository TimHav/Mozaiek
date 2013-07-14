// Copyright 12-7-2008 Tim Havinga
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class MImage extends JPanel implements MouseListener, MouseMotionListener {
	private final static long serialVersionUID = 1L;

	BufferedImage fields, numbs, grid;
	boolean firstTime = true;

	final int CREATE_MODE = 0;
	final int SOLVE_MODE = 1;
	final int SHOW_MODE = 2;

	int height = 1;
	int width = 1;

	int selX = -1;
	int selY = -1;
	int selW = 0;
	int selH = 0;
	Boolean add = true;

	int STATE = CREATE_MODE;
	int SIZE = 20;
	int FONTSIZE = 16;
	boolean NUMBERS = false;
	boolean CREATING = false;

	int [][] numbers = {{0}};
	Boolean [][] black = {{false}};

	CreateWorker worker;
	Mozaiek gui;

	MImage (int w, int h, Mozaiek parent) {
		width = w;
		height = h;
		numbers = new int [width][height];
		black = new Boolean [width][height];
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				black[i][j] = false;
			}
		}
		init(parent);
	}

	MImage (Mozaiek parent) {
		enterSize();
		init(parent);
	}

	private void init(Mozaiek parent) {
		addMouseListener(this);
		addMouseMotionListener(this);
		gui = parent;
		//setBorder(BorderFactory.createLineBorder(Color.black));
		setBackground(Color.white);
		repaint();
	}

	public boolean getNumbersVisible() {
		return NUMBERS;
	}

	public void setNumbersVisible(boolean vis) {
		if (NUMBERS != vis) {
			NUMBERS = vis;
			repaint();
			gui.setNumbersVisible(vis);
		}
		//if (vis) renewNumbersImage();
	}

	public boolean getCreating() {
		return CREATING;
	}

	public int getState() {
		return STATE;
	}

	public int getWidth() {
		return width * SIZE + 1;
	}

	public int getHeight() {
		return height * SIZE + 1;
	}

	public int getFields() {
		return width * height;
	}

	public boolean setSize(int newSize) {
		if (newSize < 10 || newSize > 50) {
			JOptionPane op = new JOptionPane();
			op.showMessageDialog(gui, "De grootte moet een waarde tussen de 10 en 50 zijn.", "Error", JOptionPane.INFORMATION_MESSAGE);
			return false;
		} 
		SIZE = newSize;
		FONTSIZE = Math.max(10, SIZE - 4);
		renewGridImage();
		renewFieldsImage();
		//if (NUMBERS) renewNumbersImage();
		return true;
	}

	public void enterSize() {
		if (STATE == CREATE_MODE) {
			int oldwidth = width;
			int oldheight = height;
			JOptionPane op = new JOptionPane();
			boolean corr = false;
			while(!corr) {
				String h = op.showInputDialog(gui, "Geef het aantal rijen (de hoogte):", 1);
				try {
					height = Integer.parseInt(h);
				} catch (NumberFormatException e) {
					height = 0;
				}
				if (height < 1 || height > 100) {
					op = new JOptionPane();
					int ans = op.showConfirmDialog(gui, "De hoogte moet een waarde tussen de 1 en 100 zijn.\nWil je opnieuw proberen?\nZo niet, dan wordt de waarde afgerond naar de dichtstbijzijndste grens.", "Error", JOptionPane.YES_NO_OPTION);
					if (ans != JOptionPane.YES_OPTION) {
						height = Math.min(Math.max(height,1), 100);
						corr = true;
					}
				} else {
					corr = true;
				}
			}
			corr = false;
			while(!corr) {
				op = new JOptionPane();
				String w = op.showInputDialog(gui, "Geef het aantal kolommen (de breedte):", 1);
				try {
					width = Integer.parseInt(w);
				} catch (NumberFormatException e) {
					width = 0;
				}
				if (height < 1 || height > 100) {
					op = new JOptionPane();
					int ans = op.showConfirmDialog(gui, "De breedte moet een waarde tussen de 1 en 100 zijn.\nWil je opnieuw proberen?\nZo niet, dan wordt de waarde afgerond naar de dichtstbijzijndste grens.", "Error", JOptionPane.YES_NO_OPTION);
					if (ans != JOptionPane.YES_OPTION) {
						width = Math.min(Math.max(width,1), 100);
						corr = true;
					}
				} else {
					corr = true;
				}
			}
			numbers = (int [][]) resizeArray(numbers, width);
			for (int i=0; i<width; i++) {
				if (numbers[i] == null)	{
					numbers[i] = new int [height];
				} else {
					numbers[i] = (int []) resizeArray(numbers[i], height);
				}
			}
			black = (Boolean [][]) resizeArray(black, width);
			for (int i=0; i<width; i++) {
				if (black[i] == null) {
					black[i] = new Boolean [height];
					for (int j=0; j<height; j++)
						black[i][j] = false;
				} else {
					black[i] = (Boolean []) resizeArray(black[i], height);
					for (int j=oldheight; j<height; j++)
						black[i][j] = false;
				}
			}
			if (oldwidth < width) {
				for (int i=0; i<height; i++) {
					numbers[oldwidth][i] = 0;
					if (getValue(oldwidth-1, i-1)) numbers[oldwidth][i] ++;
					if (getValue(oldwidth-1, i))   numbers[oldwidth][i] ++;
					if (getValue(oldwidth-1, i+1)) numbers[oldwidth][i] ++;
				}
			} else if (width < oldwidth) {
				for (int i=0; i<height; i++) {
					numbers[width-1][i] = 0;
					if (getValue(width-2, i-1)) numbers[width-1][i] ++;
					if (getValue(width-1, i-1)) numbers[width-1][i] ++;
					if (getValue(width-2, i))   numbers[width-1][i] ++;
					if (getValue(width-1, i))   numbers[width-1][i] ++;
					if (getValue(width-2, i+1)) numbers[width-1][i] ++;
					if (getValue(width-1, i+1)) numbers[width-1][i] ++;
				}
			}
			if (oldheight < height) {
				for (int i=0; i<width; i++) {
					numbers[i][oldheight] = 0;
					if (getValue(i-1, oldheight-1)) numbers[i][oldheight] ++;
					if (getValue(i,   oldheight-1)) numbers[i][oldheight] ++;
					if (getValue(i+1, oldheight-1)) numbers[i][oldheight] ++;
				}
			} else if (height < oldheight) {
				for (int i=0; i<width; i++) {
					numbers[i][height-1] = 0;
					if (getValue(i-1, height-2)) numbers[i][height-1] ++;
					if (getValue(i-1, height-1)) numbers[i][height-1] ++;
					if (getValue(i,   height-2)) numbers[i][height-1] ++;
					if (getValue(i,   height-1)) numbers[i][height-1] ++;
					if (getValue(i+1, height-2)) numbers[i][height-1] ++;
					if (getValue(i+1, height-1)) numbers[i][height-1] ++;
				}
			}
			renewGridImage();
			renewFieldsImage();
			//if (NUMBERS) renewNumbersImage();
			repaint();
		} else {
			JOptionPane op = new JOptionPane();
			op.showMessageDialog(gui, "Aanpassen van de grootte is alleen mogelijk in Tekenmodus.", "Niet mogelijk", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private Object resizeArray (Object oldArray, int newSize) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType,newSize);
		int preserveLength = Math.min(oldSize,newSize);
		if (preserveLength > 0)
			System.arraycopy (oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	public void paintComponent(Graphics g) {
		if (width == numbers.length && height == numbers[0].length &&
			width == black.length && height == black[0].length) { // while not resizing

			super.paintComponent(g); // draws the background color

			if (firstTime) {
				renewGridImage();
				renewFieldsImage();
				//renewNumbersImage();
				firstTime = false;
			}

			g.drawImage(fields, 0, 0, this);
			drawSelection(g);
			if (NUMBERS) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setFont(new Font("Verdana", Font.PLAIN, FONTSIZE));
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				drawNumbers(g2d);
			}
			g.drawImage(grid, 0, 0, this);
		}
	}

	private void renewFieldsImage() {
		int type = BufferedImage.TYPE_BYTE_GRAY;
        fields = new BufferedImage(getWidth(), getHeight(), type);
		Graphics2D g2d = fields.createGraphics();
		g2d.setBackground(getBackground()); // white
        g2d.clearRect(0,0,getWidth(), getHeight());
		drawFields(g2d);
		g2d.dispose();
	}

	/*
	private void renewNumbersImage() {
		System.out.println("Redrawing numbers");
		int type = BufferedImage.TYPE_4BYTE_ABGR_PRE;
        numbs = new BufferedImage(getWidth(), getHeight(), type);
		Graphics2D g2d = numbs.createGraphics();
		g2d.setFont(new Font("Verdana", Font.PLAIN, FONTSIZE));
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		drawNumbers(g2d);
		g2d.dispose();
	}
	*/

	private void renewGridImage() {
		int type = BufferedImage.TYPE_4BYTE_ABGR_PRE;
        grid = new BufferedImage(getWidth(), getHeight(), type);
		Graphics2D g2d = grid.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		drawGrid(g2d);
		g2d.dispose();
	}

	/**
	  * Draws all fields black or gray that need to be.
	  */
	private void drawFields(Graphics2D g2d) {
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (black [i][j] == null) {
					g2d.setColor(new Color(192,192,192));
					g2d.fillRect(i*SIZE, j*SIZE, SIZE, SIZE);
				} else if (black [i][j]) {
					g2d.setColor(Color.black);
					g2d.fillRect(i*SIZE, j*SIZE, SIZE, SIZE);
				}
			}
		}
	}

	/**
	  * Draws the numbers
	  */
	private void drawNumbers(Graphics2D g2d) {
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (numbers[i][j] >= 0) {
					if (black [i][j] == null || !black[i][j]) {
						g2d.setColor(Color.black);
					} else {
						g2d.setColor(Color.white);
					}
					g2d.drawString("" + numbers[i][j], i*SIZE + SIZE/2 - FONTSIZE/3, j*SIZE + SIZE/2 + FONTSIZE/2 - 1);
				}
			}
		}
	}

	/**
	  * Draws all numbers black
	  */
	private void drawBlackNumbers(Graphics2D g2d) {
		g2d.setColor(Color.black);
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (numbers[i][j] >= 0) {
					g2d.drawString("" + numbers[i][j], i*SIZE + SIZE/2 - FONTSIZE/3, j*SIZE + SIZE/2 + FONTSIZE/2 - 1);
				}
			}
		}
	}

	/**
	  * Draws the current selection with a translucent color,
	  * to show the selection as a coloured overlay.
	  */
	private void drawSelection(Graphics g) {
		if (selX != -1 && selY != -1) { // there is a selection

			// set the color
			if (add == null) {
				g.setColor(new Color(255, 255, 0, 128)); // yellow
			} else if (add) {
				g.setColor(new Color(0, 255, 0, 128)); // green
			} else { // add = false
				g.setColor(new Color(255, 0, 0, 128)); // red
			}

			// draw the selection
			if (selW < 0) {
				int nSelX = selX + selW;
				if (selH < 0) {
					int nSelY = selY + selH;
					g.fillRect(nSelX*SIZE,nSelY*SIZE,(-selW+1)*SIZE,(-selH+1)*SIZE);
				} else {
					g.fillRect(nSelX*SIZE,selY*SIZE,(-selW+1)*SIZE,(selH+1)*SIZE);
				}
			} else if (selH < 0) { // selX cannot be < 0
				int nSelY = selY + selH;
				g.fillRect(selX*SIZE,nSelY*SIZE,(selW+1)*SIZE,(-selH+1)*SIZE);
			} else {
				g.fillRect(selX*SIZE,selY*SIZE,(selW+1)*SIZE,(selH+1)*SIZE);
			}
		}
	}

	/**
	  * Draws the grid,
	  * with a thicker line every 10 fields.
	  */
	private void drawGrid(Graphics2D g2d) {
		g2d.setColor(Color.black);
		float lineThickness = Math.max(SIZE / 10f - 1f, 1f);

		g2d.setStroke(new BasicStroke(lineThickness));
		// draw the normal grid
		for (int i=0; i<=height; i++) {
			g2d.drawLine(0, i*SIZE, width*SIZE, i*SIZE);
		}
		for (int i=0; i<=width; i++) {
			g2d.drawLine(i*SIZE, 0, i*SIZE, height*SIZE);
		}

		// draw the thicker lines, every 10 fields.
		g2d.setStroke(new BasicStroke(lineThickness * 2f));
		for (int i=0; i<(width/10+1); i++) {
			g2d.drawLine(i*10*SIZE, 0, i*10*SIZE, height*SIZE);
		}
		for (int i=0; i<(height/10+1); i++) {
			g2d.drawLine(0, i*10*SIZE, width*SIZE, i*10*SIZE);
		}
		if (width % 10 != 0)
			g2d.drawLine(width*SIZE, 0, width*SIZE, height*SIZE);
		if (height % 10 != 0)
			g2d.drawLine(0, height*SIZE, width*SIZE, height*SIZE);
	}

	public Dimension getMinimumSize() {
		return new Dimension(width * SIZE + 1, height * SIZE + 1);
	}
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	public Dimension getMaximumSize() {
		return getMinimumSize();
	}

	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		selX = e.getX() / SIZE;
		selY = e.getY() / SIZE;
		selW = 0;
		selH = 0;
		if (e.getButton() == MouseEvent.BUTTON1) { // links
			add = true;
		} else if (e.getButton() == MouseEvent.BUTTON2) { // wiel
			add = null;
		} else { // rechts
			add = false;
		}
		repaint();
	}
	public void mouseReleased(MouseEvent e) {
		if (selX != -1 && selY != -1) {
			if (add == null && STATE == CREATE_MODE) {
				JOptionPane op = new JOptionPane();
				op.showMessageDialog(null, "Je mag niet kleuren met grijs (onbekend) bij het maken van een puzzel.", "Niet toegestaan", JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				if (selW < 0) {
					selX += selW;
					selW = -selW;
				}
				if (selH < 0) {
					selY += selH;
					selH = -selH;
				}
				for (int i=(selX < 0 ? 0 : selX); i<=selX+selW && i<width; i++) {
					for (int j=(selY < 0 ? 0 : selY); j<=selY+selH && j<height; j++) {
						setValue(i, j, add);
					}
				}
				//if (NUMBERS) renewNumbersImage();
			}
			selX = -1;
			selY = -1;
			selH = 0;
			selW = 0;
		}
		renewFieldsImage();
		repaint();
	}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}

	public void mouseMoved(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {
		if (selX != -1 && selY != -1) {
			int nSelW = Math.min(e.getX() / SIZE, width-1) - selX;
			int nSelH = Math.min(e.getY() / SIZE, height-1) - selY;
			if (nSelW != selW || nSelH != selH) {
				selW = nSelW;
				selH = nSelH;
				repaint();
			}
		}
	}

	private void setValue(int x, int y, Boolean value) {
		if (STATE == SHOW_MODE) {
			STATE = SOLVE_MODE;
		}
		if (STATE == CREATE_MODE && black[x][y] != value) {
			for (int i=(x>0 ? x-1 : 0); i<=x+1 && i<width; i++) {
				for (int j=(y>0 ? y-1 : 0); j<=y+1 && j<height; j++) {
					numbers[i][j] += value ? 1 : -1;
				}
			}
		}
		black [x][y] = value;
	}

	private Boolean getValue(int x, int y) {
		if (x >= 0 && y >= 0 && x < width && y < height) {
			return black[x][y];
		}
		return false;
	}

	private int getGetal(int x, int y) {
		if (x >= 0 && y >= 0 && x < width && y < height) {
			return numbers[x][y];
		}
		return -1;
	}

	public void checkNumbers() {
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				numbers[i][j] = getNrBlacks(i,j);
			}
		}
	}

	private int getNrBlacks(int x, int y) {
		int number = 0;
		for (int i=(x>0?x-1:0); i<=x+1 && i<width; i++) {
			for (int j=(y>0?y-1:0); j<=y+1 && j<height; j++) {
				if (black[i][j] != null)
					number += black[i][j] ? 1 : 0;
			}
		}
		return number;
	}

	public void save() {
		JOptionPane op = new JOptionPane();
		Object[] options = {"Plaatje", "CSV bestand", "Annuleren"};
		int n = JOptionPane.showOptionDialog(gui,
			"Wil je de puzzel opslaan als plaatje of als CSV bestand?\n\n" +
			"Gebruik opslaan als plaatje om de puzzel uit te printen,\n" +
			"en gebruik opslaan als CSV bestand om het bestand te kunnen aanpassen in Excel\n" + 
			"of een text editor, en het later weer te kunnen laden in dit programma.",
			"Hoe wilt u opslaan?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

		if (n == JOptionPane.CLOSED_OPTION || n == 2) return;

		File f = new File(""/*System.getProperty("user.dir")*/);
		JFileChooser fc = new JFileChooser(f);
		fc.setMultiSelectionEnabled(false);
		boolean [] ans = null;
		if (n == 0) {
			fc.setFileFilter(new ImageFileFilter());
			ImageDialog id = new ImageDialog("Wat wilt u opslaan?");
			id.showModalDialog();
			ans = id.getValues();
			if (ans == null) return;

		} else {
			fc.setFileFilter(new CsvFileFilter());
		}
		int opt = fc.showSaveDialog(gui);
		if (opt == fc.APPROVE_OPTION) {
			File selFile = fc.getSelectedFile();
			if (n == 0) {
				try {
					System.out.println("Writing image to file: " + selFile.getAbsolutePath());
					Dimension size = getPreferredSize();
					int width = (int) size.getWidth();
					int height = (int) size.getHeight();
		
					BufferedImage image = (BufferedImage) createImage(width, height);
					Graphics2D g = image.createGraphics();
					super.paintComponent(g);
					if (ans[1]) drawFields(g);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
					if (ans[2]) {
						g.setFont(new Font("Verdana", Font.PLAIN, FONTSIZE));
						g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
						if (ans[1]) drawNumbers(g);
						if (!ans[1]) drawBlackNumbers(g);
					}
					if (ans[0]) drawGrid(g);

					ImageIO.write(image, selFile.getName().substring(selFile.getName().indexOf('.')+1), selFile);
				}
				catch (IOException e) {
					System.out.println("Error:\n" + e.getMessage());
				}
			}
			if (n == 1) {
				try {
					System.out.println("Writing image to .CSV file: " + selFile.getAbsolutePath());
					FileWriter fstream = new FileWriter(selFile.getAbsolutePath());
					BufferedWriter out = new BufferedWriter(fstream);
					for (int i=0; i<height; i++) {
						for (int j=0; j<width; j++) {
							out.write((numbers[j][i] >= 0 ? numbers[j][i] : "") + (j == (width-1) ? "" : ";"));
						}
						out.write("\r\n");
					}
					out.close();
				} catch (IOException e) {
					System.out.println("Error:\n" + e.getMessage());
				}
			}
		}
	}

	public void load() {
		File f = new File(""/*System.getProperty("user.dir")*/);
		JFileChooser fc = new JFileChooser(f);
		fc.setFileFilter(new CsvFileFilter());
		fc.setMultiSelectionEnabled(false);
		int opt = fc.showOpenDialog(gui);
		if (opt == fc.APPROVE_OPTION) {
		File selFile = fc.getSelectedFile();
			String path = selFile.getAbsolutePath();
			System.out.println("Loading CSV file " + path);
			loadFile(path);
		}
	}

	public void loadFile(String fName) {
		try {
			height = 1;
			width = 1;
			numbers = new int [1][1];
			numbers[0][0] = -1;
			black = new Boolean [1][1];
			FileInputStream fis = new FileInputStream(fName);
			InputStreamReader in = new InputStreamReader(fis);
			int nextCh = in.read();
			while ((char) nextCh != '\r') {
				if ((char) nextCh == ';') {
					width++;
					numbers = (int [][]) resizeArray(numbers, width);
					numbers[width-1] = new int [1];
					numbers [width-1][height-1] = -1;
				} else {
					numbers[width-1][height-1] = Integer.parseInt("" + (char) nextCh);
				}
				nextCh = in.read();
			}
			while (nextCh != -1 && (char) nextCh == '\r') { // read lines until no more are found.
				in.read(); // lees '\n'
				nextCh = in.read();
				if (nextCh == -1) break; // einde bestand
				height ++;
				for (int i=0; i<width; i++) {
					numbers[i] = (int []) resizeArray(numbers[i], height);
				}
				for (int i=0; i<width-1; i++) { // all except the last one
					if ((char) nextCh == ';') {
						numbers[i][height-1] = -1;
					} else {
						numbers[i][height-1] = Integer.parseInt("" + (char) nextCh);
						if ((char) in.read() != ';')
							throw new RuntimeException("Illegal character found, ';' expected.");
					}
					nextCh = in.read();
				}
				if ((char) nextCh == '\r' || nextCh == -1) { // last one does not have ';'
					numbers[width-1][height-1] = -1;
				} else {
					numbers[width-1][height-1] = Integer.parseInt("" + (char) nextCh);
					nextCh = (char) in.read();
				}
			}
			black = new Boolean[width][height];
			in.close();
			fis.close();
			STATE = SOLVE_MODE;
		}
		catch (IOException e) {
			throw new RuntimeException("Error while loading file:\n\n" + e.getMessage());
		}
		catch (NumberFormatException e) {
			throw new RuntimeException("Error in invoer-bestand, getal verwacht op regel " + height);
		}
		renewGridImage();
		repaint();
	}

	public void solve() {
		Solver solver = new Solver(numbers);
		boolean solved = solver.solve(3);
		if (!solved) {
			JOptionPane op = new JOptionPane();
			op.showMessageDialog(gui, "Gedeeltelijke oplossing gevonden. De grijze vakjes konden niet worden opgelost.", "Gedeeltelijk opgelost.", JOptionPane.INFORMATION_MESSAGE);
		}
		black = solver.getBlackResult();
		STATE = SHOW_MODE;
		renewFieldsImage();
		//if (NUMBERS) renewNumbersImage();
		repaint();
	}

	public boolean currentNumbersPossible(int percent, int level) {
		int emptyFields = countEmptyFields();
		if (emptyFields == 0) {
			return false;
		}
		if (emptyFields < (int)(((100 - percent) * height * width) / 100)) {
			System.out.println("smaller");
			Solver solver = new Solver(numbers);
			return solver.solve(level);
		}
		return false;
	}

	public int countNulls() {
		int counter = 0;
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (black[i][j] == null) {
					counter++;
				}
			}
		}
		return counter;
	}

	public int countEmptyFields() {
		int emptyFields = 0;
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (numbers[i][j] < 0) {
					emptyFields ++;
				}
			}
		}
		return emptyFields;
	}

	private void print (Boolean [][] array) {
		for (int i=0; i<array.length; i++) {
			for (int j=0; j<array[0].length; j++) {
				if (array[i][j] == null) {
					System.out.print("-");
				} else {
					System.out.print(array[i][j] ? "1" : "0");
				}
			}
			System.out.println();
		}
	}

	public void abortCreating() {
		worker.stop(false);
	}

	public void startCreating(int percent, int slim) {
		CREATING = true;
		worker = new CreateWorker();
		worker.init(percent, slim);
		worker.execute();
	}

	public class CreateWorker extends SwingWorker<Boolean, Void> {
		Solver solver;
		int percent = 100;
		int level = 0;

		public void init(int pct, int slim) {
			percent = pct;
			level = slim;
			gui.setCreatingPuzzle(true);
		}

		public Boolean doInBackground() {
			solver = new Solver(numbers, gui);
			boolean result = solver.createPuzzle2(percent, level);
			return result;
		}

		public void stop(boolean abort) {
			if (abort) {
				cancel(true);
			}
			else {
				solver.cancel();
			}
		}

		public void done() {
			CREATING = false;
			JOptionPane op = new JOptionPane();
			boolean success = false;
			try {
				success = get();
			} catch (Exception e) {
				System.out.print(e.getMessage());
				e.printStackTrace();
			}
			numbers = solver.getNumResult();
			if (success) {
				STATE = SHOW_MODE;
			} else {
				op.showMessageDialog(null, "Creatie van puzzel is geannuleerd /\n er kon geen moeilijkere puzzel gemaakt worden.", "Done", JOptionPane.INFORMATION_MESSAGE);
				STATE = CREATE_MODE;
				gui.setMoeilijkheid((getFields() - countEmptyFields()) * 100 / getFields());
			}
			setNumbersVisible(true);
			gui.setCreatingPuzzle(false);
			gui.deleteProgressBar();
			repaint();
		}
	}

	public class ImageDialog extends JDialog implements ActionListener {
		private boolean canceled;
		private JButton okButton;
		private JButton cancelButton;
		private JCheckBox grid;
		private JCheckBox fields;
		private JCheckBox numbers;

		public ImageDialog( String title) {
			super((JFrame) null, title);
			initComponents();
			setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
			addWindowListener( new WindowAdapter() {
				public void windowClosing( WindowEvent e ) {
					close(true);
				}
			});
		}

		public void actionPerformed(ActionEvent e) {
			close(e.getSource() == cancelButton);
		}

		private void close(boolean canceled) {
			this.canceled = canceled;
			dispose();
		}

		private void initComponents() {
			JPanel contents = new JPanel();
			contents.setLayout(new BorderLayout(3,12));
			contents.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));

			contents.add(new JLabel("Wat wilt u opslaan in het plaatje?"), BorderLayout.NORTH);

			JPanel options = new JPanel();
			options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
			grid = new JCheckBox("Het raster", true);
			options.add(grid);
			fields = new JCheckBox("De vlakjes (tekening)", false);
			options.add(fields);
			numbers = new JCheckBox("De getallen (puzzel)", true);
			options.add(numbers);

			contents.add(options, BorderLayout.CENTER);

			JPanel btnPanel = new JPanel();
			btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
			okButton = new JButton("OK");
			okButton.addActionListener(this);
			btnPanel.add(okButton);
			cancelButton = new JButton("Annuleren");
			cancelButton.addActionListener(this);
			btnPanel.add(cancelButton);

			contents.add(btnPanel, BorderLayout.SOUTH);
			getContentPane().add(contents);

			setResizable(false);
		}

		public boolean isCanceled() {
			return canceled;
		}

		public boolean[] getValues() {
			if (isCanceled()) return null;
			boolean [] result = {grid.isSelected(), fields.isSelected(), numbers.isSelected()};
			return result;
		}

		public boolean showModalDialog() {
			setModal( true );
			pack();
			setVisible(true);
			return canceled;
		}
	}

}
