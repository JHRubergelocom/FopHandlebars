package test;

import com.aspose.cells.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.Objects;

public class AsposeTest {
    private void removeEvaluation(String csvFilePath) {
        String csvFilePath_ = csvFilePath + "_";
        if (new File(csvFilePath).renameTo(new File(csvFilePath_))) {
            try {
                BufferedReader fromFile = new BufferedReader(new FileReader(csvFilePath_));
                BufferedWriter toFile = new BufferedWriter(new FileWriter(csvFilePath));

                String aktLine = fromFile.readLine();
                while (aktLine != null) {
                    if (!aktLine.contains("Aspose")) {
                        toFile.write(aktLine);
                        toFile.newLine();
                    }
                    aktLine = fromFile.readLine();
                }
                toFile.flush();
                fromFile.close();
                toFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TestConvertCSV.xlsx"})
    public void TestConvertExcelToCSV(String xlsxFile) throws Exception {

        String dirPath = xlsxFile.split("\\.")[0];
        new File(dirPath).mkdir();

        TxtSaveOptions opts = new TxtSaveOptions(SaveFormat.CSV);
        opts.setSeparator(';');

        Workbook workbook = new Workbook(xlsxFile);

        for(int idx = 0; idx < workbook.getWorksheets().getCount(); idx++) {
            String csvFileName = workbook.getWorksheets().get(idx).getName() + ".csv";
            Worksheet worksheet = workbook.getWorksheets().get(idx);
            worksheet.moveTo(0);
            workbook.save(dirPath + "/" + csvFileName, opts);
        }

        // Remove 'Evaluation' from CSV File
        File file = new File(dirPath);
        for (String csvFileName: Objects.requireNonNull(file.list())) {
            final String csvFilePath = dirPath + "\\" + csvFileName;
            removeEvaluation(csvFilePath);
        }
        file = new File(dirPath);
        for (String csvFileName: Objects.requireNonNull(file.list())) {
            final String csvFilePath = dirPath + "\\" + csvFileName;
            if (csvFilePath.contains(".csv_")) {
                new File(csvFilePath).delete();
            }
        }
    }
}
