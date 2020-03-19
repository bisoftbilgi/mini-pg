package com.bisoft.sample.controller;

import com.bisoft.sample.model.Student;
import com.bisoft.sample.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentController {
    
    @Autowired
    StudentService studentService;
    
    @GetMapping("/student/")
    public Iterable<Student> studentList() {
        
        return studentService.listAll();
    }
}
