import java.util.ArrayList;
import java.util.ListIterator;

class Getal {
	final int xx;
	final int yy;
	protected int getal;
	protected int bekend;
	protected int gevuld;
	protected Boolean[][] omgeving;

	protected ArrayList<Getal> buren;

	Getal (int x, int y, int g) {
		xx = x;
		yy = y;
		getal = g;
		bekend = 0;
		gevuld = 0;
		buren = new ArrayList<Getal>();
		omgeving = new Boolean[3][3];
		if (x == 0) {
			setValue(-1,-1,false);
			setValue(-1,0,false);
			setValue(-1,1,false);
		}
		if (y == 0) {
			setValue(-1,-1,false);
			setValue(0,-1,false);
			setValue(1,-1,false);
		}
	}

	public int getX() {
		return xx;
	}

	public int getY() {
		return yy;
	}

	public int getGetal() {
		return getal;
	}

	public int getBekend() {
		return bekend;
	}

	public int getGevuld() {
		return gevuld;
	}

	public Boolean[][] getOmgeving() {
		return omgeving;
	}

	public int [] getNulls() {
		int [] nulls = new int [0];
		for (int i=0; i<3; i++) {
			for (int j=0; j<3; j++) {
				if (omgeving[i][j] == null) {
					int l = nulls.length;
					nulls = (int []) resizeArray(nulls, l+2);
					nulls[l]   = xx - 1 + i;
					nulls[l+1] = yy - 1 + j;
				}
			}
		}
		return nulls;
	}

	public void addNeighbour(Getal g) {
		buren.add(g);
	}

	public ListIterator getNeighbours() {
		return buren.listIterator();
	}

	public boolean isSolveable() {
		return (gevuld == getal || (9-bekend + gevuld) == getal);
	}

	public boolean isSolved() {
		return bekend == 9;
	}

	public void solve() {
		if (bekend == 9) return;
		Boolean actie = null;
		if (gevuld == getal) {
			actie = false;
		}
		else if (9 - bekend + gevuld == getal) {
			actie = true;
		}
		if (actie != null) {
			for (int i=0; i<3; i++) {
				for (int j=0; j<3; j++) {
					setValue(i-1, j-1, actie);
				}
			}
		}
	}

	public boolean checkWithNeighbours(int level) {
		boolean change = false;
		ListIterator it = buren.listIterator();
		while (it.hasNext()) {
			Getal g = (Getal) it.next();
			if (level > 2 ||
				(Math.abs(g.getX() - xx) <= 2 && g.getY() == yy) ||
				(Math.abs(g.getY() - yy) <= 2 && g.getX() == xx)) {
				boolean allIn = true;
				int [] nulls = g.getNulls();
				int overlap = 0;
				for (int i=0; i<nulls.length/2; i++) {
					if (Math.abs(nulls[i*2] - xx) < 2 && Math.abs(nulls[i*2+1] - yy) < 2) {
						overlap++;
					} else {
						allIn = false;
					}
				}
				int teVullen = g.getGetal() - g.getGevuld();
				Boolean actie = null;
				if (allIn) {
					if (getal - gevuld == 9 - bekend - nulls.length/2 + teVullen)
						actie = true;
					if (getal - gevuld == teVullen)
						actie = false;
				} else {
					if (getal - gevuld == 9 - bekend - overlap + teVullen)
						actie = true;
					if (getal - gevuld == teVullen - (nulls.length/2 - overlap))
						actie = false;
				}
				if (actie != null) {
					boolean [][] overlay = toBoolOverlay(nulls);
					for (int i=0; i<3; i++) {
						for (int j=0; j<3; j++) {
							if (omgeving[i][j] == null && !overlay[i][j]) {
								setValue(i-1, j-1, actie);
								change = true;
								System.out.println("< " + g);
							}
						}
					}
					if (change) return change;
				}
			}
		}
		return change;
	}

