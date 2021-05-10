package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class ItemDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ItemEntity> getItemsforCategory(Integer categoryId){
        try{
            return entityManager.createNativeQuery("select i.* from item i inner join (select ci.* from category_item ci where ci.category_id = ?) c on i.id = c.item_id", ItemEntity.class)
                    .setParameter(1, categoryId).getResultList();
        }
        catch(NoResultException exception) {
            return null;
        }
    }

    public List<ItemEntity> getTopItems (Integer restaurantId){
        try {
            return entityManager.createNativeQuery("select i.* from item i inner join (select oi.item_id from order_item oi inner join (select o.* from orders o where o.restaurant_id = ?) order_filter on order_filter.id = oi.order_id group by oi.item_id order by count(oi.item_id) desc limit 5) top_items on i.id=top_items.item_id;", ItemEntity.class)
                    .setParameter(1, restaurantId)
                    .getResultList();
        }
        catch (NoResultException exception){
            return null;
        }
    }
}
