// Tim Havinga
// 23-6-2008

import javax.swing.filechooser.FileFilter;
import java.io.*;

class CsvFileFilter extends FileFilter {
	CsvFileFilter() {}

	public boolean accept (File f) {
		if (f != null) {
			if (f.getName().endsWith(".csv") || f.isDirectory()) return true;
		}
		return false;
	}

	public String getDescription() {
		return "Comma-separated files (*.csv)";
	}
}
