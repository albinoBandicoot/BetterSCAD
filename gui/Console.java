package gui;
import javax.swing.*;
public class Console extends JScrollPane {

	public JTextArea text;

	public Console () {
		super ();
		text = new JTextArea ("Welcome to BetterSCAD");
		text.setEditable(false);
		text.setBounds (0,0,BetterSCAD.CONS_XS, BetterSCAD.CONS_YS);
		setPreferredSize (new java.awt.Dimension (BetterSCAD.CONS_XS, BetterSCAD.CONS_YS));
		JViewport vp = new JViewport();
		vp.add (text);
		setViewport (vp);
	}

	public void write (String s) {
		text.append (s);
	}

}
