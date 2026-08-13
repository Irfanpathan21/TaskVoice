package com.taskvoice.dao;

import com.taskvoice.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryDAO {
    List<Category> findAll();
    Optional<Category> findById(int id);
    int insert(Category category);
    void update(Category category);
    void delete(int id);
}
