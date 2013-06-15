import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;


public class AlphaParamsEstimator {

    public static final DecimalFormat df = new DecimalFormat("#.##");
    public static double TMAX = 89235.82;
    
    public static void main(String[] args) {
	new AlphaParamsEstimator(args[0]);
    }
    
    public AlphaParamsEstimator(String file) {
	List<String> lines = readFileContent(file);
	List<AlphaMeasuredExecution> executions = new ArrayList<AlphaMeasuredExecution>();
	for (String line : lines) {
	    String[] parts = line.split(" ");
	    executions.add(new AlphaMeasuredExecution(Double.parseDouble(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
	}
	
	Set<AvgCalc> set = new TreeSet<AvgCalc>();
	for (int i = 1; i < executions.size(); i++) {
	    //System.out.println("============== cut-off: " + i + " =============");
	    
	    AlphaMeasuredExecution[] filteredExecs = filterExecutionsRandom(i, executions);
	    AvgCalc alphaRandom = estimateAlpha(filteredExecs);
	    // System.out.println("Random: " + df.format(alphaRandom.average) + "\t" + df.format(alphaRandom.stdev));
	    AvgCalc result = calculateTexpected(executions, alphaRandom.average);
	    result.type = "Random: " + i + " " + df.format(alphaRandom.average) + "\t" + df.format(alphaRandom.stdev);
	    result.alphaAvg = alphaRandom.average;
	    result.alphaStdev = alphaRandom.stdev;
	    // System.out.println("===> " + df.format(result.average) + " " + df.format(result.stdev));
	    set.add(result);
	    
	    filteredExecs = filterExecutionsSimpler(i, executions);
	    AvgCalc alphaSimple = estimateAlpha(filteredExecs);
	    result = calculateTexpected(executions, alphaSimple.average);
	    result.type = "Simple: " + i + " " + df.format(alphaSimple.average) + "\t" + df.format(alphaSimple.stdev);
	    result.alphaAvg = alphaRandom.average;
	    result.alphaStdev = alphaRandom.stdev;
	    set.add(result);
	    
	    //System.out.println("\n\n");
	}
	
	int rank = 0;
	for (AvgCalc avg : set) {
	    rank++;
	    System.out.println("Rank " + rank + "\terror: " + df.format(avg.average) + "\t" + df.format(avg.stdev) + "\t" + avg.type);
	    System.out.println(avg.output);
	    
	}
    }

    private AvgCalc calculateTexpected(List<AlphaMeasuredExecution> execs, double alpha) {
	AvgCalc percentError = new AvgCalc();
	for (AlphaMeasuredExecution e : execs) {
	    double tExp = TMAX / (1 + e.K * (1 + e.N + 2 * e.M * alpha));
	    double tReal = e.throughputReal;
	    double error = Math.abs(100.0 - Math.abs(tExp/tReal) * 100.0);
	    percentError.values.add(error);
	    String str = "\t" + e + "\tTexp : " + df.format(tExp) + "\terror: " + df.format(error);
	    percentError.output += str;
	    // System.out.println(str);
	}
	return percentError.calculateStats();
    }
    
    private AvgCalc estimateAlpha(AlphaMeasuredExecution[] execs) {
	AvgCalc alpha = new AvgCalc();
	for (AlphaMeasuredExecution m : execs) {
	    double value = (((TMAX/m.throughputReal - 1)/m.K)-1-m.N)/(2*m.M);
	    alpha.values.add(value);
	}
	return alpha.calculateStats();
    }
    
    private AlphaMeasuredExecution[] filterExecutionsRandom(int cutoff, List<AlphaMeasuredExecution> executions) {
	Random random = new Random();
	List<AlphaMeasuredExecution> copy = new ArrayList<AlphaMeasuredExecution>(executions);
	AlphaMeasuredExecution[] result = new AlphaMeasuredExecution[cutoff];
	for (int i = 0; i < cutoff; i++) {
	    int r = random.nextInt(copy.size());
	    AlphaMeasuredExecution chosen = copy.get(r);
	    copy.remove(r);
	    result[i] = chosen;
	}
	return result;
    }
    
    private AlphaMeasuredExecution[] filterExecutionsSimpler(int cutoff, List<AlphaMeasuredExecution> executions) {
	Object[] copy = new ArrayList<AlphaMeasuredExecution>(executions).toArray();
	Arrays.sort(copy);
	AlphaMeasuredExecution[] result = new AlphaMeasuredExecution[cutoff];
	for (int i = 0; i < cutoff; i++) {
	    result[i] = (AlphaMeasuredExecution) copy[i];
	}
	return result;
    }
    
    private class AvgCalc implements Comparable<AvgCalc> {
	public double average;
	public double stdev;
	public List<Double> values = new ArrayList<Double>();
	public String output = "";
	public String type = "";
	public double alphaAvg;
	public double alphaStdev;
	
	public AvgCalc calculateStats() {
	    this.average = 0.0;
	    for (double val : values) {
		this.average += val;
	    }
	    this.average = this.average / values.size();
	    
	    double sum = 0.0;
	    for (double val : values) {
		sum += Math.pow(val - average, 2);
	    }
	    sum = sum / values.size();
	    this.stdev = Math.sqrt(sum);
	    return this;
	}

	@Override
	public int compareTo(AvgCalc other) {
	    if (this.average < other.average) {
		return -1;
	    } else if (this.average == other.average) {
		return 0;
	    } else {
		return 1;
	    }
	}
    }
    
    private class AlphaMeasuredExecution implements Comparable<AlphaMeasuredExecution> {
	
	public final double throughputReal;
	public final int N;
	public final int M;
	public final int K;
	
	public AlphaMeasuredExecution(double throughputReal, int N, int M, int K) {
	    this.throughputReal = throughputReal;
	    this.N = N;
	    this.M = M;
	    this.K = K;
	}

	@Override
	public int compareTo(AlphaMeasuredExecution other) {
	    if ((this.N + this.M + this.K) < (other.N + other.M + other.K)) {
		return -1;
	    } else if ((this.N + this.M + this.K) == (other.N + other.M + other.K)) {
		return 0;
	    } else {
		return 1;
	    }
	}
	
	@Override
	public String toString() {
	    return "Execution:\tTr: " + this.throughputReal + "\tN: " + this.N + "\tM" + this.M + "\tK: " + this.K;
	}
	
    }
    
    private List<String> readFileContent(String filename) {
	List<String> lines = new ArrayList<String>();
	try {
	    FileInputStream is = new FileInputStream(filename);
	    DataInputStream in = new DataInputStream(is);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		if (strLine.equals("")) {
		    continue;
		}
		lines.add(strLine);
	    }
	    br.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	return lines;
    }
    
}