package testing2;


public class Node {

	private String func;
	private int id;
	
	Node(int NodeId, String FuncName)
	{
		id = NodeId;
		func = FuncName;
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated Node stub
		return func.hashCode();
	}
	
	public String getFunc(){
		return func;
	}
	public void setFunc (String func){
		this.func = func;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Node)) return false;
	    return (func.equals(((Node)o).getFunc()) );
	}
	public String toString(){
		return func;
	}
}

