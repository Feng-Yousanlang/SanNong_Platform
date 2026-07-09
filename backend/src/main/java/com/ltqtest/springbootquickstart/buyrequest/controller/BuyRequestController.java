package com.ltqtest.springbootquickstart.buyrequest.controller;

import com.ltqtest.springbootquickstart.common.Result;
import com.ltqtest.springbootquickstart.buyrequest.entity.BuyRequest;
import com.ltqtest.springbootquickstart.buyrequest.repository.BuyRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/buyRequest")
public class BuyRequestController {
    
    private static final Logger logger = Logger.getLogger(BuyRequestController.class.getName());
    
    @Autowired
    private BuyRequestRepository buyRequestRepository;
    
    @PostMapping("/publish")
    public Result<Map<String, Object>> publishBuyRequest(@RequestBody Map<String, Object> requestBody) {
        try {
            if (requestBody == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            String title = (requestBody.get("title")).toString();
            String content = (requestBody.get("content")).toString();
            String contact = (requestBody.get("contact")).toString();
            
            if (title == null || title.trim().isEmpty()) {
                title = "暂无标题";
            }
            
            if (content == null || content.trim().isEmpty()) {
                return Result.error(400, "求购说明内容不能为空");
            }
            
            BuyRequest buyRequest = new BuyRequest();
            buyRequest.setTitle(title);
            buyRequest.setContent(content);
            buyRequest.setContact(contact);
            buyRequest.setCreateTime(new Date());
            
            BuyRequest saved = buyRequestRepository.save(buyRequest);
            
            Map<String, Object> data = new HashMap<>();
            data.put("buyRequestId", saved.getBuyRequestId());
            data.put("title", saved.getTitle());
            data.put("content", saved.getContent());
            data.put("contact", saved.getContact());
            data.put("createTime", saved.getCreateTime());
            
            return Result.success(200, "发布成功", data);
        } catch (Exception e) {
            logger.severe("发布求购需求失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
    
    @DeleteMapping("/delete")
    public Result<?> deleteBuyRequest(@RequestParam("id") Integer buyRequestId) {
        try {
            if (buyRequestId == null || buyRequestId <= 0) {
                return Result.error(400, "参数错误：求购ID不能为空且必须大于0");
            }
            
            if (!buyRequestRepository.existsById(buyRequestId)) {
                return Result.error(404, "求购需求不存在");
            }
            
            buyRequestRepository.deleteById(buyRequestId);
            
            return Result.success(200, "删除成功");
        } catch (Exception e) {
            logger.severe("删除求购需求失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
    
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchBuyRequests(
        @RequestBody Map<String, String> requestBody) {
        try {
            List<BuyRequest> buyRequests;
            
            if (requestBody.containsKey("keyword") && !requestBody.get("keyword").trim().isEmpty()) {
                String keyword = requestBody.get("keyword").trim();
                buyRequests = buyRequestRepository.searchByKeyword(keyword);
                if ("time_asc".equals(requestBody.get("sort"))) {
                    buyRequests.sort(Comparator.comparing(BuyRequest::getCreateTime));
                } else {
                    buyRequests.sort(Comparator.comparing(BuyRequest::getCreateTime).reversed());
                }
            } else {
                if ("time_asc".equals(requestBody.get("sort"))) {
                    buyRequests = buyRequestRepository.findAllByOrderByCreateTimeAsc();
                } else {
                    buyRequests = buyRequestRepository.findAllByOrderByCreateTimeDesc();
                }
            }
            
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (BuyRequest buyRequest : buyRequests) {
                Map<String, Object> data = new HashMap<>();
                data.put("buyRequestId", buyRequest.getBuyRequestId());
                data.put("title", buyRequest.getTitle());
                data.put("content", buyRequest.getContent());
                data.put("contact", buyRequest.getContact());
                data.put("createTime", buyRequest.getCreateTime());
                dataList.add(data);
            }
            
            return Result.success(200, "搜索成功", dataList);
        } catch (Exception e) {
            logger.severe("检索求购需求失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
    
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getBuyRequestList() {
        try {
            List<BuyRequest> buyRequests = buyRequestRepository.findAllByOrderByCreateTimeDesc();
            
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (BuyRequest buyRequest : buyRequests) {
                Map<String, Object> data = new HashMap<>();
                data.put("buyRequestId", buyRequest.getBuyRequestId());
                data.put("title", buyRequest.getTitle());
                data.put("content", buyRequest.getContent());
                data.put("contact", buyRequest.getContact());
                data.put("createTime", buyRequest.getCreateTime());
                dataList.add(data);
            }
            
            return Result.success(200, "获取求购需求列表成功", dataList);
        } catch (Exception e) {
            logger.severe("获取求购需求列表失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "服务器内部错误，请稍后再试");
        }
    }
}
