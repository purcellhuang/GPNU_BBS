package com.gpnu.bbs.interceptor;

import com.gpnu.bbs.mapper.UserMapper;
import com.gpnu.bbs.model.HostHolder;
import com.gpnu.bbs.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @Author: PurcellHuang
 * @Date: 2019-07-24 10:55
 */
@Component
public class PassportInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name = "redisTemplate")
    private ValueOperations string;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("preHandle: -------------------------------");
        String ticket = null;
        if(request.getCookies()!=null){
            for(Cookie cookie: request.getCookies()){
                if(cookie.getName().equals("ticket")){
                    ticket = cookie.getValue();
                    break;
                }
            }
        }
        if (ticket != null){    //判断user的ticket是否一致及有效期
            User u = (User) request.getAttribute("user");
            String key = User.getTicketKey(u.getId());
            if(redisTemplate.hasKey(key)){
                if (ticket.equals(string.get(key))){
                    User user = userMapper.selectByPrimaryKey(u.getId());
                    redisTemplate.expire(key,3, TimeUnit.DAYS);
                    hostHolder.setUser(user);
                    return true;
                }
            }

        }
        response.sendRedirect("/login.html");
        return false;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("postHandle: -------------------------------");

        if(modelAndView!=null && hostHolder.getUser()!=null){
            modelAndView.addObject("user",hostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("afterCompletion: -------------------------------");

        hostHolder.clear();

    }
}
