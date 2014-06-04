package gui;
import render.*;
import common.*;
import javax.swing.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Viewer extends JPanel {

	public BufferedImage img;
	public int[] data;
	public int xs, ys;
	private long time;

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
		time = Raytrace.render (BetterSCAD.current.sc, data, xs, ys);
		img.setRGB (0, 0, xs, ys, data, 0, xs);
		repaint();
	}

}
