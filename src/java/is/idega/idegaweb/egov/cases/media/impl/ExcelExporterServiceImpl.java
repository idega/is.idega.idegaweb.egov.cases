package is.idega.idegaweb.egov.cases.media.impl;

import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.media.business.ExcelUtil;
import com.idega.core.business.DefaultSpringBean;

import is.idega.idegaweb.egov.cases.media.ExcelExporterService;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ExcelExporterServiceImpl extends DefaultSpringBean implements ExcelExporterService {

	@Override
	public boolean autosizeSheetColumns(Sheet sheet, int nrOfCells) {
		return ExcelUtil.getInstance().autosizeSheetColumns(sheet, nrOfCells);
	}

	@Override
	public boolean autosizeColumns(Sheet sheet) {
		return ExcelUtil.getInstance().autosizeColumns(sheet);
	}

}