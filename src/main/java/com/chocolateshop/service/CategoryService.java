package com.chocolateshop.service;

import com.chocolateshop.entity.Category;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    @Transactional
    public Category saveCategory(Category category) {
        if (category.getId() == null) {
            if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
                throw new IllegalArgumentException("Category with this name already exists: " + category.getName());
            }
        } else {
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(category.getName(), category.getId())) {
                throw new IllegalArgumentException("Another category with this name already exists: " + category.getName());
            }
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Category category = getCategoryById(id);
        category.setStatus(category.getStatus() == Enums.Status.ACTIVE ? Enums.Status.INACTIVE : Enums.Status.ACTIVE);
        categoryRepository.save(category);
    }
}
