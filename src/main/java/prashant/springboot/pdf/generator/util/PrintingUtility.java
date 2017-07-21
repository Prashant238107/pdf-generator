package prashant.springboot.pdf.generator.util;

import java.util.Map;

/**
 * Created by GREYORANGE\prashant.v on 22/7/17.
 */
public interface PrintingUtility {
    void printPDF(Map<String, Object> template, Map<String, Object> data) throws RuntimeException;
}