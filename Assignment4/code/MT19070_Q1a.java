package com.fuseki.assignment4;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.URIref;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import au.com.bytecode.opencsv.CSVReader;

public class Ont extends Object {
        
    public static void main (String args[]) {
    	
        // create an empty model
        Model ontmodel = ModelFactory.createDefaultModel();
        
        Properties prop = readConfiguration();
        
		CSVReader reader = null;

		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("semweb", prop.getProperty("BASE"));							// set prefix for semweb
		model.setNsPrefix("schema", prop.getProperty("BASE1"));
		model.setNsPrefix("classification", prop.getProperty("BASE2"));
		model.setNsPrefix("objectrole", prop.getProperty("BASE3"));
		model.setNsPrefix("agentrole", prop.getProperty("BASE4"));
		model.setNsPrefix("event", prop.getProperty("BASE5"));
		
        // use the FileManager to find the input file
        InputStream in = FileManager.get().open(prop.getProperty("inputFileName"));
        if (in == null) {
            throw new IllegalArgumentException( "File: " + prop.getProperty("inputFileName") + " not found");
        }
        
        // read the RDF/XML file
        ontmodel.read(new InputStreamReader(in), "");
       
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, ontmodel);
        ExtendedIterator<OntClass> classes = m.listNamedClasses();
        
        while(classes.hasNext()) {
        	OntClass c = (OntClass)classes.next();
        	if(c.hasSuperClass()) {
	        	model.add(ResourceFactory.createResource(c.getURI()), RDFS.subClassOf, ResourceFactory.createResource(c.getSuperClass().getURI()));
	        	/*System.out.println(c.getSuperClass().getURI());
	        	System.out.println(c.getURI());
	        	System.out.println("Namespace::"+c.getNameSpace());*/
        	}
        }
        model.add(ResourceFactory.createResource(prop.getProperty("BASE1")+"Actor"),RDFS.subClassOf, ResourceFactory.createResource(prop.getProperty("BASE1")+"Person"));
        System.out.println("------------------------");
        System.out.println("Printed Classes");
        
        ExtendedIterator<ObjectProperty> properties = m.listObjectProperties();
        
        while(properties.hasNext()) {
        	ObjectProperty p = (ObjectProperty)properties.next();
        	/*System.out.println(p.getURI());
        	System.out.println("Domain:"+p.getDomain());
        	System.out.println("Range:"+p.getRange());*/
        	if(null!= p.getDomain() && null != p.getRange()) {
        		model.add(ResourceFactory.createResource(p.getURI()),RDFS.domain,p.getDomain());
        		model.add(ResourceFactory.createResource(p.getURI()),RDFS.range,p.getRange());
        	}
        	System.out.println();
        }
        
        Model returnedModel = createTriples(model);
        
