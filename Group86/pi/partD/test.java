import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {

    private int T_SUPPORT = 3;
    private float T_CONFIDENCE = 65;
    public Node caller = null;
    public Node callee = null;
    
    private final String call_gra = "Call graph";
    private final String callee_start = "calls function";
    private final String null_func = "null function";

    HashSet<Node> Nodes = new HashSet<Node>();
    HashMap<Node, HashSet<Node>> callMap = new HashMap<Node, HashSet<Node>>();
    HashMap<Node, HashSet<Node>> calleeMap = new HashMap<Node, HashSet<Node>>();
    HashMap<Node, ArrayList<Node>> orderCallMap = new HashMap<Node, ArrayList<Node>>();
    HashMap<Node, Integer> NodeSupport = new HashMap<Node, Integer>();
    HashMap<Pair, HashSet<Node>> orderPairMap = new HashMap<Pair, HashSet<Node>>();
    HashMap<Pair, Integer> orderPairSupport = new HashMap<Pair, Integer>();

    public Pair ps = null;
    public Node small = null;
    public Node large = null;
    boolean nullFunc = false;

    public static void main(String[] args) {
        test test = new test();
        test.setInput(args);
        test.readFileByLines();
        test.createOrderPair();
        test.chooseThreOrderPair();
        test.saveConfidenceMap();
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

    public int Num;
    
    public void saveCallMap(Node caller, Node callee) {
        
        ArrayList<Node> orderCallee = new ArrayList<Node>();
        if (!callMap.containsKey(caller)) {
            Num = 0;
            HashSet<Node> set = new HashSet<Node>();
            set.add(callee);
            callMap.put(caller, set);
            orderCallee.add(callee);
            
            Num++;
            orderCallMap.put(caller, orderCallee);
            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
        } else if (!callMap.get(caller).contains(callee)) {
            
            callMap.get(caller).add(callee);
            orderCallMap.get(caller).add(callee);
            
            Num++;
            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
            
        } else {
            orderCallMap.get(caller).add(callee);
        }
    }

    // creat and save pair that considered order according to orderCallMap
    public void createOrderPair() {
        for (Node key : orderCallMap.keySet()) {
            ArrayList map = orderCallMap.get(key);
            //            Set<Integer> indexSet = map.keySet();
            int size = map.size();
            for (int i = 0; i < size; i++) {
                Node first = (Node) map.get(i);
                saveCalleeMap(first, key);
                for (int j = i + 1; j < size; j++) {
                    Node second = (Node) map.get(j);
                    if (first != second) {
                        Pair ps = new Pair(first, second);
                        saveOrderPairs(ps, key);
                    }
                }
            }
        }
      //  System.out.println("calleesize" + calleeMap.size());
    }
    
    public void saveCalleeMap(Node callee, Node key) {
        
        if (!calleeMap.containsKey(callee)) {
            HashSet<Node> temp = new HashSet<Node>();
            temp.add(key);
            calleeMap.put(callee, temp);
        } else {
            calleeMap.get(callee).add(key);
        }
    }
    
    public void saveOrderPairs(Pair ps, Node key) {
        
        if (!orderPairMap.containsKey(ps)) {
            
            HashSet<Node> temp = new HashSet<Node>();
            temp.add(key);
            orderPairMap.put(ps, temp);
        } else {
            orderPairMap.get(ps).add(key);
            
        }
        
    }
    
    public void chooseThreOrderPair() {
        
        Set<Pair> set = orderPairMap.keySet();
        for (Iterator<Pair> iter = set.iterator(); iter.hasNext();) {
            
            Pair key = (Pair) iter.next();
            
            // when support values for two functions in one pair is equal
            // that pair will not be considered as a bug
            if(NodeSupport.get(key.getNode1())!=NodeSupport.get(key.getNode2())){
            int value = orderPairMap.get(key).size();
                if (value >= T_SUPPORT) {
                orderPairSupport.put(key, value);
                }
            }
        }
        //System.out.println(orderPairSupport.size());
    }
    
    public void saveConfidenceMap() {
        
        Iterator<Pair> key = orderPairSupport.keySet().iterator();
        int supportPair;
        
        while (key.hasNext()) {
            Pair pair = key.next();
            supportPair = orderPairSupport.get(pair);
            if (supportPair >= T_SUPPORT) {
                Node left = pair.getNode1();
                findBugs(left, pair, supportPair);
                Node right = pair.getNode2();
                findBugs(right, pair, supportPair);
            }
        }
       // System.out.println(a);
        //        System.out.println("confidenceResult: " + orderPairSupport);
    }
    
    public void findBugs(Node function, Pair pair, int supportPair) {
        
        int supportFunc = NodeSupport.get(function);
        double tempConf = (double) supportPair / (double) supportFunc * 100;
        if (tempConf >= T_CONFIDENCE) {
            for (Node temp : calleeMap.get(function)) {
                
                if (!orderPairMap.get(pair).contains(temp)) {
                   
                    System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n",
                                      function,
                                      temp.toString(),
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
    
     Pair(Node m1, Node m2){
     Node1 = m1;
     Node2 = m2;
     }
    /*
    Pair(Node m1, Node m2){
        if(m1.getFunc().compareTo(m2.getFunc())<=0){
            Node1 = m1; Node2 = m2;
        }else{
            Node1 = m2; Node2 = m1;
        }
    }*/
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

