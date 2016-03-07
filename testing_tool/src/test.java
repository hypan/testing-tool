
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

public class test {
//hello

    private final String call_gra = "Call graph";
    private final String callee_start = "calls function";
    private final String null_func = "null function";

    HashSet<Method> methods = new HashSet<Method>();
    HashMap<Method, HashSet<Method>> callMap = new HashMap<Method, HashSet<Method>>();
    HashMap<Method, Integer> methodSupport = new HashMap<Method, Integer>();
    HashMap<Pair, HashSet<Method>> pairMap = new HashMap<Pair, HashSet<Method>>();
    HashMap<Pair, Integer> pairSupport = new HashMap<Pair, Integer>();
    private Map<Pair, double[]> confidenceResult = new HashMap<Pair, double[]>();
    private HashMap<Method, int[]> programCounter = new HashMap<Method, int[]>();
//    public Set<Bug> bugs = new HashSet<Bug>();

    public Integer presentCaller;
    public Integer presentCallee;
    public Method caller = null;
    public Method callee = null;
    int threSupport = 3;
    double threConfidence = 0.65;
    public Pair ps = null;
    public int a = 0;
    public Method small = null;
    public Method large = null;
        public Method m1;
    public Method m2;
    boolean nullFunc = false;

    public static void main(String[] args) {
        test test = new test();
        String fileName = "/Users/haoyuepan/Documents/netbean/call2";
//        String fileName = "/Users/yananchen/Downloads/test3.txt";
        test.readFileByLines(fileName);
//        test.chooseThrePair();
        test.calculateConfidence();

        test.displayBugs(10.00, 80.00);
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
            chooseThrePair();
            System.out.println("method: " + methods.size());
            System.out.println("callMap: " + callMap.size());
            System.out.println("pairMap: " + pairMap);
            System.out.println("pairsupport: " + pairSupport.size());
            System.out.println("methodSupport: " + methodSupport.size());
//            System.out.println("pairSupport: " + analysis);
//            System.out.println("finalPairSupport: " + analysis.size());
//            System.out.println("bugs: " + bugs.size());
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

    public void saveGraph(String graphLine) {

        if (graphLine.trim().startsWith(call_gra)) {
            if (!graphLine.contains(null_func)) {
                nullFunc = true;
                String currentCaller = getFuncName(graphLine);
                caller = new Method(currentCaller.hashCode(), currentCaller);
                addMethod(caller);
            } else {
                nullFunc = false;
            }
        } else if (nullFunc
                && graphLine.contains(callee_start)) {

            String currentCallee = getFuncName(graphLine); // find func name;
            callee = new Method(currentCallee.hashCode(), currentCallee);
            addMethod(callee);
            HashSet<Method> set = callMap.get(caller);
            
            if (set == null) {
                set = new HashSet<Method>();
                set.add(callee);
                callMap.put(caller, set);
                methodSupport.put(callee, methodSupport.get(callee) + 1);
            } else if (!set.contains(callee)) {
                set.add(callee);
                methodSupport.put(callee, methodSupport.get(callee) + 1);
                savePairs(set, callee); // pair support
            }
//            this.addCallee(caller, callee);
//            if(!methodSupport.containsKey(callee)){
//                methodSupport.put(callee, 1);
//            }else{
//            methodSupport.put(callee, methodSupport.get(callee) + 1);
//            }
//            savePairs();
        }

    }

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

    public void addMethod(Method m) {
        if (!methods.contains(m)) {
            methods.add(m);
            methodSupport.put(m, 0);
        }
    }

    public void addCallee(Method m1, Method m2) {
    
        if(!callMap.containsKey(m1)){
            HashSet<Method> temp = new HashSet<Method>();
            temp.add(m2);
            callMap.put(m1, temp);
            
        }else {

            Set<Method> callees = callMap.get(m1);
            callees.add(m2);

        }   
    }
    
    public void savePairs(HashSet<Method> set, Method callee) {
       for (Iterator<Method> iter = set.iterator(); iter.hasNext();) {
            Method key = (Method) iter.next();
            if (key != callee) {

                m1 = key;
                m2 = callee;
                if (key.getId() > callee.getId() ) {
                    m1 = callee;
                    m2 = key;
                }

                Pair p1 = new Pair(m1, m2);
                save(p1);
            }
        }
    }
//   
// public void savePairs(HashSet<Method> set, Method callee) {
//        HashSet<Method> temp = callMap.get(caller);
//
//        for (Iterator<Method> iter = set.iterator(); iter.hasNext();) {
//            Method key = (Method) iter.next();
//
//            if (key.getId() != callee.getId()) {
//                small = callee;
//                large = key;
//
//                if (large.getId() < small.getId()) {
//                    Pair ps = new Pair(callee, key);
//                    save(ps, caller);
//                } else {
//
//                    Pair ps = new Pair(large, small);
//                    save(ps, caller);
//                }
//
//            }
//        }
//    }

    public void save(Pair ps) {
        if (!pairMap.containsKey(ps)) {
            HashSet<Method> temp = new HashSet<Method>();
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
            if (value >= threSupport) {
                pairSupport.put(key, value);
            }
        }
    }

    public void calculateConfidence() {
        Iterator<Pair> key = pairSupport.keySet().iterator();
        int supportN1, supportN2, supportPair;
        supportN1 = 0;
        supportN2 = 0;
        supportPair = 0;
        while (key.hasNext()) {
            Pair pair = key.next();
            supportN1 = methodSupport.get(pair.getMethod1());
            supportN2 = methodSupport.get(pair.getMethod2());
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
//            System.out.println(result[1]);
        }
        System.out.println("confidenceResult: " + confidenceResult.size());

    }

    public void displayBugs(double support, double confidence) {
        DecimalFormat nf1 = new DecimalFormat("#"); // change the output format
        DecimalFormat nf2 = new DecimalFormat("#.00");

        Set<Method> cont = callMap.keySet();
        Set<Pair> conf = confidenceResult.keySet();
        for (Pair p : conf) {
            Method f1 = p.getMethod1();
            Method f2 = p.getMethod2();
            double[] tab = confidenceResult.get(p);
            if(tab[2]>=support){
            if (tab[3] >= confidence) {
                for (Iterator<Method> iter = cont.iterator(); iter.hasNext();) {
                    Method key = (Method) iter.next();
                    HashSet<Method> methods = callMap.get(key);

                    if (methods.contains(f1) && (!methods.contains(f2))) {

                        a++;
                        System.out.println("bug: "
                                + p.getMethod1().toString() + " in "
                                + key.toString() + ", pair: ("
                                + p.toString() + "), support: "
                                + nf1.format(tab[2]) + ", confidence: "
                                + nf2.format(tab[3]) + "%");
                    }
                }
            }
            if (tab[4] >= confidence) {

                for (Iterator<Method> iter = cont.iterator(); iter.hasNext();) {
                    Method key = (Method) iter.next();
                    HashSet<Method> methods = callMap.get(key);
                    if (!methods.contains(f1) && (methods.contains(f2))) {

                        a++;
                        System.out.println("bug: "
                                + p.getMethod2().toString() + " in "
                                + key.toString() + ", pair: ("
                                + p.toString() + "), support: "
                                + nf1.format(tab[2]) + ", confidence: "
                                + nf2.format(tab[4]) + "%");
                    }
                }
            }
        }}
         System.out.println(a);
    }
}
