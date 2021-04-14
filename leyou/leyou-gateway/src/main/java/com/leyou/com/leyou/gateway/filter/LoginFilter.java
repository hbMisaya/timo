package com.leyou.com.leyou.gateway.filter;

import com.ctc.wstx.util.StringUtil;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;


    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        //获取资源文件中的白名单
        List<String> allowPaths = this.filterProperties.getAllowPaths ();
        //获取当前请求路径
        RequestContext currentContext = RequestContext.getCurrentContext ();
        HttpServletRequest request = currentContext.getRequest ();
        String url = request.getRequestURL ().toString ();
        for (String allowPath : allowPaths) {
            if(StringUtils.contains(url,allowPath)){
                return false;
            }
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //初始化zuul网关运行上下文
        RequestContext currentContext = RequestContext.getCurrentContext ();
        //获取Request对象
        HttpServletRequest request = currentContext.getRequest ();
        //获取cookie值
        String token = CookieUtils.getCookieValue (request, this.jwtProperties.getCookieName ());
//        if (StringUtils.isBlank (token)){
//            currentContext.setSendZuulResponse (false);
//            currentContext.setResponseStatusCode (HttpStatus.UNAUTHORIZED.value ());
//        }
        try {
            JwtUtils.getInfoFromToken (token, this.jwtProperties.getPublicKey ());
        } catch (Exception e) {
            e.printStackTrace ();
            currentContext.setSendZuulResponse (false);
            currentContext.setResponseStatusCode (HttpStatus.UNAUTHORIZED.value ());
        }
        return null;
    }
}
