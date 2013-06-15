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


public class ParamsEstimator {

    public static final DecimalFormat df = new DecimalFormat("#.##");
    
    public static void main(String[] args) {
	new ParamsEstimator(args[0]);
    }
    
    public ParamsEstimator(String file) {
	List<String> lines = readFileContent(file);
	List<MeasuredExecution> executions = new ArrayList<MeasuredExecution>();
	for (String line : lines) {
	    String[] parts = line.split(" ");
	    executions.add(new MeasuredExecution(Double.parseDouble(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
	}
	
	Set<AvgCalc> set = new TreeSet<AvgCalc>();
	for (int i = 1; i < executions.size(); i++) {
	    //System.out.println("============== cut-off: " + i + " =============");
	    
	    MeasuredExecution[] filteredExecs = filterExecutionsRandom(i, executions);
	    AvgCalc tmaxRandom = estimateTmax(filteredExecs);
	    // System.out.println("Random: " + df.format(tmaxRandom.average) + "\t" + df.format(tmaxRandom.stdev));
	    AvgCalc result = calculateTexpected(executions, tmaxRandom.average);
	    result.type = "Random: " + i + " " + df.format(tmaxRandom.average) + "\t" + df.format(tmaxRandom.stdev);
	    result.tmaxAvg = tmaxRandom.average;
	    result.tmaxStdev = tmaxRandom.stdev;
	    // System.out.println("===> " + df.format(result.average) + " " + df.format(result.stdev));
	    set.add(result);
	    
	    filteredExecs = filterExecutionsSimpler(i, executions);
	    AvgCalc tmaxSimple = estimateTmax(filteredExecs);
	    //System.out.println("\nSimple: " + df.format(tmaxSimple.average) + "\t" + df.format(tmaxSimple.stdev));
	    result = calculateTexpected(executions, tmaxSimple.average);
	    //System.out.println("===> " + df.format(result.average) + " " + df.format(result.stdev));
	    result.type = "Simple: " + i + " " + df.format(tmaxSimple.average) + "\t" + df.format(tmaxSimple.stdev);
	    result.tmaxAvg = tmaxRandom.average;
	    result.tmaxStdev = tmaxRandom.stdev;
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

    private AvgCalc calculateTexpected(List<MeasuredExecution> execs, double tmax) {
	AvgCalc percentError = new AvgCalc();
	for (MeasuredExecution e : execs) {
	    double tExp = tmax / (1 + e.K * (1 + e.N));
	    double tReal = e.throughputReal;
	    double error = Math.abs(100.0 - Math.abs(tExp/tReal) * 100.0);
	    percentError.values.add(error);
	    String str = "\t" + e + "\tTexp : " + df.format(tExp) + "\terror: " + df.format(error);
	    percentError.output += str;
	    // System.out.println(str);
	}
	return percentError.calculateStats();
    }
    
    private AvgCalc estimateTmax(MeasuredExecution[] execs) {
	AvgCalc tmax = new AvgCalc();
	for (MeasuredExecution m : execs) {
	    double value = m.throughputReal * (1 + m.K * (1 + m.N));
	    tmax.values.add(value);
	}
	return tmax.calculateStats();
    }
    
    private MeasuredExecution[] filterExecutionsRandom(int cutoff, List<MeasuredExecution> executions) {
	Random random = new Random();
	List<MeasuredExecution> copy = new ArrayList<MeasuredExecution>(executions);
	MeasuredExecution[] result = new MeasuredExecution[cutoff];
	for (int i = 0; i < cutoff; i++) {
	    int r = random.nextInt(copy.size());
	    MeasuredExecution chosen = copy.get(r);
	    copy.remove(r);
	    result[i] = chosen;
	}
	return result;
    }
    
    private MeasuredExecution[] filterExecutionsSimpler(int cutoff, List<MeasuredExecution> executions) {
	Object[] copy = new ArrayList<MeasuredExecution>(executions).toArray();
	Arrays.sort(copy);
	MeasuredExecution[] result = new MeasuredExecution[cutoff];
	for (int i = 0; i < cutoff; i++) {
	    result[i] = (MeasuredExecution) copy[i];
	}
	return result;
    }
    
    private class AvgCalc implements Comparable<AvgCalc> {
	public double average;
	public double stdev;
	public List<Double> values = new ArrayList<Double>();
	public String output = "";
	public String type = "";
	public double tmaxAvg;
	public double tmaxStdev;
	
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
    
    private class MeasuredExecution implements Comparable<MeasuredExecution> {
	
	public final double throughputReal;
	public final int N;
	public final int K;
	
	public MeasuredExecution(double throughputReal, int N, int K) {
	    this.throughputReal = throughputReal;
	    this.N = N;
	    this.K = K;
	}

	@Override
	public int compareTo(MeasuredExecution other) {
	    if ((this.N + this.K) < (other.N + other.K)) {
		return -1;
	    } else if ((this.N + this.K) == (other.N + other.K)) {
		return 0;
	    } else {
		return 1;
	    }
	}
	
	@Override
	public String toString() {
	    return "Execution:\tTr: " + this.throughputReal + "\tN: " + this.N + "\tK: " + this.K;
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