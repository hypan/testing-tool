
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
import java.io.InputStream;
import java.io.InputStreamReader;

public class test {

    public static int T_SUPPORT;
    public static double T_CONFIDENCE;
    public Node caller = null;
    public Node callee = null;

    private final String call_gra = "Call graph";
    private final String callee_start = "calls function";
    private final String null_func = "null function";

    HashSet<Node> Nodes = new HashSet<Node>();
    HashMap<Node, HashSet<Node>> callMap = new HashMap<Node, HashSet<Node>>();
    HashMap<Node, Integer> NodeSupport = new HashMap<Node, Integer>();
    HashMap<Pair, HashSet<Node>> pairMap = new HashMap<Pair, HashSet<Node>>();
    HashMap<Pair, Integer> pairSupport = new HashMap<Pair, Integer>();
    HashMap<Pair, double[]> confidenceResult = new HashMap<Pair, double[]>();

    public Pair ps = null;
    public int a = 0;     // Used for test, should be deleted finally.
    public Node small = null;
    public Node large = null;
    public boolean nullFunc = false;
    public String fileName;

    public static void main(String[] args) {
        test test = new test();
        test.setInput(args);
//        String fileName = "/Users/haoyuepan/Documents/netbean/call2";
//        String fileName = "/Users/yananchen/Downloads/test3.txt";
        test.readFileByLines();
        test.chooseThrePair();
        test.saveConfidenceMap();
        test.findBugs(T_SUPPORT, T_CONFIDENCE);
    }

    private void setInput(String[] args) {
        fileName = args[0];
        System.out.println(fileName);
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
        final String optCommand = "opt -print-callgraph " + fileName + " >/dev/null";
        ProcessBuilder proc = new ProcessBuilder("bash", "-c", optCommand);
        Process process;
        try {
            process = proc.start();
            InputStream stderr = process.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            String tempString = null;

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
        HashSet<Node> set = callMap.get(caller);
        if (set == null) {
            set = new HashSet<Node>();
            set.add(callee);
            callMap.put(caller, set);
            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
        } else if (!set.contains(callee)) {
            set.add(callee);
            NodeSupport.put(callee, NodeSupport.get(callee) + 1);
            createNodePair(set, callee); // pair support
        }
    }

    public void createNodePair(HashSet<Node> set, Node callee) {
        HashSet<Node> temp = callMap.get(caller);
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

                        if (Nodes.contains(f1) && (!Nodes.contains(f2))) {

                            System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n",
                                    p.getNode2().toString(),
                                    key.toString(),
                                    p.getNode1().toString(),
                                    p.getNode2().toString(),
                                    (int) tab[2],
                                    tab[3]
                            );
                        }
                    }
                }
                if (tab[4] >= confidence) {

                    for (Iterator<Node> iter = cont.iterator(); iter.hasNext();) {
                        Node key = (Node) iter.next();
                        HashSet<Node> Nodes = callMap.get(key);
                        if (!Nodes.contains(f1) && (Nodes.contains(f2))) {

                            System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n",
                                    p.getNode2().toString(),
                                    key.toString(),
                                    p.getNode1().toString(),
                                    p.getNode2().toString(),
                                    (int) tab[2],
                                    tab[3]
                            );
                        }
                    }
                }
            }
        }
    }

}

