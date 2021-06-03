package is.idega.idegaweb.egov.cases.media;

import org.apache.poi.hssf.usermodel.HSSFSheet;

public interface ExcelExporterService {

	boolean autosizeColumns(HSSFSheet sheet);

	boolean autosizeSheetColumns(HSSFSheet sheet, int nrOfCells);

}