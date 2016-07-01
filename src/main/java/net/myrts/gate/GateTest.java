package net.myrts.gate;

import gate.*;
import gate.corpora.RepositioningInfo;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
     * Initialise the ANNIE system. This creates a "corpus pipeline"
     * application that can be used to run sets of documents through
     * the extraction system.
     */
    private CorpusController initAnnie() throws GateException, IOException {
        // load the ANNIE application from the saved state in plugins/ANNIE
        File pluginsHome = Gate.getPluginsHome();
        File anniePlugin = new File(pluginsHome, "ANNIE");
        File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
        return (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);
    } // initAnnie()

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

    private List<ContentAnnotation> getNamedAnnotations(String annotationType, Document doc) throws InvalidOffsetException {
        AnnotationSet annotationSet = doc.getAnnotations(annotationType);
        assertNotNull(annotationSet);
        return getContentAnnotations(annotationType, doc, annotationSet);
    }
    
    private List<ContentAnnotation> getDefaultAnnotations(String annotationType, Document doc) throws InvalidOffsetException {
        AnnotationSet annotationSet = doc.getAnnotations();
        assertNotNull(annotationSet);
        return getContentAnnotations(annotationType, doc, annotationSet);
    }

    private List<ContentAnnotation> getContentAnnotations(String annotationType, Document doc, AnnotationSet annotationSet) {
        FeatureMap docFeatures = doc.getFeatures();
        String originalContent = (String)
                docFeatures.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
        RepositioningInfo info = (RepositioningInfo)
                docFeatures.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);

        SortedAnnotationList sortedAnnotations = new SortedAnnotationList();
        for (Annotation anAnnotationSet : annotationSet) {
            sortedAnnotations.addSortedExclusive(anAnnotationSet);
        } // while
        
        List<ContentAnnotation> annotations = new ArrayList<>();
        for(Annotation annotation : sortedAnnotations) {
            if(annotationType.equals(annotation.getType())) {
                long insertPositionStart =
                        annotation.getStartNode().getOffset();
                long insertPositionEnd = annotation.getEndNode().getOffset();
                if(info != null) {
                    insertPositionStart = info.getOriginalPos(insertPositionStart);
                    insertPositionEnd = info.getOriginalPos(insertPositionEnd, true);
                }
                if (insertPositionEnd != -1 && insertPositionStart != -1) {
                    annotations.add(new ContentAnnotation(annotation, originalContent.substring((int)insertPositionStart, (int)insertPositionEnd)));
                }
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
        CorpusController annieController = initAnnie();

        // create a GATE corpus and add a document for each command-line
        // argument
        Corpus corpus = addDocumentsToCorpus();

        // tell the pipeline about the corpus and run it
        annieController.setCorpus(corpus);
        annieController.execute();
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

    /**
     *
     */
    public static class SortedAnnotationList extends Vector<Annotation> {
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
                currAnot = get(i);
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
