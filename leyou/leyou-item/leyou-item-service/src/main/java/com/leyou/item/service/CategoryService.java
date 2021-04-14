package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    //根据父节点查询子节点
    public List<Category> queryCatagoryById(Long pid) {
        Category record = new Category();
        record.setParentId(pid);
        return this.categoryMapper.select(record);
    }

    //根据多个id查询分类名称
    public List<String> queryNameByIds(List<Long> ids) {
        List<Category> categories = this.categoryMapper.selectByIdList(ids);
        return categories.stream().map(category -> category.getName()).collect(Collectors.toList());
    }

    public void deleteCategoryById(Long id) {
//        this.categoryMapper.deleteById(id);
        this.categoryMapper.deleteByPrimaryKey (id);
        System.out.println (id);
    }

    /**
     * 根据品牌id查询分类
     * @param bid
     * @return
     */
    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }

    /**
     * 新增分类
     * @param category
     */
    public void saveCategory(Category category) {
        /**
         * 将本节点插入到数据库中
         * 将此category的父节点的isParent设为true
         */
        //1.首先置id为null
        category.setId(null);
        //2.保存
        this.categoryMapper.insert(category);
        //3.修改父节点
        Category parent = new Category();
        parent.setId(category.getParentId());
        parent.setIsParent(true);
        this.categoryMapper.updateByPrimaryKeySelective(parent);
    }

    /**
     * 修改分类
     * @param category
     */
    public void updateCategory(Category category) {
        this.categoryMapper.updateByPrimaryKeySelective(category);
    }


    public List<Category> queryCategorysParentList() {
        return this.categoryMapper.selectParentList();
    }
}
