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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static net.myrts.gate.Asserts.assertAnnotation;
import static org.junit.Assert.assertEquals;
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
    
    @Test
    public void shouldParseLookupCity() throws IOException, GateException {
        //given
        //when
        Corpus corpus = annotateDocument("1.txt");

        String annotationType = "Lookup";
        String annotationSubType = "location";
        Iterator iter = corpus.iterator();
        Document doc = (Document) iter.next();
        List<ContentAnnotation> annotations = getDefaultAnnotations(annotationType, doc);
        assertTrue(!annotations.isEmpty());
        LOG.info("Matched annotations by " + annotationType + " annotations " + annotations);
        
        // then
        String annotationMinorType = "city";
        assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, "Hepburn", 0L);
        assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, "Hepburn", 355L);
        assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, "Hepburn", 546L);
        assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, "Louisville", 561L);
        assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, "Hepburn", 616L);
        assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, "Hepburn", 848L);
        assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, "Lee", 2204L);
        assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, "Wall", 2247L);
    }


    @Test
    public void shouldParseNamedAnnotation() throws IOException, GateException {
        //given
        //when
        Corpus corpus = annotateDocument("1.txt");

        String annotationType = "FirstPerson";
        Iterator iter = corpus.iterator();
        Document doc = (Document) iter.next();
        List<ContentAnnotation> annotations = getNamedAnnotations("testNE", annotationType, doc);
        assertTrue(!annotations.isEmpty());
        LOG.info("Matched annotations by " + annotationType + " annotations " + annotations);

        // then
        assertAnnotation(annotations, annotationType, "Salmon", 115L);
        assertAnnotation(annotations, annotationType, "Henry", 398L);
        assertAnnotation(annotations, annotationType, "Lee", 2204L);
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

    private List<ContentAnnotation> getNamedAnnotations(String annotationSetName, String annotationType, Document doc) throws InvalidOffsetException {
        AnnotationSet annotationSet = doc.getAnnotations(annotationSetName);
        assertNotNull(annotationSet);
        return getContentAnnotations(annotationType, doc, annotationSet);
    }

    private List<ContentAnnotation> getDefaultAnnotations(String annotationType, Document doc) throws InvalidOffsetException {
        AnnotationSet annotationSet = doc.getAnnotations();
        assertNotNull(annotationSet);
        return getContentAnnotations(annotationType, doc, annotationSet);
    }

    private List<ContentAnnotation> getDefaultAnnotations(String annotationType, String annotationSubType, Document doc) throws InvalidOffsetException {
        AnnotationSet annotationSet = doc.getAnnotations();
        List<ContentAnnotation> subTypeAnnotations = new ArrayList<>();
        List<ContentAnnotation> annotations = getContentAnnotations(annotationType, doc, annotationSet);
        for (ContentAnnotation contentAnnotation : annotations) {
            Annotation annotation = contentAnnotation.getAnnotation();
            FeatureMap featureMap = annotation.getFeatures();
            if (Objects.equals(annotationSubType, featureMap.get("majorType"))) {
            	subTypeAnnotations.add(contentAnnotation);
            }
        }
        return subTypeAnnotations;
    }
    
    private List<ContentAnnotation> getDefaultAnnotations(String annotationType, String annotationSubType, String annotationMinorType, Document doc) throws InvalidOffsetException {
        AnnotationSet annotationSet = doc.getAnnotations();
        List<ContentAnnotation> subTypeAnnotations = new ArrayList<>();
        List<ContentAnnotation> annotations = getContentAnnotations(annotationType, doc, annotationSet);
        for (ContentAnnotation contentAnnotation : annotations) {
            Annotation annotation = contentAnnotation.getAnnotation();
            FeatureMap featureMap = annotation.getFeatures();
            if (Objects.equals(annotationSubType, featureMap.get("majorType"))
                    && Objects.equals(annotationMinorType, featureMap.get("minorType"))) {
            	subTypeAnnotations.add(contentAnnotation);
            }
        }
        return subTypeAnnotations;
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

    private Corpus annotateDocument(String fileNames) throws GateException, IOException {
        String workDir = System.getProperty("user.dir");
        System.setProperty("gate.plugins.home", workDir + "/src/main/resources/gate-home/plugins");
        System.setProperty("gate.site.config", workDir + "/src/main/resources/gate-home/gate.xml");
        StringBuilder corpusPathSeparated = new StringBuilder();
        for(String fileName : fileNames.split(",")) {
            String fullyQualifiedName = String.valueOf(new File(workDir + "/src/main/resources/corpus/" + fileName).toURI());
            corpusPathSeparated.append(fullyQualifiedName).append(',');
        }
        if(corpusPathSeparated.length() > 0) {
            corpusPathSeparated.deleteCharAt(corpusPathSeparated.length() - 1);
            System.setProperty("gate.corpus.files", corpusPathSeparated.toString());
        }
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

    
    @Test
    public void generateAsserts() throws IOException, GateException {
        Document doc = getDocument("1.txt");

        List<ContentAnnotation> annotations = null ;
        /* For Location */
        
        String annotationType = "Location";
        annotations = getDefaultAnnotations(annotationType, doc);
        
        String output = Asserts.generateAsserts(annotations);

        String input = "assertAnnotation(annotations, annotationType, \"Hepburn\", 0L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"United States\", 68L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"United States\", 485L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Hepburn\", 546L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"United States\", 636L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Kentucky\", 790L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"United States\", 1180L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Lee\", 2204L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"U.S.\", 2241L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Wall\", 2247L); ";

        assertEquals(input, output);

    /*    For Person  */ 
        annotationType = "Person";
        annotations = getDefaultAnnotations(annotationType, doc);
        output = Asserts.generateAsserts(annotations);

        input = ""; 
        input = input + "assertAnnotation(annotations, annotationType, \"Griswold\", 11L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Salmon P. Chase\", 115L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Chase\", 242L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Mrs. Hepburn\", 350L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Henry Griswold\", 398L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Griswold\", 532L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Mrs. Hepburn\", 611L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Mrs. Hepburn\", 843L); ";
        input = input + "assertAnnotation(annotations, annotationType, \"Chase\", 2278L); ";

        assertEquals(input, output);
        
      /* For lookup */
        annotationType = "Lookup";
        String annotationSubType = "govern_key"; 
        annotations = getDefaultAnnotations(annotationType, annotationSubType, doc);
        output = Asserts.generateAssertsForSubType(annotations);
        input = "";  
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, \"Court\", 55L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, \"Court\", 149L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, \"Court\", 581L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, \"Court\", 771L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, \"Court\", 894L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, \"Court\", 952L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, \"Court\", 982L); ";
        
		assertEquals(input, output);
		
        annotationType = "Lookup";
        annotationSubType = "location"; 
        String annotationMinorType = "city"; 
        annotations = getDefaultAnnotations(annotationType, annotationSubType, annotationMinorType, doc);
        output = Asserts.generateAssertsForMinorSubType(annotations);
        input = "";  
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"Hepburn\", 0L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"Hepburn\", 355L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"Hepburn\", 546L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"Louisville\", 561L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"Hepburn\", 616L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"Hepburn\", 848L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"Lee\", 2204L); ";
		input = input + "assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"Wall\", 2247L); ";
		assertEquals(input, output);
		
    }

    
}
