package com.chocolateshop.service;

import com.chocolateshop.entity.Brand;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Brand> getActiveBrands() {
        return brandRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Brand getBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + id));
    }

    @Transactional
    public Brand saveBrand(Brand brand) {
        if (brand.getId() == null) {
            if (brandRepository.existsByNameIgnoreCase(brand.getName())) {
                throw new IllegalArgumentException("Brand with this name already exists: " + brand.getName());
            }
        } else {
            if (brandRepository.existsByNameIgnoreCaseAndIdNot(brand.getName(), brand.getId())) {
                throw new IllegalArgumentException("Another brand with this name already exists: " + brand.getName());
            }
        }
        return brandRepository.save(brand);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Brand brand = getBrandById(id);
        brand.setStatus(brand.getStatus() == Enums.Status.ACTIVE ? Enums.Status.INACTIVE : Enums.Status.ACTIVE);
        brandRepository.save(brand);
    }
}
