package frontend;

import java.util.ArrayList;
import java.io.*;
import common.*;

public class Test {

	public static void main (String[] args) {
		File inp = new File (args[0]);
		Tree t = new Parser (inp).parse();
		System.out.println(t);
		Semantics.makeSymtables (t);
		System.out.println ("Semantics has finished");
		System.out.println(t);
		Interpreter i = new Interpreter (t);
		Node n = i.run ();
		System.out.println ("Execution complete; CSG tree is \n" + n);
	}
}
