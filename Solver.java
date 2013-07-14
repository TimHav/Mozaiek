import java.util.*;
import javax.swing.*;

public class Solver {
	ArrayList<Getal> numb;
	Boolean [][] black;
	int [][] numbers;
	int height;
	int width;
	Mozaiek gui;

	boolean CANCEL = false;

	Solver(int [][] num) {
		width = num.length;
		height = num[0].length;
		numbers = copyList(num);
		black = new Boolean[width][height];
		createNumberList(num);
	}

	Solver(int [][] num, Mozaiek m) {
		width = num.length;
		height = num[0].length;
		numbers = copyList(num);
		gui = m;
		black = new Boolean[width][height];
		createNumberList(num);
	}

	private void createNumberList(int [][] num) { // only uses the 2D numbers array
		numb = new ArrayList<Getal>();
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (num[i][j] >= 0) {
					Getal g = new Getal(i,j,num[i][j]);
					if (i == width-1) {
						g.setValue(1,-1,false);
						g.setValue(1,0,false);
						g.setValue(1,1,false);
					}
					if (j == height-1) {
						g.setValue(-1,1,false);
						g.setValue(0,1,false);
						g.setValue(1,1,false);
					}
					numb.add(g);
				}
			}
		}
		ListIterator li = numb.listIterator();
		while (li.hasNext() && li.nextIndex() != numb.size()) {
			Getal g = (Getal) li.next();
			ListIterator it = numb.listIterator(li.nextIndex());
			while (it.hasNext()) {
				Getal h = (Getal) it.next();
				if (Math.abs(g.getX() - h.getX()) <= 2 &&
					Math.abs(g.getY() - h.getY()) <= 2) {
					g.addNeighbour(h);
					h.addNeighbour(g);
				}
			}
		}
	}

	private void deleteUnremovables() {
		for (int i=0; i<numb.size(); i++) {
			Getal g = numb.get(i);
			if (!g.removable(width, height)) {
				numb.remove(i);
				i--;
			}
		}
	}

	public boolean solve(int level) {
		int times = 0;
		int countNull = 0;
		int newCountNull;
		while (countNull != (newCountNull = countNulls())) {
			times ++;
			while (countNull != (newCountNull = countNulls())) {
				countNull = newCountNull;
				ListIterator it = numb.listIterator();
				while (it.hasNext()) {
					Getal g = (Getal) it.next();
					if (!g.isSolved() && g.isSolveable()) {
						g.solve();
						updateBlack(g);
						System.out.println(g);
					}
				}
			}

			if (level > 1) {
				if (countNulls() == 0) break;
				ListIterator li = numb.listIterator();
				while (li.hasNext()) {
					Getal g = (Getal) li.next();
					if (!g.isSolved()) {
						if(g.checkWithNeighbours(level)) {
							updateBlack(g);
							System.out.println("> " + g);
						}
					}
				}
			}
		}
		return countNull == 0;
	}

	public boolean createPuzzle2(int percent, int level) {
		CANCEL = false;
		int toLeave = (int) (height * width * percent / 100.0);
		
		createNumberList(numbers);

		boolean solvable = solve(level);
		if (!solvable) return false;

		ArrayList<Getal> removables = new ArrayList<Getal> (numb);

		int left = removables.size();
		if (gui != null) gui.updateProgressBar(height * width - left);

		while (!CANCEL && left > toLeave && removables.size() > 0) {

			int index = (int)(Math.random() * removables.size());
			Getal g = removables.get(index);
			while (!g.removable(width, height)) {
				removables.remove(index);
				if (removables.size() == 0) return false;
				index = (int)(Math.random() * removables.size());
				g = removables.get(index);
			}
			// g is removable, try to remove it and see if the puzzle is still solveable
			ListIterator li = g.getNeighbours();
			int x = g.getX();
			int y = g.getY();
			
			for (int i=(x>0 ? x-1 : 0); i<=x+1 && i<width; i++) {
				for (int j=(y>0 ? y-1 : 0); j<=y+1 && j<height; j++) {
					g.setValue(i-x, j-y, null);
					//black[i][j] = null;
				}
			}
			numbers[x][y] = -1;
			g.remove();
			numb.remove(g);

			ListIterator it = numb.listIterator();
			while (it.hasNext()) {
				((Getal) it.next()).reset(width, height);
			}
			black = new Boolean[width][height];
			
			// test if the puzzle is still possible
			solvable = solve(level);

			if (!solvable) { // failed, restore original puzzle
				while (li.hasNext()) {
					Getal buur = (Getal) li.next();
					buur.addNeighbour(g);
				}
				numbers[x][y] = g.getGetal();
				numb.add(g);
			} else {
				left --;
				if (gui != null) gui.updateProgressBar(height * width - left);
			}
			/*
			int startX = Math.max(x-3, 0);
			int startY = Math.max(y-3, 0);
			int endX = Math.min(x+4, width);
			int endY = Math.min(y+4, height);
			Boolean [][] omg = new Boolean [endX-startX][endY-startY];
			for (int i=startX; i<endX; i++) {
				for (int j=startY; j<endY; j++) {
					omg[i-startX][j-startY] = black[i][j];
				}
			}

			numbers[x][y] = -1;
			g.remove();
			while (li.hasNext()) {
				Getal buur = (Getal) li.next();
				buur.reset(width, height);
				updateBlack2(buur);
			}
			numb.remove(g);
			
			// test if the puzzle is still possible
			solvable = solve(level);

			if (!solvable) { // failed, restore original puzzle
				while (li.hasPrevious()) { // at end of list, so now traverse it backwards
					Getal buur = (Getal) li.previous();
					buur.addNeighbour(g);
					for (int i=0; i<7; i++) {
						for (int j=0; j<7; j++) {
							g.setValue(buur, i-3, j-3, null);
						}
					}
				}
				for (int i=startX; i<endX; i++) {
					for (int j=startY; j<endY; j++) {
						black[i][j] = omg[i-startX][j-startY];
					}
				}
				numbers[x][y] = g.getGetal();
				numb.add(g);
			} else {
				left --;
				if (gui != null) gui.updateProgressBar(height * width - left);
			}
			*/
			removables.remove(g);
		}
		return (left == toLeave);
	}

	public boolean createPuzzle(int percent) {
		CANCEL = false;
		int toLeave = (int) (height * width * percent / 100.0);

		int [][] allNumbers = copyList(numbers);

		boolean success = tryToRemove(toLeave, height * width);
		while (!CANCEL && !success) {
			numbers = allNumbers;
			success = tryToRemove(toLeave, height * width);
		}
		return success;
	}

	private boolean tryToRemove(int toLeave, int toRemove) {
		if (CANCEL) return false;
		int [][] numbersCopy = copyList(numbers);

		createNumberList(numbers);

		int tryToRemove = Math.min(numb.size() / 2, numb.size() - toLeave);
		tryToRemove = Math.min(tryToRemove, toRemove);
		tryToRemove = Math.min(tryToRemove, 8);

		int numberLeft = numb.size();

		deleteUnremovables();	// verwijder onverwijderbare elementen uit de lijst,
								// zodat niet onnodig gecheckt wordt of ze verwijderbaar zijn.
		
		if (gui != null) {
			gui.updateProgressBar(height * width - numberLeft);
		}
		
		int numbSize = numb.size();

		if (numb.size() < tryToRemove) return false;

		for (int i=0; i<tryToRemove; i++) {
			boolean success = false;
			while (!success) {
				int index = (int) (Math.random() * numb.size());
				Getal g = numb.get(index);
				if (g.removable(width, height)) { // kan onverwijderbaar geworden zijn!
					g.remove();
					numbersCopy[g.getX()][g.getY()] = -1;
					numb.remove(index);
					success = true;
					deleteUnremovables();
					if (tryToRemove - i > numb.size()) {
						return false;
					}
				}
			}
		}

		createNumberList(numbersCopy);
		black = new Boolean[width][height];
		boolean solveable = solve(3);

		int nextToRemove;
		if (solveable) {
			numbers = numbersCopy;
			if (tryToRemove + toLeave == numberLeft) {
				return true;
			}
			nextToRemove = height * width; // zoveel mogelijk
		} else {
			if (tryToRemove == 0) return false;
			if (tryToRemove >= 64) {
				nextToRemove = tryToRemove / 4;
			} else {
				nextToRemove = tryToRemove / 2;
			}
		}

		return tryToRemove(toLeave, nextToRemove);
	}

	int [][] copyList(int [][] array) {
		int [][] result = new int [array.length][array[0].length];
		for (int i=0; i<array.length; i++) {
			for (int j=0; j<array[0].length; j++) {
				result [i][j] = array [i][j];
			}
		}
		return result;
	}

	public void cancel() {
		CANCEL = true;
	}

	private void updateBlack(Getal g) {
		int x = g.getX();
		int y = g.getY();
		Boolean [][] omg = g.getOmgeving();
		for (int i=(x>0 ? -1 : 0); i<(x<width-1 ? 2 : 1); i++) {
			for (int j=(y>0 ? -1 : 0); j<(y<height-1 ? 2 : 1); j++) {
				if (black[x+i][y+j] == null)
					black[x+i][y+j] = omg[i+1][j+1];
			}
		}
	}

	private void updateBlack2(Getal g) {
		int x = g.getX();
		int y = g.getY();
		Boolean [][] omg = g.getOmgeving();
		for (int i=(x>0 ? -1 : 0); i<(x<width-1 ? 2 : 1); i++) {
			for (int j=(y>0 ? -1 : 0); j<(y<height-1 ? 2 : 1); j++) {
				black[x+i][y+j] = omg[i+1][j+1];
			}
		}
	}

	public Boolean [][] getBlackResult() {
		return black.clone();
	}

	public int [][] getNumResult() {
		return copyList(numbers);
	}

	private int countNulls() {
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

	private void print (int [][] array) {
		for (int i=0; i<array[0].length; i++) {
			for (int j=0; j<array.length; j++) {
				if (array[j][i] < 0) {
					System.out.print(".");
				} else {
					System.out.print(array[j][i]);
				}
			}
			System.out.print(" ");
		}
		System.out.println();
	}

	private void print (Boolean [][] array) {
		for (int i=0; i<array[0].length; i++) {
			for (int j=0; j<array.length; j++) {
				if (array[j][i] == null) {
					System.out.print(".");
				} else {
					System.out.print(array[j][i] ? "1" : "0");
				}
			}
			System.out.print(" ");
		}
		System.out.println();
	}

	public void print(ArrayList al) {
		ListIterator li = al.listIterator();
		while (li.hasNext()) {
			System.out.println("- " + li.next());
		}
	}
}
