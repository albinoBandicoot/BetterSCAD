package frontend;

import java.util.ArrayList;
public class Tree {

	public Treetype type;
	public Object data;
	public ArrayList<Tree> children;
	public Tree parent;
	public STSet st;	// will only be used at nodes that need it.
	public int id;
	public int nest_depth;	// for module and function definitions.
	public FileMark fm;
	public int idata;	// arbitrary piece of integer data. Currently only used for predefined functions' argument counts.

	private static int num = 0;

	public Tree (Treetype type) {
		this.type = type;
		data =null;
		children = new ArrayList<Tree>();
		parent = null;
		id = num++;
		fm = new FileMark (0,0,0);
	}

	public Tree (Treetype type, FileMark fm) {
		this(type);
		this.fm = fm;
	}

	public Tree (Treetype type, Object data, FileMark fm) {
		this.type = type;
		this.data = data;
		children = new ArrayList<Tree> ();
		parent = null;
		id = num++;
		this.fm = fm;
	}

	public String name () {	// since the data is so often used to encode a name, why not have a convenience method for it?
		if (data instanceof String) {
			return (String) data;
		} else {
			return type.toString();	// next best thing, I guess. Could also return null, but this is probably better.
		}
	}

	public void addChild (Tree t) {
		children.add (t);
		t.parent = this;
	}

	public void addChildAll (ArrayList<Tree> t) {
		for (Tree tr : t) {
			children.add (tr);
			tr.parent = this;
		}
	}

	public STSet findPST () {	// start searching with the parent to find a symbol table
		if (parent == null) return null;
		if (parent.st != null) return parent.st;
		return parent.findPST();
	}

	public STSet findST () {	// start searching at the current node to find a symbol table
		if (st != null) return st;
		return findPST();
	}
	
	public void createST () {
		st = new STSet (findPST(), this);
	}

	public String toString () {
		return toString (0);
	}

	private String toString (int depth) {
		StringBuilder sb = new StringBuilder ();
		for (int i=0; i<depth; i++) {
			sb.append ("   ");
		}
		sb.append ("(" + id + ") " + type.name() + "  nest= " + nest_depth + "   ");
		if (data != null) {
			if (data instanceof ArrayList) {
				for (Object o : ((ArrayList) data)) {
					sb.append (o + ", ");
				}
			} else {
				sb.append (data);
			}
		}
		if (st != null) {
			sb.append ("   " + st);
		}
		sb.append ("\n");
		for (Tree t : children) {
			sb.append (t.toString (depth+1));
		}
		return sb.toString();
	}
}
