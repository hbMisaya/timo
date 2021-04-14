package com.leyou.item.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;



    //
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //初始化Example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        //根据name模糊查询，或者根据首字母查询
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("name", "%"+key+"%").orEqualTo("letter",key);
        }

        //添加分页条件
        PageHelper.startPage(page,rows);
        //添加排序条件
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+" "+(desc?"desc":"asc"));
        }
        List<Brand> brands = this.brandMapper.selectByExample(example);
        //包装成PageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //包装成分页结果集
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

    //新增品牌
//    @Transactional
//    public void saveBrand(Brand brand,List<Long > cids) {
//        //先新增Brand表
//        this.brandMapper.insertSelective(brand);
//        //在新增中间表
//            cids.forEach(cid ->{
//                this.brandMapper.insertCategoryAndBrand(cid,brand.getId());
//            });
//    }

    @Transactional(rollbackFor = Exception.class)
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌信息
        int i = this.brandMapper.insertSelective(brand);
        if (i!=1){
            throw new RuntimeException();
        }
        // 新增品牌和分类中间表
        for (Long cid : cids) {
            this.brandMapper.insertCategoryAndBrand(cid, brand.getId());
        }
    }

    public List<Brand> queryBrandByCid(Long cid) {

        return this.brandMapper.selectBrandsByCid(cid);
    }

    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 品牌更新
     * @param brand
     * @param categories
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBrand(Brand brand,List<Long> categories) {
        deleteByBrandIdInCategoryBrand(brand.getId());
        // 修改品牌信息
        this.brandMapper.updateByPrimaryKeySelective(brand);

        //维护品牌和分类中间表
        for (Long cid : categories) {
            System.out.println("cid:"+cid+",bid:"+brand.getId());
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }

    /**
     * 删除中间表中的数据
     * @param bid
     */
    public void deleteByBrandIdInCategoryBrand(Long bid) {
        brandMapper.deleteByBrandIdInCategoryBrand(bid);
    }
}
