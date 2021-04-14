package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Specification;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    //根据分类id查询参数组
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsById(@PathVariable("cid")Long cid){
        List<SpecGroup> groups=this.specificationService.queryGroupsById(cid);
        if (CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }

    //根据条件查询规格参数
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(@RequestParam(value = "gid",required = false)Long gid,
                                                       @RequestParam(value = "cid",required = false)Long cid,
                                                       @RequestParam(value = "generic",required = false)Boolean generic,
                                                       @RequestParam(value = "searching",required = false)Boolean searching)
                                                       {
        List<SpecParam> params=this.specificationService.queryParams(gid,cid,generic,searching);
        if (CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }

    @PostMapping("param")
    public ResponseEntity<Void> saveParam(@RequestBody SpecParam specParam){
        this.specificationService.saveParam (specParam);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("param")
    public ResponseEntity<Void> updateParam(@RequestBody SpecParam specParam){
        this.specificationService.updateParam (specParam);
        return ResponseEntity.status(HttpStatus.OK).build();
    }



    //根据id删除
    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> deleteParam(@PathVariable("id")Long id){
        this.specificationService.deleteParam(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //根据分组保存
    @PostMapping("group")
    public ResponseEntity<Void> saveSpecification(@RequestBody SpecGroup specGroup){
        this.specificationService.saveSpecGroup(specGroup);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @PutMapping("group")
    public ResponseEntity<Void> updateSpecification(SpecGroup specGroup){
        this.specificationService.updateSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsWithParam(@PathVariable("cid")Long cid){
        List<SpecGroup> groups=this.specificationService.queryGroupsWithParam(cid);
        if (CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }
}
