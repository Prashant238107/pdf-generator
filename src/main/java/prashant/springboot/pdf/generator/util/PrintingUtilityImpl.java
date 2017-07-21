package prashant.springboot.pdf.generator.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by GREYORANGE\prashant.v on 22/7/17.
 */

//Vertical table
//Image in a table
//Image column in table
public class PrintingUtilityImpl implements PrintingUtility {

    @Override
    public void printPDF(Map<String, Object> template, Map<String, Object> data) throws RuntimeException {
        try {
            final Document document = new Document();
            final String path = "/home/prashant.v/Prashant/Sodimac/PDF/";
            final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + template.get("fileName").toString() + ".pdf"));
            float length = Utilities.millimetersToPoints(((Map<String, Double>) template.get("size")).get("length").floatValue());
            float breadth = Utilities.millimetersToPoints(((Map<String, Double>) template.get("size")).get("breadth").floatValue());
            float marginLeft = Utilities.millimetersToPoints(((Map<String, Double>) template.get("size")).get("marginLeft").floatValue());
            float marginRight = Utilities.millimetersToPoints(((Map<String, Double>) template.get("size")).get("marginRight").floatValue());
            float marginBottom = Utilities.millimetersToPoints(((Map<String, Double>) template.get("size")).get("marginBottom").floatValue());
            float marginTop = Utilities.millimetersToPoints(((Map<String, Double>) template.get("size")).get("marginTop").floatValue());
            int border = ((Map<String, Integer>) template.get("size")).get("border");
            float borderWidth = Utilities.millimetersToPoints(((Map<String, Double>) template.get("size")).get("borderWidth").floatValue());
            final Rectangle page = new Rectangle(length, breadth);
            page.setBorder(border);
            page.setBorderWidth(borderWidth);
            document.setPageSize(page);
            document.setMargins(marginLeft, marginRight, marginTop, marginBottom);
            document.open();
            final List<Map<String, Object>> listOfLayoutComponents = (List<Map<String, Object>>) template.get("layout");
            for (Map<String, Object> layoutObject : listOfLayoutComponents) {
                List<Map<String, Object>> columnList = (List<Map<String, Object>>) layoutObject.get("columns");
                PdfPTable table = new PdfPTable(columnList.size());
                table.setTotalWidth(Utilities.millimetersToPoints(Double.valueOf(layoutObject.get("tableWidth").toString()).floatValue()));
                table.setLockedWidth(true);
                int[] ratio = ((ArrayList<Integer>) layoutObject.get("ratio")).stream().mapToInt(i -> i).toArray();
                table.setWidths(ratio);
                for (Map<String, Object> column : columnList) {
                    if (column.get("type").equals("barcode")) {
                        Barcode128 barcode = new Barcode128();
                        barcode.setGenerateChecksum(true);
                        barcode.setCode(data.get(column.get("key").toString()).toString());
                        PdfPCell cell = new PdfPCell(barcode.createImageWithBarcode(writer.getDirectContent(), null, null), true);
                        cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                        table.addCell(cell);
                    } else if (column.get("type").equals("text")) {
                        Map<String, Object> map = (Map<String, Object>) data.get(column.get("key").toString());
                        String header = map.get("header").toString();
                        String seperator = map.get("seperator").toString();
                        String value = map.get("value").toString();
                        PdfPCell cell = new PdfPCell(new Phrase(header + " " + seperator + " " + value));
                        cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                        table.addCell(cell);
                    } else if (column.get("type").equals("date")) {
                        Date myDate = new Date();
                        PdfPCell cell = new PdfPCell(new Phrase(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(myDate)));
                        cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                        table.addCell(cell);
                    } else if (column.get("type").equals("table")) {
                        List<Object> headerList = (List<Object>) data.get(column.get("headerListKey").toString());
                        PdfPTable subTable = new PdfPTable(headerList.size());
                        subTable.setTotalWidth(Utilities.millimetersToPoints(Double.valueOf(column.get("subTableWidth").toString()).floatValue()));
                        subTable.setLockedWidth(true);
                        int[] subTableRatio = ((ArrayList<Integer>) column.get("subTableRatio")).stream().mapToInt(i -> i).toArray();
                        subTable.setWidths(subTableRatio);
                        for (Object header : headerList) {
                            PdfPCell cell = new PdfPCell(new Phrase(header.toString()));
                            cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                            cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                            cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                            cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                            cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                            cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                            subTable.addCell(cell);
                        }
                        List<Object> valueKeyList = (List<Object>) data.get(column.get("valueListKey").toString());
                        List<Object> values = (List<Object>) data.get(valueKeyList.get(0));
                        Object[][] matrix = new Object[values.size()][headerList.size()];
                        for (int i = 0; i < values.size(); i++) {
                            for (int j = 0; j < valueKeyList.size(); j++) {
                                matrix[i][j] = ((List<Object>) data.get(valueKeyList.get(j).toString())).get(i);
                            }
                        }
                        for (int i = 0; i < values.size(); i++) {
                            for (int j = 0; j < valueKeyList.size(); j++) {
                                PdfPCell cell = new PdfPCell(new Phrase(matrix[i][j].toString()));
                                cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                                cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                                cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                                cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                                cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                                cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                                subTable.addCell(cell);
                            }
                        }

                        List<Integer> listOfSum = (ArrayList<Integer>) column.get("sum");
                        if (listOfSum.size() != 0) {
                            for (int i = 0; i < values.size(); i++) {
                                if (listOfSum.contains(i)) {
                                    int total = 0;
                                    for (int j = 0; j < values.size(); j++) {
                                        total = total + Integer.parseInt(matrix[j][i].toString());
                                    }
                                    PdfPCell cell = new PdfPCell(new Phrase("Total : " + total));
                                    cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                                    cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                                    cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                                    cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                                    cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                                    cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                                    subTable.addCell(cell);
                                } else {
                                    PdfPCell cell = new PdfPCell();
                                    cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                                    cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                                    cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                                    cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                                    cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                                    cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                                    subTable.addCell(cell);
                                }
                            }
                        }
                        table.addCell(subTable);
                    } else if (column.get("type").equals("image")) {
                        Image img = Image.getInstance((data.get(column.get("key").toString())).toString());
                        PdfPCell cell = new PdfPCell(img);
                        cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                        table.addCell(cell);
                    } else if (column.get("type").equals("blank")) {
                        PdfPCell cell = new PdfPCell();
                        cell.setBorder(Integer.parseInt(column.get("borderWidth").toString()));
                        cell.setPaddingLeft(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingLeft").toString())).floatValue()));
                        cell.setPaddingRight(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingRight").toString())).floatValue()));
                        cell.setPaddingTop(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingTop").toString())).floatValue()));
                        cell.setPaddingBottom(Utilities.millimetersToPoints((Double.valueOf(column.get("paddingBottom").toString())).floatValue()));
                        cell.setFixedHeight(Utilities.millimetersToPoints((Double.valueOf(column.get("height").toString())).floatValue()));
                        table.addCell(cell);
                    }
                }
                document.add(table);
            }
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Document Not Found");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File Not Found");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Image Not Found");
        } catch (IOException e) {
            throw new RuntimeException("Input/Output exception");
        }
    }
}
