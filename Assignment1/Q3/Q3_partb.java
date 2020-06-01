import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.URIref;
import org.json.JSONArray;
import org.json.JSONObject;

public class Q3_partb {

	public static void main(String[] args) 
    {
		
		String BASE = "http://api.conceptnet.io";					// creating a base url

		Model model = ModelFactory.createDefaultModel();
		
		BufferedReader buf = null;
		try {
			InputStream is = new FileInputStream(args[0]);	// opening the json file in input stream
			buf = new BufferedReader(new InputStreamReader(is));
			String line = buf.readLine();
			StringBuilder sb = new StringBuilder();
			while (line != null) {
				sb.append(line).append("\n");
				line = buf.readLine();
			}
			String fileAsString = sb.toString();							// storing the json file as string
			
			JSONObject jsonObject = new JSONObject(fileAsString);			// opening the file stored as string in JSONObject
 
			JSONArray jsonArray = jsonObject.getJSONArray("edges");			// getting the value of edges
 
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject startObj = (JSONObject)jsonArray.getJSONObject(i).get("start");	//getting the value of start
				
				Resource startId = model.createResource(BASE+URIref.encode(String.valueOf(startObj.get("@id"))));	// make a resource for id
				Property startP1 = model.createProperty(BASE + "/language");		// make language a property
				model.setNsPrefix("language", BASE+"/language");					// make language url as prefix
				RDFNode v1 = model.createTypedLiteral(startObj.get("language"), XSDDatatype.XSDstring);	// store the value of language as string
				startId.addProperty(startP1, v1);									// add language property to the resource
				if(!startObj.isNull("sense_label")) {								// if sense_label is present in the start key
					Property startP2 = model.createProperty(BASE + "/sense_label");	// make sense_label a property
					model.setNsPrefix("sense_label", BASE+"/sense_label");			// make sense_label as prefix
					RDFNode v2 = model.createTypedLiteral(startObj.get("sense_label"), XSDDatatype.XSDstring);	// store the value of sense_label as string
					startId.addProperty(startP2, v2);								// add sense_label property to the resource
					
				}
				
				JSONObject relObj = (JSONObject)jsonArray.getJSONObject(i).get("rel");

				Resource relId = model.createResource(BASE+relObj.get("@id"));
				Property relP2 = model.createProperty(BASE+relObj.get("@id"));
				if(String.valueOf(relObj.get("@id")).toLowerCase().contains("synonym")) {
					model.setNsPrefix("synonym", BASE+relObj.get("@id"));
				}
				else {
					model.setNsPrefix("relatedTo", BASE+relObj.get("@id"));
				}
				Property relP1 = model.createProperty(BASE + "/language"); 

				JSONObject endObj = (JSONObject)jsonArray.getJSONObject(i).get("end");

				Resource endId = model.createResource(BASE+endObj.get("@id"));
				Property endP1 = model.createProperty(BASE + "/language");
				Property endP2 = model.createProperty(BASE + endObj.get("@id"));
				RDFNode endV1 = model.createTypedLiteral(endObj.get("language"), XSDDatatype.XSDstring);
				endId.addProperty(endP1, endV1);
				
				if(!endObj.isNull("sense_label")) {
					Property endP3 = model.createProperty(BASE + "/sense_label");
					model.setNsPrefix("sense_label", BASE+"/sense_label");
					RDFNode endV2 = model.createTypedLiteral(endObj.get("sense_label"), XSDDatatype.XSDstring);
					endId.addProperty(endP3, endV2);	
				}
				
				startId.addProperty(relP2, endP2);		// add relation between the edges
		        
			}
			model.write(new FileOutputStream("A1_MT19070_Q3.ttl"), "TURTLE");	
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				buf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
	
}
