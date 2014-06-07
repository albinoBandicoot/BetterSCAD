package gui;
import frontend.*;
import common.*;
import render.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;
public class Etab extends JTextArea { 

	/* This class provides a place to store all things related to a certain file,
	 * as well as being the editor display */

	public Scene sc;
	public String name;
	public File  file;

	public Console cons;	// this doesn't go in the editor panel, but there's a separete one for each etab
	
	public boolean isCompiled = false;
	public boolean saved = true;

	public void render () {
		if (isCompiled) {
			int ds = Prefs.current.DOWNSAMPLING;
			sc.updateCameraSize (BetterSCAD.VIEW_XS / ds, BetterSCAD.VIEW_YS / ds);
			BetterSCAD.view.render();
		} else {
		}
	}

	public Etab (File f, int xs, int ys) {
		super ();
		file = f;
		if (f == null) {
			name = "Untitled";
		} else {
			name = f.getName();
		}
		String fstr = "";
		if (f != null) {
			try {
				fstr = new Scanner (f).useDelimiter("\\Z").next();
			} catch (IOException e) {
			}
		}

		setLocation (0, 0);
		setLineWrap (true);
		setTabSize (Prefs.current.EDITOR_TAB_SIZE);
		setText (fstr);
		setFont (Prefs.current.EDITOR_FONT);

		cons = new Console();

	}

	public void compile () {
		cons.clear();
		try {
			Tree t = new Parser (file, getText(), false).parse();
			Tree rt = Semantics.makeSymtables (t);
			System.out.println ("---- TREE IS -----");
			System.out.println (rt + "\n");

			Interpreter i = new Interpreter (t);
			sc = new Scene (i.run());
			isCompiled = true;

			render();
		} catch (ParseException pe) {
			cons.append (pe.getMessage());
			setCaretPosition (pe.fm.idx);
		} catch (SemanticException se) {
			cons.append (se.getMessage());
			if (se.fm != null) {
				setCaretPosition (se.fm.idx);
			}
		} catch (RTException rte) {
			cons.append (rte.getMessage());
			if (rte.fm != null) {
				setCaretPosition (rte.fm.idx);
			}
		}
	}

	public void save () throws IOException {
		FileWriter fw = new FileWriter (file);
		BufferedWriter bw = new BufferedWriter (fw);
		String t = getText();
		bw.write (t, 0, t.length());
		bw.close();
		fw.close();
	}

}

