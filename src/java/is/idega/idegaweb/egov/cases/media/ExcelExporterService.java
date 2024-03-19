package is.idega.idegaweb.egov.cases.media;

import org.apache.poi.ss.usermodel.Sheet;

public interface ExcelExporterService {

	boolean autosizeColumns(Sheet sheet);

	boolean autosizeSheetColumns(Sheet sheet, int nrOfCells);

}