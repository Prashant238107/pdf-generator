package prashant.springboot.pdf.generator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import prashant.springboot.pdf.generator.util.PrintingUtility;
import prashant.springboot.pdf.generator.util.PrintingUtilityImpl;

import java.util.Map;

@RestController
public class PDFController {
    @RequestMapping(path = "/pdf-generator", method = RequestMethod.POST)
    public ResponseEntity<?> pdfGenerator(@RequestBody final Map<String, Object> params) {
        PrintingUtility printingUtility = new PrintingUtilityImpl();
        printingUtility.printPDF((Map<String, Object>) params.get("packingSlipTemplateData"), (Map<String, Object>) params.get("packingSlipData"));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}