	private boolean [][] toBoolOverlay(int [] other) {
		boolean [][] overlay = new boolean[3][3];
		for (int i=0; i<other.length/2; i++) {
			if (Math.abs(other[i*2] - xx) < 2 &&
				Math.abs(other[i*2+1] - yy) < 2)
				overlay[other[i*2] - xx + 1][other[i*2+1] - yy + 1] = true;
		}
		return overlay;
	}

	// x en y zitten in het bereik [-1, 0, 1]
	public void setValue(int x, int y, Boolean value) {
		boolean modified = false;
		if (value != null) {
			if (omgeving[x+1][y+1] == null) {
				omgeving[x+1][y+1] = value;
				bekend ++;
				if (value) gevuld ++;
				modified = true;
			}
		} else {
			if (omgeving[x+1][y+1] != null) {
				if (omgeving[x+1][y+1]) gevuld --;
				omgeving[x+1][y+1] = value;
				bekend --;
				modified = true;
			}
		}
		if (modified) {
			ListIterator it = buren.listIterator();
			while (it.hasNext()) {
				Getal g = (Getal) it.next();
				setValue(g, x, y, value);
			}
		}
	}

	public void setValue(Getal g, int x, int y, Boolean value) {
		int dx = g.getX() - xx;
		int dy = g.getY() - yy;
		if (Math.abs(dx - x) <= 1 && Math.abs(dy - y) <= 1) {
			g.setValue(x - dx, y - dy, value);
		}
	}

	public boolean removable(int width, int height) {
		boolean [][] known = new boolean [3][3];
		for (int i=0; i<3; i++) {
			if (xx == 0) known[0][i] = true;
			if (yy == 0) known[i][0] = true;
			if (xx == width-1)  known[2][i] = true;
			if (yy == height-1) known[i][2] = true;
		}
		/*for (int i=0; i<3; i++) {
			for (int j=0; j<3; j++) {
				if (known[i][j] == null) {
					known[i][j] = false;
				}
			}
		}*/

		ListIterator it = buren.listIterator();
		while (it.hasNext()) {
			Getal g = (Getal) it.next();
			int x = g.getX();
			int y = g.getY();
			for (int i=0; i<3; i++) {
				for (int j=0; j<3; j++) {
					if (Math.abs(x-(i-1)-xx) < 2 && Math.abs(y-(j-1)-yy) < 2) {
						known[i][j] = true;
					}
				}
			}
		}
		boolean removable = true;
		for (int i=0; i<3; i++) {
			for (int j=0; j<3; j++) {
				removable = removable && known[i][j];
			}
		}
		return removable;
	}

	public void remove() {
		ListIterator it = buren.listIterator();
		while (it.hasNext()) {
			Getal g = (Getal) it.next();
			g.remove(this);
		}
	}

	public void remove(Getal g) {
		int index = buren.indexOf(g);
		if (index >= 0)
			buren.remove(index);
	}

	public void reset(int width, int height) {
		bekend = 0;
		gevuld = 0;
		omgeving = new Boolean[3][3];
		if (xx == 0) {
			setValue(-1,-1,false);
			setValue(-1,0,false);
			setValue(-1,1,false);
		}
		if (yy == 0) {
			setValue(-1,-1,false);
			setValue(0,-1,false);
			setValue(1,-1,false);
		}
		if (xx == width-1) {
			setValue(1,-1,false);
			setValue(1,0,false);
			setValue(1,1,false);
		}
		if (yy == height-1) {
			setValue(-1,1,false);
			setValue(0,1,false);
			setValue(1,1,false);
		}
	}

	public String toString() {
		String s = "";
		s += "(" + xx + ", " + yy + "): " + getal + " b: " + bekend + ", g: " + gevuld + "\t";
		for (int h=0; h<3; h++) {
			for (int w=0; w<3; w++) {
				s += (omgeving[w][h] == null ? "." : (omgeving[w][h] ? "1" : "0"));
			}
			if (h<2) s += " ";
		}
		return s;
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

}
