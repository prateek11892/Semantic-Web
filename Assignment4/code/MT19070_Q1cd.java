package com.prateek.assignment4;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class App {
	
	public static Properties readConfiguration() {
    	Properties prop=new Properties();
    	try {
			FileInputStream ip= new FileInputStream("config.properties");
			prop.load(ip);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return prop;
    }
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		Properties prop = readConfiguration();
		
		DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
		// Build the Fuseki Server on mentioned port and create a dataset
		FusekiServer server = FusekiServer.create().setPort(Integer.parseInt(prop.getProperty("port")))
													.add("/ds", dsg, true).build();
		server.start();
		
		UpdateRequest request = UpdateFactory.create() ;

		// Load the triples into the default graph Q1 c) ii)
		Txn.executeWrite(dsg, ()->RDFDataMgr.read(dsg, prop.getProperty("ttlfilepath")));
		
		// Create two named graphs in the store Q1 c) i)
		request.add("CREATE GRAPH "+prop.getProperty("newmovies_IRI"));
		request.add("CREATE GRAPH "+prop.getProperty("oldmovies_IRI"));
		
		// Q1 c) iii)
		// Move the movies whose release year is on or after 2016 to the new
		// movies graph and the rest of them to the old movies graph. After this
		// operation, there should not be any triples in the default graph.
		
		// Add the movies prior to year 2016 to the old movies graph
		String oldMoviesGraphQuery = "PREFIX schema: "+prop.getProperty("schema_IRI")+"\r\n"
				+ "INSERT \r\n" + 
				"  { \r\n" + 
				"    GRAPH "+prop.getProperty("oldmovies_IRI")+" \r\n" + 
				"      {\r\n" + 
				"        ?title  ?p  ?o . \r\n" + 
				"      }\r\n" + 
				"  }\r\n" + 
				"WHERE\r\n" + 
				"  { \r\n" + 
				"  	{\r\n" + 
				"    	?title ?p ?o.\r\n" + 
				"        ?title  schema:releasedYear ?year.FILTER(?year<\"2016\").  \r\n" + 
				"  	}  \r\n" + 
				"  }  ";
		// Add the movies after the year 2016 to the new movies graph
		String newMoviesGraphQuery = "PREFIX schema: "+prop.getProperty("schema_IRI")+"\r\n"
				+"INSERT \r\n" + 
				"  { \r\n" + 
				"    GRAPH "+prop.getProperty("newmovies_IRI")+" \r\n" + 
				"      {\r\n" + 
				"        ?title  ?p  ?o . \r\n" + 
				"      }\r\n" + 
				"  }\r\n" + 
				"WHERE\r\n" + 
				"  { \r\n" + 
				"  	{\r\n" + 
				"    	?title ?p ?o.\r\n" + 
				"        ?title  schema:releasedYear ?year.FILTER(?year>=\"2016\").  \r\n" + 
				"  	}  \r\n" + 
				"  }";
		
		// Delete from the default graph the movies before the year 2016
		String deleteOldQuery = "PREFIX schema: "+prop.getProperty("schema_IRI")+" \r\n"
				+"DELETE { ?title ?p ?o }\r\n" + 
				"WHERE\r\n" + 
				"  { \r\n" + 
				"  		?title ?p ?o.\r\n" + 
				"      ?title schema:releasedYear ?year.FILTER(?year<\"2016\"). \r\n" + 
				"  }";
		
		// Delete from the default graph the movies after the year 2016
		String deleteNewQuery = "PREFIX schema: "+prop.getProperty("schema_IRI")+"\r\n"
				+"DELETE { ?title ?p ?o }\r\n" + 
				"WHERE\r\n" + 
				"  { \r\n" + 
				"  		?title ?p ?o.\r\n" + 
				"      ?title schema:releasedYear ?year.FILTER(?year>=\"2016\"). \r\n" + 
				"  }";
		
		// Add the remaining triples in the old movies graph
		String addToBothGraphs1 = "INSERT \r\n" + 
				"  { \r\n" + 
				"    GRAPH "+prop.getProperty("newmovies_IRI")+" \r\n" + 
				"      {\r\n" + 
				"        ?title  ?p  ?o . \r\n" + 
				"      }\r\n" + 
				"  }\r\n" + 
				"WHERE\r\n" + 
				"  { \r\n" + 
				"  	{\r\n" + 
				"    	?title ?p ?o.        \r\n" + 
				"  	}  \r\n" + 
				"  }   ";
		
		// Add the remaining triples in the new movies graph
		String addToBothGraphs2 = "INSERT \r\n" + 
				"  { \r\n" + 
				"    GRAPH "+prop.getProperty("oldmovies_IRI")+" \r\n" + 
				"      {\r\n" + 
				"        ?title  ?p  ?o . \r\n" + 
				"      }\r\n" + 
				"  }\r\n" + 
				"WHERE\r\n" + 
				"  { \r\n" + 
				"  	{\r\n" + 
				"    	?title ?p ?o.        \r\n" + 
				"  	}  \r\n" + 
				"  }   ";
		
		// Delete the triples from the default graph
		String deleteFromOld = "  DELETE { ?title ?p ?o }\r\n" + 
				"WHERE\r\n" + 
				"  { \r\n" + 
				"  		?title ?p ?o.\r\n" + 
				"      \r\n" + 
				"  }";
		// Now after the addition of the the triples from the default graph to the 
		// new movies graph and old movies graph, we have nothing left in the default graph
		
		// Q1 (d)
		// Adding the triple for the actor Aamir_Khan using the owl:sameAs property.
		String insertActor = "PREFIX semweb:"+prop.getProperty("semweb_IRI")+"\r\n"+
				"PREFIX owl:"+prop.getProperty("owl_IRI") +"\r\n"+
				"INSERT DATA {\r\n" + 
				"    GRAPH "+prop.getProperty("oldmovies_IRI")+" {\r\n" + 
				"		semweb:Aamir_Khan  owl:sameAs  <http://dbpedia.org/page/Aamir_Khan> \r\n" + 
				"	}\r\n" + 
				"}";
		// Adding the triple for the actor Ayushmann_Khurrana using the owl:sameAs property.
		String insertActor2 = "PREFIX semweb:"+prop.getProperty("semweb_IRI")+"\r\n"+
				"PREFIX owl:"+prop.getProperty("owl_IRI") +"\r\n"+
				"INSERT DATA {\r\n" + 
				"    GRAPH "+prop.getProperty("oldmovies_IRI")+" {\r\n" + 
				"		semweb:Ayushmann_Khurrana  owl:sameAs  <http://dbpedia.org/page/Ayushmann_Khurrana> \r\n" + 
				"	}\r\n" + 
				"}";
		// Adding the triple for the country India using the owl:sameAs property.
		String insertCountry = "PREFIX semweb:"+prop.getProperty("semweb_IRI")+"\r\n"+
				"PREFIX owl:"+prop.getProperty("owl_IRI") +"\r\n"+
				"INSERT DATA {\r\n" + 
				"    GRAPH "+prop.getProperty("oldmovies_IRI")+" {\r\n" + 
				"		semweb:India  owl:sameAs  <https://www.geonames.org/1269750> \r\n" + 
				"	}\r\n" + 
				"}";
		// Adding the triple for the country Thailand using the owl:sameAs property.
		String insertCountry2 = "PREFIX semweb:"+prop.getProperty("semweb_IRI")+"\r\n"+
				"PREFIX owl:"+prop.getProperty("owl_IRI") +"\r\n"+
				"INSERT DATA {\r\n" + 
				"    GRAPH "+prop.getProperty("oldmovies_IRI")+" {\r\n" + 
				"		semweb:Thailand  owl:sameAs  <https://www.geonames.org/1605651> \r\n" + 
				"	}\r\n" + 
				"}";
		request.add(oldMoviesGraphQuery);
		request.add(newMoviesGraphQuery);
		request.add(deleteOldQuery);
		request.add(deleteNewQuery);
		request.add(addToBothGraphs1);
		request.add(addToBothGraphs2);
		request.add(deleteFromOld);
		
		request.add(insertActor);
		request.add(insertActor2);
		request.add(insertCountry);
		request.add(insertCountry2);
		UpdateAction.execute(request, dsg);
		
		// running a select query to check whether all the triples have moved to the named graphs
		String queryToExecute = "SELECT DISTINCT ?s ?g { {?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }";
		query("http://localhost:" + Integer.parseInt(prop.getProperty("port")) + "/ds/query", queryToExecute, qExec -> {
            ResultSet rs = qExec.execSelect();
            while(rs.hasNext()) {
            	System.out.println(rs.next());
            }
        });
		//server.stop();       
	}
	static void query(String URL, String query, Consumer<QueryExecution> body) {
        try (QueryExecution qExec = QueryExecutionFactory.sparqlService(URL, query)) {
            body.accept(qExec);
        }
    }
}
