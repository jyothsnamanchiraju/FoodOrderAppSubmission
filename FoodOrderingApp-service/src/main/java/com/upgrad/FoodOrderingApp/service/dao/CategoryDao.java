package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CategoryDao {

    @PersistenceContext
    private EntityManager entityManager;

    // get all categories from the table
    public List<CategoryEntity> getAllCategory(){
        try{
            return entityManager.createNativeQuery("Select c.* from category c order by c.category_name desc", CategoryEntity.class)
                    .getResultList();
        }
        catch(NoResultException exception)
        {
            return null;
        }
    }

    public CategoryEntity getCategoryById(String uuid){
        try{
            return (CategoryEntity) entityManager.createNativeQuery("select c.* from category c where LOWER(c.uuid) similar to LOWER(?);", CategoryEntity.class)
                    .setParameter(1, uuid)
                    .getSingleResult();
        }
        catch (NoResultException exception){
            return null;
        }
    }

}
