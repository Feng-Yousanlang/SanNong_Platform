package com.ltqtest.springbootquickstart.home.controller;

import com.ltqtest.springbootquickstart.common.Result;
import com.ltqtest.springbootquickstart.home.entity.News;
import com.ltqtest.springbootquickstart.home.repository.NewsRepository;
import com.ltqtest.springbootquickstart.loan.entity.FinancialProduct;
import com.ltqtest.springbootquickstart.loan.repository.FinancialProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class HomeController {
    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private FinancialProductRepository financialProductRepository;

    @GetMapping("/news")
    public Result<Map<String, Object>> getNewsList() {
        try {
            List<News> newsList = newsRepository.findAll(Sort.by(Sort.Direction.ASC, "newsId"));

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("newsList", newsList);

            return Result.success(responseData);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误");
        }
    }

    @GetMapping("/financing/products/")
    public Result<Map<String, Object>> getFinancialProducts() {
        try {
            List<FinancialProduct> products = financialProductRepository.findAll();

            Map<String, Object> responseData = new HashMap<>();
            List<Map<String, Object>> productList = new ArrayList<>();
            for (FinancialProduct product : products) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("fpId", product.getFpId());
                productMap.put("fpName", product.getFpName());
                productMap.put("fpDescription", product.getFpDescription());
                productMap.put("annualRate", product.getAnnualRate());
                productMap.put("tags", product.getTagsArray());
                productList.add(productMap);
            }

            responseData.put("products", productList);

            if (productList.isEmpty()) {
                Result<Map<String, Object>> result = new Result<>();
                result.setCode(200);
                result.setMessage("暂无金融产品数据");
                result.setData(responseData);
                return result;
            }

            return Result.success(responseData);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误");
        }
    }

    @PostMapping("/financial/products/")
    public Result<Map<String, Object>> getFinancialProductDetail(@RequestBody Map<String, Integer> request) {
        try {
            Integer fpId = request.get("fpId");
            if (fpId == null || fpId <= 0) {
                return Result.error(400, "参数错误");
            }

            Optional<FinancialProduct> productOptional = financialProductRepository.findById(fpId);
            if (!productOptional.isPresent()) {
                return Result.error(300, "该金融产品不存在");
            }

            FinancialProduct product = productOptional.get();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fpId", product.getFpId());
            responseData.put("fpName", product.getFpName());
            responseData.put("fpDescription", product.getFpDescription());
            responseData.put("annualRate", product.getAnnualRate());
            responseData.put("tags", product.getTagsArray());
            responseData.put("fpManagerName", product.getFpManagerName());
            responseData.put("fpManagerPhone", product.getFpManagerPhone());
            responseData.put("fpManagerEmail", product.getFpManagerEmail());

            Result<Map<String, Object>> result = new Result<>();
            result.setCode(200);
            result.setMessage("操作成功");
            result.setData(responseData);
            return result;
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误");
        }
    }
}
