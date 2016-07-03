package net.myrts.gate;

import gate.Annotation;
import gate.FeatureMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@gmail.com">Vitaliy Zhovtyuk</a>
 *         Date: 7/1/16
 *         Time: 5:58 PM
 */
public class Asserts {
    public static void assertAnnotation(List<ContentAnnotation> annotations, String annotationType, String matchedValue, Long startPosition) {
        boolean matched = false;
        for (ContentAnnotation contentAnnotation : annotations) {
            Annotation annotation = contentAnnotation.getAnnotation();
            if (matchedValue.equals(contentAnnotation.getMarkedText())
                    && Objects.equals(startPosition, annotation.getStartNode().getOffset())) {
                assertEquals("Start position should match for " + contentAnnotation, startPosition, annotation.getStartNode().getOffset());
                assertEquals("End position should match for " + contentAnnotation, Long.valueOf(startPosition + matchedValue.length()), annotation.getEndNode().getOffset());
                matched = true;
            }
        }
        if (!matched) {
            fail("Failed to match by type '" + annotationType + "' expected value '" + matchedValue + "' start offset=" + startPosition);
        }
    }

    public static void assertAnnotation(List<ContentAnnotation> annotations, String annotationType, String annotationSubType, String matchedValue, Long startPosition) {
        boolean matched = false;
        for (ContentAnnotation contentAnnotation : annotations) {
            Annotation annotation = contentAnnotation.getAnnotation();
            FeatureMap featureMap = annotation.getFeatures();
            if (matchedValue.equals(contentAnnotation.getMarkedText())
                    && Objects.equals(annotationSubType, featureMap.get("majorType"))
                    && Objects.equals(startPosition, annotation.getStartNode().getOffset())) {
                assertEquals("Start position should match for " + contentAnnotation, startPosition, annotation.getStartNode().getOffset());
                assertEquals("End position should match for " + contentAnnotation, Long.valueOf(startPosition + matchedValue.length()), annotation.getEndNode().getOffset());
                matched = true;
            }
        }
        if (!matched) {
            fail("Failed to match by type '" + annotationType + "' expected value '" + matchedValue + "' start offset=" + startPosition);
        }
    }
    
    public static List<String> generateAnnotations(List<ContentAnnotation> contentAannotations, String annotationType){
        assertTrue(!contentAannotations.isEmpty());
        List<String> annotations = new ArrayList<>(); 
        for (ContentAnnotation contentAnnotation : contentAannotations) {
        	annotations.add("assertAnnotation(annotations, annotationType, \""+contentAnnotation.getMarkedText()+"\", "+contentAnnotation.getAnnotation().getStartNode().getOffset()+"L); ");
		}
       return annotations;    	
    }
    
    public static List<String> generateAnnotations(List<ContentAnnotation> contentAannotations, String annotationType, String annotationSubType){
        assertTrue(!contentAannotations.isEmpty());
        List<String> annotations = new ArrayList<>(); 
        for (ContentAnnotation contentAnnotation : contentAannotations) {
        	annotations.add("assertAnnotation(annotations, annotationType, annotationSubType, \""+contentAnnotation.getMarkedText()+"\", "+contentAnnotation.getAnnotation().getStartNode().getOffset()+"L); ");
		}
       return annotations;    	
    }    
}
