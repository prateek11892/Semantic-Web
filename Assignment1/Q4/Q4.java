import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.URIref;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import au.com.bytecode.opencsv.CSVReader;

public class Q4 {

	public static void main(String[] args) {
		CSVReader reader = null;
		String BASE = "http://www.example.org/";					// creating a base url

		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("semweb", BASE);							// set prefix for semweb
		
		Resource typeClass = ResourceFactory.createResource("http://www.example.org/type");		// create a resource type
        Resource movie = ResourceFactory.createResource("http://www.example.org/movie");		// create a resource movie
        Resource tv_show = ResourceFactory.createResource("http://www.example.org/tvshow");		// create a resource tv show
        
        model.setNsPrefix("type", BASE+"type");							// set prefix for type
        model.setNsPrefix("movie", BASE+"type/movie");					// set prefix for movie
        model.setNsPrefix("tvshow", BASE+"type/tvshow");				// set prefix for tv show
        
        
        model.add (typeClass, RDF.type, RDFS.Class);				// defining type to be a type of class
        model.add (movie, RDFS.subClassOf, typeClass);				// movie is a subclass of type
        model.add (tv_show, RDFS.subClassOf, typeClass);			// tv show is a subclass of type
        
		try {
			//reader = new CSVReader(new FileReader("G:\\Workspace\\Assignment1Q4\\src\\NetflixList.csv"));
			reader = new CSVReader(new FileReader(args[0]));		// reading the csv file
			String[] readNextLine;
			readNextLine = reader.readNext();
			String[] headers = new String[readNextLine.length];			 
			for (int i = 0; i < readNextLine.length; i++) {
				headers[i] = "has" + readNextLine[i].replaceFirst(String.valueOf(readNextLine[i].charAt(0)),
						String.valueOf(Character.toUpperCase(readNextLine[i].charAt(0))));		// reading the headers and defining them to be a property
			}
			while ((readNextLine = reader.readNext()) != null) {
				// Resource r1 = model.createResource(BASE + nextLine[2].replaceAll(" ", "_"));
				Resource r1 = model.createResource(BASE + URIref.encode(readNextLine[2]));		// creating the resource for title and encoding for special characters 
				for (int i = 0; i < readNextLine.length; i++) {
					if (readNextLine[i].length() > 0) {											// if the particular property has value then only reading
						if (i != 2) {
							Property p1 = model.createProperty(BASE + headers[i]);				// making of property
							RDFNode v1;
							if (i == 0) {
								v1 = model.createTypedLiteral(readNextLine[i], XSDDatatype.XSDinteger);		// show_id is at 0th position and hence of integer type
								r1.addProperty(p1, v1);											// add property to resource
							}
							else if(i == 1){													// 1st position corresponds to type
								if(readNextLine[i].equalsIgnoreCase("Movie")) {		
									model.add(r1,RDF.type, movie);								// defining type to each rdf triple
								}
								else if(readNextLine[i].equalsIgnoreCase("TV Show")) {
									model.add(r1,RDF.type, tv_show);							// defining type to each rdf triple
								}
							}
							else {
								String[] internalElements = (readNextLine[i].split(","));		
								if (internalElements.length > 1) {								// if there are multiple values corresponding to a single property
									for (int j = 0; j < internalElements.length; j++) {
										v1 = model.createTypedLiteral(internalElements[j].strip(),
												XSDDatatype.XSDstring);							// for multiple values corresponding to single property, create a literal
										r1.addProperty(p1, v1);									// and add to the resource
									}
								} else {
									v1 = model.createTypedLiteral(readNextLine[i], XSDDatatype.XSDstring);	// all other properties are of string type
									r1.addProperty(p1, v1);
								}
							}
						}
					}
				}
			}
			model.setNsPrefixes(PrefixMapping.Standard);
			model.write(new FileOutputStream("A1_Q4.ttl"), "TURTLE");					// write to a turtle file
			//System.out.println(URIref.decode("Joaqu%C3%83%C2%ADn%20Reyes:%20Una%20y%20no%20m%C3%83%C2%A1s"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
