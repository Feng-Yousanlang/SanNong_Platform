package com.ltqtest.springbootquickstart.expert.controller;

import com.ltqtest.springbootquickstart.common.Result;
import com.ltqtest.springbootquickstart.integration.deepseek.dto.DeepseekRequest;
import com.ltqtest.springbootquickstart.expert.entity.Expert;
import com.ltqtest.springbootquickstart.expert.entity.ExpertAppointment;
import com.ltqtest.springbootquickstart.expert.entity.ExpertUserChatRecord;
import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.expert.repository.ExpertRepository;
import com.ltqtest.springbootquickstart.expert.repository.ExpertUserChatRecordRepository;
import com.ltqtest.springbootquickstart.expert.repository.ExpertAppointmentRepository;
import com.ltqtest.springbootquickstart.user.repository.UserRepository;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import cn.hutool.log.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.gson.Gson;

@RestController
@RequestMapping("/api")
public class ExpertController {

    @Autowired
    private ExpertRepository expertRepository;
    
    @Autowired
    private ExpertAppointmentRepository expertAppointmentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpertUserChatRecordRepository expertUserChatRecordRepository;

    @Value("${deepseek.api-key:}")
    private String deepseekApiKey;

    private final Gson gson = new Gson();

    @GetMapping("/experts/")
    public Result<Map<String, Object>> getExperts() {
        try {
            List<Expert> experts = expertRepository.findAll();
            
            List<Map<String, Object>> expertList = new ArrayList<>();
            for (Expert expert : experts) {
                Map<String, Object> expertMap = new HashMap<>();
                expertMap.put("expertId", expert.getExpertId());
                expertMap.put("name", expert.getExpertName());
                expertMap.put("field", expert.getField());
                expertMap.put("expertDescription", expert.getExpertDescription());
                expertList.add(expertMap);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("experts", expertList);
            
            return Result.success(200, "获取专家列表成功", responseData);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/expert-appointment/create")
    public Result<Map<String, Object>> createExpertAppointment(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            if (!request.containsKey("expertId") || request.get("expertId") == null) {
                return Result.error(400, "参数错误：专家ID不能为空");
            }
            if ((!request.containsKey("userId"))) {
                return Result.error(400, "参数错误：用户ID不能为空");
            }
            if (!request.containsKey("date") || request.get("date") == null) {
                return Result.error(400, "参数错误：预约日期不能为空");
            }
            if (!request.containsKey("startTime") || request.get("startTime") == null) {
                return Result.error(400, "参数错误：开始时间不能为空");
            }
            if (!request.containsKey("endTime") || request.get("endTime") == null) {
                return Result.error(400, "参数错误：结束时间不能为空");
            }
            
            Integer userId;
            try {
                String userIdStr = request.containsKey("user_id") ? request.get("user_id").toString() : request.get("userId").toString();
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：用户ID格式不正确");
            }
            
            String dateStr = request.get("date").toString();
            String startTimeStr = request.get("startTime").toString();
            String endTimeStr = request.get("endTime").toString();
            String topic = request.containsKey("topic") ? request.get("topic").toString() : null;
            String remark = request.containsKey("remark") ? request.get("remark").toString() : null;
            Integer expertId = Integer.parseInt(request.get("expertId").toString());
            
            java.time.LocalDate date;
            try {
                date = java.time.LocalDate.parse(dateStr);
            } catch (Exception e) {
                return Result.error(400, "参数错误：日期格式不正确，请使用YYYY-MM-DD格式");
            }
            Expert expert = expertRepository.findByExpertId(expertId).orElse(null);
            if(expert == null){
                return Result.error(400, "参数错误：专家ID不存在");
            }
        
            java.time.LocalDate currentDate = java.time.LocalDate.now();
            java.time.LocalTime currentTime = java.time.LocalTime.now();
            
            if (date.isBefore(currentDate)) {
                return Result.error(400, "参数错误：预约日期不能早于当前日期");
            }
            
            java.time.LocalTime startTime;
            java.time.LocalTime endTime;
            try {
                startTime = java.time.LocalTime.parse(startTimeStr);
                endTime = java.time.LocalTime.parse(endTimeStr);
                
                if (!endTime.isAfter(startTime)) {
                    return Result.error(400, "参数错误：结束时间必须大于开始时间");
                }
                
                if (date.isEqual(currentDate)) {
                    if (startTime.isBefore(currentTime)) {
                        return Result.error(400, "参数错误：开始时间不能早于当前时间");
                    }
                }
            } catch (Exception e) {
                return Result.error(400, "参数错误：时间格式不正确，请使用HH:mm格式");
            }
            List<ExpertAppointment> existingAppointments = expertAppointmentRepository.findByExpertIdAndDate(expertId, date);
            boolean hasConflict = false;
            
            for (ExpertAppointment existing : existingAppointments) {
                boolean isCompletelyBefore = endTime.isBefore(existing.getStartTime()) || endTime.equals(existing.getStartTime());
                boolean isCompletelyAfter = startTime.isAfter(existing.getEndTime()) || startTime.equals(existing.getEndTime());
                
                if (isCompletelyBefore||isCompletelyAfter) {
                    hasConflict = true;
                    break;
                }
            }
            
            if (hasConflict) {
                return Result.error(409, "该专家在该时间段已被预约，请选择其他时间");
            }
            
            ExpertAppointment appointment = new ExpertAppointment();
            appointment.setExpertId(expertId);
            appointment.setUserId(userId);
            appointment.setDate(date);
            appointment.setStartTime(startTime);
            appointment.setEndTime(endTime);
            appointment.setTopic(topic);
            appointment.setRemark(remark);
            
            ExpertAppointment savedAppointment = expertAppointmentRepository.save(appointment);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("appointmentId", savedAppointment.getAppointmentId());
            responseData.put("status", "pending");
            
            return Result.success(200, "预约申请已提交，等待专家确认", responseData);
            
        } catch (RuntimeException e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/experts/search")
    public Result<Map<String, Object>> searchExperts(@RequestParam("q") String keyword) {
         
        try {

            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error(400, "参数错误：搜索关键词不能为空");
            }
            
            List<Expert> experts = expertRepository.findByExpertNameContainingOrFieldContainingOrExpertDescriptionContaining(
                    keyword, keyword, keyword);
            
            List<Map<String, Object>> expertList = new ArrayList<>();
            for (Expert expert : experts) {
                Map<String, Object> expertMap = new HashMap<>();
                expertMap.put("expertId", expert.getExpertId());
                expertMap.put("name", expert.getExpertName());
                expertMap.put("field", expert.getField());
                expertMap.put("expertDescription", expert.getExpertDescription());
                expertList.add(expertMap);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("experts", expertList);
            return Result.success(200, "搜索专家成功", responseData);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/experts/{expertId}")
    public Result<Map<String, Object>> getExpertDetail(@PathVariable Integer expertId) {
        try {
            if (expertId == null || expertId <= 0) {
                return Result.error(400, "参数错误：专家ID无效");
            }
            
            Expert expert = expertRepository.findById(expertId)
                    .orElseThrow(() -> new RuntimeException("专家不存在"));
            
            Map<String, Object> expertMap = new HashMap<>();
            expertMap.put("expertId", expert.getExpertId());
            expertMap.put("name", expert.getExpertName());
            expertMap.put("field", expert.getField());
            expertMap.put("expertDescription", expert.getExpertDescription());
            expertMap.put("expertImg", expert.getExpertImg());
            expertMap.put("example", expert.getExample());
            expertMap.put("expertPhone", expert.getExpertPhone());
            expertMap.put("expertEmail", expert.getExpertEmail());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("experts", Collections.singletonList(expertMap));
            
            return Result.success(200, "获取专家详情成功", responseData);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return Result.error(404, "资源丢失：专家不存在");
            }
            return Result.error(400, "参数错误：" + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/expert-appointment/user/list")
    public Result<List<Map<String, Object>>> getUserAppointments(@RequestParam("userId") Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：用户ID无效");
            }
            
            List<ExpertAppointment> appointments = expertAppointmentRepository.findByUserId(userId);
            
            List<Map<String, Object>> appointmentList = new ArrayList<>();
            for (ExpertAppointment appointment : appointments) {
                Map<String, Object> appointmentMap = new HashMap<>();
                
                appointmentMap.put("id", appointment.getAppointmentId());
                appointmentMap.put("date", appointment.getDate().toString());
                appointmentMap.put("startTime", appointment.getStartTime().toString());
                appointmentMap.put("endTime", appointment.getEndTime().toString());
                appointmentMap.put("topic", appointment.getTopic());
                appointmentMap.put("status", appointment.getStatus());
                
                Expert expert = appointment.getExpert();
                if (expert != null) {
                    Map<String, Object> expertMap = new HashMap<>();
                    expertMap.put("id", expert.getExpertId());
                    expertMap.put("name", expert.getExpertName());
                    expertMap.put("expertImg", expert.getExpertImg());
                    expertMap.put("field", expert.getField());
                    appointmentMap.put("expert", expertMap);
                }
                
                appointmentList.add(appointmentMap);
            }
            
            return Result.success(200, "获取预约记录成功", appointmentList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/expert-appointment/cancel")
    public Result<Map<String, Object>> cancelAppointment(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            if(!request.containsKey("appointmentId")){
                return Result.error(400, "参数错误：预约ID不能为空");
            }
            Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
            ExpertAppointment appointment = expertAppointmentRepository.findByAppointmentId(appointmentId)
                    .orElseThrow(() -> new RuntimeException("预约记录不存在"));
            
            LocalDate today = LocalDate.now();
            LocalDate appointmentDate = appointment.getDate();
            
            if (appointmentDate.isBefore(today)) {
                return Result.error(400, "该预约已过期，无法取消");
            }
            
            if ("cancelled".equals(appointment.getStatus())) {
                return Result.error(400, "该预约已经被取消");
            }
            
            if ("completed".equals(appointment.getStatus())) {
                return Result.error(400, "该预约已完成，无法取消");
            }
            
            appointment.setStatus("cancelled");
            expertAppointmentRepository.save(appointment);
            
            return Result.success(200, "预约已取消", null);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("专家不存在")) {
                return Result.error(404, "专家不存在");
            }
            if (e.getMessage().contains("预约记录不存在")) {
                return Result.error(404, "预约记录不存在");
            }
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/expert-appointment/pending")
    public Result<List<Map<String, Object>>> getPendingAppointments(
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：用户ID无效");
            }
            
            User user1 = userRepository.findByUserId(userId).orElse(null);
            if (user1 == null) {
                return Result.error(404, "用户不存在");
            }
            
            Integer expertId = user1.getExpertId();
            if (expertId == null) {
                return Result.error(403, "该用户不是专家，无法查看预约列表");
            }
            
            if (page < 1 || size < 1 || size > 100) {
                return Result.error(400, "参数错误：分页参数无效");
            }
            
            if (!expertRepository.existsById(expertId)) {
                return Result.error(404, "专家不存在");
            }
            
            org.springframework.data.domain.PageRequest pageRequest = 
                    org.springframework.data.domain.PageRequest.of(page - 1, size);
            
            org.springframework.data.domain.Page<ExpertAppointment> appointmentPage = 
                    expertAppointmentRepository.findByExpertIdAndStatus(expertId, "pending", pageRequest);
            
            List<Map<String, Object>> appointmentList = new ArrayList<>();
            for (ExpertAppointment appointment : appointmentPage.getContent()) {
                Map<String, Object> appointmentMap = new HashMap<>();
                
                appointmentMap.put("id", appointment.getAppointmentId());
                appointmentMap.put("date", appointment.getDate().toString());
                appointmentMap.put("startTime", appointment.getStartTime().toString());
                appointmentMap.put("endTime", appointment.getEndTime().toString());
                appointmentMap.put("topic", appointment.getTopic());
                appointmentMap.put("remark", appointment.getRemark());
                appointmentMap.put("status", appointment.getStatus());
                
                User user = userRepository.findByUserId(appointment.getUserId()).orElse(null);  
                if (user != null) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getUserId());
                    userMap.put("name", user.getRealName() != null ? user.getRealName() : user.getUsername());
                    userMap.put("avatar", user.getImageUrl() != null ? user.getImageUrl() : "https://example.com/u1.jpg");
                    appointmentMap.put("user", userMap);
                } else {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", appointment.getUserId());
                    userMap.put("name", "未知用户");
                    userMap.put("avatar", "https://example.com/u1.jpg");
                    appointmentMap.put("user", userMap);
                }
                
                appointmentList.add(appointmentMap);
            }
            
            return Result.success(200, "获取待审核预约列表成功", appointmentList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/expert-appointment/review")
    public Result<Map<String, Object>> reviewAppointment(@RequestBody Map<String, Object> request) {
        try {
            
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            if (!request.containsKey("appointmentId") || request.get("appointmentId") == null) {
                return Result.error(400, "参数错误：预约ID不能为空");
            }
            if (!request.containsKey("userId") || request.get("userId") == null) {
                return Result.error(400, "参数错误：用户ID不能为空");
            }
            if (!request.containsKey("action") || request.get("action") == null) {
                return Result.error(400, "参数错误：审批操作不能为空");
            }
            
            Integer userId;
            try {
                userId = Integer.parseInt(request.get("userId").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：用户ID格式不正确");
            }
            
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            if (user.getExpertId() == null) {
                return Result.error(403, "该用户不是专家，无法审批预约");
            }
            
            Long appointmentId;
            Integer expertId;
            Integer action;
            try {
                appointmentId = Long.parseLong(request.get("appointmentId").toString());
                expertId = user.getExpertId();
                
                Object actionObj = request.get("action");
                if (actionObj instanceof String) {
                    String actionStr = (String) actionObj;
                    if ("同意".equals(actionStr)) {
                        action = 1;
                    } else if ("拒绝".equals(actionStr)) {
                        action = 0;
                    } else {
                        action = Integer.parseInt(actionStr);
                    }
                } else {
                    action = Integer.parseInt(actionObj.toString());
                }
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：预约ID或操作类型格式不正确");
            } catch (Exception e) {
                return Result.error(400, "参数错误：" + e.getMessage());
            }
            
            if (action != 0 && action != 1) {
                return Result.error(400, "参数错误：审批操作类型无效，应为0（拒绝）或1（同意）");
            }
            
            String comment = request.containsKey("comment") ? request.get("comment").toString() : null;
            
            if (!expertRepository.existsById(expertId)) {
                return Result.error(404, "专家不存在");
            }
            
            ExpertAppointment appointment = expertAppointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("预约记录不存在"));
            
            if (!appointment.getExpertId().equals(expertId)) {
                return Result.error(403, "无权审批该预约");
            }
            
            // 验证预约是否可以被审批（状态必须为pending）
            if (!"pending".equals(appointment.getStatus())) {
                return Result.error(400, "该预约已经被处理过，无法重复审批");
            }
            
            if (action == 1) {
                appointment.setStatus("approved");
            } else {
                appointment.setStatus("rejected");
            }
            
            if (comment != null) {
                appointment.setComment(comment);
            }
            
            expertAppointmentRepository.save(appointment);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("appointmentId", appointment.getAppointmentId());
            responseData.put("status", appointment.getStatus());
            
            return Result.success(200, "预约已审批", responseData);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return Result.error(404, e.getMessage());
            }
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/expert-appointment/update-status")
    public Result<Void> updateAppointmentStatus(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            if (!request.containsKey("appointmentId") || request.get("appointmentId") == null) {
                return Result.error(400, "参数错误：预约ID不能为空");
            }
            if (!request.containsKey("status") || request.get("status") == null) {
                return Result.error(400, "参数错误：状态不能为空");
            }
            if (!request.containsKey("meetTime") ) {
                return Result.error(400, "参数错误：时间不能为空");
            }
            
            Long appointmentId;
            try {
                appointmentId = Long.parseLong(request.get("appointmentId").toString());
                
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：预约ID或专家ID格式不正确");
            }
            
            String status = request.get("status").toString();
            String report = request.containsKey("report") ? request.get("report").toString() : null;
            
            LocalDateTime meetTime;
            try {
                String meetTimeStr = request.get("meetTime").toString();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                if (meetTimeStr.length() <= 16) {
                    meetTimeStr += ":00";
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                }
                meetTime = LocalDateTime.parse(meetTimeStr, formatter);
                
                if (LocalDateTime.now().isBefore(meetTime)) {
                    return Result.error(400, "参数错误：当前时间小于预约时间，不能更新状态");
                }
            } catch (Exception e) {
                return Result.error(400, "参数错误：时间格式不正确，请使用yyyy-MM-dd HH:mm格式");
            }
            
            if (!"completed".equals(status) && !"no_show".equals(status)) {
                return Result.error(400, "参数错误：状态值无效，应为completed或no_show");
            }
            ExpertAppointment appointment = expertAppointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("预约记录不存在"));
            if (!"approved".equals(appointment.getStatus())) {
                return Result.error(400, "只有已批准(approved)的预约才能更新为完成或未出席状态");
            }
            
            appointment.setStatus(status);
            
            if (report != null) {
                appointment.setReport(report);
            }
            
            expertAppointmentRepository.save(appointment);
            
            return Result.success(200, "状态已更新为 " + status, null);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return Result.error(404, e.getMessage());
            }
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @GetMapping("/expert-appointment/schedule")
    public Result<List<Map<String, Object>>> getAppointmentSchedule(
             Integer userId,
            @RequestParam(required = false) String date) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "参数错误：用户ID无效");
            }
            
            Integer expertId=userRepository.findByUserId(userId).orElse(null).getExpertId();
            if (userRepository.findByUserId(userId).orElse(null)==null) {
                return Result.error(404, "用户不存在");
            }
            if(expertId==null||expertId<=0){
                return Result.error(404, "用户不是专家");
            }
            
            List<ExpertAppointment> appointments;
            if (date != null && !date.trim().isEmpty()) {
                LocalDate targetDate;
                try {
                    targetDate = LocalDate.parse(date);
                } catch (Exception e) {
                    return Result.error(400, "参数错误：日期格式不正确，请使用YYYY-MM-DD格式");
                }
                appointments = expertAppointmentRepository.findByExpertIdAndDateAndStatusIn(
                        expertId, targetDate, Arrays.asList("approved", "completed", "no_show"));
            } else {
                appointments = expertAppointmentRepository.findByExpertIdAndStatusIn(
                        expertId, Arrays.asList("approved", "completed", "no_show"));
            }
            
            List<Map<String, Object>> appointmentList = new ArrayList<>();
            for (ExpertAppointment appointment : appointments) {
                Map<String, Object> appointmentMap = new HashMap<>();
                
                appointmentMap.put("id", appointment.getAppointmentId());
                appointmentMap.put("date", appointment.getDate().toString());
                appointmentMap.put("startTime", appointment.getStartTime().toString());
                appointmentMap.put("endTime", appointment.getEndTime().toString());
                appointmentMap.put("topic", appointment.getTopic());
                appointmentMap.put("status", appointment.getStatus());
                
                User user = userRepository.findByUserId(Integer.valueOf(appointment.getUserId())).orElse(null);
                String userName = "未知用户";
                if (user != null) {
                    userName = user.getRealName() != null ? user.getRealName() : user.getUsername();
                }
                appointmentMap.put("userName", userName);
                
                appointmentList.add(appointmentMap);
            }
            
            return Result.success(200, "获取预约日程成功", appointmentList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/expert/ask/ai")
    public Result<Object> askExpert(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            if (!request.containsKey("question") || request.get("question") == null) {
                return Result.error(400, "参数错误：问题不能为空");
            }
            if (!request.containsKey("userId") || request.get("userId") == null) {
                return Result.error(400, "参数错误：用户ID不能为空");
            }

            Integer userId;
            try {
                userId = Integer.parseInt(request.get("userId").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：用户ID格式不正确");
            }

            String question = request.get("question").toString();

            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            List <DeepseekRequest.Message> messages = new ArrayList<>();

            messages.add(DeepseekRequest.Message.builder().role("system").content("你是一个农业专家").build());

            messages.add(DeepseekRequest.Message.builder().role("user").content(question).build());

            DeepseekRequest requestBody = DeepseekRequest.builder()
                    .model("deepseek-chat")
                    .messages(messages)
                    .build();
            
            if (deepseekApiKey == null || deepseekApiKey.isBlank()) {
                return Result.error(500, "DeepSeek API Key 未配置，请在 application-local.yml 或环境变量 DEEPSEEK_API_KEY 中设置");
            }

            String apiUrl = "https://api.deepseek.com/v1/chat/completions";
            HttpResponse<String> response = Unirest.post(apiUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + deepseekApiKey)
                .body(gson.toJson(requestBody))
                .asString();
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("answer", response.getBody());
            
            return Result.success(200, "AI回答成功", responseData);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/expert/chat-records")
    public Result<List<Map<String, Object>>> getExpertUserChatRecords(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            if (!request.containsKey("expertId") || request.get("expertId") == null) {
                return Result.error(400, "参数错误：专家ID不能为空");
            }
            if (!request.containsKey("userId") || request.get("userId") == null) {
                return Result.error(400, "参数错误：用户ID不能为空");
            }
            Integer expertId;
            Integer userId;
            try {
                expertId = Integer.parseInt(request.get("expertId").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：专家ID格式不正确");
            }
            try {
                userId = Integer.parseInt(request.get("userId").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：用户ID格式不正确");
            }

            Expert expert = expertRepository.findByExpertId(expertId).orElse(null);
            if (expert == null) {
                return Result.error(404, "专家不存在");
            }
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            List<ExpertUserChatRecord> chatRecords = expertUserChatRecordRepository.findByExpertIdAndUserId(expertId, userId);

            System.out.println("查询到聊天记录数量: " + chatRecords.size());
            System.out.println("聊天记录内容: " + chatRecords);
            List<Map<String, Object>> recordList = new ArrayList<>();
            for (ExpertUserChatRecord record : chatRecords) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("euc_id", record.getEuChatId());
                recordMap.put("expertId", record.getExpertId());
                recordMap.put("userId", record.getUserId());
                recordMap.put("question", record.getQuestion());
                recordMap.put("answer", record.getAnswer());
                recordMap.put("time", record.getSendTime().toString());
                recordList.add(recordMap);
            }
            
            return Result.success(200, "获取聊天记录成功", recordList);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/expert/answer")
    public Result<Map<String, Object>> expertAnswer(@RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return Result.error(400, "请求参数不能为空");
            }
            if (!request.containsKey("euc_id") || request.get("euc_id") == null) {
                return Result.error(400, "参数错误：聊天记录ID不能为空");
            }
            if (!request.containsKey("answer") || request.get("answer") == null) {
                return Result.error(400, "参数错误：回答不能为空");
            }

            Long eucId;
            try {
                eucId = Long.parseLong(request.get("euc_id").toString());
            } catch (NumberFormatException e) {
                return Result.error(400, "参数错误：聊天记录ID格式不正确");
            }
            String answer = request.get("answer").toString();
            ExpertUserChatRecord chatRecord = expertUserChatRecordRepository.findByEuChatId(eucId)
                    .orElseThrow(() -> new RuntimeException("聊天记录不存在"));
            chatRecord.setAnswer(answer);
            chatRecord.setSendTime(LocalDateTime.now());
            expertUserChatRecordRepository.save(chatRecord);

            return Result.success(200, "回答成功");
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
}
