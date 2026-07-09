package com.ltqtest.springbootquickstart.loan.controller;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import com.ltqtest.springbootquickstart.common.Result;
import com.ltqtest.springbootquickstart.loan.entity.FinancialProduct;
import com.ltqtest.springbootquickstart.loan.entity.LoanApplication;
import com.ltqtest.springbootquickstart.loan.entity.ApprovalRecord;
import com.ltqtest.springbootquickstart.loan.entity.RepaymentPlan;
import com.ltqtest.springbootquickstart.loan.entity.RepaymentRecord;
import com.ltqtest.springbootquickstart.loan.entity.LoanStatus;
import com.ltqtest.springbootquickstart.user.entity.User;
import com.ltqtest.springbootquickstart.loan.repository.FinancialProductRepository;
import com.ltqtest.springbootquickstart.loan.repository.LoanApplicationRepository;
import com.ltqtest.springbootquickstart.loan.repository.ApprovalRecordRepository;
import com.ltqtest.springbootquickstart.loan.repository.RepaymentPlanRepository;
import com.ltqtest.springbootquickstart.loan.repository.RepaymentRecordRepository;
import com.ltqtest.springbootquickstart.loan.repository.LoanStatusRepository;
import com.ltqtest.springbootquickstart.user.repository.UserRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/loan")
public class LoanController {

    @Autowired
    private FinancialProductRepository financialProductRepository;
    
    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    
    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RepaymentPlanRepository repaymentPlanRepository;
    
    @Autowired
    private RepaymentRecordRepository repaymentRecordRepository;
    
    @Autowired
    private LoanStatusRepository loanStatusRepository;
    
