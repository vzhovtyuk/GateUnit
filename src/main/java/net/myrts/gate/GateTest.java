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
import java.util.*;

import static net.myrts.gate.Asserts.assertAnnotation;
import static org.junit.Assert.*;

/**
 * Test cases for extractor component.
 *
 * @author <a href="mailto:vzhovtiuk@gmail.com">Vitaliy Zhovtyuk</a>
 *         Date: 2/3/16
 *         Time: 10:01 AM
 */
public class GateTest {
    private static final Logger LOG = LoggerFactory.getLogger(GateTest.class);
    
    private static final String PLUGIN_NAME = "ANNIE";
    
    private static final String PROJECT_FILE_NAME = "ANNIE_with_defaults.gapp";
    
    @Test
    public void shouldParseLocation() throws IOException, GateException {
        //given
        //when
        Document doc = getDocument("1.txt");
        
        String annotationType = "Location";
        List<ContentAnnotation> annotations = getDefaultAnnotations(annotationType, doc);
        assertTrue(!annotations.isEmpty());
        LOG.info("Matched annotations by " + annotationType + " annotations " + annotations);
        
        // then
        assertAnnotation(annotations, annotationType, "Hepburn", 0L);
        assertAnnotation(annotations, annotationType, "United States", 68L);
        assertAnnotation(annotations, annotationType, "United States", 485L);
        assertAnnotation(annotations, annotationType, "Hepburn", 546L);
        assertAnnotation(annotations, annotationType, "United States", 636L);
        assertAnnotation(annotations, annotationType, "Kentucky", 790L);
        assertAnnotation(annotations, annotationType, "United States", 1180L);
        assertAnnotation(annotations, annotationType, "Lee", 2204L);
        assertAnnotation(annotations, annotationType, "U.S.", 2241L);
        assertAnnotation(annotations, annotationType, "Wall", 2247L);       
    }

    @Test
    public void shouldParsePerson() throws IOException, GateException {
        //given
        //when
        Document doc = getDocument("1.txt");
        
        String annotationType = "Person";
        List<ContentAnnotation> annotations = getDefaultAnnotations(annotationType, doc);
        assertTrue(!annotations.isEmpty());
        LOG.info("Matched annotations by " + annotationType + " annotations " + annotations);
        
        // then
        assertAnnotation(annotations, annotationType, "Griswold", 11L);
        assertAnnotation(annotations, annotationType, "Salmon P. Chase", 115L);
        assertAnnotation(annotations, annotationType, "Chase", 242L);
        assertAnnotation(annotations, annotationType, "Mrs. Hepburn", 350L);
        assertAnnotation(annotations, annotationType, "Henry Griswold", 398L);
        assertAnnotation(annotations, annotationType, "Griswold", 532L);
        assertAnnotation(annotations, annotationType, "Mrs. Hepburn", 611L);
        assertAnnotation(annotations, annotationType, "Mrs. Hepburn", 843L);
        assertAnnotation(annotations, annotationType, "Chase", 2278L);
    }

    @Test
    public void shouldParseLookupCountryCode() throws IOException, GateException {
        //given
        //when
        Corpus corpus = annotateDocument("1.txt");

        String annotationType = "Lookup";
        String annotationSubType = "govern_key";
        Iterator iter = corpus.iterator();
        Document doc = (Document) iter.next();
        List<ContentAnnotation> annotations = getDefaultAnnotations(annotationType, doc);
        assertTrue(!annotations.isEmpty());
        LOG.info("Matched annotations by " + annotationType + " annotations " + annotations);
        
        // then
        assertAnnotation(annotations, annotationType, annotationSubType, "Court", 55L);
        assertAnnotation(annotations, annotationType, annotationSubType, "Court", 149L);
        assertAnnotation(annotations, annotationType, annotationSubType, "Court", 581L);
        assertAnnotation(annotations, annotationType, annotationSubType, "Court", 771L);
        assertAnnotation(annotations, annotationType, annotationSubType, "Court", 894L);
        assertAnnotation(annotations, annotationType, annotationSubType, "Court", 952L);
        assertAnnotation(annotations, annotationType, annotationSubType, "Court", 982L);
    }


    private Document getDocument(String inputFileName) throws GateException, IOException {
        Corpus corpus = annotateDocument(inputFileName);

        Iterator iter = corpus.iterator();
        return (Document) iter.next();
    }

    /**
     * Initialise the ANNIE system. This creates a "corpus pipeline"
     * application that can be used to run sets of documents through
     * the extraction system.
     */
    private CorpusController initGateProject(String pluginName, String projectFileName) throws GateException, IOException {
        // load the ANNIE application from the saved state in plugins/ANNIE
        File pluginsHome = Gate.getPluginsHome();
        File anniePlugin = new File(pluginsHome, pluginName);
        File annieGapp = new File(anniePlugin, projectFileName);
        return (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);
    } // initGateProject()

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
        List<Annotation> annotationList = new ArrayList<>(annotationSet);
        Collections.sort(annotationList, (o1, o2) -> o1.getStartNode().getOffset().compareTo(o2.getStartNode().getOffset()));
        List<ContentAnnotation> annotations = new ArrayList<>();
        for(Annotation annotation : annotationList) {
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

        CorpusController annieController = initGateProject(PLUGIN_NAME, PROJECT_FILE_NAME);

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

}
