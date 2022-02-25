package com.alok.spring.batch.controller;

import com.alok.spring.batch.response.GetTransactionResponse;
import com.alok.spring.batch.response.GetTransactionsResponse;
import com.alok.spring.batch.response.UploadFileResponse;
import com.alok.spring.batch.service.BankService;
import com.alok.spring.batch.service.FileStorageService;
import com.alok.spring.batch.service.JobExecutorService;
import com.alok.spring.batch.utils.UploadType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/expense")
public class ExpenseController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JobExecutorService jobExecutorService;

    @Autowired
    private BankService bankService;


    @CrossOrigin
    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadFileResponse> uploadStatement(
            @RequestParam MultipartFile file
    ) {

        log.info("Uploaded file: {}, type: {}, size: {}", file.getOriginalFilename(),
                file.getContentType(), file.getSize());

        String fineName = fileStorageService.storeFile(file, UploadType.ExpenseGoogleSheet);


        // brute force way
        try {
            jobExecutorService.executeAllJobs();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .body(
                        UploadFileResponse.builder()
                                .fileName(fineName)
                                .size(file.getSize())
                                .fileType(file.getContentType())
                                .message("File submitted for processing")
                                .build()
                );
    }

    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetTransactionsResponse getAllTransactions() {
        return bankService.getAllTransactions();
    }

    @CrossOrigin
    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetTransactionResponse getTransaction(@PathVariable(value = "id") Integer id) {
        return bankService.getTransaction(id);
    }
}