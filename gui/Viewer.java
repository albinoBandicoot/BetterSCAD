package gui;
import render.*;
import common.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Viewer extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

	public BufferedImage img;
	public int[] data;
	public int xs, ys;
	private long time;

	private double theta, phi;	// changes in theta and phi from last render
	private double scale = 1;
	private int xstart, ystart;
	private boolean dragging = false;

	public Viewer (int xs, int ys) {
		img = new BufferedImage (xs, ys, 1);
		data = new int[xs*ys];
		this.xs = xs;
		this.ys = ys;
	}

	public void paintComponent (Graphics g) {
		g.drawImage (img, 0, 0, null);
		g.drawString (time + "ms", 20, 20);
	}

	public void render () {
		Scene sc = BetterSCAD.current.sc;
		sc.rotateStuff (phi, theta);
		/*
		Float3 newdir = sc.cam.dir.axisRotate (sc.cam.right.normalize(), phi);
		newdir = newdir.axisRotate (sc.cam.up.normalize(), -theta);
		sc.cam = sc.cam.lookAt (new Float3(0,0,0), newdir);
		*/
		sc.cam.scale (scale);
		scale = 1;

		time = Raytrace.render (sc, data, xs, ys);
		img.setRGB (0, 0, xs, ys, data, 0, xs);
		repaint();
	}

	public void mousePressed (MouseEvent e) {
		System.out.println("MOUSE PRESSED");
		xstart = e.getX();
		ystart = e.getY();
		dragging = true;
	}

	public void mouseReleased (MouseEvent e) {
		dragging = false;
		theta = 0;
		phi = 0;
	}

	public void mouseClicked (MouseEvent e) {}
	public void mouseEntered (MouseEvent e) {}
	public void mouseExited (MouseEvent e) {}

	public void mouseDragged (MouseEvent e) {
		phi = (e.getY() - ystart) / Prefs.current.PHI_SCALE;
		theta = (e.getX() - xstart) / Prefs.current.THETA_SCALE;
		System.out.println ("MOUSE DRAGGED; phi = " + phi + "; theta = " + theta);
		ystart = e.getY();
		xstart = e.getX();
		render();
	}

	public void mouseMoved (MouseEvent e) {}

	public void mouseWheelMoved (MouseWheelEvent e) {
		int rot = e.getWheelRotation();
		scale *= Math.pow (Prefs.current.SCALE_BASE, rot);
		render();
	}

}
