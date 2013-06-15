import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;


/*
 * Reads an input file with the following format:
 * <PercInsert> <PercModify> <PercSearch>
 * 
 * and an unknown number of lines as follows:
 * <T> <perc> <attr1> ... <attrN>
 * 
 * where T is the type of the operation: I(nsert), M(odification) and S(earch)
 */
public class QueryGenerator {

    private final ThreadLocal<Random> ranGen;
    
    private final TreeSet<QueryTemplate> templatesInsert;
    private final TreeSet<QueryTemplate> templatesModification;
    private final TreeSet<QueryTemplate> templatesSearch;

    private final double percInsert;
    private final double percModify;
    private final double percSearch;
    
    public static final String[] PARAMS_NAMES = { "factual_id", "name", "category", "lowest_price", "highest_price", "ratings", "status", "stars", "tel", "region", "locality", "postcode", "longitude", "latitude", "address" };
    private final Map<String, Object[]> attrsUniqueValues;
    
    public static final int REPEAT = 100;
    
    public QueryGenerator(String configFile, String dataFile) {
	List<String> configLines = readFileContent(configFile);
	if (configLines.size() <= 1) {
	    exitError("Must have at least a line for percentages of operations and one query example");
	}

	String[] percentagesConfig = configLines.get(0).split(" ");
	if (percentagesConfig.length != 3) {
	    exitError("Must configure 3 percentages for: <PercInsert> <PercModify> <PercSearch>");
	}

	this.percInsert = Double.parseDouble(percentagesConfig[0]);
	this.percModify = Double.parseDouble(percentagesConfig[1]);
	this.percSearch = Double.parseDouble(percentagesConfig[2]);
	if ((this.percInsert + this.percModify + this.percSearch) != 100.0) {
	    exitError("The sum of percentages of each type of operation must be 100.0");
	}

	this.templatesInsert = new TreeSet<QueryTemplate>();
	this.templatesModification = new TreeSet<QueryTemplate>();
	this.templatesSearch = new TreeSet<QueryTemplate>();

	Map<String, Double> percCreator = new HashMap<String, Double>();
	percCreator.put("I", 0.0);
	percCreator.put("M", 0.0);
	percCreator.put("S", 0.0);
	
	configLines.remove(0);
	for (String line : configLines) {
	    String[] template = line.split(" ");
	    if (template.length == 0) {
		continue;
	    }
	    if (template.length < 3) {
		exitError("Incorrect query template " + line);
	    }
	    
	    String type = template[0];
	    // use the last roof of this type as the basis of the interval for this template
	    double percentageTemplate = Double.parseDouble(template[1]) + percCreator.get(type);
	    List<String> attrs = new ArrayList<String>();
	    for (int i = 2; i < template.length; i++) {
		String attributeName = template[i];
		attrs.add(attributeName);
	    }
	    
	    TreeSet<QueryTemplate> typeTemplates = getTypeTemplates(type);
	    typeTemplates.add(new QueryTemplate(percentageTemplate, attrs));
	    // update the roof for this type
	    percCreator.put(type, percentageTemplate);
	}

	double tmpPercInsert = percCreator.get("I");
	double tmpPercModify = percCreator.get("M");
	double tmpPercSearch = percCreator.get("S");
	
	if ((tmpPercInsert != 100.0 && this.percInsert != 0.0)) {
	    exitError("The sum of percentages for templates of inserts is not 100.0");
	}
	if ((tmpPercModify != 100.0 && this.percModify != 0.0)) {
	    exitError("The sum of percentages for templates of modify is not 100.0");
	}
	//if ((tmpPercSearch != 100.0 && this.percSearch != 0.0)) {
	if ((tmpPercSearch < 100 - 0.00002 && tmpPercSearch > 100 - 0.00002) && this.percSearch != 0.0) {
	    exitError("The sum of percentages for templates of search is not 100.0");
	}
	
	this.ranGen = new ThreadLocal<Random>() {
	    @Override
	    protected Random initialValue() {
		return new Random();
	    }
	};
	
	Map<String, Set<Object>> dataValues = new HashMap<String, Set<Object>>();
	for (String paramName : PARAMS_NAMES) {
	    dataValues.put(paramName, new HashSet<Object>());
	}
	this.attrsUniqueValues = new HashMap<String, Object[]>();
	
	List<Object> values = new ArrayList<Object>();
	List<String> dataLines = readFileContent(dataFile);
        Map<String, Integer> countPerRegion = new HashMap<String, Integer>();
	for (String line : dataLines) {
	    String[] parts = line.split(",");
	    if (PARAMS_NAMES.length != parts.length) {
		exitError("Data line is missing parameters: " + line);
	    }

	    values.add(parts[0].trim());
	    values.add(parts[1].trim());
	    values.add(parts[2].trim());
	    values.add(new String(""+Integer.parseInt(parts[3].trim())));
	    values.add(Integer.parseInt(parts[4].trim()));
	    values.add(new String(""+Double.parseDouble(parts[5].trim())));
	    values.add(parts[6].trim());
	    values.add(new String(""+Integer.parseInt(parts[7].trim())));
	    values.add(parts[8].trim());
	    values.add(parts[9].trim());
	    values.add(parts[10].trim());
	    values.add(parts[11].trim());
	    values.add(Double.parseDouble(parts[12].trim()));
	    values.add(Double.parseDouble(parts[13].trim()));
	    values.add(parts[14].trim());

            String region = parts[9].trim();
            Integer cnt = countPerRegion.get(region);
            if (cnt == null) {
                cnt = 0;
            }
            cnt++;
            countPerRegion.put(region, cnt);

            if (cnt <= 50) {
                for (int i = 0; i < values.size(); i++) {
                    dataValues.get(PARAMS_NAMES[i]).add(values.get(i));
                }
            }
	    
	    values.clear();
	}
	
	for (String paramName : PARAMS_NAMES) {
	    this.attrsUniqueValues.put(paramName, dataValues.get(paramName).toArray());
	}
    }
    
