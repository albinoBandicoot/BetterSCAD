package gui;
import frontend.*;
import common.*;
import render.*;

import javax.swing.*;
import java.io.*;
import java.util.Scanner;
public class Etab extends JPanel {

	/* This class provides a place to store all things related to a certain file,
	 * as well as being the editor display */

	public Scene sc;
	public String name;
	public File  file;

	public Console cons;	// this doesn't go in the editor panel, but there's a separete one for each etab
	
	public JTextArea text;
	public boolean isCompiled = false;
	public boolean saved = true;

	public void render () {
		if (isCompiled) {
			sc.updateCameraSize (BetterSCAD.VIEW_XS, BetterSCAD.VIEW_YS);
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

		text = new JTextArea (fstr);
		text.setBounds(20,0,xs,ys);
		text.setLineWrap (true);

		add (text);
	}

	public void compile () {
		Tree t = new Parser (file, text.getText(), false).parse();
		Tree rt = Semantics.makeSymtables (t);
		Interpreter i = new Interpreter (t);
		sc = new Scene (i.run());
		isCompiled = true;

		render();
	}

	public void save () throws IOException {
		FileWriter fw = new FileWriter (file);
		BufferedWriter bw = new BufferedWriter (fw);
		String t = text.getText();
		bw.write (t, 0, t.length());
		bw.close();
		fw.close();
	}

}

