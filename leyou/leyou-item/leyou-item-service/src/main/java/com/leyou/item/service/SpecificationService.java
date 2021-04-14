package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.mapper.SpecificationMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper groupMapper;

    @Autowired
    private SpecParamMapper paramMapper;

    @Autowired
    private SpecificationMapper specificationMapper;

    //根据分类id查询参数组
    public List<SpecGroup> queryGroupsById(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return this.groupMapper.select(record);
    }

    //根据条件查询规格参数
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return this.paramMapper.select(record);
    }

    //根据参数保存
    public void saveParam(SpecParam param) {
        int i = this.paramMapper.insertSelective(param);
        if (i != 1) {
            throw new RuntimeException();
        }
    }

    //删除命名
    public void deleteParam(Long id) {
        int i = this.paramMapper.deleteByPrimaryKey(id);
        if (i != 1) {
            throw new RuntimeException();
        }
    }

    //修改命令
    public void updateParam(SpecParam param) {

        int i = paramMapper.updateByPrimaryKeySelective(param);
        if (i != 1) {
            throw new RuntimeException();
        }
    }

    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> groups = this.queryGroupsById(cid);
        groups.forEach(group -> {
            List<SpecParam> params = this.queryParams(group.getId(), null, null, null);
            group.setParams(params);
        });
        return groups;
    }

    public void saveSpecification(Specification specification) {
        this.specificationMapper.insert (specification);
    }

    public void updateSpecificationm(Specification specification) {
        this.specificationMapper.updateByPrimaryKeySelective(specification);
    }

    public void saveSpecGroup(SpecGroup specGroup) {
        this.groupMapper.insert (specGroup);
    }

    public void updateSpecGroup(SpecGroup specGroup) {
        this.groupMapper.updateByPrimaryKeySelective (specGroup);
    }
}
