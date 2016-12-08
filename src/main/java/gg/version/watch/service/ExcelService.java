package gg.version.watch.service;

import gg.version.watch.model.Dependency;
import gg.version.watch.model.VersioneState;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Gokcen Guner
 * 06.12.2016
 */
@Service
public class ExcelService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExcelService.class);

  public void saveToFile(Set<Dependency> dependencySet, ServletOutputStream outputStream) {
    Set<Dependency> sortedDependencySet = new TreeSet<>();
    sortedDependencySet.addAll(dependencySet);

    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Dependencies");

    createHeader(sheet);
    Map<VersioneState, CellStyle> stateStyleMap = getStyles(sheet);
    int rownum = 1;
    for (Dependency dependency : sortedDependencySet) {
      Row row = sheet.createRow(rownum++);
      int cellnum = 0;
      VersioneState versioneState = dependency.getVersioneState();
      CellStyle cellStyle = stateStyleMap.get(versioneState);
      cellnum = createCell(cellStyle, row, cellnum, dependency.getGroupId());
      cellnum = createCell(cellStyle, row, cellnum, dependency.getArtifactId());
      cellnum = createCell(cellStyle, row, cellnum, dependency.getCurrentVersion());
      cellnum = createCell(cellStyle, row, cellnum, dependency.getLatestVersion());
      cellnum = createCell(cellStyle, row, cellnum, dependency.getReleaseVersion());
      createCell(cellStyle, row, cellnum, versioneState.name());
    }
    for (int i = 0; i < 7; i++) {
      sheet.autoSizeColumn(i);
    }

    try {
      workbook.write(outputStream);
      LOGGER.info("{} dependencies are written to the stream succesfully.", sortedDependencySet.size());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<VersioneState, CellStyle> getStyles(XSSFSheet sheet) {
    Map<VersioneState, CellStyle> cellStyleMap = new HashMap<>();

    XSSFWorkbook workbook = sheet.getWorkbook();

    cellStyleMap.put(VersioneState.OUTDATED, getXssfCellStyle(workbook, IndexedColors.RED.getIndex()));
    cellStyleMap.put(VersioneState.UP_TO_DATE, getXssfCellStyle(workbook, IndexedColors.LIGHT_GREEN.getIndex()));
    cellStyleMap.put(VersioneState.UNKNOWN, getXssfCellStyle(workbook, IndexedColors.LIGHT_ORANGE.getIndex()));

    return cellStyleMap;
  }

  private XSSFCellStyle getXssfCellStyle(XSSFWorkbook workbook, short index) {
    XSSFCellStyle outdatedStyle = workbook.createCellStyle();
    outdatedStyle.setFillForegroundColor(index);
    outdatedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return outdatedStyle;
  }

  private void createHeader(XSSFSheet sheet) {
    XSSFWorkbook workbook = sheet.getWorkbook();
    CellStyle cellStyle = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 16);
    cellStyle.setFont(font);

    Row row = sheet.createRow(0);
    int i = 0;

    i = createCell(cellStyle, row, i, "Group Id");
    i = createCell(cellStyle, row, i, "Artifact Id");
    i = createCell(cellStyle, row, i, "Current Version");
    i = createCell(cellStyle, row, i, "Latest Version");
    i = createCell(cellStyle, row, i, "Release Version");
    createCell(cellStyle, row, i, "Status");
  }

  private int createCell(CellStyle cellStyle, Row row, int column, String cellValue) {
    Cell cell = row.createCell(column++, CellType.STRING);
    cell.setCellValue(cellValue);
    cell.setCellStyle(cellStyle);
    return column;
  }
}
