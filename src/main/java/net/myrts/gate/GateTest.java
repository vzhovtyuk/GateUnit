package net.myrts.gate;

import gate.*;
import gate.corpora.RepositioningInfo;
import gate.creole.ANNIEConstants;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ResourceInstantiationException;
import gate.util.Files;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for extractor component.
 *
 * @author <a href="mailto:vzhovtiuk@gmail.com">Vitaliy Zhovtyuk</a>
 *         Date: 2/3/16
 *         Time: 10:01 AM
 */
public class GateTest {
    private static final Logger LOG = LoggerFactory.getLogger(GateTest.class);
    
    
    /**
     * The Corpus Pipeline application to contain ANNIE
     */
    private CorpusController annieController;


    /**
     * Initialise the ANNIE system. This creates a "corpus pipeline"
     * application that can be used to run sets of documents through
     * the extraction system.
     */
    public void initAnnie() throws GateException, IOException {
        Out.prln("Initialising ANNIE...");

        // load the ANNIE application from the saved state in plugins/ANNIE
        File pluginsHome = Gate.getPluginsHome();
        File anniePlugin = new File(pluginsHome, "ANNIE");
        File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
        annieController =
                (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);

        Out.prln("...ANNIE loaded");
    } // initAnnie()

    /**
     * Tell ANNIE's controller about the corpus you want to run on
     */
    public void setCorpus(Corpus corpus) {
        annieController.setCorpus(corpus);
    } // setCorpus

    /**
     * Run ANNIE
     */
    public void execute() throws GateException {
        Out.prln("Running ANNIE...");
        annieController.execute();
        Out.prln("...ANNIE complete");
    } // execute()


