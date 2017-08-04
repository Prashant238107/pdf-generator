package prashant.springboot.pdf.generator.component;

import java.util.Map;

public interface PDFGeneratorComponent {
    void generatePDF(Map<String, Object> template, Map<String, Object> data);
}
