package net.myrts.gate;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.GateConstants;
import gate.corpora.RepositioningInfo;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import static org.junit.Assert.assertNotNull;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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

        // for each document, get an XML document with the
        // person and location names added
        Iterator iter = corpus.iterator();
        int count = 0;
        String startTagPart_1 = "<span GateID=\"";
        String startTagPart_2 = "\" title=\"";
        String startTagPart_3 = "\" style=\"background:Red;\">";
        String endTag = "</span>";

        while (iter.hasNext()) {
            Document doc = (Document) iter.next();
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            Set<String> annotTypesRequired = new HashSet<String>();
            annotTypesRequired.add("Person");
            annotTypesRequired.add("Location");
            Set<Annotation> peopleAndPlaces =
                    new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));

            FeatureMap features = doc.getFeatures();
            String originalContent = (String)
                    features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
            RepositioningInfo info = (RepositioningInfo)
                    features.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);

            ++count;
            File file = new File("StANNIE_" + count + ".HTML");
            Out.prln("File name: '" + file.getAbsolutePath() + "'");
            if (originalContent != null && info != null) {
                Out.prln("OrigContent and reposInfo existing. Generate file...");

                Iterator it = peopleAndPlaces.iterator();
                SortedAnnotationList sortedAnnotations = new SortedAnnotationList();

                while (it.hasNext()) {
                    sortedAnnotations.addSortedExclusive((Annotation) it.next());
                } // while

                StringBuilder editableContent = new StringBuilder(originalContent);
                // insert anotation tags backward
                Out.prln("Unsorted annotations count: " + peopleAndPlaces.size());
                Out.prln("Sorted annotations count: " + sortedAnnotations.size());
                for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                    final Annotation currAnnot = (Annotation) sortedAnnotations.get(i);
                    long insertPositionStart =
                            currAnnot.getStartNode().getOffset();
                    insertPositionStart = info.getOriginalPos(insertPositionStart);
                    long insertPositionEnd = currAnnot.getEndNode().getOffset();
                    insertPositionEnd = info.getOriginalPos(insertPositionEnd, true);
                    if (insertPositionEnd != -1 && insertPositionStart != -1) {
                        editableContent.insert((int) insertPositionEnd, endTag);
                        editableContent.insert((int) insertPositionStart, startTagPart_3);
                        editableContent.insert((int) insertPositionStart,
                                currAnnot.getType());
                        editableContent.insert((int) insertPositionStart, startTagPart_2);
                        editableContent.insert((int) insertPositionStart,
                                currAnnot.getId().toString());
                        editableContent.insert((int) insertPositionStart, startTagPart_1);
                    } // if
                } // for

                FileWriter writer = new FileWriter(file);
                writer.write(editableContent.toString());
                writer.close();
            } // if - should generate
            else if (originalContent != null) {
                Out.prln("OrigContent existing. Generate file...");

                Iterator it = peopleAndPlaces.iterator();
                Annotation currAnnot;
                SortedAnnotationList sortedAnnotations = new SortedAnnotationList();

                while (it.hasNext()) {
                    currAnnot = (Annotation) it.next();
                    sortedAnnotations.addSortedExclusive(currAnnot);
                } // while

                StringBuilder editableContent = new StringBuilder(originalContent);
                long insertPositionEnd;
                long insertPositionStart;
                // insert anotation tags backward
                Out.prln("Unsorted annotations count: " + peopleAndPlaces.size());
                Out.prln("Sorted annotations count: " + sortedAnnotations.size());
                for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                    currAnnot = (Annotation) sortedAnnotations.get(i);
                    insertPositionStart =
                            currAnnot.getStartNode().getOffset();
                    insertPositionEnd = currAnnot.getEndNode().getOffset();
                    if (insertPositionEnd != -1 && insertPositionStart != -1) {
                        editableContent.insert((int) insertPositionEnd, endTag);
                        editableContent.insert((int) insertPositionStart, startTagPart_3);
                        editableContent.insert((int) insertPositionStart,
                                currAnnot.getType());
                        editableContent.insert((int) insertPositionStart, startTagPart_2);
                        editableContent.insert((int) insertPositionStart,
                                currAnnot.getId().toString());
                        editableContent.insert((int) insertPositionStart, startTagPart_1);
                    } // if
                } // for

                FileWriter writer = new FileWriter(file);
                writer.write(editableContent.toString());
                writer.close();
            } else {
                Out.prln("Repositioning: " + info);
            }

            String xmlDocument = doc.toXml(peopleAndPlaces, false);
            String fileName = "StANNIE_toXML_" + count + ".HTML";
            FileWriter writer = new FileWriter(fileName);
            writer.write(xmlDocument);
            writer.close();

        } // for each doc
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
