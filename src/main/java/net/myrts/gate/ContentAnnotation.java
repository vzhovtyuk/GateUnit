package net.myrts.gate;

import gate.Annotation;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@gmail.com">Vitaliy Zhovtyuk</a>
 *         Date: 6/29/16
 *         Time: 6:04 PM
 */
public class ContentAnnotation {
    
    private final String markedText;
    private final Annotation annotation;

    public ContentAnnotation(Annotation annotation, String markedText) {
        this.annotation = annotation;
        this.markedText = markedText;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public String getMarkedText() {
        return markedText;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ContentAnnotation{");
        sb.append("markedText='").append(markedText).append('\'');
        sb.append(", annotationType=").append(annotation.getType());
        sb.append(", annotationStart=").append(annotation.getStartNode().getOffset());
        sb.append(", annotationEnd=").append(annotation.getEndNode().getOffset());
        String majorType = (String) annotation.getFeatures().get("majorType");
        if(majorType != null) {
            sb.append(", majorType='").append(majorType).append('\'');
            String minorType = (String) annotation.getFeatures().get("minorType");
            if(minorType != null) {
                sb.append(", minorType='").append(minorType).append('\'');
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
