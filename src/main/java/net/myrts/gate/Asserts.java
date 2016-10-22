package net.myrts.gate;

import document.Node;
import document.TextWithNodes;
import gate.Annotation;
import gate.FeatureMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

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

    public static void assertAnnotation(List<document.Annotation> annotations, TextWithNodes textWithNodes, String annotationType, String matchedValue, Long startPosition) {
        boolean matched = false;
        for (document.Annotation annotation : annotations) {
            String markedText = parseValue(textWithNodes, annotation);
            if (matchedValue.equals(markedText) && Objects.equals(startPosition, annotation.getStartNode())) {
                assertEquals("Start position should match for " + annotation, startPosition, Long.valueOf(annotation.getStartNode()));
                assertEquals("End position should match for " + annotation, Long.valueOf(startPosition + matchedValue.length()), Long.valueOf(annotation.getEndNode()));
                matched = true;
            }
        }
        if (!matched) {
            fail("Failed to match by type '" + annotationType + "' expected value '" + matchedValue + "' start offset=" + startPosition);
        }
    }

    private static String parseValue(TextWithNodes textWithNodes, document.Annotation annotation) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean accString = false;
        for (Object o : textWithNodes.getContent()) {
            if (o instanceof Node) {
                Node node = (Node) o;
                int nodeId = (int) node.getId();
                int startNode = (int) annotation.getStartNode();
                int endNode = (int) annotation.getEndNode();
                if (nodeId == startNode) {
                    accString = true;
                }
                if (nodeId == endNode) {
                    break;
                }
            }
            if (o instanceof String && accString) {
                String contentValue = (String) o;
                stringBuilder.append(contentValue);
            }
        }
        return stringBuilder.toString();
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

    public static void assertAnnotation(List<ContentAnnotation> annotations, String annotationType, String annotationSubType, String annotationMinorType, String matchedValue, Long startPosition) {
        boolean matched = false;
        for (ContentAnnotation contentAnnotation : annotations) {
            Annotation annotation = contentAnnotation.getAnnotation();
            FeatureMap featureMap = annotation.getFeatures();
            if (matchedValue.equals(contentAnnotation.getMarkedText())
                    && Objects.equals(annotationSubType, featureMap.get("majorType"))
                    && Objects.equals(annotationMinorType, featureMap.get("minorType"))
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

    static String generateAsserts(List<ContentAnnotation> contentAnnotations) {
        return convertToString(generateAnnotations(contentAnnotations));
    }

    static String generateAssertsForSubType(List<ContentAnnotation> annotations) {
        return convertToString(generateAnnotationsForSubType(annotations));
    }

    static String generateAssertsForMinorSubType(List<ContentAnnotation> annotations) {
        return convertToString(generateAnnotationsForMinorSubType(annotations));
    }

    private static List<String> generateAnnotations(List<ContentAnnotation> contentAnnotations) {
        assertTrue(!contentAnnotations.isEmpty());
        List<String> annotations = new ArrayList<>();
        for (ContentAnnotation contentAnnotation : contentAnnotations) {
            annotations.add("assertAnnotation(annotations, annotationType, \"" + contentAnnotation.getMarkedText() + "\", " + contentAnnotation.getAnnotation().getStartNode().getOffset() + "L); ");
        }
        return annotations;
    }

    private static List<String> generateAnnotationsForSubType(List<ContentAnnotation> contentAnnotations) {
        assertTrue(!contentAnnotations.isEmpty());
        List<String> annotations = new ArrayList<>();
        for (ContentAnnotation contentAnnotation : contentAnnotations) {
            annotations.add("assertAnnotation(annotations, annotationType, annotationSubType, \"" + contentAnnotation.getMarkedText() + "\", " + contentAnnotation.getAnnotation().getStartNode().getOffset() + "L); ");
        }
        return annotations;
    }

    private static List<String> generateAnnotationsForMinorSubType(List<ContentAnnotation> contentAnnotations) {
        assertTrue(!contentAnnotations.isEmpty());
        List<String> annotations = new ArrayList<>();
        for (ContentAnnotation contentAnnotation : contentAnnotations) {
            annotations.add("assertAnnotation(annotations, annotationType, annotationSubType, annotationMinorType, \"" + contentAnnotation.getMarkedText() + "\", " + contentAnnotation.getAnnotation().getStartNode().getOffset() + "L); ");
        }
        return annotations;
    }

    private static String convertToString(List<String> annotations) {
        String str = "";
        for (String annotation : annotations) {
            str = str + annotation;
        }

        return str;
    }


}
