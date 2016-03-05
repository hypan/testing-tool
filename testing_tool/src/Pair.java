
public class Pair {
	
	private Method method1;
	private Method method2;
	/*
	Pair(Method m1, Method m2){
		method1 = m1;
		method2 = m2;
	}*/
	
	Pair(Method m1, Method m2){
		if(m1.getName().compareTo(m2.getName())<=0){
			method1 = m1; method2 = m2;
		}else{
			method1 = m2; method2 = m1;
		}
	}
	public Method getMethod1() {
		return method1;
	}
	public void setMethod1(Method m1) {
		this.method1 = m1;
	}
	public Method getMethod2() {
		return method2;
	}
	public void setMethod2(Method m2) {
		this.method2 = m2;
	}
	
	public String toString(){
		return method1.getName()+" "+ method2.getName();
		
	}
	@Override
	public int hashCode() {
		//return (this.getMethod1().getId()+""+this.getMethod2().getId()).hashCode();
		return this.method1.hashCode()+this.method2.hashCode();

	}
	
	public Method getTwin(Method m)
	{
		if (method1.equals(m))
			return method2;
		else
			return method1;
	}
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof Pair))return false;
		Pair pair = (Pair)obj;
		return this.toString().equals(pair.toString());

	}

}
