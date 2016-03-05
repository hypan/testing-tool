
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    public Integer presentCaller;
    public Integer presentCallee;
    public Method caller = null;
    public Method callee = null;

    int threSupport = 3;
    double threConfidence = 0.65;

    public static void main(String[] args) {
        test test = new test();
        String fileName = "/Users/haoyuepan/Documents/netbean/call";
        test.readFileByLines(fileName);
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

            System.out.println("method: " + methods.size());
            System.out.println("callMap: " + callMap);
            System.out.println("pairMap: " + pairMap.size());
            System.out.println("methodSupport: " + methodSupport);
            System.out.println("pairSupport: " + pairSupport.size());

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
            HashSet<Method> set = callMap.get(caller);
            methodSupport.put(callee, methodSupport.get(callee) + 1);
            savePairs(set, callee, caller);
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
    Pair ps;

    public void savePairs(HashSet set, Method index, Method caller) {
        if (set != null) {
            for (Iterator<Method> iter = set.iterator(); iter.hasNext();) {
                Method key = (Method) iter.next();
                if (key.getId() != index.getId()) {
                    Method s1 = index;
                    Method s2 = key;
                    if (s1.getId() < s2.getId()) {
                        Pair ps = new Pair(s1, s2);
                    } else {
                        Pair ps = new Pair(s2, s1);
                    }

                    if (!pairMap.containsKey(ps)) {
                        pairSupport.put(ps, 1);
                        HashSet<Method> temp = new HashSet<Method>();
                        temp.add(caller);
                        pairMap.put(ps, temp);
                    } else {

                        pairSupport.put(ps, (pairSupport.get(ps) + 1));
                        pairMap.get(ps).add(caller);

                    }
                }

            }
        }
    }
}
