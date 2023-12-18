package deringo.pdfboxSample;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.settings.VerticalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.ImageCell;
import org.vandeseer.easytable.structure.cell.TextCell;

public class PDFBoxSample {
    private static final Path BILD1 = Paths.get("src/main/resources/cat-2934720_1280.jpg");
    private static final Path BILD2 = Paths.get("src/main/resources/tiger-2535888_640.jpg");
    private static final Path BILD3 = Paths.get("src/main/resources/kingfisher-2046453_1920.jpg");
    private static final Path BILD4 = Paths.get("src/main/resources/ireland-1985088_1280.jpg");
    

    private static final String IDENTIFIKATIONSNUMMER = "DE-NRW-S-23-0001";

    private static final LocalDate DATUM = LocalDate.now();
    private static final String INFO1 = "Lorem Ipsum";
    private static final String INFO2 = "Lorem ipsum dolor sit amet";

    public static void main(String[] args) throws Exception {
        Path file = getTMPFile();
        
        createPDF(file);
        
        // PDF öffnen
        Desktop.getDesktop().open(file.toFile());
    }
    
    private static void createPDF(Path file) throws Exception {
        // Erstellen eines neuen Dokuments
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        float spaceLeft = 20f;
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(new PDType1Font(FontName.COURIER), 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(spaceLeft, page.getCropBox().getHeight()-20-12);
        contentStream.showText("Identifikationsnummer: ");
        contentStream.setFont(new PDType1Font(FontName.COURIER_BOLD), 12);
        contentStream.showText(IDENTIFIKATIONSNUMMER);
        contentStream.endText();

        //////////////////////////////////////////
        float startY = page.getMediaBox().getUpperRightY() - 20f - 12f - 20f;
        for (int i=0; i<6; i++) {
            // Build the table
            Table table = getTable(document, page, i);

            // new Page
            if (startY - table.getHeight() < 0) {
                page = new PDPage();
                document.addPage(page);
                contentStream.close();
                contentStream = new PDPageContentStream(document, page);
                startY =  page.getMediaBox().getUpperRightY() - 20f;
            }

            // Set up the drawer
            TableDrawer tableDrawer = TableDrawer.builder()
                    .contentStream(contentStream)
                    .startX(20f)
                    .startY(startY)
                    .table(table)
                    .build();
            
            // And go for it!
            tableDrawer.draw();
            
            startY = tableDrawer.getFinalY() - 20f;
        }

        //////////////////////////////////////////
        
        contentStream.close();

        document.save(file.toFile());
        document.close();
    }
      
    private static Table getTable(PDDocument document, PDPage page, int i) throws IOException {
        // Erstellen von Image-Objekten für die Bilder
        PDImageXObject image1;
        PDImageXObject image2;
        if (i%2 == 0) {
            image1 = PDImageXObject.createFromFileByContent(BILD1.toFile(), document);
            image2 = PDImageXObject.createFromFileByContent(BILD2.toFile(), document);
        } else {
            image1 = PDImageXObject.createFromFileByContent(BILD3.toFile(), document);
            image2 = PDImageXObject.createFromFileByContent(BILD4.toFile(), document);
        }
        
        
        float spaceLeft = 20f;
        float spaceRight = 20f;
        float tableWidth = page.getMediaBox().getWidth() - spaceLeft - spaceRight;
        float tablePadding = 2f;
        float borderWidth = 0.5f;
        float colWidth = (tableWidth - tablePadding - 3*borderWidth) / 2;
        float rowHeight = 20f;
        
        // Build the table
        Table myTable = Table.builder()
                .addColumnsOfWidth(colWidth, colWidth)
                .padding(tablePadding)
                .borderWidth(borderWidth)
                .addRow(Row.builder()
                        .height(rowHeight)
                        .verticalAlignment(VerticalAlignment.MIDDLE)
                        .add(TextCell.builder().text("Datum der Aufnahme:").borderWidthRight(0).build())
                        .add(TextCell.builder().text(DATUM.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).borderWidthLeft(0).build())
                        .build())
                .addRow(Row.builder()
                        .height(rowHeight)
                        .verticalAlignment(VerticalAlignment.MIDDLE)
                        .add(TextCell.builder().text("Info 1:").borderWidthRight(0).build())
                        .add(TextCell.builder().text(INFO1).borderWidthLeft(0).build())
                        .build())
                .addRow(Row.builder()
                        .height(rowHeight)
                        .verticalAlignment(VerticalAlignment.MIDDLE)
                        .add(TextCell.builder().text("Info 2:").borderWidthRight(0).build())
                        .add(TextCell.builder().text(INFO2).borderWidthLeft(0).build())
                        .build())
                .addRow(Row.builder()
                        .height(rowHeight)
                        .verticalAlignment(VerticalAlignment.MIDDLE)
                        .add(TextCell.builder().text("Bild links:").borderWidthBottom(0).build())
                        .add(TextCell.builder().text("Bild rechts:").borderWidthBottom(0).build())
                        .build())
                .addRow(Row.builder()
                        .add(ImageCell.builder()
                                .verticalAlignment(VerticalAlignment.MIDDLE)
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                .maxHeight(300f)
                                .image(image1)
                                .borderWidthTop(0)
                                .build())
                        .add(ImageCell.builder()
                                .verticalAlignment(VerticalAlignment.MIDDLE)
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                .maxHeight(300f)
                                .image(image2)
                                .borderWidthTop(0)
                                .build())
                        .build())
                .build();
        
        return myTable;
    }
    
    
    private static Path getTMPFile() {
        Path tmpdir = Paths.get(System.getProperty ("java.io.tmpdir"));
        Path tmpfile = tmpdir.resolve(String.valueOf(System.currentTimeMillis()) + ".pdf");
        return tmpfile;
    }
}
