package gui;
import javax.swing.*;
public class Console extends JScrollPane {

	public JTextArea text;

	public Console () {
		super ();
		text = new JTextArea ("Welcome to BetterSCAD\n");
		text.setEditable(false);
		text.setBounds (0,0,BetterSCAD.CONS_XS, BetterSCAD.CONS_YS);
		text.setLineWrap (true);
		setPreferredSize (new java.awt.Dimension (BetterSCAD.CONS_XS, BetterSCAD.CONS_YS));
		JViewport vp = new JViewport();
		vp.add (text);
		setViewport (vp);
	}

	public void append (String s) {
		text.append (s + "\n");
	}

	public void clear () {
		text.setText ("");
	}

}
