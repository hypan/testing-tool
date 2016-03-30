

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {

	 private int T_SUPPORT;
	    private float T_CONFIDENCE;
	    public Node caller = null;
	    public Node callee = null;

	    private final String call_gra = "Call graph";
	    private final String callee_start = "calls function";
	    private final String null_func = "null function";
	    int expandLevel=3;

	    HashSet<Node> Nodes = new HashSet<Node>();
	    HashMap<Node, HashSet<Node>> callMap = new HashMap<Node, HashSet<Node>>();
	    HashMap<Node, Integer> NodeSupport = new HashMap<Node, Integer>();
	    HashMap<Pair, HashSet<Node>> pairMap = new HashMap<Pair, HashSet<Node>>();
	    HashMap<Pair, Integer> pairSupport = new HashMap<Pair, Integer>();
	    HashMap<Node,HashSet<Node>> expandedCallMap=new HashMap<Node,HashSet<Node>>();
	   

	    public Pair ps = null;
	    public int a = 0;     // Used for test, should be deleted finally.
	    public Node small = null;
	    public Node large = null;
	    boolean nullFunc = false;

	    public static void main(String[] args) {
	        test test = new test();
	        test.setInput(args);
//	        String fileName = "/Users/haoyuepan/Documents/netbean/call2";
//	        String fileName = "/Users/yananchen/Downloads/test3.txt";
	        test.readFileByLines();
            test.saveExpandedCallMap();
	        test.chooseThrePair();
	        test.findBugs();
	    }

	    public String callgraph;
	     private void setInput(String[] args) {
	        callgraph = args[0];
	      //  System.out.println(fileName);
	        switch (args.length) {
	            case 3:
	                T_SUPPORT = Integer.parseInt(args[1]);
	                T_CONFIDENCE = Integer.parseInt(args[2]);
	                break;
	            default:
	                T_SUPPORT = 3;
	                T_CONFIDENCE = 65;
	                break;
	        }
	    }

	    public void readFileByLines() {
	      String tempString = null;
	        try {

	            BufferedReader reader = new BufferedReader(new FileReader(callgraph));
	            while ((tempString = reader.readLine()) != null) {
	            saveGraph(tempString);
	            }
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }


	    //parse Call Graph lines and save Call Graph to Hashmap/HashSet
	    public void saveGraph(String graphLine) {

	        if (graphLine.trim().startsWith(call_gra)) {
	            if (!graphLine.contains(null_func)) {
	                nullFunc = true;
	                String currentCaller = getFuncName(graphLine);
	                caller = new Node(currentCaller.hashCode(), currentCaller);
	                addNode(caller);
	            } else {
	                nullFunc = false;
	            }
	        } else if (nullFunc
	                && graphLine.contains(callee_start)) {

	            String currentCallee = getFuncName(graphLine); // find func name;
	            callee = new Node(currentCallee.hashCode(), currentCallee);
	            addNode(callee);
	            saveCallMap(caller, callee);
	        }
	    }

	    // Get function names from each line
	    public String getFuncName(String line) {

	        String regEx1 = "'(.+)'"; // string within ' '
	        Pattern p = Pattern.compile(regEx1);
	        Matcher m = p.matcher(line);
	        String result = null;
	        while (m.find()) {
	            result = m.group(1);
	        }
	        String funcName = result;
	        return funcName;
	    }

	    public void addNode(Node m) {
	        if (!Nodes.contains(m)) {
	            Nodes.add(m);
	            NodeSupport.put(m, 0);
	        }
	    }

	    public void saveCallMap(Node caller, Node callee) {
	        if (!callMap.containsKey(caller)){
	            HashSet<Node> set = new HashSet<Node>();
	            set.add(callee);
	            callMap.put(caller,set);
	            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
	        }else if(!callMap.get(caller).contains(callee)){
	            callMap.get(caller).add(callee);
	            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
	            createNodePair(callMap.get(caller), callee); // pair support
	        }
	    }
	    
	    
	    public void saveExpandedCallMap(){
	          
//	        HashSet<Node> calleeSet=new  HashSet<Node>();       
	        expandedCallMap = new HashMap<Node, HashSet<Node>>(callMap);
		        for (int i = 0; i < expandLevel; i++) {
		            HashMap<Node, HashSet<Node>> tempCallMap = new HashMap<Node, HashSet<Node>>();

		            for (Node scope : expandedCallMap.keySet()) 
		            {
		            	HashSet<Node> calleeSet = new HashSet<Node>(expandedCallMap.get(scope));
		                for (Node curfun : expandedCallMap.get(scope))
		                {
		                    if (expandedCallMap.containsKey(curfun)) 
		                    {
	                               //scope_set.remove(curfun);
		                        for (Node callee2 : expandedCallMap.get(curfun))  
		                        {
		                            if (!calleeSet.contains(callee2)) {
		                            	calleeSet.add(callee2);
		                            }
		                        }
		                    }
		                }
		                tempCallMap.put(scope, calleeSet);
		            }
		            expandedCallMap = new HashMap<Node, HashSet<Node>>(tempCallMap);

		        }
	                //System.out.println("expandedCallMap"+expandedCallMap);
	    }
	    

	    public void createNodePair(HashSet<Node> set, Node callee) {
	      
	        for (Iterator<Node> iter = set.iterator(); iter.hasNext();) {
	            Node key = (Node) iter.next();
	            if (key != callee) {
	                 Pair ps = new Pair(callee, key);
	                 savePairs(ps);
	            }
	        }
	    }

	    public void savePairs(Pair ps) {
	        if (!pairMap.containsKey(ps)) {
	            HashSet<Node> temp = new HashSet<Node>();
	            temp.add(caller);
	            pairMap.put(ps, temp);
	        } else {
	            pairMap.get(ps).add(caller);
	        }
	    }

	    public void chooseThrePair() {
	        Set<Pair> set = pairMap.keySet();
	        for (Iterator<Pair> iter = set.iterator(); iter.hasNext();) {
	            Pair key = (Pair) iter.next();
	            int value = pairMap.get(key).size();
	            if (value >= T_SUPPORT) {
	                pairSupport.put(key, value);
	            }
	        }
	    }

	    public void findBugs() {
	        
	        Iterator<Pair> key = pairSupport.keySet().iterator();
	        int supportPair;
	        
	        while (key.hasNext()) {
	            Pair pair = key.next();
	            supportPair = pairSupport.get(pair);
	            if (supportPair >= T_SUPPORT) {
	                Node left = pair.getNode1();
	                findBugs(left, pair, supportPair);
	                Node right = pair.getNode2();
	                findBugs(right, pair, supportPair);
	            }
	        }
	    }
	    
	    public void findBugs(Node function, Pair pair, int supportPair) {
	        
	        Set<Node> cont = callMap.keySet();
	        int supportFunc = NodeSupport.get(function);
	        double tempConf = (double)supportPair / (double)supportFunc * 100;
	        if (tempConf >= T_CONFIDENCE) {
	            for (Iterator<Node> iter = cont.iterator(); iter.hasNext();) {
	                Node key = (Node) iter.next();
	                HashSet<Node> Nodes = callMap.get(key);
	                HashSet<Node> expandedNodes=expandedCallMap.get(key);
	                Node left = pair.getNode1();
	                Node right = pair.getNode2();
	                Node otherFunc = null;
	                if(function==left){
	                    otherFunc = pair.getNode2();
	                }else if(function==right){
	                    otherFunc = pair.getNode1();
	                }
	                //                System.out.println(otherFunc);
	                if (Nodes.contains(function) && !Nodes.contains(otherFunc)) {
	                	if(expandedNodes.contains(function)&&!expandedNodes.contains(otherFunc)){
	                		a++;//used for test, should be deleted finally
	                    
	                    System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n",
	                                      function,
	                                      key.toString(),
	                                      pair.getNode1().toString(),
	                                      pair.getNode2().toString(),
	                                      supportPair,
	                                      tempConf
	                                      );
	                	}
	                    
	                }
	            }
	        }
	        
	    }

}

class Node {
    
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

class Pair {
    
    public Node Node1;
    public Node Node2;
    /*
     Pair(Node m1, Node m2){
     Node1 = m1;
     Node2 = m2;
     }*/
    
    Pair(Node m1, Node m2){
        if(m1.getFunc().compareTo(m2.getFunc())<=0){
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
        return Node1.getFunc()+","+ Node2.getFunc();
        
    }
    @Override
    public int hashCode() {
        //return (this.getNode1().getId()+""+this.getNode2().getId()).hashCode();
        return this.Node1.hashCode()+this.Node2.hashCode();
        
    }
    
    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof Pair))return false;
        Pair pair = (Pair)obj;
        return this.toString().equals(pair.toString());
        
    }
    
}



