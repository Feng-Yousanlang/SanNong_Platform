#!/usr/bin/env python3
"""按业务域重组 Spring Boot 后端包结构。"""
from __future__ import annotations

import re
import shutil
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent / "src" / "main" / "java" / "com" / "ltqtest" / "springbootquickstart"

MOVES: list[tuple[str, str, str, str | None]] = [
    ("controller/AuthController.java", "auth/controller/AuthController.java", "com.ltqtest.springbootquickstart.auth.controller", None),

    ("controller/UserController.java", "user/controller/UserController.java", "com.ltqtest.springbootquickstart.user.controller", None),
    ("service/UserService.java", "user/service/UserService.java", "com.ltqtest.springbootquickstart.user.service", None),
    ("entity/User.java", "user/entity/User.java", "com.ltqtest.springbootquickstart.user.entity", None),
    ("entity/UserAddress.java", "user/entity/UserAddress.java", "com.ltqtest.springbootquickstart.user.entity", None),
    ("repository/UserRepository.java", "user/repository/UserRepository.java", "com.ltqtest.springbootquickstart.user.repository", None),
    ("repository/UserAddressRepository.java", "user/repository/UserAddressRepository.java", "com.ltqtest.springbootquickstart.user.repository", None),

    ("controller/AgricultureController.java", "product/controller/ProductController.java", "com.ltqtest.springbootquickstart.product.controller", "ProductController"),
    ("entity/Product.java", "product/entity/Product.java", "com.ltqtest.springbootquickstart.product.entity", None),
    ("entity/ShoppingCart.java", "product/entity/ShoppingCart.java", "com.ltqtest.springbootquickstart.product.entity", None),
    ("entity/Purchase.java", "product/entity/Purchase.java", "com.ltqtest.springbootquickstart.product.entity", None),
    ("repository/ProductRepository.java", "product/repository/ProductRepository.java", "com.ltqtest.springbootquickstart.product.repository", None),
    ("repository/ShoppingCartRepository.java", "product/repository/ShoppingCartRepository.java", "com.ltqtest.springbootquickstart.product.repository", None),
    ("repository/PurchaseRepository.java", "product/repository/PurchaseRepository.java", "com.ltqtest.springbootquickstart.product.repository", None),

    ("controller/ProductCommentController.java", "comment/controller/ProductCommentController.java", "com.ltqtest.springbootquickstart.comment.controller", None),
    ("entity/ProductComment.java", "comment/entity/ProductComment.java", "com.ltqtest.springbootquickstart.comment.entity", None),
    ("repository/ProductCommentRepository.java", "comment/repository/ProductCommentRepository.java", "com.ltqtest.springbootquickstart.comment.repository", None),

    ("controller/LoanController.java", "loan/controller/LoanController.java", "com.ltqtest.springbootquickstart.loan.controller", None),
    ("entity/FinancialProduct.java", "loan/entity/FinancialProduct.java", "com.ltqtest.springbootquickstart.loan.entity", None),
    ("entity/LoanApplication.java", "loan/entity/LoanApplication.java", "com.ltqtest.springbootquickstart.loan.entity", None),
    ("entity/ApprovalRecord.java", "loan/entity/ApprovalRecord.java", "com.ltqtest.springbootquickstart.loan.entity", None),
    ("entity/RepaymentPlan.java", "loan/entity/RepaymentPlan.java", "com.ltqtest.springbootquickstart.loan.entity", None),
    ("entity/RepaymentRecord.java", "loan/entity/RepaymentRecord.java", "com.ltqtest.springbootquickstart.loan.entity", None),
    ("entity/LoanStatus.java", "loan/entity/LoanStatus.java", "com.ltqtest.springbootquickstart.loan.entity", None),
    ("entity/Approver.java", "loan/entity/Approver.java", "com.ltqtest.springbootquickstart.loan.entity", None),
    ("repository/FinancialProductRepository.java", "loan/repository/FinancialProductRepository.java", "com.ltqtest.springbootquickstart.loan.repository", None),
    ("repository/LoanApplicationRepository.java", "loan/repository/LoanApplicationRepository.java", "com.ltqtest.springbootquickstart.loan.repository", None),
    ("repository/ApprovalRecordRepository.java", "loan/repository/ApprovalRecordRepository.java", "com.ltqtest.springbootquickstart.loan.repository", None),
    ("repository/RepaymentPlanRepository.java", "loan/repository/RepaymentPlanRepository.java", "com.ltqtest.springbootquickstart.loan.repository", None),
    ("repository/RepaymentRecordRepository.java", "loan/repository/RepaymentRecordRepository.java", "com.ltqtest.springbootquickstart.loan.repository", None),
    ("repository/LoanStatusRepository.java", "loan/repository/LoanStatusRepository.java", "com.ltqtest.springbootquickstart.loan.repository", None),
    ("repository/ApproverRepository.java", "loan/repository/ApproverRepository.java", "com.ltqtest.springbootquickstart.loan.repository", None),

    ("controller/ExpertController.java", "expert/controller/ExpertController.java", "com.ltqtest.springbootquickstart.expert.controller", None),
    ("entity/Expert.java", "expert/entity/Expert.java", "com.ltqtest.springbootquickstart.expert.entity", None),
    ("entity/ExpertAppointment.java", "expert/entity/ExpertAppointment.java", "com.ltqtest.springbootquickstart.expert.entity", None),
    ("entity/ExpertUserChatRecord.java", "expert/entity/ExpertUserChatRecord.java", "com.ltqtest.springbootquickstart.expert.entity", None),
    ("repository/ExpertRepository.java", "expert/repository/ExpertRepository.java", "com.ltqtest.springbootquickstart.expert.repository", None),
    ("repository/ExpertAppointmentRepository.java", "expert/repository/ExpertAppointmentRepository.java", "com.ltqtest.springbootquickstart.expert.repository", None),
    ("repository/ExpertUserChatRecordRepository.java", "expert/repository/ExpertUserChatRecordRepository.java", "com.ltqtest.springbootquickstart.expert.repository", None),

    ("controller/KnowledgeController.java", "knowledge/controller/KnowledgeController.java", "com.ltqtest.springbootquickstart.knowledge.controller", None),
    ("entity/AgricultureKnowledge.java", "knowledge/entity/AgricultureKnowledge.java", "com.ltqtest.springbootquickstart.knowledge.entity", None),
    ("repository/AgricultureKnowledgeRepository.java", "knowledge/repository/AgricultureKnowledgeRepository.java", "com.ltqtest.springbootquickstart.knowledge.repository", None),

    ("controller/BuyRequestController.java", "buyrequest/controller/BuyRequestController.java", "com.ltqtest.springbootquickstart.buyrequest.controller", None),
    ("entity/BuyRequest.java", "buyrequest/entity/BuyRequest.java", "com.ltqtest.springbootquickstart.buyrequest.entity", None),
    ("repository/BuyRequestRepository.java", "buyrequest/repository/BuyRequestRepository.java", "com.ltqtest.springbootquickstart.buyrequest.repository", None),

    ("controller/HomeController.java", "home/controller/HomeController.java", "com.ltqtest.springbootquickstart.home.controller", None),
    ("entity/News.java", "home/entity/News.java", "com.ltqtest.springbootquickstart.home.entity", None),
    ("entity/AgricultureProducer.java", "home/entity/AgricultureProducer.java", "com.ltqtest.springbootquickstart.home.entity", None),
    ("repository/NewsRepository.java", "home/repository/NewsRepository.java", "com.ltqtest.springbootquickstart.home.repository", None),

    ("util/PayUtil.java", "integration/payment/PayUtil.java", "com.ltqtest.springbootquickstart.integration.payment", None),
    ("entity/DeepseekRequest.java", "integration/deepseek/dto/DeepseekRequest.java", "com.ltqtest.springbootquickstart.integration.deepseek.dto", None),
]

