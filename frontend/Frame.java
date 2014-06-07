package frontend;
import java.util.*;
public class Frame {

	private static int NUM = 0;

	public ListMap <String, Datum> entries;
	public String name;	// this is really just for debugging purposes
	public Tree tr;		// tree corresponding to the definition of the function or module or block this frame represents.
	public Frame static_link;
	public int id;

	public Frame (String name, Frame sl, Tree tr) {
		this.id = NUM++;
		this.name = name;
		this.static_link = sl;
		this.tr = tr;
		entries = new ListMap <String, Datum>();
	}

	public Datum find (String name) {
		return entries.get(name);
	}

	public void put (String name, Datum val) {
		entries.put (name, val);
	}

	public void print () {
		System.out.print ("[" + tr.nest_depth + "] " + id + "(" + name + ") --> (" + (static_link == null ? "null" : static_link.id) + "):");
		for (ListMap.Entry e : entries.entrySet()) {
			System.out.print (e.getKey() + " = " + e.getValue() + ", ");
		}
		System.out.println();
	}


}
	
