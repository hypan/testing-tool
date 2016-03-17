
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    HashSet<Node> Nodes = new HashSet<>();
    HashMap<Node, HashSet<Node>> callMap = new HashMap<>();
    HashMap<Node, Integer> NodeSupport = new HashMap<>();
    HashMap<Pair, HashSet<Node>> pairMap = new HashMap<>();
    HashMap<Pair, Integer> pairSupport = new HashMap<>();
    

    public Pair ps = null;
    public int a = 0;     // Used for test, should be deleted finally.
    public Node small = null;
    public Node large = null;
    boolean nullFunc = false;

    public static void main(String[] args) {

        test test = new test();
        String fileName = "/Users/haoyuepan/Documents/netbean/call";
//        String fileName = "/Users/yananchen/Downloads/test3.txt";
        test.readFileByLines(fileName);
        test.chooseThrePair();
        test.saveConfidenceMap();

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

            System.out.println(callMap.size());
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

        if (!callMap.containsKey(caller)) {
            HashSet<Node> set = new HashSet<Node>();
            set.add(callee);
            callMap.put(caller, set);
            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
        } else if (!callMap.get(caller).contains(callee)) {
            callMap.get(caller).add(callee);
            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
            createNodePair(callMap.get(caller), callee); // pair support
        }
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

    public void saveConfidenceMap() {

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

//        System.out.println("confidenceResult: " + confidenceResult.size());
    }

    public void findBugs(Node function, Pair pair, int supportPair) {

        Set<Node> cont = callMap.keySet();
//        Set<Pair> conf = confidenceResult.keySet();
        int supportFunc = NodeSupport.get(function);
        double tempConf = (double)supportPair / (double)supportFunc * 100;
//        System.out.println(tempConf);
        if (tempConf >= T_CONFIDENCE) {
            for (Iterator<Node> iter = cont.iterator(); iter.hasNext();) {
                Node key = (Node) iter.next();
                HashSet<Node> Nodes = callMap.get(key);
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