REPLACEMENTS: dict[str, str] = {
    "com.ltqtest.springbootquickstart.service.UserService": "com.ltqtest.springbootquickstart.user.service.UserService",
    "com.ltqtest.springbootquickstart.entity.UserAddress": "com.ltqtest.springbootquickstart.user.entity.UserAddress",
    "com.ltqtest.springbootquickstart.entity.User": "com.ltqtest.springbootquickstart.user.entity.User",
    "com.ltqtest.springbootquickstart.repository.UserAddressRepository": "com.ltqtest.springbootquickstart.user.repository.UserAddressRepository",
    "com.ltqtest.springbootquickstart.repository.UserRepository": "com.ltqtest.springbootquickstart.user.repository.UserRepository",
    "com.ltqtest.springbootquickstart.entity.ShoppingCart": "com.ltqtest.springbootquickstart.product.entity.ShoppingCart",
    "com.ltqtest.springbootquickstart.entity.Purchase": "com.ltqtest.springbootquickstart.product.entity.Purchase",
    "com.ltqtest.springbootquickstart.entity.ProductComment": "com.ltqtest.springbootquickstart.comment.entity.ProductComment",
    "com.ltqtest.springbootquickstart.entity.Product": "com.ltqtest.springbootquickstart.product.entity.Product",
    "com.ltqtest.springbootquickstart.repository.ShoppingCartRepository": "com.ltqtest.springbootquickstart.product.repository.ShoppingCartRepository",
    "com.ltqtest.springbootquickstart.repository.PurchaseRepository": "com.ltqtest.springbootquickstart.product.repository.PurchaseRepository",
    "com.ltqtest.springbootquickstart.repository.ProductCommentRepository": "com.ltqtest.springbootquickstart.comment.repository.ProductCommentRepository",
    "com.ltqtest.springbootquickstart.repository.ProductRepository": "com.ltqtest.springbootquickstart.product.repository.ProductRepository",
    "com.ltqtest.springbootquickstart.entity.FinancialProduct": "com.ltqtest.springbootquickstart.loan.entity.FinancialProduct",
    "com.ltqtest.springbootquickstart.entity.LoanApplication": "com.ltqtest.springbootquickstart.loan.entity.LoanApplication",
    "com.ltqtest.springbootquickstart.entity.ApprovalRecord": "com.ltqtest.springbootquickstart.loan.entity.ApprovalRecord",
    "com.ltqtest.springbootquickstart.entity.RepaymentPlan": "com.ltqtest.springbootquickstart.loan.entity.RepaymentPlan",
    "com.ltqtest.springbootquickstart.entity.RepaymentRecord": "com.ltqtest.springbootquickstart.loan.entity.RepaymentRecord",
    "com.ltqtest.springbootquickstart.entity.LoanStatus": "com.ltqtest.springbootquickstart.loan.entity.LoanStatus",
    "com.ltqtest.springbootquickstart.entity.Approver": "com.ltqtest.springbootquickstart.loan.entity.Approver",
    "com.ltqtest.springbootquickstart.repository.FinancialProductRepository": "com.ltqtest.springbootquickstart.loan.repository.FinancialProductRepository",
    "com.ltqtest.springbootquickstart.repository.LoanApplicationRepository": "com.ltqtest.springbootquickstart.loan.repository.LoanApplicationRepository",
    "com.ltqtest.springbootquickstart.repository.ApprovalRecordRepository": "com.ltqtest.springbootquickstart.loan.repository.ApprovalRecordRepository",
    "com.ltqtest.springbootquickstart.repository.RepaymentPlanRepository": "com.ltqtest.springbootquickstart.loan.repository.RepaymentPlanRepository",
    "com.ltqtest.springbootquickstart.repository.RepaymentRecordRepository": "com.ltqtest.springbootquickstart.loan.repository.RepaymentRecordRepository",
    "com.ltqtest.springbootquickstart.repository.LoanStatusRepository": "com.ltqtest.springbootquickstart.loan.repository.LoanStatusRepository",
    "com.ltqtest.springbootquickstart.repository.ApproverRepository": "com.ltqtest.springbootquickstart.loan.repository.ApproverRepository",
    "com.ltqtest.springbootquickstart.entity.ExpertUserChatRecord": "com.ltqtest.springbootquickstart.expert.entity.ExpertUserChatRecord",
    "com.ltqtest.springbootquickstart.entity.ExpertAppointment": "com.ltqtest.springbootquickstart.expert.entity.ExpertAppointment",
    "com.ltqtest.springbootquickstart.entity.Expert": "com.ltqtest.springbootquickstart.expert.entity.Expert",
    "com.ltqtest.springbootquickstart.repository.ExpertUserChatRecordRepository": "com.ltqtest.springbootquickstart.expert.repository.ExpertUserChatRecordRepository",
    "com.ltqtest.springbootquickstart.repository.ExpertAppointmentRepository": "com.ltqtest.springbootquickstart.expert.repository.ExpertAppointmentRepository",
    "com.ltqtest.springbootquickstart.repository.ExpertRepository": "com.ltqtest.springbootquickstart.expert.repository.ExpertRepository",
    "com.ltqtest.springbootquickstart.entity.AgricultureKnowledge": "com.ltqtest.springbootquickstart.knowledge.entity.AgricultureKnowledge",
    "com.ltqtest.springbootquickstart.repository.AgricultureKnowledgeRepository": "com.ltqtest.springbootquickstart.knowledge.repository.AgricultureKnowledgeRepository",
    "com.ltqtest.springbootquickstart.entity.BuyRequest": "com.ltqtest.springbootquickstart.buyrequest.entity.BuyRequest",
    "com.ltqtest.springbootquickstart.repository.BuyRequestRepository": "com.ltqtest.springbootquickstart.buyrequest.repository.BuyRequestRepository",
    "com.ltqtest.springbootquickstart.entity.AgricultureProducer": "com.ltqtest.springbootquickstart.home.entity.AgricultureProducer",
    "com.ltqtest.springbootquickstart.entity.News": "com.ltqtest.springbootquickstart.home.entity.News",
    "com.ltqtest.springbootquickstart.repository.NewsRepository": "com.ltqtest.springbootquickstart.home.repository.NewsRepository",
    "com.ltqtest.springbootquickstart.util.PayUtil": "com.ltqtest.springbootquickstart.integration.payment.PayUtil",
    "com.ltqtest.springbootquickstart.entity.DeepseekRequest": "com.ltqtest.springbootquickstart.integration.deepseek.dto.DeepseekRequest",
}

