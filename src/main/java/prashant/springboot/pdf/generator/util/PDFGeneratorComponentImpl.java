package prashant.springboot.pdf.generator.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static prashant.springboot.pdf.generator.constants.Constants.*;

/**
 * Created by GREYORANGE\prashant.v on 24/7/17.
 */
@Component
public class PDFGeneratorComponentImpl implements PDFGeneratorComponent {

    @Autowired
    private Environment environment;

    @Override
    public void generatePDF(Map<String, Object> template, Map<String, Object> data) {
        try {
            final Document document = new Document();
            final String path = "/home/prashant.v/Prashant/Sodimac/PDF/";
            final String fileName = template.get(FILE_NAME).toString();
            final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + fileName + PDF_EXTENSION));
            float length = Utilities.millimetersToPoints(((Map<String, Double>) template.get(SIZE)).get(LENGTH).floatValue());
            float breadth = Utilities.millimetersToPoints(((Map<String, Double>) template.get(SIZE)).get(BREADTH).floatValue());
            float marginLeft = Utilities.millimetersToPoints(((Map<String, Double>) template.get(SIZE)).get(MARGIN_LEFT).floatValue());
            float marginRight = Utilities.millimetersToPoints(((Map<String, Double>) template.get(SIZE)).get(MARGIN_RIGHT).floatValue());
            float marginBottom = Utilities.millimetersToPoints(((Map<String, Double>) template.get(SIZE)).get(MARGIN_BOTTOM).floatValue());
            float marginTop = Utilities.millimetersToPoints(((Map<String, Double>) template.get(SIZE)).get(MARGIN_TOP).floatValue());
            int border = ((Map<String, Integer>) template.get(SIZE)).get(BORDER);
            float borderWidth = Utilities.millimetersToPoints(((Map<String, Double>) template.get(SIZE)).get(BORDER_WIDTH).floatValue());
            final Rectangle page = new Rectangle(length, breadth);
            page.setBorder(border);
            page.setBorderWidth(borderWidth);
            document.setPageSize(page);
            document.setMargins(marginLeft, marginRight, marginTop, marginBottom);
            document.open();
            final List<Map<String, Object>> listOfLayoutComponents = (List<Map<String, Object>>) template.get(LAYOUT);
            for (Map<String, Object> layoutObject : listOfLayoutComponents) {
                List<Map<String, Object>> columnList = (List<Map<String, Object>>) layoutObject.get(COLUMNS);
                PdfPTable table = new PdfPTable(columnList.size());
                table.setTotalWidth(Utilities.millimetersToPoints(Double.valueOf(layoutObject.get(TABLE_WIDTH).toString()).floatValue()));
                table.setLockedWidth(true);
                int[] ratio = ((ArrayList<Integer>) layoutObject.get(RATIO)).stream().mapToInt(i -> i).toArray();
                table.setWidths(ratio);
                for (Map<String, Object> column : columnList) {
                    if (column.get(TYPE).equals(TYPE_BARCODE)) {
                        addBarcodeColumn(data, writer, table, column);
                    } else if (column.get(TYPE).equals(TYPE_TEXT)) {
                        addTextColumn(data, table, column);
                    } else if (column.get(TYPE).equals(TYPE_DATE)) {
                        addDateColumn(table, column);
                    } else if (column.get(TYPE).equals(TYPE_TABLE)) {
                        PdfPTable subTable = getPdfPTable(data, column);
                        table.addCell(subTable);
                    } else if (column.get(TYPE).equals(TYPE_IMAGE)) {
                        addImageColumn(data, table, column);
                    } else if (column.get(TYPE).equals(TYPE_BLANK)) {
                        addBlankColumn(table, column);
                    }
                }
                document.add(table);
            }
            document.close();
        } catch (BadElementException e) {
            throw new RuntimeException("Input/Output exception");
        } catch (DocumentException e) {
            throw new RuntimeException("Document Not Found");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Image Not Found");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Image URL not correct");
        } catch (IOException e) {
            throw new RuntimeException("Input/Output exception");
        }
    }

    private void addBlankColumn(PdfPTable table, Map<String, Object> column) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Integer.parseInt(column.get(BORDER_WIDTH).toString()));
        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_LEFT).toString())).floatValue()));
        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_RIGHT).toString())).floatValue()));
        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_TOP).toString())).floatValue()));
        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_BOTTOM).toString())).floatValue()));
        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get(HEIGHT).toString())).floatValue()));
        table.addCell(cell);
    }

    private void addImageColumn(Map<String, Object> data, PdfPTable table, Map<String, Object> column) throws BadElementException, IOException {
        Image img = Image.getInstance((data.get(column.get(KEY).toString())).toString());
        PdfPCell cell = new PdfPCell(img);
        cell.setBorder(Integer.parseInt(column.get(BORDER_WIDTH).toString()));
        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_LEFT).toString())).floatValue()));
        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_RIGHT).toString())).floatValue()));
        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_TOP).toString())).floatValue()));
        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_BOTTOM).toString())).floatValue()));
        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get(HEIGHT).toString())).floatValue()));
        table.addCell(cell);
    }

    private PdfPTable getPdfPTable(Map<String, Object> data, Map<String, Object> column) throws DocumentException {
        List<Object> headerList = (List<Object>) data.get(column.get(HEADER_LIST_KEY).toString());
        PdfPTable subTable = new PdfPTable(headerList.size());
        subTable.setTotalWidth(Utilities.millimetersToPoints(Double.valueOf(column.get(SUB_TABLE_WIDTH).toString()).floatValue()));
        subTable.setLockedWidth(true);
        int[] subTableRatio = ((ArrayList<Integer>) column.get(SUB_TABLE_RATIO)).stream().mapToInt(i -> i).toArray();
        subTable.setWidths(subTableRatio);
        for (Object header : headerList) {
            Map<String, Object> headerProperties = (Map<String, Object>) column.get(HEADER_PROPERTIES);
            FontSelector fontSelector = new FontSelector();
            Font font = FontFactory.getFont(headerProperties.get(HEADER_FONT_NAME).toString(), Integer.parseInt(headerProperties.get(HEADER_FONT_SIZE).toString()));
            font.setColor(Integer.parseInt(headerProperties.get(HEADER_FONT_COLOUR_RED).toString()), Integer.parseInt(headerProperties.get(HEADER_FONT_COLOUR_GREEN).toString()), Integer.parseInt(headerProperties.get(HEADER_FONT_COLOUR_BLUE).toString()));
            fontSelector.addFont(font);
            PdfPCell cell = new PdfPCell(fontSelector.process(header.toString()));
            cell.setBackgroundColor(new BaseColor(Integer.parseInt(headerProperties.get(HEADER_BACKGROUND_RED).toString()), Integer.parseInt(headerProperties.get(HEADER_BACKGROUND_GREEN).toString()), Integer.parseInt(headerProperties.get(HEADER_BACKGROUND_BLUE).toString())));
            cell.setBorder(Integer.parseInt(headerProperties.get(HEADER_BORDER_WIDTH).toString()));
            cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(headerProperties.get(HEADER_PADDING_LEFT).toString())).floatValue()));
            cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(headerProperties.get(HEADER_PADDING_RIGHT).toString())).floatValue()));
            cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(headerProperties.get(HEADER_PADDING_TOP).toString())).floatValue()));
            cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(headerProperties.get(HEADER_PADDING_BOTTOM).toString())).floatValue()));
            cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(headerProperties.get(HEADER_HEIGHT).toString())).floatValue()));
            subTable.addCell(cell);
        }
        List<Object> valueKeyList = (List<Object>) data.get(column.get(VALUE_LIST_KEY).toString());
        List<Object> values = (List<Object>) data.get(valueKeyList.get(0));
        Object[][] matrix = new Object[values.size()][headerList.size()];
        for (int i = 0; i < values.size(); i++) {
            for (int j = 0; j < valueKeyList.size(); j++) {
                matrix[i][j] = ((List<Object>) data.get(valueKeyList.get(j).toString())).get(i);
            }
        }
        for (int i = 0; i < values.size(); i++) {
            for (int j = 0; j < valueKeyList.size(); j++) {
                List<Map<String, Object>> subTableColumns = (List<Map<String, Object>>) column.get(SUBTABLE_COLUMNS);
                Map<String, Object> columnProperties = subTableColumns.get(j);
                FontSelector fontSelector = new FontSelector();
                Font font = FontFactory.getFont(columnProperties.get(FONT_NAME).toString(), Integer.parseInt(columnProperties.get(FONT_SIZE).toString()));
                font.setColor(Integer.parseInt(columnProperties.get(FONT_COLOUR_RED).toString()), Integer.parseInt(columnProperties.get(FONT_COLOUR_GREEN).toString()), Integer.parseInt(columnProperties.get(FONT_COLOUR_BLUE).toString()));
                fontSelector.addFont(font);
                PdfPCell cell = new PdfPCell(fontSelector.process(matrix[i][j].toString()));
                cell.setBackgroundColor(new BaseColor(Integer.parseInt(columnProperties.get(BACKGROUND_RED).toString()), Integer.parseInt(columnProperties.get(BACKGROUND_GREEN).toString()), Integer.parseInt(columnProperties.get(BACKGROUND_BLUE).toString())));
                cell.setBorder(Integer.parseInt(columnProperties.get(BORDER_WIDTH).toString()));
                cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(columnProperties.get(PADDING_LEFT).toString())).floatValue()));
                cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(columnProperties.get(PADDING_RIGHT).toString())).floatValue()));
                cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(columnProperties.get(PADDING_TOP).toString())).floatValue()));
                cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(columnProperties.get(PADDING_BOTTOM).toString())).floatValue()));
                cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(columnProperties.get(HEIGHT).toString())).floatValue()));
                subTable.addCell(cell);
            }
        }

        List<Integer> listOfSum = (ArrayList<Integer>) column.get(SUM);
        List<Object> footerList = (List<Object>) data.get(column.get(FOOTER_LIST_KEY).toString());
        if (listOfSum.size() != 0) {
            for (int i = 0; i < values.size(); i++) {
                if (listOfSum.contains(i)) {
                    int total = 0;
                    for (int j = 0; j < values.size(); j++) {
                        total = total + Integer.parseInt(matrix[j][i].toString());
                    }
                    Map<String, Object> footerProperties = (Map<String, Object>) column.get(FOOTER_PROPERTIES);
                    FontSelector fontSelector = new FontSelector();
                    Font font = FontFactory.getFont(footerProperties.get(FOOTER_FONT_NAME).toString(), Integer.parseInt(footerProperties.get(FOOTER_FONT_SIZE).toString()));
                    font.setColor(Integer.parseInt(footerProperties.get(FOOTER_FONT_COLOUR_RED).toString()), Integer.parseInt(footerProperties.get(FOOTER_FONT_COLOUR_GREEN).toString()), Integer.parseInt(footerProperties.get(FOOTER_FONT_COLOUR_BLUE).toString()));
                    fontSelector.addFont(font);
                    PdfPCell cell = new PdfPCell(fontSelector.process(footerList.get(listOfSum.indexOf(i)).toString() + total));
                    cell.setBackgroundColor(new BaseColor(Integer.parseInt(footerProperties.get(FOOTER_BACKGROUND_RED).toString()), Integer.parseInt(footerProperties.get(FOOTER_BACKGROUND_GREEN).toString()), Integer.parseInt(footerProperties.get(FOOTER_BACKGROUND_BLUE).toString())));
                    cell.setBorder(Integer.parseInt(footerProperties.get(FOOTER_BORDER_WIDTH).toString()));
                    cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_PADDING_LEFT).toString())).floatValue()));
                    cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_PADDING_RIGHT).toString())).floatValue()));
                    cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_PADDING_TOP).toString())).floatValue()));
                    cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_PADDING_BOTTOM).toString())).floatValue()));
                    cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_HEIGHT).toString())).floatValue()));
                    subTable.addCell(cell);
                } else {
                    Map<String, Object> footerProperties = (Map<String, Object>) column.get(FOOTER_PROPERTIES);
                    FontSelector fontSelector = new FontSelector();
                    Font font = FontFactory.getFont(footerProperties.get(FOOTER_FONT_NAME).toString(), Integer.parseInt(footerProperties.get(FOOTER_FONT_SIZE).toString()));
                    font.setColor(Integer.parseInt(footerProperties.get(FOOTER_FONT_COLOUR_RED).toString()), Integer.parseInt(footerProperties.get(FOOTER_FONT_COLOUR_GREEN).toString()), Integer.parseInt(footerProperties.get(FOOTER_FONT_COLOUR_BLUE).toString()));
                    fontSelector.addFont(font);
                    PdfPCell cell = new PdfPCell(fontSelector.process(""));
                    cell.setBorder(Integer.parseInt(footerProperties.get(FOOTER_BORDER_WIDTH).toString()));
                    cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_PADDING_LEFT).toString())).floatValue()));
                    cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_PADDING_RIGHT).toString())).floatValue()));
                    cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_PADDING_TOP).toString())).floatValue()));
                    cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_PADDING_BOTTOM).toString())).floatValue()));
                    cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(footerProperties.get(FOOTER_HEIGHT).toString())).floatValue()));
                    subTable.addCell(cell);
                }
            }
        }
        return subTable;
    }

    private void addDateColumn(PdfPTable table, Map<String, Object> column) {
        FontSelector fontSelector = new FontSelector();
        Font font = FontFactory.getFont(column.get(FONT_NAME).toString(), Integer.parseInt(column.get(FONT_SIZE).toString()));
        font.setColor(Integer.parseInt(column.get(FONT_COLOUR_RED).toString()), Integer.parseInt(column.get(FONT_COLOUR_GREEN).toString()), Integer.parseInt(column.get(FONT_COLOUR_BLUE).toString()));
        fontSelector.addFont(font);
        String dateFormat = column.get(DATE_FORMAT).toString();
        Date myDate = new Date();
        PdfPCell cell = new PdfPCell(new Phrase(new SimpleDateFormat(dateFormat).format(myDate)));
        cell.setBorder(Integer.parseInt(column.get(BORDER_WIDTH).toString()));
        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_LEFT).toString())).floatValue()));
        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_RIGHT).toString())).floatValue()));
        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_TOP).toString())).floatValue()));
        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_BOTTOM).toString())).floatValue()));
        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get(HEIGHT).toString())).floatValue()));
        table.addCell(cell);
    }

    private void addTextColumn(Map<String, Object> data, PdfPTable table, Map<String, Object> column) {
        FontSelector fontSelector = new FontSelector();
        Font font = FontFactory.getFont(column.get(FONT_NAME).toString(), Integer.parseInt(column.get(FONT_SIZE).toString()));
        font.setColor(Integer.parseInt(column.get(FONT_BASE_COLOUR_RED).toString()), Integer.parseInt(column.get(FONT_BASE_COLOUR_GREEN).toString()), Integer.parseInt(column.get(FONT_BASE_COLOUR_BLUE).toString()));
        fontSelector.addFont(font);
        Map<String, Object> map = (Map<String, Object>) data.get(column.get(KEY).toString());
        String header = map.get(HEADER).toString();
        String seperator = map.get(SEPERATOR).toString();
        String value = map.get(VALUE).toString();
        PdfPCell cell = new PdfPCell(fontSelector.process(header + " " + seperator + " " + value));
        cell.setBorder(Integer.parseInt(column.get(BORDER_WIDTH).toString()));
        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_LEFT).toString())).floatValue()));
        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_RIGHT).toString())).floatValue()));
        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_TOP).toString())).floatValue()));
        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_BOTTOM).toString())).floatValue()));
        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get(HEIGHT).toString())).floatValue()));
        table.addCell(cell);
    }

    private void addBarcodeColumn(Map<String, Object> data, PdfWriter writer, PdfPTable table, Map<String, Object> column) {
        Barcode128 barcode = new Barcode128();
        barcode.setGenerateChecksum(true);
        barcode.setCode(data.get(column.get(KEY).toString()).toString());
        PdfPCell cell = new PdfPCell(barcode.createImageWithBarcode(writer.getDirectContent(), null, null), true);
        cell.setBorder(Integer.parseInt(column.get(BORDER_WIDTH).toString()));
        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_LEFT).toString())).floatValue()));
        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_RIGHT).toString())).floatValue()));
        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_TOP).toString())).floatValue()));
        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get(PADDING_BOTTOM).toString())).floatValue()));
        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get(HEIGHT).toString())).floatValue()));
        table.addCell(cell);
    }
}