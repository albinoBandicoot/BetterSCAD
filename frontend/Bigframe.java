package frontend;
import java.util.*;

public class Bigframe {

	public Stack<Smallframe> stack;
	public Smallframe base;
	public String name;

	public Bigframe (String name) {
		this.name = name;
		stack = new Stack<Smallframe>();
		base = new Smallframe (name + "_base");
	}

	public void put (String name, Datum d) {
		if (stack.isEmpty ()) {
			base.entries.put (name, d);
		} else {
			stack.peek().entries.put (name, d);
		}
	}

	public Datum findShallow (String name) {
		return base.find (name);
	}

	public Datum findDeep (String name) {
		Datum d = new Undef ();
		int i = stack.size()-1;
		while (d.isUndef()) {
			if (i == -1) {
				return base.find (name);
			} else {
				d = stack.get(i).find(name);
			}
			i--;
		}
		return d;
	}

	public void print () {
		System.out.println ("/ " + name);
		if (stack.isEmpty()) {
			System.out.print ("\\ ");
		} else {
			System.out.print ("| ");
		}
		base.print();
		System.out.println();
		for (int i=0; i < stack.size(); i++) {
			if (i == stack.size() - 1) {
				System.out.print ("\\ ");
			} else {
				System.out.print ("| ");
			}
			stack.get(i).print();
			System.out.println();
		}
	}
}
