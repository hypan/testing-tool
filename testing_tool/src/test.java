
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
    public static Integer support = 3;
    int threSupport = 3;
    double threConfidence = 0.65;

    public static void main(String[] args) {
        test test = new test();
        //String fileName = "/Users/haoyuepan/Documents/netbean/call2";
        String fileName = "/Users/yananchen/Downloads/test3.txt";
        test.readFileByLines(fileName);
        test.calculateConfidence();
        test.displayBugs(3.00, 65.00);
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
//            analyse();
//        displayBugs(3.00, 65.00);
     
        
            System.out.println("method: " + methods.size());
//            System.out.println("callMap: " + callMap);
            System.out.println("pairMap: " + pairMap.size());
            System.out.println("pairsupport: " + pairSupport.size());
            
//            System.out.println("methodSupport: " + methodSupport);
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

    boolean nullFunc = false;

    public void saveGraph(String graphLine) {

        if (graphLine.trim().startsWith(call_gra)) {
            if (!graphLine.contains(null_func)) {
                nullFunc = true;
                String currentCaller = getFuncName(graphLine);
                caller = new Method(currentCaller.hashCode(), currentCaller);
                this.addMethod(caller);
            } else {
                nullFunc = false;
            }
        } else if (nullFunc
                && graphLine.contains(callee_start)) {

            String currentCallee = getFuncName(graphLine); // find func name;
            callee = new Method(currentCallee.hashCode(), currentCallee);
            this.addCallee(caller, callee);

            methodSupport.put(callee, methodSupport.get(callee) + 1);
            savePairs();
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
            callMap.put(m, new HashSet<Method>());
            methodSupport.put(m, 0);
        }
    }

    public void addCallee(Method m1, Method m2) {
        addMethod(m2);
        if (callMap.containsKey(m1)) {

            Set<Method> callees = callMap.get(m1);
            callees.add(m2);

        }
    }
    public Pair ps = null;
    public Method small = null;
    public Method large = null;

    public void savePairs() {
        HashSet<Method> set = callMap.get(caller);

        for (Iterator<Method> iter = set.iterator(); iter.hasNext();) {
            Method key = (Method) iter.next();

            if (key.getId() != callee.getId()) {
                small = callee;
                large = key;

                if (large.getId() < small.getId()) {
                    Pair ps = new Pair(callee, key);
                    save(ps, caller);
                } else {

                    Pair ps = new Pair(large, small);
                    save(ps, caller);
                }

            }
        }
    }

    public void save(Pair ps, Method caller) {
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
//            System.out.println(value);
            if (value >= threSupport)
            {
                pairSupport.put(key, value);
            }
        }
    }

    public void calculateConfidence() {
        Iterator<Pair> key = pairSupport.keySet().iterator();
        int supportN1, supportN2, supportPair;
        supportN1 = supportN2 = supportPair = 0;
        while (key.hasNext()) {
            Pair pair = key.next();
            supportN1 = methodSupport.get(pair.getMethod1());
            supportN2 = methodSupport.get(pair.getMethod2());
            supportPair = pairSupport.get(pair);
            double[] result = new double[5];
            result[0] = supportN1;
            result[1] = supportN2;
            result[2] = supportPair;
            if (supportN1 != 0) {
                double confidenceN1 = (double) supportPair / (double) supportN1
                        * 100;
                result[3] = confidenceN1;
            }
            if (supportN2 != 0) {
                double confidenceN2 = (double) supportPair / (double) supportN2
                        * 100;
                result[4] = confidenceN2;
            }

            confidenceResult.put(pair, result);
        }

    }
//    public void displayAnalysis() {
//		NumberFormat nf = NumberFormat.getNumberInstance();
//		nf.setMaximumFractionDigits(2);
//		for (Pair key : confidenceResult.keySet()) {
//			double[] tab = confidenceResult.get(key);
//			// output format is changed
//			System.out.println(key.getMethod1().toString() + "  "
//					+ key.getMethod2().toString() + " | " + nf.format(tab[0])
//					+ " | " + nf.format(tab[1]) + " | " + nf.format(tab[2])
//					+ " | " + nf.format(tab[3]) + " | " + nf.format(tab[4]));
//		}
//
//	}

    public void displayBugs(double support, double confidence) {
        DecimalFormat nf1 = new DecimalFormat("#"); // change the output format
        DecimalFormat nf2 = new DecimalFormat("#.00");
        for (Pair key : confidenceResult.keySet()) {
            double[] tab = confidenceResult.get(key);
            if (tab[2] >= support && tab[3] >= confidence) {
                for (Method keyM : callMap.keySet()) {
                    Set<Method> calledMethods = callMap.get(keyM);
                    if (calledMethods.contains(key.getMethod1())
                            && !calledMethods.contains(key.getMethod2())) {
                        // output format is changed
                        System.out.println("bug: "
                                + key.getMethod1().toString() + " in "
                                + keyM.toString() + ", pair: ("
                                + key.toString() + "), support: "
                                + nf1.format(tab[2]) + ", confidence: "
                                + nf2.format(tab[3]) + "%");
                    }

                }
            }
            if (tab[2] >= support && tab[4] >= confidence) {
                for (Method keyM : callMap.keySet()) {

                    Set<Method> calledMethods = callMap.get(keyM);
                    if (calledMethods.contains(key.getMethod2())
                            && !calledMethods.contains(key.getMethod1())) {
                        // output format is changed
                        System.out.println("bug: "
                                + key.getMethod2().toString() + " in "
                                + keyM.toString() + ", pair: ("
                                + key.toString() + "), support: "
                                + nf1.format(tab[2]) + ", confidence: "
                                + nf2.format(tab[4]) + "%");
                    }

                }
            }

        }

    }
}
