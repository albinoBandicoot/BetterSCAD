package frontend;

import java.util.ArrayList;
import java.io.*;

public class Test {

	public static void main (String[] args) {
		File inp = new File (args[0]);
		Tree t = new Parser (inp).parse();
		System.out.println(t);
		Semantics.makeSymtables (t);
		System.out.println ("Semantics has finished");
		System.out.println(t);
		System.out.println ("We're done; goodbye.");

	}
}