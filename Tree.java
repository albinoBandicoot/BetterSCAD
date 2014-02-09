import java.util.ArrayList;
public class Tree<T> {

	public Treetype type;
	public T data;
	public ArrayList<Tree> children;

	public Tree (Treetype type) {
		this.type = type;
		data = null;
		children = new ArrayList<Tree>();
	}

	public Tree (Treetype type, T data) {
		this.type = type;
		this.data = data;
		children = new ArrayList<Tree> ();
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
