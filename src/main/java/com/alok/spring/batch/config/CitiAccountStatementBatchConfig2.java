package com.alok.spring.batch.config;

import com.alok.spring.batch.model.RawTransaction;
import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.PDFReader;
import com.alok.spring.batch.repository.ProcessedFileRepository;
import com.alok.spring.batch.utils.DefaultLineExtractor;
import com.alok.spring.batch.utils.LineExtractor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;

import java.awt.geom.QuadCurve2D;

@Configuration
@EnableBatchProcessing
public class CitiAccountStatementBatchConfig2 {
    @Value("file:${file.path.citi_account.password2}")
    private Resource[] resources;

    @Value("${file.password.citi.password2}")
    private String filePassword;

    private ProcessedFileRepository processedFileRepository;

    @Bean("CitiBankJob2")
    public Job citiBankJob1(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<RawTransaction> citiItemsReader2,
                           ItemProcessor<RawTransaction, Transaction> citiBankAccountProcessor,
                           ItemWriter<Transaction> bankAccountDbWriter,
                            ProcessedFileRepository processedFileRepository
    ) {
        this.processedFileRepository = processedFileRepository;
        Step step1 = stepBuilderFactory.get("CitiAccount-ETL-Job2-file-load")
                .<RawTransaction,Transaction>chunk(1000)
                .reader(citiItemsReader2)
                .processor(citiBankAccountProcessor)
                .writer(bankAccountDbWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("CitiAccount-ETL-Job2-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("CitiAccount-ETL-Job2")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }



    @Bean
    public MultiResourceItemReader<RawTransaction> citiItemsReader2(PDFReader citiItemReader2) {

        MultiResourceItemReader<RawTransaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(citiItemReader2);
        return reader;
    }

    @Bean
    @DependsOn({"processedFileRepository"})
    public PDFReader citiItemReader2(@Qualifier("PDFReader") PDFReader flatFileItemReader) {

        //return CitiUtils.getCitiItemReader(filePassword, processedFileRepository);
        //PDFReader flatFileItemReader = new PDFReader(processedFileRepository);
        flatFileItemReader.setName("CitiBank-PDF-Reader2");
        flatFileItemReader.setFilePassword(filePassword);

        LineExtractor defaultLineExtractor = new DefaultLineExtractor();
        defaultLineExtractor.setStartReadingText("Date Transaction.*");
        defaultLineExtractor.setEndReadingText("Banking Reward Points.*");
        defaultLineExtractor.setLinesToSkip(
                new String[] {
                        "^Your  Citibank  Account.*",
                        "^Statement  Period.*",
                        "^Page .*"
                }
        );

        flatFileItemReader.setLineExtractor(defaultLineExtractor);

        return flatFileItemReader;
    }
}
