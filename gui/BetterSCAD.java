package gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.util.ArrayList;
import java.io.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
public class BetterSCAD extends JFrame implements ActionListener, ComponentListener, PropertyChangeListener  {

	public static ArrayList<Etab> tabs = new ArrayList<Etab>();
	public static Etab current = null;

	public Editor edit;
	public JScrollPane scroller;
	public static Viewer view;
	public static JSplitPane horiz, vert;

	public static JFileChooser fchooser = new JFileChooser();

	public JMenuBar mbar;
	public JMenu file;
	public JMenuItem file_new;
	public JMenuItem file_open;
	public JMenuItem file_save;
	public JMenuItem file_save_as;
	public JMenuItem file_save_all;
	public JMenuItem file_quit;

	public JMenu model;
	public JMenuItem model_compile;
	public JMenuItem model_slice;

	public static int FR_XS = 1080;
	public static int FR_YS = 660;
	public static int fr_real_ys = 720;

	public static int EDIT_XS = 600;
	public static int EDIT_YS = FR_YS;

	public static int VIEW_XS = FR_XS - EDIT_XS;
	public static int VIEW_YS = 500;

	public static int CONS_XS = VIEW_XS;
	public static int CONS_YS = FR_YS - VIEW_YS;

	public BetterSCAD () {
		super ("BetterSCAD");
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		JPanel content = new JPanel ();
		content.setOpaque (true);
		content.setLayout(null);

		edit = new Editor ();
		edit.setBounds(0,0,EDIT_XS,EDIT_YS);
		edit.setPreferredSize (new Dimension (EDIT_XS, EDIT_YS));

		view = new Viewer (VIEW_XS, VIEW_YS);
		view.setBounds(0,0,VIEW_XS,VIEW_YS);
		view.addMouseListener (view);
		view.addMouseMotionListener (view);
		view.addMouseWheelListener (view);

		vert = new JSplitPane (JSplitPane.VERTICAL_SPLIT, view, null);
		vert.setBounds (EDIT_XS, 0, VIEW_XS, FR_YS);
		vert.setDividerLocation (VIEW_YS);

		vert.addPropertyChangeListener (JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

		horiz = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, edit, vert);
		horiz.setBounds (0, 0, FR_XS, FR_YS);
		horiz.setDividerLocation (EDIT_XS);

		horiz.addPropertyChangeListener (JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

		content.add (horiz);

		/* Menu stuff */
		mbar = new JMenuBar ();
		file = new JMenu ("File");
		model = new JMenu ("Model");

		file_new = new JMenuItem ("New");
		file_open = new JMenuItem ("Open");
		file_save = new JMenuItem ("Save");
		file_save_as  = new JMenuItem ("Save as");
		file_save_all = new JMenuItem ("Save all tabs");
		file_quit = new JMenuItem ("Quit");

		file_new.addActionListener (this);
		file_open.addActionListener (this);
		file_save.addActionListener (this);
		file_save_as.addActionListener (this);
		file_save_all.addActionListener (this);
		file_quit.addActionListener (this);

		file_new.setActionCommand ("new");
		file_open.setActionCommand ("open");
		file_save.setActionCommand ("save");
		file_save_as.setActionCommand ("saveas");
		file_save_all.setActionCommand ("saveall");
		file_quit.setActionCommand ("quit");

		file.add (file_new);
		file.add (file_open);
		file.add (file_save);
		file.add (file_save_as);
		file.add (file_save_all);
		file.add (file_quit);

		model_compile = new JMenuItem ("Compile");
		model_slice = new JMenuItem ("Slice");
		
		model_compile.addActionListener(this);
		model_slice.addActionListener(this);

		model_compile.setActionCommand ("compile");
		model_slice.setActionCommand ("slice");

		model.add (model_compile);
		model.add (model_slice);

		mbar.add (file);
		mbar.add (model);

		setJMenuBar (mbar);
		setContentPane (content);
		pack();
		setSize (FR_XS+10, fr_real_ys);
		setVisible (true);
	}

	public static void main (String[] args) {
		BetterSCAD b = new BetterSCAD ();
	}

	public void openFile (File f) {
		Etab e = new Etab (f, EDIT_XS-20, EDIT_YS);
		tabs.add(e);
		JViewport vp = new JViewport();
		vp.add(e);
		edit.setViewport (vp);
//		edit.addTab (e.name, e);
		switchTab (e);
	}

	public void closeFile (Etab e) {
		if (e.saved) {
		} else {
		}
	}

	public void switchTab (Etab e) {
		current = e;
//		edit.setSelectedComponent (e);
		vert.setBottomComponent (e.cons);
		vert.setDividerLocation (VIEW_YS);
		e.render();
	}

	public void saveas () {
		int resp = fchooser.showSaveDialog (this);
		if (resp == JFileChooser.APPROVE_OPTION) {
			File out = fchooser.getSelectedFile();
			if (current.file == null) {
				current.file = out;
			}
			try {
				current.save();
			} catch (IOException e) {
				// do something
			}
		}
	}

	public void actionPerformed (ActionEvent e) {
		String c = e.getActionCommand();
		if (c.equals("new")) {
			openFile (null);
		} else if (c.equals ("open")) {
			int resp = fchooser.showOpenDialog (this);
			if (resp == JFileChooser.APPROVE_OPTION) {
				openFile (fchooser.getSelectedFile());
			}
		} else if (c.equals ("save")) {
			if (current.file == null) {
				saveas();
			} else {
				try {
					current.save();
				} catch (IOException ex) {
					// do something
				}
			}
		} else if (c.equals ("saveas")) {
			saveas();
		} else if (c.equals ("saveall")) {
			for (Etab et : tabs) {
				if (et.file == null) {
					saveas();
				} else {
					try {
						et.save();
					} catch (IOException ex) {
						// do something
					}
				}
			}
		} else if (c.equals ("compile")) {
			current.compile();
		} else if (c.equals ("slice")) {
		} else if (c.equals ("quit")) {
			System.exit(0);
		}
	}

	public void componentHidden  (ComponentEvent e) { }
	public void componentShown   (ComponentEvent e) { }
	public void componentMoved   (ComponentEvent e) { }
	public void componentResized (ComponentEvent e) {
		// do stuff
	}

	public void propertyChange (PropertyChangeEvent e) {
		if (e.getSource() == horiz) {
			int hdiv = horiz.getDividerLocation();
			System.out.println ("PCE fired; horiz divider position is now " + hdiv);
			EDIT_XS = hdiv;
			VIEW_XS = FR_XS - EDIT_XS;
			CONS_XS = VIEW_XS;
			view.changeSize (VIEW_XS, VIEW_YS);
			current.render();

		} else if (e.getSource() == vert) {
			int vdiv = vert.getDividerLocation();
			VIEW_YS = vdiv;
			CONS_YS = FR_YS - VIEW_YS;
			view.changeSize (VIEW_XS, VIEW_YS);
			current.render();
		}
	}

}
