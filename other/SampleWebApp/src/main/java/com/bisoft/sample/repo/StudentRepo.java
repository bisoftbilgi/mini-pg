package com.bisoft.sample.repo;

import com.bisoft.sample.model.Student;
import org.springframework.data.repository.CrudRepository;

public interface StudentRepo extends CrudRepository<Student, Long> {
    
}
