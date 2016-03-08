
public class Node {

	private String name;
	private int id;
	
	Node(int NodeId, String NodeName)
	{
		id = NodeId;
		name = NodeName;
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated Node stub
		return name.hashCode();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
//	public boolean equals(Object o) {
//		if (!(o instanceof Node)) return false;
//	    return (id==((Node)o).getId() );
//	}
	public boolean equals(Object o) {
		if (!(o instanceof Node)) return false;
	    return (name.equals(((Node)o).getName()) );
	}
	public String toString(){
		return name;
	}

	
	
	
	

}