    /*
     * Thread safe method
     */
    public Query nextQuery() {
	double queryTypeRan = Math.abs(ranGen.get().nextDouble()) * 100.0;
	double queryTemplateRan = Math.abs(ranGen.get().nextDouble()) * 100.0;
	String type = getTypeGivenPerc(queryTypeRan);
	QueryTemplate template = getTypeTemplates(type).ceiling(new QueryTemplate(queryTemplateRan, null));
	if (template == null) {
	    exitError("Could not find a template for type " + type + " and perc " + queryTemplateRan);
	}
	
	int repeatRan = Math.abs(ranGen.get().nextInt()) % REPEAT;
	Map<String, Object> params = new HashMap<String, Object>();
	//Object cachedValue = null; // used to ensure region == postcode if both appear in the query
	for (String attr : template.attributesToQuery) {
	    Object[] uniqueValues = this.attrsUniqueValues.get(attr.equals("postcode") ? "region" : attr);
	    int attrValueRan = Math.abs(ranGen.get().nextInt()) % uniqueValues.length;
	    Object value = uniqueValues[attrValueRan];
	    //if (attr.equals("postcode") || attr.equals("region")) {
	    //	if (cachedValue == null) {
	    //	    cachedValue = value;
	    //	} else {
            //	    value = cachedValue;       
	    //	}
	    //}
	    if (value instanceof String) {
	        String strValue = (String) value;
	        params.put(attr, value + "-" + repeatRan);
	    } else {
	        params.put(attr, value);
	    }
	}
	
	return new Query(type, params);
    }
    
    private String getTypeGivenPerc(double perc) {
	if (perc < this.percInsert) {
	    return "I"; 
	} else if (perc < (this.percInsert + this.percModify)) {
	    return "M";
	} else {
	    return "S";
	}
    }

    private TreeSet<QueryTemplate> getTypeTemplates(String type) {
	if (type.equals("I")) {
	    return this.templatesInsert;
	} else if (type.equals("M")) {
	    return this.templatesModification;
	} else {
	    return this.templatesSearch;
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

    private void exitError(String error) {
	System.err.println(error);
	System.exit(1);
    }

    @Override
    public String toString() {
        String output = "Query Generator I: " + this.percInsert + "%  M: " + this.percModify + "%  S: " + this.percSearch + "%";
        output += "\n" + templatesToString("I", this.templatesInsert);
        output += "\n" + templatesToString("M", this.templatesModification);
        output += "\n" + templatesToString("S", this.templatesSearch);
        return output;
    }
    
    private String templatesToString(String type, TreeSet<QueryTemplate> templates) {
	String output = type;
	double previousRoof = 0.0;
	for (QueryTemplate template : templates) {
	    output += "\n\t]" + previousRoof + ", " + template.percentageRoof + "]";
	    for (String attr : template.attributesToQuery) {
		output += " " + attr;
	    }
	    previousRoof = template.percentageRoof;
	}
	return output;
    }
    
    //public class Query {
    //    
    //    public final String type;
    //    public final Map<String, Object> params;
    //    
    //    public Query(String type, Map<String, Object> params) {
    //        this.type = type;
    //        this.params = params;
    //    }
    //    
    //    @Override
    //    public String toString() {
    //        String output =  this.type;
    //        for (Map.Entry<String, Object> entry : this.params.entrySet()) {
    //    	output += " " + entry.getKey() + ":" + entry.getValue();
    //        }
    //        return output;
    //    }
    //}
    
    /*
     * A QueryTemplate <Q> is parametrized by a percentage roof <R>.
     * There is a set of Qs: Q1, Q2, Q3
     * 
     * The interval for which Q1 is responsible is ]0, Q1.R]
     * For Q2 it is: ]Q1.R, Q2.R]
     * And for Q3: ]Q2.R, Q3.R]
     * 
     * Invariant: the last Q, namely Q3 in this example, should have R = 100.0
     */
    private class QueryTemplate implements Comparable<QueryTemplate> {

	private final double percentageRoof;
	private final List<String> attributesToQuery;

	QueryTemplate(double percentageRoof, List<String> attributes) {
	    this.percentageRoof = percentageRoof;
	    this.attributesToQuery = attributes;
	}

	@Override
	public int compareTo(QueryTemplate other) {
	    if (this.percentageRoof > other.percentageRoof) {
		return 1;
	    } else if (this.percentageRoof == other.percentageRoof) {
		return 0;
	    } else {
		return -1;
	    }
	}

    }
}