    @GetMapping("/products")
    public Result<List<FinancialProduct>> getLoanProducts() {
        try {
            List<FinancialProduct> products = financialProductRepository.findAll();
            
            return Result.success(products);
        }catch (Exception e) {
            return Result.error("获取贷款产品列表失败");
        }
    }
    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingLoanApplications() {
        try {
            List<LoanApplication> applications = loanApplicationRepository.findByStatus(1);
            
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (LoanApplication application : applications) {
                Map<String, Object> applicationMap = new HashMap<>();
                applicationMap.put("applicationId", application.getApplicationId());
                applicationMap.put("userId", application.getUserId());
                
                Optional<FinancialProduct> productOpt = financialProductRepository.findById(application.getFpId());
                if (productOpt.isPresent()) {
                    applicationMap.put("productName", productOpt.get().getFpName());
                } else {
                    applicationMap.put("productName", "未知产品");
                }
                
                applicationMap.put("amount", application.getAmount());
                applicationMap.put("term", application.getTerm());
                applicationMap.put("applyTime", application.getApplyTime());
                
                resultList.add(applicationMap);
            }
            
            return Result.success(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取待审批申请列表失败");
        }
    }
     
    
    @PostMapping("/apply")
    public Result<?> submitLoanApplication(
            @RequestParam(value="userId", required = false) Integer userId,
            @RequestParam(value="productName", required = false) String productName,
            @RequestParam(value="amount", required = false) Integer amount,
            @RequestParam(value="term", required = false) Integer term,
            @RequestParam(value = "documents", required = false) MultipartFile documents[]) {
        try {
            if (userId == null || productName == null || productName.isEmpty() || amount == null || term == null ) {
                return Result.error("缺少必填参数");
            }
            
            FinancialProduct product = financialProductRepository.findByFpName(productName);
            if (product == null) {
                return Result.error("指定的贷款产品不存在");
            }
            
            LoanApplication application = new LoanApplication();
            application.setUserId(userId);
            application.setFpId(product.getFpId());
            application.setAmount(amount);
            application.setTerm(term);
            application.setStatus(1); // 待审批（对应loan_status表中的1）
            application.setApplyTime(new java.util.Date());
            
            if (documents != null && documents.length > 0 && !documents[0].isEmpty()) {
                String uploadDir = System.getProperty("user.dir") + "/uploads/loan_documents/" + userId + "/" + System.currentTimeMillis() + "/";
                File directory = new File(uploadDir);
                
                if (!directory.exists()) {
                    boolean created = directory.mkdirs();
                    if (!created) {
                        throw new RuntimeException("无法创建文件存储目录");
                    }
                }
                
                StringBuilder filePaths = new StringBuilder();
                
                for (MultipartFile file : documents) {
                    if (!file.isEmpty()) {
                        try {
                            String originalFilename = file.getOriginalFilename();
                            String fileExtension = "";
                            if (originalFilename != null && originalFilename.contains(".")) {
                                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                            }
                            
                            String uniqueFilename = System.currentTimeMillis() + "_" + userId + "_" + productName + fileExtension;
                            String filePath = uploadDir + uniqueFilename;
                            
                            File dest = new File(filePath);
                            file.transferTo(dest);
                            
                            filePaths.append(filePath).append(",");
                            
                        } catch (IOException e) {
                            throw new RuntimeException("文件保存失败: " + e.getMessage());
                        }
                    }
                }
                
                if (filePaths.length() > 0) {
                    filePaths.setLength(filePaths.length() - 1);
                }
                
                application.setDocuments(filePaths.toString());
            }
            
            LoanApplication savedApplication = loanApplicationRepository.save(application);
            
            Map<String, Integer> responseData = new HashMap<>();
            responseData.put("applicationId", savedApplication.getApplicationId());
            
            return Result.success(responseData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交贷款申请失败");
        }
    }
    
    @GetMapping("/applications")
    public Result<?> getUserLoanApplications(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer status) {
        try {
            if (userId == null) {
                return Result.error("缺少用户ID参数");
            }
            
            List<LoanApplication> applications;
            if (status != null) {
                applications = loanApplicationRepository.findByUserIdAndStatus(userId, status);
            } else {
                applications = loanApplicationRepository.findByUserId(userId);
            }
            
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (LoanApplication application : applications) {
                Map<String, Object> applicationMap = new HashMap<>();
                applicationMap.put("applicationId", application.getApplicationId());
                
                Optional<FinancialProduct> productOpt = financialProductRepository.findById(application.getFpId());
                if (productOpt.isPresent()) {
                    applicationMap.put("productName", productOpt.get().getFpName());
                } else {
                    applicationMap.put("productName", "未知产品");
                }
                
                applicationMap.put("status", application.getStatus());
                applicationMap.put("amount", application.getAmount());
                applicationMap.put("term", application.getTerm());
                
                resultList.add(applicationMap);
            }
            
            return Result.success(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取贷款申请列表失败");
        }
    }
    
    @GetMapping("/applications/{id}")
    public Result<?> getLoanApplicationDetail(@PathVariable Integer id) {
        try {
            Optional<LoanApplication> applicationOpt = loanApplicationRepository.findById(id);
            if (!applicationOpt.isPresent()) {
                return Result.error("贷款申请不存在");
            }
            
            LoanApplication application = applicationOpt.get();
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("applicationId", application.getApplicationId());
            resultMap.put("fpId", application.getFpId());
            resultMap.put("amount", application.getAmount());
            resultMap.put("term", application.getTerm());
            resultMap.put("status", application.getStatus());
            
            List<ApprovalRecord> approvalRecords = approvalRecordRepository.findByApplicationId(id);
            resultMap.put("approvalRecords", approvalRecords);
            
            return Result.success(resultMap);
        } catch (Exception e) {
            return Result.error("获取贷款申请详情失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/products/{id}")
    public Result<?> deleteLoanProduct(@PathVariable Integer id) {
        try {
            if (!financialProductRepository.existsById(id)) {
                return Result.error("产品不存在");
            }
            
            financialProductRepository.deleteById(id);
            
            Result<Object> result = new Result<>();
            result.setCode(200);
            result.setMessage("删除成功");
            return result;
        } catch (Exception e) {
            return Result.error("删除产品失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/products/{id}")
    public Result<FinancialProduct> getLoanProductById(@PathVariable Integer id) {
        try {
            Optional<FinancialProduct> productOpt = financialProductRepository.findById(id);
            
            if (productOpt.isPresent()) {
                return Result.success(productOpt.get());
            } else {
                return Result.error("产品不存在");
            }
        } catch (Exception e) {
            return Result.error("获取产品详情失败：" + e.getMessage());
        }
    }

    @PostMapping("/products")
    public Result<Map<String, Object>> addLoanProduct(@RequestBody Map<String, Object> requestBody) {
        try {
            FinancialProduct product = new FinancialProduct();
            
            product.setFpName((String) requestBody.get("name"));
            product.setFpDescription((String) requestBody.get("description"));
            product.setAnnualRate(requestBody.get("interestRate") != null ? 
                    Float.valueOf(requestBody.get("interestRate").toString()) : null);
            product.setMaxAmount(requestBody.get("maxAmount") != null ? 
                    Integer.valueOf(requestBody.get("maxAmount").toString()) : null);
            product.setMinAmount(requestBody.get("minAmount") != null ? 
                    Integer.valueOf(requestBody.get("minAmount").toString()) : null);
            
            String category = (String) requestBody.get("category");
            product.setTags(category != null ? category : "");
        
            Integer term = null;
            Object termObj = requestBody.get("term");
            if (termObj != null) {
                try {
                    term = Integer.valueOf(termObj.toString());
                    product.setTerm(term);
                } catch (NumberFormatException e) {
                    return Result.error(400, "贷款期限格式错误，请输入数字");
                }
            }
            
            product.setFpManagerName("暂无名字");
            product.setFpManagerPhone("暂无电话");
            product.setFpManagerEmail("暂无邮件");
            
            FinancialProduct savedProduct = financialProductRepository.save(product);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("productId", savedProduct.getFpId());
            
            return Result.success(responseData);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    @PostMapping("/modifyproducts")
    public Result<?> modifyLoanProduct(@RequestBody Map<String, Object> requestBody) {
        try {
            Integer productId = null;
            Object productIdObj = requestBody.get("fpId");
            if (productIdObj != null) {
                try {
                    productId = Integer.valueOf(productIdObj.toString());
                } catch (NumberFormatException e) {
                    return Result.error(400, "产品ID格式错误，请输入数字");
                }
            }
            
            if (productId == null) {
                return Result.error(400, "缺少产品ID参数");
            }
            
            Optional<FinancialProduct> productOpt = financialProductRepository.findById(productId);
            if (!productOpt.isPresent()) {
                return Result.error(404, "产品不存在");
            }
            
            FinancialProduct product = productOpt.get();
            
            if (requestBody.containsKey("name")) {
                product.setFpName((String) requestBody.get("name"));
            }
            
            if (requestBody.containsKey("category")) {
                product.setTags((String) requestBody.get("category"));
            }
            
            if (requestBody.containsKey("maxAmount")) {
                Object maxAmountObj = requestBody.get("maxAmount");
                if (maxAmountObj != null) {
                    try {
                        product.setMaxAmount(Integer.valueOf(maxAmountObj.toString()));
                    } catch (NumberFormatException e) {
                        return Result.error(400, "最大额度格式错误，请输入数字");
                    }
                }
            }
            
            if (requestBody.containsKey("minAmount")) {
                Object minAmountObj = requestBody.get("minAmount");
                if (minAmountObj != null) {
                    try {
                        product.setMinAmount(Integer.valueOf(minAmountObj.toString()));
                    } catch (NumberFormatException e) {
                        return Result.error(400, "最小额度格式错误，请输入数字");
                    }
                }
            }
            
            if (requestBody.containsKey("interestRate")) {
                Object interestRateObj = requestBody.get("interestRate");
                if (interestRateObj != null) {
                    try {
                        product.setAnnualRate(Float.valueOf(interestRateObj.toString()));
                    } catch (NumberFormatException e) {
                        return Result.error(400, "利率格式错误，请输入数字");
                    }
                }
            }
            
            if (requestBody.containsKey("term")) {
                Object termObj = requestBody.get("term");
                if (termObj != null) {
                    try {
                        product.setTerm(Integer.valueOf(termObj.toString()));
                    } catch (NumberFormatException e) {
                        return Result.error(400, "贷款期限格式错误，请输入数字");
                    }
                }
            }
            
            if (requestBody.containsKey("description")) {
                product.setFpDescription((String) requestBody.get("description"));
            }
            
            financialProductRepository.save(product);
            
            return Result.success(200, "修改成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "修改产品失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/approve")
    public Result<?> approveLoanApplication(@RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey("applicationId") || !requestBody.containsKey("userId") || !requestBody.containsKey("decision") || !requestBody.containsKey("remark")) {
                return Result.error("缺少必填参数");
            }
            Integer applicationId = Integer.valueOf(requestBody.get("applicationId").toString());
            Integer userId = Integer.valueOf(requestBody.get("userId").toString());
            Integer decision = Integer.valueOf(requestBody.get("decision").toString());
            String remark = (String) requestBody.get("remark");
            
            if (!userRepository.existsById(userId)) {
                return Result.error("审批人不存在");
            }
            User user = userRepository.findByUserId(userId).get();
            if(user.getApproverId()==0){
                return Result.error("用户不是审批人");
            }
            
            Optional<LoanApplication> applicationOpt = loanApplicationRepository.findById(applicationId);
            if (!applicationOpt.isPresent()) {
                return Result.error("贷款申请不存在");
            }
            
            LoanApplication application = applicationOpt.get();
            
            if (application.getStatus() != 1 && application.getStatus() != 3) {
                return Result.error("该申请已完成审批，无法重复操作");
            }
            
            if (decision == 1) {
                application.setStatus(3); // 已审核（对应loan_status表中的3）
                loanApplicationRepository.save(application);
                
                generateRepaymentPlan(application);
            } else if (decision == 0) {
                application.setStatus(2); // 已打回（对应loan_status表中的2）
                loanApplicationRepository.save(application);
            } else {
                return Result.error("无效的审批决定，decision只能是0或1");
            }
            
            ApprovalRecord approvalRecord = new ApprovalRecord();
            approvalRecord.setApplicationId(applicationId);
            approvalRecord.setApproverId(userRepository.findByUserId(userId).get().getApproverId());
            approvalRecord.setDecision(decision == 1);
            approvalRecord.setOpinion(remark);
            approvalRecord.setApprovalTime(new java.util.Date());
            
            approvalRecordRepository.save(approvalRecord);
            
            return Result.success(200,null, "审批完成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "审批操作失败: " + e.getMessage());
        }
    }
    
    private void generateRepaymentPlan(LoanApplication application) {
        try {
            Integer applicationId = application.getApplicationId();
            Integer amount = application.getAmount();
            Integer term = application.getTerm();

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            Integer fpId = application.getFpId();
            FinancialProduct financialProduct = financialProductRepository.findById(fpId).get();
            double annualRate = financialProduct.getAnnualRate();
            double totalAmount=amount+amount*annualRate*0.01*term/12;
                RepaymentPlan repaymentPlan = new RepaymentPlan();
                repaymentPlan.setApplicationId(applicationId);
                calendar.add(java.util.Calendar.MONTH, term);
                repaymentPlan.setDueDate(calendar.getTime());
                repaymentPlan.setRemainingAmount(totalAmount);
                repaymentPlan.setStatus("未还");
                
                repaymentPlanRepository.save(repaymentPlan);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @GetMapping("/approvals")
    public Result<List<Map<String, Object>>> getApprovalHistory(@RequestParam Integer userId) {
        try {
            if (userId == null) {
                return Result.error(400, "缺少必填参数userId");
            }
            Integer approverId = userRepository.findByUserId(userId).get().getApproverId();
            List<ApprovalRecord> approvalRecords = approvalRecordRepository.findByApproverId(approverId);
            
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (ApprovalRecord record : approvalRecords) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("approverId", record.getApproverId());
                recordMap.put("decision", record.getDecision() ? "通过" : "拒绝");
                recordMap.put("remark", record.getOpinion());

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               if (record.getApprovalTime() != null) {
                recordMap.put("date", dateFormat.format(record.getApprovalTime()));
                 } else { 
                recordMap.put("date", null);
                }
                resultList.add(recordMap);
            }
            
            return Result.success(200, "成功", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取审批历史失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/repayment-plan")
    public Result<List<Map<String, Object>>> getRepaymentPlan(@RequestParam Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "无效的用户ID");
            }
            
            List<LoanApplication> applications = loanApplicationRepository.findByUserId(userId);
            
            if (applications == null || applications.isEmpty()) {
                return Result.error(404, "未找到该用户的贷款申请记录");
            }
            
            List<Map<String, Object>> resultList = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            for (LoanApplication application : applications) {
                Integer applicationId = application.getApplicationId();
                
                List<RepaymentPlan> repaymentPlans = repaymentPlanRepository.findByApplicationId(applicationId);
                
                checkAndUpdateOverdueStatus(applicationId, repaymentPlans);
                
                repaymentPlans = repaymentPlanRepository.findByApplicationId(applicationId);
                
                for (RepaymentPlan plan : repaymentPlans) {
                    Map<String, Object> planMap = new HashMap<>();
                    planMap.put("applicationId", applicationId);
                    planMap.put("dueDate", dateFormat.format(plan.getDueDate()));
                    planMap.put("RemainingAmout", plan.getRemainingAmount());
                    if (plan.getRemainingAmount() > 0) {
                        if (plan.getDueDate().before(new Date())) {
                            planMap.put("status", "已逾期");
                        } else {
                            planMap.put("status", "未还清");
                        }
                    } else {
                        planMap.put("status", "已还款");
                    }
                    resultList.add(planMap);
                }
            }
            
            return Result.success(200, "成功", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "系统异常");
        }
    }

    @PostMapping("/repay")
    public Result<?> repayLoan(@RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey("applicationId") ||
                !requestBody.containsKey("amount") ||
                !requestBody.containsKey("payDate")) {
                return Result.error(400, "参数错误");
            }
            Integer applicationId = Integer.parseInt(requestBody.get("applicationId").toString());
            Float amount = Float.parseFloat(requestBody.get("amount").toString());
            Date payDate = new SimpleDateFormat("yyyy-MM-dd").parse(requestBody.get("payDate").toString());    

            List<RepaymentPlan> repaymentPlans = repaymentPlanRepository.findByApplicationId(applicationId);
            if (repaymentPlans == null || repaymentPlans.isEmpty()) {
                return Result.error(404, "未找到还款计划");
            }
            
            double totalRemainingToPay = repaymentPlans.stream()
                    .mapToDouble(RepaymentPlan::getRemainingAmount)
                    .sum();
            
            double remainingAmount = amount;
            
            if (amount >= totalRemainingToPay) {
                for (RepaymentPlan plan : repaymentPlans) {
                    if (plan.getRemainingAmount() > 0) {
                        plan.setRemainingAmount(0f);
                        plan.setStatus("已还款");
                        repaymentPlanRepository.save(plan);
                    }
                }
                remainingAmount = 0;
            } else {
                for (RepaymentPlan plan : repaymentPlans) {
                    if (plan.getRemainingAmount() > 0 && remainingAmount > 0) {
                        if (plan.getRemainingAmount() <= remainingAmount) {
                            remainingAmount -= plan.getRemainingAmount();
                            plan.setRemainingAmount(0f);
                            plan.setStatus("已还款");
                        } else {
                            plan.setRemainingAmount(plan.getRemainingAmount() - remainingAmount);
                            remainingAmount = 0f;
                        }
                        repaymentPlanRepository.save(plan);
                    }
                    if (remainingAmount <= 0) {
                        break;
                    }
                }
            }

            Optional<LoanApplication> loanApplicationOpt = loanApplicationRepository.findById(applicationId);
            if (!loanApplicationOpt.isPresent()) {
                return Result.error(404, "贷款申请不存在");
            }
            Integer userId = loanApplicationOpt.get().getUserId();

            LoanApplication loanApplication = loanApplicationOpt.get();
            if (amount >= totalRemainingToPay) {
                loanApplication.setStatus(5);
            } else {
                boolean isOverdue = false;
                List<RepaymentPlan> updatedPlans = repaymentPlanRepository.findByApplicationId(applicationId);
                for (RepaymentPlan plan : updatedPlans) {
                    if (plan.getRemainingAmount() > 0 && plan.getDueDate().before(new Date())) {
                        isOverdue = true;
                        break;
                    }
                }
                loanApplication.setStatus(isOverdue ? 6 : 4);
            }
            loanApplicationRepository.save(loanApplication);

            RepaymentRecord record = new RepaymentRecord();
            record.setApplicationId(applicationId);
            record.setUserId(userId);
            if (amount > totalRemainingToPay) {
                record.setAmount((float) totalRemainingToPay);
            } else {
                record.setAmount(amount);
            }
            record.setPayDate(payDate);
            if (amount >= totalRemainingToPay) {
                record.setStatus("已还款");
            } else {
                boolean isOverdue = false;
                List<RepaymentPlan> updatedPlans = repaymentPlanRepository.findByApplicationId(applicationId);
                for (RepaymentPlan plan : updatedPlans) {
                    if (plan.getRemainingAmount() > 0 && plan.getDueDate().before(new Date())) {
                        isOverdue = true;
                        break;
                    }
                }
                record.setStatus(isOverdue ? "已逾期" : "未还清");
            }
            repaymentRecordRepository.save(record);

            List<RepaymentPlan> updatedPlans = repaymentPlanRepository.findByApplicationId(applicationId);
            double planRemaining = 0;
            if (updatedPlans != null) {
                planRemaining = updatedPlans.stream()
                        .mapToDouble(RepaymentPlan::getRemainingAmount)
                        .sum();
            }
            Float totalRemaining = (float) (planRemaining + remainingAmount);

            Map<String, Object> data = new HashMap<>();
            data.put("remainingAmount", totalRemaining);

            return Result.success(200, "还款成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "还款失败：" + e.getMessage());
        }
    }

    @GetMapping("/repayments")
    public Result<?> getRepaymentHistory(@RequestParam Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return Result.error(400, "用户ID参数错误");
            }

            List<LoanApplication> loanApplications = loanApplicationRepository.findByUserId(userId);
            
            for (LoanApplication application : loanApplications) {
                List<RepaymentPlan> plans = repaymentPlanRepository.findByApplicationId(application.getApplicationId());
                checkAndUpdateOverdueStatus(application.getApplicationId(), plans);
            }

            List<RepaymentRecord> repaymentRecords = repaymentRecordRepository.findByUserId(userId);
            List<Map<String, Object>> resultList = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (RepaymentRecord record : repaymentRecords) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("applicationId", record.getApplicationId());
                recordMap.put("amount", record.getAmount());
                recordMap.put("payDate", dateFormat.format(record.getPayDate()));
                resultList.add(recordMap);
            }

            return Result.success(200, "成功", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "查询还款历史失败：" + e.getMessage());
        }
    }
    
    private void checkAndUpdateOverdueStatus(Integer applicationId, List<RepaymentPlan> repaymentPlans) {
        try {
            Optional<LoanApplication> loanApplicationOpt = loanApplicationRepository.findById(applicationId);
            if (!loanApplicationOpt.isPresent()) {
                return;
            }
            LoanApplication loanApplication = loanApplicationOpt.get();
            
            boolean hasOverdue = false;
            for (RepaymentPlan plan : repaymentPlans) {
                if (plan.getRemainingAmount() > 0 && plan.getDueDate().before(new Date())) {
                    hasOverdue = true;
                    plan.setStatus("已逾期");
                    repaymentPlanRepository.save(plan);
                }
            }
            
            if (hasOverdue && loanApplication.getStatus() != 5) {
                loanApplication.setStatus(6);
                loanApplicationRepository.save(loanApplication);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @GetMapping("/status")
    public Result<List<LoanStatus>> getAllLoanStatus() {
        try {
            List<LoanStatus> statusList = loanStatusRepository.findAll();
            return Result.success(200, "成功", statusList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "查询贷款状态失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/status")
    public Result<Map<String, Object>> addLoanStatus(@RequestBody Map<String, Object> requestBody) {
        try {
            Integer statusCode = (Integer) requestBody.get("status_code");
            String statusName = (String) requestBody.get("status_name");
            String description = (String) requestBody.get("description");
            
            if (statusCode == null || statusName == null || statusName.isEmpty()) {
                return Result.error(400, "状态码和状态名称为必填项");
            }
            
            if (loanStatusRepository.existsByStatusCode(statusCode)) {
                return Result.error(400, "该状态码已存在");
            }
            
            LoanStatus loanStatus = new LoanStatus();
            loanStatus.setStatusCode(statusCode);
            loanStatus.setStatusName(statusName);
            loanStatus.setDescription(description);
            
            LoanStatus savedStatus = loanStatusRepository.save(loanStatus);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status_id", savedStatus.getStatusId());
            return Result.success(200, "新增状态成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "新增贷款状态失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/status/{id}")
    public Result<?> updateLoanStatus(@PathVariable Integer id, @RequestBody Map<String, Object> requestBody) {
        try {
            Optional<LoanStatus> optionalStatus = loanStatusRepository.findById(id);
            if (!optionalStatus.isPresent()) {
                return Result.error(404, "贷款状态不存在");
            }
            
            LoanStatus loanStatus = optionalStatus.get();
            if (requestBody.containsKey("status_name")) {
                String statusName = (String) requestBody.get("status_name");
                loanStatus.setStatusName(statusName);
            }
            if (requestBody.containsKey("description")) {
                String description = (String) requestBody.get("description");
                loanStatus.setDescription(description);
            }
            
            loanStatusRepository.save(loanStatus);
            
            return Result.success(200, "更新成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新贷款状态失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/status/{id}")
    public Result<?> deleteLoanStatus(@PathVariable Integer id) {
        try {
            if (!loanStatusRepository.existsById(id)) {
                return Result.error(404, "贷款状态不存在");
            }
            
            loanStatusRepository.deleteById(id);
            
            return Result.success(200, "删除成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除贷款状态失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/searchproduct")
    public Result<?> searchProduct(@RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey("q") || requestBody.get("q") == null || requestBody.get("q").toString().trim().isEmpty()) {
                return Result.error(400, "搜索关键词不能为空");
            }
            
            List<FinancialProduct> allProducts = financialProductRepository.findAll();
            List<FinancialProduct> matchedProducts = new ArrayList<>();
            
            String searchKeyword = requestBody.get("q").toString().toLowerCase().trim();
            for (FinancialProduct product : allProducts) {
                if (product.getFpName().toLowerCase().contains(searchKeyword)) {
                    matchedProducts.add(product);
                }
            }
            
            return Result.success(200, "成功", matchedProducts);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "搜索产品失败：" + e.getMessage());
        }
    }
}