        try {
        	returnedModel.setNsPrefixes(PrefixMapping.Standard);
			returnedModel.write(new FileOutputStream("A4_MT19070_Q4.ttl"), "TURTLE");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
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
    
    public static Model createTriples(Model model) {
		CSVReader reader = null;
		
		Properties prop = readConfiguration();
		
		//Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("semweb", prop.getProperty("BASE"));							// set prefix for semweb
		
		Resource typeClass = ResourceFactory.createResource("http://www.example.org/type");		// create a resource type
        Resource movie = ResourceFactory.createResource("http://www.example.org/movie");		// create a resource movie
        Resource tv_show = ResourceFactory.createResource("http://www.example.org/tvshow");		// create a resource tv show
        
        model.setNsPrefix("type", prop.getProperty("BASE")+"type");							// set prefix for type
        model.setNsPrefix("movie", prop.getProperty("BASE")+"type/movie");					// set prefix for movie
        model.setNsPrefix("tvshow", prop.getProperty("BASE")+"type/tvshow");				// set prefix for tv show
        
        
        model.add (typeClass, RDF.type, RDFS.Class);				// defining type to be a type of class
        model.add (movie, RDFS.subClassOf, typeClass);				// movie is a subclass of type
        model.add (tv_show, RDFS.subClassOf, typeClass);			// tv show is a subclass of type
        
		try {
			reader = new CSVReader(new FileReader(prop.getProperty("file")));
			String[] readNextLine;
			readNextLine = reader.readNext();
			String[] headers = new String[readNextLine.length];
			String[] originalHeaders = new String[readNextLine.length];
			for (int i = 0; i < readNextLine.length; i++) {
				headers[i] = "has" + readNextLine[i].replaceFirst(String.valueOf(readNextLine[i].charAt(0)),
						String.valueOf(Character.toUpperCase(readNextLine[i].charAt(0))));		// reading the headers and defining them to be a property
				originalHeaders[i] = readNextLine[i];
			}
			while ((readNextLine = reader.readNext()) != null) {
				//Resource r1 = model.createResource(BASE + readNextLine[2].replaceAll(" ", "_"));
				Resource r1 = model.createResource(prop.getProperty("BASE") + URIref.encode(readNextLine[2]));		// creating the resource for title and encoding for special characters 
				for (int i = 0; i < readNextLine.length; i++) {
					if (readNextLine[i].length() > 0) {											// if the particular property has value then only reading
						Property p1;
						if(prop.containsKey(originalHeaders[i])){
							//System.out.println(prop.getProperty(headers[i]));
							p1 = model.getProperty(prop.getProperty("BASE1")+prop.getProperty(originalHeaders[i]));
						}
						else {
							p1 = model.createProperty(prop.getProperty("BASE1") + headers[i]);				// making of property
						}
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
							else if(i == 5) {
								String[] internalElements = readNextLine[i].split(",");		
								if (internalElements.length > 1) {								// if there are multiple values corresponding to a single property
									for (int j = 0; j < internalElements.length; j++) {
										//v1 = model.createTypedLiteral(internalElements[j].strip(),
												//XSDDatatype.XSDstring);							// for multiple values corresponding to single property, create a literal
										//r1.addProperty(p1, v1);									// and add to the resource
										if(internalElements[j].equalsIgnoreCase("India") || internalElements[j].equalsIgnoreCase("Thailand")) {
											r1.addProperty(p1, ResourceFactory.createResource(prop.getProperty("BASE")+URIref.encode(internalElements[j].strip().replace(".","").replaceAll(" ", "_"))));
										}
										else {
											v1 = model.createTypedLiteral(internalElements[j], XSDDatatype.XSDstring);	// all other properties are of string type
											r1.addProperty(p1, v1);
										}
									}
								} else {
									//v1 = model.createTypedLiteral(readNextLine[i], XSDDatatype.XSDstring);	// all other properties are of string type
									//r1.addProperty(p1, v1);
									if(readNextLine[i].equalsIgnoreCase("India") || readNextLine[i].equalsIgnoreCase("Thailand")) {
										r1.addProperty(p1, ResourceFactory.createResource(prop.getProperty("BASE")+URIref.encode(readNextLine[i].replace(".","").replaceAll(" ", "_"))));
									}
									else {
										v1 = model.createTypedLiteral(readNextLine[i], XSDDatatype.XSDstring);	// all other properties are of string type
										r1.addProperty(p1, v1);
									}
								}
							}
							else if(i == 6){													// 6th position corresponds to date added
								v1 = model.createTypedLiteral(readNextLine[i], XSDDatatype.XSDstring);	// all other properties are of string type
								r1.addProperty(p1, v1);
							}
							else if(i == 3 || i == 4){													// 6th position corresponds to date added
								String[] internalElements = readNextLine[i].split(",");		
								if (internalElements.length > 1) {								// if there are multiple values corresponding to a single property
									for (int j = 0; j < internalElements.length; j++) {
										//v1 = model.createTypedLiteral(internalElements[j].strip(),
												//XSDDatatype.XSDstring);							// for multiple values corresponding to single property, create a literal
										//r1.addProperty(p1, v1);									// and add to the resource
										if(internalElements[j].equalsIgnoreCase("Aamir Khan") ) {
											r1.addProperty(p1, ResourceFactory.createResource(prop.getProperty("BASE")+URIref.encode(internalElements[j].strip().replace(".","").replaceAll(" ", "_"))));
										}
										else {
											v1 = model.createTypedLiteral(internalElements[j].strip(),
													XSDDatatype.XSDstring);							// for multiple values corresponding to single property, create a literal
											r1.addProperty(p1, v1);									// and add to the resource
										}
									}
								} else {
									//v1 = model.createTypedLiteral(readNextLine[i], XSDDatatype.XSDstring);	// all other properties are of string type
									//r1.addProperty(p1, v1);
									if(readNextLine[i].equalsIgnoreCase("Aamir Khan") ) {
										r1.addProperty(p1, ResourceFactory.createResource(prop.getProperty("BASE")+URIref.encode(readNextLine[i].replace(".","").replaceAll(" ", "_"))));
									}
									else {
										v1 = model.createTypedLiteral(readNextLine[i].strip(),
												XSDDatatype.XSDstring);							// for multiple values corresponding to single property, create a literal
										r1.addProperty(p1, v1);									// and add to the resource
									}
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
						//}
					}
				}
			}
			//model.setNsPrefixes(PrefixMapping.Standard);
			//model.write(new FileOutputStream("A3_MT19070_Q4.ttl"), "TURTLE");					// write to a turtle file
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
		return model;
	}
}