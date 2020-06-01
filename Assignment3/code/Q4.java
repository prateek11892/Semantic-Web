package com.prateek.mavenproj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owl.explanation.impl.blackbox.checker.InconsistentOntologyExplanationGeneratorFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.jfact.JFactFactory;

public class Assignment3 {

	HashMap<String, ArrayList<String>> mapHermit = new HashMap<>(); // map for storing the superclasses w.r.t the two reasoners
	HashMap<String, ArrayList<String>> mapJFact = new HashMap<>();
		
	
	/* Function to get the reasoner factory object*/
	public OWLReasonerFactory getReasoner(String reasoner) {
		if (reasoner.equalsIgnoreCase("HermiT") )
			return new ReasonerFactory();
		else if(reasoner.equalsIgnoreCase("JFact"))
			return new JFactFactory(); 
		return null;
	}
	
	/* Function to get the superclasses of a given class*/
	public void getSuper(OWLOntology o, OWLReasoner r, String reasoner) {
		PrintStream ps;
		try {
			ps = new PrintStream(new File("MT19070_Q4.txt"));
		
			HashMap<String, ArrayList<String>> map = new HashMap<>();		
			NodeSet<OWLClass> returnedClasses;
			for (OWLClass c : o.getClassesInSignature()) { // For all the classes present in the ontology
				
				returnedClasses = r.getSuperClasses(c, true); // Capture the returned superclasses
				
				for(Node<OWLClass> x:returnedClasses) {
					ArrayList<String> superclassList = new ArrayList<>();
					Set<OWLClass> entitySet = x.getEntities();
					for(OWLClass y: entitySet) {
						superclassList.add(y.getIRI().getShortForm()); // Get names for all the classes without the IRI
					}
					map.put(c.getIRI().getShortForm(), superclassList); // Store the names of superclass corresponding to the particular subclass
				}
			}
			if (reasoner.equalsIgnoreCase("HermiT") ) {
				mapHermit = map;	// Store the map corresponding ton HermiT reasoner in the hermit map
				System.setOut(ps);
				System.out.println("HermiT Reasoner Superclasses");
				System.out.println(mapHermit);
			}
			else if(reasoner.equalsIgnoreCase("JFact")) {
				mapJFact = map;		//Store the map corresponding to the JFact reasoner in the JFact map
				System.setOut(ps);
				System.out.println("JFact Reasoner Superclasses");
				System.out.println(mapJFact);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}
	public static void main(String[] args) {
		Assignment3 fp = new Assignment3();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				
		OWLOntology o;

		try {
			File file = new File("C://Users\\\\PrateekAgarwal\\\\Desktop\\\\assignment3.owl");
			o = man.loadOntologyFromOntologyDocument(file);
			List<String> arrList = new ArrayList<>();
			arrList.add("HermiT");
			arrList.add("JFact");
			for(String reasoner: arrList) {
				
				OWLReasonerFactory rf = fp.getReasoner(reasoner); // Get the reasoner factory
				
				OWLReasoner r = rf.createReasoner(o);
				r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
				
				if(r.isConsistent()) {
					// Get super classes
					fp.getSuper(o, r, reasoner); // Get the superclasses of classes in the ontology
				}
				else {
					System.out.println("The input ontology is inconsistent");
				}
			}
			fp.compareOutput(); // To compare the output of the two reasoners
			fp.generateExplanation(o);
			fp.checkInconsistent(o);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	/*Function to compare the output of two reasoners*/
	public void compareOutput() {
		PrintStream ps;
		try {
			ps = new PrintStream(new File("MT19070_Q4.txt"));
			// To compare the outputs of HermiT and JFact reasoners
			ArrayList<String> hermitList;
			ArrayList<String> jfactList;
			HashMap<String, ArrayList<String>> mapHermitDiff = new HashMap<>();// store all extra elements present in HermiT
			HashMap<String, ArrayList<String>> mapJFactDiff = new HashMap<>();// store all extra elements present in JFact
			for(String key: mapHermit.keySet()) {
				hermitList = mapHermit.get(key);	// Get the superclasses of a particular class from the HermiT reasoner
				jfactList = mapJFact.get(key);		// Get the superclasses of a particular class from the JFact reasoner
				if(!hermitList.equals(jfactList)) {	// if both the reasoners are not containing same superclasses
					hermitList.removeAll(jfactList);	// check what all classes are extra in hermit
					mapHermitDiff.put(key, hermitList); // put those classes in the difference
					hermitList = mapHermit.get(key);
					jfactList.removeAll(hermitList);	// check what all classes are different in JFact
					mapJFactDiff.put(key, jfactList);	// put those classes in the difference
				}
			}
			System.setOut(ps);
			System.out.println("Superclasses present in Hermit but not in JFact");
			System.out.println(mapHermitDiff);
			System.out.println("Superclasses present in JFact but not in Hermit");
			System.out.println(mapJFactDiff);
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/* Function to generate explanation for consistent ontology - subclassof relationship */
	public void generateExplanation(OWLOntology ont) {
		
	    OWLReasonerFactory rf = new JFactFactory();
		    
		try {
			PrintStream ps = new PrintStream(new File("MT19070_Q4.txt"));
			System.setOut(ps);
		    OWLOntologyManager m = ont.getOWLOntologyManager();
		    OWLDataFactory df = m.getOWLDataFactory();

		    // Create an explanation generator factory for the consistent ontology
		    ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(rf);
		    
		    ExplanationGenerator<OWLAxiom> gen = genFac.createExplanationGenerator(ont);
		    // case 1 - A subClassOf B where A and B are simple classes
		    IRI classA = IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#Master");
		    IRI classB = IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#StrongAthlete");
		    
		    // Get explanations
		    Set<Explanation<OWLAxiom>> explanations = gen.getExplanations(df.getOWLSubClassOfAxiom(df.getOWLClass(classA), df.getOWLClass(classB)));
		    
		    // Print explanations
		    System.out.println("Explanation-Case 1:: " + explanations);
		    // case 2 - A subClassOf B where A is an individual and B is a simple class
		    classA = IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#Tim");
		    classB = IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#TeamMember");
		    
		    // Get explanations
		    explanations = gen.getExplanations(df.getOWLSubClassOfAxiom(df.getOWLClass(classA), df.getOWLClass(classB)));
		    
		    // Print explanations
		    System.out.println("Explanation-Case 2:: " + explanations);
		    
		    // case 3 - A subClassOf B where A is a simple class, B is a complex class expression such as exists R.C
		    classA = IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#Team");
		    classB = IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#SportGame");
		    
		    IRI propertyR = IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#plays");
		    OWLObjectProperty R = df.getOWLObjectProperty(propertyR);
		    
		    // Get explanations
		    explanations = gen.getExplanations(df.getOWLSubClassOfAxiom(df.getOWLClass(classA), df.getOWLObjectSomeValuesFrom(R, df.getOWLClass(classB))));
		    
		    // Print explanations
		    System.out.println("Explanation-Case 3:: " + explanations);
				    
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* Function to generate explanation for inconsistent ontology */
	public void checkInconsistent(OWLOntology ont){

		try {
			PrintStream ps = new PrintStream(new File("MT19070_Q4.txt"));
			System.setOut(ps);
		    OWLReasonerFactory rf = new JFactFactory();
		    OWLOntologyManager m = ont.getOWLOntologyManager();
		    OWLDataFactory df = m.getOWLDataFactory();
		    
		    // Create some inconsistency
		    OWLClass class1 = df.getOWLClass(IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#class1"));
		    OWLClass class2 = df.getOWLClass(IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#class2"));
		    OWLIndividual i = df.getOWLNamedIndividual(IRI.create("http://www.semanticweb.org/prateekagarwal/ontologies/2020/3/untitled-ontology-9#i"));
		    
		    m.addAxiom(ont, df.getOWLDisjointClassesAxiom(class1, class2));
		    m.addAxiom(ont, df.getOWLClassAssertionAxiom(class1, i));
		    m.addAxiom(ont, df.getOWLClassAssertionAxiom(class2, i));
		    
		    // generate explanation generator
		    ExplanationGenerator<OWLAxiom> explainInconsistency = new InconsistentOntologyExplanationGeneratorFactory(rf,
		        1000L).createExplanationGenerator(ont);
		    
		    // Get Explanations
		    Set<Explanation<OWLAxiom>> explanations = explainInconsistency.getExplanations(df.getOWLSubClassOfAxiom(df
		        .getOWLThing(), df.getOWLNothing()));
		    
		    // Print explanations
		    System.out.println("Explanation-Inconsistent::" + explanations);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
