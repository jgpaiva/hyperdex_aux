import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


import hyperclient.HyperClient;
import hyperclient.Range;
import hyperclient.SearchBase;

public class Client {

    static final String configFile = "/home/morazow/Runs/Config";
    static final String dataFile = "/home/morazow/Runs/data.dat";
    static final QueryGenerator gen = new QueryGenerator(configFile, dataFile);
    static private String COORD_IP;// = "146.193.41.74";
    static private final int MAX_THR = 32;
    static private final int SECS    = 180;
    static private final int MIN     = 30;
    static private final double NANO = 1000000000.0;
    static Random rand = new Random();

    public static class ClientThread extends Thread {
	private int id;
	public volatile int cnt;
        private HashMap<Integer, Long> countMap;
        public long searchLat, updateLat;
        public long searchCount, updateCount, getLat;

	public ClientThread(int _id) {
	    this.id = _id;
	    this.cnt = 0;
            this.countMap = new HashMap<Integer, Long>();
            this.searchLat = 0L;
            this.updateLat = 0L;
            this.searchCount = 0L;
            this.updateCount = 0L;
            this.getLat = 0L;
	}

	public void run() {

	    HyperClient m_client = new HyperClient(COORD_IP, 1982);
	    SearchBase s;
            Random rand = new Random();

	    try {
		long end, begin = System.nanoTime();
		hello:while (true) {
		    Query nextQuery = gen.nextQuery();
		    int error = 0;
		    while (true) {
			try {

			    if (nextQuery.type.equals("S")) {
                                Map<String, Object> values = new HashMap<String, Object>();
                                for (Map.Entry<String, Object> entry : nextQuery.params.entrySet()) {
                                    //values.put(entry.getKey(), Integer.toString(rand.nextInt()));
                                    values.put(entry.getKey(), entry.getValue());
                                }
                                int querySize = values.size();
                                Long queryCount = countMap.get(querySize);
                                if (queryCount == null) {
                                    queryCount = 0L;
                                }
                                end = System.nanoTime();
                                if ((end - begin) / NANO > MIN){
                                  queryCount++;
                                  countMap.put(querySize, queryCount);
                                }

                                long lb = System.nanoTime();
				s = m_client.search("hotels", values);

				int count=0;
				while (s.hasNext()) {

				    s.next();
				    count++;
				    end = System.nanoTime();
				    if ((end - begin) / NANO > SECS) {
					System.out.println("DID_NOT_FINISH Counter = "+(cnt+1)+" ID = "+id+" Error = " +error + " Query = " + nextQuery+" Count = "+count);
					break hello; 
				    }
				}
                                long le = System.nanoTime();
                                end = System.nanoTime();
                                if ((end - begin) / NANO > MIN){
                                  searchLat += (le - lb);
                                  searchCount++;
                                }


			    } else if (nextQuery.type.equals("M")) {
				String key = (String)nextQuery.params.get("factual_id");
                                long lb = System.nanoTime();
				Map row = m_client.get("hotels", key);
                                long le = System.nanoTime();
                                end = System.nanoTime();
                                if ((end - begin) / NANO > MIN){
                                  getLat += (le - lb);
                                }
                                
                                if (row == null) {
                                    System.err.println("NULL Key = "+key);
                                }

				Map<String, Object> values = new HashMap<String, Object>();
				for (int i = 0; i < QueryGenerator.PARAMS_NAMES.length; i++) {
				    String attrName = QueryGenerator.PARAMS_NAMES[i];
				    values.put(attrName, row.get(attrName));
				}
                                for (String kk: nextQuery.params.keySet()){
                                    Object t = nextQuery.params.get(kk);
                                    if(t instanceof String)
                                        nextQuery.params.put(kk,t);
                                }
				values.putAll(nextQuery.params);
                                values.remove("factual_id");

                                //System.err.println("Key = "+key+" Values = "+values);
                                lb = System.nanoTime();
				m_client.put("hotels", key, values);
                                le = System.nanoTime();
                                end = System.nanoTime();
                                if ((end - begin) / NANO > MIN){
                                  updateLat += (le - lb);
                                  updateCount++;
                                }

			    } else {
				// use nextQuery.params as the Map with values for a new insert
			    }

			    end = System.nanoTime();
			    if ((end - begin) / NANO > SECS)
				break hello; 

                            if ((end - begin) / NANO > MIN)
                              cnt++;

                            break;
			}
			catch (Exception e) {
			    error++;
                            e.printStackTrace();
			    end = System.nanoTime();
			    if ((end - begin) / NANO > SECS) {
				System.out.println("Couldn finish search, tried = "+error+" "+e);
				break hello;
			    }
			}

		    }
		}
	    }
	    catch (Throwable t) {
		throw new RuntimeException(t);
	    }

	}
    }

    private static String shuffleStr(String word) {  
        List<Character> characters = new ArrayList<Character>();  
        for(char c : word.toCharArray()) {  
            characters.add(c);
        }  
        Collections.shuffle(characters);  
        StringBuilder sb = new StringBuilder();  
        for(char c : characters) {  
            sb.append(c);  
        }  
        return sb.toString();  
    }  

    public static void main(String[] args) {
        if (args.length > 0)
            COORD_IP = args[0];
        else
            System.out.println("CLIENT REQUIRES COORD IP");

	ClientThread[] clients = new ClientThread[MAX_THR];
	for (int i = 0; i < clients.length; i++) {
	    clients[i] = new ClientThread(i);
	}

	System.out.println("STARTING");
	long startTime = System.nanoTime();

	try {
	    for (int i = 0; i < clients.length; i++)
		clients[i].start();

	    for (int i = 0; i < clients.length; i++) {
		clients[i].join((long)(SECS * 1000 * 1.1));
		if (clients[i].isAlive()) 
		    break;
	    }
	} catch (Throwable t) {
	    throw new RuntimeException(t);
	}
        
        HashMap<Integer, Long> countMap = new HashMap<Integer, Long>();
        long searchLat = 0L, updateLat = 0L;
        long searchCount = 0L, updateCount = 0L;
        long getLat = 0L;

        long sum = 0;
	for (int i = 0; i < clients.length; i++) {
	    sum += clients[i].cnt;
            searchLat += clients[i].searchLat;
            updateLat += clients[i].updateLat;
            searchCount += clients[i].searchCount;
            updateCount += clients[i].updateCount;
            getLat += clients[i].getLat;
            for (Map.Entry<Integer, Long> entry : clients[i].countMap.entrySet()) {
                Long queryCount = countMap.get(entry.getKey());
                if (queryCount == null) {
                    queryCount = 0L;
                }
                queryCount += entry.getValue();
                countMap.put(entry.getKey(), queryCount);
            }
	}
        System.err.println(countMap);
        System.err.println("Gozleg = "+(double)searchLat/searchCount);
        System.err.println("Update = "+(double)updateLat/updateCount);
        System.err.println("Get = "+(double)getLat/updateCount);
        System.err.println("Gozleg Count = "+searchCount);
        System.err.println("Update Count = "+updateCount);

	long endTime = System.nanoTime();
	double secs = (endTime - startTime)/1000000000.0;
	System.out.println("DONE");        
	System.out.println(SECS + " secs, " + (((double)sum) / (SECS-MIN)) + " ops/sec"); 

	System.exit(0);
    }
}
