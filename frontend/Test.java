package frontend;

import java.util.ArrayList;
import java.io.*;
import common.*;
import render.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Test {

	public static void main (String[] args) throws IOException {
		File inp = new File (args[0]);
		Tree t = new Parser (inp, "", true).parse();
		System.out.println(t);
		Tree rt = Semantics.makeSymtables (t);
		System.out.println ("Semantics has finished");
		System.out.println(rt);
		Interpreter i = new Interpreter (t);
		Node n = i.run ();
		System.out.println ("Execution complete; CSG tree is \n" + n);
		/*
		Scene s = new Scene (n);
		BufferedImage img = new BufferedImage (200, 200, 1);
		Raytrace.render (s, img);
		ImageIO.write (img, "png", new File (args[1]));
		*/
	}
}
