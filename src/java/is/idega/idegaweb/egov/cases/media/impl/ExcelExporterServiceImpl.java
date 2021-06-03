package is.idega.idegaweb.egov.cases.media.impl;

import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.business.DefaultSpringBean;

import is.idega.idegaweb.egov.cases.media.ExcelExporterService;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ExcelExporterServiceImpl extends DefaultSpringBean implements ExcelExporterService {

	@Override
	public boolean autosizeSheetColumns(HSSFSheet sheet, int nrOfCells) {
		if (sheet == null || nrOfCells <= 0) {
			return false;
		}

		for (int column = 0; column < nrOfCells; column++) {
			try {
				sheet.autoSizeColumn(column);
			} catch (Exception e) {}
		}

		return true;
	}

	public boolean autosizeColumns(HSSFSheet sheet) {
		if (sheet == null) {
			return Boolean.FALSE;
		}

		short lastCell = 0;
		for (Iterator<Row> rowIterator = sheet.iterator(); rowIterator.hasNext();) {
			Row row = rowIterator.next();
			if (row == null) {
				continue;
			}

			short cellNum = row.getLastCellNum();
			lastCell = lastCell < cellNum ? cellNum : lastCell;
		}

		return autosizeSheetColumns(sheet, lastCell);
	}

}