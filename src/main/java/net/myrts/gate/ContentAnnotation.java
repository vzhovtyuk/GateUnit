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
    
    private final String docContent;
    private final Annotation annotation;

    public ContentAnnotation(Annotation annotation, String docContent) {
        this.annotation = annotation;
        this.docContent = docContent;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public String getDocContent() {
        return docContent;
    }
}
