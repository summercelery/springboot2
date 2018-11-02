package springboot.core.shiro;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springboot.core.redis.RedisSessionDao;
import springboot.listener.CustomSessionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;


@Configuration
public class ShiroConf {

    //配置自定义的密码比较器
    @Bean(name = "credentialsMatcher")
    public CredentialsMatcher credentialsMatcher() {
        return new CredentialsMather();
    }

    //配置自定义的权限登录器
    @Bean(name = "loginNameRealm")
    public LoginNameRealm loginNameRealm() {
        LoginNameRealm loginNameRealm = new LoginNameRealm();
        loginNameRealm.setCredentialsMatcher(new CredentialsMather());
        return loginNameRealm;
    }

    @Bean(name = "phoneRealm")
    public PhoneRealm phoneRealm() {
        return new PhoneRealm();
    }

    @Bean(name = "sessionManager")
    public ShiroSessionManager defaultWebSessionManager() {
        ShiroSessionManager sessionManager = new ShiroSessionManager();
        sessionManager.setSessionDAO(redisSessionDAO());//如不想使用REDIS可注释此行
        Collection<SessionListener> sessionListeners = new ArrayList<>();
        sessionListeners.add(customSessionListener());
        sessionManager.setSessionListeners(sessionListeners);
        //单位为毫秒（1秒=1000毫秒） 3600000毫秒为1个小时
        sessionManager.setSessionValidationInterval(3600000 * 12);
        //3600000 milliseconds = 1 hour
        sessionManager.setGlobalSessionTimeout(3600000 * 12);
        //是否删除无效的，默认也是开启
        sessionManager.setDeleteInvalidSessions(true);
        //是否开启 检测，默认开启
        sessionManager.setSessionValidationSchedulerEnabled(true);
        //创建会话Cookie
        Cookie cookie = new SimpleCookie(ShiroHttpSession.DEFAULT_SESSION_ID_NAME);
        cookie.setName("WEBID");
        cookie.setHttpOnly(true);
        sessionManager.setSessionIdCookie(cookie);
        return sessionManager;
    }

    //配置核心安全事务管理器
    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        CustomModularReamAuthenticator authenticator = new CustomModularReamAuthenticator();

        List<Realm> realmlist = new ArrayList<>();
        realmlist.add(loginNameRealm());
        realmlist.add(phoneRealm());
        authenticator.setRealms(realmlist);

        manager.setSessionManager(defaultWebSessionManager());
        manager.setRealm(loginNameRealm());
        manager.setRealm(phoneRealm());
        manager.setAuthenticator(authenticator);
        return manager;
    }

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") DefaultWebSecurityManager manager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(manager);
        //配置登录的url和登录成功的url
        bean.setLoginUrl("/login.html");
        bean.setSuccessUrl("/home");
        //配置访问权限
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/login.html", "anon"); //表示可以匿名访问
        filterChainDefinitionMap.put("/register.html", "anon");

//        filterChainDefinitionMap.put("/loginUser", "anon");
//        filterChainDefinitionMap.put("/logout*","anon");
//        filterChainDefinitionMap.put("/jsp/error.jsp*","anon");
//        filterChainDefinitionMap.put("/jsp/index.jsp*","authc");
        filterChainDefinitionMap.put("/*", "anon");//表示需要认证才可以访问
        filterChainDefinitionMap.put("/**", "anon");//"authc"表示需要认证才可以访问
        filterChainDefinitionMap.put("/*.*", "anon");
        bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return bean;
    }

    @Bean(name = "redisSessionDAO")
    public RedisSessionDao redisSessionDAO() {
        return new RedisSessionDao();
    }

    @Bean(name = "customSessionListener")
    public CustomSessionListener customSessionListener() {
        return new CustomSessionListener();
    }


    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    //加入注解的使用，不加入这个注解不生效
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(org.apache.shiro.mgt.SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }


}
