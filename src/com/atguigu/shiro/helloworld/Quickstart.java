package com.atguigu.shiro.helloworld;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple Quickstart application showing how to use Shiro's API.
 *
 * @since 0.9 RC2
 */
public class Quickstart {

    private static final transient Logger log = LoggerFactory.getLogger(Quickstart.class);


    public static void main(String[] args) {

        // The easiest way to create a Shiro SecurityManager with configured
        // realms, users, roles and permissions is to use the simple INI config.
        // We'll do that by using a factory that can ingest a .ini file and
        // return a SecurityManager instance:

        // Use the shiro.ini file at the root of the classpath
        // (file: and url: prefixes load from files and urls respectively):
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();

        // for this simple example quickstart, make the SecurityManager
        // accessible as a JVM singleton.  Most applications wouldn't do this
        // and instead rely on their container configuration or web.xml for
        // webapps.  That is outside the scope of this simple quickstart, so
        // we'll just do the bare minimum so you can continue to get a feel
        // for things.
        SecurityUtils.setSecurityManager(securityManager);

        // Now that a simple Shiro environment is set up, let's see what you can do:

        // get the currently executing user:
        // 获取和 Shiro 交互的当前的 Subject 对象: 调用 SecurityUtils.getSubject() 方法. 
        Subject currentUser = SecurityUtils.getSubject();

        // Do some stuff with a Session (no need for a web or EJB container!!!)
        // 测试使用 Session. 了解. 
        Session session = currentUser.getSession();
        session.setAttribute("someKey", "aValue");
        String value = (String) session.getAttribute("someKey");
        if (value.equals("aValue")) {
            log.info("--> Retrieved the correct value! [" + value + "]");
        }

        // let's login the current user so we can check against roles and permissions:
        // 验证当前用户是否已经被认证. 即是否登陆. 调用 Subject 的 isAuthenticated() 方法. 
        if (!currentUser.isAuthenticated()) {
        	// 把用户名和密码封装为一个 UsernamePasswordToken 对象!
            UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
            token.setRememberMe(true);
            try {
            	// 执行认证操作. 调用 Subject 的 login(UsernamePasswordToken); 方法来完成登录. 
                currentUser.login(token);
            } 
            // 若用户名不存在, 则会抛出 UnknownAccountException 异常. 
            catch (UnknownAccountException uae) {
                log.info("--> There is no user with username of " + token.getPrincipal());
                return;
            } 
            // 用户名存在, 但和密码不匹配, 则会抛出 IncorrectCredentialsException 异常. 
            catch (IncorrectCredentialsException ice) {
                log.info("--> Password for account " + token.getPrincipal() + " was incorrect!");
                return;
            } 
            // 若该用户被锁定. 则可以自抛出 LockedAccountException
            catch (LockedAccountException lae) {
                log.info("The account for username " + token.getPrincipal() + " is locked.  " +
                        "Please contact your administrator to unlock it.");
            }
            // ... catch more exceptions here (maybe custom ones specific to your application?
            // 其他的认证异常. 实际上上述的一次都是 AuthenticationException 的子类
            // 也可以自定义 AuthenticationException 的子类. 然后由程序抛出
            catch (AuthenticationException ae) {
                //unexpected condition?  error?
            }
        }

        //say who they are:
        //print their identifying principal (in this case, a username):
        log.info("--> User [" + currentUser.getPrincipal() + "] logged in successfully.");

        //test a role:
        // 检测用户是否具备某一个角色. 
        if (currentUser.hasRole("schwartz")) {
            log.info("--> May the Schwartz be with you!");
        } else {
            log.info("Hello, mere mortal.");
        }

        //test a typed permission (not instance-level)
        // 检测用户是否具备某一个具体的行为. 即能否具体做某一个操作. 
        if (currentUser.isPermitted("lightsaber:weild")) {
            log.info("--> You may use a lightsaber ring.  Use it wisely.");
        } else {
            log.info("Sorry, lightsaber rings are for schwartz masters only.");
        }

        //a (very powerful) Instance Level permission:
        // 检测用户是否可以进行更加细粒度的操作: 可以对 winnebago 实体的 eagle5 实例进行 drive
        if (currentUser.isPermitted("winnebago:drive:eagle5")) {
            log.info("--> You are permitted to 'drive' the winnebago with license plate (id) 'eagle5'.  " +
                    "Here are the keys - have fun!");
        } else {
            log.info("Sorry, you aren't allowed to drive the 'eagle5' winnebago!");
        }

        //all done - log out!
        // 登出. 
        currentUser.logout();

        System.exit(0);
    }
}
