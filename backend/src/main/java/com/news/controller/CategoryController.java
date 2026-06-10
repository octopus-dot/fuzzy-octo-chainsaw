package com.news.controller;

import com.news.common.Result;
import com.news.entity.Category;
import com.news.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> listAll() {
        return Result.ok(categoryService.listAll());
    }
}
