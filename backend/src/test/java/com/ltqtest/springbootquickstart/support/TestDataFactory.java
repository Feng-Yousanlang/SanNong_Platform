package com.ltqtest.springbootquickstart.support;

import com.ltqtest.springbootquickstart.buyrequest.entity.BuyRequest;
import com.ltqtest.springbootquickstart.home.entity.News;
import com.ltqtest.springbootquickstart.knowledge.entity.AgricultureKnowledge;
import com.ltqtest.springbootquickstart.loan.entity.FinancialProduct;
import com.ltqtest.springbootquickstart.user.entity.User;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public final class TestDataFactory {

    private static final AtomicInteger USER_SEQ = new AtomicInteger(1);

    private TestDataFactory() {
    }

    public static User newFarmer(String username) {
        User user = baseUser(username);
        user.setRoleType(1);
        user.setRealName("测试农户");
        return user;
    }

    public static User newBuyer(String username) {
        User user = baseUser(username);
        user.setRoleType(2);
        user.setRealName("测试买家");
        return user;
    }

    private static User baseUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setStatus(1);
        user.setLoginStatus(0);
        user.setExpertId(0);
        user.setApproverId(0);
        return user;
    }

    public static String uniqueUsername(String prefix) {
        return prefix + USER_SEQ.getAndIncrement();
    }

    public static BuyRequest newBuyRequest(String title, String content) {
        BuyRequest request = new BuyRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setContact("13800000000");
        request.setCreateTime(new Date());
        return request;
    }

    public static News newNews(String title) {
        News news = new News();
        news.setTitle(title);
        news.setImgUrl("https://example.com/news.jpg");
        news.setNewsUrl("https://example.com/article");
        return news;
    }

    public static AgricultureKnowledge newKnowledge(String title) {
        AgricultureKnowledge knowledge = new AgricultureKnowledge();
        knowledge.setTitle(title);
        knowledge.setSource("单元测试");
        knowledge.setUrl("https://example.com/knowledge");
        knowledge.setPublish(new Date());
        return knowledge;
    }

    public static FinancialProduct newFinancialProduct(String name) {
        FinancialProduct product = new FinancialProduct();
        product.setFpName(name);
        product.setFpDescription("测试贷款产品");
        product.setAnnualRate(4.5f);
        product.setTags("低息,农户专享");
        product.setFpManagerName("张经理");
        product.setFpManagerPhone("13800000001");
        product.setFpManagerEmail("bank@test.com");
        product.setMinAmount(1000);
        product.setMaxAmount(100000);
        product.setTerm(12);
        return product;
    }

    public static com.ltqtest.springbootquickstart.product.entity.Product newProduct(
            String name, int userId) {
        com.ltqtest.springbootquickstart.product.entity.Product product =
                new com.ltqtest.springbootquickstart.product.entity.Product();
        product.setProductName(name);
        product.setPrice(12.5);
        product.setProducer("测试农场");
        product.setSalesVolume(0);
        product.setProductImg("/uploads/demo.jpg");
        product.setSurplus(100);
        product.setUserId(userId);
        product.setTotalVolumn(100);
        product.setStatus(1);
        return product;
    }
}
