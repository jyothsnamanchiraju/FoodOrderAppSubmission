package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    public List<CategoryEntity> getAllCategoriesOrderedByName() throws CategoryNotFoundException {
        List<CategoryEntity> result = categoryDao.getAllCategory();
        if(result==null){
            throw new CategoryNotFoundException("CNF-002", "Category doesn't exist");
        }
        return result;
    }

    public CategoryEntity getCategoryById(String uuid) throws CategoryNotFoundException {
        CategoryEntity result = categoryDao.getCategoryById(uuid);

        if(result==null){
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }

        return result;

    }

    public List<CategoryEntity> getCategoriesForRestaurant(Integer id) {
        return categoryDao.getCategoryForRestaurant(id);
    }
}
