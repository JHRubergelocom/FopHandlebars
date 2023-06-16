package test;

import org.apache.fop.apps.*;
import org.junit.jupiter.api.Test;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class FopTest {
    private void convertFO2PDF(File foFile, File pdfFile) throws IOException, FOPException, TransformerException {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            fos = new FileOutputStream(pdfFile);
            bos = new BufferedOutputStream(fos);
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, bos);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            Source src = new StreamSource(foFile);
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);

            // Result processing
            FormattingResults foResults = fop.getResults();
            java.util.List pageSequences = foResults.getPageSequences();
            for (Object pageSequence : pageSequences) {
                PageSequenceResults pageSequenceResults = (PageSequenceResults) pageSequence;
                System.out.println("PageSequence "
                        + (String.valueOf(pageSequenceResults.getID()).length() > 0
                        ? pageSequenceResults.getID() : "<no id>")
                        + " generated " + pageSequenceResults.getPageCount() + " pages.");
            }
            System.out.println("Generated " + foResults.getPageCount() + " pages in total.");
        } finally {
            bos.close();
        }
    }
    @Test
    public void TestFop() throws FOPException, IOException, TransformerException {
        File foFile = new File("Helloworld.fo");
        File pdfFile = new File("Helloworld.pdf");

        convertFO2PDF(foFile, pdfFile);
    }
}
