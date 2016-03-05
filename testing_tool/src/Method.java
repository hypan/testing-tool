
public class Method {

	private String name;
	private int id;
	
	Method(int methodId, String methodName)
	{
		id = methodId;
		name = methodName;
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
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
//		if (!(o instanceof Method)) return false;
//	    return (id==((Method)o).getId() );
//	}
	public boolean equals(Object o) {
		if (!(o instanceof Method)) return false;
	    return (name.equals(((Method)o).getName()) );
	}
	public String toString(){
		return name;
	}

	
	
	
	

}
