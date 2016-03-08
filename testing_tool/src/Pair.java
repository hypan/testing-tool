
public class Pair {
	
	public Node Node1;
	public Node Node2;
	/*
	Pair(Node m1, Node m2){
		Node1 = m1;
		Node2 = m2;
	}*/
	
	Pair(Node m1, Node m2){
		if(m1.getName().compareTo(m2.getName())<=0){
			Node1 = m1; Node2 = m2;
		}else{
			Node1 = m2; Node2 = m1;
		}
	}
	public Node getNode1() {
		return Node1;
	}
	public void setNode1(Node m1) {
		this.Node1 = m1;
	}
	public Node getNode2() {
		return Node2;
	}
	public void setNode2(Node m2) {
		this.Node2 = m2;
	}
	
	public String toString(){
		return Node1.getName()+" "+ Node2.getName();
		
	}
	@Override
	public int hashCode() {
		//return (this.getNode1().getId()+""+this.getNode2().getId()).hashCode();
		return this.Node1.hashCode()+this.Node2.hashCode();

	}
	
	public Node getTwin(Node m)
	{
		if (Node1.equals(m))
			return Node2;
		else
			return Node1;
	}
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof Pair))return false;
		Pair pair = (Pair)obj;
		return this.toString().equals(pair.toString());

	}

}

