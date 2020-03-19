package com.bisoft.sample.service;

import com.bisoft.sample.model.Student;
import com.bisoft.sample.repo.StudentRepo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepo repo;
    
    public Long save(Student student) {
        
        repo.save(student);
        return student.getStudentNumber();
    }
    
    public Iterable<Student> listAll(){
        Iterable<Student> result=  repo.findAll();
        for (Student student : result) {
            System.out.println(student.getName());
        }
        return  result;
    }
    
}