    @Test
    public void shouldProcessBatch() throws IOException, GateException {
        /** Index of the first non-option argument on the command line. */
        int firstFile = 0;

        /** Path to the saved application file. */
        File gappFile = new File(System.getProperty("gate.app.state"));

        /**
         * List of annotation types to write out.  If null, write everything as
         * GateXML.
         */
        List annotTypesToWrite = null;

        /**
         * The character encoding to use when loading the docments.  If null, the
         * platform default encoding is used.
         */
        String encoding = null;

        // initialise GATE - this must be done before calling any GATE APIs
        Gate.init();

        // load the saved application
        CorpusController application =
                (CorpusController) PersistenceManager.loadObjectFromFile(gappFile);

        // Create a Corpus to use.  We recycle the same Corpus object for each
        // iteration.  The string parameter to newCorpus() is simply the
        // GATE-internal name to use for the corpus.  It has no particular
        // significance.
        Corpus corpus = Factory.newCorpus("BatchProcessApp Corpus");
        application.setCorpus(corpus);

        // process the files one by one
        String[] args = new String[0];
        for (int i = firstFile; i < args.length; i++) {
            // load the document (using the specified encoding if one was given)
            File docFile = new File(args[i]);
            System.out.print("Processing document " + docFile + "...");
            Document doc = Factory.newDocument(docFile.toURL(), encoding);

            // put the document in the corpus
            corpus.add(doc);

            // run the application
            application.execute();

            // remove the document from the corpus again
            corpus.clear();

            String docXMLString = null;
            // if we want to just write out specific annotation types, we must
            // extract the annotations into a Set
            if (annotTypesToWrite != null) {
                // Create a temporary Set to hold the annotations we wish to write out
                Set annotationsToWrite = new HashSet();

                // we only extract annotations from the default (unnamed) AnnotationSet
                // in this example
                AnnotationSet defaultAnnots = doc.getAnnotations();
                Iterator annotTypesIt = annotTypesToWrite.iterator();
                while (annotTypesIt.hasNext()) {
                    // extract all the annotations of each requested type and add them to
                    // the temporary set
                    AnnotationSet annotsOfThisType =
                            defaultAnnots.get((String) annotTypesIt.next());
                    if (annotsOfThisType != null) {
                        annotationsToWrite.addAll(annotsOfThisType);
                    }
                }

                // create the XML string using these annotations
                docXMLString = doc.toXml(annotationsToWrite);
            }
            // otherwise, just write out the whole document as GateXML
            else {
                docXMLString = doc.toXml();
            }

            // Release the document, as it is no longer needed
            Factory.deleteResource(doc);

            // output the XML to <inputFile>.out.xml
            String outputFileName = docFile.getName() + ".out.xml";
            File outputFile = new File(docFile.getParentFile(), outputFileName);

            // Write output files using the same encoding as the original
            FileOutputStream fos = new FileOutputStream(outputFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            OutputStreamWriter out;
            if (encoding == null) {
                out = new OutputStreamWriter(bos);
            } else {
                out = new OutputStreamWriter(bos, encoding);
            }

            out.write(docXMLString);

            out.close();
            LOG.info("done");
        } // for each file

        LOG.info("All done");


    }


    @Test
    public void shouldParseAnnieAnnotations() throws IOException, GateException {
        //given
        //when
        Corpus corpus = annotateDocument("1.txt");

        // then
        Iterator iter = corpus.iterator();
        Document doc = (Document) iter.next();
        List<ContentAnnotation> annotations = getDefaultAnnotations("Location", doc);
        assertTrue(!annotations.isEmpty());
    }

    private List<ContentAnnotation> getDefaultAnnotations(String annotationType, Document doc) throws InvalidOffsetException {
        AnnotationSet annotationSet = doc.getAnnotations();
        assertNotNull(annotationSet);
        DocumentContent docContent = doc.getContent();
        
        List<ContentAnnotation> annotations = new ArrayList<>();
        for(Annotation annotation : annotationSet) {
            if(annotationType.equals(annotation.getType())) {
                FeatureMap featureMap = annotation.getFeatures();
                List<Integer> matches = (List<Integer>) featureMap.get("matches");
                Integer startNode = matches.get(0);
                Integer endNode = matches.get(1);
                
                annotations.add(new ContentAnnotation(annotation, docContent.toString().substring(startNode.intValue(), endNode.intValue())));
            }
        }
        return annotations;
    }

    private Corpus annotateDocument(String fileName) throws GateException, IOException {
        String workDir = System.getProperty("user.dir");
        System.setProperty("gate.plugins.home", workDir + "/src/main/resources/gate-home/plugins");
        System.setProperty("gate.site.config", workDir + "/src/main/resources/gate-home/gate.xml");
        System.setProperty("gate.corpus.files", String.valueOf(new File(workDir + "/src/main/resources/corpus/" + fileName).toURI()));
        // initialise the GATE library
        Out.prln("Initialising GATE...");
        Gate.init();
        Out.prln("...GATE initialised");

        // initialise ANNIE (this may take several minutes)
        GateTest annie = new GateTest();
        annie.initAnnie();

        // create a GATE corpus and add a document for each command-line
        // argument
        Corpus corpus = addDocumentsToCorpus();

        // tell the pipeline about the corpus and run it
        annie.setCorpus(corpus);
        annie.execute();
        return corpus;
    }

    private Corpus addDocumentsToCorpus() throws ResourceInstantiationException, MalformedURLException {
        Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");
        String filesIncorpus = System.getProperty("gate.corpus.files");
        String[] args = filesIncorpus.split(",");
        for (String arg : args) {
            URL u = new URL(arg);
            FeatureMap params = Factory.newFeatureMap();
            params.put("sourceUrl", u);
            params.put("markupAware", false);
            params.put("preserveOriginalContent", true);
            params.put("collectRepositioningInfo", true);
            Out.prln("Creating doc for " + u);
            Document doc = (Document)
                    Factory.createResource("gate.corpora.DocumentImpl", params);
            corpus.add(doc);
        } // for each of args
        return corpus;
    }


    /*
       * This method runs ANNIE with defaults on a document, then saves
       * it as a GATE XML document and loads it back. All the annotations on the
       * loaded document should be the same as the original ones.
       *
       * It also verifies if the matches feature still holds after an export/import to XML
       */
    public void testAnnotationConsistencyForSaveAsXml() throws Exception {
        // Load a document from the test repository
        //Document origDoc = gate.Factory.newDocument(Gate.getUrl("tests/xml/gateTestSaveAsXML.xml"));
        String testDoc = gate.util.Files.getGateResourceAsString("gate.ac.uk/tests/xml/gateTestSaveAsXML.xml");
        Document origDoc = gate.Factory.newDocument(testDoc);

        // Load ANNIE with defaults and run it on the document
        ConditionalSerialAnalyserController annie = (ConditionalSerialAnalyserController)
                PersistenceManager.loadObjectFromFile(new File(new File(
                        Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR),
                        ANNIEConstants.DEFAULT_FILE));
        assertTrue("ANNIE not loaded!", annie != null);
        Corpus c = Factory.newCorpus("test");
        c.add(origDoc);
        annie.setCorpus(c);
        annie.execute();

        // SaveAS XML and reload the document into another GATE doc
        // Export the Gate document called origDoc as XML, into a temp file,
        // using the working encoding
        String workingEncoding = StandardCharsets.UTF_8.name();
        File xmlFile = Files.writeTempFile(origDoc.toXml(), workingEncoding);
        System.out.println("Saved to temp file :" + xmlFile.toURI().toURL());

        Document reloadedDoc = gate.Factory.newDocument(xmlFile.toURI().toURL(), workingEncoding);
        // Verifies if the maximum annotation ID on the origDoc is less than the
        // Annotation ID generator of the document.

        // Verify if the annotations are identical in the two docs.
        Map<Integer, Annotation> origAnnotMap = buildID2AnnotMap(origDoc);
        Map<Integer, Annotation> reloadedAnnMap = buildID2AnnotMap(reloadedDoc);

        // Clean up the XMl file
        xmlFile.delete();
    }// End testAnnotationIDConsistencyForSaveAsXml


    /**
     * Scans a target Doc for all Annotations and builds a map (from anot ID to annot) in the process
     * I also checks to see if there are two annotations with the same ID.
     *
     * @param aDoc The GATE doc to be scaned
     * @return a Map ID2Annot
     */
    private Map<Integer, Annotation> buildID2AnnotMap(Document aDoc) {
        Map<Integer, Annotation> id2AnnMap = new HashMap<Integer, Annotation>();
        // Scan the default annotation set
        AnnotationSet annotSet = aDoc.getAnnotations();
        addAnnotSet2Map(annotSet, id2AnnMap);
        // Scan all named annotation sets
        if (aDoc.getNamedAnnotationSets() != null) {
            for (Iterator<AnnotationSet> namedAnnotSetsIter = aDoc.getNamedAnnotationSets().values().iterator();
                 namedAnnotSetsIter.hasNext(); ) {

                addAnnotSet2Map(namedAnnotSetsIter.next(), id2AnnMap);
            }// End while
        }// End if
        return id2AnnMap;
    }// End of buildID2AnnotMap()


    private Map<Integer, Annotation> addAnnotSet2Map(AnnotationSet annotSet, Map<Integer, Annotation> id2AnnMap) {
        for (Iterator<Annotation> it = annotSet.iterator(); it.hasNext(); ) {
            Annotation a = it.next();
            Integer id = a.getId();

            assertTrue("Found two annotations(one with type = " + a.getType() +
                    ")with the same ID=" + id, !id2AnnMap.keySet().contains(id));

            id2AnnMap.put(id, a);
        }// End for
        return id2AnnMap;
    }
    
    public static void assertByteArrayEquals(byte[] expected, byte[] actual) {
        assertNotNull(actual);
        if (!Arrays.equals(expected, actual)) {
            //LOG.warn("Values are not match expected {} actual {} ", new String(expected), new String(actual));
            throw new ComparisonFailure("Values are not equal", new String(expected), new String(actual));
        }
    }


    /**
     *
     */
    public static class SortedAnnotationList extends Vector {
        public SortedAnnotationList() {
            super();
        } // SortedAnnotationList

        public boolean addSortedExclusive(Annotation annot) {
            Annotation currAnot = null;

            // overlapping check
            for (Object o : this) {
                currAnot = (Annotation) o;
                if (annot.overlaps(currAnot)) {
                    return false;
                } // if
            } // for

            long annotStart = annot.getStartNode().getOffset();
            long currStart;
            // insert
            for (int i = 0; i < size(); ++i) {
                currAnot = (Annotation) get(i);
                currStart = currAnot.getStartNode().getOffset();
                if (annotStart < currStart) {
                    insertElementAt(annot, i);
        /*
         Out.prln("Insert start: "+annotStart+" at position: "+i+" size="+size());
         Out.prln("Current start: "+currStart);
         */
                    return true;
                } // if
            } // for

            int size = size();
            insertElementAt(annot, size);
//Out.prln("Insert start: "+annotStart+" at size position: "+size);
            return true;
        } // addSorted
    } // SortedAnnotationList

}
