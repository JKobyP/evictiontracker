package org.dsacleveland.evictiontracker.service.evictiondata;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.dsacleveland.evictiontracker.model.evictiondata.dto.CaseDto;
import org.dsacleveland.evictiontracker.service.pdfreader.PdfCaseParser;
import org.dsacleveland.evictiontracker.service.pdfreader.PdfReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PdfUploadService {

    private PdfCaseParser pdfCaseParser;
    private PdfReader pdfReader;
    private CaseService caseService;
    private TaskExecutor taskExecutor;

    @Autowired
    public PdfUploadService(PdfCaseParser pdfCaseParser, PdfReader pdfReader,
                            CaseService caseService, TaskExecutor taskExecutor) {
        this.pdfCaseParser = pdfCaseParser;
        this.pdfReader = pdfReader;
        this.taskExecutor = taskExecutor;
        this.caseService = caseService;
    }

    public void loadPdf(byte[] pdfBytes, Runnable callback) throws IOException {
        PDDocument document = PDDocument.load(pdfBytes);
        List<CaseDto> cases = pdfCaseParser.parseCasesFromPdfText(pdfReader.readFromPdf(document));

        taskExecutor.execute(() -> {
            cases.forEach(caseDto -> caseService.create(caseDto));
            callback.run();
        });
    }
}