DEAD_FILES = [
    "entity/AgricultureProduct.java",
    "repository/AgricultureRepository.java",
]

HOME_IMPORTS = """import com.ltqtest.springbootquickstart.common.Result;
import com.ltqtest.springbootquickstart.home.entity.News;
import com.ltqtest.springbootquickstart.loan.entity.FinancialProduct;
import com.ltqtest.springbootquickstart.home.repository.NewsRepository;
import com.ltqtest.springbootquickstart.loan.repository.FinancialProductRepository;
"""


def update_package(content: str, package: str) -> str:
    return re.sub(r"^package\s+[^;]+;", f"package {package};", content, count=1, flags=re.MULTILINE)


def main() -> None:
    for src_rel, dest_rel, package, rename_class in MOVES:
        src = BASE / src_rel.replace("/", "\\") if False else BASE / Path(src_rel)
        dest = BASE / Path(dest_rel)
        if not src.exists():
            print(f"SKIP missing: {src_rel}")
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(src), str(dest))
        content = dest.read_text(encoding="utf-8")
        content = update_package(content, package)
        if rename_class:
            content = content.replace("AgricultureController", rename_class)
        dest.write_text(content, encoding="utf-8")
        print(f"MOVED {src_rel} -> {dest_rel}")

    java_root = BASE.parent.parent.parent.parent  # src/main/java
    for java_file in java_root.rglob("*.java"):
        content = java_file.read_text(encoding="utf-8")
        original = content
        for old, new in sorted(REPLACEMENTS.items(), key=lambda item: len(item[0]), reverse=True):
            content = content.replace(old, new)
        if content != original:
            java_file.write_text(content, encoding="utf-8")

    home_controller = BASE / "home" / "controller" / "HomeController.java"
    if home_controller.exists():
        content = home_controller.read_text(encoding="utf-8")
        content = re.sub(
            r"import com\.ltqtest\.springbootquickstart\.common\.Result;\s*"
            r"import com\.ltqtest\.springbootquickstart\.entity\.\*;\s*"
            r"import com\.ltqtest\.springbootquickstart\.repository\.\*;",
            HOME_IMPORTS.strip(),
            content,
            count=1,
        )
        home_controller.write_text(content, encoding="utf-8")

    for rel in DEAD_FILES:
        path = BASE / Path(rel)
        if path.exists():
            path.unlink()
            print(f"DELETED {rel}")

    for folder in ("controller", "service", "entity", "repository", "util"):
        folder_path = BASE / folder
        if folder_path.exists() and not any(folder_path.rglob("*")):
            folder_path.rmdir()
            print(f"REMOVED empty dir: {folder}")

    print("Done.")


if __name__ == "__main__":
    main()
