// Tim Havinga
// 23-6-2008

import javax.swing.filechooser.FileFilter;
import java.io.*;
import javax.imageio.ImageIO;

class ImageFileFilter extends FileFilter {
	String [] availableTypes;

	ImageFileFilter() {
		availableTypes = ImageIO.getReaderFormatNames();
	}

	public boolean accept (File f) {
		if (f != null) {
			if (f.isDirectory()) return true;
			for (int i=0; i<availableTypes.length; i++) {
				if (f.getName().endsWith(availableTypes[i]))
					return true;
			}
		}
		return false;
	}

	public String getDescription() {
		String images = "Image types (";
		for (int i=0; i<availableTypes.length; i++) {
			images += availableTypes[i] + (i < availableTypes.length - 1 ? ", " : "");
		}
		return images + ")";
	}
}
