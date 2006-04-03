package is.idega.idegaweb.egov.cases.business;

import java.io.FileOutputStream;

import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PDFWriter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PDFWriter writer = new PDFWriter();
		
		writer.createPDF();
	}
	
	public void createPDF() {
		Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
		Font bigFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
		Font paraFont = new Font(Font.HELVETICA, 11, Font.BOLD);
		Font tagFont = new Font(Font.HELVETICA, 10, Font.BOLD);
		Font textFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

		
		try {
			MemoryFileBuffer buffer = new MemoryFileBuffer();
			MemoryOutputStream mos = new MemoryOutputStream(buffer);
//			MemoryInputStream mis = new MemoryInputStream(buffer);
			// FileOutputStream fos = new FileOutputStream(file);
			Document document = new Document(PageSize.A4, 50, 50, 50, 50);
			PdfWriter.getInstance(document, new FileOutputStream("/Users/bluebottle/Desktop/test.pdf"));
			document.addAuthor("Idegaweb eGov");
			document.addSubject("Case");
			document.open();
			document.newPage();
			String title = "Leikskólaumsókn";
			Paragraph cTitle = new Paragraph(title, titleFont);
			document.setPageCount(1);
			document.add(cTitle);
			document.add(new Phrase(""));
			PdfPTable table = new PdfPTable(2);
			table.getDefaultCell().setBorder(0);
			table.addCell(new Phrase("Melkorka Ingibjörg Pálsdóttir", bigFont));
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(new Phrase("120300-3180", bigFont));
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
			table.addCell(new Phrase("Galtalind 13", textFont));
			table.addCell("");
			table.addCell(new Phrase("201 Kópavogur", textFont));
			table.addCell(new Phrase("", textFont));
			
			table.setWidthPercentage(100);
			document.add(table);
			
			document.add(new Phrase("\n"));
			document.add(new Phrase("Upplýsingar um val á rekstraraðila", paraFont));
			PdfPTable table2 = new PdfPTable(4);
			table2.getDefaultCell().setBorder(0);
			table2.addCell(new Phrase("Dagvistun 1", tagFont));
			table2.addCell(new Phrase("Álfheimar", textFont));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase("Frá dags.", tagFont));
			table2.addCell(new Phrase("28.03.2006", textFont));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase("Skilaboð", tagFont));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(""));
			table2.addCell(new Phrase(""));
			table2.getDefaultCell().setColspan(4);
			table2.addCell(new Phrase("Bla bla bla bla bla", textFont));
			
			table2.setWidthPercentage(100);
			document.add(table2);
			
			document.add(new Phrase("\n"));
			document.add(new Phrase("Frá/til upplýsingar", paraFont));
			PdfPTable table3 = new PdfPTable(4);
			table3.getDefaultCell().setBorder(0);
			table3.addCell(new Phrase("Frá", tagFont));
			table3.addCell(new Phrase("07:00", textFont));
			table3.addCell(new Phrase(""));
			table3.addCell(new Phrase(""));
			table3.addCell(new Phrase("Til", tagFont));
			table3.addCell(new Phrase("16:00", textFont));
			table3.addCell(new Phrase(""));
			table3.addCell(new Phrase(""));
			
			table3.setWidthPercentage(100);
			document.add(table3);
			
			document.add(new Phrase("\n"));
			document.add(new Phrase("Foreldrar/forráðamenn", paraFont));
			PdfPTable table4 = new PdfPTable(4);
			table4.getDefaultCell().setBorder(0);
			table4.addCell(new Phrase("Tengsl", tagFont));
			table4.addCell(new Phrase("Faðir", textFont));
			table4.addCell(new Phrase("Móðir", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Nafn", tagFont));
			table4.addCell(new Phrase("Páll Helgason", textFont));
			table4.addCell(new Phrase("Karitas Gunnarsdóttir", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Kennitala", tagFont));
			table4.addCell(new Phrase("0610703899", textFont));
			table4.addCell(new Phrase("1409743589", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Heimilisfang",tagFont));
			table4.addCell(new Phrase("Galtalind 13", textFont));
			table4.addCell(new Phrase("Galtalind 13", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Póstnúmer", tagFont));
			table4.addCell(new Phrase("201 Kópavogur", textFont));
			table4.addCell(new Phrase("201 Kópavogur", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Heimasími", tagFont));
			table4.addCell(new Phrase("1234567", textFont));
			table4.addCell(new Phrase("1234567", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Vinnusími", tagFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Farsími", tagFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Netfang", tagFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Þjóðerni", tagFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase(""));
			table4.addCell(new Phrase("Hjúskaparstaða", tagFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase("", textFont));
			table4.addCell(new Phrase(""));
			
			table4.setWidthPercentage(100);
			document.add(table4);
			
			document.add(new Phrase("\n"));
			document.add(new Phrase("Tengiliðir", paraFont));
			PdfPTable table5 = new PdfPTable(4);
			table5.getDefaultCell().setBorder(0);
			table5.addCell(new Phrase("Tengsl", tagFont));
			table5.addCell(new Phrase("Amma", textFont));
			table5.addCell(new Phrase("", textFont));
			table5.addCell(new Phrase(""));
			table5.addCell(new Phrase("Nafn", tagFont));
			table5.addCell(new Phrase("Ingibjörg Pálsdóttir", textFont));
			table5.addCell(new Phrase("", textFont));
			table5.addCell(new Phrase(""));
			table5.addCell(new Phrase("Heimasími", tagFont));
			table5.addCell(new Phrase("7654321", textFont));
			table5.addCell(new Phrase("", textFont));
			table5.addCell(new Phrase(""));
			table5.addCell(new Phrase("Vinnusími", tagFont));
			table5.addCell(new Phrase("1232341", textFont));
			table5.addCell(new Phrase("", textFont));
			table5.addCell(new Phrase(""));
			table5.addCell(new Phrase("Farsími", tagFont));
			table5.addCell(new Phrase("456356", textFont));
			table5.addCell(new Phrase("", textFont));
			table5.addCell(new Phrase(""));
			table5.addCell(new Phrase("Netfang", tagFont));
			table5.addCell(new Phrase("inga@bjorg.is", textFont));
			table5.addCell(new Phrase("", textFont));
			table5.addCell(new Phrase(""));

			table5.setWidthPercentage(100);
			document.add(table5);

			document.newPage();
			document.add(new Phrase("Mikilvægar upplýsingar", paraFont));
			PdfPTable table6 = new PdfPTable(1);
			table6.getDefaultCell().setBorder(0);
			table6.addCell(new Phrase("Töluð eru fleiri en eitt tungumál á heimili barnsins", tagFont));
			table6.addCell(new Phrase("Já, Urdu", textFont));
			table6.addCell(new Phrase(""));
			table6.addCell(new Phrase("Nám foreldris/forráðamanns", tagFont));
			table6.addCell(new Phrase("Magadans (3.2.2006 - 7.1.2000)", textFont));
			table6.addCell(new Phrase(""));
			table6.addCell(new Phrase("Barnið hefur greinst með fötlun, þroskafrávik eða átt við langvinn veikindi að stríða", tagFont));
			table6.addCell(new Phrase("Já, Eitthvað um fötlun", textFont));
			table6.addCell(new Phrase(""));
			table6.addCell(new Phrase("Barnið er með ofnæði eða óþol", tagFont));
			table6.addCell(new Phrase("Já, Ofnæmi fyrir Þórhalli frænda", textFont));
			table6.addCell(new Phrase(""));
			table6.addCell(new Phrase("Barnið er hjá dagforeldri", tagFont));
			table6.addCell(new Phrase("Nei", textFont));
			table6.addCell(new Phrase(""));
			table6.addCell(new Phrase("Síðasti leikskóli eða dagforeldri", tagFont));
			table6.addCell(new Phrase("Núpur", textFont));
			table6.addCell(new Phrase(""));
			table6.addCell(new Phrase("Heimilt er að hafa samband við grunnskóla, leikskóla eða dagforeldri og fá þaðan upplýsingar um barnið", tagFont));
			table6.addCell(new Phrase("Já", textFont));
			table6.addCell(new Phrase(""));
			table6.addCell(new Phrase("Birta má myndir af barninu", tagFont));
			table6.addCell(new Phrase("Já", textFont));
			table6.addCell(new Phrase(""));
			table6.addCell(new Phrase("Eru einhverjar aðrar upplýsingar sem þú kýst að veita dagvistunaraðilanum?", tagFont));
			table6.addCell(new Phrase("Aðrar upplýsingar til dagvistunaraðila", textFont));
			table6.addCell(new Phrase(""));
			
			table6.setWidthPercentage(100);
			document.add(table6);
			
			document.close();
			try {
				mos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}