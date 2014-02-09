import java.util.ArrayList;
public class Tree {

	public Treetype type;
	public Object data;
	public ArrayList<Tree> children;
	public Tree parent;
	public STSet st;	// will only be used at nodes that need it.

	public Tree (Treetype type) {
		this.type = type;
		data = null;
		children = new ArrayList<Tree>();
		parent = null;
	}

	public Tree (Treetype type, Object data) {
		this.type = type;
		this.data = data;
		children = new ArrayList<Tree> ();
		parent = null;
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

	public STSet findPST () {
		if (parent == null) return null;
		if (parent.st != null) return parent.st;
		return parent.findPST();
	}
	
	public void createST () {
		st = new STSet (findPST());
	}

	public String toString () {
		return toString (0);
	}

	private String toString (int depth) {
		StringBuilder sb = new StringBuilder ();
		for (int i=0; i<depth; i++) {
			sb.append ("   ");
		}
		sb.append (type.name() + "  ");
		if (data != null) {
			if (data instanceof ArrayList) {
				for (Object o : ((ArrayList) data)) {
					sb.append (o + ", ");
				}
			} else {
				sb.append (data);
			}
		}
		sb.append ("\n");
		for (Tree t : children) {
			sb.append (t.toString (depth+1));
		}
		return sb.toString();
	}
}
