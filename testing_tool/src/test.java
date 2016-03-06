
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
    HashMap<Pair, Integer> finalPairSupport = new HashMap<Pair, Integer>();
    private Map<Pair, double[]> analysis = new HashMap<Pair, double[]>();
    private HashMap<Method, int[]> programCounter = new HashMap<Method, int[]>();
    public Set<Bug> bugs = new HashSet<Bug>();
//	public Set<Bug> bugs = new HashSet<Bug>();

    public Integer presentCaller;
    public Integer presentCallee;
    public Method caller = null;
    public Method callee = null;
    public static Integer support = 3;
    int threSupport = 3;
    double threConfidence = 0.65;

    public static void main(String[] args) {
        test test = new test();
        String fileName = "/Users/haoyuepan/Documents/netbean/call2";
        test.readFileByLines(fileName);
       
        test.analyse();
//        test.displayBugs(3.00, 65.00);
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
            
        
     
        System.out.println("finalPairSupport: " + finalPairSupport.size());
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
//        System.out.println(set);
        for (Iterator<Method> iter = set.iterator(); iter.hasNext();) {
            Method key = (Method) iter.next();
//            System.out.println("callee: "+callee);
//            System.out.println("calleeID: "+callee.getId());
//            System.out.println("key:  "+key);
//            System.out.println("keyID:  "+key.getId());

//            Pair ps = new Pair(key, callee);
            if (key.getId() != callee.getId()) {
//                System.out.println("not equal  "+(key.getId() != callee.getId()));
                small = callee;
                large = key;

                if (large.getId() < small.getId()) {
//                    System.out.println("small  "+(large.getId() < small.getId()));
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
                finalPairSupport.put(key, value);
            }
        }
    }

    public void analyse() {
        Iterator<Pair> itr = pairMap.keySet().iterator();
        int supportM1, supportM2, supportPair;
        supportM1 = supportM2 = supportPair = 0;
        while (itr.hasNext()) {
            Pair pair = itr.next();

            supportM1 = methodSupport.get(pair.getMethod1());
			// else supportM1 = 0 as initial

            supportM2 = methodSupport.get(pair.getMethod2());
            // else supportM2 = 0 as initial
            supportPair = pairSupport.get(pair);
            double[] result = new double[5];
            result[0] = supportM1;
            result[1] = supportM2;
            result[2] = supportPair;
            if (supportM1 != 0) {
                double confidenceM1 = (double) supportPair / (double) supportM1
                        * 100;
                result[3] = confidenceM1;
            }
            if (supportM2 != 0) {
                double confidenceM2 = (double) supportPair / (double) supportM2
                        * 100;
                result[4] = confidenceM2;
            }

            analysis.put(pair, result);
        }

    }
//    public void displayAnalysis() {
//		NumberFormat nf = NumberFormat.getNumberInstance();
//		nf.setMaximumFractionDigits(2);
//		for (Pair key : analysis.keySet()) {
//			double[] tab = analysis.get(key);
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
        for (Pair key : analysis.keySet()) {
            double[] tab = analysis.get(key);
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
//
//	/*
//	 * the following methods are used for part c
//	 */
//
//	// analyse the support and confidence for pairs already created
//	public void analysePairs() {
//		Iterator<Pair> itr = pairMap.keySet().iterator();
//		;
//		int supporM1, supportM2, supportPair;
//		boolean testM1, testM2;
//		while (itr.hasNext()) {
//			Pair pair = itr.next();
//			supporM1 = supportM2 = supportPair = 0;
//			for (Method key : callMap.keySet()) {
//				if (key.toString().equals("null function")) // add the if
//															// statement !!!
//					continue;
//				Set<Method> calledMethods = callMap.get(key);
//				testM1 = testM2 = false;
//				if (calledMethods.contains(pair.getMethod1())) {
//					testM1 = true;
//					supporM1++;
//				}
//				if (calledMethods.contains(pair.getMethod2())) {
//					testM2 = true;
//					supportM2++;
//				}
//				if (testM1 == true && testM2 == true) {
//					supportPair++;
//				}
//			}
//			double[] result = new double[5];
//			result[0] = supporM1;
//			result[1] = supportM2;
//			result[2] = supportPair;
//			if (supporM1 != 0) {
//				double confidenceM1 = (double) supportPair / (double) supporM1
//						* 100;
//				result[3] = confidenceM1;
//			}
//			if (supportM2 != 0) {
//				double confidenceM2 = (double) supportPair / (double) supportM2
//						* 100;
//				result[4] = confidenceM2;
//			}
//
////			analysis.put(pair, result);
//
//		}
//	}

	// find and store bugs into the set of bug used to reduce the number of
    // false positives
//	public void storeBugs(double support, double confidence) {
//		DecimalFormat nf1 = new DecimalFormat("#"); // change the output format
//		DecimalFormat nf2 = new DecimalFormat("#.00");
//		for (Pair key : analysis.keySet()) {
//			double[] tab = analysis.get(key);
//			if (tab[2] >= support && tab[3] >= confidence) {
//				for (Method keyM : callMap.keySet()) {
//
//					Set<Method> calledMethods = callMap.get(keyM);
//					if (calledMethods.contains(key.getMethod1())
//							&& !calledMethods.contains(key.getMethod2())) {
//						// create bug
//						Bug bug = new Bug(key.getMethod1(), keyM, key, tab[2],
//								tab[3]);
//						bugs.add(bug);
//
//						// output format is changed
//						System.out.println("bug: "
//								+ key.getMethod1().toString() + " in "
//								+ keyM.toString() + ", pair: ("
//								+ key.toString() + "), support: "
//								+ nf1.format(tab[2]) + ", confidence: "
//								+ nf2.format(tab[3]) + "%");
//					}
//
//				}
//			}
//			if (tab[2] >= support && tab[4] >= confidence) {
//				for (Method keyM : callMap.keySet()) {
//
//					Set<Method> calledMethods = callMap.get(keyM);
//					if (calledMethods.contains(key.getMethod2())
//							&& !calledMethods.contains(key.getMethod1())) {
//						// create bug
//						Bug bug = new Bug(key.getMethod2(), keyM, key, tab[2],
//								tab[4]);
//						bugs.add(bug);
//
//						// output format is changed
//						System.out.println("bug: "
//								+ key.getMethod2().toString() + " in "
//								+ keyM.toString() + ", pair: ("
//								+ key.toString() + "), support: "
//								+ nf1.format(tab[2]) + ", confidence: "
//								+ nf2.format(tab[4]) + "%");
//					}
//
//				}
//			}
//
//		}
//
//	}
}
