package testing2;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author yananchen
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test3 {

    private int T_SUPPORT = 3;
    private float T_CONFIDENCE = 65;
    public Node caller = null;
    public Node callee = null;

    private final String call_gra = "Call graph";
    private final String callee_start = "calls function";
    private final String null_func = "null function";
    int expandLevel=2;

    HashSet<Node> Nodes = new HashSet<Node>();
    HashMap<Node, HashSet<Node>> callMap = new HashMap<Node, HashSet<Node>>();
    HashMap<Node, Integer> NodeSupport = new HashMap<Node, Integer>();
    HashMap<Pair, HashSet<Node>> pairMap = new HashMap<Pair, HashSet<Node>>();
    HashMap<Pair, Integer> pairSupport = new HashMap<Pair, Integer>();
    HashMap<Pair, double[]> confidenceResult = new HashMap<Pair, double[]>();
    HashMap<Node,HashSet<Node>> expandedCallMap=new HashMap<Node,HashSet<Node>>();

    HashMap<Node,HashSet<Node>> calleeCaller=new HashMap<Node,HashSet<Node>>();
    public Pair ps = null;
    public int a = 0;     // Used for test, should be deleted finally.
    public Node small = null;
    public Node large = null;
    boolean nullFunc = false;

    public static void main(String[] args) {
        test3 test = new test3();
//        String fileName = "/Users/yananchen/Downloads/call";
        String fileName = "/Users/yananchen/Downloads/test3.txt";
        test.readFileByLines(fileName);
        test.saveExpandedCallMap();
        
        test.createNodePair();
        test.chooseThrePair();
        test.saveConfidenceMap();
        test.findBugs(3, 65.00);
        
       
    }

    public void readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                saveGraph(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        
        //System.out.print("expandedCallMap"+expandedCallMap.size());
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
        HashSet<Node> set = callMap.get(caller);
        if (set == null) {
            set = new HashSet<Node>();
            set.add(callee);
            callMap.put(caller, set);
            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
        } else if (!set.contains(callee)) {
            set.add(callee);
            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
//            createNodePair(set, callee); // pair support
        }  
    }
    
    
    public void saveExpandedCallMap(){
          
//        HashSet<Node> calleeSet=new  HashSet<Node>();       
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
                System.out.println("expandedCallMap"+expandedCallMap);
    }
    
    
//    public void saveExpandedCallMap(){
//    	expandedCallMap=new HashMap<Node,HashSet<Node>>(callMap);
////    	for(Iterator it=callMap.keySet().iterator();it.hasNext();){
////			Node key=(Node)it.next();
////			expandedCallMap.put(callee, callMap.get(key));
////		}
//    	for(int i=0;i<expandLevel;i++){
//    		HashMap<Node,HashSet<Node>> tempCallMap=new HashMap<Node,HashSet<Node>>();
//    		//Set<Node> callerSet=expandedCallMap.keySet();
//    		for(Node tempCaller:expandedCallMap.keySet()){
//    			HashSet<Node> calleeSet=new HashSet<Node>();
//    			for(Iterator it=expandedCallMap.get(tempCaller).iterator();it.hasNext();){
//    				Node key=(Node)it.next();
//    				calleeSet.add(key);
//    			}
//    			//HashSet<Node> calleeSet=callMap.get(tempCaller);
//    			for(Node tempcallee:callMap.get(tempCaller)){
//    				if(callMap.containsKey(tempcallee)){
//    					
//    					
//    					for(Node tempCallee2:callMap.get(tempcallee)){
//    						if(!calleeSet.contains(tempCallee2)){
//    							calleeSet.add(tempCallee2);
//    						}
//    					}
//    				}
//    			}
//    			tempCallMap.put(tempCaller, calleeSet);
//    		}
//    		expandedCallMap=new HashMap<Node,HashSet<Node>>(tempCallMap);
//    		
//    	}
//    	
//    }
    
    
    public void removeFunction(HashSet<Node> set,Node function){
    	if(set.contains(function)){
    		set.remove(function);
    	}
    }
    
    
    
    
    public void createNodePair() {
        Set<Node> set = callMap.keySet();
        for (Iterator<Node> iter = set.iterator(); iter.hasNext();) {
            Node key = (Node) iter.next();
            Set<Node> value = callMap.get(key);
            for (Iterator<Node> iter2 = value.iterator(); iter2.hasNext();) {
                Node currCallee = (Node) iter2.next();
                for (Node temp : value) {
                    if (temp != currCallee) {
                        Pair ps = new Pair(temp, currCallee);
                        savePairs(ps, key);
                    }
                }
            }
        }
    }
//    public void createNodePair(HashSet<Node> set, Node callee) {
//      
//        for (Iterator<Node> iter = set.iterator(); iter.hasNext();) {
//            Node key = (Node) iter.next();
//            if (key != callee) {
//                 Pair ps = new Pair(callee, key);
//                 savePairs(ps);
//            }
//        }
//    }

    public void savePairs(Pair ps, Node caller) {
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

    public void saveConfidenceMap() {
        Iterator<Pair> key = pairSupport.keySet().iterator();
        int supportN1, supportN2, supportPair;
        supportN1 = 0;
        supportN2 = 0;
        supportPair = 0;
        while (key.hasNext()) {
            Pair pair = key.next();
            supportN1 = NodeSupport.get(pair.getNode1());
            supportN2 = NodeSupport.get(pair.getNode2());
            supportPair = pairSupport.get(pair);
            double[] result = new double[5];
            result[0] = supportN1;
            result[1] = supportN2;
            result[2] = supportPair;
            double confidenceN1 = (double) supportPair / (double) supportN1
                    * 100;
            result[3] = confidenceN1;

            double confidenceN2 = (double) supportPair / (double) supportN2
                    * 100;
            result[4] = confidenceN2;

            confidenceResult.put(pair, result);
        }
        System.out.println("confidenceResult: " + confidenceResult.size());

    }

    public void findBugs(int support, double confidence) {

        Set<Node> cont = callMap.keySet();
        Set<Pair> conf = confidenceResult.keySet();
        for (Pair p : conf) {
            Node f1 = p.getNode1();
            Node f2 = p.getNode2();
            double[] tab = confidenceResult.get(p);
            if (tab[2] >= support) {
                if (tab[3] >= confidence) {
                    for (Iterator<Node> iter = cont.iterator(); iter.hasNext();) {
                        Node key = (Node) iter.next();
                        HashSet<Node> Nodes = callMap.get(key);
                        HashSet<Node> expandedNodes=expandedCallMap.get(key);
                        if (Nodes.contains(f1) && (!Nodes.contains(f2))) {
                            if(expandedNodes.contains(f1)&&!expandedNodes.contains(f2)){
                             a++;//used for test, should be deleted finally
                            System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n",
                                    p.getNode1().toString(),
                                    key.toString(),
                                    p.getNode1().toString(),
                                    p.getNode2().toString(),
                                    (int) tab[2],
                                    tab[3]
                            );
                            }
//                            saveBugs(p, key, tab);
                           
                        }
                    }
                }
                if (tab[4] >= confidence) {

                    for (Iterator<Node> iter = cont.iterator(); iter.hasNext();) {
                        Node key = (Node) iter.next();
                        HashSet<Node> Nodes = callMap.get(key);
                        HashSet<Node> expandedNodes=expandedCallMap.get(key);
                        if (!Nodes.contains(f1) && (Nodes.contains(f2))) {
                            if(!expandedNodes.contains(f1)&&expandedNodes.contains(f2)){
                            
//                                 saveBugs(p, key, tab);
                            a++;

                            System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n",
                                    p.getNode2().toString(),
                                    key.toString(),
                                    p.getNode1().toString(),
                                    p.getNode2().toString(),
                                    (int) tab[2],
                                    tab[4]
                            );
                            }
//                   
                        }
                    }
                }
            }
        }
        System.out.println(a);
    }
}