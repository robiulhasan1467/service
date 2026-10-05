package com.chocolateshop.service;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Expense;
import com.chocolateshop.entity.ExpenseCategory;
import com.chocolateshop.repository.ExpenseCategoryRepository;
import com.chocolateshop.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ExpenseCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategory> getActiveCategories() {
        return categoryRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public ExpenseCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense category not found with id: " + id));
    }

    @Transactional
    public ExpenseCategory saveCategory(ExpenseCategory category) {
        if (category.getId() == null) {
            if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
                throw new IllegalArgumentException("Expense category already exists: " + category.getName());
            }
        } else {
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(category.getName(), category.getId())) {
                throw new IllegalArgumentException("Another expense category already exists: " + category.getName());
            }
        }
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<Expense> getExpenses(Long categoryId,
                                     Enums.PaymentMethod paymentMethod,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     Pageable pageable) {
        return expenseRepository.findWithFilters(categoryId, paymentMethod, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with id: " + id));
    }

    @Transactional
    public Expense saveExpense(Expense expense) {
        if (expense.getAmount() == null || expense.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumExpensesBetween(LocalDate start, LocalDate end) {
        return expenseRepository.sumExpensesBetween(start, end);
    }
}
