package com.bisoft.sample;

import com.bisoft.sample.model.Student;
import com.bisoft.sample.repo.StudentRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@Slf4j
public class SampleWebAppApplication   extends SpringBootServletInitializer implements CommandLineRunner   {
    
    static long a;

    static long b;

    long c;

    long d;
 
    @Autowired
    private StudentRepo repository;
    
    public static void main(String[] args)  {
        a = System.currentTimeMillis();
      
        
        
        SpringApplication.run(SampleWebAppApplication.class, args);
        // some code 1
        b = System.currentTimeMillis();

    }
    
    @Override
    public void run(String... args) {
        // some code 2
        c = System.currentTimeMillis();
      
        
        log.info("StartApplication...");
        
        Student student= Student.builder()
                .name("John12432141234")
//                .rowUuid(UUID.randomUUID())
                .studentNumber(1L)
                .surname("JOWE")
                .build();
         
        repository.save(student);
         
        Student student2 = Student.builder()
                .name("Jacky12341234321423")
//                .rowUuid(UUID.randomUUID())
                .studentNumber(2L)
                .surname("OWEL")
                .build();
        
        repository.save(student2);
        
        System.out.println("\nfindAll()");
        repository.findAll().forEach(x -> System.out.println(x));
        
        System.out.println("\nfindById(1L)");
        repository.findById(1l).ifPresent(x -> System.out.println(x));
        
//        System.out.println("\nfindByName('Node')");
//        repository.findByName("John").forEach(x -> System.out.println(x));
        // some code 3
        d = System.currentTimeMillis();
        System.out.println("Some code 1 took "+(b-a)+" mil to execute. ("+((b-a)/1000)+" seconds)");
        System.out.println("Some code 2 took "+(c-b)+" mil to execute. ("+((c-b)/1000)+" seconds)");
        System.out.println("Some code 3 took "+(d-c)+" mil to execute. ("+((d-c)/1000)+" seconds)");
        System.out.println("Some code 3 took "+(d-c)+" mil to execute. ("+((d-c)/1000)+" seconds)");

    }
    
}
