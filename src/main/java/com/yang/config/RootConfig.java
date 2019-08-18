package com.yang.config;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;
import java.util.Properties;


@Configuration
//定义Spring扫描的包
@ComponentScan(value = "com.*", includeFilters = {@Filter(type = FilterType.ANNOTATION, value = {Service.class})})
//使用事务驱动管理器
@EnableTransactionManagement
//实现接口TransactionManagementConfigurer,这样可以配置注解驱动事务
public class RootConfig implements TransactionManagementConfigurer {

    private DataSource dataSource = null;

    @Bean(name = "dataSource")
    public DataSource initDataSource() {
        if (dataSource != null) {
            return dataSource;
        }
        Properties props = new Properties();
        props.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
        props.setProperty("url", "jdbc:mysql://localhost:3306/db_test?useSSL=false&serverTimezone=UTC");
        props.setProperty("username", "root");
        props.setProperty("password", "root_wy");
        props.setProperty("maxActive", "200");
        props.setProperty("maxIdle", "20");
        props.setProperty("maxWait", "30000");
        try {
            dataSource = BasicDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource;
    }


    /**
     * 配置SqlSessionFactoryBean
     *
     * @return SqlSessionFactoryBean
     */
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactoryBean initSqlSessionFactory() {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(initDataSource());
        //配置MyBatis配置文件
        Resource resource = new ClassPathResource("/mybatis/mybatis-config.xml");
        sqlSessionFactory.setConfigLocation(resource);
        return sqlSessionFactory;
    }

    /**
     * 通过自动扫描，发现 MyBatis Mapper接口
     *
     * @return Mapper扫描器
     */
    @Bean
    public MapperScannerConfigurer initMapperScannerConfigurer() {
        MapperScannerConfigurer msc = new MapperScannerConfigurer();
        msc.setBasePackage("com.*");
        msc.setSqlSessionFactoryBeanName("sqlSessionFactory");
        msc.setAnnotationClass(Repository.class);
        return msc;
    }

    /**
     * 实现接口方法，注册注解事务，当@Transactional使用的时候产生数据库事务
     */
    @Override
    @Bean(name = "annotationDrivenTransactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(initDataSource());
        return transactionManager;
    }
}
