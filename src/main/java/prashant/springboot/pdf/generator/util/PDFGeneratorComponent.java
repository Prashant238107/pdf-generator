package prashant.springboot.pdf.generator.util;

import java.util.Map;

/**
 * Created by GREYORANGE\prashant.v on 24/7/17.
 */
public interface PDFGeneratorComponent {
    void generatePDF(Map<String, Object> template, Map<String, Object> data);
}
