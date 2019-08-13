package com.yang.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;

public class WebAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    //Spring IoC环境配置
    @Override
    protected Class<?>[] getRootConfigClasses() {
        //配置Spring IoC资源
        //return new Class[0];
        return new Class[]{RootConfig.class};
    }

    //DispatcherServlet环境配置
    @Override
    protected Class<?>[] getServletConfigClasses() {
        //加载Java配置类
        return new Class[]{WebConfig.class};
    }

    //DispatcherServlet拦截请求配置
    @Override
    protected String[] getServletMappings() {
        return new String[]{"*.do"};
    }


    /**
     * @param dynamic Servlet上传文件配置
     */
    protected void customizeRegistration(Dynamic dynamic) {

        //配置上传文件路径
        String filePath = "d:/mvc/uploads";

        //5MB
        Long singleMax = (long) (5 * Math.pow(2, 20));

        //10MB
        Long totleMax = (long) (10 * Math.pow(2, 20));

        //设置上传文件配置
        dynamic.setMultipartConfig(new MultipartConfigElement(filePath,
                singleMax, totleMax, 0));
    }
}
