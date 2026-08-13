package com.taskvoice.dao;

import com.taskvoice.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentDAO {
    Optional<Department> findById(int id);
    List<Department> findAll();
    int insert(Department department);
    void update(Department department);
    void delete(int id);
    int countEmployees(int departmentId);
}
