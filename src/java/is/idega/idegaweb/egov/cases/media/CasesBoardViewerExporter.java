package is.idega.idegaweb.egov.cases.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import com.idega.core.file.util.MimeTypeUtil;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.io.MemoryFileBuffer;
import com.idega.presentation.IWContext;
import com.idega.util.FileUtil;

public class CasesBoardViewerExporter extends DownloadWriter implements MediaWritable {
	
private MemoryFileBuffer memory;
	
	@Override
	public String getMimeType() {
		return MimeTypeUtil.MIME_TYPE_EXCEL_2;
	}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		
	}
	
	@Override
	public void writeTo(OutputStream streamOut) throws IOException {
		InputStream streamIn = new ByteArrayInputStream(memory.buffer());
		FileUtil.streamToOutputStream(streamIn, streamOut);
		
		streamOut.flush();
		streamOut.close();
		streamIn.close();
	}

}
