package com.leyou.item.controller;

import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //根据父节点的id查询子节点
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategorysByPid(@RequestParam(value = "pid", defaultValue = "0") Long pid) {
        if (pid == null || pid < 0) {
            // return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCatagoryById(pid);
        if (CollectionUtils.isEmpty(categories)) {
            //资源服务器未找到
            //return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            return ResponseEntity.noContent().build();
        }
        //查询成功
        return ResponseEntity.ok(categories);

        // 500服务器内部错误
        //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    //查询二级节点
    @GetMapping("isParentList")
    public ResponseEntity<List<Category>> queryCategorysParentList() {
        List<Category> categories = this.categoryService.queryCategorysParentList();
        if (CollectionUtils.isEmpty(categories)) {
            //资源服务器未找到
            //return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            return ResponseEntity.noContent().build();
        }
        //查询成功
        return ResponseEntity.ok(categories);
        // 500服务器内部错误
        //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 用于修改品牌信息时，商品分类信息的回显
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid){
        List<Category> list = categoryService.queryByBrandId(bid);
        if(list == null || list.size() < 1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping
    public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids") List<Long> ids){
        List<String> names = this.categoryService.queryNameByIds(ids);
        if (CollectionUtils.isEmpty(names)) {
            //资源服务器未找到
            //return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(names);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable("id")Long id){
        if (id == null || id < 0) {
            return ResponseEntity.badRequest().build();
        }
        this.categoryService.deleteCategoryById(id);
        return ResponseEntity.status (HttpStatus.CREATED).build ();
    }

    /**
     * 保存（新增分类）
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveCategory(Category category){
        this.categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 更新
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCategory(Category category){
        this.categoryService.updateCategory(category);
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
